package com.example.alex.amalgamasongs;

import android.app.Fragment;
import android.os.Bundle;
import android.text.Html;

import com.example.alex.amalgamasongs.entity.Artist;
import com.example.alex.amalgamasongs.entity.Song;

public class TranslationActivity extends SingleFragmentActivity {

    public static final String EXTRA_ARTIST = "com.example.alex.amalgamasongs.TranslationActivity.EXTRA_ARTIST";
    public static final String EXTRA_SONG = "com.example.alex.amalgamasongs.TranslationActivity.EXTRA_SONG";

    @Override
    protected Fragment createFragment() {
        Artist artist = (Artist) getIntent().getSerializableExtra(EXTRA_ARTIST);
        Song song = (Song) getIntent().getSerializableExtra(EXTRA_SONG);
        setTitle(Html.fromHtml(song.getTitle()));
        return TranslationFragment.newInstance(artist, song);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Song song = (Song) getIntent().getSerializableExtra(EXTRA_SONG);
        setTitle(Html.fromHtml(song.getTitle()));
    }
}