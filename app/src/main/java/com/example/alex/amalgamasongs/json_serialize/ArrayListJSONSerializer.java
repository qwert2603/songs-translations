package com.example.alex.amalgamasongs.json_serialize;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Класс для сериализации и десериализации объектов,
 * реализующих интерфейс JSONSerializable и объединенных в ArrayList.
 * @param <E> тип сериализуемых объектов
 */
public class ArrayListJSONSerializer<E extends JSONSerializable> {

    private static final String TAG = "ArrayListJSONSerializer";

    public static final String SAVED_SONGS_FILENAME = "saved_songs";
    public static final String SAVED_ARTISTS_LIST_FILENAME = "saved_artists_list_";
    public static final String SAVED_SONGS_LIST_FILENAME = "saved_songs_list_";

    private static final String FILENAME_SUFFIX = ".json";

    /**
     * Объект ex нужен для создания других объектов
     */
    @NonNull
    public ArrayList<E> loadArrayList(Context context, String filename, E ex) {
        ArrayList<E> result = new ArrayList<>();

        InputStream is = null;
        try {
            is = context.openFileInput(filename + FILENAME_SUFFIX);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder arrayString = new StringBuilder();
            String line;
            while((line = br.readLine()) != null) {
                arrayString.append(line);
            }
            JSONArray jsonArray = (JSONArray) new JSONTokener(arrayString.toString()).nextValue();
            for(int i = 0; i != jsonArray.length(); ++i) {
                result.add((E) ex.newInstance(jsonArray.getJSONObject(i)));
            }
        }
        catch (Exception e) {
            Log.e(TAG, "error!!! loadArrayList " + e);
        }
        finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    Log.e(TAG, "error!!! loadArrayList . InputStream(close) " + e, e);
                }
            }
        }

        return result;
    }

    public void saveArrayList(Context context, String filename, ArrayList<E> list) {
        OutputStream os = null;
        try {
            JSONArray jsonArray = new JSONArray();
            for (E e : list) {
                jsonArray.put(e.toJSON());
            }
            os = context.openFileOutput(filename + FILENAME_SUFFIX, Context.MODE_PRIVATE);
            os.write(jsonArray.toString().getBytes());
        }
        catch (Exception e) {
            Log.e(TAG, "error!!! saveArrayList " + e, e);
        }
        finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    Log.e(TAG, "error!!! saveArrayList . OutputStream(close) " + e, e);
                }
            }
        }
    }

    /**
     * Удалить сохраненные списки исполнителей
     */
    public void deleteArtistsLists(Context context) {
        for (String name : context.fileList()) {
            if (name.startsWith(SAVED_ARTISTS_LIST_FILENAME)) {
                context.deleteFile(name);
                Log.d("obj", "delete == " + name);
            }
        }
    }

    /**
     * Удалить сохраненные списки песен
     */
    public void deleteSongsLists(Context context) {
        for (String name : context.fileList()) {
            if (name.startsWith(SAVED_SONGS_LIST_FILENAME)) {
                context.deleteFile(name);
                Log.d("obj", "delete == " + name);
            }
        }
    }

}
