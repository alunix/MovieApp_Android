package com.mycompany.tuts;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Allows fragment to be displayed which is the Preferences.xml.
 */
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setTitle("Settings");

    /**Discovered that at this time there is not an effective solution
     to style a preference.xml file to be compatible with a custom made toolbar
     default toolbar is used for this Activity**/

    // begin a transaction to display the fragment
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {

        //find out who called so can go back to them
        Intent i;
        getIntent();

        Bundle bundle = this.getIntent().getExtras();
        String className = bundle.getString("ClassName");

        assert className != null;
        switch (className) {
            case "RSSFeeds":
                i = new Intent(SettingsActivity.this, RSSFeeds.class);
                startActivity(i);
                break;
            case "MovieAPI":
                i = new Intent(SettingsActivity.this, MovieAPI.class);
                startActivity(i);
                break;
            case "MainActivity":
                i = new Intent(SettingsActivity.this, MainActivity.class);
                startActivity(i);
                break;
        }

        this.finish();
  }
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }
    //SettingsActivity hosts the MyPreferenceFragment that displays our preference settings
    public static class MyPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
        }

    }

}





