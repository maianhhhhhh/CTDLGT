package maze.algorithms;

public final class AlgorithmFactory {
    private AlgorithmFactory() { }
    public static Pathfinding create(String name) {
        return switch (name) {
            case "BFS" -> new BFS();
            case "DFS" -> new DFS();
            case "Dijkstra" -> new Dijkstra();
            case "A*" -> new AStar();
            default -> throw new IllegalArgumentException("Thuật toán chưa hỗ trợ: " + name);
        };
    }
    public static String[] names() { return new String[] {"BFS", "DFS", "Dijkstra", "A*"}; }
}
