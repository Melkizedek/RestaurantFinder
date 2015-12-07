package com.restfind.restaurantfinder;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;

public class FriendsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Get current logged-in username
        SharedPreferences spLoginCurrent = getApplicationContext().getSharedPreferences(getResources().getString(R.string.login_current), Context.MODE_PRIVATE);
        String username = spLoginCurrent.getString(getResources().getString(R.string.login_current), null);

        /*
        TODO: ListView for Elements, Delete and Add Button in AppBar
         */

        //Start task
        GetFriendsTask task = new GetFriendsTask();
        task.execute(username);
    }

    //<Input for doInBackground, (Progress), Input for onPostExecute>
    private class GetFriendsTask extends AsyncTask<String, Integer, List<String>> {

        /*
        TODO: Gets Friends of user from Database and displays them in ListView
         */

        @Override
        protected List<String> doInBackground(String... params) {
            List<String> friends = new ArrayList<>();

//            friends = Database.getFriends(params[0]);

            return friends;


        }

        @Override
        protected void onPostExecute(List<String> result) {

        }
    }
}
