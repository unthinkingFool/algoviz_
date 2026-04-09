package com.dsa.visualizer.controllers;

import com.dsa.visualizer.algorithms.tree.BinarySearchTree;
import com.dsa.visualizer.models.TreeNode;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.*;

public class TreeController implements Initializable {

    @FXML private Pane treePane;
    @FXML private TextField inputField;
    @FXML private Label statusLabel;
    @FXML private Label infoLabel;

    private BinarySearchTree bst;
    private boolean isRunning = false;

    private static final double NODE_RADIUS    = 20;
    private static final int    ANIM_DELAY     = 400;

    // Must match the constants in BinarySearchTree.java
    private static final double PANE_WIDTH     = 1400;
    private static final double INITIAL_OFFSET = PANE_WIDTH / 2.0;
    private static final double ROOT_X         = PANE_WIDTH / 2.0;
    private static final double ROOT_Y         = 50;
    private static final double LEVEL_HEIGHT   = 80;
    private static final double MIN_SEP        = 50;

    private Set<Integer> insertedValues;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        bst = new BinarySearchTree();
        insertedValues = new HashSet<>();
        statusLabel.setText("Binary Search Tree mode - Enter values to insert");
        updateInfoLabel();
    }

    // ─── FXML handlers ───────────────────────────────────────────────────────

    @FXML
    private void handleInsert() {
        if (isRunning) return;

        String input = inputField.getText().trim();
        if (input.isEmpty()) { statusLabel.setText("Please enter a value"); return; }

        try {
            int value = Integer.parseInt(input);
            inputField.clear();

            if (insertedValues.contains(value)) {
                statusLabel.setText("Value " + value + " already exists!");
                return;
            }

            isRunning = true;
            new Thread(() -> {
                try {
                    bst.root = bst.insert(bst.root, value, this);
                    insertedValues.add(value);
                    Platform.runLater(() -> {
                        statusLabel.setText("Inserted: " + value);
                        updateInfoLabel();
                        isRunning = false;
                    });
                } catch (InterruptedException e) {
                    Platform.runLater(() -> { statusLabel.setText("Interrupted"); isRunning = false; });
                }
            }).start();

        } catch (NumberFormatException e) { statusLabel.setText("Invalid number format"); }
    }

    @FXML
    private void handleDelete() {
        if (isRunning) return;

        String input = inputField.getText().trim();
        if (input.isEmpty()) { statusLabel.setText("Please enter a value to delete"); return; }

        try {
            int value = Integer.parseInt(input);
            inputField.clear();

            if (!insertedValues.contains(value)) {
                statusLabel.setText("Value " + value + " not found");
                return;
            }

            isRunning = true;
            new Thread(() -> {
                try {
                    bst.root = bst.delete(bst.root, value, this);
                    insertedValues.remove(value);
                    Platform.runLater(() -> {
                        statusLabel.setText("Deleted: " + value);
                        updateInfoLabel();
                        isRunning = false;
                        
                        completeRedraw();
                    });
                } catch (InterruptedException e) {
                    Platform.runLater(() -> { statusLabel.setText("Interrupted"); isRunning = false; });
                }
            }).start();

        } catch (NumberFormatException e) { statusLabel.setText("Invalid number format"); }
    }

    @FXML
    private void handleSearch() {
        if (isRunning) return;

        String input = inputField.getText().trim();
        if (input.isEmpty()) { statusLabel.setText("Please enter a value to search"); return; }

        try {
            int value = Integer.parseInt(input);
            isRunning = true;
            new Thread(() -> {
                try {
                    resetColors();
                    TreeNode result = bst.search(bst.root, value, this);
                    Platform.runLater(() -> {
                        statusLabel.setText(result != null ? "Found: " + value : "Not found: " + value);
                        isRunning = false;
                    });
                } catch (InterruptedException e) {
                    Platform.runLater(() -> { statusLabel.setText("Interrupted"); isRunning = false; });
                }
            }).start();

        } catch (NumberFormatException e) { statusLabel.setText("Invalid number format"); }
    }

    @FXML private void handleInorder()    { performTraversal("Inorder"); }
    @FXML private void handlePreorder()   { performTraversal("Preorder"); }
    @FXML private void handlePostorder()  { performTraversal("Postorder"); }
    @FXML private void handleLevelOrder() { performTraversal("Level Order"); }

    private void performTraversal(String type) {
        if (isRunning) return;
        if (bst.root == null) { statusLabel.setText("Tree is empty"); return; }

        isRunning = true;
        new Thread(() -> {
            try {
                resetColors();
                List<Integer> result = new ArrayList<>();
                switch (type) {
                    case "Inorder":     inorder(bst.root, result);    break;
                    case "Preorder":    preorder(bst.root, result);   break;
                    case "Postorder":   postorder(bst.root, result);  break;
                    case "Level Order": levelOrder(bst.root, result); break;
                }
                Platform.runLater(() -> {
                    statusLabel.setText(type + ": " + result.toString());
                    isRunning = false;
                });
            } catch (InterruptedException e) {
                Platform.runLater(() -> { statusLabel.setText("Interrupted"); isRunning = false; });
            }
        }).start();
    }

    private void inorder(TreeNode n, List<Integer> r) throws InterruptedException {
        if (n == null) return;
        inorder(n.left, r); visitNode(n); r.add(n.value); inorder(n.right, r);
    }
    private void preorder(TreeNode n, List<Integer> r) throws InterruptedException {
        if (n == null) return;
        visitNode(n); r.add(n.value); preorder(n.left, r); preorder(n.right, r);
    }
    private void postorder(TreeNode n, List<Integer> r) throws InterruptedException {
        if (n == null) return;
        postorder(n.left, r); postorder(n.right, r); visitNode(n); r.add(n.value);
    }
    private void levelOrder(TreeNode root, List<Integer> r) throws InterruptedException {
        if (root == null) return;
        Queue<TreeNode> q = new LinkedList<>();
        q.add(root);
        while (!q.isEmpty()) {
            TreeNode cur = q.poll();
            visitNode(cur); r.add(cur.value);
            if (cur.left  != null) q.add(cur.left);
            if (cur.right != null) q.add(cur.right);
        }
    }

    @FXML
    private void handleFindMin() {
        if (isRunning || bst.root == null) {
            statusLabel.setText(bst.root == null ? "Tree is empty" : "Operation in progress");
            return;
        }
        TreeNode min = bst.findMin(bst.root);
        resetColors();
        highlightNodeSync(min);
        statusLabel.setText("Minimum value: " + min.value);
    }

    @FXML
    private void handleFindMax() {
        if (isRunning || bst.root == null) {
            statusLabel.setText(bst.root == null ? "Tree is empty" : "Operation in progress");
            return;
        }
        TreeNode max = bst.findMax(bst.root);
        resetColors();
        highlightNodeSync(max);
        statusLabel.setText("Maximum value: " + max.value);
    }

    @FXML
    private void generateRandom() {
        if (isRunning) return;
        clearTree();

        Random random = new Random();
        int count = 7 + random.nextInt(6);
        Set<Integer> values = new LinkedHashSet<>();
        while (values.size() < count) values.add(random.nextInt(100) + 1);

        isRunning = true;
        new Thread(() -> {
            try {
                Thread.sleep(300);
                for (int value : values) {
                    bst.root = bst.insert(bst.root, value, this);
                    insertedValues.add(value);
                    Thread.sleep(200);
                }
                Platform.runLater(() -> {
                    statusLabel.setText("Random BST generated with " + count + " nodes");
                    updateInfoLabel();
                    isRunning = false;
                });
            } catch (InterruptedException e) {
                Platform.runLater(() -> { statusLabel.setText("Interrupted"); isRunning = false; });
            }
        }).start();
    }

    @FXML
    private void clearTree() {
        if (isRunning) return;
        treePane.getChildren().clear();
        bst = new BinarySearchTree();
        insertedValues = new HashSet<>();
        statusLabel.setText("Tree cleared");
        updateInfoLabel();
    }

    
    public void addNodeToVisualization(TreeNode node, double x, double y) throws InterruptedException {
        Platform.runLater(() -> drawNodeAt(node, x, y));
        Thread.sleep(ANIM_DELAY);
    }

    public void connectNodes(TreeNode parent, TreeNode child, boolean isLeft) throws InterruptedException {
        Platform.runLater(() -> drawEdge(parent, child, isLeft));
        Thread.sleep(150);
    }

    public void highlightNode(TreeNode node) throws InterruptedException {
        Platform.runLater(() -> { if (node.circle != null) node.circle.setFill(Color.rgb(241, 196, 15)); });
        Thread.sleep(ANIM_DELAY);
        Platform.runLater(() -> { if (node.circle != null) node.circle.setFill(Color.rgb(52, 152, 219)); });
    }

    public void visitNode(TreeNode node) throws InterruptedException {
        Platform.runLater(() -> { if (node.circle != null) node.circle.setFill(Color.rgb(46, 204, 113)); });
        Thread.sleep(ANIM_DELAY);
    }

    public void foundNode(TreeNode node) throws InterruptedException {
        Platform.runLater(() -> {
            if (node.circle != null) {
                node.circle.setFill(Color.rgb(231, 76, 60));
                node.circle.setStrokeWidth(4);
            }
        });
        Thread.sleep(ANIM_DELAY);
    }

    public void removeNode(TreeNode node) throws InterruptedException {
        
        Platform.runLater(() -> {
            if (node.circle != null) {
                node.circle.setFill(Color.rgb(231, 76, 60));
                node.circle.setStrokeWidth(4);
            }
        });
        Thread.sleep(ANIM_DELAY);

        Platform.runLater(() -> {
            if (node.circle    != null) treePane.getChildren().remove(node.circle);
            if (node.label     != null) treePane.getChildren().remove(node.label);
            if (node.leftLine  != null) treePane.getChildren().remove(node.leftLine);
            if (node.rightLine != null) treePane.getChildren().remove(node.rightLine);
        });
        Thread.sleep(ANIM_DELAY);
    }

    public void updateNodeValue(TreeNode node, int newValue) throws InterruptedException {
       
        Platform.runLater(() -> {
            if (node.circle != null) {
                node.circle.setFill(Color.rgb(230, 126, 34));
                node.circle.setStrokeWidth(4);
                node.circle.setStroke(Color.rgb(211, 84, 0));
            }
        });
        Platform.runLater(() -> statusLabel.setText("Replacing deleted node with successor: " + newValue));
        Thread.sleep(ANIM_DELAY * 2);

        Platform.runLater(() -> {
            if (node.label != null) {
                node.label.setText(String.valueOf(newValue));
                node.label.setX(node.x - (newValue < 10 ? 5 : newValue < 100 ? 8 : 12));
            }
            if (node.circle != null) {
                node.circle.setFill(Color.rgb(46, 204, 113));
                node.circle.setStrokeWidth(2);
                node.circle.setStroke(Color.rgb(41, 128, 185));
            }
        });
        Thread.sleep(ANIM_DELAY);
    }

    

    private void completeRedraw() {
        treePane.getChildren().clear();
        if (bst.root != null) {
            redrawNode(bst.root, ROOT_X, ROOT_Y, INITIAL_OFFSET);
        }
    }

    
    private void redrawNode(TreeNode node, double x, double y, double offset) {
        if (node == null) return;

        drawNodeAt(node, x, y);

        double childOffset = Math.max(offset / 2.0, MIN_SEP);

        if (node.left != null) {
            redrawNode(node.left,  x - childOffset, y + LEVEL_HEIGHT, childOffset);
            drawEdge(node, node.left,  true);
        }
        if (node.right != null) {
            redrawNode(node.right, x + childOffset, y + LEVEL_HEIGHT, childOffset);
            drawEdge(node, node.right, false);
        }
    }

   

    private void drawNodeAt(TreeNode node, double x, double y) {
        Circle circle = new Circle(NODE_RADIUS);
        circle.setCenterX(x);
        circle.setCenterY(y);
        circle.setFill(Color.rgb(52, 152, 219));
        circle.setStroke(Color.rgb(41, 128, 185));
        circle.setStrokeWidth(2);

        Text label = new Text(String.valueOf(node.value));
        label.setX(x - (node.value < 10 ? 5 : node.value < 100 ? 8 : 12));
        label.setY(y + 5);
        label.setFill(Color.WHITE);
        label.setFont(Font.font("Arial", 14));
        label.setStyle("-fx-font-weight: bold;");

        node.circle = circle;
        node.label  = label;
        node.x      = x;
        node.y      = y;

        treePane.getChildren().addAll(circle, label);
    }

    private void drawEdge(TreeNode parent, TreeNode child, boolean isLeft) {
        Line line = new Line(
                parent.x, parent.y + NODE_RADIUS,
                child.x,  child.y  - NODE_RADIUS);
        line.setStroke(Color.GRAY);
        line.setStrokeWidth(2);

        if (isLeft) parent.leftLine  = line;
        else        parent.rightLine = line;

        treePane.getChildren().add(0, line);
    }



    private void highlightNodeSync(TreeNode node) {
        if (node != null && node.circle != null) {
            node.circle.setFill(Color.rgb(231, 76, 60));
            node.circle.setStrokeWidth(4);
        }
    }

    private void resetColors() {
        if (bst.root != null) resetNodeColors(bst.root);
    }

    private void resetNodeColors(TreeNode node) {
        if (node == null) return;
        if (node.circle != null) {
            node.circle.setFill(Color.rgb(52, 152, 219));
            node.circle.setStrokeWidth(2);
            node.circle.setStroke(Color.rgb(41, 128, 185));
        }
        resetNodeColors(node.left);
        resetNodeColors(node.right);
    }

    private void updateInfoLabel() {
        if (bst.root == null) {
            infoLabel.setText("Nodes: 0 | Height: 0 | Leaves: 0");
        } else {
            infoLabel.setText("Nodes: "     + countNodes(bst.root)  +
                    " | Height: " + bst.root.getHeight()  +
                    " | Leaves: " + countLeaves(bst.root));
        }
    }

    private int countNodes(TreeNode node) {
        if (node == null) return 0;
        return 1 + countNodes(node.left) + countNodes(node.right);
    }

    private int countLeaves(TreeNode node) {
        if (node == null) return 0;
        if (node.left == null && node.right == null) return 1;
        return countLeaves(node.left) + countLeaves(node.right);
    }
}
