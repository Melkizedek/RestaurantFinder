package com.restfind.restaurantfinder;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class CheckInvitationsService extends Service {
    public CheckInvitationsService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
