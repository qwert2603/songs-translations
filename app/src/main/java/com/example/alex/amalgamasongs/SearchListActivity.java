package com.example.alex.amalgamasongs;

import android.app.Fragment;
import android.app.SearchManager;
import android.content.Intent;

public class SearchListActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new SearchListFragment();
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            SearchListFragment fragment = (SearchListFragment)
                    getFragmentManager().findFragmentById(R.id.fragment_container);
            fragment.setShowingQuery(query);
        }
    }

}