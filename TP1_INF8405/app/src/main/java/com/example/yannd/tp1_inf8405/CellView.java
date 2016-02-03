package com.example.yannd.tp1_inf8405;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.util.Pair;
import android.view.View;

/**
 * Created by yannd on 2016-01-28.
 */
public class CellView extends View {

    private int color;
    private boolean isEndpoint;
    private boolean isUsed;
    private Pair<Integer, Integer> position;
    private Paint cellPaint;
    private Pair<Integer, Integer> precedingCellPosition;
    private Pair<Integer, Integer> nextCellPosition;

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

    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        cellPaint.reset();

        cellPaint.setColor(this.color);
        if(this.isEndpoint){
            canvas.drawCircle(getWidth() / 2, getHeight() / 2, getHeight() / 3, cellPaint);
        }else if(this.isUsed){
            int drawOffset = (int) (0.333 * getWidth());

            //This first condition checks if the current cell is a "corner" between two other cells
            if(nextCellPosition.first != -1 && precedingCellPosition.first != -1 &&
               Math.abs(nextCellPosition.first - precedingCellPosition.first) == 1 &&
               Math.abs(nextCellPosition.second - precedingCellPosition.second) == 1){

                // +--
                // |

                // AND

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

                // AND

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

                //Else means we're a regular drawn cell (rectangular)
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
