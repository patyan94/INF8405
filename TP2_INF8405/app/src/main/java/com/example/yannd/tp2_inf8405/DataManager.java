package com.example.yannd.tp2_inf8405;

import android.content.Context;
import android.util.Log;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yannd on 2016-02-18.
 */
public class DataManager {

    private static DataManager instance = null;
    private Firebase firebaseRef;
    private Group currentGroup;
    private UserProfile currentUser;
    private List<Group> groupList;

    private DataManager(){
        firebaseRef = new Firebase("https://inf8405-tp2.firebaseio.com/");
        currentGroup = new Group();
        currentUser = new UserProfile();
        groupList = new ArrayList<Group>();

        // Event listener used to keep a 1 to 1 relation between the groupList and the one in the Firebase database
        firebaseRef.child("groups").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                List<Group> newList = new ArrayList<Group>();

                //For each group in the DB
                for (DataSnapshot ds : dataSnapshot.getChildren()) {

                    //We 'extract' the group object
                    Group tempGroup = ds.getValue(Group.class);

                    //We store all the groups in this array
                    newList.add(tempGroup);

                    //If the current group concerned has changed, we update it's content (Members and groupEvents)
                    if (tempGroup.getGroupName().equalsIgnoreCase(currentGroup.getGroupName())) {
                        currentGroup.setGroupMembers(tempGroup.getGroupMembers());

                        //For each meeting in the "new" version of the currentGroup, we add or update it to the currentGroup
                        for(MeetingEvent me : tempGroup.getGroupEvents()){
                            currentGroup.addOrUpdateEvent(me);

                            //So here, bascially, we should 'react' to the changes to each MeetingEvent. Here or in 'addOrUpdateEvent' or further down,
                            //in order to detect if a user action is required depending on the state or the meeting. Ex : a new event is added and the user must vote.
                            //or even, all the users have voted and the organiser must analyse all the votes in order to finalize the meeting adding.
                        }
                    }
                }

                //We empty the list and replace it with the newest version
                groupList.clear();
                groupList.addAll(newList);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.d("ERROR", firebaseError.getMessage());
            }
        });
    }

    public static DataManager getInstance(){
        if(instance == null)
            instance = new DataManager();

        return instance;
    }

    public void createGroup(String groupName){
        Group group = new Group(groupName);
        this.currentGroup = group;
        firebaseRef.child("groups").child(groupName).setValue(group);
    }

    public Group getGroup(String groupName){

        if(groupName.equalsIgnoreCase(currentGroup.getGroupName()))
            return currentGroup;

        for(Group g : groupList){
            if(g.getGroupName().equalsIgnoreCase(groupName)){
                return g;
            }
        }
        //This name isn't present in the database
        return null;
    }

    public UserProfile getUser(String username)
    {
        if(currentGroup.getGroupMembers() != null){
            for(UserProfile up : currentGroup.getGroupMembers()){
                if(up.getUsername().equalsIgnoreCase(username)){
                    return up;
                }
            }
        }
        return null;
    }

    public void addOrUpdateUser(UserProfile user){

        currentGroup.addOrUpdateGroupMember(user);

        //Sync with DB
        firebaseRef.child("groups").child(currentGroup.getGroupName()).setValue(currentGroup);
    }

    public void addOrUpdateEvent(MeetingEvent event){

        currentGroup.addOrUpdateEvent(event);

        //Sync with DB
        firebaseRef.child("groups").child(currentGroup.getGroupName()).setValue(currentGroup);
    }

    public Group getCurrentGroup()
    {
        return this.currentGroup;
    }

    public void setCurrentGroup(Group group)
    {
        this.currentGroup = group;
    }

    public UserProfile getCurrentUser(){
        return this.currentUser;
    }

    public void setCurrentUser(UserProfile user)
    {
        this.currentUser = user;
    }

}
