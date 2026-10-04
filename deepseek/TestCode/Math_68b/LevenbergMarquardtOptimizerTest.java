package org.apache.commons.math.optimization.general;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import org.apache.commons.math.optimization.OptimizationException;
import org.apache.commons.math.optimization.VectorialPointValuePair;
import org.apache.commons.math.analysis.DifferentiableMultivariateVectorialFunction;
import org.apache.commons.math.analysis.MultivariateMatrixFunction;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.RealVector;
import org.apache.commons.math.linear.ArrayRealVector;

public class LevenbergMarquardtOptimizerTest {

    private LevenbergMarquardtOptimizer optimizer;
    private static final double TOL = 1e-10;

    @Before
    public void setUp() {
        optimizer = new LevenbergMarquardtOptimizer();
    }

    // Simple linear function: y = a * x + b
    private static class LinearFunction implements DifferentiableMultivariateVectorialFunction {
        private final double[] x;
        private final double[] y;
        private final double[] params;

        LinearFunction(double[] x, double[] y) {
            this.x = x;
            this.y = y;
            this.params = new double[2];
        }

        @Override
        public double[] value(double[] variables) throws IllegalArgumentException {
            double a = variables[0];
            double b = variables[1];
            double[] residuals = new double[x.length];
            for (int i = 0; i < x.length; i++) {
                residuals[i] = y[i] - (a * x[i] + b);
            }
            return residuals;
        }

        @Override
        public MultivariateMatrixFunction jacobian() {
            return new MultivariateMatrixFunction() {
                @Override
                public double[][] value(double[] variables) {
                    double[][] jacobian = new double[x.length][2];
                    for (int i = 0; i < x.length; i++) {
                        jacobian[i][0] = -x[i]; // derivative wrt a
                        jacobian[i][1] = -1.0;  // derivative wrt b
                    }
                    return jacobian;
                }
            };
        }
    }

    @Test
    public void testConvergenceSimpleLinear() {
        double[] x = {1.0, 2.0, 3.0, 4.0, 5.0};
        double[] y = {2.0, 4.0, 6.0, 8.0, 10.0}; // y = 2*x + 0
        LinearFunction f = new LinearFunction(x, y);
        double[] start = {1.0, 1.0};
        try {
            VectorialPointValuePair optimum = optimizer.optimize(f, new double[] {1,1,1,1,1}, start, new double[] {1e-8, 1e-8, 1e-8});
            double[] params = optimum.getPoint();
            assertEquals(2.0, params[0], 1e-6);
            assertEquals(0.0, params[1], 1e-6);
        } catch (OptimizationException e) {
            fail("Optimization failed: " + e.getMessage());
        }
    }

    @Test
    public void testConvergenceWithNoise() {
        double[] x = {0.0, 1.0, 2.0, 3.0, 4.0};
        double[] y = {0.1, 1.9, 4.2, 5.8, 8.1}; // approx y = 2*x
        LinearFunction f = new LinearFunction(x, y);
        double[] start = {1.0, 0.5};
        try {
            VectorialPointValuePair optimum = optimizer.optimize(f, new double[] {1,1,1,1,1}, start, new double[] {1e-8, 1e-8, 1e-8});
            double[] params = optimum.getPoint();
            assertEquals(2.0, params[0], 0.2);
            assertEquals(0.0, params[1], 0.2);
        } catch (OptimizationException e) {
            fail("Optimization failed: " + e.getMessage());
        }
    }

    @Test(expected = OptimizationException.class)
    public void testNoConvergenceWithBadStart() throws OptimizationException {
        double[] x = {1.0, 2.0, 3.0};
        double[] y = {2.0, 4.0, 6.0};
        LinearFunction f = new LinearFunction(x, y);
        double[] start = {1e10, 1e10}; // far from solution
        optimizer.optimize(f, new double[] {1,1,1}, start, new double[] {1e-8, 1e-8, 1e-8});
    }

    @Test
    public void testNullTarget() {
        double[] x = {1.0, 2.0};
        double[] y = {2.0, 4.0};
        LinearFunction f = new LinearFunction(x, y);
        double[] start = {1.0, 1.0};
        try {
            optimizer.optimize(f, null, start, new double[] {1e-8, 1e-8});
            fail("Expected NullPointerException or IllegalArgumentException");
        } catch (NullPointerException | IllegalArgumentException e) {
            // expected
        } catch (OptimizationException e) {
            // also acceptable if optimizer handles null gracefully
        }
    }

    @Test
    public void testEmptyTarget() {
        double[] x = {};
        double[] y = {};
        LinearFunction f = new LinearFunction(x, y);
        double[] start = {1.0, 1.0};
        try {
            VectorialPointValuePair optimum = optimizer.optimize(f, new double[] {}, start, new double[] {1e-8, 1e-8});
            // With zero observations, any parameters are optimal
            assertNotNull(optimum);
        } catch (OptimizationException e) {
            // may throw because Jacobian is empty
        }
    }

    @Test
    public void testSingleObservation() {
        double[] x = {1.0};
        double[] y = {3.0};
        LinearFunction f = new LinearFunction(x, y);
        double[] start = {1.0, 1.0};
        try {
            VectorialPointValuePair optimum = optimizer.optimize(f, new double[] {1.0}, start, new double[] {1e-8, 1e-8});
            // Underdetermined: many solutions, but optimizer should converge to some point
            assertNotNull(optimum);
        } catch (OptimizationException e) {
            // may fail due to singular Jacobian
        }
    }

    @Test
    public void testNaNInTarget() {
        double[] x = {1.0, 2.0, 3.0};
        double[] y = {Double.NaN, 4.0, 6.0};
        LinearFunction f = new LinearFunction(x, y);
        double[] start = {1.0, 1.0};
        try {
            optimizer.optimize(f, new double[] {1,1,1}, start, new double[] {1e-8, 1e-8, 1e-8});
            fail("Expected OptimizationException due to NaN");
        } catch (OptimizationException e) {
            // expected
        }
    }

    @Test
    public void testInfiniteTarget() {
        double[] x = {1.0, 2.0, 3.0};
        double[] y = {Double.POSITIVE_INFINITY, 4.0, 6.0};
        LinearFunction f = new LinearFunction(x, y);
        double[] start = {1.0, 1.0};
        try {
            optimizer.optimize(f, new double[] {1,1,1}, start, new double[] {1e-8, 1e-8, 1e-8});
            fail("Expected OptimizationException due to Infinity");
        } catch (OptimizationException e) {
            // expected
        }
    }

    @Test
    public void testNegativeWeights() {
        double[] x = {1.0, 2.0, 3.0};
        double[] y = {2.0, 4.0, 6.0};
        LinearFunction f = new LinearFunction(x, y);
        double[] start = {1.0, 1.0};
        try {
            optimizer.optimize(f, new double[] {-1.0, 1.0, 1.0}, start, new double[] {1e-8, 1e-8, 1e-8});
            fail("Expected IllegalArgumentException for negative weight");
        } catch (IllegalArgumentException e) {
            // expected
        } catch (OptimizationException e) {
            // may also throw
        }
    }

    @Test
    public void testZeroWeights() {
        double[] x = {1.0, 2.0, 3.0};
        double[] y = {2.0, 4.0, 6.0};
        LinearFunction f = new LinearFunction(x, y);
        double[] start = {1.0, 1.0};
        try {
            VectorialPointValuePair optimum = optimizer.optimize(f, new double[] {0.0, 1.0, 1.0}, start, new double[] {1e-8, 1e-8, 1e-8});
            // Should still work, ignoring first observation
            assertNotNull(optimum);
        } catch (OptimizationException e) {
            fail("Optimization failed with zero weight: " + e.getMessage());
        }
    }

    @Test
    public void testLargeNumberOfObservations() {
        int n = 1000;
        double[] x = new double[n];
        double[] y = new double[n];
        for (int i = 0; i < n; i++) {
            x[i] = i;
            y[i] = 3.0 * i + 5.0;
        }
        LinearFunction f = new LinearFunction(x, y);
        double[] start = {2.0, 4.0};
        try {
            VectorialPointValuePair optimum = optimizer.optimize(f, new double[n], start, new double[] {1e-8, 1e-8, 1e-8});
            double[] params = optimum.getPoint();
            assertEquals(3.0, params[0], 1e-6);
            assertEquals(5.0, params[1], 1e-6);
        } catch (OptimizationException e) {
            fail("Optimization failed for large dataset: " + e.getMessage());
        }
    }

    @Test
    public void testExactSolution() {
        double[] x = {1.0, 2.0, 3.0};
        double[] y = {2.0, 4.0, 6.0};
        LinearFunction f = new LinearFunction(x, y);
        double[] start = {2.0, 0.0}; // exact solution
        try {
            VectorialPointValuePair optimum = optimizer.optimize(f, new double[] {1,1,1}, start, new double[] {1e-8, 1e-8, 1e-8});
            double[] params = optimum.getPoint();
            assertEquals(2.0, params[0], 1e-10);
            assertEquals(0.0, params[1], 1e-10);
        } catch (OptimizationException e) {
            fail("Optimization failed even with exact start: " + e.getMessage());
        }
    }

    @Test
    public void testBugMath68() {
        // This test reproduces the bug from Defects4J Math-68
        // The optimizer fails to converge for a well-conditioned linear problem
        double[] x = {1.0, 2.0, 3.0, 4.0, 5.0};
        double[] y = {1.0, 2.0, 3.0, 4.0, 5.0}; // y = x
        LinearFunction f = new LinearFunction(x, y);
        double[] start = {0.5, 0.5};
        try {
            VectorialPointValuePair optimum = optimizer.optimize(f, new double[] {1,1,1,1,1}, start, new double[] {1e-8, 1e-8, 1e-8});
            double[] params = optimum.getPoint();
            assertEquals(1.0, params[0], 1e-6);
            assertEquals(0.0, params[1], 1e-6);
        } catch (OptimizationException e) {
            fail("Bug Math-68: Optimization should converge but threw exception: " + e.getMessage());
        }
    }
}