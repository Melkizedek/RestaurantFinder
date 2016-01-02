package com.restfind.restaurantfinder;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class FriendsActivity extends AppBarActivity {

    private ListView lvFriendList;
    private String username;
    private AlertDialog dialogAddFriend;
    private GetFriendsTask task;

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
        task = new GetFriendsTask();
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
                                    }
//                                    catch (InviteAlreadyExistsException e){
//                                        showAlertDialog("This User has already sent you an Invite!");
//                                    }
                                    catch (Exception e) {
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

    //Creates a context menu to delete a friend
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId()==R.id.lvFriendList) {
            menu.add("Delete");
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();

        switch (item.getItemId()) {
            //Delete Friend
            case 0:
                final String friend = ((Map.Entry<String, Boolean>)lvFriendList.getAdapter().getItem(info.position)).getKey();

                //Thread that tries to delete friend
                ExecutorService es = Executors.newSingleThreadExecutor();
                Future<Boolean> result = es.submit(new Callable<Boolean>() {
                    public Boolean call() throws IOException {
                        return Database.deleteFriend(username, friend);
                    }
                });

                try {
                    result.get();
                } catch (Exception e) {
                    //Could not connect to Server with .php-files
                    showAlertDialog(getResources().getString(R.string.connection_error));
                    return true;
                } finally {
                    es.shutdown();
                }

                //removes the friend from the listView
                ((FriendAdapter)lvFriendList.getAdapter()).remove(info.position);

                return true;
            default:
                super.onContextItemSelected(item);
        }
        return true;
    }

    //<Input for doInBackground, (Progress), Input for onPostExecute>
    private class GetFriendsTask extends AsyncTask<String, Integer, Map<String, Boolean>> {

        @Override
        protected Map<String, Boolean> doInBackground(String... params) {
            //Map with <Friend-Name, invite or not>
            Map<String, Boolean> friendsMap = new TreeMap<>();
            List<String> friends;

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
                        friendsMap.put(friends.get(i), true);
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
                friends = result.get();

                if(friends != null){
                    for(int i = 0; i < friends.size(); i++){
                        friendsMap.put(friends.get(i), false);
                    }
                }
            } catch (Exception e) {
                //Could not connect to Server with .php-files
                showAlertDialog(getResources().getString(R.string.connection_error));
                return null;
            } finally {
                es.shutdown();
            }

            return friendsMap;
        }

        //puts the friend-requests and friends into the listView
        @Override
        protected void onPostExecute(Map<String, Boolean> result) {
            if(result != null){
                lvFriendList = (ListView) findViewById(R.id.lvFriendList);
                FriendAdapter adapter = new FriendAdapter(result);
                lvFriendList.setAdapter(adapter);
                registerForContextMenu(lvFriendList);
            }
        }
    }

    public class FriendAdapter extends BaseAdapter implements ListAdapter {
        private final ArrayList mData;

        public FriendAdapter(Map<String, Boolean> map) {
            mData = new ArrayList();
            mData.addAll(map.entrySet());
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Map.Entry<String, Boolean> getItem(int position) {
            return (Map.Entry) mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        public void remove(int position){
            mData.remove(position);
            notifyDataSetChanged();
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            final View view;

            if (convertView == null) {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_view_element_friend, parent, false);
            } else {
                view = convertView;
            }

            final Map.Entry<String, Boolean> item = getItem(position);

            //Handle TextView and display string from your list
            TextView listItemText = (TextView)view.findViewById(R.id.tvFriend);
            listItemText.setText(item.getKey());

            //Handle buttons and add onClickListeners
            ImageButton btnAccept = (ImageButton)view.findViewById(R.id.btnAccept);
            ImageButton btnDecline = (ImageButton)view.findViewById(R.id.btnDecline);

            //List-item is a friend-request -> show accept and decline buttons
            if(item.getValue()) {
                btnAccept.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boolean acceptSuccessful = false;

                        //Thread that tries to accept friend
                        ExecutorService es = Executors.newSingleThreadExecutor();
                        Future<Boolean> result = es.submit(new Callable<Boolean>() {
                            public Boolean call() throws IOException {
                                return Database.acceptFriendInvite(username, item.getKey());
                            }
                        });

                        try {
                            acceptSuccessful = result.get();
                        } catch (Exception e) {
                            //Could not connect to Server with .php-files
                            showAlertDialog(getResources().getString(R.string.connection_error));
                            return;
                        } finally {
                            es.shutdown();
                        }

                        if (acceptSuccessful) {
                            lvFriendList.setAdapter(null);
                            //Start task
                            task = new GetFriendsTask();
                            task.execute(username);
                        }

//                        notifyDataSetChanged();
                    }
                });
                btnDecline.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Thread that tries to decline friend
                        ExecutorService es = Executors.newSingleThreadExecutor();
                        Future<Boolean> result = es.submit(new Callable<Boolean>() {
                            public Boolean call() throws IOException {
                                return Database.declineFriendInvite(username, item.getKey());
                            }
                        });

                        try {
                            result.get();
                        } catch (Exception e) {
                            //Could not connect to Server with .php-files
                            showAlertDialog(getResources().getString(R.string.connection_error));
                            return;
                        } finally {
                            es.shutdown();
                        }

                        mData.remove(position);
                        notifyDataSetChanged();
                    }
                });

                btnAccept.setVisibility(View.VISIBLE);
                btnDecline.setVisibility(View.VISIBLE);
            }
            //Friends don't have accept or decline buttons
            else{
                btnAccept.setVisibility(View.GONE);
                btnDecline.setVisibility(View.GONE);
            }

            view.setPadding(5, 20, 5, 20);
            return view;
        }
    }
}
