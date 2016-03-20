package com.example.yannd.tp2_inf8405;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

/*
* This activity permits to create an event, vote for a place and see the events and people on the map
*/
public class MeetingPlannerActivity extends FragmentActivity
        implements OnMapReadyCallback, com.google.android.gms.location.LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener , Observer{

    Button createMeetingButton;
    EditText meetingName;
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

        // Initialize Firebase for this context
        Firebase.setAndroidContext(this);

        // Creates a meeting when we click on the createMeeting button
        createMeetingButton = (Button) findViewById(R.id.create_meeting_button);
        createMeetingButton.setEnabled(false);
        createMeetingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RessourceMonitor.getInstance().SaveCurrentBatteryUsage();
                CreateMeeting();
            }
        });

        // Enables the create meeting button if the event name has at least 3 characters
        meetingName = (EditText)findViewById(R.id.meetingName);
        meetingName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                boolean enableCreation = false;
                enableCreation = (s.length() >= 3) && !DataManager.getInstance().getCurrentGroup().ContainsEvent(s.toString());
                createMeetingButton.setEnabled(enableCreation);

            }
        });


        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(MeetingPlannerActivity.this);
        scheduledMeetingsList = (ListView) findViewById(R.id.scheduledMeetings);
        scheduledMeetingsList.setAdapter(new EventRowAdapter(getApplicationContext(), (ArrayList) DataManager.getInstance().getCurrentGroup().getGroupEvents()));
    }

    // Returns a string containing the places types that at least one group member liked
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

    void SetEventDate(MeetingEvent event){
        if(DataManager.getInstance().getCurrentGroup() == null || DataManager.getInstance().getCurrentGroup().getGroupEvents() == null) return;

        List<MeetingEvent> events = DataManager.getInstance().getCurrentGroup().getGroupEvents();

        HashMap<Calendar, Integer> mapAv = new HashMap<>();
        List<UserProfile> users = DataManager.getInstance().getCurrentGroup().getGroupMembers();
        for (UserProfile u : users) {
            for (Calendar c : u.getAvailabilities()) {
                if (mapAv.containsKey(c)) {
                    mapAv.put(c, mapAv.get(c) + 1);
                } else {
                    mapAv.put(c, 1);
                }
            }
        }
        int maxValue = -1;
        Calendar finaldate = null;
        for (Map.Entry<Calendar, Integer> e : mapAv.entrySet()) {
            if (e.getValue() > maxValue) {
                maxValue = e.getValue();
                finaldate = e.getKey();
                event.setDate(finaldate);
            }
        }
    }

    // Function to create a meeting, by finding the plausible places for the vote and the date
    private void CreateMeeting(){
        MeetingEvent event = new MeetingEvent();
        event.addObserver(this);
        event.setMeetingName(((EditText) findViewById(R.id.meetingName)).getText().toString());
        String places = GetPlacesPreferences();
        Location location = GetCentralLocation();
        SetEventDate(event);
        // Async function to create the event
        new PlaceFinder().execute(places, location, event, getApplicationContext());
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

    // Show the users positions on the map
    void ShowUserPositionsOnMap() {
        map.clear();
        Group currentGroup = DataManager.getInstance().getCurrentGroup();
        List<UserProfile> groupMembers = currentGroup.getGroupMembers();
        if (groupMembers != null) {
            for (UserProfile u : groupMembers) {
                MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(u.getLatitude(), u.getLongitude()))
                        .title(u.getUsername())
                        .snippet(u.getUsername());
                map.addMarker(markerOptions);
            }

            LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
            for (UserProfile u : groupMembers) {
                MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(u.getLatitude(), u.getLongitude()))
                        .title(u.getUsername())
                        .snippet(u.getUsername());
                map.addMarker(markerOptions);
                boundsBuilder.include(new LatLng(u.getLatitude(), u.getLongitude()));
            }
            try {
                LatLngBounds bounds = boundsBuilder.build();
                Log.d("DEBUG", "BOUNDS : " + bounds.toString());

                int padding; // offset from edges of the map in pixels
                if(groupMembers.size() > 1){
                    padding = 1000;
                }else{
                    padding = 250;
                }

                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                map.animateCamera(cu);
            }
            catch (Exception e){
                Log.d("DEBUG", e.toString() + "!");
            }
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
        // Creates location requests every 5-60 sec
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(60000);
        mLocationRequest.setFastestInterval(5000);
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
        if(location != null && (location.getLatitude() != 0 && location.getLongitude() != 0) && DataManager.getInstance().getCurrentGroup() != null && DataManager.getInstance().getCurrentUser() != null){
            UserProfile currentProfile = DataManager.getInstance().getCurrentUser();
            currentProfile.setLongitude(location.getLongitude());
            currentProfile.setLatitude(location.getLatitude());
            Group currentGroup = DataManager.getInstance().getCurrentGroup();
            DataManager.getInstance().addOrUpdateUser(currentProfile);
            ShowUserPositionsOnMap();
        }
    }

    // Receives a Meeting Event
    @Override
    public void update(Observable observable, Object data) {
        if(observable.getClass() == MeetingEvent.class) {
            DataManager.getInstance().addOrUpdateEvent((MeetingEvent) observable);
            meetingName.setText("");
        }
        Toast.makeText(getApplicationContext(), "Create meeting battery usage : " + String.valueOf(RessourceMonitor.getInstance().GetLastBatteryUsage()), Toast.LENGTH_LONG).show();
    }
}
