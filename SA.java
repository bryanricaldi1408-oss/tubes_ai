import java.util.*;
import java.io.File;
import java.io.FileNotFoundException;

public class SA {
    static class Point {
        int row,col;
        Point(int row, int col){
            this.row = row;
            this.col = col;
        }
    }
    static class Node{
        int row, col;
        double distance;
        Node(int row, int col, double distance){
            this.row = row;
            this.col = col;
            this.distance = distance;
        }
    }
    
    
    static final int EMPTY = 0;
    static final int HOUSE = 1;
    static final int TREE = 2;
    static final int FIRE_STATION = 3;
    
    static final Random rnd = new Random();
    static List<Point> houses = new ArrayList<>();
    static List<Point> trees = new ArrayList<>();
    static List<Point> fireStations = new ArrayList<>();
    static int grid[][];
    static int panjangBoard;
    static int lebarBoard;
    
    static int banyakStation;
    static int banyakRumah;
    static int banyakPohon;
    static Map<String, Double> distanceCache = new HashMap<>();

    static void readInput(String filename) throws FileNotFoundException {
        Scanner sc = new Scanner(new File(filename));
        panjangBoard = sc.nextInt();
        lebarBoard = sc.nextInt();
        grid = new int[panjangBoard][lebarBoard];

        banyakStation = sc.nextInt();
        banyakRumah = sc.nextInt();
        banyakPohon = sc.nextInt();

        for (int i = 0; i < banyakRumah; i++) {
            int x = sc.nextInt() - 1;
            int y = sc.nextInt() - 1;
            houses.add(new Point(x, y));
            grid[x][y] = 1;
        }

        for (int i = 0; i < banyakPohon; i++) {
            int x = sc.nextInt() - 1;
            int y = sc.nextInt() - 1;
            trees.add(new Point(x, y));
            grid[x][y] = 2;
        }
        sc.close();
    }
    
    static Point getNeighbor(Point p){
        int[][] dir = {{1,0},{-1,0},{0,1},{0,-1}};
        List<Point> validNeighbors = new ArrayList<>();

        for (int[] d : dir) {
            int newRow = p.row + d[0];
            int newCol = p.col + d[1];

            if (isNotOutOfBound(newRow, newCol) && isInEmptySpace(newRow, newCol)) {
                validNeighbors.add(new Point(newRow, newCol));
            }
        }

        if (validNeighbors.isEmpty()) {
            return p; // Tidak ada tetangga valid, kembalikan posisi semula
        }

        // Pilih satu tetangga valid secara acak
        return validNeighbors.get(rnd.nextInt(validNeighbors.size()));
    }
    
    
    static boolean isInEmptySpace(int nx, int ny){
        if(grid[nx][ny] != EMPTY){
            return false;
        }
        return true;
    }
    
    static boolean isNotOutOfBound(int nx, int ny){
        if(nx < 0 || nx >= panjangBoard || ny < 0 || ny >= lebarBoard){
            return false;
        }
        return true;
    }
    
    static double fitnessFunction() {
        double totalDistance = 0.0;
        int[][] distances = new int[panjangBoard][lebarBoard];
        for (int[] row : distances) {
            Arrays.fill(row, -1); // -1 menandakan belum dikunjungi
        }

        Queue<Point> queue = new LinkedList<>();

        // Inisialisasi jarak untuk semua fire stations
        for (Point station : fireStations) {
            if (isNotOutOfBound(station.row, station.col)) {
                queue.add(new Point(station.row, station.col));
                distances[station.row][station.col] = 0;
            }
        }

        int[][] dir = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

        while(!queue.isEmpty()){
            Point curr = queue.poll();

            for(int[] d : dir){
                int nx = curr.row + d[0];
                int ny = curr.col + d[1];

                if (isNotOutOfBound(nx, ny) && grid[nx][ny] != 2 && distances[nx][ny] == -1) {
                    distances[nx][ny] = distances[curr.row][curr.col] + 1;
                    queue.add(new Point(nx, ny));
                }
            }
        }

        // Hitung total jarak untuk semua rumah
        for (Point house : houses) {
            int dist = distances[house.row][house.col];
            if (dist == -1) { // Rumah tidak dapat dijangkau
                return Double.POSITIVE_INFINITY;
            }
            totalDistance += dist;
        }


        return totalDistance / houses.size();
    }
    
    static void simulatedAnnealing(double t0, double cooling, double stopping_temp) {
        // Store initial grid state (houses and trees)
        int[][] initialGrid = new int[panjangBoard][lebarBoard];
        for (int r = 0; r < panjangBoard; r++) {
            for (int c = 0; c < lebarBoard; c++) {
                initialGrid[r][c] = grid[r][c];
            }
        }

        // Inisialisasi awal (sudah benar)
        do {
            // Reset grid to initial state (houses and trees only)
            for (int r = 0; r < panjangBoard; r++) {
                for (int c = 0; c < lebarBoard; c++) {
                    grid[r][c] = initialGrid[r][c];
                }
            }
            fireStations.clear(); // Clear previous attempts

            for(int i=0; i<banyakStation; i++){
                Point pos = getRandomEmptyCell();
                grid[pos.row][pos.col] = 3;
                fireStations.add(pos); // fireStations adalah state saat ini (CURRENT)
            }
        } while (Double.isInfinite(fitnessFunction()));
        
        double currentFitness = fitnessFunction(); // Fitness dari state saat ini
        double bestFitness = currentFitness; // Best fitness
        List<Point> bestPos = clonePoints(fireStations); // Best solution
        double temprature = t0;
        
        
        while(temprature > stopping_temp){
            int idx = rnd.nextInt(banyakStation);
            Point currentStation = fireStations.get(idx); 
            Point neighbor = getNeighbor(currentStation);
            
            grid[currentStation.row][currentStation.col] = 0;
            fireStations.set(idx, neighbor);
            grid[neighbor.row][neighbor.col] = 3;
            
            double newFitness = fitnessFunction();
            double deltaE = newFitness - currentFitness; 
            // Logika Penerimaan
            // (3) Cek apakah move DITERIMA (otomatis jika lebih baik, atau dengan probabilitas jika lebih buruk)
            if(deltaE < 0){ // Move LEBIH BAIK (minimasi)
                // TERIMA. Update Current State
                currentFitness = newFitness;
                // Cek apakah ini Best Ever
                if(currentFitness < bestFitness){
                    bestFitness = currentFitness;
                    bestPos = clonePoints(fireStations);
                }
            }else{ 
                double probability = Math.exp(-deltaE / temprature); 
                if(probability >= rnd.nextDouble()){
                    currentFitness = newFitness;
                }else{
                    grid[neighbor.row][neighbor.col] = 0;
                    fireStations.set(idx, currentStation);
                    grid[currentStation.row][currentStation.col] = 3;
                }
            }
            temprature*=cooling;
        }
        System.out.printf("%d %.5f%n", banyakStation, bestFitness);
        for (Point s : bestPos) {
            System.out.printf("%d %d%n", (s.row + 1), (s.col + 1));
        }
    }
        
        static List<Point> clonePoints(List<Point> src){
            List<Point> out = new ArrayList<>(src.size());
            for (Point p : src) out.add(new Point(p.row, p.col));
            return out;
        }
        
        
        static Point getRandomEmptyCell(){
            while(true){
                int row = rnd.nextInt(panjangBoard);
                int col = rnd.nextInt(lebarBoard);
                if(grid[row][col] == 0)return new Point(row, col);
            }
        }
        
        public static void main(String[] args) {
            try {
                String path = args[0];
                readInput(path);
                
            } catch (FileNotFoundException e) {
                System.out.println("File not found!");
                e.printStackTrace();
                return;
            } 
            
            simulatedAnnealing(Double.parseDouble(args[1]),Double.parseDouble(args[2]), Double.parseDouble(args[3]));
        }
    }
    