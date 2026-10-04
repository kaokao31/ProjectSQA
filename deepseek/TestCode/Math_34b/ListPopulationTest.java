package org.apache.commons.math3.genetics;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test suite for ListPopulation class (Defects4J Math-34).
 * Designed to achieve maximum coverage and trigger the known bug
 * where iterator().remove() throws UnsupportedOperationException.
 */
public class ListPopulationTest {

    private static final int DEFAULT_LIMIT = 10;
    private ListPopulation population;
    private Chromosome chromosome1;
    private Chromosome chromosome2;

    @Before
    public void setUp() {
        chromosome1 = new DummyChromosome(1);
        chromosome2 = new DummyChromosome(2);
        population = new ListPopulation(DEFAULT_LIMIT) {
            @Override
            public ListPopulation nextGeneration() {
                return null; // not used in tests
            }
        };
    }

    // ---------- Constructor Tests ----------

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNegativeLimit() {
        new ListPopulation(-1) {
            @Override
            public ListPopulation nextGeneration() { return null; }
        };
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorZeroLimit() {
        new ListPopulation(0) {
            @Override
            public ListPopulation nextGeneration() { return null; }
        };
    }

    @Test
    public void testConstructorWithLimit() {
        ListPopulation pop = new ListPopulation(5) {
            @Override
            public ListPopulation nextGeneration() { return null; }
        };
        Assert.assertEquals(5, pop.getPopulationLimit());
        Assert.assertTrue(pop.getChromosomes().isEmpty());
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullChromosomes() {
        new ListPopulation(null, 10) {
            @Override
            public ListPopulation nextGeneration() { return null; }
        };
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorChromosomesExceedLimit() {
        List<Chromosome> list = new ArrayList<>();
        list.add(chromosome1);
        list.add(chromosome2);
        new ListPopulation(list, 1) {
            @Override
            public ListPopulation nextGeneration() { return null; }
        };
    }

    @Test
    public void testConstructorWithChromosomes() {
        List<Chromosome> list = new ArrayList<>();
        list.add(chromosome1);
        list.add(chromosome2);
        ListPopulation pop = new ListPopulation(list, 10) {
            @Override
            public ListPopulation nextGeneration() { return null; }
        };
        Assert.assertEquals(2, pop.getPopulationSize());
        Assert.assertEquals(10, pop.getPopulationLimit());
    }

    // ---------- addChromosome Tests ----------

    @Test
    public void testAddChromosome() {
        population.addChromosome(chromosome1);
        Assert.assertEquals(1, population.getPopulationSize());
        Assert.assertSame(chromosome1, population.getChromosome(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddChromosomeNull() {
        population.addChromosome(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddChromosomeExceedsLimit() {
        ListPopulation pop = new ListPopulation(1) {
            @Override
            public ListPopulation nextGeneration() { return null; }
        };
        pop.addChromosome(chromosome1);
        pop.addChromosome(chromosome2); // should throw
    }

    // ---------- getChromosome Tests ----------

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetChromosomeNegativeIndex() {
        population.getChromosome(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetChromosomeIndexTooLarge() {
        population.addChromosome(chromosome1);
        population.getChromosome(1);
    }

    @Test
    public void testGetChromosomeValid() {
        population.addChromosome(chromosome1);
        Assert.assertSame(chromosome1, population.getChromosome(0));
    }

    // ---------- getChromosomes Tests ----------

    @Test(expected = UnsupportedOperationException.class)
    public void testGetChromosomesUnmodifiable() {
        population.addChromosome(chromosome1);
        List<Chromosome> list = population.getChromosomes();
        list.add(chromosome2); // should throw
    }

    @Test
    public void testGetChromosomesReturnsCopy() {
        population.addChromosome(chromosome1);
        List<Chromosome> list = population.getChromosomes();
        Assert.assertEquals(1, list.size());
        Assert.assertSame(chromosome1, list.get(0));
    }

    // ---------- setChromosomes Tests ----------

    @Test(expected = NullPointerException.class)
    public void testSetChromosomesNull() {
        population.setChromosomes(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetChromosomesExceedsLimit() {
        List<Chromosome> list = new ArrayList<>();
        list.add(chromosome1);
        list.add(chromosome2);
        ListPopulation pop = new ListPopulation(1) {
            @Override
            public ListPopulation nextGeneration() { return null; }
        };
        pop.setChromosomes(list);
    }

    @Test
    public void testSetChromosomes() {
        List<Chromosome> list = new ArrayList<>();
        list.add(chromosome1);
        population.setChromosomes(list);
        Assert.assertEquals(1, population.getPopulationSize());
        Assert.assertSame(chromosome1, population.getChromosome(0));
    }

    // ---------- addChromosomes Tests ----------

    @Test
    public void testAddChromosomes() {
        List<Chromosome> list = new ArrayList<>();
        list.add(chromosome1);
        list.add(chromosome2);
        population.addChromosomes(list);
        Assert.assertEquals(2, population.getPopulationSize());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddChromosomesExceedsLimit() {
        ListPopulation pop = new ListPopulation(1) {
            @Override
            public ListPopulation nextGeneration() { return null; }
        };
        List<Chromosome> list = new ArrayList<>();
        list.add(chromosome1);
        list.add(chromosome2);
        pop.addChromosomes(list);
    }

    // ---------- clear Tests ----------

    @Test
    public void testClear() {
        population.addChromosome(chromosome1);
        population.clear();
        Assert.assertEquals(0, population.getPopulationSize());
        Assert.assertTrue(population.getChromosomes().isEmpty());
    }

    // ---------- isEmpty Tests ----------

    @Test
    public void testIsEmptyTrue() {
        Assert.assertTrue(population.getChromosomes().isEmpty());
    }

    @Test
    public void testIsEmptyFalse() {
        population.addChromosome(chromosome1);
        Assert.assertFalse(population.getChromosomes().isEmpty());
    }

    // ---------- getPopulationLimit Tests ----------

    @Test
    public void testGetPopulationLimit() {
        Assert.assertEquals(DEFAULT_LIMIT, population.getPopulationLimit());
    }

    // ---------- iterator Tests (bug detection) ----------

    @Test
    public void testIterator() {
        population.addChromosome(chromosome1);
        population.addChromosome(chromosome2);
        Iterator<Chromosome> iter = population.iterator();
        Assert.assertTrue(iter.hasNext());
        Assert.assertSame(chromosome1, iter.next());
        Assert.assertTrue(iter.hasNext());
        Assert.assertSame(chromosome2, iter.next());
        Assert.assertFalse(iter.hasNext());
    }

    @Test
    public void testIteratorRemoveSupported() {
        // This test triggers the known bug in Defects4J Math-34:
        // iterator().remove() should work but throws UnsupportedOperationException
        population.addChromosome(chromosome1);
        Iterator<Chromosome> iter = population.iterator();
        iter.next();
        iter.remove(); // should not throw
        Assert.assertEquals(0, population.getPopulationSize());
    }

    @Test(expected = IllegalStateException.class)
    public void testIteratorRemoveWithoutNext() {
        population.addChromosome(chromosome1);
        Iterator<Chromosome> iter = population.iterator();
        iter.remove(); // should throw IllegalStateException
    }

    // ---------- Helper class ----------

    private static class DummyChromosome extends Chromosome {
        private final int id;

        DummyChromosome(int id) {
            this.id = id;
        }

        @Override
        public double fitness() {
            return id;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof DummyChromosome)) return false;
            DummyChromosome other = (DummyChromosome) obj;
            return id == other.id;
        }

        @Override
        public int hashCode() {
            return id;
        }
    }
}