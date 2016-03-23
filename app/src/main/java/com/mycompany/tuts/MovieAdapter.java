package com.mycompany.tuts;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;

import java.util.ArrayList;

/**
 * Uses Shared Preferences to style the custom row of RecyclerView. Determines state
 * of TextView for saveMovie/Remove. Allows animation of custom row, handles checking for movie duplicates, saving and deleting.
 * Places default values and default image if nothing present from the movie API.
 */
public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.ViewHolderBoxOffice>{

    private ArrayList<Movie> listMovies = new ArrayList<>();
    private Activity movieAPI;
    private ImageLoader imageLoader;
    private int previousPosition = 0;//used for animation
    private static DBMovies mDatabase;
    private boolean reading;
    public static boolean READING = true;
    public static boolean WRITING = false;

    public MovieAdapter(Context context, ArrayList<Movie> listMovies, boolean reading){

        this.reading = reading;
        //Make a reference to Activity and get that layout
        //Put this data on that layout
        movieAPI = (Activity)context;
        mDatabase = new DBMovies(movieAPI);

        this.listMovies = listMovies;
        VolleySingleton volleySingleton = VolleySingleton.getInstance(context);
        imageLoader = volleySingleton.getImageLoader();

        this.listMovies = listMovies;
    }

    @Override
    public ViewHolderBoxOffice onCreateViewHolder(ViewGroup parent, int viewType) {
        //get the view to inflate
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_movie, parent, false);
            ViewHolderBoxOffice viewHolder = new ViewHolderBoxOffice(v);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolderBoxOffice holder, int position) {

        if( listMovies.size() == 0){
            return;
        }

        Movie currentMovie = listMovies.get(position);

        //get position for animation - decides if animate from top or bottom
        if(position>previousPosition){
            com.mycompany.tuts.AnimationUtils.animate(holder, true);
        }
        else{
            com.mycompany.tuts.AnimationUtils.animate(holder, false);
        }

        previousPosition = position;

        if(mDatabase.isDuplicate(currentMovie.getId())) {
            holder.saveMovie.setText("Remove");
        }
        //get the title
        holder.titleText.setText(currentMovie.getTitle());

        //get the release date
        if(currentMovie.getDate() != null){
            holder.releaseDateText.setText(currentMovie.getDate().toString());
        }
        else{
            holder.releaseDateText.setText("No Date");
        }
        //get rating
        double rateScore = currentMovie.getVoteAverage();

        if(rateScore<1){
            //if no rating display 0 rating and slide_fade out rating bar
            //issues with ratings so currently using <1
            holder.ratings.setRating(0.0f);
            holder.ratings.setAlpha(0.5f);
        }
        else{
            holder.ratings.setRating((float) (currentMovie.getVoteAverage()/2.0f));
            holder.ratings.setAlpha((float) (currentMovie.getVoteAverage()/1.0f));
        }
        // get image
         String urlThumbnail = currentMovie.getPosterPath();
        if(urlThumbnail!= null && !urlThumbnail.equals("null")){
            imageLoader.get("https://image.tmdb.org/t/p/w185"+ urlThumbnail, new ImageLoader.ImageListener() {

                @Override
                public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                    holder.imageView.setImageBitmap(response.getBitmap());
                }

                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });

        } else {
            //checking for null
            //Toast.makeText(movieAPI, "Thumb is: "+ urlThumbnail, Toast.LENGTH_SHORT).show();
            //set default image
            holder.imageView.setImageResource(R.drawable.ic_robot); //backup
            String pathName = "http://i.imgur.com/7spzG.png";
            imageLoader.get(pathName, new ImageLoader.ImageListener() {

                @Override
                public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                    holder.imageView.setImageBitmap(response.getBitmap());
                }

                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });
        }

        //All styling chosen by user happens here for RecyclerView
        SharedPreferences myPreference = PreferenceManager.getDefaultSharedPreferences(movieAPI);
        Movie current = listMovies.get(position);
        holder.titleText.setText(current.getTitle());

        //get font type dynamically from settings menu
        String myFont = myPreference.getString("fontType", "");
        if (myFont.equals("sans-serif-thin")) {
            holder.titleText.setTypeface(Typeface.SANS_SERIF, Typeface.ITALIC);
            holder.saveMovie.setTypeface(Typeface.SANS_SERIF, Typeface.ITALIC);
            holder.releaseDateText.setTypeface(Typeface.SANS_SERIF, Typeface.ITALIC);
        } else if (myFont.equals("Sans-serif")) {
            holder.titleText.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC);
            holder.saveMovie.setTypeface(Typeface.SANS_SERIF, Typeface.ITALIC);
            holder.releaseDateText.setTypeface(Typeface.SANS_SERIF, Typeface.ITALIC);
        } else if (myFont.equals("Monospace")) {
            holder.titleText.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
            holder.saveMovie.setTypeface(Typeface.SANS_SERIF, Typeface.ITALIC);
            holder.releaseDateText.setTypeface(Typeface.SANS_SERIF, Typeface.ITALIC);
        }

        //Text size preference dynamically
        String myTextSize = myPreference.getString("textSizeType", "");
        if(myTextSize.equals("Large")){
            holder.titleText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18.f);}
        else if(myTextSize.equals("Larger")) {
            holder.titleText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20.f);}
        else if(myTextSize.equals("Jumbo")) {
            holder.titleText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 24.f);}

        //Text color preference dynamically
        String textColorPreference = myPreference.getString("textColorType", "");
                if(textColorPreference.equals("White")){
                    textColorPreference =  "#FFFFFF";
                }
                else if(textColorPreference.equals("Blue")) {
                    textColorPreference =  "#1a8cff";
                }
                else if(textColorPreference.equals("Orange")) {
                    textColorPreference =  "#ff9933";
                }
                else if(textColorPreference.equals("Black")) {
                    textColorPreference =  "#000000";
                }

        holder.titleText.setTextColor(Color.parseColor(textColorPreference));
        holder.saveMovie.setTextColor(Color.parseColor(textColorPreference));
        holder.releaseDateText.setTextColor(Color.parseColor(textColorPreference));

        //set background preference dynamically
        String backColorPreference = myPreference.getString("backColorType", "");
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

        holder.itemView.setBackgroundColor(Color.parseColor(backColorPreference));
    }

    @Override
    public int getItemCount() {
        return listMovies.size();
    }

    public void  setMovieList(ArrayList<Movie> listMovies) {
        this.listMovies = listMovies;
        notifyDataSetChanged();
    }

    class ViewHolderBoxOffice extends RecyclerView.ViewHolder implements View.OnClickListener{
        //ViewHolder is a way to avoid creating objects every time. Does it once.
        ImageView imageView;
        TextView titleText, saveMovie;
        TextView releaseDateText;
        RatingBar ratings;

        public ViewHolderBoxOffice(View itemView) {
            super(itemView);

            //all view bindings happen at the same time
             imageView= (ImageView) itemView.findViewById(R.id.movieThumbnail);
             titleText = (TextView) itemView.findViewById(R.id.movieTitle);
             saveMovie = (TextView) itemView.findViewById(R.id.saveMovie);

            //use boolean to switch word of TextView 'save movie' to 'remove'

            if(reading){saveMovie.setText("Remove");}

             releaseDateText = (TextView) itemView.findViewById(R.id.movieReleaseDate);
             ratings = (RatingBar) itemView.findViewById(R.id.movieVote);

             titleText.setOnClickListener(this);
             imageView.setOnClickListener(this);
             releaseDateText.setOnClickListener(this);
             saveMovie.setOnClickListener(this);

            //RatingBar overrides onTouchEvent()
            ratings.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        passIntent();
                    }
                    return true;
                }
             });

        }

    public void passIntent(){

        Intent intent;
        intent = new Intent(movieAPI, MovieDetails.class);
        //parse the id with ""
        intent.putExtra("movie_id", "" + listMovies.get(getAdapterPosition()).getId());
        intent.putExtra("movie_title", "" + listMovies.get(getAdapterPosition()).getTitle());

        intent.putExtra("poster_path", "" + listMovies.get(getAdapterPosition()).getPosterPath());
        intent.putExtra("overview", "" + listMovies.get(getAdapterPosition()).getOverview());

        ActivityOptionsCompat compat = ActivityOptionsCompat.makeSceneTransitionAnimation(movieAPI,null );
        movieAPI.startActivity(intent, compat.toBundle());
    }

        @SuppressLint("NewApi")//needed for transition
        @Override
        public void onClick(View v){

            switch (v.getId()) {
                case R.id.movieTitle:
                   // Toast.makeText(v.getContext(), titleText.getText() + " position = " + getAdapterPosition(), Toast.LENGTH_SHORT).show();
                     passIntent();
                    break;
                case R.id.movieThumbnail:
                    passIntent();
                    break;
            case R.id.movieReleaseDate:
                    String genrestr ="";//for testing
                    int[] genres = listMovies.get(getAdapterPosition()).getGenres();
                    for(int i=0;i<genres.length;i++){
                        genrestr+= genres[i]+" ";
                    }
                   // Toast.makeText(v.getContext(), "Genres: " + genrestr, Toast.LENGTH_SHORT).show();
                    passIntent();
                    break;
                case R.id.saveMovie:
                    Movie clickedMovie = listMovies.get(getAdapterPosition());

                    if(mDatabase.isDuplicate(clickedMovie.getId())){

                        try {
                            mDatabase.delete(clickedMovie.getId());
                            if(reading) {
                                listMovies.remove(clickedMovie);
                            }

                            notifyItemChanged(getAdapterPosition());
                        }
                        catch(Exception e){
                           // Toast.makeText(movieAPI, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                    else {
                            mDatabase.insertMovie(clickedMovie, false);
                           // Toast.makeText(movieAPI, "Movie Saved", Toast.LENGTH_SHORT).show();
                            notifyItemChanged(getAdapterPosition());
                            return;
                     }
                    break;
                }
                    //Toast.makeText( v.getContext(), "query: " + mDatabase.queryTest, Toast.LENGTH_SHORT).show();
            }
            //Toast.makeText( v.getContext(), "", Toast.LENGTH_SHORT).show();
        }
    }


