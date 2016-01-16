package com.restfind.restaurantfinder;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;

import com.restfind.restaurantfinder.assistant.Place;
import com.restfind.restaurantfinder.database.Database;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Shows all available Details of a Place
 * Enables the user to favorite and unfavorite this Place
 * Enables the user to start the CreateInvitationActivity to invite friends to this Place
 */
public class PlaceDetailsActivity extends AppBarActivity {

    private Intent intent;
    private ImageButton btnFavorite;
    private boolean isFavorite;
    private String username;
    private String placeID;
    private boolean comingFromFavoriteActivity;

    /**
     * When tapping on the heart in the bottom left:
     * if this place is not a favorite, it saves this place as a favorite in the Database and changes the empty heart to a filled heart
     * if this place is already a favorte, it deletes this place from the Database and changes the full heart to an empty heart
     */
    private View.OnClickListener onClickListenerFavorite = new View.OnClickListener() {
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
    };

    /**
     * When tapping the "Invite Friends"-Button the CreateInvitationActivity is started
     */
    private View.OnClickListener onClickListenerInvite = new View.OnClickListener() {
        public void onClick(View v) {
            Intent intent = new Intent(PlaceDetailsActivity.this, CreateInvitationActivity.class);
            intent.putExtra("username", username);
            intent.putExtra("placeID", placeID);
            startActivity(intent);
        }
    };

    /**
     * GetPlacesDetailsTask and CheckFavoriteTask are executed
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_details);

        isFavorite = false;
        intent = getIntent();
        placeID = intent.getStringExtra("placeID");
        Place place = intent.getParcelableExtra("place");
        comingFromFavoriteActivity = intent.getBooleanExtra("favorite", false);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Place Details");
        setSupportActionBar(toolbar);

        //Get current logged-in username
        username = getCurrentUsername();

        btnFavorite = (ImageButton) findViewById(R.id.btnFavorite);
        btnFavorite.setOnClickListener(onClickListenerFavorite);

        Button btnInviteFriends = (Button) findViewById(R.id.btnInviteFriends);
        btnInviteFriends.setOnClickListener(onClickListenerInvite);

        new GetPlacesDetailsTask().execute(place);
        new CheckFavoriteTask().execute();
    }

    /**
     * Changes the Favorite-ImageButton to a full heart
     */
    private void setFavorite(){
        btnFavorite.setImageResource(R.drawable.ic_favorite_black_48dp);
        isFavorite = true;
    }

    /**
     * Changes the Favorite-ImageButton to an empty heart
     */
    private void setNotFavorite(){
        btnFavorite.setImageResource(R.drawable.ic_favorite_border_black_48dp);
        isFavorite = false;
    }

    /**
     * Performs a Place-Details Request to the Google Places Api
     * and displays all available details with icons to the left
     */
    private class GetPlacesDetailsTask extends AsyncTask<Place, Integer, Place> {

        @Override
        protected Place doInBackground(Place... params) {
            if(params[0] != null){
                return params[0];
            }
            return getPlaceDetails(placeID);
        }

        @Override
        protected void onPostExecute(Place place) {
            TextView tvName = (TextView) findViewById(R.id.tvName);
            if(place.getIcon().equals(getResources().getString(R.string.iconRestaurant))){
                tvName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_local_dining_black_24dp, 0, 0, 0);

            }
            if(place.getIcon().equals(getResources().getString(R.string.iconBar))){
                tvName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_local_bar_black_24dp, 0, 0, 0);

            }
            if(place.getIcon().equals(getResources().getString(R.string.iconCafe))){
                tvName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_local_cafe_black_24dp, 0, 0, 0);

            }
            tvName.setText(place.getName());

            List<String> types = new ArrayList<>();
            for(int i = 0; i < place.getTypes().size(); i++) {
                if(place.getTypes().get(i).equals("restaurant")
                        || place.getTypes().get(i).equals("bar")
                        || place.getTypes().get(i).equals("cafe")
                        || place.getTypes().get(i).equals("meal_takeaway")) {
                    types.add(place.getTypes().get(i));
                }
            }
            TextView tvTypes = (TextView) findViewById(R.id.tvTypes);
            tvTypes.setText(types.toString());

            TextView tvVicinity = (TextView) findViewById(R.id.tvVicinity);
            tvVicinity.setText(place.getVicinity());

            if(place.getFormatted_phone_number() != null && !place.getFormatted_phone_number().isEmpty()) {
                TextView tvPhoneNumber = (TextView) findViewById(R.id.tvPhoneNumber);
                tvPhoneNumber.setText(place.getFormatted_phone_number());
            } else{
                findViewById(R.id.tvPhoneNumber).setVisibility(View.GONE);
            }
            if(place.getWebsite() != null && !place.getWebsite().isEmpty()) {
                TextView tvWebsite = (TextView) findViewById(R.id.tvWebsite);
                tvWebsite.setText(place.getWebsite());
            } else{
                findViewById(R.id.tvWebsite).setVisibility(View.GONE);
            }
            if(place.getRating() >= 0) {
                RatingBar ratingBar = (RatingBar) findViewById(R.id.ratingBar);
                ratingBar.setRating(Float.parseFloat(place.getRating().toString()));

                TextView tvUserRatingsTotal = (TextView) findViewById(R.id.tvUserRatingsTotal);
                tvUserRatingsTotal.setText(" (" + place.getUser_ratings_total() + " ratings)");
            } else{
                findViewById(R.id.ratingBar).setVisibility(View.GONE);
                TextView tvUserRatingsTotal = (TextView) findViewById(R.id.tvUserRatingsTotal);
                tvUserRatingsTotal.setText("(no ratings)");
            }
            if(place.getOpeningHours() != null && !place.getOpeningHours().isEmpty()) {
                String text = "Opening hours ";
                TextView tvOpenNow = (TextView) findViewById(R.id.tvOpenNow);
                if (place.isOpenNow()) {
                    text += "(open now)";
                }
                if (!place.isOpenNow()) {
                    text += "(closed now)";
                }
                tvOpenNow.setText(text);

                TextView tvOpeningHours = (TextView) findViewById(R.id.tvOpeningHours);
                StringBuilder builder = new StringBuilder();

                for (String s : place.getOpeningHours()) {
                    int split = s.indexOf(":");

                    builder.append(s.substring(0, split));
                    builder.append(":\n");
                    builder.append(s.substring(split + 1, s.length()));
                    builder.append("\n");
                }
                tvOpeningHours.setText(builder.toString());
            } else{
                findViewById(R.id.tvOpenNow).setVisibility(View.GONE);
            }
        }
    }

    /**
     * Checks in the Database, if this Place is a favorite
     * and changes the Favorite-ImageButton heart accordingly
     */
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

    /**
     * If the calling Activity was Favorites-MapActivity and this Place was deleted from favorites,
     * the calling Activity gets notified to remove this Place from the Map
     */
    @Override
    public void onBackPressed() {
        if(comingFromFavoriteActivity) {
            intent.putExtra("deleted", !isFavorite);
            setResult(RESULT_OK, intent);
        }
        super.onBackPressed();
    }
}
