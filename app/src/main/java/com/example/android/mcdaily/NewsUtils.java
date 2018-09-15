/*
 * Copyright (C) 2016 The Android Open Source Project
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
package com.example.android.mcdaily;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper methods related to requesting and receiving news data from Guardianapis.
 */
public final class NewsUtils {

    /**
     * Tag for the log messages
     */
    private static final String LOG_TAG = NewsUtils.class.getSimpleName();

    /**
     * Create a private constructor because no one should ever create a {@link NewsUtils} object.
     * This class is only meant to hold static variables and methods, which can be accessed
     * directly from the class name NewsUtils (and an object instance of NewsUtils is not needed).
     */
    private NewsUtils() {
    }

    /**
     * Query the guardianapis dataset and return a list of {@link NewsModel} objects.
     */
    public static List<NewsModel> fetchNewsData(String requestUrl) {
        // Create URL object
        URL url = createUrl(requestUrl);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e);
        }

        // Extract relevant fields from the JSON response and create a list of {@link NewsModel}s
        List<NewsModel> newsModel = extractResultsFromJson(jsonResponse);

        // Return the list of {@link NewsModel}s
        return newsModel;
    }

    /**
     * Returns new URL object from the given string URL.
     */
    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Problem building the URL ", e);
        }
        return url;
    }

    /**
     * Make an HTTP request to the given URL and return a String as the response.
     */
    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the news JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                // Closing the input stream could throw an IOException, which is why
                // the makeHttpRequest(URL url) method signature specifies than an IOException
                // could be thrown.
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    /**
     * Convert the {@link InputStream} into a String which contains the
     * whole JSON response from the server.
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    /**
     * Return a list of {@link NewsModel} objects that has been built up from
     * parsing the given JSON response.
     */
    private static List<NewsModel> extractResultsFromJson(String newsJSON) {
        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(newsJSON)) {
            return null;
        }

        // Create an empty ArrayList that we can start adding news to
        List<NewsModel> newsModels = new ArrayList<>();

        // Try to parse the JSON response string. If there's a problem with the way the JSON
        // is formatted, a JSONException exception object will be thrown.
        // Catch the exception so the app doesn't crash, and print the error message to the logs.
        try {

            // Create a JSONObject from the JSON response string
            JSONObject baseJsonResponse = new JSONObject(newsJSON);

            // Extract the JSONArray associated with the key called "response",
            // which represents a list of features (or news).
            JSONObject newsObject = baseJsonResponse.getJSONObject("response");

            JSONArray newsArray = newsObject.getJSONArray("results");

            // For each news article in the newsArray, create an {@link NewsModel} object
            for (int i = 0; i < newsArray.length(); i++) {

                // Get a single NewsModel at position i within the list of NewsModels
                JSONObject properties = newsArray.getJSONObject(i);

                // For a given NewsModel, extract the JSONObject associated with the
                // key called "response", which represents a list of all results
                // for that news section.
                //JSONObject properties = currentNews.getJSONObject("results");

                // The title of the web artice
                String title = properties.getString("webTitle");

                // the section of the web article
                String section = properties.getString("sectionName");

                // the publication date of the article
                String pubDate = properties.getString("webPublicationDate");

                // the web url of the article
                String url = properties.getString("webUrl");

                // the writer of the article
                String author = "The Guardian Article";
                JSONArray thisTag = properties.optJSONArray("tags");
                JSONObject thisTagsObj = null;
                if (thisTag != null)
                    thisTagsObj = thisTag.optJSONObject(0);
                if (thisTagsObj != null)
                    author = thisTagsObj.getString("webTitle");

                // Create a new {@link NewsModel} object with the title, section, pubdate, url and author,
                NewsModel newsModel = new NewsModel(title, section, author, pubDate, url);

                // Add the new {@link NewsModel} to the list of NewsModels.
                newsModels.add(newsModel);
            }

        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.e("NewsUtils", "Problem parsing the News JSON results", e);
        }

        // Return the list of NewsModels
        return newsModels;
    }
}
