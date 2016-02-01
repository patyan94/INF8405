package com.example.yannd.tp1_inf8405;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Parcelable;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

/**
 * Created by yannd on 2016-01-28.
 */
public class CellView extends View {

    private int color;
    private boolean isEndpoint;
    private boolean isUsed;
    private Pair<Integer, Integer> position;
    private Paint cellPaint;

    public CellView(Context context, int color, boolean isEndpoint, Pair<Integer, Integer> position)
    {
        super(context);
        this.color = color;
        this.isEndpoint = isEndpoint;
        this.isUsed = false;
        this.position = position;
        cellPaint = new Paint();
    }

    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        cellPaint.reset();

        cellPaint.setColor(this.color);
        if(this.isEndpoint){
            canvas.drawCircle(getWidth() / 2, getHeight() / 2, getHeight() / 3, cellPaint);
        }else if(this.isUsed){
            canvas.drawRect(0, 0, getWidth()-8, getHeight()-8, cellPaint);
        }
        cellPaint.setStyle(Paint.Style.STROKE);
        cellPaint.setColor(Color.BLACK);
        canvas.drawRect(4, 4, getWidth()-4, getHeight()-4, cellPaint);
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
}
