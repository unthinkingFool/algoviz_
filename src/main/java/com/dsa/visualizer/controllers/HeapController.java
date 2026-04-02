package com.dsa.visualizer.controllers;

import com.dsa.visualizer.algorithms.heap.Heap;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.*;

public class HeapController implements Initializable {

    // ─── FXML fields — same as original ──────────────────────────────────────

    @FXML private Pane treePane;
    @FXML private Pane arrayPane;
    @FXML private Label statusLabel;
    @FXML private Label logLabel;
    @FXML private Label sizeLabel;

    @FXML private TextField insertField;
    @FXML private TextField buildArrayField;

    @FXML private Button btnInsert;
    @FXML private Button btnExtractMax;   // label changes to "Extract Min" in MIN mode
    @FXML private Button btnPeekMax;      // label changes to "Peek Min"  in MIN mode
    @FXML private Button btnHeapSort;
    @FXML private Button btnBuildHeap;
    @FXML private Button btnClear;
    @FXML private Button btnRandom;       // new generate random button

    // ─── NEW: Mode-selection buttons ─────────────────────────────────────────
    @FXML private Button btnMaxHeap;
    @FXML private Button btnMinHeap;

    // ─── NEW: The main operation panel — hidden until mode is chosen ──────────
    @FXML private VBox operationPanel;

    // ─── Colors ──────────────────────────────────────────────────────────────

    private static final String C_DEFAULT   = "#3498DB";
    private static final String C_COMPARE   = "#F1C40F";
    private static final String C_SWAP      = "#E74C3C";
    private static final String C_HIGHLIGHT = "#9B59B6";
    private static final String C_SORTED    = "#2ECC71";
    private static final String C_ROOT_MAX  = "#E67E22";   // orange for max-heap root
    private static final String C_ROOT_MIN  = "#1ABC9C";   // teal  for min-heap root

    private static final double R = 22;

    // ─── State ────────────────────────────────────────────────────────────────

    private Heap heap;
    private boolean isBusy = false;
    private Thread workerThread;
    private Heap.Mode currentMode = null;   // null = not yet chosen

    // ─── Initialize ──────────────────────────────────────────────────────────

//    @Override
//    public void initialize(URL location, ResourceBundle resources) {
//        // Hide the operation panel until the user picks a mode
//        operationPanel.setVisible(false);
//        operationPanel.setManaged(false);
//
//        // Default heap placeholder so nothing is null
//        heap = new Heap(this, Heap.Mode.MAX);
//        updateUI(Collections.emptyList(), -1, "Choose a heap type above to begin");
//    }
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        heap = new Heap(this, Heap.Mode.MAX);
        applyMode(Heap.Mode.MAX);  // auto-select Max-Heap on load
    }
    // ─── NEW: Mode selection handlers ────────────────────────────────────────

    @FXML
    private void selectMaxHeap() {
        applyMode(Heap.Mode.MAX);
    }

    @FXML
    private void selectMinHeap() {
        applyMode(Heap.Mode.MIN);
    }

    private void applyMode(Heap.Mode mode) {
        currentMode = mode;
        heap = new Heap(this, mode);

        // Update button labels
        boolean isMax = (mode == Heap.Mode.MAX);
        btnExtractMax.setText(isMax ? "Extract Max" : "Extract Min");
        btnPeekMax.setText(isMax    ? "Peek Max"    : "Peek Min");

        // Visual: highlight the selected mode button
        String activeStyle   = isMax
                ? "-fx-background-color: #E67E22; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; -fx-cursor: hand;"
                : "-fx-background-color: #1ABC9C; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; -fx-cursor: hand;";
        String inactiveStyle = "-fx-background-color: #BDC3C7; -fx-text-fill: #2C3E50; -fx-font-size: 13px; -fx-cursor: hand;";

        btnMaxHeap.setStyle(isMax  ? activeStyle : inactiveStyle);
        btnMinHeap.setStyle(!isMax ? activeStyle : inactiveStyle);

        // Show the operation panel
        operationPanel.setVisible(true);
        operationPanel.setManaged(true);

        String label = isMax ? "Max-Heap" : "Min-Heap";
        updateUI(Collections.emptyList(), -1, label + " selected — ready to use");
    }

    // ─── Original FXML handlers (unchanged logic, labels updated) ────────────

    @FXML
    private void handleInsert() {
        if (currentMode == null) { flash("Please select Max or Min heap first"); return; }
        String txt = insertField.getText().trim();
        if (txt.isEmpty()) { flash("Enter a value to insert"); return; }
        try {
            int val = Integer.parseInt(txt);
            if (val < 1 || val > 999) { flash("Value must be 1–999"); return; }
            insertField.clear();
            runAsync(() -> heap.insert(val));
        } catch (NumberFormatException e) {
            flash("Invalid number");
        }
    }

    @FXML
    private void handleExtractMax() {
        if (currentMode == null) { flash("Please select Max or Min heap first"); return; }
        if (heap.isEmpty()) { flash("Heap is empty!"); return; }
        runAsync(() -> {
            int root = heap.extractRoot();
            String label = currentMode == Heap.Mode.MAX ? "Extracted max: " : "Extracted min: ";
            Platform.runLater(() -> flash(label + root));
        });
    }

    @FXML
    private void handlePeekMax() {
        if (currentMode == null) { flash("Please select Max or Min heap first"); return; }
        if (heap.isEmpty()) { flash("Heap is empty!"); return; }
        int root = heap.peekRoot();
        String label = currentMode == Heap.Mode.MAX ? "Max (root) = " : "Min (root) = ";
        Platform.runLater(() -> {
            onHighlight(0, heap.getHeap(), "Peek Root  " + root);
            flash(label + root);
        });
    }

    @FXML
    private void handleHeapSort() {
        if (currentMode == null) { flash("Please select Max or Min heap first"); return; }
        if (heap.isEmpty()) { flash("Heap is empty!"); return; }
        runAsync(() -> heap.heapSort());
    }

    @FXML
    private void handleBuildHeap() {
        if (currentMode == null) { flash("Please select Max or Min heap first"); return; }
        String txt = buildArrayField.getText().trim();
        if (txt.isEmpty()) { flash("Enter comma-separated values"); return; }
        try {
            String[] parts = txt.split(",");
            int[] arr = new int[parts.length];
            for (int i = 0; i < parts.length; i++) {
                arr[i] = Integer.parseInt(parts[i].trim());
                if (arr[i] < 1 || arr[i] > 999) { flash("Values must be 1–999"); return; }
            }
            buildArrayField.clear();
            runAsync(() -> heap.buildHeap(arr));
        } catch (NumberFormatException e) {
            flash("Invalid input");
        }
    }

    // ─── NEW: Generate random heap ────────────────────────────────────────────

    @FXML
    private void handleRandom() {
        if (currentMode == null) { flash("Please select Max or Min heap first"); return; }
        runAsync(() -> heap.generateRandom());
    }

    @FXML
    private void handleClear() {
        if (workerThread != null && workerThread.isAlive()) workerThread.interrupt();
        isBusy = false;
        if (currentMode != null) {
            heap = new Heap(this, currentMode);
        }
        updateUI(Collections.emptyList(), -1, "Heap cleared");
        setControlsDisabled(false);
    }

    // ─── Visualization callbacks (called from Heap on background thread) ──────

    public void onHeapChanged(List<Integer> h, int highlight, String msg) throws InterruptedException {
        List<Integer> copy = new ArrayList<>(h);
        Platform.runLater(() -> updateUI(copy, highlight, msg));
        Thread.sleep(delay());
    }

    public void onCompare(int i, int j, List<Integer> h, String msg) throws InterruptedException {
        List<Integer> copy = new ArrayList<>(h);
        Platform.runLater(() -> {
            drawTree(copy, i, j, C_COMPARE, C_COMPARE);
            drawArrayBar(copy, i, j, C_COMPARE);
            logLabel.setText("⚖ " + msg);
        });
        Thread.sleep(delay());
    }

    public void onSwap(int i, int j, List<Integer> h, String msg) throws InterruptedException {
        List<Integer> copy = new ArrayList<>(h);
        Platform.runLater(() -> {
            drawTree(copy, i, j, C_SWAP, C_SWAP);
            drawArrayBar(copy, i, j, C_SWAP);
            logLabel.setText("🔄 " + msg);
        });
        Thread.sleep(delay());
    }

    public void onHighlight(int idx, List<Integer> h, String msg) {
        List<Integer> copy = new ArrayList<>(h);
        Platform.runLater(() -> {
            drawTree(copy, idx, -1, C_HIGHLIGHT, null);
            drawArrayBar(copy, idx, -1, C_HIGHLIGHT);
            logLabel.setText("📌 " + msg);
        });
    }

    public void onSortStep(List<Integer> remaining, List<Integer> sorted, String msg) throws InterruptedException {
        List<Integer> remCopy    = new ArrayList<>(remaining);
        List<Integer> sortedCopy = new ArrayList<>(sorted);
        Platform.runLater(() -> {
            drawSortStep(remCopy, sortedCopy);
            logLabel.setText("📊 " + msg);
        });
        Thread.sleep(delay());
    }

    public void onSortComplete(List<Integer> sorted, String msg) throws InterruptedException {
        List<Integer> copy = new ArrayList<>(sorted);
        Platform.runLater(() -> {
            drawSortComplete(copy);
            statusLabel.setText(msg);
            logLabel.setText("✅ " + msg);
        });
        Thread.sleep(delay());
    }

    // ─── UI helpers ───────────────────────────────────────────────────────────

    private void updateUI(List<Integer> h, int highlight, String msg) {
        drawTree(h, highlight, -1, C_HIGHLIGHT, null);
        drawArrayBar(h, highlight, -1, C_HIGHLIGHT);
        statusLabel.setText(msg);
        sizeLabel.setText("Size: " + h.size());
        setControlsDisabled(isBusy);
    }

    private String rootColor() {
        if (currentMode == Heap.Mode.MIN) return C_ROOT_MIN;
        return C_ROOT_MAX;
    }

    private double[] nodePos(int i, int n, double w, double h) {
        int depth      = (int) (Math.log(i + 1) / Math.log(2));
        int posInRow   = i - ((1 << depth) - 1);
        int nodesInRow = 1 << depth;
        double y = 50 + depth * 70;
        double x = w / (nodesInRow + 1) * (posInRow + 1);
        return new double[]{x, y};
    }

    private void drawTree(List<Integer> h, int hi1, int hi2, String col1, String col2) {
        treePane.getChildren().clear();
        if (h.isEmpty()) return;

        double w  = Math.max(treePane.getWidth(),  500);
        double ht = Math.max(treePane.getHeight(), 400);

        // Draw edges first
        for (int i = 1; i < h.size(); i++) {
            int    parent = (i - 1) / 2;
            double[] pc   = nodePos(parent, h.size(), w, ht);
            double[] cc   = nodePos(i,      h.size(), w, ht);
            Line line = new Line(pc[0], pc[1], cc[0], cc[1]);
            line.setStroke(Color.web("#7F8C8D"));
            line.setStrokeWidth(1.5);
            treePane.getChildren().add(line);
        }

        // Draw nodes
        for (int i = 0; i < h.size(); i++) {
            double[] pos = nodePos(i, h.size(), w, ht);
            String colour = C_DEFAULT;
            if (i == 0)                           colour = rootColor();
            if (i == hi1)                         colour = col1;
            else if (i == hi2 && col2 != null)    colour = col2;

            Circle circle = new Circle(pos[0], pos[1], R);
            circle.setFill(Color.web(colour));
            circle.setStroke(Color.web("#2C3E50"));
            circle.setStrokeWidth(2);

            Text idxText = new Text(pos[0] - 4, pos[1] - R - 4, String.valueOf(i));
            idxText.setFill(Color.web("#95A5A6"));
            idxText.setStyle("-fx-font-size: 9px;");

            Text valText = new Text(String.valueOf(h.get(i)));
            valText.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
            valText.setFill(Color.WHITE);
            valText.setX(pos[0] - valText.getLayoutBounds().getWidth() / 2 - 1);
            valText.setY(pos[1] + 4);

            treePane.getChildren().addAll(circle, idxText, valText);
        }
    }

    private void drawArrayBar(List<Integer> h, int hi1, int hi2, String hlCol) {
        arrayPane.getChildren().clear();
        if (h.isEmpty()) return;

        double totalW = Math.max(arrayPane.getWidth(), 500);
        double cellW  = Math.min(50, (totalW - 20) / h.size());
        double cellH  = 36;
        double startX = 10;
        double startY = 10;

        for (int i = 0; i < h.size(); i++) {
            String bg = C_DEFAULT;
            if (i == 0)      bg = rootColor();
            if (i == hi1)    bg = hlCol;
            else if (i == hi2) bg = hlCol;

            Rectangle rect = new Rectangle(startX + i * cellW, startY, cellW - 2, cellH);
            rect.setFill(Color.web(bg));
            rect.setStroke(Color.web("#2C3E50"));
            rect.setArcWidth(4);
            rect.setArcHeight(4);

            Text val = new Text(String.valueOf(h.get(i)));
            val.setFill(Color.WHITE);
            val.setStyle("-fx-font-size: 11px; -fx-font-weight: bold;");
            val.setX(startX + i * cellW + cellW / 2 - val.getLayoutBounds().getWidth() / 2);
            val.setY(startY + cellH / 2 + 4);

            Text idx = new Text(String.valueOf(i));
            idx.setFill(Color.web("#95A5A6"));
            idx.setStyle("-fx-font-size: 9px;");
            idx.setX(startX + i * cellW + cellW / 2 - 3);
            idx.setY(startY + cellH + 14);

            arrayPane.getChildren().addAll(rect, val, idx);
        }
    }

    private void drawSortStep(List<Integer> remaining, List<Integer> sorted) {
        treePane.getChildren().clear();
        arrayPane.getChildren().clear();

        double w  = Math.max(treePane.getWidth(),  500);
        double ht = Math.max(treePane.getHeight(), 400);

        for (int i = 1; i < remaining.size(); i++) {
            int    parent = (i - 1) / 2;
            double[] pc   = nodePos(parent, remaining.size(), w, ht);
            double[] cc   = nodePos(i,      remaining.size(), w, ht);
            Line line = new Line(pc[0], pc[1], cc[0], cc[1]);
            line.setStroke(Color.web("#7F8C8D"));
            line.setStrokeWidth(1.5);
            treePane.getChildren().add(line);
        }
        for (int i = 0; i < remaining.size(); i++) {
            double[] pos = nodePos(i, remaining.size(), w, ht);
            Circle c = new Circle(pos[0], pos[1], R);
            c.setFill(Color.web(i == 0 ? rootColor() : C_DEFAULT));
            c.setStroke(Color.web("#2C3E50"));
            c.setStrokeWidth(2);
            Text t = new Text(String.valueOf(remaining.get(i)));
            t.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
            t.setFill(Color.WHITE);
            t.setX(pos[0] - t.getLayoutBounds().getWidth() / 2 - 1);
            t.setY(pos[1] + 4);
            treePane.getChildren().addAll(c, t);
        }

        double cellW = Math.min(50, (Math.max(arrayPane.getWidth(), 500) - 20) / Math.max(sorted.size(), 1));
        for (int i = 0; i < sorted.size(); i++) {
            Rectangle r = new Rectangle(10 + i * cellW, 10, cellW - 2, 36);
            r.setFill(Color.web(C_SORTED));
            r.setStroke(Color.web("#2C3E50"));
            r.setArcWidth(4);
            r.setArcHeight(4);
            Text t = new Text(String.valueOf(sorted.get(i)));
            t.setFill(Color.WHITE);
            t.setStyle("-fx-font-size: 11px; -fx-font-weight: bold;");
            t.setX(10 + i * cellW + cellW / 2 - t.getLayoutBounds().getWidth() / 2);
            t.setY(32);
            arrayPane.getChildren().addAll(r, t);
        }
    }

    private void drawSortComplete(List<Integer> sorted) {
        treePane.getChildren().clear();
        arrayPane.getChildren().clear();

        double cellW = Math.min(50, (Math.max(arrayPane.getWidth(), 500) - 20) / sorted.size());
        for (int i = 0; i < sorted.size(); i++) {
            Rectangle r = new Rectangle(10 + i * cellW, 10, cellW - 2, 36);
            r.setFill(Color.web(C_SORTED));
            r.setStroke(Color.web("#2C3E50"));
            r.setArcWidth(4);
            r.setArcHeight(4);
            Text t = new Text(String.valueOf(sorted.get(i)));
            t.setFill(Color.WHITE);
            t.setStyle("-fx-font-size: 11px; -fx-font-weight: bold;");
            t.setX(10 + i * cellW + cellW / 2 - t.getLayoutBounds().getWidth() / 2);
            t.setY(32);
            arrayPane.getChildren().addAll(r, t);
        }

        Text label = new Text(10, 65, currentMode == Heap.Mode.MAX
                ? "Sorted ascending ↑  (Max-Heap Sort)"
                : "Sorted descending ↓  (Min-Heap Sort)");
        label.setFill(Color.web(C_SORTED));
        label.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
        arrayPane.getChildren().add(label);
    }

    // ─── Async runner (same as original) ─────────────────────────────────────

    @FunctionalInterface
    interface HeapOp {
        void run() throws Exception;
    }

    private void runAsync(HeapOp op) {
        if (isBusy) { flash("Operation in progress..."); return; }
        isBusy = true;
        setControlsDisabled(true);

        workerThread = new Thread(() -> {
            try {
                op.run();
            } catch (InterruptedException e) {
                Platform.runLater(() -> statusLabel.setText("Operation interrupted"));
            } catch (Exception e) {
                Platform.runLater(() -> flash("Error: " + e.getMessage()));
            } finally {
                isBusy = false;
                Platform.runLater(() -> {
                    setControlsDisabled(false);
                    updateUI(heap.getHeap(), -1, statusLabel.getText());
                });
            }
        });
        workerThread.setDaemon(true);
        workerThread.start();
    }

    private void setControlsDisabled(boolean disabled) {
        btnInsert.setDisable(disabled);
        btnExtractMax.setDisable(disabled);
        btnPeekMax.setDisable(disabled);
        btnHeapSort.setDisable(disabled);
        btnBuildHeap.setDisable(disabled);
        btnRandom.setDisable(disabled);
    }

    private void flash(String msg) {
        Platform.runLater(() -> statusLabel.setText(msg));
    }

    private long delay() {
        return 1000L;
    }
}
//package com.dsa.visualizer.controllers;
//
//import com.dsa.visualizer.algorithms.heap.Heap;
//import javafx.application.Platform;
//import javafx.fxml.FXML;
//import javafx.fxml.Initializable;
//import javafx.scene.control.*;
//import javafx.scene.layout.*;
//import javafx.scene.paint.Color;
//import javafx.scene.shape.*;
//import javafx.scene.text.Text;
//
//import java.net.URL;
//import java.util.*;
//
//public class HeapController implements Initializable {
//
//
//    @FXML
//    private Pane treePane;
//    @FXML
//    private Pane arrayPane;
//    @FXML
//    private Label statusLabel;
//    @FXML
//    private Label logLabel;
//    @FXML
//    private Label sizeLabel;
//
//
//    @FXML
//    private TextField insertField;
//    @FXML
//    private TextField buildArrayField;
//
//    @FXML
//    private Button btnInsert;
//    @FXML
//    private Button btnExtractMax;
//    @FXML
//    private Button btnPeekMax;
//    @FXML
//    private Button btnHeapSort;
//    @FXML
//    private Button btnBuildHeap;
//    @FXML
//    private Button btnClear;
//
//
//    private Heap heap;
//    private boolean isBusy = false;
//    private Thread workerThread;
//
//    private static final String C_DEFAULT = "#3498DB";
//    private static final String C_COMPARE = "#F1C40F";
//    private static final String C_SWAP = "#E74C3C";
//    private static final String C_HIGHLIGHT = "#9B59B6";
//    private static final String C_SORTED = "#2ECC71";
//    private static final String C_ROOT = "#E67E22";
//
//    private static final double R = 22;
//
//    @Override
//    public void initialize(URL location, ResourceBundle resources) {
//        heap = new Heap(this);
//        updateUI(Collections.emptyList(), -1, "Ready — insert values to begin");
//    }
//
//
//    @FXML
//    private void handleInsert() {
//        String txt = insertField.getText().trim();
//        if (txt.isEmpty()) {
//            flash("Enter a value to insert");
//            return;
//        }
//        try {
//            int val = Integer.parseInt(txt);
//            if (val < 1 || val > 999) {
//                flash("Value must be 1–999");
//                return;
//            }
//            insertField.clear();
//            runAsync(() -> heap.insert(val));
//        } catch (NumberFormatException e) {
//            flash("Invalid number");
//        }
//    }
//
//    @FXML
//    private void handleExtractMax() {
//        if (heap.isEmpty()) {
//            flash("Heap is empty!");
//            return;
//        }
//        runAsync(() -> {
//            int max = heap.extractMax();
//            Platform.runLater(() -> flash("Extracted max: " + max));
//        });
//    }
//
//
//    @FXML
//    private void handlePeekMax() {
//        if (heap.isEmpty()) {
//            flash("Heap is empty!");
//            return;
//        }
//        int max = heap.peekMax();
//        Platform.runLater(() -> {
//            onHighlight(0, heap.getHeap(), "Peek Max  " + max);
//            flash("Max (root) = " + max);
//        });
//    }
//
//
//    @FXML
//    private void handleHeapSort() {
//        if (heap.isEmpty()) {
//            flash("Heap is empty!");
//            return;
//        }
//        runAsync(() -> heap.heapSort());
//    }
//
//    @FXML
//    private void handleBuildHeap() {
//        String txt = buildArrayField.getText().trim();
//        if (txt.isEmpty()) {
//            flash("Enter comma-separated values");
//            return;
//        }
//        try {
//            String[] parts = txt.split(",");
//            int[] arr = new int[parts.length];
//            for (int i = 0; i < parts.length; i++) {
//                arr[i] = Integer.parseInt(parts[i].trim());
//                if (arr[i] < 1 || arr[i] > 999) {
//                    flash("Values must be 1–999");
//                    return;
//                }
//            }
//            buildArrayField.clear();
//            runAsync(() -> heap.buildHeap(arr));
//        } catch (NumberFormatException e) {
//            flash("Invalid input");
//        }
//    }
//
//    @FXML
//    private void handleClear() {
//        if (workerThread != null && workerThread.isAlive()) workerThread.interrupt();
//        isBusy = false;
//        heap = new Heap(this);
//        updateUI(Collections.emptyList(), -1, "Heap cleared");
//        setControlsDisabled(false);
//    }
//
//
//    public void onHeapChanged(List<Integer> h, int highlight, String msg) throws InterruptedException {
//        List<Integer> copy = new ArrayList<>(h);
//        Platform.runLater(() -> updateUI(copy, highlight, msg));
//        Thread.sleep(delay());
//    }
//
//    public void onCompare(int i, int j, List<Integer> h, String msg) throws InterruptedException {
//        List<Integer> copy = new ArrayList<>(h);
//        Platform.runLater(() -> {
//            drawTree(copy, i, j, C_COMPARE, C_COMPARE);
//            drawArrayBar(copy, i, j, C_COMPARE);
//            logLabel.setText(" " + msg);
//        });
//        Thread.sleep(delay());
//    }
//
//    public void onSwap(int i, int j, List<Integer> h, String msg) throws InterruptedException {
//        List<Integer> copy = new ArrayList<>(h);
//        Platform.runLater(() -> {
//            drawTree(copy, i, j, C_SWAP, C_SWAP);
//            drawArrayBar(copy, i, j, C_SWAP);
//            logLabel.setText("🔄 " + msg);
//        });
//        Thread.sleep(delay());
//    }
//
//    public void onHighlight(int idx, List<Integer> h, String msg) {
//        List<Integer> copy = new ArrayList<>(h);
//        Platform.runLater(() -> {
//            drawTree(copy, idx, -1, C_HIGHLIGHT, null);
//            drawArrayBar(copy, idx, -1, C_HIGHLIGHT);
//            logLabel.setText(" " + msg);
//        });
//    }
//
//    public void onSortStep(List<Integer> remaining, List<Integer> sorted, String msg) throws InterruptedException {
//        List<Integer> remCopy = new ArrayList<>(remaining);
//        List<Integer> sortedCopy = new ArrayList<>(sorted);
//        Platform.runLater(() -> {
//            drawSortStep(remCopy, sortedCopy);
//            logLabel.setText("📊 " + msg);
//        });
//        Thread.sleep(delay());
//    }
//
//    public void onSortComplete(List<Integer> sorted, String msg) throws InterruptedException {
//        List<Integer> copy = new ArrayList<>(sorted);
//        Platform.runLater(() -> {
//            drawSortComplete(copy);
//            statusLabel.setText(msg);
//            logLabel.setText(" " + msg);
//        });
//        Thread.sleep(delay());
//    }
//
//
//    private void updateUI(List<Integer> h, int highlight, String msg) {
//        drawTree(h, highlight, -1, C_HIGHLIGHT, null);
//        drawArrayBar(h, highlight, -1, C_HIGHLIGHT);
//        statusLabel.setText(msg);
//        sizeLabel.setText("Size: " + h.size());
//
//        setControlsDisabled(isBusy);
//    }
//
//    private double[] nodePos(int i, int n, double w, double h) {
//        int depth = (int) (Math.log(i + 1) / Math.log(2));
//        int posInRow = i - ((1 << depth) - 1);
//        int nodesInRow = 1 << depth;
//
//        double y = 50 + depth * 70;
//        double x = w / (nodesInRow + 1) * (posInRow + 1);
//        return new double[]{x, y};
//    }
//
//    private void drawTree(List<Integer> h, int hi1, int hi2, String col1, String col2) {
//        treePane.getChildren().clear();
//        if (h.isEmpty()) return;
//
//        double w = Math.max(treePane.getWidth(), 500);
//        double ht = Math.max(treePane.getHeight(), 400);
//
//        for (int i = 1; i < h.size(); i++) {
//            int parent = (i - 1) / 2;
//            double[] pc = nodePos(parent, h.size(), w, ht);
//            double[] cc = nodePos(i, h.size(), w, ht);
//            Line line = new Line(pc[0], pc[1], cc[0], cc[1]);
//            line.setStroke(Color.web("#7F8C8D"));
//            line.setStrokeWidth(1.5);
//            treePane.getChildren().add(line);
//        }
//
//        for (int i = 0; i < h.size(); i++) {
//            double[] pos = nodePos(i, h.size(), w, ht);
//            String colour = C_DEFAULT;
//            if (i == 0) colour = C_ROOT;
//            if (i == hi1) colour = col1;
//            else if (i == hi2 && col2 != null) colour = col2;
//
//            Circle circle = new Circle(pos[0], pos[1], R);
//            circle.setFill(Color.web(colour));
//            circle.setStroke(Color.web("#2C3E50"));
//            circle.setStrokeWidth(2);
//
//
//            Text idxText = new Text(pos[0] - 4, pos[1] - R - 4, String.valueOf(i));
//            idxText.setFill(Color.web("#95A5A6"));
//            idxText.setStyle("-fx-font-size: 9px;");
//
//            Text valText = new Text(String.valueOf(h.get(i)));
//            valText.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
//            valText.setFill(Color.WHITE);
//            valText.setX(pos[0] - valText.getLayoutBounds().getWidth() / 2 - 1);
//            valText.setY(pos[1] + 4);
//
//            treePane.getChildren().addAll(circle, idxText, valText);
//        }
//    }
//
//
//
//    private void drawArrayBar(List<Integer> h, int hi1, int hi2, String hlCol) {
//        arrayPane.getChildren().clear();
//        if (h.isEmpty()) return;
//
//        double totalW = Math.max(arrayPane.getWidth(), 500);
//        double cellW = Math.min(50, (totalW - 20) / h.size());
//        double cellH = 36;
//        double startX = 10;
//        double startY = 10;
//
//        for (int i = 0; i < h.size(); i++) {
//            String bg = C_DEFAULT;
//            if (i == 0) bg = C_ROOT;
//            if (i == hi1) bg = hlCol;
//            else if (i == hi2) bg = hlCol;
//
//            Rectangle rect = new Rectangle(startX + i * cellW, startY, cellW - 2, cellH);
//            rect.setFill(Color.web(bg));
//            rect.setStroke(Color.web("#2C3E50"));
//            rect.setArcWidth(4);
//            rect.setArcHeight(4);
//
//            Text val = new Text(String.valueOf(h.get(i)));
//            val.setFill(Color.WHITE);
//            val.setStyle("-fx-font-size: 11px; -fx-font-weight: bold;");
//            val.setX(startX + i * cellW + cellW / 2 - val.getLayoutBounds().getWidth() / 2);
//            val.setY(startY + cellH / 2 + 4);
//
//            Text idx = new Text(String.valueOf(i));
//            idx.setFill(Color.web("#95A5A6"));
//            idx.setStyle("-fx-font-size: 9px;");
//            idx.setX(startX + i * cellW + cellW / 2 - 3);
//            idx.setY(startY + cellH + 14);
//
//            arrayPane.getChildren().addAll(rect, val, idx);
//        }
//    }
//
//    private void drawSortStep(List<Integer> remaining, List<Integer> sorted) {
//        treePane.getChildren().clear();
//        arrayPane.getChildren().clear();
//
//        double w = Math.max(treePane.getWidth(), 500);
//        double ht = Math.max(treePane.getHeight(), 400);
//
//
//        for (int i = 1; i < remaining.size(); i++) {
//            int parent = (i - 1) / 2;
//            double[] pc = nodePos(parent, remaining.size(), w, ht);
//            double[] cc = nodePos(i, remaining.size(), w, ht);
//            Line line = new Line(pc[0], pc[1], cc[0], cc[1]);
//            line.setStroke(Color.web("#7F8C8D"));
//            line.setStrokeWidth(1.5);
//            treePane.getChildren().add(line);
//        }
//        for (int i = 0; i < remaining.size(); i++) {
//            double[] pos = nodePos(i, remaining.size(), w, ht);
//            Circle c = new Circle(pos[0], pos[1], R);
//            c.setFill(Color.web(i == 0 ? C_ROOT : C_DEFAULT));
//            c.setStroke(Color.web("#2C3E50"));
//            c.setStrokeWidth(2);
//            Text t = new Text(String.valueOf(remaining.get(i)));
//            t.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
//            t.setFill(Color.WHITE);
//            t.setX(pos[0] - t.getLayoutBounds().getWidth() / 2 - 1);
//            t.setY(pos[1] + 4);
//            treePane.getChildren().addAll(c, t);
//        }
//
//        double cellW = Math.min(50, (Math.max(arrayPane.getWidth(), 500) - 20) / Math.max(sorted.size(), 1));
//        for (int i = 0; i < sorted.size(); i++) {
//            Rectangle r = new Rectangle(10 + i * cellW, 10, cellW - 2, 36);
//            r.setFill(Color.web(C_SORTED));
//            r.setStroke(Color.web("#2C3E50"));
//            r.setArcWidth(4);
//            r.setArcHeight(4);
//            Text t = new Text(String.valueOf(sorted.get(i)));
//            t.setFill(Color.WHITE);
//            t.setStyle("-fx-font-size: 11px; -fx-font-weight: bold;");
//            t.setX(10 + i * cellW + cellW / 2 - t.getLayoutBounds().getWidth() / 2);
//            t.setY(32);
//            arrayPane.getChildren().addAll(r, t);
//        }
//    }
//
//    private void drawSortComplete(List<Integer> sorted) {
//        treePane.getChildren().clear();
//        arrayPane.getChildren().clear();
//
//        double cellW = Math.min(50, (Math.max(arrayPane.getWidth(), 500) - 20) / sorted.size());
//        for (int i = 0; i < sorted.size(); i++) {
//            Rectangle r = new Rectangle(10 + i * cellW, 10, cellW - 2, 36);
//            r.setFill(Color.web(C_SORTED));
//            r.setStroke(Color.web("#2C3E50"));
//            r.setArcWidth(4);
//            r.setArcHeight(4);
//            Text t = new Text(String.valueOf(sorted.get(i)));
//            t.setFill(Color.WHITE);
//            t.setStyle("-fx-font-size: 11px; -fx-font-weight: bold;");
//            t.setX(10 + i * cellW + cellW / 2 - t.getLayoutBounds().getWidth() / 2);
//            t.setY(32);
//            arrayPane.getChildren().addAll(r, t);
//        }
//
//
//        Text label = new Text(10, 65, "Sorted (ascending) ↑");
//        label.setFill(Color.web(C_SORTED));
//        label.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
//        arrayPane.getChildren().add(label);
//    }
//
//
//    @FunctionalInterface
//    interface HeapOp {
//        void run() throws Exception;
//    }
//
//    private void runAsync(HeapOp op) {
//        if (isBusy) {
//            flash("Operation in progress...");
//            return;
//        }
//        isBusy = true;
//        setControlsDisabled(true);
//
//        workerThread = new Thread(() -> {
//            try {
//                op.run();
//            } catch (InterruptedException e) {
//                Platform.runLater(() -> statusLabel.setText("Operation interrupted"));
//            } catch (Exception e) {
//                Platform.runLater(() -> flash("Error: " + e.getMessage()));
//            } finally {
//                isBusy = false;
//                Platform.runLater(() -> {
//                    setControlsDisabled(false);
//                    updateUI(heap.getHeap(), -1, statusLabel.getText());
//                });
//            }
//        });
//        workerThread.setDaemon(true);
//        workerThread.start();
//    }
//
//    private void setControlsDisabled(boolean disabled) {
//        btnInsert.setDisable(disabled);
//        btnExtractMax.setDisable(disabled);
//        btnPeekMax.setDisable(disabled);
//        btnHeapSort.setDisable(disabled);
//        btnBuildHeap.setDisable(disabled);
//    }
//
//    private void flash(String msg) {
//        Platform.runLater(() -> statusLabel.setText(msg));
//    }
//
//    private long delay() {
//
//        return (long) (1000);
//    }
//}