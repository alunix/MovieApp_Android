package com.mycompany.tuts;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.SearchRecentSuggestions;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.fafaldo.fabtoolbar.widget.FABToolbarLayout;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Processes rss feeds using async task. Allows a search by title. Sends info for displaying results.
 *  Offers search suggestions and a clear search. Uses Shared Preferences and getView() method to
 *  modify styling.
 */
public class RSSFeeds extends AppCompatActivity implements SearchView.OnQueryTextListener, AdapterView.OnItemClickListener, View.OnClickListener   {

    private String NAT_GEO = "http://movieweb.com/rss/movie-news/";
    private String GLOBE_MAIL = "http://www.rollingstone.com/movies.rss";
    private String RSS_FEED = NAT_GEO;

    private ListView lv;
    private ArrayList<String> title;
    private ArrayList<String> description ;
    private ArrayList<String> link;
    private ArrayList<String> pubDate;

    private final int NATGEO = 1;
    private final int MENU_QUIT_ID = 2;
    private final int CHOOSE_CLEAR = 3;
    private final int GLOBE = 4;

    private Boolean majorBoolean = true;
    FABToolbarLayout fabToolbar;

    private ItemAdapter itemAdapter;
    private boolean connected = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rssfeeds);

        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        getSupportActionBar().setTitle("Rss Feeds");
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //get default settings
        PreferenceManager.setDefaultValues(RSSFeeds.this, R.xml.preferences, false);
        lv = (ListView) findViewById(R.id.list_rss);
        lv.setOnItemClickListener(this);

        //check for internet connectivity
        if (!isConnected(RSSFeeds.this)) {

            connected = false;
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builderSingle = new AlertDialog.Builder(RSSFeeds.this);
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

        //create an instance of  async task and execute it
        if(connected) {
            SAXProcessingTask saxTask = new SAXProcessingTask(this);
            saxTask.execute();
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
    public boolean onCreateOptionsMenu(Menu menu)  {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_searching, menu);

        //Add items onto main action bar
        menu.add(0, NATGEO, 0, "MovieWeb");
        menu.add(0, GLOBE, 0, "Rolling Stone");
        menu.add(0, CHOOSE_CLEAR, 0, "Clear Search");
        menu.add(0, MENU_QUIT_ID, 0, "Quit Search");

        ImageView robot_img = (ImageView) findViewById(R.id.fab_img);
        ImageView film_img = (ImageView) findViewById(R.id.fab_film);
        ImageView rss_img = (ImageView) findViewById(R.id.fab_rss);
        robot_img.setOnClickListener(this);
        film_img.setOnClickListener(this);
        rss_img.setOnClickListener(this);

        fabToolbar = (FABToolbarLayout) findViewById(R.id.fabtoolbar);
        // Get the SearchView and set the searchable configuration
        SearchManager searchManager =(SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(this);
        searchView.setSubmitButtonEnabled(false);

        View searchPlate = searchView.findViewById(android.support.v7.appcompat.R.id.search_plate);
        searchPlate.setBackgroundColor(Color.LTGRAY);

        LinearLayout linearLayout1 = (LinearLayout) searchView.getChildAt(0);
        LinearLayout linearLayout2 = (LinearLayout) linearLayout1.getChildAt(2);
        LinearLayout linearLayout3 = (LinearLayout) linearLayout2.getChildAt(1);
        AutoCompleteTextView autoComplete = (AutoCompleteTextView) linearLayout3.getChildAt(0);
        //Set the input text color
        autoComplete.setTextColor(Color.DKGRAY);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
    //not necessary but I like it; the method is required
        for (String v : title) {
            if (v.contains(query)) {
                //Toast.makeText(RSSFeeds.this, "I am in title", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        itemAdapter.getFilter().filter(newText);
        if (TextUtils.isEmpty(newText)) {
            lv.clearTextFilter();
            lv.setVisibility(View.INVISIBLE);
        }
        else {
            lv.setVisibility(View.VISIBLE);
            lv.setFilterText(newText);
        }
        return true;
    }

    @Override
    public boolean onSearchRequested() {
        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent){

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {

            String query = intent.getStringExtra(SearchManager.QUERY);
           // Toast.makeText(RSSFeeds.this, query, Toast.LENGTH_SHORT).show();

            if(!itemAdapter.isEmpty()){
                Typeface empty = Typeface.createFromAsset(getAssets(),
                        "fonts/AbrilFatface-Regular.ttf");
                TextView noResults = (TextView) findViewById(R.id.noResultsFound);
                noResults.setTypeface(empty);
                lv.setEmptyView(findViewById(R.id.noResultsFound));
            }

            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(RSSFeeds.this,
                    SuggestionProvider.AUTHORITY, SuggestionProvider.MODE);
            suggestions.saveRecentQuery(query, null);

            if (!TextUtils.isEmpty(query)) {
                itemAdapter.getFilter().filter(query);
                lv.setVisibility(View.VISIBLE);
                lv.setFilterText(query);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        showPreferences();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        showPreferences();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            //send callers name to receiving activity to identify the return
            Bundle bundle = new Bundle();
            bundle.putString("ClassName", "RSSFeeds");
            Intent newIntent = new Intent(this.getApplicationContext(), SettingsActivity.class);
            newIntent.putExtras(bundle);
            startActivityForResult(newIntent, 0);
        }

        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
        }

        itemAdapter.clear();
        itemAdapter.notifyDataSetChanged();
        lv = (ListView) findViewById(R.id.list_rss);
        final SAXProcessingTask task = new SAXProcessingTask(this);

        switch (item.getItemId()) {
            case NATGEO:
                majorBoolean = false;//determines how to manage rss feeds
                RSS_FEED = NAT_GEO;
                Intent j = new Intent(RSSFeeds.this, RSSFeeds.class);
                startActivity(j);
                getIntent().setData(null);//clear search results
                task.execute();
                break;
            case GLOBE:
                majorBoolean = false;
                RSS_FEED = GLOBE_MAIL;
                Intent k = new Intent(RSSFeeds.this, RSSFeeds.class);
                startActivity(k);
                getIntent().setData(null);
                task.execute();
                break;
            case MENU_QUIT_ID:
                majorBoolean = true;
                getIntent().setData(null);
                Intent x = new Intent(RSSFeeds.this, RSSFeeds.class);
                startActivity(x);
                task.execute();
                break;
            case CHOOSE_CLEAR:
                final SearchRecentSuggestions suggestions = new SearchRecentSuggestions(RSSFeeds.this,
                        SuggestionProvider.AUTHORITY, SuggestionProvider.MODE);
                AlertDialog.Builder builderSingle = new AlertDialog.Builder(RSSFeeds.this);
                builderSingle.setIcon(R.drawable.ic_launcher);
                builderSingle.setTitle("Message");
                builderSingle.setMessage("Clear search suggestions?")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                suggestions.clearHistory();
                                majorBoolean=true;
                                Intent j = new Intent(RSSFeeds.this, RSSFeeds.class);
                                startActivity(j);
                                task.execute();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User cancelled the dialog
                                //start activity again
                                Intent j = new Intent(RSSFeeds.this, RSSFeeds.class);
                                startActivity(j);
                                task.execute();
                            }
                        });

                builderSingle.show();

                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        //pull title out of item in view and search through entire title arrayList
        // until find title to get the position needed to display contents of search results

        TextView tv_title = (TextView) view.findViewById(android.R.id.text1);
        String t= tv_title.getText().toString();
       // Log.d("AMANDA", t);

        position = -1;
        for(int i=0; i<title.size();i++)
        {
            if(t.equals(title.get(i)))
            {
                position = i;
            }
        }

        if(position != -1) {
            Intent intent = new Intent(RSSFeeds.this, DisplayDetailsRss.class);
            //Log.d("AMANDA", position + " " + id);
            //using a bundle for all extras
            Bundle extras = new Bundle();
            extras.putString("title", title.get(position));
            extras.putString("description", description.get(position));
            extras.putString("link", link.get(position));
            extras.putString("pubDate", pubDate.get(position));
            intent.putExtras(extras);

            startActivity(intent);
        }

    }

    public void showPreferences() {
        //Use PreferenceManager to get default shared preferences
        SharedPreferences myPreference = PreferenceManager.getDefaultSharedPreferences(this);
        //Set the background color
        RelativeLayout setting_layout = (RelativeLayout) findViewById(R.id.relative_layout);
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
                Toast.makeText(RSSFeeds.this, "RSS Feeds!", Toast.LENGTH_SHORT).show();
                break;
        }
        fabToolbar.hide();

    }

    //asynchronous task for processing an rss feed with SAX
    class SAXProcessingTask extends AsyncTask<Void, Integer, Void> {

        private FreepSAXHandler handler;
        private Context mCon;

        public SAXProcessingTask(Context con) {
            mCon = con;
        }

        @Override
        protected Void doInBackground(Void... params) {
            //create a SAXParser
            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXParser saxParser = null;

            try {
                saxParser = spf.newSAXParser();
            } catch (ParserConfigurationException | SAXException e) {
                e.printStackTrace();
            }

            //create instance of our custom handler
            handler = new FreepSAXHandler();

            if(majorBoolean) {
                try {
                    RSS_FEED = NAT_GEO;
                    saxParser.parse(RSS_FEED, handler);//do not change
                    RSS_FEED = GLOBE_MAIL;
                    saxParser.parse(RSS_FEED, handler);
                } catch (SAXException | IOException e) {
                    e.printStackTrace();
                }

            }

            if(!majorBoolean) {
                if (RSS_FEED.equals(NAT_GEO)) {
                    try {
                        saxParser.parse(NAT_GEO, handler);
                    } catch (SAXException | IOException e) {
                        e.printStackTrace();
                    }
                } else if(Objects.equals(RSS_FEED, GLOBE_MAIL)){
                    try {
                        saxParser.parse(GLOBE_MAIL, handler);
                    } catch (SAXException | IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            lv = (ListView) findViewById(R.id.list_rss);

            //get contents of each array
            title = handler.getTitleArrayList();
            link = handler.getLinkArrayList();
            description = handler.getDescriptionArrayList();
            pubDate = handler.getPubDateArrayList();

            itemAdapter = new ItemAdapter(RSSFeeds.this, android.R.layout.simple_list_item_1, title);
            lv.setAdapter(itemAdapter);

        }
    }

    //This is use to parse the xml. The handler
    private class FreepSAXHandler extends DefaultHandler {

        //could use ArrayLists or other collection object(s) to collect data as parsed
        private ArrayList<String> getTitle, getLink, getDescription, getPubDate, getFeedName;
        private boolean inItem, inTitle, inDescription, inLink, inPubDate;

        //String builder use to gather the content inside elements
        private StringBuilder sb;

        public ArrayList getTitleArrayList() {
            return getTitle;
        }

        public ArrayList getDescriptionArrayList() {
            return getDescription;
        }

        public ArrayList getLinkArrayList() {
            return getLink;
        }

        public ArrayList getPubDateArrayList() {
            return getPubDate;
        }

        //constructor
        public FreepSAXHandler() {
            getTitle = new ArrayList<>();
            getDescription = new ArrayList<>();
            getLink = new ArrayList<>();
            getPubDate = new ArrayList<>();
        }

        @Override
        public void startDocument() throws SAXException {
            super.startDocument();
        }

        @Override
        public void endDocument() throws SAXException {
            super.endDocument();
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);

            sb = new StringBuilder();

            if (qName.equals("item")) {
                inItem = true;

            } else if (inItem && qName.equals("title")) {
                inTitle = true;

            } else if (inItem && qName.equals("link")) {
                inLink = true;

            } else if (inItem && qName.equals("description")) {
                inDescription = true;

            } else if (inItem && qName.equals("pubDate")) {
                inPubDate = true;
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);

            if (qName.equals("item")) {
                inItem = false;
            }
            else if (inItem && qName.equals("title")) {

                if (!sb.toString().equals("")){
                    getTitle.add(sb.toString());
                }
                else {
                    getTitle.add("N/A");
                }
                inTitle = false;
            }
            else if (inItem && qName.equals("link")) {

                if(! sb.toString().equals("")) {
                    getLink.add(sb.toString());
                }
                else {
                    getLink.add("N/A");
                }
                inLink = false;
            }
            else if (inItem && qName.equals("description")) {
                if(! sb.toString().equals(""))
                    getDescription.add(sb.toString());
                else{
                    getDescription.add("N/A");
                }
                inDescription = false;

            }
            else if (inItem &&  qName.equals("pubDate")) {
                if(! sb.toString().equals("")){
                    getPubDate.add(sb.toString());
                }
                else{
                    getPubDate.add("N/A");
                }
                inPubDate = false;
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            super.characters(ch, start, length);
            String s = new String(ch, start, length);
            // add the characters to collection

                if (inTitle) {
                    sb.append(s);

                } else if (inLink) {
                    sb.append(s);

                } else if (inDescription) {
                    sb.append(s);

                } else if (inPubDate) {
                    sb.append(s);
                }

        }
    }

    //class for adapter
  class ItemAdapter extends ArrayAdapter<String> {

        private ArrayList<String> items;

        public ItemAdapter(Context context, int resource, ArrayList<String> items) {
            super(context, resource, items);
            this.items = items;


        }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
      View row = super.getView(position, convertView, parent);

      //change styling by user
       //Here we get the TextView and set the color
      TextView tv = (TextView) row.findViewById(android.R.id.text1);

      SharedPreferences myPreference = PreferenceManager.getDefaultSharedPreferences(RSSFeeds.this);

      //get font type dynamically from settings menu
      String myFont = myPreference.getString("fontType", "");
      switch (myFont) {
          case "Sans-serif-thin":
              tv.setTypeface(Typeface.SANS_SERIF, Typeface.ITALIC);
              break;
          case "Sans-serif":
              tv.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC);
              break;
          case "Monospace":
              tv.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
              break;
      }

      String textColorPreference = myPreference.getString("textColorType", "");
      switch (textColorPreference) {
          case "White":
              textColorPreference = "#FFFFFF";
              break;
          case "Blue":
              textColorPreference = "#1a8cff";
              break;
          case "Orange":
              textColorPreference = "#ff9933";
              break;
          case "Black":
              textColorPreference = "#000000";
              break;
      }
                tv.setTextColor(Color.parseColor(textColorPreference));

            String backColorPreference = myPreference.getString("backColorType", "");

      switch (backColorPreference) {
          case "Blue":
              backColorPreference = "#90CAF9";
              break;
          case "Orange":
              backColorPreference = "#FFE0B2";
              break;
          case "Gray":
              backColorPreference = "#B0BEC5";
              break;
          case "Green":
              backColorPreference = "#00796B";
              break;
          case "Black":
              backColorPreference = "#000000";
              break;
          case "White":
              backColorPreference = "#FFFFFF";
              break;
      }

            lv.setBackgroundColor(Color.parseColor(backColorPreference));

            //Text size preference dynamically
      String myTextSize = myPreference.getString("textSizeType", "");
      switch (myTextSize) {
          case "Large":
              tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18.f);
              break;
          case "Larger":
              tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20.f);
              break;
          case "Jumbo":
              tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 24.f);
              break;
      }

      return row;
    }

  }

}

