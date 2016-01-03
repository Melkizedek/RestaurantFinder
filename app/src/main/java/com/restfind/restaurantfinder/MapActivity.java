package com.restfind.restaurantfinder;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.restfind.restaurantfinder.assistant.Place;
import com.restfind.restaurantfinder.assistant.SearchOptions;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapActivity extends AppBarActivity implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private FusedLocationProviderApi fusedLocationProviderApi;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;

    private GoogleMap mMap;

    private final int FAVORITE_REQUEST = 1;

    private MapActivityType mapActivityType;
    private List<Place> places;
    private Map<Marker, Place> markerPlaces;
    private Map<Marker, Location> markerFriends;
    private Marker curMarker;
    private Marker curSelectedMarker;

    //TODO: if(mapActivityType == MapActivityType.Invitations): Display friends on map, if time until invitation is close (new Service updates their locaation periodically this activity only displays them)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Intent intent = getIntent();
        mapActivityType = (MapActivityType) intent.getSerializableExtra(getResources().getString(R.string.map_activity_type));
        SearchOptions searchOptions = intent.getParcelableExtra(getResources().getString(R.string.search_options));

        markerPlaces = new HashMap<>();
        markerFriends = new HashMap<>();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if(mapActivityType == MapActivityType.SearchResults) {
            toolbar.setTitle("Search Results");
            new GetSearchResultsTask().execute(searchOptions);
        }
        else if(mapActivityType == MapActivityType.Invitations) {
            toolbar.setTitle("Invitations");
        }
        else if(mapActivityType == MapActivityType.Favorites) {
            toolbar.setTitle("Favorites");
            new GetFavoritesTask().execute();
        }
        setSupportActionBar(toolbar);
    }

    private void createMap(){
        if(places == null || places.isEmpty()){
            showAlertDialog("No Results found!");
        } else {
            buildApiClient();

            //Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        for(Place p : places){
            LatLng pos = new LatLng(p.getLat(), p.getLng());

            MarkerOptions m = new MarkerOptions().position(pos)
                    .title(p.getName())
                    .snippet("<Tap here for more Details and Options>");

            //TODO: specific icons for different types (restaurant, bar, cafe, takeaway
            if(p.getIcon().equals(getResources().getString(R.string.iconRestaurant))) {
                m.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_star_black_24dp));
            }

            markerPlaces.put(mMap.addMarker(m), p);

            if(markerPlaces.size() == 1){
                mMap.moveCamera(CameraUpdateFactory.newLatLng(pos));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(15.0f));
            }
        }

        // Set a listener for info window events.
        mMap.setOnInfoWindowClickListener(this);
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Intent intent = new Intent(MapActivity.this, PlaceDetailsActivity.class);
        intent.putExtra("placeID", markerPlaces.get(marker).getPlace_ID());

        if(mapActivityType == MapActivityType.Favorites){
            curSelectedMarker = marker;
            intent.putExtra("favorite", true);
            startActivityForResult(intent, FAVORITE_REQUEST);
        }else {
            startActivity(intent);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FAVORITE_REQUEST) {
            if (resultCode == RESULT_OK) {
                boolean deleted = data.getBooleanExtra("deleted", false);
                if(deleted){
                    markerPlaces.remove(curSelectedMarker);
                    curSelectedMarker.remove();
                }
            }
        }
    }

    /*
    Everything below gets current Position every few seconds
    buildApiClient() is the first method called
    onLocationChanged() is the last method called
     */

    private void buildApiClient(){
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(6000);
        fusedLocationProviderApi = LocationServices.FusedLocationApi;

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        if (googleApiClient != null) {
            googleApiClient.connect();
        }
    }

    private void getLocation(){
        fusedLocationProviderApi.requestLocationUpdates(googleApiClient, locationRequest, this);
//        location = fusedLocationProviderApi.getLastLocation(googleApiClient);
    }

    //starts a new Activity based on which operation is chosen
    @Override
    public void onLocationChanged(Location location) {
        LatLng currentPos = new LatLng(location.getLatitude(), location.getLongitude());

        if(mMap != null){
            if(curMarker == null){
                curMarker = mMap.addMarker(new MarkerOptions().position(currentPos).title("Your Current Location"));
            } else{
                curMarker.setPosition(currentPos);
            }
        }
    }

    //called by googleApiClient.connect()
    @Override
    public void onConnected(Bundle bundle) {
        //Check permission
        // Assume thisActivity is the current activity
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {
                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_ACCESS_FINE_LOCATION);
            }
        }else{
            getLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted
                    getLocation();

                } else {
                    // permission denied
                }
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    //<Input for doInBackground, (Progress), Input for onPostExecute>
    private class GetFavoritesTask extends AsyncTask<Void, Integer, ArrayList<Place>> {

        @Override
        protected ArrayList<Place> doInBackground(Void... params) {
            //Get current logged-in username
            String username = getCurrentUsername();

            ArrayList<Place> placesTmp = new ArrayList<>();
            List<String> placeIDs;

            try {
                placeIDs = Database.getFavorites(username);
            } catch (Exception e) {
                //Could not connect to Server with .php-files
                return null;
            }
            if(placeIDs == null){
                return null;
            }
            for(String s : placeIDs){
                placesTmp.add(getPlaceDetails(s));
            }
            return placesTmp;
        }

        //puts the friend-requests and friends into the listView
        @Override
        protected void onPostExecute(ArrayList<Place> result) {
            findViewById(R.id.loadingPanel).setVisibility(View.GONE);

            if(result == null){
                showAlertDialog(getResources().getString(R.string.connection_error));
            }
            places = result;
            createMap();
        }
    }

    //<Input for doInBackground, (Progress), Input for onPostExecute>
    private class GetSearchResultsTask extends AsyncTask<SearchOptions, Integer, List<Place>> {

        private List<String> typesRestaurant;
        private List<String> typesBar;
        private List<String> typesCafe;
        private List<String> typesTakeaway;

        private boolean searched = false;
        private String requestPartOne;
        private List<Place> results;

        private String checkTypes (){
            boolean isFirst = true;
            StringBuilder builderTyp = new StringBuilder();
            if(!typesRestaurant.isEmpty()) {
                builderTyp.append("restaurant");
                isFirst = false;
            }
            if(!typesBar.isEmpty()) {
                if(isFirst) {
                    builderTyp.append("bar");
                    isFirst = false;
                }else {
                    builderTyp.append("|bar");
                }

            }
            if(!typesCafe.isEmpty()) {
                if (isFirst) {
                    builderTyp.append("cafe");
                    isFirst = false;
                } else {
                    builderTyp.append("|cafe");
                }
            }
            if(!typesTakeaway.isEmpty()) {
                if(isFirst) {
                    builderTyp.append("takeaway");
                }else {
                    builderTyp.append("|takeaway");
                }
            }
            return builderTyp.toString();
        }

        private void searchByKeyword(List<String> subTypes, String mainType){
            if(!subTypes.isEmpty() && !subTypes.get(0).equals(mainType)) {
                searched = true;
                for (String s : subTypes) {
                    try {
                        List<Place> placesTmp = createPlaces(getApiResult(requestPartOne + "&keyword=" + s.replace(" ", "%20") + "&types=" + mainType.toLowerCase()));

                        if(placesTmp != null) {
                            results.addAll(placesTmp);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        @Override
        protected List<Place> doInBackground(SearchOptions... params) {
            SearchOptions searchOptions = params[0];
            typesRestaurant = searchOptions.getTypesRestaurant();
            typesBar = searchOptions.getTypesBar();
            typesCafe = searchOptions.getTypesCafe();
            typesTakeaway = searchOptions.getTypesTakeaway();
            StringBuilder builder = new StringBuilder();

            //Nearby Search
            builder.append("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
            builder.append("key=");
            builder.append(getResources().getString(R.string.api_browser_key));
            builder.append("&location=");
            builder.append(searchOptions.getLatitude());
            builder.append(",");
            builder.append(searchOptions.getLongitude());
            builder.append("&radius=");
            builder.append(searchOptions.getRadius());
            builder.append("&sensor=true");
            if(!searchOptions.getName().isEmpty()){
                builder.append("&name=" + searchOptions.getName());
            }
            if(searchOptions.isTimeNow()){
                builder.append("&openNow=true");
            }
            requestPartOne = builder.toString();
            results = new ArrayList<>();

            searchByKeyword(typesRestaurant, "Restaurant");
            searchByKeyword(typesBar, "Bar");
            searchByKeyword(typesCafe, "Cafe");
            searchByKeyword(typesTakeaway, "Takeaway");

            if(!searched && !typesRestaurant.isEmpty() && !typesBar.isEmpty() && !typesCafe.isEmpty() && !typesTakeaway.isEmpty()){
                try {
                    results = createPlaces(getApiResult(requestPartOne + "&types=" + checkTypes()));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return results;
        }

        @Override
        protected void onPostExecute(List<Place> result) {
            findViewById(R.id.loadingPanel).setVisibility(View.GONE);

            places = result;
            createMap();

//            Intent intent = new Intent(SearchOptionsActivity.this, MapActivity.class);
//            intent.putExtra(getResources().getString(R.string.map_activity_type), MapActivityType.SearchResults);
//            intent.putParcelableArrayListExtra("places", (ArrayList<Place>)places);
//
//            startActivity(intent);
        }
    }
}
