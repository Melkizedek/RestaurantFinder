package com.restfind.restaurantfinder;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ListView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Places;

import java.util.List;

public class SearchResultsActivity extends AppBarActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;

    private SearchOptions searchOptions;
    private ListView lvSearchResults;

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

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();


    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(Bundle bundle) {
        //start AsyncTask
        GetSearchResultsTask task = new GetSearchResultsTask();
        task.execute(searchOptions);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        showAlertDialog("Could not connect to the Google Places API!");
    }

    private class SearchResult {

    }

    //<Input for doInBackground, (Progress), Input for onPostExecute>
    private class GetSearchResultsTask extends AsyncTask<SearchOptions, Integer, List<SearchResult>>{

        @Override
        protected List<SearchResult> doInBackground(SearchOptions... params) {
            SearchOptions options = params[0];

            if(options != null){
                Log.v(LOG_TAG, "name: " + options.getName());
                Log.v(LOG_TAG, "radius: " + options.getRadius());
                Log.v(LOG_TAG, "longitude: " + options.getLongitude());
                Log.v(LOG_TAG, "latitude: " + options.getLatitude());
                Log.v(LOG_TAG, "timeIsNow: " + options.isTimeNow());
                Log.v(LOG_TAG, "time: " + options.getTime());
                Log.v(LOG_TAG, "dayOfWeek: " + options.getDayOfWeek());

                if(options.getTypesRestaurant() != null) {
                    for (String s : options.getTypesRestaurant()) {
                        Log.v(LOG_TAG, "Restaurant-types: " + s);
                    }
                }
                if(options.getTypesBar() != null) {
                    for (String s : options.getTypesBar()) {
                        Log.v(LOG_TAG, "Bar-types: " + s);
                    }
                }
                if(options.getTypesCafe() != null) {
                    for (String s : options.getTypesCafe()) {
                        Log.v(LOG_TAG, "Cafe-types: " + s);
                    }
                }
                if(options.getTypesTakeaway() != null) {
                    for (String s : options.getTypesTakeaway()) {
                        Log.v(LOG_TAG, "Takeaway-types: " + s);
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
