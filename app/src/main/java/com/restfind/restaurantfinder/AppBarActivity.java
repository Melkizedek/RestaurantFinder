package com.restfind.restaurantfinder;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.restfind.restaurantfinder.assistant.Place;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

//handles almost all Toolbar-Actions, some methods used by multiple sub-Activities and can create a custom AlertDialog with String-Parameter
public abstract class AppBarActivity extends AppCompatActivity {

    protected final String LOG_TAG = "RESTFIND_LOG";
    //Constant-Int for real-time permissions
    protected static final int MY_PERMISSIONS_ACCESS_FINE_LOCATION = 1;

    //enum to differentiate multiple different uses of the MapActivity
    protected enum MapActivityType{
        SearchResults,
        Invitation,
        Favorites
    }

    //enum to differentiate different success or errors, when accessing Database
    protected enum DatabaseResultType{
        Success,
        Connection_Error,
        Login_Incorrect,
        Register_Name_Unabailable
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_bar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    //Handles the chosen Action in the Toolbar and starts the corresponding Activity
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                //delete currently logged in username
                SharedPreferences spLoginCurrent = getApplicationContext().getSharedPreferences(getResources().getString(R.string.login_current), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = spLoginCurrent.edit();
                editor.clear();
                editor.apply();

                //delete saved login
                SharedPreferences spLoginSaved = getApplicationContext().getSharedPreferences(getResources().getString(R.string.login_saved), Context.MODE_PRIVATE);
                editor = spLoginSaved.edit();
                editor.clear();
                editor.apply();

                //return to Login
                startActivity(new Intent(AppBarActivity.this, LoginActivity.class));
                return true;
            case R.id.action_invitations:
                startActivity(new Intent(AppBarActivity.this, InvitationsActivity.class));
                return true;
            case R.id.action_favorites:
                Intent intent = new Intent(AppBarActivity.this, MapActivity.class);
                intent.putExtra(getResources().getString(R.string.map_activity_type), MapActivityType.Favorites);
                startActivity(intent);
                return true;
            case R.id.action_friends:
                startActivity(new Intent(AppBarActivity.this, FriendsActivity.class));
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    //Get current logged-in username
    protected String getCurrentUsername(){
        SharedPreferences spLoginCurrent = getApplicationContext().getSharedPreferences(getResources().getString(R.string.login_current), Context.MODE_PRIVATE);
        return spLoginCurrent.getString(getResources().getString(R.string.login_current), null);
    }

    //creates an AlertDialog with the given text
    protected void showAlertDialog(String text){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Error!");
        builder.setMessage(text);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    //creates a Place-Object with a google places details-search
    protected Place getPlaceDetails(String placeID){
        Place place = new Place();
        try {
            place = createPlaces(getApiResult(createDetailsRequest(placeID))).get(0);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return place;
    }

    //creates a URL for the Google Places Web Service Details-Search
    private String createDetailsRequest(String placeID){
        return "https://maps.googleapis.com/maps/api/place/details/json?placeid=" + placeID + "&key=" + getResources().getString(R.string.api_browser_key);
    }

    //gets the JSON-Result of a Google Places Search
    protected String getApiResult(final String request){
        String result = null;
        try{
            URL url = new URL(request);
            InputStream iStream;
            HttpURLConnection urlConnection;
            urlConnection = (HttpURLConnection) url.openConnection();
            iStream = urlConnection.getInputStream();
            urlConnection.connect();
            BufferedReader reader = new BufferedReader(new InputStreamReader(iStream, "UTF-8"), 8);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            result = sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    //creates all found Places based on the JSON-Result
    protected List<Place> createPlaces(String apiResult) throws JSONException {
        List<Place> places = new ArrayList<>();
        JSONObject object;
        if(apiResult != null) {
            object = new JSONObject(apiResult);

            if(object.has("results")) {
                JSONArray results = object.getJSONArray("results");

                for (int i = 0; i < results.length(); i++) {
                    JSONObject curObject = results.getJSONObject(i);
                    places.add(createPlace(curObject));
                }
            }
            //only 1 result (Details-request)
            else{
                places.add(createPlace(object.getJSONObject("result")));
            }
        }
        return places;
    }

    //goes through the JSON-Result and creates a single Place-Object
    private Place createPlace(JSONObject curObject) throws JSONException {
        Place place_act = new Place();
        if (curObject.has("geometry")) {
            JSONObject geometryObject = curObject.getJSONObject("geometry");
            if (geometryObject.has("location")) {
                JSONObject locationObject = geometryObject.getJSONObject("location");
                place_act.setLat(locationObject.getDouble("lat"));
                place_act.setLng(locationObject.getDouble("lng"));
            }
        }
        if (curObject.has("icon")) {
            place_act.setIcon(curObject.getString("icon"));
        }
        if (curObject.has("name")) {
            place_act.setName(curObject.getString("name"));
        }
        if (curObject.has("opening_hours")) {
            JSONObject opening_hoursObject = curObject.getJSONObject("opening_hours");
            place_act.setOpenNow(opening_hoursObject.getBoolean("open_now"));
            if (opening_hoursObject.has("weekday_text")) {
                JSONArray weekday = opening_hoursObject.getJSONArray("weekday_text");
                for (int j = 0; j < weekday.length(); j++) {
                    place_act.setOpeningHours(weekday.getString(j));
                }
            }
        }
        if (curObject.has("place_id")) {
            place_act.setPlace_ID(curObject.getString("place_id"));
        }
        if (curObject.has("rating")) {
            place_act.setRating(curObject.getDouble("rating"));
        }
        if (curObject.has("types")) {
            JSONArray types = curObject.getJSONArray("types");
            for (int j = 0; j < types.length(); j++) {
                place_act.setTypes(types.getString(j));
            }
        }
        if (curObject.has("vicinity")) {
            place_act.setVicinity(curObject.getString("vicinity"));
        }
        if(curObject.has("user_ratings_total")){
            place_act.setUser_ratings_total(curObject.getInt("user_ratings_total"));
        }
        if(curObject.has("website")){
            place_act.setWebsite(curObject.getString("website"));
        }
        return place_act;
    }
}
