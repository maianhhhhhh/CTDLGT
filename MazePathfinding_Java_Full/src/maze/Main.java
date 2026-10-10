package maze;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import maze.ui.MainFrame;

/** Điểm vào chương trình - luôn tạo giao diện trên Swing Event Dispatch Thread. */
public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Vẫn chạy với giao diện mặc định nếu máy không hỗ trợ theme hệ thống.
        }
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
