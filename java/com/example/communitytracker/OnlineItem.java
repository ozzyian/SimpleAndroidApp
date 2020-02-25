package com.example.communitytracker;


/**
 * Helper class to handle the data of each item
 * in the recycleView to be displayed in the
 * OnlineFragment.
 */
public class OnlineItem {

    private String name, email, id, phoneNumber;

    public OnlineItem() {
    }

    public OnlineItem(String name, String email, String phoneNumber, String id) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setID(String userId) {
        this.id = userId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getID() {
        return id;
    }
}
