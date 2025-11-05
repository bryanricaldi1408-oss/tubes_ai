
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class GeneticAlgo {
    public final Random rand = new Random();

    class Pos {
        int x,y;

        public Pos(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    int num_of_column, num_of_row;
    int num_of_firestations, num_of_house, num_of_tree;
    boolean[][] isTree;
    boolean[][] isHouse;
    List<Pos> houses = new ArrayList<>();
    List<Pos> trees = new ArrayList<>();
    List<Pos> emptyCells = new ArrayList<>();
    List<Chromosome> currPopulation = new ArrayList<>();
    Map<Integer, Pos> idxToPos = new HashMap<>();

    static final int POPULATION_SIZE = 200;
    static final int GENERATION_NUM = 400;
    static final double MUTATION_RATE = 0.001;
    static final double CROSSOVER_RATE = 0.01;
    static final int[] dx = {1,-1,0,0};
    static final int[] dy = {0,0,1,-1};

    public GeneticAlgo() {
        
    }

    static class Chromosome implements Comparable<Chromosome>{
        int[] gene; //lokasi setiap firestation
        double fitness;

        public Chromosome(int[] gene) {
            this.gene = gene;
        }

        @Override
        public int compareTo(Chromosome other) {
            return Double.compare(this.fitness, other.fitness);
        }
    }

    double fitness_func(double cost) {
        return 1.0/cost+1.0;
    }

    boolean clamp(int x, int y) {
        return x >= 1 && x <= num_of_column && y >= 1 && y <= num_of_row;
    }

    List<Pos> getEmptyCell() {
        List<Pos> emptyCells = new ArrayList<>();
        
        for (int x = 1; x <= num_of_column; x++) {
            for (int y = 1; y <= num_of_row; y++) {
                if (!isHouse[x][y] && !isTree[x][y]) emptyCells.add(new Pos(x, y));
            }
        }

        return emptyCells;
    }

    Chromosome generateIndividual() {
        int[] gene = new int[num_of_firestations];

        for (int i = 0; i<num_of_firestations; i++) {
            Pos randCell = emptyCells.get(rand.nextInt(emptyCells.size()));
            gene[i] = emptyCells.indexOf(randCell);
        }

        return new Chromosome(gene);
    }

    void mutate() {}

    void crossover() {}


    public static void main(String[] args) {
        GeneticAlgo ga = new GeneticAlgo();

        //initialize initial population
        List<Chromosome> population = new ArrayList<>();
        for (int i = 0; i<POPULATION_SIZE; i++) {
            Chromosome newIndividual = ga.generateIndividual();
        }
    }
}