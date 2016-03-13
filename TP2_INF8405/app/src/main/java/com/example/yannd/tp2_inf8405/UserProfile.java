package com.example.yannd.tp2_inf8405;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import java.util.Calendar;
import java.util.List;

/**
 * Created by yannd on 2016-03-03.
 */
public class UserProfile implements LocationListener {
    private boolean meetingOrganizer;
    private List<String> preferences;
    private List<Calendar> availabilities;
    private String username;
    private double latitude;
    private double longitude;
    private final Handler handler = new Handler();


    public UserProfile(){  }

    public UserProfile(boolean organizer, String username){
        this.meetingOrganizer = organizer;
        this.username = username;
    }

    public List<Calendar> getAvailabilities(){
        return availabilities;
    }

    public void setAvailabilities(List<Calendar> availabilities){
        this.availabilities = availabilities;
    }

    //ICI je separe longitude et latitude pcq firebase supporte pas les pair ou l'object Location directement
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


    //Task running each 5 minutes after we log in, in order to keep our availabilites updated in a 5 minutes window
    Runnable mHandlerTask = new Runnable()
    {
        @Override
        public void run() {
            Calendar start = Calendar.getInstance();
            Calendar end = (Calendar) start.clone();
            end.add(Calendar.DAY_OF_YEAR, 7);
            List<Calendar> newAvailabilities = CalendarManager.getInstance().getAvailabilities(start, end);
            UserProfile.this.setAvailabilities(newAvailabilities);

            //Updating Firebase
            DataManager.getInstance().addOrUpdateUser(UserProfile.this);

            //Update each 5 minutes
            handler.postDelayed(mHandlerTask, 1000 * 60 * 5);
        }
    };


    public void startAvailabilitesRefreshTask(){
        mHandlerTask.run(); // Start updating the availabilites each 5 minutes
    }
}
