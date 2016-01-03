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
import android.widget.CheckBox;
import android.widget.EditText;

import com.restfind.restaurantfinder.service.NotificationEventReceiver;

public class LoginActivity extends AppBarActivity {

    private SharedPreferences spLoginSaved;
    private CheckBox cbAutomaticLogin;
    private String usernameChosen;
    private String passwordChosen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Login");
        setSupportActionBar(toolbar);

        //create UI Elements
        final EditText username_Field = (EditText) findViewById(R.id.etUsername);
        final EditText password_Field = (EditText) findViewById(R.id.etPassword);
        Button registerButton = (Button) findViewById(R.id.btnRegister);
        Button signInButton = (Button) findViewById(R.id.btnLogin);
        cbAutomaticLogin = (CheckBox) findViewById(R.id.cbAutomaticLogin);

        //look for saved username and password
        spLoginSaved = getApplicationContext().getSharedPreferences(getResources().getString(R.string.login_saved), Context.MODE_PRIVATE);
        String username = spLoginSaved.getString("username", null);
        String password = spLoginSaved.getString("password", null);

        //username and password were saved -> perform automatic login
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

        //Login Button
        signInButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final String username = username_Field.getText().toString();
                final String password = password_Field.getText().toString();

                //username or password contain illegal characters
                if(username.contains(";") || password.contains(";")){
                    showAlertDialog(getResources().getString(R.string.login_illegal));
                }else{
                    //username and password not empty -> login
                    if (!username.equals("") && !password.equals("")) {
                        login(username, password);
                    }
                }
            }
        });
    }

    private void login(final String username, final String password){
        usernameChosen = username;
        passwordChosen = password;
        findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
        new LoginTask().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    @Override
    public void onBackPressed() {
        //Do nothing
    }

    //<Input for doInBackground, (Progress), Input for onPostExecute>
    private class LoginTask extends AsyncTask<Void, Integer, DatabaseResultType> {

        @Override
        protected DatabaseResultType doInBackground(Void... params) {
            boolean loginSuccessful;
            try {
                loginSuccessful = Database.login(usernameChosen, passwordChosen);
            } catch (Exception e) {
                //Could not connect to Server with .php-files
                return DatabaseResultType.Connection_Error;
            }
            if(loginSuccessful) {
                return DatabaseResultType.Success;
            } else{
                return DatabaseResultType.Login_Incorrect;
            }
        }

        @Override
        protected void onPostExecute(DatabaseResultType result) {
            findViewById(R.id.loadingPanel).setVisibility(View.GONE);

            if (result == DatabaseResultType.Success) {
                //if Checkbox is checked -> Save username and password for future automatic logins
                if(cbAutomaticLogin.isChecked()){
                    SharedPreferences.Editor editor = spLoginSaved.edit();
                    editor.putString("username", usernameChosen);
                    editor.putString("password", passwordChosen);
                    editor.apply();
                }

                //save current logged-in username used in the App
                SharedPreferences spLoginCurrent = getApplicationContext().getSharedPreferences(getResources().getString(R.string.login_current), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = spLoginCurrent.edit();
                editor.putString(getResources().getString(R.string.login_current), usernameChosen);
                editor.apply();

                //Start CheckInvitationsService which checks for new invitations every 5 minutes
                NotificationEventReceiver.setupAlarm(getApplicationContext());

                //Start new Activity
                LoginActivity.this.startActivity(new Intent(LoginActivity.this, SearchOptionsActivity.class));
            }
            //login failed (Incorrect Login-data)
            else if(result == DatabaseResultType.Login_Incorrect) {
                showAlertDialog(getResources().getString(R.string.login_incorrect));
            } else{
                showAlertDialog(getResources().getString(R.string.connection_error));
            }
        }
    }
}
