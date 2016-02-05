package com.example.alex.amalgamasongs;

import android.app.Fragment;
import android.text.Html;

import com.example.alex.amalgamasongs.entity.Artist;

public class SongsListActivity extends SingleFragmentActivity {

    public static final String EXTRA_ARTIST = "com.example.alex.amalgamasongs.SongsListActivity.EXTRA_ARTIST";

    @Override
    protected Fragment createFragment() {
        Artist artist = (Artist) getIntent().getSerializableExtra(EXTRA_ARTIST);
        setTitle(Html.fromHtml(artist.getName()));
        return SongsListFragment.newInstance(artist);
    }

}
