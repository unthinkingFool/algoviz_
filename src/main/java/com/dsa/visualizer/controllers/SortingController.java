
package com.dsa.visualizer.controllers;

import com.dsa.visualizer.algorithms.sorting.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.net.URL;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SortingController implements Initializable {

    @FXML private Slider arraySizeSlider;
    @FXML private Slider speedSlider;
    @FXML private TextField customArrayField;
    @FXML private Button btnPlayAll;
    @FXML private Button btnStep;
    @FXML private Button btnPause;
    @FXML private Button btnBubble;
    @FXML private Button btnInsertion;
    @FXML private Button btnSelection;
    @FXML private Button btnMerge;
    @FXML private  Button btnQuick;
    @FXML private Pane visualizationPane;
    @FXML private Label statusLabel;
    @FXML private Label algorithmLabel;
    @FXML private Label comparisonsLabel;
    @FXML private Label swapsLabel;

    private int[] array;
    private int[] originalArray;
    private List<Rectangle> bars;
    private boolean isPaused = false;
    private boolean isSorting = false;
    private boolean stepMode = false;
    private int comparisons = 0;
    private int swaps = 0;
    private Thread sortingThread;
    private String selectedAlgorithm = null;

    private final Lock stepLock = new ReentrantLock();
    private volatile boolean waitingForStep = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        bars = new ArrayList<>();
        generateArray();

        arraySizeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (!isSorting) {
                generateArray();
            }
        });

        btnStep.setDisable(true);
    }

    @FXML
    private void selectBubbleSort() {
        selectAlgorithm("Bubble Sort", btnBubble);
    }

    @FXML
    private void selectInsertionSort() {
        selectAlgorithm("Insertion Sort", btnInsertion);
    }

    @FXML
    private void selectSelectionSort() {
        selectAlgorithm("Selection Sort", btnSelection);
    }

    @FXML
    private void selectMergeSort() {
        selectAlgorithm("Merge Sort", btnMerge);
    }
    @FXML
    private void selectQuickSort() {
        selectAlgorithm("Quick Sort", btnQuick);
    }

    private void selectAlgorithm(String algorithm, Button selectedButton) {
        if (isSorting) return;

        selectedAlgorithm = algorithm;
        algorithmLabel.setText("Algorithm: " + algorithm);

        String defaultStyle = "-fx-background-color: #3498DB; -fx-text-fill: white; -fx-font-size: 13px; -fx-cursor: hand;";
        String selectedStyle = "-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-font-size: 13px; -fx-cursor: hand; -fx-font-weight: bold;";

        btnBubble.setStyle(defaultStyle);
        btnInsertion.setStyle(defaultStyle);
        btnSelection.setStyle(defaultStyle);
        btnMerge.setStyle(defaultStyle);
        btnQuick.setStyle(defaultStyle);

        selectedButton.setStyle(selectedStyle);

        resetSort();
    }

    @FXML
    private void loadCustomArray() {
        if (isSorting) return;

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
                if (array[i] < 10 || array[i] > 500) {
                    statusLabel.setText("Values must be between 10 and 500");
                    generateArray();
                    return;
                }
            }

            originalArray = Arrays.copyOf(array, array.length);
            drawBars();
            comparisons = 0;
            swaps = 0;
            updateStats();
            statusLabel.setText("Custom array loaded");

        } catch (NumberFormatException e) {
            statusLabel.setText("Invalid input format");
        }
    }

    @FXML
    private void generateArray() {
        if (isSorting) return;

        int size = (int) arraySizeSlider.getValue();
        array = new int[size];
        Random random = new Random();

        for (int i = 0; i < size; i++) {
            array[i] = random.nextInt(400) + 50;
        }

        originalArray = Arrays.copyOf(array, array.length);
        drawBars();
        comparisons = 0;
        swaps = 0;
        updateStats();
        statusLabel.setText("Random array generated");
    }

    private void drawBars() {
        visualizationPane.getChildren().clear();
        bars.clear();

        double width = visualizationPane.getWidth();
        double height = visualizationPane.getHeight();
        double barWidth = (width - 40) / array.length;
        double spacing = 2;

        for (int i = 0; i < array.length; i++) {
            double barHeight = (array[i] / 500.0) * (height - 40);
            Rectangle bar = new Rectangle(barWidth - spacing, barHeight);
            bar.setX(20 + i * barWidth);
            bar.setY(height - barHeight - 20);
            bar.setFill(Color.rgb(52, 152, 219));
            bar.setStroke(Color.rgb(41, 128, 185));
            bars.add(bar);
            visualizationPane.getChildren().add(bar);
        }
    }

    @FXML
    private void playAll() {
        if (selectedAlgorithm == null) {
            statusLabel.setText("Please select an algorithm first");
            return;
        }

        if (isSorting && isPaused) {
            isPaused = false;
            stepMode = false;
            btnPause.setText("⏸ Pause");
            statusLabel.setText("Sorting...");

            synchronized (stepLock) {
                waitingForStep = false;
                stepLock.notifyAll();
            }
            return;
        }

        if (isSorting) return;

        stepMode = false;
        startSorting();
    }

    @FXML
    private void stepForward() {
        if (selectedAlgorithm == null) {
            statusLabel.setText("Please select an algorithm first");
            return;
        }

        if (!isSorting) {
            stepMode = true;
            startSorting();
        } else if (waitingForStep) {
            synchronized (stepLock) {
                waitingForStep = false;
                stepLock.notifyAll();
            }
        }
    }

    private void startSorting() {
        isSorting = true;
        isPaused = false;
        comparisons = 0;
        swaps = 0;
        statusLabel.setText(stepMode ? "Step Mode: Click Step Forward" : "Sorting...");

        btnStep.setDisable(false);
        btnPlayAll.setDisable(false);

        sortingThread = new Thread(() -> {
            try {
                int[] arrCopy = Arrays.copyOf(array, array.length);

                switch (selectedAlgorithm) {
                    case "Bubble Sort":
                        new BubbleSort().sort(arrCopy, this);
                        break;
                    case "Insertion Sort":
                        new InsertionSort().sort(arrCopy, this);
                        break;
                    case "Selection Sort":
                        new SelectionSort().sort(arrCopy, this);
                        break;
                    case "Merge Sort":
                        new MergeSort().sort(arrCopy, this);
                        break;
                    case "Quick Sort":
                        new QuickSort().sort(arrCopy,0,arrCopy.length-1,this);
                }

                Platform.runLater(() -> {
                    statusLabel.setText("Sorting Complete!");
                    isSorting = false;
                    stepMode = false;
                    btnStep.setDisable(true);
                    highlightSorted();
                });

            } catch (InterruptedException e) {
                Platform.runLater(() -> {
                    statusLabel.setText("Sorting Stopped");
                    isSorting = false;
                    stepMode = false;
                    btnStep.setDisable(true);
                });
            }
        });

        sortingThread.setDaemon(true);
        sortingThread.start();
    }

    @FXML
    private void pauseSort() {
        if (isSorting) {
            isPaused = !isPaused;
            btnPause.setText(isPaused ? "▶ Resume" : "⏸ Pause");
            statusLabel.setText(isPaused ? "Paused" : "Sorting...");

            if (!isPaused) {
                synchronized (stepLock) {
                    stepLock.notifyAll();
                }
            }
        }
    }

    @FXML
    private void resetSort() {
        if (sortingThread != null && sortingThread.isAlive()) {
            sortingThread.interrupt();
        }
        isSorting = false;
        isPaused = false;
        stepMode = false;
        waitingForStep = false;
        btnPause.setText("⏸ Pause");
        btnStep.setDisable(true);

        if (originalArray != null) {
            array = Arrays.copyOf(originalArray, originalArray.length);
            drawBars();
        } else {
            generateArray();
        }

        comparisons = 0;
        swaps = 0;
        updateStats();
        statusLabel.setText("Reset - Ready to sort");
    }

    public void visualizeSwap(int i, int j, int[] arr) throws InterruptedException {
        waitForStep();

        Platform.runLater(() -> {
            bars.get(i).setFill(Color.rgb(231, 76, 60));
            bars.get(j).setFill(Color.rgb(231, 76, 60));
        });

        Thread.sleep(getDelay());

        Platform.runLater(() -> {
            double tempHeight = bars.get(i).getHeight();
            double tempY = bars.get(i).getY();

            bars.get(i).setHeight(bars.get(j).getHeight());
            bars.get(i).setY(bars.get(j).getY());
            bars.get(j).setHeight(tempHeight);
            bars.get(j).setY(tempY);

            bars.get(i).setFill(Color.rgb(52, 152, 219));
            bars.get(j).setFill(Color.rgb(52, 152, 219));

            swaps++;
            updateStats();
        });
    }

    public void visualizeComparison(int i, int j) throws InterruptedException {
        waitForStep();

        Platform.runLater(() -> {
            bars.get(i).setFill(Color.rgb(241, 196, 15));
            bars.get(j).setFill(Color.rgb(241, 196, 15));
            comparisons++;
            updateStats();
        });

        Thread.sleep(getDelay() / 2);

        Platform.runLater(() -> {
            bars.get(i).setFill(Color.rgb(52, 152, 219));
            bars.get(j).setFill(Color.rgb(52, 152, 219));
        });
    }

    public void visualizeSet(int index, int value, int[] arr) throws InterruptedException {
        waitForStep();

        Platform.runLater(() -> {
            double height = visualizationPane.getHeight();
            double barHeight = (value / 500.0) * (height - 40);
            bars.get(index).setHeight(barHeight);
            bars.get(index).setY(height - barHeight - 20);
            bars.get(index).setFill(Color.rgb(46, 204, 113));
        });

        Thread.sleep(getDelay());

        Platform.runLater(() -> {
            bars.get(index).setFill(Color.rgb(52, 152, 219));
        });
    }

    private void waitForStep() throws InterruptedException {
        if (stepMode) {
            synchronized (stepLock) {
                waitingForStep = true;
                Platform.runLater(() -> statusLabel.setText("Waiting - Click Step Forward"));
                stepLock.wait();
            }
        }

        while (isPaused && !stepMode) {
            Thread.sleep(100);
        }
    }

    private void highlightSorted() {
        for (Rectangle bar : bars) {
            bar.setFill(Color.rgb(46, 204, 113));
        }
    }

    private long getDelay() {
        if (stepMode) return 200;
        return (long) (100 - speedSlider.getValue() + 10);
    }

    private void updateStats() {
        comparisonsLabel.setText("Comparisons: " + comparisons);
        swapsLabel.setText("Swaps: " + swaps);
    }
}

