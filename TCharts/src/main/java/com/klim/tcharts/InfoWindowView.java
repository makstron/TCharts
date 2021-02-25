package com.klim.tcharts;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.View;

import com.klim.tcharts.interfaces.OnShowLinesListener;
import com.klim.tcharts.utils.ColorU;
import com.klim.tcharts.utils.PaintU;
import com.klim.tcharts.utils.SearchU;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class InfoWindowView extends BaseView implements OnShowLinesListener {

    private float infoWindowRoutedCorners;
    private float padding;
    private float infoWindowShadow;
    private float infoWindowTitleSize;
    private float infoWindowLabelSize;
    private float infoWindowLabelValSize;

    private long time;
    private ArrayList<String> names;
    private ArrayList<Integer> colors;
    private ArrayList<Integer> values;

    private Paint bBitmap;
    private Paint pBackground;
    private Paint pTitle;
    private Paint pLabel;
    private Paint pValue;

    //cache background
    private Bitmap bitmapBgCache;
    private Canvas canvasBgCache;

    //positions elements in info window
    private ArrayList<Point> posTitle = new ArrayList<>();
    private ArrayList<Point> posLabel = new ArrayList<>();
    private ArrayList<Point> posValues = new ArrayList<>();

    private SimpleDateFormat dayFormat = new SimpleDateFormat("E,");
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd");

    public InfoWindowView(View view) {
        super(view);

        infoWindowRoutedCorners = getDimen(R.dimen.infoWindowRoutedCorners);
        padding = getDimen(R.dimen.infoWindowPadding);
        infoWindowShadow = getDimen(R.dimen.infoWindowShadow);
        infoWindowTitleSize = getDimen(R.dimen.infoWindowTitleSize);
        infoWindowLabelSize = getDimen(R.dimen.infoWindowLabelSize);
        infoWindowLabelValSize = getDimen(R.dimen.infoWindowLabelValSize);

        createPaints();
    }

    private void createPaints() {
        bBitmap = new Paint(Paint.FILTER_BITMAP_FLAG);

        pBackground = PaintU.createPaint(getColor(R.color.infoWindowBackground), Paint.Style.FILL);
        pBackground.setShadowLayer(infoWindowShadow, 0.0f, 0.0f, getColor(R.color.infoWindowShadowColor));

        pTitle = PaintU.createPaint(getColor(R.color.infoWindowTitleColor), Paint.Style.FILL);
        pTitle.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        pTitle.setTextSize(infoWindowTitleSize);

        pLabel = PaintU.createPaint(Color.WHITE, Paint.Style.FILL);
        pLabel.setTextSize(infoWindowLabelSize);

        pValue = PaintU.createPaint(Color.WHITE, Paint.Style.FILL);
        pValue.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        pValue.setTextSize(infoWindowLabelValSize);
    }

    @Override
    public void prepareUi() {

    }

//    @Override
    public void drawOn(Canvas canvas, int a) {
        super.drawOn(canvas);
        //draw background
        if (bitmapBgCache == null) {
            bitmapBgCache = Bitmap.createBitmap((int) (getWidth() + infoWindowShadow * 2), (int) (getHeight() + infoWindowShadow * 2), Bitmap.Config.ARGB_8888);
            canvasBgCache = new Canvas(bitmapBgCache);
            canvasBgCache.drawRoundRect(new RectF(infoWindowShadow, infoWindowShadow, getWidth() + infoWindowShadow, getHeight() + infoWindowShadow), infoWindowRoutedCorners, infoWindowRoutedCorners, pBackground);
        }

        bBitmap.setColor(ColorU.colorSetA(bBitmap.getColor(), a));
        canvas.drawBitmap(bitmapBgCache, posX - infoWindowShadow, posY - infoWindowShadow, bBitmap);

        //draw title (date)
        Date date = new Date(time);
        pTitle.setColor(ColorU.colorSetA(pTitle.getColor(), a));
        canvas.drawText(SearchU.firstCharUpper(dayFormat.format(date)), posTitle.get(0).x + posX, posTitle.get(0).y + posY, pTitle);
        canvas.drawText(SearchU.firstCharUpper(dateFormat.format(date)), posTitle.get(1).x + posX, posTitle.get(1).y + posY, pTitle);

        //draw values
        for (int i = 0; i < values.size(); i++) {
            if (getView().linesForShow[i]) {
                pLabel.setColor(ColorU.colorSetA(colors.get(i), a));
                pValue.setColor(ColorU.colorSetA(colors.get(i), a));
                canvas.drawText(names.get(i), posLabel.get(i).x + posX, posLabel.get(i).y + posY, pLabel);
                canvas.drawText(values.get(i) + "", posValues.get(i).x + posX, posValues.get(i).y + posY, pValue);
            }
        }
        super.drawOn(canvas);
    }

    /**
     * Calculate width & height for info window
     * Calculate positions for info window elements
     */
    public void calcSizePositions() {
        height = 0;
        width = 0;
        bitmapBgCache = null;

        float widthDayOfWeek = 0;
        float widthMonth = 0;

        posTitle.clear();
        posLabel.clear();
        posValues.clear();

        //position for title
        height += padding;
        height += infoWindowTitleSize;
        posTitle.add(new Point((int) (padding), (int) (height))); //week day

        Rect textBounds = new Rect();
        Calendar calendar = new GregorianCalendar();
        SimpleDateFormat format = new SimpleDateFormat("E,_", context.getResources().getConfiguration().locale);
        String temp = "";
        for (int day = Calendar.SUNDAY; day <= Calendar.SATURDAY; day++) {
            calendar.set(Calendar.DAY_OF_WEEK, day);
            temp = format.format(calendar.getTime());
            pTitle.getTextBounds(temp, 0, temp.length(), textBounds);
            widthDayOfWeek = Math.max(widthDayOfWeek, textBounds.width());
        }

        posTitle.add(new Point((int) (padding + widthDayOfWeek), (int) (height))); //month and number

        format.applyPattern("MMM 29");
        for (int month = Calendar.JANUARY; month <= Calendar.DECEMBER; month++) {
            calendar.set(Calendar.MONTH, month);
            temp = format.format(calendar.getTime());
            pTitle.getTextBounds(temp, 0, temp.length(), textBounds);
            widthMonth = Math.max(widthMonth, textBounds.width());
        }

        width = Math.max(width, widthDayOfWeek + widthMonth + padding * 2);

        height += padding;

        //position for labels and values
        for (int i = 0; i < names.size(); i++) {
            if (getView().linesForShow[i]) {
                height += infoWindowLabelSize;
                posLabel.add(new Point((int) (padding), (int) (height))); //label
                height += infoWindowLabelValSize;
                posValues.add(new Point((int) (padding * 3), (int) (height))); //value
            } else {
                posLabel.add(new Point(0, 0)); //label
                posValues.add(new Point(0, 0)); //value
            }
        }

        height += padding;
    }

    public void setNames(ArrayList<String> names) {
        this.names = names;
    }

    public void setColors(ArrayList<Integer> colors) {
        this.colors = colors;
    }

    public void setValues(long time, ArrayList<Integer> values) {
        this.time = time;
        this.values = values;
    }

    @Override
    public void onShowLines(int lineIndex, boolean show) {
        bitmapBgCache = null;
        calcSizePositions();
    }
}
