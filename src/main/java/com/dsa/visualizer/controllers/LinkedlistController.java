package com.dsa.visualizer.controllers;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LinkedlistController {

    @FXML
    private TextField inputField;
    @FXML
    private TextField indexField;
    @FXML
    private Label statusLabel;
    @FXML
    private Pane visualPane;
    @FXML
    private Button btnInsertHead;
    @FXML
    private Button btnInsertTail;
    @FXML
    private Button btnInsertAt;
    @FXML
    private Button btnDeleteHead;
    @FXML
    private Button btnDeleteTail;
    @FXML
    private Button btnDeleteAt;
    @FXML private Button btnSearch;
    @FXML private Button btnRandom;
    @FXML private Button btnClear;
    @FXML private Label  sizeLabel;

    private List<Integer> linkedList = new ArrayList<>();

    private static final double NODE_WIDTH  = 70;
    private static final double NODE_HEIGHT = 40;
    private static final double H_GAP      = 30;
    private static final double START_X    = 20;
    private static final double START_Y    = 80;
    private static final int    MAX_NODES  = 15;

    @FXML
    public void initialize() {
        setStatus("Welcome! Enter a value and use the buttons to interact with the Linked List.", "#2C3E50");
        drawList(-1);
    }

    @FXML
    private void handleInsertHead() {
        if (linkedList.size() >= MAX_NODES) {
            setStatus("Limit reached! Maximum " + MAX_NODES + " nodes allowed.", "#E74C3C");
            return;
        }
        Integer val = parseInput();
        if (val == null) return;
        linkedList.add(0, val);
        setStatus("Inserted " + val + " at Head.", "#27AE60");
        drawList(0);
        inputField.clear();
    }

    @FXML
    private void handleInsertTail() {
        if (linkedList.size() >= MAX_NODES) {
            setStatus("Limit reached! Maximum " + MAX_NODES + " nodes allowed.", "#E74C3C");
            return;
        }
        Integer val = parseInput();
        if (val == null) return;
        linkedList.add(val);
        setStatus("Inserted " + val + " at Tail.", "#27AE60");
        drawList(linkedList.size() - 1);
        inputField.clear();
    }

    @FXML
    private void handleInsertAt() {
        if (linkedList.size() >= MAX_NODES) {
            setStatus("Limit reached! Maximum " + MAX_NODES + " nodes allowed.", "#E74C3C");
            return;
        }
        Integer val = parseInput();
        Integer idx = parseIndex();
        if (val == null || idx == null) return;
        if (idx < 0 || idx > linkedList.size()) {
            setStatus("Index out of bounds! Valid range: 0 to " + linkedList.size(), "#E74C3C");
            return;
        }
        linkedList.add(idx, val);
        setStatus("Inserted " + val + " at index " + idx + ".", "#27AE60");
        drawList(idx);
        inputField.clear();
        indexField.clear();
    }

    @FXML
    private void handleDeleteHead() {
        if (linkedList.isEmpty()) {
            setStatus("List is empty!", "#E74C3C");
            return;
        }
        int removed = linkedList.remove(0);
        setStatus("Deleted head node: " + removed, "#E74C3C");
        drawList(-1);
    }

    @FXML
    private void handleDeleteTail() {
        if (linkedList.isEmpty()) {
            setStatus("List is empty!", "#E74C3C");
            return;
        }
        int removed = linkedList.remove(linkedList.size() - 1);
        setStatus("Deleted tail node: " + removed, "#E74C3C");
        drawList(-1);
    }

    @FXML
    private void handleDeleteAt() {
        Integer idx = parseIndex();
        if (idx == null) return;
        if (idx < 0 || idx >= linkedList.size()) {
            setStatus("Index out of bounds! Valid range: 0 to " + (linkedList.size() - 1), "#E74C3C");
            return;
        }
        int removed = linkedList.remove((int) idx);
        setStatus("Deleted node at index " + idx + ": " + removed, "#E74C3C");
        drawList(-1);
        indexField.clear();
    }

    @FXML
    private void handleSearch() {
        Integer val = parseInput();
        if (val == null) return;
        int foundIdx = -1;
        for (int i = 0; i < linkedList.size(); i++) {
            if (linkedList.get(i).equals(val)) {
                foundIdx = i;
                break;
            }
        }
        if (foundIdx >= 0) {
            setStatus("Found " + val + " at index " + foundIdx + "!", "#8E44AD");
            drawList(foundIdx);
        } else {
            setStatus(val + " not found in the list.", "#E74C3C");
            drawList(-1);
        }
        inputField.clear();
    }

    @FXML
    private void handleClear() {
        linkedList.clear();
        setStatus("List cleared.", "#7F8C8D");
        drawList(-1);
    }

    @FXML
    private void handleGenerateRandom() {
        linkedList.clear();
        Random rng = new Random();
        int count = 4 + rng.nextInt(7); // 4–10 nodes
        for (int i = 0; i < count; i++)
            linkedList.add(rng.nextInt(99) + 1);
        setStatus("Generated random list with " + count + " nodes.", "#9B59B6");
        drawList(-1);
    }


    private void drawList(int highlightIndex) {
        visualPane.getChildren().clear();

        // Update size label in status bar
        sizeLabel.setText("Size: " + linkedList.size() + " / " + MAX_NODES);

        if (linkedList.isEmpty()) {
            Text empty = new Text("[ Empty list ]");
            empty.setFont(Font.font("System", FontWeight.BOLD, 18));
            empty.setFill(Color.web("#95A5A6"));
            empty.setLayoutX(START_X + 20);
            empty.setLayoutY(START_Y + NODE_HEIGHT / 2 + 6);
            visualPane.getChildren().add(empty);
            visualPane.setPrefWidth(1400);
            return;
        }

        // Expand pane so ScrollPane knows how far to scroll
        double requiredWidth = START_X + linkedList.size() * (NODE_WIDTH + H_GAP) + 100;
        visualPane.setPrefWidth(Math.max(1400, requiredWidth));

        double x = START_X;


        Text headLabel = new Text("HEAD");
        headLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        headLabel.setFill(Color.web("#E67E22"));
        headLabel.setLayoutX(x + 18);
        headLabel.setLayoutY(START_Y - 8);
        visualPane.getChildren().add(headLabel);

        for (int i = 0; i < linkedList.size(); i++) {
            boolean highlight = (i == highlightIndex);
            drawNode(x, START_Y, linkedList.get(i), i, highlight);


            if (i < linkedList.size() - 1) {
                drawArrow(x + NODE_WIDTH, START_Y + NODE_HEIGHT / 2,
                        x + NODE_WIDTH + H_GAP, START_Y + NODE_HEIGHT / 2);
            }

            x += NODE_WIDTH + H_GAP;
        }


        double tailX = START_X + (linkedList.size() - 1) * (NODE_WIDTH + H_GAP);
        Text tailLabel = new Text("TAIL");
        tailLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        tailLabel.setFill(Color.web("#E67E22"));
        tailLabel.setLayoutX(tailX + 18);
        tailLabel.setLayoutY(START_Y + NODE_HEIGHT + 18);
        visualPane.getChildren().add(tailLabel);

        addNullLabel(x);
    }

    private void drawNode(double x, double y, int value, int index, boolean highlight) {

        Rectangle dataBox = new Rectangle(x, y, NODE_WIDTH - 20, NODE_HEIGHT);
        dataBox.setFill(highlight ? Color.web("#F39C12") : Color.web("#3498DB"));
        dataBox.setStroke(Color.web("#2C3E50"));
        dataBox.setStrokeWidth(2);
        dataBox.setArcWidth(6);
        dataBox.setArcHeight(6);

        Text valText = new Text(String.valueOf(value));
        valText.setFont(Font.font("System", FontWeight.BOLD, 14));
        valText.setFill(Color.WHITE);
        valText.setLayoutX(x + (NODE_WIDTH - 20) / 2 - valText.getLayoutBounds().getWidth() / 2 - 1);
        valText.setLayoutY(y + NODE_HEIGHT / 2 + 5);


        Rectangle ptrBox = new Rectangle(x + NODE_WIDTH - 20, y, 20, NODE_HEIGHT);
        ptrBox.setFill(Color.web("#2980B9"));
        ptrBox.setStroke(Color.web("#2C3E50"));
        ptrBox.setStrokeWidth(2);

        Text ptrText = new Text("→");
        ptrText.setFont(Font.font("System", 11));
        ptrText.setFill(Color.WHITE);
        ptrText.setLayoutX(x + NODE_WIDTH - 15);
        ptrText.setLayoutY(y + NODE_HEIGHT / 2 + 4);

        Text idxText = new Text("[" + index + "]");

        idxText.setFont(Font.font("System", FontWeight.BOLD, 14));

        idxText.setFill(Color.web("#7F8C8D"));
        idxText.setLayoutX(x + (NODE_WIDTH / 2) - 8);
        idxText.setLayoutY(y + NODE_HEIGHT + 40);

        visualPane.getChildren().addAll(dataBox, ptrBox, valText, ptrText, idxText);
    }

    private void drawArrow(double x1, double y1, double x2, double y2) {
        Line line = new Line(x1, y1, x2, y2);
        line.setStroke(Color.web("#2C3E50"));
        line.setStrokeWidth(2);


        Polygon arrow = new Polygon();
        arrow.getPoints().addAll(x2, y2, x2 - 8, y2 - 4, x2 - 8, y2 + 4);
        arrow.setFill(Color.web("#2C3E50"));

        visualPane.getChildren().addAll(line, arrow);
    }

    private void addNullLabel(double x) {

        Line line = new Line(x, START_Y + NODE_HEIGHT / 2, x + 10, START_Y + NODE_HEIGHT / 2);
        line.setStroke(Color.web("#2C3E50"));
        line.setStrokeWidth(2);

        Text nullText = new Text("NULL");
        nullText.setFont(Font.font("System", FontWeight.BOLD, 13));
        nullText.setFill(Color.web("#E74C3C"));
        nullText.setLayoutX(x + 12);
        nullText.setLayoutY(START_Y + NODE_HEIGHT / 2 + 5);

        visualPane.getChildren().addAll(line, nullText);
    }


    public void visualizeNodeVisit(int index, int value, String phase) {
        Platform.runLater(() -> {
            drawList(index);
            switch (phase) {
                case "search":
                    setStatus("Searching… checking index " + index + " → value: " + value, "#8E44AD");
                    break;
                case "insertion-walk":
                    setStatus("Walking to insertion point… at index " + index, "#2980B9");
                    break;
                case "deletion-walk":
                    setStatus("Walking to deletion point… at index " + index, "#E67E22");
                    break;
                case "delete-target":
                    setStatus("Found node to delete at index " + index + " → value: " + value, "#E74C3C");
                    break;
                default:
                    setStatus("Visiting index " + index + " → value: " + value, "#2C3E50");
            }
        });
    }

    public void visualizeFound(int index, int value) {
        Platform.runLater(() -> {
            drawList(index);
            setStatus("✔ Found " + value + " at index " + index + "!", "#27AE60");
        });
    }

    public void visualizeNotFound(int target) {
        Platform.runLater(() -> {
            drawList(-1);
            setStatus("✘ Value " + target + " not found in the list.", "#E74C3C");
        });
    }

    public void visualizeInsert(int index, int value) {
        Platform.runLater(() -> {
            linkedList.add(index, value);
            drawList(index);
            setStatus("✔ Inserted " + value + " at index " + index + ".", "#27AE60");
        });
    }


    public void visualizeDelete(int index, int value) {
        Platform.runLater(() -> {
            linkedList.remove(index);
            drawList(-1);
            setStatus("✔ Deleted value " + value + " from index " + index + ".", "#E74C3C");
        });
    }

    public void visualizeComplete(String message) {
        Platform.runLater(() -> setStatus(message, "#2C3E50"));
    }

    @FXML
    private void handleAnimatedTraversal() {
        if (linkedList.isEmpty()) {
            setStatus("List is empty!", "#E74C3C");
            return;
        }
        List<Integer> snapshot = new ArrayList<>(linkedList);
        new Thread(() -> {
            try {
                new com.dsa.visualizer.algorithms.linkedlist.LinkedListAlgorithm()
                        .traverse(snapshot, this);
            } catch (InterruptedException ignored) {
            }
        }).start();
    }

    @FXML
    private void handleAnimatedSearch() {
        Integer val = parseInput();
        if (val == null) return;
        if (linkedList.isEmpty()) {
            setStatus("List is empty!", "#E74C3C");
            return;
        }
        List<Integer> snapshot = new ArrayList<>(linkedList);
        int target = val;
        new Thread(() -> {
            try {
                new com.dsa.visualizer.algorithms.linkedlist.LinkedListAlgorithm()
                        .searchTraversal(snapshot, target, this);
            } catch (InterruptedException ignored) {
            }
        }).start();
        inputField.clear();
    }


    private Integer parseInput() {
        try {
            return Integer.parseInt(inputField.getText().trim());
        } catch (NumberFormatException e) {
            setStatus("Please enter a valid integer value.", "#E74C3C");
            return null;
        }
    }

    private Integer parseIndex() {
        try {
            return Integer.parseInt(indexField.getText().trim());
        } catch (NumberFormatException e) {
            setStatus("Please enter a valid integer index.", "#E74C3C");
            return null;
        }
    }

    private void setStatus(String msg, String color) {
        statusLabel.setText(msg);

        statusLabel.setStyle(
//                "-fx-text-fill: " + color + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 12px;" +
                        "-fx-padding: 8 12 8 12;" +
                        "-fx-alignment: center-left;" +
                        "-fx-background-color: #34495E;" +
//                        "-fx-border-color: #D5DBDB;" +
//                        "-fx-border-width: 1;" +
//                        "-fx-border-radius: 6;" +
                        "-fx-background-radius: 6;"
        );

    }
}