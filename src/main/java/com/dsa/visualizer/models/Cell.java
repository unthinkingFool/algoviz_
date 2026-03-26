package com.dsa.visualizer.models;

public class Cell {
    public int row;
    public int col;
    public boolean isWall;
    public boolean isStart;
    public boolean isEnd;
    public boolean isVisited;
    public boolean isPath;
    public int distance;
    public int fScore;
    public Cell parent;

    public Cell(int row, int col) {
        this.row = row;
        this.col = col;
        this.isWall = false;
        this.isStart = false;
        this.isEnd = false;
        this.isVisited = false;
        this.isPath = false;
        this.distance = Integer.MAX_VALUE;
        this.fScore = Integer.MAX_VALUE;
        this.parent = null;
    }

    public void reset() {
        this.isVisited = false;
        this.isPath = false;
        this.distance = Integer.MAX_VALUE;
        this.fScore = Integer.MAX_VALUE;
        this.parent = null;
    }
}