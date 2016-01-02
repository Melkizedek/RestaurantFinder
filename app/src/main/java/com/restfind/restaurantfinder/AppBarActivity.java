package com.restfind.restaurantfinder;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.JsonReader;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

//handles almost all Toolbar-Actions and can create a custom AlertDialog with String-Parameter
public abstract class AppBarActivity extends AppCompatActivity {

    protected final String LOG_TAG = "RESTFIND_LOG";
    protected static final int MY_PERMISSIONS_ACCESS_FINE_LOCATION = 1;

    protected static enum MapActivityType{
        SearchResults,
        Invitations,
        Favorites
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_bar);
    }
    /*
    String getApiResult(String request){
        StringBuilder builder =null;
        try {
            URL url = new URL(request.toString());
            InputStream iStream = null;
            HttpURLConnection urlConnection = null;
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            iStream = urlConnection.getInputStream();
            builder = new StringBuilder();

            JsonReader jsonReader = new JsonReader(new InputStreamReader(iStream,"UTF8"));
            jsonReader.beginArray();
            while(jsonReader.hasNext()){
                jsonReader.beginObject();
                while(jsonReader.hasNext()){
                    builder.append(jsonReader.nextName());

                }
                jsonReader.endObject();
            }
            jsonReader.endArray();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return builder.toString();
    }
    */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    //Handles the chosen Action in the Toolbar
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

                //Stops the currently running Service
                //TODO: stop Service

                //return to Login
                startActivity(new Intent(AppBarActivity.this, LoginActivity.class));
                return true;
            case R.id.action_invitations:
                startActivity(new Intent(AppBarActivity.this, InvitationsActivity.class));
                return true;
            case R.id.action_favorites:
                startActivity(new Intent(AppBarActivity.this, FavoritesActivity.class));
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

    protected Place getPlaceDetails(String placeID){
        Place place = new Place();
        try {
            place = createPlaces(getApiResult(createDetailsRequest(placeID))).get(0);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return place;
    }

    protected String createDetailsRequest(String placeID){
        StringBuilder builder = new StringBuilder();
        builder.append("https://maps.googleapis.com/maps/api/place/details/json?");
        builder.append("placeid=");
        builder.append(placeID);
        builder.append("&key=");
        builder.append(getResources().getString(R.string.api_browser_key));
        return builder.toString();
    }

    protected String getApiResult(final String request){
//        JasonTask jasonTask = new JasonTask();
        String result = null;

        //Thread that tries to login
        ExecutorService es = Executors.newSingleThreadExecutor();
        Future<String> future = es.submit(new Callable<String>() {
            public String call() throws IOException {
                String resultFuture = null;
                JSONObject jObj = null;
                try {
                    URL url = new URL(request);
                    InputStream iStream = null;
                    HttpURLConnection urlConnection = null;
                    urlConnection = (HttpURLConnection) url.openConnection();
                    iStream = urlConnection.getInputStream();
                    urlConnection.connect();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(iStream, "UTF-8"), 8);
                    StringBuilder sb = new StringBuilder();
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    resultFuture = sb.toString();

                } catch (MalformedURLException e1) {
                    e1.printStackTrace();
                } catch (UnsupportedEncodingException e1) {
                    e1.printStackTrace();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                return resultFuture;
            }
        });

        try {
            result = future.get();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            es.shutdown();
        }


//        try {
//            result = jasonTask.execute(request).get();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        }
        return result;
    }

    protected List<Place> createPlaces(String apiResult) throws JSONException {
        List<Place> places = new ArrayList<Place>();
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
        if (curObject.has("reference")) {
            place_act.setReference(curObject.getString("reference"));
        }
        if (curObject.has("types")) {
            JSONArray types = new JSONArray();
            for (int j = 0; j < types.length(); j++) {
                place_act.setTypes(types.getString(j));
            }

        }
        if (curObject.has("vicinity")) {
            place_act.setVicinity(curObject.getString("vicinity"));
        }
        if (curObject.has("formatted_address")) {
            place_act.setFormatted_address(curObject.getString("formatted_address"));
        }
        return place_act;
    }

//    private class JasonTask extends AsyncTask<String, Integer, String> {
//        JsonReader reader;
//        StringBuilder builder = new StringBuilder();
//
//        @Override
//        protected String doInBackground(String... params) {
//            params.toString();
//            String result = null;
//            JSONObject jObj = null;
//            try {
//                URL url = new URL(params[0]);
//                InputStream iStream = null;
//                HttpURLConnection urlConnection = null;
//                urlConnection = (HttpURLConnection) url.openConnection();
//                iStream = urlConnection.getInputStream();
//                urlConnection.connect();
//                BufferedReader reader = new BufferedReader(new InputStreamReader(iStream, "UTF-8"), 8);
//                StringBuilder sb = new StringBuilder();
//                String line = null;
//                while ((line = reader.readLine()) != null) {
//                    sb.append(line + "\n");
//                }
//                result = sb.toString();
//
//            } catch (MalformedURLException e1) {
//                e1.printStackTrace();
//            } catch (UnsupportedEncodingException e1) {
//                e1.printStackTrace();
//            } catch (IOException e1) {
//                e1.printStackTrace();
//            }
//            return result;
//        }
//    }
}
