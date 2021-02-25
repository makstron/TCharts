package com.klim.tcharts;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.klim.tcharts.entities.ChartData;
import com.klim.tcharts.interfaces.OnSelectedTimeLineChanged;
import com.klim.tcharts.utils.PaintU;

public class TChart extends View implements OnSelectedTimeLineChanged {
    private float width = 0;
    private float paddingX = 0;
    private float detailViewSize = 0;
    private int detailViewHeight = 0;
    private int divisionCount = 0;
    private int chartNavLineHeight = 0;

    private DetailView detailView;
    private NavigationView navigationView;

    protected boolean[] linesForShow;
    private boolean saveShowedLines;

    //parametrs
    private String title = "";

    private float titleHeight = 0;

    private ChartData data = null;
    private long selectedStartTime = 0;
    private long selectedEndTime = 0;

    //paints
    private Paint pFill;
    private Paint paintForTopShadow;
    private Paint pForTitle;

    private RectF topShadowSize;
    private Shader shader;

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
        setLayerType(View.LAYER_TYPE_HARDWARE, null);

        // TODO: 10.03.2019 add other parameters
        TypedArray a = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.TChart, 0, 0);

        try {
            title = a.getString(R.styleable.TChart_title);
        } finally {
            a.recycle();
        }

        paddingX = getResources().getDimension(R.dimen.chartPadding);
        detailViewSize = getResources().getDimension(R.dimen.detailViewSize);
        detailViewHeight = (int) getResources().getDimension(R.dimen.detailViewSize);
        divisionCount = 6;
        chartNavLineHeight = (int) getResources().getDimension(R.dimen.chartNavLineHeight);
        initPaints();

        navigationView = new NavigationView(this, chartNavLineHeight);
        detailView = new DetailView(this, detailViewHeight, divisionCount);

        navigationView.addOnSelectedTimeLineChanged(detailView);
        navigationView.addOnSelectedTimeLineChanged(this);


        paintForTopShadow = new Paint();
        paintForTopShadow.setShader(shader);

        topShadowSize = new RectF(0, 0, width, paddingX);
    }

    public void showLine(String lineKey, boolean show) {
        int position = 0;
        for (int i = 0; i < data.getKeys().size(); i++) {
            if (data.getKeys().get(i).equals(lineKey)) {
                position = i;
                break;
            }
        }
        linesForShow[position] = show;
        data.updateLinesForShow(linesForShow);
        navigationView.onShowLines(position, show);
        detailView.onShowLines(position, show);
    }

    private void initPaints() {
        pFill = PaintU.createPaint(this.getResources().getColor(R.color.chartBackgroundColor), Paint.Style.FILL);
        pForTitle = PaintU.createPaint(getResources().getColor(R.color.titleColor), Paint.Style.FILL);
        pForTitle.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        pForTitle.setTextSize(getResources().getDimension(R.dimen.chartTitleSize));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredWidth = getLayoutParams().width;
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);

        //Measure Width
        if (widthMode == MeasureSpec.EXACTLY) {
            //Must be this size
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            width = Math.min(desiredWidth, widthSize);
        } else {
            //Be whatever you want
            width = desiredWidth;
        }

        float availableWidth = width - paddingX * 2f;
        navigationView.setWidth((int) availableWidth);
        detailView.setWidth(width);
        detailView.setAvailableWidth(availableWidth);
        topShadowSize.right = width;

        //HEIGHT
        int height = 0;
        height += paddingX;

        if (title != null && !title.isEmpty()) {
            Rect textBounds = new Rect();
            pForTitle.getTextBounds(title, 0, title.length(), textBounds);
            titleHeight = textBounds.height();
            height += titleHeight;
        }

        height += paddingX;
        topShadowSize.bottom = height;
        shader = new LinearGradient(0, 0, 0, height, this.getResources().getColor(R.color.chartBackgroundColor), Color.TRANSPARENT, Shader.TileMode.CLAMP);
        paintForTopShadow.setShader(shader);

        detailView.setPosX(0);
        detailView.setPadding(paddingX);
        detailView.setPosY(height);

        height += detailViewSize;
        height += paddingX;

        navigationView.setPosX(paddingX);
        navigationView.setPosY(height);

        detailView.prepare();
        navigationView.prepare();

        height += chartNavLineHeight;
        height += paddingX / 2;

        height += paddingX;

        setMeasuredDimension(widthMeasureSpec, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawPaint(pFill);

        if (data != null) {
            detailView.drawOn(canvas);
            navigationView.drawOn(canvas);
        }

        //draw top shadow
        canvas.drawRect(topShadowSize, paintForTopShadow);

        //drawTitle
        if (title != null && !title.isEmpty()) {
            canvas.drawText(title, paddingX, paddingX + titleHeight, pForTitle);
        }

        BaseView.drawDebug(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float tapY = event.getY();
        boolean res = false;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                if (navigationView.getPosY() < tapY && tapY < navigationView.getPosY() + navigationView.getHeight()) {
                    res = navigationView.onTouchEvent(event);
                }
                if (detailView.getPosY() < tapY && tapY < detailView.getPosY() + detailView.getHeight()) {
                    res = detailView.onTouchEvent(event);
                }
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                if (navigationView.isiAmPressed()) {
                    res = navigationView.onTouchEvent(event);
                } else if (detailView.isiAmPressed()) {
                    res = detailView.onTouchEvent(event);
                }
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                if (navigationView.isiAmPressed()) {
                    res = navigationView.onTouchEvent(event);
                } else if (detailView.isiAmPressed()) {
                    res = detailView.onTouchEvent(event);
                }
                break;
            }
            default:
                res = false;
        }

        if (res) {
            navigationView.prepareUi();
            detailView.prepareUi();

            invalidate();
        }
        return res;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("superState", super.onSaveInstanceState());
        bundle.putLong("curStartTime", selectedStartTime);
        bundle.putLong("curEndTime", selectedEndTime);
        bundle.putBooleanArray("linesForShow", linesForShow);
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state != null && state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            this.selectedStartTime = bundle.getLong("curStartTime");
            this.selectedEndTime = bundle.getLong("curEndTime");
            if (saveShowedLines) {
                this.linesForShow = bundle.getBooleanArray("linesForShow");
                data.updateLinesForShow(linesForShow);
            }
            state = bundle.getParcelable("superState");
        }
        super.onRestoreInstanceState(state);

        navigationView.setStartPeriodTimes(selectedStartTime, selectedEndTime);
    }

    @Override
    public void onTimeLineChanged(long start, long end, boolean changeZoom) {
        selectedStartTime = start;
        selectedEndTime = end;
    }

    public void setData(ChartData data, boolean saveShowedLines) {
        this.data = data;
        this.saveShowedLines = saveShowedLines;
        linesForShow = new boolean[data.getNames().size()];
        for (int i = 0; i < data.getNames().size(); i++) {
            linesForShow[i] = true;
        }

        selectedStartTime = data.getItems().get(data.getItems().size() - (int) (data.getItems().size() / 3)).getTime();
        selectedEndTime = data.getItems().get(data.getItems().size() - 1).getTime();

        navigationView.setData(data);
        detailView.setData(data);

        navigationView.setStartPeriodTimes(selectedStartTime, selectedEndTime);
    }

    //params
    public void setTitle(String title) {
        this.title = title;
    }
}
