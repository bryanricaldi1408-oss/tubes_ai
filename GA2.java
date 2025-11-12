import java.io.*;
import java.util.*;

public class GA2 {

    static class Point {
        int row, col;

        Point(int r, int c) {
            row = r;
            col = c;
        }
    }

    static int[][] grid;
    static int panjangBoard, lebarBoard;
    static int p, h, t;
    static List<Point> houses = new ArrayList<>();
    static List<Point> trees = new ArrayList<>();
    static Random rnd = new Random();

    // GA Parameters
    static int POP_SIZE = 40;
    static int MAX_GEN = 100;
    static double CROSS_RATE = 0.8;
    static double MUT_RATE = 0.2;

    static class Individual {
        List<Point> fireStations;
        double fitness;

        Individual(List<Point> stations) {
            this.fireStations = stations;
            this.fitness = computeFitness(stations);
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Usage: java GA_FireStation_Optimized input.txt maxGen");
            return;
        }

        String filename = args[0];
        MAX_GEN = Integer.parseInt(args[1]);

        readInput(filename);
        Individual best = geneticAlgorithm();

        // OUTPUT
        System.out.printf("%d %.5f%n", p, best.fitness);
        for (Point fs : best.fireStations) {
            System.out.println((fs.row + 1) + " " + (fs.col + 1)); // back to 1-based
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

    // ================== GA CORE ===================
    static Individual geneticAlgorithm() {
        List<Individual> population = initPopulation();
        Individual best = getBest(population);

        for (int gen = 0; gen < MAX_GEN; gen++) {
            List<Individual> newPop = new ArrayList<>();
            newPop.add(best); // elitism

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

            if (gen % 10 == 0) {
                // System.out.printf("Gen %d | Best Fitness: %.5f%n", gen, best.fitness);
            }
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
        Point p;
        do {
            p = randomEmptyPoint();
        } while (contains(stations, p));
        stations.set(idx, p);
    }

    static Individual getBest(List<Individual> pop) {
        return pop.stream().min(Comparator.comparingDouble(ind -> ind.fitness)).get();
    }

    // ================== UTIL ===================
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

    // ================== FITNESS (OPTIMIZED) ===================
    static double computeFitness(List<Point> fireStations) {
        int[][] dist = multiSourceBFS(fireStations);
        double total = 0.0;
        for (Point h : houses) {
            int d = dist[h.row][h.col];
            if (d == Integer.MAX_VALUE)
                return Double.POSITIVE_INFINITY;
            total += d;
        }
        return total / houses.size();
    }

    // Multi-source BFS: hitung jarak minimum ke salah satu fire station
    static int[][] multiSourceBFS(List<Point> stations) {
        int[][] dist = new int[panjangBoard][lebarBoard];
        for (int i = 0; i < panjangBoard; i++)
            Arrays.fill(dist[i], Integer.MAX_VALUE);

        Queue<Point> q = new ArrayDeque<>();
        for (Point s : stations) {
            dist[s.row][s.col] = 0;
            q.add(s);
        }

        int[][] dir = { { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 } };
        while (!q.isEmpty()) {
            Point cur = q.poll();
            int cd = dist[cur.row][cur.col];
            for (int[] d : dir) {
                int nx = cur.row + d[0];
                int ny = cur.col + d[1];
                if (nx >= 0 && ny >= 0 && nx < panjangBoard && ny < lebarBoard && grid[nx][ny] != 2) {
                    if (dist[nx][ny] > cd + 1) {
                        dist[nx][ny] = cd + 1;
                        q.add(new Point(nx, ny));
                    }
                }
            }
        }
        return dist;
    }
}
