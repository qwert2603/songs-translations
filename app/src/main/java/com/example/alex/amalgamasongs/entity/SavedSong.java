package com.example.alex.amalgamasongs.entity;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.example.alex.amalgamasongs.json_serialize.ArrayListJSONSerializer;
import com.example.alex.amalgamasongs.json_serialize.JSONSerializable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Класс, представляющий собой полноценный перевод песни.
 * Содержит объект исполнителя, песни и перевода.
 * А заодно сожержит несколько статических методов,
 * которые управляют сохранением переводов в память телефона.
 * При добавлении или удалении отлельного перевода, а также при их полной очистке
 * изменения немедленно заносятся в память телефона.
 * Этот класс реализует интерфейс JSONSerializable,
 * поэтому он может превращаться в JSONObject и создавать себе подобных из JSONObject.
 */
@SuppressWarnings("unused")
public class SavedSong implements Serializable, JSONSerializable {

    private static ArrayList<SavedSong> sSavedSongs = null;

    private static void checkSavedSongsLoaded(Context context) {
        if(sSavedSongs == null) {
            sSavedSongs = new ArrayListJSONSerializer<SavedSong>()
                    .loadArrayList(
                            context
                            , ArrayListJSONSerializer.SAVED_SONGS_FILENAME
                            , new SavedSong());
            checkN_2_BR(context);
        }
    }

    public static ArrayList<SavedSong> getSavedSongs(Context context) {
        checkSavedSongsLoaded(context);
        return sSavedSongs;
    }

    private static void saveSavedSongs(Context context){
        checkSavedSongsLoaded(context);
        new ArrayListJSONSerializer<SavedSong>()
                .saveArrayList(
                        context
                        , ArrayListJSONSerializer.SAVED_SONGS_FILENAME
                        , sSavedSongs);
    }

    public static int addSongToSaved(Context context, SavedSong savedSong) {
        checkSavedSongsLoaded(context);
        sSavedSongs.add(savedSong);
        saveSavedSongs(context);
        return sSavedSongs.size() - 1;
    }

    public static void removeSongFromSaved(Context context, int index) {
        checkSavedSongsLoaded(context);
        sSavedSongs.remove(index);
        saveSavedSongs(context);
    }

    /**
     * Удалить из сохраненных все песни, которые были переданы в songsToRemove
     * @param songsToRemove список песен для удаления
     */
    public static void removeSongsFromSaved(Context context, Collection<SavedSong> songsToRemove) {
        checkSavedSongsLoaded(context);
        sSavedSongs.removeAll(songsToRemove);
        saveSavedSongs(context);
    }

    public static void clearSavedSongs(Context context) {
        checkSavedSongsLoaded(context);
        sSavedSongs.clear();
        saveSavedSongs(context);
    }

    /**
     * Была ли выполнена замена "\n" на "<br/>" во всех ранее сохраненных переводах.
     */
    private static final String PREF_N_2_BR = "PREF_N_2_BR";    // bool

    /**
     * При первом запуске меняем "\n" на "<br/>" во всех ранее сохраненных переводах.
     * Это необходимо для корректного отображения с использованием 'Html.fromHtml()'.
     */
    private static void checkN_2_BR(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (! sharedPreferences.getBoolean(PREF_N_2_BR, false)) {
            for (SavedSong savedSong : sSavedSongs) {
                Translation translation = savedSong.getTranslation();
                translation.mEngText = translation.mEngText.replace("\n", "<br/>");
                translation.mRusText = translation.mRusText.replace("\n", "<br/>");
            }
            sharedPreferences.edit()
                    .putBoolean(PREF_N_2_BR, true)
                    .apply();
        }
    }

    private final static String JSON_ARTIST = "artist";
    private final static String JSON_SONG = "song";
    private final static String JSON_TRANSLATION = "translation";

    private Artist mArtist;
    private Song mSong;
    private Translation mTranslation;

    private SavedSong() {
    }

    public SavedSong(Artist artist, Song song, Translation translation) {
        mArtist = artist;
        mSong = song;
        mTranslation = translation;
    }

    protected SavedSong(JSONObject object) throws JSONException{
        mArtist = new Artist(object.getJSONObject(JSON_ARTIST));
        mSong = new Song(object.getJSONObject(JSON_SONG));
        mTranslation = new Translation(object.getJSONObject(JSON_TRANSLATION));
    }

    public void setArtist(Artist artist ) {
        mArtist = artist;
    }

    public Artist getArtist() {
        return mArtist;
    }

    public void setSong(Song song) {
        mSong = song;
    }

    public Song getSong() {
        return mSong;
    }

    public void setTranslation(Translation translation) {
        mTranslation = translation;
    }

    public Translation getTranslation() {
        return mTranslation;
    }

    @Override
    public String toString() {
        return mArtist.getName() + " - " + mSong.getTitle();
    }

    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject object = new JSONObject();

        object.put(JSON_ARTIST, mArtist.toJSON());
        object.put(JSON_SONG, mSong.toJSON());
        object.put(JSON_TRANSLATION, mTranslation.toJSON());

        return object;
    }

    @Override
    public SavedSong newInstance(JSONObject object) throws JSONException {
        return new SavedSong(object);
    }
}