package com.restfind.restaurantfinder.assistant;


import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Map;

public class Invitation implements Parcelable{
    private int id;
    private String host;
    private String placeID;
    private long time;
    private boolean received;
    private Map<String, Integer> invitees;

    public Invitation(int id, String host, String placeID, long time) {
        this(id, host, placeID, time, false, null);
    }

    public Invitation(int id, String host, String placeID, long time, boolean received, Map<String, Integer> invitees) {
        this.id = id;
        this.host = host;
        this.placeID = placeID;
        this.time = time;
        this.received = received;
        this.invitees = invitees;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPlaceID() {
        return placeID;
    }

    public void setPlaceID(String placeID) {
        this.placeID = placeID;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public boolean isReceived() {
        return received;
    }

    public void setReceived(boolean received) {
        this.received = received;
    }

    public Map<String, Integer> getInvitees() {
        return invitees;
    }

    public void setInvitees(Map<String, Integer> invitees) {
        this.invitees = invitees;
    }

    //Parcelable Part

    protected Invitation(Parcel in) {
        id = in.readInt();
        host = in.readString();
        placeID = in.readString();
        time = in.readLong();
        received = in.readInt() != 0;

        invitees = new HashMap<>();
        int size = in.readInt();
        for(int i = 0; i < size; i++){
            String key = in.readString();
            int value = in.readInt();
            invitees.put(key, value);
        }
    }

    public static final Creator<Invitation> CREATOR = new Creator<Invitation>() {
        @Override
        public Invitation createFromParcel(Parcel in) {
            return new Invitation(in);
        }

        @Override
        public Invitation[] newArray(int size) {
            return new Invitation[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(host);
        dest.writeString(placeID);
        dest.writeLong(time);
        dest.writeInt(received ? 1 : 0);

        dest.writeInt(invitees.size());
        for(Map.Entry<String, Integer> entry : invitees.entrySet()){
            dest.writeString(entry.getKey());
            dest.writeInt(entry.getValue());
        }
    }
}


