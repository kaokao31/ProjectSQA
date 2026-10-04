package org.apache.commons.math.optimization.general;

import org.apache.commons.math.analysis.DifferentiableMultivariateVectorialFunction;
import org.apache.commons.math.optimization.VectorialPointValuePair;
import org.junit.Assert;
import org.junit.Test;

public class LevenbergMarquardtOptimizerTest {

    private static class LinearFunction implements DifferentiableMultivariateVectorialFunction {
        private final double slope;
        private final double intercept;

        public LinearFunction(double slope, double intercept) {
            this.slope = slope;
            this.intercept = intercept;
        }

        public double[] value(double[] point) {
            double[] y = new double[2];
            y[0] = slope * point[0] + intercept;
            y[1] = slope * point[1] + intercept;
            return y;
        }

        public org.apache.commons.math.analysis.MultivariateMatrixFunction jacobian() {
            return new org.apache.commons.math.analysis.MultivariateMatrixFunction() {
                public double[][] value(double[] point) {
                    return new double[][] {
                        {point[0], 1.0},
                        {point[1], 1.0}
                    };
                }
            };
        }
    }

    private static class SimpleFunction implements DifferentiableMultivariateVectorialFunction {
        public double[] value(double[] point) {
            return new double[] { point[0] * point[0] };
        }

        public org.apache.commons.math.analysis.MultivariateMatrixFunction jacobian() {
            return new org.apache.commons.math.analysis.MultivariateMatrixFunction() {
                public double[][] value(double[] point) {
                    return new double[][] { { 2.0 * point[0] } };
                }
            };
        }
    }

    @Test
    public void testDefaultConstructor() {
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        assertNotNull(optimizer);
    }

    @Test
    public void testOptimizeSimple() throws Exception {
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        
        SimpleFunction f = new SimpleFunction();
        double[] target = new double[] { 0.0 };
        double[] weights = new double[] { 1.0 };
        double[] startPoint = new double[] { 2.0 };

        VectorialPointValuePair result = optimizer.optimize(f, target, weights, startPoint);
        assertNotNull(result);
        Assert.assertEquals(0.0, result.getPoint()[0], 1e-3);
    }

    @Test
    public void testParametersAndSettings() {
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        optimizer.setmaxIterations(100);
        optimizer.setConvergenceChecker(null);
        assertNotNull(optimizer);
    }

    @Test
    public void testOrthogonalColumnsCheck() throws Exception {
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        
        LinearFunction f = new LinearFunction(1.0, 2.0);
        double[] target = new double[] { 3.0, 5.0 };
        double[] weights = new double[] { 1.0, 1.0 };
        double[] startPoint = new double[] { 1.0, 1.0 };

        VectorialPointValuePair result = optimizer.optimize(f, target, weights, startPoint);
        assertNotNull(result);
    }

    @Test
    public void testZeroRowsOrColumns() {
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        try {
            optimizer.optimize(
                new SimpleFunction(),
                new double[0],
                new double[0],
                new double[] { 1.0 }
            );
        } catch (Exception e) {
            // Expected for dimension mismatch
        }
    }
}