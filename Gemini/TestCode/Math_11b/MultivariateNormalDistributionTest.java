package org.apache.commons.math3.distribution;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.linear.NotPositiveDefiniteMatrixException;
import org.junit.Assert;
import org.junit.Test;

public class MultivariateNormalDistributionTest {

    @Test
    public void testValidInitializationAndAccessors() {
        double[] means = {1.0, 2.0};
        double[][] covariances = {
            {1.0, 0.3},
            {0.3, 1.0}
        };

        MultivariateNormalDistribution dist = new MultivariateNormalDistribution(means, covariances);

        Assert.assertArrayEquals(means, dist.getMeans(), 1e-12);
        Assert.assertEquals(covariances.length, dist.getCovariances().getRowDimension());
        Assert.assertEquals(covariances[0].length, dist.getCovariances().getColumnDimension());
        Assert.assertEquals(2, dist.getDimension());
    }

    @Test(expected = DimensionMismatchException.class)
    public void testDimensionMismatchMeansAndCovariance() {
        double[] means = {1.0, 2.0, 3.0}; // 3 elements
        double[][] covariances = {
            {1.0, 0.3},
            {0.3, 1.0}
        }; // 2x2 matrix

        new MultivariateNormalDistribution(means, covariances);
    }

    @Test(expected = DimensionMismatchException.class)
    public void testNonSquareCovarianceMatrix() {
        double[] means = {1.0, 2.0};
        double[][] covariances = {
            {1.0, 0.3, 0.0},
            {0.3, 1.0, 0.0}
        }; // 2x3 matrix

        new MultivariateNormalDistribution(means, covariances);
    }

    @Test(expected = NotPositiveDefiniteMatrixException.class)
    public void testNotPositiveDefiniteCovarianceMatrix() {
        double[] means = {1.0, 2.0};
        // Singular or non-positive definite matrix
        double[][] covariances = {
            {1.0, 2.0},
            {2.0, 1.0}
        };

        new MultivariateNormalDistribution(means, covariances);
    }

    @Test
    public void testDensityEvaluation() {
        double[] means = {0.0, 0.0};
        double[][] covariances = {
            {1.0, 0.0},
            {0.0, 1.0}
        };

        MultivariateNormalDistribution dist = new MultivariateNormalDistribution(means, covariances);

        double densityAtMean = dist.density(new double[] {0.0, 0.0});
        Assert.assertTrue(densityAtMean > 0.0);

        // Density at a point away from mean should be smaller than at mean
        double densityAway = dist.density(new double[] {1.0, 1.0});
        Assert.assertTrue(densityAway > 0.0);
        Assert.assertTrue(densityAway < densityAtMean);
    }

    @Test(expected = DimensionMismatchException.class)
    public void testDensityWrongDimension() {
        double[] means = {0.0, 0.0};
        double[][] covariances = {
            {1.0, 0.0},
            {0.0, 1.0}
        };

        MultivariateNormalDistribution dist = new MultivariateNormalDistribution(means, covariances);
        dist.density(new double[] {0.0}); // Expecting 2 dimensions, providing 1
    }

    @Test
    public void testSampling() {
        double[] means = {10.0, -10.0};
        double[][] covariances = {
            {2.0, 0.5},
            {0.5, 2.0}
        };

        MultivariateNormalDistribution dist = new MultivariateNormalDistribution(means, covariances);
        
        double[][] samples = dist.sample(5);
        Assert.assertNotNull(samples);
        Assert.assertEquals(5, samples.length);
        Assert.assertEquals(2, samples[0].length);
    }
}