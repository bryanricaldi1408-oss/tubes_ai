import java.util.*;
import java.io.File;
import java.io.FileNotFoundException;

public class HC {
    
    static final Random rnd = new Random();
    
    static List<Point> houses = new ArrayList<>();
    static List<Point> trees = new ArrayList<>();
    static List<Point> fireStations = new ArrayList<>();
    static int grid[][];
    static int panjangBoard;
    static int lebarBoard;
    static int maxIter;
    static class Point {
        int row,col;
        Point(int row, int col){
            this.row = row;
            this.col = col;
        }
    }
    static class Node{
        int row, col, distance;
        Node(int row, int col, int distance){
            this.row = row;
            this.col = col;
            this.distance = distance;
        }
    }
    public static void main(String[] args) {
        int banyakStation;
        try {
            String path = args[0];
            Scanner sc = new Scanner(new File(path));
            
            panjangBoard = sc.nextInt(); 
            lebarBoard = sc.nextInt();  
            
            grid = new int[panjangBoard][lebarBoard];
            banyakStation = sc.nextInt(); 
            int banyakRumah = sc.nextInt(); 
            int banyakPohon = sc.nextInt(); 
            maxIter = Integer.parseInt(args[1]);
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
        
        hillClimbing(banyakStation);
    }
    static void hillClimbing(int banyakStation){
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
        System.out.printf("%d %.5f%n", banyakStation, bestAvg);
        for (Point s : bestStations) {
            System.out.printf("%d %d%n", (s.row + 1), (s.col + 1));
        }
        
    }
    
    static Point getNeighbor(Point p){
        int[][] dir = {{1,0},{-1,0},{0,1},{0,-1}};
        
        boolean validNeighbor = false;
        int newRow = 0;
        int newCol = 0;
        
        while(!validNeighbor){
            int newDir = rnd.nextInt(4);
            
            newRow = (int) ((p.row + dir[newDir][0]));
            newCol = (int) ((p.col + dir[newDir][1]));
            
            validNeighbor = isNotOutOfBound(newRow, newCol) && isInEmptySpace(newRow, newCol);
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
        int houseR = house.row;
        int houseC = house.col;
        
        Queue <Node> queue = new LinkedList<>();
        boolean[][] visited = new boolean[panjangBoard][lebarBoard];
        
        queue.add(new Node(houseR, houseC, 0));
        visited[houseR][houseC] = true;
        
        while(!queue.isEmpty()){
            Node cur = queue.poll();
            
            if(cur.row == firestation.row && cur.col == firestation.col){
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
    
    
    
}

