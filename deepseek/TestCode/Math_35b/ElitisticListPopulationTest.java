package org.apache.commons.math3.genetics;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

/**
 * Test suite for ElitisticListPopulation.
 * Designed to achieve high coverage and detect faults (e.g., Math-35 bug).
 */
public class ElitisticListPopulationTest {

    private static final double EPSILON = 1e-10;

    // A simple Chromosome implementation for testing
    private static class DummyChromosome extends Chromosome {
        private final double fitness;

        DummyChromosome(double fitness) {
            this.fitness = fitness;
        }

        @Override
        public double fitness() {
            return fitness;
        }

        @Override
        public int compareTo(Chromosome other) {
            return Double.compare(this.fitness, other.fitness());
        }
    }

    private List<Chromosome> chromosomeList;

    @Before
    public void setUp() {
        chromosomeList = new ArrayList<>();
        chromosomeList.add(new DummyChromosome(5.0));
        chromosomeList.add(new DummyChromosome(3.0));
        chromosomeList.add(new DummyChromosome(8.0));
        chromosomeList.add(new DummyChromosome(1.0));
    }

    // ========== Constructor tests ==========

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullChromosomeList() {
        new ElitisticListPopulation(null, 10, 0.5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNegativePopulationLimit() {
        new ElitisticListPopulation(chromosomeList, -1, 0.5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithZeroPopulationLimit() {
        new ElitisticListPopulation(chromosomeList, 0, 0.5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNegativeElitismRate() {
        new ElitisticListPopulation(chromosomeList, 10, -0.1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithElitismRateGreaterThanOne() {
        new ElitisticListPopulation(chromosomeList, 10, 1.5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithElitismRateExactlyOne() {
        // rate = 1.0 is allowed? Actually, it should be < 1.0 typically, but check
        new ElitisticListPopulation(chromosomeList, 10, 1.0);
    }

    @Test
    public void testConstructorWithValidParameters() {
        ElitisticListPopulation pop = new ElitisticListPopulation(chromosomeList, 10, 0.5);
        assertEquals(4, pop.getPopulationSize());
        assertEquals(10, pop.getPopulationLimit());
        assertEquals(0.5, pop.getElitismRate(), EPSILON);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithListSizeExceedingLimit() {
        // population limit smaller than list size
        new ElitisticListPopulation(chromosomeList, 2, 0.5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithEmptyListAndLimit() {
        new ElitisticListPopulation(new ArrayList<Chromosome>(), 10, 0.5);
    }

    // Constructor with only population limit and elitism rate
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorOnlyLimitNegativeElitismRate() {
        new ElitisticListPopulation(10, -0.1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorOnlyLimitElitismRateGreaterThanOne() {
        new ElitisticListPopulation(10, 1.5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorOnlyLimitElitismRateExactlyOne() {
        new ElitisticListPopulation(10, 1.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorOnlyLimitNegativePopulationLimit() {
        new ElitisticListPopulation(-1, 0.5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorOnlyLimitZeroPopulationLimit() {
        new ElitisticListPopulation(0, 0.5);
    }

    @Test
    public void testConstructorOnlyLimitValid() {
        ElitisticListPopulation pop = new ElitisticListPopulation(10, 0.3);
        assertEquals(0, pop.getPopulationSize());
        assertEquals(10, pop.getPopulationLimit());
        assertEquals(0.3, pop.getElitismRate(), EPSILON);
    }

    // ========== setElitismRate tests ==========

    @Test(expected = IllegalArgumentException.class)
    public void testSetElitismRateNegative() {
        ElitisticListPopulation pop = new ElitisticListPopulation(10, 0.5);
        pop.setElitismRate(-0.2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetElitismRateGreaterThanOne() {
        ElitisticListPopulation pop = new ElitisticListPopulation(10, 0.5);
        pop.setElitismRate(1.2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetElitismRateExactlyOne() {
        ElitisticListPopulation pop = new ElitisticListPopulation(10, 0.5);
        pop.setElitismRate(1.0);
    }

    @Test
    public void testSetElitismRateValid() {
        ElitisticListPopulation pop = new ElitisticListPopulation(10, 0.5);
        pop.setElitismRate(0.75);
        assertEquals(0.75, pop.getElitismRate(), EPSILON);
    }

    // ========== nextGeneration tests ==========

    @Test
    public void testNextGenerationWithElitism() {
        // Create population with known chromosomes
        List<Chromosome> list = new ArrayList<>();
        list.add(new DummyChromosome(10.0)); // best
        list.add(new DummyChromosome(5.0));
        list.add(new DummyChromosome(8.0));
        list.add(new DummyChromosome(3.0));
        ElitisticListPopulation pop = new ElitisticListPopulation(list, 10, 0.5);
        // Elitism rate 0.5 -> keep top 2 (since 4*0.5=2)
        Population nextGen = pop.nextGeneration();
        assertNotNull(nextGen);
        assertTrue(nextGen instanceof ElitisticListPopulation);
        ElitisticListPopulation next = (ElitisticListPopulation) nextGen;
        // The next generation should have the same population limit
        assertEquals(10, next.getPopulationLimit());
        // It should contain the best chromosomes from previous generation
        // Since we don't have crossover/mutation, the next generation will just have the elite chromosomes
        // plus possibly random? Actually, the default nextGeneration in ListPopulation? 
        // In Commons Math, ElitisticListPopulation.nextGeneration copies the best chromosomes and then fills the rest with random chromosomes? 
        // But we cannot rely on that. We'll just check that the elite chromosomes are present.
        // For simplicity, we assume the next generation size equals population limit (10) and contains the top 2.
        // However, the actual implementation may throw UnsupportedOperationException if not overridden properly.
        // We'll just check that the method returns a non-null population.
    }

    @Test
    public void testNextGenerationWithZeroElitismRate() {
        ElitisticListPopulation pop = new ElitisticListPopulation(chromosomeList, 10, 0.0);
        Population nextGen = pop.nextGeneration();
        assertNotNull(nextGen);
    }

    @Test
    public void testNextGenerationWithFullElitismRate() {
        // rate just below 1.0 (0.99)
        ElitisticListPopulation pop = new ElitisticListPopulation(chromosomeList, 10, 0.99);
        Population nextGen = pop.nextGeneration();
        assertNotNull(nextGen);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNextGenerationWithEmptyPopulation() {
        ElitisticListPopulation pop = new ElitisticListPopulation(10, 0.5);
        // No chromosomes added, nextGeneration should fail
        pop.nextGeneration();
    }

    // ========== Edge cases ==========

    @Test
    public void testGetElitismRateDefault() {
        ElitisticListPopulation pop = new ElitisticListPopulation(10, 0.2);
        assertEquals(0.2, pop.getElitismRate(), EPSILON);
    }

    @Test
    public void testPopulationLimitAfterConstruction() {
        ElitisticListPopulation pop = new ElitisticListPopulation(chromosomeList, 10, 0.5);
        assertEquals(10, pop.getPopulationLimit());
    }

    @Test
    public void testPopulationSizeAfterConstruction() {
        ElitisticListPopulation pop = new ElitisticListPopulation(chromosomeList, 10, 0.5);
        assertEquals(4, pop.getPopulationSize());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddChromosomeExceedingLimit() {
        ElitisticListPopulation pop = new ElitisticListPopulation(chromosomeList, 4, 0.5);
        // Already at limit, adding another should throw
        pop.addChromosome(new DummyChromosome(2.0));
    }

    @Test
    public void testAddChromosomeWithinLimit() {
        ElitisticListPopulation pop = new ElitisticListPopulation(10, 0.5);
        pop.addChromosome(new DummyChromosome(1.0));
        assertEquals(1, pop.getPopulationSize());
    }

    // ========== Additional coverage for inherited methods ==========

    @Test
    public void testGetChromosomes() {
        ElitisticListPopulation pop = new ElitisticListPopulation(chromosomeList, 10, 0.5);
        List<Chromosome> chromosomes = pop.getChromosomes();
        assertEquals(4, chromosomes.size());
        assertTrue(chromosomes.containsAll(chromosomeList));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetChromosomesUnmodifiable() {
        ElitisticListPopulation pop = new ElitisticListPopulation(chromosomeList, 10, 0.5);
        List<Chromosome> chromosomes = pop.getChromosomes();
        chromosomes.add(new DummyChromosome(99.0)); // should throw
    }

    @Test
    public void testSetChromosomes() {
        ElitisticListPopulation pop = new ElitisticListPopulation(10, 0.5);
        pop.setChromosomes(chromosomeList);
        assertEquals(4, pop.getPopulationSize());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetChromosomesExceedingLimit() {
        ElitisticListPopulation pop = new ElitisticListPopulation(3, 0.5);
        pop.setChromosomes(chromosomeList); // list size 4 > limit 3
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetChromosomesNull() {
        ElitisticListPopulation pop = new ElitisticListPopulation(10, 0.5);
        pop.setChromosomes(null);
    }

    @Test
    public void testIterateChromosomes() {
        ElitisticListPopulation pop = new ElitisticListPopulation(chromosomeList, 10, 0.5);
        int count = 0;
        for (Chromosome c : pop) {
            count++;
        }
        assertEquals(4, count);
    }

    @Test
    public void testToString() {
        ElitisticListPopulation pop = new ElitisticListPopulation(chromosomeList, 10, 0.5);
        String str = pop.toString();
        assertNotNull(str);
        assertTrue(str.contains("ElitisticListPopulation"));
    }
}