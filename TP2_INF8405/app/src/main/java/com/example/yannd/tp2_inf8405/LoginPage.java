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

import java.util.BitSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginPage extends AppCompatActivity {

    private EditText email, groupName;
    private CheckBox groupExists, meetingOrganizer;
    private CheckBox[] preferences;
    private Button loginButton, signinButton;
    private LinearLayout signinOptions;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        preferences = new CheckBox[PREFERENCES.NUMBER_OF_PREFERENCES.getValue()];
        meetingOrganizer = (CheckBox)findViewById(R.id.meeting_organizer);
        email = (EditText)findViewById(R.id.email);
        groupName = (EditText)findViewById(R.id.group_name);
        groupExists = (CheckBox)findViewById(R.id.group_exists);
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
        signinOptions.setVisibility(View.INVISIBLE);

        meetingOrganizer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckEntryFields();
            }
        });
        email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                CheckEntryFields();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        groupName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                CheckEntryFields();
            }

            @Override
            public void afterTextChanged(Editable s) {

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
        if(!groupExists.isChecked())
        {
            DatabaseManager.CreateGroup(groupName.getText().toString());
        }
        DatabaseManager.AddGroupMember(groupName.getText().toString(), email.getText().toString());
        UserProfile.setCurrentUser(DatabaseManager.GetUserProfile(email.getText().toString()));
        UserProfile.setCurrentGroupName(groupName.getText().toString());
        Intent intent = new Intent(this, MeetingPlannerActivity.class);
        startActivity(intent);
    }

    private void Signin() {
        UserProfile newProfile = new UserProfile();
        newProfile.setUsername(email.getText().toString());
        newProfile.setMeetingOrganizer(meetingOrganizer.isChecked());
        BitSet userPreferences = new BitSet(PREFERENCES.NUMBER_OF_PREFERENCES.getValue());
        for(int i = 0; i < preferences.length; ++i){
            if(preferences[i].isChecked()){
                userPreferences.or(PREFERENCES.values()[i].getBitsetValue());
            }
        }
        newProfile.setPreferences(userPreferences);
        DatabaseManager.SaveUserProfile(newProfile, getApplicationContext());

        Login();
    }

    boolean IsEmailValid()
    {
        String regex = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email.getText());
        return matcher.matches();
    }
    boolean IsGroupNameValid(){
        return groupName.getText().toString().length() >= 3;
    }

    void CheckEntryFields() {
        boolean validEmail = IsEmailValid();
        boolean userProfileExists =  validEmail && DatabaseManager.userProfileExists(email.toString());
        boolean groupExists = DatabaseManager.groupExists(groupName.toString());
        boolean validGroupName = IsGroupNameValid();

        signinOptions.setVisibility(userProfileExists ? View.INVISIBLE : View.VISIBLE);
        loginButton.setVisibility(userProfileExists ? View.VISIBLE : View.INVISIBLE);
        signinButton.setEnabled(validEmail && validGroupName && (groupExists || meetingOrganizer.isChecked()));
        loginButton.setEnabled(validEmail && validGroupName&& (groupExists || meetingOrganizer.isChecked()));
    }
}
