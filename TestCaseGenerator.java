import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

/**
 * Membuat test case untuk masalah penempatan Fire Station.
 * * Aturan:
 * - h (rumah): 15-20% dari total sel
 * - t (pohon): 20-25% dari total sel
 * - p (stasiun): 1 stasiun per 15-25 rumah
 * - Koordinat: (1,1) adalah kiri Bawah. (x: 1..n, y: 1..m)
 */
public class TestCaseGenerator {

    private final int m, n, p, h, t;
    private final Set<Point> houseLocations;
    private final Set<Point> treeLocations;
    private final Random rand;

    /**
     * Kelas internal sederhana untuk menyimpan koordinat dan menangani keunikan.
     */
    private static class Point {
        final int x, y;

        Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public String toString() {
            return x + " " + y; // Format untuk output file
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Point point = (Point) o;
            return x == point.x && y == point.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }

    public TestCaseGenerator(int m, int n) {
        this.m = m;
        this.n = n;
        this.rand = new Random();
        int totalCells = m * n;

        // 1. Hitung h (rumah): 15-20% dari total
        // (rand.nextDouble() * 0.05) -> angka acak antara 0.00 dan 0.05
        double housePercent = 0.15 + (rand.nextDouble() * 0.05);
        this.h = (int) (totalCells * housePercent);

        // 2. Hitung t (pohon): 20-25% dari total
        double treePercent = 0.20 + (rand.nextDouble() * 0.05);
        this.t = (int) (totalCells * treePercent);

        // 3. Hitung p (stasiun): 1 stasiun per 15-25 rumah
        // (rand.nextInt(11)) -> angka acak antara 0 dan 10. (15 + ...) -> 15 s.d. 25
        int housesPerStation = 15 + rand.nextInt(11);
        this.p = (int) Math.ceil((double) this.h / housesPerStation);
        
        // Pastikan h + t tidak melebihi total sel (meskipun dengan persen ini sangat tidak mungkin)
        if (h + t > totalCells) {
            // Ini seharusnya tidak terjadi dengan persentase 20% + 25% = 45%
            System.err.println("Peringatan: Jumlah rumah dan pohon melebihi total sel. Coba lagi.");
            // Untuk skenario nyata, mungkin perlu penyesuaian h dan t
        }

        // 4. Generate lokasi unik
        this.houseLocations = new HashSet<>(h);
        this.treeLocations = new HashSet<>(t);
        generateLocations();
    }

    /**
     * Menghasilkan lokasi unik untuk rumah dan pohon.
     */
    private void generateLocations() {
        Set<Point> occupiedCells = new HashSet<>(h + t);

        // Generate lokasi rumah
        for (int i = 0; i < h; i++) {
            Point p = generateUniquePoint(occupiedCells);
            this.houseLocations.add(p);
        }

        // Generate lokasi pohon
        for (int i = 0; i < t; i++) {
            Point p = generateUniquePoint(occupiedCells);
            this.treeLocations.add(p);
        }
    }

    /**
     * Membuat satu Point unik yang belum ada di 'occupiedCells'.
     * (x: 1..n, y: 1..m)
     */
    private Point generateUniquePoint(Set<Point> occupiedCells) {
        Point p;
        do {
            // x (kolom) dari 1 sampai n
            int x = rand.nextInt(n) + 1;
            // y (baris) dari 1 sampai m
            int y = rand.nextInt(m) + 1;
            p = new Point(x, y);
        } while (occupiedCells.contains(p)); // Ulangi jika koordinat sudah terisi
        
        occupiedCells.add(p); // Tandai sebagai terisi
        return p;
    }

    /**
     * Menyimpan data yang dihasilkan ke file teks.
     */
    public void saveToFile(String filename) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            // Baris 1: m n
            writer.println(m + " " + n);

            // Baris 2: p h t
            writer.println(p + " " + h + " " + t);

            // Baris 3 s.d. 3+h-1: Lokasi rumah
            for (Point house : houseLocations) {
                writer.println(house.toString());
            }

            // Baris berikutnya: Lokasi pohon
            for (Point tree : treeLocations) {
                writer.println(tree.toString());
            }
        }
    }

    // --- MAIN METHOD UNTUK EKSEKUSI ---

    /**
     * Main method untuk menjalankan generator dan membuat 3 file yang diminta.
     */
    public static void main(String[] args) {
        try {
            System.out.println("Membuat test case kecil (20x20)...");
            TestCaseGenerator small = new TestCaseGenerator(20, 20);
            small.saveToFile("testcase_small.txt");
            System.out.printf("-> Tersimpan di 'testcase_small.txt' (p=%d, h=%d, t=%d)\n\n", small.p, small.h, small.t);

            System.out.println("Membuat test case menengah (40x40)...");
            TestCaseGenerator medium = new TestCaseGenerator(40, 40);
            medium.saveToFile("testcase_medium.txt");
            System.out.printf("-> Tersimpan di 'testcase_medium.txt' (p=%d, h=%d, t=%d)\n\n", medium.p, medium.h, medium.t);

            System.out.println("Membuat test case besar (80x80)...");
            TestCaseGenerator large = new TestCaseGenerator(80, 80);
            large.saveToFile("testcase_large.txt");
            System.out.printf("-> Tersimpan di 'testcase_large.txt' (p=%d, h=%d, t=%d)\n\n", large.p, large.h, large.t);
            
            System.out.println("Semua test case berhasil dibuat.");

        } catch (IOException e) {
            System.err.println("Terjadi error saat menulis file:");
            e.printStackTrace();
        }
    }
}