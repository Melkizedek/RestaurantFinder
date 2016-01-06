package com.restfind.restaurantfinder;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.restfind.restaurantfinder.assistant.Invitation;
import com.restfind.restaurantfinder.assistant.Place;
import com.restfind.restaurantfinder.assistant.SearchOptions;
import com.restfind.restaurantfinder.database.Database;

import org.json.JSONException;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MapActivity extends AppBarActivity implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private FusedLocationProviderApi fusedLocationProviderApi;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;

    private GoogleMap mMap;

    private final int FAVORITE_REQUEST = 1;

    private MapActivityType mapActivityType;
    private List<Place> places;
    private Map<Marker, Place> markerPlaces;
    private Map<String, Marker> markerParticipants;
    private Marker curMarker;
    private Marker curSelectedMarker;
    private Invitation invitation;
    private String username;
    private CheckBox cbAllowTracking;

    private LinearLayout info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Intent intent = getIntent();
        mapActivityType = (MapActivityType) intent.getSerializableExtra(getResources().getString(R.string.map_activity_type));
        SearchOptions searchOptions = intent.getParcelableExtra(getResources().getString(R.string.search_options));
        invitation = intent.getParcelableExtra("invitation");

        username = getCurrentUsername();

        markerPlaces = new HashMap<>();
        markerParticipants = new HashMap<>();
        places = new ArrayList<>();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if(mapActivityType == MapActivityType.SearchResults) {
            toolbar.setTitle("Search Results");
            new GetSearchResultsTask().execute(searchOptions);
        }
        else if(mapActivityType == MapActivityType.Invitation) {
            toolbar.setTitle("Invitations");
            new GetInvitationTask().execute();
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
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        for(Place p : places){
            LatLng pos = new LatLng(p.getLat(), p.getLng());
            String snippet = "&lt;Tap here for more details/options&gt;";
            StringBuilder builder = new StringBuilder();

            if(mapActivityType == MapActivityType.Invitation){
                builder.append("Time: ");
                builder.append(new SimpleDateFormat("dd.MM.yyyy - HH:mm").format(new Timestamp(invitation.getTime())));
                builder.append("<br><br>Participants:");
                builder.append("<br>");
                builder.append(invitation.getHost());
                builder.append(" (host)");

                TreeMap<String, Integer> tree = new TreeMap(invitation.getInvitees());
                Map<String, Integer> sorted = sortByValue(tree);

                for(Map.Entry<String, Integer> e : sorted.entrySet()){
                    String acceptType;
                    if(e.getValue() == 1){
                        acceptType = "<font color=#00FF00>(accepted)</font>";
                    } else if(e.getValue() == 0){
                        acceptType = "(undecided)";
                    } else{
                        acceptType = "<font color=#ff0000>(declined)</font>";
                    }
                    builder.append("<br>");
                    builder.append(e.getKey());
                    builder.append(" ");
                    builder.append(acceptType);
                }
                builder.append("<br><br>");
            }
            snippet = builder.toString() + snippet;

            MarkerOptions m = new MarkerOptions().position(pos)
                    .title(p.getName())
                    .snippet(snippet);

            if(p.getIcon().equals(getResources().getString(R.string.iconCafe))) {
                m.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_local_cafe_black_24dp));
            }
            else if(p.getIcon().equals(getResources().getString(R.string.iconBar))) {
                m.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_local_bar_black_24dp));
            }
            else {
                m.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_local_dining_black_24dp));
            }

            markerPlaces.put(mMap.addMarker(m), p);

            if(markerPlaces.size() == 1){
                mMap.moveCamera(CameraUpdateFactory.newLatLng(pos));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(15.0f));
            }
        }

        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                info = new LinearLayout(MapActivity.this);
                info.setOrientation(LinearLayout.VERTICAL);

                TextView title = new TextView(MapActivity.this);
                title.setTextColor(Color.BLACK);
                title.setGravity(Gravity.CENTER);
                title.setTypeface(null, Typeface.BOLD);
                title.setText(marker.getTitle());

                info.addView(title);

                if(marker.getSnippet() != null) {
                    TextView snippet = new TextView(MapActivity.this);
                    snippet.setText(Html.fromHtml(marker.getSnippet()));
                    info.addView(snippet);
                }
                return info;
            }
        });

        // Set a listener for info window events.
        mMap.setOnInfoWindowClickListener(this);

        if(mapActivityType == MapActivityType.Invitation){
            Marker m = markerPlaces.entrySet().iterator().next().getKey();
            m.showInfoWindow();

            displayParticipants();
        }
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Intent intent = new Intent(MapActivity.this, PlaceDetailsActivity.class);
        intent.putExtra("placeID", markerPlaces.get(marker).getPlace_ID());

        if(mapActivityType != MapActivityType.SearchResults) {
            intent.putExtra("place", markerPlaces.get(marker));
        }
        if (mapActivityType == MapActivityType.Favorites) {
            curSelectedMarker = marker;
            intent.putExtra("favorite", true);
            startActivityForResult(intent, FAVORITE_REQUEST);
        } else{
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
    }

    //starts a new Activity based on which operation is chosen
    @Override
    public void onLocationChanged(Location location) {
        LatLng currentPos = new LatLng(location.getLatitude(), location.getLongitude());

        if(mMap != null){
            if(curMarker == null){
                curMarker = mMap.addMarker(new MarkerOptions().position(currentPos).title("Your Current Location").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_my_location_black_24dp)));
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

    private void displayParticipants(){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                long timeTillDeadline = invitation.getTime() - Calendar.getInstance().getTimeInMillis();
                Log.v(LOG_TAG, "timeTillDeadline: " + timeTillDeadline);
                //Time till deadline <= 15 minutes
                if(timeTillDeadline > 900000){
                    try {
                        Log.v(LOG_TAG, "sleeping for: " + (timeTillDeadline - 900000));
                        Thread.sleep(timeTillDeadline - 900000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                timeTillDeadline = invitation.getTime() - Calendar.getInstance().getTimeInMillis();
                //until 1 hour after deadline
                while(timeTillDeadline > (-3600000)){
                    Log.v(LOG_TAG, "timeTillDeadline: " + timeTillDeadline);

                    new GetParticipantsLocationTask().execute();

                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    timeTillDeadline = invitation.getTime() - Calendar.getInstance().getTimeInMillis();
                }
            }
        });
        thread.start();
    }

    //<Input for doInBackground, (Progress), Input for onPostExecute>
    private class GetParticipantsLocationTask extends AsyncTask<Void, Integer, Map<String, LatLng>> {

        @Override
        protected Map<String, LatLng> doInBackground(Void... params) {
            Map<String, LatLng> result = new HashMap<>();
            try {
                List<String> userLocations = Database.getUserLocations(String.valueOf(invitation.getId()));
                for(String s : userLocations){
//                    Log.v(LOG_TAG, s);
                    String[] split = s.split(";");
                    if(split.length == 3 && !split[0].equals(username)) {
                        result.put(split[0], new LatLng(Double.parseDouble(split[1]), Double.parseDouble(split[2])));
                    }
                }
            } catch (IOException e) {
                return null;
            }
            return result;
        }

        @Override
        protected void onPostExecute(Map<String, LatLng> result) {
            if(result != null) {
                for (Map.Entry<String, LatLng> e : result.entrySet()) {
                    if (markerParticipants.containsKey(e.getKey())) {
                        markerParticipants.get(e.getKey()).setPosition(e.getValue());
                    } else {
                        markerParticipants.put(e.getKey(), mMap.addMarker(new MarkerOptions().position(e.getValue()).title(e.getKey()).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_directions_run_black_24dp))));
                    }
                }
            } else{
//                showAlertDialog(getResources().getString(R.string.connection_error));
            }
        }
    }

    //<Input for doInBackground, (Progress), Input for onPostExecute>
    private class GetInvitationTask extends AsyncTask<Void, Integer, Place> {

        @Override
        protected Place doInBackground(Void... params) {
            return getPlaceDetails(invitation.getPlaceID());
        }

        @Override
        protected void onPostExecute(Place result) {
            if(result != null) {
                findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                //after accept
                if(!places.isEmpty()) {
                    places = new ArrayList<>();
                    markerPlaces.keySet().iterator().next().hideInfoWindow();
                    markerPlaces.keySet().iterator().next().remove();
                    markerPlaces = new HashMap<>();
                    invitation.getInvitees().put(username, 1);
                }
                places.add(result);
                createMap();

                if(invitation.getInvitees().get(username) != null && invitation.getInvitees().get(username) == 0) {
                    findViewById(R.id.llInvitationButtons).setVisibility(View.VISIBLE);
                    Button btnAccept = (Button) findViewById(R.id.btnAccept);
                    Button btnDecline = (Button) findViewById(R.id.btnDecline);
                    cbAllowTracking = (CheckBox) findViewById(R.id.cbAllowTracking);

                    btnAccept.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
                            new AcceptInvitationTask().execute(cbAllowTracking.isChecked());
                        }
                    });
                    btnDecline.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
                            new DeclineInvitationTask().execute(cbAllowTracking.isChecked());
                        }
                    });
                }
            }
        }
    }

    //<Input for doInBackground, (Progress), Input for onPostExecute>
    private class AcceptInvitationTask extends AsyncTask<Boolean, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(Boolean... params) {
            try {
                Database.acceptInvitation(String.valueOf(invitation.getId()), username);
            } catch (Exception e) {
                return null;
            }

            SharedPreferences spTracking = getApplicationContext().getSharedPreferences("tracking", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = spTracking.edit();
            editor.putBoolean("tracking", params[0]);
            editor.apply();

            if (!params[0]) {
                try {
                    Database.deleteUserLocation(username);
                } catch (Exception e) {
                    //Could not connect to Server with .php-files
                    return null;
                }
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            findViewById(R.id.loadingPanel).setVisibility(View.GONE);

            if(result != null){
                Toast.makeText(MapActivity.this, "Accepted", Toast.LENGTH_SHORT).show();
                findViewById(R.id.llInvitationButtons).setVisibility(View.GONE);
                new GetInvitationTask().execute();
            } else{
                showAlertDialog(getResources().getString(R.string.connection_error));
            }
        }
    }

    //<Input for doInBackground, (Progress), Input for onPostExecute>
    private class DeclineInvitationTask extends AsyncTask<Boolean, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(Boolean... params) {
            try {
                Database.declineInvitation(String.valueOf(invitation.getId()), username);
            } catch (Exception e) {
                return null;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            findViewById(R.id.loadingPanel).setVisibility(View.GONE);

            if (result != null){
                Toast.makeText(MapActivity.this, "Declined", Toast.LENGTH_SHORT).show();
                finish();
            } else{
                showAlertDialog(getResources().getString(R.string.connection_error));
            }
        }
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
                    builderTyp.append("meal_takeaway");
                }else {
                    builderTyp.append("|meal_takeaway");
                }
            }
            return builderTyp.toString();
        }

        private void searchByKeyword(List<String> subTypes, String mainType){
            if(mainType.equals("Takeaway")){
                mainType = "meal_takeaway";
            }
            if(!subTypes.isEmpty()) {
                for (String s : subTypes) {
                    try {
                        String request;
                        if(s.equals(mainType)){
                            request = requestPartOne + "&types=" + mainType.toLowerCase();
                        } else{
                            request = requestPartOne + "&keyword=" + s.replace(" ", "%20") + "&types=" + mainType.toLowerCase();
                        }
                        Log.v(LOG_TAG, request);
                        List<Place> placesTmp = createPlaces(getApiResult(request));

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
                builder.append("&name=");
                builder.append(searchOptions.getName());
            }
            if(searchOptions.isTimeNow()){
                builder.append("&openNow=true");
            }
            requestPartOne = builder.toString();
            results = new ArrayList<>();

            if((!typesRestaurant.isEmpty() && !typesRestaurant.get(0).equals("Restaurant"))
                    || (!typesBar.isEmpty() && !typesBar.get(0).equals("Bar"))
                    || (!typesCafe.isEmpty() && !typesCafe.get(0).equals("Cafe"))
                    || (!typesTakeaway.isEmpty() && !typesTakeaway.get(0).equals("Takeaway"))) {
                searchByKeyword(typesRestaurant, "Restaurant");
                searchByKeyword(typesBar, "Bar");
                searchByKeyword(typesCafe, "Cafe");
                searchByKeyword(typesTakeaway, "Takeaway");
            }else {
                try {
                    Log.v(LOG_TAG, requestPartOne + "&types=" + checkTypes());
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
        }
    }
}
