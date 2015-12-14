package com.restfind.restaurantfinder;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

//Saves all Search Options that are used to Search Locations
public class SearchOptions implements Parcelable {
    private String searchText;
    private double longitude;
    private double latitude;
    private int radius;
    private List<String> types;

    public SearchOptions(String searchText) {
        this.searchText = searchText;
    }

    public SearchOptions(double longitude, double latitude, int radius, List<String> types){
        this.longitude = longitude;
        this.latitude = latitude;
        this.radius = radius;
        this.types = types;
    }

    public String getSearchText() { return searchText; }
    public double getLatitude() { return latitude; }
    public double getLongitude() {
        return longitude;
    }
    public List<String> getTypes() { return types; }
    public int getRadius() { return radius; }



    //Parcelable Part

    protected SearchOptions(Parcel in) {
        this.searchText = in.readString();
        this.longitude = in.readDouble();
        this.latitude = in.readDouble();
        this.radius = in.readInt();

        this.types = new ArrayList<String>();
        in.readStringList(types);
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
        dest.writeString(searchText);
        dest.writeDouble(longitude);
        dest.writeDouble(latitude);
        dest.writeInt(radius);
        dest.writeStringList(types);
    }
}
