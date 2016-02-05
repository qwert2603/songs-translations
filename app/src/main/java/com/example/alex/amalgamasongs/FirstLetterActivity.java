package com.example.alex.amalgamasongs;

import android.app.Fragment;

public class FirstLetterActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new FirstLetterFragment();
    }

}