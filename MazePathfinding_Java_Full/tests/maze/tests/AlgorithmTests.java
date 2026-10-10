package maze.tests;

import java.util.List;
import maze.algorithms.AStar;
import maze.algorithms.AlgorithmFactory;
import maze.algorithms.BFS;
import maze.algorithms.Dijkstra;
import maze.algorithms.Pathfinding;
import maze.model.Cell;
import maze.model.Maze;
import maze.model.SearchResult;
import maze.util.MazeGenerator;

/** Bộ test độc lập, không cần JUnit hay Internet. */
public class AlgorithmTests {
    private static int passed;

    public static void main(String[] args) {
        testEmptyMaze();
        testWeightedDetour();
        testUnreachable();
        testRandomMaze();
        System.out.println("PASS: " + passed + " nhóm kiểm thử.");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static void validatePath(Maze maze, SearchResult result) {
        if (!result.found()) return;
        List<Cell> path = result.path();
        check(path.get(0).equals(maze.start()), "Đường đi phải bắt đầu từ Start");
        check(path.get(path.size() - 1).equals(maze.end()), "Đường đi phải kết thúc ở End");
        int cost = 0;
        for (int i = 1; i < path.size(); i++) {
            Cell previous = path.get(i - 1), next = path.get(i);
            int distance = Math.abs(previous.row() - next.row()) +
                           Math.abs(previous.col() - next.col());
            check(distance == 1, "Chỉ được đi 4 hướng, mỗi lần sang ô kề");
            check(maze.isWalkable(next), "Không được đi qua tường");
            cost += maze.enterCost(next);
        }
        check(cost == result.pathCost(), "Chi phí đường đi phải khớp");
    }

    private static void testEmptyMaze() {
        Maze maze = new Maze(5, 5);
        for (String name : AlgorithmFactory.names()) {
            SearchResult result = AlgorithmFactory.create(name).search(maze);
            check(result.found(), name + " không tìm thấy đường trong mê cung trống");
            validatePath(maze, result);
        }
        check(new BFS().search(maze).steps() == 4, "BFS cần đúng 4 bước");
        check(new Dijkstra().search(maze).pathCost() == 4, "Dijkstra cần cost=4");
        check(new AStar().search(maze).pathCost() == 4, "A* cần cost=4");
        passed++;
        System.out.println("[OK] Empty maze");
    }

    private static void testWeightedDetour() {
        Maze maze = new Maze(5, 7);
        maze.setStart(new Cell(2, 1));
        maze.setEnd(new Cell(2, 5));
        for (int c = 2; c <= 4; c++) maze.setTerrain(new Cell(2, c), Maze.WEIGHTED);
        SearchResult bfs = new BFS().search(maze);
        SearchResult dj = new Dijkstra().search(maze);
        SearchResult star = new AStar().search(maze);
        check(bfs.steps() == 4, "BFS ưu tiên ít bước nhất qua vùng trọng số");
        check(bfs.pathCost() == 16, "BFS đường thẳng phải có cost=16");
        check(dj.steps() == 6 && dj.pathCost() == 6, "Dijkstra phải đi vòng với cost=6");
        check(star.steps() == 6 && star.pathCost() == 6, "A* phải đi vòng với cost=6");
        for (String name : AlgorithmFactory.names()) validatePath(maze, AlgorithmFactory.create(name).search(maze));
        passed++;
        System.out.println("[OK] Weighted detour");
    }

    private static void testUnreachable() {
        Maze maze = new Maze(5, 5);
        maze.setTerrain(new Cell(1, 0), Maze.WALL);
        maze.setTerrain(new Cell(0, 1), Maze.WALL);
        maze.setTerrain(new Cell(1, 2), Maze.WALL);
        maze.setTerrain(new Cell(2, 1), Maze.WALL);
        for (String name : AlgorithmFactory.names()) {
            SearchResult result = AlgorithmFactory.create(name).search(maze);
            check(!result.found() && result.pathCost() == -1,
                    name + " phải trả về không có đường đi");
        }
        passed++;
        System.out.println("[OK] Unreachable maze");
    }

    private static void testRandomMaze() {
        for (int i = 0; i < 20; i++) {
            Maze maze = MazeGenerator.generate(19, 25, true);
            SearchResult dj = new Dijkstra().search(maze);
            SearchResult star = new AStar().search(maze);
            check(dj.found() && star.found(), "Mê cung ngẫu nhiên phải có đường đi");
            check(dj.pathCost() == star.pathCost(), "Dijkstra và A* phải có cost tối ưu giống nhau");
            validatePath(maze, dj);
            validatePath(maze, star);
        }
        passed++;
        System.out.println("[OK] Random maze (20 runs)");
    }
}
