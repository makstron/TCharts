package com.klim.tcharts.entities;

import java.util.List;

public class ChartItem {
    private long time;
    private List<Integer> values;
    private int maxValue;

    public ChartItem(long time, List<Integer> values) {
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

    public List<Integer> getValues() {
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
