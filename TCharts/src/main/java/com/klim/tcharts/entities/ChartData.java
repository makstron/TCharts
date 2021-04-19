package com.klim.tcharts.entities;

import java.util.ArrayList;
import java.util.List;

public class ChartData {
    private List<String> keys;
    private List<String> names;
    private List<Integer> colors;
    private List<ChartItem> items;
    //    private ArrayList<Boolean> linesForShow;
    private ArrayList<Integer> maxValueInLine;

    public ChartData(List<String> keys, List<String> names, List<Integer> colors, List<ChartItem> items) {
        this.keys = keys;
        this.names = names;
        this.colors = colors;
        this.items = items;

//        linesForShow = new ArrayList<>(names.size());
//        for (int i = 0; i < names.size(); i++) {
//            linesForShow.add(true);
//        }

        //find max value for each lines
        maxValueInLine = new ArrayList<>(names.size());
        for (int i = 0; i < names.size(); i++) {
            maxValueInLine.add(0);
        }
        for (ChartItem item : items) {
            for (int i = 0; i < maxValueInLine.size(); i++) {
                if (maxValueInLine.get(i) < item.getValues().get(i)) {
                    maxValueInLine.set(i, item.getValues().get(i));
                }
            }
        }
    }

    public List<String> getKeys() {
        return keys;
    }

    public List<String> getNames() {
        return names;
    }

    public List<Integer> getColors() {
        return colors;
    }

    public List<ChartItem> getItems() {
        return items;
    }

//    public ArrayList<Boolean> getLinesForShow() {
//        return linesForShow;
//    }

    public void updateLinesForShow(boolean[] showLines) {
        for (ChartItem item : items) {
            item.updateMaxValue(showLines);
        }
    }

    public ArrayList<Integer> getMaxValueInLine() {
        return maxValueInLine;
    }
}
