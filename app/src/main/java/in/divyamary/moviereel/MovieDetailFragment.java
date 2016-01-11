package in.divyamary.moviereel;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.view.Gravity;
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
 * MovieDetailFragment displays details about the selected movie.
 */
public class MovieDetailFragment extends Fragment {

    public MovieDetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_movie_detail, container, false);
        Intent movieDetailIntent = getActivity().getIntent();
        if (null != movieDetailIntent) {
            if (movieDetailIntent.hasExtra(MovieGridFragment.MOVIE)) {
                long movieId = movieDetailIntent.getLongExtra(MovieGridFragment.MOVIE, 0);
                if (Utils.isInternetConnected(getContext())) {
                    new FetchMovieDetailsTask().execute(movieId);
                } else {
                    Snackbar snackbar = Snackbar.make(rootView.findViewById(R.id.fragment_detail_container),
                            getString(R.string.no_internet),
                            Snackbar.LENGTH_LONG);
                    View snackBarView = snackbar.getView();
                    snackBarView.setBackgroundColor(getResources().getColor(R.color.grey_900));
                    snackbar.show();
                }
            }
        }
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    public class FetchMovieDetailsTask extends AsyncTask<Long, Void, MovieDetails> {

        final String LOG_TAG = FetchMovieDetailsTask.class.getSimpleName();


        @Override
        protected MovieDetails doInBackground(Long... longs) {
            String responseJSONString;
            URL url;
            HttpURLConnection httpURLConnection = null;
            BufferedReader bufferedReader = null;
            String id = "";
            MovieDetails movieDetails = null;
            if (null != longs && longs.length > 0) {
                id = String.valueOf(longs[0]);
            }
            Uri baseUri = Uri.parse(getString(R.string.url_base_movie));
            Uri appendUri = Uri.withAppendedPath(baseUri, id).buildUpon()
                    .appendQueryParameter(getString(R.string.param_api), getString(R.string.tmdb_api_key))
                    .appendQueryParameter(getString(R.string.param_append), getString(R.string.append_values)).build();
            try {
                url = new URL(appendUri.toString());
                Log.v(LOG_TAG, "BUILT URI" + appendUri.toString());
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
                Log.v(LOG_TAG, "RESPONSE JSON" + responseJSONString);
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
                JSONArray genreJSONArray = responseJSONObject.getJSONArray(getString(R.string.json_genres));
                for (int i = 0; i < genreJSONArray.length(); i++) {
                    genreList.add(genreJSONArray.getJSONObject(i).getString(getString(R.string.json_name)));
                }
                movieDetails.setGenreList(genreList);
                String backdropPath = responseJSONObject.getString(getString(R.string.json_backdrop_path));
                backdropPath = backdropPath.replaceAll("\\/", "");
                Uri builtUri1 = Uri.parse(getString(R.string.url_base_image)).buildUpon()
                        .appendPath(getString(R.string.backdrop_image_size))
                        .appendPath(backdropPath).build();
                movieDetails.setMovieBackdropURL(builtUri1.toString());
                movieDetails.setBackdropPath(responseJSONObject.getString(getString(R.string.json_backdrop_path)));
                movieDetails.setOriginalLanguage(responseJSONObject.getString(getString(R.string.json_orig_lang)));
                String origTitle = responseJSONObject.getString(getString(R.string.json_title));
                if (null != origTitle && origTitle.length() > 0) {
                    movieDetails.setOriginalTitle(origTitle);
                } else {
                    movieDetails.setOriginalTitle(getString(R.string.text_not_available));
                }
                String overview = responseJSONObject.getString(getString(R.string.json_overview));
                if (null != overview && overview.length() > 0) {
                    movieDetails.setOverview(overview);
                } else {
                    movieDetails.setOverview(getString(R.string.text_not_available));
                }
                movieDetails.setPopularity(responseJSONObject.getLong(getString(R.string.json_popularity)));
                String posterPath = responseJSONObject.getString(getString(R.string.json_poster_path));
                posterPath = posterPath.replaceAll("\\/", "");
                Uri builtUri = Uri.parse(getString(R.string.url_base_image)).buildUpon()
                        .appendPath(getString(R.string.poster_image_size))
                        .appendPath(posterPath).build();
                movieDetails.setMoviePosterURL(builtUri.toString());
                movieDetails.setPosterPath(responseJSONObject.getString(getString(R.string.json_poster_path)));
                String releaseDate = responseJSONObject.getString(getString(R.string.json_release_date));
                if (null != releaseDate && releaseDate.length() > 0) {
                    movieDetails.setReleaseDate(releaseDate);
                } else {
                    movieDetails.setReleaseDate(getString(R.string.text_not_available));
                }
                movieDetails.setRuntime(responseJSONObject.getInt(getString(R.string.json_runtime)));
                movieDetails.setStatus(responseJSONObject.getString(getString(R.string.json_status)));
                movieDetails.setVoteAverage(responseJSONObject.getDouble(getString(R.string.json_vote_avg)));
                movieDetails.setVoteCount(responseJSONObject.getInt(getString(R.string.json_vote_count)));
                JSONObject creditsJSONObject = responseJSONObject.getJSONObject(getString(R.string.json_credits));
                JSONArray crewJSONArray = creditsJSONObject.getJSONArray(getString(R.string.json_crew));
                for (int i = 0; i < crewJSONArray.length(); i++) {
                    String job = crewJSONArray.getJSONObject(i).getString(getString(R.string.json_job));
                    if (job.equals(getString(R.string.json_Director))) {
                        String directorName = crewJSONArray.getJSONObject(i).getString(getString(R.string.json_name));
                        if (null != directorName && directorName.length() > 0) {
                            movieDetails.setDirector(directorName);
                        } else {
                            movieDetails.setDirector(getString(R.string.text_not_available));
                        }
                        break;
                    }
                }
                JSONArray castJSONArray = creditsJSONObject.getJSONArray(getString(R.string.json_cast));
                for (int i = 0; i < castJSONArray.length(); i++) {
                    String name = castJSONArray.getJSONObject(i).getString(getString(R.string.json_name));
                    String profilePath = castJSONArray.getJSONObject(i).getString(getString(R.string.json_profile_path));
                    castMap.put(name, profilePath);
                }
                JSONObject releasesJSONObject = responseJSONObject.getJSONObject(getString(R.string.json_releases));
                JSONArray countriesJSONArray = releasesJSONObject.getJSONArray(getString(R.string.json_countries));
                for (int i = 0; i < countriesJSONArray.length(); i++) {
                    JSONObject certificationJSONObject = countriesJSONArray.getJSONObject(i);
                    String iso_3166_1 = certificationJSONObject.getString(getString(R.string.json_iso_3166_1));
                    if (iso_3166_1.equals(getString(R.string.json_US))) {
                        if (null != certificationJSONObject.getString(getString(R.string.json_certification)) &&
                                certificationJSONObject.getString(getString(R.string.json_certification)).length() > 0) {
                            movieDetails.setCertification(certificationJSONObject.getString(getString(R.string.json_certification)));
                        } else {
                            movieDetails.setCertification(getString(R.string.text_not_available));
                        }
                        break;
                    } else {
                        movieDetails.setCertification(getString(R.string.text_not_available));
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
            voteAvgTextView.setText(String.format(getString(R.string.vote_format), movieDetails.getVoteAverage()));
            final TextView voteCountTextView = (TextView) getView().findViewById(R.id.vote_count);
            voteCountTextView.setText(String.valueOf(movieDetails.getVoteCount()) + getString(R.string.text_votes));
            final TextView runtimeTextView = (TextView) getView().findViewById(R.id.runtime);
            runtimeTextView.setText(String.valueOf(movieDetails.getRuntime()));
            //Format the date into yyyy.MMM d format and split the year from it.
            String releaseDate = "";
            final TextView yearTextView = (TextView) getView().findViewById(R.id.release_year);
            final TextView ddmmTextView = (TextView) getView().findViewById(R.id.release_ddmm);
            if (movieDetails.getReleaseDate().length() > 0) {
                releaseDate = movieDetails.getReleaseDate();
                DateFormat formatter = new SimpleDateFormat(getString(R.string.date_format));
                Date date = null;
                try {
                    date = formatter.parse(releaseDate);
                    SimpleDateFormat newFormat = new SimpleDateFormat(getString(R.string.date_format_new));
                    String formattedReleaseDate = newFormat.format(date);
                    String dateArray[] = formattedReleaseDate.split("\\.");
                    String year = dateArray[0];
                    String dayMonth = dateArray[1];
                    yearTextView.setText(year);
                    ddmmTextView.setText(dayMonth);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } else {
                yearTextView.setText(getString(R.string.text_not_available));
                ddmmTextView.setText(getString(R.string.text_not_available));
            }
            TextView directorTextView = (TextView) getView().findViewById(R.id.director);
            directorTextView.setText(movieDetails.getDirector());
            TextView certificateTextView = (TextView) getView().findViewById(R.id.certificate);
            certificateTextView.setText(movieDetails.getCertification());
            /**Creating horizontal linear layouts with a textview and imageview and adding it to genre_layout
             based on the genre types of a given movie */
            LinearLayout genreLayout = (LinearLayout) getView().findViewById(R.id.genre_layout);
            for (String genre : movieDetails.getGenreList()) {
                LinearLayout childGenreLayout = new LinearLayout(getContext());
                childGenreLayout.setOrientation(LinearLayout.HORIZONTAL);
                LinearLayout.LayoutParams childGenreLayoutParam = new LinearLayout
                        .LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                childGenreLayoutParam.bottomMargin = getResources().getInteger(R.integer.layout_bottom_margin);
                childGenreLayout.setLayoutParams(childGenreLayoutParam);
                ImageView genreImageView = new ImageView(getContext());
                TableRow.LayoutParams imagelayoutParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                        TableRow.LayoutParams.WRAP_CONTENT);
                imagelayoutParams.rightMargin = getResources().getInteger(R.integer.layout_right_margin);
                imagelayoutParams.gravity = Gravity.CENTER;
                genreImageView.setLayoutParams(imagelayoutParams);
                //Get the image drawable based on the genre name
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
                genreView.setTextColor(getResources().getColor(R.color.grey_900));
                genreView.setTypeface(Typeface.create(getString(R.string.sans_serif_light), Typeface.NORMAL));
                genreView.setText(genre);
                childGenreLayout.addView(genreImageView);
                childGenreLayout.addView(genreView);
                genreLayout.addView(childGenreLayout);
            }
            ImageView posterView = (ImageView) getView().findViewById(R.id.poster);
            Picasso.with(getContext()).load(movieDetails.getMoviePosterURL()).into(posterView);
            ImageView backdropView = (ImageView) getView().findViewById(R.id.backdrop);
            // Load movie backdrop and generate a palette from it.
            Picasso.with(getContext())
                    .load(movieDetails.getMovieBackdropURL())
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.no_image)
                    .transform(PaletteTransformation.instance())
                    .into(backdropView, new PaletteTransformation.PaletteCallback(backdropView) {
                        @Override
                        public void onError() {
                            Log.e(LOG_TAG, "Palette Error");
                        }

                        @Override
                        public void onSuccess(Palette palette) {
                            int defaultColor = getResources().getColor(R.color.grey_900);
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
