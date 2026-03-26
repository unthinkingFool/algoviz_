package com.dsa.visualizer.algorithms.linkedlist;

import com.dsa.visualizer.controllers.LinkedlistController;
import java.util.List;

public class LinkedListAlgorithm {


    public void traverse(List<Integer> list, LinkedlistController controller)
            throws InterruptedException {

        for (int i = 0; i < list.size(); i++) {
            controller.visualizeNodeVisit(i, list.get(i), "traversal");
            Thread.sleep(600);
        }
        controller.visualizeComplete("Traversal complete. Visited " + list.size() + " node(s).");
    }



    public void searchTraversal(List<Integer> list, int target,
                                LinkedlistController controller)
            throws InterruptedException {

        for (int i = 0; i < list.size(); i++) {
            int value = list.get(i);
            controller.visualizeNodeVisit(i, value, "search");
            Thread.sleep(800);

            if (value == target) {
                controller.visualizeFound(i, value);
                return;
            }
        }

        controller.visualizeNotFound(target);
    }



    public void insertionTraversal(List<Integer> list, int index, int value,
                                   LinkedlistController controller)
            throws InterruptedException {

        // Walk to the insertion point
        int walkTo = Math.min(index, list.size()); // clamp to valid range
        for (int i = 0; i < walkTo; i++) {
            controller.visualizeNodeVisit(i, list.get(i), "insertion-walk");
            Thread.sleep(500);
        }


        controller.visualizeInsert(index, value);
    }


    public void deletionTraversal(List<Integer> list, int index,
                                  LinkedlistController controller)
            throws InterruptedException {

        if (index < 0 || index >= list.size()) {
            controller.visualizeComplete("Invalid index for deletion.");
            return;
        }

        // Walk to the node to be deleted
        for (int i = 0; i < index; i++) {
            controller.visualizeNodeVisit(i, list.get(i), "deletion-walk");
            Thread.sleep(500);
        }

        // Highlight the node about to be removed
        controller.visualizeNodeVisit(index, list.get(index), "delete-target");
        Thread.sleep(700);

        // Signal the actual delete
        controller.visualizeDelete(index, list.get(index));
    }
}