package com.mycompany.tuts;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Slide;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Display the details of the movie and gets the cast list. Uses progress bar with async task.
 */
public class MovieDetails extends AppCompatActivity{

    private TextView movieDetails;
    private ImageView imageView;
    private final String API_KEY = "api_key=792b8b19f4ff5e7321cd29f9c26bb6ae";

    private String theMovieURL = "http://api.themoviedb.org/3/movie/";
    private ArrayList<String> castMovies = new ArrayList<>();

    private ProgressBar progressBar;
    private Movie movie;//will hold the movie
    //get movie that already exists

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        if(Build.VERSION.SDK_INT >=21){
            TransitionInflater inflater = TransitionInflater.from(this);
            Transition transition= inflater.inflateTransition(R.transition.slide_fade);
            getWindow().setExitTransition(transition);
            //Seems can only have one transition type at a time
            Slide slide = new Slide();
            slide.setDuration(1000);
            getWindow().setEnterTransition(slide);
            getWindow().setReenterTransition(slide);
            getWindow().setReturnTransition(TransitionInflater.from(this).inflateTransition(R.transition.slide_fade));
        }

        //set default settings
        PreferenceManager.setDefaultValues(MovieDetails.this, R.xml.preferences, false);

        movieDetails  = (TextView) findViewById(R.id.movieDetails);
        TextView movieTitle = (TextView) findViewById(R.id.titleDetail);
        TextView overview = (TextView) findViewById(R.id.overview);
        imageView= (ImageView) findViewById(R.id.movieThumbnail);
        TextView castTitle = (TextView) findViewById(R.id.castTitle);
        TextView summaryLabel = (TextView) findViewById(R.id.summaryLabel);

        Typeface heading = Typeface.createFromAsset(getAssets(),
                "fonts/Acme-Regular.ttf");
        Typeface subs = Typeface.createFromAsset(getAssets(),
                "fonts/AbrilFatface-Regular.ttf");
        Typeface cast = Typeface.createFromAsset(getAssets(),
                "fonts/Audiowide-Regular.ttf");

        movieTitle.setTypeface(heading);// movie title
        castTitle.setTypeface(subs);//cast Label
        summaryLabel.setTypeface(subs);//overview label
        overview.setTypeface(heading);//overview
        movieDetails.setTypeface(cast);//cast

        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        getSupportActionBar().setTitle("Movie Details");
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressBar = (ProgressBar) findViewById(R.id.prog_bar);
        progressBar.setVisibility(View.VISIBLE);

        Intent intent = getIntent();
        String passedID = intent.getStringExtra("movie_id");
        String passedTitle = intent.getStringExtra("movie_title");
        String passedPosterPath = intent.getStringExtra("poster_path");

        String passedOverview = intent.getStringExtra("overview");

        theMovieURL += passedID +"/credits?" + API_KEY;

        movieTitle.setText(passedTitle);
        overview.setText(passedOverview);

        //Need to get the passed image
        String urlThumbnail = passedPosterPath;

        VolleySingleton volleySingleton1 = VolleySingleton.getInstance(this);
        ImageLoader imageLoader = volleySingleton1.getImageLoader();

        if(urlThumbnail != null && !urlThumbnail.equals("null")) {
            imageLoader.get("https://image.tmdb.org/t/p/w185" + urlThumbnail, new ImageLoader.ImageListener() {

                @Override
                public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                    imageView.setImageBitmap(response.getBitmap());
                }

                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });
        } else {
            //Toast.makeText(movieAPI, "Thumb is: "+urlThumbnail, Toast.LENGTH_SHORT).show();

            //set default image
            imageView.setImageResource(R.drawable.ic_robot);
            String pathName = "http://i.imgur.com/7spzG.png";

            imageLoader.get(pathName, new ImageLoader.ImageListener() {

                @Override
                public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                    imageView.setImageBitmap(response.getBitmap());
                }

                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });
        }

        //Create a new async task
        DelayTask delayTask = new DelayTask();
        //Execute the task
        delayTask.execute();

        VolleySingleton volleySingleton = VolleySingleton.getInstance(this);
        volleySingleton.getRequestQueue();

        makeCastJsonObjectRequest(theMovieURL);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_movie_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        showPreferences();
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

        public void showPreferences() {
        //Use PreferenceManager to get default shared preferences
        SharedPreferences myPreference = PreferenceManager.getDefaultSharedPreferences(this);
        //Set the background color
        RelativeLayout setting_layout = ( RelativeLayout) findViewById(R.id.details_layout);
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

    //is used for both parse responses
    private void makeCastJsonObjectRequest(String urlJsonObj) {
        //pass the method, the url,object to post(null), response listener, error listener
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                urlJsonObj,
                (String) null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        // Parsing json object response
                        castMovies = parseCastResponse(response);

                        String allCast="";
                        for(int i=0;i<castMovies.size();i++) {
                            allCast += castMovies.get(i)  + '\n';
                        }
                        movieDetails.setText(allCast);

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

    private ArrayList<String> parseCastResponse(JSONObject response) {

              JSONArray arrayCast = null;

        try {
            //retrieve array containing the results from the API for CAST
            arrayCast = response.getJSONArray("cast");

            for (int i = 0; i < arrayCast.length(); i++) {

                //make variable to hold JSONObject
                JSONObject currentCast = arrayCast.getJSONObject(i);

                if (currentCast.has("name")) {
                    castMovies.add(currentCast.getString("name"));
                }
            }

           // Toast.makeText(MovieDetails.this, castMovies.size() + " items", Toast.LENGTH_SHORT).show();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
        }

        return castMovies;
    }


}
