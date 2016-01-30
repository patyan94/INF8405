package com.example.yannd.tp1_inf8405;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;

public class GamingActivity extends AppCompatActivity {

    private int gameHeight;
    private int gameWidth;
    private ArrayList<CellView> endpointCells;
    private int currentColorDragged = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gaming);

        // Gather the value passed by the mainMenu to know the size of the grid
        Intent intent = getIntent();
        int gridSize = intent.getIntExtra("size", 0); //DefaultValue set at zero.

        gameHeight = gridSize;
        gameWidth = gridSize;

        //Temporary manual initialisation of the endpointCells array. This parameter will be fed depending on the state of the game and the grid size
        endpointCells = new ArrayList<CellView>();
        endpointCells.add(new CellView(this, Color.RED, true, new Pair<Integer, Integer>(0,0)));
        endpointCells.add(new CellView(this, Color.BLUE, true, new Pair<Integer, Integer>(2,4)));
        endpointCells.add(new CellView(this, Color.GREEN, true, new Pair<Integer, Integer>(4,7)));
        endpointCells.add(new CellView(this, Color.CYAN, true, new Pair<Integer, Integer>(6, 1)));

        this.fillTable();
    }

    private void fillTable(){
        TableLayout gameLayout = (TableLayout)findViewById(R.id.gameLayout);
        gameLayout.removeAllViews();
        for(int i = 0; i < gameHeight; ++i)
        {
            TableRow row = new TableRow(this);
            row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            for(int j = 0; j < gameWidth; ++j)
            {
                CellView cell = null;
                Pair<Integer, Integer> pos;
                boolean endpointFound = false;
                for(CellView c: endpointCells){
                    pos = c.getPosition();
                    if(pos.first == i && pos.second == j){
                        cell = c;
                        endpointFound = true;
                    }
                }
                if(!endpointFound){
                    cell = new CellView(this, Color.BLACK, false, new Pair<Integer, Integer>(i, j));
                }
                cell.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
                cell.setPadding(5,5,5,5);
                row.addView(cell);
            }
            gameLayout.addView(row);
        }

        gameLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                TableLayout gameLayout = (TableLayout)findViewById(R.id.gameLayout);

                int colWidth = gameLayout.getWidth() / GamingActivity.this.gameWidth;
                int rowHeight = gameLayout.getHeight() / GamingActivity.this.gameHeight;

                int colIdx = (int) (event.getX() / colWidth);
                int rowIdx = (int) (event.getY() / rowHeight);

                TableRow row = (TableRow) gameLayout.getChildAt(rowIdx);
                CellView cell = (CellView) row.getChildAt(colIdx);

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if(cell.isEndpoint()){
                            GamingActivity.this.currentColorDragged = cell.getColor();
                            cell.setUsed(true);
                        }
                        return true;
                    case MotionEvent.ACTION_UP:
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        if(!cell.isEndpoint() && cell.getColor() == Color.BLACK){
                            if(GamingActivity.this.currentColorDragged != Color.BLACK){ //If the currentColorBeing dragged is black this means there's no endpoint being dragged right now
                                cell.setColor(GamingActivity.this.currentColorDragged);
                                cell.setUsed(true);
                                cell.invalidate(); //Forces cell to re-draw itself
                            }
                        }
                        return true;
                    default:
                        return false;
                }
            }
        });
    }

    public int getCurrentColorDragged(){
        return this.currentColorDragged;
    }

    public void setCurrentColorDragged(int color){
        //Set to Color.BLACK means not dragging currently any endpoint cell
        this.currentColorDragged = color;
    }
}
