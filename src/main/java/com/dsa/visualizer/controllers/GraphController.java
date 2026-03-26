package com.dsa.visualizer.controllers;

import com.dsa.visualizer.algorithms.graph.*;
import com.dsa.visualizer.models.Edge;
import com.dsa.visualizer.models.GraphNode;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class GraphController implements Initializable {

    @FXML private Pane  graphPane;
    @FXML private Label statusLabel;
    @FXML private Label algorithmLabel;
    @FXML private Label traversalResultLabel;
    @FXML private Button btnDFS;
    @FXML private Button btnBFS;
    @FXML private Button btnKruskal;
    @FXML private Button btnPlayAll;
    @FXML private Button btnStep;

    private List<GraphNode> nodes;
    private GraphNode selectedNode;
    private int nodeIdCounter = 0;
    private boolean isRunning = false;
    private boolean stepMode  = false;
    private String  selectedAlgorithm = null;
    private static final double NODE_RADIUS = 20;
    private final List<Integer> traversalOrder = new ArrayList<>();

    private final Lock       stepLock       = new ReentrantLock();
    private volatile boolean waitingForStep = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        nodes = new ArrayList<>();
        graphPane.setOnMouseClicked(e -> {
            if (isRunning) return;
            if (e.getButton() == MouseButton.PRIMARY && !e.isControlDown()) {
                addNode(e.getX(), e.getY());
            }
        });
        btnStep.setDisable(true);
    }

    // ── Algorithm selection ───────────────────────────────────────────────────

    @FXML private void selectDFS()     { selectAlgorithm("DFS Traversal", btnDFS);     }
    @FXML private void selectBFS()     { selectAlgorithm("BFS Traversal", btnBFS);     }
    @FXML private void selectKruskal() { selectAlgorithm("Kruskal MST",   btnKruskal); }

    private void selectAlgorithm(String algorithm, Button selectedButton) {
        if (isRunning) return;
        selectedAlgorithm = algorithm;
        algorithmLabel.setText("Algorithm: " + algorithm);
        String def = "-fx-background-color: #3498DB; -fx-text-fill: white; -fx-font-size: 13px; -fx-cursor: hand;";
        String act = "-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-font-size: 13px; -fx-cursor: hand; -fx-font-weight: bold;";
        btnDFS.setStyle(def); btnBFS.setStyle(def); btnKruskal.setStyle(def);
        selectedButton.setStyle(act);
    }

    // ── Graph building ────────────────────────────────────────────────────────

    private void addNode(double x, double y) {
        GraphNode node = new GraphNode(nodeIdCounter++, x, y);

        Circle circle = new Circle(NODE_RADIUS);
        circle.setCenterX(x); circle.setCenterY(y);
        circle.setFill(Color.rgb(52, 152, 219));
        circle.setStroke(Color.rgb(41, 128, 185));
        circle.setStrokeWidth(2);

        Text label = new Text(String.valueOf(node.id));
        label.setX(x - 5); label.setY(y + 5);
        label.setFill(Color.WHITE);
        label.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        node.circle = circle;
        node.label  = label;

        circle.setOnMouseClicked(e -> {
            if (isRunning) return;
            if (e.isControlDown() && e.getButton() == MouseButton.PRIMARY) {
                if (selectedNode == null) {
                    selectedNode = node;
                    circle.setFill(Color.ORANGE);
                    statusLabel.setText("Ctrl+Click another node to connect");
                } else if (selectedNode != node) {
                    addEdge(selectedNode, node);
                    selectedNode.circle.setFill(Color.rgb(52, 152, 219));
                    selectedNode = null;
                    statusLabel.setText("Edge added");
                }
                e.consume();
            }
        });

        nodes.add(node);
        graphPane.getChildren().addAll(circle, label);
        statusLabel.setText("Node " + node.id + " added");
    }

    private void addEdge(GraphNode from, GraphNode to) {
        int weight = new Random().nextInt(10) + 1;

        Line line = new Line(from.x, from.y, to.x, to.y);
        line.setStroke(Color.GRAY);
        line.setStrokeWidth(2);

        double midX = (from.x + to.x) / 2;
        double midY = (from.y + to.y) / 2;
        Text weightLabel = new Text(String.valueOf(weight));
        weightLabel.setX(midX); weightLabel.setY(midY);
        weightLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");

        // Forward edge — has the shared Line reference
        Edge forwardEdge = new Edge(from, to, weight);
        forwardEdge.line        = line;
        forwardEdge.weightLabel = weightLabel;
        from.addEdge(forwardEdge);

        // Reverse edge — shares the SAME Line so Kruskal can highlight it
        // (previously this was `new Edge(...)` with a null line — that was the bug)
        Edge reverseEdge = new Edge(to, from, weight);
        reverseEdge.line        = line;        // ← shared reference, not null
        reverseEdge.weightLabel = weightLabel;
        to.addEdge(reverseEdge);

        graphPane.getChildren().add(0, line);
        graphPane.getChildren().add(weightLabel);
    }

    @FXML
    private void generateRandomGraph() {
        if (isRunning) return;
        clearGraph();
        Random random = new Random();
        int numNodes = 8;

        // Add all nodes first
        for (int i = 0; i < numNodes; i++) {
            double x = 100 + random.nextDouble() * 700;
            double y = 100 + random.nextDouble() * 400;
            addNode(x, y);
        }

        // Guarantee connectivity: build a spanning tree first by connecting
        // each new node to a random already-connected node (like Prim's).
        // This ensures Kruskal always has a valid MST to find.
        for (int i = 1; i < numNodes; i++) {
            int target = random.nextInt(i);   // pick any already-added node
            addEdge(nodes.get(i), nodes.get(target));
        }

        // Add a few extra random edges for variety (creates cycles Kruskal skips)
        int extraEdges = numNodes / 2;
        for (int k = 0; k < extraEdges; k++) {
            int a = random.nextInt(numNodes);
            int b = random.nextInt(numNodes);
            if (a != b && !hasEdge(nodes.get(a), nodes.get(b))) {
                addEdge(nodes.get(a), nodes.get(b));
            }
        }

        statusLabel.setText("Random connected graph generated");
    }

    private boolean hasEdge(GraphNode from, GraphNode to) {
        for (Edge edge : from.edges) {
            if (edge.to == to) return true;
        }
        return false;
    }

    @FXML
    private void clearGraph() {
        if (isRunning) return;
        graphPane.getChildren().clear();
        nodes.clear();
        selectedNode = null;
        nodeIdCounter = 0;   // reset so IDs stay 0..n-1
        traversalOrder.clear();
        traversalResultLabel.setText("Run an algorithm to see traversal order");
        statusLabel.setText("Graph cleared");
    }

    @FXML
    private void resetColors() {
        if (isRunning) return;
        for (GraphNode node : nodes) {
            node.visited = false;
            node.circle.setFill(Color.rgb(52, 152, 219));
            for (Edge edge : node.edges) {
                if (edge.line != null) {
                    edge.line.setStroke(Color.GRAY);
                    edge.line.setStrokeWidth(2);
                }
            }
        }
        traversalOrder.clear();
        traversalResultLabel.setText("Run an algorithm to see traversal order");
        statusLabel.setText("Colors reset");
    }

    // ── Controls ──────────────────────────────────────────────────────────────

    @FXML
    private void playAll() {
        if (selectedAlgorithm == null) { statusLabel.setText("Please select an algorithm first"); return; }
        if (isRunning) return;
        stepMode = false;
        startAlgorithm();
    }

    @FXML
    private void stepForward() {
        if (selectedAlgorithm == null) { statusLabel.setText("Please select an algorithm first"); return; }
        if (!isRunning) { stepMode = true; startAlgorithm(); }
        else if (waitingForStep) {
            synchronized (stepLock) { waitingForStep = false; stepLock.notifyAll(); }
        }
    }

    private void startAlgorithm() {
        if (nodes.isEmpty()) { statusLabel.setText("Please add nodes first"); return; }
        resetColors();
        isRunning = true;
        traversalOrder.clear();
        Platform.runLater(() -> traversalResultLabel.setText(
                "Kruskal MST".equals(selectedAlgorithm) ? "Building MST..." : "Traversing..."));
        statusLabel.setText(stepMode ? "Step Mode: Click Step Forward" : "Running algorithm...");
        btnStep.setDisable(false);

        Thread algoThread = new Thread(() -> {
            try {
                switch (selectedAlgorithm) {
                    case "DFS Traversal": new DFSTraversal().traverse(nodes, this); break;
                    case "BFS Traversal": new BFSTraversal().traverse(nodes, this); break;
                    case "Kruskal MST":   new KruskalMST().findMST(nodes, this);    break;
                }
                Platform.runLater(() -> {
                    if (!"Kruskal MST".equals(selectedAlgorithm)) {
                        traversalResultLabel.setText(selectedAlgorithm + " Order: " + traversalOrder);
                    }
                    statusLabel.setText("Algorithm complete!");
                    isRunning = false; stepMode = false; btnStep.setDisable(true);
                });
            } catch (InterruptedException e) {
                Platform.runLater(() -> {
                    statusLabel.setText("Algorithm interrupted");
                    isRunning = false; stepMode = false; btnStep.setDisable(true);
                });
            }
        });
        algoThread.setDaemon(true);
        algoThread.start();
    }

    // ── Public API for algorithms ─────────────────────────────────────────────

    public void visualizeNodeVisit(GraphNode node) throws InterruptedException {
        waitForStep();
        Platform.runLater(() -> {
            node.circle.setFill(Color.rgb(46, 204, 113));
            traversalOrder.add(node.id);
        });
        Thread.sleep(stepMode ? 300 : 500);
    }

    public void visualizeEdge(Edge edge) throws InterruptedException {
        waitForStep();
        Platform.runLater(() -> {
            if (edge.line != null) {
                edge.line.setStroke(Color.rgb(46, 204, 113));
                edge.line.setStrokeWidth(4);
            }
        });
        Thread.sleep(stepMode ? 200 : 600);
    }

    /**
     * Called by KruskalMST when it finishes.
     * Reports whether a full spanning tree was found (graph was connected)
     * or only a spanning forest (graph was disconnected).
     */
    public void onKruskalComplete(int edgesAdded, int edgesNeeded) {
        Platform.runLater(() -> {
            if (edgesAdded == edgesNeeded) {
                traversalResultLabel.setText(
                        "MST complete! " + edgesAdded + " edges selected (total weight shown on edges).");
            } else {
                traversalResultLabel.setText(
                        "Graph is disconnected — spanning forest built with "
                                + edgesAdded + " edges (needed " + edgesNeeded + " for full MST).");
            }
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
    }
}