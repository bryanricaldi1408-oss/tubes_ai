import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class TestCaseGenerator {

    private final int m, n, p, h, t;
    private final Random rand = new Random();
    private int[][] grid; // 0=empty,1=house,2=tree
    private List<Point> houses = new ArrayList<>();
    private List<Point> trees = new ArrayList<>();
    private List<Point> stations = new ArrayList<>();

    static class Point {
        final int x, y;
        Point(int x, int y) { this.x = x; this.y = y; }
    }

    public TestCaseGenerator(int m, int n) {
        this.m = m;
        this.n = n;
        this.grid = new int[m][n];

        int total = m * n;

        this.h = (int)(total * (0.15 + rand.nextDouble() * 0.05));
        this.t = (int)(total * (0.20 + rand.nextDouble() * 0.05));
        int housesPerStation = 15 + rand.nextInt(11);
        this.p = (int)Math.ceil((double)h / housesPerStation);

        generateWithGuarantee();
    }

    private void generateWithGuarantee() {
        while (true) {
            clearGrid();
            placeRandom(houses, 1, h);
            placeRandom(trees, 2, t);
            placeStations();

            if (allHousesReachable()) {
                System.out.println("Valid testcase generated.");
                return;
            }

            System.out.println("Invalid (unreachable house) → regenerating...");
        }
    }

    private void clearGrid() {
        houses.clear();
        trees.clear();
        stations.clear();
        for (int i = 0; i < m; i++) Arrays.fill(grid[i], 0);
    }

    private void placeRandom(List<Point> list, int type, int count) {
        while (list.size() < count) {
            int x = rand.nextInt(m);
            int y = rand.nextInt(n);
            if (grid[x][y] == 0) {
                grid[x][y] = type;
                list.add(new Point(x + 1, y + 1)); // 1-based output
            }
        }
    }

    private void placeStations() {
        for (int i = 0; i < p; i++) {
            Point h = houses.get(rand.nextInt(houses.size()));
            stations.add(new Point(h.x, h.y));
        }
    }

    private boolean allHousesReachable() {
        boolean[][] vis = new boolean[m][n];
        Queue<int[]> q = new LinkedList<>();

        for (Point s : stations) {
            q.add(new int[]{s.x - 1, s.y - 1});
            vis[s.x - 1][s.y - 1] = true;
        }

        int[][] dir = {{1,0},{-1,0},{0,1},{0,-1}};

        while (!q.isEmpty()) {
            int[] c = q.poll();
            for (int[] d : dir) {
                int nx = c[0] + d[0];
                int ny = c[1] + d[1];
                if (nx>=0 && ny>=0 && nx<m && ny<n && grid[nx][ny]!=2 && !vis[nx][ny]) {
                    vis[nx][ny] = true;
                    q.add(new int[]{nx, ny});
                }
            }
        }

        // check all houses reachable
        for (Point h : houses) {
            if (!vis[h.x - 1][h.y - 1]) return false;
        }
        return true;
    }

    public void save(String filename) throws IOException {
        try (PrintWriter out = new PrintWriter(new FileWriter(filename))) {
            out.println(m + " " + n);
            out.println(p + " " + h + " " + t);
            for (Point h : houses) out.println(h.x + " " + h.y);
            for (Point r : trees) out.println(r.x + " " + r.y);
        }
    }

    public static void main(String[] args) throws IOException {
        generateAndSave(20, 20, "testcase_small.txt");
        generateAndSave(40, 40, "testcase_medium.txt");
        generateAndSave(80, 80, "testcase_large.txt");
    }

    private static void generateAndSave(int m, int n, String name) throws IOException {
        System.out.println("Generating " + name);
        TestCaseGenerator gen = new TestCaseGenerator(m, n);
        gen.save(name);
        System.out.printf("Saved %s (p=%d, h=%d, t=%d)\n\n", name, gen.p, gen.h, gen.t);
    }
}
