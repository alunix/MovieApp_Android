package com.mycompany.tuts;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

import com.github.fafaldo.fabtoolbar.widget.FABToolbarLayout;
public class MovieWeb extends AppCompatActivity implements View.OnClickListener{

    private FABToolbarLayout fabToolbar;
    private boolean connected = true;
    private String passedLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_web);

        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("Movie Web Sites");
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        passedLink = intent.getStringExtra("link");

            fabToolbar = (FABToolbarLayout) findViewById(R.id.fabtoolbar);

            ImageView robot_img = (ImageView) findViewById(R.id.fab_img);
            ImageView film_img = (ImageView) findViewById(R.id.fab_film);
            ImageView rss_img = (ImageView) findViewById(R.id.fab_rss);

            robot_img.setOnClickListener(this);
            film_img.setOnClickListener(this);
            rss_img.setOnClickListener(this);
            fabToolbar = (FABToolbarLayout) findViewById(R.id.fabtoolbar);

        //check for internet connectivity
        if (!isConnected(MovieWeb.this)) {

            connected=false;
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builderSingle = new AlertDialog.Builder(MovieWeb.this);
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
           WebView webview = (WebView)findViewById(R.id.webview);

            //need to pass value of web site link
            WebSettings webSettings = webview.getSettings();
            webSettings.setJavaScriptEnabled(true);

            webview.setVerticalScrollBarEnabled(true);
            webview.setHorizontalScrollBarEnabled(true);

            if(passedLink.startsWith("http://movieweb.com/")){
                webview.loadUrl("http://movieweb.com/");
            }
            else if(passedLink.startsWith("http://www.rollingstone.com/")) {
                webview.loadUrl("http://www.rollingstone.com/");
            }

            webview.setWebViewClient(new WebViewClient() {
                public boolean shouldOverrideUrlLoading(WebView viewx, String urlx) {
                    viewx.loadUrl(urlx);
                    return false;
                }
            });

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_movie_web, menu);
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
                startActivity(new Intent(this, MovieAPI.class));
                break;
            case R.id.fab_rss:
                startActivity(new Intent(this, RSSFeeds.class));
                break;
        }
        fabToolbar.hide();

    }
}
