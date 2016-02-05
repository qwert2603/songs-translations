package com.example.alex.amalgamasongs;

import android.app.Fragment;

public class SavedSongsListActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new SavedSongsListFragment();
    }

}
