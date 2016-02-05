package com.example.alex.amalgamasongs.json_serialize;

import org.json.JSONException;
import org.json.JSONObject;

public interface JSONSerializable {

    /**
     * Преобразование объекта в JSONObject (сериализация)
     */
    JSONObject toJSON() throws JSONException;

    /**
     * Метод-фабрика.
     * Создание объекта сериализуемого класса из JSONObject объекта
     */
    JSONSerializable newInstance(JSONObject object) throws JSONException;

}