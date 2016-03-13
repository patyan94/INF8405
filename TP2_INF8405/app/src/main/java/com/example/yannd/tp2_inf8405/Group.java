package com.example.yannd.tp2_inf8405;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by yannd on 2016-03-03.
 */
public class Group {
    private String groupName;
    private List<UserProfile> groupMembers;
    private List<MeetingEvent> groupEvents;

    public List<MeetingEvent> getGroupEvents() {
        if(groupEvents == null)
        {
            groupEvents = new ArrayList<>();
        }
        return groupEvents;
    }

    public void setGroupEvents(List<MeetingEvent> groupEvents) {
        this.groupEvents = groupEvents;
    }

    public Group(){  }

    public Group(String name){
        this.groupName = name;
    }

    public List<UserProfile> getGroupMembers() {
        return groupMembers;
    }

    public void setGroupMembers(List<UserProfile> groupMembers) {
        this.groupMembers = groupMembers;
    }

    public void addOrUpdateEvent(MeetingEvent event){

        if(groupEvents == null){
            groupEvents = new ArrayList<>();
        }

        for(MeetingEvent me : groupEvents){
            if(me.getMeetingName().equalsIgnoreCase(event.getMeetingName())){
                me.setPlaces(event.getPlaces());
                me.setFinalPlace(event.getFinalPlace());
                return;
            }
        }

        groupEvents.add(event);
    }

    public void addOrUpdateGroupMember(UserProfile member){

        if(groupMembers == null){
            groupMembers = new ArrayList<UserProfile>();
        }

        //We check if the member already exists in the group
        for(UserProfile user : groupMembers){
            //If it does, we update it instead of add another entry in the list
            if(user.getUsername().equalsIgnoreCase(member.getUsername())){
                user.setMeetingOrganizer(member.isMeetingOrganizer());
                user.setLatitude(member.getLatitude());
                user.setLongitude(member.getLongitude());
                user.setPreferences(member.getPreferences());
                user.setAvailabilities(member.getAvailabilities());
                return;
            }
        }
        //If we get here, we didn't returned earlier
        groupMembers.add(member);
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}
