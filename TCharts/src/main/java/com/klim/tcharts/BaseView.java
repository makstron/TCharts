package com.klim.tcharts;

import android.content.Context;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.View;

public abstract class BaseView {
    protected Context context;
    protected TChart view;
    protected float posX;
    protected float posY;
    protected float width;
    protected float height;

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

    public abstract void prepareUi();

    protected void drawOn(Canvas canvas) {
        drawDebug(canvas, posX, posY, width, height);
    }

    public static void drawDebug(Canvas canvas) {
        drawDebug(canvas, 0, 0, canvas.getWidth(), canvas.getHeight());
    }

    public static void drawDebug(Canvas canvas, float x, float y, float width, float height) {
//        Paint tempP = new Paint();
//        tempP.setStyle(Paint.Style.STROKE);
//        tempP.setColor(Color.RED);
//        tempP.setStrokeWidth(1);
//        canvas.drawRect(x, y, x + width - 1, y + height - 1, tempP);
//        canvas.drawLine(x, y, x + width - 1, y + height - 1, tempP);
//        canvas.drawLine(x, y + height - 1, x + width - 1, y, tempP);
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

    public int getColor(/*@ColorRes*/ int id) {
        return context.getResources().getColor(id);
    }

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

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }
}
