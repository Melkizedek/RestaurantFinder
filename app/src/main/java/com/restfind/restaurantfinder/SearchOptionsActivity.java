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
                if(etRadius.getText() == null || etRadius.getText().toString().isEmpty()) {
                    showAlertDialog("You need to enter a Radius!");
                }else {
                    if (chosenNewPosition && !rbCurrentPos.isChecked()) {
                        startSearchResultsActivity();
                    } else {
                        //Get current Position and start SearchResultsActivity
                        operation = Operation.Search;
                        buildApiClient();
                    }
                }
            }
        });

        //Get saved SharedPreferences
        SharedPreferences spOptions = getApplicationContext().getSharedPreferences(getResources().getString(R.string.search_options), Context.MODE_PRIVATE);
        if(spOptions.contains("radius")){
            etRadius.setText(spOptions.getString("radius", ""));
        }
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
        editor.commit();
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

    private SearchOptions createSearchOptions(){
        SearchOptions options = new SearchOptions();

        if(etName.getText() != null){
            options.setName(etName.getText().toString());
        }
        if(etRadius.getText() != null){
            options.setRadius(Integer.parseInt(etRadius.getText().toString()));
        }
        if(chosenNewPosition){
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

    private void startSearchResultsActivity() {
        SearchOptions options = createSearchOptions();

        if(options != null) {
            Intent intent = new Intent(SearchOptionsActivity.this, SearchResultsActivity.class);
            intent.putExtra(getResources().getString(R.string.search_options), options);
            startActivity(intent);
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
        longitude = location.getLongitude();
        latitude = location.getLatitude();

        Log.v(LOG_TAG, "onLocationChanged");

        if(operation == Operation.ChangePosition){
            Intent intent = new Intent(SearchOptionsActivity.this, ChangePositionActivity.class);
            intent.putExtra(getResources().getString(R.string.longitude), longitude);
            intent.putExtra(getResources().getString(R.string.latitude), latitude);

            startActivityForResult(intent, CHANGE_POSITION_REQUEST);
        }
        else if(operation == Operation.Search) {
            startSearchResultsActivity();
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
