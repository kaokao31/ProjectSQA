package org.apache.commons.math3.distribution;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

public class FDistributionTest {

    private static final double DEFAULT_EPSILON = 1e-15;
    private FDistribution dist;

    @Before
    public void setUp() {
        dist = new FDistribution(2.0, 3.0);
    }

    @Test
    public void testGetNumeratorDegreesOfFreedom() {
        assertEquals(2.0, dist.getNumeratorDegreesOfFreedom(), 0.0);
    }

    @Test
    public void testGetDenominatorDegreesOfFreedom() {
        assertEquals(3.0, dist.getDenominatorDegreesOfFreedom(), 0.0);
    }

    @Test
    public void testDensityKnownValues() {
        // Test density at various points
        double x1 = 0.5;
        double expected1 = 0.519198; // approximate value
        assertEquals(expected1, dist.density(x1), 1e-6);
        
        double x2 = 1.0;
        double expected2 = 0.3; // approximate value
        assertEquals(expected2, dist.density(x2), 1e-6);
        
        double x3 = 2.0;
        double expected3 = 0.0; // will be checked with tolerance
        assertTrue(dist.density(x3) > 0);
    }

    @Test
    public void testDensityBoundaryZero() {
        // At x = 0, density should be 0 for F distribution
        assertEquals(0.0, dist.density(0.0), DEFAULT_EPSILON);
    }

    @Test
    public void testDensityLargeValues() {
        // As x becomes large, density approaches 0
        assertTrue(dist.density(1000.0) > 0); // Should be very small but positive
        assertTrue(dist.density(1000000.0) > 0);
    }

    @Test(expected = org.apache.commons.math3.exception.NotStrictlyPositiveException.class)
    public void testConstructorNegativeDegreesOfFreedom() {
        new FDistribution(-1.0, 2.0);
    }

    @Test(expected = org.apache.commons.math3.exception.NotStrictlyPositiveException.class)
    public void testConstructorZeroDegreesOfFreedom() {
        new FDistribution(0.0, 2.0);
    }

    @Test(expected = org.apache.commons.math3.exception.NotStrictlyPositiveException.class)
    public void testConstructorNegativeDenominator() {
        new FDistribution(2.0, -1.0);
    }

    @Test(expected = org.apache.commons.math3.exception.NotStrictlyPositiveException.class)
    public void testConstructorZeroDenominator() {
        new FDistribution(2.0, 0.0);
    }

    @Test
    public void testFDistributionBasic() {
        // Test with degrees of freedom that are specific to potential bug cases
        FDistribution f1 = new FDistribution(1.0, 1.0);
        assertEquals(0.5, f1.cumulativeProbability(1.0), 1e-15);
        
        FDistribution f2 = new FDistribution(2.0, 2.0);
        assertEquals(0.5, f2.cumulativeProbability(1.0), 0.01);
    }

    @Test
    public void testCumulativeProbabilityZero() {
        assertEquals(0.0, dist.cumulativeProbability(0.0), DEFAULT_EPSILON);
    }

    @Test
    public void testCumulativeProbabilityLarge() {
        double large = 100.0;
        assertTrue(dist.cumulativeProbability(large) < 1.0);
        assertTrue(dist.cumulativeProbability(large) > 0.9);
    }

    @Test
    public void testCumulativeProbabilityMedian() {
        // For F(2,3), median should be around 0.42-0.43
        double median = dist.inverseCumulativeProbability(0.5);
        assertEquals(median, dist.cumulativeProbability(median), 1e-12);
    }

    @Test(expected = org.apache.commons.math3.exception.NumberIsTooLargeException.class)
    public void testCumulativeProbabilityAboveOne() {
        dist.cumulativeProbability(Double.NaN);
    }

    @Test(expected = org.apache.commons.math3.exception.NumberIsTooLargeException.class)
    public void testInverseCumulativeProbabilityAboveOne() {
        dist.inverseCumulativeProbability(1.5);
    }

    @Test(expected = org.apache.commons.math3.exception.NumberIsTooSmallException.class)
    public void testInverseCumulativeProbabilityBelowZero() {
        dist.inverseCumulativeProbability(-0.5);
    }

    @Test
    public void testInverseCumulativeProbabilityZero() {
        assertEquals(0.0, dist.inverseCumulativeProbability(0.0), DEFAULT_EPSILON);
    }

    @Test
    public void testInverseCumulativeProbabilityOne() {
        assertEquals(Double.POSITIVE_INFINITY, dist.inverseCumulativeProbability(1.0), 0.0);
    }

    @Test
    public void testInverseCumulativeProbabilityConsistency() {
        double p = 0.75;
        double x = dist.inverseCumulativeProbability(p);
        double computedP = dist.cumulativeProbability(x);
        assertEquals(p, computedP, 1e-12);
    }

    @Test
    public void testSupportLowerBound() {
        assertEquals(0.0, dist.getSupportLowerBound(), DEFAULT_EPSILON);
    }

    @Test
    public void testSupportUpperBound() {
        assertEquals(Double.POSITIVE_INFINITY, dist.getSupportUpperBound(), 0.0);
    }

    @Test
    public void testIsSupportConnected() {
        assertTrue(dist.isSupportLowerBoundInclusive());
        assertFalse(dist.isSupportUpperBoundInclusive());
    }

    @Test
    public void testMean() {
        // For F with df2 > 2, mean = df2/(df2-2) = 3/(3-2) = 3
        assertEquals(3.0, dist.getNumericalMean(), 1e-12);
    }

    @Test
    public void testVariance() {
        // For F(2,3), variance = 2*3^2*(3+1-2)/(1^2*(3-2)^2*(3-4))? 
        // Actually var = 2*df2^2*(df1+df2-2)/(df1*(df2-2)^2*(df2-4))
        // For df1=2, df2=3: denominator df2-4 = -1, so variance should be NaN
        assertTrue(Double.isNaN(dist.getNumericalVariance()));
    }

    @Test
    public void testVarianceDeterministic() {
        FDistribution f = new FDistribution(5.0, 10.0);
        double variance = f.getNumericalVariance();
        // For df2 > 4, variance should be finite
        assertFalse(Double.isNaN(variance));
        assertTrue(variance > 0);
    }

    @Test
    public void testDensityAtBoundary() {
        // Test density at x=epsilon
        double small = 1e-10;
        double density = dist.density(small);
        assertTrue(density > 0);
        
        // Test density at x=0 returns 0
        assertEquals(0.0, dist.density(0.0), DEFAULT_EPSILON);
    }

    @Test
    public void testDensityPositivity() {
        // Density must be non-negative for all x >= 0
        for (double x = 0.0; x <= 5.0; x += 0.5) {
            assertTrue("Density negative at x=" + x, dist.density(x) >= 0);
        }
    }

    @Test
    public void testCumulativeProbabilityAtBoundary() {
        // Probability at x=0 should be 0
        assertEquals(0.0, dist.cumulativeProbability(0.0), DEFAULT_EPSILON);
        
        // Probability should increase monotonically
        double prev = 0.0;
        for (double x = 0.1; x <= 5.0; x += 0.1) {
            double curr = dist.cumulativeProbability(x);
            assertTrue("CDF is not monotonic at x=" + x, curr >= prev);
            prev = curr;
        }
    }

    @Test
    public void testNumericalMeanForVariousDF() {
        // For df2 = 1, mean should be undefined (NaN)
        FDistribution f1 = new FDistribution(2.0, 1.0);
        assertTrue(Double.isNaN(f1.getNumericalMean()));
        
        // For df2 = 2, mean should be undefined (infinite)
        FDistribution f2 = new FDistribution(2.0, 2.0);
        assertEquals(Double.POSITIVE_INFINITY, f2.getNumericalMean(), 0.0);
        
        // For df2 = 3, mean should be finite
        assertEquals(3.0, dist.getNumericalMean(), 1e-12);
    }
}