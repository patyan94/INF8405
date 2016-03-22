package com.example.yannd.tp2_inf8405;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;


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
 * This class finds a list of places around a point and returns the 3 closest places
 */
public class PlaceFinder extends AsyncTask {
    private final int NUM_PLACES = 3;
    Context appContext;
    /* Params :
     0 : places string
     1 : Location location
     2 : event
     3 : context
    */
    @Override
    protected Object doInBackground(Object[] params) {
        appContext = (Context)params[3];
        MeetingEvent event = (MeetingEvent)params[2];
        event.setPlaces(FindPlaces((String)params[0], (Location)params[1]));
        return event;
    }

    @Override
    protected void onPostExecute(Object event) {
        ((MeetingEvent)event).notifyObservers();
        // Add Event to Datamanager
    }

    // Function to return an array of plausible meeting places
    public ArrayList<EventPlace> FindPlaces(String placeSpecification, Location location) {

        String urlString = createPlaceSearchUrl(location.getLatitude(), location.getLongitude(), placeSpecification);

        ArrayList<EventPlace> arrayList = new ArrayList<EventPlace>();
        try {
            Log.v("URL", urlString);
            String json = getResponse(urlString);

            //System.out.println(json);
            JSONObject object = new JSONObject(json);
            JSONArray array = object.getJSONArray("results");

            // Picks the 3 closest places
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


    private String createPlaceSearchUrl(double latitude, double longitude, String places) {
        StringBuilder urlString = new StringBuilder(
                "https://maps.googleapis.com/maps/api/place/search/json?");

        urlString.append("location=");
        urlString.append(Double.toString(latitude));
        urlString.append(",");
        urlString.append(Double.toString(longitude));
        urlString.append("&sensor=false&key=" + "AIzaSyDXBEWnKX7wRE3uwU-Is97v14Y_XcVvqxg");
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
