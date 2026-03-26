package com.dsa.visualizer.algorithms.searching;

import com.dsa.visualizer.controllers.SearchingController;

public class LinearSearch {

    public int search(int[] arr, int target, SearchingController controller)
            throws InterruptedException {
        for (int i = 0; i < arr.length; i++) {
            controller.visualizeCheck(i);

            if (arr[i] == target) {
                return i;
            }
        }
        return -1;
    }
}
