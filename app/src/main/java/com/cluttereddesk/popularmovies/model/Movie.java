package com.cluttereddesk.popularmovies.model;

import android.os.Parcel;
import android.os.Parcelable;


public class Movie implements Parcelable {

    public String identifier;
    public String imagePath;
    public String title;
    public String plot;
    public String releaseDate;
    public Double rating;

    public Movie() {
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(identifier);
        out.writeString(imagePath);
        out.writeString(title);
        out.writeString(plot);
        out.writeString(releaseDate);
        out.writeDouble(rating);

    }

    public static final Parcelable.Creator<Movie> CREATOR
            = new Parcelable.Creator<Movie>() {
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    private Movie(Parcel in) {
        identifier = in.readString();
        imagePath = in.readString();
        title = in.readString();
        plot = in.readString();
        releaseDate = in.readString();
        rating = in.readDouble();
    }


}
