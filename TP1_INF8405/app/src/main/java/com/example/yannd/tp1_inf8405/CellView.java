package com.example.yannd.tp1_inf8405;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Pair;
import android.view.View;

/**
 * Created by yannd on 2016-01-28.
 *
 * This class represent each cell of a game's grid. We exteend View, so taht you can overload onDraw, in order to
 * draw the correct shape when a mtion is detected on the parent layout container.
 */
public class CellView extends View {

    private int color;
    private boolean isEndpoint;
    private boolean isUsed;
    private Pair<Integer, Integer> position;
    private Paint cellPaint;
    private Pair<Integer, Integer> precedingCellPosition;
    private Pair<Integer, Integer> nextCellPosition;

    //Constructor
    public CellView(Context context, int color, boolean isEndpoint, Pair<Integer, Integer> position)
    {
        super(context);
        this.color = color;
        this.isEndpoint = isEndpoint;
        this.isUsed = false;
        this.position = position;
        this.cellPaint = new Paint();
        this.precedingCellPosition = new Pair<>(-1, -1);
        this.nextCellPosition = new Pair<>(-1, -1);
    }

    //Main part of this class, this method defines if and how the current cell will be drawn on the screen
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        cellPaint.reset();
        int drawOffset = (int) (0.333 * getWidth());

        cellPaint.setColor(this.color);

        //If the cell is an endpoint, it's a dot on start, and a more particular shape if it has been linked to another cell
        if(this.isEndpoint){
            canvas.drawCircle(getWidth() / 2, getHeight() / 2, getHeight() / 3, cellPaint);
            if(isUsed){

                //This Pair represent the position right before (if this endpoint closes the path) or right after (if this endpoint opens the path)
                Pair<Integer, Integer> cellReferencePosition;

                if(nextCellPosition.first != -1){
                    cellReferencePosition = nextCellPosition;
                }else{
                    cellReferencePosition = precedingCellPosition;
                }

                //If the endpoint is on the same row as its preceding cell
                if(cellReferencePosition.first == position.first){
                    //If the preceding cell is on the next column on the right, the rectangle will be facing right
                    if(cellReferencePosition.second > position.second){
                        canvas.drawRect(getWidth()/2, drawOffset, getWidth(), getHeight() - drawOffset, cellPaint);
                    }else{
                        canvas.drawRect(0, drawOffset, getWidth()/2, getHeight() - drawOffset, cellPaint);
                    }
                    //Else, the endpoint is on the same column as its preceding cell
                }else{
                    //If the preceding cell is on the next row below, the rectangle will be facing down
                    if(cellReferencePosition.first > position.first){
                        canvas.drawRect(drawOffset, getHeight() / 2, getWidth() - drawOffset, getHeight(), cellPaint);
                    }else{
                        canvas.drawRect(drawOffset, 0, getWidth() - drawOffset, getHeight() / 2, cellPaint);
                    }
                }
            }

        // If the cell isn't an endpoint, it will be drawn only if it's being traced or has been linked
        // It deals with different shapes, either a straight vertical or horizontal line, or one of the four type of corners
        }else if(this.isUsed){

            //This first condition checks if the current cell is a "corner" between two other cells
            if(nextCellPosition.first != -1 && precedingCellPosition.first != -1 &&
               Math.abs(nextCellPosition.first - precedingCellPosition.first) == 1 &&
               Math.abs(nextCellPosition.second - precedingCellPosition.second) == 1){

                // +--
                // |

                // OR

                //   |
                // --+
                if( (nextCellPosition.first < precedingCellPosition.first && nextCellPosition.second > precedingCellPosition.second)
                    || (nextCellPosition.first > precedingCellPosition.first && nextCellPosition.second < precedingCellPosition.second)){

                    if( (position.second < nextCellPosition.second ) || (position.second < precedingCellPosition.second) ){

                        canvas.drawCircle(getWidth() / 2, getHeight() / 2, getHeight() / 6, cellPaint);
                        canvas.drawRect(getWidth() / 2, drawOffset, getWidth(), getHeight() - drawOffset, cellPaint);
                        canvas.drawRect(drawOffset, getHeight() / 2, getWidth() - drawOffset, getHeight(), cellPaint);

                    }else{

                        canvas.drawCircle(getWidth() / 2, getHeight() / 2, getHeight() / 6, cellPaint);
                        canvas.drawRect(0, drawOffset, getWidth() / 2, getHeight() - drawOffset, cellPaint);
                        canvas.drawRect(drawOffset, 0, getWidth() - drawOffset, getHeight() / 2, cellPaint);
                    }

                } else

                // --+
                //   |

                // OR

                // |
                // +--
                if( (nextCellPosition.first > precedingCellPosition.first && nextCellPosition.second > precedingCellPosition.second)
                    || (nextCellPosition.first < precedingCellPosition.first && nextCellPosition.second < precedingCellPosition.second)){

                    if( (position.second > nextCellPosition.second) || (position.second > precedingCellPosition.second)  ){

                        canvas.drawCircle(getWidth() / 2, getHeight() / 2, getHeight() / 6, cellPaint);
                        canvas.drawRect(0, drawOffset, getWidth() / 2, getHeight() - drawOffset, cellPaint);
                        canvas.drawRect(drawOffset, getHeight() / 2, getWidth() - drawOffset, getHeight(), cellPaint);

                    }else{

                        canvas.drawCircle(getWidth() / 2, getHeight() / 2, getHeight() / 6, cellPaint);
                        canvas.drawRect(drawOffset, 0, getWidth() - drawOffset, getHeight() / 2, cellPaint);
                        canvas.drawRect(getWidth() / 2, +drawOffset, getWidth(), getHeight() - drawOffset, cellPaint);
                    }

                }

            //Else means we're a regular drawn cell (rectangular) horitzontal or vertical
            } else {

                    //If the preceding cell is on the same row we draw horizontally streched
                if(precedingCellPosition.first == position.first) {
                    canvas.drawRect(0, drawOffset, getWidth(), getHeight()-drawOffset, cellPaint);
                } else {
                    canvas.drawRect(drawOffset, 0, getWidth()-drawOffset, getHeight(), cellPaint);
                }
            }
        }

        //Contour of the cell (Grid)
        cellPaint.setStyle(Paint.Style.STROKE);
        cellPaint.setColor(Color.BLACK);
        canvas.drawRect(0, 0, getWidth(), getHeight(), cellPaint);
    }

    //This function is used to make sure the cells are always a square
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int size = 0;
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();

        //Sets the cells as square
        if (width > height) {
            size = height;
        } else {
            size = width;
        }
        setMeasuredDimension(size, size);
    }

    /*
     * Getters and setters
     */

    public Pair<Integer, Integer> getPosition(){
        return this.position;
    }

    public boolean isEndpoint(){
        return this.isEndpoint;
    }

    public int getColor(){
        return this.color;
    }

    public void setColor(int color){
        this.color = color;
    }

    public void setUsed(boolean isUsed){
        this.isUsed = isUsed;
    }

    public boolean isUsed(){
        return this.isUsed;
    }

    public void setPrecedingCellPosition(Pair<Integer, Integer> pos){
        this.precedingCellPosition = pos;
    }

    public void setNextCellPosition(Pair<Integer, Integer> pos){
        this.nextCellPosition = pos;
    }

    public Pair<Integer, Integer> getPrecedingCellPosition(){
        return precedingCellPosition;
    }

    public Pair<Integer, Integer> getNextCellPosition(){
        return nextCellPosition;
    }

    public void emptyOldCellPositions(){
        this.nextCellPosition = new Pair<>(-1, -1);
        this.precedingCellPosition = new Pair<>(-1, -1);
    }
}
