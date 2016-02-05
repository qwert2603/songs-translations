package com.example.alex.amalgamasongs;

import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.alex.amalgamasongs.entity.Artist;
import com.example.alex.amalgamasongs.entity.SavedSong;
import com.example.alex.amalgamasongs.entity.Song;
import com.example.alex.amalgamasongs.entity.Translation;

import java.util.ArrayList;

public class TranslationFragment extends Fragment {

    private static final String artistKey = "artistKey";
    private static final String songKey = "songKey";

    private Artist mArtist;
    private Song mSong;
    private Translation mTranslation = null;
    private TextView mSongTitleEng;
    private TextView mSongAuthorEng;
    private TextView mSongTextEng;
    private TextView mSongTitleRus;
    private TextView mSongAuthorRus;
    private TextView mSongTextRus;

    private FetchTranslationTask mFetchTranslationTask = new FetchTranslationTask();

    // номер с списке сохраненных
    private int mSavedIndex = -1;
    private ArrayList<SavedSong> mSavedSongs;

    public static TranslationFragment newInstance(Artist artist, Song song) {
        TranslationFragment result = new TranslationFragment();
        Bundle args = new Bundle();
        args.putSerializable(artistKey, artist);
        args.putSerializable(songKey, song);
        result.setArguments(args);
        return result;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);

        mArtist = (Artist) getArguments().getSerializable(artistKey);
        mSong = (Song) getArguments().getSerializable(songKey);

        mSavedSongs = SavedSong.getSavedSongs(getActivity());
        for(int i = 0; i < mSavedSongs.size(); ++i) {
            SavedSong ss = mSavedSongs.get(i);
            if(ss.getArtist().getName().equals(mArtist.getName())
                    && ss.getSong().getTitle().equals(mSong.getTitle())) {
                mSavedIndex = i;
                break;
            }
        }

        if (mSavedIndex < 0) {
            mFetchTranslationTask.execute(mArtist.getLink(), mSong.getLink());
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ((SingleFragmentActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        View view = inflater.inflate(R.layout.fragment_translation, container, false);

        mSongTitleEng = (TextView) view.findViewById(R.id.song_title_eng);
        mSongAuthorEng = (TextView) view.findViewById(R.id.song_author_eng);
        mSongTextEng = (TextView) view.findViewById(R.id.song_text_eng);
        mSongTitleRus = (TextView) view.findViewById(R.id.song_title_rus);
        mSongAuthorRus = (TextView) view.findViewById(R.id.song_author_rus);
        mSongTextRus = (TextView) view.findViewById(R.id.song_text_rus);

        mSongTitleEng.setText(Html.fromHtml(mSong.getTitle()));
        mSongAuthorEng.setText(Html.fromHtml("(" + getString(R.string.text_original) + " " + mArtist.getName() + ")"));

        /* todo: delete later
        mSongTitleEng.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SEARCH);
                intent.putExtra(SearchManager.QUERY, new SavedSong(mArtist, mSong, null).toString());
                startActivity(intent);
            }
        });*/

        if (mSavedIndex >= 0) {
            showSubtitle();
            mTranslation = mSavedSongs.get(mSavedIndex).getTranslation();
        }

        if(mTranslation != null) {
            showTranslation();
        }

        return view;
    }

    @SuppressWarnings("ConstantConditions")
    private void showSubtitle() {
        ((SingleFragmentActivity) getActivity()).getSupportActionBar().setSubtitle(R.string.text_was_saved);
    }

    private class FetchTranslationTask extends AsyncTask<String, Void, Translation> {
        @Override
        protected Translation doInBackground(String... params) {
            return Fetcher.fetchTranslation(mArtist.getLink(), mSong.getLink());
        }

        @Override
        protected void onPostExecute(Translation translation) {
            if(getActivity() != null) {
                mTranslation = translation;
                showTranslation();
            }
        }
    }

    private void showTranslation() {
        mSongTextEng.setText(Html.fromHtml(mTranslation.mEngText));
        mSongTitleRus.setText(Html.fromHtml(mTranslation.mRusTitle));
        mSongAuthorRus.setText(Html.fromHtml("(" + getString(R.string.text_translation) + " " + mTranslation.mRusAuthor + ")"));
        mSongTextRus.setText(Html.fromHtml(mTranslation.mRusText));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_translation, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getActivity().finish();
            return true;
        }
        else if (item.getItemId() == R.id.action_share) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, Fetcher.siteURL + mArtist.getLink() + mSong.getLink());
            intent = Intent.createChooser(intent, getString(R.string.text_share_via));
            startActivity(intent);
            return true;
        }
        else if (item.getItemId() == R.id.action_save) {
            if(mSavedIndex < 0
                    && mTranslation != null
                    && ! mTranslation.mEngText.isEmpty()) {
                mSavedIndex = SavedSong.addSongToSaved(getActivity(), new SavedSong(mArtist, mSong, mTranslation));
                showSubtitle();
            }
        }
        return super.onOptionsItemSelected(item);
    }

}