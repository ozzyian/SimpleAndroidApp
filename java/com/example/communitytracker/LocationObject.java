package com.example.communitytracker;


import java.io.Serializable;

/**
 * Helper class to push and retrieve
 * location data through noSQL (Firebase)
 */
public class LocationObject implements Serializable {

    private double longitude, latitude;

    public LocationObject() {
    }

    public LocationObject(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }


    public Double getLongitude() {
        return longitude;
    }


    public Double getLatitude() {
        return latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
}
