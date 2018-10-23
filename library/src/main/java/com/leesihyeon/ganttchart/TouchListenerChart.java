package com.leesihyeon.ganttchart;

import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TouchListenerChart implements View.OnTouchListener {

    GanttChart chart;

    static final float EXPANSION = 1.2f;
    static final float COLLAPSE = 0.8f;

    static final int CENTERZOOM = 0;
    static final int TABZOOM = 1;
    static final int POINTZOOM = 2;

    int zoommode;

    float touchX, touchY;
    float touchX2, touchY2;
    float stackX, stackY;
    float centerW, centerH;


    boolean moving = false;
    boolean zooming = false;
    boolean zoomingW = false;
    boolean zoomingH = false;
    boolean downTouch = false;
    boolean threadZooming = false;

    TouchListenerChart(GanttChart chart) {
        this.chart = chart;
    }

    GanttChart.OnBarClickModifyListener onBarClickModifyListener;

    public GanttChart.OnBarClickModifyListener getOnBarClickModifyListener() {
        return onBarClickModifyListener;
    }

    public void setOnBarClickModifyListener(GanttChart.OnBarClickModifyListener onBarClickModifyListener) {
        this.onBarClickModifyListener = onBarClickModifyListener;
    }

    /*
     * Touch Service
     * */
    @Override
    public boolean onTouch(View view, MotionEvent event) {
        int action = event.getAction();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                downTouch = true;

                touchX = event.getX();
                touchY = event.getY();

                // 1 새로운 터치가 올 때 마다 카운트다운 시작
                chart.longClickCount = chart.LONGCLICKCOUNT;

                if (chart.isClickedItem()) {
                    if (chart.longClickItem.modifyClickPosition(touchX, touchY)) {
                        if (chart.longClickItem.isMoveClicked())
                            chart.longClickItem.setCenterDate(chart.data.xPositionToDate(touchX));
                    }
                }

                if (chart.seekBarListener != null) {
                    chart.seekBarCount = chart.SEEKBARCOUNT;
                    chart.seekBarListener.showSeek();
                }

                break;
            case 261:
                touchX = event.getX(0);
                touchY = event.getY(0);
                touchX2 = event.getX(1);
                touchY2 = event.getY(1);
                break;
            case MotionEvent.ACTION_MOVE:
                move(event);
                zoom(event);
                break;
            case MotionEvent.ACTION_UP:
                if (!moving && !zooming) {

                    if (chart.doubleClickCount != 0 && !threadZooming) {
                        doubleClickZoom();
                    }

                    if (!chart.isClickedItem() || !chart.longClickItem.isModifyClick()) {
                        if (chart.isClickedItem()) {

                            // GanttChart Min 제한을 걸어두지 않았으면서, 최소 일자를 초과한 경우 다시 그려준다.
                            if (chart.getMinDate() == null &&
                                    chart.longClickItem.getSdate().compareTo(chart.data.startDate) <= 0) {
                                chart.data.changeData();
                            }

                            // 수정 완료
                            if (onBarClickModifyListener != null) {
                                onBarClickModifyListener.itemModifyClose(modifiedBar());
                            }

                            chart.closeClickeditem();
                        } else {
                            // 클릭
                            if (!chart.onClick(touchX, touchY))
                                chart.doubleClickCount = chart.DOUBLECLICKCOUNT;
                        }
                    }
                }

                moving = false;
                zooming = false;
                zoomingW = false;
                zoomingH = false;
                downTouch = false;
                break;
        }
        return true;
    }

    void move(MotionEvent event) {
        if (event.getPointerCount() == 2 || zooming) return;

        if (chart.isClickedItem() && chart.longClickItem.isModifyClick()) {
            actionRefBarITem(event.getX());
        } else {
            actionMoveChart(event.getX(), event.getY());
        }
    }

    void zoom(MotionEvent event) {
        if (event.getPointerCount() == 1 || moving) return;
        zooming = true;
        zoommode = POINTZOOM;

        float nX1 = event.getX(0);
        float nY1 = event.getY(0);
        float nX2 = event.getX(1);
        float nY2 = event.getY(1);

        float calcW = (Math.abs(nX1 - nX2)) / (Math.abs(touchX - touchX2));

        centerW = (touchX + touchX2) / 2;

        repositionWidth(calcW, zoommode);

        touchX = nX1;
        touchX2 = nX2;
        touchY = nY1;
        touchY2 = nY2;
    }


    /*
     * action
     * */
    synchronized void repositionWidth(float ratio, int zoomMode) {

        if (chart.width * ratio < chart.OVERMINWIDTH || chart.width * ratio > chart.OVERMAXWIDTH)
            return;

        chart.width *= ratio;
        chart.positionX *= ratio;

        switch (zoomMode) {
            case CENTERZOOM:
                chart.positionX += chart.getWidth() / 2 - (chart.getWidth() / 2 * ratio);
                break;
            case TABZOOM:
                chart.positionX += touchX - (touchX * ratio);
                break;
            case POINTZOOM:
                chart.positionX += centerW - (centerW * ratio);
                break;
        }
        if (chart.seekBarListener != null)
            chart.seekBarListener.setSeekPosition((int) chart.width);
    }

    void repositionHeight(float ratio, int zoomMode) {

        if (chart.height * ratio < chart.OVERMINHEIGHT || chart.height * ratio > chart.OVERMAXHEIGHT)
            return;

        chart.height *= ratio;
        chart.positionY *= ratio;

        switch (zoomMode) {
            case CENTERZOOM:
                chart.positionY += chart.getHeight() / 2 - (chart.getHeight() / 2 * ratio);
                break;
            case TABZOOM:
                chart.positionY += touchY - (touchY * ratio);
                break;
            case POINTZOOM:
                chart.positionY += centerH - (centerH * ratio);
                break;
        }
    }

    // 차트 움직임
    void actionMoveChart(float x, float y) {
        float distanceX = x - touchX;
        float distanceY = y - touchY;

        if (moving || Math.abs(distanceX) >= chart.TOUCHSLOP) {
            moving = true;
            chart.positionX += distanceX;
            stackX += distanceX;
        }

        if (moving || Math.abs(distanceY) >= chart.TOUCHSLOP) {
            moving = true;
            chart.positionY += distanceY;
            stackY += distanceY;
        }

        touchX = x;
        touchY = y;
    }

    // 수정 바 움직임
    void actionRefBarITem(float x) {
        moving = true;

        if (chart.longClickItem.isPercentClicked()) {
            // 퍼센트 버튼 수정
            int percent = (int) ((x - chart.longClickItem.getLeft()) / chart.longClickItem.getPercentage());
            chart.longClickItem.setPercent(percent);
        } else if (chart.longClickItem.isMoveClicked()) {
            moveCenter(x);
        } else if (chart.longClickItem.issDateClicked()) {
            moveLeft(x);
        } else if (chart.longClickItem.iseDateClicked()) {
            moveRight(x);
        }
    }

    // 중앙버튼 수정
    void moveCenter(float x) {

        Calendar cal = chart.data.xPositionToDate(x);
        int day = chart.data.diffDay(chart.longClickItem.getCenterDate(), cal);

        if (chart.getMinDate() == null ||
                chart.longClickItem.getSdate().compareTo(chart.getMinDate()) >= 0 || day > 0) {

            // 부모를 벗어나지 못할 경우
            if (chart.isParentsOverMoveImpossible()) {

                if (chart.longClickItem.getParents() != null) {
                    if (day < 0 && compareTo(chart.longClickItem.getSdate(), day, chart.longClickItem.getParents().getSdate()) >= 0) {
                        chart.longClickItem.moveBar(day);
                        moveToChild(chart.longClickItem.getArrowList(), day);
                    } else if (day > 0) {
                        int a = compareTo(chart.longClickItem.getEdate(), day, chart.longClickItem.getParents().getEdate());
                        if (a<=0){
                            chart.longClickItem.moveBar(day);
                            moveToChild(chart.longClickItem.getArrowList(), day);
                        }
                    }
                } else {
                    chart.longClickItem.moveBar(day);
                    moveToChild(chart.longClickItem.getArrowList(), day);
                }
            }

            // 부모를 벗어나도 되는 경우
            else {
                chart.longClickItem.moveBar(day);
            }

        }

        // 업데이트
        chart.longClickItem.setCenterDate(cal);
    }

    // 왼쪽버튼 날짜 수정
    void moveLeft(float x) {
        Calendar sdate = chart.data.xPositionToDate(x);
        int day = chart.data.diffDay(chart.longClickItem.getSdate(), sdate) - 1;

        if (chart.getMinDate() == null || chart.longClickItem.getSdate().compareTo(chart.getMinDate()) >= 0 || day > 0) {
            if (chart.isParentsOverMoveImpossible()) {
                // 부모가 있다면
                if (chart.longClickItem.getParents() != null) {
                    // 부모의 sdate를 감시
                    if (compareTo(chart.longClickItem.getSdate(), day, chart.longClickItem.getParents().getSdate()) >= 0 || day > 0) {
                        // 자식크기를 변경 할 수 있다면..
                        if (isAddLeftToChild(chart.longClickItem.getArrowList(), day)) {
                            chart.longClickItem.addSdate(day);
                            addLeftToChild(chart.longClickItem.getArrowList(), day);
                        } else {
                            return;
                        }
                    }
                } else {
                    if (isAddLeftToChild(chart.longClickItem.getArrowList(), day)) {
                        chart.longClickItem.addSdate(day);
                        addLeftToChild(chart.longClickItem.getArrowList(), day);
                    }
                }
            } else if (chart.longClickItem.isAddSdate(day))chart.longClickItem.addSdate(day);
        }
    }

    // 오른쪽 버튼 날짜 수정
    void moveRight(float x) {

        Calendar edate = chart.data.xPositionToDate(x);
        int day = chart.data.diffDay(chart.longClickItem.getEdate(), edate);

        if (chart.isParentsOverMoveImpossible()) {
            // 부모가 있다면
            if (chart.longClickItem.getParents() != null) {
                // 부모의 sdate를 감시
                int a = compareTo(chart.longClickItem.getEdate(), day, chart.longClickItem.getParents().getEdate());
                if (a<=0 || day < 0) {
                    // 자식크기를 변경 할 수 있다면..
                    if (isAddRightToChild(chart.longClickItem.getArrowList(), day)) {
                        chart.longClickItem.addEdate(day);
                        addRightToChild(chart.longClickItem.getArrowList(), day);
                    } else {
                        return;
                    }
                }
            } else {
                if (isAddRightToChild(chart.longClickItem.getArrowList(), day)) {
                    chart.longClickItem.addEdate(day);
                    addRightToChild(chart.longClickItem.getArrowList(), day);
                }
            }
        } else
            if (chart.longClickItem.isAddEdate(day)) chart.longClickItem.addEdate(day);
    }


    /*
    * 자식바 재귀함수
    * */
    void moveToChild(List<Integer> list, int day) {
        if (list == null) return;
        else {
            for (int index : list) {
                BarItem item = chart.data.bars.get(index);
                item.moveBar(day);
                moveToChild(item.getArrowList(), day);
            }
        }
    }

    boolean isAddLeftToChild(List<Integer> list, int day) {
        if (list == null) return true;
        else {
            for (int index : list) {
                BarItem item = chart.data.bars.get(index);
                if (!item.isAddSdate(day)) {
                    return false;
                }
                if (!isAddLeftToChild(item.getArrowList(), day)) return false;
            }
            return true;
        }
    }

    void addLeftToChild(List<Integer> list, int day) {
        if (list == null) return;
        else {
            for (int index : list) {
                BarItem item = chart.data.bars.get(index);
                item.addSdate(day);
                addLeftToChild(item.getArrowList(), day);
            }
        }
    }

    boolean isAddRightToChild(List<Integer> list, int day) {
        if (list == null) return true;
        else {
            for (int index : list) {
                BarItem item = chart.data.bars.get(index);
                if (!item.isAddEdate(day)) {
                    return false;
                }
                if (!isAddRightToChild(item.getArrowList(), day)) return false;
            }
            return true;
        }
    }

    void addRightToChild(List<Integer> list, int day) {
        if (list == null) return;
        else {
            for (int index : list) {
                BarItem item = chart.data.bars.get(index);
                item.addEdate(day);
                addRightToChild(item.getArrowList(), day);
            }
        }
    }

    int compareTo(Calendar target, int day, Calendar cal) {
        Calendar t = Calendar.getInstance();
        Calendar c = Calendar.getInstance();

        t.set(target.get(Calendar.YEAR), target.get(Calendar.MONTH), target.get(Calendar.DATE) + day, 0, 0, 0);
        c.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE), 0, 0, 0);

        return t.compareTo(c);
    }

    List<BarItem> modifiedBar(){
        List<BarItem> list = new ArrayList<>();

        list.add(chart.longClickItem);

        modifyChild(list, chart.longClickItem.getArrowList());

        return list;
    }

    void modifyChild(List<BarItem> list, List<Integer> arrowList){
        if (arrowList == null) return;

        for (int index : arrowList){
            BarItem item = chart.data.bars.get(index);
            list.add(item);
            modifyChild(list, item.getArrowList());
        }
    }


    /*
     * animation
     * */

    void doubleClickZoom() {
        Thread doubleClickThread = new Thread(new Runnable() {
            @Override
            public void run() {
                threadZooming = true;
                int speed = 30;
                zoommode = TABZOOM;
                if (chart.width <= chart.MINWIDTH) {
                    while (chart.width < chart.MAXWIDTH) {
                        try {
                            Thread.sleep(speed);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        repositionWidth(EXPANSION, zoommode);
                    }
                } else {
                    while (chart.width > chart.MINWIDTH) {
                        try {
                            Thread.sleep(speed);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        repositionWidth(COLLAPSE, zoommode);
                    }
                }
                threadZooming = false;
            }
        });
        doubleClickThread.start();
    }

}
