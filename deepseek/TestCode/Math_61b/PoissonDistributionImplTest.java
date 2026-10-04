package org.apache.commons.math.distribution;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

/**
 * Test class for PoissonDistributionImpl.
 * Designed to achieve high coverage and detect faults (e.g., bug 61).
 */
public class PoissonDistributionImplTest {

    private static final double DEFAULT_EPSILON = 1e-12;
    private static final int DEFAULT_MAX_ITERATIONS = 10000000;

    private PoissonDistributionImpl distribution;

    @Before
    public void setUp() {
        // Default distribution with mean 1.0
        distribution = new PoissonDistributionImpl(1.0);
    }

    // ---------- Constructor Tests ----------

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNegativeMean() {
        new PoissonDistributionImpl(-1.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorZeroMean() {
        new PoissonDistributionImpl(0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNegativeEpsilon() {
        new PoissonDistributionImpl(1.0, -1.0, DEFAULT_MAX_ITERATIONS);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorZeroEpsilon() {
        new PoissonDistributionImpl(1.0, 0.0, DEFAULT_MAX_ITERATIONS);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNegativeMaxIterations() {
        new PoissonDistributionImpl(1.0, DEFAULT_EPSILON, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorZeroMaxIterations() {
        new PoissonDistributionImpl(1.0, DEFAULT_EPSILON, 0);
    }

    @Test
    public void testConstructorValid() {
        PoissonDistributionImpl d = new PoissonDistributionImpl(2.5);
        assertEquals(2.5, d.getMean(), 1e-15);
    }

    @Test
    public void testConstructorWithEpsilonAndMaxIterations() {
        PoissonDistributionImpl d = new PoissonDistributionImpl(3.0, 1e-10, 1000);
        assertEquals(3.0, d.getMean(), 1e-15);
    }

    // ---------- getMean() Tests ----------

    @Test
    public void testGetMean() {
        assertEquals(1.0, distribution.getMean(), 1e-15);
    }

    // ---------- probability(int x) Tests ----------

    @Test
    public void testProbabilityNegativeX() {
        assertEquals(0.0, distribution.probability(-1), 1e-15);
    }

    @Test
    public void testProbabilityZero() {
        // P(X=0) for mean=1.0 = e^-1 ≈ 0.36787944117144233
        assertEquals(Math.exp(-1.0), distribution.probability(0), 1e-15);
    }

    @Test
    public void testProbabilityOne() {
        // P(X=1) = e^-1 * 1^1 / 1! = e^-1
        assertEquals(Math.exp(-1.0), distribution.probability(1), 1e-15);
    }

    @Test
    public void testProbabilityLargeX() {
        // For mean=1.0, probability for x=100 should be extremely small but non-negative
        double prob = distribution.probability(100);
        assertTrue(prob >= 0.0);
        assertTrue(prob < 1e-10);
    }

    @Test
    public void testProbabilitySumToOne() {
        // Sum of probabilities for x=0..20 should be close to 1
        double sum = 0.0;
        for (int x = 0; x <= 20; x++) {
            sum += distribution.probability(x);
        }
        assertEquals(1.0, sum, 1e-12);
    }

    // ---------- cumulativeProbability(int x) Tests ----------

    @Test
    public void testCumulativeProbabilityNegativeX() {
        assertEquals(0.0, distribution.cumulativeProbability(-1), 1e-15);
    }

    @Test
    public void testCumulativeProbabilityZero() {
        // P(X<=0) = P(X=0) = e^-1
        assertEquals(Math.exp(-1.0), distribution.cumulativeProbability(0), 1e-15);
    }

    @Test
    public void testCumulativeProbabilityOne() {
        // P(X<=1) = P(0)+P(1) = 2*e^-1
        assertEquals(2.0 * Math.exp(-1.0), distribution.cumulativeProbability(1), 1e-15);
    }

    @Test
    public void testCumulativeProbabilityLargeX() {
        // For large x, cumulative probability should approach 1
        double cum = distribution.cumulativeProbability(100);
        assertEquals(1.0, cum, 1e-12);
    }

    @Test
    public void testCumulativeProbabilityVeryLargeMean() {
        // Bug 61: large mean may cause overflow/underflow issues
        PoissonDistributionImpl largeMean = new PoissonDistributionImpl(1000.0);
        double cum = largeMean.cumulativeProbability(1000);
        // Should be around 0.5 (since mean=1000, median approx mean)
        assertTrue(cum > 0.4 && cum < 0.6);
    }

    @Test
    public void testCumulativeProbabilityExtremeMean() {
        // Very large mean to test numerical stability
        PoissonDistributionImpl extremeMean = new PoissonDistributionImpl(1e6);
        double cum = extremeMean.cumulativeProbability((int) 1e6);
        assertTrue(cum > 0.4 && cum < 0.6);
    }

    @Test
    public void testCumulativeProbabilitySmallMean() {
        PoissonDistributionImpl smallMean = new PoissonDistributionImpl(0.1);
        // P(X<=0) = e^-0.1 ≈ 0.9048374180359595
        assertEquals(Math.exp(-0.1), smallMean.cumulativeProbability(0), 1e-15);
        // P(X<=1) = e^-0.1 + 0.1*e^-0.1 = 1.1*e^-0.1
        assertEquals(1.1 * Math.exp(-0.1), smallMean.cumulativeProbability(1), 1e-15);
    }

    // ---------- sample() Tests ----------

    @Test
    public void testSampleNonNegative() {
        int sample = distribution.sample();
        assertTrue(sample >= 0);
    }

    @Test
    public void testSampleMultiple() {
        int[] samples = distribution.sample(1000);
        assertEquals(1000, samples.length);
        for (int s : samples) {
            assertTrue(s >= 0);
        }
    }

    // ---------- Edge Cases and Bug Triggers ----------

    @Test
    public void testProbabilityMeanZero() {
        // mean=0 is not allowed by constructor, but if it were, probability(0)=1, else 0
        // We'll test with a very small mean instead
        PoissonDistributionImpl tinyMean = new PoissonDistributionImpl(1e-10);
        assertEquals(1.0, tinyMean.probability(0), 1e-10);
        assertEquals(0.0, tinyMean.probability(1), 1e-10);
    }

    @Test
    public void testCumulativeProbabilityMeanZero() {
        PoissonDistributionImpl tinyMean = new PoissonDistributionImpl(1e-10);
        assertEquals(1.0, tinyMean.cumulativeProbability(0), 1e-10);
        assertEquals(1.0, tinyMean.cumulativeProbability(1), 1e-10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorMeanZeroThrows() {
        new PoissonDistributionImpl(0.0);
    }

    @Test
    public void testCumulativeProbabilityLargeXSmallMean() {
        PoissonDistributionImpl smallMean = new PoissonDistributionImpl(0.5);
        double cum = smallMean.cumulativeProbability(100);
        assertEquals(1.0, cum, 1e-12);
    }

    @Test
    public void testProbabilityLargeXSmallMean() {
        PoissonDistributionImpl smallMean = new PoissonDistributionImpl(0.5);
        double prob = smallMean.probability(100);
        assertEquals(0.0, prob, 1e-15);
    }

    // ---------- Additional Coverage: Internal methods ----------

    // Test that cumulativeProbability works for x values near Integer.MAX_VALUE
    @Test
    public void testCumulativeProbabilityMaxInt() {
        // This may be slow but should not throw
        PoissonDistributionImpl d = new PoissonDistributionImpl(1.0);
        double cum = d.cumulativeProbability(Integer.MAX_VALUE);
        assertEquals(1.0, cum, 1e-12);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNegativeMeanWithEpsilon() {
        new PoissonDistributionImpl(-2.0, DEFAULT_EPSILON, DEFAULT_MAX_ITERATIONS);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorZeroMeanWithEpsilon() {
        new PoissonDistributionImpl(0.0, DEFAULT_EPSILON, DEFAULT_MAX_ITERATIONS);
    }

    // Test that probability returns 0 for very large x (beyond support)
    @Test
    public void testProbabilityExtremeX() {
        PoissonDistributionImpl d = new PoissonDistributionImpl(10.0);
        double prob = d.probability(1000);
        assertEquals(0.0, prob, 1e-15);
    }

    // Test cumulative probability for mean=1, x=5 (known value)
    @Test
    public void testCumulativeProbabilityKnownValue() {
        // For mean=1, P(X<=5) = sum_{k=0}^5 e^{-1}/k! ≈ 0.9994058151824183
        double expected = 0.9994058151824183;
        assertEquals(expected, distribution.cumulativeProbability(5), 1e-12);
    }

    // Test that sample() returns values consistent with distribution
    @Test
    public void testSampleMean() {
        int n = 10000;
        int[] samples = distribution.sample(n);
        double sum = 0;
        for (int s : samples) {
            sum += s;
        }
        double sampleMean = sum / n;
        // Sample mean should be close to 1.0
        assertEquals(1.0, sampleMean, 0.1);
    }

    // Test that cumulativeProbability is monotonic
    @Test
    public void testCumulativeProbabilityMonotonic() {
        double prev = 0.0;
        for (int x = 0; x <= 10; x++) {
            double cur = distribution.cumulativeProbability(x);
            assertTrue(cur >= prev);
            prev = cur;
        }
    }

    // Test that probability is non-negative and sum to 1
    @Test
    public void testProbabilityNonNegative() {
        for (int x = 0; x <= 20; x++) {
            assertTrue(distribution.probability(x) >= 0.0);
        }
    }
}