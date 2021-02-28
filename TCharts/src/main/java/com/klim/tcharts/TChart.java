package com.klim.tcharts;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.klim.tcharts.entities.ChartData;
import com.klim.tcharts.entities.ChartItem;
import com.klim.tcharts.interfaces.OnSelectedTimeLineChanged;
import com.klim.tcharts.utils.PaintU;
import com.klim.tcharts.views.DetailView;
import com.klim.tcharts.views.NavigationView;
import com.klim.tcharts.views.TitleView;

import java.util.ArrayList;
import java.util.Random;

public class TChart extends View implements OnSelectedTimeLineChanged {
    private int width = 0;
    private int height = 0;

    private int titlePaddingTop = 0;
    private int titlePaddingBottom = 0;

    private int divisionCount = 0;

    private int detailHeight = 0;
    private int navHeight = 0;

    private TitleView titleView;
    private DetailView detailView;
    private NavigationView navigationView;

    public boolean[] linesForShow;

    //parametrs
    private boolean debug = false; //todo get from settings
    private String title = "";
    private int titleFontSize = 0;
    private boolean showTitle = false;
    private Colors colors = new Colors();

    private ChartData data = null;
    private long selectedStartTime = 0;
    private long selectedEndTime = 0;

    //paints
    private Paint pFill;
    private Paint paintForTopShadow;

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

        int [] attributes = new int [] {android.R.attr.paddingLeft, android.R.attr.paddingTop, android.R.attr.paddingRight, android.R.attr.paddingBottom};
        TypedArray arr = getContext().obtainStyledAttributes(attrs, attributes);
        int leftPadding = 0;
        int topPadding = 0;
        int rightPadding = 0;
        int bottomPadding = 0;
        try {
            leftPadding = arr.getDimensionPixelOffset(0, Math.round(getResources().getDimension(R.dimen.paddingDefault)));
            topPadding = arr.getDimensionPixelOffset(1, Math.round(getResources().getDimension(R.dimen.paddingDefault)));
            rightPadding = arr.getDimensionPixelOffset(2, Math.round(getResources().getDimension(R.dimen.paddingDefault)));
            bottomPadding = arr.getDimensionPixelOffset(3, Math.round(getResources().getDimension(R.dimen.paddingDefault)));
        } finally {
            arr.recycle();
        }
        setPadding(leftPadding, topPadding, rightPadding, bottomPadding);

        TypedArray a = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.TChart, 0, 0);
        try {
            colors.backgroundColor = a.getColor(R.styleable.TChart_backgroundColor, ContextCompat.getColor(getContext(), R.color.chartBackground));

            title = a.getString(R.styleable.TChart_title);
            titleFontSize = a.getDimensionPixelSize(R.styleable.TChart_titleFontSize, Math.round(getResources().getDimension(R.dimen.titleFontSize)));
            showTitle = a.getBoolean(R.styleable.TChart_showTitle, showTitle);
            colors.titleFontColor = a.getColor(R.styleable.TChart_titleFontColor, ContextCompat.getColor(getContext(), R.color.titleFont));

            colors.detailLabelsFontColor = a.getColor(R.styleable.TChart_detailLabelsFontColor, ContextCompat.getColor(getContext(), R.color.detailLabelsFont));
            colors.detailDivisionColor = a.getColor(R.styleable.TChart_detailDivisionColor, ContextCompat.getColor(getContext(), R.color.detailDivision));
            colors.detailLineSelectedPosition = a.getColor(R.styleable.TChart_detailLineSelectedPosition, ContextCompat.getColor(getContext(), R.color.lineSelectedPosition));

            colors.infoWindowBackground = a.getColor(R.styleable.TChart_infoWindowBackground, ContextCompat.getColor(getContext(), R.color.infoWindowBackground));
            colors.infoWindowShadowColor = a.getColor(R.styleable.TChart_infoWindowShadowColor, ContextCompat.getColor(getContext(), R.color.infoWindowShadow));
            colors.infoWindowTitleColor = a.getColor(R.styleable.TChart_infoWindowTitleColor, ContextCompat.getColor(getContext(), R.color.infoWindowTitle));

            colors.navFillColor = a.getColor(R.styleable.TChart_navViewFillColor, ContextCompat.getColor(getContext(), R.color.navFill));
            colors.navBordersColor = a.getColor(R.styleable.TChart_navBordersColor, ContextCompat.getColor(getContext(), R.color.navBorders));
            colors.navTapCircleColor = a.getColor(R.styleable.TChart_navTapCircleColor, ContextCompat.getColor(getContext(), R.color.navTapCircle));
        } finally {
            a.recycle();
        }

        titlePaddingTop = Math.round(getResources().getDimension(R.dimen.titlePaddingTop));
        titlePaddingBottom = Math.round(getResources().getDimension(R.dimen.titlePaddingBottom));

        detailHeight = Math.round(getResources().getDimension(R.dimen.detailViewDesiredHeight));
        navHeight = Math.round(getResources().getDimension(R.dimen.navViewDesiredHeight));

        //init sub views
        titleView = new TitleView(this, colors);
        titleView.title = title;
        titleView.setPaddingTop(titlePaddingTop);
        titleView.setPaddingBottom(titlePaddingBottom);
        titleView.titleFontSize = titleFontSize;
        titleView.showTitle = showTitle;
        titleView.init();

        navigationView = new NavigationView(this, colors);
        navigationView.init();

        detailView = new DetailView(this, colors, divisionCount);
        detailView.setPaddingLeft(getPaddingLeft());
        detailView.setPaddingRight(getPaddingRight());
        detailView.init();

        initPaints();

        divisionCount = 6; //todo get from params

        navigationView.addOnSelectedTimeLineChanged(detailView);
        navigationView.addOnSelectedTimeLineChanged(this);

        setPlaceholder();
    }

    private void setPlaceholder() {
        if (isInEditMode()) {
            ArrayList<String> keys = new ArrayList<String>();
            ArrayList<String> names = new ArrayList<String>();
            ArrayList<Integer> colors = new ArrayList<Integer>();
            ArrayList<ChartItem> items = new ArrayList<ChartItem>();
            keys.add("y0");
            keys.add("y1");
            names.add("Red Line");
            names.add("Green Line");
            colors.add(Color.RED);
            colors.add(Color.GREEN);

            long startTime = 1614542230000L;
            Random random = new Random();
            for (int i = 0; i < 100; i++) {
                startTime += 86_400_000;
                ArrayList<Integer> values = new ArrayList<Integer>();
                for (int j = 0; j < keys.size(); j++) {
                    values.add(random.nextInt(1000));
                }
                ChartItem chartItem = new ChartItem(startTime, values);
                items.add(chartItem);
            }
            setData(new ChartData(keys, names, colors, items));
        }
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
        pFill = PaintU.createPaint(colors.backgroundColor, Paint.Style.FILL);
        paintForTopShadow = new Paint();
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

        titleView.setWidth(width - getPaddingLeft() - getPaddingRight());
        titleView.setPosX(getPaddingLeft());
        navigationView.setWidth(width - getPaddingLeft() - getPaddingRight());
        navigationView.setPosX(getPaddingLeft());
        detailView.setWidth(width);
        detailView.setPosX(0);

        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if (heightMode == MeasureSpec.EXACTLY) { //Must be this size
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) { //Can't be bigger than...
            height = Math.min(desiredWidth, heightSize);
        } else { //Be whatever you want
//            height = desiredHeight;
        }

        int titleHeight;
        int navHeight;
        int detailHeight;
        if (heightMode == MeasureSpec.EXACTLY || heightMode == MeasureSpec.AT_MOST) {
            titleHeight = titleView.calculateHeight();
            navHeight = navigationView.calculateHeight();
            detailHeight = height - getPaddingTop() - getPaddingBottom() - titleHeight - navHeight;
        } else {
            titleHeight = titleView.calculateHeight();
            navHeight = navigationView.calculateHeight();
            detailHeight = detailView.calculateHeight();
            height = getPaddingTop() + titleHeight + navHeight + detailHeight + getPaddingBottom();
        }

        titleView.setHeight(titleHeight);
        titleView.setPosY(getPaddingTop());
        detailView.setHeight(detailHeight);
        detailView.setPosY(getPaddingTop() + titleHeight);
        navigationView.setPosY(getPaddingTop() + titleHeight + detailHeight);
        navigationView.setHeight(navHeight);

        setMeasuredDimension(widthMeasureSpec, height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        topShadowSize = new RectF(0, 0, width, titleView.getHeight());
        shader = new LinearGradient(0, 0, 0, titleView.getHeight(), colors.backgroundColor, Color.TRANSPARENT, Shader.TileMode.CLAMP);
        paintForTopShadow.setShader(shader);

        titleView.onSizeChanged();
        navigationView.onSizeChanged();
        detailView.onSizeChanged();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawPaint(pFill);

        if (data != null) {
            detailView.drawOn(canvas);
            detailView.drawDebug(canvas, debug);
            navigationView.drawOn(canvas);
            navigationView.drawDebug(canvas, debug);
        }

        //draw top shadow
        canvas.drawRect(topShadowSize, paintForTopShadow);

        //drawTitle
        titleView.drawOn(canvas);
        titleView.drawDebug(canvas, debug);

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
            navigationView.prepareDataForPrinting(false);
            detailView.prepareDataForPrinting(false);

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
//            if (saveShowedLines) {
            this.linesForShow = bundle.getBooleanArray("linesForShow");
//            data.updateLinesForShow(linesForShow);
//            }
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

    public void setData(ChartData data) {
        this.data = data;
        linesForShow = new boolean[data.getNames().size()];
        for (int i = 0; i < data.getNames().size(); i++) {
            linesForShow[i] = true;
        }

        selectedStartTime = data.getItems().get(data.getItems().size() - (int) (data.getItems().size() / 3)).getTime();
        selectedEndTime = data.getItems().get(data.getItems().size() - 1).getTime();

        detailView.setData(data, selectedStartTime, selectedEndTime);
        navigationView.setData(data, selectedStartTime, selectedEndTime);

        invalidate();
    }

    //params
    public void setTitle(String title) {
        this.title = title;
    }
}
