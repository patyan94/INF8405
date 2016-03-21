package com.example.yannd.tp2_inf8405;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.firebase.client.Firebase;

public class LoginPage extends AppCompatActivity {

    private final int SELECT_PHOTO = 1;
    private EditText email, groupName;
    private CheckBox meetingOrganizer;
    private CheckBox[] preferences;
    private Button loginButton, signinButton, setProfilePictureButton;
    private ImageView profilePicture;
    private LinearLayout signinOptions;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        RessourceMonitor.getInstance();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        Intent batteryStatus = registerReceiver(RessourceMonitor.getInstance(), new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        //Initializing the Firebase library and the DataManager singleton at start
        //Important if we want the firebase callbacks in the DataManager to be initialized asap
        Firebase.setAndroidContext(this);
        DataManager.getInstance();

        //Initliazing app context of the calendar manager singleton...
        CalendarManager.getInstance().setApplicationContext(getApplicationContext());

        preferences = new CheckBox[PREFERENCES.NUMBER_OF_PREFERENCES.getValue()];
        meetingOrganizer = (CheckBox)findViewById(R.id.meeting_organizer);
        email = (EditText)findViewById(R.id.email);
        groupName = (EditText)findViewById(R.id.group_name);
        preferences[PREFERENCES.PARK.getValue()] = (CheckBox)findViewById(R.id.park_checkbox);
        preferences[PREFERENCES.BAR.getValue()] = (CheckBox)findViewById(R.id.bar_checkbox);
        preferences[PREFERENCES.RESTAURANT.getValue()] = (CheckBox)findViewById(R.id.restaurant_checkbox);
        preferences[PREFERENCES.LIBRARY.getValue()] = (CheckBox)findViewById(R.id.library_checkbox);
        preferences[PREFERENCES.CAFE.getValue()] = (CheckBox)findViewById(R.id.cafe_checkbox);
        preferences[PREFERENCES.UNIVERSITY.getValue()] = (CheckBox)findViewById(R.id.university_checkbox);
        loginButton = (Button)findViewById(R.id.login_button);
        signinButton = (Button)findViewById(R.id.signin_button);
        signinOptions = (LinearLayout)findViewById(R.id.signin_options);
        setProfilePictureButton = (Button)findViewById(R.id.set_profile_picture_button);
        profilePicture = (ImageView)findViewById(R.id.profilePicture);
        loginButton.setEnabled(false);
        signinButton.setEnabled(false);

        profilePicture.setVisibility(View.GONE);
        email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (email.length() > 2 && groupName.length() > 2) {
                    loginButton.setEnabled(true);
                    signinButton.setEnabled(true);
                } else {
                    loginButton.setEnabled(false);
                    signinButton.setEnabled(false);
                }
            }
        });
        groupName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (groupName.length() > 2 && email.length() > 2) {
                    loginButton.setEnabled(true);
                    signinButton.setEnabled(true);
                } else {
                    loginButton.setEnabled(false);
                    signinButton.setEnabled(false);
                }
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Login();
            }
        });
        signinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Signin();
            }
        });

        setProfilePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(photoPickerIntent, SELECT_PHOTO);
            }
        });
    }

    private void Login() {
        String groupNameString = groupName.getText().toString();
        String emailString = email.getText().toString();

        Group group = DataManager.getInstance().getGroup(groupNameString);
        if(group != null){
            DataManager.getInstance().setCurrentGroup(group);
            UserProfile user = DataManager.getInstance().getUser(emailString);
            if(user != null){

                DataManager.getInstance().setCurrentUser(user);

                //Unchecking all the checkboxes, in case we come back to it
                meetingOrganizer.setChecked(false);
                for(int i = 0; i < preferences.length; ++i){
                   preferences[i].setChecked(false);
                }

                //We start the next activity
                Intent intent = new Intent(this, MeetingPlannerActivity.class);
                startActivity(intent);
            }else{
                Toast.makeText(getApplicationContext(), "This email address isn't registered in " + group.getGroupName() + ". You must sign in first.", Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(getApplicationContext(), "This group doesn't exist", Toast.LENGTH_SHORT).show();
        }
    }

    private void Signin() {

        String groupNameString = groupName.getText().toString();
        String emailString = email.getText().toString();
        boolean isOrganizer = meetingOrganizer.isChecked();

        UserProfile newProfile = new UserProfile(isOrganizer, emailString);

        try {
            newProfile.setUserProfileImage(((BitmapDrawable) profilePicture.getDrawable()).getBitmap());
        }
        catch (Exception e){
            Toast.makeText(getApplicationContext(), "Please select a valid profile picture from your device.", Toast.LENGTH_SHORT).show();
            return;
        }

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

        //We check if the group already exists
        Group group = DataManager.getInstance().getGroup(groupNameString);
        if(group != null){

            UserProfile user = DataManager.getInstance().getUser(emailString);
            if(user != null){
                Toast.makeText(getApplicationContext(), "This email address is already registered in " + group.getGroupName() + ".", Toast.LENGTH_SHORT).show();
                return;
            }else{
                //If the group already exist and this user want to signin as an organizer
                //we can't let that happen since there cannont be more than one
                if(isOrganizer){
                    Toast.makeText(getApplicationContext(), "There's is already an organizer for this group.", Toast.LENGTH_SHORT).show();
                    return;
                }

                //If it does, then we can simply make it our current group and add this new user to it
                DataManager.getInstance().setCurrentGroup(group);
                DataManager.getInstance().addOrUpdateUser(newProfile);
            }

        }else{
            if(isOrganizer){
                //Creating a new group
                DataManager.getInstance().createGroup(groupNameString);
                //Adding the user to the new group
                DataManager.getInstance().addOrUpdateUser(newProfile);
            }else{
                Toast.makeText(getApplicationContext(), "This group doesn't exist. You must be the meeting organizer if you want to create it.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        Login();
    }
    @Override
    public void onBackPressed() {
        String batteryLevelMessage =
                new String("Battery usage : " + String.valueOf(RessourceMonitor.getInstance().GetTotalBatteryUsage()));
        ShowBatteryUsage("Application battery usage", batteryLevelMessage, true);
    }



    void ShowBatteryUsage(String title, String message, boolean leavePage) {
        final boolean leave = leavePage;
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginPage.this);

        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (leave) finish();
            }
        });
        if(leave)
        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // do nothing
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
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
                        profilePicture.setVisibility(View.VISIBLE);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                }
        }
    }
}
