package com.klim.tcharts.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;

import com.klim.tcharts.TChart;

public abstract class BaseView {
    protected Context context;
    protected TChart view;
    protected float posX;
    protected float posY;
    protected int width;
    protected int height;
    protected int paddingTop = 0;
    protected int paddingBottom = 0;
    protected int paddingLeft = 0;
    protected int paddingRight = 0;

    protected float paddingX;

    protected boolean iAmPressed = false;

    public BaseView(View view) {
        this.view = (TChart) view;
        this.context = view.getContext();
    }

    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }

    public boolean tapOnMe(float x, float y) {
        return posX <= x && x <= posX + width && posY <= y && y <= posY + height;
    }

    public abstract void prepareDataForPrinting(boolean hardPrepare);

    void drawOn(Canvas canvas) {

    }

     void onSizeChanged() {

     }

    public void drawDebug(Canvas canvas, boolean isDebug) {
        if (isDebug) {
            drawDebug(canvas, posX, posY, width, height);
        }
    }


    public void drawDebug(Canvas canvas, float x, float y, float width, float height) {
        //todo do not need paint alvais reinit
        Paint tempP = new Paint();
        tempP.setStyle(Paint.Style.STROKE);
        tempP.setColor(Color.RED);
        tempP.setStrokeWidth(1);
        canvas.drawRect(x, y, x + width - 1, y + height - 1, tempP);
        canvas.drawLine(x, y, x + width - 1, y + height - 1, tempP);
        canvas.drawLine(x, y + height - 1, x + width - 1, y, tempP);

        canvas.drawRect(x, y, x + width - 1, y + height - 1, tempP);

        tempP.setStyle(Paint.Style.FILL);
        tempP.setColor(Color.parseColor("#50FF0000"));

        canvas.drawRect(x + paddingLeft, y, x + width - paddingRight, y + paddingTop, tempP); //top
        canvas.drawRect(x, y + paddingTop, x + paddingLeft, y + height - paddingBottom, tempP); //left
        canvas.drawRect(x + width - paddingRight, y + paddingTop, x + width, y + height - paddingBottom, tempP); //right
        canvas.drawRect(x + paddingLeft, y + height - paddingBottom, x + width - paddingRight, y + height, tempP); //bottom
    }

    public void invalidate() {
        view.invalidate();
    }

    protected TChart getView() {
        return view;
    }

    public void onRestoreInstantState() {

    }

    public int getInt(/*@IntegerRes*/ int id) {
        return context.getResources().getInteger(id);
    }

    public float getDimen(/*@DimenRes*/ int id) {
        return context.getResources().getDimension(id);
    }

//    public int getColor(/*@ColorRes*/ int id) {
//        return context.getResources().getColor(id);
//    }

    public boolean isiAmPressed() {
        return iAmPressed;
    }

    public float getPosX() {
        return posX;
    }

    public void setPosX(float posX) {
        this.posX = posX;
    }

    public float getPosY() {
        return posY;
    }

    public void setPosY(float posY) {
        this.posY = posY;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getPaddingTop() {
        return paddingTop;
    }

    public void setPaddingTop(int paddingTop) {
        this.paddingTop = paddingTop;
    }

    public int getPaddingBottom() {
        return paddingBottom;
    }

    public void setPaddingBottom(int paddingBottom) {
        this.paddingBottom = paddingBottom;
    }

    public int getPaddingLeft() {
        return paddingLeft;
    }

    public void setPaddingLeft(int paddingLeft) {
        this.paddingLeft = paddingLeft;
    }

    public int getPaddingRight() {
        return paddingRight;
    }

    public void setPaddingRight(int paddingRight) {
        this.paddingRight = paddingRight;
    }
}
