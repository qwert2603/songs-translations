package com.example.alex.amalgamasongs.entity;

import android.support.annotation.DrawableRes;

import com.example.alex.amalgamasongs.R;

/**
 * Класс, сожержащий 1 результат поиска (Исполнитель или песня).
 */
public class SearchResult {

    @DrawableRes public static final int DRAWABLE_ARTIST = R.drawable.artist;
    @DrawableRes public static final int DRAWABLE_SONG = R.drawable.song;

    public final boolean mIsSong;
    public final Artist mArtist;
    public final Song mSong;

    public SearchResult(boolean isSong, Artist artist, Song song) {
        mIsSong = isSong;
        mArtist = artist;
        mSong = mIsSong ? song : null;
    }

    @DrawableRes
    public int getDrawableRes() {
        return mIsSong ? DRAWABLE_SONG : DRAWABLE_ARTIST;
    }

    @Override
    public String toString() {
        return mArtist + (mIsSong ? (" - " + mSong) : "");
    }
}
