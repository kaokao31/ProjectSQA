package org.apache.commons.math.distribution;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class NormalDistributionImplTest {

    private NormalDistributionImpl standardNormal;
    private NormalDistributionImpl customNormal;

    @Before
    public void setUp() {
        standardNormal = new NormalDistributionImpl();
        customNormal = new NormalDistributionImpl(2.0, 1.5);
    }

    @Test
    public void testConstructors() {
        Assert.assertEquals(0.0, standardNormal.getMean(), 1e-12);
        Assert.assertEquals(1.0, standardNormal.getStandardDeviation(), 1e-12);

        Assert.assertEquals(2.0, customNormal.getMean(), 1e-12);
        Assert.assertEquals(1.5, customNormal.getStandardDeviation(), 1e-12);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidStandardDeviationZero() {
        new NormalDistributionImpl(0.0, 0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidStandardDeviationNegative() {
        new NormalDistributionImpl(0.0, -1.0);
    }

    @Test
    public void testSetMean() {
        standardNormal.setMean(5.0);
        Assert.assertEquals(5.0, standardNormal.getMean(), 1e-12);
    }

    @Test
    public void testSetStandardDeviation() {
        standardNormal.setStandardDeviation(2.5);
        Assert.assertEquals(2.5, standardNormal.getStandardDeviation(), 1e-12);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetStandardDeviationInvalidZero() {
        standardNormal.setStandardDeviation(0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetStandardDeviationInvalidNegative() {
        standardNormal.setStandardDeviation(-0.5);
    }

    @Test
    public void testCumulativeProbability() throws Exception {
        // Mean 0, SD 1 -> P(X <= 0) should be 0.5
        Assert.assertEquals(0.5, standardNormal.cumulativeProbability(0.0), 1e-6);

        // Extreme values
        Assert.assertEquals(0.0, standardNormal.cumulativeProbability(-100.0), 1e-6);
        Assert.assertEquals(1.0, standardNormal.cumulativeProbability(100.0), 1e-6);

        // Custom normal
        // P(X <= 2.0) for mean 2.0, SD 1.5 should be 0.5
        Assert.assertEquals(0.5, customNormal.cumulativeProbability(2.0), 1e-6);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCumulativeProbabilityNaN() throws Exception {
        standardNormal.cumulativeProbability(Double.NaN);
    }

    @Test
    public void testInverseCumulativeProbability() throws Exception {
        Assert.assertEquals(0.0, standardNormal.inverseCumulativeProbability(0.5), 1e-6);
        Assert.assertEquals(Double.NEGATIVE_INFINITY, standardNormal.inverseCumulativeProbability(0.0), 1e-6);
        Assert.assertEquals(Double.POSITIVE_INFINITY, standardNormal.inverseCumulativeProbability(1.0), 1e-6);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInverseCumulativeProbabilityTooSmall() throws Exception {
        standardNormal.inverseCumulativeProbability(-0.1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInverseCumulativeProbabilityTooLarge() throws Exception {
        standardNormal.inverseCumulativeProbability(1.1);
    }

    @Test
    public void testGetDomainLowerBound() {
        // For mean=0, sd=1, p=0.0 -> lower bound should return something very small (e.g. mean - max(sd * 100, ...))
        double lower = standardNormal.getDomainLowerBound(0.0);
        Assert.assertTrue(Double.isFinite(lower));
    }

    @Test
    public void testGetDomainUpperBound() {
        double upper = standardNormal.getDomainUpperBound(1.0);
        Assert.assertTrue(Double.isFinite(upper));
    }

    @Test
    public void testGetInitialDomain() {
        double initial = standardNormal.getInitialDomain(0.5);
        Assert.assertEquals(0.0, initial, 1e-12);

        double initialCustom = customNormal.getInitialDomain(0.5);
        Assert.assertEquals(2.0, initialCustom, 1e-12);
    }
}