package com.leesihyeon.ganttchart;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

import java.util.Calendar;
import java.util.List;

public class GanttChart extends ViewGroup {

    public final static int TOPWIDTHMONTHUNIT = 4;
    public final static int TOPWIDTHWEEKUNIT = 7;

    public static float MAXWIDTH;
    public static float MINWIDTH;
    public static float OVERMAXWIDTH;
    public static float OVERMINWIDTH;
    public static float MAXHEIGHT;
    public static float MINHEIGHT;
    public static float OVERMAXHEIGHT;
    public static float OVERMINHEIGHT;
    public static float CHANGEMODE;

    private final int WIDTHDP = 60;
    private final int HEIGHTDP = 40;
    private final int TOPHEIGHTDP = 40;

    /*
     *
     * Main Value
     *
     * */

    float width;
    float height;
    float topHeight;
    float positionX, positionY;
    float floodingX, floodingY;

    Calendar minDate;

    boolean parentsOverMoveImpossible;

    /*
     *
     * Primary Color
     *
     * */

    int backgroundColor;
    int lineColor;
    int textColor;
    int redColor;
    int blueColor;
    int toDayColor;

    int lineWidth;


    /*
     *
     * DRAW ELEMENTS
     * ( BACKGROUND, BAR )
     *
     * */

    Line line;
    Data data;




    /*
     *  생성자
     *
     * */

    public GanttChart(Context context) {
        super(context);
        init();

    }

    public GanttChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();

    }

    public GanttChart(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {

    }

    @Override
    protected void onDraw(Canvas canvas) {

        this.line.drawBack(canvas);

        if (onTheChartDrawListener != null) onTheChartDrawListener.drawBack(canvas);

        if (data != null) {
            this.data.draw(canvas);
        }

        this.line.drawLine(canvas);

        if (onTheChartDrawListener != null) onTheChartDrawListener.drawFront(canvas);


        sliding();
        reposition();
        countdown();
        invalidate();
    }

    void init() {
        setWillNotDraw(false);

        backgroundColor = 0xff172F28;
        lineColor = Color.GRAY;
        textColor = Color.WHITE;
        redColor = Color.RED;
        blueColor = Color.BLUE;
        toDayColor = Color.GRAY;

        topHeight = ConvertDpPixels.convertDpToPixel(TOPHEIGHTDP, getContext());
        width = ConvertDpPixels.convertDpToPixel(WIDTHDP, getContext());
        height = ConvertDpPixels.convertDpToPixel(HEIGHTDP, getContext());

        CHANGEMODE = width * 5;

        MAXWIDTH = width * 6;
        MINWIDTH = width;

        MAXHEIGHT = height * 2;
        MINHEIGHT = height;

        OVERMAXWIDTH = MAXWIDTH * 1.2f;
        OVERMINWIDTH = MINWIDTH * 0.8f;

        OVERMAXHEIGHT = MAXHEIGHT * 1.2f;
        OVERMINHEIGHT = MINHEIGHT * 0.8f;

        this.touchListenerChart = new TouchListenerChart(this);
        this.line = new Line(this);

        setOnTouchListener(touchListenerChart);
    }



    /*
     * Count ( SeekBar, DoubleClick )
     * */

    final int DOUBLECLICKCOUNT = 20;
    final int SEEKBARCOUNT = 200;
    final int LONGCLICKCOUNT = 50;

    int doubleClickCount;
    int seekBarCount;
    int longClickCount;

    void countdown() {
        if (--seekBarCount <= 0 && seekBarListener != null) seekBarListener.hideSeek();
        if (doubleClickCount > 0) doubleClickCount--;
        if (touchListenerChart.downTouch && !touchListenerChart.moving && !touchListenerChart.zooming) {
            if (longClickCount-- == 0){
                if (isClickedItem()) {
                    touchListenerChart.onBarClickModifyListener.itemModifyClose(
                            touchListenerChart.modifiedBar()
                    );
                    closeClickeditem();
                }
                longClickItem = data.onLongClick(touchListenerChart.touchX, touchListenerChart.touchY);
            }
        }
    }


    /*
     *  Touch, Business
     *
     * */

    final int TOUCHSLOP = ViewConfiguration.get(getContext()).getScaledTouchSlop();

    TouchListenerChart touchListenerChart;

    BarItem longClickItem;

    void sliding() {
        if (touchListenerChart.stackX == 0 && touchListenerChart.stackY == 0) return;

        touchListenerChart.stackX *= 0.8f;
        touchListenerChart.stackY *= 0.8f;

        if (!touchListenerChart.moving) {
            positionX += touchListenerChart.stackX;
            positionY += touchListenerChart.stackY;
        }

        if (Math.abs(touchListenerChart.stackX) < 0.01f) touchListenerChart.stackX = 0;
        if (Math.abs(touchListenerChart.stackY) < 0.01f) touchListenerChart.stackY = 0;
    }

    void reposition() {
        // Sliding Animation Move
        floodingAnimationMove();

        // Over Position X, Y;
        overPositionMove();
    }

    void floodingAnimationMove() {
        if (floodingX == 0 && floodingY == 0) return;

        floodingX *= 0.5f;
        floodingY *= 0.5f;

        if (!touchListenerChart.moving) {
            positionX += floodingX;
            positionY += floodingY;
        }

        if (Math.abs(floodingX) < 0.01f) floodingX = 0;
        if (Math.abs(floodingY) < 0.01f) floodingY = 0;
    }

    void overPositionMove() {
        if (!touchListenerChart.moving && positionX > 0) {
            if ((positionX /= 2) < 0.1f) {
                positionX = 0;
            }
        }

        if (!touchListenerChart.moving && positionY > 0) {
            if ((positionY /= 2) < 0.1f) {
                positionY = 0;
            }
        }
    }

    boolean onClick(float x, float y) {
        if(data != null)
            return data.onClick(x, y);
        else return false;
    }



    /*
     *  Listener
     *
     * */

    SeekBarListener seekBarListener;

    OnTheBarDrawClickListener onTheBarDrawClickListener;

    OnBarClickListener onBarClickListener;

    OnTheChartDrawListener onTheChartDrawListener;

    OnBarClickModifyListener onBarClickModifyListener;

    public OnBarClickModifyListener getOnBarClickModifyListener() {
        return onBarClickModifyListener;
    }

    public void setOnBarClickModifyListener(OnBarClickModifyListener onBarClickModifyListener) {
        this.onBarClickModifyListener = onBarClickModifyListener;
        this.touchListenerChart.setOnBarClickModifyListener(onBarClickModifyListener);
    }

    public void setSeekBarListener(SeekBarListener seekBarListener) {
        this.seekBarListener = seekBarListener;
    }

    public SeekBarListener getSeekBarListener() {
        return seekBarListener;
    }

    public OnTheBarDrawClickListener getOnTheBarDrawListener() {
        return onTheBarDrawClickListener;
    }

    public void setOnTheBarDrawClickListener(OnTheBarDrawClickListener onTheBarDrawClickListener) {
        this.onTheBarDrawClickListener = onTheBarDrawClickListener;
        if (data != null) {
            this.data.setOnTheBarDrawClickListener(onTheBarDrawClickListener);
        }
    }

    public OnBarClickListener getOnBarClickListener() {
        return onBarClickListener;
    }

    public void setOnBarClickListener(OnBarClickListener onBarClickListener) {
        this.onBarClickListener = onBarClickListener;
        if (data != null) {
            this.data.setOnBarClickListener(onBarClickListener);
        }
    }

    public OnTheChartDrawListener getOnTheChartDrawListener() {
        return onTheChartDrawListener;
    }

    public void setOnTheChartDrawListener(OnTheChartDrawListener onTheChartDrawListener) {
        this.onTheChartDrawListener = onTheChartDrawListener;
    }





    /*
     *  Service
     * */

    public void closeClickeditem() {
        longClickItem.closeLongClick();
        longClickItem = null;
    }

    public boolean isClickedItem() {
        if (longClickItem == null) return false;
        else return true;
    }

    public void setFloodingX(float floodingX) {
        this.floodingX = floodingX;
    }

    public void setFloodingY(float floodingY) {
        this.floodingY = floodingY;
    }

    public void resetSeekbarCount() {
        seekBarCount = SEEKBARCOUNT;
    }

    public void goToDay() {
        setFloodingX(-(data.dateToPositionX(data.getToDay()) - getWidth() / 2));
    }

    public void goToDay(Calendar date) {
        setFloodingX(-(this.data.dateToPositionX(date) - getWidth() / 2));
    }

    public void goToItem(BarItem barItem) {
        setFloodingX(-(barItem.getLeft() - getWidth() / 3));
        setFloodingY(-(barItem.getTop() - getHeight() / 3));
    }

    public void goToItem(OnTheBarItem onTheBarItem) {
        setIntervalWidth(GanttChart.MAXWIDTH);
        goToDay(onTheBarItem.getSdate());
        setFloodingY(-(onTheBarItem.getTop() - getHeight() / 2));
    }

    public void revalidate(){
        data.changeData();
    }



    /*
     *  Getter, Setter
     * */

    public boolean isParentsOverMoveImpossible() {
        return parentsOverMoveImpossible;
    }

    public void setParentsOverMoveImpossible(boolean parentsOverMoveImpossible) {
        this.parentsOverMoveImpossible = parentsOverMoveImpossible;
    }

    public Calendar getMinDate() {
        return minDate;
    }

    public void setMinDate(Calendar minDate) {
        minDate.add(Calendar.DATE, 1);
        this.minDate = minDate;
    }

    public void setData(Data data) {
        this.data = data;
        this.data.setChart(this);
        if (onTheBarDrawClickListener != null) {
            this.data.setOnTheBarDrawClickListener(onTheBarDrawClickListener);
        }

        if (onBarClickListener != null) {
            this.data.setOnBarClickListener(onBarClickListener);
        }
    }

    public Data getDate() {
        return data;
    }

    public void setBackgroundColor(int color) {
        backgroundColor = color;
        line.setBackgroundColor(color);
    }

    public void setLineColor(int color) {
        lineColor = color;
        line.setLineColor(color);
    }

    public void setTextColor(int color) {
        textColor = color;
    }

    public int getTextColor() {
        return textColor;
    }

    public float getTextSize(){
        if (data != null)
            return data.getTextSize();
        else return 40.0f;
    }

    public void setToDayColor(int color) {
        toDayColor = color;
    }

    public int getToDayColor() {
        return toDayColor;
    }

    public void setLineStrokeWidth(int width) {
        lineWidth = width;
    }

    public float getLineStrokeWidth() {
        return lineWidth;
    }

    public void setIntervalWidth(float width) {
        if (touchListenerChart != null)
            touchListenerChart.repositionWidth(width / this.width, TouchListenerChart.CENTERZOOM);
    }

    public float getIntervalWidth() {
        return width;
    }

    public float getIntervalHeight() {
        return height;
    }

    public void setIntervalHeight(float height) {
        height = height;
    }


    /*
     *  Listener Interface
     *
     * */

    public interface SeekBarListener {
        void showSeek();

        void hideSeek();

        void setSeekPosition(int width);
    }

    public interface OnTheBarDrawClickListener {
        void itemSelect(BarItem barItem, List<OnTheBarItem> onTheBarItemItems);
    }

    public interface OnTheChartDrawListener {
        void drawFront(Canvas canvas);

        void drawBack(Canvas canvas);
    }

    public interface OnBarClickListener {
        void itemSelect(BarItem barItem);
    }

    public interface OnBarClickModifyListener {
        void itemModifyClose(List<BarItem> items);
    }

}