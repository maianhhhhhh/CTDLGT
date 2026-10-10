package maze.model;

import java.util.List;

/** visitOrder là thứ tự các ô MỞ RỘNG (đưa ra xử lý), không phải hoạt ảnh. */
public record SearchResult(String algorithm, List<Cell> path,
                           List<Cell> visitOrder, int pathCost, long nanos) {
    public SearchResult {
        path = List.copyOf(path);
        visitOrder = List.copyOf(visitOrder);
    }
    public boolean found() { return !path.isEmpty(); }
    public int steps() { return found() ? path.size() - 1 : -1; }
    public int expandedNodes() { return visitOrder.size(); }
    public double milliseconds() { return nanos / 1_000_000.0; }
}
