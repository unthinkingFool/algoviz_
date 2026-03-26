
// ==================== MERGE SORT ====================
// File: src/main/java/com/dsa/visualizer/algorithms/sorting/MergeSort.java
package com.dsa.visualizer.algorithms.sorting;

import com.dsa.visualizer.controllers.SortingController;

public class MergeSort {

    public void sort(int[] arr, SortingController controller) throws InterruptedException {
        mergeSort(arr, 0, arr.length - 1, controller);
    }

    private void mergeSort(int[] arr, int left, int right, SortingController controller)
            throws InterruptedException {
        if (left < right) {
            int mid = left + (right - left) / 2;

            mergeSort(arr, left, mid, controller);
            mergeSort(arr, mid + 1, right, controller);
            merge(arr, left, mid, right, controller);
        }
    }

    private void merge(int[] arr, int left, int mid, int right, SortingController controller)
            throws InterruptedException {
        int n1 = mid - left + 1;
        int n2 = right - mid;

        int[] leftArr = new int[n1];
        int[] rightArr = new int[n2];

        for (int i = 0; i < n1; i++) {
            leftArr[i] = arr[left + i];
        }
        for (int j = 0; j < n2; j++) {
            rightArr[j] = arr[mid + 1 + j];
        }

        int i = 0, j = 0, k = left;

        while (i < n1 && j < n2) {
            controller.visualizeComparison(left + i, mid + 1 + j);

            if (leftArr[i] <= rightArr[j]) {
                arr[k] = leftArr[i];
                controller.visualizeSet(k, leftArr[i], arr);
                i++;
            } else {
                arr[k] = rightArr[j];
                controller.visualizeSet(k, rightArr[j], arr);
                j++;
            }
            k++;
        }

        while (i < n1) {
            arr[k] = leftArr[i];
            controller.visualizeSet(k, leftArr[i], arr);
            i++;
            k++;
        }

        while (j < n2) {
            arr[k] = rightArr[j];
            controller.visualizeSet(k, rightArr[j], arr);
            j++;
            k++;
        }
    }
}