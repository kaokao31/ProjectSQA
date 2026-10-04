package org.apache.commons.math.distribution;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PoissonDistributionImplTest {

    private PoissonDistributionImpl defaultDistribution;
    private PoissonDistributionImpl customDistribution;

    @Before
    public void setUp() {
        defaultDistribution = new PoissonDistributionImpl(1.0);
        customDistribution = new PoissonDistributionImpl(2.5);
    }

    @Test
    public void testConstructorValid() {
        PoissonDistributionImpl dist = new PoissonDistributionImpl(3.0);
        Assert.assertEquals(3.0, dist.getMean(), 1e-12);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorZeroMean() {
        new PoissonDistributionImpl(0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNegativeMean() {
        new PoissonDistributionImpl(-1.0);
    }

    @Test
    public void testSetMeanValid() {
        defaultDistribution.setMean(5.0);
        Assert.assertEquals(5.0, defaultDistribution.getMean(), 1e-12);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetMeanZero() {
        defaultDistribution.setMean(0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetMeanNegative() {
        defaultDistribution.setMean(-0.5);
    }

    @Test
    public void testGetMean() {
        Assert.assertEquals(1.0, defaultDistribution.getMean(), 1e-12);
        Assert.assertEquals(2.5, customDistribution.getMean(), 1e-12);
    }

    @Test
    public void testProbability() {
        // P(X = k) for Poisson(mean = 1.0)
        // P(X = 0) = e^(-1) * 1^0 / 0! = 1/e ≈ 0.36787944117
        Assert.assertEquals(Math.exp(-1.0), defaultDistribution.probability(0), 1e-10);
        // P(X = 1) = e^(-1) * 1^1 / 1! = 1/e ≈ 0.36787944117
        Assert.assertEquals(Math.exp(-1.0), defaultDistribution.probability(1), 1e-10);

        // Out of domain probabilities
        Assert.assertEquals(0.0, defaultDistribution.probability(-1), 1e-10);
    }

    @Test
    public void testCumulativeProbability() throws Exception {
        // P(X <= 0) for Poisson(1.0) = P(X = 0) = e^(-1)
        Assert.assertEquals(Math.exp(-1.0), defaultDistribution.cumulativeProbability(0), 1e-10);

        // Out of domain cumulative probabilities
        Assert.assertEquals(0.0, defaultDistribution.cumulativeProbability(-1), 1e-10);
    }

    @Test
    public void testNormalApproximation() throws Exception {
        // Using a larger mean to trigger normal approximation paths if any
        PoissonDistributionImpl largeMeanDist = new PoissonDistributionImpl(50.0);
        double prob = largeMeanDist.cumulativeProbability(40);
        Assert.assertTrue(prob >= 0.0 && prob <= 1.0);
        
        double upperProb = largeMeanDist.cumulativeProbability(60);
        Assert.assertTrue(upperProb >= 0.0 && upperProb <= 1.0);
    }

    @Test
    public void testInverseCumulativeProbability() throws Exception {
        Assert.assertEquals(0, defaultDistribution.inverseCumulativeProbability(0.1), 1e-10);
        Assert.assertEquals(0, defaultDistribution.inverseCumulativeProbability(0.36787944117), 1e-10);

        // Edge values for cumulative probability
        Assert.assertEquals(-1, defaultDistribution.inverseCumulativeProbability(0.0), 1e-10);
        Assert.assertEquals(Integer.MAX_VALUE, defaultDistribution.inverseCumulativeProbability(1.0), 1e-10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInverseCumulativeProbabilityOutOfBoundsLow() throws Exception {
        defaultDistribution.inverseCumulativeProbability(-0.1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInverseCumulativeProbabilityOutOfBoundsHigh() throws Exception {
        defaultDistribution.inverseCumulativeProbability(1.1);
    }

    @Test
    public void testGetDomainLowerBound() {
        Assert.assertEquals(0, defaultDistribution.getDomainLowerBound(1.0));
        Assert.assertEquals(0, customDistribution.getDomainLowerBound(2.5));
    }

    @Test
    public void testGetDomainUpperBound() {
        // Upper bound typically depends on mean and p
        Assert.assertTrue(defaultDistribution.getDomainUpperBound(1.0) > 0);
        Assert.assertTrue(customDistribution.getDomainUpperBound(0.5) > 0);
    }
}