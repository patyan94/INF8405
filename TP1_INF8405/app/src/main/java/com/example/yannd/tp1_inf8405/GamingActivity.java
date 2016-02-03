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
        endpointCells = new ArrayList<CellView>();
        endpointCells.add(new CellView(this, Color.RED, true, new Pair<Integer, Integer>(0,0)));
        endpointCells.add(new CellView(this, Color.BLUE, true, new Pair<Integer, Integer>(2,4)));
        endpointCells.add(new CellView(this, Color.BLUE, true, new Pair<Integer, Integer>(6,7)));
        endpointCells.add(new CellView(this, Color.GREEN, true, new Pair<Integer, Integer>(4,7)));
        endpointCells.add(new CellView(this, Color.CYAN, true, new Pair<Integer, Integer>(6, 1)));

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
                    currentColorDragged = Color.BLACK;
                    return false;
                }

                //This is a flag used to only trigger the MOVE event when the position detected actually changes (Évite les doublons)
                boolean ignoreMoveEvent = (colIdx == pastColIdx && rowIdx == pastRowIdx) ||
                                           Math.abs(colIdx - pastColIdx)>1 ||
                                           Math.abs(rowIdx - pastRowIdx)>1;

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
}
