package com.restfind.restaurantfinder;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class SearchResultsActivity extends AppBarActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnInfoWindowClickListener {

    private SearchOptions searchOptions;
    private ListView lvSearchResults;
    private GoogleMap mMap;
    private View infoWindow;
    private Marker curPos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        //Get Search Options from previous Activity
        Intent intent = getIntent();
        searchOptions = intent.getParcelableExtra(getResources().getString(R.string.search_options));

        //Set up UI-Elements
//        lvSearchResults = (ListView) findViewById(R.id.lvSearchResults);

        infoWindow = getLayoutInflater().inflate(R.layout.map_info_window, null);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in current Position and move the camera
        LatLng currentPos = new LatLng(searchOptions.getLatitude(), searchOptions.getLongitude());
        curPos = mMap.addMarker(new MarkerOptions().position(currentPos).title("Current Position"));
//        .icon(BitmapDescriptorFactory.fromResource(R.drawable.)
        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentPos));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15.0f));

        mMap.setOnInfoWindowClickListener(null);
        mMap.setOnMarkerClickListener(this);

//        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
//            @Override
//            public void onMapClick(LatLng point) {
//                if(lastPos != null){
//                    lastPos.remove();
//                }
//                lastPos = mMap.addMarker(new MarkerOptions().position(point));
//
//                if(circle != null){
//                    circle.setCenter(lastPos.getPosition());
//                }
//            }
//        });

        GetSearchResultsTask task = new GetSearchResultsTask();
        task.execute();
    }

    @Override
    public void onInfoWindowClick(Marker marker) {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if(marker.equals(curPos)){

            infoWindow.setClickable(false);

        }

        return false;
    }

    //<Input for doInBackground, (Progress), Input for onPostExecute>
    private class GetSearchResultsTask extends AsyncTask<SearchOptions, Integer, List<Place>>{

        @Override
        protected List<Place> doInBackground(SearchOptions... params) {
            if(searchOptions != null){
                Log.v(LOG_TAG, "name: " + searchOptions.getName());
                Log.v(LOG_TAG, "radius: " + searchOptions.getRadius());
                Log.v(LOG_TAG, "longitude: " + searchOptions.getLongitude());
                Log.v(LOG_TAG, "latitude: " + searchOptions.getLatitude());
                Log.v(LOG_TAG, "timeIsNow: " + searchOptions.isTimeNow());
                Log.v(LOG_TAG, "time: " + searchOptions.getTime());
                Log.v(LOG_TAG, "dayOfWeek: " + searchOptions.getDayOfWeek());

                if(searchOptions.getTypesRestaurant() != null) {
                    for (String s : searchOptions.getTypesRestaurant()) {
                        Log.v(LOG_TAG, "Restaurant-types: " + s);
                    }
                }
                if(searchOptions.getTypesBar() != null) {
                    for (String s : searchOptions.getTypesBar()) {
                        Log.v(LOG_TAG, "Bar-types: " + s);
                    }
                }
                if(searchOptions.getTypesCafe() != null) {
                    for (String s : searchOptions.getTypesCafe()) {
                        Log.v(LOG_TAG, "Cafe-types: " + s);
                    }
                }
                if(searchOptions.getTypesTakeaway() != null) {
                    for (String s : searchOptions.getTypesTakeaway()) {
                        Log.v(LOG_TAG, "Takeaway-types: " + s);
                    }
                }
            }

//            //Google Places API
//            HttpURLConnection conn = null;
//            StringBuilder jsonResults = new StringBuilder();
//
//            try {
// URL url = new URL(PLACES_SEARCH_URL +
////                        "key=" + getResources().getString(R.string.api_browser_key) +
////                "");
//
//                StringBuilder request = new StringBuilder(PLACES_SEARCH_URL);
//             request.append("key=" + getResources().getString(R.string.api_browser_key));
//                request.append("&location=" + 48.306793 + "," + 14.287260);
//                request.append("&radius=200");
//                request.append("&types=" + "restaurant");
//                request.append("&sensor=true");
//
//                URL url = new URL(request.toString());
//
//                String data = "";
//                InputStream iStream = null;
//                HttpURLConnection urlConnection = null;
//                try {
//                    // Creating an http connection to communicate with url
//                    urlConnection = (HttpURLConnection) url.openConnection();
//
//                    // Connecting to url
//                    urlConnection.connect();
//
//                    // Reading data from url
//                    iStream = urlConnection.getInputStream();
//
//                    BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
//
//                    StringBuffer sb = new StringBuffer();
//
//                    String line = "";
//                    while ((line = br.readLine()) != null) {
//                        sb.append(line);
//                        Log.v(LOG_TAG, line);
//                    }
//
//                    data = sb.toString();
//
//                    br.close();
//
//                } catch (Exception e) {
//                    Log.d(LOG_TAG, "Exception while downloading url");
//                } finally {
//                    iStream.close();
//                    urlConnection.disconnect();
//                }
//
//
//            } catch (Exception e) {
//                e.printStackTrace();
//            } finally {
//                if (conn != null) {
//                    conn.disconnect();
//                }
//            }



            return null;
        }

        @Override
        protected void onPostExecute(List<Place> result) {
            DisplaySearchResultsTask task = new DisplaySearchResultsTask();
            task.execute();
        }
    }

    //<Input for doInBackground, (Progress), Input for onPostExecute>
    private class DisplaySearchResultsTask extends AsyncTask<List<Place>, Integer, List<Place>>{

        @Override
        protected List doInBackground(List<Place>... params) {
            return null;
        }

        @Override
        protected void onPostExecute(List<Place> result) {
            Log.v(LOG_TAG, "Display");
        }
    }
}
