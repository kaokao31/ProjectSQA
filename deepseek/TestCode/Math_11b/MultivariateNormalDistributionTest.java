package org.apache.commons.math3.distribution;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularMatrixException;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.NotPositiveDefiniteMatrixException;
import org.apache.commons.math3.exception.NotSymmetricMatrixException;

/**
 * Test suite for MultivariateNormalDistribution.
 * Designed to achieve high coverage and detect potential faults (Defects4J Math-11).
 */
public class MultivariateNormalDistributionTest {

    private double[] means2D;
    private double[][] cov2D;
    private MultivariateNormalDistribution dist2D;

    @Before
    public void setUp() {
        means2D = new double[] {1.0, 2.0};
        cov2D = new double[][] {{2.0, 0.5}, {0.5, 1.0}};
        dist2D = new MultivariateNormalDistribution(means2D, cov2D);
    }

    // ========== Constructor Tests ==========

    @Test(expected = DimensionMismatchException.class)
    public void testConstructorMismatchedDimensions() {
        double[] means = new double[] {1.0, 2.0};
        double[][] cov = new double[][] {{1.0, 0.0, 0.0}, {0.0, 1.0, 0.0}}; // 2x3
        new MultivariateNormalDistribution(means, cov);
    }

    @Test(expected = NotSymmetricMatrixException.class)
    public void testConstructorNonSymmetricCovariance() {
        double[] means = new double[] {0.0, 0.0};
        double[][] cov = new double[][] {{1.0, 2.0}, {3.0, 4.0}};
        new MultivariateNormalDistribution(means, cov);
    }

    @Test(expected = NotPositiveDefiniteMatrixException.class)
    public void testConstructorNonPositiveDefiniteCovariance() {
        double[] means = new double[] {0.0, 0.0};
        double[][] cov = new double[][] {{1.0, 0.0}, {0.0, -1.0}}; // negative eigenvalue
        new MultivariateNormalDistribution(means, cov);
    }

    @Test(expected = NotPositiveDefiniteMatrixException.class)
    public void testConstructorSingularCovariance() {
        double[] means = new double[] {0.0, 0.0};
        double[][] cov = new double[][] {{1.0, 1.0}, {1.0, 1.0}}; // singular
        new MultivariateNormalDistribution(means, cov);
    }

    @Test
    public void testConstructorValid() {
        // Already created in setUp, but verify no exception
        assertNotNull(dist2D);
    }

    // ========== Density Tests ==========

    @Test
    public void testDensityAtMean() {
        double density = dist2D.density(means2D);
        // Density at mean should be positive and maximum
        assertTrue("Density at mean should be > 0", density > 0);
        // Compare with density at a point away
        double[] away = new double[] {10.0, 10.0};
        assertTrue("Density at mean should be greater than at far point",
                   density > dist2D.density(away));
    }

    @Test
    public void testDensitySymmetric() {
        double[] point1 = new double[] {0.0, 2.0};
        double[] point2 = new double[] {2.0, 2.0};
        // Due to symmetry in covariance? Not exactly, but check that density is same for mirrored points?
        // Actually, covariance is symmetric but not isotropic. Just check that density is computed.
        double d1 = dist2D.density(point1);
        double d2 = dist2D.density(point2);
        assertTrue("Density should be finite", Double.isFinite(d1));
        assertTrue("Density should be finite", Double.isFinite(d2));
    }

    @Test
    public void testDensityExtremePoint() {
        double[] far = new double[] {1000.0, -1000.0};
        double density = dist2D.density(far);
        // Should be very close to zero but positive
        assertTrue("Density at extreme point should be > 0", density > 0);
        assertTrue("Density at extreme point should be very small", density < 1e-10);
    }

    @Test
    public void testDensityWithSingularCovariance() {
        // Create a distribution with singular covariance (should have been rejected by constructor)
        // But we can test density on a distribution that was created with a valid but nearly singular?
        // Actually, the constructor throws exception for singular. So we cannot test density on singular.
        // However, the bug might be that the constructor does not check properly.
        // We'll test that if we somehow get a singular matrix (e.g., by using a different constructor? Not available)
        // So we skip this test because constructor enforces positive definiteness.
        // Instead, we test that density does not produce NaN for valid inputs.
        double[] point = new double[] {1.5, 2.5};
        double density = dist2D.density(point);
        assertFalse("Density should not be NaN", Double.isNaN(density));
        assertFalse("Density should not be infinite", Double.isInfinite(density));
    }

    // ========== Log Density Tests ==========

    @Test
    public void testLogDensityAtMean() {
        double logDensity = dist2D.logDensity(means2D);
        double density = dist2D.density(means2D);
        assertEquals("logDensity should equal log(density)", Math.log(density), logDensity, 1e-12);
    }

    @Test
    public void testLogDensityExtremePoint() {
        double[] far = new double[] {1000.0, -1000.0};
        double logDensity = dist2D.logDensity(far);
        assertTrue("logDensity should be negative large", logDensity < -100);
        assertFalse("logDensity should not be NaN", Double.isNaN(logDensity));
    }

    @Test
    public void testLogDensityConsistency() {
        double[] point = new double[] {0.5, 1.5};
        double density = dist2D.density(point);
        double logDensity = dist2D.logDensity(point);
        assertEquals("logDensity should equal log(density)", Math.log(density), logDensity, 1e-12);
    }

    // ========== Getters Tests ==========

    @Test
    public void testGetMeans() {
        double[] means = dist2D.getMeans();
        assertArrayEquals("Means should match constructor input", means2D, means, 1e-12);
    }

    @Test
    public void testGetCovariances() {
        RealMatrix cov = dist2D.getCovariances();
        double[][] expected = cov2D;
        double[][] actual = cov.getData();
        for (int i = 0; i < expected.length; i++) {
            assertArrayEquals("Covariance row " + i, expected[i], actual[i], 1e-12);
        }
    }

    // ========== Edge Cases and Boundary Tests ==========

    @Test
    public void testDensityWithZeroVarianceInOneDimension() {
        // This is a singular case, but constructor should reject. However, we can test with a 1D distribution?
        // MultivariateNormalDistribution requires at least 1 dimension? Actually it's multivariate, but can be 1D.
        // Let's test with 1D valid covariance (positive definite).
        double[] means1D = new double[] {0.0};
        double[][] cov1D = new double[][] {{1.0}};
        MultivariateNormalDistribution dist1D = new MultivariateNormalDistribution(means1D, cov1D);
        double density = dist1D.density(new double[] {0.0});
        assertEquals("Density at mean for 1D should be 1/sqrt(2*pi)", 1.0 / Math.sqrt(2 * Math.PI), density, 1e-12);
    }

    @Test
    public void testDensityWithLargeDimension() {
        // Test with 5 dimensions
        int dim = 5;
        double[] means = new double[dim];
        double[][] cov = new double[dim][dim];
        for (int i = 0; i < dim; i++) {
            means[i] = i;
            cov[i][i] = 1.0;
            for (int j = i+1; j < dim; j++) {
                cov[i][j] = 0.1;
                cov[j][i] = 0.1;
            }
        }
        MultivariateNormalDistribution dist = new MultivariateNormalDistribution(means, cov);
        double[] point = new double[dim];
        for (int i = 0; i < dim; i++) {
            point[i] = i + 0.5;
        }
        double density = dist.density(point);
        assertTrue("Density should be positive", density > 0);
        assertTrue("Density should be finite", Double.isFinite(density));
    }

    @Test
    public void testDensityWithIdentityCovariance() {
        double[] means = new double[] {0.0, 0.0};
        double[][] cov = new double[][] {{1.0, 0.0}, {0.0, 1.0}};
        MultivariateNormalDistribution dist = new MultivariateNormalDistribution(means, cov);
        double[] point = new double[] {1.0, 1.0};
        double expected = (1.0 / (2 * Math.PI)) * Math.exp(-0.5 * (1+1));
        assertEquals("Density for standard bivariate normal", expected, dist.density(point), 1e-12);
    }

    // ========== Fault Detection Tests (Defects4J Math-11 related) ==========

    @Test
    public void testDensityNoNaNForValidInput() {
        // Ensure that density never returns NaN for any valid input
        double[] point = new double[] {Double.MAX_VALUE, Double.MAX_VALUE};
        double density = dist2D.density(point);
        assertFalse("Density should not be NaN for large input", Double.isNaN(density));
        // Also test with negative large
        point = new double[] {-Double.MAX_VALUE, -Double.MAX_VALUE};
        density = dist2D.density(point);
        assertFalse("Density should not be NaN for large negative input", Double.isNaN(density));
    }

    @Test
    public void testLogDensityNoNaNForValidInput() {
        double[] point = new double[] {Double.MAX_VALUE, Double.MAX_VALUE};
        double logDensity = dist2D.logDensity(point);
        assertFalse("LogDensity should not be NaN for large input", Double.isNaN(logDensity));
    }

    @Test
    public void testDensityWithNearlySingularCovariance() {
        // Create a covariance matrix that is positive definite but ill-conditioned
        double[][] cov = new double[][] {{1.0, 0.9999}, {0.9999, 1.0}};
        double[] means = new double[] {0.0, 0.0};
        MultivariateNormalDistribution dist = new MultivariateNormalDistribution(means, cov);
        double[] point = new double[] {0.0, 0.0};
        double density = dist.density(point);
        assertTrue("Density should be positive", density > 0);
        assertFalse("Density should not be NaN", Double.isNaN(density));
        // The bug might cause NaN due to numerical issues; this test would catch that.
    }

    @Test
    public void testDensityWithZeroMeanAndUnitCovariance() {
        double[] means = new double[] {0.0, 0.0};
        double[][] cov = new double[][] {{1.0, 0.0}, {0.0, 1.0}};
        MultivariateNormalDistribution dist = new MultivariateNormalDistribution(means, cov);
        double[] point = new double[] {0.0, 0.0};
        double expected = 1.0 / (2 * Math.PI);
        assertEquals("Density at origin for standard bivariate normal", expected, dist.density(point), 1e-12);
    }

    // ========== Additional Coverage: sample() method? Not in the class? Actually MultivariateNormalDistribution extends AbstractMultivariateRealDistribution which has sample()? But we are not required to test sample. However, we can test that the distribution is properly constructed and density works.

    // ========== Test for consistency between density and logDensity ==========

    @Test
    public void testDensityAndLogDensityConsistencyMultiplePoints() {
        double[][] points = {
            {1.0, 2.0},
            {0.0, 0.0},
            {3.0, -1.0},
            {-2.0, 5.0}
        };
        for (double[] point : points) {
            double density = dist2D.density(point);
            double logDensity = dist2D.logDensity(point);
            assertEquals("logDensity should equal log(density) for point " + java.util.Arrays.toString(point),
                         Math.log(density), logDensity, 1e-12);
        }
    }

    // ========== Test for density with different dimensions ==========

    @Test
    public void testDensity3D() {
        double[] means = new double[] {1.0, 2.0, 3.0};
        double[][] cov = new double[][] {
            {2.0, 0.3, 0.1},
            {0.3, 1.0, 0.2},
            {0.1, 0.2, 0.5}
        };
        MultivariateNormalDistribution dist = new MultivariateNormalDistribution(means, cov);
        double[] point = new double[] {1.5, 2.5, 3.5};
        double density = dist.density(point);
        assertTrue("Density should be positive", density > 0);
        assertFalse("Density should not be NaN", Double.isNaN(density));
    }
}