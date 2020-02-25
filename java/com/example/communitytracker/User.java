package com.example.communitytracker;


import java.io.Serializable;

/**
 * Helper class to push and retrieve consistent data through noSQL (Firebase)
 */
public class User implements Serializable {

    private String id, email, name, phoneNumber;
    private LocationObject location;


    public User() {
    }

    public User(String id, String email, LocationObject location, String name, String phoneNumber) {
        this.id = id;
        this.email = email;
        this.location = location;
        this.name = name;
        this.phoneNumber = phoneNumber;
    }



    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getName() {
        return name;
    }

    public LocationObject getLocation() {
        return location;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLocation(LocationObject location) {
        this.location = location;
    }


}
