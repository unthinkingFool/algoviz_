package com.dsa.visualizer.controllers;

import com.dsa.visualizer.algorithms.searching.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import java.net.URL;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SearchingController implements Initializable {

    @FXML private TextField targetField;
    @FXML private TextField customArrayField;
    @FXML private Pane visualizationPane;
    @FXML private Label statusLabel;
    @FXML private Label algorithmLabel;
    @FXML private Label comparisonsLabel;
    @FXML private Button btnLinear;
    @FXML private Button btnBinary;
    @FXML private Button btnJump;
    @FXML private Button btnPlayAll;
    @FXML private Button btnStep;
    @FXML private Button btnReset;

    private int[] array;
    private List<Rectangle> bars;
    private List<Text> labels;
    private int comparisons = 0;
    private boolean isSearching = false;
    private boolean stepMode = false;
    private String selectedAlgorithm = null;

    private final Lock stepLock = new ReentrantLock();
    private volatile boolean waitingForStep = false;

    // ✅ FIX: Keep a reference to the running search thread so we can interrupt it
    private Thread searchThread = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        bars = new ArrayList<>();
        labels = new ArrayList<>();
        btnStep.setDisable(true);
    }

    @FXML
    private void selectLinearSearch() {
        selectAlgorithm("Linear Search", btnLinear, false);
    }

    @FXML
    private void selectBinarySearch() {
        selectAlgorithm("Binary Search", btnBinary, true);
    }

    private void selectAlgorithm(String algorithm, Button selectedButton, boolean needsSorted) {
        if (isSearching) return;

        selectedAlgorithm = algorithm;
        algorithmLabel.setText("Algorithm: " + algorithm);

        String defaultStyle = "-fx-background-color: #3498DB; -fx-text-fill: white; -fx-font-size: 13px; -fx-cursor: hand;";
        String selectedStyle = "-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-font-size: 13px; -fx-cursor: hand; -fx-font-weight: bold;";

        btnLinear.setStyle(defaultStyle);
        btnBinary.setStyle(defaultStyle);

        selectedButton.setStyle(selectedStyle);

        if (needsSorted && array != null) {
            Arrays.sort(array);
            drawArray();
            statusLabel.setText("Array sorted for " + algorithm);
        }
    }

    @FXML
    private void loadCustomArray() {
        if (isSearching) return;

        String input = customArrayField.getText().trim();
        if (input.isEmpty()) {
            statusLabel.setText("Please enter array values");
            return;
        }

        try {
            String[] parts = input.split(",");
            array = new int[parts.length];

            for (int i = 0; i < parts.length; i++) {
                array[i] = Integer.parseInt(parts[i].trim());
            }

            if (selectedAlgorithm != null && !selectedAlgorithm.equals("Linear Search")) {
                Arrays.sort(array);
            }

            drawArray();
            comparisons = 0;
            updateStats();
            statusLabel.setText("Custom array loaded");

        } catch (NumberFormatException e) {
            statusLabel.setText("Invalid input format");
        }
    }

    @FXML
    private void generateArray() {
        if (isSearching) return;

        array = new int[20];

        for (int i = 0; i < 20; i++) {
            array[i] = (int) (Math.random() * 501); // 0 to 500
        }

        if (selectedAlgorithm == null || "Linear Search".equals(selectedAlgorithm)) {
            shuffleArray(array);
        }

        drawArray();
        comparisons = 0;
        updateStats();
        statusLabel.setText("Random array generated");
    }

    private void shuffleArray(int[] arr) {
        Random random = new Random();
        for (int i = arr.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int temp = arr[i];
            arr[i] = arr[j];
            arr[j] = temp;
        }
    }

    private void drawArray() {
        visualizationPane.getChildren().clear();
        bars.clear();
        labels.clear();

        visualizationPane.layout();

        double width = visualizationPane.getWidth() > 0 ? visualizationPane.getWidth() : 950;
        double height = visualizationPane.getHeight() > 0 ? visualizationPane.getHeight() : 450;
        double barWidth = (width - 40) / array.length;

        for (int i = 0; i < array.length; i++) {
            Rectangle bar = new Rectangle(barWidth - 5, 60);
            bar.setX(20 + i * barWidth);
            bar.setY(height / 2 - 30);
            bar.setFill(Color.rgb(52, 152, 219));
            bar.setStroke(Color.BLACK);

            Text label = new Text(String.valueOf(array[i]));
            label.setX(20 + i * barWidth + (barWidth - 5) / 2 - 10);
            label.setY(height / 2 + 5);
            label.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

            bars.add(bar);
            labels.add(label);
            visualizationPane.getChildren().addAll(bar, label);
        }
    }

    @FXML
    private void playAll() {
        if (selectedAlgorithm == null) {
            statusLabel.setText("Please select an algorithm first");
            return;
        }

        if (isSearching) return;

        stepMode = false;
        startSearch();
    }

    @FXML
    private void stepForward() {
        if (selectedAlgorithm == null) {
            statusLabel.setText("Please select an algorithm first");
            return;
        }

        if (!isSearching) {
            stepMode = true;
            startSearch();
        } else if (waitingForStep) {
            synchronized (stepLock) {
                waitingForStep = false;
                stepLock.notifyAll();
            }
        }
    }

    private void startSearch() {
        String targetStr = targetField.getText();
        if (targetStr.isEmpty()) {
            statusLabel.setText("Please enter a target value");
            return;
        }

        try {
            int target = Integer.parseInt(targetStr);
            isSearching = true;
            comparisons = 0;
            statusLabel.setText(stepMode ? "Step Mode: Click Step Forward" : "Searching...");

            btnStep.setDisable(false);
            btnPlayAll.setDisable(false);

            // ✅ FIX: Save reference to the thread so reset() can interrupt it
            searchThread = new Thread(() -> {
                try {
                    int result = -1;

                    switch (selectedAlgorithm) {
                        case "Linear Search":
                            result = new LinearSearch().search(array, target, this);
                            break;
                        case "Binary Search":
                            result = new BinarySearch().search(array, target, this);
                            break;
                    }

                    final int finalResult = result;
                    Platform.runLater(() -> {
                        if (finalResult != -1) {
                            statusLabel.setText("Found at index " + finalResult + "!");
                            bars.get(finalResult).setFill(Color.rgb(46, 204, 113));
                        } else {
                            statusLabel.setText("Not found");
                        }
                        isSearching = false;
                        stepMode = false;
                        btnStep.setDisable(true);
                    });

                } catch (InterruptedException e) {
                    // ✅ FIX: Thread was interrupted by reset() — clean up gracefully
                    Thread.currentThread().interrupt(); // restore interrupted status
                    Platform.runLater(() -> {
                        statusLabel.setText("Search interrupted");
                        isSearching = false;
                        stepMode = false;
                        btnStep.setDisable(true);
                    });
                }
            });

            searchThread.setDaemon(true);
            searchThread.start();

        } catch (NumberFormatException e) {
            statusLabel.setText("Invalid target value");
        }
    }

    @FXML
    private void reset() {
        // ✅ FIX 1: Update flags BEFORE waking/interrupting the thread
        isSearching = false;
        stepMode = false;

        // ✅ FIX 2: Wake up the thread if it's blocked in stepLock.wait()
        //           so it can proceed and then get interrupted cleanly
        synchronized (stepLock) {
            waitingForStep = false;
            stepLock.notifyAll();
        }

        // ✅ FIX 3: Interrupt the thread so Thread.sleep() throws InterruptedException
        //           and the thread actually stops running
        if (searchThread != null && searchThread.isAlive()) {
            searchThread.interrupt();
            searchThread = null;
        }

        // ✅ FIX 4: Restore UI to exactly the initial state (blank canvas)
        Platform.runLater(() -> {
            btnStep.setDisable(true);
            btnPlayAll.setDisable(false);
            comparisons = 0;
            updateStats();
            statusLabel.setText("");
            algorithmLabel.setText("Algorithm: None");

            // Clear the canvas completely — no array drawn, just like on first load
            visualizationPane.getChildren().clear();
            bars.clear();
            labels.clear();
            array = null;

            // Reset algorithm selection highlight
            String defaultStyle = "-fx-background-color: #3498DB; -fx-text-fill: white; -fx-font-size: 13px; -fx-cursor: hand;";
            btnLinear.setStyle(defaultStyle);
            btnBinary.setStyle(defaultStyle);
            selectedAlgorithm = null;
        });
    }

    public void visualizeCheck(int index) throws InterruptedException {
        // ✅ FIX: Check interrupted flag so reset mid-animation works immediately
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException("Search was reset");
        }

        waitForStep();

        Platform.runLater(() -> {
            bars.get(index).setFill(Color.rgb(241, 196, 15));
            comparisons++;
            updateStats();
        });

        Thread.sleep(stepMode ? 150 : 250);

        Platform.runLater(() -> {
            bars.get(index).setFill(Color.rgb(231, 76, 60));
        });

        Thread.sleep(stepMode ? 100 : 150);
    }

    public void visualizeRange(int start, int end) throws InterruptedException {
        // ✅ FIX: Same interrupt check here
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException("Search was reset");
        }

        waitForStep();

        Platform.runLater(() -> {
            for (int i = start; i <= end; i++) {
                bars.get(i).setFill(Color.rgb(155, 89, 182));
            }
        });

        Thread.sleep(stepMode ? 150 : 150);
    }

    private void waitForStep() throws InterruptedException {
        if (stepMode) {
            synchronized (stepLock) {
                waitingForStep = true;
                Platform.runLater(() -> statusLabel.setText("Waiting - Click Step Forward"));
                stepLock.wait();

                // ✅ FIX: If woken up by reset (not by stepForward), throw to exit the thread
                if (!isSearching) {
                    throw new InterruptedException("Search was reset during step wait");
                }
            }
        }
    }

    private void updateStats() {
        comparisonsLabel.setText("Comparisons: " + comparisons);
    }
}
//
//package com.dsa.visualizer.controllers;
//
//import com.dsa.visualizer.algorithms.searching.*;
//import javafx.application.Platform;
//import javafx.fxml.FXML;
//import javafx.fxml.Initializable;
//import javafx.scene.control.*;
//import javafx.scene.layout.Pane;
//import javafx.scene.paint.Color;
//import javafx.scene.shape.Rectangle;
//import javafx.scene.text.Text;
//import java.net.URL;
//import java.util.*;
//import java.util.concurrent.locks.Lock;
//import java.util.concurrent.locks.ReentrantLock;
//
//public class SearchingController implements Initializable {
//
//    @FXML private TextField targetField;
//    @FXML private TextField customArrayField;
//    @FXML private Pane visualizationPane;
//    @FXML private Label statusLabel;
//    @FXML private Label algorithmLabel;
//    @FXML private Label comparisonsLabel;
//    @FXML private Button btnLinear;
//    @FXML private Button btnBinary;
//    @FXML private Button btnJump;
//    @FXML private Button btnPlayAll;
//    @FXML private Button btnStep;
//    @FXML private Button btnReset;
//
//
//    private int[] array;
//    private List<Rectangle> bars;
//    private List<Text> labels;
//    private int comparisons = 0;
//    private boolean isSearching = false;
//    private boolean stepMode = false;
//    private String selectedAlgorithm = null;
//
//    private final Lock stepLock = new ReentrantLock();
//    private volatile boolean waitingForStep = false;
//
//    @Override
//    public void initialize(URL location, ResourceBundle resources) {
//        bars = new ArrayList<>();
//        labels = new ArrayList<>();
////        generateArray();
//        btnStep.setDisable(true);
//    }
//
//    @FXML
//    private void selectLinearSearch() {
//        selectAlgorithm("Linear Search", btnLinear, false);
//    }
//
//    @FXML
//    private void selectBinarySearch() {
//        selectAlgorithm("Binary Search", btnBinary, true);
//    }
//
//
//
//    private void selectAlgorithm(String algorithm, Button selectedButton, boolean needsSorted) {
//        if (isSearching) return;
//
//        selectedAlgorithm = algorithm;
//        algorithmLabel.setText("Algorithm: " + algorithm);
//
//        String defaultStyle = "-fx-background-color: #3498DB; -fx-text-fill: white; -fx-font-size: 13px; -fx-cursor: hand;";
//        String selectedStyle = "-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-font-size: 13px; -fx-cursor: hand; -fx-font-weight: bold;";
//
//        btnLinear.setStyle(defaultStyle);
//        btnBinary.setStyle(defaultStyle);
//
//
//        selectedButton.setStyle(selectedStyle);
//
//        if (needsSorted) {
//            Arrays.sort(array);
//            drawArray();
//            statusLabel.setText("Array sorted for " + algorithm);
//        }
//    }
//
//    @FXML
//    private void loadCustomArray() {
//        if (isSearching) return;
//
//        String input = customArrayField.getText().trim();
//        if (input.isEmpty()) {
//            statusLabel.setText("Please enter array values");
//            return;
//        }
//
//        try {
//            String[] parts = input.split(",");
//            array = new int[parts.length];
//
//            for (int i = 0; i < parts.length; i++) {
//                array[i] = Integer.parseInt(parts[i].trim());
//            }
//
//            if (selectedAlgorithm != null && !selectedAlgorithm.equals("Linear Search")) {
//                Arrays.sort(array);
//            }
//
//            drawArray();
//            comparisons = 0;
//            updateStats();
//            statusLabel.setText("Custom array loaded");
//
//        } catch (NumberFormatException e) {
//            statusLabel.setText("Invalid input format");
//        }
//    }
//
//    @FXML
//    private void generateArray() {
//        if (isSearching) return;
//
//        array = new int[20];
//
//        for (int i = 0; i < 20; i++) {
//            array[i] = (int)(Math.random() * 501); // 0 to 500
//        }
//
//        if (selectedAlgorithm == null || "Linear Search".equals(selectedAlgorithm)) {
//            shuffleArray(array);
//        }
//
//        drawArray();
//        comparisons = 0;
//        updateStats();
//        statusLabel.setText("Random array generated");
//    }
//
//    private void shuffleArray(int[] arr) {
//        Random random = new Random();
//        for (int i = arr.length - 1; i > 0; i--) {
//            int j = random.nextInt(i + 1);
//            int temp = arr[i];
//            arr[i] = arr[j];
//            arr[j] = temp;
//        }
//    }
//
//    private void drawArray() {
//        visualizationPane.getChildren().clear();
//        bars.clear();
//        labels.clear();
//
//        // Force layout update to get actual dimensions
//        visualizationPane.layout();
//
//        double width = visualizationPane.getWidth() > 0 ? visualizationPane.getWidth() : 950;
//        double height = visualizationPane.getHeight() > 0 ? visualizationPane.getHeight() : 450;
//        double barWidth = (width - 40) / array.length;
//
//        for (int i = 0; i < array.length; i++) {
//            Rectangle bar = new Rectangle(barWidth - 5, 60);
//            bar.setX(20 + i * barWidth);
//            bar.setY(height / 2 - 30);
//            bar.setFill(Color.rgb(52, 152, 219));
//            bar.setStroke(Color.BLACK);
//
//            Text label = new Text(String.valueOf(array[i]));
//            label.setX(20 + i * barWidth + (barWidth - 5) / 2 - 10);
//            label.setY(height / 2 + 5);
//            label.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
//
//            bars.add(bar);
//            labels.add(label);
//            visualizationPane.getChildren().addAll(bar, label);
//        }
//    }
//
//    @FXML
//    private void playAll() {
//        if (selectedAlgorithm == null) {
//            statusLabel.setText("Please select an algorithm first");
//            return;
//        }
//
//        if (isSearching) return;
//
//        stepMode = false;
//        startSearch();
//    }
//
//    @FXML
//    private void stepForward() {
//        if (selectedAlgorithm == null) {
//            statusLabel.setText("Please select an algorithm first");
//            return;
//        }
//
//        if (!isSearching) {
//            stepMode = true;
//            startSearch();
//        } else if (waitingForStep) {
//            synchronized (stepLock) {
//                waitingForStep = false;
//                stepLock.notifyAll();
//            }
//        }
//    }
//
//    private void startSearch() {
//        String targetStr = targetField.getText();
//        if (targetStr.isEmpty()) {
//            statusLabel.setText("Please enter a target value");
//            return;
//        }
//
//        try {
//            int target = Integer.parseInt(targetStr);
//            isSearching = true;
//            comparisons = 0;
//            statusLabel.setText(stepMode ? "Step Mode: Click Step Forward" : "Searching...");
//
//            btnStep.setDisable(false);
//            btnPlayAll.setDisable(false);
//
//            Thread searchThread = new Thread(() -> {
//                try {
//                    int result = -1;
//
//                    switch (selectedAlgorithm) {
//                        case "Linear Search":
//                            result = new LinearSearch().search(array, target, this);
//                            break;
//                        case "Binary Search":
//                            result = new BinarySearch().search(array, target, this);
//                            break;
//
//                    }
//
//                    final int finalResult = result;
//                    Platform.runLater(() -> {
//                        if (finalResult != -1) {
//                            statusLabel.setText("Found at index " + finalResult + "!");
//                            bars.get(finalResult).setFill(Color.rgb(46, 204, 113));
//                        } else {
//                            statusLabel.setText("Not found");
//                        }
//                        isSearching = false;
//                        stepMode = false;
//                        btnStep.setDisable(true);
//                    });
//
//                } catch (InterruptedException e) {
//                    Platform.runLater(() -> {
//                        statusLabel.setText("Search interrupted");
//                        isSearching = false;
//                        stepMode = false;
//                        btnStep.setDisable(true);
//                    });
//                }
//            });
//
//            searchThread.setDaemon(true);
//            searchThread.start();
//
//        } catch (NumberFormatException e) {
//            statusLabel.setText("Invalid target value");
//        }
//    }
//
//    @FXML
//    private void reset() {
//        isSearching = false;
//        stepMode = false;
//        waitingForStep = false;
//        btnStep.setDisable(true);
//       // generateArray();
//    }
//
//    public void visualizeCheck(int index) throws InterruptedException {
//        waitForStep();
//
//        Platform.runLater(() -> {
//            bars.get(index).setFill(Color.rgb(241, 196, 15));
//            comparisons++;
//            updateStats();
//        });
//
//        Thread.sleep(stepMode ? 150 : 250);
//
//        Platform.runLater(() -> {
//            bars.get(index).setFill(Color.rgb(231, 76, 60));
//        });
//
//        Thread.sleep(stepMode ? 100 : 150);
//    }
//
//    public void visualizeRange(int start, int end) throws InterruptedException {
//        waitForStep();
//
//        Platform.runLater(() -> {
//            for (int i = start; i <= end; i++) {
//                bars.get(i).setFill(Color.rgb(155, 89, 182));
//            }
//        });
//
//        Thread.sleep(stepMode ? 150 : 150);
//    }
//
//    private void waitForStep() throws InterruptedException {
//        if (stepMode) {
//            synchronized (stepLock) {
//                waitingForStep = true;
//                Platform.runLater(() -> statusLabel.setText("Waiting - Click Step Forward"));
//                stepLock.wait();
//            }
//        }
//    }
//
//    private void updateStats() {
//        comparisonsLabel.setText("Comparisons: " + comparisons);
//    }
//}