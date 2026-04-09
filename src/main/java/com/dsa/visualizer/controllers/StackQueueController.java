package com.dsa.visualizer.controllers;
import com.dsa.visualizer.algorithms.stack.Stack;
import com.dsa.visualizer.algorithms.queue.Queue;

import com.dsa.visualizer.models.QueueNode;
import com.dsa.visualizer.models.StackNode;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.*;

public class StackQueueController implements Initializable {

    @FXML private Pane visualizationPane;
    @FXML private TextField inputField;
    @FXML private Label statusLabel;
    @FXML private Label sizeLabel;
    @FXML private RadioButton rbStack;
    @FXML private RadioButton rbQueue;
    @FXML private Button btnPush;
    @FXML private Button btnPop;
    @FXML private Button btnPeek;
    @FXML private Button btnPeekRear;
    @FXML private Button btnReverse;
    @FXML private Button btnSearch;

    private Stack stack;
    private Queue queue;
    private boolean isStackMode = true;
    private boolean isRunning = false;
    private static final int MAX_SIZE = 10;
    private static final double CELL_WIDTH = 80;
    private static final double CELL_HEIGHT = 50;
    private static final int ANIMATION_DELAY = 300;

    private ToggleGroup dataStructureGroup;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        stack = new Stack(MAX_SIZE);
        queue = new Queue(MAX_SIZE);

        dataStructureGroup = new ToggleGroup();
        rbStack.setToggleGroup(dataStructureGroup);
        rbQueue.setToggleGroup(dataStructureGroup);
        rbStack.setSelected(true);

        rbStack.setOnAction(e -> switchToStack());
        rbQueue.setOnAction(e -> switchToQueue());

        updateButtonLabels();
        updateLabels();
        statusLabel.setText("Stack mode - Push/Pop from top");
    }

    private void switchToStack() {
        if (isRunning) return;
        isStackMode = true;
        clearAll();
        updateButtonLabels();
        statusLabel.setText("Stack mode - Push/Pop from top");
        btnPeekRear.setVisible(false);
        btnReverse.setVisible(false);
    }

    private void switchToQueue() {
        if (isRunning) return;
        isStackMode = false;
        clearAll();
        updateButtonLabels();
        statusLabel.setText("Queue mode - Enqueue/Dequeue from front");
        btnPeekRear.setVisible(true);
        btnReverse.setVisible(true);
    }

    private void updateButtonLabels() {
        if (isStackMode) {
            btnPush.setText("Push");
            btnPop.setText("Pop");
            btnPeek.setText("Peek");
        } else {
            btnPush.setText("Enqueue");
            btnPop.setText("Dequeue");
            btnPeek.setText("Peek Front");
        }
    }

    @FXML
    private void handlePush() {
        if (isRunning) return;
        String input = inputField.getText().trim();
        if (input.isEmpty()) { statusLabel.setText("Please enter a value"); return; }

        try {
            int value = Integer.parseInt(input);
            inputField.clear();

            if (isStackMode && stack.isFull()) { statusLabel.setText("Stack is full! (Max " + MAX_SIZE + ")"); return; }
            if (!isStackMode && queue.isFull()) { statusLabel.setText("Queue is full! (Max " + MAX_SIZE + ")"); return; }

            isRunning = true;
            new Thread(() -> {
                try {
                    if (isStackMode) {
                        stack.push(value, this);
                        Platform.runLater(() -> { statusLabel.setText("Pushed: " + value); updateLabels(); isRunning = false; });
                    } else {
                        queue.enqueue(value, this);
                        Platform.runLater(() -> { statusLabel.setText("Enqueued: " + value); updateLabels(); isRunning = false; });
                    }
                } catch (InterruptedException e) {
                    Platform.runLater(() -> { statusLabel.setText("Interrupted"); isRunning = false; });
                }
            }).start();
        } catch (NumberFormatException e) {
            statusLabel.setText("Invalid number format");
        }
    }

    @FXML
    private void handlePop() {
        if (isRunning) return;
        if (isStackMode && stack.isEmpty()) { statusLabel.setText("Stack is empty!"); return; }
        if (!isStackMode && queue.isEmpty()) { statusLabel.setText("Queue is empty!"); return; }

        isRunning = true;
        new Thread(() -> {
            try {
                if (isStackMode) {
                    Integer value = stack.pop(this);
                    Platform.runLater(() -> { statusLabel.setText(value != null ? "Popped: " + value : "Stack empty"); updateLabels(); isRunning = false; });
                } else {
                    Integer value = queue.dequeue(this);
                    Platform.runLater(() -> { statusLabel.setText(value != null ? "Dequeued: " + value : "Queue empty"); updateLabels(); isRunning = false; });
                }
            } catch (InterruptedException e) {
                Platform.runLater(() -> { statusLabel.setText("Interrupted"); isRunning = false; });
            }
        }).start();
    }

    @FXML
    private void handlePeek() {
        if (isRunning) return;
        if (isStackMode && stack.isEmpty()) { statusLabel.setText("Stack is empty!"); return; }
        if (!isStackMode && queue.isEmpty()) { statusLabel.setText("Queue is empty!"); return; }

        isRunning = true;
        new Thread(() -> {
            try {
                Integer value = isStackMode ? stack.peek(this) : queue.peek(this);
                Platform.runLater(() -> { statusLabel.setText((isStackMode ? "Top: " : "Front: ") + value); isRunning = false; });
            } catch (InterruptedException e) {
                Platform.runLater(() -> { statusLabel.setText("Interrupted"); isRunning = false; });
            }
        }).start();
    }

    @FXML
    private void handlePeekRear() {
        if (isRunning || isStackMode) return;
        if (queue.isEmpty()) { statusLabel.setText("Queue is empty!"); return; }

        isRunning = true;
        new Thread(() -> {
            try {
                Integer value = queue.peekRear(this);
                Platform.runLater(() -> { statusLabel.setText("Rear: " + value); isRunning = false; });
            } catch (InterruptedException e) {
                Platform.runLater(() -> { statusLabel.setText("Interrupted"); isRunning = false; });
            }
        }).start();
    }

    @FXML
    private void handleSearch() {
        if (isRunning) return;
        String input = inputField.getText().trim();
        if (input.isEmpty()) { statusLabel.setText("Please enter a value to search"); return; }

        try {
            int value = Integer.parseInt(input);
            if (isStackMode && stack.isEmpty()) { statusLabel.setText("Stack is empty!"); return; }
            if (!isStackMode && queue.isEmpty()) { statusLabel.setText("Queue is empty!"); return; }

            isRunning = true;
            new Thread(() -> {
                try {
                    int pos = isStackMode ? stack.search(value, this) : queue.search(value, this);
                    Platform.runLater(() -> {
                        statusLabel.setText(pos != -1 ? "Found " + value + " at position " + pos : "Not found: " + value);
                        isRunning = false;
                    });
                } catch (InterruptedException e) {
                    Platform.runLater(() -> { statusLabel.setText("Interrupted"); isRunning = false; });
                }
            }).start();
        } catch (NumberFormatException e) {
            statusLabel.setText("Invalid number format");
        }
    }

    @FXML
    private void handleClear() {
        if (isRunning) return;
        clearAll();
        statusLabel.setText("Cleared all elements");
    }

    @FXML
    private void handleRandom() {
        if (isRunning) return;
        clearAll();
        Random random = new Random();
        int count = 4 + random.nextInt(4);

        isRunning = true;
        new Thread(() -> {
            try {
                for (int i = 0; i < count; i++) {
                    int value = random.nextInt(99) + 1;
                    if (isStackMode) stack.push(value, this);
                    else queue.enqueue(value, this);
                    Thread.sleep(300);
                }
                Platform.runLater(() -> { statusLabel.setText("Generated " + count + " random elements"); updateLabels(); isRunning = false; });
            } catch (InterruptedException e) {
                Platform.runLater(() -> { statusLabel.setText("Interrupted"); isRunning = false; });
            }
        }).start();
    }

    @FXML
    private void handleReverse() {
        if (isRunning || isStackMode) return;
        if (queue.isEmpty()) { statusLabel.setText("Queue is empty!"); return; }

        isRunning = true;
        new Thread(() -> {
            try {
                queue.reverse(this);
                Platform.runLater(() -> { statusLabel.setText("Queue reversed"); isRunning = false; });
            } catch (InterruptedException e) {
                Platform.runLater(() -> { statusLabel.setText("Interrupted"); isRunning = false; });
            }
        }).start();
    }

    // ─── Stack visualization ──────────────────────────────────────────────────

    public void addStackNode(StackNode node, int position) throws InterruptedException {
        Platform.runLater(this::redrawStackNow);
        Thread.sleep(ANIMATION_DELAY);
    }

    public void removeStackNode(StackNode node) throws InterruptedException {
        Platform.runLater(this::redrawStackNow);
        Thread.sleep(ANIMATION_DELAY);
    }

    public void highlightStackNode(StackNode node) throws InterruptedException {
        Platform.runLater(() -> { if (node.rectangle != null) node.rectangle.setFill(Color.rgb(241, 196, 15)); });
        Thread.sleep(ANIMATION_DELAY);
        Platform.runLater(() -> { if (node.rectangle != null) node.rectangle.setFill(Color.rgb(52, 152, 219)); });
    }

    public void foundStackNode(StackNode node) throws InterruptedException {
        Platform.runLater(() -> {
            if (node.rectangle != null) { node.rectangle.setFill(Color.rgb(46, 204, 113)); node.rectangle.setStrokeWidth(3); }
        });
        Thread.sleep(ANIMATION_DELAY);
    }

    public void updateStackSize(int size) {
        Platform.runLater(() -> sizeLabel.setText("Size: " + size + " / " + MAX_SIZE));
    }

    // ─── Queue visualization ──────────────────────────────────────────────────

    public void addQueueNode(QueueNode node, int position) throws InterruptedException {
        Platform.runLater(this::redrawQueueNow);
        Thread.sleep(ANIMATION_DELAY);
    }

    public void removeQueueNode(QueueNode node) throws InterruptedException {
        Platform.runLater(this::redrawQueueNow);
        Thread.sleep(ANIMATION_DELAY);
    }

    public void highlightQueueNode(QueueNode node) throws InterruptedException {
        Platform.runLater(() -> { if (node.rectangle != null) node.rectangle.setFill(Color.rgb(241, 196, 15)); });
        Thread.sleep(ANIMATION_DELAY);
        Platform.runLater(() -> { if (node.rectangle != null) node.rectangle.setFill(Color.rgb(52, 152, 219)); });
    }

    public void foundQueueNode(QueueNode node) throws InterruptedException {
        Platform.runLater(() -> {
            if (node.rectangle != null) { node.rectangle.setFill(Color.rgb(46, 204, 113)); node.rectangle.setStrokeWidth(3); }
        });
        Thread.sleep(ANIMATION_DELAY);
    }

    public void updateQueueSize(int size) {
        Platform.runLater(() -> sizeLabel.setText("Size: " + size + " / " + MAX_SIZE));
    }

    public void showError(String message) {
        Platform.runLater(() -> statusLabel.setText(message));
    }

    // ─── Core redraw helpers (must run on FX thread) ──────────────────────────


    private void redrawStackNow() {
        visualizationPane.getChildren().clear();

        // Collect nodes from top to bottom, then reverse so index 0 = bottom
        Deque<StackNode> deque = new ArrayDeque<>();
        StackNode cur = stack.getTop();
        while (cur != null) {
            deque.addFirst(cur);
            cur = cur.next;
        }
        List<StackNode> ordered = new ArrayList<>(deque);

        int total = ordered.size();
        if (total == 0) return;

        double paneWidth  = Math.max(visualizationPane.getWidth(),  400);
        double paneHeight = Math.max(visualizationPane.getHeight(), 400);
        double centerX = (paneWidth - CELL_WIDTH) / 2;
        double baseY   = paneHeight - 70;
        double stepY   = CELL_HEIGHT + 8;

        for (int i = 0; i < total; i++) {
            StackNode node = ordered.get(i);
            boolean isTop = (i == total - 1);
            double y = baseY - i * stepY;

            Rectangle rect = new Rectangle(CELL_WIDTH, CELL_HEIGHT);
            rect.setX(centerX);
            rect.setY(y);
            rect.setFill(isTop ? Color.rgb(41, 128, 185) : Color.rgb(52, 152, 219));
            rect.setStroke(isTop ? Color.rgb(231, 76, 60) : Color.rgb(41, 128, 185));
            rect.setStrokeWidth(isTop ? 3 : 2);
            rect.setArcWidth(6);
            rect.setArcHeight(6);

            Text valLabel = new Text(String.valueOf(node.value));
            valLabel.setX(centerX + CELL_WIDTH / 2 - textOffset(node.value));
            valLabel.setY(y + CELL_HEIGHT / 2 + 6);
            valLabel.setFill(Color.WHITE);
            valLabel.setFont(Font.font("Arial", FontWeight.BOLD, 15));

            node.rectangle = rect;
            node.label = valLabel;
            node.x = centerX;
            node.y = y;

            visualizationPane.getChildren().addAll(rect, valLabel);

            if (isTop) {
                Text topTag = new Text("◄ TOP");
                topTag.setX(centerX + CELL_WIDTH + 8);
                topTag.setY(y + CELL_HEIGHT / 2 + 6);
                topTag.setFill(Color.rgb(231, 76, 60));
                topTag.setFont(Font.font("Arial", FontWeight.BOLD, 12));
                visualizationPane.getChildren().add(topTag);
            }
        }

        // Base line under stack
        javafx.scene.shape.Line base = new javafx.scene.shape.Line(
                centerX - 10, baseY + CELL_HEIGHT,
                centerX + CELL_WIDTH + 10, baseY + CELL_HEIGHT);
        base.setStroke(Color.DARKGRAY);
        base.setStrokeWidth(3);
        visualizationPane.getChildren().add(base);
    }


    private void redrawQueueNow() {
        visualizationPane.getChildren().clear();

        List<QueueNode> nodes = new ArrayList<>();
        QueueNode cur = queue.getFront();
        while (cur != null) { nodes.add(cur); cur = cur.next; }

        int total = nodes.size();
        if (total == 0) return;

        double paneWidth  = Math.max(visualizationPane.getWidth(),  400);
        double paneHeight = Math.max(visualizationPane.getHeight(), 300);
        double gap    = 30;  // enough room for the arrow between cells
        double stepX  = CELL_WIDTH + gap;
        double totalW = total * CELL_WIDTH + (total - 1) * gap;
        double startX = Math.max(20, (paneWidth - totalW) / 2);
        double cy     = (paneHeight - CELL_HEIGHT) / 2;

        for (int i = 0; i < total; i++) {
            QueueNode node = nodes.get(i);
            boolean isFront = (i == 0);
            boolean isRear  = (i == total - 1);
            double x = startX + i * stepX;

            Rectangle rect = new Rectangle(CELL_WIDTH, CELL_HEIGHT);
            rect.setX(x);
            rect.setY(cy);
            rect.setFill(Color.rgb(52, 152, 219));
            rect.setStroke(isFront ? Color.rgb(46, 204, 113) : isRear ? Color.rgb(231, 76, 60) : Color.rgb(41, 128, 185));
            rect.setStrokeWidth(isFront || isRear ? 3 : 2);
            rect.setArcWidth(6);
            rect.setArcHeight(6);

            Text valLabel = new Text(String.valueOf(node.value));
            valLabel.setX(x + CELL_WIDTH / 2 - textOffset(node.value));
            valLabel.setY(cy + CELL_HEIGHT / 2 + 6);
            valLabel.setFill(Color.WHITE);
            valLabel.setFont(Font.font("Arial", FontWeight.BOLD, 15));

            node.rectangle = rect;
            node.label = valLabel;
            node.x = x;
            node.y = cy;

            visualizationPane.getChildren().addAll(rect, valLabel);

            if (isFront) {
                Text fl = new Text("FRONT");
                fl.setX(x + CELL_WIDTH / 2 - 22);
                fl.setY(cy - 10);
                fl.setFill(Color.rgb(46, 204, 113));
                fl.setFont(Font.font("Arial", FontWeight.BOLD, 11));
                visualizationPane.getChildren().add(fl);
            }
            if (isRear) {
                Text rl = new Text("REAR");
                rl.setX(x + CELL_WIDTH / 2 - 16);
                rl.setY(cy + CELL_HEIGHT + 18);
                rl.setFill(Color.rgb(231, 76, 60));
                rl.setFont(Font.font("Arial", FontWeight.BOLD, 11));
                visualizationPane.getChildren().add(rl);
            }

            // Arrow between nodes — centered in the gap
            if (i < total - 1) {
                Text arrow = new Text("→");
                arrow.setX(x + CELL_WIDTH + gap / 2 - 6);
                arrow.setY(cy + CELL_HEIGHT / 2 + 6);
                arrow.setFill(Color.DARKGRAY);
                arrow.setFont(Font.font("Arial", 14));
                visualizationPane.getChildren().add(arrow);
            }
        }
    }

    private double textOffset(int value) {
        if (value < 10) return 5;
        if (value < 100) return 9;
        return 13;
    }

    private void clearAll() {
        visualizationPane.getChildren().clear();
        stack = new Stack(MAX_SIZE);
        queue = new Queue(MAX_SIZE);
        updateLabels();
    }

    private void updateLabels() {
        int size = isStackMode ? stack.getSize() : queue.getSize();
        sizeLabel.setText("Size: " + size + " / " + MAX_SIZE);
    }
}