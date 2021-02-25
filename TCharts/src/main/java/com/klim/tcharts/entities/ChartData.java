package com.klim.tcharts.entities;

import java.util.ArrayList;

public class ChartData {
    private ArrayList<String> keys;
    private ArrayList<String> names;
    private ArrayList<Integer> colors;
    private ArrayList<ChartItem> items;
    //    private ArrayList<Boolean> linesForShow;
    private ArrayList<Integer> maxValueInLine;

    public ChartData(ArrayList<String> keys, ArrayList<String> names, ArrayList<Integer> colors, ArrayList<ChartItem> items) {
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

    public ArrayList<String> getKeys() {
        return keys;
    }

    public ArrayList<String> getNames() {
        return names;
    }

    public ArrayList<Integer> getColors() {
        return colors;
    }

    public ArrayList<ChartItem> getItems() {
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
