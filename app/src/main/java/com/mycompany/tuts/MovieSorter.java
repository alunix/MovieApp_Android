package com.mycompany.tuts;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by Amanda on 26/11/2015.
 */
public class MovieSorter {


    public MovieSorter(){

    }

    public static void sortByName(ArrayList<Movie> movies){
        Collections.sort(movies, new Comparator<Movie>() {
            @Override
            public int compare(Movie lhs, Movie rhs) {
                return lhs.getTitle().compareTo(rhs.getTitle());
            }
        });
    }
    public static void sortByDate(ArrayList<Movie> movies){

        Collections.sort(movies, new Comparator<Movie>() {
            @Override
            public int compare(Movie lhs, Movie rhs) {
                Date lhsDate= (Date) lhs.getDate();
                Date rhsDate= (Date) rhs.getDate();
                if(lhs.getDate()!=null && rhs.getDate()!=null)
                {
                    return rhs.getDate().compareTo(lhs.getDate());
                }
                else {
                    return 0;
                }

            }
        });
    }
    public static void sortByRating(ArrayList<Movie> movies){
        Collections.sort(movies, new Comparator<Movie>() {
            @Override
            public int compare(Movie lhs, Movie rhs) {
                double ratingLhs=lhs.getVoteAverage();
                double ratingRhs=rhs.getVoteAverage();
                if(ratingLhs<ratingRhs)
                {
                    return 1;
                }
                else if(ratingLhs>ratingRhs)
                {
                    return -1;
                }
                else
                {
                    return 0;
                }
            }
        });
    }
}
