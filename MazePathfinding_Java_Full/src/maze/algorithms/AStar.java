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

/** A*: f = g + h; h = Manhattan * 1, luôn không vượt chi phí tối ưu. */
public class AStar implements Pathfinding {
    private record Node(Cell cell, int g, int f, long seq) { }
    @Override public String name() { return "A*"; }

    private static int heuristic(Cell cell, Cell goal) {
        return Math.abs(cell.row() - goal.row()) + Math.abs(cell.col() - goal.col());
    }

    @Override public SearchResult search(Maze maze) {
        long begin = System.nanoTime();
        int[][] gScore = new int[maze.rows()][maze.cols()];
        boolean[][] closed = new boolean[maze.rows()][maze.cols()];
        for (int[] row : gScore) Arrays.fill(row, Integer.MAX_VALUE);
        Map<Cell, Cell> parent = new HashMap<>();
        List<Cell> expanded = new ArrayList<>();
        PriorityQueue<Node> open = new PriorityQueue<>(
            Comparator.comparingInt(Node::f).thenComparingInt(Node::g).thenComparingLong(Node::seq));
        Cell start = maze.start(), goal = maze.end();
        long seq = 0;
        gScore[start.row()][start.col()] = 0;
        open.add(new Node(start, 0, heuristic(start, goal), seq++));
        boolean found = false;

        while (!open.isEmpty()) {
            Node node = open.poll();
            Cell current = node.cell();
            int r = current.row(), c = current.col();
            if (closed[r][c] || node.g() != gScore[r][c]) continue;
            closed[r][c] = true;
            expanded.add(current);
            if (current.equals(goal)) { found = true; break; }
            for (Cell next : maze.neighbors(current)) {
                int tentativeG = gScore[r][c] + maze.enterCost(next);
                if (!closed[next.row()][next.col()] && tentativeG < gScore[next.row()][next.col()]) {
                    gScore[next.row()][next.col()] = tentativeG;
                    parent.put(next, current);
                    int f = tentativeG + heuristic(next, goal);
                    open.add(new Node(next, tentativeG, f, seq++));
                }
            }
        }
        return SearchUtils.result(name(), maze, parent, expanded, found, begin);
    }
}
