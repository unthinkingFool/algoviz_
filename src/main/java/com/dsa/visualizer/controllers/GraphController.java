package com.dsa.visualizer.controllers;

import com.dsa.visualizer.algorithms.graph.*;
import com.dsa.visualizer.models.Edge;
import com.dsa.visualizer.models.GraphNode;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.net.URL;
//import java.sql.Time;
import java.util.*;
import java.util.Optional;

public class GraphController implements Initializable {

    @FXML private Pane  graphPane;
    @FXML private Label statusLabel, algorithmLabel, traversalResultLabel, sectionLabel;

    @FXML private HBox  unweightedAlgoPanel, weightedAlgoPanel;
    @FXML private Button btnSectionUnweighted, btnSectionWeighted;
    @FXML private Button btnDFS, btnBFS, btnCycleU, btnTopoU, btnBipartiteU;
    @FXML private Button btnDijkstra, btnKruskal, btnCycleW, btnTopoW, btnBipartiteW;
    @FXML private Button btnPlayAll, btnStep, btnSetSource, btnDirectedToggle;

    // ── State ─────────────────────────────────────────────────────────────────
    private List<GraphNode> nodes      = new ArrayList<>();
    private GraphNode selectedNode, dijkstraSource;
    private int     nodeIdCounter      = 0;
    private boolean isRunning          = false;
    private boolean stepMode           = false;
    private boolean isWeighted         = false;
    private boolean isDirected         = false;
    private boolean selectingSource    = false;
    private String  selectedAlgorithm  = null;

    private static final double NODE_RADIUS = 20;
    private final List<Integer>            traversalOrder = new ArrayList<>();
    private final List<javafx.scene.Node>  overlayNodes   = new ArrayList<>();

    private final Object stepMonitor = new Object();
    private volatile boolean waitingForStep = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        showSection(false);
        btnStep.setDisable(true);
        btnSetSource.setVisible(false);

        graphPane.setOnMouseClicked(e -> {
            if (isRunning || selectingSource) return;
            if (e.getButton() == MouseButton.PRIMARY && !e.isControlDown())
                addNode(e.getX(), e.getY());
        });
    }


    @FXML private void switchToUnweighted() {
        if (isRunning) return;
        isWeighted = false;
        showSection(false);
        resetAlgoSelection();
        clearGraph();
    }

    @FXML private void switchToWeighted() {
        if (isRunning) return;
        isWeighted = true;
        showSection(true);
        resetAlgoSelection();
        clearGraph();
    }

    private void showSection(boolean weighted) {
        unweightedAlgoPanel.setVisible(!weighted); unweightedAlgoPanel.setManaged(!weighted);
        weightedAlgoPanel.setVisible(weighted);    weightedAlgoPanel.setManaged(weighted);

        String active   = "-fx-background-color: #1A252F; -fx-text-fill: white; -fx-font-size: 13px; -fx-background-radius: 5; -fx-cursor: hand; -fx-font-weight: bold;";
        String inactive = "-fx-background-color: #3498DB; -fx-text-fill: white; -fx-font-size: 13px; -fx-background-radius: 5; -fx-cursor: hand;";
        btnSectionUnweighted.setStyle(weighted ? inactive : active);
        btnSectionWeighted.setStyle(weighted ? active : inactive);

        sectionLabel.setText(weighted ? " Weighted" : " Unweighted");
        sectionLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: "
                + (weighted ? "#F39C12" : "#3498DB") + "; -fx-padding: 3 9 3 9;"
                + " -fx-background-color: " + (weighted ? "#FEF9E7" : "#EBF5FB") + ";"
                + " -fx-background-radius: 10;");
    }

    @FXML private void toggleDirected() {
        if (isRunning) return;
        isDirected = !isDirected;
        btnDirectedToggle.setText(isDirected ? "→ Directed" : "↔ Undirected");
        btnDirectedToggle.setStyle(isDirected
                ? "-fx-background-color: #E67E22; -fx-text-fill: white; -fx-font-size: 12px; -fx-background-radius: 5; -fx-cursor: hand;"
                : "-fx-background-color: #7F8C8D; -fx-text-fill: white; -fx-font-size: 12px; -fx-background-radius: 5; -fx-cursor: hand;");
        clearGraph();
        statusLabel.setText("Switched to " + (isDirected ? "directed" : "undirected") + " mode");
    }

    @FXML private void selectDFS()       { selectAlgorithm("DFS Traversal",      btnDFS);        }
    @FXML private void selectBFS()       { selectAlgorithm("BFS Traversal",      btnBFS);        }
    @FXML private void selectDijkstra()  { selectAlgorithm("Dijkstra",           btnDijkstra);   }
    @FXML private void selectKruskal()   { selectAlgorithm("Kruskal MST",        btnKruskal);    }
    @FXML private void selectCycle()     { selectAlgorithm("Cycle Detection",    isWeighted ? btnCycleW    : btnCycleU);     }
    @FXML private void selectTopo()      { selectAlgorithm("Topological Sort",   isWeighted ? btnTopoW     : btnTopoU);      }
    @FXML private void selectBipartite() { selectAlgorithm("Bipartite Check",    isWeighted ? btnBipartiteW: btnBipartiteU); }

    private void selectAlgorithm(String algo, Button btn) {
        if (isRunning) return;
        selectedAlgorithm = algo;
        algorithmLabel.setText("Algorithm: " + algo);
        String def = defaultBtnStyle();
        String act = "-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-font-size: 12px; -fx-cursor: hand; -fx-font-weight: bold; -fx-background-radius: 5;";
        allAlgoBtns().forEach(b -> { if (b != null) b.setStyle(def); });
        if (btn != null) btn.setStyle(act);
        boolean needsSrc = "Dijkstra".equals(algo);
        btnSetSource.setVisible(needsSrc);
        if (!needsSrc) dijkstraSource = null;
    }

    private void resetAlgoSelection() {
        selectedAlgorithm = null;
        algorithmLabel.setText("Algorithm: None Selected");
        allAlgoBtns().forEach(b -> { if (b != null) b.setStyle(defaultBtnStyle()); });
        btnSetSource.setVisible(false);
    }

    private List<Button> allAlgoBtns() {
        return Arrays.asList(btnDFS, btnBFS, btnCycleU, btnTopoU, btnBipartiteU,
                btnDijkstra, btnKruskal, btnCycleW, btnTopoW, btnBipartiteW);
    }

    private String defaultBtnStyle() {
        return "-fx-background-color: #3498DB; -fx-text-fill: white; -fx-font-size: 12px; -fx-cursor: hand; -fx-background-radius: 5;";
    }


    private void addNode(double x, double y) {
        GraphNode node = new GraphNode(nodeIdCounter++, x, y);

        Circle circle = new Circle(NODE_RADIUS);
        circle.setCenterX(x); circle.setCenterY(y);
        circle.setFill(Color.rgb(52, 152, 219));
        circle.setStroke(Color.rgb(41, 128, 185));
        circle.setStrokeWidth(2.5);
        circle.setEffect(createDropShadow());

        Text label = new Text(String.valueOf(node.id));
        label.setX(x - (node.id >= 10 ? 8 : 5)); label.setY(y + 5);
        label.setFill(Color.WHITE);
        label.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        node.circle = circle;
        node.label  = label;

        circle.setOnMouseClicked(e -> {
            if (selectingSource && !isRunning) { setDijkstraSource(node); e.consume(); return; }
            if (isRunning) return;
            if (e.isControlDown() && e.getButton() == MouseButton.PRIMARY) {
                handleCtrlClick(node); e.consume();
            }
        });
        circle.setOnMouseEntered(e -> { if (!isRunning) { circle.setScaleX(1.12); circle.setScaleY(1.12); } });
        circle.setOnMouseExited (e -> { circle.setScaleX(1.0); circle.setScaleY(1.0); });

        nodes.add(node);
        graphPane.getChildren().addAll(circle, label);


        circle.setScaleX(0); circle.setScaleY(0);
        ScaleTransition pop = new ScaleTransition(Duration.millis(220), circle);
        pop.setToX(1.0); pop.setToY(1.0);
        pop.setInterpolator(Interpolator.EASE_BOTH); pop.play();

        statusLabel.setText("Node " + node.id + " added  •  Ctrl+Click two nodes to connect");
    }

    private void handleCtrlClick(GraphNode node) {
        if (selectedNode == null) {
            selectedNode = node;
            node.circle.setFill(Color.ORANGE);
            Glow g = new Glow(0.9); node.circle.setEffect(g);
            statusLabel.setText("Ctrl+Click another node to connect → " + node.id);
        } else if (selectedNode != node) {
            addEdge(selectedNode, node);
            selectedNode.circle.setFill(Color.rgb(52, 152, 219));
            selectedNode.circle.setEffect(createDropShadow());
            selectedNode = null;
            statusLabel.setText("Edge added");
        }
    }

    private void addEdge(GraphNode from, GraphNode to) {
        if (hasEdge(from, to)) return;
        int weight = 1;
        if (isWeighted) {
            javafx.scene.control.TextInputDialog dialog =
                    new javafx.scene.control.TextInputDialog("1");
            dialog.setTitle("Edge Weight");
            dialog.setHeaderText("Enter weight for this edge");
            dialog.setContentText("Weight (1-99):");

            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                try {
                    weight = Integer.parseInt(result.get().trim());
                    if (weight < 1) weight = 1;
                    if (weight > 99) weight = 99;
                } catch (NumberFormatException e) {
                    weight = 1;
                }
            } else {
                return;
            }
        }

        double dx = to.x - from.x, dy = to.y - from.y;
        double len = Math.sqrt(dx * dx + dy * dy);
        if (len < 1) return;
        double ux = dx / len, uy = dy / len;

        Line line = new Line(
                from.x + ux * NODE_RADIUS, from.y + uy * NODE_RADIUS,
                to.x   - ux * NODE_RADIUS, to.y   - uy * NODE_RADIUS);
        line.setStroke(Color.rgb(149, 165, 166)); line.setStrokeWidth(2);


        Text wtLabel = null;
        if (isWeighted) {
            double mx = (from.x + to.x) / 2 - uy * 14;
            double my = (from.y + to.y) / 2 + ux * 14;
            Rectangle pill = new Rectangle(mx - 11, my - 12, 24, 17);
            pill.setFill(Color.WHITE); pill.setStroke(Color.rgb(189,195,199));
            pill.setArcWidth(7); pill.setArcHeight(7);
            wtLabel = new Text(String.valueOf(weight));
            wtLabel.setX(mx - 5); wtLabel.setY(my + 4);
            wtLabel.setFill(Color.rgb(44, 62, 80));
            wtLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
            graphPane.getChildren().add(0, pill);
            graphPane.getChildren().add(wtLabel);
        }


        Polygon arrow = null;
        if (isDirected) {
            double tipX = to.x - ux * NODE_RADIUS, tipY = to.y - uy * NODE_RADIUS;
            double ang = Math.atan2(dy, dx), spread = Math.toRadians(28), sz = 13;
            arrow = new Polygon(tipX, tipY,
                    tipX - sz * Math.cos(ang - spread), tipY - sz * Math.sin(ang - spread),
                    tipX - sz * Math.cos(ang + spread), tipY - sz * Math.sin(ang + spread));
            arrow.setFill(Color.rgb(149, 165, 166));
            graphPane.getChildren().add(arrow);
        }

        Edge fwd = new Edge(from, to, weight);
        fwd.line = line; fwd.weightLabel = wtLabel; fwd.arrowHead = arrow;
        from.addEdge(fwd);

        if (!isDirected) {
            Edge rev = new Edge(to, from, weight);
            rev.line = line; rev.weightLabel = wtLabel; rev.arrowHead = null;
            to.addEdge(rev);
        }

        graphPane.getChildren().add(0, line);
        line.setOpacity(0);

        FadeTransition ft = new FadeTransition(Duration.millis(280), line);
        ft.setToValue(1.0);
        ft.play();
    }
    private void addEdgeWithWeight(GraphNode from, GraphNode to, int weight) {
        if (hasEdge(from, to)) return;

        double dx = to.x - from.x, dy = to.y - from.y;
        double len = Math.sqrt(dx * dx + dy * dy);
        if (len < 1) return;
        double ux = dx / len, uy = dy / len;

        Line line = new Line(
                from.x + ux * NODE_RADIUS, from.y + uy * NODE_RADIUS,
                to.x   - ux * NODE_RADIUS, to.y   - uy * NODE_RADIUS);
        line.setStroke(Color.rgb(149, 165, 166)); line.setStrokeWidth(2);

        Text wtLabel = null;
        if (isWeighted) {
            double mx = (from.x + to.x) / 2 - uy * 14;
            double my = (from.y + to.y) / 2 + ux * 14;
            Rectangle pill = new Rectangle(mx - 11, my - 12, 24, 17);
            pill.setFill(Color.WHITE); pill.setStroke(Color.rgb(189,195,199));
            pill.setArcWidth(7); pill.setArcHeight(7);
            wtLabel = new Text(String.valueOf(weight));
            wtLabel.setX(mx - 5); wtLabel.setY(my + 4);
            wtLabel.setFill(Color.rgb(44, 62, 80));
            wtLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
            graphPane.getChildren().add(0, pill);
            graphPane.getChildren().add(wtLabel);
        }

        Polygon arrow = null;
        if (isDirected) {
            double tipX = to.x - ux * NODE_RADIUS, tipY = to.y - uy * NODE_RADIUS;
            double ang = Math.atan2(dy, dx), spread = Math.toRadians(28), sz = 13;
            arrow = new Polygon(tipX, tipY,
                    tipX - sz * Math.cos(ang - spread), tipY - sz * Math.sin(ang - spread),
                    tipX - sz * Math.cos(ang + spread), tipY - sz * Math.sin(ang + spread));
            arrow.setFill(Color.rgb(149, 165, 166));
            graphPane.getChildren().add(arrow);
        }

        Edge fwd = new Edge(from, to, weight);
        fwd.line = line; fwd.weightLabel = wtLabel; fwd.arrowHead = arrow;
        from.addEdge(fwd);

        if (!isDirected) {
            Edge rev = new Edge(to, from, weight);
            rev.line = line; rev.weightLabel = wtLabel; rev.arrowHead = null;
            to.addEdge(rev);
        }

        graphPane.getChildren().add(0, line);
        line.setOpacity(0);
        FadeTransition ft = new FadeTransition(Duration.millis(280), line);
        ft.setToValue(1.0); ft.play();
    }
    @FXML private void generateRandomGraph() {
        if (isRunning) return;
        clearGraph();
        int n = 8;
        double cx = 490, cy = 310, r = 210;
        Random rng = new Random();

        for (int i = 0; i < n; i++) {
            double angle = 2 * Math.PI * i / n - Math.PI / 2;
            addNode(cx + r * Math.cos(angle) + (rng.nextDouble() - .5) * 38,
                    cy + r * Math.sin(angle) + (rng.nextDouble() - .5) * 38);
        }


        for (int i = 1; i < n; i++)
            addEdgeWithWeight(nodes.get(i), nodes.get(rng.nextInt(i)), rng.nextInt(19) + 1); // weight 1–19

        for (int k = 0; k < n / 2; k++) {
            int a = rng.nextInt(n), b = rng.nextInt(n);
            if (a != b)
                addEdgeWithWeight(nodes.get(a), nodes.get(b), rng.nextInt(19) + 1);
        }

        statusLabel.setText("Random " + (isDirected ? "directed" : "undirected")
                + " " + (isWeighted ? "weighted" : "unweighted") + " graph generated");
    }



    private boolean hasEdge(GraphNode from, GraphNode to) {
        return from.edges.stream().anyMatch(e -> e.to == to);
    }

    @FXML private void clearGraph() {
        if (isRunning) return;
        graphPane.getChildren().clear();
        nodes.clear(); overlayNodes.clear();
        selectedNode = null; dijkstraSource = null;
        nodeIdCounter = 0; traversalOrder.clear(); selectingSource = false;
        traversalResultLabel.setText("Run an algorithm to see results");
        statusLabel.setText("Graph cleared  •  Click canvas to add nodes");
    }

    @FXML private void resetColors() {
        if (isRunning) return;
        graphPane.getChildren().removeAll(overlayNodes); overlayNodes.clear();
        for (GraphNode node : nodes) {
            node.visited = false;
            node.circle.setFill(Color.rgb(52, 152, 219));
            node.circle.setStroke(Color.rgb(41, 128, 185)); node.circle.setStrokeWidth(2.5);
            node.circle.setEffect(createDropShadow());
            for (Edge e : node.edges) {
                if (e.line != null) { e.line.setStroke(Color.rgb(149,165,166)); e.line.setStrokeWidth(2); e.line.getStrokeDashArray().clear(); }
                if (e.arrowHead != null) e.arrowHead.setFill(Color.rgb(149, 165, 166));
            }
        }
        if (dijkstraSource != null) dijkstraSource.circle.setFill(Color.rgb(230, 126, 34));
        traversalOrder.clear();
        traversalResultLabel.setText("Run an algorithm to see results");
        statusLabel.setText("Colors reset");
    }

    @FXML private void activateSourceSelection() {
        if (nodes.isEmpty()) { statusLabel.setText("Add nodes first"); return; }
        selectingSource = true;
        nodes.forEach(n -> { n.circle.setStroke(Color.rgb(230, 126, 34)); n.circle.setStrokeWidth(3); });
        statusLabel.setText(" Click a node to set as Dijkstra source");
    }

    private void setDijkstraSource(GraphNode node) {
        selectingSource = false;
        dijkstraSource  = node;
        nodes.forEach(n -> { n.circle.setStroke(Color.rgb(41,128,185)); n.circle.setStrokeWidth(2.5); });
        node.circle.setFill(Color.rgb(230, 126, 34));
        node.circle.setStroke(Color.rgb(211, 84, 0)); node.circle.setStrokeWidth(3);
        statusLabel.setText("Source: Node " + node.id + "  •  Click ▶ Run");
    }

    @FXML private void playAll() {
        if (selectedAlgorithm == null) { statusLabel.setText("Select an algorithm first"); return; }
        if (isRunning) return;
        stepMode = false; startAlgorithm();
    }

    @FXML private void stepForward() {
        if (selectedAlgorithm == null) { statusLabel.setText("Select an algorithm first"); return; }
        if (!isRunning) { stepMode = true; startAlgorithm(); }
        else if (waitingForStep) {
            synchronized (stepMonitor) { waitingForStep = false; stepMonitor.notifyAll(); }
        }
    }

    private void startAlgorithm() {
        if (nodes.isEmpty()) { statusLabel.setText("Add nodes first"); return; }
        if ("Dijkstra".equals(selectedAlgorithm) && dijkstraSource == null) {
            statusLabel.setText("Click ' Set Source' to pick a starting node"); return;
        }
        if ("Topological Sort".equals(selectedAlgorithm) && !isDirected) {
            traversalResultLabel.setText("⚠ Topological Sort requires Directed mode — toggle → Directed first.");
            statusLabel.setText("Enable directed mode (→ Directed button)"); return;
        }
        resetColors();
        isRunning = true; traversalOrder.clear();
        Platform.runLater(() -> traversalResultLabel.setText("Running " + selectedAlgorithm + "..."));
        statusLabel.setText(stepMode ? "⏸ Step Mode — click Step ▶|" : "▶ Running...");
        btnStep.setDisable(false);

        new Thread(() -> {
            try {
                switch (selectedAlgorithm) {
                    case "DFS Traversal":    new DFSTraversal().traverse(nodes, this);                              break;
                    case "BFS Traversal":    new BFSTraversal().traverse(nodes, this);                              break;
                    case "Kruskal MST":      new KruskalMST().findMST(nodes, this);                                 break;
                    case "Dijkstra":         new DijkstraShortestPath().findShortestPath(nodes, dijkstraSource, this); break;
                    case "Cycle Detection":  new CycleDetection().detect(nodes, isDirected, this);                  break;
                    case "Topological Sort": new TopologicalSort().sort(nodes, this);                               break;
                    case "Bipartite Check":  new BipartiteDetection().detect(nodes, this);                         break;
                }
                Platform.runLater(() -> {
                    if ("DFS Traversal".equals(selectedAlgorithm) || "BFS Traversal".equals(selectedAlgorithm))
                        traversalResultLabel.setText(selectedAlgorithm + " Order: " + traversalOrder);
                    statusLabel.setText("✓ Algorithm complete!");
                    isRunning = false; stepMode = false; btnStep.setDisable(true);
                });
            } catch (InterruptedException e) {
                Platform.runLater(() -> { statusLabel.setText("Interrupted"); isRunning = false; stepMode = false; btnStep.setDisable(true); });
            }
        }) {{ setDaemon(true); start(); }};
    }

    public void visualizeNodeVisit(GraphNode node) throws InterruptedException {
        waitForStep();
        Platform.runLater(() -> {
            node.circle.setFill(Color.rgb(46, 204, 113));
            pulse(node.circle, Color.rgb(46, 204, 113));
            traversalOrder.add(node.id);
        });
        Thread.sleep(stepMode ? 250 : 500);
    }

    public void visualizeEdge(Edge edge) throws InterruptedException {
        waitForStep();
        Platform.runLater(() -> flashEdge(edge, Color.rgb(46, 204, 113), 4));
        Thread.sleep(stepMode ? 200 : 400);
    }

    public void visualizeEdgeWithColor(Edge edge, Color color, double width) throws InterruptedException {
        waitForStep();
        Platform.runLater(() -> flashEdge(edge, color, width));
        Thread.sleep(stepMode ? 150 : 300);
    }

    public void visualizeNodeWithColor(GraphNode node, Color color) throws InterruptedException {
        waitForStep();
        Platform.runLater(() -> { node.circle.setFill(color); pulse(node.circle, color); });
        Thread.sleep(stepMode ? 200 : 350);
    }

    public void visualizeProcessingNode(GraphNode node) throws InterruptedException {
        waitForStep();
        Platform.runLater(() -> { node.circle.setFill(Color.rgb(230, 126, 34)); pulse(node.circle, Color.rgb(230, 126, 34)); });
        Thread.sleep(stepMode ? 180 : 320);
    }

    public void visualizeDijkstraNode(GraphNode node, int distance) throws InterruptedException {
        waitForStep();
        Platform.runLater(() -> {
            node.circle.setFill(Color.rgb(44, 62, 80));
            node.circle.setStroke(Color.rgb(241, 196, 15)); node.circle.setStrokeWidth(3);


            String id = "d_" + node.id;
            overlayNodes.stream().filter(n -> id.equals(n.getId())).findFirst().ifPresent(old -> {
                graphPane.getChildren().remove(old);
            });
            overlayNodes.removeIf(n -> id.equals(n.getId()));

            Text badge = new Text(distance == Integer.MAX_VALUE ? "∞" : String.valueOf(distance));
            badge.setId(id);
            badge.setX(node.x + NODE_RADIUS + 3); badge.setY(node.y - NODE_RADIUS - 2);
            badge.setFill(Color.rgb(241, 196, 15));
            badge.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;"); badge.setOpacity(0);
            overlayNodes.add(badge); graphPane.getChildren().add(badge);
            FadeTransition ft = new FadeTransition(Duration.millis(300), badge);
            ft.setToValue(1.0);
            ft.play();
        });
        Thread.sleep(stepMode ? 200 : 350);
    }

    public void visualizeShortestPathEdge(Edge edge) throws InterruptedException {
        waitForStep();
        Platform.runLater(() -> flashEdge(edge, Color.rgb(241, 196, 15), 5));
        Thread.sleep(stepMode ? 150 : 300);
    }

    public void visualizeCycleEdge(Edge edge) throws InterruptedException {
        waitForStep();
        Platform.runLater(() -> {
            if (edge.line == null) return;
            edge.line.setStroke(Color.rgb(231, 76, 60)); edge.line.setStrokeWidth(5);
            edge.line.getStrokeDashArray().setAll(10.0, 5.0);
            if (edge.arrowHead != null) edge.arrowHead.setFill(Color.rgb(231, 76, 60));
            // Flash animation
            Timeline t1=new Timeline(
                    new KeyFrame(Duration.millis(0),   e -> edge.line.setOpacity(1.0)),
                    new KeyFrame(Duration.millis(180), e -> edge.line.setOpacity(0.25)),
                    new KeyFrame(Duration.millis(360), e -> edge.line.setOpacity(1.0)),
                    new KeyFrame(Duration.millis(540), e -> edge.line.setOpacity(0.25)),
                    new KeyFrame(Duration.millis(720), e -> edge.line.setOpacity(1.0))
            ) ;
            t1.play();
        });
        Thread.sleep(stepMode ? 300 : 800);
    }

    public void visualizeTopoNode(GraphNode node, int order) throws InterruptedException {
        waitForStep();
        Platform.runLater(() -> {
            node.circle.setFill(Color.rgb(142, 68, 173)); pulse(node.circle, Color.rgb(142, 68, 173));
            Text badge = new Text("#" + order);
            badge.setX(node.x - 11); badge.setY(node.y - NODE_RADIUS - 7);
            badge.setFill(Color.rgb(187, 143, 206));
            badge.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;"); badge.setOpacity(0);
            overlayNodes.add(badge); graphPane.getChildren().add(badge);
            FadeTransition ft = new FadeTransition(Duration.millis(320), badge);
            ft.setToValue(1.0);
            ft.play();
        });
        Thread.sleep(stepMode ? 400 : 650);
    }

    public void onKruskalComplete(int added, int needed) {
        Platform.runLater(() -> traversalResultLabel.setText(added == needed
                ? "✓ MST complete! " + added + " edges — minimum spanning tree found."
                : "⚠ Spanning forest: " + added + "/" + needed + " edges (disconnected graph)."));
    }

    public void onCycleResult(boolean has) {
        Platform.runLater(() -> traversalResultLabel.setText(has
                ? " Cycle detected! Back edge highlighted in red."
                : " No cycle — graph is acyclic."));
    }

    public void onBipartiteResult(boolean is) {
        Platform.runLater(() -> traversalResultLabel.setText(is
                ? " Graph is BIPARTITE — two groups: 🔵 Cyan and 🟠 Orange"
                : " NOT bipartite — odd-length cycle exists (conflict edge in red)."));
    }

    public void onTopoComplete(List<GraphNode> sorted) {
        Platform.runLater(() -> {
            StringBuilder sb = new StringBuilder("Topo Order: ");
            for (int i = 0; i < sorted.size(); i++) { sb.append(sorted.get(i).id); if (i < sorted.size()-1) sb.append(" → "); }
            traversalResultLabel.setText(sb.toString());
        });
    }

    public void onDijkstraComplete(GraphNode src, Map<GraphNode, Integer> dist) {
        Platform.runLater(() -> {
            StringBuilder sb = new StringBuilder("Shortest paths from " + src.id + ":  ");
            nodes.stream().sorted(Comparator.comparingInt(n -> n.id)).forEach(n -> {
                int d = dist.getOrDefault(n, Integer.MAX_VALUE);
                sb.append(n.id).append("=").append(d == Integer.MAX_VALUE ? "∞" : d).append("  ");
            });
            traversalResultLabel.setText(sb.toString());
        });
    }

    private void pulse(Circle c, Color color) {
        ScaleTransition st = new ScaleTransition(Duration.millis(160), c);
        st.setFromX(1.0); st.setFromY(1.0); st.setToX(1.3); st.setToY(1.3);
        st.setAutoReverse(true); st.setCycleCount(2); st.play();
        Glow g = new Glow(0.7); c.setEffect(g);
        Timeline t1=new Timeline(new KeyFrame(Duration.millis(380), e -> c.setEffect(createDropShadow())));
        t1.play();

    }

    private void flashEdge(Edge edge, Color color, double width) {
        if (edge.line == null) return;
        Timeline t1=new Timeline(
                new KeyFrame(Duration.millis(0),   e -> { edge.line.setStroke(Color.rgb(200,200,200)); edge.line.setStrokeWidth(2); }),
                new KeyFrame(Duration.millis(220), e -> { edge.line.setStroke(color); edge.line.setStrokeWidth(width); })
        ) ;
        t1.play();
        if (edge.arrowHead != null) edge.arrowHead.setFill(color);
    }

    private DropShadow createDropShadow() {
        DropShadow ds = new DropShadow(); ds.setRadius(8); ds.setColor(Color.rgb(0,0,0,0.28)); return ds;
    }

    private void waitForStep() throws InterruptedException {
        if (stepMode) {
            synchronized (stepMonitor) {
                waitingForStep = true;
                Platform.runLater(() -> statusLabel.setText("⏸ Paused — click Step ▶|"));
                stepMonitor.wait();
            }
        }
    }

    public boolean isDirected() { return isDirected; }
}

