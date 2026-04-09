package com.dsa.visualizer.models;

import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Text;

public class Edge {
    public GraphNode from;
    public GraphNode to;
    public int weight;
    public Line    line;
    public Text    weightLabel;
    public Polygon arrowHead;         // ← add this field

    public Edge(GraphNode from, GraphNode to, int weight) {
        this.from   = from;
        this.to     = to;
        this.weight = weight;
    }
}
