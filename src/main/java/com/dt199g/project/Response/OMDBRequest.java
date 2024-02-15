package com.dt199g.project.Response;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.stream.Collectors;

/**
 * creates a request to fetch movies with stated title from OpenMovieDataBase
 * @author Anton Byström
 */
public class OMDBRequest {
    private static final String API_KEY = "9aa03196";
    private static final String API_URL = "http://www.omdbapi.com/?apikey=" + API_KEY;

    public OMDBRequest() {}

    /**
     * creates url for selected title to search for movies
     * @param title movie title
     * @return string of all movies found
     */
    public String fetchTitle(String title) {
        try {
            return omdbConnect(new URL(String.format("%s&t=%s", API_URL,title))).blockingGet();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * creates url for selected title to search for movies
     * @param title movie title
     * @return string of all movies found
     */
    public String fetchTitle(String title, String year) {
        try {
            return omdbConnect(new URL(String.format("%s&t=%s&y=%s", API_URL,title,year))).blockingGet();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *
     * @param url omdb url
     * @return string of all movies found
     */
    private Single<String> omdbConnect(URL url) {
        return Single.fromCallable(() -> {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                return reader.lines().collect(Collectors.joining());
            } catch (IOException e) {
                System.err.println("Error reading input stream: " + e.getMessage());
            } finally {
                connection.disconnect();
            }
            return "Sorry but I could not find that movie on OMDB";
        }).subscribeOn(Schedulers.io());
    }
}
