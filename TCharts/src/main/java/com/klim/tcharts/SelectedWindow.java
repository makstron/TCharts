package com.klim.tcharts;

import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.view.MotionEvent;
import android.view.View;

import com.klim.tcharts.enums.BorderType;

import com.klim.tcharts.interfaces.OnSelectedTimeLineChanged;
import com.klim.tcharts.utils.AnimatorU;
import com.klim.tcharts.utils.PaintU;

import java.util.ArrayList;

public class SelectedWindow extends BaseView {

    private Paint pForGray;
    private Paint pen;
    private Paint pTapCircle;

    private Point tapPosition;

    private BorderView borderViewLeft;
    private BorderView borderViewRight;

    private ArrayList<OnSelectedTimeLineChanged> onSelectedTimeLineChanged = new ArrayList<>();

    private boolean selected = false;

    private ValueAnimator animCirclRadius;
    private float maxCircRadius;
    private float curCircRadius;

    private long startShowMinTime;
    private long startShowMaxTime;

    private float timeInPixel;
    private long minTime;
    private long maxTime;

    public SelectedWindow(View view, float height) {
        super(view);
        this.height = height;

        pForGray = PaintU.createPaint(getColor(R.color.navGray), Paint.Style.FILL);
        pen = PaintU.createPaint(getColor(R.color.navGrayBorder), Paint.Style.FILL);
        pTapCircle = PaintU.createPaint(getColor(R.color.navTapCircle), Paint.Style.FILL);

        borderViewLeft = new BorderView(view, BorderType.LEFT, 400, posY, getDimen(R.dimen.chartNavBorderWidth), height, pen);
        borderViewRight = new BorderView(view, BorderType.RIGHT, posX + width, posY, getDimen(R.dimen.chartNavBorderWidth), height, pen);

        maxCircRadius = height * 0.8f;

        createAnimators();
    }

    private void createAnimators() {
        animCirclRadius = AnimatorU.createFloatValueAnimator(0, maxCircRadius, 200, new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                curCircRadius = (Float) animation.getAnimatedValue();
                invalidate();
            }
        });
    }

    @Override
    public void prepareUi() {

    }

    @Override
    public void drawOn(Canvas canvas) {
        //left gray part
        canvas.drawRect(posX, posY, borderViewLeft.getPosX(), posY + height, pForGray);

        //borders
        borderViewLeft.drawOn(canvas);
        borderViewRight.drawOn(canvas);
        //line between
        canvas.drawRect(borderViewLeft.getPosX() + borderViewLeft.getWidth(), posY, borderViewRight.getPosX(), posY + getDimen(R.dimen.chartNavBorderTopHeight), pen);
        canvas.drawRect(borderViewLeft.getPosX() + borderViewLeft.getWidth(), posY + height - getDimen(R.dimen.chartNavBorderTopHeight), borderViewRight.getPosX(), posY + height, pen);

        //right gray part
        canvas.drawRect(borderViewRight.getPosX() + borderViewRight.getWidth(), posY, posX + width, posY + height, pForGray);

        //tap circle
        if (tapPosition != null) {
            canvas.drawCircle(tapPosition.x, tapPosition.y, curCircRadius, pTapCircle);
        }

        super.drawOn(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float tapX = event.getX();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                actionDown(tapX);
                return true;
            }
            case MotionEvent.ACTION_MOVE: {
                if (Math.abs(tapX) >= 1) {
                    actionMove(tapX);
                }
                return true;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                actionUp();
                return true;
            }
            default:
                return false;
        }
    }

    private void actionDown(float tapX) {
        if (borderViewLeft.isItMe(tapX)) { //left border
            borderViewLeft.setSelected(true);
            borderViewLeft.setShiftPositinTap(tapX - borderViewLeft.getPosX());
        } else if (borderViewLeft.getPosX() + borderViewLeft.getWidth() < tapX && tapX < borderViewRight.getPosX()) { //center
            selected = true;
            borderViewLeft.setShiftPositinTap(tapX - borderViewLeft.getPosX());
            borderViewRight.setShiftPositinTap(tapX - borderViewRight.getPosX());
        } else if (borderViewRight.isItMe(tapX)) { //right border
            borderViewRight.setSelected(true);
            borderViewRight.setShiftPositinTap(tapX - borderViewRight.getPosX());
        }

        if (borderViewLeft.isSelected() || selected || borderViewRight.isSelected()) {
            tapPosition = new Point((int) tapX, (int) (posY + height / 2));
            if (animCirclRadius.isRunning()) {
                animCirclRadius.cancel();
            }
            animCirclRadius.setFloatValues(curCircRadius, maxCircRadius);
            animCirclRadius.start();
        }
    }

    private void actionMove(float tapX) {
        if (borderViewLeft.isSelected()) {
            float newPosL = tapX - borderViewLeft.getShiftPositinTap();
            if (newPosL < posX) {
                newPosL = posX;
            } else if (newPosL + borderViewLeft.getWidth() > borderViewRight.getPosX()) {
                newPosL = borderViewRight.getPosX() - borderViewLeft.getWidth();
            }
            borderViewLeft.setPosX(newPosL);
        } else if (selected) {
            float newPosL = tapX - borderViewLeft.getShiftPositinTap();
            float newPosR = tapX - borderViewRight.getShiftPositinTap();
            float distance = borderViewRight.getPosX() - borderViewLeft.getPosX();
            if (newPosL < posX) {
                newPosL = posX;
                newPosR = newPosL + distance;
            } else if (newPosR + borderViewRight.getWidth() > posX + width) {
                newPosR = posX + width - borderViewLeft.getWidth();
                newPosL = newPosR - distance;
            }
            borderViewLeft.setPosX(newPosL);
            borderViewRight.setPosX(newPosR);
        } else if (borderViewRight.isSelected()) {
            float newPosR = tapX - borderViewRight.getShiftPositinTap();
            if (newPosR < borderViewLeft.getPosX() + borderViewLeft.getWidth()) {
                newPosR = borderViewLeft.getPosX() + borderViewLeft.getWidth();
            } else if (newPosR + borderViewRight.getWidth() > posX + width) {
                newPosR = posX + width - borderViewLeft.getWidth();
            }
            borderViewRight.setPosX(newPosR);
        }

        if (borderViewLeft.isSelected() || selected || borderViewRight.isSelected()) {
            tapPosition.x = (int) tapX;
        }

        sendTimeLineChanged();
    }

    private void sendTimeLineChanged() {
        onSelectedTimeLineChanged = onSelectedTimeLineChanged;
        if (onSelectedTimeLineChanged != null) {
            for (OnSelectedTimeLineChanged onSelectedTimeLineChanged : onSelectedTimeLineChanged)
                onSelectedTimeLineChanged.onTimeLineChanged(
                        Math.max((long) ((borderViewLeft.getPosX() - posX) / timeInPixel) + minTime, minTime),
                        Math.min((long) ((borderViewRight.getPosX() + borderViewRight.getWidth() - posX) / timeInPixel) + minTime, maxTime),
                        !selected);
        }
    }

    private void actionUp() {
        borderViewLeft.setSelected(false);
        selected = false;
        borderViewRight.setSelected(false);

        if (animCirclRadius.isRunning()) {
            animCirclRadius.cancel();
        }
        animCirclRadius.setFloatValues(curCircRadius, 0);
        animCirclRadius.start();
    }

    public void setStartPeriodTimes(long start, long end) {
        startShowMinTime = start;
        startShowMaxTime = end;
        agrrr();
    }

    @Override
    public void setPosY(float posY) {
        super.setPosY(posY);
        borderViewLeft.setPosY(posY);
        borderViewRight.setPosY(posY);
    }

    @Override
    public void setHeight(float height) {
        super.setHeight(height);
        borderViewLeft.setHeight(height);
        borderViewRight.setHeight(height);
        agrrr();
    }

    @Override
    public void setWidth(float width) {
        super.setWidth(width);
        agrrr();
    }

    private void agrrr() {
        if (width != 0 && height != 0 && startShowMinTime != 0 && startShowMaxTime != 0 && minTime != 0 && maxTime != 0) {
            moveBordersToStart();
        }
    }

    private void moveBordersToStart() {
        borderViewLeft.setPosX((startShowMinTime - minTime) * timeInPixel + posX);
        if (startShowMaxTime == maxTime) {
            borderViewRight.setPosX(posX + width - borderViewRight.getWidth());
        } else {
            borderViewRight.setPosX((startShowMaxTime - minTime) * timeInPixel - borderViewRight.getWidth() + posX);
        }
        sendTimeLineChanged();
    }

    public void addOnSelectedTimeLineChanged(OnSelectedTimeLineChanged onSelectedTimeLineChanged) {
        this.onSelectedTimeLineChanged.add(onSelectedTimeLineChanged);
    }

    public void setTimeInPixel(float timeInPixel, long minTime, long maxTime) {
        this.timeInPixel = timeInPixel;
        this.minTime = minTime;
        this.maxTime = maxTime;
    }
}
