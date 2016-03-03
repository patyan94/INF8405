package com.example.yannd.tp2_inf8405;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by yannd on 2016-02-18.
 */
public class DatabaseManager {
    public static void CreateGroup(String groupName){
        // TODO
    }
    public static Group GetGroup(String groupName)
    {
        // TODO
        return new Group();
    }
    public static void AddGroupMember(String groupName, String username)
    {
        // TODO
    }
    public static UserProfile GetUserProfile(String username)
    {
        // TODO
        return new UserProfile();
    }
    public static void SaveUserProfile(UserProfile profile, Context appContext){
        // TODO
    }
    static boolean userProfileExists(String username){
        // TODO
        return false;
    };
    static boolean groupExists(String groupName){
        // TODO
        return false;
    };
}
