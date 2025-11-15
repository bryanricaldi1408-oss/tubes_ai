import java.io.*;
import java.util.*;

public class GA {

    static class Point {
        int row, col;

        Point(int r, int c) {
            row = r;
            col = c;
        }
    }

    static class Node {
        int row, col;
        double distance;

        Node(int r, int c, double d) {
            row = r;
            col = c;
            distance = d;
        }
    }

    static int[][] grid;
    static int panjangBoard, lebarBoard;
    static int p, h, t;
    static List<Point> houses = new ArrayList<>();
    static List<Point> trees = new ArrayList<>();
    static Random rnd = new Random(67);

    // GA Parameters
    static int MAX_GEN;
    static int POP_SIZE;
    static double CROSS_RATE;
    static double MUT_RATE;
    static double ELITISM_RATE;

    static class Individual {
        List<Point> fireStations;
        double fitness;

        Individual(List<Point> stations) {
            this.fireStations = stations;
            this.fitness = fitnessFunction(stations);
        }
    }

    public static void main(String[] args) throws Exception {
        String filename = args[0];
        MAX_GEN = Integer.parseInt(args[1]);
        POP_SIZE = Integer.parseInt(args[2]);
        CROSS_RATE = Double.parseDouble(args[3]);
        MUT_RATE = Double.parseDouble(args[4]);
        readInput(filename);
        Individual best = geneticAlgorithm();

        System.out.printf("%d %.5f%n", p, best.fitness);
        for (Point fs : best.fireStations)
            System.out.println((fs.row) + " " + (fs.col));
    }

    // ================== INPUT ===================
    static void readInput(String filename) throws FileNotFoundException {
        houses.clear();
        trees.clear();
        Scanner sc = new Scanner(new File(filename));
        panjangBoard = sc.nextInt();
        lebarBoard = sc.nextInt();
        grid = new int[panjangBoard][lebarBoard];

        p = sc.nextInt(); // num stations
        h = sc.nextInt(); // num houses
        t = sc.nextInt(); // num trees

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

        int elitismCount = (int) (POP_SIZE * ELITISM_RATE);

        for (int gen = 0; gen < MAX_GEN; gen++) {
            List<Individual> newPop = new ArrayList<>();

            List<Individual> sorted = new ArrayList<>(population);
            sorted.sort(Comparator.comparingDouble(a -> a.fitness));

            for (int i = 0; i < elitismCount; i++)
                newPop.add(sorted.get(i));

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

    // ================== SELECTION ===================
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

    // ================== CROSSOVER ===================
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

    // ================== MUTATION ===================
    static void mutate(List<Point> stations) {
        int idx = rnd.nextInt(stations.size());
        stations.set(idx, randomEmptyPoint());
    }

    // ================== POPULATION & UTILS ===================
    static List<Individual> initPopulation() {
        List<Individual> pop = new ArrayList<>();
        for (int i = 0; i < POP_SIZE; i++)
            pop.add(new Individual(generateRandomStations()));
        return pop;
    }

    static List<Point> generateRandomStations() {
        List<Point> stations = new ArrayList<>();
        while (stations.size() < p) {
            Point pt = randomEmptyPoint();
            if (!contains(stations, pt))
                stations.add(pt);
        }
        return stations;
    }

    static Point randomEmptyPoint() {
        while (true) {
            int x = rnd.nextInt(panjangBoard);
            int y = rnd.nextInt(lebarBoard);
            if (grid[x][y] == 0)
                return new Point(x, y);
        }
    }

    static boolean contains(List<Point> list, Point p) {
        for (Point q : list)
            if (q.row == p.row && q.col == p.col)
                return true;
        return false;
    }

    static Individual getBest(List<Individual> pop) {
        Individual best = pop.get(0);
        for (Individual ind : pop) {
            if (ind.fitness < best.fitness)
                best = ind;
        }
        return best;
    }

    // ================== FITNESS ===================
    static double fitnessFunction(List<Point> stations) {
        double totalDistance = 0.0;
        int[][] dist = new int[panjangBoard][lebarBoard];

        for (int[] row : dist)
            Arrays.fill(row, -1);

        Queue<Point> q = new LinkedList<>();
        for (Point s : stations) {
            q.add(s);
            dist[s.row][s.col] = 0;
        }

        int[][] dir = { { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 } };
        while (!q.isEmpty()) {
            Point c = q.poll();
            for (int[] d : dir) {
                int nx = c.row + d[0];
                int ny = c.col + d[1];
                if (isNotOutOfBound(nx, ny) && grid[nx][ny] != 2 && dist[nx][ny] == -1) {
                    dist[nx][ny] = dist[c.row][c.col] + 1;
                    q.add(new Point(nx, ny));
                }
            }
        }

        for (Point house : houses) {
            if (dist[house.row][house.col] == -1)
                return Double.POSITIVE_INFINITY;
            totalDistance += dist[house.row][house.col];
        }

        return totalDistance / houses.size();
    }

    static boolean isNotOutOfBound(int x, int y) {
        return x >= 0 && y >= 0 && x < panjangBoard && y < lebarBoard;
    }
}
