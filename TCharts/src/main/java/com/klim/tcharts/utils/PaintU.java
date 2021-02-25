package com.klim.tcharts.utils;

import android.graphics.Paint;

public class PaintU {

    public static Paint createPaint(int color, Paint.Style style) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(color);
        paint.setStyle(style);
        return paint;
    }

    public static Paint createPaint(int color, Paint.Style style, float strokeWidth) {
        Paint p = createPaint(color, style);
        p.setStrokeWidth(strokeWidth);
        return p;
    }
}
