package com.restfind.restaurantfinder;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class CheckInvitationsService extends Service {

    private String username;

    public CheckInvitationsService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }

    Handler handler = new Handler();

    Runnable runnable = new Runnable(){

        @Override
        public void run() {
            checkInvitations();
            handler.postDelayed(this, 1000); // 1000 - Milliseconds
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null) {
            Log.v("CheckInvitationsService", "111");

            username = intent.getStringExtra("username");

            Log.v("CheckInvitationsService", "username: " + username);

            handler.postDelayed(runnable, 1000);
        } else{
            Log.v("CheckInvitationsService", "intent == null");
        }

        return Service.START_STICKY;
//        return super.onStartCommand(intent, flags, startId);
    }

    private void checkInvitations(){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; true; i++) {
                    Log.v("CheckInvitationsService", "i: " + i);

                    createNotification();

                    try {
                        Thread.sleep(8000, 0);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();
    }

    private void createNotification(){
        Notification noti = new Notification.Builder(this)
                .setContentTitle("Notification Title")
                .setContentText("Click here to read").setSmallIcon(R.mipmap.ic_launcher)
                .build();
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // hide the notification after its selected
        noti.flags |= Notification.FLAG_AUTO_CANCEL;

        notificationManager.notify(0, noti);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
