package com.dsa.visualizer.models;

import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

public class QueueNode {
    public int value;
    public QueueNode next;

    // UI elements
    public Rectangle rectangle;
    public Text label;

    // Position for visualization
    public double x;
    public double y;

    // Visual state
    public boolean isHighlighted;
    public boolean isFront;
    public boolean isRear;

    public QueueNode(int value) {
        this.value = value;
        this.next = null;
        this.isHighlighted = false;
        this.isFront = false;
        this.isRear = false;
    }

    public QueueNode(int value, double x, double y) {
        this(value);
        this.x = x;
        this.y = y;
    }
}