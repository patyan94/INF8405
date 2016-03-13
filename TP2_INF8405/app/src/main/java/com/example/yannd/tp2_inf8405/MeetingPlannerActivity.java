package com.example.yannd.tp2_inf8405;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.SortedMap;
import java.util.concurrent.ExecutionException;

public class MeetingPlannerActivity extends FragmentActivity
        implements OnMapReadyCallback, com.google.android.gms.location.LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener , Observer{

    private static final int eventRadius = 10000;//m
    Button createMeetingButton;
    LocationRequest mLocationRequest;
    ListView scheduledMeetingsList;
    GoogleMap map;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Connects to google API to get locations
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(AppIndex.API)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting_planner);

        // Creates a meeting when we click on the createMeeting button
        createMeetingButton = (Button) findViewById(R.id.create_meeting_button);
        createMeetingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateMeeting();
            }
        });
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(MeetingPlannerActivity.this);
        //scheduledMeetingsList = (ListView) findViewById(R.id.scheduledMeetings)
    //mLocationRequest = new LocationRequest();
    }

    private String GetPlacesPreferences(){


        ArrayList<String> preferences = new ArrayList<>();
        Group currentGroup = DataManager.getInstance().getCurrentGroup();
        List<UserProfile> groupMembers = currentGroup.getGroupMembers();
        for (UserProfile u : groupMembers) {
            for(String pref : u.getPreferences()){
                String lowerCasePref = pref.toLowerCase();
                if(!preferences.contains(lowerCasePref))
                {
                    preferences.add(lowerCasePref);
                return lowerCasePref;
                }
            }
        }
        StringBuilder prefStr = new StringBuilder();

        for(int i = 0; i < preferences.size(); ++i){
            prefStr.append(preferences.get(i));
            if(i<preferences.size()-1)
                prefStr.append('|');
        }
        return prefStr.toString();
    }

    // Function to create a meeting, by finding the plausible places for the vote and the date
    private void CreateMeeting(){
        MeetingEvent event = new MeetingEvent();
        event.addObserver(this);
        event.setMeetingName(((EditText)findViewById(R.id.meetingName)).getText().toString());
        String places = GetPlacesPreferences();
        Location location = GetCentralLocation();

        // TODO find date

        try {
            event = (MeetingEvent) new PlaceFinder().execute(places, location, eventRadius, event, getApplicationContext()).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    // Function to get the location that is in the middle of the positions of the group
    public Location GetCentralLocation() {
        double latitude = 0, longitude = 0;
        Group currentGroup = DataManager.getInstance().getCurrentGroup();
        List<UserProfile> groupMembers = currentGroup.getGroupMembers();
        int countValidLocations = 0;
        for (UserProfile u : groupMembers) {
            if(u.getLongitude() != 0 && u.getLatitude() != 0) {
                ++countValidLocations;
                longitude += u.getLongitude();
                latitude += u.getLatitude();
            }
        }
        Location location = new Location(LocationManager.PASSIVE_PROVIDER);
        latitude /= countValidLocations;
        longitude /= countValidLocations;
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        return location;
    }

    void ShowUserPositionsOnMap(){
        map.clear();

        Group currentGroup = DataManager.getInstance().getCurrentGroup();
        List<UserProfile> groupMembers = currentGroup.getGroupMembers();
        for (UserProfile u : groupMembers) {
            MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(u.getLatitude(), u.getLongitude()))
                                                             .title(u.getUsername())
                                                             .snippet(u.getUsername());
            map.addMarker(markerOptions);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        ShowUserPositionsOnMap();

    }

    @Override
    public void onConnected(Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        // Creates location requests every 30-60 sec
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(60000);
        mLocationRequest.setFastestInterval(30000);
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    // Updates the user's location
    @Override
    public void onLocationChanged(Location location) {
        if(location != null){
            UserProfile currentProfile = DataManager.getInstance().getCurrentUser();
            currentProfile.setLongitude(location.getLongitude());
            currentProfile.setLatitude(location.getLatitude());
            Group currentGroup = DataManager.getInstance().getCurrentGroup();
            DataManager.getInstance().addOrUpdateUser(currentProfile);
        }
    }

    // Receives a Meeting Event
    @Override
    public void update(Observable observable, Object data) {

    }
}
