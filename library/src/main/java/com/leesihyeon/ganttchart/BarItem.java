package com.leesihyeon.ganttchart;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public abstract class BarItem {

    /*
     *
     *  Main Value
     * */

    private int percent;
    private String color;
    private String title;
    private Calendar sdate;
    private Calendar edate;

    private List<OnTheBarItem> onTheBarItems;

    public BarItem() {
        background = new Paint();
        barColor = new Paint();
        textPaint = new Paint();
        strokePaint = new Paint();
        modifyPaint = new Paint();

        textColor = Color.WHITE;
        highlightColor = Color.YELLOW;

        barColor.setStrokeWidth(3);

        background.setColor(0xbbf0f0f0);

        textPaint.setAntiAlias(true);
        textPaint.setColor(textColor);

        strokePaint.setAntiAlias(true);
        strokePaint.setColor(Color.BLACK);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(4);

        modifyPaint.setColor(0x88f5f5dc);
        modifyPaint.setStrokeWidth(4);

        highLightPaint = new Paint();
        highLightPaint.setColor(Color.CYAN);


        depthArrow = 6;
    }

    public BarItem(boolean division) {
        this.division = division;
    }





    /*
     *
     * Draw
     * */

    private final int startTextPosition = 50;

    private final int HIGHLIGHTCOUNT = 100;

    private Paint barColor;
    private Paint background;
    private Paint textPaint;
    private Paint strokePaint;
    private Paint modifyPaint;
    private Paint highLightPaint;
    private int textColor;

    private float titleTextSize;
    private float titleTextWidth;

    private float percentTextSize;
    private int highlightColor;

    private float height;

    private float percentage;

    private BarItem parents;

    private List<Integer> arrowList;

    private int lastArrow;

    private int depthArrow;

    private int highLightCount;

    private float left;
    private float top;
    private float right;
    private float bottom;

    private RectF percentArc;
    private RectF moveTaskArc;
    private RectF sDateRect;
    private RectF eDateRect;



    void drawBar(float left, float top, float right, float bottom, Canvas canvas) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;

        percentage = (right - left) / 100f;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (0 < highLightCount) {
                canvas.drawRoundRect(left - 3, top - 3, right + 3, bottom + 3, 10, 10, highLightPaint);
                highLightCount--;
            }
            canvas.drawRoundRect(left, top, right, bottom, 10, 10, background);
            canvas.drawRoundRect(left, top, left + percentage * percent, bottom, 10, 10, barColor);
        } else {
            if (0 < highLightCount) {
                canvas.drawRect(left - 3, top - 3, right + 3, bottom + 3, highLightPaint);
                highLightCount--;
            }
            canvas.drawRect(left, top, right, bottom, background);
            canvas.drawRect(left, top, left + percentage * percent, bottom, barColor);
        }

        if (barLongClicked) drawModify(canvas);
    }

    void drawTitle(Canvas canvas) {
        if (sdate == null) return;

        if (titleTextSize == 0) calculation();
        if (percentDraw) drawPercent(canvas);

        canvas.drawText(title, startTextPosition, ((top + bottom) / 2) + titleTextSize * 0.4f, strokePaint);
        canvas.drawText(title, startTextPosition, ((top + bottom) / 2) + titleTextSize * 0.4f, textPaint);
    }

    void drawArrow(float x, float y, Canvas canvas) {

        if (x < left) {
            canvas.drawLine(left, bottom - 10, x - depthArrow * 10, bottom - 10, barColor);
            canvas.drawLine(x - depthArrow * 10, bottom - 10, x - depthArrow * 10, y, barColor);
            canvas.drawLine(x - depthArrow * 10, y, x, y, barColor);
        } else {
            canvas.drawLine(left, bottom - 10, left - depthArrow * 10, bottom - 10, barColor);
            canvas.drawLine(left - depthArrow * 10, bottom - 10, left - depthArrow * 10, y, barColor);
            canvas.drawLine(left - depthArrow * 10, y, x, y, barColor);
        }


        canvas.drawLine(x - 10, y - 10, x, y, barColor);
        canvas.drawLine(x - 10, y + 10, x, y, barColor);
    }

    void drawModify(Canvas canvas) {
        drawModifyPercent(canvas);
        drawModifyMove(canvas);
        drawModifyDate(canvas);
    }

    void drawModifyPercent(Canvas canvas) {
        percentArc = new RectF(left - titleTextSize + percentage * percent,
                top - titleTextSize * 2, left + titleTextSize + percentage * percent, top);

        canvas.drawArc(percentArc, 0, 360, false, modifyPaint);
        canvas.drawLine(percentArc.centerX(), top, percentArc.centerX(), bottom, modifyPaint);
    }

    void drawModifyDate(Canvas canvas) {
        sDateRect = new RectF(left, top, left + titleTextSize, bottom);
        eDateRect = new RectF(right - titleTextSize, top, right, bottom);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            canvas.drawRoundRect(sDateRect, 10, 10, modifyPaint);
            canvas.drawRoundRect(eDateRect, 10, 10, modifyPaint);
        } else {
            canvas.drawRect(sDateRect, modifyPaint);
            canvas.drawRect(eDateRect, modifyPaint);
        }
    }

    void drawModifyMove(Canvas canvas) {
        moveTaskArc = new RectF((left + right) / 2 - titleTextSize + 10, (top + bottom) / 2 - titleTextSize + 10,
                (left + right) / 2 + titleTextSize - 10, (top + bottom) / 2 + titleTextSize - 10);

        canvas.drawArc(moveTaskArc, 0, 360, false, modifyPaint);
    }

    void drawPercent(Canvas canvas) {
        Paint paint = new Paint();
        paint.setTextSize(percentTextSize);

        if (percent == 100) {
            paint.setColor(highlightColor);
        } else {
            paint.setColor(textColor);
        }
        canvas.drawText(percent + "%", right + (height / 3), bottom, paint);
    }





    /*
     *
     * Click Event
     * */

    private boolean division = false;
    private boolean barLongClicked = false;
    private boolean titleClicked = false;
    private boolean percentClicked = false;
    private boolean moveClicked = false;
    private boolean sDateClicked = false;
    private boolean eDateClicked = false;
    private boolean percentDraw = true;
    private boolean clickable = true;
    private boolean modifyClick = false;

    private Calendar centerDate;

    boolean clickPosition(float x, float y) {

        // OnTheBarDraw Click Event
        if (this.onTheBarItems != null && this.onTheBarItems.size() > 0 && onTheBarDrawClickListener != null) {
            List<OnTheBarItem> list = new ArrayList<>();
            for (OnTheBarItem b : this.onTheBarItems) {
                if (b.onClickPosition(x, y)) {
                    list.add(b);
                }
            }
            if (list.size() > 0) {
                onTheBarDrawClickListener.itemSelect(this, list);
                return true;
            }
        }

        // Bar Click Event
        if (x < right && x > left && y < bottom && y > top) {
            if (onBarClickListener != null) onBarClickListener.itemSelect(this);
            return true;
        }

        // Title Click Event
        else if (x > startTextPosition && x < startTextPosition + titleTextWidth && y < bottom && y > top) {
            titleClicked = true;
            return true;
        }

        return false;
    }

    boolean longClickRef(float x, float y) {
        if (isClickable() && x < right && x > left && y < bottom && y > top) {
            return true;
        } else {
            return false;
        }
    }

    void setLongClick() {
        barLongClicked = true;
        modifyClick = true;
        textPaint.setColor(Color.CYAN);
    }

    void closeLongClick() {
        barLongClicked = false;
        modifyClick = true;
        textPaint.setColor(textColor);
    }

    boolean modifyClickPosition(float x, float y) {
        if (x > percentArc.left && x < percentArc.right && y > percentArc.top && y < percentArc.bottom) {
            percentClicked = true;
        } else {
            percentClicked = false;
        }

        if (x > moveTaskArc.left && x < moveTaskArc.right && y > moveTaskArc.top && y < moveTaskArc.bottom) {
            moveClicked = true;
        } else {
            moveClicked = false;
        }

        if (x > sDateRect.left && x < sDateRect.right && y > sDateRect.top && y < sDateRect.bottom) {
            sDateClicked = true;
        } else {
            sDateClicked = false;
        }

        if (x > eDateRect.left && x < eDateRect.right && y > eDateRect.top && y < eDateRect.bottom) {
            eDateClicked = true;
        } else {
            eDateClicked = false;
        }

        if (percentClicked || moveClicked || sDateClicked || eDateClicked) modifyClick = true;
        else modifyClick = false;

        return modifyClick;
    }

    boolean isModifyClick() {
        return modifyClick;
    }



    /*
     *
     * ABSTRACT
     * */

    protected abstract void updatePercent(int percent);

    protected abstract void updateStartDate(Calendar calendar);

    protected abstract void updateEndDate(Calendar calendar);




    /*
     *
     * Service
     * */

    boolean isPercentClicked() {
        return percentClicked;
    }

    void setPercentClicked(boolean percentClicked) {
        this.percentClicked = percentClicked;
    }

    boolean isMoveClicked() {
        return moveClicked;
    }

    void setMoveClicked(boolean moveClicked) {
        this.moveClicked = moveClicked;
    }

    boolean issDateClicked() {
        return sDateClicked;
    }

    void setsDateClicked(boolean sDateClicked) {
        this.sDateClicked = sDateClicked;
    }

    boolean iseDateClicked() {
        return eDateClicked;
    }

    void seteDateClicked(boolean eDateClicked) {
        this.eDateClicked = eDateClicked;
    }

    boolean isTitleClicked() {
        return titleClicked;
    }

    void setTitleClicked(boolean titleClicked) {
        this.titleClicked = titleClicked;
    }

    float getPercentage() {
        return percentage;
    }

    void setPercentage(float percentage) {
        this.percentage = percentage;
    }

    void calculation() {
        height = bottom - top;
        titleTextSize = height * 0.5f;
        percentTextSize = titleTextSize * 0.8f;

        textPaint.setTextSize(titleTextSize);
        strokePaint.setTextSize(titleTextSize);
        titleTextWidth = textPaint.measureText(title, 0, title.length());
    }




    /*
    * Modify Sevice
    * */

    public void moveBar(int day) {
        this.sdate.add(Calendar.DATE, day);
        this.edate.add(Calendar.DATE, day);
        updateStartDate(sdate);
        updateEndDate(edate);
    }

    public void addSdate(int day) {
        this.sdate.add(Calendar.DATE, day);
        updateStartDate(this.sdate);
    }

    boolean isAddSdate(int day){
        Calendar cal = Calendar.getInstance();
        cal.setTime(sdate.getTime());
        cal.add(Calendar.DATE, day);

        if (edate.compareTo(cal) <= 0) return false;
        return true;
    }

    public void addEdate(int day) {
        this.edate.add(Calendar.DATE, day);
        updateEndDate(edate);
    }

    boolean isAddEdate(int day){
        Calendar cal = Calendar.getInstance();
        cal.setTime(edate.getTime());
        cal.add(Calendar.DATE, day);

        if (cal.compareTo(sdate) <= 0) return false;
        return true;
    }

    public void setPercent(int percent) {
        if (percent < 0) percent = 0;
        else if (percent > 100) percent = 100;
        this.percent = percent;

        updatePercent(percent);
    }



    /*
     *
     *  Getter, Setter
     * */

    BarItem getParents() {
        return parents;
    }

    void setParents(BarItem parents) {
        this.parents = parents;
    }

    public int getPercent() {
        return percent;
    }

    public void setOnTheBarItems(List<OnTheBarItem> onTheBarItems) {
        this.onTheBarItems = onTheBarItems;
    }

    public void addOnTheBarDraws(OnTheBarItem onTheBarItem) {
        if (this.onTheBarItems == null) {
            this.onTheBarItems = new ArrayList<>();
        }
        onTheBarItems.add(onTheBarItem);
    }

    public List<OnTheBarItem> getOnTheBarItems() {
        return onTheBarItems;
    }

    public void addArrowList(int index) {
        if (this.arrowList == null) arrowList = new ArrayList<>();
        arrowList.add(index);
        lastArrow = index;
    }

    int getLastArrow(){
        return lastArrow;
    }

    public List<Integer> getArrowList() {
        return arrowList;
    }

    public Calendar getCenterDate() {
        return centerDate;
    }

    public void setCenterDate(Calendar centerDate) {
        this.centerDate = centerDate;
    }

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
        this.edate.add(Calendar.DATE, 1);
    }

    public void setBarColor(String color) {
        this.color = color;

        int c = 0xff000000;

        c += Integer.parseInt(this.color, 16);
        barColor.setColor(c);
    }

    public void setBarColor(int color) {
        barColor.setColor(color);
    }

    public String getBarColor() {
        return color;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isBarLongClicked() {
        return this.barLongClicked;
    }

    public void setBarLongClicked(boolean barLongClicked) {
        this.barLongClicked = barLongClicked;
    }

    public boolean isClickable() {
        return clickable;
    }

    public void setClickable(boolean clickable) {
        this.clickable = clickable;
    }

    public boolean isPercentDraw() {
        return percentDraw;
    }

    public void setPercentDraw(boolean percentDraw) {
        this.percentDraw = percentDraw;
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
        textPaint.setColor(textColor);
    }

    public int getDepthArrow() {
        return depthArrow;
    }

    public void setDownDepth(int depthArrow) {
        if (depthArrow == 0) this.depthArrow = 1;
        this.depthArrow = depthArrow;
    }

    public boolean isDivision() {
        return division;
    }

    public float getLeft() {
        return left;
    }

    public float getTop() {
        return top;
    }

    public float getRight() {
        return right;
    }

    public float getBottom() {
        return bottom;
    }

    public void setHighlightCount() {
        this.highLightCount = HIGHLIGHTCOUNT;
    }





    /*
     *
     *  Listener
     * */

    GanttChart.OnTheBarDrawClickListener onTheBarDrawClickListener;

    GanttChart.OnBarClickListener onBarClickListener;

    public GanttChart.OnBarClickListener getOnBarClickListener() {
        return onBarClickListener;
    }

    public void setOnBarClickListener(GanttChart.OnBarClickListener onBarClickListener) {
        this.onBarClickListener = onBarClickListener;
    }

    public GanttChart.OnTheBarDrawClickListener getOnTheBarDrawClickListener() {
        return onTheBarDrawClickListener;
    }

    public void setOnTheBarDrawClickListener(GanttChart.OnTheBarDrawClickListener onTheBarClickListener) {
        this.onTheBarDrawClickListener = onTheBarClickListener;
    }

}

