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
    
    
    static final Random rnd = new Random();
    static List<Point> houses = new ArrayList<>();
    static List<Point> trees = new ArrayList<>();
    static List<Point> fireStations = new ArrayList<>();
    static int grid[][];
    static int panjangBoard;
    static int lebarBoard;
    
    static int banyakStation;
    static Map<String, Double> distanceCache = new HashMap<>();
    
    static Point getNeighbor(Point p, double stepSize){
        int[][] dir = {{1,0},{-1,0},{0,1},{0,-1}};
        
        boolean validNeighbor = false;
        int newRow = 0;
        int newCol = 0;
        int itr = 0;
        
        while(!validNeighbor && itr < 1000){
            int newDir = rnd.nextInt(4);
            
            int tempRow = p.row + (int) Math.round(dir[newDir][0]  * stepSize);
            int tempCol = p.col + (int) Math.round(dir[newDir][1]  * stepSize);
            
            if (isNotOutOfBound(tempRow, tempCol) && isInEmptySpace(tempRow, tempCol)) {
                newRow = tempRow;
                newCol = tempCol;
                break;
            }
            itr++;
        }
        return new Point(newRow, newCol);
        
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
    
    static double fitnessFunction(){
        double totalDistance = 0.0;
        
        for(Point house : houses){
            double best = Double.MAX_VALUE;
            for (Point s : fireStations) {
                double d = bfs(house, s);
                if (d < best) best = d;
            }
            if(best == Double.MAX_VALUE)
            return Double.POSITIVE_INFINITY;
            totalDistance += best;
        }
        
        return totalDistance/houses.size();
    }
    
    static double bfs(Point house, Point firestation){
        int[][] dir = {{1,0},{-1,0},{0,1},{0,-1}};
        String key = house.row + "," + house.col + "-" + firestation.row + "," + firestation.col;
        if (distanceCache.containsKey(key)) return distanceCache.get(key);
        int houseR = house.row;
        int houseC = house.col;
        
        Queue <Node> queue = new LinkedList<>();
        boolean[][] visited = new boolean[panjangBoard][lebarBoard];
        
        queue.add(new Node(houseR, houseC, 0));
        visited[houseR][houseC] = true;
        
        while(!queue.isEmpty()){
            Node cur = queue.poll();
            
            if(cur.row == firestation.row && cur.col == firestation.col){
                distanceCache.put(key, cur.distance);
                return cur.distance;
            }
            for (int[] d : dir) {
                int nx = cur.row + d[0];
                int ny = cur.col + d[1];
                
                if(isNotOutOfBound(nx,ny) && grid[nx][ny] != 2 && !visited[nx][ny]){
                    visited[nx][ny] = true;
                    queue.add(new Node(nx, ny, cur.distance+1));
                }
            }
        }
        
        return Double.MAX_VALUE;
    }
    static void simulatedAnnealing(double t0, double cooling, double stopping_temp, double stepSize) {
        // Inisialisasi awal (sudah benar)
        for(int i=0; i<banyakStation; i++){
            Point pos = getRandomEmptyCell();
            grid[pos.row][pos.col] = 3;
            fireStations.add(pos); // fireStations adalah state saat ini (CURRENT)
        }
        
        double currentFitness = fitnessFunction(); // Fitness dari state saat ini
        double bestFitness = currentFitness; // Best fitness
        List<Point> bestPos = clonePoints(fireStations); // Best solution
        double temprature = t0;
        
        
        while(temprature > stopping_temp){
            int idx = rnd.nextInt(banyakStation);
            Point currentStation = fireStations.get(idx); 
            Point neighbor = getNeighbor(currentStation, stepSize);
            
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
                Scanner sc = new Scanner(new File(path));
                
                panjangBoard = sc.nextInt(); 
                lebarBoard = sc.nextInt();  
                
                grid = new int[panjangBoard][lebarBoard];
                banyakStation = sc.nextInt(); 
                int banyakRumah = sc.nextInt(); 
                int banyakPohon = sc.nextInt(); 
                
                /*1 menandakan rumah, 2 menandakan pohon, 3 menandakan fire station */            
                for (int i = 0; i < banyakRumah; i++) {
                    int x = sc.nextInt()-1;
                    int y = sc.nextInt()-1;
                    grid[x][y]=1; 
                    houses.add(new Point(x, y));
                }
                
                for (int i = 0; i < banyakPohon; i++) {
                    int x = sc.nextInt()-1;
                    int y = sc.nextInt()-1;
                    grid[x][y]=2;
                    trees.add(new Point(x, y));
                }
                
                sc.close();
                
            } catch (FileNotFoundException e) {
                System.out.println("File not found!");
                e.printStackTrace();
                return;
            } 
            
            simulatedAnnealing(100,0.999, 0.001, 0.5);
        }
    }
    