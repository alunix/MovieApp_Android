package com.mycompany.tuts;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.github.fafaldo.fabtoolbar.widget.FABToolbarLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Date;
import java.util.ArrayList;

/**
 * Pulls a movie API from IMDB Movies, allows search by genre, most popular, and in theatre.
 * Uses a progress bar and checks for internet connectivity. Uses Shared Preferences to style the
 * background. Passes contents to MovieDetails Activity.
 */

public class MovieAPI extends AppCompatActivity implements View.OnClickListener {

    // json object response url
    private final String API_KEY = "792b8b19f4ff5e7321cd29f9c26bb6ae";

    //set new urls
    private String action = "http://api.themoviedb.org/3/genre/28/movies?api_key=" + API_KEY;
    private String crime = "http://api.themoviedb.org/3/genre/80/movies?api_key=" + API_KEY;
    private String drama = "http://api.themoviedb.org/3/genre/18/movies?api_key=" + API_KEY;
    private String thriller = "http://api.themoviedb.org/3/genre/53/movies?api_key=" + API_KEY;
    private String mostPopular = "http://api.themoviedb.org/3/movie/popular?api_key=" + API_KEY;
    private String inTheatres = "http://api.themoviedb.org/3/movie/now_playing?api_key=" + API_KEY;


    private String urlJsonObj = inTheatres;
    private ArrayList<Movie> listMovies = new ArrayList<>();
    private MovieAdapter movieAdapter;

    private ProgressBar progressBar;
    private boolean connected=true;

    FABToolbarLayout fabToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_api);

            Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(true);

            getSupportActionBar().setTitle("Movies");
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            progressBar = (ProgressBar) findViewById(R.id.prog_bar);
            progressBar.setVisibility(View.VISIBLE);

            ImageView robot_img = (ImageView) findViewById(R.id.fab_img);
            ImageView film_img = (ImageView) findViewById(R.id.fab_film);
            ImageView rss_img = (ImageView) findViewById(R.id.fab_rss);
            robot_img.setOnClickListener(this);
            film_img.setOnClickListener(this);
            rss_img.setOnClickListener(this);
            fabToolbar = (FABToolbarLayout) findViewById(R.id.fabtoolbar);

        PreferenceManager.setDefaultValues(MovieAPI.this, R.xml.preferences, false);

        //check for internet connectivity
        if (!isConnected(MovieAPI.this)) {

            connected=false;
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builderSingle = new AlertDialog.Builder(MovieAPI.this);
            builderSingle.setIcon(R.drawable.ic_movie_black_24dp);

            builderSingle.setTitle("Message");
            builderSingle.setMessage("No Internet Connectivity")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });
            // Create the AlertDialog object and return it
            builderSingle.show();
        }


        if(connected) {

            VolleySingleton volleySingleton = VolleySingleton.getInstance(this);

            volleySingleton.getRequestQueue();
            makeJsonObjectRequest(urlJsonObj);

            movieAdapter = new MovieAdapter(MovieAPI.this, listMovies, MovieAdapter.WRITING);

            RecyclerView listMovieHits = (RecyclerView) findViewById(R.id.list_movie1);

            listMovieHits.setLayoutManager(new LinearLayoutManager(this));
            listMovieHits.setAdapter(movieAdapter);
            listMovieHits.setHasFixedSize(true);
            listMovieHits.setOnClickListener(this);
            listMovieHits.setNestedScrollingEnabled(true);

            //Create a new async task
            DelayTask delayTask = new DelayTask();
            //Execute the task
            delayTask.execute();
        }

   }

    public static boolean isConnected(Context context) {
        //Get the Connectivity_Service
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        //Get the information of the active network
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        //Return true if the network information isn't null and is connected to the internet
        // or connecting to the internet
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.fab_img:
                startActivity(new Intent(this, MainActivity.class));
                break;
            case R.id.fab_film:
                Toast.makeText(MovieAPI.this, "Get Movies!", Toast.LENGTH_SHORT).show();
                break;
            case R.id.fab_rss:
                startActivity(new Intent(this, RSSFeeds.class));
                break;
        }
        fabToolbar.hide();

        }

    public void getGenre() {
        //make a dialog for genres
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(MovieAPI.this);
        builderSingle.setIcon(R.drawable.ic_launcher);
        builderSingle.setTitle(R.string.pick_type);

        //set the adapter for genres
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                MovieAPI.this,
                android.R.layout.select_dialog_singlechoice);
        arrayAdapter.add("Action");
        arrayAdapter.add("Crime");
        arrayAdapter.add("Drama");
        arrayAdapter.add("Thriller");

        builderSingle.setNegativeButton(
                "cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        builderSingle.setAdapter(
                arrayAdapter,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String strName = arrayAdapter.getItem(which);
                        AlertDialog.Builder builderInner = new AlertDialog.Builder(
                                MovieAPI.this);
                        // builderInner.setMessage(strName);
                        //send new API query and call request method based on selection
                        if (which == 0) {
                            urlJsonObj = action;
                            makeJsonObjectRequest(urlJsonObj);
                        } else if (which == 1) {
                            urlJsonObj = crime;
                            makeJsonObjectRequest(urlJsonObj);
                        } else if (which == 2) {
                            urlJsonObj = drama;
                            makeJsonObjectRequest(urlJsonObj);
                        } else if (which == 3) {
                            urlJsonObj = thriller;
                            makeJsonObjectRequest(urlJsonObj);
                        }
                    }
                });
        builderSingle.show();
    }

    //is used for both parse responses
    private void makeJsonObjectRequest(String urlJsonObj) {
        //pass the method, the url,object to post(null), response listener, error listener
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                urlJsonObj,
                (String) null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        // Parsing json object response
                        listMovies = parseJSONResponse(response);
                        //set th adapter to hold the movies in ArrayList
                        movieAdapter.setMovieList(listMovies);
                        movieAdapter.notifyDataSetChanged();

                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Adding single request to request queue
        VolleySingleton.getInstance(this).addToRequestQueue(jsonObjReq);
    }

    //what if throws exception was here instead of a try catch?
    private ArrayList<Movie> parseJSONResponse(JSONObject response) {

       // StringBuilder data = new StringBuilder();//for debugging
        JSONArray arrayMovies = null;

        for (int i = 0; listMovies.size() > 0; ) {
            listMovies.remove(i);
        }

        if (response == null || response.length() == 0) {
            return listMovies;
        }
        //set default values - if missing from API, these values will be used
        long id = 0;
        String title = "N/A";
        String releaseDate = "N/A";
        String overview = "N/A";
        String poster_path = "N/A";

        int vote_average = -1; //in case it is empty

        try {
            //retrieve array containing the results from the API
            arrayMovies = response.getJSONArray("results");

            for (int i = 0; i < arrayMovies.length(); i++) {

                //make variable to hold JSONObject
                JSONObject currentMovie = arrayMovies.getJSONObject(i);
                //Create the movie and use the setters
                Movie movie = new Movie();

                id = currentMovie.getLong("id");
                //data.append(id + "\n");
                movie.setId(id);

                releaseDate = currentMovie.getString("release_date");
                if (currentMovie.has("release_date")) {
                    movie.setDate(Date.valueOf(releaseDate));
                }

                title = currentMovie.getString("title");

                if (currentMovie.has("title")) {
                    movie.setTitle(title);
                }

                poster_path = currentMovie.getString("poster_path");
                if (currentMovie.has("poster_path")) {
                    movie.setPosterPath(poster_path);
                }

                overview = currentMovie.getString("overview");
                if (currentMovie.has("overview")) {
                    movie.setOverview(overview);
                }

                JSONArray genreArray = currentMovie.getJSONArray("genre_ids");

                if (currentMovie.has("genre_ids")) {
                    int[] genres = new int[genreArray.length()];
                    //There is more than one integer for the genre
                    for (int j = 0; j < genreArray.length(); j++) {
                        genres[j] = genreArray.getInt(j);
                    }
                    movie.setGenres(genres);

                }

                vote_average = currentMovie.getInt("vote_average");

                if (currentMovie.has("vote_average")) {
                    movie.setVoteAverage(vote_average);
                }
                movie.setVoteAverage(vote_average);

                if (id != -1 && !title.equals("N/A")) {
                    //add movie to ArrayList
                    listMovies.add(movie);

                }
            }
           // Toast.makeText(MovieAPI.this, listMovies.size() + " items", Toast.LENGTH_SHORT).show();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
        }
        return listMovies;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_movie_api, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            //need SettingsActivity to know who sent the intent so pass class name
            //need to be able to go back to calling activity
            Bundle bundle = new Bundle();
            bundle.putString("ClassName", "MovieAPI");

            Intent newIntent = new Intent(this.getApplicationContext(), SettingsActivity.class);
            newIntent.putExtras(bundle);
            startActivityForResult(newIntent, 0);
        }

        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
        }

        if (id ==R.id.film_type) {
                //create the dialog
            AlertDialog.Builder builderSingle = new AlertDialog.Builder(MovieAPI.this);
            builderSingle.setIcon(R.drawable.ic_movie_black_24dp);
            builderSingle.setTitle(getString(R.string.movie_genres));

            //create adapter
            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                    MovieAPI.this,
                    android.R.layout.select_dialog_singlechoice);
            arrayAdapter.add("Most Popular");
            arrayAdapter.add("In Theatres");
            arrayAdapter.add("Genres");
            builderSingle.setNegativeButton(
                    "cancel",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            builderSingle.setAdapter(
                    arrayAdapter,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String strName = arrayAdapter.getItem(which);
                            AlertDialog.Builder builderInner = new AlertDialog.Builder(
                                    MovieAPI.this);
                            // builderInner.setMessage(strName);
                            //determine which one was selected
                            if (which == 0) {
                                //set the url
                                urlJsonObj = mostPopular;
                                //call the request
                                makeJsonObjectRequest(urlJsonObj);
                            } else if (which == 1) {
                                urlJsonObj = inTheatres;
                                makeJsonObjectRequest(urlJsonObj);
                            } else if (which == 2) {
                                //send new dialog for genre types
                                getGenre();
                            }
                        }
                    });
            builderSingle.show();

   }

            return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        showPreferences();
    }

    @Override
    protected void onResume() {
        super.onResume();
        showPreferences();
    }

    public void showPreferences() {
        //Use PreferenceManager to get default shared preferences
        SharedPreferences myPreference = PreferenceManager.getDefaultSharedPreferences(this);
        //Set the background color
        RelativeLayout setting_layout = ( RelativeLayout) findViewById(R.id.movie_rel_layout);
        //pass key and empty string for preference, preference is "in" the empty string
        String backColorPreference = myPreference.getString("colorType", "");
        if(backColorPreference.equals("Blue")){
            backColorPreference = "#90CAF9";}
        else if (backColorPreference.equals("Orange")){
            backColorPreference = "#FFE0B2";}
        else if (backColorPreference.equals("Gray")){
            backColorPreference = "#B0BEC5";}
        else if (backColorPreference.equals("Green")){
            backColorPreference = "#00796B";}
        else if (backColorPreference.equals("Black")){
            backColorPreference = "#000000";}
        else if (backColorPreference.equals("White")){
            backColorPreference = "#FFFFFF";}

        setting_layout.setBackgroundColor(Color.parseColor(backColorPreference));
    }

    //Used to create thread for progress bar
    public class DelayTask extends AsyncTask<Void, Integer, Void> {
        int count = 0;

        @Override
        protected void onPreExecute() {
            //Show the progress bar
            progressBar.setVisibility(ProgressBar.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            while (count < 3) {
                //Delay the system clock
                SystemClock.sleep(1000);
                count++;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressBar.setVisibility(View.INVISIBLE);

        }

    }

}
