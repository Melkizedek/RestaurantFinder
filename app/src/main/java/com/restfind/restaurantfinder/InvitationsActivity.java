package com.restfind.restaurantfinder;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.widget.ListView;

import java.util.List;

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
        SharedPreferences spLoginCurrent = getApplicationContext().getSharedPreferences(getResources().getString(R.string.login_current), Context.MODE_PRIVATE);
        username = spLoginCurrent.getString(getResources().getString(R.string.login_current), null);

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
