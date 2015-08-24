package com.cluttereddesk.popularmovies;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.cluttereddesk.popularmovies.model.Review;
import com.cluttereddesk.popularmovies.model.Trailer;
import com.cluttereddesk.popularmovies.persistence.MovieContract;
import com.cluttereddesk.popularmovies.servicetasks.FetchReviewsTask;
import com.cluttereddesk.popularmovies.servicetasks.FetchTrailersTask;
import com.squareup.picasso.Picasso;


/**
 * A placeholder fragment containing a simple view.
 */
public class MovieDetailsActivityFragment extends Fragment {

    Uri movieUri;
    String identifier;
    String imagePath;
    String title;
    String releaseDate;
    Double rating;
    String plot;

    private ArrayAdapter<Trailer> mTrailerAdapter;
    private ArrayAdapter<Review> mReviewAdapter;

    public MovieDetailsActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            identifier = getArguments().getString(MovieContract.MovieEntry.COLUMN_IDENTIFIER);
            imagePath = getArguments().getString(MovieContract.MovieEntry.COLUMN_IMAGE_PATH);
            title = getArguments().getString(MovieContract.MovieEntry.COLUMN_TITLE);
            releaseDate = getArguments().getString(MovieContract.MovieEntry.COLUMN_RELEASE_DATE);
            rating = getArguments().getDouble(MovieContract.MovieEntry.COLUMN_RATING, 0.0d);
            plot = getArguments().getString(MovieContract.MovieEntry.COLUMN_PLOT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Intent intent = getActivity().getIntent();

        if (getArguments() == null) {
            identifier = intent.getStringExtra(MovieContract.MovieEntry.COLUMN_IDENTIFIER);
            imagePath = intent.getStringExtra(MovieContract.MovieEntry.COLUMN_IMAGE_PATH);
            title = intent.getStringExtra(MovieContract.MovieEntry.COLUMN_TITLE);
            releaseDate = intent.getStringExtra(MovieContract.MovieEntry.COLUMN_RELEASE_DATE);
            rating = intent.getDoubleExtra(MovieContract.MovieEntry.COLUMN_RATING, 0.0d);
            plot = intent.getStringExtra(MovieContract.MovieEntry.COLUMN_PLOT);
        }
        View movieDetailsView = inflater.inflate(R.layout.fragment_movie_details, container, false);


        ImageView imageView = (ImageView) movieDetailsView.findViewById(R.id.movie_image);
        Picasso.with(getActivity()).load("http://image.tmdb.org/t/p/w185" + imagePath).into(imageView);

        TextView titleTextView = (TextView) movieDetailsView.findViewById(R.id.titleTextView);
        titleTextView.setText(title);

        TextView releaseDateView = (TextView) movieDetailsView.findViewById(R.id.releaseDate);
        releaseDateView.setText(releaseDate);

        TextView ratingView = (TextView) movieDetailsView.findViewById(R.id.rating);
        ratingView.setText(rating.toString());

        TextView plotView = (TextView) movieDetailsView.findViewById(R.id.plotSynopsis);
        plotView.setText(plot);

        String[] columns = {MovieContract.MovieEntry._ID};

        String[] whereValues = {"-1"};
        if (identifier != null)
            whereValues[0] = identifier;

        final Cursor cursor = getActivity().getContentResolver().query(
                MovieContract.MovieEntry.CONTENT_URI,
                columns, // leaving "columns" null just returns all the columns.
                MovieContract.MovieEntry.COLUMN_IDENTIFIER + "=?", // cols for "where" clause
                whereValues, // values for "where" clause
                null  // sort order
        );

        Button favoriteButton = (Button) movieDetailsView.findViewById(R.id.markAsFavorite);
        if (cursor.getCount() > 0) {
            favoriteButton.setText(R.string.unmark_as_favorite);
            cursor.moveToFirst();
            try {
                Long id = cursor.getLong(cursor.getColumnIndexOrThrow(MovieContract.MovieEntry._ID));
                movieUri = MovieContract.MovieEntry.buildMovieUri(id);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("MJM", "error", e);
            }



            favoriteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    unmarkAsFavorite(v);
                    ((Button)v).setText(R.string.mark_as_favorite);

                }
            });
        } else {
            favoriteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    markAsFavorite(v);
                    ((Button)v).setText(R.string.unmark_as_favorite);
                }
            });
        }

        mReviewAdapter = new ArrayAdapter<Review>(getActivity(), R.layout.review_list_item, R.id.reviewText);
        FetchReviewsTask frt = new FetchReviewsTask(getActivity(), mReviewAdapter);

        frt.execute(getString(R.string.api_key), identifier);

        ListView reviews = (ListView) movieDetailsView.findViewById(R.id.reviews);
        reviews.setAdapter(mReviewAdapter);




        mTrailerAdapter = new ArrayAdapter<Trailer>(getActivity(), R.layout.trailer_list_item, R.id.trailerTitle);
        FetchTrailersTask ftt = new FetchTrailersTask(getActivity(), mTrailerAdapter);

        ftt.execute(getString(R.string.api_key), identifier);

        ListView trailers = (ListView) movieDetailsView.findViewById(R.id.trailers);
        trailers.setAdapter(mTrailerAdapter);
        trailers.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        Trailer trailer = mTrailerAdapter.getItem(position);

                        Uri videoUrl = Uri.parse("http://www.youtube.com/watch").buildUpon().appendQueryParameter("v", trailer.videoKey).build();

                        Intent intent = new Intent(Intent.ACTION_VIEW, videoUrl);

                        //This causes my phone to disambiguate to a picture viewer for some reason.
                        //intent.setDataAndType(videoUrl, "video/*");

                        startActivity(intent);
                    }
                }
        );


        ScrollView scrollView = (ScrollView) movieDetailsView.findViewById(R.id.scrollView);
        scrollView.fullScroll(0);
        return movieDetailsView;
    }

    public Trailer getFirstTrailer() {
        if (mTrailerAdapter != null && mTrailerAdapter.getCount() > 0)
            return mTrailerAdapter.getItem(0);
        else
            return null;
    }

    public void markAsFavorite(View v) {

        ContentValues movieValues = new ContentValues();

        movieValues.put(MovieContract.MovieEntry.COLUMN_IDENTIFIER, identifier);
        movieValues.put(MovieContract.MovieEntry.COLUMN_TITLE, title);
        movieValues.put(MovieContract.MovieEntry.COLUMN_IMAGE_PATH, imagePath);
        movieValues.put(MovieContract.MovieEntry.COLUMN_PLOT, plot);
        movieValues.put(MovieContract.MovieEntry.COLUMN_RATING, rating);
        movieValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, releaseDate);

        movieUri = getActivity().getContentResolver().insert(MovieContract.MovieEntry.CONTENT_URI, movieValues);


    }
    public void unmarkAsFavorite(View v) {
        String[] criteria = {identifier};
        getActivity().getContentResolver().delete(movieUri, MovieContract.MovieEntry.COLUMN_IDENTIFIER + "=?", criteria);
    }

}
