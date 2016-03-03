package com.example.yannd.tp2_inf8405;

import android.location.Location;
import android.location.LocationManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;

public class MeetingPlannerActivity extends AppCompatActivity {

    private static final String API_KEY = "YOUR KEY";

    int eventRadius = 1000;//m
    Button createMeetingButton;
    ListView scheduledMeetingsList;
    MapView mapView;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting_planner);

        createMeetingButton = (Button) findViewById(R.id.create_meeting_button);
        scheduledMeetingsList = (ListView) findViewById(R.id.scheduledMeetings);
        mapView = (MapView) findViewById(R.id.mapView);

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                //TODO add position markers
            }
        });

        mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    public void CreateEvent() {
        //TODO creates an event to the next available time, according to everyone's known location
    }

    public void FindPlaces() {
        String placeTypes = "";
        Location centralLocation = GetCentralLocation();
        ArrayList<Place> places = search(centralLocation.getLatitude(), centralLocation.getLongitude(), eventRadius, placeTypes);
    }

    public ArrayList<Place> search(double lat, double lng, int radius, String placeTypes) {

        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            StringBuilder sb = new StringBuilder("https://maps.googleapis.com/maps/api/place/search/json?sensor=false");
            sb.append("&key=" + API_KEY);
            sb.append("&location=" + String.valueOf(lat) + "," + String.valueOf(lng));
            sb.append("&radius=" + String.valueOf(radius));
            sb.append("&types=" + String.valueOf(radius));

            URL url = new URL(sb.toString());
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            int read;
            char[] buff = new char[4096];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (Exception e) {
            return null;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        try {
            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONArray resultsArray = jsonObj.getJSONArray("results");

            // Extract the Place descriptions from the results
            final ArrayList<Place> resultList = new ArrayList<Place>(resultsArray.length());
            synchronized (resultList) {
                for (int i = 0; i < resultsArray.length(); i++) {
                    Places.GeoDataApi.getPlaceById(mGoogleApiClient, resultsArray.getJSONObject(i).getString("place_id"))
                            .setResultCallback(new ResultCallback<PlaceBuffer>() {
                                @Override
                                public void onResult(PlaceBuffer places) {
                                    if (places.getStatus().isSuccess() && places.getCount() > 0) {
                                        final Place myPlace = places.get(0);
                                        resultList.add(myPlace);
                                    }
                                    places.release();
                                }
                            });
                }
            }
            return  resultList;
        } catch (JSONException e) {
        }

        return null;
    }

    public Location GetCentralLocation() {
        double latitude = 0, longitude = 0;
        Group currentGroup = DatabaseManager.GetGroup(UserProfile.currentGroupName);
        HashSet<UserProfile> groupMembers = currentGroup.getGroupMembers();
        for (UserProfile u : groupMembers) {
            latitude += u.getLocation().getLatitude();
            longitude += u.getLocation().getLongitude();
        }
        latitude /= groupMembers.size();
        longitude /= groupMembers.size();
        Location location = new Location(LocationManager.GPS_PROVIDER);
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        return location;
    }
}
