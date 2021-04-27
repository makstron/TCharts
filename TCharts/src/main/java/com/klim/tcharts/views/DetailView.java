package com.klim.tcharts.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;

import com.klim.tcharts.Colors;
import com.klim.tcharts.R;
import com.klim.tcharts.entities.ChartData;
import com.klim.tcharts.entities.ChartItem;
import com.klim.tcharts.interfaces.OnSelectedTimeLineChanged;
import com.klim.tcharts.interfaces.OnShowLinesListener;
import com.klim.tcharts.utils.AnimatorU;
import com.klim.tcharts.utils.ColorU;
import com.klim.tcharts.utils.PaintU;
import com.klim.tcharts.utils.SearchU;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;

public class DetailView extends BaseView implements OnSelectedTimeLineChanged, OnShowLinesListener {
    private int PART_PERIOD_OF_TIME = 5;  //means that 5 time labels will be showed

    //params
    private Colors colors;
    private int divisionCount;
    //dimen
    private float dimenLabelSize;
    private float availableWidth;
    private float topBottomChartPadding;
    private float labelPadding;
    private float selectedCircleRadius;

    private ChartData data = null;

    private Paint pBitmap;
    private Paint pDivisionColor;
    private Paint pForLine;
    private Paint pForLabel;
    private Paint pForSelectedLine;
    private Paint pForEmptyCircle;

    private boolean needRePrepare = false;

    private ArrayList<Path> preparePath;
    private ArrayList<Divider> dividers = new ArrayList<>();
    private ArrayList<TimeLabel> timeLabels = new ArrayList<>();
    private ArrayList<TimeLabel> timeLabelsHide = new ArrayList<>();

    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd");

    private InfoWindowView infoWindowView;

    private float availableHeight;

    private float timeInPixel; //count time in pixels

    private long startPeriod;
    private long endPeriod;

    private float tapPositionX;
    private float tapPositionY;

    private float maxValueLocal;
    private float newMaxValueLocal;

    private int extendedLeft;
    private int extendedRight;
    private float valueInPixel;

    private int periodTimeLabel;
    private int newPeriodTimeLabel;

    private float timeLabelWidth;

    private ValueAnimator animatorMaxValue;
    private ValueAnimator animatorLineAlpha;
    private ValueAnimator animatorInfoWindowPosition;
    private ValueAnimator animatorInfoWindowsAlpha;

    private int lineIndex = -1;
    private boolean showLine;
    private int alphaLine = 255;

    private boolean alignFromLeft = false;
    private  float INFO_WINDOW_FINGER_PADDING;
    private float infoWindowPosition;
    private float newInfoWindowPosition;
    private int infoWindowsTransparent;

    public DetailView(View view, Colors colors, int divisionCount) {
        super(view);
        this.divisionCount = divisionCount;
        this.colors = colors;

        //params
        dimenLabelSize = getDimen(R.dimen.labelSize);
        topBottomChartPadding = getDimen(R.dimen.detailViewBottomLineHeight);
        labelPadding = getDimen(R.dimen.labelPadding);
        INFO_WINDOW_FINGER_PADDING = getDimen(R.dimen.infoWindowFingerPadding);
        infoWindowPosition = INFO_WINDOW_FINGER_PADDING;
        selectedCircleRadius = getDimen(R.dimen.selectedCircleRadius); //chart circle radius

        infoWindowView = new InfoWindowView(view, colors);

        createAnimators();
    }

    public void init() {
        createPaints();
        calculateTimeLabelMaxSize();
    }

    private void createPaints() {
        //paints
        pBitmap = new Paint(Paint.FILTER_BITMAP_FLAG);

        pDivisionColor = PaintU.createPaint(colors.detailDivisionColor, Paint.Style.STROKE, getDimen(R.dimen.fonLineswWeight));
        pDivisionColor.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));

        pForLine = PaintU.createPaint(Color.WHITE, Paint.Style.STROKE, getDimen(R.dimen.chartDetailLineHeight));
        pForLine.setStrokeCap(Paint.Cap.ROUND);
        pForLine.setStrokeJoin(Paint.Join.ROUND);

        pForLabel = PaintU.createPaint(colors.detailLabelsFontColor, Paint.Style.FILL);
        pForLabel.setTextSize(dimenLabelSize);

        pForSelectedLine = PaintU.createPaint(colors.detailLineSelectedPosition, Paint.Style.STROKE, getDimen(R.dimen.selectedLineWeight));
        pForSelectedLine.setStrokeCap(Paint.Cap.BUTT);

        pForEmptyCircle = PaintU.createPaint(colors.backgroundColor, Paint.Style.FILL, 0);
    }

    private void createAnimators() {
        animatorMaxValue = AnimatorU.createFloatValueAnimator(maxValueLocal, newMaxValueLocal, 200, new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                maxValueLocal = (Float) animation.getAnimatedValue();
                valueInPixel = availableHeight / maxValueLocal;
                prepareDataForPrinting(true);
            }
        });

        animatorLineAlpha = AnimatorU.createIntValueAnimator(0, 255, 250, new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                alphaLine = (Integer) animation.getAnimatedValue();
                invalidate();
            }
        });

        animatorInfoWindowsAlpha = AnimatorU.createIntValueAnimator(0, 255, 150, new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                infoWindowsTransparent = (Integer) animation.getAnimatedValue();
                invalidate();
            }
        });

        animatorInfoWindowPosition = AnimatorU.createFloatValueAnimator(0, 0, 250, new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                infoWindowPosition = (Float) animation.getAnimatedValue();
                invalidate();
            }
        });
    }

    public int calculateHeight() {
        return Math.round(getDimen(R.dimen.detailViewDesiredHeight));
    }

    @Override
    public void onSizeChanged() {
        prepare();
    }

    private void calculateTimeLabelMaxSize() {
        Rect textBounds = new Rect();
        Calendar calendar = new GregorianCalendar();
        SimpleDateFormat format = new SimpleDateFormat("MMM 33", context.getResources().getConfiguration().locale);
        String temp = "";
        for (int day = Calendar.JANUARY; day <= Calendar.DECEMBER; day++) {
            calendar.set(Calendar.MONTH, day);
            temp = format.format(calendar.getTime());
            pForLabel.getTextBounds(temp, 0, temp.length(), textBounds);
            timeLabelWidth = Math.max(timeLabelWidth, textBounds.width());
        }
        timeLabelWidth *= 1.20f;
    }

    private void startLabelAnimator(final ArrayList<TimeLabel> labels, String animateFor) {
        ValueAnimator animatorsAlphaTimeLabel = AnimatorU.createIntValueAnimator(0, 255, 250, new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                for (TimeLabel timeLabel : labels) {
                    timeLabel.alpha = (Integer) animation.getAnimatedValue();
                }
                invalidate();
            }
        });
        if (animateFor.equals("hide")) {
            animatorsAlphaTimeLabel.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    for (TimeLabel timeLabel : labels) {
                        timeLabelsHide.remove(timeLabel);
                    }
                }
            });
        }
        if (animateFor.equals("show")) {
            animatorsAlphaTimeLabel.start();
        } else {
            animatorsAlphaTimeLabel.reverse();
        }
    }

    private void startDividersAnimator(final ArrayList<Divider> dividers, String animateFor) {
        ValueAnimator animatorsAlphaTimeLabel = AnimatorU.createIntValueAnimator(0, 255, 200, new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                for (Divider divider : dividers) {
                    divider.alpha = (Integer) animation.getAnimatedValue();
                }
                invalidate();
            }
        });
        if (animateFor.equals("hide")) {
            animatorsAlphaTimeLabel.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    for (Divider divider : dividers) {
                        DetailView.this.dividers.remove(divider);
                    }
                }
            });
        }
        if (animateFor.equals("show")) {
            animatorsAlphaTimeLabel.start();
        } else {
            animatorsAlphaTimeLabel.reverse();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float tapX = event.getX();
        float tapY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                iAmPressed = true;
                tapPositionX = tapX;
                tapPositionY = tapY;

                if (animatorInfoWindowsAlpha.isRunning()) {
                    animatorInfoWindowsAlpha.cancel();
                }
                animatorInfoWindowsAlpha.start();
                checkInfoWindow();
                return true;
            }
            case MotionEvent.ACTION_MOVE: {
                if (iAmPressed) {
                    tapPositionX = tapX;
                    tapPositionY = tapY;

                    checkInfoWindow();
                }
                return true;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                iAmPressed = false;

                if (animatorInfoWindowsAlpha.isRunning()) {
                    animatorInfoWindowsAlpha.cancel();
                }
                animatorInfoWindowsAlpha.reverse();
                return true;
            }
            default:
                return false;
        }
    }

    private void checkInfoWindow() {
        boolean isChanged = false;
        if (tapPositionX + newInfoWindowPosition + infoWindowView.getWidth() > width) {
            newInfoWindowPosition = -INFO_WINDOW_FINGER_PADDING - infoWindowView.getWidth();
            isChanged = true;
        } else if (tapPositionX + newInfoWindowPosition < 0) {
            newInfoWindowPosition = INFO_WINDOW_FINGER_PADDING;
            isChanged = true;
        }
        if (isChanged) {
            if (animatorInfoWindowPosition.isRunning()) {
                animatorInfoWindowPosition.cancel();
            }
            animatorInfoWindowPosition.setFloatValues(infoWindowPosition, newInfoWindowPosition);
            animatorInfoWindowPosition.start();
        }
    }

    private boolean calculateExtendedTimeLimit() {
        float newMaxValue = 0;
        ChartItem chartItem;
        long start = (long) (((float) startPeriod * timeInPixel - paddingLeft) / timeInPixel);
        long end = (long) (((float) endPeriod * timeInPixel + paddingRight) / timeInPixel);

        extendedLeft = 0;
        if (start >= data.getItems().get(0).getTime() && start <= data.getItems().get(data.getItems().size() - 1).getTime()) {
            extendedLeft = SearchU.binarySearchLeft(data.getItems(), 0, data.getItems().size(), start);
        }
        extendedRight = data.getItems().size() - 1;
        if (end >= data.getItems().get(0).getTime() && end <= data.getItems().get(data.getItems().size() - 1).getTime()) {
            extendedRight = SearchU.binarySearchRight(data.getItems(), 0, data.getItems().size(), end);
        }

        extendedLeft = Math.max(extendedLeft, 0);
        extendedRight = Math.min(extendedRight, data.getItems().size() - 1);

        for (int i = extendedLeft; i <= extendedRight; i++) {
            chartItem = data.getItems().get(i);
            if (chartItem.getMaxValue() > newMaxValue) {
                newMaxValue = chartItem.getMaxValue();
            }
        }

        if (Math.abs(newMaxValueLocal - newMaxValue) >= topBottomChartPadding * 0.8f / valueInPixel || maxValueLocal == 0) { //0.8f - delta change max value for change dividers
            newMaxValueLocal = newMaxValue;
            return true;
        }
        return false;
    }

    @Override
    public void prepareDataForPrinting(boolean hardPrepare) {
        if (needRePrepare || hardPrepare) {
            //prepareLines
            preparePath = new ArrayList<>(data.getItems().get(0).getValues().size());
            Path path;
            for (int l = 0; l < data.getItems().get(0).getValues().size(); l++) {
                if (getView().linesForShow[l] || (lineIndex == l && !showLine)) {
                    path = new Path();
                    float curx;
                    float cury;
                    boolean firstPoint = true;
                    ChartItem chartItem;
                    for (int i = extendedLeft; i <= extendedRight; i++) {
                        chartItem = data.getItems().get(i);

                        curx = posX + (chartItem.getTime() - startPeriod) * timeInPixel + paddingLeft;
                        cury = posY + availableHeight + topBottomChartPadding - valueInPixel * chartItem.getValues().get(l);

                        if (firstPoint) {
                            firstPoint = false;
                            path.moveTo(curx, cury);
                        } else {
                            path.lineTo(curx, cury);
                        }
                    }
                } else {
                    path = null;
                }
                preparePath.add(path);
            }
            needRePrepare = false;
        }
    }

    @Override
    public void drawOn(Canvas canvas) {
        prepareDataForPrinting(false);

        //draw divisions lines
        Divider divider;
        float y;
        for (int i = 0; i < dividers.size(); i++) {
            divider = dividers.get(i);
            y = posY + availableHeight - valueInPixel * divider.value + topBottomChartPadding;
            pDivisionColor.setAlpha(divider.alpha);
            canvas.drawLine(posX + paddingLeft, y, posX + paddingLeft + availableWidth, y, pDivisionColor);
        }
        pDivisionColor.setAlpha(255);
        canvas.drawLine(posX + paddingLeft, posY + topBottomChartPadding + availableHeight, posX + paddingLeft + availableWidth, posY + topBottomChartPadding + availableHeight, pDivisionColor);

        //draw lines (chart)
        if (preparePath != null) {
            for (int l = 0; l < getView().linesForShow.length; l++) {
                if (preparePath.get(l) != null) {
                    if (lineIndex == l) {
                        pForLine.setColor(ColorU.colorSetA(data.getColors().get(l), alphaLine));
                        canvas.drawPath(preparePath.get(l), pForLine);
                    } else if (getView().linesForShow[l]) {
                        pForLine.setColor(data.getColors().get(l));
                        canvas.drawPath(preparePath.get(l), pForLine);
                    }
                }
            }
        }

        //draw divisions  labels
        for (int i = 0; i < dividers.size(); i++) {
            divider = dividers.get(i);
            y = posY + availableHeight - valueInPixel * divider.value + topBottomChartPadding - labelPadding;
            pForLabel.setAlpha(divider.alpha);
            canvas.drawText(String.format("%d", (int) divider.value), posX + paddingLeft, y, pForLabel);
        }
        pForLabel.setAlpha(255);
        canvas.drawText(String.format("%d", 0), posX + paddingLeft, posY + topBottomChartPadding + availableHeight - labelPadding, pForLabel);

        //draw time labels
        drawTimeLabels(canvas, timeLabels);
        drawTimeLabels(canvas, timeLabelsHide);

        //selected line
        if (iAmPressed || animatorInfoWindowsAlpha.isRunning()) {
            float tapValidPositionX = tapPositionX;
            boolean outOfRight = false;
            if (tapPositionX < paddingLeft) {
                tapValidPositionX = paddingLeft;
            }
            if (tapPositionX > paddingLeft + (int) availableWidth) {
                tapValidPositionX = paddingLeft + (int) availableWidth;
                outOfRight = true;
            }

            pForSelectedLine.setColor(ColorU.colorSetA(pForSelectedLine.getColor(), infoWindowsTransparent));
            canvas.drawLine(tapValidPositionX, posY + topBottomChartPadding, tapValidPositionX, posY + topBottomChartPadding + availableHeight, pForSelectedLine);

            //draw circle on lines
            long selectedTime = (long) ((tapValidPositionX - paddingLeft) / timeInPixel) + startPeriod;
            if (selectedTime < data.getItems().get(0).getTime()) {
                selectedTime = data.getItems().get(0).getTime();
            } else if (selectedTime > data.getItems().get(data.getItems().size() - 1).getTime()) {
                selectedTime = data.getItems().get(data.getItems().size() - 1).getTime();
            }
            int selectedNearLeftPosition = 0;
            int selectedNearRightPosition = 0;
            // TODO: 12.03.2019 use binary search
            for (int i = 0; i < data.getItems().size(); i++) {
                ChartItem chartItem = data.getItems().get(i);
                if (chartItem.getTime() == selectedTime) {
                    selectedNearLeftPosition = i;
                    selectedNearRightPosition = i;
                    break;
                } else if (selectedTime < chartItem.getTime()) {
                    selectedNearLeftPosition = Math.max(i - 1, 0);
                    selectedNearRightPosition = i;
                    break;
                }
            }

            ChartItem chartItemLeft = data.getItems().get(selectedNearLeftPosition);
            ChartItem chartItemRight = data.getItems().get(selectedNearRightPosition);
            float circlePositionY = 0;
            ArrayList<Integer> values = new ArrayList<>();
            for (int i = 0; i < chartItemLeft.getValues().size(); i++) {
                if (getView().linesForShow[i]) {
                    pForLine.setColor(data.getColors().get(i));
                    if (selectedNearLeftPosition == selectedNearRightPosition) {
                        circlePositionY = chartItemLeft.getValues().get(i);
                    } /*else if (selectedNearRightPosition == data.getItems().size() - 1 && outOfRight) {
                        circlePositionY = chartItemRight.getValues().get(i);
                    }*/ else {
                        //interpolation
                        circlePositionY = chartItemLeft.getValues().get(i) + //left data item
                                (chartItemRight.getValues().get(i) - chartItemLeft.getValues().get(i)) * // different between left and right value
                                        ( //x align proportion
                                                (float) (((long)((tapValidPositionX - paddingLeft) / timeInPixel) + startPeriod) - chartItemLeft.getTime()) / //finger position
                                                (chartItemRight.getTime() - chartItemLeft.getTime()) // time between two values
                                        );
                    }
                    values.add((int) circlePositionY);
                    circlePositionY = availableHeight - circlePositionY * valueInPixel + topBottomChartPadding;

                    pForEmptyCircle.setColor(ColorU.colorSetA(pForEmptyCircle.getColor(), infoWindowsTransparent));
                    pForLine.setColor(ColorU.colorSetA(pForLine.getColor(), infoWindowsTransparent));
                    canvas.drawCircle(tapValidPositionX, posY + circlePositionY, selectedCircleRadius, pForEmptyCircle);
                    canvas.drawCircle(tapValidPositionX, posY + circlePositionY, selectedCircleRadius, pForLine);
                } else {
                    values.add(0);
                }
            }

            //draw info window
            infoWindowView.setValues(selectedTime, values);
            infoWindowView.setPosX(tapValidPositionX + infoWindowPosition);
            infoWindowView.drawOn(canvas, infoWindowsTransparent);
        }
        super.drawOn(canvas);
    }

    private void drawTimeLabels(Canvas canvas, ArrayList<TimeLabel> timeLabels) {
        for (TimeLabel timeLabel : timeLabels) {
            if (timeLabel.alpha > 0) {
                pForLabel.setColor(ColorU.colorSetA(colors.detailLabelsFontColor, timeLabel.alpha));
                canvas.drawText(timeLabel.label,
                        posX + (timeLabel.time - startPeriod) * timeInPixel + paddingLeft,
                        posY + availableHeight + topBottomChartPadding + dimenLabelSize + labelPadding,
                        pForLabel);
            }
        }
    }

    private Pair<ArrayList<Divider>, ArrayList<Divider>> dividersCalc() {
        ArrayList<Divider> dividersForHide = new ArrayList<>();
        ArrayList<Divider> dividersForShow = new ArrayList<>();

        //hide old dividers
        for (Divider divider : dividers) {
            dividersForHide.add(divider);
        }
        float valSum = newMaxValueLocal / 6f; //divider interval
        //show new dividers
        while (newMaxValueLocal > valSum + 1) {
            Divider divider = new Divider((int) valSum);
            dividers.add(divider);
            dividersForShow.add(divider);
            valSum += newMaxValueLocal / 6f;
        }
        return new Pair<>(dividersForHide, dividersForShow);
    }

    @Override
    public void onTimeLineChanged(long start, long end, boolean changeZoom) {
        if (startPeriod != start || endPeriod != end) {
            needRePrepare = true;
            startPeriod = start;
            endPeriod = end;

            timeInPixel = (float) (width - paddingLeft * 2) / (endPeriod - startPeriod);

            boolean maxValueChange = calculateExtendedTimeLimit();
            if (maxValueLocal == 0) {
                maxValueLocal = newMaxValueLocal;
            }
            if (maxValueLocal != newMaxValueLocal) {
                if (maxValueChange) {
                    if (animatorMaxValue.isRunning()) {
                        animatorMaxValue.cancel();
                    }
                    animatorMaxValue.setFloatValues(maxValueLocal, newMaxValueLocal);
                    animatorMaxValue.start();

                    Pair<ArrayList<Divider>, ArrayList<Divider>> p = dividersCalc();
                    startDividersAnimator(p.first, "hide");
                    startDividersAnimator(p.second, "show");
                }
            }



            if (periodTimeLabel == 0) {
                if (extendedRight - extendedLeft < PART_PERIOD_OF_TIME) {
                    periodTimeLabel = 1;
                } else {
                    periodTimeLabel = (int) (((float) extendedRight - (float) extendedLeft) / PART_PERIOD_OF_TIME);
                }
            }
            if (changeZoom) {
                if (periodTimeLabel != 0) {
                    float widthBetween = data.getItems().get(periodTimeLabel).getTime() * timeInPixel - data.getItems().get(0).getTime() * timeInPixel; //width between two time label
                    if (widthBetween < timeLabelWidth) {
                        newPeriodTimeLabel = periodTimeLabel * 2;
                        if (newPeriodTimeLabel >= data.getItems().size())
                            newPeriodTimeLabel = data.getItems().size() - 1;
                    } else if (widthBetween > timeLabelWidth * 2) {
                        int cD = (int) (widthBetween / (timeLabelWidth * 2f)) + 1;
                        newPeriodTimeLabel = (int) ((float) periodTimeLabel / (float) cD);
                    }
                }
                if (periodTimeLabel != 0) {
                    if (timeLabels.size() == 0) {
                        ChartItem chartItem;
                        for (int i = extendedLeft; i <= extendedRight; i++) {
                            chartItem = data.getItems().get(i);
                            if (i % periodTimeLabel == 0) {
                                TimeLabel timeLabel = new TimeLabel(i, chartItem.getTime(), SearchU.firstCharUpper(dateFormat.format(new Date(chartItem.getTime()))));
                                timeLabel.alpha = 255;
                                timeLabels.add(timeLabel);
                            }
                        }
                    } else {
                        if (newPeriodTimeLabel > periodTimeLabel) {
                            periodTimeLabel = newPeriodTimeLabel;
                            ArrayList<TimeLabel> labelsForHide = new ArrayList<>();

                            Iterator<TimeLabel> itr = timeLabels.iterator();
                            TimeLabel tl;
                            while (itr.hasNext()) {
                                tl = itr.next();
                                if (tl.index % periodTimeLabel != 0) {
                                    labelsForHide.add(tl);
                                    timeLabelsHide.add(tl);
                                    itr.remove();
                                }
                            }

                            addTimeLabelIfNeed(255);

                            if (labelsForHide.size() > 0) {
                                startLabelAnimator(labelsForHide, "hide");
                            }
                        } else if (newPeriodTimeLabel != 0 && newPeriodTimeLabel < periodTimeLabel) {
                            periodTimeLabel = newPeriodTimeLabel;

                            ArrayList<TimeLabel> labelsForShow = new ArrayList<>();
                            labelsForShow.addAll(addTimeLabelIfNeed(0));

                            Iterator<TimeLabel> itr = timeLabels.iterator();
                            TimeLabel tl;
                            while (itr.hasNext()) {
                                tl = itr.next();
                                if (tl.index % periodTimeLabel != 0) {
                                    itr.remove();
                                }
                            }

                            if (labelsForShow.size() > 0) {
                                startLabelAnimator(labelsForShow, "show");
                            }
                        } else {
                            addTimeLabelIfNeed(255);
                        }
                    }
                }
            } else {
                addTimeLabelIfNeed(255);
            }
        }

    }

    private ArrayList<TimeLabel> addTimeLabelIfNeed(int startA) {
        if (periodTimeLabel == 0) {
            if (extendedRight - extendedLeft < PART_PERIOD_OF_TIME) {
                periodTimeLabel = 1;
            } else {
                periodTimeLabel = (int) (((float) extendedRight - (float) extendedLeft) / PART_PERIOD_OF_TIME);
            }
        }

        ArrayList<TimeLabel> labels = new ArrayList<>();
        ChartItem chartItem;
        for (int i = extendedLeft; i <= extendedRight; i++) {//todo could be optimized (do not need to check each element if you know period)
            if (i % periodTimeLabel == 0) {
                chartItem = data.getItems().get(i);

                boolean find = false;
                for (TimeLabel timeLabel : timeLabels) {
                    if (timeLabel.index == i) {
                        find = true;
                        break;
                    }
                }
                if (!find) {
                    TimeLabel timeLabel = new TimeLabel(i, chartItem.getTime(), SearchU.firstCharUpper(dateFormat.format(new Date(chartItem.getTime()))));
                    timeLabel.alpha = startA;
                    timeLabels.add(timeLabel);
                    labels.add(timeLabel);
                }
            }

        }
        return labels;
    }

    public void setPosY(float posY) {
        this.posY = posY;
        infoWindowView.setPosY(posY);
    }

    public void setData(ChartData data, long start, long end) {
        this.data = data;
        this.startPeriod = start;
        this.endPeriod = end;
        needRePrepare = true;
        infoWindowView.setNames(data.getNames());
        infoWindowView.setColors(data.getColors());
        infoWindowView.calcSizePositions();
        prepare();
    }

    public void prepare() {
        if (width != 0 && height != 0 && availableWidth != 0 && data != null && endPeriod != 0) {
            timeInPixel = (float) (width - paddingLeft * 2) / (endPeriod - startPeriod);
            calculateExtendedTimeLimit();
            maxValueLocal = newMaxValueLocal;
            valueInPixel = availableHeight / maxValueLocal;
            Pair<ArrayList<Divider>, ArrayList<Divider>> p = dividersCalc();
            startDividersAnimator(p.first, "hide");
            startDividersAnimator(p.second, "show");
            addTimeLabelIfNeed(255);
        }
    }

    @Override
    public void onShowLines(int lineIndex, boolean show) {
        this.lineIndex = lineIndex;
        this.showLine = show;

        needRePrepare = true;
        calculateExtendedTimeLimit();

        if (newMaxValueLocal != maxValueLocal) {
            animatorMaxValue.setFloatValues(maxValueLocal, newMaxValueLocal);
            animatorMaxValue.start();

            Pair<ArrayList<Divider>, ArrayList<Divider>> p = dividersCalc();
            startDividersAnimator(p.first, "hide");
            startDividersAnimator(p.second, "show");
        }
        if (show) {
            animatorLineAlpha.start();
        } else {
            animatorLineAlpha.reverse();
        }

        infoWindowView.onShowLines(lineIndex, show);

        if (timeLabels.size() == 0) {
            addTimeLabelIfNeed(255);
        }
    }

    @Override
    public void setWidth(int width) {
        super.setWidth(width);
        this.availableWidth = width - getPaddingLeft() - getPaddingRight();
    }

    @Override
    public void setHeight(int height) {
        super.setHeight(height);
        availableHeight = height - topBottomChartPadding * 2;
    }

    private class TimeLabel {
        int index;
        long time;
        String label;
        int alpha = 0;

        public TimeLabel(int index, long time, String label) {
            this.index = index;
            this.time = time;
            this.label = label;
        }
    }

    private class Divider {
        int value;
        int alpha = 0;

        public Divider(int value) {
            this.value = value;
        }
    }
}
