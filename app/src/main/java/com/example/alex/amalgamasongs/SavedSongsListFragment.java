package com.example.alex.amalgamasongs;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.alex.amalgamasongs.entity.SavedSong;

import java.util.ArrayList;

public class SavedSongsListFragment extends Fragment {

    private ListView mListView;
    private ArrayList<SavedSong> mSavedSongs;
    private TextItemsAdapter<SavedSong> mAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_saved_songs_list, container, false);

        mListView = (ListView) view.findViewById(android.R.id.list);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(getActivity(), TranslationActivity.class);
                SavedSong ss = mSavedSongs.get(position);
                i.putExtra(TranslationActivity.EXTRA_ARTIST, ss.getArtist());
                i.putExtra(TranslationActivity.EXTRA_SONG, ss.getSong());
                startActivity(i);
            }
        });

        mSavedSongs = SavedSong.getSavedSongs(getActivity());
        mAdapter = new TextItemsAdapter<>(getActivity(), R.layout.list_item_saved_song, mSavedSongs);
        mListView.setAdapter(mAdapter);

        mListView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        mListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                mode.setTitle(getString(R.string.text_selected) + mListView.getCheckedItemCount());
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.getMenuInflater().inflate(R.menu.menu_item_saved_song_list, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                ArrayList<SavedSong> songsToDelete = new ArrayList<>();
                if (item.getItemId() == R.id.action_delete_item) {
                    for (int i = mSavedSongs.size() - 1; i >= 0; --i) {
                        if (mListView.isItemChecked(i)) {
                            songsToDelete.add(mSavedSongs.get(i));
                        }
                    }
                    SavedSong.removeSongsFromSaved(getActivity(), songsToDelete);
                    mAdapter.notifyDataSetChanged();
                    mode.finish();
                    return true;
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
            }
        });

        if(mSavedSongs.isEmpty()) {
            Toast.makeText(getActivity(), R.string.text_no_saved_songs, Toast.LENGTH_LONG).show();
        }

        return view;
    }
}
