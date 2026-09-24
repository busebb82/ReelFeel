package com.reelfeel;

import javax.swing.ImageIcon;

public class Film {

    // Gemini'den gelen bilgiler
    private String title;
    private int year;
    private String reason;

    // TMDB'den gelen bilgiler (TMDB anahtarı yoksa boş kalır)
    private String overview;
    private double rating;
    private String posterPath;
    private transient ImageIcon poster; // transient: Gson bu alanı JSON'da aramasın

    public Film() {
    }

    public Film(String title, int year, String reason) {
        this.title = title;
        this.year = year;
        this.reason = reason;
    }

    public String getTitle() {
        return title;
    }

    public int getYear() {
        return year;
    }

    public String getReason() {
        return reason;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public ImageIcon getPoster() {
        return poster;
    }

    public void setPoster(ImageIcon poster) {
        this.poster = poster;
    }
}
