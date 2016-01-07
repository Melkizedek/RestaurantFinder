package com.restfind.restaurantfinder.service;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.restfind.restaurantfinder.InvitationsActivity;
import com.restfind.restaurantfinder.R;
import com.restfind.restaurantfinder.assistant.Invitation;
import com.restfind.restaurantfinder.database.Database;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class CheckInvitationsService extends IntentService implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    protected static final int NOTIFICATION_ID = 1;
    protected static final String ACTION_START = "ACTION_START";
    protected static final String ACTION_DELETE = "ACTION_DELETE";
    protected String username;

    protected static final int MY_PERMISSIONS_ACCESS_FINE_LOCATION = 1;
    private FusedLocationProviderApi fusedLocationProviderApi;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;

    public CheckInvitationsService() {
        super(CheckInvitationsService.class.getSimpleName());
    }

    public static Intent createIntentStartNotificationService(Context context) {
        Intent intent = new Intent(context, CheckInvitationsService.class);
        intent.setAction(ACTION_START);
        return intent;
    }

    public static Intent createIntentDeleteNotification(Context context) {
        Intent intent = new Intent(context, CheckInvitationsService.class);
        intent.setAction(ACTION_DELETE);
        return intent;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(getClass().getSimpleName(), "onHandleIntent, started handling a notification event");
        try {
            String action = intent.getAction();
            if (ACTION_START.equals(action)) {
                processStartNotification();
            }
            if (ACTION_DELETE.equals(action)) {
                processDeleteNotification(intent);
            }
        } finally {
            WakefulBroadcastReceiver.completeWakefulIntent(intent);
        }
    }

    private void processDeleteNotification(Intent intent) {

    }

    //gets called every few minutes to check for new invitations and create a notification
    protected void processStartNotification() {
        //TODO: get invitations, create notification if needed



        if(username == null || username.isEmpty()){
            //Get current logged-in username
            SharedPreferences spLoginCurrent = getApplicationContext().getSharedPreferences(getResources().getString(R.string.login_current), Context.MODE_PRIVATE);
            username = spLoginCurrent.getString(getResources().getString(R.string.login_current), null);
        }
        if(username != null && !username.isEmpty()){
            //Check if tracking is allowed
            SharedPreferences spTracking = getApplicationContext().getSharedPreferences("tracking", Context.MODE_PRIVATE);
            boolean trackingAllowed = spTracking.getBoolean("tracking", false);

            //Thread that tries to add friend
            ExecutorService es = Executors.newSingleThreadExecutor();
            Future<List<Invitation>> result = es.submit(new Callable<List<Invitation>>() {
                public List<Invitation> call() throws IOException {
                    return Database.getInvitations(username);
                }
            });
            try {
                List<Invitation> invitations = result.get();
                boolean startedLocation = false;

                for(final Invitation i : invitations){
                    //show Notification
                    if(!i.getHost().equals(username) && !i.isReceived()){
                        Log.v("Service_Log", "Notification: " + i.getId());
                        Thread t = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Database.receivedInvitation(i.getId(), username);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        t.start();

                        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
                        builder.setContentTitle(getResources().getString(R.string.app_name))
                                .setAutoCancel(true)
                                .setContentText(i.getHost() + " has sent you an Invitation")
                                .setSmallIcon(R.mipmap.ic_launcher);

                        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                                NOTIFICATION_ID,
                                new Intent(this, InvitationsActivity.class),
                                PendingIntent.FLAG_UPDATE_CURRENT);
                        builder.setContentIntent(pendingIntent);
                        builder.setDeleteIntent(NotificationEventReceiver.getDeleteIntent(this));

                        final NotificationManager manager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
                        manager.notify(NOTIFICATION_ID, builder.build());
                    }

                    double timeTillDeadline = i.getTime() - Calendar.getInstance().getTimeInMillis();

                    //between 15 minutes before and 10 minutes after deadline
                    if(!startedLocation && timeTillDeadline > (-600000) && timeTillDeadline <= 900000 && trackingAllowed && i.getInvitees().get(username) == 1){
                        Log.v("Service_Log", "Deadline: " + i.getId());
                        startedLocation = true;
                        buildApiClient();
                    }
                }
            }
            catch (Exception e) {
                //Could not connect to Server with .php-files
                e.printStackTrace();
            } finally {
                es.shutdown();
            }
        }
    }

    private void buildApiClient(){
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000); //every 10 seconds
        locationRequest.setNumUpdates(30); //for 5 minutes
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

//    double longitude = -1;
//    double latitude = -1;

    //starts a new Activity based on which operation is chosen
    @Override
    public void onLocationChanged(Location location) {
        final double longitude = location.getLongitude();
        final double latitude = location.getLatitude();

//        if(latitude < 0){
//            latitude = location.getLatitude();
//            longitude = location.getLongitude();
//        } else{
//            latitude += 0.0005;
//            longitude += 0.005;
//        }

        Log.v("Service_Log", "latitude: " + latitude + ", " + "longitude: " + longitude);

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Database.updateUserLocation(username, String.valueOf(latitude), String.valueOf(longitude));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();
    }

    //called by googleApiClient.connect()
    @Override
    public void onConnected(Bundle bundle) {
            getLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }
}
