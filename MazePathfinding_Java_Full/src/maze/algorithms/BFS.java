package maze.algorithms;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import maze.model.Cell;
import maze.model.Maze;
import maze.model.SearchResult;

/** BFS: Queue, bảo đảm ít bước nhất khi chỉ đi 4 hướng, không tính trọng số. */
public class BFS implements Pathfinding {
    @Override public String name() { return "BFS"; }

    @Override public SearchResult search(Maze maze) {
        long begin = System.nanoTime();
        Queue<Cell> queue = new ArrayDeque<>();
        Set<Cell> seen = new HashSet<>();
        Map<Cell, Cell> parent = new HashMap<>();
        List<Cell> expanded = new ArrayList<>();
        queue.add(maze.start());
        seen.add(maze.start());
        boolean found = false;

        while (!queue.isEmpty()) {
            Cell current = queue.remove();
            expanded.add(current);
            if (current.equals(maze.end())) { found = true; break; }
            for (Cell next : maze.neighbors(current)) {
                if (seen.add(next)) {
                    parent.put(next, current);
                    queue.add(next);
                }
            }
        }
        return SearchUtils.result(name(), maze, parent, expanded, found, begin);
    }
}
