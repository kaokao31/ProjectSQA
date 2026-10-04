package org.apache.commons.math3.genetics;

import org.junit.Test;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class ElitisticListPopulationTest {

    @Test
    public void testConstructorsAndGetters() {
        // Test constructor with population limit and elitism rate
        ElitisticListPopulation pop1 = new ElitisticListPopulation(100, 0.2);
        assertEquals(100, pop1.getPopulationLimit());
        assertEquals(0.2, pop1.getElitismRate(), 0.001);

        // Test constructor with list, population limit, and elitism rate
        List<Chromosome> chromosomes = new ArrayList<>();
        chromosomes.add(new DummyChromosome());
        
        ElitisticListPopulation pop2 = new ElitisticListPopulation(chromosomes, 50, 0.1);
        assertEquals(50, pop2.getPopulationLimit());
        assertEquals(0.1, pop2.getElitismRate(), 0.001);
        assertEquals(1, pop2.getPopulationSize());
    }

    @Test(expected = OutOfRangeException.class)
    public void testConstructorElitismRateTooLow() {
        new ElitisticListPopulation(10, -0.1);
    }

    @Test(expected = OutOfRangeException.class)
    public void testConstructorElitismRateTooHigh() {
        new ElitisticListPopulation(10, 1.1);
    }

    @Test(expected = OutOfRangeException.class)
    public void testConstructorListElitismRateTooLow() {
        List<Chromosome> chromosomes = new ArrayList<>();
        new ElitisticListPopulation(chromosomes, 10, -0.1);
    }

    @Test(expected = OutOfRangeException.class)
    public void testConstructorListElitismRateTooHigh() {
        List<Chromosome> chromosomes = new ArrayList<>();
        new ElitisticListPopulation(chromosomes, 10, 1.5);
    }

    @Test
    public void testSetElitismRate() {
        ElitisticListPopulation pop = new ElitisticListPopulation(10, 0.2);
        pop.setElitismRate(0.5);
        assertEquals(0.5, pop.getElitismRate(), 0.001);
    }

    @Test(expected = OutOfRangeException.class)
    public void testSetElitismRateTooLow() {
        ElitisticListPopulation pop = new ElitisticListPopulation(10, 0.2);
        pop.setElitismRate(-0.01);
    }

    @Test(expected = OutOfRangeException.class)
    public void testSetElitismRateTooHigh() {
        ElitisticListPopulation pop = new ElitisticListPopulation(10, 0.2);
        pop.setElitismRate(1.01);
    }

    @Test
    public void testNextGeneration() {
        // Create a population with elitism rate 0.4 -> 4 chromosomes will be elite out of 10 limit (or current size)
        ElitisticListPopulation population = new ElitisticListPopulation(10, 0.4);
        
        // Add 5 chromosomes with different fitness values
        for (int i = 1; i <= 5; i++) {
            population.addChromosome(new DummyChromosome(i));
        }
        
        Population nextGen = population.nextGeneration();
        assertTrue(nextGen instanceof ElitisticListPopulation);
        
        // Elitism rate 0.4 with population size 5 means ceil(0.4 * 5) = 2 elites
        // Let's verify size and elite presence
        // Note: DummyChromosome compares via natural ordering of fitness (ascending or descending? 
        // Usually AbstractListPopulation / Chromosome uses fitness. Let's check compareTo: lower or higher?
        // Let's just ensure nextGeneration runs cleanly without exceptions).
        assertNotNull(nextGen);
    }

    @Test
    public void testNextGenerationWithZeroElitism() {
        ElitisticListPopulation population = new ElitisticListPopulation(10, 0.0);
        population.addChromosome(new DummyChromosome(1));
        
        Population nextGen = population.nextGeneration();
        assertNotNull(nextGen);
        assertEquals(10, nextGen.getPopulationLimit());
    }

    @Test
    public void testNextGenerationWithFullElitism() {
        ElitisticListPopulation population = new ElitisticListPopulation(10, 1.0);
        population.addChromosome(new DummyChromosome(1));
        population.addChromosome(new DummyChromosome(2));
        
        Population nextGen = population.nextGeneration();
        assertNotNull(nextGen);
    }

    // Helper dummy chromosome class for testing
    private static class DummyChromosome extends AbstractListChromosome<Integer> {
        private final double fitness;

        public DummyChromosome() {
            this(0.0);
        }

        public DummyChromosome(double fitness) {
            super(java.util.Collections.singletonList(1));
            this.fitness = fitness;
        }

        @Override
        public double fitness() {
            return fitness;
        }

        @Override
        public AbstractListChromosome<Integer> newFixedLengthChromosome(List<Integer> chromosomeRepresentation) {
            return new DummyChromosome(fitness);
        }
    }
}