package com.restfind.restaurantfinder;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.restfind.restaurantfinder.database.Database;

public class RegisterActivity extends AppBarActivity {

    private String username;
    private String password;

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
                username = username_Field.getText().toString();
                password = password_Field.getText().toString();

                //username or password contain illegal characters
                if(username.contains(";") || password.contains(";")){
                    showAlertDialog("Username or Password contain illegal Characters");
                }else{
                    //username and password not empty -> register
                    if (!username.equals("") && !password.equals("")) {
                        register();
                    }
                }
            }
        });
    }

    private void register() {
        findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
        new RegisterTask().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    //<Input for doInBackground, (Progress), Input for onPostExecute>
    private class RegisterTask extends AsyncTask<Void, Integer, DatabaseResultType> {

        @Override
        protected DatabaseResultType doInBackground(Void... params) {
            boolean registerSuccessful;
            try {
                registerSuccessful = Database.register(username, password);
            } catch (Exception e) {
                //Could not connect to Server with .php-files
                return DatabaseResultType.Connection_Error;
            }
            if(registerSuccessful) {
                return DatabaseResultType.Success;
            } else{
                return DatabaseResultType.Register_Name_Unabailable;
            }
        }

        @Override
        protected void onPostExecute(DatabaseResultType result) {
            findViewById(R.id.loadingPanel).setVisibility(View.GONE);

            //Register successful
            if (result == DatabaseResultType.Success) {
                //delete saved login
                SharedPreferences spLoginSaved = getApplicationContext().getSharedPreferences(getResources().getString(R.string.login_saved), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = spLoginSaved.edit();
                editor.clear();
                editor.apply();

                //Start new Activity (back to Login)
                RegisterActivity.this.startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            }
            //Registration failed (Username already in use)
            else if(result == DatabaseResultType.Register_Name_Unabailable){
                showAlertDialog(getResources().getString(R.string.register_name_unavailable));
            } else{
                showAlertDialog(getResources().getString(R.string.connection_error));
            }
        }
    }
}
