package com.restfind.restaurantfinder;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class LoginActivity extends AppCompatActivity {

    SharedPreferences spLoginSaved;
    CheckBox cbAutomaticLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //create UI Elements
        final EditText username_Field = (EditText) findViewById(R.id.username);
        final EditText password_Field = (EditText) findViewById(R.id.passwort);
        Button registerButton = (Button) findViewById(R.id.register);
        Button signInButton = (Button) findViewById(R.id.signIn);
        cbAutomaticLogin = (CheckBox) findViewById(R.id.cbAutomaticLogin);

        //look for saved username and password
        spLoginSaved = getApplicationContext().getSharedPreferences(getResources().getString(R.string.login_saved), Context.MODE_PRIVATE);
        String username = spLoginSaved.getString("username", null);
        String password = spLoginSaved.getString("password", null);

        //username and password were saved (perform automatic login)
        if(username != null && password != null){
            login(username, password);
        }

        //Register Button
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //start new Activity (Register)
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

        //Sign-In Button
        signInButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final String username = username_Field.getText().toString();
                final String password = password_Field.getText().toString();

                //username or password contain illegal characters
                if(username.contains(";") || password.contains(";")){
                    showAlertDialog(getResources().getString(R.string.login_illegal));
                }else{
                    //username and password not empty
                    if (!username.equals("") && !password.equals("")) {
                        login(username, password);
                    }
                }
            }
        });
    }

    private void login(final String username, final String password){
        boolean loginSuccessful = false;

        //Thread that tries to login
        ExecutorService es = Executors.newSingleThreadExecutor();
        Future<Boolean> result = es.submit(new Callable<Boolean>() {
            public Boolean call() throws Exception {
                return Database.login(username, password);
            }
        });

        try {
            loginSuccessful = result.get();
        } catch (Exception e) {
            // failed
        }
        es.shutdown();

        if (loginSuccessful) {
            //Save username and password for future automatic logins
            if(cbAutomaticLogin.isChecked()){
                SharedPreferences.Editor editor = spLoginSaved.edit();
                editor.putString("username", username);
                editor.putString("password", password);
                editor.commit();
            }

            //save current logged-in username used in the App
            SharedPreferences spLoginCurrent = getApplicationContext().getSharedPreferences(getResources().getString(R.string.login_current), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = spLoginCurrent.edit();
            editor.putString(getResources().getString(R.string.login_current), username);
            editor.commit();

            //Start new Activity
            LoginActivity.this.startActivity(new Intent(LoginActivity.this, SearchOptionsActivity.class));
        }
        //login failed
        else {
            showAlertDialog(getResources().getString(R.string.login_incorrect));
        }
    }

    //AlertDialog for incorrect input
    private void showAlertDialog(String text){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(text);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
