package com.dsa.visualizer.algorithms.queue;

import com.dsa.visualizer.controllers.StackQueueController;
import com.dsa.visualizer.models.QueueNode;

public class Queue {

    private QueueNode front;
    private QueueNode rear;
    private int size;
    private int maxSize;

    public Queue(int maxSize) {
        this.front = null;
        this.rear = null;
        this.size = 0;
        this.maxSize = maxSize;
    }

    // Enqueue (add) element to queue
    public void enqueue(int value, StackQueueController controller) throws InterruptedException {
        if (isFull()) {
            controller.showError("Queue Overflow! Maximum size reached.");
            return;
        }

        QueueNode newNode = new QueueNode(value);

        if (isEmpty()) {
            front = newNode;
            rear = newNode;
            newNode.isFront = true;
            newNode.isRear = true;
            controller.addQueueNode(newNode, 0);
        } else {
            rear.isRear = false;
            rear.next = newNode;
            rear = newNode;
            newNode.isRear = true;
            controller.addQueueNode(newNode, size);
        }

        size++;
        controller.highlightQueueNode(rear);
        controller.updateQueueSize(size);
    }

    // Dequeue (remove) element from queue
    public Integer dequeue(StackQueueController controller) throws InterruptedException {
        if (isEmpty()) {
            controller.showError("Queue Underflow! Queue is empty.");
            return null;
        }

        controller.highlightQueueNode(front);

        int value = front.value;
        QueueNode temp = front;
        front = front.next;

        if (front == null) {
            rear = null;
        } else {
            front.isFront = true;
        }

        controller.removeQueueNode(temp);
        size--;
        controller.updateQueueSize(size);

        return value;
    }

    // Peek at front element
    public Integer peek(StackQueueController controller) throws InterruptedException {
        if (isEmpty()) {
            controller.showError("Queue is empty!");
            return null;
        }

        controller.highlightQueueNode(front);
        return front.value;
    }

    // Peek at rear element
    public Integer peekRear(StackQueueController controller) throws InterruptedException {
        if (isEmpty()) {
            controller.showError("Queue is empty!");
            return null;
        }

        controller.highlightQueueNode(rear);
        return rear.value;
    }

    // Check if queue is empty
    public boolean isEmpty() {
        return front == null;
    }

    // Check if queue is full
    public boolean isFull() {
        return size >= maxSize;
    }

    // Get current size
    public int getSize() {
        return size;
    }

    // Get front node
    public QueueNode getFront() {
        return front;
    }

    // Get rear node
    public QueueNode getRear() {
        return rear;
    }

    // Clear the queue
    public void clear(StackQueueController controller) throws InterruptedException {
        while (!isEmpty()) {
            dequeue(controller);
        }
    }

    // Search for an element (returns position from front, 1-indexed)
    public int search(int value, StackQueueController controller) throws InterruptedException {
        if (isEmpty()) return -1;

        QueueNode current = front;
        int position = 1;

        while (current != null) {
            controller.highlightQueueNode(current);

            if (current.value == value) {
                controller.foundQueueNode(current);
                return position;
            }

            current = current.next;
            position++;
        }

        return -1; // Not found
    }

    // Display all elements (for debugging)
    public String display() {
        if (isEmpty()) return "Queue is empty";

        StringBuilder sb = new StringBuilder();
        sb.append("Front -> ");

        QueueNode current = front;
        while (current != null) {
            sb.append(current.value);
            if (current.next != null) sb.append(" -> ");
            current = current.next;
        }

        sb.append(" <- Rear");
        return sb.toString();
    }

    // Reverse the queue
    public void reverse(StackQueueController controller) throws InterruptedException {
        if (isEmpty() || size == 1) return;

        reverseRecursive(controller);
    }

    private void reverseRecursive(StackQueueController controller) throws InterruptedException {
        if (isEmpty()) return;

        int value = dequeue(controller);
        reverseRecursive(controller);
        enqueue(value, controller);
    }
}