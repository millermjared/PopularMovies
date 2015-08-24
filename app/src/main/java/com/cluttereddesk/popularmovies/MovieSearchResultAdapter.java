package com.cluttereddesk.popularmovies;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cluttereddesk.popularmovies.model.Movie;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Matt on 8/2/15.
 */
public class MovieSearchResultAdapter extends ArrayAdapter {

    private static final String BASE_IMAGE_URL = "http://image.tmdb.org/t/p/w185";

    private Context context;
    private int layoutResourceId;

    public MovieSearchResultAdapter(Context context, int resource, List objects) {
        super(context, resource, objects);
        this.context = context;
        this.layoutResourceId = resource;
    }
    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView = (ImageView)convertView;
        if (convertView == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            imageView = (ImageView) inflater.inflate(layoutResourceId, parent, false);
        }

        Movie movie = (Movie) getItem(position);
        Picasso.with(context).load(BASE_IMAGE_URL + movie.imagePath).into(imageView);

        return imageView;
    }


}
