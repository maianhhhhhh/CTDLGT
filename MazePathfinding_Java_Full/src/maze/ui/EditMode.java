package maze.ui;

public enum EditMode {
    WALL("Vẽ tường"), WEIGHT("Vùng chi phí 5"), START("Đặt Start"),
    END("Đặt End"), ERASE("Xóa ô");

    private final String label;
    EditMode(String label) { this.label = label; }
    @Override public String toString() { return label; }
}
