package com.dsa.visualizer.algorithms.pathfinding;

import com.dsa.visualizer.controllers.PathfinderController;
import com.dsa.visualizer.models.Cell;
import java.util.*;

public class Dijkstra {

    public boolean findPath(Cell[][] grid, Cell start, Cell end, PathfinderController controller)
            throws InterruptedException {
        PriorityQueue<Cell> pq = new PriorityQueue<>(Comparator.comparingInt(c -> c.distance));
        start.distance = 0;
        pq.add(start);

        while (!pq.isEmpty()) {
            Cell current = pq.poll();

            if (current.isVisited) continue;

            current.isVisited = true;
            controller.visualizeVisited(current);

            if (current == end) {
                reconstructPath(end, controller);
                return true;
            }

            for (Cell neighbor : controller.getNeighbors(current, grid)) {
                if (!neighbor.isVisited) {
                    int newDist = current.distance + 1;

                    if (newDist < neighbor.distance) {
                        neighbor.distance = newDist;
                        neighbor.parent = current;
                        pq.add(neighbor);
                    }
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