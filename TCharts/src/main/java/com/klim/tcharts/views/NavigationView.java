package com.klim.tcharts.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;

import com.klim.tcharts.Colors;
import com.klim.tcharts.R;
import com.klim.tcharts.entities.ChartData;
import com.klim.tcharts.entities.ChartItem;
import com.klim.tcharts.interfaces.OnSelectedTimeLineChanged;
import com.klim.tcharts.interfaces.OnShowLinesListener;
import com.klim.tcharts.utils.AnimatorU;
import com.klim.tcharts.utils.PaintU;

import java.util.ArrayList;

public class NavigationView extends BaseView implements OnShowLinesListener {
    private SelectedWindow selectedWindow;

    private ChartData data = null;
    private int maxValue;
    private long minTime;
    private long maxTime;
    private long startShowMinTime;
    private long startShowMaxTime;
    private float pixelInTime;
    private float pixelInValue;

    private ArrayList<float[]> prepareLines;
    private boolean needRePrepare = true;

    private float maxValueLocal;
    private boolean enableShowLine = false;

    private ValueAnimator animatorsMaxValue;

    private int lineIndex = -1; // for pain last hide line

    //params
    private float paddingDataGraph;
    public Colors colors;

    //paints
    private Paint pForLine;
    private Paint pForHideLine;

    public NavigationView(View view, Colors colors) {
        super(view);
        this.colors = colors;
        selectedWindow = new SelectedWindow(this.view, colors);

        //params
        paddingDataGraph = getDimen(R.dimen.chartNavLineTopBottomPadding);

        createAnimators();
    }

    public void init() {
        createPaints();
    }

    private void createPaints() {
        //paints
        pForLine = PaintU.createPaint(Color.WHITE, Paint.Style.STROKE, getDimen(R.dimen.chartNavLineWeight));
        pForLine.setStrokeCap(Paint.Cap.ROUND);

        pForHideLine = PaintU.createPaint(colors.backgroundColor, Paint.Style.STROKE, getDimen(R.dimen.chartNavLineWeight));
        pForHideLine.setStrokeCap(Paint.Cap.ROUND);
    }

    private void createAnimators() {
        animatorsMaxValue = AnimatorU.createFloatValueAnimator(maxValueLocal, maxValue, 200, animation -> {
            maxValueLocal = (Float) animation.getAnimatedValue();
//                valueInPixel = availableHeight / maxValueLocal;
            pixelInValue = (height - paddingDataGraph * 2) / maxValueLocal;
            prepareDataForPrinting(true);
            invalidate();
        });
        animatorsMaxValue.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                enableShowLine = true;
                prepareDataForPrinting(true);
                invalidate();
            }
        });
    }

    private void calculateTimeValueInPixel() {
        if (data != null) {
            minTime = data.getItems().get(0).getTime();
            maxTime = data.getItems().get(data.getItems().size() - 1).getTime();
            pixelInTime = width / (float) (maxTime - minTime);
            if (maxValue > 0) {
                pixelInValue = (height - paddingDataGraph * 2) / maxValue;
            }
            selectedWindow.setTimeInPixel(pixelInTime, minTime, maxTime);
        }
    }

    public int calculateHeight() {
        return Math.round(getDimen(R.dimen.navViewDesiredHeight));
    }

    @Override
    public void onSizeChanged() {
        if (width != 0 && height != 0 && data != null) {
            calculateTimeValueInPixel();
            prepareDataForPrinting(false);
        }
        selectedWindow.onSizeChanged();
    }

    @Override
    public void prepareDataForPrinting(boolean hardPrepare) {
        if (needRePrepare || hardPrepare) {
            prepareLines = new ArrayList<>(data.getItems().get(0).getValues().size());
            float[] arrLines;
            for (int l = 0; l < data.getItems().get(0).getValues().size(); l++) {
                if (getView().linesForShow[l] && (lineIndex != l || (enableShowLine && lineIndex == l))) {
                    float lastx = 0;
                    float lasty = 0;
                    float curx = 0;
                    float cury = 0;

                    boolean firstPoint = true;
                    arrLines = new float[(data.getItems().size()) * 4];
                    ChartItem chartItem;
                    for (int i = 0; i < data.getItems().size(); i++) {
                        chartItem = data.getItems().get(i);

                        curx = posX + (chartItem.getTime() - minTime) * pixelInTime;
                        cury = posY + (height) - (pixelInValue * chartItem.getValues().get(l) + paddingDataGraph);
                        System.out.println(posY);

                        if (firstPoint) {
                            firstPoint = false;
                        } else {
                            arrLines[i * 4] = lastx;
                            arrLines[i * 4 + 1] = lasty;
                            arrLines[i * 4 + 2] = curx;
                            arrLines[i * 4 + 3] = cury;
                        }
                        lastx = curx;
                        lasty = cury;
                    }
                } else {
                    arrLines = null;
                }
                prepareLines.add(arrLines);
            }
            needRePrepare = false;
        }
    }

    @Override
    public void drawOn(Canvas canvas) {
        for (int l = 0; l < getView().linesForShow.length; l++) {
            if (getView().linesForShow[l] && (lineIndex != l || (enableShowLine && lineIndex == l))) {
                //todo seted color every time?
                pForLine.setColor(data.getColors().get(l));
                canvas.drawLines(prepareLines.get(l), pForLine);
            }
        }

        //clear left right overpaint
        canvas.drawLine(posX + paddingX - getDimen(R.dimen.chartNavLineWeight) / 2, posY, posX + paddingX - getDimen(R.dimen.chartNavLineWeight) / 2, posY + height, pForHideLine);
        canvas.drawLine(posX + paddingX + width + getDimen(R.dimen.chartNavLineWeight) / 2, posY, posX + paddingX + getDimen(R.dimen.chartNavLineWeight) / 2 + width, posY + height, pForHideLine);

        //draw window
        selectedWindow.drawOn(canvas);

        super.drawOn(canvas);
    }

    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                iAmPressed = true;
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                iAmPressed = false;
                break;
            }
        }
        return selectedWindow.onTouchEvent(event);
    }

    public void setData(ChartData data, long start, long end) {
        setStartPeriodTimes(start, end);
        this.data = data;
        updateMaxValue();
        maxValueLocal = maxValue;
        if (width != 0 && height != 0 && data != null) {
            calculateTimeValueInPixel();
            prepareDataForPrinting(false);
        }
        selectedWindow.onSizeChanged();
    }

    public void setPosX(float posX) {
        this.posX = posX;
        selectedWindow.setPosX(posX);
    }

    public void setPosY(float posY) {
        this.posY = posY;
        selectedWindow.setPosY(posY);
    }

    public void setStartPeriodTimes(long start, long end) {
        this.startShowMinTime = start;
        this.startShowMaxTime = end;
        selectedWindow.setStartPeriodTimes(start, end);
    }

    @Override
    public void setWidth(int width) {
        super.setWidth(width);
        selectedWindow.setWidth(width);
    }

    @Override
    public void setHeight(int height) {
        super.setHeight(height);
        selectedWindow.setHeight(height);
    }

    public void addOnSelectedTimeLineChanged(OnSelectedTimeLineChanged onSelectedTimeLineChanged) {
        selectedWindow.addOnSelectedTimeLineChanged(onSelectedTimeLineChanged);
    }

    private void updateMaxValue() {
        maxValue = 0;
        for (int i = 0; i < data.getMaxValueInLine().size(); i++) {
            if (getView().linesForShow[i] && maxValue < data.getMaxValueInLine().get(i)) {
                maxValue = data.getMaxValueInLine().get(i);
            }
        }
    }

    @Override
    public void onShowLines(int lineIndex, boolean show) {
        this.lineIndex = lineIndex;
        needRePrepare = true;
        updateMaxValue();
        calculateTimeValueInPixel();

        if (show) {
            enableShowLine = false;
        }
        animatorsMaxValue.setFloatValues(maxValueLocal, maxValue);
        animatorsMaxValue.start();
    }
}
