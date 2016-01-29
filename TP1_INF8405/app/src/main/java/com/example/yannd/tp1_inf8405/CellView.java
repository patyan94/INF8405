package com.example.yannd.tp1_inf8405;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Parcelable;
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

    public CellView(Context context, int color, boolean isEndpoint, Pair<Integer, Integer> position)
    {
        super(context);
        this.color = color;
        this.isEndpoint = isEndpoint;
        this.isUsed = isEndpoint; //The only case the cell is "used" already at construction is if it is an endpoint, otherwise it's going to switch while being dragged on
        this.position = position;
    }

    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        Paint paint = new Paint();
        paint.setColor(this.color);
        if(this.isEndpoint){
            canvas.drawCircle(getWidth() / 2, getHeight() / 2, getHeight() / 3, paint);
        }
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.BLACK);
        canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
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
}
