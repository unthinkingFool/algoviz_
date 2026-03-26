package com.dsa.visualizer.models;

import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;

public class TreeNode {
    public int value;
    public TreeNode left;
    public TreeNode right;
    public TreeNode parent;

    // UI elements
    public Circle circle;
    public Text label;
    public Line leftLine;
    public Line rightLine;

    // Position for visualization
    public double x;
    public double y;

    // Visual state
    public boolean isVisited;
    public boolean isHighlighted;

    // Layout slot assigned by the Reingold-Tilford layout engine in TreeController
    public double slot;

    public TreeNode(int value) {
        this.value = value;
        this.left = null;
        this.right = null;
        this.parent = null;
        this.isVisited = false;
        this.isHighlighted = false;
    }

    public TreeNode(int value, double x, double y) {
        this(value);
        this.x = x;
        this.y = y;
    }

    public boolean isLeaf() {
        return left == null && right == null;
    }

    public boolean hasLeftChild() {
        return left != null;
    }

    public boolean hasRightChild() {
        return right != null;
    }

    public int getHeight() {
        if (this == null) return 0;
        int leftHeight = (left != null) ? left.getHeight() : 0;
        int rightHeight = (right != null) ? right.getHeight() : 0;
        return 1 + Math.max(leftHeight, rightHeight);
    }
}