package com.restfind.restaurantfinder;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class CheckInvitationsService extends Service {
    /*
    TODO: regularly check Database for Friendinvites and Restaurant-Invitations
    if there is something, send it to currently running Activity (maybe an AlertDialog)
    There you can accept or decline it

    Probably best with starting Service, not binding

    MAYBE: if App is not running send Notification
    */

    public CheckInvitationsService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
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
