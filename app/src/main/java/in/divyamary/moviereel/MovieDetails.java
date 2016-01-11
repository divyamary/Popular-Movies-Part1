package in.divyamary.moviereel;

import java.util.List;
import java.util.Map;

/**
 * Created by divyamary on 03-01-2016.
 */
public class MovieDetails {

    private String backdropPath;
    private String originalLanguage;
    private String originalTitle;
    private String overview;
    private long popularity;
    private String posterPath;
    private String releaseDate;
    private int runtime;
    private String status;
    private double voteAverage;
    private int voteCount;
    private List<String> genreList;
    private Map<String, String> castMap;
    private String moviePosterURL;
    private String movieBackdropURL;
    private String director;
    private String certification;

    public String getBackdropPath() {
        return backdropPath;
    }

    public void setBackdropPath(String backdropPath) {
        this.backdropPath = backdropPath;
    }

    public String getOriginalLanguage() {
        return originalLanguage;
    }

    public void setOriginalLanguage(String originalLanguage) {
        this.originalLanguage = originalLanguage;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    public void setOriginalTitle(String originalTitle) {
        this.originalTitle = originalTitle;
    }

    public long getPopularity() {
        return popularity;
    }

    public void setPopularity(long popularity) {
        this.popularity = popularity;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public int getRuntime() {
        return runtime;
    }

    public void setRuntime(int runtime) {
        this.runtime = runtime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getVoteAverage() {
        return voteAverage;
    }

    public void setVoteAverage(double voteAverage) {
        this.voteAverage = voteAverage;
    }

    public int getVoteCount() {
        return voteCount;
    }

    public void setVoteCount(int voteCount) {
        this.voteCount = voteCount;
    }

    public List<String> getGenreList() {
        return genreList;
    }

    public void setGenreList(List<String> genreList) {
        this.genreList = genreList;
    }

    public Map<String, String> getCastMap() {
        return castMap;
    }

    public void setCastMap(Map<String, String> castMap) {
        this.castMap = castMap;
    }

    public void setMoviePosterURL(String moviePosterURL) {
        this.moviePosterURL = moviePosterURL;
    }

    public String getMoviePosterURL() {
        return moviePosterURL;
    }

    public void setMovieBackdropURL(String movieBackdropURL) {
        this.movieBackdropURL = movieBackdropURL;
    }

    public String getMovieBackdropURL() {
        return movieBackdropURL;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public String getDirector() {
        return director;
    }

    public void setCertification(String certification) {
        this.certification = certification;
    }

    public String getCertification() {
        return certification;
    }
}
