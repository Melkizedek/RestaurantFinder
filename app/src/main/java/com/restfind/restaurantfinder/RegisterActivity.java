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
import android.widget.EditText;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //create UI Elements
        final EditText username_Field = (EditText) findViewById(R.id.username);
        final EditText password_Field = (EditText) findViewById(R.id.passwort);
        Button registerButton = (Button) findViewById(R.id.register);

        //Register Button
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String username = username_Field.getText().toString();
                final String password = password_Field.getText().toString();

                //username or password contain illegal characters
                if(username.contains(";") || password.contains(";")){
                    showAlertDialog("Username or Password contain illegal Characters");
                }else{
                    //username and password not empty
                    if (!username.equals("") && !password.equals("")) {
                        register(username, password);
                    }
                }
            }
        });
    }

    private void register(final String username, final String password) {
        boolean registerSuccessful = false;

        //Thread that tries to register
        ExecutorService es = Executors.newSingleThreadExecutor();
        Future<Boolean> result = es.submit(new Callable<Boolean>() {
            public Boolean call() throws Exception {
                return Database.register(username, password);
            }
        });

        try {
            registerSuccessful = result.get();
        } catch (Exception e) {
            showAlertDialog(getResources().getString(R.string.connection_error));
            return;
        } finally {
            es.shutdown();
        }

        //Register successful
        if (registerSuccessful) {
            //delete saved login
            SharedPreferences spLoginSaved = getApplicationContext().getSharedPreferences(getResources().getString(R.string.login_saved), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = spLoginSaved.edit();
            editor.clear();
            editor.commit();

            //Start new Activity (back to Login)
            RegisterActivity.this.startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
        }
        //Registration failed
        else {
            showAlertDialog(getResources().getString(R.string.registerNameUnavailable));
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
