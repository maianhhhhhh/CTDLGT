package maze.algorithms;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import maze.model.Cell;
import maze.model.Maze;
import maze.model.SearchResult;

/** DFS: Stack, tìm một đường đi nhưng không cam kết ngắn nhất. */
public class DFS implements Pathfinding {
    @Override public String name() { return "DFS"; }

    @Override public SearchResult search(Maze maze) {
        long begin = System.nanoTime();
        ArrayDeque<Cell> stack = new ArrayDeque<>();
        Set<Cell> seen = new HashSet<>();
        Map<Cell, Cell> parent = new HashMap<>();
        List<Cell> expanded = new ArrayList<>();
        stack.push(maze.start());
        seen.add(maze.start());
        boolean found = false;

        while (!stack.isEmpty()) {
            Cell current = stack.pop();
            expanded.add(current);
            if (current.equals(maze.end())) { found = true; break; }
            List<Cell> neighbors = maze.neighbors(current);
            // Đẩy theo chiều ngược, để ưu tiên: phải, xuống, trái, lên.
            for (int i = neighbors.size() - 1; i >= 0; i--) {
                Cell next = neighbors.get(i);
                if (seen.add(next)) {
                    parent.put(next, current);
                    stack.push(next);
                }
            }
        }
        return SearchUtils.result(name(), maze, parent, expanded, found, begin);
    }
}
