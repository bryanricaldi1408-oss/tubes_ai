import java.util.*;
import java.io.File;
import java.io.FileNotFoundException;

public class HC {
    
    static final Random rnd = new Random(67);
    
    static List<Point> houses = new ArrayList<>();
    static List<Point> trees = new ArrayList<>();
    static List<Point> fireStations = new ArrayList<>();
    static int grid[][];
    static int panjangBoard;
    static int lebarBoard;
    static int maxIter;
    static int banyakStation;
    static int banyakRumah;
    static int banyakPohon;


    static class Solution{
        List<Point> stations;
        double fitness;

        Solution(List<Point> stations, double fitness){
            this.stations = stations;
            this.fitness = fitness;
        }
    }
    /*tipe data point untuk menyimpan semua koordinat rumah, pohon, dan fireStations */
    static class Point {
        int row,col;
        Point(int row, int col){
            this.row = row;
            this.col = col;
        }
    }
    /*tipe data Node untuk menyimpan posisi rumah dan jarak rumah ke firestations */
    static class Node{
        int row, col;
        double distance;
        Node(int row, int col, double distance){
            this.row = row;
            this.col = col;
            this.distance = distance;
        }
    }

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

    public static void main(String[] args) {
        try {
            String path = args[0];
            readInput(path);
            maxIter = Integer.parseInt(args[1]);
        } catch (FileNotFoundException e) {
            System.out.println("File not found!");
            e.printStackTrace();
            return;
        }
        
        randomRestartHC(Integer.parseInt(args[2]));
    }
    static Solution hillClimbing(){
        //Inisialisasi solusi awal: p station di sel kosong yang random
        for(int i=0; i<banyakStation; i++){
            Point pos = getRandomEmptyCell();
            grid[pos.row][pos.col] = 3;
            fireStations.add(pos);
        }
        
        //cost awal
        double bestAvg = fitnessFunction();
        List<Point> bestStations = clonePoints(fireStations);
        
        
        for (int i = 0; i < maxIter; i++) {
            int idx = rnd.nextInt(banyakStation);   // 1) pilih station yang akan digeser
            Point lama = fireStations.get(idx);
            
            Point baru = getNeighbor(lama);      // 2) pilih sel kosong acak (atas, kanan, kiri, bawah)
            
            grid[lama.row][lama.col] = 0;
            grid[baru.row][baru.col] = 3;
            fireStations.set(idx, baru);
            
            double avgBaru = fitnessFunction();     // 4) evaluasi neighbor
            // 5) Terima/rollback
            if (avgBaru >= bestAvg || Double.isInfinite(avgBaru)) {
                grid[baru.row][baru.col] = 0;       // rollback
                grid[lama.row][lama.col] = 3;
                fireStations.set(idx, lama);
            }else{
                bestAvg = avgBaru;                  // accept
                bestStations = clonePoints(fireStations);
            }
            
        }
        
        clearGrid(fireStations);
        return new Solution(bestStations, bestAvg);
    }
    /*
     * randomRestartHC
     * @param nRestarts berapa kali melakukan hill climbing
     */
    static void randomRestartHC(int nRestart){
        double globalBestFitness = Double.MAX_VALUE;
        List<Point> globalBestStations = new ArrayList<>();

        for(int i=0; i<nRestart; i++){
            fireStations.clear();

            Solution result = hillClimbing();

            if(result.fitness < globalBestFitness){
                globalBestFitness = result.fitness;
                globalBestStations = result.stations;
            }
        }
        System.out.printf("%d %.5f%n", banyakStation, globalBestFitness);
        for (Point s : globalBestStations) {
            System.out.printf("%d %d%n", (s.row + 1), (s.col + 1));
        }
    }


    static void clearGrid(List<Point> stationToClear){
        for(Point p : stationToClear){
            if(isNotOutOfBound(p.row, p.col)){
                if(grid[p.row][p.col] == 3){
                    grid[p.row][p.col] = 0;
                }
            }
        }
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
        if(grid[nx][ny] != 0){
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
    
}

