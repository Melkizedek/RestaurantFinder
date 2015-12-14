package com.restfind.restaurantfinder;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class RegisterActivity extends AppBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Register");
        setSupportActionBar(toolbar);

        //create UI Elements
        final EditText username_Field = (EditText) findViewById(R.id.etUsername);
        final EditText password_Field = (EditText) findViewById(R.id.etPassword);
        Button registerButton = (Button) findViewById(R.id.btnRegister);

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
                    //username and password not empty -> register
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
            //Could not connect to Server with .php-files
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
        //Registration failed (Username already in use)
        else {
            showAlertDialog(getResources().getString(R.string.register_name_unavailable));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }
}
