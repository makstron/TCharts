package com.klim.tcharts;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class TChart extends View {

    //paints
    private Paint pFill;

    public TChart(Context context) {
        super(context);
        init(null);
    }

    public TChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public TChart(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        pFill = createPaint(this.getResources().getColor(R.color.chartBackgroundColor), Paint.Style.FILL);
    }

    public static Paint createPaint(int color, Paint.Style style) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(color);
        paint.setStyle(style);
        return paint;
    }



    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPaint(pFill);
    }
}
