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

    public CellView(Context context, int color, boolean isEndpoint, Pair<Integer, Integer> position)
    {
        super(context);
        this.color = color;
        this.isEndpoint = isEndpoint;
        this.isUsed = isEndpoint; //The only case the cell is "used" already at construction is if it is an endpoint, otherwise it's going to switch while being dragged on
        this.position = position;

        this.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                //Get the parent activity
                GamingActivity parent = (GamingActivity) getContext();
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        Log.d("Appl.", CellView.this.position.first + ", " + CellView.this.position.second + " : DOWN");
                        parent.setCurrentColorDragged(CellView.this.color);
                        return true;
                    case MotionEvent.ACTION_UP:
                        Log.d("Appl.", CellView.this.position.first + ", " + CellView.this.position.second + " : UP" );
                        parent.setCurrentColorDragged(Color.BLACK);
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        //If another endpoint cell is currently being dragged
                        Log.d("Appl.", CellView.this.position.first + ", " + CellView.this.position.second + " : MOVE");
                        if (parent.getCurrentColorDragged() != Color.BLACK && !CellView.this.isEndpoint) {
                           //Toast.makeText(getContext(), "USED!" + CellView.this.position, Toast.LENGTH_SHORT).show();
                            CellView.this.isUsed = true;
                            CellView.this.color = parent.getCurrentColorDragged();
                            CellView.this.invalidate(); //Force the re-draw
                        }
                        return true;
                    case MotionEvent.ACTION_CANCEL:
                        Log.d("Appl.", CellView.this.position.first + ", " + CellView.this.position.second + " : CANCEL" );
                        Toast.makeText(getContext(), "CANCEL", Toast.LENGTH_SHORT).show();
                        return true;
                    case MotionEvent.ACTION_OUTSIDE:
                        Log.d("Appl.", CellView.this.position.first + ", " + CellView.this.position.second + " : OUTSIDE");
                        Toast.makeText(getContext(), "OUTSIDE", Toast.LENGTH_SHORT).show();
                        return true;
                }
                return false;
            }
        });
    }

    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        Paint paint = new Paint();

        paint.setColor(this.color);
        if(this.isEndpoint){
            canvas.drawCircle(getWidth() / 2, getHeight() / 2, getHeight() / 3, paint);
        }else if(this.isUsed){
            Toast.makeText(getContext(), "USED!", Toast.LENGTH_SHORT).show();
            canvas.drawRect(0, 0, getWidth()-8, getHeight()-8, paint);
        }
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.BLACK);
        canvas.drawRect(4, 4, getWidth()-4, getHeight()-4, paint);
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
