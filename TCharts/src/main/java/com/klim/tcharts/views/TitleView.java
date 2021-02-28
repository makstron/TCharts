package com.klim.tcharts.views;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.view.View;

import com.klim.tcharts.Colors;
import com.klim.tcharts.R;
import com.klim.tcharts.utils.PaintU;

public class TitleView extends BaseView {
    private Colors colors;

    private Paint pForTitle;

    //    params
    public String title = "";
    public int titleFontSize = 0;
    public boolean showTitle = false;

    public TitleView(View view, Colors colors) {
        super(view);
        this.colors = colors;
    }

    public void init() {
        createPaints();
    }

    public int calculateHeight() {
        int height = 0;
        if (showTitle) {
            Rect textBounds = new Rect();
            pForTitle.getTextBounds("W", 0, "W".length(), textBounds);
            height = textBounds.height() + paddingTop + paddingBottom;
        } else {
            height = Math.round(getDimen(R.dimen.minTitleHeight));
        }
        return height;
    }

    private void createPaints() {
        pForTitle = PaintU.createPaint(colors.titleFontColor, Paint.Style.FILL);
        pForTitle.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        pForTitle.setTextSize(titleFontSize);
    }

    @Override
    public void onSizeChanged() {
    }

    @Override
    public void prepareDataForPrinting(boolean hardPrepare) {

    }

    @Override
    public void drawOn(Canvas canvas) {
        if (showTitle) {
            canvas.drawText(title, posX + paddingLeft, posY + height - paddingTop, pForTitle);
        }
    }

}
