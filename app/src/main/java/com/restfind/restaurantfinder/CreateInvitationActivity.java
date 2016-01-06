package com.restfind.restaurantfinder;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.restfind.restaurantfinder.database.Database;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class CreateInvitationActivity extends AppBarActivity {

    private String username;
    private String placeID;
    private List<CheckBox> checkBoxesFriends;
    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_invitation);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Send Invitation");
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        username = intent.getStringExtra("username");
        placeID = intent.getStringExtra("placeID");

        calendar = Calendar.getInstance();

        final TextView tvTime = (TextView) findViewById(R.id.tvTime);
        final TextView tvDate = (TextView) findViewById(R.id.tvDate);
        final Button btnInviteFriends = (Button) findViewById(R.id.btnInviteFriends);

        /*
        TextView: Date
         */
        //Change Date
        tvDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar c = Calendar.getInstance();
                final int year = c.get(Calendar.YEAR);
                final int month = c.get(Calendar.MONTH);
                final int day = c.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(CreateInvitationActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int selectedYear, int selectedMonth, int selectedDay) {
                        calendar.set(Calendar.DATE, selectedDay);
                        calendar.set(Calendar.MONTH, selectedMonth);
                        calendar.set(Calendar.YEAR, selectedYear);

                        Log.v(LOG_TAG, "Date: " + new Timestamp(calendar.getTimeInMillis()));

                        long dateInMs = calendar.getTimeInMillis();
                        String formatted_date = (DateFormat.format("dd.MM.yyyy", dateInMs))
                                .toString();
                        tvDate.setText(formatted_date);

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
                int hour = c.get(Calendar.HOUR_OF_DAY);

                TimePickerDialog dialog;
                dialog = new TimePickerDialog(CreateInvitationActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        calendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                        calendar.set(Calendar.MINUTE, selectedMinute);

                        tvTime.setText(DateFormat.format("HH:mm", calendar.getTimeInMillis()));
                    }
                }, hour, min, true);

                dialog.setTitle("Select Time");
                dialog.show();
            }
        });
        //Button Invite Friends
        btnInviteFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(tvDate.getText().equals("Choose Date") || tvTime.getText().equals("Choose Time")){
                    showAlertDialog("Choose a Time and Date!");
                } else{
                    List<String> friends = new ArrayList<>();
                    for(CheckBox cb : checkBoxesFriends){
                        if(cb.isChecked()) {
                            friends.add(cb.getText().toString());
                        }
                    }
                    if(friends.isEmpty()){
                        showAlertDialog("Choose at least one Friend!");
                    } else {
                        findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);

                        new InviteTask().execute(friends);
                    }
                }
            }
        });
        findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
        new GetFriendsTask().execute();
    }

    //<Input for doInBackground, (Progress), Input for onPostExecute>
    private class GetFriendsTask extends AsyncTask<Void, Integer, List<String>> {

        @Override
        protected List<String> doInBackground(Void... params) {
            List<String> friends;
            try {
                friends = Database.getFriends(username);
            } catch (Exception e) {
                //Could not connect to Server with .php-files
                return null;
            }
            return friends;
        }

        @Override
        protected void onPostExecute(List<String> friends) {
            findViewById(R.id.loadingPanel).setVisibility(View.GONE);
            if(friends != null){
                checkBoxesFriends = new ArrayList<>();

                for(String s : friends){
                    CheckBox cb = new CheckBox(CreateInvitationActivity.this);
                    cb.setText(s);
                    ((LinearLayout)findViewById(R.id.llCheckboxesFriends)).addView(cb);
                    checkBoxesFriends.add(cb);
                }
            } else{
                showAlertDialog(getResources().getString(R.string.connection_error));
            }
        }
    }

    //<Input for doInBackground, (Progress), Input for onPostExecute>
    private class InviteTask extends AsyncTask<List<String>, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(List<String>... params) {
            List<String> friends = params[0];
            Timestamp timestamp = new Timestamp(calendar.getTimeInMillis());
            try {
                return Database.createInvitation(username, placeID, timestamp, friends);
            } catch (Exception e) {
                //Could not connect to Server with .php-files
                return null;
            }
        }

        //puts the friend-requests and friends into the listView
        @Override
        protected void onPostExecute(Boolean result) {
            findViewById(R.id.loadingPanel).setVisibility(View.GONE);
            if(result != null){
                final CheckBox cbAllowTracking = (CheckBox) findViewById(R.id.cbAllowTracking);
                SharedPreferences spTracking = getApplicationContext().getSharedPreferences("tracking", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = spTracking.edit();
                editor.putBoolean("tracking", cbAllowTracking.isChecked());
                editor.apply();

                if (!cbAllowTracking.isChecked()) {
                    findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
                    //Thread that tries to delete user_location in the database
                    ExecutorService es = Executors.newSingleThreadExecutor();
                    Future<Boolean> resultFuture = es.submit(new Callable<Boolean>() {
                        public Boolean call() throws IOException {
                            return Database.deleteUserLocation(username);
                        }
                    });
                    try {
                        resultFuture.get();
                    } catch (Exception e) {
                        //Could not connect to Server with .php-files
                        showAlertDialog(getResources().getString(R.string.connection_error));
                        return;
                    } finally {
                        findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                        es.shutdown();
                    }
                }

                Toast.makeText(CreateInvitationActivity.this, "Invitation sent!", Toast.LENGTH_SHORT).show();
            } else{
                showAlertDialog(getResources().getString(R.string.connection_error));
            }
        }
    }
}
