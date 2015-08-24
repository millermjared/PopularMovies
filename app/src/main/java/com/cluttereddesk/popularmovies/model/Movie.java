package com.cluttereddesk.popularmovies.model;

import org.json.JSONObject;

/**
 * Created by Matt on 8/2/15.
 */
public class Movie {

    public String identifier;
    public String imagePath;
    public String title;
    public String plot;
    public String releaseDate;
    public Double rating;
    public String runtime;

    public Movie() {
    }

    public Movie(JSONObject source) {

    }

}
