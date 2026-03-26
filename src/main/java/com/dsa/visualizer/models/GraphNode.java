package com.dsa.visualizer.models;

import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import java.util.*;

public class GraphNode {
    public int id;
    public double x;
    public double y;
    public Circle circle;
    public Text label;
    public List<Edge> edges;
    public boolean visited;

    public GraphNode(int id, double x, double y) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.edges = new ArrayList<>();
        this.visited = false;
    }

    public void addEdge(Edge edge) {
        edges.add(edge);
    }
}