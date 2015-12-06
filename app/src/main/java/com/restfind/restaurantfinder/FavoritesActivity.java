package com.restfind.restaurantfinder;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.List;

public class FavoritesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        //Get current logged-in username
        SharedPreferences spLoginCurrent = getApplicationContext().getSharedPreferences(getResources().getString(R.string.login_current), Context.MODE_PRIVATE);
        String username = spLoginCurrent.getString(getResources().getString(R.string.login_current), null);

        //Start task
        GetFavoritesTask task = new GetFavoritesTask();
        task.execute(username);
    }

    //<Input for doInBackground, (Progress), Input for onPostExecute>
    private class GetFavoritesTask extends AsyncTask<String, Integer, List<String>> {

        @Override
        protected List<String> doInBackground(String... params) {
            return null;
        }

        @Override
        protected void onPostExecute(List<String> result) {

        }
    }
}
