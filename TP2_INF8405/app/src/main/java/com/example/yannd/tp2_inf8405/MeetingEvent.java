package com.example.yannd.tp2_inf8405;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.ContactsContract;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.android.gms.location.places.Place;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Observable;

/**
 * Created by yannd on 2016-03-03.
 */
public class MeetingEvent extends Observable {
    String meetingName;
    String description;
    String encodedPhoto;
    List<EventPlace> Places = new ArrayList<EventPlace>();
    EventPlace FinalPlace = null;
    Calendar date;

    @JsonIgnore
    public void setDecodedPhoto(Bitmap image){
        ByteArrayOutputStream bYtE = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.WEBP, 100, bYtE); //On pourrait essayer d'autre CompressFormat si jamais le décodage marche pas.
        //image.recycle();
        byte[] byteArray = bYtE.toByteArray();
        String imageFile = com.firebase.client.utilities.Base64.encodeBytes(byteArray);
        encodedPhoto = imageFile;
        setChanged();
    }
    @JsonIgnore
    public Bitmap getGetDecodedImage(){
        try {
            byte[] decodedByte = com.firebase.client.utilities.Base64.decode(encodedPhoto);
            Bitmap bitmap= BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
            return bitmap;
        } catch(Exception e) {
            return null;
        }
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        setChanged();
    }

    public String getEncodedPhoto() {
        return encodedPhoto;
    }

    public void setEncodedPhoto(String encodedPhoto) {
        this.encodedPhoto = encodedPhoto;
    }

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
        ChooseFinalPlace();
        setChanged();
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

    public Calendar getDate(){
        return date;
    }

    public void setDate(Calendar newDate){
        date = newDate;
    }

    public String getMeetingName() {
        return meetingName;
    }

    public void setMeetingName(String meetingName) {
        this.meetingName = meetingName;
        setChanged();
    }

    public List<EventPlace> getPlaces() {
        return Places;
    }

    public void setPlaces(List<EventPlace> places) {
        Places = places;
        setChanged();
    }

    public EventPlace getFinalPlace() {
        return FinalPlace;
    }

    public void setFinalPlace(EventPlace finalPlace) {
        FinalPlace = finalPlace;
        setChanged();
    }

    public void ConfirmEvent(){

    }
}
