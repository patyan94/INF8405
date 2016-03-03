package com.example.yannd.tp2_inf8405;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MeetingPlannerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting_planner);
    }

    public void CreateEvent(){
        //TODO creates an event to the next available time, according to everyone's known location
    }
}
