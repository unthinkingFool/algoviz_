package com.dsa.visualizer.models;

import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

public class StackNode {
    public int value;
    public StackNode next;

    // UI elements
    public Rectangle rectangle;
    public Text label;

    // Position for visualization
    public double x;
    public double y;

    // Visual state
    public boolean isHighlighted;

    public StackNode(int value) {
        this.value = value;
        this.next = null;
        this.isHighlighted = false;
    }

    public StackNode(int value, double x, double y) {
        this(value);
        this.x = x;
        this.y = y;
    }
}