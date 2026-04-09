
package com.dsa.visualizer.algorithms.sorting;

import com.dsa.visualizer.controllers.SortingController;

public class BubbleSort {

    public void sort(int[] arr, SortingController controller) throws InterruptedException {
        int n = arr.length;

        for (int i = 0; i < n - 1; i++) {
            boolean swapped = false;

            for (int j = 0; j < n - i - 1; j++) {
                controller.visualizeComparison(j, j + 1);

                if (arr[j] > arr[j + 1]) {
                    int temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;

                    controller.visualizeSwap(j, j + 1, arr);
                    swapped = true;
                }
            }

            if (!swapped) {
                break;
            }
        }
    }
}