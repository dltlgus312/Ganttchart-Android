package com.leesihyeon.ganttchart;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.Calendar;

public abstract class OnTheBarItem {

    private Calendar sdate;
    private Calendar edate;

    private float left, top, right, bottom;

    private boolean clickable = true;


    private final int HIGHLIGHTCOUNT = 100;

    private int count;

    private Paint highLightColor;


    public OnTheBarItem(){
        sdate = Calendar.getInstance();
        edate = Calendar.getInstance();

        highLightColor = new Paint();
        highLightColor.setColor(Color.CYAN);
    }



    void draw(float left, float top, float right, float bottom, Canvas canvas){
        setdate(sdate, edate);

        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;

        if (0<count){
            canvas.drawRect(left, top, right, bottom, highLightColor);
            count--;
        }
        drawItem(left, top, right, bottom, canvas);
    }

    boolean onClickPosition(float x, float y){

        if (clickable && x<right && x>left && y<bottom && y>top){
            return true;
        }
        return false;
    }





    protected abstract void setdate(Calendar sdate, Calendar edate);

    protected abstract void drawItem(float left, float top, float right, float bottom, Canvas canvas);




    public Calendar getSdate() {
        return sdate;
    }

    public void setSdate(Calendar sdate) {
        this.sdate = sdate;
    }

    public Calendar getEdate() {
        return edate;
    }

    public void setEdate(Calendar edate) {
        this.edate = edate;
    }

    public boolean isClickable() {
        return clickable;
    }

    public void setClickable(boolean clickable) {
        this.clickable = clickable;
    }

    public float getLeft() {
        return left;
    }

    public void setLeft(float left) {
        this.left = left;
    }

    public float getTop() {
        return top;
    }

    public void setTop(float top) {
        this.top = top;
    }

    public float getRight() {
        return right;
    }

    public void setRight(float right) {
        this.right = right;
    }

    public float getBottom() {
        return bottom;
    }

    public void setBottom(float bottom) {
        this.bottom = bottom;
    }

    public void setHighlightCount() {
        this.count = HIGHLIGHTCOUNT;
    }
}
