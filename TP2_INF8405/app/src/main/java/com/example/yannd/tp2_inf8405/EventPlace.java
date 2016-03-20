package com.example.yannd.tp2_inf8405;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by yannd on 2016-03-10.
 * This class represent a place and its caracteristics
 */
public class EventPlace {
    private List<String> Votes = new ArrayList<String>();
    private String id;
    private String icon;
    private String name;
    private String vicinity;
    private Double latitude;
    private Double longitude;

    // Returns true if a user has voted for this place
    public boolean hasVoted(String username){
        return Votes.contains(username);
    }

    // Returns the number of users who voted for this place
    public int GetVoteCount(){return Votes.size();}

    // Adds user to the list of user who voted for this place
    public void Vote(String username){
        if(Votes.contains(username)){
            return;
        }
        Votes.add(username);
    }

    // Removes a user from the list of users who voted for this place
    public void UnVote(String username){
        if(!Votes.contains(username)){
            return;
        }
        Votes.remove(username);
    }

    public List<String> getVotes() { return Votes; }
    public void setVotes(List<String> votes) { Votes = votes; }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getIcon() {
        return icon;
    }
    public void setIcon(String icon) {
        this.icon = icon;
    }
    public Double getLatitude() {
        return latitude;
    }
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }
    public Double getLongitude() {
        return longitude;
    }
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getVicinity() {
        return vicinity;
    }
    public void setVicinity(String vicinity) {
        this.vicinity = vicinity;
    }

    // Create an EventPlace from a Json object
    static EventPlace jsonToPlaceReference(JSONObject placeReference) {
        try {
            EventPlace result = new EventPlace();
            JSONObject geometry = (JSONObject) placeReference.get("geometry");
            JSONObject location = (JSONObject) geometry.get("location");
            result.setLatitude((Double) location.get("lat"));
            result.setLongitude((Double) location.get("lng"));
            result.setIcon(placeReference.getString("icon"));
            result.setName(placeReference.getString("name"));
            result.setVicinity(placeReference.getString("vicinity"));
            result.setId(placeReference.getString("id"));
            return result;
        } catch (JSONException ex) {
            Logger.getLogger(EventPlace.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    // Creates a JSON string from an Event place object
    @Override
    public String toString() {
        return "Place{" + "id=" + id + ", icon=" + icon + ", name=" + name + ", latitude=" + latitude + ", longitude=" + longitude + '}';
    }

}