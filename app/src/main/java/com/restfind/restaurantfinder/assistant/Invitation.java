package com.restfind.restaurantfinder.assistant;


import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Map;

public class Invitation implements Parcelable{
    private int id;
    private String inviter;
    private String placeID;
    private long time;
    private Map<String, Boolean> invitees;

    public Invitation(int id, String inviter, String placeID, long time, Map<String, Boolean> invitees) {
        this.id = id;
        this.inviter = inviter;
        this.placeID = placeID;
        this.time = time;
        this.invitees = invitees;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getInviter() {
        return inviter;
    }

    public void setInviter(String inviter) {
        this.inviter = inviter;
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

    public Map<String, Boolean> getInvitees() {
        return invitees;
    }

    public void setInvitees(Map<String, Boolean> invitees) {
        this.invitees = invitees;
    }

    //Parcelable Part

    protected Invitation(Parcel in) {
        id = in.readInt();
        inviter = in.readString();
        placeID = in.readString();
        time = in.readLong();

        invitees = new HashMap<>();
        int size = in.readInt();
        for(int i = 0; i < size; i++){
            String key = in.readString();
            boolean value = in.readInt() != 0;
            invitees.put(key,value);
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
        dest.writeString(inviter);
        dest.writeString(placeID);
        dest.writeLong(time);

        dest.writeInt(invitees.size());
        for(Map.Entry<String, Boolean> entry : invitees.entrySet()){
            dest.writeString(entry.getKey());
            dest.writeInt(entry.getValue() ? 1 : 0);
        }
    }
}


