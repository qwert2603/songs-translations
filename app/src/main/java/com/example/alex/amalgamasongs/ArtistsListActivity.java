package com.example.alex.amalgamasongs;

import android.app.Fragment;

public class ArtistsListActivity extends SingleFragmentActivity {

    public static final String EXTRA_LETTER = "com.example.alex.amalgamasongs.ArtistsListActivity.EXTRA_LETTER";

    @Override
    protected Fragment createFragment() {
        String letter = getIntent().getStringExtra(EXTRA_LETTER);
        setTitle(letter);
        return ArtistsListFragment.newInstance(letter);
    }

}
