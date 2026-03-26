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
//    private void mergeSort(int[] arr, int left, int right, SortingController controller)
//            throws InterruptedException {
//        if (left < right) {
//            int mid = left + (right - left) / 2;
//
//            mergeSort(arr, left, mid, controller);
//            mergeSort(arr, mid + 1, right, controller);
//            merge(arr, left, mid, right, controller);
//        }
//    }
//
//    private void merge(int[] arr, int left, int mid, int right, SortingController controller)
//            throws InterruptedException {
//        int n1 = mid - left + 1;
//        int n2 = right - mid;
//
//        int[] leftArr = new int[n1];
//        int[] rightArr = new int[n2];
//
//        for (int i = 0; i < n1; i++) {
//            leftArr[i] = arr[left + i];
//        }
//        for (int j = 0; j < n2; j++) {
//            rightArr[j] = arr[mid + 1 + j];
//        }
//
//        int i = 0, j = 0, k = left;
//
//        while (i < n1 && j < n2) {
//            controller.visualizeComparison(left + i, mid + 1 + j);
//
//            if (leftArr[i] <= rightArr[j]) {
//                arr[k] = leftArr[i];
//                controller.visualizeSet(k, leftArr[i], arr);
//                i++;
//            } else {
//                arr[k] = rightArr[j];
//                controller.visualizeSet(k, rightArr[j], arr);
//                j++;
//            }
//            k++;
//        }
//
//        while (i < n1) {
//            arr[k] = leftArr[i];
//            controller.visualizeSet(k, leftArr[i], arr);
//            i++;
//            k++;
//        }
//
//        while (j < n2) {
//            arr[k] = rightArr[j];
//            controller.visualizeSet(k, rightArr[j], arr);
//            j++;
//            k++;
//        }
//    }
//}