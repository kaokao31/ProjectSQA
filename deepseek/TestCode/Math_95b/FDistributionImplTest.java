package org.apache.commons.math.distribution;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test class for FDistributionImpl (Defects4J Math-95).
 * Covers cumulativeProbability, inverseCumulativeProbability, and edge cases.
 */
public class FDistributionImplTest {

    private FDistributionImpl dist1; // numerator df=1, denominator df=1
    private FDistributionImpl dist2; // numerator df=5, denominator df=10
    private FDistributionImpl dist3; // numerator df=100, denominator df=100
    private FDistributionImpl dist4; // numerator df=1, denominator df=100
    private FDistributionImpl dist5; // numerator df=100, denominator df=1

    @Before
    public void setUp() {
        dist1 = new FDistributionImpl(1.0, 1.0);
        dist2 = new FDistributionImpl(5.0, 10.0);
        dist3 = new FDistributionImpl(100.0, 100.0);
        dist4 = new FDistributionImpl(1.0, 100.0);
        dist5 = new FDistributionImpl(100.0, 1.0);
    }

    // --- cumulativeProbability tests ---

    @Test
    public void testCumulativeProbability_Zero() {
        // F(0) should be 0 for any valid distribution
        assertEquals(0.0, dist1.cumulativeProbability(0.0), 1e-15);
        assertEquals(0.0, dist2.cumulativeProbability(0.0), 1e-15);
        assertEquals(0.0, dist3.cumulativeProbability(0.0), 1e-15);
        assertEquals(0.0, dist4.cumulativeProbability(0.0), 1e-15);
        assertEquals(0.0, dist5.cumulativeProbability(0.0), 1e-15);
    }

    @Test
    public void testCumulativeProbability_Infinity() {
        // F(Infinity) should be 1
        assertEquals(1.0, dist1.cumulativeProbability(Double.POSITIVE_INFINITY), 1e-15);
        assertEquals(1.0, dist2.cumulativeProbability(Double.POSITIVE_INFINITY), 1e-15);
        assertEquals(1.0, dist3.cumulativeProbability(Double.POSITIVE_INFINITY), 1e-15);
        assertEquals(1.0, dist4.cumulativeProbability(Double.POSITIVE_INFINITY), 1e-15);
        assertEquals(1.0, dist5.cumulativeProbability(Double.POSITIVE_INFINITY), 1e-15);
    }

    @Test
    public void testCumulativeProbability_Negative() {
        // Negative x should return 0
        assertEquals(0.0, dist1.cumulativeProbability(-1.0), 1e-15);
        assertEquals(0.0, dist2.cumulativeProbability(-0.5), 1e-15);
    }

    @Test
    public void testCumulativeProbability_NormalValues() {
        // Known values from R or statistical tables
        // For F(1,1) at x=1: p = 0.5 (since F distribution with df1=1,df2=1 median is 1)
        assertEquals(0.5, dist1.cumulativeProbability(1.0), 1e-5);
        // For F(5,10) at x=2: approximate p ~ 0.85
        double p = dist2.cumulativeProbability(2.0);
        assertTrue("p should be between 0 and 1", p > 0 && p < 1);
        assertTrue("p should be > 0.8", p > 0.8);
        // For F(100,100) at x=1: p should be close to 0.5
        assertEquals(0.5, dist3.cumulativeProbability(1.0), 0.1);
    }

    @Test
    public void testCumulativeProbability_Monotonicity() {
        // Cumulative probability should be non-decreasing
        double x1 = 0.5;
        double x2 = 1.5;
        double p1 = dist2.cumulativeProbability(x1);
        double p2 = dist2.cumulativeProbability(x2);
        assertTrue("CDF should be non-decreasing", p2 >= p1);
    }

    @Test
    public void testCumulativeProbability_EdgeCases() {
        // Very large x should approach 1
        double p = dist1.cumulativeProbability(1e10);
        assertTrue("p should be close to 1", p > 0.999);
        // Very small x (but >0) should approach 0
        p = dist1.cumulativeProbability(1e-10);
        assertTrue("p should be close to 0", p < 0.001);
    }

    // --- inverseCumulativeProbability tests ---

    @Test
    public void testInverseCumulativeProbability_Median() {
        // For symmetric F distributions (df1=df2), median is 1
        assertEquals(1.0, dist1.inverseCumulativeProbability(0.5), 1e-5);
        assertEquals(1.0, dist3.inverseCumulativeProbability(0.5), 1e-5);
    }

    @Test
    public void testInverseCumulativeProbability_Boundaries() {
        // p=0 should return 0
        assertEquals(0.0, dist1.inverseCumulativeProbability(0.0), 1e-15);
        // p=1 should return Infinity
        assertEquals(Double.POSITIVE_INFINITY, dist1.inverseCumulativeProbability(1.0), 1e-15);
    }

    @Test
    public void testInverseCumulativeProbability_Consistency() {
        // For a given x, inverseCDF(CDF(x)) should return x (within tolerance)
        double[] testPoints = {0.1, 0.5, 1.0, 2.0, 5.0};
        for (double x : testPoints) {
            double p = dist2.cumulativeProbability(x);
            double xInv = dist2.inverseCumulativeProbability(p);
            assertEquals("Inverse CDF should recover x", x, xInv, 1e-6);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInverseCumulativeProbability_InvalidP_Low() {
        dist1.inverseCumulativeProbability(-0.1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInverseCumulativeProbability_InvalidP_High() {
        dist1.inverseCumulativeProbability(1.1);
    }

    // --- Degrees of freedom setters/getters ---

    @Test
    public void testSetNumeratorDegreesOfFreedom() {
        dist1.setNumeratorDegreesOfFreedom(10.0);
        assertEquals(10.0, dist1.getNumeratorDegreesOfFreedom(), 1e-15);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetNumeratorDegreesOfFreedom_Invalid() {
        dist1.setNumeratorDegreesOfFreedom(0.0);
    }

    @Test
    public void testSetDenominatorDegreesOfFreedom() {
        dist1.setDenominatorDegreesOfFreedom(20.0);
        assertEquals(20.0, dist1.getDenominatorDegreesOfFreedom(), 1e-15);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetDenominatorDegreesOfFreedom_Invalid() {
        dist1.setDenominatorDegreesOfFreedom(-1.0);
    }

    // --- Bug-specific tests (Math-95) ---

    @Test
    public void testCumulativeProbability_Bug95_Regression() {
        // The bug caused incorrect results for certain parameter combinations.
        // Test that cumulative probability returns a valid number (not NaN) and is in [0,1].
        // Known problematic case: numerator df=1, denominator df=1, x=0.5
        double p = dist1.cumulativeProbability(0.5);
        assertFalse("CDF should not be NaN", Double.isNaN(p));
        assertTrue("CDF should be in [0,1]", p >= 0.0 && p <= 1.0);
        // Another case: numerator df=1, denominator df=100, x=0.5
        p = dist4.cumulativeProbability(0.5);
        assertFalse("CDF should not be NaN", Double.isNaN(p));
        assertTrue("CDF should be in [0,1]", p >= 0.0 && p <= 1.0);
        // Case: numerator df=100, denominator df=1, x=0.5
        p = dist5.cumulativeProbability(0.5);
        assertFalse("CDF should not be NaN", Double.isNaN(p));
        assertTrue("CDF should be in [0,1]", p >= 0.0 && p <= 1.0);
    }

    @Test
    public void testCumulativeProbability_Bug95_Accuracy() {
        // The bug caused the CDF to be computed incorrectly.
        // For F(1,1) at x=1, the correct value is 0.5.
        // The buggy implementation returned something else (e.g., using wrong formula).
        assertEquals("CDF for F(1,1) at x=1 should be 0.5", 0.5, dist1.cumulativeProbability(1.0), 1e-6);
        // For F(5,10) at x=2, approximate from R: pf(2,5,10) = 0.849
        assertEquals("CDF for F(5,10) at x=2 should be ~0.849", 0.849, dist2.cumulativeProbability(2.0), 1e-3);
    }

    @Test
    public void testInverseCumulativeProbability_Bug95_Consistency() {
        // The bug also affected inverseCDF due to reliance on CDF.
        // Test that inverseCDF(p) returns a finite value for p in (0,1)
        double p = 0.9;
        double x = dist2.inverseCumulativeProbability(p);
        assertFalse("Inverse CDF should not be NaN", Double.isNaN(x));
        assertTrue("Inverse CDF should be finite", Double.isFinite(x));
        // Check that CDF(x) is close to p
        double pBack = dist2.cumulativeProbability(x);
        assertEquals("CDF(inverseCDF(p)) should equal p", p, pBack, 1e-6);
    }
}