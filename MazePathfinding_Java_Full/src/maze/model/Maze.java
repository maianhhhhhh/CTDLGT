package maze.model;

import java.util.ArrayList;
import java.util.List;

/** Quản lý bản đồ; thuật toán không được thay đổi dữ liệu bản đồ. */
public class Maze {
    public static final int OPEN = 0;
    public static final int WALL = 1;
    public static final int WEIGHTED = 2;
    public static final int WEIGHTED_COST = 5;

    private final int rows;
    private final int cols;
    private final int[][] terrain;
    private Cell start;
    private Cell end;

    public Maze(int rows, int cols) {
        if (rows < 5 || cols < 5) {
            throw new IllegalArgumentException("Mê cung cần ít nhất 5 hàng và 5 cột.");
        }
        this.rows = rows;
        this.cols = cols;
        this.terrain = new int[rows][cols];
        this.start = new Cell(1, 1);
        this.end = new Cell(rows - 2, cols - 2);
    }

    public int rows() { return rows; }
    public int cols() { return cols; }
    public Cell start() { return start; }
    public Cell end() { return end; }

    public boolean inBounds(int row, int col) {
        return row >= 0 && row < rows && col >= 0 && col < cols;
    }

    public int terrainAt(Cell cell) {
        return terrain[cell.row()][cell.col()];
    }

    public boolean isWalkable(Cell cell) {
        return inBounds(cell.row(), cell.col()) && terrainAt(cell) != WALL;
    }

    public int enterCost(Cell cell) {
        return terrainAt(cell) == WEIGHTED ? WEIGHTED_COST : 1;
    }

    public boolean setTerrain(Cell cell, int type) {
        if (!inBounds(cell.row(), cell.col()) || cell.equals(start) || cell.equals(end)) return false;
        if (type < OPEN || type > WEIGHTED || terrainAt(cell) == type) return false;
        terrain[cell.row()][cell.col()] = type;
        return true;
    }

    public boolean setStart(Cell cell) {
        if (!inBounds(cell.row(), cell.col()) || cell.equals(end) || cell.equals(start)) return false;
        start = cell;
        terrain[cell.row()][cell.col()] = OPEN;
        return true;
    }

    public boolean setEnd(Cell cell) {
        if (!inBounds(cell.row(), cell.col()) || cell.equals(start) || cell.equals(end)) return false;
        end = cell;
        terrain[cell.row()][cell.col()] = OPEN;
        return true;
    }

    /** Trật tự ổn định: phải -> xuống -> trái -> lên. Không đi chéo. */
    public List<Cell> neighbors(Cell cell) {
        int[][] dirs = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}};
        List<Cell> output = new ArrayList<>(4);
        for (int[] direction : dirs) {
            Cell next = new Cell(cell.row() + direction[0], cell.col() + direction[1]);
            if (isWalkable(next)) output.add(next);
        }
        return output;
    }

    public Maze copy() {
        Maze clone = new Maze(rows, cols);
        for (int r = 0; r < rows; r++) {
            System.arraycopy(terrain[r], 0, clone.terrain[r], 0, cols);
        }
        clone.start = start;
        clone.end = end;
        return clone;
    }
}
