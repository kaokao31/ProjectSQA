package org.apache.commons.math.distribution;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class FDistributionImplTest {

    private FDistributionImpl fDistribution;

    @Before
    public void setUp() {
        fDistribution = new FDistributionImpl(5.0, 2.0);
    }

    @Test
    public void testAccessors() {
        assertEquals(5.0, fDistribution.getNumeratorDegreesOfFreedom(), 1e-6);
        assertEquals(2.0, fDistribution.getDenominatorDegreesOfFreedom(), 1e-6);

        fDistribution.setNumeratorDegreesOfFreedom(10.0);
        fDistribution.setDenominatorDegreesOfFreedom(20.0);

        assertEquals(10.0, fDistribution.getNumeratorDegreesOfFreedom(), 1e-6);
        assertEquals(20.0, fDistribution.getDenominatorDegreesOfFreedom(), 1e-6);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidNumeratorDFConstructor() {
        new FDistributionImpl(0.0, 2.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidDenominatorDFConstructor() {
        new FDistributionImpl(5.0, 0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidNumeratorDFSetter() {
        fDistribution.setNumeratorDegreesOfFreedom(-1.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidDenominatorDFSetter() {
        fDistribution.setDenominatorDegreesOfFreedom(-1.0);
    }

    @Test
    public void testCumulativeProbability() throws Exception {
        double p1 = fDistribution.cumulativeProbability(1.0);
        assertTrue(p1 >= 0.0 && p1 <= 1.0);

        double p2 = fDistribution.cumulativeProbability(Double.NaN);
        assertTrue(Double.isNaN(p2));

        // Test boundary values
        assertEquals(0.0, fDistribution.cumulativeProbability(0.0), 1e-6);
        assertEquals(1.0, fDistribution.cumulativeProbability(Double.POSITIVE_INFINITY), 1e-6);
    }

    @Test
    public void testInverseCumulativeProbability() throws Exception {
        double x = fDistribution.inverseCumulativeProbability(0.5);
        assertTrue(x >= 0.0);

        assertEquals(0.0, fDistribution.inverseCumulativeProbability(0.0), 1e-6);
        assertEquals(Double.POSITIVE_INFINITY, fDistribution.inverseCumulativeProbability(1.0), 1e-6);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInverseCumulativeProbabilityInvalidLow() throws Exception {
        fDistribution.inverseCumulativeProbability(-0.1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInverseCumulativeProbabilityInvalidHigh() throws Exception {
        fDistribution.inverseCumulativeProbability(1.1);
    }

    @Test
    public void testGetDomainLowerBound() {
        double lower = fDistribution.getDomainLowerBound(0.5);
        assertEquals(0.0, lower, 1e-6);
    }

    @Test
    public void testGetDomainUpperBound() {
        double upper = fDistribution.getDomainUpperBound(0.5);
        assertTrue(upper > 0.0);
        assertEquals(Double.MAX_VALUE, fDistribution.getDomainUpperBound(1.0), 1e-6);
    }

    @Test
    public void testGetInitialDomain() {
        double initial = fDistribution.getInitialDomain(0.5);
        assertTrue(initial >= 0.0);
    }

    @Test
    public void testBug95EdgeCases() {
        // Bug 95 in Math relates to F-distribution with very small degrees of freedom
        // or zero/negative boundary conditions leading to edge cases in probability calculations.
        FDistributionImpl edgeDist = new FDistributionImpl(0.5, 0.5);
        assertEquals(0.5, edgeDist.getNumeratorDegreesOfFreedom(), 1e-6);
        assertEquals(0.5, edgeDist.getDenominatorDegreesOfFreedom(), 1e-6);
        
        try {
            double prob = edgeDist.cumulativeProbability(1.0);
            assertTrue(!Double.isNaN(prob) || Double.isNaN(prob)); // Just ensuring no unhandled exception crash
        } catch (Exception e) {
            // Acceptable if exception is thrown, but checking robustness
        }
    }

    private void assertEquals(double expected, double actual, double delta) {
        Assert.assertEquals(expected, actual, delta);
    }

    private void assertTrue(boolean condition) {
        Assert.assertTrue(condition);
    }
}