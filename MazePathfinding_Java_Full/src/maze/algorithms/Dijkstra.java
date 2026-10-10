package maze.algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import maze.model.Cell;
import maze.model.Maze;
import maze.model.SearchResult;

/** Dijkstra: tối ưu tổng chi phí đi qua từng ô (trọng số luôn dương). */
public class Dijkstra implements Pathfinding {
    private record Node(Cell cell, int cost, long seq) { }
    @Override public String name() { return "Dijkstra"; }

    @Override public SearchResult search(Maze maze) {
        long begin = System.nanoTime();
        int[][] dist = new int[maze.rows()][maze.cols()];
        boolean[][] closed = new boolean[maze.rows()][maze.cols()];
        for (int[] row : dist) Arrays.fill(row, Integer.MAX_VALUE);
        Map<Cell, Cell> parent = new HashMap<>();
        List<Cell> expanded = new ArrayList<>();
        PriorityQueue<Node> pq = new PriorityQueue<>(
            Comparator.comparingInt(Node::cost).thenComparingLong(Node::seq));
        long seq = 0;
        Cell start = maze.start();
        dist[start.row()][start.col()] = 0;
        pq.add(new Node(start, 0, seq++));
        boolean found = false;

        while (!pq.isEmpty()) {
            Node node = pq.poll();
            Cell current = node.cell();
            int r = current.row(), c = current.col();
            if (closed[r][c] || node.cost() != dist[r][c]) continue;
            closed[r][c] = true;
            expanded.add(current);
            if (current.equals(maze.end())) { found = true; break; }
            for (Cell next : maze.neighbors(current)) {
                int nextCost = dist[r][c] + maze.enterCost(next);
                if (!closed[next.row()][next.col()] && nextCost < dist[next.row()][next.col()]) {
                    dist[next.row()][next.col()] = nextCost;
                    parent.put(next, current);
                    pq.add(new Node(next, nextCost, seq++));
                }
            }
        }
        return SearchUtils.result(name(), maze, parent, expanded, found, begin);
    }
}
