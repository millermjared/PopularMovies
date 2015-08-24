package com.cluttereddesk.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;

import com.cluttereddesk.popularmovies.persistence.MovieContract;
import com.squareup.picasso.Picasso;


/**
 * Created by Matt on 8/2/15.
 */
public class MovieFavoritesCursorAdapter extends CursorAdapter {

    private static final String BASE_IMAGE_URL = "http://image.tmdb.org/t/p/w185";

    public MovieFavoritesCursorAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.movie_grid_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(MovieContract.MovieEntry.COLUMN_IMAGE_PATH));
        Picasso.with(context).load(BASE_IMAGE_URL + imagePath).into((ImageView)view);
    }

}
