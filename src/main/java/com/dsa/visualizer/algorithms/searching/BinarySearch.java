package com.dsa.visualizer.algorithms.searching;

import com.dsa.visualizer.controllers.SearchingController;

public class BinarySearch {

    public int search(int[] arr, int target, SearchingController controller)
            throws InterruptedException {
        int left = 0;
        int right = arr.length - 1;

        while (left <= right) {
            int mid = left + (right - left) / 2;

            controller.visualizeRange(left, right);
            controller.visualizeCheck(mid);

            if (arr[mid] == target) {
                return mid;
            }

            if (arr[mid] < target) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }

        return -1;
    }
}

