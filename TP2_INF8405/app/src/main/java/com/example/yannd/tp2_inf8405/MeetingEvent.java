package com.example.yannd.tp2_inf8405;

import com.google.android.gms.location.places.Place;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by yannd on 2016-03-03.
 */
public class MeetingEvent {
    String meetingName;
    Group group;
    HashMap<Place, HashSet<String>> places;
    Place[] placesPropositions;

    public void Vote(String username, int place){

    }

    public void ConfirmEvent(){

    }
}
