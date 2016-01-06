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

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InvitationsActivity extends AppBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invitations);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Invitations");
        setSupportActionBar(toolbar);

//        //Start task
//        GetInvitationsTask task = new GetInvitationsTask();
//        task.execute();
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Start task
        GetInvitationsTask task = new GetInvitationsTask();
        task.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_logout_only, menu);
        menu.getItem(0).setTitle("Logout" + " (" + getCurrentUsername() + ")");
        return true;
    }

    //<Input for doInBackground, (Progress), Input for onPostExecute>
    private class GetInvitationsTask extends AsyncTask<Void, Integer, List<Invitation>> {
        @Override
        protected List<Invitation> doInBackground(Void... params) {
            List<Invitation> invitations = new ArrayList<>();
            List<Invitation> result = new ArrayList<>();

            //TODO: get invitations of user (only where he is undecided or accepted)!

            Map<String, Integer> map = new HashMap<>();
            map.put("Alex", 1);
            map.put("Pia", 0);
            map.put("Max", 0);
            map.put("Monika", 1);
            map.put("Josef", -1);
            Invitation invitation = new Invitation(1, "Thomas", "ChIJ8e5PJ4eXc0cRybSO-hsltRA", Calendar.getInstance().getTimeInMillis() + 600000, true, map);
            invitations.add(invitation);

            map = new HashMap<>();
            map.put("Alex", -1);
            map.put("Max", 1);
            map.put("Monika", 1);
            invitation = new Invitation(2, "Pia", "ChIJQTCNfYGXc0cRUeRPK6fpBk4", Calendar.getInstance().getTimeInMillis() + 100000, true, map);
            invitations.add(invitation);

            for(Invitation i : invitations){
                if(i.getInvitees().get(getCurrentUsername()) >= 0){
                    result.add(i);
                }
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
            listItemDate.setText(new SimpleDateFormat("dd.MM.yyyy - HH:mm").format(new Timestamp(item.getTime())));

            TextView listItemHost = (TextView)view.findViewById(R.id.tvInvitationHost);
            listItemHost.setText(item.getHost());

            //Handle buttons and add onClickListeners
            ImageView imgArrow = (ImageView) findViewById(R.id.imgArrow);

            view.setPadding(5, 20, 5, 20);
            return view;
        }
    }
}
