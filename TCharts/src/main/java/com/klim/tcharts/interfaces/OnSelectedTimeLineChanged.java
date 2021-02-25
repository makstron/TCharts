package com.klim.tcharts.interfaces;

public interface OnSelectedTimeLineChanged {
    void onTimeLineChanged(long start, long end, boolean changeZoom);
}
