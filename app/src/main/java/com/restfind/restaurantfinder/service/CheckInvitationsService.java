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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Service that wakes up every 5 minutes to check for new Restaurant-Invitations
 * Creates a Notification and updates own current Location if necessary
 */
public class CheckInvitationsService extends IntentService implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    protected static final int NOTIFICATION_ID = 1;
    protected static final String ACTION_START = "ACTION_START";
    protected static final String ACTION_DELETE = "ACTION_DELETE";
    protected String username;

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

    /**
     * gets called when this service gets created or woken up every 5 minutes
     * and create a notification for new invitations.
     * If at least 1 invitation is in 15 minutes or less than 10 minutes ago
     * and the user has accepted the invitation and location tracking
     * -> it will call the method buildApiClient(),
     * to update the current user-location in the Database
     */
    protected void processStartNotification() {
        if(username == null || username.isEmpty()){
            //Get current logged-in username
            SharedPreferences spLoginCurrent = getApplicationContext().getSharedPreferences(getResources().getString(R.string.login_current), Context.MODE_PRIVATE);
            username = spLoginCurrent.getString(getResources().getString(R.string.login_current), null);
        }
        if(username != null && !username.isEmpty()){
            //Check if user has allowed location-tracking
            SharedPreferences spTracking = getApplicationContext().getSharedPreferences("tracking", Context.MODE_PRIVATE);
            boolean trackingAllowed = spTracking.getBoolean("tracking", false);

            //Gets all Invitations the user is part of (as host or invitee)
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
                    //if this invitation is new and the user is not the host -> show invitation
                    if(!i.getHost().equals(username) && !i.isReceived()){
                        //set Invitation as received in Database
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

                        //create Notification which sends you to the Invitation-Activity
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

                    //Invitation-Deadline is between 15 minutes before and 10 minutes after deadline
                    //-> update own current-location
                    if(!startedLocation && timeTillDeadline > (-600000) && timeTillDeadline <= 900000 && trackingAllowed && (i.getHost().equals(username) || i.getInvitees().get(username) == 1)){
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

    /**
     * builds Google Api-LocationRequest which gets the current user-location every 10 seconds for 5 minutes
     * and calls method onConnected(), if successful
     */
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

    /**
     * called after buildApiClient(), calls getLocation()
     */
    @Override
    public void onConnected(Bundle bundle) {
        getLocation();
    }

    /**
     * called after onConnected(), calls onLocationChanged()
     */
    private void getLocation(){
        fusedLocationProviderApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    /**
     * gets called every 10 seconds with the current new user-location
     * and saves it to the Database
     * @param location New User-Location
     */
    @Override
    public void onLocationChanged(Location location) {
        final double longitude = location.getLongitude();
        final double latitude = location.getLatitude();

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

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }
}
