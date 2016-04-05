package Model;

import android.location.Location;

import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;

/**
 * Created by yannd on 2016-03-25.
 */
public class DatabaseInterface {

    private final String path = "https://finalprojectmobile.firebaseio.com/";
    private Firebase firebaseRef;
    GeoQuery geoQuery;
    private GeoFire geofireRef;
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
        geofireRef = new GeoFire(firebaseRef.child("positions"));
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

    public void AddNewUSer(String username){
        firebaseRef.child("user_ids").child(authData.getUid()).setValue(username);
    }

    public void SaveCurrentUserProfile() {
        firebaseRef.child("users").child(userData.getUsername()).child(this.authData.getUid()).child("UserData").setValue(userData);
    }

    public void DeleteCurrentUser(){
        this.userData = null;
        firebaseRef = null;
    }

    public UserData GetCurrentUserData(){
        return this.userData;
    }

    public Firebase GetUsersNode(){
        return firebaseRef.child("users");
    }

    public Firebase GetUserIDNode(){
        return firebaseRef.child("user_ids");
    }
    //endregion

    //region positionmanagement
    public void UpdateUserPosition(Location position){
        geofireRef.setLocation(userData.getUsername(), new GeoLocation(position.getLatitude(), position.getLongitude()));
    }

    public void StartListeningToCloseUsers(Location position, double radius, GeoQueryEventListener listener){
        if(geoQuery != null) return;
        geoQuery =  geofireRef.queryAtLocation(new GeoLocation(position.getLatitude(), position.getLongitude()), radius);
        geoQuery.addGeoQueryEventListener(listener);
    }

    public void UpdateGeoQueryPosition(Location position){
        geoQuery.setCenter(new GeoLocation(position.getLatitude(), position.getLongitude()));
    }
    //endregion

    //region friendsManagement
    public Firebase GetReceivedFriendRequestsNode(){
        return firebaseRef.child("friend_requests").child(this.userData.getUsername());
    }

    public Firebase GetFriendListNode(){
        return firebaseRef.child("users").child(this.userData.getUsername()).child("friends");
    }

    public void SendFriendRequest(String username){
        firebaseRef.child("friend_requests").child(username).child(this.userData.getUsername()).setValue(this.userData.getUsername());
    }

    public void CancelFriendRequest(String username){
        firebaseRef.child("friend_requests").child(username).child(this.userData.getUsername()).removeValue();
    }

    public void AcceptFriendRequest(String username){
        firebaseRef.child("friend_requests").child(this.userData.getUsername()).child(username).removeValue();
        firebaseRef.child("users").child(userData.getUsername()).child("friends").child(username).setValue(username);
        firebaseRef.child("users").child(username).child("friends").child(userData.getUsername()).setValue(userData.getUsername());
    }

    public void RefuseFriendRequest(String username){
        firebaseRef.child("friend_requests").child(this.userData.getUsername()).child(username).removeValue();
    }

    public void DeleteFriend(String username){
        firebaseRef.child("users").child(userData.getUsername()).child("friends").child(username).removeValue();
        firebaseRef.child("users").child(username).child("friends").child(userData.getUsername()).removeValue();
    }
    //endregion

    //region seriesManagement
    public Firebase GetSeriesSuggestionNode(){
        return firebaseRef.child("serie_suggestions").child(this.userData.getUsername());
    }
    public void SendSerieSuggestion(String username, String suggestionID){
        // Serie with a list of people who suggested it
        firebaseRef.child("serie_suggestions").child(username).child(suggestionID).push().setValue(this.userData.getUsername());
    }

    public Firebase GetSeriesListNode(){
        return firebaseRef.child("users").child(this.userData.getUsername()).child("series");
    }

    public void AcceptSerieSuggestion(String suggestionID){
        firebaseRef.child("serie_suggestions").child(userData.getUsername()).child(suggestionID).removeValue();
        firebaseRef.child("users").child(userData.getUsername()).child("series").child(suggestionID).setValue(suggestionID);
    }

    public void RefuseSerieSuggestion(String suggestionID){
        firebaseRef.child("serie_suggestions").child(userData.getUsername()).child(suggestionID).removeValue();
    }

    public void DeleteSerie(String suggestionID){
        firebaseRef.child("users").child(userData.getUsername()).child("series").child(suggestionID).removeValue();
    }
    //endregion
}
