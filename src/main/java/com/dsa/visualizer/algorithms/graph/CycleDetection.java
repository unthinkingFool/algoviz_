package com.dsa.visualizer.algorithms.graph;

import com.dsa.visualizer.controllers.GraphController;
import com.dsa.visualizer.models.Edge;
import com.dsa.visualizer.models.GraphNode;
import javafx.scene.paint.Color;

import java.util.*;

public class CycleDetection {


    private final Map<GraphNode, Integer> color = new HashMap<>();
    private boolean cycleFound = false;

    public void detect(List<GraphNode> nodes, boolean directed,
                       GraphController controller) throws InterruptedException {

        for (GraphNode n : nodes) color.put(n, 0);

        for (GraphNode n : nodes) {
            if (color.get(n) == 0) {
                if (directed) dfsDirected(n, controller);
                else          dfsUndirected(n, null, controller);
                if (cycleFound) break;
            }
        }

        controller.onCycleResult(cycleFound);
    }


    private void dfsDirected(GraphNode u, GraphController ctrl) throws InterruptedException {
        color.put(u, 1);
        ctrl.visualizeProcessingNode(u);

        for (Edge edge : u.edges) {
            GraphNode v = edge.to;
            if (color.get(v) == 0) {
                ctrl.visualizeEdge(edge);
                dfsDirected(v, ctrl);
                if (cycleFound) return;
            } else if (color.get(v) == 1) {
                // Back edge → cycle!
                cycleFound = true;
                ctrl.visualizeCycleEdge(edge);
                ctrl.visualizeNodeWithColor(v, Color.rgb(231, 76, 60));
                return;
            }
        }

        color.put(u, 2);
        ctrl.visualizeNodeWithColor(u, Color.rgb(46, 204, 113));
    }


    private boolean dfsUndirected(GraphNode u, GraphNode parent,
                                  GraphController ctrl) throws InterruptedException {
        color.put(u, 1);
        ctrl.visualizeProcessingNode(u);

        for (Edge edge : u.edges) {
            GraphNode v = edge.to;
            if (color.get(v) == 0) {
                ctrl.visualizeEdge(edge);
                if (dfsUndirected(v, u, ctrl)) return true;
            } else if (v != parent && color.get(v) == 1) {
                cycleFound = true;
                ctrl.visualizeCycleEdge(edge);
                ctrl.visualizeNodeWithColor(v, Color.rgb(231, 76, 60));
                return true;
            }
        }

        color.put(u, 2);
        ctrl.visualizeNodeWithColor(u, Color.rgb(46, 204, 113));
        return false;
    }
}