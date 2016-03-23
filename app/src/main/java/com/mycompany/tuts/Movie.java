package com.mycompany.tuts;

import java.util.ArrayList;
import java.util.Date;

/**
 * Movie object
 */
public class Movie {
    private long id;
    private Date release_date;
    private String result;
    private int[] genres;
    private String title;
    private String overview;
    private String poster_path;
    private double vote;
    private ArrayList<String> cast;

    public Movie() {
    }

    public Movie(long id,
                 String title,
                 Date release_date, String overview, String poster_path, Double vote,
                 int[] genres, ArrayList<String> cast)

    {
        this.id = id;
        this.title = title;
        this.release_date = release_date;
        this.vote = vote;
        this.overview = overview;
        this.poster_path = poster_path;
        this.genres = genres;
        this.cast = cast;

    }

    public void setId(long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

     public void setVoteAverage(double vote) {
       this.vote = vote;
     }

    public void setDate(Date date) {
        this.release_date = date;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public void setPosterPath(String posterPath) {
        this.poster_path = posterPath;
    }

    public long getId() {
       return id;
     }

    public void setGenres(int[] genres) {this.genres = genres;}

    public void setCast(ArrayList<String> cast) {this.cast = cast;}

    public String getTitle() {
        return title;
     }

    public Date getDate() {
        return release_date;
    }

    public double getVoteAverage() {
        return vote;
    }

    public String getOverview() {
        return overview;
    }

    public String getPosterPath() {
        return poster_path;
    }

    public int[] getGenres() {
        return genres;
    }

    public int getGenre(int i) {
        return genres[i];
    }

    public ArrayList<String> getCast(ArrayList<String> cast) {
        return cast;
    }

    public String toString(){

        return "ID" + id +
                "Title" + title +
                "Date" + release_date +
                "Overview" + overview +
                "Poster Path" + poster_path;
        //plus genres plus actors?
    }
}
