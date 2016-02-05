package com.example.alex.amalgamasongs;

import android.util.Log;

import com.example.alex.amalgamasongs.entity.Artist;
import com.example.alex.amalgamasongs.entity.Song;
import com.example.alex.amalgamasongs.entity.Translation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

public class Fetcher {

    private static final String TAG = "Fetcher";
    public static final String siteURL = "http://www.amalgama-lab.com";

    public static ArrayList<Artist> fetchArtists(String letter) {
        ArrayList<Artist> result = new ArrayList<>();

        InputStream is = null;
        try {
            is = new URL(siteURL + "/songs/" + Character.toLowerCase(letter.charAt(0)) + "/").openStream();
            String encoding = System.getProperty("console.encoding", "windows-1251");
            Scanner sc = new Scanner(is, encoding);
            String s;
            while ((s = sc.nextLine()) != null) {
                if (s.startsWith("<!-- Начало списка фотографий групп")) {
                    break;
                }
            }

            while (sc.hasNext() && !(s = sc.nextLine().trim()).startsWith("<!-- Конец списка фотографий групп")) {
                if (s.startsWith("<li>")) {
                    while (sc.hasNext() && !s.endsWith("</li>")) {
                        s += sc.nextLine().trim();
                    }
                    if (s.contains("<a href=\"")) {
                        int i = s.indexOf("<a href=\"") + 9;
                        int j = s.indexOf("\">", i);
                        int k = s.indexOf("</a>", j);
                        String link = s.substring(i, j);
                        String name = s.substring(j + 2, k);
                        result.add(new Artist(name, link));
                    }
                }
            }
        }
        catch (Exception e) {
            Log.e(TAG, "error!!! fetchArtists " + e, e);
        }
        finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    Log.e(TAG, "error!!! fetchArtists . InputStream(close) " + e, e);
                }
            }
        }

        return result;
    }

    public static ArrayList<Song> fetchSongs(String artistLink) {
        ArrayList<Song> result = new ArrayList<>();

        InputStream is = null;
        try {
            is = new URL(siteURL + artistLink).openStream();
            String encoding = System.getProperty("console.encoding", "windows-1251");
            Scanner sc = new Scanner(is, encoding);
            String s;
            while ((s = sc.nextLine()) != null) {
                if (s.startsWith("<!-- Начало подменю активной группы")) {
                    break;
                }
            }

            while (sc.hasNext() && !(s = sc.nextLine().trim()).startsWith("<!-- Конец подменю активной группы")) {
                if(s.startsWith("<li><a href=\"")) {
                    while (sc.hasNext() && ! s.endsWith("</li>")) {
                        s += sc.nextLine().trim();
                    }
                    int i = s.indexOf("\"") + 1;
                    int j = s.indexOf("\">");
                    int k = s.indexOf("</a>");
                    String link = s.substring(i, j);
                    String title = s.substring(j+2, k);
                    result.add(new Song(title, link));
                }
            }
        }
        catch (Exception e) {
            Log.e(TAG, "error!!! fetchSongs " + e, e);
        }
        finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    Log.e(TAG, "error!!! fetchSongs . InputStream(close) " + e, e);
                }
            }
        }

        return result;
    }

    public static Translation fetchTranslation(String artistLink, String songLink) {
        Translation result = new Translation();

        InputStream is = null;
        try {
            is = new URL(siteURL + artistLink + songLink).openStream();

            String encoding = System.getProperty("console.encoding", "windows-1251");
            BufferedReader br = new BufferedReader(new InputStreamReader(is, encoding));

            String s;
            while ((s=br.readLine()) != null) {
                if (s.startsWith("<div style=\"overflow:hidden;\">")) {
                    break;
                }
            }

            while ((s=br.readLine()) != null) {
                if (s.startsWith("<div id=\"click_area\">")) {
                    break;
                }

                int ind = s.indexOf("\"nofollow\">");
                if(ind >= 0) {
                    ind += 11;
                    StringBuffer rus_author = new StringBuffer();
                    readText(s, ind, rus_author);
                    result.mRusAuthor = rus_author.substring(0, rus_author.length() - 1);
                }

                ind = s.indexOf("\"translate\">");
                if(ind >= 0) {
                    ind += 12;
                    StringBuffer rus_title = new StringBuffer();
                    readText(s, ind, rus_title);
                    result.mRusTitle = rus_title.substring(0, rus_title.length() - 1);
                }

            }

            StringBuffer eng = new StringBuffer();
            StringBuffer rus = new StringBuffer();
            int state = 0;

            while ((s = br.readLine()) != null) {
                s = s.trim();

                if(s.startsWith("<div id=\"quality\" class=\"noprint\">") || s.contains("<strong>")) {
                    break;
                }

                int ind;

                if(state == 1) {
                    boolean isEnd = readText(s, 0, eng);
                    if(!isEnd) {
                        state = 0;
                    }
                }

                if(state == 2) {
                    boolean isEnd = readText(s, 0, rus);
                    if(!isEnd) {
                        state = 0;
                    }
                }

                ind = s.indexOf("\"original\">");
                if(ind >= 0) {
                    ind += 11;
                    boolean isEnd = readText(s, ind, eng);
                    if(isEnd) {
                        state = 1;
                    }
                }

                ind = s.indexOf("\"translate\">");
                if(ind >= 0) {
                    ind += 12;
                    boolean isEnd = readText(s, ind, rus);
                    if(isEnd) {
                        state = 2;
                    }
                }

            }

            result.mEngText = eng.toString();
            result.mRusText = rus.toString();

            // для корректного отображения в 'Html.fromHtml()'.
            result.mEngText = result.mEngText.replace("\n", "<br/>");
            result.mRusText = result.mRusText.replace("\n", "<br/>");
        }
        catch (Exception e) {
            Log.e(TAG, "error!!! fetchSongs " + e, e);
        }
        finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    Log.e(TAG, "error!!! fetchSongs . InputStream(close) " + e, e);
                }
            }
        }

        return result;
    }

    /**
     * Читать символы из одной строки в другую, пока не встретится '<' или не дочитаем до конца строки 'from'
     * @param from строка, откуда читать
     * @param ind откуда начинать
     * @param to куда записывать то, что прочитали
     * @return дочитали ли до конца строки
     */
    private static boolean readText(String from, int ind, StringBuffer to) {
        while(ind < from.length()) {
            if(from.charAt(ind) == '<') {
                to.append('\n');
                return false;
            }
            to.append(from.charAt(ind));
            ++ind;
        }
        return true;
    }

}