package com.restfind.restaurantfinder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Melkizedek on 18.12.2015.
 */
public class Place {
    String icon;
    Double lat;
    Double lng;
    String id;
    String name;
    List<String> openingHours;
    boolean openNow;
    String place_ID;
    String scope;

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    String reference;
    //alt_ids
    String price_level;
    Double rating;
    List<String> types;
    String vicinity;
    String formatted_address;
    Place(){
        openingHours = new ArrayList<String>();
        types = new ArrayList<String>();
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
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

}
