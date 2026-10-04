package org.apache.commons.math.distribution;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class NormalDistributionImplTest {

    private NormalDistributionImpl distribution;
    private NormalDistributionImpl customDistribution;

    @Before
    public void setUp() {
        distribution = new NormalDistributionImpl();
        customDistribution = new NormalDistributionImpl(2.0, 3.0);
    }

    @Test
    public void testDefaultConstructor() {
        Assert.assertEquals(0.0, distribution.getMean(), 1e-9);
        Assert.assertEquals(1.0, distribution.getStandardDeviation(), 1e-9);
    }

    @Test
    public void testParameterizedConstructor() {
        Assert.assertEquals(2.0, customDistribution.getMean(), 1e-9);
        Assert.assertEquals(3.0, customDistribution.getStandardDeviation(), 1e-9);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidStandardDeviationConstructor() {
        new NormalDistributionImpl(0.0, 0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeStandardDeviationConstructor() {
        new NormalDistributionImpl(0.0, -1.0);
    }

    @Test
    public void testSetMean() {
        distribution.setMean(5.0);
        Assert.assertEquals(5.0, distribution.getMean(), 1e-9);
    }

    @Test
    public void testSetStandardDeviation() {
        distribution.setStandardDeviation(4.0);
        Assert.assertEquals(4.0, distribution.getStandardDeviation(), 1e-9);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetInvalidStandardDeviationZero() {
        distribution.setStandardDeviation(0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetInvalidStandardDeviationNegative() {
        distribution.setStandardDeviation(-2.5);
    }

    @Test
    public void testCumulativeProbability() throws Exception {
        // Standard normal distribution: mean = 0, sd = 1
        // P(X <= 0) should be 0.5
        Assert.assertEquals(0.5, distribution.cumulativeProbability(0.0), 1e-4);

        // P(X <= infinity) -> 1.0
        Assert.assertEquals(1.0, distribution.cumulativeProbability(Double.MAX_VALUE), 1e-4);

        // P(X <= -infinity) -> 0.0
        Assert.assertEquals(0.0, distribution.cumulativeProbability(-Double.MAX_VALUE), 1e-4);
    }

    @Test
    public void testCumulativeProbabilityCustom() throws Exception {
        // Custom normal distribution: mean = 2, sd = 3
        // At mean, cumulative probability should be 0.5
        Assert.assertEquals(0.5, customDistribution.cumulativeProbability(2.0), 1e-4);
    }

    @Test(expected = MathException.class)
    public void testCumulativeProbabilityExtremeValues() throws Exception {
        // Depending on implementation, extremely large or NaN values might throw MathException or return bounds
        distribution.cumulativeProbability(Double.NaN);
    }

    @Test
    public void testInverseCumulativeProbability() throws Exception {
        // Inverse cumulative probability at 0.5 for standard normal should be 0.0
        Assert.assertEquals(0.0, distribution.inverseCumulativeProbability(0.5), 1e-4);

        // Inverse cumulative probability at 0.0 should be -Infinity
        Assert.assertEquals(Double.NEGATIVE_INFINITY, distribution.inverseCumulativeProbability(0.0), 1e-4);

        // Inverse cumulative probability at 1.0 should be +Infinity
        Assert.assertEquals(Double.POSITIVE_INFINITY, distribution.inverseCumulativeProbability(1.0), 1e-4);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInverseCumulativeProbabilityOutOfBoundsLow() throws Exception {
        distribution.inverseCumulativeProbability(-0.1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInverseCumulativeProbabilityOutOfBoundsHigh() throws Exception {
        distribution.inverseCumulativeProbability(1.1);
    }

    @Test
    public void testGetDomainLowerBound() {
        // For standard normal, lower bound for p=0.5 could be tested via domain bounds
        // mean = 0, sd = 1 -> lower bound for p=0.5 (which is mean - setbacks)
        double lowerBound = distribution.getDomainLowerBound(0.5);
        Assert.assertTrue(lowerBound < 0.0);
    }

    @Test
    public void testGetDomainUpperBound() {
        double upperBound = distribution.getDomainUpperBound(0.5);
        Assert.assertTrue(upperBound > 0.0);
    }

    @Test
    public void testGetInitialDomain() {
        double initial = distribution.getInitialDomain(0.5);
        Assert.assertEquals(0.0, initial, 1e-9);

        double initialCustom = customDistribution.getInitialDomain(0.5);
        Assert.assertEquals(2.0, initialCustom, 1e-9);
    }
}