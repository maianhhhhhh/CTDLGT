package maze.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;
import maze.algorithms.AlgorithmFactory;
import maze.model.Cell;
import maze.model.Maze;
import maze.model.SearchResult;
import maze.util.Benchmark;
import maze.util.MazeGenerator;

/** Giao diện Java Swing, liên kết mô hình - thuật toán - hoạt ảnh - thống kê. */
public class MainFrame extends JFrame {
    private static final Color DARK = new Color(29, 41, 60);
    private static final Color LIGHT = new Color(243, 246, 251);
    private static final String[] SIZES = {"15 x 15", "25 x 19", "35 x 25", "45 x 31"};
    private static final int[][] DIMENSIONS = {{15, 15}, {19, 25}, {25, 35}, {31, 45}};

    private Maze maze = new Maze(19, 25);
    private final MazePanel mazePanel = new MazePanel(maze);
    private final JComboBox<String> algorithmBox = new JComboBox<>(AlgorithmFactory.names());
    private final JComboBox<String> sizeBox = new JComboBox<>(SIZES);
    private final JComboBox<EditMode> editBox = new JComboBox<>(EditMode.values());
    private final JCheckBox weightedBox = new JCheckBox("Tạo ô chi phí 5 khi sinh mê cung", true);
    private final JSlider speed = new JSlider(1, 20, 8);
    private final JLabel status = new JLabel("Sẵn sàng. Hãy vẽ tường hoặc sinh mê cung.");
    private final JLabel resultLabel = new JLabel("Chưa có kết quả chạy.");
    private final DefaultTableModel stats = new DefaultTableModel(
            new Object[] {"Thuật toán", "Bước", "Chi phí", "Mở rộng", "TB (ms)"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final ComparisonChartPanel chart = new ComparisonChartPanel();
    private final JButton runButton = new JButton("▶ Chạy thuật toán");
    private final JButton pauseButton = new JButton("Tạm dừng");
    private final JButton compareButton = new JButton("So sánh cả 4");
    private final JButton randomButton = new JButton("Sinh mê cung");
    private final JButton emptyButton = new JButton("Mê cung trống");
    private final JButton clearButton = new JButton("Xóa hoạt ảnh");
    private Timer animation;
    private SearchResult currentResult;
    private int visitedIndex;
    private int pathIndex;
    private boolean comparisonBusy;

    public MainFrame() {
        super("CTDLGT - Mô phỏng và so sánh thuật toán tìm đường trong mê cung");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1050, 690));
        setSize(1280, 825);
        setLocationRelativeTo(null);
        initializeLayout();
        configureActions();
    }

    private void initializeLayout() {
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBackground(LIGHT);
        root.setBorder(BorderFactory.createEmptyBorder(0, 12, 12, 12));
        setContentPane(root);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(DARK);
        header.setBorder(BorderFactory.createEmptyBorder(17, 20, 17, 20));
        JLabel title = new JLabel("MAZE PATHFINDING  |  JAVA SWING");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        header.add(title, BorderLayout.WEST);
        JLabel subtitle = new JLabel("BFS · DFS · Dijkstra · A*");
        subtitle.setForeground(new Color(201, 215, 232));
        header.add(subtitle, BorderLayout.EAST);
        root.add(header, BorderLayout.NORTH);

        JPanel boardArea = new JPanel(new BorderLayout());
        boardArea.setBackground(Color.WHITE);
        boardArea.setBorder(BorderFactory.createLineBorder(new Color(218, 226, 236)));
        boardArea.add(mazePanel, BorderLayout.CENTER);
        JLabel guide = new JLabel("  Chuột trái: thao tác đã chọn    |    Chuột phải: xóa ô    |    S: Start, E: End, W: Tường, R: Xóa", SwingConstants.LEFT);
        guide.setForeground(new Color(72, 82, 101));
        guide.setBorder(BorderFactory.createEmptyBorder(6, 4, 10, 4));
        boardArea.add(guide, BorderLayout.SOUTH);
        root.add(boardArea, BorderLayout.CENTER);

        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setBackground(LIGHT);
        right.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 6));
        right.add(settingsSection());
        right.add(Box.createVerticalStrut(9));
        right.add(actionsSection());
        right.add(Box.createVerticalStrut(9));
        right.add(resultSection());
        right.add(Box.createVerticalStrut(9));
        right.add(comparisonSection());
        right.add(Box.createVerticalStrut(9));
        right.add(chartSection());
        right.add(Box.createVerticalGlue());
        JScrollPane scroll = new JScrollPane(right,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setPreferredSize(new Dimension(445, 680));
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        root.add(scroll, BorderLayout.EAST);

        status.setOpaque(true);
        status.setBackground(Color.WHITE);
        status.setForeground(DARK);
        status.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        root.add(status, BorderLayout.SOUTH);
    }

    private JPanel section(String title, Component contents) {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(218, 226, 236)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        JLabel heading = new JLabel(title);
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 14f));
        heading.setForeground(DARK);
        panel.add(heading, BorderLayout.NORTH);
        panel.add(contents, BorderLayout.CENTER);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1000));
        return panel;
    }

    private JPanel settingsSection() {
        JPanel fields = new JPanel(new GridLayout(3, 2, 8, 7));
        fields.setBackground(Color.WHITE);
        fields.add(new JLabel("Thuật toán"));
        fields.add(algorithmBox);
        fields.add(new JLabel("Kích thước"));
        fields.add(sizeBox);
        fields.add(new JLabel("Chế độ vẽ"));
        fields.add(editBox);
        sizeBox.setSelectedIndex(1);
        editBox.setSelectedItem(EditMode.WALL);

        JPanel content = new JPanel(new BorderLayout(0, 9));
        content.setBackground(Color.WHITE);
        content.add(fields, BorderLayout.NORTH);
        JPanel speedArea = new JPanel(new BorderLayout(5, 3));
        speedArea.setBackground(Color.WHITE);
        speedArea.add(weightedBox, BorderLayout.NORTH);
        JPanel sliderRow = new JPanel(new BorderLayout(8, 0));
        sliderRow.setBackground(Color.WHITE);
        sliderRow.add(new JLabel("Tốc độ"), BorderLayout.WEST);
        sliderRow.add(speed, BorderLayout.CENTER);
        speed.setOpaque(false);
        sliderRow.add(new JLabel("Nhanh →"), BorderLayout.EAST);
        speedArea.add(sliderRow, BorderLayout.SOUTH);
        content.add(speedArea, BorderLayout.SOUTH);
        return section("1. CÀI ĐẶT MÊ CUNG", content);
    }

    private JPanel actionsSection() {
        JPanel actions = new JPanel(new GridLayout(3, 2, 8, 8));
        actions.setBackground(Color.WHITE);
        actions.add(runButton);
        actions.add(pauseButton);
        actions.add(randomButton);
        actions.add(emptyButton);
        actions.add(clearButton);
        actions.add(compareButton);
        pauseButton.setEnabled(false);
        return section("2. THAO TÁC", actions);
    }

    private JPanel resultSection() {
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(Color.WHITE);
        resultLabel.setForeground(new Color(48, 72, 100));
        resultLabel.setFont(resultLabel.getFont().deriveFont(12f));
        content.add(resultLabel);
        return section("3. KẾT QUẢ CHẠY GẦN NHẤT", content);
    }

    private JPanel comparisonSection() {
        JTable table = new JTable(stats);
        table.getTableHeader().setReorderingAllowed(false);
        table.setRowHeight(25);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setPreferredSize(new Dimension(380, 124));
        return section("4. BẢNG SO SÁNH (30 LẦN CHẠY)", tableScroll);
    }

    private JPanel chartSection() {
        return section("5. BIỂU ĐỒ SO SÁNH", chart);
    }

    private void configureActions() {
        editBox.addActionListener(e -> mazePanel.setEditMode((EditMode) editBox.getSelectedItem()));
        sizeBox.addActionListener(e -> newEmptyMaze());
        runButton.addActionListener(e -> runSelectedAlgorithm());
        pauseButton.addActionListener(e -> togglePause());
        randomButton.addActionListener(e -> randomMaze());
        emptyButton.addActionListener(e -> newEmptyMaze());
        clearButton.addActionListener(e -> {
            stopAnimation();
            mazePanel.clearVisualization();
            resultLabel.setText("Đã xóa hoạt ảnh (giữ nguyên mê cung).");
            status.setText("Sẵn sàng.");
        });
        compareButton.addActionListener(e -> compareAll());
        mazePanel.setChangeListener(() -> {
            invalidateComparison();
            resultLabel.setText("Bản đồ đã thay đổi, hãy chạy thuật toán lại.");
            status.setText("Đã chỉnh sửa bản đồ.");
        });
        // Phím tắt điều chỉnh chế độ vẽ khi con trỏ đang ở vùng mê cung.
        mazePanel.getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(javax.swing.KeyStroke.getKeyStroke('s'), "start");
        mazePanel.getActionMap().put("start", new javax.swing.AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                editBox.setSelectedItem(EditMode.START);
            }
        });
        mazePanel.getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(javax.swing.KeyStroke.getKeyStroke('e'), "end");
        mazePanel.getActionMap().put("end", new javax.swing.AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                editBox.setSelectedItem(EditMode.END);
            }
        });
        mazePanel.getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(javax.swing.KeyStroke.getKeyStroke('w'), "wall");
        mazePanel.getActionMap().put("wall", new javax.swing.AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                editBox.setSelectedItem(EditMode.WALL);
            }
        });
        mazePanel.getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(javax.swing.KeyStroke.getKeyStroke('r'), "erase");
        mazePanel.getActionMap().put("erase", new javax.swing.AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                editBox.setSelectedItem(EditMode.ERASE);
            }
        });
    }

    private void stopAnimation() {
        if (animation != null) animation.stop();
        animation = null;
        pauseButton.setEnabled(false);
        pauseButton.setText("Tạm dừng");
        if (!comparisonBusy) mazePanel.setEditable(true);
    }

    private void invalidateComparison() {
        stats.setRowCount(0);
        chart.setRows(List.of());
    }

    private void installMaze(Maze replacement) {
        stopAnimation();
        maze = replacement;
        mazePanel.setMaze(maze);
        invalidateComparison();
        resultLabel.setText("Chưa có kết quả chạy.");
        status.setText("Đã tạo bản đồ " + maze.cols() + " × " + maze.rows() + ".");
    }

    private int[] selectedDimensions() {
        int ix = sizeBox.getSelectedIndex();
        return DIMENSIONS[Math.max(0, ix)];
    }

    private void newEmptyMaze() {
        if (comparisonBusy) return;
        int[] dim = selectedDimensions();
        installMaze(new Maze(dim[0], dim[1]));
    }

    private void randomMaze() {
        if (comparisonBusy) return;
        int[] dim = selectedDimensions();
        installMaze(MazeGenerator.generate(dim[0], dim[1], weightedBox.isSelected()));
        status.setText("Đã sinh mê cung có đường đi hợp lệ. Nhấn Chạy thuật toán.");
    }

    private void runSelectedAlgorithm() {
        if (comparisonBusy) return;
        stopAnimation();
        mazePanel.clearVisualization();
        String name = (String) algorithmBox.getSelectedItem();
        currentResult = AlgorithmFactory.create(name).search(maze.copy());
        visitedIndex = 0;
        pathIndex = 0;
        mazePanel.setEditable(false);
        status.setText("Đang mô phỏng " + name + "...");
        resultLabel.setText(name + ": Đang tô màu các ô đã duyệt...");
        animation = new Timer(30, e -> advanceAnimation());
        animation.start();
        pauseButton.setText("Tạm dừng");
        pauseButton.setEnabled(true);
    }

    private void advanceAnimation() {
        int amount = speed.getValue();
        if (visitedIndex < currentResult.visitOrder().size()) {
            for (int i = 0; i < amount && visitedIndex < currentResult.visitOrder().size(); i++) {
                mazePanel.showVisited(currentResult.visitOrder().get(visitedIndex++));
            }
        } else if (pathIndex < currentResult.path().size()) {
            for (int i = 0; i < Math.max(1, amount / 2) && pathIndex < currentResult.path().size(); i++) {
                mazePanel.showPath(currentResult.path().get(pathIndex++));
            }
        } else {
            finishAnimation();
            return;
        }
        mazePanel.repaint();
    }

    private void finishAnimation() {
        stopAnimation();
        if (currentResult.found()) {
            resultLabel.setText(String.format(Locale.US,
                "%s | %d bước | Cost %d | %d ô | %.4f ms",
                currentResult.algorithm(), currentResult.steps(), currentResult.pathCost(),
                currentResult.expandedNodes(), currentResult.milliseconds()));
            status.setText("Đã tìm thấy đường đi. Màu vàng là đường kết quả.");
        } else {
            resultLabel.setText(String.format(Locale.US,
                "%s | Không có đường | %d ô | %.4f ms",
                currentResult.algorithm(), currentResult.expandedNodes(), currentResult.milliseconds()));
            status.setText("Không tìm thấy đường đi từ S đến E.");
        }
    }

    private void togglePause() {
        if (animation == null) return;
        if (animation.isRunning()) {
            animation.stop();
            pauseButton.setText("Tiếp tục");
            status.setText("Đã tạm dừng hoạt ảnh.");
        } else {
            animation.start();
            pauseButton.setText("Tạm dừng");
            status.setText("Đang tiếp tục mô phỏng...");
        }
    }

    private void setComparisonBusy(boolean busy) {
        comparisonBusy = busy;
        runButton.setEnabled(!busy);
        compareButton.setEnabled(!busy);
        randomButton.setEnabled(!busy);
        emptyButton.setEnabled(!busy);
        clearButton.setEnabled(!busy);
        sizeBox.setEnabled(!busy);
        algorithmBox.setEnabled(!busy);
        editBox.setEnabled(!busy);
        weightedBox.setEnabled(!busy);
        mazePanel.setEditable(!busy);
    }

    private void compareAll() {
        if (comparisonBusy) return;
        stopAnimation();
        final Maze snapshot = maze.copy();
        setComparisonBusy(true);
        status.setText("Đang chạy kiểm thử cả 4 thuật toán (mỗi loại 30 lần)...");
        SwingWorker<List<Benchmark.Row>, Void> worker = new SwingWorker<>() {
            @Override protected List<Benchmark.Row> doInBackground() {
                return Benchmark.compare(snapshot);
            }
            @Override protected void done() {
                setComparisonBusy(false);
                try {
                    List<Benchmark.Row> values = get();
                    stats.setRowCount(0);
                    for (Benchmark.Row item : values) {
                        stats.addRow(new Object[] {
                            item.algorithm(), item.found() ? item.steps() : "—",
                            item.found() ? item.pathCost() : "—", item.expanded(),
                            String.format(Locale.US, "%.4f", item.averageMs())
                        });
                    }
                    chart.setRows(values);
                    status.setText("Đã so sánh 4 thuật toán trên cùng một mê cung. TB = trung bình 30 lần chạy.");
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    status.setText("So sánh bị gián đoạn.");
                } catch (ExecutionException ex) {
                    status.setText("Có lỗi khi so sánh.");
                    JOptionPane.showMessageDialog(MainFrame.this,
                            ex.getCause().toString(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
}
