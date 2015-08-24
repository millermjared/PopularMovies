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
import android.widget.ArrayAdapter;

import com.cluttereddesk.popularmovies.MovieDetailsActivity;
import com.cluttereddesk.popularmovies.model.Movie;
import com.cluttereddesk.popularmovies.model.Trailer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FetchTrailersTask extends AsyncTask<String, Void, List<Trailer>> {

    private final String LOG_TAG = FetchTrailersTask.class.getSimpleName();

    private ArrayAdapter<Trailer> mTrailerAdapter;
    private final Context mContext;

    public FetchTrailersTask(Context context, ArrayAdapter<Trailer> trailerAdapter) {
        mContext = context;
        mTrailerAdapter = trailerAdapter;
    }

    private List<Trailer> parseAndCacheResponse(String movieJsonStr)
            throws JSONException {


        final String MDB_NAME = "name";
        final String MDB_KEY = "key";

        List<Trailer> trailers = new ArrayList<Trailer>();
        try {
            JSONArray results = new JSONObject(movieJsonStr).getJSONArray("results");

            for (int i = 0; i < results.length(); i++) {

                JSONObject trailerJson = results.getJSONObject(i);

                Trailer trailer = new Trailer();
                trailer.name = trailerJson.getString(MDB_NAME);
                trailer.videoKey = trailerJson.getString(MDB_KEY);
                trailers.add(trailer);

            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        return trailers;
    }

    @Override
    protected List<Trailer> doInBackground(String... params) {

        String apiKey = params[0];
        String movieId = params[1];

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String trailersJsonStr = null;

        try {
            final String TRAILERS_BASE_URL =
                    "http://api.themoviedb.org/3/movie";
            final String API_KEY_PARAM = "api_key";

            Uri builtUri = Uri.parse(TRAILERS_BASE_URL).buildUpon()
                    .appendPath(movieId)
                    .appendPath("videos")
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
            trailersJsonStr = buffer.toString();
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
            return parseAndCacheResponse(trailersJsonStr);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        // This will only happen if there was an error getting or parsing the forecast.
        return null;
    }

    @Override
    protected void onPostExecute(List<Trailer> result) {
        if (result != null && mTrailerAdapter != null) {
            mTrailerAdapter.clear();
            for (Trailer trailer : result) {
                mTrailerAdapter.add(trailer);
            }

            //seems a hack, but the requirements are a wtf - set the share intent from async retrieved data?
            if (result.size() > 0) {
                //at a certain point android is a load of poorly designed bullshit
                //it's not worth piling hack upon hack for this stupid "feature"
                if (mContext instanceof MovieDetailsActivity)
                    ((MovieDetailsActivity) mContext).setShareIntent();
            }
        }
    }
}