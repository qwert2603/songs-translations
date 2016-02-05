package com.example.alex.amalgamasongs;

import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;

import com.example.alex.amalgamasongs.entity.Artist;

import java.util.ArrayList;

public class ArtistsListFragment extends Fragment {

    private static final String letterKey = "letterKey";

    private String mLetter;
    private ListView mListView;
    private ProgressBar mProgressBar;

    private SearchView mSearchView;

    private ArrayList<Artist> mThisLetterArtists;
    private ArrayList<Artist> mShowingArtists;
    private String mQuery;

    public static ArtistsListFragment newInstance(String letter) {
        ArtistsListFragment result = new ArtistsListFragment();
        Bundle args = new Bundle();
        args.putString(letterKey, letter);
        result.setArguments(args);
        return result;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mLetter = getArguments().getString(letterKey);

        mThisLetterArtists = Artist.loadArtistsList(getActivity(), mLetter);
        mShowingArtists = new ArrayList<>();

        // независимо от того, есть ли список исполнителей на эту букву в кеше, обновляем его
        new FetchArtistsTask().execute(mLetter);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        mListView = (ListView) view.findViewById(android.R.id.list);

        mListView.setOnItemClickListener((parent, view1, position, id) -> {
            Intent i = new Intent(getActivity(), SongsListActivity.class);
            Artist artist = ((TextItemsAdapter<Artist>) parent.getAdapter()).getItem(position);
            i.putExtra(SongsListActivity.EXTRA_ARTIST, artist);
            startActivity(i);
        });

        mProgressBar = (ProgressBar) view.findViewById(android.R.id.empty);

        if(! mThisLetterArtists.isEmpty()) {
            showList("", true);
        }

        return view;
    }

    private class FetchArtistsTask extends AsyncTask<String, Void, ArrayList<Artist>> {
        @Override
        protected ArrayList<Artist> doInBackground(String... params) {
            ArrayList<Artist> artists = Fetcher.fetchArtists(params[0]);

            if (getActivity() != null && !artists.isEmpty()) {
                // сохраняем список исполнителей на эту букву
                Artist.saveArtistsList(getActivity(), mLetter, artists);
            }

            return artists;
        }

        @Override
        protected void onPostExecute(ArrayList<Artist> artists) {
            if (getActivity() != null) {
                if (mThisLetterArtists.isEmpty() || !artists.isEmpty()) {
                    mThisLetterArtists = artists;
                }

                // обновляем список с учетом текущего поискового запроса.
                // mSearchView может быть еще не создан.
                // Особенно, если нет подключения к интернету и скачивания списка вообще не было.
                String query = (mSearchView != null) ? (mSearchView.getQuery() + "") : "";
                showList(query, false);
            }
        }
    }

    // отобразить список с учетом поискового запроса
    @SuppressWarnings("unchecked")
    private void showList(String query, boolean scrollToBegin) {
        if (query.equalsIgnoreCase(mQuery)) {
            if (scrollToBegin) {
                mListView.setSelection(0);
            }
            return;
        }
        mQuery = query;
        mShowingArtists.clear();
        if (mQuery.equals("") || mQuery.equalsIgnoreCase(mLetter)) {
            // поиск не трубуется
            mShowingArtists.addAll(mThisLetterArtists);
        }
        else {
            // поиск не зависит от регистра
            String searchQuery = mQuery.toLowerCase();
            // ищем исполнителей, соответствующих поисковому запросу
            for (int i = 0; i != mThisLetterArtists.size(); ++i) {
                if (mThisLetterArtists.get(i).getName().toLowerCase().startsWith(searchQuery)) {
                    mShowingArtists.add(mThisLetterArtists.get(i));
                }
            }
            for (int i = 0; i != mThisLetterArtists.size(); ++i) {
                if (mThisLetterArtists.get(i).getName().toLowerCase().contains(searchQuery)) {
                    if (! mShowingArtists.contains(mThisLetterArtists.get(i))) {
                        mShowingArtists.add(mThisLetterArtists.get(i));
                    }
                }
            }
        }
        if (mShowingArtists.isEmpty()) {
            View view = getView();
            if (view != null) {
                Snackbar.make(view, R.string.text_nothing_found, Snackbar.LENGTH_SHORT).show();
            }
        }
        if (mListView.getAdapter() == null) {
            TextItemsAdapter<Artist> artistsAdapter = new TextItemsAdapter<>(getActivity(), R.layout.list_item, mShowingArtists);
            mListView.setAdapter(artistsAdapter);
        }
        else {
            ((TextItemsAdapter<Artist>) mListView.getAdapter()).notifyDataSetChanged();
            if (scrollToBegin) {
                mListView.setSelection(0);
            }
        }
        mProgressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_artists_list, menu);
        MenuItem searchMenuItem = menu.findItem(R.id.action_search);
        mSearchView = (SearchView) searchMenuItem.getActionView();
        mSearchView.setSubmitButtonEnabled(false);
        mSearchView.setQuery(mLetter, false);
        mSearchView.setQueryHint(getString(R.string.text_search) + " " + mLetter);
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                showList(newText, true);
                return true;
            }
        });
    }
}