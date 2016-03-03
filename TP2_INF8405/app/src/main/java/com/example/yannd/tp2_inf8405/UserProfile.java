package com.example.yannd.tp2_inf8405;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by yannd on 2016-03-03.
 */


public class UserProfile implements LocationListener {
    public static UserProfile currentUser;
    public static String currentGroupName;
    private boolean meetingOrganizer;
    private BitSet preferences;
    private String username;
    private Location location;

    public Location getLocation() {
        return location;
    }

    public static String getCurrentGroupName() {
        return currentGroupName;
    }

    public static void setCurrentGroupName(String currentGroupName) {
        UserProfile.currentGroupName = currentGroupName;
    }


    public static UserProfile getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(UserProfile currentUser) {
        UserProfile.currentUser = currentUser;
    }



    public boolean isMeetingOrganizer() {
        return meetingOrganizer;
    }

    public void setMeetingOrganizer(boolean meetingOrganizer) {
        this.meetingOrganizer = meetingOrganizer;
    }

    public String getUsername() {

        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPreferences(BitSet preferences) {
        this.preferences = preferences;
    }

    public BitSet getPreferences() {

        return preferences;
    }

    @Override
    public void onLocationChanged(Location location) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
