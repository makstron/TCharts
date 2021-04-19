package com.klim.tcharts.utils;

import com.klim.tcharts.entities.ChartItem;

import java.util.ArrayList;
import java.util.List;

public class SearchU {

    /**
     * Make first symbol In String Upper case
     *
     * @param s input String
     * @return String with Upper first symbol
     */
    public static String firstCharUpper(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    public static int binarySearchLeft(List<ChartItem> arr, int l, int r, long x) {
        if (r >= l) {
            int mid = l + (r - l) / 2;
            if (arr.get(Math.max(mid - 1, 0)).getTime() <= x && x < arr.get(mid).getTime()) {
                return Math.max(mid - 1, 0);
            } else if (arr.get(mid).getTime() == x) {
                return Math.max(mid - 1, 0);
            }

            if (arr.get(mid).getTime() > x) {
                return binarySearchLeft(arr, l, mid - 1, x);
            }
            return binarySearchLeft(arr, mid + 1, r, x);
        }
        return -1;
    }

    public static int binarySearchRight(List<ChartItem> arr, int l, int r, long x) {
        if (r >= l) {
            int mid = l + (r - l) / 2;

            if (arr.get(mid).getTime() < x && x <= arr.get(mid + 1).getTime()) {
                return mid + 1;
            } else if (arr.get(mid).getTime() == x) {
                return mid + 1;
            }

            if (arr.get(mid).getTime() > x) {
                return binarySearchRight(arr, l, mid - 1, x);
            }
            return binarySearchRight(arr, mid + 1, r, x);
        }
        return -1;
    }
}
