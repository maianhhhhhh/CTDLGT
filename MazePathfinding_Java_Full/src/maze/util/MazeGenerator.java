package maze.util;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import maze.model.Cell;
import maze.model.Maze;

/** Randomized depth-first maze carving, bảo đảm có đường từ Start đến End. */
public final class MazeGenerator {
    private MazeGenerator() { }

    public static Maze generate(int rows, int cols, boolean weighted) {
        Maze maze = new Maze(rows, cols);
        Random random = new Random();
        // Khởi tạo mọi ô (trừ Start/End) là tường.
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) maze.setTerrain(new Cell(r, c), Maze.WALL);
        }
        // Các ô đỉnh của mê cung có hàng/cột lẻ.
        Cell first = new Cell(1, 1);
        boolean[][] visited = new boolean[rows][cols];
        ArrayDeque<Cell> stack = new ArrayDeque<>();
        visited[1][1] = true;
        stack.push(first);
        int[][] dirs = {{-2, 0}, {2, 0}, {0, -2}, {0, 2}};
        while (!stack.isEmpty()) {
            Cell current = stack.peek();
            List<Cell> choices = new ArrayList<>();
            for (int[] direction : dirs) {
                int nr = current.row() + direction[0];
                int nc = current.col() + direction[1];
                if (nr > 0 && nc > 0 && nr < rows - 1 && nc < cols - 1 && !visited[nr][nc]) {
                    choices.add(new Cell(nr, nc));
                }
            }
            if (choices.isEmpty()) {
                stack.pop();
                continue;
            }
            Cell next = choices.get(random.nextInt(choices.size()));
            Cell middle = new Cell((current.row() + next.row()) / 2,
                                   (current.col() + next.col()) / 2);
            maze.setTerrain(middle, Maze.OPEN);
            maze.setTerrain(next, Maze.OPEN);
            visited[next.row()][next.col()] = true;
            stack.push(next);
        }
        // Phá một số bức tường nằm giữa các hành lang để tạo thêm nhánh vòng.
        // Nhờ đó nhiều đường từ S đến E có thể cùng tồn tại, giúp so sánh tốt hơn.
        for (int r = 1; r < rows - 1; r++) {
            for (int c = 1; c < cols - 1; c++) {
                Cell cell = new Cell(r, c);
                if (maze.terrainAt(cell) != Maze.WALL || random.nextDouble() >= 0.13) continue;
                boolean vertical = maze.isWalkable(new Cell(r - 1, c)) &&
                                   maze.isWalkable(new Cell(r + 1, c));
                boolean horizontal = maze.isWalkable(new Cell(r, c - 1)) &&
                                     maze.isWalkable(new Cell(r, c + 1));
                if (vertical || horizontal) maze.setTerrain(cell, Maze.OPEN);
            }
        }
        if (weighted) {
            for (int r = 1; r < rows - 1; r++) {
                for (int c = 1; c < cols - 1; c++) {
                    Cell cell = new Cell(r, c);
                    if (maze.isWalkable(cell) && random.nextDouble() < 0.18) {
                        maze.setTerrain(cell, Maze.WEIGHTED);
                    }
                }
            }
        }
        return maze;
    }
}
