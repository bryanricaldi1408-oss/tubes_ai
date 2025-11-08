import java.util.Random;
import java.util.Scanner;

public class TestCaseGenerator {
    private final int m, n, p, h, t;
    private final Random rnd;

    public TestCaseGenerator(int m, int n){
        this.m = m;
        this.n = n;
        this.rnd = new Random();
        int totalCells = m*n;

        double housePercent = 0.15 + (rnd.nextDouble() * 0.05);
        this.h = (int) (housePercent * totalCells);
        
        double treePercent = 0.20 + (rnd.nextDouble() * 0.05);
        this.t = (int) (treePercent * totalCells);

        double housePerStation= 15 + (rnd.nextInt(11));
        this.p = (int) Math.ceil((double) this.h/housePerStation);
        
        if(h + t > totalCells){
            System.err.println("Jumlah rumah dan pohon melebihi total set.");
        }
    }
    
}
