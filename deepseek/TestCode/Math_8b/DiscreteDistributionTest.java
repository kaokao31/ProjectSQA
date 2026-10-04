package org.apache.commons.math3.distribution;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

/**
 * Test suite for DiscreteDistribution (Defects4J style).
 * Assumes standard methods: probability, cumulativeProbability,
 * inverseCumulativeProbability, sample, getSupportLowerBound,
 * getSupportUpperBound, and constructor with double[] probabilities.
 */
public class DiscreteDistributionTest {

    private static final double EPSILON = 1e-10;

    // Test data: three outcomes with probabilities 0.2, 0.3, 0.5
    private double[] validProbs;
    private DiscreteDistribution dist;

    @Before
    public void setUp() {
        validProbs = new double[] {0.2, 0.3, 0.5};
        dist = new DiscreteDistribution(validProbs);
    }

    // ---------- Constructor tests ----------
    @Test(expected = NullPointerException.class)
    public void testConstructorNullArray() {
        new DiscreteDistribution(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorEmptyArray() {
        new DiscreteDistribution(new double[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNegativeProbability() {
        new DiscreteDistribution(new double[] {0.5, -0.1, 0.6});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorZeroSum() {
        new DiscreteDistribution(new double[] {0.0, 0.0, 0.0});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNaNProbability() {
        new DiscreteDistribution(new double[] {0.5, Double.NaN, 0.5});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorInfiniteProbability() {
        new DiscreteDistribution(new double[] {0.5, Double.POSITIVE_INFINITY, 0.5});
    }

    @Test
    public void testConstructorValid() {
        // Should not throw
        new DiscreteDistribution(new double[] {1.0});
        new DiscreteDistribution(new double[] {0.1, 0.2, 0.3, 0.4});
    }

    // ---------- probability(int x) ----------
    @Test
    public void testProbabilityValid() {
        assertEquals(0.2, dist.probability(0), EPSILON);
        assertEquals(0.3, dist.probability(1), EPSILON);
        assertEquals(0.5, dist.probability(2), EPSILON);
    }

    @Test
    public void testProbabilityOutOfRange() {
        assertEquals(0.0, dist.probability(-1), EPSILON);
        assertEquals(0.0, dist.probability(3), EPSILON);
        assertEquals(0.0, dist.probability(Integer.MAX_VALUE), EPSILON);
        assertEquals(0.0, dist.probability(Integer.MIN_VALUE), EPSILON);
    }

    // ---------- cumulativeProbability(int x) ----------
    @Test
    public void testCumulativeProbabilityValid() {
        assertEquals(0.0, dist.cumulativeProbability(-1), EPSILON);
        assertEquals(0.2, dist.cumulativeProbability(0), EPSILON);
        assertEquals(0.5, dist.cumulativeProbability(1), EPSILON);
        assertEquals(1.0, dist.cumulativeProbability(2), EPSILON);
        assertEquals(1.0, dist.cumulativeProbability(3), EPSILON);
    }

    @Test
    public void testCumulativeProbabilityLarge() {
        assertEquals(1.0, dist.cumulativeProbability(Integer.MAX_VALUE), EPSILON);
        assertEquals(0.0, dist.cumulativeProbability(Integer.MIN_VALUE), EPSILON);
    }

    // ---------- inverseCumulativeProbability(double p) ----------
    @Test(expected = IllegalArgumentException.class)
    public void testInverseCumulativeProbabilityNegative() {
        dist.inverseCumulativeProbability(-0.1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInverseCumulativeProbabilityAboveOne() {
        dist.inverseCumulativeProbability(1.1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInverseCumulativeProbabilityNaN() {
        dist.inverseCumulativeProbability(Double.NaN);
    }

    @Test
    public void testInverseCumulativeProbabilityBoundary() {
        assertEquals(0, dist.inverseCumulativeProbability(0.0));
        assertEquals(0, dist.inverseCumulativeProbability(0.2));
        assertEquals(1, dist.inverseCumulativeProbability(0.5));
        assertEquals(2, dist.inverseCumulativeProbability(1.0));
    }

    @Test
    public void testInverseCumulativeProbabilityInside() {
        assertEquals(0, dist.inverseCumulativeProbability(0.1));
        assertEquals(1, dist.inverseCumulativeProbability(0.3));
        assertEquals(2, dist.inverseCumulativeProbability(0.8));
    }

    // ---------- sample() ----------
    @Test
    public void testSampleWithinRange() {
        for (int i = 0; i < 100; i++) {
            int s = dist.sample();
            assertTrue("Sample out of range: " + s, s >= 0 && s <= 2);
        }
    }

    @Test
    public void testSampleConsistency() {
        // Empirical check: sample should roughly match probabilities
        int[] counts = new int[3];
        int trials = 10000;
        for (int i = 0; i < trials; i++) {
            counts[dist.sample()]++;
        }
        // Allow 5% tolerance
        assertEquals(0.2, counts[0] / (double) trials, 0.05);
        assertEquals(0.3, counts[1] / (double) trials, 0.05);
        assertEquals(0.5, counts[2] / (double) trials, 0.05);
    }

    // ---------- getSupportLowerBound / getSupportUpperBound ----------
    @Test
    public void testSupportBounds() {
        assertEquals(0, dist.getSupportLowerBound());
        assertEquals(2, dist.getSupportUpperBound());
    }

    // ---------- Edge case: single outcome ----------
    @Test
    public void testSingleOutcome() {
        DiscreteDistribution single = new DiscreteDistribution(new double[] {1.0});
        assertEquals(0, single.getSupportLowerBound());
        assertEquals(0, single.getSupportUpperBound());
        assertEquals(1.0, single.cumulativeProbability(0), EPSILON);
        assertEquals(0, single.inverseCumulativeProbability(0.5));
        assertEquals(0, single.sample());
    }

    // ---------- Edge case: two outcomes ----------
    @Test
    public void testTwoOutcomes() {
        DiscreteDistribution two = new DiscreteDistribution(new double[] {0.7, 0.3});
        assertEquals(0, two.inverseCumulativeProbability(0.0));
        assertEquals(0, two.inverseCumulativeProbability(0.7));
        assertEquals(1, two.inverseCumulativeProbability(0.8));
        assertEquals(1, two.inverseCumulativeProbability(1.0));
    }

    // ---------- Large number of outcomes ----------
    @Test
    public void testManyOutcomes() {
        int n = 100;
        double[] probs = new double[n];
        for (int i = 0; i < n; i++) {
            probs[i] = 1.0 / n;
        }
        DiscreteDistribution many = new DiscreteDistribution(probs);
        assertEquals(0, many.getSupportLowerBound());
        assertEquals(n - 1, many.getSupportUpperBound());
        // Check cumulative at last element
        assertEquals(1.0, many.cumulativeProbability(n - 1), EPSILON);
        // Sample should be within [0, n-1]
        for (int i = 0; i < 100; i++) {
            int s = many.sample();
            assertTrue("Sample out of range: " + s, s >= 0 && s < n);
        }
    }

    // ---------- Test for potential overflow in cumulative sum ----------
    @Test
    public void testCumulativeSumPrecision() {
        // Very small probabilities that sum to 1
        double[] tiny = new double[1000];
        for (int i = 0; i < tiny.length; i++) {
            tiny[i] = 1.0 / tiny.length;
        }
        DiscreteDistribution tinyDist = new DiscreteDistribution(tiny);
        // cumulativeProbability at last index should be 1.0
        assertEquals(1.0, tinyDist.cumulativeProbability(tiny.length - 1), EPSILON);
        // inverseCumulativeProbability at 1.0 should return last index
        assertEquals(tiny.length - 1, tinyDist.inverseCumulativeProbability(1.0));
    }

    // ---------- Test for non-finite probabilities (already covered in constructor) ----------
    // Additional: test that sample does not hang or produce NaN
    @Test(timeout = 1000)
    public void testSamplePerformance() {
        for (int i = 0; i < 1000; i++) {
            int s = dist.sample();
            assertFalse("Sample produced NaN", Double.isNaN(s));
        }
    }
}