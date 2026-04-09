package com.dsa.visualizer.algorithms.graph;

import com.dsa.visualizer.controllers.GraphController;
import com.dsa.visualizer.models.Edge;
import com.dsa.visualizer.models.GraphNode;
import java.util.*;

public class KruskalMST {

    private int[] parent;
    private int[] rank;

    public void findMST(List<GraphNode> nodes, GraphController controller)
            throws InterruptedException {
        if (nodes.isEmpty()) return;

        int n = nodes.size();


        Map<Integer, Integer> idToIndex = new HashMap<>();
        for (int i = 0; i < n; i++) {
            idToIndex.put(nodes.get(i).id, i);
        }


        List<Edge> allEdges = new ArrayList<>();
        for (GraphNode node : nodes) {
            for (Edge edge : node.edges) {
                if (edge.line != null) {
                    allEdges.add(edge);
                }
            }
        }


        Set<javafx.scene.shape.Line> seen = new HashSet<>();
        List<Edge> uniqueEdges = new ArrayList<>();
        for (Edge e : allEdges) {
            if (seen.add(e.line)) {
                uniqueEdges.add(e);
            }
        }


        uniqueEdges.sort(Comparator.comparingInt(e -> e.weight));


        parent = new int[n];
        rank   = new int[n];
        for (int i = 0; i < n; i++) {
            parent[i] = i;
            rank[i]   = 0;
        }

        int edgesAdded = 0;
        for (Edge edge : uniqueEdges) {

            int fromIdx = idToIndex.get(edge.from.id);
            int toIdx   = idToIndex.get(edge.to.id);


            if (find(fromIdx) != find(toIdx)) {
                union(fromIdx, toIdx);
                controller.visualizeEdge(edge);
                edgesAdded++;

                if (edgesAdded == n - 1) break;
            }
        }


        controller.onKruskalComplete(edgesAdded, n - 1);
    }

    private int find(int x) {
        if (parent[x] != x) {
            parent[x] = find(parent[x]);
        }
        return parent[x];
    }

    private void union(int x, int y) {
        int rootX = find(x);
        int rootY = find(y);
        if (rootX == rootY) return;


        if (rank[rootX] < rank[rootY]) {
            parent[rootX] = rootY;
        } else if (rank[rootX] > rank[rootY]) {
            parent[rootY] = rootX;
        } else {
            parent[rootY] = rootX;
            rank[rootX]++;
        }
    }
}