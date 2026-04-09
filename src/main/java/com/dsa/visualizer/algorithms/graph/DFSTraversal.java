package com.dsa.visualizer.algorithms.graph;

import com.dsa.visualizer.controllers.GraphController;
import com.dsa.visualizer.models.Edge;
import com.dsa.visualizer.models.GraphNode;
import java.util.List;

public class DFSTraversal {

    public void traverse(List<GraphNode> nodes, GraphController controller)
            throws InterruptedException {
        if (nodes.isEmpty()) return;


        for (GraphNode node : nodes) {
            if (!node.visited) {
                dfs(node, controller);
            }
        }
    }

    private void dfs(GraphNode node, GraphController controller) throws InterruptedException {
        node.visited = true;
        controller.visualizeNodeVisit(node);

        for (Edge edge : node.edges) {
            if (!edge.to.visited) {
                controller.visualizeEdge(edge);
                dfs(edge.to, controller);
            }
        }
    }
}