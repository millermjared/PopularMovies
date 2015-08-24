package com.cluttereddesk.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.ShareActionProvider;
import android.view.Menu;
import android.view.MenuItem;

import com.cluttereddesk.popularmovies.model.Trailer;
import com.cluttereddesk.popularmovies.persistence.MovieContract;


public class MovieDetailsActivity extends ActionBarActivity {

    private ShareActionProvider mShareActionProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_movie_details, menu);

        MenuItem shareItem = menu.findItem(R.id.share_action);

        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);

        return true;
    }

    public void setShareIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        if (mShareActionProvider != null) {

            MovieDetailsActivityFragment fragment = (MovieDetailsActivityFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
            Trailer trailer = fragment.getFirstTrailer();

            Uri videoUrl = Uri.parse("http://www.youtube.com/watch").buildUpon().appendQueryParameter("v", trailer.videoKey).build();
            String title = getIntent().getStringExtra(MovieContract.MovieEntry.COLUMN_TITLE);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Check out this trailer for " + title);
            shareIntent.putExtra(Intent.EXTRA_TEXT, videoUrl.toString());

            shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            shareIntent.setType("text/plain");
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

}
