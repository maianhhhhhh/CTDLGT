package maze.algorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import maze.model.Cell;
import maze.model.Maze;
import maze.model.SearchResult;

final class SearchUtils {
    private SearchUtils() { }

    static SearchResult result(String algorithm, Maze maze, Map<Cell, Cell> parent,
                               List<Cell> visited, boolean found, long beginNanos) {
        List<Cell> path = new ArrayList<>();
        if (found) {
            Cell current = maze.end();
            while (current != null) {
                path.add(current);
                if (current.equals(maze.start())) break;
                current = parent.get(current);
            }
            Collections.reverse(path);
        }
        int cost = -1;
        if (found) {
            cost = 0;
            for (int i = 1; i < path.size(); i++) cost += maze.enterCost(path.get(i));
        }
        long elapsed = System.nanoTime() - beginNanos;
        return new SearchResult(algorithm, path, visited, cost, elapsed);
    }
}
