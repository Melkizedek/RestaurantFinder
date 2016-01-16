package com.restfind.restaurantfinder.assistant;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class that represents all Search Options that are used to Search Locations
 */
public class SearchOptions implements Parcelable {
    private String name;
    private int radius;
    private double longitude;
    private double latitude;
    private boolean timeIsNow;
    private String time;
    private int dayOfWeek;
    private List<String> typesRestaurant;
    private List<String> typesBar;
    private List<String> typesCafe;
    private List<String> typesTakeaway;

    public SearchOptions(){
        this("", 0, 0, 0, true, "", -1, null, null, null, null);
    }

    public SearchOptions(String name, int radius, double longitude, double latitude, boolean timeIsNow, String time, int dayOfWeek, List<String> typesRestaurant, List<String> typesBar, List<String> typesCafe, List<String> typesTakeaway) {
        this.name = name;
        this.radius = radius;
        this.longitude = longitude;
        this.latitude = latitude;
        this.timeIsNow = timeIsNow;
        this.time = time;
        this.dayOfWeek = dayOfWeek - 1;
        this.typesRestaurant = typesRestaurant;
        this.typesBar = typesBar;
        this.typesCafe = typesCafe;
        this.typesTakeaway = typesTakeaway;
    }

    public String getName() {
        return name;
    }

    public int getRadius() {
        return radius;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public boolean isTimeNow() {
        return timeIsNow;
    }

    public String getTime() {
        return time;
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public List<String> getTypesRestaurant() {
        return typesRestaurant;
    }

    public List<String> getTypesBar() {
        return typesBar;
    }

    public List<String> getTypesCafe() {
        return typesCafe;
    }

    public List<String> getTypesTakeaway() {
        return typesTakeaway;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setTimeIsNow(boolean timeIsNow) {
        this.timeIsNow = timeIsNow;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setDayOfWeek(int dayOfWeek) {
        this.dayOfWeek = dayOfWeek - 1;
    }

    public void setTypesRestaurant(List<String> typesRestaurant) {
        this.typesRestaurant = typesRestaurant;
    }

    public void setTypesBar(List<String> typesBar) {
        this.typesBar = typesBar;
    }

    public void setTypesCafe(List<String> typesCafe) {
        this.typesCafe = typesCafe;
    }

    public void setTypesTakeaway(List<String> typesTakeaway) {
        this.typesTakeaway = typesTakeaway;
    }

    //Parcelable Part

    protected SearchOptions(Parcel in) {
        name = in.readString();
        radius = in.readInt();
        longitude = in.readDouble();
        latitude = in.readDouble();
        timeIsNow = in.readInt() != 0;
        time = in.readString();
        dayOfWeek = in.readInt();

        typesRestaurant = new ArrayList<>();
        in.readStringList(typesRestaurant);
        typesBar = new ArrayList<>();
        in.readStringList(typesBar);
        typesCafe = new ArrayList<>();
        in.readStringList(typesCafe);
        typesTakeaway = new ArrayList<>();
        in.readStringList(typesTakeaway);
    }

    public static final Creator<SearchOptions> CREATOR = new Creator<SearchOptions>() {
        @Override
        public SearchOptions createFromParcel(Parcel in) {
            return new SearchOptions(in);
        }

        @Override
        public SearchOptions[] newArray(int size) {
            return new SearchOptions[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeInt(radius);
        dest.writeDouble(longitude);
        dest.writeDouble(latitude);
        dest.writeInt(timeIsNow ? 1 : 0);
        dest.writeString(time);
        dest.writeInt(dayOfWeek);
        dest.writeStringList(typesRestaurant);
        dest.writeStringList(typesBar);
        dest.writeStringList(typesCafe);
        dest.writeStringList(typesTakeaway);
    }
}
