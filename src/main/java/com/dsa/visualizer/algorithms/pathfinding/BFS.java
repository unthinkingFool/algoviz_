package com.dsa.visualizer.algorithms.pathfinding;

import com.dsa.visualizer.controllers.PathfinderController;
import com.dsa.visualizer.models.Cell;
import java.util.*;

public class BFS {

    public boolean findPath(Cell[][] grid, Cell start, Cell end, PathfinderController controller)
            throws InterruptedException {
        Queue<Cell> queue = new LinkedList<>();
        queue.add(start);
        start.isVisited = true;

        while (!queue.isEmpty()) {
            Cell current = queue.poll();
            controller.visualizeVisited(current);

            if (current == end) {
                reconstructPath(end, controller);
                return true;
            }

            for (Cell neighbor : controller.getNeighbors(current, grid)) {
                if (!neighbor.isVisited) {
                    neighbor.isVisited = true;
                    neighbor.parent = current;
                    queue.add(neighbor);
                }
            }
        }

        return false;
    }

    private void reconstructPath(Cell end, PathfinderController controller)
            throws InterruptedException {
        Cell current = end.parent;

        while (current != null && !current.isStart) {
            current.isPath = true;
            controller.visualizePath(current);
            current = current.parent;
        }
    }
}