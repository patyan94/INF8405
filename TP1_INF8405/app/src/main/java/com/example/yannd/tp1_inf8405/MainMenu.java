package com.example.yannd.tp1_inf8405;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainMenu extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        // Capture our button from layout
        Button button = (Button) findViewById(R.id.quitButton);
        // Register the onClick listener with the implementation above
        button.setOnClickListener(quitBtnListener);

        button = (Button) findViewById(R.id.playButton);
        button.setOnClickListener(startBtnListener);
    }

    private View.OnClickListener quitBtnListener = new View.OnClickListener() {
        public void onClick(View v) {
            finish();
        }
    };

    private View.OnClickListener startBtnListener = new View.OnClickListener() {
        public void onClick(View v) {
            //reference : http://stackoverflow.com/questions/4186021/how-to-start-new-activity-on-button-click

            Intent intent = new Intent(MainMenu.this, GamingActivity.class);
            //intent.putExtra("key", value); //Optional parameters
            MainMenu.this.startActivity(intent);
        }
    };
}


