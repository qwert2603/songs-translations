package com.example.alex.amalgamasongs.entity;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * класс, содержащий русский и оригинальный тексты песни, ее русское название и автора перевода
 */
public class Translation {

    private static final String JSON_ENG_TEXT = "eng_text";
    private static final String JSON_RUS_TEXT = "rus_text";
    private static final String JSON_RUS_TITLE = "rus_title";
    private static final String JSON_RUS_AUTHOR = "rus_author";

    public String mEngText = "";
    public String mRusText = "";
    public String mRusTitle = "";
    public String mRusAuthor = "";

    public Translation() {}

    public Translation(JSONObject object) throws JSONException{
        mEngText = object.getString(JSON_ENG_TEXT);
        mRusText = object.getString(JSON_RUS_TEXT);
        mRusTitle = object.getString(JSON_RUS_TITLE);
        mRusAuthor = object.getString(JSON_RUS_AUTHOR);
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject object = new JSONObject();

        object.put(JSON_ENG_TEXT, mEngText);
        object.put(JSON_RUS_TEXT, mRusText);
        object.put(JSON_RUS_TITLE, mRusTitle);
        object.put(JSON_RUS_AUTHOR, mRusAuthor);

        return object;
    }
}