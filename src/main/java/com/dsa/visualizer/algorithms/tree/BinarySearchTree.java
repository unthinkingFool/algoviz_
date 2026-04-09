package com.dsa.visualizer.algorithms.tree;

import com.dsa.visualizer.controllers.TreeController;
import com.dsa.visualizer.models.TreeNode;
import java.util.*;

public class BinarySearchTree {

    public TreeNode root;


    private static final double PANE_WIDTH      = 1400;
    private static final double INITIAL_OFFSET  = PANE_WIDTH / 2.0;   // 700 px
    private static final double ROOT_X          = PANE_WIDTH / 2.0;   // 700 px
    private static final double ROOT_Y          = 50;
    private static final double LEVEL_HEIGHT    = 80;
    // Minimum horizontal separation between two node centres (2*radius + gap)
    private static final double MIN_SEP         = 50;

    public BinarySearchTree() {
        this.root = null;
    }

    // ── Insert ──────────────────────────────────────────────────────────────

    public TreeNode insert(TreeNode node, int value, TreeController controller)
            throws InterruptedException {

        if (node == null) {
            TreeNode newNode = new TreeNode(value);
            if (root == null) {
                root = newNode;
                controller.addNodeToVisualization(root, ROOT_X, ROOT_Y);
            }
            return newNode;
        }

        if (value == node.value) return node;   // no duplicates

        controller.highlightNode(node);

        int depth = getDepth(node);
        
        double offset = Math.max(INITIAL_OFFSET / Math.pow(2, depth + 1), MIN_SEP);

        if (value < node.value) {
            if (node.left == null) {
                TreeNode newNode = new TreeNode(value);
                newNode.parent = node;
                node.left = newNode;
                double newX = node.x - offset;
                double newY = node.y + LEVEL_HEIGHT;
                controller.addNodeToVisualization(node.left, newX, newY);
                controller.connectNodes(node, node.left, true);
            } else {
                insert(node.left, value, controller);
            }
        } else {
            if (node.right == null) {
                TreeNode newNode = new TreeNode(value);
                newNode.parent = node;
                node.right = newNode;
                double newX = node.x + offset;
                double newY = node.y + LEVEL_HEIGHT;
                controller.addNodeToVisualization(node.right, newX, newY);
                controller.connectNodes(node, node.right, false);
            } else {
                insert(node.right, value, controller);
            }
        }

        return node;
    }

    // ── Search ──────────────────────────────────────────────────────────────

    public TreeNode search(TreeNode node, int value, TreeController controller)
            throws InterruptedException {
        if (node == null) return null;

        controller.highlightNode(node);

        if (value == node.value) {
            controller.foundNode(node);
            return node;
        }

        return value < node.value
                ? search(node.left,  value, controller)
                : search(node.right, value, controller);
    }

    // ── Delete ──────────────────────────────────────────────────────────────

    public TreeNode delete(TreeNode node, int value, TreeController controller)
            throws InterruptedException {
        if (node == null) return null;

        controller.highlightNode(node);

        if (value < node.value) {
            node.left = delete(node.left, value, controller);
        } else if (value > node.value) {
            node.right = delete(node.right, value, controller);
        } else {
            controller.foundNode(node);

            // Case 1: leaf
            if (node.left == null && node.right == null) {
                controller.removeNode(node);
                return null;
            }

            // Case 2: only right child
            if (node.left == null) {
                TreeNode temp = node.right;
                controller.removeNode(node);
                return temp;
            }

            // Case 3: only left child
            if (node.right == null) {
                TreeNode temp = node.left;
                controller.removeNode(node);
                return temp;
            }

            // Case 4: two children — replace with inorder successor
            TreeNode successor = findMin(node.right);
            controller.highlightNode(successor);

            node.value = successor.value;
            controller.updateNodeValue(node, successor.value);

            node.right = delete(node.right, successor.value, controller);
        }

        return node;
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    public TreeNode findMin(TreeNode node) {
        while (node.left != null) node = node.left;
        return node;
    }

    public TreeNode findMax(TreeNode node) {
        while (node.right != null) node = node.right;
        return node;
    }

    private int getDepth(TreeNode node) {
        int depth = 0;
        TreeNode current = node;
        while (current.parent != null) { depth++; current = current.parent; }
        return depth;
    }

    public TreeNode findSuccessor(TreeNode node) {
        if (node.right != null) return findMin(node.right);
        TreeNode parent = node.parent;
        while (parent != null && node == parent.right) { node = parent; parent = parent.parent; }
        return parent;
    }

    public TreeNode findPredecessor(TreeNode node) {
        if (node.left != null) return findMax(node.left);
        TreeNode parent = node.parent;
        while (parent != null && node == parent.left) { node = parent; parent = parent.parent; }
        return parent;
    }

    public boolean isBST(TreeNode node) {
        return isBSTHelper(node, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    private boolean isBSTHelper(TreeNode node, int min, int max) {
        if (node == null) return true;
        if (node.value <= min || node.value >= max) return false;
        return isBSTHelper(node.left, min, node.value)
                && isBSTHelper(node.right, node.value, max);
    }
}
