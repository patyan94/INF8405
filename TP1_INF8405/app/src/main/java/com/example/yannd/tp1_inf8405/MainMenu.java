package com.example.yannd.tp1_inf8405;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;

public class MainMenu extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        // Capture our button from layout
        Button button = (Button) findViewById(R.id.quitButton);
        // Register the onClick listener with the implementation above
        button.setOnClickListener(quitBtnListener);

        button = (Button) findViewById(R.id.playButton7);
        button.setOnClickListener(startBtnListener7);

        button = (Button) findViewById(R.id.playButton8);
        button.setOnClickListener(startBtnListener8);
    }

    private View.OnClickListener quitBtnListener = new View.OnClickListener() {
        public void onClick(View v) {
            finish();
        }
    };

    private View.OnClickListener startBtnListener7 = new View.OnClickListener() {
        public void onClick(View v) {
            //reference : http://stackoverflow.com/questions/4186021/how-to-start-new-activity-on-button-click

            Intent intent = new Intent(MainMenu.this, GamingActivity.class);
            intent.putExtra("size", 7); //Optional parameters : Size of the grid
            MainMenu.this.startActivity(intent);
        }
    };
    private View.OnClickListener startBtnListener8 = new View.OnClickListener() {
        public void onClick(View v) {
            //reference : http://stackoverflow.com/questions/4186021/how-to-start-new-activity-on-button-click

            Intent intent = new Intent(MainMenu.this, GamingActivity.class);
            intent.putExtra("size", 8); //Optional parameters : Size of the grid
            MainMenu.this.startActivity(intent);
        }
    };
}


