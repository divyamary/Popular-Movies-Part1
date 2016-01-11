package in.divyamary.moviereel;

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * MovieGridFragment displays the movie posters on app start.
 */
public class MovieGridFragment extends Fragment implements FetchMovieTaskListener, AdapterView.OnItemSelectedListener {

    public final static String MOVIE = "in.divyamary.MOVIE";
    private static final String BUNDLE_MOVIES_LIST = "in.divyamary.MoviesList";
    private static final String STATE_IS_LOADING = "in.divyamary.Loading";
    private RecyclerView mRecyclerView;
    private MovieRecyclerAdapter mAdapter;
    private GridLayoutManager mLayoutManager;
    private Spinner mSpinner;
    private ArrayList<Movie> mMoviesList;
    private final int mVisibleThreshold = 4;
    private int mFirstVisibleItem, mVisibleItemCount, mTotalItemCount;
    private int mCurrentPage = 1;
    private boolean mIsRefresh = false;
    private boolean mIsLoading = true;
    private boolean mIsViewRestored = false;

    public MovieGridFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        setRecyclerView(rootView);
        setSpinner(rootView);
        return rootView;
    }

    private void setRecyclerView(View rootView) {
        mMoviesList = new ArrayList<>();
        mAdapter = new MovieRecyclerAdapter(mMoviesList);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        //On layout change, change span of GridLayoutManager
        if (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(
                    new ViewTreeObserver.OnGlobalLayoutListener() {
                        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                        @Override
                        public void onGlobalLayout() {
                            mRecyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            int viewWidth = mRecyclerView.getMeasuredWidth();
                            int newSpanCount = (int) Math.floor(viewWidth / getResources().getInteger(R.integer.poster_width));
                            mLayoutManager.setSpanCount(newSpanCount);
                            mLayoutManager.requestLayout();

                        }
                    });
        }
        mLayoutManager = new GridLayoutManager(getContext(), getResources().getInteger(R.integer.num_columns));
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        // Add scroll listener
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                mFirstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();
                mVisibleItemCount = mLayoutManager.getChildCount();
                mTotalItemCount = mLayoutManager.getItemCount();
                if (dy > 0) {
                    if (!mIsLoading) {
                        if (mFirstVisibleItem + mVisibleItemCount >= mTotalItemCount - mVisibleThreshold) {
                            new FetchMovieTask(new FetchMovieTaskListener() {
                                @Override
                                public void fetchMovieCompleted() {
                                    mIsLoading = false;
                                    mCurrentPage = mCurrentPage + 1;
                                }
                            }).execute(mCurrentPage + 1);
                            mIsLoading = true;
                        }
                    }
                }
            }
        });
    }

    private void setSpinner(View rootView) {
        mSpinner = (Spinner) rootView.findViewById(R.id.spinner_sort);
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.sort_array, android.R.layout.simple_spinner_item);
        mSpinner.setAdapter(spinnerAdapter);
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerAdapter.notifyDataSetChanged();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String spinnerSelection = sharedPreferences.getString(getString(R.string.pref_sort_type), getString(R.string.pref_popularity));
        if (spinnerSelection.equals(getString(R.string.pref_popularity))) {
            //Set animate to false to prevent onItemSelected callback on spinner intialization.
            mSpinner.setSelection(0, false);
        } else {
            mSpinner.setSelection(1, false);
        }
        mSpinner.setOnItemSelectedListener(this);
    }


    @Override
    public void onStart() {
        super.onStart();
        if (!mIsViewRestored) {
            if (Utils.isInternetConnected(getContext())) {
                new FetchMovieTask(new FetchMovieTaskListener() {
                    @Override
                    public void fetchMovieCompleted() {
                        mIsLoading = false;
                    }
                }).execute(mCurrentPage);
            } else {
                Snackbar snackbar = Snackbar.make(getView().findViewById(R.id.recycler_view), getString(R.string.no_internet),
                        Snackbar.LENGTH_LONG);
                View snackBarView = snackbar.getView();
                snackBarView.setBackgroundColor(getResources().getColor(R.color.grey_900));
                snackbar.show();
            }
        }
    }

    @Override
    public void fetchMovieCompleted() {
        mIsLoading = false;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
        String sortString = adapterView.getItemAtPosition(pos).toString();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(getString(R.string.pref_sort_type), sortString).apply();
        mIsRefresh = true;
        //Set mCurrentPage back to 1 before running AsyncTask
        mCurrentPage = 1;
        new FetchMovieTask(new FetchMovieTaskListener() {
            @Override
            public void fetchMovieCompleted() {
                mIsLoading = false;
            }
        }).execute(mCurrentPage);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(BUNDLE_MOVIES_LIST, mMoviesList);
        outState.putBoolean(STATE_IS_LOADING, mIsLoading);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            ArrayList<Movie> savedMovieList = savedInstanceState.getParcelableArrayList(BUNDLE_MOVIES_LIST);
            mIsLoading = savedInstanceState.getBoolean(STATE_IS_LOADING);
            mAdapter.addData(savedMovieList, false);
            mAdapter.notifyDataSetChanged();
            mIsViewRestored = true;

        }
    }


    public class FetchMovieTask extends AsyncTask<Integer, Void, List<Movie>> {

        private FetchMovieTaskListener fetchMovieTaskListener;
        final String LOG_TAG = FetchMovieTask.class.getSimpleName();

        public FetchMovieTask(FetchMovieTaskListener listener) {
            this.fetchMovieTaskListener = listener;
        }

        @Override
        protected List<Movie> doInBackground(Integer... integer) {
            List<Movie> movieList = new ArrayList<>();
            String responseJSONString;
            URL url;
            HttpURLConnection httpURLConnection = null;
            BufferedReader bufferedReader = null;
            String page = "";
            String count;
            String sortType;
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            sortType = sharedPreferences.getString(getString(R.string.pref_sort_type), getString(R.string.pref_sort_default_value));
            if (sortType.equals(getString(R.string.pref_popularity))) {
                sortType = getString(R.string.sort_popularity);
                count = getString(R.string.default_vote_count);
            } else {
                sortType = getString(R.string.sort_rating);
                count = getString(R.string.vote_count);
            }
            if (null != integer && integer.length > 0) {
                page = String.valueOf(integer[0]);
            }
            Uri builtUri = Uri.parse(getString(R.string.url_base)).buildUpon()
                    .appendQueryParameter(getString(R.string.param_sort), sortType)
                    .appendQueryParameter(getString(R.string.param_count), count)
                    .appendQueryParameter(getString(R.string.param_api), getString(R.string.tmdb_api_key))
                    .appendQueryParameter(getString(R.string.param_page), page).build();
            try {
                url = new URL(builtUri.toString());
                Log.v(LOG_TAG, "BUILT URI" + builtUri.toString());
                httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod(getString(R.string.request_method_get));
                httpURLConnection.connect();
                InputStream inputStream = httpURLConnection.getInputStream();
                StringBuffer stringBuffer = new StringBuffer();
                if (null != inputStream) {
                    bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                }
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuffer.append(line + "\n");
                }
                responseJSONString = stringBuffer.toString();
                if (null != responseJSONString && responseJSONString.length() > 0) {
                    movieList = parseResponseJSONString(responseJSONString);
                }
            } catch (MalformedURLException e) {
                Log.e(LOG_TAG, "Malformed URL" + e);
            } catch (ProtocolException e) {
                Log.e(LOG_TAG, "Protocol Error" + e);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error in HTTP Connection" + e);
            } finally {
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e) {
                        Log.e(LOG_TAG, "Error in closing reader" + e);
                    }
                }
            }
            return movieList;
        }

        private List<Movie> parseResponseJSONString(String responseJSONString) {
            List<Movie> movieList = new ArrayList<>();
            try {
                JSONObject responseJSONObject = new JSONObject(responseJSONString);
                JSONArray resultsJSONArray = responseJSONObject.getJSONArray(getString(R.string.json_results));
                for (int i = 0; i < resultsJSONArray.length(); i++) {
                    Movie movie = new Movie();
                    JSONObject resultJSONObject = resultsJSONArray.getJSONObject(i);
                    String posterPath = resultJSONObject.getString(getString(R.string.json_poster_path));
                    posterPath = posterPath.replaceAll("\\/", "");
                    Uri imageUri = Uri.parse(getString(R.string.url_base_image)).buildUpon()
                            .appendPath(getString(R.string.poster_image_size))
                            .appendPath(posterPath).build();
                    movie.setMoviePosterURL(imageUri.toString());
                    movie.setMovieId(resultJSONObject.getLong(getString(R.string.json_id)));
                    movieList.add(movie);
                }
            } catch (JSONException e) {
                Log.e(LOG_TAG, "JSON Exception" + e);
            }
            return movieList;
        }


        @Override
        protected void onPostExecute(List<Movie> movieList) {
            super.onPostExecute(movieList);
            mAdapter.addData(movieList, mIsRefresh);
            mIsRefresh = false;
            mAdapter.notifyDataSetChanged();
            fetchMovieTaskListener.fetchMovieCompleted();
        }
    }
}
