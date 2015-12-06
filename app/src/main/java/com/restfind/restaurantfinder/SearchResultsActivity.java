package com.restfind.restaurantfinder;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.List;

public class SearchResultsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        Intent intent = getIntent();
        SearchOptions searchOptions = intent.getParcelableExtra(getResources().getString(R.string.search_options));

        //start AsyncTask
        GetSearchResultsTask task = new GetSearchResultsTask();
        task.execute(searchOptions);
    }

    private class SearchResult {

    }

    //<Input for doInBackground, (Progress), Input for onPostExecute>
    private class GetSearchResultsTask extends AsyncTask<SearchOptions, Integer, List<SearchResult>>{

        @Override
        protected List<SearchResult> doInBackground(SearchOptions... params) {
            SearchOptions options = params[0];

            if(options != null){
                Log.v("GetSearchResultsTask", "searchText: " + options.getSearchText());
                Log.v("GetSearchResultsTask", "longitude: " + options.getLongitude());
                Log.v("GetSearchResultsTask", "latitude: " + options.getLatitude());
                Log.v("GetSearchResultsTask", "radius: " + options.getRadius());

                if(options.getTypes() != null) {
                    Log.v("GetSearchResultsTask", "types!");
                    for (String s : options.getTypes()) {
                        Log.v("GetSearchResultsTask", "types: " + s);
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(List<SearchResult> result) {

        }
    }
}
