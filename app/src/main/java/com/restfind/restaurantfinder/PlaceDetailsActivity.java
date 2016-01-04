package com.restfind.restaurantfinder;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.restfind.restaurantfinder.assistant.Place;

import java.io.IOException;
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
        placeID = intent.getStringExtra("placeID");
        comingFromFavoriteActivity = intent.getBooleanExtra("favorite", false);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Place Details");
        setSupportActionBar(toolbar);

        //Get current logged-in username
        username = getCurrentUsername();

        btnFavorite = (ImageButton) findViewById(R.id.btnFavorite);
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

        Button btnInviteFriends = (Button) findViewById(R.id.btnInviteFriends);
        btnInviteFriends.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(PlaceDetailsActivity.this, CreateInvitationActivity.class);
                intent.putExtra("username", username);
                intent.putExtra("placeID", placeID);
                startActivity(intent);
            }
        });

        new GetPlacesDetailsTask().execute();
        new CheckFavoriteTask().execute();
    }

    private void setFavorite(){
        btnFavorite.setImageResource(R.drawable.ic_favorite_black_48dp);
        isFavorite = true;
    }

    private void setNotFavorite(){
        btnFavorite.setImageResource(R.drawable.ic_favorite_border_black_48dp);
        isFavorite = false;
    }

    //<Input for doInBackground, (Progress), Input for onPostExecute>
    private class GetPlacesDetailsTask extends AsyncTask<Void, Integer, Place> {

        @Override
        protected Place doInBackground(Void... params) {
            return getPlaceDetails(placeID);
        }

        //puts the friend-requests and friends into the listView
        @Override
        protected void onPostExecute(Place place) {
            //TODO: list all needed fields of the given Place-Object
            TextView tv = (TextView) findViewById(R.id.textView);
            if(place.getIcon().equals(getResources().getString(R.string.iconRestaurant))){
                tv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_local_dining_black_24dp, 0, 0, 0);

            }
            if(place.getIcon().equals(getResources().getString(R.string.iconBar))){
                tv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_local_bar_black_18dp, 0, 0, 0);

            }
            if(place.getIcon().equals(getResources().getString(R.string.iconCafe))){
                tv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_local_cafe_black_18dp, 0, 0, 0);

            }
            tv.setText(place.getName());
            TextView tv_textView_Vicinity = (TextView) findViewById(R.id.textView_Vicinity);
            tv_textView_Vicinity.setText(place.getVicinity());
            TextView textView_Website = (TextView) findViewById(R.id.textView_Website);
            textView_Website.setText(place.getWebsite());
            TextView textView_UserRating = (TextView) findViewById(R.id.textView_UserRating);
            textView_UserRating.setText(place.getUser_ratings_total());
            TextView textView_phone_number = (TextView) findViewById(R.id.phone_number);
            textView_phone_number.setText(place.getFormatted_phone_number());
            TextView openNow = (TextView) findViewById(R.id.openNow);
            if(place.isOpenNow()){
                openNow.setText("geöffnet");
            }
            if(!place.isOpenNow()){
                openNow.setText("geschlossen");
            }
            TextView openTimes = (TextView) findViewById(R.id.openTimes);
            for(int i = 0; i < place.getOpeningHours().size();i++){
                openTimes.setText(place.getOpeningHours().get(i));
            }
            TextView priceLevel = (TextView) findViewById(R.id.priceLevel);
            priceLevel.setText(place.getPrice_level());

        }
    }

    //<Input for doInBackground, (Progress), Input for onPostExecute>
    private class CheckFavoriteTask extends AsyncTask<Void, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            List<String> placeIDs;
            try {
                placeIDs = Database.getFavorites(username);
            } catch (Exception e) {
                //Could not connect to Server with .php-files
                return null;
            }
            if(placeIDs != null && placeIDs.contains(placeID)){
                return true;
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            findViewById(R.id.loadingPanel).setVisibility(View.GONE);
            findViewById(R.id.btnFavorite).setVisibility(View.VISIBLE);
            if(result == null){
                showAlertDialog(getResources().getString(R.string.connection_error));
            }
            else if(result){
                setFavorite();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if(comingFromFavoriteActivity) {
            intent.putExtra("deleted", !isFavorite);
            setResult(RESULT_OK, intent);
        }
        super.onBackPressed();
    }
}
