import java.io.*;
import java.util.*;

public class GA{

    static class Point {
        int row, col;
        Point(int r, int c) { row = r; col = c; }
    }

    static class Node {
        int row, col;
        double distance;
        Node(int r, int c, double d) { row = r; col = c; distance = d; }
    }

    static int[][] grid;
    static int panjangBoard, lebarBoard;
    static int p, h, t;
    static List<Point> houses = new ArrayList<>();
    static List<Point> trees = new ArrayList<>();
    static Map<String, Double> distanceCache = new HashMap<>();
    static Random rnd = new Random();

    // GA Parameters
    static int POP_SIZE = 50;
    static int MAX_GEN = 100;
    static double CROSS_RATE = 0.8;
    static double MUT_RATE = 0.05;

    static class Individual {
        List<Point> fireStations;
        double fitness;

        Individual(List<Point> stations) {
            this.fireStations = stations;
            this.fitness = fitnessFunction(stations);
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Usage: java GA_FireStation input.txt maxGen");
            return;
        }

        String filename = args[0];
        MAX_GEN = Integer.parseInt(args[1]);

        readInput(filename);
        Individual best = geneticAlgorithm();

        // OUTPUT
        System.out.printf("%d %.5f%n", p, best.fitness);
        for (Point fs : best.fireStations) {
            System.out.println((fs.row) + " " + (fs.col));
        }
    }

    // ================== INPUT ===================
    static void readInput(String filename) throws FileNotFoundException {
        Scanner sc = new Scanner(new File(filename));
        panjangBoard = sc.nextInt();
        lebarBoard = sc.nextInt();
        grid = new int[panjangBoard][lebarBoard];

        p = sc.nextInt();
        h = sc.nextInt();
        t = sc.nextInt();

        for (int i = 0; i < h; i++) {
            int x = sc.nextInt() - 1;
            int y = sc.nextInt() - 1;
            houses.add(new Point(x, y));
            grid[x][y] = 1;
        }

        for (int i = 0; i < t; i++) {
            int x = sc.nextInt() - 1;
            int y = sc.nextInt() - 1;
            trees.add(new Point(x, y));
            grid[x][y] = 2;
        }
        sc.close();
    }

    // ================== GENETIC ALGORITHM ===================
    static Individual geneticAlgorithm() {
        List<Individual> population = initPopulation();
        Individual best = getBest(population);

        for (int gen = 0; gen < MAX_GEN; gen++) {
            List<Individual> newPop = new ArrayList<>();

            // Elitism
            newPop.add(best);

            while (newPop.size() < POP_SIZE) {
                Individual parent1 = tournamentSelection(population);
                Individual parent2 = tournamentSelection(population);

                List<Point> childStations;
                if (rnd.nextDouble() < CROSS_RATE)
                    childStations = crossover(parent1.fireStations, parent2.fireStations);
                else
                    childStations = new ArrayList<>(parent1.fireStations);

                if (rnd.nextDouble() < MUT_RATE)
                    mutate(childStations);

                newPop.add(new Individual(childStations));
            }

            population = newPop;
            Individual currentBest = getBest(population);
            if (currentBest.fitness < best.fitness)
                best = currentBest;
        }
        return best;
    }

    static List<Individual> initPopulation() {
        List<Individual> pop = new ArrayList<>();
        for (int i = 0; i < POP_SIZE; i++) {
            List<Point> stations = generateRandomStations();
            pop.add(new Individual(stations));
        }
        return pop;
    }

    static Individual tournamentSelection(List<Individual> pop) {
        int k = 3;
        Individual best = pop.get(rnd.nextInt(pop.size()));
        for (int i = 1; i < k; i++) {
            Individual challenger = pop.get(rnd.nextInt(pop.size()));
            if (challenger.fitness < best.fitness)
                best = challenger;
        }
        return best;
    }

    static List<Point> crossover(List<Point> a, List<Point> b) {
        List<Point> child = new ArrayList<>();
        for (int i = 0; i < p; i++) {
            Point chosen = rnd.nextBoolean() ? a.get(i) : b.get(i);
            if (!contains(child, chosen))
                child.add(new Point(chosen.row, chosen.col));
            else
                child.add(randomEmptyPoint());
        }
        return child;
    }

    static void mutate(List<Point> stations) {
        int idx = rnd.nextInt(stations.size());
        stations.set(idx, randomEmptyPoint());
    }

    static Individual getBest(List<Individual> pop) {
        return pop.stream().min(Comparator.comparingDouble(ind -> ind.fitness)).get();
    }

    // ================== UTILITY ===================
    static List<Point> generateRandomStations() {
        List<Point> stations = new ArrayList<>();
        while (stations.size() < p) {
            Point pt = randomEmptyPoint();
            if (!contains(stations, pt)) stations.add(pt);
        }
        return stations;
    }

    static Point randomEmptyPoint() {
        int x, y;
        do {
            x = rnd.nextInt(panjangBoard);
            y = rnd.nextInt(lebarBoard);
        } while (grid[x][y] != 0);
        return new Point(x, y);
    }

    static boolean contains(List<Point> list, Point p) {
        for (Point q : list)
            if (q.row == p.row && q.col == p.col)
                return true;
        return false;
    }

    // ================== FITNESS ===================
    static double fitnessFunction(List<Point> fireStations) {
        double totalDistance = 0.0;
        for (Point house : houses) {
            double best = Double.MAX_VALUE;
            for (Point s : fireStations) {
                double d = bfs(house, s);
                if (d < best) best = d;
            }
            if (best == Double.MAX_VALUE)
                return Double.POSITIVE_INFINITY;
            totalDistance += best;
        }
        return totalDistance / houses.size();
    }

    // ================== BFS ===================
    static double bfs(Point house, Point firestation) {
        int[][] dir = {{1,0},{-1,0},{0,1},{0,-1}};
        String key = house.row + "," + house.col + "-" + firestation.row + "," + firestation.col;
        if (distanceCache.containsKey(key))
            return distanceCache.get(key);

        Queue<Node> queue = new LinkedList<>();
        boolean[][] visited = new boolean[panjangBoard][lebarBoard];
        queue.add(new Node(house.row, house.col, 0));
        visited[house.row][house.col] = true;

        while (!queue.isEmpty()) {
            Node cur = queue.poll();
            if (cur.row == firestation.row && cur.col == firestation.col) {
                distanceCache.put(key, cur.distance);
                return cur.distance;
            }
            for (int[] d : dir) {
                int nx = cur.row + d[0];
                int ny = cur.col + d[1];
                if (isNotOutOfBound(nx, ny) && grid[nx][ny] != 2 && !visited[nx][ny]) {
                    visited[nx][ny] = true;
                    queue.add(new Node(nx, ny, cur.distance + 1));
                }
            }
        }
        return Double.MAX_VALUE;
    }

    static boolean isNotOutOfBound(int x, int y) {
        return x >= 0 && y >= 0 && x < panjangBoard && y < lebarBoard;
    }
}
