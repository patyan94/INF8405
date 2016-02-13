package com.example.yannd.tp1_inf8405;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by yannd on 2016-01-28.
 *
 * This class the activity to select a level for a selected grid size
 */
public class LevelSelection extends AppCompatActivity {

    @Override
    protected void onStart() {
        super.onStart();
        setContentView(R.layout.activity_level_selection);

        int gridSize = GameData.getInstance().getGridSize();

        // Refresh hint of current level
        int currentLevelUnlocked = -1;
        if (gridSize == 7) {
            currentLevelUnlocked = GameData.getInstance().getLevelsUnlocked77();
            ((TextView)findViewById(R.id.gridSizeHint)).setText("7 x 7");
        } else if (gridSize == 8)
        {
            currentLevelUnlocked = GameData.getInstance().getLevelsUnlocked88();
            ((TextView)findViewById(R.id.gridSizeHint)).setText("8 x 8");
        }

        // Button to play the level 1
        Button button;
        button = (Button) findViewById(R.id.playButtonLevel1);
        button.setEnabled(currentLevelUnlocked >= 1);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(LevelSelection.this, GamingActivity.class);
                intent.putExtra("level", 1);
                LevelSelection.this.startActivity(intent);
            }
        });

        // Button to play the level 2
        button = (Button) findViewById(R.id.playButtonLevel2);
        button.setEnabled(currentLevelUnlocked >= 2);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(LevelSelection.this, GamingActivity.class);
                intent.putExtra("level", 2);
                LevelSelection.this.startActivity(intent);
            }
        });

        // Button to play the level 3
        button = (Button) findViewById(R.id.playButtonLevel3);
        button.setEnabled(currentLevelUnlocked >= 3);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(LevelSelection.this, GamingActivity.class);
                intent.putExtra("level", 3);
                LevelSelection.this.startActivity(intent);
            }
        });

    }
}
