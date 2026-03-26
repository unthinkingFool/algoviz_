package com.dsa.visualizer.algorithms.stack;

import com.dsa.visualizer.controllers.StackQueueController;
import com.dsa.visualizer.models.StackNode;

public class Stack {

    private StackNode top;
    private int size;
    private int maxSize;

    public Stack(int maxSize) {
        this.top = null;
        this.size = 0;
        this.maxSize = maxSize;
    }

    // Push element onto stack
    public void push(int value, StackQueueController controller) throws InterruptedException {
        if (isFull()) {
            controller.showError("Stack Overflow! Maximum size reached.");
            return;
        }

        StackNode newNode = new StackNode(value);

        if (top == null) {
            top = newNode;
            controller.addStackNode(newNode, 0);
        } else {
            newNode.next = top;
            top = newNode;
            controller.addStackNode(newNode, size);
        }

        size++;
        controller.highlightStackNode(top);
        controller.updateStackSize(size);
    }

    // Pop element from stack
    public Integer pop(StackQueueController controller) throws InterruptedException {
        if (isEmpty()) {
            controller.showError("Stack Underflow! Stack is empty.");
            return null;
        }

        controller.highlightStackNode(top);

        int value = top.value;
        StackNode temp = top;
        top = top.next;

        controller.removeStackNode(temp);
        size--;
        controller.updateStackSize(size);

        return value;
    }

    // Peek at top element
    public Integer peek(StackQueueController controller) throws InterruptedException {
        if (isEmpty()) {
            controller.showError("Stack is empty!");
            return null;
        }

        controller.highlightStackNode(top);
        return top.value;
    }

    // Check if stack is empty
    public boolean isEmpty() {
        return top == null;
    }

    // Check if stack is full
    public boolean isFull() {
        return size >= maxSize;
    }

    // Get current size
    public int getSize() {
        return size;
    }

    // Get top node
    public StackNode getTop() {
        return top;
    }

    // Clear the stack
    public void clear(StackQueueController controller) throws InterruptedException {
        while (!isEmpty()) {
            pop(controller);
        }
    }

    // Search for an element (returns position from top, 1-indexed)
    public int search(int value, StackQueueController controller) throws InterruptedException {
        if (isEmpty()) return -1;

        StackNode current = top;
        int position = 1;

        while (current != null) {
            controller.highlightStackNode(current);

            if (current.value == value) {
                controller.foundStackNode(current);
                return position;
            }

            current = current.next;
            position++;
        }

        return -1; // Not found
    }

    // Display all elements (for debugging)
    public String display() {
        if (isEmpty()) return "Stack is empty";

        StringBuilder sb = new StringBuilder();
        sb.append("Top -> ");

        StackNode current = top;
        while (current != null) {
            sb.append(current.value);
            if (current.next != null) sb.append(" -> ");
            current = current.next;
        }

        return sb.toString();
    }
}