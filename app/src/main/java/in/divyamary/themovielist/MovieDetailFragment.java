package in.divyamary.themovielist;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class MovieDetailFragment extends Fragment {

    public MovieDetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_movie_detail, container, false);
       /* Intent movieDetailIntent = getActivity().getIntent();
        if (null != movieDetailIntent) {
            if (movieDetailIntent.hasExtra(MovieGridFragment.MOVIE)) {
                long movieId = movieDetailIntent.getLongExtra(MovieGridFragment.MOVIE, 0);
                new FetchMovieDetailsTask().execute(movieId);
            }
        }*/
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        Intent movieDetailIntent = getActivity().getIntent();
        if (null != movieDetailIntent) {
            if (movieDetailIntent.hasExtra(MovieGridFragment.MOVIE)) {
                long movieId = movieDetailIntent.getLongExtra(MovieGridFragment.MOVIE, 0);
                new FetchMovieDetailsTask().execute(movieId);
            }
        }
    }

    public class FetchMovieDetailsTask extends AsyncTask<Long, Void, MovieDetails> {

        private String BASE_URL = "http://api.themoviedb.org/3/movie";
        private static final String APPEND_PARAM = "append_to_response";
        private static final String API_KEY_PARAM = "api_key";
        private static final String apiKey = "xxx";
        private static final String request_method = "GET";
        String LOG_TAG = FetchMovieDetailsTask.class.getSimpleName();


        @Override
        protected MovieDetails doInBackground(Long... longs) {
            String responseJSONString;
            URL url;
            HttpURLConnection httpURLConnection = null;
            BufferedReader bufferedReader = null;
            String sortType = "";
            String id = "";
            String appendParams = "credits,releases";
            MovieDetails movieDetails = null;
            if (null != longs && longs.length > 0) {
                id = String.valueOf(longs[0]);
            }
            Uri baseUri = Uri.parse(BASE_URL);
            Uri appendUri = Uri.withAppendedPath(baseUri, id).buildUpon()
                    .appendQueryParameter(API_KEY_PARAM, apiKey)
                    .appendQueryParameter(APPEND_PARAM, appendParams).build();
            try {
                url = new URL(appendUri.toString());
                Log.v(LOG_TAG, "BUILT URI" + appendUri.toString());
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
                    movieDetails = parseResponseJSONString(responseJSONString);
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
            return movieDetails;
        }

        private MovieDetails parseResponseJSONString(String responseJSONString) {
            List<String> genreList = new ArrayList<>();
            HashMap<String, String> castMap = new HashMap<>();
            MovieDetails movieDetails = new MovieDetails();
            try {
                JSONObject responseJSONObject = new JSONObject(responseJSONString);
                JSONArray genreJSONArray = responseJSONObject.getJSONArray("genres");
                for (int i = 0; i < genreJSONArray.length(); i++) {
                    genreList.add(genreJSONArray.getJSONObject(i).getString("name"));
                }
                movieDetails.setGenreList(genreList);
                String backdropPath = responseJSONObject.getString("backdrop_path");
                backdropPath = backdropPath.replaceAll("\\/", "");
                String baseURL1 = "http://image.tmdb.org/t/p/";
                String imageSize1 = "w500";
                Uri builtUri1 = Uri.parse(baseURL1).buildUpon()
                        .appendPath(imageSize1)
                        .appendPath(backdropPath).build();
                movieDetails.setMovieBackdropURL(builtUri1.toString());
                movieDetails.setBackdropPath(responseJSONObject.getString("backdrop_path"));
                movieDetails.setOriginalLanguage(responseJSONObject.getString("original_language"));
                movieDetails.setOriginalTitle(responseJSONObject.getString("title"));
                movieDetails.setOverview(responseJSONObject.getString("overview"));
                movieDetails.setPopularity(responseJSONObject.getLong("popularity"));
                String posterPath = responseJSONObject.getString("poster_path");
                posterPath = posterPath.replaceAll("\\/", "");
                String baseURL = "http://image.tmdb.org/t/p/";
                String imageSize = "w154";
                Uri builtUri = Uri.parse(baseURL).buildUpon()
                        .appendPath(imageSize)
                        .appendPath(posterPath).build();
                movieDetails.setMoviePosterURL(builtUri.toString());
                movieDetails.setPosterPath(responseJSONObject.getString("poster_path"));
                movieDetails.setReleaseDate(responseJSONObject.getString("release_date"));
                movieDetails.setRuntime(responseJSONObject.getInt("runtime"));
                movieDetails.setStatus(responseJSONObject.getString("status"));
                movieDetails.setVoteAverage(responseJSONObject.getDouble("vote_average"));
                movieDetails.setVoteCount(responseJSONObject.getInt("vote_count"));
                JSONObject creditsJSONObject = responseJSONObject.getJSONObject("credits");
                JSONArray crewJSONArray = creditsJSONObject.getJSONArray("crew");
                for (int i = 0; i < crewJSONArray.length(); i++) {
                    String job = crewJSONArray.getJSONObject(i).getString("job");
                    if (job.equals("Director")) {
                        movieDetails.setDirector(crewJSONArray.getJSONObject(i).getString("name"));
                        break;
                    }
                }
                JSONArray castJSONArray = creditsJSONObject.getJSONArray("cast");
                for (int i = 0; i < castJSONArray.length(); i++) {
                    String name = castJSONArray.getJSONObject(i).getString("name");
                    String profilePath = castJSONArray.getJSONObject(i).getString("profile_path");
                    castMap.put(name, profilePath);
                }
                JSONObject releasesJSONObject = responseJSONObject.getJSONObject("releases");
                JSONArray countriesJSONArray = releasesJSONObject.getJSONArray("countries");
                for (int i = 0; i < countriesJSONArray.length(); i++) {
                    String iso_3166_1 = countriesJSONArray.getJSONObject(i).getString("iso_3166_1");
                    if (iso_3166_1.equals("US")) {
                        movieDetails.setCertification(countriesJSONArray.getJSONObject(i).getString("certification"));
                        break;
                    }
                }
            } catch (JSONException e) {
                Log.e(LOG_TAG, "JSON Exception" + e);
            }
            return movieDetails;
        }


        @Override
        protected void onPostExecute(MovieDetails movieDetails) {
            super.onPostExecute(movieDetails);
            final TextView overviewTextView = (TextView) getView().findViewById(R.id.overview);
            overviewTextView.setText(movieDetails.getOverview());
            final TextView titleTextView = (TextView) getView().findViewById(R.id.title);
            titleTextView.setText(movieDetails.getOriginalTitle());
            final TextView voteAvgTextView = (TextView) getView().findViewById(R.id.vote_average);
            voteAvgTextView.setText(String.format("%.1f",movieDetails.getVoteAverage()));
            final TextView voteCountTextView = (TextView) getView().findViewById(R.id.vote_count);
            voteCountTextView.setText(String.valueOf(movieDetails.getVoteCount()) + " votes");
            final TextView runtimeTextView = (TextView) getView().findViewById(R.id.runtime);
            runtimeTextView.setText(String.valueOf(movieDetails.getRuntime()));
            String releaseDate = movieDetails.getReleaseDate();
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-DD");
            Date date = null;
            final TextView yearTextView = (TextView) getView().findViewById(R.id.release_year);
            final TextView ddmmTextView = (TextView) getView().findViewById(R.id.release_ddmm);
            try {
                date = formatter.parse(releaseDate);
                SimpleDateFormat newFormat = new SimpleDateFormat("yyyy.MMM d");
                String formattedReleaseDate = newFormat.format(date);
                String dateArray[] = formattedReleaseDate.split("\\.");
                String year = dateArray[0];
                String dayMonth = dateArray[1];
                Log.d(LOG_TAG, "year:" + year + "day-month:" + dayMonth);
                yearTextView.setText(year);
                ddmmTextView.setText(dayMonth);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            TextView directorTextView = (TextView) getView().findViewById(R.id.director);
            directorTextView.setText(movieDetails.getDirector());
            TextView certificateTextView = (TextView) getView().findViewById(R.id.certificate);
            certificateTextView.setText(movieDetails.getCertification());
            LinearLayout genreLayout = (LinearLayout) getView().findViewById(R.id.genre_layout);
            for (String genre : movieDetails.getGenreList()) {
                LinearLayout childGenreLayout = new LinearLayout(getContext());
                childGenreLayout.setOrientation(LinearLayout.HORIZONTAL);
                LinearLayout.LayoutParams childGenreLayoutParam = new LinearLayout
                        .LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                childGenreLayout.setLayoutParams(childGenreLayoutParam);
                ImageView genreImageView = new ImageView(getContext());
                TableRow.LayoutParams imagelayoutParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                        TableRow.LayoutParams.WRAP_CONTENT);
                imagelayoutParams.rightMargin = 5;
                genreImageView.setLayoutParams(imagelayoutParams);
                Genre genreType = Genre.getFromName(genre);
                switch (genreType) {
                    case ACTION:
                        genreImageView.setImageDrawable(getView().getResources().getDrawable(R.drawable.action));
                        break;
                    case ADVENTURE:
                        genreImageView.setImageDrawable(getView().getResources().getDrawable(R.drawable.adventure));
                        break;
                    case ANIMATION:
                        genreImageView.setImageDrawable(getView().getResources().getDrawable(R.drawable.animation));
                        break;
                    case COMEDY:
                        genreImageView.setImageDrawable(getView().getResources().getDrawable(R.drawable.comedy));
                        break;
                    case CRIME:
                        genreImageView.setImageDrawable(getView().getResources().getDrawable(R.drawable.crime));
                        break;
                    case DOCUMENTARY:
                        genreImageView.setImageDrawable(getView().getResources().getDrawable(R.drawable.documentary));
                        break;
                    case DRAMA:
                        genreImageView.setImageDrawable(getView().getResources().getDrawable(R.drawable.drama));
                        break;
                    case FAMILY:
                        genreImageView.setImageDrawable(getView().getResources().getDrawable(R.drawable.family));
                        break;
                    case FANTASY:
                        genreImageView.setImageDrawable(getView().getResources().getDrawable(R.drawable.fantasy));
                        break;
                    case FOREIGN:
                        genreImageView.setImageDrawable(getView().getResources().getDrawable(R.drawable.foreign));
                        break;
                    case HISTORY:
                        genreImageView.setImageDrawable(getView().getResources().getDrawable(R.drawable.history));
                        break;
                    case HORROR:
                        genreImageView.setImageDrawable(getView().getResources().getDrawable(R.drawable.horror));
                        break;
                    case MUSICAL:
                        genreImageView.setImageDrawable(getView().getResources().getDrawable(R.drawable.music));
                        break;
                    case MYSTERY:
                        genreImageView.setImageDrawable(getView().getResources().getDrawable(R.drawable.mystery));
                        break;
                    case ROMANCE:
                        genreImageView.setImageDrawable(getView().getResources().getDrawable(R.drawable.romance));
                        break;
                    case SCI_FI:
                        genreImageView.setImageDrawable(getView().getResources().getDrawable(R.drawable.scifi));
                        break;
                    case THRILLER:
                        genreImageView.setImageDrawable(getView().getResources().getDrawable(R.drawable.thriller));
                        break;
                    case TV_MOVIE:
                        genreImageView.setImageDrawable(getView().getResources().getDrawable(R.drawable.tv));
                        break;
                    case WAR:
                        genreImageView.setImageDrawable(getView().getResources().getDrawable(R.drawable.war));
                        break;
                    case WESTERN:
                        genreImageView.setImageDrawable(getView().getResources().getDrawable(R.drawable.western));
                        break;
                }
                TextView genreView = new TextView(getContext());
                TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                        TableRow.LayoutParams.WRAP_CONTENT);
                genreView.setLayoutParams(layoutParams);
                genreView.setTextColor(getResources().getColor(R.color.textColor));
                genreView.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
                genreView.setText(genre);
                childGenreLayout.addView(genreImageView);
                childGenreLayout.addView(genreView);
                genreLayout.addView(childGenreLayout);
            }
            ImageView posterView = (ImageView) getView().findViewById(R.id.poster);
            Picasso.with(getContext()).load(movieDetails.getMoviePosterURL()).into(posterView);
            ImageView backdropView = (ImageView) getView().findViewById(R.id.backdrop);
            Picasso.with(getContext())
                    .load(movieDetails.getMovieBackdropURL())
                    .transform(PaletteTransformation.instance())
                    .into(backdropView, new PaletteTransformation.PaletteCallback(backdropView) {
                        @Override
                        public void onError() {
                            Log.e(LOG_TAG, "Palette Error");
                        }

                        @Override
                        public void onSuccess(Palette palette) {
                            int defaultColor = getResources().getColor(R.color.textColor);
                            titleTextView.setTextColor(palette.getDarkVibrantColor(defaultColor));
                            voteAvgTextView.setTextColor(palette.getDarkMutedColor(defaultColor));
                            voteCountTextView.setTextColor(palette.getDarkMutedColor(defaultColor));
                            runtimeTextView.setTextColor(palette.getDarkMutedColor(defaultColor));
                            yearTextView.setTextColor(palette.getDarkMutedColor(defaultColor));
                            ddmmTextView.setTextColor(palette.getDarkMutedColor(defaultColor));
                            TextView minTitleTextView = (TextView) getView().findViewById(R.id.minutes);
                            minTitleTextView.setTextColor(palette.getDarkMutedColor(defaultColor));
                            TextView oveviewTitleTextView = (TextView) getView().findViewById(R.id.overview_title);
                            TextView directorTitleTextView = (TextView) getView().findViewById(R.id.director_title);
                            TextView certTitleTextView = (TextView) getView().findViewById(R.id.certification_title);
                            oveviewTitleTextView.setTextColor(palette.getDarkVibrantColor(defaultColor));
                            certTitleTextView.setTextColor(palette.getDarkVibrantColor(defaultColor));
                            directorTitleTextView.setTextColor(palette.getDarkVibrantColor(defaultColor));
                        }
                    });
        }
    }
}
