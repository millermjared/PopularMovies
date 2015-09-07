package com.cluttereddesk.popularmovies;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.cluttereddesk.popularmovies.model.Movie;
import com.cluttereddesk.popularmovies.persistence.MovieContract;
import com.cluttereddesk.popularmovies.servicetasks.FetchMoviesTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A list fragment representing a list of Movies. This fragment
 * also supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is
 * currently being viewed in a {@link MovieDetailsActivityFragment}.
 * <p/>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class MovieListFragment extends Fragment {

    /**
     * The serialization (saved instance state) Bundle key representing the
     * activated item position. Only used on tablets.
     */
    private static final String STATE_ACTIVATED_POSITION = "activated_position";
    private static final String CURRENT_SORT = "current_sort";
    private static final String CURRENT_MOVIES = "current_movies";

    /**
     * The fragment's current callback object, which is notified of list item
     * clicks.
     */
    private Callbacks mCallbacks = sDummyCallbacks;

    /**
     * The current activated item position. Only used on tablets.
     */
    private int mActivatedPosition = ListView.INVALID_POSITION;

    private ArrayList<Movie> movies = new ArrayList<Movie>();
    private GridView searchResults;
    private MovieSearchResultAdapter mMovieListAdapter;
    private MovieFavoritesCursorAdapter mFavoritesCursorAdapter;

    private ProgressBar mProgressBar;
    private String currentSort;

    public AbsListView getListView() {
        return (GridView) getView().findViewById(R.id.movie_search_results);
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callbacks {
        /**
         * Callback for when an item has been selected.
         */
        void onItemSelected(Movie movie);
    }

    /**
     * A dummy implementation of the {@link Callbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onItemSelected(Movie movie) {
        }
    };

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MovieListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setRetainInstance(true);
        if (savedInstanceState != null) {
            movies = savedInstanceState.getParcelableArrayList(CURRENT_MOVIES);
            if (mMovieListAdapter != null) {
                mMovieListAdapter.clear();
                mMovieListAdapter.addAll(movies);
            }
            currentSort = savedInstanceState.getString(CURRENT_SORT);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragView = inflater.inflate(R.layout.fragment_display_movie_posters, container, false);

        bindListAdapter(fragView);

//        if (movies == null || movies.isEmpty() || ! currentSort.equals(sortPreference())) {
//            reloadMovies(fragView);
//        }
        return fragView;
    }

    private void bindListAdapter(View view) {
        mProgressBar = (ProgressBar) view.findViewById(R.id.search_progress_bar);

        searchResults = (GridView) view.findViewById(R.id.movie_search_results);
        mMovieListAdapter = new MovieSearchResultAdapter(getActivity(), R.layout.movie_grid_item, movies);

        if (getString(R.string.sort_preference_favorites).equals(sortPreference())) {
            final Cursor cursor = getActivity().getContentResolver().query(
                    MovieContract.MovieEntry.CONTENT_URI,
                    null, // leaving "columns" null just returns all the columns.
                    null, // cols for "where" clause
                    null, // values for "where" clause
                    null  // sort order
            );

            if (cursor.getCount() == 0) {
                Toast.makeText(getActivity(), getResources().getString(R.string.no_favorites_selected), Toast.LENGTH_SHORT).show();
            }

            mFavoritesCursorAdapter = new MovieFavoritesCursorAdapter(getActivity(), cursor, 0);
            searchResults.setAdapter(mFavoritesCursorAdapter);

            searchResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    cursor.moveToPosition(position);
                    Movie movie = new Movie();

                    movie.identifier = cursor.getString(cursor.getColumnIndexOrThrow(MovieContract.MovieEntry.COLUMN_IDENTIFIER));
                    movie.imagePath = cursor.getString(cursor.getColumnIndexOrThrow(MovieContract.MovieEntry.COLUMN_IMAGE_PATH));
                    movie.plot = cursor.getString(cursor.getColumnIndexOrThrow(MovieContract.MovieEntry.COLUMN_PLOT));
                    movie.rating = cursor.getDouble(cursor.getColumnIndexOrThrow(MovieContract.MovieEntry.COLUMN_RATING));
                    movie.releaseDate = cursor.getString(cursor.getColumnIndexOrThrow(MovieContract.MovieEntry.COLUMN_RELEASE_DATE));
                    movie.title = cursor.getString(cursor.getColumnIndexOrThrow(MovieContract.MovieEntry.COLUMN_TITLE));

                    mCallbacks.onItemSelected(movie);
                }
            });

            currentSort = sortPreference();
        } else {
            searchResults.setAdapter(mMovieListAdapter);
            searchResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Movie movie = (Movie) mMovieListAdapter.getItem(position);

                    mCallbacks.onItemSelected(movie);
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();


        if (movies == null || movies.isEmpty() || currentSort == null || ! currentSort.equals(sortPreference())) {

            if (currentSort == null || ! currentSort.equals(sortPreference()))
                bindListAdapter(getView());

            reloadMovies(getView());
        }


    }

    public void reloadMovies(View view) {
        movies.clear();
        mProgressBar.setVisibility(View.VISIBLE);


        if (getString(R.string.sort_preference_favorites).equals(sortPreference())) {
           // mFavoritesCursorAdapter.getCursor().requery();
            mFavoritesCursorAdapter.notifyDataSetChanged();
            mProgressBar.setVisibility(View.GONE);

            if (mFavoritesCursorAdapter.getCursor().getCount() == 0) {
                Toast.makeText(getActivity(), getResources().getString(R.string.no_favorites_selected), Toast.LENGTH_SHORT).show();
            }

            currentSort = sortPreference();
        } else {
            if (isNetworkAvailable()) {
                retrievePopularMovies();
                currentSort = sortPreference();
            } else {
                Toast.makeText(getActivity(), getResources().getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
            }

        }
    }


    private String sortPreference() {
        return PreferenceManager.getDefaultSharedPreferences(this.getActivity()).getString(getString(R.string.sort_preference_key), "popularity.desc");
    }

    private void retrievePopularMovies() {
        FetchMoviesTask fpmt = new FetchMoviesTask(getActivity(), mMovieListAdapter, mProgressBar);

        AsyncTask<String, Void, List<Movie>> response = fpmt.execute(getString(R.string.api_key), sortPreference());

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager conMan = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = conMan.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Restore the previously serialized activated item position.
        if (savedInstanceState != null
                && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Reset the active callbacks interface to the dummy implementation.
        mCallbacks = sDummyCallbacks;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        if (mActivatedPosition != ListView.INVALID_POSITION) {
            // Serialize and persist the activated item position.
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
        outState.putString(CURRENT_SORT, currentSort);
        if (movies != null)
            outState.putParcelableArrayList(CURRENT_MOVIES, movies);
        super.onSaveInstanceState(outState);
    }

    /**
     * Turns on activate-on-click mode. When this mode is on, list items will be
     * given the 'activated' state when touched.
     */
    public void setActivateOnItemClick(boolean activateOnItemClick) {
        // When setting CHOICE_MODE_SINGLE, ListView will automatically
        // give items the 'activated' state when touched.
        getListView().setChoiceMode(activateOnItemClick
                ? ListView.CHOICE_MODE_SINGLE
                : ListView.CHOICE_MODE_NONE);
    }

    private void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
            getListView().setItemChecked(mActivatedPosition, false);
        } else {
            getListView().setItemChecked(position, true);
        }

        mActivatedPosition = position;
    }
}
