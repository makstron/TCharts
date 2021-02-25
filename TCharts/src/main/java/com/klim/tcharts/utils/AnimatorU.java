package com.klim.tcharts.utils;

import android.animation.ValueAnimator;
import android.view.animation.LinearInterpolator;

public class AnimatorU {

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
