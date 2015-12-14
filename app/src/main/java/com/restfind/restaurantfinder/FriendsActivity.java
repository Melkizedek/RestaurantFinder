package com.restfind.restaurantfinder;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class FriendsActivity extends AppBarActivity {

    private ListView lvFriendList;
    private String username;
    AlertDialog dialogAddFriend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Friends");
        setSupportActionBar(toolbar);

        //Get current logged-in username
        SharedPreferences spLoginCurrent = getApplicationContext().getSharedPreferences(getResources().getString(R.string.login_current), Context.MODE_PRIVATE);
        username = spLoginCurrent.getString(getResources().getString(R.string.login_current), null);

        //Start task
        GetFriendsTask task = new GetFriendsTask();
        task.execute(username);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_friends, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //Toolbar-Action: Add Friend
            case R.id.action_add_friend:
                //Show AlertDialog with Input-Text to add Friend
                final EditText input = new EditText(this);
                input.setHint("Username of Friend");
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this)
                        .setTitle("Add Friend")
                        .setView(input)
                        //Button that will send Friend-Request to the given User
                        .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final String friend = input.getText().toString();

                                if(friend.isEmpty()){
                                    showAlertDialog("Enter a name!");
                                } else {
                                    boolean addingSuccessful = false;

                                    //Thread that tries to add friend
                                    ExecutorService es = Executors.newSingleThreadExecutor();
                                    Future<Boolean> result = es.submit(new Callable<Boolean>() {
                                        public Boolean call() throws IOException {
                                            return Database.sendFriendInvite(username, friend);
                                        }
                                    });

                                    try {
                                        addingSuccessful = result.get();
                                    } catch (Exception e) {
                                        //Could not connect to Server with .php-files
                                        showAlertDialog(getResources().getString(R.string.connection_error));
                                        return;
                                    } finally {
                                        es.shutdown();
                                    }

                                    //Friend does not exist
                                    if (!addingSuccessful) {
                                        showAlertDialog("This username does not exist!");
                                    } else{
                                        //Start task
                                        GetFriendsTask task = new GetFriendsTask();
                                        task.execute(username);
                                    }
                                }
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialogAddFriend.cancel();
                            }
                        });
                dialogAddFriend = dialogBuilder.create();
                dialogAddFriend.show();
                return true;

            default:
                // Logout
                return super.onOptionsItemSelected(item);
        }
    }

    //<Input for doInBackground, (Progress), Input for onPostExecute>
    private class GetFriendsTask extends AsyncTask<String, Integer, List<String>> {

        @Override
        protected List<String> doInBackground(String... params) {
            List<String> friends = null;

            //Thread that tries to get friend-requests
            ExecutorService es = Executors.newSingleThreadExecutor();
            Future<List<String>> result = es.submit(new Callable<List<String>>() {
                public List<String> call() throws IOException {
                    return Database.getFriendInvites(username);
                }
            });

            try {
                friends = result.get();

                if(friends != null){
                    for(int i = 0; i < friends.size(); i++){
                        friends.set(i, friends.get(i) + ";");
                    }
                }
            } catch (Exception e) {
                //Could not connect to Server with .php-files
                showAlertDialog(getResources().getString(R.string.connection_error));
                return null;
            } finally {
                es.shutdown();
            }

            //Thread that tries to get friends
            es = Executors.newSingleThreadExecutor();
            result = es.submit(new Callable<List<String>>() {
                public List<String> call() throws IOException {
                    return Database.getFriends(username);
                }
            });

            try {
                if(friends != null) {
                    friends.addAll(result.get());
                } else{
                    friends = result.get();
                }
            } catch (Exception e) {
                //Could not connect to Server with .php-files
                showAlertDialog(getResources().getString(R.string.connection_error));
                return null;
            } finally {
                es.shutdown();
            }

            return friends;
        }

        @Override
        protected void onPostExecute(List<String> result) {
            lvFriendList = (ListView) findViewById(R.id.lvFriendList);

            //TODO: create custom Adapter with check-symbol and and x for friend invites (friend + ";" = friend invite)

            if(result != null){
                ListAdapter adapter = new ArrayAdapter<String>(FriendsActivity.this, android.R.layout.simple_list_item_1, result);
                lvFriendList.setAdapter(adapter);
            }
        }
    }
}
