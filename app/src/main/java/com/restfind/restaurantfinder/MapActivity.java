package com.restfind.restaurantfinder;

import android.content.Intent;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.Map;

public class MapActivity extends AppBarActivity {

    private MapActivityType mapActivityType;
    private Map<Marker, Place> markerPlaces;
    private Map<Marker, Location> markerFriends;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Intent intent = getIntent();
        mapActivityType = (MapActivityType) intent.getSerializableExtra(getResources().getString(R.string.map_activity_type));
        ArrayList<Place> places = intent.getParcelableArrayListExtra("places");

        for(Place p : places){
            Log.v(LOG_TAG, p.getName() + ", " + p.getPlace_ID());
            Log.v(LOG_TAG, p.getLat() + ", " + p.getLng());
            Log.v(LOG_TAG, "rating: " + p.getRating());
        }

        Log.v(LOG_TAG, "Details: ");

        Place place = getPlaceDetails(places.get(1).getPlace_ID());
        Log.v(LOG_TAG, place.getName() + ", " + place.getPlace_ID());
        Log.v(LOG_TAG, place.getLat() + ", " + place.getLng());
        Log.v(LOG_TAG, "rating: " + place.getRating());
    }
}
