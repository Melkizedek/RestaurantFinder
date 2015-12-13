package com.restfind.restaurantfinder;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

public class SearchOptionsActivity extends AppBarActivity implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener{

    private static final int MY_PERMISSIONS_ACCESS_FINE_LOCATION = 1;

    private FusedLocationProviderApi fusedLocationProviderApi;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;

    private int radius;
//    private List<String> types;

    private List<CheckBox> checkBoxesRest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_options);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Set up UI-Elements
        final Button btnSearch = (Button) findViewById(R.id.btnSearch);
        final EditText etSearchText = (EditText) findViewById(R.id.etSearchText);
        final EditText etRadius = (EditText) findViewById(R.id.radiusEditText);
        final CheckBox cbCafe = (CheckBox) findViewById(R.id.cbCafe);
        final CheckBox cbPub = (CheckBox) findViewById(R.id.cbPub);
        final CheckBox cbRest = (CheckBox) findViewById(R.id.cbRestaurant);
        final LinearLayout llCbRest = (LinearLayout) findViewById(R.id.llCheckboxesRest);

        checkBoxesRest = new ArrayList<>();
        String[] restaurantTypes = getResources().getStringArray(R.array.restaurant_types);

        for(int i = 0; i < restaurantTypes.length; i++){
            CheckBox cb = new CheckBox(this);
            cb.setText(restaurantTypes[i]);

            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked && allCheckboxesChecked()) {
                        cbRest.setChecked(true);
                    } else {
                        cbRest.setChecked(false);
                    }
                }
            });

            llCbRest.addView(cb);

            checkBoxesRest.add(cb);
        }

        etSearchText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.setFocusable(true);
                v.setFocusableInTouchMode(true);
                return false;
            }
        });

        etRadius.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.setFocusable(true);
                v.setFocusableInTouchMode(true);
                return false;
            }
        });

//        etRadius.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//
//            public void onFocusChange(View v, boolean hasFocus) {
//                if (!hasFocus && !etRadius.getText().toString().isEmpty()) {
//                    Log.v("SearchOptions", "focus lost");
//                    etRadius.setText(etRadius.getText().toString() + " meters");
//                }
//            }
//        });

        /*Set up Spinner
        0...Search by Options
        1...Search by Name
         */
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.search_options_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(0);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    etSearchText.setVisibility(View.GONE);
                } else if (position == 1) {
                    etSearchText.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        cbRest.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                CheckBox cb = (CheckBox) v;
                if(cb.isChecked()){
                    for(CheckBox b : checkBoxesRest){
                        b.setChecked(true);
                    }
                } else{
                    for(CheckBox b : checkBoxesRest){
                        b.setChecked(false);
                    }
                }
            }
        });

        //Search-Button
        btnSearch.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(SearchOptionsActivity.this, SearchResultsActivity.class);
                intent.putExtra(getResources().getString(R.string.search_options), new SearchOptions(""));
                startActivity(intent);
            }
        });
    }

    private boolean allCheckboxesChecked(){
        for(CheckBox cb : checkBoxesRest){
            if(!cb.isChecked()){
                return false;
            }
        }
        return true;
    }






    /*
    Everything below gets current Position once
    buildApiClient() is the first method called
    onLocationChanged() is the last method called
     */

    private void buildApiClient(){
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setNumUpdates(1);
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

    //repeatedly called when fusedLocationProviderApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    @Override
    public void onLocationChanged(Location location) {
        List<String> types = new ArrayList<String>();
        types.add("test1");
        types.add("test2");

        Intent intent = new Intent(SearchOptionsActivity.this, SearchResultsActivity.class);
        intent.putExtra(getResources().getString(R.string.search_options), new SearchOptions(location.getLongitude(), location.getLatitude(), radius, types));
        startActivity(intent);
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
                return;
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    @Override
    public void onBackPressed() {
        //Do nothing (don't go back to Login)
    }
}
