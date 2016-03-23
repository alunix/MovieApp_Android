package com.mycompany.tuts;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class DisplayDetailsRss extends AppCompatActivity implements View.OnClickListener {

    String passedLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_display_details_rss);

        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        getSupportActionBar().setTitle("Rss Details");
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

       // get info from from the intent of Main Activity using intent.
        Intent intent = getIntent();
        String passedTitle = intent.getStringExtra("title");
        String passedDesc = intent.getStringExtra("description");
        passedLink = intent.getStringExtra("link");
        String passedPubDate = intent.getStringExtra("pubDate");

        TextView text_titles = (TextView) findViewById(R.id.text_titles);
        TextView text_details = (TextView) findViewById(R.id.text_details);
        TextView text_link = (TextView) findViewById(R.id.text_link);
        TextView text_pub_date = (TextView) findViewById(R.id.text_pub_date);

        text_titles.setText(passedTitle);
        text_details.setText(passedDesc);
        text_link.setText(passedLink);
        text_pub_date.setText(passedPubDate);

        Button button_link = (Button)findViewById(R.id.button_link);
        button_link.setOnClickListener(this);
        Typeface tf = Typeface.createFromAsset(getAssets(),
                "fonts/Amaranth-Bold.ttf");

        text_titles.setTypeface(tf);
        text_details.setTypeface(tf);
        text_link.setTypeface(tf);
        text_pub_date.setTypeface(tf);

        PreferenceManager.setDefaultValues(DisplayDetailsRss.this, R.xml.preferences, false);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_display_details_rss, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
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
    public void onClick(View v) {
        Intent intent = new Intent(DisplayDetailsRss.this, MovieWeb.class);
        Bundle extras = new Bundle();
        extras.putString("link", passedLink );
        intent.putExtras(extras);
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        showPreferences();
    }
//    @Override
//    public void onBackPressed() {
//       finish();
//    }
    @Override
    protected void onResume() {
        super.onResume();
        showPreferences();
    }

    public void showPreferences() {
        //Use PreferenceManager to get default shared preferences
        SharedPreferences myPreference = PreferenceManager.getDefaultSharedPreferences(this);
        //Set the background color
        RelativeLayout setting_layout = ( RelativeLayout) findViewById(R.id.rss_details);
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
