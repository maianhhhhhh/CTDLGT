package maze.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import maze.model.Cell;
import maze.model.Maze;

/** Vùng hiển thị; không chứa thuật toán, chỉ vẽ và cập nhật bản đồ bằng chuột. */
public class MazePanel extends JPanel {
    private static final Color BACKGROUND = new Color(240, 244, 249);
    private static final Color EMPTY = Color.WHITE;
    private static final Color WALL = new Color(40, 51, 69);
    private static final Color WEIGHT = new Color(251, 203, 117);
    private static final Color EXPLORED = new Color(156, 205, 250);
    private static final Color PATH = new Color(249, 205, 70);
    private static final Color START = new Color(39, 174, 96);
    private static final Color END = new Color(222, 68, 68);
    private static final int PADDING = 18;

    private Maze maze;
    private EditMode editMode = EditMode.WALL;
    private boolean editable = true;
    private Runnable changeListener = () -> { };
    private final Set<Cell> shownVisited = new HashSet<>();
    private final Set<Cell> shownPath = new HashSet<>();

    public MazePanel(Maze maze) {
        this.maze = maze;
        setBackground(BACKGROUND);
        setPreferredSize(new Dimension(770, 640));
        MouseAdapter painter = new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { updateCell(e); }
            @Override public void mouseDragged(MouseEvent e) { updateCell(e); }
        };
        addMouseListener(painter);
        addMouseMotionListener(painter);
    }

    public void setMaze(Maze maze) {
        this.maze = maze;
        clearVisualization();
    }
    public void setEditMode(EditMode mode) { this.editMode = mode; }
    public void setEditable(boolean editable) { this.editable = editable; }
    public void setChangeListener(Runnable callback) { changeListener = callback; }

    public void clearVisualization() {
        shownVisited.clear();
        shownPath.clear();
        repaint();
    }
    public void showVisited(Cell cell) { shownVisited.add(cell); }
    public void showPath(Cell cell) { shownPath.add(cell); }

    private record GridGeometry(int left, int top, int cell) { }

    private GridGeometry geometry() {
        int size = Math.max(1, Math.min((getWidth() - PADDING * 2) / maze.cols(),
                                        (getHeight() - PADDING * 2) / maze.rows()));
        int left = (getWidth() - maze.cols() * size) / 2;
        int top = (getHeight() - maze.rows() * size) / 2;
        return new GridGeometry(left, top, size);
    }

    private void updateCell(MouseEvent e) {
        if (!editable) return;
        GridGeometry g = geometry();
        int col = Math.floorDiv(e.getX() - g.left(), g.cell());
        int row = Math.floorDiv(e.getY() - g.top(), g.cell());
        if (!maze.inBounds(row, col)) return;
        Cell cell = new Cell(row, col);
        boolean rightClick = SwingUtilities.isRightMouseButton(e) ||
                             (e.getModifiersEx() & MouseEvent.BUTTON3_DOWN_MASK) != 0;
        boolean changed;
        if (rightClick) changed = maze.setTerrain(cell, Maze.OPEN);
        else changed = switch (editMode) {
            case WALL -> maze.setTerrain(cell, Maze.WALL);
            case WEIGHT -> maze.setTerrain(cell, Maze.WEIGHTED);
            case START -> maze.setStart(cell);
            case END -> maze.setEnd(cell);
            case ERASE -> maze.setTerrain(cell, Maze.OPEN);
        };
        if (changed) {
            clearVisualization();
            changeListener.run();
            repaint();
        }
    }

    @Override protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        GridGeometry geo = geometry();
        int size = geo.cell();
        for (int r = 0; r < maze.rows(); r++) {
            for (int c = 0; c < maze.cols(); c++) {
                Cell cell = new Cell(r, c);
                Color color = EMPTY;
                if (maze.terrainAt(cell) == Maze.WALL) color = WALL;
                else if (maze.terrainAt(cell) == Maze.WEIGHTED) color = WEIGHT;
                if (shownVisited.contains(cell)) color = EXPLORED;
                if (shownPath.contains(cell)) color = PATH;
                if (cell.equals(maze.start())) color = START;
                if (cell.equals(maze.end())) color = END;
                int x = geo.left() + c * size;
                int y = geo.top() + r * size;
                g.setColor(color);
                g.fillRoundRect(x + 1, y + 1, Math.max(1, size - 2), Math.max(1, size - 2), 5, 5);
                if (size >= 15 && (cell.equals(maze.start()) || cell.equals(maze.end()))) {
                    g.setColor(Color.WHITE);
                    g.setFont(getFont().deriveFont(Font.BOLD, Math.min(14, size * 0.48f)));
                    String label = cell.equals(maze.start()) ? "S" : "E";
                    FontMetrics fm = g.getFontMetrics();
                    g.drawString(label, x + (size - fm.stringWidth(label)) / 2,
                                 y + (size + fm.getAscent() - fm.getDescent()) / 2);
                }
            }
        }
        g.dispose();
    }
}
