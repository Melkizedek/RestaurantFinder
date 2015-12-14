package com.restfind.restaurantfinder;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class CheckInvitationsService extends Service {
    /*
    TODO: gestartet von SearchOptionsActivity sofort nach Login
    (vielleicht kann sich nur AppBarActivity dran binden,
    damit alle Activities die davon ableiten auch gebindet sind)

    holt sich in bestimmten Abständen Friend-Invites und Restaurant-Invitations von Database,
    wenn neue vorhanden (maybe durch Timestamps überprüfen) irgendwie in gerade laufender App anzeigen
    (kleine Zahl auf z.B. Friends-Button in AppBar, oder ein Toast, oder AlertDialog, k.a.)
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
