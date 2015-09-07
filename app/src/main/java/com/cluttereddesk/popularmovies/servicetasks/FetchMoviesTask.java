/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cluttereddesk.popularmovies.servicetasks;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.cluttereddesk.popularmovies.model.Movie;
import com.cluttereddesk.popularmovies.MovieSearchResultAdapter;

public class FetchMoviesTask extends AsyncTask<String, Void, List<Movie>> {

    private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();

    private List<Movie> mMovies;
    private ProgressBar mProgressBar;
    private final Context mContext;
    private MovieSearchResultAdapter mResultAdapter;

    public FetchMoviesTask(Context context, MovieSearchResultAdapter resultAdapter, ProgressBar progressBar) {
        mContext = context;
        mProgressBar = progressBar;
        mMovies = new ArrayList<Movie>();
        mResultAdapter = resultAdapter;

    }

    private boolean DEBUG = true;

    private List<Movie> parseAndCacheResponse(String movieJsonStr)
            throws JSONException {

        final String MDB_ID = "id";
        final String MDB_TITLE = "original_title";
        final String MDB_IMAGE_PATH = "poster_path";
        final String MDB_PLOT = "overview";
        final String MDB_RATING = "vote_average";
        final String MDB_RELEASE_DATE = "release_date";
        final String MDB_DATE_FORMAT = "yyyy-MM-dd";

        DateFormat df = new SimpleDateFormat(MDB_DATE_FORMAT);
        List<Movie> movies = new ArrayList<Movie>();
        try {
            JSONArray results = new JSONObject(movieJsonStr).getJSONArray("results");

            for (int i = 0; i < results.length(); i++) {

                JSONObject movieJson = results.getJSONObject(i);

                String identifier = movieJson.getString(MDB_ID);
                String originalTitle = movieJson.getString(MDB_TITLE);
                String imagePath = movieJson.getString(MDB_IMAGE_PATH);
                String plotSynopsis = movieJson.getString(MDB_PLOT);
                Double rating = movieJson.getDouble(MDB_RATING);
                String releaseDate = movieJson.getString(MDB_RELEASE_DATE);

                Movie movie = new Movie();
                movie.identifier = identifier;
                movie.imagePath = imagePath;
                if (!"null".equals(plotSynopsis))
                    movie.plot = plotSynopsis;
                else
                    movie.plot = "";
                movie.rating = rating;
                movie.releaseDate = releaseDate;
                movie.title = originalTitle;

                movies.add(movie);
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        return movies;
    }

    @Override
    protected List<Movie> doInBackground(String... params) {

        String apiKey = params[0];
        String sort = params[1];

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        String movieJsonStr = null;

        String format = "json";

        try {

            final String POP_MOVIE_BASE_URL =
                    "http://api.themoviedb.org/3/discover/movie?";
            final String SORT_PARAM = "sort_by";
            final String API_KEY_PARAM = "api_key";

            Uri builtUri = Uri.parse(POP_MOVIE_BASE_URL).buildUpon()
                    .appendQueryParameter(SORT_PARAM, sort)
                    .appendQueryParameter(API_KEY_PARAM, apiKey)
                    .build();

            URL url = new URL(builtUri.toString());

            // Create the request to themoviedb, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            movieJsonStr = buffer.toString();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

        try {
            return parseAndCacheResponse(movieJsonStr);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(List<Movie> result) {
        if (result != null && mMovies != null) {
            mMovies.clear();
            for (Movie movie : result) {
                mResultAdapter.add(movie);
            }
            mProgressBar.setVisibility(View.GONE);
        }

    }
}