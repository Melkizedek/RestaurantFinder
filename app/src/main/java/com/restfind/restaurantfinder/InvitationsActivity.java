package com.restfind.restaurantfinder;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.restfind.restaurantfinder.assistant.Invitation;
import com.restfind.restaurantfinder.database.Database;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Shows all Restaurant-Invitation, that the user is a part of (eiter host or invitee), as a list
 */
public class InvitationsActivity extends AppBarActivity {

    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invitations);

        username = getCurrentUsername();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Invitations");
        setSupportActionBar(toolbar);
    }

    /**
     * Everytime, the Activity gets resumed, it executes GetInvitationsTask()
     * to refresh the Invitation-List
     */
    @Override
    protected void onResume() {
        super.onResume();

        //Start task
        GetInvitationsTask task = new GetInvitationsTask();
        task.execute();
    }

    /**
     * Inflates the menu with all Actions except the Invitations-Action
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_invitations, menu);
        menu.getItem(0).setTitle("Logout" + " (" + username + ")");
        return true;
    }

    /**
     * Gets all Invitations from the Database and displays them in a list
     * When an Invitation gets pressed, the MapActivity gets started to show this Invitation on a Map
     */
    private class GetInvitationsTask extends AsyncTask<Void, Integer, List<Invitation>> {
        @Override
        protected List<Invitation> doInBackground(Void... params) {
            List<Invitation> result;
            try {
                result = Database.getInvitations(username);
            } catch (IOException e) {
                return null;
            }
            return result;
        }

        @Override
        protected void onPostExecute(List<Invitation> result) {
            findViewById(R.id.loadingPanel).setVisibility(View.GONE);

            if(result != null){
                ListView lvInvitationList = (ListView) findViewById(R.id.lvInvitationList);
                InvitationAdapter adapter = new InvitationAdapter(result);
                lvInvitationList.setAdapter(adapter);
                registerForContextMenu(lvInvitationList);

                lvInvitationList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                        Intent intent = new Intent(InvitationsActivity.this, MapActivity.class);
                        intent.putExtra(getResources().getString(R.string.map_activity_type), MapActivityType.Invitation);
                        intent.putExtra("invitation", (Invitation)arg0.getItemAtPosition(position));
                        startActivity(intent);
                    }
                });
            } else{
                showAlertDialog(getResources().getString(R.string.connection_error));
            }
        }
    }

    /**
     * Custom ListAdapter for the Invitation-ListView,
     * that shows the date and time in the first line
     * and the host-name in the second line
     */
    public class InvitationAdapter extends BaseAdapter implements ListAdapter {
        private final List mData;

        public InvitationAdapter(List<Invitation> invitations) {
            mData = invitations;
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Invitation getItem(int position) {
            return (Invitation) mData.get(position);
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
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_view_element_invitation, parent, false);
            } else {
                view = convertView;
            }

            final Invitation item = getItem(position);

            //Handle TextViews and display string from your list
            TextView listItemDate = (TextView)view.findViewById(R.id.tvInvitationDate);
            listItemDate.setText(new SimpleDateFormat("dd.MM.yyyy - HH:mm").format(item.getTime()));

            TextView listItemHost = (TextView)view.findViewById(R.id.tvInvitationHost);
            listItemHost.setText(item.getHost());

            //Handle buttons and add onClickListeners
            ImageView imgArrow = (ImageView) findViewById(R.id.imgArrow);

            view.setPadding(5, 20, 5, 20);
            return view;
        }
    }
}
