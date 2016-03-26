package Model;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;

/**
 * Created by yannd on 2016-03-25.
 */
public class DatabaseInterface {

    private final String path = "https://finalprojectmobile.firebaseio.com/";
    private Firebase firebaseRef;
    private UserData userData = null;
    private AuthData authData = null;

    //region getters/setters
    public UserData getUserData() {
        return userData;
    }
    public AuthData getAuthData() {
        return authData;
    }

    public void setAuthData(AuthData authData) {
        this.authData = authData;
    }

    public Firebase GetDatabaseMainNode(){
        return firebaseRef;
    }
    //endregion

    //region singleton
    private DatabaseInterface(){
        firebaseRef = new Firebase(path);
    }
    private static DatabaseInterface instance = null;
    public static DatabaseInterface Instance(){
        if(instance == null){
            instance = new DatabaseInterface();
        }
        return instance;
    }
    //endregion

    //region usermanagement
    public void SetCurrentUser(UserData userData){
        this.userData = userData;
        this.userData.setProvider(authData.getProvider());
    }

    public void SaveCurrentUserProfile(){
        firebaseRef.child("users").child(this.authData.getUid()).child("UserData").setValue(userData);
    }

    public void DeleteCurrentUser(){
        this.userData = null;
        firebaseRef = null;
    }
    //endregion

    //region usernameManagement
    public void AddNewUsername(String username){
        firebaseRef.child("usernames").child(username).setValue(username);

    }
    public void DeleteUsername(String username){
        //TODO
    }
    public Firebase GetUsernamesNode(){
        return firebaseRef.child("usernames");
    }
    //endregion
}
