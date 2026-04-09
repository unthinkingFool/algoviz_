package com.dsa.visualizer.algorithms.graph;

import com.dsa.visualizer.controllers.GraphController;
import com.dsa.visualizer.models.Edge;
import com.dsa.visualizer.models.GraphNode;
import javafx.scene.paint.Color;

import java.util.*;

public class BipartiteDetection {

    public void detect(List<GraphNode> nodes, GraphController controller)
            throws InterruptedException {

        Map<GraphNode, Integer> colorMap = new HashMap<>();
        boolean isBipartite = true;
        Edge conflictEdge = null;

        for (GraphNode start : nodes) {
            if (colorMap.containsKey(start)) continue;

            Queue<GraphNode> queue = new LinkedList<>();
            queue.add(start);
            colorMap.put(start, 0);

            while (!queue.isEmpty() && isBipartite) {
                GraphNode current = queue.poll();
                int currentColor = colorMap.get(current);

                Color displayColor = currentColor == 0
                        ? Color.CYAN : Color.rgb(230, 126, 34);
                controller.visualizeNodeWithColor(current, displayColor);

                for (Edge edge : current.edges) {
                    if (!colorMap.containsKey(edge.to)) {
                        colorMap.put(edge.to, 1 - currentColor);
                        queue.add(edge.to);
                        controller.visualizeEdge(edge);
                    } else if (colorMap.get(edge.to).equals(currentColor)) {
                        isBipartite = false;
                        conflictEdge = edge;
                        break;
                    }
                }
            }
            if (!isBipartite) break;
        }

        if (!isBipartite && conflictEdge != null)
            controller.visualizeCycleEdge(conflictEdge);

        controller.onBipartiteResult(isBipartite);
    }
}