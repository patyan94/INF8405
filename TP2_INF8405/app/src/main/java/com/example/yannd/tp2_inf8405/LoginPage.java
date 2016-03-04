package com.example.yannd.tp2_inf8405;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.firebase.client.Firebase;

public class LoginPage extends AppCompatActivity {

    private EditText email, groupName;
    private CheckBox meetingOrganizer;
    private CheckBox[] preferences;
    private Button loginButton, signinButton;
    private LinearLayout signinOptions;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        //Initializing the Firebase library at start
        Firebase.setAndroidContext(this);
        DataManager.getInstance();

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
        loginButton.setEnabled(false);
        signinButton.setEnabled(false);

        email.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void afterTextChanged(Editable s) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if( email.length() > 2 && groupName.length() > 2){
                    loginButton.setEnabled(true);
                    signinButton.setEnabled(true);
                }else{
                    loginButton.setEnabled(false);
                    signinButton.setEnabled(false);
                }

            }
        });
        groupName.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void afterTextChanged(Editable s) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if( groupName.length() > 2 && email.length() > 2){
                    loginButton.setEnabled(true);
                    signinButton.setEnabled(true);
                }else{
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
                Toast.makeText(getApplicationContext(), "This user isn't in " + group.getGroupName(), Toast.LENGTH_SHORT).show();
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

        //We check that there's at least two preferences selected
        List<String> userPreferences = new ArrayList<String>();
        for(int i = 0; i < preferences.length; ++i){
            if(preferences[i].isChecked()){
                userPreferences.add(preferences[i].getText().toString());
            }
        }
        if(userPreferences.size() < 2){
            Toast.makeText(getApplicationContext(), "Please specify at least two locations preferences.", Toast.LENGTH_SHORT).show();
            return;
        }
        newProfile.setPreferences(userPreferences);

        //We check if the group already exists
        Group group = DataManager.getInstance().getGroup(groupNameString);
        if(group != null){

            //If the groud already exist and this user want to signin as an organizer
            //we can't let that happen since there cannont be more than one
            if(isOrganizer){
                Toast.makeText(getApplicationContext(), "There's is already an organizer for this group.", Toast.LENGTH_SHORT).show();
                return;
            }

            //If it does, then we can simply make it our current group and add this new user to it
            DataManager.getInstance().setCurrentGroup(group);
            DataManager.getInstance().addOrUpdateUser(newProfile);

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

}
