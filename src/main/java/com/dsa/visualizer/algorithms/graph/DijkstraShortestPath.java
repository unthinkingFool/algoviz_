package com.dsa.visualizer.algorithms.graph;

import com.dsa.visualizer.controllers.GraphController;
import com.dsa.visualizer.models.Edge;
import com.dsa.visualizer.models.GraphNode;
import javafx.scene.paint.Color;

import java.util.*;

public class DijkstraShortestPath {

    public void findShortestPath(List<GraphNode> nodes, GraphNode source,
                                 GraphController controller) throws InterruptedException {

        Map<GraphNode, Integer> dist     = new HashMap<>();
        Map<GraphNode, Edge>    prevEdge = new HashMap<>();
        Set<GraphNode>          settled  = new HashSet<>();

        for (GraphNode n : nodes) dist.put(n, Integer.MAX_VALUE);
        dist.put(source, 0);

        for (GraphNode n : nodes) controller.visualizeDijkstraNode(n, dist.get(n));

        PriorityQueue<GraphNode> pq = new PriorityQueue<>(
                Comparator.comparingInt(n -> dist.getOrDefault(n, Integer.MAX_VALUE)));
        pq.add(source);

        while (!pq.isEmpty()) {
            GraphNode u = pq.poll();
            if (settled.contains(u)) continue;
            settled.add(u);
            controller.visualizeProcessingNode(u);

            for (Edge edge : u.edges) {
                GraphNode v = edge.to;
                if (settled.contains(v)) continue;
                int d = dist.get(u);
                if (d == Integer.MAX_VALUE) continue;

                int newDist = d + edge.weight;
                if (newDist < dist.getOrDefault(v, Integer.MAX_VALUE)) {
                    dist.put(v, newDist);
                    prevEdge.put(v, edge);
                    pq.add(v);
                    controller.visualizeEdgeWithColor(edge, Color.rgb(52, 152, 219), 3);
                    controller.visualizeDijkstraNode(v, newDist);
                }
            }
        }


        for (Edge e : prevEdge.values()) controller.visualizeShortestPathEdge(e);

        controller.onDijkstraComplete(source, dist);
    }
}