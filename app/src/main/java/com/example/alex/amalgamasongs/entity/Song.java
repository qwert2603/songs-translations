package com.example.alex.amalgamasongs.entity;

import android.content.Context;

import com.example.alex.amalgamasongs.json_serialize.ArrayListJSONSerializer;
import com.example.alex.amalgamasongs.json_serialize.JSONSerializable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Смотри комментарии к классу Artist.
 * Тот и этот подобны.
 */
@SuppressWarnings("unused")
public class Song implements Serializable, JSONSerializable {

    private static HashMap<Artist, ArrayList<Song>> sSavedSongsMap = new HashMap<>();

    public static ArrayList<Song> loadSongsList(Context context, Artist artist) {
        if (sSavedSongsMap.get(artist) == null) {
            sSavedSongsMap.put(artist, new ArrayListJSONSerializer<Song>()
                    .loadArrayList(
                            context
                            , (ArrayListJSONSerializer.SAVED_SONGS_LIST_FILENAME + artist)
                            , new Song()));
        }
        return sSavedSongsMap.get(artist);
    }

    public static void saveSongsList(Context context, Artist artist, ArrayList<Song> songs) {
        sSavedSongsMap.put(artist, songs);
        new ArrayListJSONSerializer<Song>()
                .saveArrayList(
                        context
                        , (ArrayListJSONSerializer.SAVED_SONGS_LIST_FILENAME + artist)
                        , songs);
    }

    public static void clearSavedSongsLists(Context context) {
        sSavedSongsMap.clear();
        new ArrayListJSONSerializer<>().deleteSongsLists(context);
    }

    private final static String JSON_TITLE = "title";
    private final static String JSON_LINK = "link";

    private String mTitle;
    private String mLink;

    private Song() {
    }

    public Song(String title, String link) {
        mTitle = title;
        mLink = link;
    }

    protected Song(JSONObject object) throws JSONException {
        mTitle = object.getString(JSON_TITLE);
        mLink = object.getString(JSON_LINK);
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setLink(String link) {
        mLink = link;
    }

    public String getLink() {
        return mLink;
    }

    @Override
    public String toString() {
        return mTitle;
    }

    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject object = new JSONObject();
        object.put(JSON_TITLE, mTitle);
        object.put(JSON_LINK, mLink);
        return object;
    }

    @Override
    public Song newInstance(JSONObject object) throws JSONException {
        return new Song(object);
    }
}