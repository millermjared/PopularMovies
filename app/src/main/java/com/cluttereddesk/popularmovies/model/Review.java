package com.cluttereddesk.popularmovies.model;

/**
 * Created by Matt on 8/21/15.
 */
public class Review {
    public String content;
    public String author;

    public String toString() {
        return author + " - " + content;
    }
}
