package com.example.yannd.tp2_inf8405;

import android.provider.ContactsContract;

import com.google.android.gms.location.places.Place;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

/**
 * Created by yannd on 2016-03-03.
 */
public class MeetingEvent extends Observable {
    String meetingName;
    List<EventPlace> Places = new ArrayList<EventPlace>();
    EventPlace FinalPlace = null;

    public int GetTotalVotes(){
        int count = 0;
        for(EventPlace place : Places){
            count += place.GetVoteCount();
        }
        return count;
    }

    public void Vote(EventPlace place, String username){
        if(FinalPlace != null) return;
        for(EventPlace plc : Places ){
            if(plc == place)
                plc.Vote(username);
            else
                plc.UnVote(username);
        }
    }

    void ChooseFinalPlace(){
        if(GetTotalVotes() == DataManager.getInstance().getCurrentGroup().getGroupMembers().size()){
            int maxVote = 0;
            for(EventPlace plc : Places ){
                if(plc.GetVoteCount() > maxVote)
                    FinalPlace = plc;
            }
        }
    }

    public String getMeetingName() {
        return meetingName;
    }

    public void setMeetingName(String meetingName) {
        this.meetingName = meetingName;
    }

    public List<EventPlace> getPlaces() {
        return Places;
    }

    public void setPlaces(List<EventPlace> places) {
        Places = places;
    }

    public EventPlace getFinalPlace() {
        return FinalPlace;
    }

    public void setFinalPlace(EventPlace finalPlace) {
        FinalPlace = finalPlace;
    }

    public void ConfirmEvent(){

    }
}
