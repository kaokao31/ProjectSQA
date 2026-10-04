package org.apache.commons.math3.distribution;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class HypergeometricDistributionTest {

    private HypergeometricDistribution standardDistribution;
    private HypergeometricDistribution exactDistribution;

    @Before
    public void setUp() {
        // populationSize = 10, numberofSuccesses = 5, sampleSize = 5
        standardDistribution = new HypergeometricDistribution(10, 5, 5);
        // Edge cases or specific bounds
        exactDistribution = new HypergeometricDistribution(20, 10, 10);
    }

    @Test(expected = NotPositiveException.class)
    public void testPreconditionsPopulationNotPositive() {
        new HypergeometricDistribution(0, 5, 5);
    }

    @Test(expected = NotPositiveException.class)
    public void testPreconditionsPopulationNegative() {
        new HypergeometricDistribution(-1, 5, 5);
    }

    @Test(expected = NumberIsTooLargeException.class)
    public void testPreconditionsSuccessesGreaterThanPopulation() {
        new HypergeometricDistribution(10, 11, 5);
    }

    @Test(expected = NotPositiveException.class)
    public void testPreconditionsSuccessesNegative() {
        new HypergeometricDistribution(10, -1, 5);
    }

    @Test(expected = NumberIsTooLargeException.class)
    public void testPreconditionsSampleSizeGreaterThanPopulation() {
        new HypergeometricDistribution(10, 5, 11);
    }

    @Test(expected = NotPositiveException.class)
    public void testPreconditionsSampleSizeNegative() {
        new HypergeometricDistribution(10, 5, -1);
    }

    @Test
    public void testAccessors() {
        Assert.assertEquals(10, standardDistribution.getPopulationSize());
        Assert.assertEquals(5, standardDistribution.getNumberOfSuccesses());
        Assert.assertEquals(5, standardDistribution.getSampleSize());
    }

    @Test
    public void testNumericalMean() {
        // Mean = sampleSize * (numberOfSuccesses / populationSize) = 5 * (5 / 10) = 2.5
        Assert.assertEquals(2.5, standardDistribution.getNumericalMean(), 1e-9);
    }

    @Test
    public void testNumericalVariance() {
        // Variance = [n * K * (N - K) * (N - n)] / [N^2 * (N - 1)]
        // = [5 * 5 * (10 - 5) * (10 - 5)] / [100 * 9]
        // = [25 * 5 * 5] / 900 = 625 / 900 = 0.694444...
        Assert.assertEquals(625.0 / 900.0, standardDistribution.getNumericalVariance(), 1e-9);
    }

    @Test
    public void testSupportBounds() {
        // max(0, sampleSize + numberOfSuccesses - populationSize) = max(0, 5 + 5 - 10) = 0
        Assert.assertEquals(0, standardDistribution. getSupportLowerBound());
        // min(sampleSize, numberOfSuccesses) = min(5, 5) = 5
        Assert.assertEquals(5, standardDistribution.getSupportUpperBound());
        Assert.assertTrue(standardDistribution.isSupportConnected());
    }

    @Test
    public void testSupportBoundsWithDifferentValues() {
        // population = 30, successes = 15, sample = 25
        // lower = max(0, 25 + 15 - 30) = 10
        // upper = min(25, 15) = 15
        HypergeometricDistribution dist = new HypergeometricDistribution(30, 15, 25);
        Assert.assertEquals(10, dist.getSupportLowerBound());
        Assert.assertEquals(15, dist.getSupportUpperBound());
    }

    @Test
    public void testProbability() {
        // P(X = k)
        double p0 = standardDistribution.probability(0);
        double p1 = standardDistribution.probability(1);
        double p2 = standardDistribution.probability(2);
        double p3 = standardDistribution.probability(3);
        double p4 = standardDistribution.probability(4);
        double p5 = standardDistribution.probability(5);

        Assert.assertTrue(p0 >= 0.0);
        Assert.assertTrue(p1 >= 0.0);
        Assert.assertTrue(p2 >= 0.0);
        Assert.assertTrue(p3 >= 0.0);
        Assert.assertTrue(p4 >= 0.0);
        Assert.assertTrue(p5 >= 0.0);

        // Out of support probabilities
        Assert.assertEquals(0.0, standardDistribution.probability(-1), 1e-9);
        Assert.assertEquals(0.0, standardDistribution.probability(6), 1e-9);
    }

    @Test
    public void testCumulativeProbability() {
        double cp0 = standardDistribution.cumulativeProbability(0);
        double cp5 = standardDistribution.cumulativeProbability(5);

        Assert.assertEquals(standardDistribution.probability(0), cp0, 1e-9);
        Assert.assertEquals(1.0, cp5, 1e-9);

        // Out of support cumulative probabilities
        Assert.assertEquals(0.0, standardDistribution.cumulativeProbability(-1), 1e-9);
        Assert.assertEquals(1.0, standardDistribution.cumulativeProbability(10), 1e-9);
    }

    @Test
    public void testUpperCumulativeProbability() {
        // Test edge values for upper cumulative probability if implemented via AbstractIntegerDistribution or overridden
        double ucp = standardDistribution.upperCumulativeProbability(2);
        Assert.assertTrue(ucp >= 0.0 && ucp <= 1.0);
    }

    @Test
    public void testDegenerateDistributions() {
        // 0 successes in population
        HypergeometricDistribution zeroSuccess = new HypergeometricDistribution(10, 0, 5);
        Assert.assertEquals(0.0, zeroSuccess.getNumericalMean(), 1e-9);
        Assert.assertEquals(0, zeroSuccess.getSupportLowerBound());
        Assert.assertEquals(0, zeroSuccess.getSupportUpperBound());
        Assert.assertEquals(1.0, zeroSuccess.probability(0), 1e-9);

        // population successes equals population size
        HypergeometricDistribution allSuccess = new HypergeometricDistribution(10, 10, 5);
        Assert.assertEquals(5.0, allSuccess.getNumericalMean(), 1e-9);
        Assert.assertEquals(5, allSuccess.getSupportLowerBound());
        Assert.assertEquals(5, allSuccess.getSupportUpperBound());
        Assert.assertEquals(1.0, allSuccess.probability(5), 1e-9);
    }

    @Test
    public void testSampling() {
        HypergeometricDistribution.Sampler sampler = standardDistribution.createSampler(null);
        Assert.assertNotNull(sampler);
        int sampleVal = standardDistribution.sample();
        Assert.assertTrue(sampleVal >= standardDistribution.getSupportLowerBound());
        Assert.assertTrue(sampleVal <= standardDistribution.getSupportUpperBound());

        int[] sampleArray = standardDistribution.sample(10);
        Assert.assertEquals(10, sampleArray.length);
        for (int s : sampleArray) {
            Assert.assertTrue(s >= standardDistribution.getSupportLowerBound());
            Assert.assertTrue(s <= standardDistribution.getSupportUpperBound());
        }
    }
}