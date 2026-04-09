
package com.dsa.visualizer.algorithms.sorting;

import com.dsa.visualizer.controllers.SortingController;

public class InsertionSort {

    public void sort(int[] arr, SortingController controller) throws InterruptedException {
        int n = arr.length;

        for (int i = 1; i < n; i++) {
            int key = arr[i];
            int j = i - 1;

            while (j >= 0) {
                controller.visualizeComparison(j, i);

                if (arr[j] > key) {
                    arr[j + 1] = arr[j];
                    controller.visualizeSet(j + 1, arr[j], arr);
                    j--;
                } else {
                    break;
                }
            }

            arr[j + 1] = key;
            controller.visualizeSet(j + 1, key, arr);
        }
    }
}


