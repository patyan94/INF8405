package Model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.ByteArrayOutputStream;

/**
 * Created by yannd on 2016-04-03.
 */
public class FriendRequest {
    private String username;
    private String encodedUserProfileImage;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEncodedUserProfileImage() {
        return encodedUserProfileImage;
    }

    public void setEncodedUserProfileImage(String encodedUserProfileImage) {
        this.encodedUserProfileImage = encodedUserProfileImage;
    }

    @JsonIgnore
    public void setUserProfileImage(Bitmap image){
        ByteArrayOutputStream bYtE = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, bYtE);
        //image.recycle();
        byte[] byteArray = bYtE.toByteArray();
        String imageFile = com.firebase.client.utilities.Base64.encodeBytes(byteArray);
        encodedUserProfileImage = imageFile;
    }
    @JsonIgnore
    public Bitmap getUserProfileImage(){
        try {
            byte[] decodedByte = com.firebase.client.utilities.Base64.decode(encodedUserProfileImage);
            Bitmap bitmap= BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
            return bitmap;
        } catch(Exception e) {
            return null;
        }
    }
}
