package com.restfind.restaurantfinder;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.widget.ListView;

import com.restfind.restaurantfinder.assistant.Invitation;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

public class InvitationsActivity extends AppBarActivity {

    private ListView lvInvitationList;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invitations);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Invitations");
        setSupportActionBar(toolbar);

        //Get current logged-in username
        username = getCurrentUsername();

//        Intent intent = getIntent();
//        Invitation inv = intent.getParcelableExtra("invitation");
//        Log.v(LOG_TAG, "id: " + inv.getId());
//        Log.v(LOG_TAG, "inviter: " + inv.getInviter());
//        Log.v(LOG_TAG, "placeID: " + inv.getPlaceID());
//        Timestamp time = new Timestamp(inv.getTime());
//        Log.v(LOG_TAG, "time: " + time.toString());
//        for(Map.Entry<String, Boolean> e : inv.getInvitees().entrySet()){
//            Log.v(LOG_TAG, "invitee: " + e.getKey() + ", accepted: " + e.getValue());
//        }

        //Set up UI-Elements
        lvInvitationList = (ListView) findViewById(R.id.lvInvitationList);

        //Start task
        GetInvitationsTask task = new GetInvitationsTask();
        task.execute(username);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_logout_only, menu);
        return true;
    }

    //<Input for doInBackground, (Progress), Input for onPostExecute>
    private class GetInvitationsTask extends AsyncTask<String, Integer, List<String>> {

        /*
        TODO: Gets Invitations of user from Database and displays them in ListView?
         */

        @Override
        protected List<String> doInBackground(String... params) {
            return null;
        }

        @Override
        protected void onPostExecute(List<String> result) {

        }
    }
}
