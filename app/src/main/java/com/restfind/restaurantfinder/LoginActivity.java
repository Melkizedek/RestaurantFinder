package com.restfind.restaurantfinder;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class LoginActivity extends AppCompatActivity {

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final Context context = this;
        final String tag = "LogIn";
        final AlertDialog.Builder passwordFalse = new AlertDialog.Builder(context);
        passwordFalse.setTitle("Warning");
        passwordFalse.setMessage("Password or Username not correct");
        passwordFalse.setCancelable(true);
        passwordFalse.setNegativeButton("OK",new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        final EditText username_Field = (EditText) findViewById(R.id.username);
        final EditText password_Field = (EditText) findViewById(R.id.passwort);
        String username = username_Field.toString();
        String password = password_Field.toString();
        Button registerButton = (Button) findViewById(R.id.register);
        Button signInButton = (Button) findViewById(R.id.signIn);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivity(new Intent(LoginActivity.this, RegisterActivity));
            }
        });
        signInButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                boolean loginResult = false;
                if (!username_Field.getText().toString().equals("") && !password_Field.getText().toString().equals("")&&!username_Field.getText().toString().equals("Username")) {
                    Log.v("Test_after_if","true");
                    ExecutorService es = Executors.newSingleThreadExecutor();
                    Future<Boolean> result = es.submit(new Callable<Boolean>() {
                        public Boolean call() throws Exception {
                            if (Database.login(username_Field.getText().toString(),password_Field.getText().toString())) {
                                return true;
                            } else {
                                return false;
                            }
                        }
                    });
                    try {
                        loginResult = result.get();
                    } catch (Exception e) {
                        // failed
                    }
                    es.shutdown();
                    if (loginResult) {
                        Log.v("Test_after_if","true");
                    } else {
                        AlertDialog dialog = passwordFalse.create();
                        dialog.show();

                    }

                }
            }
        });
        // CharSequence test = getText(R.id.username);
        // CharSequence test2 = getText(R.id.passwort);


        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }


    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.restfind.restaurantfinder/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.restfind.restaurantfinder/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
}
