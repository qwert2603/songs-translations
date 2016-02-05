package com.example.alex.amalgamasongs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.example.alex.amalgamasongs.entity.Artist;
import com.example.alex.amalgamasongs.entity.SavedSong;
import com.example.alex.amalgamasongs.entity.Song;

import java.util.ArrayList;

public class FirstLetterFragment extends Fragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_first_letter, container, false);

        GridView gridView = (GridView) view.findViewById(R.id.letters_grid_view);
        TextItemsAdapter<String> lettersAdapter = new TextItemsAdapter<>(getActivity(), R.layout.list_item_letter, getLetters());
        gridView.setAdapter(lettersAdapter);
        gridView.setOnItemClickListener((parent, view1, position, id) -> {
            Intent i = new Intent(getActivity(), ArtistsListActivity.class);
            String letter = ((TextItemsAdapter<String>) parent.getAdapter()).getItem(position);
            i.putExtra(ArtistsListActivity.EXTRA_LETTER, letter);
            startActivity(i);
        });

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_first_letter, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_search) {
            Intent intent = new Intent(getActivity(), SearchListActivity.class);
            startActivity(intent);
            return true;
        }
        else if (item.getItemId() == R.id.action_open_saved) {
            Intent intent = new Intent(getActivity(), SavedSongsListActivity.class);
            startActivity(intent);
            return true;
        }
        else if (item.getItemId() == R.id.action_delete_lists) {
            Artist.clearSavedArtistsLists(getActivity());
            Song.clearSavedSongsLists(getActivity());
            View view = getView();
            if (view != null) {
                Snackbar.make(view, R.string.text_lists_cache_cleared, Snackbar.LENGTH_SHORT).show();
            }
            return true;
        }
        else if (item.getItemId() == R.id.action_delete_saved_songs) {
            new DialogFragment() {
                @NonNull
                @Override
                public Dialog onCreateDialog(Bundle savedInstanceState) {
                    return new AlertDialog.Builder(getActivity())
                            .setTitle(getString(R.string.text_clear_saved_songs_cache) + "?")
                            .setPositiveButton("OK", (dialog, which) -> {
                                SavedSong.clearSavedSongs(getActivity());
                                View view = FirstLetterFragment.this.getView();
                                if (view != null) {
                                    Snackbar.make(view, R.string.text_saved_songs_cache_cleared, Snackbar.LENGTH_SHORT).show();
                                }
                            })
                            .setNegativeButton(R.string.text_cancel, null)
                            .create();
                }
            }.show(getActivity().getFragmentManager(), "delete_saved_songsDialogFragment");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static ArrayList<String> getLetters() {
        ArrayList<String> result = new ArrayList<>();
        char c = 'A';
        while (c <= 'Z') {
            result.add(c + "");
            ++c;
        }
        c = '1';
        while (c <= '9') {
            result.add(c + "");
            ++c;
        }
        return result;
    }

}
