package com.restfind.restaurantfinder;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class LoginActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final EditText username_Field = (EditText) findViewById(R.id.username);
        final EditText password_Field = (EditText) findViewById(R.id.passwort);
        Button registerButton = (Button) findViewById(R.id.register);
        Button signInButton = (Button) findViewById(R.id.signIn);

//        final Context context = this;
        final AlertDialog.Builder passwordFalse = new AlertDialog.Builder(this);
        passwordFalse.setTitle("Warning");
        passwordFalse.setMessage("Password or Username not correct");
        passwordFalse.setCancelable(true);
        passwordFalse.setNegativeButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });


        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivity(new Intent(LoginActivity.this, RegisterActivity));
            }
        });

        signInButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                boolean loginResult = false;
                final String username = username_Field.getText().toString();
                final String password = password_Field.getText().toString();

                if (!username.equals("") && !password.equals("")) {
                    ExecutorService es = Executors.newSingleThreadExecutor();
                    Future<Boolean> result = es.submit(new Callable<Boolean>() {
                        public Boolean call() throws Exception {
                            if (Database.login(username, password)) {
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

                    //Login successful
                    if (loginResult) {
                        Log.v("LoginTest", "successful");
                        Intent myIntent = new Intent(LoginActivity.this, MainActivity.class);
                        myIntent.putExtra("username", username); //Optional parameters
                        LoginActivity.this.startActivity(myIntent);
                    } else {
                        AlertDialog dialog = passwordFalse.create();
                        dialog.show();
                    }

                }
            }
        });
        // CharSequence test = getText(R.id.username);
        // CharSequence test2 = getText(R.id.passwort);
    }

    @Override
    protected void onStart() {
        super.onStart();


    }
}
