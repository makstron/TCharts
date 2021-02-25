package com.klim.tcharts.utils;

import android.graphics.Color;

public class ColorU {

    public static int colorSetA(int color, int a) {
        return Color.argb(a, Color.red(color), Color.green(color), Color.blue(color));
    }
}
