package com.dsa.visualizer.algorithms.sorting;
import com.dsa.visualizer.controllers.SortingController;
public class QuickSort {

    public void sort(int[] arr, int p, int r, SortingController controller) throws InterruptedException {
        quicksort(arr, p, r, controller);
    }

    private void quicksort(int[] arr, int p, int r, SortingController controller) throws InterruptedException {
        if (p < r) {
            int q = partition(arr, p, r, controller);
            quicksort(arr, p, q - 1, controller);
            quicksort(arr, q + 1, r, controller);
        }
    }

    private int partition(int[] arr, int p, int r, SortingController controller) throws InterruptedException {
        int x = arr[r];
        int i = p - 1;
        for (int j = p; j < r; j++) {
            controller.visualizeComparison(i+1, j);
            if (arr[j] <= x) {
                i++;

                int temp = arr[i];
                arr[i] = arr[j];
                arr[j] = temp;
                controller.visualizeSwap(i, j, arr);
            }
        }
        i++;
        controller.visualizeComparison(i, r);
        int temp = arr[i];
        arr[i] = arr[r];
        arr[r] = temp;
        controller.visualizeSwap(i, r, arr);
        return i;

    }
}
