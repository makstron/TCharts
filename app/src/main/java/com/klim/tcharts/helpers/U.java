package com.klim.tcharts.helpers;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.animation.LinearInterpolator;

import java.util.ArrayList;

/**
 * Class with utilities
 */

public class U {

    /**
     * Make first symbol In String Upper case
     *
     * @param s input String
     * @return String with Upper first symbol
     */
    public static String firstCharUpper(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

//    public static int binarySearchLeft(ArrayList<ChartItem> arr, int l, int r, long x) {
//        if (r >= l) {
//            int mid = l + (r - l) / 2;
//            if (arr.get(Math.max(mid - 1, 0)).getTime() <= x && x < arr.get(mid).getTime()) {
//                return Math.max(mid - 1, 0);
//            } else if (arr.get(mid).getTime() == x) {
//                return Math.max(mid - 1, 0);
//            }
//
//            if (arr.get(mid).getTime() > x) {
//                return binarySearchLeft(arr, l, mid - 1, x);
//            }
//            return binarySearchLeft(arr, mid + 1, r, x);
//        }
//        return -1;
//    }
//
//    public static int binarySearchRight(ArrayList<ChartItem> arr, int l, int r, long x) {
//        if (r >= l) {
//            int mid = l + (r - l) / 2;
//
//            if (arr.get(mid).getTime() < x && x <= arr.get(mid + 1).getTime()) {
//                return mid + 1;
//            } else if (arr.get(mid).getTime() == x) {
//                return mid + 1;
//            }
//
//            if (arr.get(mid).getTime() > x) {
//                return binarySearchRight(arr, l, mid - 1, x);
//            }
//            return binarySearchRight(arr, mid + 1, r, x);
//        }
//        return -1;
//    }

    public static Paint createPaint(int color, Paint.Style style, float strokeWidth) {
        Paint p = createPaint(color, style);
        p.setStrokeWidth(strokeWidth);
        return p;
    }

    public static Paint createPaint(int color, Paint.Style style) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(color);
        paint.setStyle(style);
        return paint;
    }

    public static int getStatusBarHeight(Context context, int def) {
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return context.getResources().getDimensionPixelSize(resourceId);
        }
        return def;
    }

    public static int colorSetA(int color, int a) {
        return Color.argb(a, Color.red(color), Color.green(color), Color.blue(color));
    }

    public static ValueAnimator createIntValueAnimator(int start, int end, int duration, ValueAnimator.AnimatorUpdateListener updateListener) {
        ValueAnimator valueAnimator = ValueAnimator.ofInt(start, end);
        return crValAnim(valueAnimator, duration, updateListener);
    }

    public static ValueAnimator createFloatValueAnimator(float start, float end, int duration, ValueAnimator.AnimatorUpdateListener updateListener) {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(start, end);
        return crValAnim(valueAnimator, duration, updateListener);
    }

    public static ValueAnimator crValAnim(ValueAnimator valueAnimator, int duration, ValueAnimator.AnimatorUpdateListener updateListener) {
        valueAnimator.addUpdateListener(updateListener);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.setDuration(duration);
        return valueAnimator;
    }
}