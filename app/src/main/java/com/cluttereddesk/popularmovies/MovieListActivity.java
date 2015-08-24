package com.cluttereddesk.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.cluttereddesk.popularmovies.model.Movie;
import com.cluttereddesk.popularmovies.persistence.MovieContract;


/**
 * An activity representing a list of Movies. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link MovieDetailsActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p/>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link MovieListFragment} and the item details
 * (if present) is a {@link MovieDetailsActivityFragment}.
 * <p/>
 * This activity also implements the required
 * {@link MovieListFragment.Callbacks} interface
 * to listen for item selections.
 */
public class MovieListActivity extends ActionBarActivity
        implements MovieListFragment.Callbacks {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_list);

        if (findViewById(R.id.movie_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            ((MovieListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.movie_list))
                    .setActivateOnItemClick(true);
        }

        // TODO: If exposing deep links into your app, handle intents here.
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.sort_preference) {

            Intent settingsIntent = new Intent(this, SettingsActivity.class);

            startActivity(settingsIntent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * Callback method from {@link MovieListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(Movie movie) {

        String identifier = movie.identifier;
        String imagePath = movie.imagePath;
        String title = movie.title;
        String plot = movie.plot;
        String releaseDate = movie.releaseDate;
        Double rating = movie.rating;

        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle arguments = new Bundle();

            arguments.putString(MovieContract.MovieEntry.COLUMN_IDENTIFIER, identifier);
            arguments.putString(MovieContract.MovieEntry.COLUMN_IMAGE_PATH, imagePath);
            arguments.putString(MovieContract.MovieEntry.COLUMN_PLOT, plot);
            arguments.putString(MovieContract.MovieEntry.COLUMN_TITLE, title);
            arguments.putString(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, releaseDate);
            arguments.putDouble(MovieContract.MovieEntry.COLUMN_RATING, rating);

            MovieDetailsActivityFragment fragment = new MovieDetailsActivityFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_detail_container, fragment)
                    .commit();

        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent detailIntent = new Intent(this, MovieDetailsActivity.class);


            detailIntent.putExtra(MovieContract.MovieEntry.COLUMN_IDENTIFIER, identifier);
            detailIntent.putExtra(MovieContract.MovieEntry.COLUMN_IMAGE_PATH, imagePath);
            detailIntent.putExtra(MovieContract.MovieEntry.COLUMN_PLOT, plot);
            detailIntent.putExtra(MovieContract.MovieEntry.COLUMN_TITLE, title);
            detailIntent.putExtra(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, releaseDate);
            detailIntent.putExtra(MovieContract.MovieEntry.COLUMN_RATING, rating);
            startActivity(detailIntent);
        }
    }
}
