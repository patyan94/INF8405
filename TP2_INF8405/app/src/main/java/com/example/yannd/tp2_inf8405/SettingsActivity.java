package com.example.yannd.tp2_inf8405;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.firebase.client.Firebase;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    private final int SELECT_PHOTO = 1;
    private CheckBox meetingOrganizer;
    private CheckBox[] preferences;
    private Button setProfilePictureButton, saveButton;
    private ImageView profilePicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Firebase.setAndroidContext(this);
        DataManager.getInstance();

        //Initliazing app context of the calendar manager singleton...
        CalendarManager.getInstance().setApplicationContext(getApplicationContext());

        preferences = new CheckBox[PREFERENCES.NUMBER_OF_PREFERENCES.getValue()];
        meetingOrganizer = (CheckBox)findViewById(R.id.meeting_organizer);
        preferences[PREFERENCES.PARK.getValue()] = (CheckBox)findViewById(R.id.park_checkbox);
        preferences[PREFERENCES.BAR.getValue()] = (CheckBox)findViewById(R.id.bar_checkbox);
        preferences[PREFERENCES.RESTAURANT.getValue()] = (CheckBox)findViewById(R.id.restaurant_checkbox);
        preferences[PREFERENCES.LIBRARY.getValue()] = (CheckBox)findViewById(R.id.library_checkbox);
        preferences[PREFERENCES.CAFE.getValue()] = (CheckBox)findViewById(R.id.cafe_checkbox);
        preferences[PREFERENCES.UNIVERSITY.getValue()] = (CheckBox)findViewById(R.id.university_checkbox);
        setProfilePictureButton = (Button)findViewById(R.id.set_profile_picture_button);
        saveButton = (Button)findViewById(R.id.save_button);
        profilePicture = (ImageView)findViewById(R.id.profilePicture);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Save();
                Toast.makeText(getApplicationContext(), "Saved", Toast.LENGTH_SHORT).show();
            }
        });
        setProfilePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(photoPickerIntent, SELECT_PHOTO);
            }
        });
        UserProfile currentUser = DataManager.getInstance().getCurrentUser();
        profilePicture.setImageBitmap(currentUser.getUserProfileImage());

        List<String> prefs = currentUser.getPreferences();

        for(CheckBox prefCheckBox : preferences){
            prefCheckBox.setChecked(prefs.contains(prefCheckBox.getText().toString()));
        }
    }

    private void Save() {

        boolean isOrganizer = meetingOrganizer.isChecked();

        UserProfile newProfile = DataManager.getInstance().getCurrentUser();

        newProfile.setUserProfileImage(((BitmapDrawable)profilePicture.getDrawable()).getBitmap());

        //We check that there's at least two preferences selected
        List<String> userPreferences = new ArrayList<String>();
        for(int i = 0; i < preferences.length; ++i){
            if(preferences[i].isChecked()){
                userPreferences.add(preferences[i].getText().toString());
            }
        }
        if(userPreferences.size() < 3){
            Toast.makeText(getApplicationContext(), "Please specify at least three locations preferences.", Toast.LENGTH_SHORT).show();
            return;
        }
        newProfile.setPreferences(userPreferences);
        newProfile.setMeetingOrganizer(isOrganizer);
        DataManager.getInstance().addOrUpdateUser(newProfile);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch(requestCode) {
            case SELECT_PHOTO:
                if(resultCode == RESULT_OK){
                    try {
                        final Uri imageUri = imageReturnedIntent.getData();
                        final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                        final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                        profilePicture.setImageBitmap(selectedImage);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                }
        }
    }

}
