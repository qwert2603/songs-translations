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
 * Класс, представляющий собой исполнителя.
 * Содежрит название исполнителя и ссылку на него.
 * А заодно сожержит несколько статических методов,
 * которые управляют сохранением списков испонителей в память телефона.
 * При сохранении списка исполнителей он немедленно сохраняется в память.
 * Очиста списков тоже происходит немедленно.
 * Этот класс реализует интерфейс JSONSerializable,
 * поэтому он может превращаться в JSONObject и создавать себе подобных из JSONObject.
 */
@SuppressWarnings("unused")
public class Artist implements Serializable, JSONSerializable {

    private static HashMap<String, ArrayList<Artist>> sSavedArtistsMap = new HashMap<>();

    public static ArrayList<Artist> loadArtistsList(Context context, String letter) {
        letter = letter.toLowerCase();
        if (sSavedArtistsMap.get(letter) == null) {
            sSavedArtistsMap.put(letter, new ArrayListJSONSerializer<Artist>()
                    .loadArrayList(
                            context
                            , (ArrayListJSONSerializer.SAVED_ARTISTS_LIST_FILENAME + letter)
                            , new Artist()));
        }
        return sSavedArtistsMap.get(letter);
    }

    public static void saveArtistsList(Context context, String letter, ArrayList<Artist> artists) {
        letter = letter.toLowerCase();
        sSavedArtistsMap.put(letter, artists);
        new ArrayListJSONSerializer<Artist>()
                .saveArrayList(
                        context
                        , (ArrayListJSONSerializer.SAVED_ARTISTS_LIST_FILENAME + letter)
                        , artists);
    }

    public static void clearSavedArtistsLists(Context context) {
        sSavedArtistsMap.clear();
        new ArrayListJSONSerializer<>().deleteArtistsLists(context);
    }

    private final static String JSON_NAME = "name";
    private final static String JSON_LINK = "link";

    private String mName;
    private String mLink;

    private Artist(){
    }

    public Artist(String name, String link) {
        mName = name;
        mLink = link;
    }

    protected Artist(JSONObject object) throws JSONException {
        mName = object.getString(JSON_NAME);
        mLink = object.getString(JSON_LINK);
    }

    public void setName(String name) {
        mName = name;
    }

    public String getName() {
        return mName;
    }

    public void setLink(String link) {
        mLink = link;
    }

    public String getLink() {
        return mLink;
    }

    @Override
    public String toString() {
        return mName;
    }

    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject object = new JSONObject();
        object.put(JSON_NAME, mName);
        object.put(JSON_LINK, mLink);
        return object;
    }

    @Override
    public Artist newInstance(JSONObject object) throws JSONException {
        return new Artist(object);
    }
}