package com.restfind.restaurantfinder;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Activity in which you can change the location from where you want to search Restaurants
 */
public class ChangePositionActivity extends AppBarActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private double longitude;
    private double latitude;
    private int radius;
    private Marker lastPos;
    private Circle circle;
    private Intent intent;

    /**
     * OnClickListener that returns to the last Activity without a new Location
     */
    private View.OnClickListener listenerCancel = new View.OnClickListener() {
        public void onClick(View v) {
            setResult(RESULT_CANCELED, intent);
            finish();
        }
    };

    /**
     * OnClickListener that returns to the last Activity with a new Location
     */
    private View.OnClickListener listenerAccept = new View.OnClickListener() {
        public void onClick(View v) {
            if(lastPos != null){
                double newLongitude = lastPos.getPosition().longitude;
                double newLatitude = lastPos.getPosition().latitude;

                intent.putExtra(getResources().getString(R.string.newLongitude), newLongitude);
                intent.putExtra(getResources().getString(R.string.newLatitude), newLatitude);

                setResult(RESULT_OK, intent);
                finish();
            }
        }
    };

    /**
     * OnMapClickListener that places a Marker on the clicked Position and draws a Circle around it
     */
    private GoogleMap.OnMapClickListener onMapClickListener = new GoogleMap.OnMapClickListener() {
        @Override
        public void onMapClick(LatLng point) {
            if(lastPos != null){
                lastPos.remove();
            }
            lastPos = mMap.addMarker(new MarkerOptions().position(point));

            if(circle != null){
                circle.setCenter(lastPos.getPosition());
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_position);

        intent = getIntent();
        longitude = intent.getDoubleExtra(getResources().getString(R.string.longitude), -1);
        latitude = intent.getDoubleExtra(getResources().getString(R.string.latitude), -1);
        radius = intent.getIntExtra("radius", 0);

        //Set up UI
        Button btnCancel = (Button) findViewById(R.id.btnCancel);
        Button btnAccept = (Button) findViewById(R.id.btnAccept);

        btnCancel.setOnClickListener(listenerCancel);
        btnAccept.setOnClickListener(listenerAccept);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in current Position and move the camera
        LatLng currentPos = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions().position(currentPos).title("Current Position").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_my_location_black_24dp)));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentPos));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15.0f));

        //Add a circle around the current Position
        if(radius > 0){
            circle = mMap.addCircle(new CircleOptions()
                    .center(new LatLng(latitude, longitude))
                    .radius(radius)
                    .strokeColor(Color.RED)
                    .fillColor(0x50ed6f01));
        }

        //create new Marker based on the touched Location
        mMap.setOnMapClickListener(onMapClickListener);
    }
}
