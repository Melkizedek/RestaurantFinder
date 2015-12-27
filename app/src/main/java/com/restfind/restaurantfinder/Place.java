package com.restfind.restaurantfinder;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Melkizedek on 18.12.2015.
 */
public class Place implements Parcelable {
    private String icon;
    private Double lat;
    private Double lng;
    private String name;
    private List<String> openingHours;
    private boolean openNow;
    private String place_ID;
    private String reference;
    //alt_ids
    private String price_level;
    private Double rating;
    private List<String> types;
    private String vicinity;
    private String formatted_address;
    private String id;
    private String scope;

    public Place(){
        openingHours = new ArrayList<String>();
        types = new ArrayList<String>();
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) { this.reference = reference; }

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

    public String getPrice_level() {
        return price_level;
    }

    public void setPrice_level(String price_level) {
        this.price_level = price_level;
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

    public String getFormatted_address() {
        return formatted_address;
    }

    public void setFormatted_address(String formatted_address) {
        this.formatted_address = formatted_address;
    }


    //Parcelable Part

    protected Place(Parcel in) {
        icon = in.readString();
        lat = in.readDouble();
        lng = in.readDouble();
        name = in.readString();
        openingHours = new ArrayList<>();
        in.readStringList(openingHours);
        openNow = in.readInt() != 0;;
        place_ID = in.readString();
        reference = in.readString();
        price_level = in.readString();
        rating = in.readDouble();
        types = new ArrayList<>();
        in.readStringList(types);
        vicinity = in.readString();
        formatted_address = in.readString();
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
        dest.writeString(reference);
        if(price_level != null) {
            dest.writeString(price_level);
        } else{
            dest.writeString("-1");
        }
        if(rating != null) {
            dest.writeDouble(rating);
        } else{
            dest.writeDouble(-1);
        }
        dest.writeStringList(types);
        dest.writeString(vicinity);
        dest.writeString(formatted_address);
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public void setId(String id) {
        this.id = id;
    }
}
