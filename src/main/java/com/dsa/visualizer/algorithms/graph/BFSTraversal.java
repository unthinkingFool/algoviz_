package com.dsa.visualizer.algorithms.graph;

import com.dsa.visualizer.controllers.GraphController;
import com.dsa.visualizer.models.Edge;
import com.dsa.visualizer.models.GraphNode;
import java.util.*;

public class BFSTraversal {

    public void traverse(List<GraphNode> nodes, GraphController controller)
            throws InterruptedException {
        if (nodes.isEmpty()) return;

        Queue<GraphNode> queue = new LinkedList<>();

        // Iterate through every node to ensure we hit disconnected components
        for (GraphNode startNode : nodes) {
            if (!startNode.visited) {
                startNode.visited = true;
                queue.add(startNode);
                controller.visualizeNodeVisit(startNode);

                while (!queue.isEmpty()) {
                    GraphNode current = queue.poll();

                    for (Edge edge : current.edges) {
                        if (!edge.to.visited) {
                            edge.to.visited = true;
                            controller.visualizeEdge(edge);
                            controller.visualizeNodeVisit(edge.to);
                            queue.add(edge.to);
                        }
                    }
                }
            }
        }
    }
}