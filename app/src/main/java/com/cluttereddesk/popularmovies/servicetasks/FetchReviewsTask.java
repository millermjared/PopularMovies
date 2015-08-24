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

import com.cluttereddesk.popularmovies.model.Review;

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

public class FetchReviewsTask extends AsyncTask<String, Void, List<Review>> {

    private final String LOG_TAG = FetchReviewsTask.class.getSimpleName();

    private ArrayAdapter<Review> mReviewAdapter;
    private final Context mContext;

    public FetchReviewsTask(Context context, ArrayAdapter<Review> reviewAdapter) {
        mContext = context;
        mReviewAdapter = reviewAdapter;
    }

    private List<Review> parseAndCacheResponse(String reviewsJsonStr)
            throws JSONException {

        final String MDB_AUTHOR = "author";
        final String MDB_CONTENT = "content";

        List<Review> reviews = new ArrayList<Review>();
        try {
            JSONArray results = new JSONObject(reviewsJsonStr).getJSONArray("results");

            for (int i = 0; i < results.length(); i++) {

                JSONObject reviewJson = results.getJSONObject(i);

                Review review = new Review();
                review.author = reviewJson.getString(MDB_AUTHOR);
                review.content = reviewJson.getString(MDB_CONTENT);
                reviews.add(review);

            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        return reviews;
    }

    @Override
    protected List<Review> doInBackground(String... params) {

        String apiKey = params[0];
        String movieId = params[1];

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        String reviewsJsonStr = null;

        try {
            final String REVIEWS_BASE_URL =
                    "http://api.themoviedb.org/3/movie";
            final String API_KEY_PARAM = "api_key";

            Uri builtUri = Uri.parse(REVIEWS_BASE_URL).buildUpon()
                    .appendPath(movieId)
                    .appendPath("reviews")
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
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                return null;
            }
            reviewsJsonStr = buffer.toString();
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
            return parseAndCacheResponse(reviewsJsonStr);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(List<Review> result) {
        if (result != null && mReviewAdapter != null) {
            mReviewAdapter.clear();
            for (Review review : result) {
                mReviewAdapter.add(review);
            }
        }
    }
}