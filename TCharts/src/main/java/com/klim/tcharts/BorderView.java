package com.klim.tcharts;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

import com.klim.tcharts.enums.BorderType;

public class BorderView extends BaseView {
    private BorderType type;

    private Paint pen;
    private Paint penDebug;

    private boolean selected = false;
    private float shiftPositinTap;

    public BorderView(View view, BorderType type, float posX, float posY, float width, float height, Paint pen) {
        super(view);
        this.posX = posX;
        this.posY = posY;
        this.width = width;
        this.height = height;
        this.type = type;
        this.pen = pen;

        penDebug = new Paint();
        penDebug.setColor(Color.RED);
        penDebug.setStrokeWidth(2);
    }

    public boolean isItMe(float tapX) {
        switch (type) {
            case LEFT:
                return posX - width * 2 < tapX && tapX < posX + width;
            case RIGHT:
                return posX < tapX && tapX < posX + width * 3;
        }
        return false;
    }

    @Override
    public void prepareUi() {

    }

    @Override
    protected void drawOn(Canvas canvas) {
        canvas.drawRect(posX, posY, posX + width, posY + height, pen);
//        switch (type) {
//            case LEFT: {
//                canvas.drawLine(posX - width * 2, posY, posX + width, posY + height, penDebug);
//                break;
//            }
//            case RIGHT: {
//                canvas.drawLine(posX, posY, posX + width * 3, posY + height, penDebug);
//                break;
//            }
//        }

        super.drawOn(canvas);
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public float getShiftPositinTap() {
        return shiftPositinTap;
    }

    public void setShiftPositinTap(float shiftPositinTap) {
        this.shiftPositinTap = shiftPositinTap;
    }
}
