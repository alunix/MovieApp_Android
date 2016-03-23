package com.mycompany.tuts;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
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
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.github.fafaldo.fabtoolbar.widget.FABToolbarLayout;

import java.util.ArrayList;

/**
 * Allows the user to save movies to the database, implement a search  by genre,
 * or a search by title, data, or rating. Uses a navigation drawer. Makes a custom toolbar.
 * Uses Shared Preferences to style the background.
 */
public class MainActivity extends AppCompatActivity  implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    private static Context context;
    private static final String SELECTED_ITEM_ID = "selected_item_id";
    private static final String FIRST_TIME = "first_time";

    public DrawerLayout drawer_layout;
    private ActionBarDrawerToggle drawerToggle;//needed to show drawer icon

    private int selected_id;
    private boolean userSawDrawer = false;
    private ArrayList<Movie> listMovies = new ArrayList<>();
    private MovieAdapter movieAdapter;
    private ImageLoader imageLoader;//needs this variable but is using one in VolleySingleton

    private DBMovies mDatabase;
    private final int SORT = 1;
    private final int RETRIEVE = 2;
    FABToolbarLayout fabToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("My Movies");

        MainActivity.context = getApplicationContext();

        fabToolbar = (FABToolbarLayout) findViewById(R.id.fabtoolbar);

        ImageView robot_img = (ImageView) findViewById(R.id.fab_img);
        ImageView film_img = (ImageView) findViewById(R.id.fab_film);
        ImageView rss_img = (ImageView) findViewById(R.id.fab_rss);

        robot_img.setOnClickListener(this);
        film_img.setOnClickListener(this);
        rss_img.setOnClickListener(this);

        //set default settings
        PreferenceManager.setDefaultValues(MainActivity.this, R.xml.preferences, false);
        //load listMovies from db
        mDatabase = new DBMovies(this);
        listMovies = mDatabase.readMovies();

        movieAdapter = new MovieAdapter(MainActivity.this, listMovies, MovieAdapter.READING);

        RecyclerView listMovieHits = (RecyclerView) findViewById(R.id.list_my_movies);
        listMovieHits.setLayoutManager(new LinearLayoutManager(this));
        listMovieHits.setAdapter(movieAdapter);
        listMovieHits.setHasFixedSize(true);
        listMovieHits.setNestedScrollingEnabled(true);

        NavigationView drawer = (NavigationView) findViewById(R.id.main_drawer);
        drawer.setNavigationItemSelectedListener(this);

        drawer_layout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer_layout.setScrimColor(Color.BLACK);//shade the gap area

        //create the drawer toggle
        drawerToggle = new ActionBarDrawerToggle(this,
                drawer_layout,
                toolbar,
                R.string.drawer_open,
                R.string.drawer_close);

        drawer_layout.setDrawerListener(drawerToggle);
        drawerToggle.syncState();// Lets app know whether in Activity or drawer

        //determine which menu id - might be returning from rotation
        selected_id = savedInstanceState == null ? R.id.navigation_item_1 : savedInstanceState.getInt(SELECTED_ITEM_ID);

        navigate(selected_id);

        if (!userSawDrawer()) {
            markDrawerSeen();
            showDrawer();
            userSawDrawer=true;
        } else {
            userSawDrawer=true;
            hideDrawer();
        }

        if(userSawDrawer){
            hideDrawer();
        }
    }

    public static Context getContext()
    {
        return MainActivity.context;
    }

    private boolean userSawDrawer() {
        //check for boolean stored to see if the user has ever seen drawer
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.getBoolean(FIRST_TIME, false);

        return userSawDrawer;
    }

    private void showDrawer() {
        drawer_layout.openDrawer(GravityCompat.START);
        markDrawerSeen();
    }

    public void hideDrawer() {

        drawer_layout.closeDrawer(GravityCompat.START);;
    }

    private void markDrawerSeen() {
        //update shared preferences
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        userSawDrawer = true;
        //save boolean
        sharedPreferences.edit().putBoolean(FIRST_TIME, userSawDrawer).apply();
    }

    private void navigate(int selected_id) {
        //start activity based on selection
      if (selected_id == R.id.navigation_item_2) {
            drawer_layout.closeDrawer(GravityCompat.START);
       }
      else if (selected_id == R.id.navigation_item_5) {
            startActivity(new Intent(this, MovieAPI.class));
        }
        else if (selected_id == R.id.navigation_item_6) {
          Intent i = new Intent(MainActivity.this, RSSFeeds.class);
          startActivity(i);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        //Called by the system when the device configuration
        //changes while your activity is running
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        //Add items onto main action bar
        menu.add(0, SORT, 0, "Choose Genre");
        menu.add(0, RETRIEVE, 0, "Order By");

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle tool bar item clicks here.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            //let new class know who is calling
            Bundle bundle = new Bundle();
            bundle.putString("ClassName", "MainActivity");

            Intent newIntent = new Intent(this.getApplicationContext(), SettingsActivity.class);
            newIntent.putExtras(bundle);
            startActivityForResult(newIntent, 0);
        }
        //start this activity if click on arrow
        if (id == R.id.navigate) {
            startActivity(new Intent(this, MovieAPI.class));
        }
        switch (item.getItemId()) {
            case SORT:
                getGenreMovie();
            break;
            case RETRIEVE:
                getSortOptions();
            break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        //close the drawer or use default behavior

        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();

        }
        showPreferences();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
      //set the menu item
        menuItem.setChecked(true);
        selected_id = menuItem.getItemId();
        navigate(selected_id);

        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //save id of menu_item, put in key
        outState.putInt(SELECTED_ITEM_ID, selected_id);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_img:
                Toast.makeText(MainActivity.this, "Your Movie Page!", Toast.LENGTH_SHORT).show();
                break;
            case R.id.fab_film:
                startActivity(new Intent(this, MovieAPI.class));
                break;
            case R.id.fab_rss:
                startActivity(new Intent(this, RSSFeeds.class));
                break;
        }
        fabToolbar.hide();
    }

    public void getGenreMovie() {
        //make a dialog for genres
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(MainActivity.this);
        builderSingle.setIcon(R.drawable.ic_movie_black_24dp);
        builderSingle.setTitle(R.string.pick_type);

        //set the adapter for genres
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                MainActivity.this,
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
                                MainActivity.this);
                        // builderInner.setMessage(strName);
                        //send new API query and call request method based on selection
                        long genreId = -1;
                        String genreName = "";
                        if (which == 0) {
                            genreId = mDatabase.getGenreId("Action");
                        } else if (which == 1) {
                            genreId = mDatabase.getGenreId("Crime");

                        } else if (which == 2) {
                            genreId = mDatabase.getGenreId("Drama");

                        } else if (which == 3) {
                            genreId = mDatabase.getGenreId("Thriller");

                        }
                        //Toast.makeText(MainActivity.this, "Genre: " + genreId, Toast.LENGTH_SHORT).show();
                        // Toast.makeText(MainActivity.this, "Genre: " + genreName, Toast.LENGTH_SHORT).show();
                        //now you have the genreId, to use in a query.

                        listMovies = mDatabase.readMoviesByGenre(genreId);
                        //put into movie adaptor
                        movieAdapter.setMovieList(listMovies);

                        //Toast.makeText(MainActivity.this, mDatabase.queryTest, Toast.LENGTH_LONG).show();
                        //Toast.makeText(MainActivity.this, "First Movie: " + listMovies.get(0).getTitle(), Toast.LENGTH_SHORT).show();
                        movieAdapter.notifyDataSetChanged();
                    }
                });
        builderSingle.show();
    }

    public void getSortOptions() {
        //make a dialog for genres
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(MainActivity.this);
        builderSingle.setIcon(R.drawable.ic_movie_black_24dp);
        builderSingle.setTitle(R.string.pick_type);

        //set the adapter for genres
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                MainActivity.this,
                android.R.layout.select_dialog_singlechoice);
        arrayAdapter.add("By Name");
        arrayAdapter.add("By Date");
        arrayAdapter.add("By Rating");

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
                                MainActivity.this);
                        // builderInner.setMessage(strName);
                        //method based on selection
                        if (which == 0) {
                            MovieSorter.sortByName(listMovies);
                        } else if (which == 1) {
                            MovieSorter.sortByDate(listMovies);
                        } else if (which == 2) {
                            MovieSorter.sortByRating(listMovies);
                        }
                        movieAdapter.notifyDataSetChanged();
                    }
                });
        builderSingle.show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(userSawDrawer){
            hideDrawer();
        }
        showPreferences();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(userSawDrawer){
            hideDrawer();
        }
        showPreferences();
    }
    public void showPreferences() {
        //Use PreferenceManager to get default shared preferences
        SharedPreferences myPreference = PreferenceManager.getDefaultSharedPreferences(this);
        //Set the background color
        RelativeLayout setting_layout = (RelativeLayout) findViewById(R.id.main_root);
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
}

