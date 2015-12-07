package com.restfind.restaurantfinder;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import java.util.List;

public class InvitationsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invitations);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Get current logged-in username
        SharedPreferences spLoginCurrent = getApplicationContext().getSharedPreferences(getResources().getString(R.string.login_current), Context.MODE_PRIVATE);
        String username = spLoginCurrent.getString(getResources().getString(R.string.login_current), null);

        /*
        TODO: ListView for Elements, Checkbox: Accepted or not yet accepted
         */

        //Start task
        GetInvitationsTask task = new GetInvitationsTask();
        task.execute(username);
    }

    //<Input for doInBackground, (Progress), Input for onPostExecute>
    private class GetInvitationsTask extends AsyncTask<String, Integer, List<String>> {

        /*
        TODO: Gets Invitations of user from Database and displays them in ListView
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
