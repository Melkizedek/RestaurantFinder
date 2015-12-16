package com.restfind.restaurantfinder;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class SearchOptionsActivity extends AppBarActivity implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener{

    private enum Operation {
        ChangePosition, Search
    }
    private Operation operation;

    private static final int MY_PERMISSIONS_ACCESS_FINE_LOCATION = 1;

    private FusedLocationProviderApi fusedLocationProviderApi;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;

    private final String TAG_LOG = "SearchOptionsActivity";
    private final int CHANGE_POSITION_REQUEST = 1;

    private int radius;
    private List<String> types;
    private boolean timeIsNow;
    private boolean dateIsNow;
    private int dayOfWeek; //Sunday = 1, Saturday = 7
    private String chosenTime;

    private boolean chosenNewPosition;
    private double chosenLongitude;
    private double chosenLatitude;

    private TextView tvTime;
    private RadioButton rbCurrentPos;
    private List<CheckBox> checkBoxesRest;
    private List<CheckBox> checkBoxesCafe;
    private List<CheckBox> checkBoxesBar;
    private List<CheckBox> checkBoxesTakeaway;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_options);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Search Options");
        setSupportActionBar(toolbar);

        timeIsNow = true;
        dateIsNow = true;
        chosenNewPosition = false;

        //Set up UI-Elements
        final Button btnSearch = (Button) findViewById(R.id.btnSearch);
        final EditText etSearchText = (EditText) findViewById(R.id.etSearchText);
        final EditText etRadius = (EditText) findViewById(R.id.etRadius);
        final CheckBox cbCafe = (CheckBox) findViewById(R.id.cbCafe);
        final CheckBox cbBar = (CheckBox) findViewById(R.id.cbBar);
        final CheckBox cbRestaurant = (CheckBox) findViewById(R.id.cbRestaurant);
        final CheckBox cbTakeaway = (CheckBox) findViewById(R.id.cbTakeaway);
        final LinearLayout llCbRest = (LinearLayout) findViewById(R.id.llCheckboxesRest);
        final LinearLayout llCbBar = (LinearLayout) findViewById(R.id.llCheckboxesBar);
        final LinearLayout llCbCafe = (LinearLayout) findViewById(R.id.llCheckboxesCafe);
        final LinearLayout llCbTakeaway = (LinearLayout) findViewById(R.id.llCheckboxesTakeaway);
        final Button btnChangePos = (Button) findViewById(R.id.btnChangePos);
        final RadioGroup rgPos = (RadioGroup) findViewById(R.id.rgPos);
        rbCurrentPos = (RadioButton) findViewById(R.id.rbCurrentPos);
        final TextView tvDate = (TextView) findViewById(R.id.tvDate);
        tvTime = (TextView) findViewById(R.id.tvTime);

        /*
        EditTexts SearchText & Radius
         */
        //EditTexts are only focused when touched, not when starting the Activity
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

        /*
        RadioGroup: Use current position or use custom position
         */
        rgPos.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rbCurrentPos:
                        btnChangePos.setVisibility(View.GONE);
                        return;
                    case R.id.rbCustomPos:
                        btnChangePos.setVisibility(View.VISIBLE);
                }
            }
        });
        rgPos.check(R.id.rbCurrentPos);

        /*
        Button: Change Position
         */
        btnChangePos.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Get current Position and start ChangePositionActivity
                operation = Operation.ChangePosition;
                buildApiClient();
            }
        });

        /*
        TextView: Date
         */
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");
        String formattedDate = df.format(c.getTime());
        tvDate.setText(formattedDate);

        dayOfWeek = c.get(Calendar.DAY_OF_WEEK);

        //Change Date
        tvDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar c = Calendar.getInstance();
                final int year = c.get(Calendar.YEAR);
                final int month = c.get(Calendar.MONTH);
                final int day = c.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(SearchOptionsActivity.this, new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker datePicker, int selectedYear, int selectedMonth, int selectedDay) {
                                if(year != selectedYear || month != selectedMonth || day != selectedDay) {
                                    dateIsNow = false;
                                } else{
                                    dateIsNow = true;
                                }

                                Calendar calNow = Calendar.getInstance();
                                Calendar calSet = (Calendar) calNow.clone();

                                calSet.set(Calendar.DATE, selectedDay);
                                calSet.set(Calendar.MONTH, selectedMonth);
                                calSet.set(Calendar.YEAR, selectedYear);

                                long time_val = calSet.getTimeInMillis();

                                String formatted_date = (DateFormat.format("dd.MM.yyyy", time_val))
                                        .toString();

                                tvDate.setText(formatted_date);

                                dayOfWeek = calSet.get(Calendar.DAY_OF_WEEK);
                            }
                        }, year, month, day);

                dialog.setTitle("Select Date");
                dialog.show();

            }
        });


        /*
        TextView: Time
         */
        //Change Time
        tvTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar c = Calendar.getInstance();
                final int min = c.get(Calendar.MINUTE);
                int hour = c.get(Calendar.HOUR);
                if(c.get(Calendar.AM_PM) == Calendar.PM){
                    hour += 12;
                }
                final int curHour = hour;

                TimePickerDialog dialog;
                dialog = new TimePickerDialog(SearchOptionsActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        String hourString = String.valueOf(selectedHour);
                        String minString = String.valueOf(selectedMinute);

                        if(selectedHour <= 9){
                            hourString = "0" + hourString;
                        }
                        if(selectedMinute <= 9){
                            minString = "0" + minString;
                        }
                        chosenTime = hourString + ":" + minString;

                        if(curHour != selectedHour || min != selectedMinute) {
                            timeIsNow = false;
                            tvTime.setText(chosenTime);
                        } else{
                            timeIsNow = true;
                            tvTime.setText("Now");
                        }
                    }
                }, curHour, min, true);

                dialog.setTitle("Select Time");
                dialog.show();

            }
        });

        /*
        Checkboxes: Restaurant, Bar, Cafe, Takeaway Types
         */
        checkBoxesRest = new ArrayList<>();
        checkBoxesBar = new ArrayList<>();
        checkBoxesCafe = new ArrayList<>();
        checkBoxesTakeaway = new ArrayList<>();

        //Get Restaurant-Types out of String-Array
        String[] restaurantTypesArray = getResources().getStringArray(R.array.restaurant_types);
        String[] barTypesArray = getResources().getStringArray(R.array.bar_types);
        String[] cafeTypesArray = getResources().getStringArray(R.array.cafe_types);
        String[] takeawayTypesArray = getResources().getStringArray(R.array.takeaway_types);

        //create sub-types for all types
        setupCheckboxes(cbBar, checkBoxesBar, barTypesArray, llCbBar);
        setupCheckboxes(cbCafe, checkBoxesCafe, cafeTypesArray, llCbCafe);
        setupCheckboxes(cbRestaurant, checkBoxesRest, restaurantTypesArray, llCbRest);
        setupCheckboxes(cbTakeaway, checkBoxesTakeaway, takeawayTypesArray, llCbTakeaway);


        /*
        Button: Search
         */
        btnSearch.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //TODO: create SearchOptions
                if(chosenNewPosition && !rbCurrentPos.isChecked()) {
                    Log.v(TAG_LOG, "chosenLongitude: " + chosenLongitude);
                    Log.v(TAG_LOG, "chosenLatitude: " + chosenLatitude);
                    SearchOptions options = new SearchOptions("");

                    Intent intent = new Intent(SearchOptionsActivity.this, SearchResultsActivity.class);
                    intent.putExtra(getResources().getString(R.string.search_options), options);
                    startActivity(intent);
                }else{
                    Log.v(TAG_LOG, "currentLocation");

                    //Get current Position and start SearchResultsActivity
//                operation = Operation.Search;
//                buildApiClient();
                }
            }
        });
    }

    //Creates all sub-Checkboxes (for Restaurants, Cafes, Bars, Takeaway) and gives them Listeners
    private void setupCheckboxes(final CheckBox cbType, final List<CheckBox> cbSubTypes, final String[] typeArray, LinearLayout llcbType){
        //Restaurant-Checkbox checks or unchecks all Sub-Checkboxes
        cbType.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                CheckBox cb = (CheckBox) v;
                if (cb.isChecked()) {
                    for (CheckBox b : cbSubTypes) {
                        b.setChecked(true);
                    }
                } else {
                    for (CheckBox b : cbSubTypes) {
                        b.setChecked(false);
                    }
                }
            }
        });

        //Create Checkboxes out of those Takeaway-Types and add them to the ListView and a List
        for(int i = 0; i < typeArray.length; i++){
            CheckBox cb = new CheckBox(this);
            cb.setText(typeArray[i]);

            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    //All Sub-Checkboxes are now checked -> Restaurant-Checkbox gets checked
                    if (isChecked && allCheckboxesChecked(cbSubTypes)) {
                        cbType.setChecked(true);
                    }
                    //All Sub-Checkboxes aren't checked now -> Restaurant-Checkbox gets unchecked
                    else {
                        cbType.setChecked(false);
                    }
                }
            });
            llcbType.addView(cb);
            cbSubTypes.add(cb);
        }
    }

    //Checks if all Sub-Checkboxes of Restaurant are checked
    private boolean allCheckboxesChecked(List<CheckBox> checkBoxes){
        for(CheckBox cb : checkBoxes){
            if(!cb.isChecked()){
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(timeIsNow) {
            Calendar c = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("HH:mm");
            chosenTime = df.format(c.getTime());

            tvTime.setText("Now");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        //TODO: Save Options to SharedPreferences
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CHANGE_POSITION_REQUEST) {
            if (resultCode == RESULT_OK) {
                chosenLongitude = data.getDoubleExtra(getResources().getString(R.string.newLongitude), 0);
                chosenLatitude = data.getDoubleExtra(getResources().getString(R.string.newLatitude), 0);

                chosenNewPosition = true;
            }
        }
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

    //starts a new Activity based on which operation is chosen
    @Override
    public void onLocationChanged(Location location) {
        double longitude = location.getLongitude();
        double latitude = location.getLatitude();

        if(operation == Operation.ChangePosition){
            Intent intent = new Intent(SearchOptionsActivity.this, ChangePositionActivity.class);
            intent.putExtra(getResources().getString(R.string.longitude), longitude);
            intent.putExtra(getResources().getString(R.string.latitude), latitude);

            startActivityForResult(intent, CHANGE_POSITION_REQUEST);
        }
        else if(operation == Operation.Search) {
            List<String> types = new ArrayList<String>();
            types.add("test1");
            types.add("test2");

            Intent intent = new Intent(SearchOptionsActivity.this, SearchResultsActivity.class);
            intent.putExtra(getResources().getString(R.string.search_options), new SearchOptions(location.getLongitude(), location.getLatitude(), radius, types));
            startActivity(intent);
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
