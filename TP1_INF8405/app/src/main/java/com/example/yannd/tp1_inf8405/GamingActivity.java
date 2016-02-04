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
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Queue;

public class GamingActivity extends AppCompatActivity {

    private int gameHeight;
    private int gameWidth;
    private ArrayList<CellView> endpointCells;
    private int currentColorDragged = Color.BLACK;
    private ArrayList<CellView> selectedCells;
    private int pastColIdx;
    private int pastRowIdx;
    private int currentLevel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gaming);

        selectedCells = new ArrayList<CellView>();

        // Gather the value passed by the mainMenu to know the size of the grid
        Intent intent = getIntent();
        int gridSize = intent.getIntExtra("size", 0); //DefaultValue set at zero.

        gameHeight = gridSize;
        gameWidth = gridSize;

        //Temporary manual initialisation of the endpointCells array. This parameter will be fed depending on the state of the game and the grid size
        currentLevel = 1;
        endpointCells = GetGrid(gridSize , currentLevel);

        //Constructing the game's grid & adding the endpoint cells to it
        this.fillTable();

        //Settings up the event listener for the game's mechanics
        TableLayout gameLayout = (TableLayout) findViewById(R.id.gameLayout);
        gameLayout.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                TableLayout gameLayout = (TableLayout) findViewById(R.id.gameLayout);
                int colWidth = gameLayout.getWidth() / GamingActivity.this.gameWidth;
                int rowHeight = gameLayout.getHeight() / GamingActivity.this.gameHeight;

                int colIdx = (int) (event.getX() / colWidth);
                int rowIdx = (int) (event.getY() / rowHeight);

                //This 'if' serves as protection against dragging in diagonal.
                //Expl : If both the row and col indexes are DIFFERENT from the previous ones this means we moved diagonally. We don't allow it
                if ((pastColIdx != colIdx && pastRowIdx != rowIdx) && currentColorDragged != Color.BLACK) {
                    //currentColorDragged = Color.BLACK;
                    return false;
                }

                //This is a flag used to only trigger the MOVE event when the position detected actually changes (Évite les doublons)
                boolean ignoreMoveEvent = (colIdx == pastColIdx && rowIdx == pastRowIdx) ||
                        Math.abs(colIdx - pastColIdx) > 1 ||
                        Math.abs(rowIdx - pastRowIdx) > 1;

                TableRow row = null;
                CellView cell = null;
                //Checking that the coordinates do, in fact, point to a cell inside the layout
                if (colIdx < GamingActivity.this.gameWidth && rowIdx < GamingActivity.this.gameHeight) {
                    row = (TableRow) gameLayout.getChildAt(rowIdx);
                    cell = (CellView) row.getChildAt(colIdx);
                }

                if (cell != null) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            if (cell.isEndpoint() && !cell.isUsed()) {
                                GamingActivity.this.currentColorDragged = cell.getColor();
                                cell.setUsed(true);
                                selectedCells.add(cell);
                                pastRowIdx = rowIdx;
                                pastColIdx = colIdx;
                            }
                            return true;
                        case MotionEvent.ACTION_UP:
                            GamingActivity.this.currentColorDragged = Color.BLACK;
                            GamingActivity.this.clearSelectedCells();
                            CheckVictory();
                            return true;
                        case MotionEvent.ACTION_MOVE:

                            //We ignore move events when we previously detect that it is the same cell that has been triggered
                            //This allows us to detect if we have to remove cells from the path (if the player is stepping back from the path)
                            if (!ignoreMoveEvent) {
                                //Linking two endpoints of the same color
                                if (cell.isEndpoint() && !cell.isUsed() && cell.getColor() == GamingActivity.this.currentColorDragged) {
                                    cell.setUsed(true);
                                    GamingActivity.this.selectedCells.clear(); //Clearing the temp array since the link is now permanent
                                    GamingActivity.this.currentColorDragged = Color.BLACK; //Meaning we stop dragging any color since it's been linked
                                    return true;
                                }

                                //Drawing of the path under certain conditions
                                if (!cell.isEndpoint() && !cell.isUsed()) {
                                    if (GamingActivity.this.currentColorDragged != Color.BLACK) { //If the currentColorBeing dragged is black this means there's no endpoint being dragged right now
                                        cell.setColor(GamingActivity.this.currentColorDragged);
                                        cell.setUsed(true);
                                        selectedCells.add(cell);
                                        cell.invalidate(); //Forces cell to re-draw itself
                                        pastRowIdx = rowIdx;
                                        pastColIdx = colIdx;

                                        //Setting up the position of the preceding cell inside the new cell
                                        cell.setPrecedingCellPosition(GamingActivity.this.selectedCells.get(selectedCells.size() - 2).getPosition());

                                        //Setting up the position of the next cell inside the previous cell
                                        CellView previousCell = GamingActivity.this.selectedCells.get(selectedCells.size() - 2);
                                        previousCell.setNextCellPosition(cell.getPosition());
                                        previousCell.invalidate(); // Forcing the prev. cell to re-draw if we want a corner to appear

                                        return true;
                                    }
                                } else {
                                    //This part is used to remove the path when the user "backs-up" over previously painted cells.
                                    if (selectedCells.contains(cell) && !cell.isEndpoint()) {
                                        int count = selectedCells.size() - selectedCells.indexOf(cell);
                                        while (count-- > 0) {
                                            CellView last = selectedCells.get(selectedCells.size() - 1);
                                            last.setUsed(false);
                                            last.setColor(Color.BLACK);
                                            last.invalidate();
                                            selectedCells.remove(last);
                                        }
                                        return true;
                                    }
                                    //This condition is used to detect if we cross another color already placed
                                    if (cell.getColor() != Color.BLACK) {
                                        return true;
                                    }
                                }
                            } else {
                                return false;
                            }
                            return true;
                        default:
                            return false;
                    }
                }
                return false;
            }
        });


        Button btnReset = (Button) findViewById(R.id.buttonResetBoard);
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TableLayout gameLayout = (TableLayout) findViewById(R.id.gameLayout);

                for (int i = 0; i < gameHeight; i++) {
                    TableRow row = (TableRow) gameLayout.getChildAt(i);

                    for (int j = 0; j < gameWidth; j++) {
                        CellView cell = (CellView) row.getChildAt(j);
                        cell.setUsed(false);
                        if (!cell.isEndpoint()) {
                            cell.setColor(Color.BLACK);
                        }
                        cell.emptyOldCellPositions();
                        cell.invalidate();
                    }
                }
            }
        });
        Button btnNextLevel = (Button) findViewById(R.id.buttonNextLevel);
        btnNextLevel.setVisibility(View.INVISIBLE);
        btnNextLevel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GoToNextLevel();
        }
        });
        Button btnRestartLevel = (Button) findViewById(R.id.buttonRestartLevel);
        btnRestartLevel.setVisibility(View.INVISIBLE);
        btnRestartLevel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RestartLevel();
            }
        });

    }

    //Constructing the game's grid & adding the endpoint cells to it
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
    }


    private void clearSelectedCells(){
        for(CellView c : this.selectedCells){
            c.setUsed(false);
            if(!c.isEndpoint()){
                c.setColor(Color.BLACK);
            }
            c.emptyOldCellPositions();
            c.invalidate();
        }
        selectedCells.clear();
    }

    private ArrayList<CellView> GetGrid(int size , int level){
     ArrayList<CellView > grid = new ArrayList<CellView>();
        if(size == 7)
        {
            if(level ==1)

            {
                grid.add(new CellView(this, Color.BLUE, true, new Pair<Integer, Integer>(1,0)));
                grid.add(new CellView(this, Color.BLUE, true, new Pair<Integer, Integer>(6,0)));
                grid.add(new CellView(this, Color.RED, true, new Pair<Integer, Integer>(2,2)));
                grid.add(new CellView(this, Color.RED, true, new Pair<Integer, Integer>(3,4)));
                grid.add(new CellView(this, Color.YELLOW, true, new Pair<Integer, Integer>(4,2)));
                grid.add(new CellView(this, Color.YELLOW, true, new Pair<Integer, Integer>(5,4)));
                grid.add(new CellView(this, Color.GREEN, true, new Pair<Integer, Integer>(5,0)));
                grid.add(new CellView(this, Color.GREEN, true, new Pair<Integer, Integer>(5,5)));
                grid.add(new CellView(this, Color.rgb(255, 153, 0), true, new Pair<Integer, Integer>(5,1)));
                grid.add(new CellView(this, Color.rgb(255, 153, 0), true, new Pair<Integer, Integer>(4,4)));
            }
            if(level == 2)
            {

                grid.add(new CellView(this, Color.BLUE, true, new Pair<Integer, Integer>(5,0)));
                grid.add(new CellView(this, Color.BLUE, true, new Pair<Integer, Integer>(3,6)));
                grid.add(new CellView(this, Color.RED, true, new Pair<Integer, Integer>(4,6)));
                grid.add(new CellView(this, Color.RED, true, new Pair<Integer, Integer>(6,6)));
                grid.add(new CellView(this, Color.YELLOW, true, new Pair<Integer, Integer>(5,1)));
                grid.add(new CellView(this, Color.YELLOW, true, new Pair<Integer, Integer>(2,6)));
                grid.add(new CellView(this, Color.GREEN, true, new Pair<Integer, Integer>(4,5)));
                grid.add(new CellView(this, Color.GREEN, true, new Pair<Integer, Integer>(6,5)));
                grid.add(new CellView(this, Color.rgb(255, 153, 0), true, new Pair<Integer, Integer>(5,3)));//orange
                grid.add(new CellView(this, Color.rgb(255, 153, 0), true, new Pair<Integer, Integer>(2,5)));
                grid.add(new CellView(this, Color.LTGRAY, true, new Pair<Integer, Integer>(1,1)));
                grid.add(new CellView(this, Color.LTGRAY, true, new Pair<Integer, Integer>(5,2)));
                grid.add(new CellView(this, Color.rgb(139,69,19), true, new Pair<Integer, Integer>(1,5)));//brown
                grid.add(new CellView(this, Color.rgb(139,69,19), true, new Pair<Integer, Integer>(2,2)));

            }
            if(level == 3)
            {

                grid.add(new CellView(this, Color.BLUE, true, new Pair<Integer, Integer>(5,0)));
                grid.add(new CellView(this, Color.BLUE, true, new Pair<Integer, Integer>(4,3)));
                grid.add(new CellView(this, Color.RED, true, new Pair<Integer, Integer>(5,3)));
                grid.add(new CellView(this, Color.RED, true, new Pair<Integer, Integer>(6,6)));
                grid.add(new CellView(this, Color.YELLOW, true, new Pair<Integer, Integer>(2,2)));
                grid.add(new CellView(this, Color.YELLOW, true, new Pair<Integer, Integer>(2,4)));
                grid.add(new CellView(this, Color.GREEN, true, new Pair<Integer, Integer>(3,1)));
                grid.add(new CellView(this, Color.GREEN, true, new Pair<Integer, Integer>(4,4)));
                grid.add(new CellView(this, Color.rgb(255, 153, 0), true, new Pair<Integer, Integer>(5,1)));//orange
                grid.add(new CellView(this, Color.rgb(255, 153, 0), true, new Pair<Integer, Integer>(5,4)));
                grid.add(new CellView(this, Color.LTGRAY, true, new Pair<Integer, Integer>(2,1)));
                grid.add(new CellView(this, Color.LTGRAY, true, new Pair<Integer, Integer>(4,5)));

            }
        }
        if(size == 8)
        {
            if(level ==1)

            {
                grid.add(new CellView(this, Color.BLUE, true, new Pair<Integer, Integer>(1,5)));
                grid.add(new CellView(this, Color.BLUE, true, new Pair<Integer, Integer>(1,7)));
                grid.add(new CellView(this, Color.RED, true, new Pair<Integer, Integer>(0,4)));
                grid.add(new CellView(this, Color.RED, true, new Pair<Integer, Integer>(5,4)));
                grid.add(new CellView(this, Color.YELLOW, true, new Pair<Integer, Integer>(3,0)));
                grid.add(new CellView(this, Color.YELLOW, true, new Pair<Integer, Integer>(3,6)));
                grid.add(new CellView(this, Color.GREEN, true, new Pair<Integer, Integer>(1,0)));
                grid.add(new CellView(this, Color.GREEN, true, new Pair<Integer, Integer>(2,2)));
                grid.add(new CellView(this, Color.rgb(255, 153, 0), true, new Pair<Integer, Integer>(4,3)));//orange
                grid.add(new CellView(this, Color.rgb(255, 153, 0), true, new Pair<Integer, Integer>(5,2)));//orange
                grid.add(new CellView(this, Color.rgb(152,251,152) , true, new Pair<Integer, Integer>(3,5)));//light green
                grid.add(new CellView(this, Color.rgb(152,251,152) , true, new Pair<Integer, Integer>(4,2)));//light green
                grid.add(new CellView(this, Color.rgb(139,69,19), true, new Pair<Integer, Integer>(1,6)));//brown
                grid.add(new CellView(this, Color.rgb(139,69,19), true, new Pair<Integer, Integer>(2,5)));//brown
                grid.add(new CellView(this, Color.LTGRAY, true, new Pair<Integer, Integer>(7,7)));
                grid.add(new CellView(this, Color.LTGRAY, true, new Pair<Integer, Integer>(2,7)));

            }
            if(level == 2)
            {

                grid.add(new CellView(this, Color.BLUE, true, new Pair<Integer, Integer>(5,5)));
                grid.add(new CellView(this, Color.BLUE, true, new Pair<Integer, Integer>(6,2)));
                grid.add(new CellView(this, Color.RED, true, new Pair<Integer, Integer>(4,3)));
                grid.add(new CellView(this, Color.RED, true, new Pair<Integer, Integer>(6,1)));
                grid.add(new CellView(this, Color.YELLOW, true, new Pair<Integer, Integer>(0,5)));
                grid.add(new CellView(this, Color.YELLOW, true, new Pair<Integer, Integer>(3,5)));
                grid.add(new CellView(this, Color.GREEN, true, new Pair<Integer, Integer>(1,6)));
                grid.add(new CellView(this, Color.GREEN, true, new Pair<Integer, Integer>(3,6)));
                grid.add(new CellView(this, Color.rgb(255, 153, 0), true, new Pair<Integer, Integer>(1,4)));//orange
                grid.add(new CellView(this, Color.rgb(255, 153, 0), true, new Pair<Integer, Integer>(6,3)));
                grid.add(new CellView(this, Color.LTGRAY, true, new Pair<Integer, Integer>(0,4)));
                grid.add(new CellView(this, Color.LTGRAY, true, new Pair<Integer, Integer>(0,6)));
                grid.add(new CellView(this, Color.rgb(152,251,152) , true, new Pair<Integer, Integer>(2,2)));//light green
                grid.add(new CellView(this, Color.rgb(152,251,152) , true, new Pair<Integer, Integer>(4,2)));//light green

            }
            if(level == 3)
            {

                grid.add(new CellView(this, Color.BLUE, true, new Pair<Integer, Integer>(1,1)));
                grid.add(new CellView(this, Color.BLUE, true, new Pair<Integer, Integer>(6,2)));
                grid.add(new CellView(this, Color.RED, true, new Pair<Integer, Integer>(5,2)));
                grid.add(new CellView(this, Color.RED, true, new Pair<Integer, Integer>(4,4)));
                grid.add(new CellView(this, Color.YELLOW, true, new Pair<Integer, Integer>(1,5)));
                grid.add(new CellView(this, Color.YELLOW, true, new Pair<Integer, Integer>(5,3)));
                grid.add(new CellView(this, Color.GREEN, true, new Pair<Integer, Integer>(0,3)));
                grid.add(new CellView(this, Color.GREEN, true, new Pair<Integer, Integer>(3,0)));
                grid.add(new CellView(this, Color.rgb(255, 153, 0), true, new Pair<Integer, Integer>(1,3)));//orang
                grid.add(new CellView(this, Color.rgb(255, 153, 0), true, new Pair<Integer, Integer>(3,4)));
                grid.add(new CellView(this, Color.LTGRAY, true, new Pair<Integer, Integer>(1,4)));
                grid.add(new CellView(this, Color.LTGRAY, true, new Pair<Integer, Integer>(4,0)));
                grid.add(new CellView(this, Color.rgb(139,69,19), true, new Pair<Integer, Integer>(1,2)));//brown
                grid.add(new CellView(this, Color.rgb(139,69,19), true, new Pair<Integer, Integer>(3,3)));//brown
                grid.add(new CellView(this, Color.rgb(152,251,152) , true, new Pair<Integer, Integer>(2,5)));//light green
                grid.add(new CellView(this, Color.rgb(152,251,152) , true, new Pair<Integer, Integer>(5,4)));//light green



            }
        }
        return grid;
    }

    private void CheckVictory()
    {
        TableLayout gameLayout = (TableLayout)findViewById(R.id.gameLayout);
        boolean ended = true;
        for(int i = 0; i < gameWidth; ++i)
        {
            for(int j = 0; j < gameHeight; ++j)
            {
                TableRow row = (TableRow) gameLayout.getChildAt(i);
                CellView cell = (CellView) row.getChildAt(j);
                if(!cell.isUsed()) {
                    ended = false;
                    break;
                }
            }
        }
        if(ended)
        {
            if(currentLevel <=3) {

                Button btnNextLevel = (Button) findViewById(R.id.buttonNextLevel);
                Button btnRestartLevel = (Button) findViewById(R.id.buttonRestartLevel);
                btnNextLevel.setVisibility(View.VISIBLE);
                btnRestartLevel.setVisibility(View.VISIBLE);
            }
        }
    }
    private void GoToNextLevel()
    {
        if(currentLevel >= 3){
            Intent intent = new Intent(GamingActivity.this, MainMenu.class);
            GamingActivity.this.startActivity(intent);
        }

        endpointCells = GetGrid(gameHeight, ++currentLevel);
        clearSelectedCells();
        fillTable();

        Button btnNextLevel = (Button) findViewById(R.id.buttonNextLevel);
        Button btnRestartLevel = (Button) findViewById(R.id.buttonRestartLevel);
        btnNextLevel.setVisibility(View.INVISIBLE);
        btnRestartLevel.setVisibility(View.INVISIBLE);
    }
    private void RestartLevel()
    {
        Button btnNextLevel = (Button) findViewById(R.id.buttonNextLevel);
        Button btnRestartLevel = (Button) findViewById(R.id.buttonRestartLevel);
        btnNextLevel.setVisibility(View.INVISIBLE);
        btnRestartLevel.setVisibility(View.INVISIBLE);

        endpointCells = GetGrid(gameHeight, currentLevel);
        clearSelectedCells();
        fillTable();
    }
}
