package Model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.util.List;

/**
 * Created by yannd on 2016-03-25.
 */
public class UserProfile {
    private String username;
    private String encodedUserProfileImage;
    private List<String> preferedSeriesTitles;
    private boolean sharePosition;


    //@JsonIgnore
    public void setUserProfileImage(Bitmap image){
        ByteArrayOutputStream bYtE = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, bYtE); //On pourrait essayer d'autre CompressFormat si jamais le décodage marche pas.
        //image.recycle();
        byte[] byteArray = bYtE.toByteArray();
        // TODO: Integrate firebase
        String imageFile = "";//com.firebase.client.utilities.Base64.encodeBytes(byteArray);
        encodedUserProfileImage = imageFile;
    }
    //@JsonIgnore
    public Bitmap getUserProfileImage(){
        try {
            // TODO: Integrate firebase
            byte[] decodedByte = null;//com.firebase.client.utilities.Base64.decode(mEncodedUserProfileImage);
            Bitmap bitmap= BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
            return bitmap;
        } catch(Exception e) {
            return null;
        }
    }

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

    public List<String> getPreferedSeriesTitles() {
        return preferedSeriesTitles;
    }

    public void setPreferedSeriesTitles(List<String> preferedSeriesTitles) {
        this.preferedSeriesTitles = preferedSeriesTitles;
    }

    public boolean isSharePosition() {
        return sharePosition;
    }

    public void setSharePosition(boolean sharePosition) {
        this.sharePosition = sharePosition;
    }
}
