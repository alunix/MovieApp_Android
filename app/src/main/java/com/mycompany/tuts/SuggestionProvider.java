package com.mycompany.tuts;

import android.content.SearchRecentSuggestionsProvider;

//Sets up a Content Provider for recent query suggestions
public class SuggestionProvider extends SearchRecentSuggestionsProvider {
    public final static String AUTHORITY = "com.mycompany.anything";
    public final static int MODE = DATABASE_MODE_QUERIES;

    public SuggestionProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }
}
