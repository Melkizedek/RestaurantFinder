package com.restfind.restaurantfinder;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class PlaceDetailsActivity extends AppBarActivity {

    private Intent intent;
    private ImageButton btnFavorite;
    private boolean isFavorite;
    private String username;
    private String placeID;
    private boolean comingFromFavoriteActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_details);

        isFavorite = false;
        intent = getIntent();
        Place place = intent.getParcelableExtra("place");
        comingFromFavoriteActivity = intent.getBooleanExtra("favorite", false);
        placeID = place.getPlace_ID();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Place Details");
        setSupportActionBar(toolbar);

        //Get current logged-in username
        SharedPreferences spLoginCurrent = getApplicationContext().getSharedPreferences(getResources().getString(R.string.login_current), Context.MODE_PRIVATE);
        username = spLoginCurrent.getString(getResources().getString(R.string.login_current), null);

        btnFavorite = (ImageButton) findViewById(R.id.btnFavorite);

        //TODO: list all needed fields of the given Place-Object

        TextView tv = (TextView) findViewById(R.id.textView);
        tv.setText(place.getName());

        btnFavorite.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (isFavorite) {
                    //Thread that tries to delete this Favorite
                    ExecutorService es = Executors.newSingleThreadExecutor();
                    Future<Boolean> result = es.submit(new Callable<Boolean>() {
                        public Boolean call() throws IOException {
                            return Database.deleteFavorite(username, placeID);
                        }
                    });
                    try {
                        result.get();
                        setNotFavorite();
                    } catch (Exception e) {
                        //Could not connect to Server with .php-files
                        showAlertDialog(getResources().getString(R.string.connection_error));
                    } finally {
                        es.shutdown();
                    }
                } else {
                    //Thread that tries to favorite this place
                    ExecutorService es = Executors.newSingleThreadExecutor();
                    Future<Boolean> result = es.submit(new Callable<Boolean>() {
                        public Boolean call() throws IOException {
                            return Database.favorite(username, placeID);
                        }
                    });
                    try {
                        result.get();
                        setFavorite();
                    } catch (Exception e) {
                        //Could not connect to Server with .php-files
                        showAlertDialog(getResources().getString(R.string.connection_error));
                    } finally {
                        es.shutdown();
                    }
                }
            }
        });

        CheckFavoriteTask task = new CheckFavoriteTask();
        task.execute(place.getPlace_ID());
    }

    private void setFavorite(){
        btnFavorite.setImageResource(R.drawable.ic_favorite_black_48dp);
        isFavorite = true;
    }

    private void setNotFavorite(){
        //TODO: Change Image to empty heart
        btnFavorite.setImageResource(R.drawable.ic_star_border_black_48dp);
        isFavorite = false;
    }

    //<Input for doInBackground, (Progress), Input for onPostExecute>
    private class CheckFavoriteTask extends AsyncTask<String, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            String placeID = params[0];
            List<String> placeIDs = new ArrayList<>();

            //Thread that tries to get favorites
            ExecutorService es = Executors.newSingleThreadExecutor();
            Future<List<String>> result = es.submit(new Callable<List<String>>() {
                public List<String> call() throws IOException {
                    return Database.getFavorites(username);
                }
            });

            try {
                placeIDs = result.get();
            } catch (Exception e) {
                //Could not connect to Server with .php-files
                showAlertDialog(getResources().getString(R.string.connection_error));
                return null;
            } finally {
                es.shutdown();
            }
            if(placeIDs.contains(placeID)){
                return true;
            }
            return false;
        }

        //puts the friend-requests and friends into the listView
        @Override
        protected void onPostExecute(Boolean result) {
            if(result){
                setFavorite();
            }
        }
    }

    @Override
    public void onBackPressed() {
        intent.putExtra("deleted", !isFavorite);
        setResult(RESULT_OK, intent);

        super.onBackPressed();
    }
}
