package com.leesihyeon.ganttchart;

import android.graphics.Canvas;
import android.graphics.Paint;

public class Line {

    private GanttChart chart;

    private Paint backgroundColor;
    private Paint lineColor;


    public Line(GanttChart ganttChart){
        this.chart = ganttChart;
        backgroundColor = new Paint();
        lineColor = new Paint();

        backgroundColor.setColor(chart.backgroundColor);
        lineColor.setColor(chart.lineColor);
    }

    public void drawBack(Canvas canvas){
        canvas.drawRect(0, 0, chart.getWidth(), chart.getHeight(), backgroundColor);
    }

    public void drawLine(Canvas canvas){
        // vertical line.
        canvas.drawLine(0, chart.topHeight, chart.getWidth(), chart.topHeight, lineColor);
        canvas.drawLine(0, chart.topHeight * 2, chart.getWidth(), chart.topHeight * 2, lineColor);

        if (chart.width >= chart.CHANGEMODE) {
            float unit = chart.width / GanttChart.TOPWIDTHWEEKUNIT;
            float startDayLine = unit + (chart.positionX % unit);

            for (; startDayLine <= chart.getWidth(); startDayLine += unit) {
                canvas.drawLine(startDayLine, chart.topHeight, startDayLine, chart.getHeight(), lineColor);
            }

        } else {
            float startWidthLine = chart.width + (chart.positionX % chart.width);
            for (; startWidthLine <= chart.getWidth(); startWidthLine += chart.width) {
                canvas.drawLine(startWidthLine, chart.topHeight, startWidthLine, chart.getHeight(), lineColor);
            }
        }
    }

    public void setBackgroundColor(int color){
        backgroundColor.setColor(color);
    }

    public void setLineColor(int color){
        lineColor.setColor(color);
    }

}
