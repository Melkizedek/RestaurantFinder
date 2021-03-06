package com.restfind.restaurantfinder;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.restfind.restaurantfinder.assistant.SearchOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Gives the user multiple options to adjust the search for places (Restaurants, Cafes, Bars and Takeaways)
 */
public class SearchOptionsActivity extends AppBarActivity implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener{

    /**
     * Enum to select, which Activity needs to be started
     */
    private enum Operation {
        ChangePosition, Search
    }
    private Operation operation;

    private FusedLocationProviderApi fusedLocationProviderApi;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;

    private final int CHANGE_POSITION_REQUEST = 1;

    private boolean timeIsNow;
    private boolean dateIsNow;
    private int dayOfWeek; //Sunday = 1, Saturday = 7
    private String chosenTime;

    private boolean chosenNewPosition;
    private double chosenLongitude;
    private double chosenLatitude;
    private double longitude;
    private double latitude;

    private EditText etName;
    private EditText etRadius;
    private TextView tvTime;
    private RadioButton rbCurrentPos;

    private CheckBox cbCafe;
    private CheckBox cbBar;
    private CheckBox cbRestaurant;
    private CheckBox cbTakeaway;

    private List<CheckBox> checkBoxesRest;
    private List<CheckBox> checkBoxesCafe;
    private List<CheckBox> checkBoxesBar;
    private List<CheckBox> checkBoxesTakeaway;

    /**
     * sets up all UI-Features and loads all saved options from SharedPreferences
     */
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
        etName = (EditText) findViewById(R.id.etSearchText);
        etRadius = (EditText) findViewById(R.id.etRadius);
        cbCafe = (CheckBox) findViewById(R.id.cbCafe);
        cbBar = (CheckBox) findViewById(R.id.cbBar);
        cbRestaurant = (CheckBox) findViewById(R.id.cbRestaurant);
        cbTakeaway = (CheckBox) findViewById(R.id.cbTakeaway);
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
        etName.setOnTouchListener(new View.OnTouchListener() {
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
        etRadius.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus && etRadius.getText() != null && !etRadius.getText().toString().isEmpty()) {
                    int radius = Integer.parseInt(etRadius.getText().toString());
                    if (radius > 50000) {
                        etRadius.setText("50000");
                    }
                }
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
        Search-Button: calls buildApiClient() and sets operation to Operation.Search
         */
        btnSearch.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(etRadius.getText() == null || etRadius.getText().toString().isEmpty()) {
                    showAlertDialog("You need to enter a Radius!");
                }else {
                    if (chosenNewPosition && !rbCurrentPos.isChecked()) {
                        Log.v(LOG_TAG, "CustomPos");
                        startMapActivity();
                    } else {
                        //Get current Position and start MapActivity
                        operation = Operation.Search;
                        buildApiClient();
                    }
                }
            }
        });

        //Get saved SharedPreferences
        SharedPreferences spOptions = getApplicationContext().getSharedPreferences(getResources().getString(R.string.search_options), Context.MODE_PRIVATE);
        etRadius.setText(spOptions.getString("radius", "500"));

        if(spOptions.contains("restaurant")) {
            cbRestaurant.performClick();
        } else{
            for(int i = 0; i < checkBoxesRest.size(); i++){
                checkBoxesRest.get(i).setChecked(spOptions.getBoolean("restaurant" + i, false));
            }
        }
        if(spOptions.contains("bar")) {
            cbBar.performClick();
        } else{
            for(int i = 0; i < checkBoxesBar.size(); i++){
                checkBoxesBar.get(i).setChecked(spOptions.getBoolean("bar" + i, false));
            }
        }
        if(spOptions.contains("cafe")) {
            cbCafe.performClick();
        } else{
            for(int i = 0; i < checkBoxesCafe.size(); i++){
                checkBoxesCafe.get(i).setChecked(spOptions.getBoolean("cafe" + i, false));
            }
        }
        if(spOptions.contains("takeaway")) {
            cbTakeaway.performClick();
        } else{
            for(int i = 0; i < checkBoxesTakeaway.size(); i++){
                checkBoxesTakeaway.get(i).setChecked(spOptions.getBoolean("takeaway" + i, false));
            }
        }
    }

    /**
     * Creates all sub-Checkboxes (for Restaurants, Cafes, Bars, Takeaway) and gives them Listeners
     */
    private void setupCheckboxes(final CheckBox cbType, final List<CheckBox> cbSubTypes, final String[] typeArray, LinearLayout llcbType){
        //Main-Type-Checkbox checks or unchecks all Sub-Checkboxes
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

        //Create Checkboxes out of those Types and add them to the ListView and a List
        for(String s : typeArray){
            CheckBox cb = new CheckBox(this);
            cb.setText(s);

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

    /**
     * Checks if all Sub-Checkboxes of a Main-Type (like "Restaurant") are checked
     */
    private boolean allCheckboxesChecked(List<CheckBox> checkBoxes){
        for(CheckBox cb : checkBoxes){
            if(!cb.isChecked()){
                return false;
            }
        }
        return true;
    }

    /**
     * If the chosen time is now, it refreshes the time
     */
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

    /**
     * Saves all necessary options to SharedPreferences
     */
    @Override
    protected void onPause() {
        super.onPause();

        //save current options
        SharedPreferences spOptions = getApplicationContext().getSharedPreferences(getResources().getString(R.string.search_options), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = spOptions.edit();
        editor.clear();
        if(etRadius.getText() != null) {
            editor.putString("radius", etRadius.getText().toString());
        }
        if(cbRestaurant.isChecked()){
            editor.putBoolean("restaurant", cbRestaurant.isChecked());
        } else{
            for(int i = 0; i < checkBoxesRest.size(); i++){
                editor.putBoolean("restaurant" + i, checkBoxesRest.get(i).isChecked());
            }
        }
        if(cbBar.isChecked()){
            editor.putBoolean("bar", cbBar.isChecked());
        } else{
            for(int i = 0; i < checkBoxesBar.size(); i++){
                editor.putBoolean("bar" + i, checkBoxesBar.get(i).isChecked());
            }
        }
        if(cbCafe.isChecked()){
            editor.putBoolean("cafe", cbCafe.isChecked());
        } else{
            for(int i = 0; i < checkBoxesCafe.size(); i++){
                editor.putBoolean("cafe" + i, checkBoxesCafe.get(i).isChecked());
            }
        }
        if(cbTakeaway.isChecked()){
            editor.putBoolean("takeaway", cbTakeaway.isChecked());
        } else{
            for(int i = 0; i < checkBoxesTakeaway.size(); i++){
                editor.putBoolean("takeaway" + i, checkBoxesTakeaway.get(i).isChecked());
            }
        }
        editor.apply();
    }

    /**
     * If the ChangePositionActivity returns with a chosen location,
     * it saves them as Search-Options
     * @param data Contains the longitude and latitude of the ChangePositionActivity-Result
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CHANGE_POSITION_REQUEST) {
            if (resultCode == RESULT_OK) {
                chosenLongitude = data.getDoubleExtra(getResources().getString(R.string.newLongitude), 0);
                chosenLatitude = data.getDoubleExtra(getResources().getString(R.string.newLatitude), 0);

                chosenNewPosition = true;
            }
        }
    }

    /**
     * Creates a SearchOptions-Object based on the chosen Options,
     * this Object will be given to the MapActivity to Search for Places, based on those Search-Options
     * @return Created SearchOptions-Object
     */
    private SearchOptions createSearchOptions(){
        SearchOptions options = new SearchOptions();

        if(etName.getText() != null){
            options.setName(etName.getText().toString());
        }
        if(etRadius.getText() != null){
            int radius = Integer.parseInt(etRadius.getText().toString());
            if(radius > 50000){
                radius = 50000;
            }
            options.setRadius(radius);
        }
        if(chosenNewPosition && !rbCurrentPos.isChecked()){
            options.setLongitude(chosenLongitude);
            options.setLatitude(chosenLatitude);
        } else{
            options.setLongitude(longitude);
            options.setLatitude(latitude);
        }
        if(timeIsNow && dateIsNow){
            options.setTimeIsNow(true);
        }else{
            options.setTimeIsNow(false);
        }
        options.setTime(chosenTime);
        options.setDayOfWeek(dayOfWeek);

        List<String> restaurantStringList = new ArrayList<>();
        List<String> barStringList = new ArrayList<>();
        List<String> cafeStringList = new ArrayList<>();
        List<String> takeawayStringList = new ArrayList<>();

        if(cbRestaurant.isChecked()){
            restaurantStringList.add(cbRestaurant.getText().toString());
        } else{
            for(CheckBox cb : checkBoxesRest){
                if(cb.isChecked()){
                    restaurantStringList.add(cb.getText().toString());
                }
            }
        }
        if(cbBar.isChecked()){
            barStringList.add(cbBar.getText().toString());
        } else{
            for(CheckBox cb : checkBoxesBar){
                if(cb.isChecked()){
                    barStringList.add(cb.getText().toString());
                }
            }
        }
        if(cbCafe.isChecked()){
            cafeStringList.add(cbCafe.getText().toString());
        } else{
            for(CheckBox cb : checkBoxesCafe){
                if(cb.isChecked()){
                    cafeStringList.add(cb.getText().toString());
                }
            }
        }
        if(cbTakeaway.isChecked()){
            takeawayStringList.add(cbTakeaway.getText().toString());
        } else{
            for(CheckBox cb : checkBoxesTakeaway){
                if(cb.isChecked()){
                    takeawayStringList.add(cb.getText().toString());
                }
            }
        }

        //either no checkbox is enabled or no Search-Name is entered
        if(options.getName().isEmpty() && restaurantStringList.isEmpty() && barStringList.isEmpty() && cafeStringList.isEmpty() && takeawayStringList.isEmpty()){
            showAlertDialog("You need to either check at least one Checkbox or enter a Search-Name!");
            return null;
        }

        options.setTypesRestaurant(restaurantStringList);
        options.setTypesBar(barStringList);
        options.setTypesCafe(cafeStringList);
        options.setTypesTakeaway(takeawayStringList);

        return options;
    }

    /**
     * Starts the MapActivity and gives it a SearchOptions-Object
     */
    private void startMapActivity() {
        SearchOptions options = createSearchOptions();

        if(options != null) {
            Intent intent = new Intent(SearchOptionsActivity.this, MapActivity.class);
            intent.putExtra(getResources().getString(R.string.map_activity_type), MapActivityType.SearchResults);
            intent.putExtra(getResources().getString(R.string.search_options), options);
            startActivity(intent);
        }
    }

    /**
     * Everything below gets current Position once
     * buildApiClient() is the first method called
     * onLocationChanged() is the last method called
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
    }

    /**
     * After getting the user's current location once,
     * a new Activity gets started, based on the operation-enum,
     * either ChangePositionActivity, or MapActivity
     * @param location The current user's location
     */
    @Override
    public void onLocationChanged(Location location) {
        longitude = location.getLongitude();
        latitude = location.getLatitude();

        if(operation == Operation.ChangePosition){
            Intent intent = new Intent(SearchOptionsActivity.this, ChangePositionActivity.class);
            intent.putExtra(getResources().getString(R.string.longitude), longitude);
            intent.putExtra(getResources().getString(R.string.latitude), latitude);
            if(etRadius.getText() != null && !etRadius.getText().toString().isEmpty()){
                int radius = Integer.parseInt(etRadius.getText().toString());

                if(radius > 50000){
                    radius = 50000;
                }
                intent.putExtra("radius", radius);
            }

            startActivityForResult(intent, CHANGE_POSITION_REQUEST);
        }
        else if(operation == Operation.Search) {
            startMapActivity();
        }
    }

    /**
     * If user hasn't granted permission for location access, he will be asked to
     * If permission is already granted, getLocation() is called
     */
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

    /**
     * If user has granted permission for location-service getLocation() gets called
     */
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

    /**
     * Back-Button is disabled
     */
    @Override
    public void onBackPressed() {
        //Do nothing (don't go back to Login)
    }
}
