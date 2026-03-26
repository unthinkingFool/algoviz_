
package com.dsa.visualizer.controllers;

import com.dsa.visualizer.algorithms.pathfinding.*;
import com.dsa.visualizer.models.Cell;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.net.URL;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PathfinderController implements Initializable {

    @FXML private GridPane gridPane;
    @FXML private Label statusLabel;
    @FXML private Label algorithmLabel;
    @FXML private Button btnDijkstra;
    @FXML private Button btnAStar;
    @FXML private Button btnBFS;
    @FXML private Button btnPlayAll;
    @FXML private Button btnStep;
    @FXML private TextField startField;
    @FXML private TextField endField;

    private static final int ROWS = 25;
    private static final int COLS = 40;
    private static final int CELL_SIZE = 20;

    private Cell[][] grid;
    private Rectangle[][] rectangles;
    private Cell startCell;
    private Cell endCell;
    private boolean isRunning = false;
    private boolean stepMode = false;
    private String selectedAlgorithm = null;

    private final Lock stepLock = new ReentrantLock();
    private volatile boolean waitingForStep = false;

    private enum PlacementMode { START, END, WALL }
    private PlacementMode currentMode = PlacementMode.START;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        grid = new Cell[ROWS][COLS];
        rectangles = new Rectangle[ROWS][COLS];

        initializeGrid();
        btnStep.setDisable(true);
        statusLabel.setText("Enter coordinates or click 'Mark as Start' to begin");
    }

    @FXML
    private void selectDijkstra() {
        selectAlgorithm("Dijkstra", btnDijkstra);
    }

    @FXML
    private void selectBFS() {
        selectAlgorithm("BFS", btnBFS);
    }

    private void selectAlgorithm(String algorithm, Button selectedButton) {
        if (isRunning) return;

        selectedAlgorithm = algorithm;
        algorithmLabel.setText("Algorithm: " + algorithm);

        String defaultStyle = "-fx-background-color: #3498DB; -fx-text-fill: white; -fx-font-size: 13px; -fx-cursor: hand;";
        String selectedStyle = "-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-font-size: 13px; -fx-cursor: hand; -fx-font-weight: bold;";

        btnDijkstra.setStyle(defaultStyle);

        btnBFS.setStyle(defaultStyle);

        selectedButton.setStyle(selectedStyle);
    }

    private void initializeGrid() {
        gridPane.getChildren().clear();

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                grid[row][col] = new Cell(row, col);

                Rectangle rect = new Rectangle(CELL_SIZE, CELL_SIZE);
                rect.setFill(Color.WHITE);
                rect.setStroke(Color.LIGHTGRAY);

                final int r = row;
                final int c = col;

                rect.setOnMouseClicked(e -> handleCellClick(r, c));
                rect.setOnMouseEntered(e -> {
                    if (e.isPrimaryButtonDown() && currentMode == PlacementMode.WALL) {
                        handleCellClick(r, c);
                    }
                });

                rectangles[row][col] = rect;
                gridPane.add(rect, col, row);
            }
        }
    }

    private void handleCellClick(int row, int col) {
        if (isRunning) return;

        Cell cell = grid[row][col];

        if (currentMode == PlacementMode.START) {
            if (startCell != null) {
                startCell.isStart = false;
                updateCellColor(startCell.row, startCell.col);
            }

            cell.isStart = true;
            cell.isWall = false;
            cell.isEnd = false;
            if (endCell == cell) {
                endCell = null;
            }
            startCell = cell;
            rectangles[row][col].setFill(Color.GREEN);
            statusLabel.setText("Start node set at (" + row + "," + col + ") - Click 'Mark as End' next");

        } else if (currentMode == PlacementMode.END) {
            if (cell.isStart) {
                statusLabel.setText("Cannot set end on start node!");
                return;
            }

            if (endCell != null) {
                endCell.isEnd = false;
                updateCellColor(endCell.row, endCell.col);
            }

            cell.isEnd = true;
            cell.isWall = false;
            endCell = cell;
            rectangles[row][col].setFill(Color.RED);
            statusLabel.setText("End node set at (" + row + "," + col + ") - Ready to find path!");

        } else if (currentMode == PlacementMode.WALL) {
            if (cell.isStart || cell.isEnd) {
                statusLabel.setText("Cannot place wall on start/end node!");
                return;
            }

            cell.isWall = !cell.isWall;
            rectangles[row][col].setFill(cell.isWall ? Color.BLACK : Color.WHITE);
        }
    }

    @FXML
    private void markAsStartMode() {
        if (isRunning) return;
        currentMode = PlacementMode.START;
        statusLabel.setText("Click any grid cell to mark as START node");
    }

    @FXML
    private void markAsEndMode() {
        if (isRunning) return;
        currentMode = PlacementMode.END;
        statusLabel.setText("Click any grid cell to mark as END node");
    }

    @FXML
    private void addWallsMode() {
        if (isRunning) return;
        currentMode = PlacementMode.WALL;
        statusLabel.setText("Click or drag to add/remove walls");
    }

    @FXML
    private void setStartEnd() {
        if (isRunning) return;

        try {
            String startInput = startField.getText().trim();
            if (startInput.isEmpty()) {
                statusLabel.setText("Please enter start coordinates");
                return;
            }

            String[] startCoords = startInput.split(",");
            if (startCoords.length != 2) {
                statusLabel.setText("Invalid format. Use: row,col");
                return;
            }

            int startRow = Integer.parseInt(startCoords[0].trim());
            int startCol = Integer.parseInt(startCoords[1].trim());

            if (startRow < 0 || startRow >= ROWS || startCol < 0 || startCol >= COLS) {
                statusLabel.setText("Start out of bounds (0-" + (ROWS-1) + ", 0-" + (COLS-1) + ")");
                return;
            }

            String endInput = endField.getText().trim();
            if (endInput.isEmpty()) {
                statusLabel.setText("Please enter end coordinates");
                return;
            }

            String[] endCoords = endInput.split(",");
            if (endCoords.length != 2) {
                statusLabel.setText("Invalid format. Use: row,col");
                return;
            }

            int endRow = Integer.parseInt(endCoords[0].trim());
            int endCol = Integer.parseInt(endCoords[1].trim());

            if (endRow < 0 || endRow >= ROWS || endCol < 0 || endCol >= COLS) {
                statusLabel.setText("End out of bounds (0-" + (ROWS-1) + ", 0-" + (COLS-1) + ")");
                return;
            }

            if (startRow == endRow && startCol == endCol) {
                statusLabel.setText("Start and end cannot be the same!");
                return;
            }

            if (startCell != null) {
                startCell.isStart = false;
                updateCellColor(startCell.row, startCell.col);
            }

            if (endCell != null) {
                endCell.isEnd = false;
                updateCellColor(endCell.row, endCell.col);
            }

            Cell newStart = grid[startRow][startCol];
            newStart.isStart = true;
            newStart.isWall = false;
            startCell = newStart;
            rectangles[startRow][startCol].setFill(Color.GREEN);

            Cell newEnd = grid[endRow][endCol];
            newEnd.isEnd = true;
            newEnd.isWall = false;
            endCell = newEnd;
            rectangles[endRow][endCol].setFill(Color.RED);

            currentMode = PlacementMode.WALL;
            statusLabel.setText("Start/End set! Generate walls or select algorithm");

        } catch (NumberFormatException e) {
            statusLabel.setText("Invalid coordinates. Use numbers only");
        }
    }

    @FXML
    private void generateRandomWalls() {
        if (isRunning) return;

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                if (!grid[row][col].isStart && !grid[row][col].isEnd) {
                    grid[row][col].isWall = false;
                    updateCellColor(row, col);
                }
            }
        }

        Random random = new Random();
        int wallCount = 0;
        int totalCells = ROWS * COLS;
        int targetWalls = (int) (totalCells * 0.25);

        while (wallCount < targetWalls) {
            int row = random.nextInt(ROWS);
            int col = random.nextInt(COLS);

            Cell cell = grid[row][col];

            if (!cell.isStart && !cell.isEnd && !cell.isWall) {
                cell.isWall = true;
                rectangles[row][col].setFill(Color.BLACK);
                wallCount++;
            }
        }

        statusLabel.setText("Random walls generated (" + wallCount + " walls). Select algorithm to start!");
    }

    @FXML
    private void clearGrid() {
        if (isRunning) return;

        startCell = null;
        endCell = null;
        currentMode = PlacementMode.START;
        statusLabel.setText("Grid cleared - Set start/end nodes");
        initializeGrid();
    }

    @FXML
    private void clearPath() {
        if (isRunning) return;

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                grid[row][col].reset();
                updateCellColor(row, col);
            }
        }
        statusLabel.setText("Path cleared - ready to search again");
    }

    @FXML
    private void playAll() {
        if (selectedAlgorithm == null) {
            statusLabel.setText("Please select an algorithm first");
            return;
        }

        if (isRunning) return;

        stepMode = false;
        startPathfinding();
    }

    @FXML
    private void stepForward() {
        if (selectedAlgorithm == null) {
            statusLabel.setText("Please select an algorithm first");
            return;
        }

        if (!isRunning) {
            stepMode = true;
            startPathfinding();
        } else if (waitingForStep) {
            synchronized (stepLock) {
                waitingForStep = false;
                stepLock.notifyAll();
            }
        }
    }

    private void startPathfinding() {
        if (startCell == null || endCell == null) {
            statusLabel.setText("Please set start and end nodes first");
            return;
        }

        clearPath();
        isRunning = true;
        statusLabel.setText(stepMode ? "Step Mode: Click Step Forward" : "Finding path...");

        btnStep.setDisable(false);
        btnPlayAll.setDisable(false);

        Thread pathThread = new Thread(() -> {
            try {
                boolean found = false;

                switch (selectedAlgorithm) {
                    case "Dijkstra":
                        found = new Dijkstra().findPath(grid, startCell, endCell, this);
                        break;
                    case "BFS":
                        found = new BFS().findPath(grid, startCell, endCell, this);
                        break;
                }

                final boolean pathFound = found;
                Platform.runLater(() -> {
                    statusLabel.setText(pathFound ? "Path found! ✓" : "No path exists ✗");
                    isRunning = false;
                    stepMode = false;
                    btnStep.setDisable(true);
                });

            } catch (InterruptedException e) {
                Platform.runLater(() -> {
                    statusLabel.setText("Pathfinding interrupted");
                    isRunning = false;
                    stepMode = false;
                    btnStep.setDisable(true);
                });
            }
        });

        pathThread.setDaemon(true);
        pathThread.start();
    }

    // PUBLIC METHODS FOR ALGORITHMS - THESE WERE MISSING!
    public void visualizeVisited(Cell cell) throws InterruptedException {
        if (cell.isStart || cell.isEnd) return;

        waitForStep();

        Platform.runLater(() -> {
            rectangles[cell.row][cell.col].setFill(Color.LIGHTBLUE);
        });

        Thread.sleep(stepMode ? 50 : 20);
    }

    public void visualizePath(Cell cell) throws InterruptedException {
        if (cell.isStart || cell.isEnd) return;

        waitForStep();

        Platform.runLater(() -> {
            rectangles[cell.row][cell.col].setFill(Color.YELLOW);
        });

        Thread.sleep(stepMode ? 100 : 30);
    }

    public List<Cell> getNeighbors(Cell cell, Cell[][] grid) {
        List<Cell> neighbors = new ArrayList<>();
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

        for (int[] dir : directions) {
            int newRow = cell.row + dir[0];
            int newCol = cell.col + dir[1];

            if (newRow >= 0 && newRow < ROWS && newCol >= 0 && newCol < COLS) {
                Cell neighbor = grid[newRow][newCol];
                if (!neighbor.isWall) {
                    neighbors.add(neighbor);
                }
            }
        }

        return neighbors;
    }

    private void waitForStep() throws InterruptedException {
        if (stepMode) {
            synchronized (stepLock) {
                waitingForStep = true;
                Platform.runLater(() -> statusLabel.setText("Waiting - Click Step Forward"));
                stepLock.wait();
            }
        }
    }

    private void updateCellColor(int row, int col) {
        Cell cell = grid[row][col];

        if (cell.isStart) {
            rectangles[row][col].setFill(Color.GREEN);
        } else if (cell.isEnd) {
            rectangles[row][col].setFill(Color.RED);
        } else if (cell.isWall) {
            rectangles[row][col].setFill(Color.BLACK);
        } else if (cell.isPath) {
            rectangles[row][col].setFill(Color.YELLOW);
        } else if (cell.isVisited) {
            rectangles[row][col].setFill(Color.LIGHTBLUE);
        } else {
            rectangles[row][col].setFill(Color.WHITE);
        }
    }
}