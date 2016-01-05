package com.restfind.restaurantfinder.assistant;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class Place implements Parcelable {
    private String icon;
    private Double lat;
    private Double lng;
    private String name;
    private List<String> openingHours;
    private boolean openNow;
    private String place_ID;
    private Double rating;
    private List<String> types;
    private String vicinity;
    private String formatted_phone_number;
    private int user_ratings_total;
    private String website;

    public Place(){
        openingHours = new ArrayList<>();
        types = new ArrayList<>();
        rating = -1.0;
        user_ratings_total = -1;
        website = "";
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getOpeningHours() {
        return openingHours;
    }

    public void setOpeningHours(String openingHour) {
        openingHours.add(openingHour);
    }

    public boolean isOpenNow() {
        return openNow;
    }

    public void setOpenNow(boolean openNow) {
        this.openNow = openNow;
    }

    public String getPlace_ID() {
        return place_ID;
    }

    public void setPlace_ID(String place_ID) {
        this.place_ID = place_ID;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public List<String> getTypes() {
        return types;
    }

    public void setTypes(String type) {
        this.types.add(type);
    }

    public String getVicinity() {
        return vicinity;
    }

    public void setVicinity(String vicinity) {
        this.vicinity = vicinity;
    }

    public int getUser_ratings_total() {
        return user_ratings_total;
    }

    public void setUser_ratings_total(int user_ratings_total) {
        this.user_ratings_total = user_ratings_total;
    }

    public String getFormatted_phone_number() {
        return formatted_phone_number;
    }

    public void setFormatted_phone_number(String formatted_phone_number) {
        this.formatted_phone_number = formatted_phone_number;
    }

    public void setTypes(List<String> types) {
        this.types = types;
    }

    public void setOpeningHours(List<String> openingHours) {
        this.openingHours = openingHours;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }


    //Parcelable Part

    protected Place(Parcel in) {
        icon = in.readString();
        lat = in.readDouble();
        lng = in.readDouble();
        name = in.readString();
        openingHours = new ArrayList<>();
        in.readStringList(openingHours);
        openNow = in.readInt() != 0;
        place_ID = in.readString();
        rating = in.readDouble();
        types = new ArrayList<>();
        in.readStringList(types);
        vicinity = in.readString();
        user_ratings_total = in.readInt();
        website = in.readString();
    }

    public static final Creator<Place> CREATOR = new Creator<Place>() {
        @Override
        public Place createFromParcel(Parcel in) {
            return new Place(in);
        }

        @Override
        public Place[] newArray(int size) {
            return new Place[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(icon);
        dest.writeDouble(lat);
        dest.writeDouble(lng);
        dest.writeString(name);
        dest.writeStringList(openingHours);
        dest.writeInt(openNow ? 1 : 0);
        dest.writeString(place_ID);
        dest.writeDouble(rating);
        dest.writeStringList(types);
        dest.writeString(vicinity);
        dest.writeInt(user_ratings_total);
        dest.writeString(website);
    }
}
