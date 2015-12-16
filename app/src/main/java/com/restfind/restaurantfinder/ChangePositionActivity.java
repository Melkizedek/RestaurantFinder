package com.restfind.restaurantfinder;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class ChangePositionActivity extends AppBarActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private double longitude;
    private double latitude;
    private Marker lastPos;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_position);

        intent = getIntent();
        longitude = intent.getDoubleExtra(getResources().getString(R.string.longitude), -1);
        latitude = intent.getDoubleExtra(getResources().getString(R.string.latitude), -1);

        //Set up UI
        Button btnCancel = (Button) findViewById(R.id.btnCancel);
        Button btnAccept = (Button) findViewById(R.id.btnAccept);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setResult(RESULT_CANCELED, intent);
                finish();
            }
        });
        btnAccept.setOnClickListener(new View.OnClickListener() {
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
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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

        // Add a marker in Sydney and move the camera
        LatLng currentPos = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions().position(currentPos).title("Current Position"));
//        .icon(BitmapDescriptorFactory.fromResource(R.drawable.)
        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentPos));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15.0f));

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                if(lastPos != null){
                    lastPos.remove();
                }
                lastPos = mMap.addMarker(new MarkerOptions().position(point));
            }
        });
    }
}
