package com.leesihyeon.ganttchart;

import android.graphics.Canvas;
import android.graphics.Paint;

import java.util.Calendar;
import java.util.List;

public class Data {

    /*
     *  Main Value
     *
     * */

    Calendar startDate;
    Calendar endDate;
    Calendar toDay;
    Calendar toDayOfWeek;

    int startWeekOfMonth;
    int startDayOfWeek;

    int totalDays;

    int itemCount;


    Paint backgroundColor;
    Paint textColor;
    Paint toDayColor;

    /*
     *  Reference
     *
     * */

    private GanttChart chart;

    public Data() {
        toDay = Calendar.getInstance();
        toDayOfWeek = Calendar.getInstance();
        startDate = Calendar.getInstance();
        endDate = Calendar.getInstance();
        toDayOfWeek.add(Calendar.DATE, -(toDay.get(Calendar.DAY_OF_WEEK) - 1));

        backgroundColor = new Paint();
        textColor = new Paint();
        toDayColor = new Paint();
    }

    void setChart(GanttChart chart) {
        this.chart = chart;

        backgroundColor.setColor(chart.backgroundColor);
        textColor.setColor(chart.textColor);
        toDayColor.setColor(chart.toDayColor);

        textColor.setTextSize(chart.topHeight * 0.4f);
    }



    /*
     * DATA
     * */

    List<? extends BarItem> bars;

    public void setBars(List<? extends BarItem> bars) {
        this.bars = bars;
    }

    List<? extends BarItem> getBars() {
        return bars;
    }



    /*
     *  Draw
     * */

    void draw(Canvas canvas) {

        if (bars == null || bars.size() == 0) return;
        else if (bars.size() != itemCount) {
            changeData();
        }

        drawData(canvas);
        drawMask(canvas);
        drawDate(canvas);
    }

    void drawData(Canvas canvas) {
        for (int i = 0; i < bars.size(); i++) {
            BarItem b = bars.get(i);

            if (b.isDivision()) continue;

            float top = getPositionY(i);
            float bot = 0;

            if (b.getArrowList() != null) {
                bot = getPositionY(b.getLastArrow()) + 10;
            }else {
                bot = getPositionY(i + 1) - 10;
            }

            if (top > chart.getHeight()) continue;
            if (bot < chart.getY()) continue;

            b.drawBar(dateToPositionX(b.getSdate()), top, dateToPositionX(b.getEdate()), getPositionY(i + 1) - 10, canvas);

            if (b.getArrowList() != null) {
                for (int j : b.getArrowList()) {
                    BarItem item = bars.get(j);
                    b.drawArrow(dateToPositionX(item.getSdate()), getPositionY(j) + 10, canvas);
                }
            }

            if (b.getOnTheBarItems() != null && b.getOnTheBarItems().size() > 0) {
                for (OnTheBarItem bi : b.getOnTheBarItems()) {
                    bi.draw(dateToPositionX(bi.getSdate()), getPositionY(i),
                            dateToPositionX(bi.getEdate()), getPositionY(i + 1) - 10, canvas);
                }
            }

            b.drawTitle(canvas);
        }
    }

    void drawMask(Canvas canvas) {
        canvas.drawRect(0, 0, chart.getWidth(), chart.topHeight * 2, backgroundColor);
    }

    void drawDate(Canvas canvas) {
        if (chart.width < chart.CHANGEMODE) drawTitleWeekMode(canvas);
        else drawTitleDayMode(canvas);
    }

    void drawTitleWeekMode(Canvas canvas) {

        float widthUnitTop = chart.width * chart.TOPWIDTHMONTHUNIT;
        float startWidthTextTop = widthUnitTop - (widthUnitTop / 2) + (chart.positionX % widthUnitTop);
        int stackM = (int) (-chart.positionX / widthUnitTop);

        int month = startDate.get(Calendar.MONTH);
        int year = startDate.get(Calendar.YEAR);

        int startMonth = (month + stackM) % 12;
        int startYear = year + (month + stackM) / 12;

        // 이번주 박스
        canvas.drawRect(dateToPositionX(toDayOfWeek), chart.topHeight, dateToPositionX(toDayOfWeek) + chart.width
                , chart.topHeight * 2, toDayColor);


        // Top Draw
        for (; startWidthTextTop <= chart.getWidth(); startWidthTextTop += widthUnitTop) {

            if (++startMonth == 13) {
                startMonth = 1;
                startYear++;
            }

            if (chart.positionX > 0 && startMonth <= month) continue;

            canvas.drawText(startYear + "." + startMonth + "월",
                    startWidthTextTop, chart.topHeight / 2 + textColor.getTextSize() * 0.4f, textColor);
        }


        // Bottom Draw
        float startWidthTextBot = (chart.width / 2) + (chart.positionX % chart.width);
        int index = (int) (-chart.positionX / chart.width) - startWeekOfMonth + 2;

        for (; startWidthTextBot <= chart.getWidth(); startWidthTextBot += chart.width, index++) {
            String format = index + "주";
            if (index < 1) continue;

            float width = textColor.measureText(format, 0, format.length());
            canvas.drawText(format, startWidthTextBot - (width / 2),
                    chart.topHeight + (chart.topHeight / 2) + textColor.getTextSize() * 0.4f, textColor);
        }
    }

    void drawTitleDayMode(Canvas canvas) {

        int indexTop = (int) (-chart.positionX / chart.width) - startWeekOfMonth + 2;
        float startWidthTextTop = (chart.width / 2) + (chart.positionX % chart.width);

        // Top Draw
        for (; startWidthTextTop <= chart.getWidth(); startWidthTextTop += chart.width, indexTop++) {
            String format = indexTop + "주";
            if (indexTop < 1) continue;

            float width = textColor.measureText(format, 0, format.length());
            canvas.drawText(format, startWidthTextTop - (width / 2),
                    chart.topHeight / 2 + textColor.getTextSize() * 0.4f, textColor);
        }


        // Bottom Draw
        float widthUnitBot = chart.width / GanttChart.TOPWIDTHWEEKUNIT;
        int indexBot = (int) (-chart.positionX / widthUnitBot) - ((startWeekOfMonth - 1) * GanttChart.TOPWIDTHWEEKUNIT) - startDayOfWeek + 1;
        float startWidthTextBot = widthUnitBot / 2 + (chart.positionX % widthUnitBot);

        // 오늘 박스
        canvas.drawRect(dateToPositionX(toDay), chart.topHeight, dateToPositionX(toDay) + widthUnitBot
                , chart.topHeight * 2, toDayColor);

        Calendar refDate = Calendar.getInstance();
        refDate.setTime(startDate.getTime());
        refDate.add(Calendar.DATE, indexBot);

        for (; startWidthTextBot <= chart.getWidth(); startWidthTextBot += widthUnitBot) {

            int dayOfWeek = refDate.get(Calendar.DAY_OF_WEEK);
            switch (dayOfWeek) {
                case 1:
                    textColor.setColor(chart.redColor);
                    break;
                case 7:
                    textColor.setColor(chart.blueColor);
                    break;
                default:
                    textColor.setColor(chart.textColor);
                    break;
            }

            String format = refDate.get(Calendar.MONTH) + 1 + "." + refDate.get(Calendar.DATE);
            float width = textColor.measureText(format, 0, format.length());

            canvas.drawText(format, startWidthTextBot - (width / 2),
                    chart.topHeight + (chart.topHeight / 2) + textColor.getTextSize() * 0.4f, textColor);

            refDate.add(Calendar.DATE, 1);
        }

        // 다시 원래의 셋팅으로..
        textColor.setColor(chart.textColor);
    }





    /*
     *  service
     *
     * */

    float dateToPositionX(Calendar date) {

        if (date == null) return 0;

        float weekUnit = chart.width / chart.TOPWIDTHWEEKUNIT;
        int diff = diffDay(startDate, date) - 1;

        float positionX = chart.width * (startWeekOfMonth - 1) + (weekUnit * startDayOfWeek) + (weekUnit * diff) + chart.positionX;

        return positionX;
    }

    Calendar xPositionToDate(float x) {
        Calendar calendar = Calendar.getInstance();

        calendar.setTime(startDate.getTime());

        float weekUnit = chart.width / chart.TOPWIDTHWEEKUNIT;

        float positionX = (-chart.positionX) + x;

        int stackDay = (int) Math.floor(positionX / weekUnit);

        int beforeDay = (startWeekOfMonth - 1) * 7 + startDayOfWeek - 2;

        calendar.add(Calendar.DATE, stackDay - beforeDay);

        return calendar;
    }

    float getPositionY(int index) {
        float position = (index + 1) * chart.height + chart.topHeight * 2 + 10 + chart.positionY;

        return position;
    }

    int diffDay(Calendar date, Calendar target) {
        int diff = 0;

        double d1 = date.getTime().getTime();
        double d2 = target.getTime().getTime();

        diff = (int) Math.round((d2 - d1) / (1000 * 60 * 60 * 24));

        return diff;
    }

    void changeData() {

        startDate.set(2100, 11, 31);
        endDate.set(2000, 0, 1);


        // 리스너 등록
        for (BarItem item : bars) {

            if (item.getSdate() == null) continue;

            if (item.getOnTheBarDrawClickListener() == null && onTheBarDrawClickListener != null) {
                item.setOnTheBarDrawClickListener(onTheBarDrawClickListener);
            }

            if (item.getOnBarClickListener() == null && onBarClickListener != null) {
                item.setOnBarClickListener(onBarClickListener);
            }

            // 부모 등록해주기...
            if (item.getArrowList() != null) {
                for (int index : item.getArrowList()) {
                    BarItem i = bars.get(index);
                    i.setParents(item);
                }
            }
        }

        if (chart.getMinDate() != null) {
            startDate.setTime(chart.getMinDate().getTime());
        } else {
            // MIN Date 찾기
            for (BarItem item : bars) {
                if (item.getSdate() == null) continue;

                if (startDate.compareTo(item.getSdate()) > 0) {
                    startDate.setTime(item.getSdate().getTime());
                }

                if (endDate.compareTo(item.getEdate()) < 0) {
                    endDate.setTime(item.getEdate().getTime());
                }
            }
        }


        totalDays = diffDay(startDate, endDate);

        startWeekOfMonth = startDate.get(Calendar.WEEK_OF_MONTH);
        startDayOfWeek = startDate.get(Calendar.DAY_OF_WEEK);

        itemCount = bars.size();
    }



    /*
     *  Click Event
     *
     * */

    boolean onClick(float x, float y) {
        for (BarItem bar : bars) {
            if (bar.clickPosition(x, y)) {
                if (bar.isTitleClicked()) {
                    chart.setFloodingX(-(bar.getLeft() - chart.getWidth() / 3));
                    bar.setTitleClicked(false);
                }
                return true;
            }
        }
        return false;
    }

    BarItem onLongClick(float x, float y) {
        for (BarItem bar : bars) {
            if (bar.longClickRef(x, y)) {
                bar.setLongClick();
                return bar;
            }
        }
        return null;
    }



    /*
     *  Listener
     *
     * */

    GanttChart.OnTheBarDrawClickListener onTheBarDrawClickListener;

    GanttChart.OnBarClickListener onBarClickListener;

    GanttChart.OnTheBarDrawClickListener getOnTheBarDrawClickListener() {
        return onTheBarDrawClickListener;
    }

    void setOnTheBarDrawClickListener(GanttChart.OnTheBarDrawClickListener onBarClickListener) {
        this.onTheBarDrawClickListener = onBarClickListener;
    }

    public GanttChart.OnBarClickListener getOnBarClickListener() {
        return onBarClickListener;
    }

    void setOnBarClickListener(GanttChart.OnBarClickListener onBarClickListener) {
        this.onBarClickListener = onBarClickListener;
    }

    /*
     * Getter, Setter
     * */

    public Calendar getToDay() {
        return toDay;
    }

    public void setToDay(Calendar toDay) {
        this.toDay = toDay;
    }

    public float getTextSize() {
        return textColor.getTextSize();
    }
}
