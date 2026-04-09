package com.dsa.visualizer.algorithms.graph;

import com.dsa.visualizer.controllers.GraphController;
import com.dsa.visualizer.models.Edge;
import com.dsa.visualizer.models.GraphNode;
import javafx.scene.paint.Color;

import java.util.*;

public class TopologicalSort {


    public void sort(List<GraphNode> nodes, GraphController controller)
            throws InterruptedException {

        Map<GraphNode, Integer> inDeg = new HashMap<>();
        for (GraphNode n : nodes) inDeg.put(n, 0);
        for (GraphNode n : nodes)
            for (Edge e : n.edges) inDeg.merge(e.to, 1, Integer::sum);


        Queue<GraphNode> queue = new LinkedList<>();
        for (GraphNode n : nodes) if (inDeg.get(n) == 0) queue.add(n);

        List<GraphNode> order = new ArrayList<>();
        int seq = 1;

        while (!queue.isEmpty()) {
            GraphNode u = queue.poll();
            order.add(u);
            controller.visualizeTopoNode(u, seq++);

            for (Edge edge : u.edges) {
                controller.visualizeEdgeWithColor(edge, Color.rgb(142, 68, 173), 3);
                int remaining = inDeg.get(edge.to) - 1;
                inDeg.put(edge.to, remaining);
                if (remaining == 0) queue.add(edge.to);
            }
        }

        if (order.size() < nodes.size()) {

            for (GraphNode n : nodes)
                if (inDeg.get(n) > 0) controller.visualizeNodeWithColor(n, Color.rgb(231, 76, 60));
            controller.onCycleResult(true);
        } else {
            controller.onTopoComplete(order);
        }
    }
}