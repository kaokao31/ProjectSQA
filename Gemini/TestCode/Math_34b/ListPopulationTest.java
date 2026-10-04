package org.apache.commons.math3.genetics;

import org.junit.Test;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.*;

public class ListPopulationTest {

    private static class DummyChromosome extends Chromosome {
        private final double fitness;

        public DummyChromosome(double fitness) {
            this.fitness = fitness;
        }

        public double fitness() {
            return fitness;
        }

        public AbstractListChromosome<Double> newFixedLengthChromosome(List<Double> representation) {
            return null;
        }
    }

    private static class ConcreteListPopulation extends ListPopulation {
        public ConcreteListPopulation(int populationLimit) {
            super(populationLimit);
        }

        public ConcreteListPopulation(List<Chromosome> chromosomes, int populationLimit) {
            super(chromosomes, populationLimit);
        }

        public Population nextGeneration() {
            return this;
        }
    }

    @Test
    public void testConstructorsAndSetters() {
        List<Chromosome> chroms = new ArrayList<Chromosome>();
        chroms.add(new DummyChromosome(1.0));

        ConcreteListPopulation pop = new ConcreteListPopulation(chroms, 10);
        assertEquals(1, pop.getPopulationSize());
        assertEquals(10, pop.getPopulationLimit());

        pop.setPopulationLimit(20);
        assertEquals(20, pop.getPopulationLimit());

        // Test IllegalArgumentException for negative population limit
        try {
            new ConcreteListPopulation(-1);
            fail("Expected NumberIsTooSmallException / IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }

        // Test IllegalArgumentException for zero population limit
        try {
            new ConcreteListPopulation(0);
            fail("Expected NumberIsTooSmallException / IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }

        // Test population limit too small for initial chromosomes list
        try {
            List<Chromosome> tooMany = new ArrayList<Chromosome>();
            tooMany.add(new DummyChromosome(1.0));
            tooMany.add(new DummyChromosome(2.0));
            new ConcreteListPopulation(tooMany, 1);
            fail("Expected NumberIsTooLargeException / IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        
        // Test null initial chromosomes list
        try {
            new ConcreteListPopulation(null, 10);
            fail("Expected NullArgumentException / NullPointerException");
        } catch (NullPointerException e) {
            // expected
        } catch (org.apache.commons.math3.exception.NullArgumentException e) {
            // expected
        }
    }

    @Test
    public void testSetPopulationLimitEdgeCases() {
        ConcreteListPopulation pop = new ConcreteListPopulation(5);
        pop.addChromosome(new DummyChromosome(1.0));
        pop.addChromosome(new DummyChromosome(2.0));

        // Setting limit smaller than current size should throw exception
        try {
            pop.setPopulationLimit(1);
            fail("Expected NumberIsTooSmallException / IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }

        // Setting valid limit
        pop.setPopulationLimit(3);
        assertEquals(3, pop.populLimit()); // or getPopulationLimit
    }

    @Test
    public void testAddChromosome() {
        ConcreteListPopulation pop = new ConcreteListPopulation(2);
        pop.addChromosome(new DummyChromosome(1.0));
        assertEquals(1, pop.getPopulationSize());

        pop.addChromosome(new DummyChromosome(2.0));
        assertEquals(2, pop.getPopulationSize());

        // Adding when full should throw exception
        try {
            pop.addChromosome(new DummyChromosome(3.0));
            fail("Expected NumberIsTooLargeException / IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testAddAll() {
        ConcreteListPopulation pop = new ConcreteListPopulation(3);
        List<Chromosome> batch = new ArrayList<Chromosome>();
        batch.add(new DummyChromosome(1.0));
        batch.add(new DummyChromosome(2.0));

        pop.addChromosomeList(batch);
        assertEquals(2, pop.getPopulationSize());

        // Add more that exceeds limit
        List<Chromosome> excess = new ArrayList<Chromosome>();
        excess.add(new DummyChromosome(3.0));
        excess.add(new DummyChromosome(4.0));

        try {
            pop.addChromosomeList(excess);
            fail("Expected NumberIsTooLargeException / IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testGetBestChromosome() {
        ConcreteListPopulation pop = new ConcreteListPopulation(5);
        assertNull(pop.getBestChromosome());

        Chromosome c1 = new DummyChromosome(1.0);
        Chromosome c2 = new DummyChromosome(5.0);
        Chromosome c3 = new DummyChromosome(3.0);

        pop.addChromosome(c1);
        pop.addChromosome(c2);
        pop.addChromosome(c3);

        assertEquals(c2, pop.getBestChromosome());
    }

    @Test
    public void testIteratorAndGetChromosomes() {
        ConcreteListPopulation pop = new ConcreteListPopulation(5);
        Chromosome c1 = new DummyChromosome(1.0);
        Chromosome c2 = new DummyChromosome(2.0);

        pop.addChromosome(c1);
        pop.addChromosome(c2);

        List<Chromosome> chroms = pop.getChromosomes();
        assertEquals(2, chroms.size());

        Iterator<Chromosome> iter = pop.iterator();
        assertNotNull(iter);
        assertTrue(iter.hasNext());
        assertEquals(c1, iter.next());
        assertTrue(iter.hasNext());
        assertEquals(c2, iter.next());
        assertFalse(iter.hasNext());
    }
}