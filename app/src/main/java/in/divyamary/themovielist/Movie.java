package in.divyamary.themovielist;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by divyamary on 28-12-2015.
 */
public class Movie implements Parcelable {
    private String moviePosterURL;
    private String movieOverview;
    private String movieReleaseDate;
    private String movieTitle;
    private String movieLanguage;
    private long movieId;
    private long moviePopularity;
    private long movieAvgRating;
    private Bitmap moviePoster;

    protected Movie(Parcel in) {
        moviePosterURL = in.readString();
        movieOverview = in.readString();
        movieReleaseDate = in.readString();
        movieTitle = in.readString();
        movieLanguage = in.readString();
        movieId = in.readLong();
        moviePopularity = in.readLong();
        movieAvgRating = in.readLong();
        moviePoster = in.readParcelable(Bitmap.class.getClassLoader());
    }

    public Movie() {
    }

    public static final Creator<Movie> CREATOR = new Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    public String getMoviePosterURL() {
        return moviePosterURL;
    }

    public void setMoviePosterURL(String moviePosterURL) {
        this.moviePosterURL = moviePosterURL;
    }

    public String getMovieOverview() {
        return movieOverview;
    }

    public void setMovieOverview(String movieOverview) {
        this.movieOverview = movieOverview;
    }

    public String getMovieReleaseDate() {
        return movieReleaseDate;
    }

    public void setMovieReleaseDate(String movieReleaseDate) {
        this.movieReleaseDate = movieReleaseDate;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
    }

    public long getMovieId() {
        return movieId;
    }

    public void setMovieId(long movieId) {
        this.movieId = movieId;
    }

    public String getMovieLanguage() {
        return movieLanguage;
    }

    public void setMovieLanguage(String movieLanguage) {
        this.movieLanguage = movieLanguage;
    }

    public long getMoviePopularity() {
        return moviePopularity;
    }

    public void setMoviePopularity(long moviePopularity) {
        this.moviePopularity = moviePopularity;
    }

    public long getMovieAvgRating() {
        return movieAvgRating;
    }

    public void setMovieAvgRating(long movieAvgRating) {
        this.movieAvgRating = movieAvgRating;
    }

    public Bitmap getMoviePoster() {
        return moviePoster;
    }

    public void setMoviePoster(Bitmap moviePoster) {
        this.moviePoster = moviePoster;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(moviePosterURL);
        parcel.writeString(movieOverview);
        parcel.writeString(movieReleaseDate);
        parcel.writeString(movieTitle);
        parcel.writeString(movieLanguage);
        parcel.writeLong(movieId);
        parcel.writeLong(moviePopularity);
        parcel.writeLong(movieAvgRating);
        parcel.writeParcelable(moviePoster, i);
    }
}
