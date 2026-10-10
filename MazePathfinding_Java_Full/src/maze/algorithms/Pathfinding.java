package maze.algorithms;

import maze.model.Maze;
import maze.model.SearchResult;

/** Giao diện thống nhất: mọi thuật toán đều trả cùng một định dạng kết quả. */
public interface Pathfinding {
    String name();
    SearchResult search(Maze maze);
}
