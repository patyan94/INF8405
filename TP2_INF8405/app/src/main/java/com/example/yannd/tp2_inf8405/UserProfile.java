package com.example.yannd.tp2_inf8405;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Pair;

import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by yannd on 2016-03-03.
 */


public class UserProfile implements LocationListener {
    private boolean meetingOrganizer;
    private List<String> preferences;
    private String username;
    private double latitude;
    private double longitude;

    public UserProfile(){  }

    public UserProfile(boolean organizer, String username){
        this.meetingOrganizer = organizer;
        this.username = username;
    }

    //ICI je separe longitude et latitude pcq firebase supporte pas les pair ou l'object location directement, c'est bien plus simple comme ca
    public double getLatitude() {
        return this.latitude;
    }

    public double getLongitude() {
        return this.longitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
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

    public void setPreferences(List<String> preferences) {
        this.preferences = preferences;
    }

    public List<String> getPreferences() {
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
