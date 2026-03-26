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

        // Build a mapping from node.id -> index (0..n-1)
        // This is necessary because node IDs may not start at 0 or be contiguous
        // after nodes are cleared and re-added (nodeIdCounter keeps incrementing).
        Map<Integer, Integer> idToIndex = new HashMap<>();
        for (int i = 0; i < n; i++) {
            idToIndex.put(nodes.get(i).id, i);
        }

        // Collect unique undirected edges.
        // addEdge() adds TWO Edge objects per connection: one on each node.
        // The reverse edge has line=null (it's a lightweight back-reference).
        // We must keep only the Edge that has a non-null line (the "visual" edge).
        List<Edge> allEdges = new ArrayList<>();
        for (GraphNode node : nodes) {
            for (Edge edge : node.edges) {
                if (edge.line != null) {   // only the original, visualizable edge
                    allEdges.add(edge);
                }
            }
        }

        // Remove duplicates — addEdge adds the same visual edge to from.edges only,
        // but generateRandomGraph can call addEdge multiple times; use a Set on the
        // line object as identity to deduplicate.
        Set<javafx.scene.shape.Line> seen = new HashSet<>();
        List<Edge> uniqueEdges = new ArrayList<>();
        for (Edge e : allEdges) {
            if (seen.add(e.line)) {
                uniqueEdges.add(e);
            }
        }

        // Sort edges by weight ascending (Kruskal's greedy step)
        uniqueEdges.sort(Comparator.comparingInt(e -> e.weight));

        // Union-Find initialisation using contiguous indices (not raw node IDs)
        parent = new int[n];
        rank   = new int[n];
        for (int i = 0; i < n; i++) {
            parent[i] = i;
            rank[i]   = 0;
        }

        int edgesAdded = 0;
        for (Edge edge : uniqueEdges) {
            // Convert node IDs to indices
            int fromIdx = idToIndex.get(edge.from.id);
            int toIdx   = idToIndex.get(edge.to.id);

            // Only add edge if it connects two different components
            if (find(fromIdx) != find(toIdx)) {
                union(fromIdx, toIdx);
                controller.visualizeEdge(edge);   // edge.line is guaranteed non-null
                edgesAdded++;

                if (edgesAdded == n - 1) break;   // MST complete
            }
        }

        // Inform the controller whether we found a full spanning tree or not
        controller.onKruskalComplete(edgesAdded, n - 1);
    }

    private int find(int x) {
        if (parent[x] != x) {
            parent[x] = find(parent[x]);  // path compression
        }
        return parent[x];
    }

    private void union(int x, int y) {
        int rootX = find(x);
        int rootY = find(y);
        if (rootX == rootY) return;

        // Union by rank
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