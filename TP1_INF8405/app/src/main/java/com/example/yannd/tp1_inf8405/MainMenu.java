package com.example.yannd.tp1_inf8405;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

public class MainMenu extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        // Button to leave the application
        TextView txtView = (TextView) findViewById(R.id.quitButton);
        txtView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

        // Button to play with the 7x7 grids
        txtView = (TextView) findViewById(R.id.playButton7);
        txtView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainMenu.this, LevelSelection.class);
                GameData.getInstance().setGridSize(7);
                MainMenu.this.startActivity(intent);
            }
        });

        // Button to play with the 8x8 grids
        txtView = (TextView) findViewById(R.id.playButton8);
        txtView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainMenu.this, LevelSelection.class);
                GameData.getInstance().setGridSize(8);
                MainMenu.this.startActivity(intent);
            }
        });
    }
}


