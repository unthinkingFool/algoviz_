package com.dsa.visualizer.algorithms.heap;

import com.dsa.visualizer.controllers.HeapController;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Heap {

    public enum Mode { MAX, MIN }

    private final List<Integer> heap = new ArrayList<>();
    private final HeapController controller;
    private Mode mode;

    public Heap(HeapController controller, Mode mode) {
        this.controller = controller;
        this.mode = mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
        heap.clear();
    }

    public Mode getMode() {
        return mode;
    }

    public List<Integer> getHeap() {
        return heap;
    }

    public int size() {
        return heap.size();
    }

    public boolean isEmpty() {
        return heap.isEmpty();
    }

    // Returns true if a should be higher in the heap than b
    private boolean hasHigherPriority(int a, int b) {
        return mode == Mode.MAX ? a > b : a < b;
    }

    // ── Insert ────────────────────────────────────────────────────────────────

    public void insert(int value) throws InterruptedException {
        heap.add(value);
        int idx = heap.size() - 1;
        controller.onHeapChanged(heap, idx, "Inserted " + value + " at index " + idx);
        shiftUp(idx);
    }

    private void shiftUp(int i) throws InterruptedException {
        while (i > 0) {
            int parent = (i - 1) / 2;
            controller.onCompare(i, parent, heap,
                    "Compare " + heap.get(i) + " (i=" + i + ") with parent " + heap.get(parent) + " (i=" + parent + ")");

            if (hasHigherPriority(heap.get(i), heap.get(parent))) {
                swap(i, parent);
                i = parent;
            } else {
                break;
            }
        }
        controller.onHeapChanged(heap, -1, "Heap property restored ✔");
    }

    // ── Extract Root (Max or Min) ─────────────────────────────────────────────

    public int extractRoot() throws InterruptedException {
        if (heap.isEmpty()) throw new IllegalStateException("Heap is empty");

        int root = heap.get(0);
        int last = heap.remove(heap.size() - 1);

        if (!heap.isEmpty()) {
            heap.set(0, last);
            controller.onHeapChanged(heap, 0, "Moved last element (" + last + ") to root");
            shiftDown(0);
        } else {
            controller.onHeapChanged(heap, -1, "Heap is now empty");
        }
        return root;
    }

    private void shiftDown(int i) throws InterruptedException {
        int n = heap.size();
        while (true) {
            int left = 2 * i + 1;
            int right = 2 * i + 2;
            int target = i;

            if (left < n) {
                controller.onCompare(left, target, heap,
                        "Compare left child " + heap.get(left) + " with " + heap.get(target));
                if (hasHigherPriority(heap.get(left), heap.get(target))) target = left;
            }
            if (right < n) {
                controller.onCompare(right, target, heap,
                        "Compare right child " + heap.get(right) + " with " + heap.get(target));
                if (hasHigherPriority(heap.get(right), heap.get(target))) target = right;
            }

            if (target != i) {
                swap(i, target);
                i = target;
            } else {
                break;
            }
        }
        controller.onHeapChanged(heap, -1, "Heap property restored ✔");
    }

    // ── Peek Root ────────────────────────────────────────────────────────────

    public int peekRoot() {
        if (heap.isEmpty()) throw new IllegalStateException("Heap is empty");
        return heap.get(0);
    }

    // ── Heap Sort ────────────────────────────────────────────────────────────

    public List<Integer> heapSort() throws InterruptedException {
        List<Integer> copy = new ArrayList<>(heap);
        List<Integer> sorted = new ArrayList<>();

        String label = mode == Mode.MAX ? "Max-Heap Sort → ascending" : "Min-Heap Sort → descending";
        controller.onHeapChanged(heap, -1, "Starting Heap Sort on current heap...");

        while (!copy.isEmpty()) {
            int root = copy.get(0);
            int last = copy.remove(copy.size() - 1);
            if (!copy.isEmpty()) {
                copy.set(0, last);
                heapifyDownCopy(copy, 0);
            }
            // MAX heap gives ascending (prepend); MIN heap gives descending (append)
            if (mode == Mode.MAX) sorted.add(0, root);
            else                  sorted.add(root);
            controller.onSortStep(copy, sorted, "Extracted root: " + root);
        }

        controller.onSortComplete(sorted, "Heap Sort complete — " + label);
        return sorted;
    }

    private void heapifyDownCopy(List<Integer> h, int i) {
        int n = h.size();
        while (true) {
            int left = 2 * i + 1, right = 2 * i + 2, target = i;
            if (left  < n && hasHigherPriority(h.get(left),  h.get(target))) target = left;
            if (right < n && hasHigherPriority(h.get(right), h.get(target))) target = right;
            if (target != i) {
                int tmp = h.get(i); h.set(i, h.get(target)); h.set(target, tmp);
                i = target;
            } else break;
        }
    }

    // ── Build Heap ───────────────────────────────────────────────────────────

    public void buildHeap(int[] arr) throws InterruptedException {
        heap.clear();
        for (int v : arr) heap.add(v);
        String label = mode == Mode.MAX ? "Max-Heap" : "Min-Heap";
        controller.onHeapChanged(heap, -1, "Array loaded — building " + label + "...");

        for (int i = heap.size() / 2 - 1; i >= 0; i--) {
            shiftDown(i);
        }
        controller.onHeapChanged(heap, -1, label + " built ✔");
    }

    // ── Generate Random ───────────────────────────────────────────────────────

    public void generateRandom() throws InterruptedException {
        heap.clear();
        Random rng = new Random();
        int count = 7 + rng.nextInt(6); // 7–12 nodes
        int[] arr = new int[count];
        for (int i = 0; i < count; i++) arr[i] = rng.nextInt(99) + 1; // 1–99
        buildHeap(arr);
    }

    // ── Swap helper ──────────────────────────────────────────────────────────

    private void swap(int i, int j) throws InterruptedException {
        int tmp = heap.get(i);
        heap.set(i, heap.get(j));
        heap.set(j, tmp);
        controller.onSwap(i, j, heap, "Swap index " + i + " ↔ " + j);
    }
}
//package com.dsa.visualizer.algorithms.heap;
//
//import com.dsa.visualizer.controllers.HeapController;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class Heap {
//
//    private final List<Integer> heap = new ArrayList<>();
//    private final HeapController controller;
//
//    public Heap(HeapController controller) {
//        this.controller = controller;
//    }
//
//    public List<Integer> getHeap() {
//        return heap;
//    }
//
//    public int size() {
//        return heap.size();
//    }
//
//    public boolean isEmpty() {
//        return heap.isEmpty();
//    }
//
//
//    public void insert(int value) throws InterruptedException {
//        heap.add(value);
//        int idx = heap.size() - 1;
//        controller.onHeapChanged(heap, idx, "Inserted " + value + " at index " + idx);
//        shiftUp(idx);
//    }
//
//    private void shiftUp(int i) throws InterruptedException {
//        while (i > 0) {
//            int parent = (i - 1) / 2;
//            controller.onCompare(i, parent, heap,
//                    "Compare " + heap.get(i) + " (i=" + i + ") with parent " + heap.get(parent) + " (i=" + parent + ")");
//
//            if (heap.get(i) > heap.get(parent)) {
//                swap(i, parent);
//                i = parent;
//            } else {
//                break;
//            }
//        }
//        controller.onHeapChanged(heap, -1, "Heap property restored ");
//    }
//
//
//
//    public int extractMax() throws InterruptedException {
//        if (heap.isEmpty()) throw new IllegalStateException("Heap is empty");
//
//        int max = heap.get(0);
//        int last = heap.remove(heap.size() - 1);
//
//        if (!heap.isEmpty()) {
//            heap.set(0, last);
//            controller.onHeapChanged(heap, 0, "Moved last element (" + last + ") to root");
//           shiftDown(0);
//        } else {
//            controller.onHeapChanged(heap, -1, "Heap is now empty");
//        }
//        return max;
//    }
//
//    private void shiftDown(int i) throws InterruptedException {
//        int n = heap.size();
//        while (true) {
//            int left = 2 * i + 1;
//            int right = 2 * i + 2;
//            int largest = i;
//
//            if (left < n) {
//                controller.onCompare(left, largest, heap,
//                        "Compare left child " + heap.get(left) + " with " + heap.get(largest));
//                if (heap.get(left) > heap.get(largest)) largest = left;
//            }
//            if (right < n) {
//                controller.onCompare(right, largest, heap,
//                        "Compare right child " + heap.get(right) + " with " + heap.get(largest));
//                if (heap.get(right) > heap.get(largest)) largest = right;
//            }
//
//            if (largest != i) {
//                swap(i, largest);
//                i = largest;
//            } else {
//                break;
//            }
//        }
//        controller.onHeapChanged(heap, -1, "Heap property restored ");
//    }
//
//
//
//
//    public int peekMax() {
//        if (heap.isEmpty()) throw new IllegalStateException("Heap is empty");
//        return heap.get(0);
//    }
//
//    public List<Integer> heapSort() throws InterruptedException {
//
//        List<Integer> copy = new ArrayList<>(heap);
//        List<Integer> sorted = new ArrayList<>();
//
//        controller.onHeapChanged(heap, -1, "Starting Heap Sort on current heap...");
//
//
//        while (!copy.isEmpty()) {
//
//            int max = copy.get(0);
//            int last = copy.remove(copy.size() - 1);
//            if (!copy.isEmpty()) {
//                copy.set(0, last);
//                heapifyDownCopy(copy, 0);
//            }
//            sorted.add(0, max); // prepend so result is ascending
//            controller.onSortStep(copy, sorted, "Extracted max: " + max);
//        }
//
//        controller.onSortComplete(sorted, "Heap Sort complete — result is ascending order");
//        return sorted;
//    }
//
//    private void heapifyDownCopy(List<Integer> h, int i) throws InterruptedException {
//        int n = h.size();
//        while (true) {
//            int left = 2 * i + 1, right = 2 * i + 2, largest = i;
//            if (left < n && h.get(left) > h.get(largest)) largest = left;
//            if (right < n && h.get(right) > h.get(largest)) largest = right;
//            if (largest != i) {
//                int temp = h.get(i);
//                h.set(i, h.get(largest));
//                h.set(largest, temp);
//                i = largest;
//            } else break;
//        }
//    }
//
//
//    public void buildHeap(int[] arr) throws InterruptedException {
//        heap.clear();
//        for (int v : arr) heap.add(v);
//        controller.onHeapChanged(heap, -1, "Array loaded — building max-heap...");
//
//        for (int i = heap.size() / 2 - 1; i >= 0; i--) {
//            shiftDown(i);
//        }
//        controller.onHeapChanged(heap, -1, "Max-Heap built ");
//    }
//
//
//    private void swap(int i, int j) throws InterruptedException {
//        int tmp = heap.get(i);
//        heap.set(i, heap.get(j));
//        heap.set(j, tmp);
//        controller.onSwap(i, j, heap, "Swap index " + i + " with " + j);
//    }
//}
