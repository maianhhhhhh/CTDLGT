package maze.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.List;
import javax.swing.JPanel;
import maze.util.Benchmark;

/** Biểu đồ so sánh số ô mở rộng, không cần thư viện ngoài. */
public class ComparisonChartPanel extends JPanel {
    private List<Benchmark.Row> rows = List.of();
    public ComparisonChartPanel() {
        setPreferredSize(new Dimension(360, 175));
        setBackground(Color.WHITE);
    }
    public void setRows(List<Benchmark.Row> rows) {
        this.rows = List.copyOf(rows);
        repaint();
    }
    @Override protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(58, 66, 82));
        g.setFont(getFont().deriveFont(Font.BOLD, 12f));
        g.drawString("SỐ Ô ĐƯỢC MỞ RỘNG", 12, 19);
        if (rows.isEmpty()) {
            g.setFont(getFont().deriveFont(Font.PLAIN, 12f));
            g.setColor(new Color(130, 140, 154));
            g.drawString("Nhấn So sánh để hiển thị biểu đồ.", 12, 62);
            g.dispose();
            return;
        }
        int max = rows.stream().mapToInt(Benchmark.Row::expanded).max().orElse(1);
        int barLeft = 90;
        int barWidth = Math.max(30, getWidth() - 166);
        Color[] colors = {new Color(48, 130, 224), new Color(120, 94, 211),
                          new Color(34, 164, 142), new Color(227, 139, 47)};
        for (int i = 0; i < rows.size(); i++) {
            Benchmark.Row entry = rows.get(i);
            int y = 36 + i * 32;
            g.setFont(getFont().deriveFont(Font.PLAIN, 12f));
            g.setColor(new Color(40, 53, 67));
            g.drawString(entry.algorithm(), 12, y + 14);
            g.setColor(new Color(230, 235, 244));
            g.fillRoundRect(barLeft, y, barWidth, 17, 8, 8);
            g.setColor(colors[i % colors.length]);
            g.fillRoundRect(barLeft, y, max == 0 ? 0 :
                            (int) ((long) entry.expanded() * barWidth / Math.max(1, max)), 17, 8, 8);
            g.setColor(new Color(40, 53, 67));
            g.drawString(Integer.toString(entry.expanded()), barLeft + barWidth + 8, y + 14);
        }
        g.dispose();
    }
}
