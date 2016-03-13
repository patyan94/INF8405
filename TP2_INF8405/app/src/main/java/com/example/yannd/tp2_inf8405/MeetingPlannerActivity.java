package com.example.yannd.tp2_inf8405;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MeetingPlannerActivity extends FragmentActivity
        implements OnMapReadyCallback, com.google.android.gms.location.LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String API_KEY = "AIzaSyAQ9dXeGuQ-t0nTlTwwyIfucrSXTDNrLjA";
    private  static final  String SERVER_KEY = "AIzaSyDPwiIVC-qg91RgpZ8u92xdozNbGI38bm0";
    private static final int PLACE_PICKER_REQUEST = 1;
    int eventRadius = 10000;//m
    Button createMeetingButton;
    LocationRequest mLocationRequest;
    Location mLastLocation;
    ListView scheduledMeetingsList;
    GoogleMap map;
    private GoogleApiClient mGoogleApiClient;
    ArrayList<MyPlace> places;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(AppIndex.API)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting_planner);

        createMeetingButton = (Button) findViewById(R.id.create_meeting_button);
        createMeetingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    places = (ArrayList<MyPlace>) new GetPlaces().execute().get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        });
        //scheduledMeetingsList = (ListView) findViewById(R.id.scheduledMeetings)
    //mLocationRequest = new LocationRequest();
    }
    private class GetPlaces extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] params) {
            String[] places = new String[3];
            places[0]="cafe";places[1]="park";places[2]="cafe";

            return FindPlaces(places[0]);
        }
    }
    public void CreateEvent() {
        //TODO creates an event to the next available time, according to everyone's known location
    }

    public ArrayList<MyPlace> FindPlaces(String placeSpecification) {

        Location centralLocation = GetCentralLocation();
        if(centralLocation == null)return null;

        String urlString = makeUrl(centralLocation.getLatitude(), centralLocation.getLongitude(), placeSpecification);
        ArrayList<MyPlace> arrayList = new ArrayList<MyPlace>();
        try {
            String json = getJSON(urlString);

            System.out.println(json);
            JSONObject object = new JSONObject(json);
            JSONArray array = object.getJSONArray("results");


            for (int i = 0; i < array.length(); i++) {
                try {
                    MyPlace place = MyPlace.jsonToPlaceReference((JSONObject) array.get(i));
                    arrayList.add(place);
                } catch (Exception e) {
                }
            }
        } catch (JSONException ex) {
            Logger.getLogger(MeetingPlannerActivity.class.getName()).log(Level.SEVERE,
                    null, ex);
        }
        return arrayList;
    }

    protected String getJSON(String url) {
        return getUrlContents(url);
    }

    private String getUrlContents(String theUrl) {
        StringBuilder content = new StringBuilder();
        try {
            URL url = new URL(theUrl);
            URLConnection urlConnection = url.openConnection();
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(urlConnection.getInputStream()), 8);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                content.append(line + "\n");
            }
            bufferedReader.close();
        }catch (Exception e) {
            e.printStackTrace();
        }
        return content.toString();
    }

    private String makeUrl(double latitude, double longitude, String place) {
        StringBuilder urlString = new StringBuilder(
                "https://maps.googleapis.com/maps/api/place/search/json?");

        urlString.append("&location=");
        urlString.append(Double.toString(latitude));
        urlString.append(",");
        urlString.append(Double.toString(longitude));
        urlString.append("&radius="+eventRadius);
        urlString.append("&sensor=false&key=" + SERVER_KEY);
        urlString.append("&rankby=distance");

        if (!place.equals("")) {
            urlString.append("&type=" + place);
        }
        return urlString.toString();
    }

    public Location GetCentralLocation() {
        /*double latitude = 0, longitude = 0;
        Group currentGroup = DataManager.getInstance().getCurrentGroup();
        List<UserProfile> groupMembers = currentGroup.getGroupMembers();
        for (UserProfile u : groupMembers) {
            longitude += u.getLongitude();
            latitude += u.getLatitude();
        }
        latitude /= groupMembers.size();
        longitude /= groupMembers.size();
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        return location;*/
        return mLastLocation;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
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
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
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

    @Override
    public void onLocationChanged(Location location) {
        if(location != null)
            mLastLocation = location;
    }
}
