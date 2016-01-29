package com.example.yannd.tp1_inf8405;

import android.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class GamingActivity extends AppCompatActivity {

    private int gameHeight;
    private int gameWidth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gaming);

        TableLayout gameLayout = (TableLayout)findViewById(R.id.gameLayout);
        gameLayout.removeAllViews();
        gameHeight = 7;
        gameWidth = 7;

        for(int i = 0; i < gameHeight; ++i)
        {
            TableRow row = new TableRow(this);
            row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            for(int j = 0; j < gameWidth; ++j)
            {
                //if(i!=j)continue;
                CellView cell = new CellView(this);
                cell.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
                cell.setPadding(5,5,5,5);
                row.addView(cell);
            }
            gameLayout.addView(row);
        }
    }
}
