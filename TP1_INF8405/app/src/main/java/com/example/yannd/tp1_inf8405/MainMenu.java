package com.example.yannd.tp1_inf8405;

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
        button.setOnClickListener(mCorkyListener);
    }

    private View.OnClickListener mCorkyListener = new View.OnClickListener() {
        public void onClick(View v) {
            finish();
        }
    };
}


