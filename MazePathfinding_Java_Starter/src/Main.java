import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;

public class Main {
    static final int ROWS = 18, COLS = 24, CELL = 28;
    static final boolean[][] walls = new boolean[ROWS][COLS];
    static final boolean[][] explored = new boolean[ROWS][COLS];
    static final boolean[][] pathCells = new boolean[ROWS][COLS];
    static final Point START = new Point(1, 1);
    static final Point END = new Point(COLS - 2, ROWS - 2);
    static JPanel board;
    static JLabel status;
    static javax.swing.Timer timer;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::createGUI);
    }

    static void createGUI() {
        JFrame frame = new JFrame("Maze Pathfinding - Java Swing");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        board = new JPanel() {
            @Override public Dimension getPreferredSize() {
                return new Dimension(COLS * CELL, ROWS * CELL);
            }
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                for (int r = 0; r < ROWS; r++) {
                    for (int c = 0; c < COLS; c++) {
                        Color color = Color.WHITE;
                        if (walls[r][c]) color = new Color(55, 65, 81);
                        else if (explored[r][c]) color = new Color(147, 197, 253);
                        if (pathCells[r][c]) color = new Color(250, 204, 21);
                        if (START.x == c && START.y == r) color = new Color(34, 197, 94);
                        if (END.x == c && END.y == r) color = new Color(239, 68, 68);
                        g.setColor(color);
                        g.fillRect(c * CELL, r * CELL, CELL, CELL);
                        g.setColor(new Color(220, 220, 220));
                        g.drawRect(c * CELL, r * CELL, CELL, CELL);
                    }
                }
            }
        };
        board.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                int c = e.getX() / CELL, r = e.getY() / CELL;
                if (r < 0 || r >= ROWS || c < 0 || c >= COLS) return;
                if ((c == START.x && r == START.y) || (c == END.x && r == END.y)) return;
                if (timer != null) timer.stop();
                walls[r][c] = !SwingUtilities.isRightMouseButton(e);
                clearSearch();
                board.repaint();
            }
        });
        JButton run = new JButton("Run BFS");
        run.addActionListener(e -> runBFS());
        JButton reset = new JButton("Reset");
        reset.addActionListener(e -> {
            if (timer != null) timer.stop();
            for (int r = 0; r < ROWS; r++)
                for (int c = 0; c < COLS; c++) walls[r][c] = false;
            clearSearch();
            status.setText("Left click: add wall | Right click: erase");
            board.repaint();
        });
        status = new JLabel("Left click: add wall | Right click: erase");
        JPanel controls = new JPanel();
        controls.add(run); controls.add(reset); controls.add(status);
        frame.add(board, BorderLayout.CENTER);
        frame.add(controls, BorderLayout.SOUTH);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    static void clearSearch() {
        for (int r = 0; r < ROWS; r++)
            for (int c = 0; c < COLS; c++) {
                explored[r][c] = false;
                pathCells[r][c] = false;
            }
    }

    static void runBFS() {
        if (timer != null) timer.stop();
        clearSearch();
        boolean[][] seen = new boolean[ROWS][COLS];
        Point[][] parent = new Point[ROWS][COLS];
        Queue<Point> q = new ArrayDeque<>();
        List<Point> order = new ArrayList<>();
        q.add(START);
        seen[START.y][START.x] = true;
        int[][] dirs = {{1,0}, {-1,0}, {0,1}, {0,-1}};
        long t0 = System.nanoTime();
        while (!q.isEmpty()) {
            Point p = q.remove();
            order.add(p);
            if (p.equals(END)) break;
            for (int[] d : dirs) {
                int nx = p.x + d[0], ny = p.y + d[1];
                if (nx >= 0 && nx < COLS && ny >= 0 && ny < ROWS
                        && !walls[ny][nx] && !seen[ny][nx]) {
                    seen[ny][nx] = true;
                    parent[ny][nx] = p;
                    q.add(new Point(nx, ny));
                }
            }
        }
        double ms = (System.nanoTime() - t0) / 1_000_000.0;
        List<Point> path = new ArrayList<>();
        if (seen[END.y][END.x]) {
            for (Point p = END; p != null; p = parent[p.y][p.x]) path.add(p);
            Collections.reverse(path);
        }
        final int[] i = {0};
        timer = new javax.swing.Timer(25, e -> {
            if (i[0] < order.size()) {
                Point p = order.get(i[0]++);
                explored[p.y][p.x] = true;
                board.repaint();
            } else {
                timer.stop();
                for (Point p : path) pathCells[p.y][p.x] = true;
                board.repaint();
                status.setText(path.isEmpty()
                    ? String.format("No path | Visited: %d | Time: %.3f ms", order.size(), ms)
                    : String.format("Steps: %d | Visited: %d | Time: %.3f ms",
                                    path.size() - 1, order.size(), ms));
            }
        });
        timer.start();
    }
}
