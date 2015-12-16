package com.restfind.restaurantfinder;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ListView;

import java.util.List;

public class SearchResultsActivity extends AppBarActivity {

    SearchOptions searchOptions;
    ListView lvSearchResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Results");
        setSupportActionBar(toolbar);

        //Get Search Options from previous Activity
        Intent intent = getIntent();
        searchOptions = intent.getParcelableExtra(getResources().getString(R.string.search_options));

        //Set up UI-Elements
        lvSearchResults = (ListView) findViewById(R.id.lvSearchResults);

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
                Log.v("GetSearchResultsTask", "name: " + options.getName());
                Log.v("GetSearchResultsTask", "radius: " + options.getRadius());
                Log.v("GetSearchResultsTask", "longitude: " + options.getLongitude());
                Log.v("GetSearchResultsTask", "latitude: " + options.getLatitude());
                Log.v("GetSearchResultsTask", "timeIsNow: " + options.isTimeNow());
                Log.v("GetSearchResultsTask", "time: " + options.getTime());
                Log.v("GetSearchResultsTask", "dayOfWeek: " + options.getDayOfWeek());

                if(options.getTypesRestaurant() != null) {
                    for (String s : options.getTypesRestaurant()) {
                        Log.v("GetSearchResultsTask", "Restaurant-types: " + s);
                    }
                }
                if(options.getTypesBar() != null) {
                    for (String s : options.getTypesBar()) {
                        Log.v("GetSearchResultsTask", "Bar-types: " + s);
                    }
                }
                if(options.getTypesCafe() != null) {
                    for (String s : options.getTypesCafe()) {
                        Log.v("GetSearchResultsTask", "Cafe-types: " + s);
                    }
                }
                if(options.getTypesTakeaway() != null) {
                    for (String s : options.getTypesTakeaway()) {
                        Log.v("GetSearchResultsTask", "Takeaway-types: " + s);
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
