package com.example.yannd.tp2_inf8405;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.FileNotFoundException;
import java.io.InputStream;
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
        implements OnMapReadyCallback, LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener , Observer{

    final int SELECT_PHOTO = 1;
    MeetingEvent eventBeingModified = null;
    Button createMeetingButton, settingsButton;
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

        settingsButton = (Button)findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MeetingPlannerActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });

        // Creates a meeting when we click on the createMeeting button
        createMeetingButton = (Button) findViewById(R.id.create_meeting_button);
        meetingName = (EditText)findViewById(R.id.meetingName);

        //If the current user of the app isn't the organizer, we don't allow to create any events
        if(DataManager.getInstance().getCurrentUser().isMeetingOrganizer()){
            createMeetingButton.setVisibility(View.VISIBLE);
            meetingName.setVisibility(View.VISIBLE);
        }else{
            createMeetingButton.setVisibility(View.GONE);
            meetingName.setVisibility(View.GONE);
        }

        createMeetingButton.setEnabled(false);
        createMeetingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RessourceMonitor.getInstance().SaveCurrentBatteryLevel();
                CreateMeeting();
            }
        });

        // Enables the create meeting button if the event name has at least 3 characters
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
        scheduledMeetingsList.setAdapter(new EventRowAdapter(this, getApplicationContext(), (ArrayList) DataManager.getInstance().getCurrentGroup().getGroupEvents()));
    }

    protected void onResume(){
        super.onResume();

        // Creates a meeting when we click on the createMeeting button
        createMeetingButton = (Button) findViewById(R.id.create_meeting_button);
        meetingName = (EditText)findViewById(R.id.meetingName);

        //If the current user of the app isn't the organizer, we don't allow to create any events
        if(DataManager.getInstance().getCurrentUser().isMeetingOrganizer()){
            createMeetingButton.setVisibility(View.VISIBLE);
            meetingName.setVisibility(View.VISIBLE);
        }else{
            createMeetingButton.setVisibility(View.GONE);
            meetingName.setVisibility(View.GONE);
        }
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

    // Choose a suitable date for an event
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
        meetingName.clearFocus();
        MeetingEvent event = new MeetingEvent();
        event.addObserver(this);
        event.setMeetingName(((EditText) findViewById(R.id.meetingName)).getText().toString());
        String places = GetPlacesPreferences();
        Location location = GetCentralLocation();
        SetEventDate(event);
        event.setDescription("Meeting created by the INF8405-TP2 app!");
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
            // Bounds the map around the users positions
            try {
                LatLngBounds bounds = boundsBuilder.build();
                int padding = 250;
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
            scheduledMeetingsList.requestFocus();
        }
        Toast.makeText(getApplicationContext(), "Create meeting battery usage : " + String.valueOf(RessourceMonitor.getInstance().GetLastBatteryUsage()), Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch(requestCode) {
            case SELECT_PHOTO:
                if(resultCode == RESULT_OK){
                    try {
                        final Uri imageUri = imageReturnedIntent.getData();
                        final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                        final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                        eventBeingModified.setDecodedPhoto(selectedImage);
                        DataManager.getInstance().addOrUpdateEvent(eventBeingModified);
                        eventBeingModified = null;
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                }
        }
    }

    // Picks a photo for an event
    public void SetEventPhoto(MeetingEvent event, EventRowAdapter adapter){
        eventBeingModified = event;
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(photoPickerIntent, SELECT_PHOTO);
        adapter.notifyDataSetChanged();
    }

    // Shows a dialog to modify the description of an event
    public void ShowEventDescriptionChangeDialog(MeetingEvent e, EventRowAdapter a){
        final MeetingEvent event = e;
        final EventRowAdapter adapter = a;
        AlertDialog.Builder builder = new AlertDialog.Builder(MeetingPlannerActivity.this);
        builder.setTitle("Description");

        final EditText input = new EditText(MeetingPlannerActivity.this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                event.setDescription(input.getText().toString());
                DataManager.getInstance().addOrUpdateEvent(event);
                adapter.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        mGoogleApiClient.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "MeetingPlanner Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.example.yannd.tp2_inf8405/http/host/path")
        );
        AppIndex.AppIndexApi.start(mGoogleApiClient, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "MeetingPlanner Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.example.yannd.tp2_inf8405/http/host/path")
        );
        AppIndex.AppIndexApi.end(mGoogleApiClient, viewAction);
        mGoogleApiClient.disconnect();
    }
}
