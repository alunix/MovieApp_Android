package com.mycompany.tuts;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import java.sql.Date;
import java.util.ArrayList;

/**
 * Relational database allows user to save movies/delete and perform a search by genre.
 */
public class DBMovies {
    public static final int MOVIE = 0;
    public static final int GENRE = 1;
    public static final int MOVIE_GENRE = 2;
    private Movie movie;
    private MoviesHelper mHelper;
    private SQLiteDatabase mDatabase;

    public String queryTest = "";

    public DBMovies(Context context) {
        mHelper = new MoviesHelper(context);
        mDatabase = mHelper.getWritableDatabase();
        //mHelper.onCreate(mDatabase);
    }

    //check for duplicate movies
    public boolean isDuplicate(long id){
        Cursor movieCursor = mDatabase.query(MoviesHelper.TABLE_MOVIES,
                new String[]{"1"},
                mHelper.COLUMN_UID + " = " + id, //The columns for the where clause
                null, //The values for the where clause
                null, //Group the rows
                null, //Filter the row groups
                null); //The sort order

        return (movieCursor != null && movieCursor.moveToFirst());
    }

    //insert just selected movie
    public void insertMovie(Movie aMovie, boolean clearPrevious){

        ArrayList<Movie> list = new ArrayList<Movie>();
        list.add(aMovie);
        insertMovies(list, clearPrevious);
    }

    //insert the entire ArrayList of movies - not using
    public void insertMovies(ArrayList<Movie> listMovies, boolean clearPrevious) {
        if (clearPrevious) {
            //deleteMovies(table);
        }

        //create a sql prepared statement for each table
        String sql = "INSERT INTO " +  MoviesHelper.TABLE_MOVIES + " VALUES (?,?,?,?,?,?);";
        String sql3 = "INSERT INTO " +  MoviesHelper.TABLE_MOVIES_GENRES + " ("+mHelper.COLUMN_MOVIE_ID+", "+mHelper.COLUMN_GENRE_ID+") VALUES (?,?);";
          //table with composite keys is hardcoded below
        //compile the statement and start a transaction
        SQLiteStatement movieStatement = mDatabase.compileStatement(sql);
        SQLiteStatement genresStatement = mDatabase.compileStatement(sql3);
        mDatabase.beginTransaction();

        for (int i = 0; i < listMovies.size(); i++) {

                //for the current movie
            Movie currentMovie = listMovies.get(i);
            movieStatement.clearBindings();
            //for a given column index, bind the data to be put inside that index
            //getMovieId
            // statement.bindString(0, currentMovie.getId());
            movieStatement.bindLong(1, currentMovie.getId());
            movieStatement.bindString(2, currentMovie.getTitle());
            movieStatement.bindLong(3, currentMovie.getDate() == null ? -1 : currentMovie.getDate().getTime());
            movieStatement.bindDouble(4, currentMovie.getVoteAverage());
            movieStatement.bindString(5, currentMovie.getOverview());
            movieStatement.bindString(6, currentMovie.getPosterPath());

           // get the array of genres for this movie
            int[] currentGenres = currentMovie.getGenres();
            for (int j = 0; j < currentGenres.length; j++)
            {
                genresStatement.clearBindings();
                genresStatement.bindLong(1, currentMovie.getId());
                genresStatement.bindLong(2, currentGenres[j]);
                genresStatement.execute();
            }

            movieStatement.execute();
            //queryTest += "\n" + statement.toString();
        }

        mDatabase.setTransactionSuccessful();
        mDatabase.endTransaction();
    }

    public ArrayList<Movie> readMovies() {
        return readMoviesByGenre(-1);
    }

    public ArrayList<Movie> readMoviesByGenre(long genreId) {

        //make a new list to hold movies
        ArrayList<Movie> listMovies = new ArrayList<>();
        //get a list of columns to be retrieved
        String whereGenres = "";

        if(genreId != -1)
        {
            whereGenres = ", " + MoviesHelper.TABLE_MOVIES_GENRES + " MG" +
            " WHERE " + mHelper.COLUMN_MOVIE_ID +
                    " = M." + mHelper.COLUMN_UID +
                    " AND " + mHelper.COLUMN_GENRE_ID + " = " + genreId;
        }

        //SELECT M._id, title, etc
        //FROM Movies M, MoviesGenres MG
        //WHERE movie_id = M._id
        //AND genre_id = 80;
        String query = "SELECT M." + MoviesHelper.COLUMN_UID + "," +
                MoviesHelper.COLUMN_TITLE + "," +
                MoviesHelper.COLUMN_RELEASE_DATE + "," +
                MoviesHelper.COLUMN_VOTE + "," +
                MoviesHelper.COLUMN_OVERVIEW + "," +
                MoviesHelper.COLUMN_URL_THUMBNAIL +
                " FROM " + MoviesHelper.TABLE_MOVIES + " M" +
                whereGenres + ";";

            //why is this here?
        String[] movieGenreColumns = {MoviesHelper.COLUMN_UID,
                MoviesHelper.COLUMN_MOVIE_ID,
                MoviesHelper.COLUMN_GENRE_ID,
        };

        queryTest = query;

        //Create a cursor item for querying the database
        Cursor movieCursor = mDatabase.rawQuery(query, null);

        if (movieCursor != null && movieCursor.moveToFirst()) {

            do {
                //create a new movie object and retrieve the data from the cursor to be stored in this movie object
                Movie movie = new Movie();

                //each step is a 2 part process, find the index of the column first, find the data of that column using
                //that index and finally set our blank movie object to contain our data

                movie.setId(movieCursor.getLong(movieCursor.getColumnIndex(MoviesHelper.COLUMN_UID)));
                movie.setTitle(movieCursor.getString(movieCursor.getColumnIndex(MoviesHelper.COLUMN_TITLE)));

                long releaseDateMilliseconds = movieCursor.getLong(movieCursor.getColumnIndex(MoviesHelper.COLUMN_RELEASE_DATE));

                movie.setDate(releaseDateMilliseconds != -1 ? new Date(releaseDateMilliseconds) : null);
                movie.setVoteAverage(movieCursor.getInt(movieCursor.getColumnIndex(MoviesHelper.COLUMN_VOTE)));
                movie.setOverview(movieCursor.getString(movieCursor.getColumnIndex(MoviesHelper.COLUMN_OVERVIEW)));
                movie.setPosterPath(movieCursor.getString(movieCursor.getColumnIndex(MoviesHelper.COLUMN_URL_THUMBNAIL)));

                //create cursor to query the movies table
                Cursor genresCursor = mDatabase.query(MoviesHelper.TABLE_MOVIES_GENRES,
                        movieGenreColumns,
                        mHelper.COLUMN_MOVIE_ID + "=" + movie.getId(), //The columns for the where clause
                        null, //The values for the where clause
                        null, //Group the rows
                        null, //Filter the row groups
                        null); //The sort order

               // make ArrayList to hold genres to be returned with this movie
                ArrayList<Long> genres = new ArrayList<>();
                if (genresCursor != null && genresCursor.moveToFirst()) {
                  //add the genres to the movie that is being read
                    do {
                        genres.add(genresCursor.getLong(genresCursor.getColumnIndex(mHelper.COLUMN_GENRE_ID)));
                    }
                    while (genresCursor.moveToNext());

                    int[] genreInts = new int[genres.size()];
                    for(int i=0;i<genres.size();i++)
                    {
                        genreInts[i] = genres.get(i).intValue();
                    }
                    movie.setGenres(genreInts);
                }

                //add the movie to the list of movie objects which we plan to return
                listMovies.add(movie);
            }
            while (movieCursor.moveToNext());
        }
        return listMovies;
    }

    //for testing
    public String getGenreName(long genreId){
        Cursor genreCursor = mDatabase.query(MoviesHelper.TABLE_GENRES,
                new String[]{MoviesHelper.COLUMN_GENRE_NAME},
                MoviesHelper.COLUMN_UID +" = " + genreId, //The columns for the where clause
                null, //The values for the where clause
                null, //Group the rows
                null, //Filter the row groups
                null); //The sort order

        if(genreCursor != null && genreCursor.moveToFirst()){
            return genreCursor.getString(0);
        }
        else {
            return null;
        }

    }
    public long getGenreId(String genreName){
        Cursor genreCursor = mDatabase.query(MoviesHelper.TABLE_GENRES,
                new String[]{mHelper.COLUMN_UID},
                mHelper.COLUMN_GENRE_NAME+" = '"+genreName+"'", //The columns for the where clause
                null, //The values for the where clause
                null, //Group the rows
                null, //Filter the row groups
                null); //The sort order

        if(genreCursor != null && genreCursor.moveToFirst()){
            return genreCursor.getInt(0);
        }else {
            return -1;
        }

    }

    public void delete(long rowId) {

        // Define 'where' part of query.

        // delete movie with movie id where movie id - movie id
        String selection = mHelper.COLUMN_UID + " = ?";
        // Specify arguments in placeholder order.
        String[] selectionArgs = { String.valueOf(rowId) };
        // Issue SQL statement.
        mDatabase.delete(mHelper.TABLE_MOVIES, selection, selectionArgs);
        selection = mHelper.COLUMN_MOVIE_ID + " = ?";
        mDatabase.delete(mHelper.TABLE_MOVIES_GENRES,selection, selectionArgs);
    }

    ////////////////////////////////////////////////////////////////////
    //principle of least privilege - grant minimum access -can only access static fields
    // of outer class - if was non static class would exist as a separate instance
    // of every object of the outer class - non-static inner classes are associated with
    // instances of their enclosing classes.
    private static class MoviesHelper extends SQLiteOpenHelper {

        public static final String TABLE_MOVIES = "movies";
        public static final String TABLE_GENRES = "genres";
        public static final String TABLE_MOVIES_GENRES = "movies_genres";

        //all tables will use this for PK
        public static final String COLUMN_UID = "_id";

        //non-PK columns for Movies
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_RELEASE_DATE = "release_date";
        public static final String COLUMN_VOTE = "vote_average";
        public static final String COLUMN_OVERVIEW = "overview";
        public static final String COLUMN_URL_THUMBNAIL = "poster_path";

        //non-PK columns for Genres
        public static final String COLUMN_GENRE_NAME = "genre_name";

        //non-PK columns for MoviesGenres
        public static final String COLUMN_GENRE_ID = "genre_id";
        public static final String COLUMN_MOVIE_ID = "movie_id";

        private static final String CREATE_TABLE_MOVIES = "CREATE TABLE " + TABLE_MOVIES + " (" +
                COLUMN_UID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COLUMN_TITLE + " TEXT," +
                COLUMN_RELEASE_DATE + " INTEGER," +
                COLUMN_VOTE + " DOUBLE," +
                COLUMN_OVERVIEW + " TEXT," +
                COLUMN_URL_THUMBNAIL + " TEXT" +
                ");";

        private static final String CREATE_TABLE_GENRES = "CREATE TABLE " + TABLE_GENRES + " (" +
                COLUMN_UID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COLUMN_GENRE_NAME + " TEXT" +
                ");";

        //Composite primary key table - preloaded
        private static final String CREATE_TABLE_MOVIES_GENRES = "CREATE TABLE " + TABLE_MOVIES_GENRES + " (" +
                COLUMN_UID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COLUMN_MOVIE_ID + " INTEGER," +
                COLUMN_GENRE_ID + " INTEGER" +
                ");";

        private static final String DELETE_DATA =
                "DELETE FROM " + TABLE_MOVIES + "; " +
                "DELETE FROM " + TABLE_GENRES + "; " +
                "DELETE FROM " + TABLE_MOVIES_GENRES + ";";//

        private static final String PRELOAD_TABLE_GENRES =
                "INSERT INTO "+TABLE_GENRES+" VALUES(28,'Action'),"+
                "(12,'Adventure'),"+
                "(16,'Animation'),"+
                "(35,'Comedy'),"+
                "(80,'Crime'),"+
                "(99,'Documentary'),"+
                "(18,'Drama'),"+
                "(10751,'Family'),"+
                "(14,'Fantasy'),"+
                "(10769,'Foreign'),"+
                "(36,'History'),"+
                "(27,'Horror'),"+
                "(10402,'Music'),"+
                "(9648,'Mystery'),"+
                "(10749,'Romance'),"+
                "(979,'Science Fiction'),"+
                "(10770,'TV Movie'),"+
                "(53,'Thriller'),"+
                "(10752,'War'),"+
                "(37,'Western');";


        private static final String DB_NAME = "movies_db";
        private static final int DB_VERSION = 6;
        private Context mContext;

        public MoviesHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
            mContext = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            try {
                db.execSQL(CREATE_TABLE_MOVIES);
                db.execSQL(CREATE_TABLE_GENRES);
                db.execSQL(CREATE_TABLE_MOVIES_GENRES);

                db.execSQL(PRELOAD_TABLE_GENRES);
            } catch (SQLiteException exception) {
                throw exception;

            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

            try {

                db.execSQL("DROP TABLE IF EXISTS " + TABLE_MOVIES + ";");
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_GENRES + ";");
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_MOVIES_GENRES + ";");

                onCreate(db);
            } catch (SQLiteException exception) {

            }
        }

    }


}

