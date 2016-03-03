package com.example.yannd.tp2_inf8405;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by yannd on 2016-03-03.
 */
public class Group {
    private String groupName;
    private HashSet<UserProfile> groupMembers;

    public HashSet<UserProfile> getGroupMembers() {
        return groupMembers;
    }

    public void setGroupMembers(HashSet<UserProfile> groupMembers) {
        this.groupMembers = groupMembers;
    }

    public String getGroupName() {
        return groupName;

    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}
