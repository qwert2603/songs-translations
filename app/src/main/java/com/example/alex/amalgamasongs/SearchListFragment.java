package com.example.alex.amalgamasongs;

import android.app.Activity;
import android.app.Fragment;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.alex.amalgamasongs.entity.Artist;
import com.example.alex.amalgamasongs.entity.SearchResult;
import com.example.alex.amalgamasongs.entity.Song;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

public class SearchListFragment extends Fragment {

    private ListView mListView;
    private ProgressBar mProgressBar;
    private String mQuery = null;
    private SearchView mSearchView = null;
    private ArrayList<SearchResult> mResults = new ArrayList<>();
    private volatile boolean mIsSearching = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        mListView = (ListView) view.findViewById(android.R.id.list);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SearchResult searchResult = mResults.get(position);
                Intent intent;
                if (searchResult.mIsSong) {
                    intent = new Intent(getActivity(), TranslationActivity.class);
                    intent.putExtra(TranslationActivity.EXTRA_ARTIST, searchResult.mArtist);
                    intent.putExtra(TranslationActivity.EXTRA_SONG, searchResult.mSong);
                } else {
                    intent = new Intent(getActivity(), SongsListActivity.class);
                    intent.putExtra(SongsListActivity.EXTRA_ARTIST, searchResult.mArtist);
                }
                startActivity(intent);
            }
        });

        mProgressBar = (ProgressBar) view.findViewById(android.R.id.empty);

        updateUI();

        return view;
    }

    public void setShowingQuery(@NonNull String query) {
        mQuery = query;
        if (mSearchView != null) {
            mSearchView.setQuery(mQuery, false);
        }
        mIsSearching = true;
        updateUI();
        new SearchTask().execute(mQuery);
    }

    private class SearchTask extends AsyncTask<String, Void, ArrayList<SearchResult>> {
        private String RESULT_URL_PREFIX = "http://www.amalgama-lab.com/songs/";

        @Override
        protected ArrayList<SearchResult> doInBackground(String... params) {
            ArrayList<SearchResult> result = new ArrayList<>();
            try {
                String query = params[0].replace(" ", "%20");
                String urlString = "http://ajax.googleapis.com/ajax/services/search/web?v=1.0&rsz=8&q=%s+site:www.amalgama-lab.com";
                URL url = new URL(String.format(urlString, query));
                InputStreamReader inputStreamReader = new InputStreamReader(url.openStream());
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader, 8192);
                String jsonString = "";
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    jsonString += line;
                }
                JSONObject jsonObject = new JSONObject(jsonString);
                JSONObject jsonObjectResponseData = jsonObject.getJSONObject("responseData");
                JSONArray jsonArray = jsonObjectResponseData.getJSONArray("results");
                for (int i = 0; i != jsonArray.length(); ++i) {
                    JSONObject oneResult = jsonArray.getJSONObject(i);
                    String resultedULR = oneResult.getString("url");
                    if (resultedULR.startsWith(RESULT_URL_PREFIX) && resultedULR.length() > RESULT_URL_PREFIX.length() + 2) {
                        SearchResult searchResult = parseSearchResult(resultedULR);
                        if (searchResult != null) {
                            result.add(searchResult);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }

        @Nullable
        private SearchResult parseSearchResult(String url) throws Exception {
            Context context = getActivity();
            if (context == null) {
                return null;
            }
            url = url.substring(RESULT_URL_PREFIX.length() + 2);
            String letter = String.valueOf(url.charAt(0));
            String artistLink = "/songs/" + letter + '/' + url.substring(0, url.indexOf('/') + 1);
            boolean isSong = false;

            Artist artist = null;
            ArrayList<Artist> artistsList = Artist.loadArtistsList(context, letter);
            if (artistsList.isEmpty()) {
                artistsList = Fetcher.fetchArtists(letter);
                Artist.saveArtistsList(context, letter, artistsList);
            }
            for (Artist a : artistsList) {
                if (artistLink.equals(a.getLink())) {
                    artist = a;
                    break;
                }
            }
            if (artist == null) {
                return null;
            }

            Song song = null;
            if (url.endsWith(".html")) {
                isSong = true;
                String songLink = url.substring(url.lastIndexOf('/') + 1, url.length());
                ArrayList<Song> songsList = Song.loadSongsList(context, artist);
                if (songsList.isEmpty()) {
                    songsList = Fetcher.fetchSongs(artistLink);
                    Song.saveSongsList(context, artist, songsList);
                }
                for (Song s : songsList) {
                    if (songLink.equals(s.getLink())) {
                        song = s;
                        break;
                    }
                }
                if (song == null) {
                    return null;
                }
            }

            return new SearchResult(isSong, artist, song);
        }

        @Override
        protected void onPostExecute(ArrayList<SearchResult> resultedSongs) {
            mResults = resultedSongs;
            mIsSearching = false;
            if (getActivity() == null) {
                return;
            }
            updateUI();
            if (mResults.isEmpty()) {
                Toast.makeText(getActivity(), R.string.text_nothing_found, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateUI() {
        if (mListView != null) {
            ResultItemsAdapter adapter = new ResultItemsAdapter(getActivity(), R.layout.list_item_search, mResults);
            mListView.setAdapter(adapter);
        }
        if (mProgressBar != null) {
            mProgressBar.setVisibility(mIsSearching ? View.VISIBLE : View.INVISIBLE);
        }
    }

    private static class ResultItemsAdapter extends ArrayAdapter<SearchResult> {
        private Activity mActivity;
        private int mLayoutRes;

        public ResultItemsAdapter(Activity activity, int layoutRes, ArrayList<SearchResult> items) {
            super(activity, 0, items);
            mLayoutRes = layoutRes;
            mActivity = activity;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mActivity.getLayoutInflater().inflate(mLayoutRes, parent, false);
            }

            SearchResult searchResult = getItem(position);

            TextView textView = (TextView) convertView.findViewById(R.id.item_text_view);
            textView.setText(Html.fromHtml(searchResult.toString()));

            ImageView imageView = (ImageView) convertView.findViewById(R.id.item_image_view);
            imageView.setImageResource(searchResult.getDrawableRes());

            return convertView;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_search_list, menu);

        MenuItem searchMenuItem = menu.findItem(R.id.action_search);
        mSearchView = (SearchView) searchMenuItem.getActionView();
        ComponentName componentName = getActivity().getComponentName();
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        SearchableInfo searchableInfo = searchManager.getSearchableInfo(componentName);
        mSearchView.setSearchableInfo(searchableInfo);

        if (mQuery != null) {
            mSearchView.setQuery(mQuery, false);
        }
    }

}