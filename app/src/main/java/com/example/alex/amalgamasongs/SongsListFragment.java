package com.example.alex.amalgamasongs;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.alex.amalgamasongs.entity.Artist;
import com.example.alex.amalgamasongs.entity.Song;

import java.util.ArrayList;

public class SongsListFragment extends Fragment {

    private static final String artistKey = "artistKey";
    private static final String PREF_SHOW_CNT = "show_cnt";
    private static final String PREF_SHOW_TOAST = "show_toast";

    private Artist mArtist;
    private ListView mListView;
    private ProgressBar mProgressBar;

    private SearchView mSearchView;

    private ArrayList<Song> mThisArtistSongs;
    private ArrayList<Song> mShowingSongs;
    private String mQuery;

    public static SongsListFragment newInstance(Artist artist) {
        SongsListFragment result = new SongsListFragment();
        Bundle args = new Bundle();
        args.putSerializable(artistKey, artist);
        result.setArguments(args);
        return result;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mArtist = (Artist) getArguments().getSerializable(artistKey);

        mThisArtistSongs = Song.loadSongsList(getActivity(), mArtist);
        mShowingSongs = new ArrayList<>();

        // независимо от того, есть ли список песен этого исполнителя в кеше, обновляем его
        new FetchSongsTask().execute(mArtist.getLink());

        // если пользователь еще не нажимал на пункт меню со звездочкой, просим его поставить оценку
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        boolean show_toast = sp.getBoolean(PREF_SHOW_TOAST, true);
        if (show_toast) {
            int q = sp.getInt(PREF_SHOW_CNT, -1);
            ++q;
            if (q == 6) {
                q = 0;
                Toast.makeText(getActivity(), R.string.text_rate_it, Toast.LENGTH_SHORT).show();
            }
            sp.edit()
                    .putInt(PREF_SHOW_CNT, q)
                    .apply();
        }
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        mListView = (ListView) view.findViewById(android.R.id.list);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(getActivity(), TranslationActivity.class);
                i.putExtra(TranslationActivity.EXTRA_ARTIST, mArtist);
                Song song = ((TextItemsAdapter<Song>) parent.getAdapter()).getItem(position);
                i.putExtra(TranslationActivity.EXTRA_SONG, song);
                startActivity(i);
            }
        });

        mProgressBar = (ProgressBar) view.findViewById(android.R.id.empty);

        if (!mThisArtistSongs.isEmpty()) {
            showList("", true);
        }

        return view;
    }

    private class FetchSongsTask extends AsyncTask<String, Void, ArrayList<Song>> {
        @Override
        protected ArrayList<Song> doInBackground(String... params) {
            ArrayList<Song> songs = Fetcher.fetchSongs(params[0]);

            if (getActivity() != null && !songs.isEmpty()) {
                // сохраняем список песен этого исполнителя
                Song.saveSongsList(getActivity(), mArtist, songs);
            }

            return songs;
        }

        @Override
        protected void onPostExecute(ArrayList<Song> songs) {
            if (getActivity() != null) {
                if (mThisArtistSongs.isEmpty() || !songs.isEmpty()) {
                    mThisArtistSongs = songs;
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
        mShowingSongs.clear();
        if (mQuery.equals("")) {
            // поиск не трубуется
            mShowingSongs.addAll(mThisArtistSongs);
        }
        else {
            // поиск не зависит от регистра
            String searchQuery = mQuery.toLowerCase();
            // ищем песни, соответствующих поисковому запросу
            for (int i = 0; i != mThisArtistSongs.size(); ++i) {
                if (mThisArtistSongs.get(i).getTitle().toLowerCase().startsWith(searchQuery)) {
                    mShowingSongs.add(mThisArtistSongs.get(i));
                }
            }
            for (int i = 0; i != mThisArtistSongs.size(); ++i) {
                if (mThisArtistSongs.get(i).getTitle().toLowerCase().contains(searchQuery)) {
                    if (! mShowingSongs.contains(mThisArtistSongs.get(i))) {
                        mShowingSongs.add(mThisArtistSongs.get(i));
                    }
                }
            }
        }
        if (mShowingSongs.isEmpty()) {
            Toast.makeText(getActivity(), R.string.text_nothing_found, Toast.LENGTH_SHORT).show();
        }
        if (mListView.getAdapter() == null) {
            TextItemsAdapter<Song> artistsAdapter = new TextItemsAdapter<>(getActivity(), R.layout.list_item, mShowingSongs);
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
        inflater.inflate(R.menu.menu_songs_list, menu);
        if (! PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean(PREF_SHOW_TOAST, true)) {
            menu.findItem(R.id.action_rate).setVisible(false);
        }

        MenuItem searchMenuItem = menu.findItem(R.id.action_search);
        mSearchView = (SearchView) searchMenuItem.getActionView();
        mSearchView.setSubmitButtonEnabled(false);
        mSearchView.setQueryHint(getString(R.string.text_search) + " " + mArtist.getName());
        mSearchView.setOnQueryTextListener(new android.widget.SearchView.OnQueryTextListener() {
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_rate) {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse("market://details?id=com.alex.amalgamasongs"));
            startActivity(i);

            // после первого нажатия перестаем показывать тосты
            PreferenceManager.getDefaultSharedPreferences(getActivity())
                    .edit()
                    .putBoolean(PREF_SHOW_TOAST, false)
                    .apply();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}