package com.example.yannd.tp2_inf8405;

import android.app.usage.UsageEvents;
import android.location.Location;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by yannd on 2016-03-13.
 */
public class PlaceFinder extends AsyncTask {
    private final int NUM_PLACES = 3;
    /* Params :
     0 : places string
     1 : Location location
     2 : radius int
     3 : event
    */
    @Override
    protected Object doInBackground(Object[] params) {
        MeetingEvent event = (MeetingEvent)params[3];
        event.setPlaces(FindPlaces((String)params[0], (Location)params[1], (int)params[2]));
        return event;
    }

    // Function to return an array of plausible meeting places
    public ArrayList<EventPlace> FindPlaces(String placeSpecification, Location location, int radius) {


        String urlString = createPlaceSearchUrl(location.getLatitude(), location.getLongitude(), radius, placeSpecification);
        ArrayList<EventPlace> arrayList = new ArrayList<EventPlace>();
        try {
            String json = getResponse(urlString);

            System.out.println(json);
            JSONObject object = new JSONObject(json);
            JSONArray array = object.getJSONArray("results");

            // Augmente le rayon de recherche si on a pas assez de places
            if(array.length() < NUM_PLACES)
                return FindPlaces(placeSpecification, location, radius * 2);

            for (int i = 0; i < NUM_PLACES; i++) {
                try {
                    EventPlace place = EventPlace.jsonToPlaceReference((JSONObject) array.get(i));
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


    private String createPlaceSearchUrl(double latitude, double longitude, int radius, String places) {
        StringBuilder urlString = new StringBuilder(
                "https://maps.googleapis.com/maps/api/place/search/json?");

        urlString.append("&location=");
        urlString.append(Double.toString(latitude));
        urlString.append(",");
        urlString.append(Double.toString(longitude));
        urlString.append("&radius="+radius);
        urlString.append("&sensor=false&key=" + R.string.google_server_key);
        urlString.append("&rankby=distance");

        if (!places.equals("")) {
            urlString.append("&types=" + places);
        }
        return urlString.toString();
    }

    private String getResponse(String theUrl) {
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
}
