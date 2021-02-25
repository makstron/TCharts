package com.klim.tcharts.entities;

import java.util.ArrayList;

public class ChartItem {
    private long time;
    private ArrayList<Integer> values;
    private int maxValue;

    public ChartItem(long time, ArrayList<Integer> values) {
        this.time = time;
        this.values = values;

        if (values != null) {
            for (Integer value : values) {
                if (value > maxValue) {
                    maxValue = value;
                }
            }
        }
    }

    public long getTime() {
        return time;
    }

    public ArrayList<Integer> getValues() {
        return values;
    }

    public Integer getMaxValue() {
        return maxValue;
    }

    public void updateMaxValue(boolean[] check) {
        maxValue = 0;
        for (int i = 0; i < values.size(); i++) {
            if (check[i] && values.get(i) > maxValue) {
                maxValue = values.get(i);
            }
        }
    }
}
