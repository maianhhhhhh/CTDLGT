package maze.util;

import java.util.ArrayList;
import java.util.List;
import maze.algorithms.AlgorithmFactory;
import maze.algorithms.Pathfinding;
import maze.model.Maze;
import maze.model.SearchResult;

/** So sánh trên CÙNG bản đồ, không bao gồm thời gian vẽ hoạt ảnh Swing. */
public final class Benchmark {
    public record Row(String algorithm, int steps, int pathCost,
                      int expanded, double averageMs, boolean found) { }

    private Benchmark() { }

    public static List<Row> compare(Maze snapshot) {
        List<Row> output = new ArrayList<>();
        for (String name : AlgorithmFactory.names()) {
            Pathfinding algorithm = AlgorithmFactory.create(name);
            // Warm-up giảm độ lệch do JIT. Mỗi phép chạy hoàn toàn độc lập.
            for (int i = 0; i < 5; i++) algorithm.search(snapshot);
            int repeats = 30;
            double totalNs = 0;
            SearchResult result = null;
            for (int i = 0; i < repeats; i++) {
                result = algorithm.search(snapshot);
                totalNs += result.nanos();
            }
            output.add(new Row(name, result.steps(), result.pathCost(),
                    result.expandedNodes(), totalNs / repeats / 1_000_000.0, result.found()));
        }
        return output;
    }
}
