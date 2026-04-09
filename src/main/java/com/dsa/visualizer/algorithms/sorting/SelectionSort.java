
package com.dsa.visualizer.algorithms.sorting;

import com.dsa.visualizer.controllers.SortingController;

public class SelectionSort {

    public void sort(int[] arr, SortingController controller) throws InterruptedException {
        int n = arr.length;

        for (int i = 0; i < n - 1; i++) {
            int minIdx = i;

            for (int j = i + 1; j < n; j++) {
                controller.visualizeComparison(minIdx, j);

                if (arr[j] < arr[minIdx]) {
                    minIdx = j;
                }
            }

            if (minIdx != i) {
                int temp = arr[i];
                arr[i] = arr[minIdx];
                arr[minIdx] = temp;

                controller.visualizeSwap(i, minIdx, arr);
            }
        }
    }
}
