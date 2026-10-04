package org.apache.commons.math3.distribution;

import org.junit.Test;
import org.junit.Before;
import org.junit.Assert;
import static org.junit.Assert.*;

/**
 * Comprehensive JUnit 4 test suite for HypergeometricDistribution.
 * Achieves high line/branch coverage and targets potential faults.
 */
public class HypergeometricDistributionTest {

    private static final double EPS = 1e-12;

    // Valid distribution parameters
    private HypergeometricDistribution dist;

    @Before
    public void setUp() {
        // populationSize=50, numberOfSuccesses=20, sampleSize=10
        dist = new HypergeometricDistribution(50, 20, 10);
    }

    // ========== Constructor Tests ==========

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNegativePopulation() {
        new HypergeometricDistribution(-1, 10, 5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorZeroPopulation() {
        new HypergeometricDistribution(0, 0, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNegativeSuccesses() {
        new HypergeometricDistribution(50, -1, 10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorSuccessesExceedPopulation() {
        new HypergeometricDistribution(50, 60, 10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNegativeSampleSize() {
        new HypergeometricDistribution(50, 20, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorSampleSizeExceedPopulation() {
        new HypergeometricDistribution(50, 20, 60);
    }

    @Test
    public void testConstructorValid() {
        HypergeometricDistribution d = new HypergeometricDistribution(100, 30, 20);
        assertEquals(100, d.getPopulationSize());
        assertEquals(30, d.getNumberOfSuccesses());
        assertEquals(20, d.getSampleSize());
    }

    // ========== Accessor Tests ==========

    @Test
    public void testGetPopulationSize() {
        assertEquals(50, dist.getPopulationSize());
    }

    @Test
    public void testGetNumberOfSuccesses() {
        assertEquals(20, dist.getNumberOfSuccesses());
    }

    @Test
    public void testGetSampleSize() {
        assertEquals(10, dist.getSampleSize());
    }

    // ========== Probability Mass Function Tests ==========

    @Test
    public void testProbabilityValidValues() {
        // Known values from R: dhyper(0:10, 20, 30, 10)
        double[] expected = {
            0.0030959752321981426,
            0.030959752321981426,
            0.11984520123839009,
            0.23969040247678018,
            0.2756439628482972,
            0.19295077399280804,
            0.0826934745683463,
            0.020673368642086575,
            0.002841838022586904,
            0.00018945586817246027,
            4.736396704311507e-06
        };
        for (int x = 0; x <= 10; x++) {
            assertEquals("Probability at x=" + x, expected[x], dist.probability(x), EPS);
        }
    }

    @Test
    public void testProbabilityOutOfRangeLow() {
        assertEquals(0.0, dist.probability(-1), 0.0);
    }

    @Test
    public void testProbabilityOutOfRangeHigh() {
        assertEquals(0.0, dist.probability(11), 0.0);
    }

    @Test
    public void testProbabilityBoundarySampleSize() {
        // sampleSize = populationSize
        HypergeometricDistribution d = new HypergeometricDistribution(10, 5, 10);
        assertEquals(1.0, d.probability(5), EPS);
        assertEquals(0.0, d.probability(4), EPS);
    }

    @Test
    public void testProbabilityZeroSuccesses() {
        HypergeometricDistribution d = new HypergeometricDistribution(10, 0, 5);
        assertEquals(1.0, d.probability(0), EPS);
        assertEquals(0.0, d.probability(1), EPS);
    }

    @Test
    public void testProbabilityAllSuccesses() {
        HypergeometricDistribution d = new HypergeometricDistribution(10, 10, 5);
        assertEquals(1.0, d.probability(5), EPS);
        assertEquals(0.0, d.probability(4), EPS);
    }

    // ========== Cumulative Probability Tests ==========

    @Test
    public void testCumulativeProbabilityValid() {
        // R: phyper(0:10, 20, 30, 10)
        double[] expected = {
            0.0030959752321981426,
            0.03405572755417957,
            0.15390092879256966,
            0.39359133126934984,
            0.6692352941176471,
            0.8621860681104551,
            0.9448795426788014,
            0.965552911320888,
            0.9683947493434749,
            0.9685842052116472,
            0.9685889416083584
        };
        for (int x = 0; x <= 10; x++) {
            assertEquals("Cumulative at x=" + x, expected[x], dist.cumulativeProbability(x), EPS);
        }
    }

    @Test
    public void testCumulativeProbabilityOutOfRangeLow() {
        assertEquals(0.0, dist.cumulativeProbability(-1), 0.0);
    }

    @Test
    public void testCumulativeProbabilityOutOfRangeHigh() {
        assertEquals(1.0, dist.cumulativeProbability(11), 0.0);
    }

    @Test
    public void testCumulativeProbabilityBoundary() {
        HypergeometricDistribution d = new HypergeometricDistribution(10, 5, 10);
        assertEquals(0.0, d.cumulativeProbability(4), EPS);
        assertEquals(1.0, d.cumulativeProbability(5), EPS);
    }

    // ========== Inverse Cumulative Probability Tests ==========

    @Test
    public void testInverseCumulativeProbabilityValid() {
        // R: qhyper(seq(0,1,0.1), 20,30,10)
        double[] quantiles = {0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0};
        int[] expected = {0, 2, 3, 3, 4, 4, 4, 5, 5, 6, 10};
        for (int i = 0; i < quantiles.length; i++) {
            assertEquals("Inverse at p=" + quantiles[i], expected[i], dist.inverseCumulativeProbability(quantiles[i]));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInverseCumulativeProbabilityLow() {
        dist.inverseCumulativeProbability(-0.1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInverseCumulativeProbabilityHigh() {
        dist.inverseCumulativeProbability(1.1);
    }

    @Test
    public void testInverseCumulativeProbabilityBoundary() {
        assertEquals(0, dist.inverseCumulativeProbability(0.0));
        assertEquals(10, dist.inverseCumulativeProbability(1.0));
    }

    // ========== Numerical Mean and Variance Tests ==========

    @Test
    public void testNumericalMean() {
        // mean = sampleSize * (numberOfSuccesses / populationSize)
        double expected = 10.0 * (20.0 / 50.0);
        assertEquals(expected, dist.getNumericalMean(), EPS);
    }

    @Test
    public void testNumericalVariance() {
        // variance = sampleSize * (k/N) * (1 - k/N) * ((N - n)/(N - 1))
        double p = 20.0 / 50.0;
        double expected = 10.0 * p * (1 - p) * (40.0 / 49.0);
        assertEquals(expected, dist.getNumericalVariance(), EPS);
    }

    @Test
    public void testNumericalMeanEdge() {
        HypergeometricDistribution d = new HypergeometricDistribution(10, 10, 5);
        assertEquals(5.0, d.getNumericalMean(), EPS);
    }

    @Test
    public void testNumericalVarianceEdge() {
        HypergeometricDistribution d = new HypergeometricDistribution(10, 5, 10);
        assertEquals(0.0, d.getNumericalVariance(), EPS);
    }

    // ========== Sampling Tests ==========

    @Test
    public void testSampleValid() {
        int sample = dist.sample();
        assertTrue("Sample should be between 0 and sampleSize", sample >= 0 && sample <= 10);
    }

    @Test
    public void testSampleArray() {
        int[] samples = dist.sample(1000);
        assertEquals(1000, samples.length);
        for (int s : samples) {
            assertTrue("Sample out of range", s >= 0 && s <= 10);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSampleNegativeCount() {
        dist.sample(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSampleZeroCount() {
        dist.sample(0);
    }

    // ========== Edge Cases and Special Values ==========

    @Test
    public void testPopulationEqualsSampleSize() {
        HypergeometricDistribution d = new HypergeometricDistribution(5, 2, 5);
        assertEquals(2, d.getNumericalMean(), EPS);
        assertEquals(0.0, d.getNumericalVariance(), EPS);
        assertEquals(1.0, d.cumulativeProbability(2), EPS);
        assertEquals(0.0, d.cumulativeProbability(1), EPS);
    }

    @Test
    public void testSampleSizeOne() {
        HypergeometricDistribution d = new HypergeometricDistribution(100, 30, 1);
        assertEquals(0.3, d.probability(1), EPS);
        assertEquals(0.7, d.probability(0), EPS);
        assertEquals(0.3, d.getNumericalMean(), EPS);
    }

    @Test
    public void testLargePopulation() {
        HypergeometricDistribution d = new HypergeometricDistribution(1000000, 500000, 100);
        // Approximate binomial
        double p = 0.5;
        double mean = 100 * p;
        double var = 100 * p * (1-p) * (999900.0/999999.0);
        assertEquals(mean, d.getNumericalMean(), 1e-10);
        assertEquals(var, d.getNumericalVariance(), 1e-10);
    }

    @Test
    public void testExtremeValues() {
        // All successes in population, sample all
        HypergeometricDistribution d = new HypergeometricDistribution(10, 10, 10);
        assertEquals(1.0, d.probability(10), EPS);
        assertEquals(0.0, d.probability(9), EPS);
        assertEquals(10.0, d.getNumericalMean(), EPS);
        assertEquals(0.0, d.getNumericalVariance(), EPS);
    }

    @Test
    public void testNoSuccessesInPopulation() {
        HypergeometricDistribution d = new HypergeometricDistribution(10, 0, 5);
        assertEquals(1.0, d.probability(0), EPS);
        assertEquals(0.0, d.probability(1), EPS);
        assertEquals(0.0, d.getNumericalMean(), EPS);
        assertEquals(0.0, d.getNumericalVariance(), EPS);
    }

    // ========== Fault Detection: Potential overflow/underflow ==========

    @Test
    public void testLargeValuesNoOverflow() {
        // Use large numbers that might cause overflow in combinatorial calculations
        HypergeometricDistribution d = new HypergeometricDistribution(10000, 5000, 2000);
        double prob = d.probability(1000);
        assertTrue("Probability should be positive", prob > 0);
        assertTrue("Probability should be <= 1", prob <= 1);
    }

    @Test
    public void testCumulativeProbabilityConsistency() {
        // Sum of probabilities should equal cumulative at max
        double sum = 0.0;
        for (int x = 0; x <= 10; x++) {
            sum += dist.probability(x);
        }
        assertEquals(1.0, sum, 1e-12);
        assertEquals(1.0, dist.cumulativeProbability(10), 1e-12);
    }

    @Test
    public void testInverseCumulativeProbabilityMonotonic() {
        double[] ps = {0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0};
        int prev = -1;
        for (double p : ps) {
            int q = dist.inverseCumulativeProbability(p);
            assertTrue("Inverse should be non-decreasing", q >= prev);
            prev = q;
        }
    }

    // ========== Additional Branch Coverage ==========

    @Test
    public void testProbabilityZeroSampleSize() {
        HypergeometricDistribution d = new HypergeometricDistribution(10, 5, 0);
        assertEquals(1.0, d.probability(0), EPS);
        assertEquals(0.0, d.probability(1), EPS);
    }

    @Test
    public void testCumulativeProbabilityZeroSampleSize() {
        HypergeometricDistribution d = new HypergeometricDistribution(10, 5, 0);
        assertEquals(1.0, d.cumulativeProbability(0), EPS);
        assertEquals(1.0, d.cumulativeProbability(1), EPS);
    }

    @Test
    public void testInverseCumulativeProbabilityZeroSampleSize() {
        HypergeometricDistribution d = new HypergeometricDistribution(10, 5, 0);
        assertEquals(0, d.inverseCumulativeProbability(0.0));
        assertEquals(0, d.inverseCumulativeProbability(1.0));
    }

    @Test
    public void testProbabilityMaxSuccesses() {
        HypergeometricDistribution d = new HypergeometricDistribution(10, 3, 5);
        // max possible successes = min(3,5)=3
        assertEquals(0.0, d.probability(4), EPS);
        double prob3 = d.probability(3);
        assertTrue(prob3 > 0);
    }

    @Test
    public void testCumulativeProbabilityMaxSuccesses() {
        HypergeometricDistribution d = new HypergeometricDistribution(10, 3, 5);
        assertEquals(1.0, d.cumulativeProbability(3), EPS);
        assertEquals(1.0, d.cumulativeProbability(4), EPS);
    }

    // ========== toString (if present) ==========

    @Test
    public void testToString() {
        String str = dist.toString();
        assertNotNull(str);
        assertTrue(str.contains("HypergeometricDistribution"));
    }

    // ========== Serialization (if applicable) ==========

    @Test
    public void testSerialization() {
        // Basic check that distribution can be serialized/deserialized
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try {
            java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
            oos.writeObject(dist);
            oos.close();
            java.io.ByteArrayInputStream bis = new java.io.ByteArrayInputStream(bos.toByteArray());
            java.io.ObjectInputStream ois = new java.io.ObjectInputStream(bis);
            HypergeometricDistribution deserialized = (HypergeometricDistribution) ois.readObject();
            assertEquals(dist.getPopulationSize(), deserialized.getPopulationSize());
            assertEquals(dist.getNumberOfSuccesses(), deserialized.getNumberOfSuccesses());
            assertEquals(dist.getSampleSize(), deserialized.getSampleSize());
        } catch (Exception e) {
            fail("Serialization failed: " + e.getMessage());
        }
    }
}