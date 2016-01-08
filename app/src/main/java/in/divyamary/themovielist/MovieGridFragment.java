package in.divyamary.themovielist;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
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
 * A placeholder fragment containing a simple view.
 */
public class MovieGridFragment extends Fragment implements FetchMovieTaskListener, AdapterView.OnItemSelectedListener {

    public final static String MOVIE = "in.divyamary.sunshine.MOVIE";
    private RecyclerView mRecyclerView;
    private MovieRecyclerAdapter mAdapter;
    private GridLayoutManager mLayoutManager;
    private int visibleThreshold = 4;
    int firstVisibleItem, visibleItemCount, totalItemCount;
    private int currentPage = 1;
    private boolean loading = true;
    private List<Movie> mMoviesList;
    private boolean isRefresh = false;

    public MovieGridFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mMoviesList = new ArrayList<>();
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new GridLayoutManager(getContext(), 2);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new MovieRecyclerAdapter(mMoviesList);

        Spinner spinner = (Spinner) rootView.findViewById(R.id.spinner_sort);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.sort_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        spinner.setOnItemSelectedListener(this);

       /* Toolbar toolbar = (Toolbar)rootView.findViewById(R.id.tool_bar);
        ArrayList<String> spinnerArray = new ArrayList<String>();
        spinnerArray.add("Popularity");
        spinnerArray.add("Highest Rated");

        Spinner spinner = new Spinner(getContext(), Spinner.MODE_DROPDOWN);
        LinearLayout.LayoutParams layoutParams = new LinearLayout
                .LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.RIGHT;
        spinner.setLayoutParams(layoutParams);
        spinner.setGravity(Gravity.RIGHT);
        spinner.setPrompt("Sort by");
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, spinnerArray);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerArrayAdapter);
        spinnerArrayAdapter.notifyDataSetChanged();
        spinner.setOnItemSelectedListener(this);
        toolbar.addView(spinner);*/

        mRecyclerView.setAdapter(mAdapter);
        // Add scroll listener
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();
                visibleItemCount = mLayoutManager.getChildCount();
                totalItemCount = mLayoutManager.getItemCount();
                if (dy > 0) {
                    if (!loading) {
                        if (firstVisibleItem + visibleItemCount >= totalItemCount - visibleThreshold) {
                            new FetchMovieTask(new FetchMovieTaskListener() {
                                @Override
                                public void fetchMovieCompleted() {
                                    loading = false;
                                    currentPage = currentPage + 1;
                                }
                            }).execute(currentPage + 1);
                            loading = true;
                        }
                    }
                }
            }
        });
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (Utils.isInternetConnected(getContext())) {
            new FetchMovieTask(new FetchMovieTaskListener() {
                @Override
                public void fetchMovieCompleted() {
                    loading = false;
                }
            }).execute(currentPage);
        } else {
            Snackbar snackbar = Snackbar.make(getView().findViewById(R.id.recycler_view), "No Internet Connection!",
                    Snackbar.LENGTH_LONG);
            View snackBarView = snackbar.getView();
            snackBarView.setBackgroundColor(Color.argb(80, 0, 0, 0));
            snackbar.show();
        }
    }

    @Override
    public void fetchMovieCompleted() {
        loading = false;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
        String sortString = adapterView.getItemAtPosition(pos).toString();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(getString(R.string.pref_sort_type), sortString).commit();
        isRefresh = true;
        currentPage = 1;
        new FetchMovieTask(new FetchMovieTaskListener() {
            @Override
            public void fetchMovieCompleted() {
                loading = false;
            }
        }).execute(currentPage);

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    public class FetchMovieTask extends AsyncTask<Integer, Void, List<Movie>> {

        private static final String BASE_URL = "http://api.themoviedb.org/3/discover/movie";
        private static final String SORT_PARAM = "sort_by";
        private static final String CNT_PARAM = "vote_count.gte";
        private static final String PAGE_PARAM = "page";
        private static final String API_KEY_PARAM = "api_key";
        private static final String apiKey = "xxx";
        private static final String request_method = "GET";
        private FetchMovieTaskListener fetchMovieTaskListener;
        private Dialog dialog;
        String LOG_TAG = FetchMovieTask.class.getSimpleName();

        public FetchMovieTask(FetchMovieTaskListener listener) {
            this.fetchMovieTaskListener = listener;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //dialog = ProgressDialog.show(getContext(), "Loading Movies", "Please Wait...");
        }

        @Override
        protected List<Movie> doInBackground(Integer... integer) {
            List<Movie> movieList = new ArrayList<>();
            String responseJSONString;
            URL url;
            HttpURLConnection httpURLConnection = null;
            BufferedReader bufferedReader = null;
            String page = "";
            String count="";
            String sortType = "";
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            sortType = sharedPreferences.getString(getString(R.string.pref_sort_type), getString(R.string.pref_sort_default_value));
            if (sortType.equals("Popularity")) {
                sortType = "popularity.desc";
                count = "0";
            } else {
                sortType = "vote_average.desc";
                count = "100";
            }
            if (null != integer && integer.length > 0) {
                page = String.valueOf(integer[0]);
            }
            Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                    .appendQueryParameter(SORT_PARAM, sortType)
                    .appendQueryParameter(CNT_PARAM, count)
                    .appendQueryParameter(API_KEY_PARAM, apiKey)
                    .appendQueryParameter(PAGE_PARAM, page).build();
            try {
                url = new URL(builtUri.toString());
                Log.v(LOG_TAG, "BUILT URI" + builtUri.toString());
                httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod(request_method);
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
                Log.v(LOG_TAG, "Movie JSON String::" + responseJSONString);
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
                JSONArray resultsJSONArray = responseJSONObject.getJSONArray("results");
                for (int i = 0; i < resultsJSONArray.length(); i++) {
                    Movie movie = new Movie();
                    JSONObject resultJSONObject = resultsJSONArray.getJSONObject(i);
                    String posterPath = resultJSONObject.getString("poster_path");
                    posterPath = posterPath.replaceAll("\\/", "");
                    String baseURL = "http://image.tmdb.org/t/p/";
                    String imageSize = "w185";
                    Uri builtUri = Uri.parse(baseURL).buildUpon()
                            .appendPath(imageSize)
                            .appendPath(posterPath).build();
                    movie.setMoviePosterURL(builtUri.toString());
                    //movie.setMoviePoster(resolvePoster(posterPath));
                    movie.setMovieOverview(resultJSONObject.getString("overview"));
                    movie.setMovieReleaseDate(resultJSONObject.getString("release_date"));
                    movie.setMovieTitle(resultJSONObject.getString("title"));
                    movie.setMovieLanguage(resultJSONObject.getString("original_language"));
                    movie.setMovieId(resultJSONObject.getLong("id"));
                    movie.setMoviePopularity(resultJSONObject.getLong("popularity"));
                    movie.setMovieAvgRating(resultJSONObject.getLong("vote_average"));
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
            //dialog.dismiss();
            mAdapter.addData(movieList, isRefresh);
            isRefresh = false;
            //mAdapter.setLoading(false);
            Log.d(LOG_TAG, "MovieList size::" + movieList.size());
            mAdapter.notifyDataSetChanged();
            fetchMovieTaskListener.fetchMovieCompleted();
        }
    }
}
