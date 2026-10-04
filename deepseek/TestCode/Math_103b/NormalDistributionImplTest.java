package org.apache.commons.math.distribution;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.Test;

public class NormalDistributionImplTest {

    private static final double DENSITY_TOL = 1e-10;
    private static final double CDF_TOL = 1e-9;
    private static final double INV_CDF_TOL = 1e-5;

    @Test
    public void testDefaultConstructor() {
        NormalDistributionImpl dist = new NormalDistributionImpl();
        assertNotNull(dist);
        assertEquals(0.0, dist.getMean(), 0.0);
        assertEquals(1.0, dist.getStandardDeviation(), 0.0);
    }

    @Test
    public void testConstructorWithParameters() {
        NormalDistributionImpl dist = new NormalDistributionImpl(2.0, 3.0);
        assertEquals(2.0, dist.getMean(), 0.0);
        assertEquals(3.0, dist.getStandardDeviation(), 0.0);
    }

    @Test
    public void testSettersAndGetters() {
        NormalDistributionImpl dist = new NormalDistributionImpl();
        dist.setMean(-1.5);
        dist.setStandardDeviation(2.5);
        assertEquals(-1.5, dist.getMean(), 0.0);
        assertEquals(2.5, dist.getStandardDeviation(), 0.0);
    }

    @Test
    public void testDensityStandardNormal() {
        NormalDistributionImpl dist = new NormalDistributionImpl();
        assertEquals(0.3989422804014327, dist.density(0.0), DENSITY_TOL);
        assertEquals(0.24197072451914337, dist.density(1.0), DENSITY_TOL);
        assertEquals(0.24197072451914337, dist.density(-1.0), DENSITY_TOL);
    }

    @Test
    public void testDensityNonStandard() {
        NormalDistributionImpl dist = new NormalDistributionImpl(2.0, 3.0);
        assertEquals(0.1329807601338109, dist.density(2.0), DENSITY_TOL);
        assertEquals(0.08065690817304779, dist.density(5.0), DENSITY_TOL);
        assertEquals(0.08065690817304779, dist.density(-1.0), DENSITY_TOL);
    }

    @Test
    public void testCumulativeProbabilityStandardNormal() throws Exception {
        NormalDistributionImpl dist = new NormalDistributionImpl();
        assertEquals(0.5, dist.cumulativeProbability(0.0), CDF_TOL);
        assertEquals(0.8413447460685429, dist.cumulativeProbability(1.0), CDF_TOL);
        assertEquals(0.15865525393145707, dist.cumulativeProbability(-1.0), CDF_TOL);
        assertEquals(0.9750021048517795, dist.cumulativeProbability(1.96), CDF_TOL);
        assertEquals(0.024997895148220435, dist.cumulativeProbability(-1.96), CDF_TOL);
    }

    @Test
    public void testCumulativeProbabilityNonStandard() throws Exception {
        NormalDistributionImpl dist = new NormalDistributionImpl(2.0, 3.0);
        assertEquals(0.5, dist.cumulativeProbability(2.0), CDF_TOL);
        assertEquals(0.8413447460685429, dist.cumulativeProbability(5.0), CDF_TOL);
        assertEquals(0.15865525393145707, dist.cumulativeProbability(-1.0), CDF_TOL);
    }

    @Test
    public void testCumulativeProbabilityInfiniteBounds() throws Exception {
        NormalDistributionImpl dist = new NormalDistributionImpl();
        assertEquals(0.0, dist.cumulativeProbability(Double.NEGATIVE_INFINITY), CDF_TOL);
        assertEquals(1.0, dist.cumulativeProbability(Double.POSITIVE_INFINITY), CDF_TOL);
    }

    @Test
    public void testInverseCumulativeProbabilityStandardNormal() throws Exception {
        NormalDistributionImpl dist = new NormalDistributionImpl();
        assertEquals(0.0, dist.inverseCumulativeProbability(0.5), INV_CDF_TOL);
        assertEquals(1.0, dist.inverseCumulativeProbability(0.8413447460685429), INV_CDF_TOL);
        assertEquals(-1.0, dist.inverseCumulativeProbability(0.15865525393145707), INV_CDF_TOL);
        assertEquals(1.959963984540054, dist.inverseCumulativeProbability(0.9750021048517795), INV_CDF_TOL);
        assertEquals(-1.959963984540054, dist.inverseCumulativeProbability(0.024997895148220435), INV_CDF_TOL);
    }

    @Test
    public void testInverseCumulativeProbabilityNonStandard() throws Exception {
        NormalDistributionImpl dist = new NormalDistributionImpl(2.0, 3.0);
        assertEquals(2.0, dist.inverseCumulativeProbability(0.5), INV_CDF_TOL);
        assertEquals(5.0, dist.inverseCumulativeProbability(0.8413447460685429), INV_CDF_TOL);
        assertEquals(-1.0, dist.inverseCumulativeProbability(0.15865525393145707), INV_CDF_TOL);
    }

    @Test
    public void testInverseCumulativeProbabilityBoundaries() throws Exception {
        NormalDistributionImpl standard = new NormalDistributionImpl();
        assertEquals(Double.NEGATIVE_INFINITY, standard.inverseCumulativeProbability(0.0), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, standard.inverseCumulativeProbability(1.0), 0.0);

        NormalDistributionImpl shifted = new NormalDistributionImpl(1.0, 2.0);
        assertEquals(Double.NEGATIVE_INFINITY, shifted.inverseCumulativeProbability(0.0), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, shifted.inverseCumulativeProbability(1.0), 0.0);
    }

    @Test
    public void testInverseCumulativeProbabilityInvalidP() throws Exception {
        NormalDistributionImpl dist = new NormalDistributionImpl();
        try {
            dist.inverseCumulativeProbability(-0.1);
            fail("Expected IllegalArgumentException for p < 0");
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            dist.inverseCumulativeProbability(1.1);
            fail("Expected IllegalArgumentException for p > 1");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testInvalidStandardDeviation() {
        try {
            NormalDistributionImpl dist = new NormalDistributionImpl();
            dist.setStandardDeviation(0.0);
            fail("Expected IllegalArgumentException for zero standard deviation");
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            NormalDistributionImpl dist = new NormalDistributionImpl();
            dist.setStandardDeviation(-1.0);
            fail("Expected IllegalArgumentException for negative standard deviation");
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            new NormalDistributionImpl(0.0, 0.0);
            fail("Expected IllegalArgumentException for constructor with zero standard deviation");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testInverseCumulativeConsistency() throws Exception {
        NormalDistributionImpl dist = new NormalDistributionImpl(1.0, 2.0);
        double[] xs = {-5.0, -1.0, 0.0, 0.5, 1.0, 3.0, 8.0};
        for (double x : xs) {
            double p = dist.cumulativeProbability(x);
            assertEquals("Inverse of CDF at x=" + x, x, dist.inverseCumulativeProbability(p), INV_CDF_TOL);
        }
    }
}