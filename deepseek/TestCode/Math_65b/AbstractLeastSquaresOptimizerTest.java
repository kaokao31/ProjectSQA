package org.apache.commons.math.optimization.general;

import org.apache.commons.math.analysis.DifferentiableMultivariateVectorialFunction;
import org.apache.commons.math.analysis.MultivariateMatrixFunction;
import org.apache.commons.math.optimization.OptimizationException;
import org.apache.commons.math.optimization.VectorialPointValuePair;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test suite for AbstractLeastSquaresOptimizer, targeting high coverage and fault detection.
 * Uses LevenbergMarquardtOptimizer as the concrete implementation.
 */
public class AbstractLeastSquaresOptimizerTest {

    private LevenbergMarquardtOptimizer optimizer;
    private double[] target;
    private double[] weights;
    private DifferentiableMultivariateVectorialFunction function;
    private MultivariateMatrixFunction jacobian;

    @Before
    public void setUp() {
        optimizer = new LevenbergMarquardtOptimizer();
        // Simple linear model: y = a*x + b
        // Data points: (1,2), (2,3), (3,4), (4,5) -> perfect line y = x + 1
        target = new double[]{2.0, 3.0, 4.0, 5.0};
        weights = new double[]{1.0, 1.0, 1.0, 1.0};
        function = new DifferentiableMultivariateVectorialFunction() {
            public double[] value(double[] point) {
                double a = point[0];
                double b = point[1];
                return new double[]{
                    a * 1.0 + b,
                    a * 2.0 + b,
                    a * 3.0 + b,
                    a * 4.0 + b
                };
            }
            public MultivariateMatrixFunction jacobian() {
                return new MultivariateMatrixFunction() {
                    public double[][] value(double[] point) {
                        // Jacobian: d(y_i)/da = x_i, d(y_i)/db = 1
                        return new double[][]{
                            {1.0, 1.0},
                            {2.0, 1.0},
                            {3.0, 1.0},
                            {4.0, 1.0}
                        };
                    }
                };
            }
        };
    }

    @Test
    public void testSimpleLinearFitting() throws OptimizationException {
        double[] startPoint = {0.0, 0.0};
        VectorialPointValuePair optimum = optimizer.optimize(function, target, weights, startPoint);
        double[] params = optimum.getPoint();
        // Expected: a=1, b=1
        assertEquals("Parameter a", 1.0, params[0], 1e-10);
        assertEquals("Parameter b", 1.0, params[1], 1e-10);
        // Check cost (should be near zero)
        double cost = optimizer.getChiSquare();
        assertTrue("Cost should be near zero", cost < 1e-10);
        // Check RMS
        double rms = optimizer.getRMS();
        assertTrue("RMS should be near zero", rms < 1e-10);
        // Check covariance matrix
        double[][] cov = optimizer.getCovariances();
        assertNotNull("Covariance matrix should not be null", cov);
        assertEquals("Covariance matrix rows", 2, cov.length);
        assertEquals("Covariance matrix columns", 2, cov[0].length);
        // Check sigma
        double[] sigma = optimizer.getSigma();
        assertNotNull("Sigma should not be null", sigma);
        assertEquals("Sigma length", 2, sigma.length);
        // For perfect fit, sigma should be zero (or very small)
        assertTrue("Sigma[0] should be near zero", sigma[0] < 1e-10);
        assertTrue("Sigma[1] should be near zero", sigma[1] < 1e-10);
    }

    @Test(expected = OptimizationException.class)
    public void testSingularJacobian() throws OptimizationException {
        // Model: y = b (constant), but we try to fit two parameters a and b where a is not identifiable
        // Jacobian columns are linearly dependent (all x_i = 0? Actually we set x_i = 0 for a)
        // Use a different function: y = a*0 + b = b, so Jacobian: d(y)/da = 0, d(y)/db = 1
        DifferentiableMultivariateVectorialFunction singularFunc = new DifferentiableMultivariateVectorialFunction() {
            public double[] value(double[] point) {
                double b = point[1];
                return new double[]{b, b, b, b};
            }
            public MultivariateMatrixFunction jacobian() {
                return new MultivariateMatrixFunction() {
                    public double[][] value(double[] point) {
                        return new double[][]{
                            {0.0, 1.0},
                            {0.0, 1.0},
                            {0.0, 1.0},
                            {0.0, 1.0}
                        };
                    }
                };
            }
        };
        double[] start = {1.0, 0.0};
        optimizer.optimize(singularFunc, target, weights, start);
        // Should throw OptimizationException due to singular convergence
    }

    @Test
    public void testZeroResiduals() throws OptimizationException {
        // Already tested in simpleLinearFitting, but explicitly check sigma and covariance
        double[] startPoint = {0.5, 0.5};
        VectorialPointValuePair optimum = optimizer.optimize(function, target, weights, startPoint);
        double[] sigma = optimizer.getSigma();
        // With perfect fit, sigma should be zero (or extremely small)
        assertTrue("Sigma[0] should be zero for perfect fit", sigma[0] < 1e-15);
        assertTrue("Sigma[1] should be zero for perfect fit", sigma[1] < 1e-15);
        // Covariance matrix should be zero (since residuals are zero)
        double[][] cov = optimizer.getCovariances();
        for (int i = 0; i < cov.length; i++) {
            for (int j = 0; j < cov[i].length; j++) {
                assertEquals("Covariance element should be zero", 0.0, cov[i][j], 1e-15);
            }
        }
    }

    @Test
    public void testLargeResiduals() throws OptimizationException {
        // Use a model with large residuals to test numerical stability
        // y = a*x + b, but target values are far from initial guess
        double[] largeTarget = {1000.0, 2000.0, 3000.0, 4000.0};
        double[] startPoint = {0.0, 0.0};
        VectorialPointValuePair optimum = optimizer.optimize(function, largeTarget, weights, startPoint);
        double[] params = optimum.getPoint();
        // Expected: a=1000, b=0? Actually y = a*x + b, with targets 1000,2000,3000,4000 => a=1000, b=0
        assertEquals("Parameter a", 1000.0, params[0], 1e-10);
        assertEquals("Parameter b", 0.0, params[1], 1e-10);
        // Check covariance and sigma are computed (not NaN)
        double[][] cov = optimizer.getCovariances();
        assertNotNull(cov);
        double[] sigma = optimizer.getSigma();
        assertNotNull(sigma);
        for (double s : sigma) {
            assertFalse("Sigma should not be NaN", Double.isNaN(s));
            assertFalse("Sigma should not be infinite", Double.isInfinite(s));
        }
    }

    @Test
    public void testGetCovariancesAndSigmaAfterOptimization() throws OptimizationException {
        // Ensure that getCovariances and getSigma can be called multiple times
        double[] startPoint = {0.0, 0.0};
        optimizer.optimize(function, target, weights, startPoint);
        double[][] cov1 = optimizer.getCovariances();
        double[] sigma1 = optimizer.getSigma();
        // Call again
        double[][] cov2 = optimizer.getCovariances();
        double[] sigma2 = optimizer.getSigma();
        // Should be identical
        assertArrayEquals(cov1[0], cov2[0], 1e-15);
        assertArrayEquals(cov1[1], cov2[1], 1e-15);
        assertArrayEquals(sigma1, sigma2, 1e-15);
    }

    @Test(expected = OptimizationException.class)
    public void testNonConvergent() throws OptimizationException {
        // Use a function that does not converge (e.g., flat region)
        DifferentiableMultivariateVectorialFunction flatFunc = new DifferentiableMultivariateVectorialFunction() {
            public double[] value(double[] point) {
                return new double[]{1.0, 1.0, 1.0, 1.0};
            }
            public MultivariateMatrixFunction jacobian() {
                return new MultivariateMatrixFunction() {
                    public double[][] value(double[] point) {
                        return new double[][]{
                            {0.0, 0.0},
                            {0.0, 0.0},
                            {0.0, 0.0},
                            {0.0, 0.0}
                        };
                    }
                };
            }
        };
        double[] start = {0.0, 0.0};
        optimizer.optimize(flatFunc, target, weights, start);
    }

    @Test
    public void testWeightedLeastSquares() throws OptimizationException {
        // Test with non-uniform weights
        double[] w = {1.0, 2.0, 3.0, 4.0};
        optimizer = new LevenbergMarquardtOptimizer();
        double[] start = {0.0, 0.0};
        VectorialPointValuePair optimum = optimizer.optimize(function, target, w, start);
        double[] params = optimum.getPoint();
        // With weights, the solution should still be a=1, b=1 (since model is exact)
        assertEquals("Weighted a", 1.0, params[0], 1e-10);
        assertEquals("Weighted b", 1.0, params[1], 1e-10);
        // Check that covariance and sigma are computed
        double[][] cov = optimizer.getCovariances();
        assertNotNull(cov);
        double[] sigma = optimizer.getSigma();
        assertNotNull(sigma);
    }

    @Test
    public void testSingleParameter() throws OptimizationException {
        // Model: y = a*x (no intercept)
        DifferentiableMultivariateVectorialFunction singleFunc = new DifferentiableMultivariateVectorialFunction() {
            public double[] value(double[] point) {
                double a = point[0];
                return new double[]{a * 1.0, a * 2.0, a * 3.0, a * 4.0};
            }
            public MultivariateMatrixFunction jacobian() {
                return new MultivariateMatrixFunction() {
                    public double[][] value(double[] point) {
                        return new double[][]{
                            {1.0},
                            {2.0},
                            {3.0},
                            {4.0}
                        };
                    }
                };
            }
        };
        double[] singleTarget = {2.0, 4.0, 6.0, 8.0}; // a=2
        double[] start = {1.0};
        VectorialPointValuePair optimum = optimizer.optimize(singleFunc, singleTarget, weights, start);
        assertEquals("Single parameter a", 2.0, optimum.getPoint()[0], 1e-10);
        double[][] cov = optimizer.getCovariances();
        assertEquals("Covariance matrix size", 1, cov.length);
        double[] sigma = optimizer.getSigma();
        assertEquals("Sigma size", 1, sigma.length);
    }
}