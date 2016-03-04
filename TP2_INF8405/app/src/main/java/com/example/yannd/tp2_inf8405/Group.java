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

    public Group(){
    }

    public Group(String name){
        this.groupName = name;
    }

    public List<UserProfile> getGroupMembers() {
        return groupMembers;
    }

    public void setGroupMembers(List<UserProfile> groupMembers) {
        this.groupMembers = groupMembers;
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
                user.setLocation(member.getLocation().first, member.getLocation().second);
                user.setPreferences(member.getPreferences());
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
