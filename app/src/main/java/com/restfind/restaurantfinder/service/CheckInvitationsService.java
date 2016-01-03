package com.restfind.restaurantfinder.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.restfind.restaurantfinder.R;

public class CheckInvitationsService extends IntentService {

    private static final int NOTIFICATION_ID = 1;
    private static final String ACTION_START = "ACTION_START";
    private static final String ACTION_DELETE = "ACTION_DELETE";
    private String username;

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
    private void processStartNotification() {
        //TODO: check invitations, create notification if needed

//        SharedPreferences spService = getApplicationContext().getSharedPreferences("service", Context.MODE_PRIVATE);
//        boolean stopped = spService.getBoolean("serviceStopped", false);
//
//        if(stopped){
//            try {
//                finalize();
//            } catch (Throwable throwable) {
//                throwable.printStackTrace();
//            }
//        }

        if(username == null || username == ""){
            //Get current logged-in username
            SharedPreferences spLoginCurrent = getApplicationContext().getSharedPreferences(getResources().getString(R.string.login_current), Context.MODE_PRIVATE);
            username = spLoginCurrent.getString(getResources().getString(R.string.login_current), null);
        }
        if(username != null && username != ""){
//            final NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
//            builder.setContentTitle("Invitation")
//                    .setAutoCancel(true)
//                    .setContentText("You have received an Invitation from " + username)
//                    .setSmallIcon(R.mipmap.ic_launcher);
//
//            PendingIntent pendingIntent = PendingIntent.getActivity(this,
//                    NOTIFICATION_ID,
//                    new Intent(this, InvitationsActivity.class),
//                    PendingIntent.FLAG_UPDATE_CURRENT);
//            builder.setContentIntent(pendingIntent);
//            builder.setDeleteIntent(NotificationEventReceiver.getDeleteIntent(this));
//
//            final NotificationManager manager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
//            manager.notify(NOTIFICATION_ID, builder.build());
        }
    }
}
