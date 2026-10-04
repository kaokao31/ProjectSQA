package org.apache.commons.math.optimization.general;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.DifferentiableMultivariateVectorialFunction;
import org.apache.commons.math.analysis.MultivariateMatrixFunction;
import org.apache.commons.math.optimization.OptimizationException;
import org.apache.commons.math.optimization.SimpleVectorialValueChecker;
import org.apache.commons.math.optimization.VectorialPointValuePair;
import org.apache.commons.math.optimization.general.LevenbergMarquardtOptimizer;

public class LevenbergMarquardtOptimizerTest {

    private LevenbergMarquardtOptimizer optimizer;

    @Before
    public void setUp() {
        optimizer = new LevenbergMarquardtOptimizer();
        optimizer.setMaxIterations(1000);
        optimizer.setConvergenceChecker(new SimpleVectorialValueChecker(1.0e-10, 1.0e-10));
    }

    // Simple linear function: y = a*x + b, with parameters a and b
    private static class LinearFunction implements DifferentiableMultivariateVectorialFunction {
        private final double a;
        private final double b;

        LinearFunction(double a, double b) {
            this.a = a;
            this.b = b;
        }

        public double[] value(double[] point) throws FunctionEvaluationException {
            double x = point[0];
            return new double[] { a * x + b };
        }

        public MultivariateMatrixFunction jacobian() {
            return new MultivariateMatrixFunction() {
                public double[][] value(double[] point) {
                    return new double[][] { { point[0], 1.0 } };
                }
            };
        }
    }

    @Test
    public void testLinearFit() throws OptimizationException, FunctionEvaluationException {
        double a = 2.0;
        double b = 3.0;
        LinearFunction f = new LinearFunction(a, b);
        double[] target = new double[] { a * 1.0 + b, a * 2.0 + b, a * 3.0 + b };
        double[] start = new double[] { 0.0, 0.0 };
        VectorialPointValuePair optimum = optimizer.optimize(f, target, start);
        double[] params = optimum.getPoint();
        assertEquals("a", a, params[0], 1.0e-6);
        assertEquals("b", b, params[1], 1.0e-6);
    }

    // Rosenbrock function: f(x,y) = (1-x)^2 + 100*(y-x^2)^2
    private static class RosenbrockFunction implements DifferentiableMultivariateVectorialFunction {
        public double[] value(double[] point) {
            double x = point[0];
            double y = point[1];
            return new double[] { 1.0 - x, 10.0 * (y - x * x) };
        }

        public MultivariateMatrixFunction jacobian() {
            return new MultivariateMatrixFunction() {
                public double[][] value(double[] point) {
                    double x = point[0];
                    double y = point[1];
                    return new double[][] {
                        { -1.0, 0.0 },
                        { -20.0 * x, 10.0 }
                    };
                }
            };
        }
    }

    @Test
    public void testRosenbrock() throws OptimizationException, FunctionEvaluationException {
        RosenbrockFunction f = new RosenbrockFunction();
        double[] target = new double[] { 0.0, 0.0 };
        double[] start = new double[] { -1.2, 1.0 };
        VectorialPointValuePair optimum = optimizer.optimize(f, target, start);
        double[] params = optimum.getPoint();
        assertEquals("x", 1.0, params[0], 1.0e-6);
        assertEquals("y", 1.0, params[1], 1.0e-6);
    }

    @Test(expected = OptimizationException.class)
    public void testMaxIterationsExceeded() throws OptimizationException, FunctionEvaluationException {
        optimizer.setMaxIterations(1);
        RosenbrockFunction f = new RosenbrockFunction();
        double[] target = new double[] { 0.0, 0.0 };
        double[] start = new double[] { -1.2, 1.0 };
        optimizer.optimize(f, target, start);
    }

    @Test(expected = NullPointerException.class)
    public void testNullFunction() throws OptimizationException, FunctionEvaluationException {
        double[] target = new double[] { 1.0 };
        double[] start = new double[] { 0.0 };
        optimizer.optimize(null, target, start);
    }

    @Test(expected = NullPointerException.class)
    public void testNullTarget() throws OptimizationException, FunctionEvaluationException {
        LinearFunction f = new LinearFunction(1.0, 0.0);
        double[] start = new double[] { 0.0 };
        optimizer.optimize(f, null, start);
    }

    @Test(expected = NullPointerException.class)
    public void testNullStart() throws OptimizationException, FunctionEvaluationException {
        LinearFunction f = new LinearFunction(1.0, 0.0);
        double[] target = new double[] { 1.0 };
        optimizer.optimize(f, target, null);
    }

    @Test(expected = FunctionEvaluationException.class)
    public void testFunctionThrowsException() throws OptimizationException, FunctionEvaluationException {
        DifferentiableMultivariateVectorialFunction badFunction = new DifferentiableMultivariateVectorialFunction() {
            public double[] value(double[] point) throws FunctionEvaluationException {
                throw new FunctionEvaluationException(point, "Error");
            }
            public MultivariateMatrixFunction jacobian() {
                return new MultivariateMatrixFunction() {
                    public double[][] value(double[] point) {
                        return new double[][] { { 1.0 } };
                    }
                };
            }
        };
        double[] target = new double[] { 0.0 };
        double[] start = new double[] { 0.0 };
        optimizer.optimize(badFunction, target, start);
    }

    @Test
    public void testConvergenceOnSmallGradient() throws OptimizationException, FunctionEvaluationException {
        // Function that is already at optimum
        LinearFunction f = new LinearFunction(1.0, 0.0);
        double[] target = new double[] { 0.0 };
        double[] start = new double[] { 0.0 };
        VectorialPointValuePair optimum = optimizer.optimize(f, target, start);
        assertEquals(0.0, optimum.getPoint()[0], 1.0e-10);
    }

    @Test
    public void testSingularJacobian() throws OptimizationException, FunctionEvaluationException {
        // Function with singular Jacobian (constant)
        DifferentiableMultivariateVectorialFunction singular = new DifferentiableMultivariateVectorialFunction() {
            public double[] value(double[] point) {
                return new double[] { 1.0 };
            }
            public MultivariateMatrixFunction jacobian() {
                return new MultivariateMatrixFunction() {
                    public double[][] value(double[] point) {
                        return new double[][] { { 0.0 } };
                    }
                };
            }
        };
        double[] target = new double[] { 0.0 };
        double[] start = new double[] { 1.0 };
        try {
            optimizer.optimize(singular, target, start);
            fail("Expected OptimizationException for singular Jacobian");
        } catch (OptimizationException e) {
            // expected
        }
    }

    @Test
    public void testLargeResiduals() throws OptimizationException, FunctionEvaluationException {
        // Function with large residuals
        LinearFunction f = new LinearFunction(1.0, 0.0);
        double[] target = new double[] { 1.0e10 };
        double[] start = new double[] { 0.0 };
        VectorialPointValuePair optimum = optimizer.optimize(f, target, start);
        assertEquals(1.0e10, optimum.getPoint()[0], 1.0e-6);
    }

    @Test
    public void testMultipleObservations() throws OptimizationException, FunctionEvaluationException {
        // Fit a line to multiple points
        double a = 1.5;
        double b = -0.5;
        LinearFunction f = new LinearFunction(a, b);
        double[] x = new double[] { -2.0, -1.0, 0.0, 1.0, 2.0 };
        double[] target = new double[x.length];
        for (int i = 0; i < x.length; i++) {
            target[i] = a * x[i] + b;
        }
        double[] start = new double[] { 0.0, 0.0 };
        VectorialPointValuePair optimum = optimizer.optimize(f, target, start);
        assertEquals("a", a, optimum.getPoint()[0], 1.0e-6);
        assertEquals("b", b, optimum.getPoint()[1], 1.0e-6);
    }

    @Test
    public void testZeroResiduals() throws OptimizationException, FunctionEvaluationException {
        // Function that can achieve zero residuals
        LinearFunction f = new LinearFunction(1.0, 0.0);
        double[] target = new double[] { 0.0, 0.0 };
        double[] start = new double[] { 1.0, 1.0 };
        VectorialPointValuePair optimum = optimizer.optimize(f, target, start);
        assertEquals(0.0, optimum.getPoint()[0], 1.0e-10);
        assertEquals(0.0, optimum.getPoint()[1], 1.0e-10);
    }

    @Test
    public void testNaNInFunction() throws OptimizationException, FunctionEvaluationException {
        DifferentiableMultivariateVectorialFunction nanFunction = new DifferentiableMultivariateVectorialFunction() {
            public double[] value(double[] point) {
                return new double[] { Double.NaN };
            }
            public MultivariateMatrixFunction jacobian() {
                return new MultivariateMatrixFunction() {
                    public double[][] value(double[] point) {
                        return new double[][] { { 1.0 } };
                    }
                };
            }
        };
        double[] target = new double[] { 0.0 };
        double[] start = new double[] { 0.0 };
        try {
            optimizer.optimize(nanFunction, target, start);
            fail("Expected OptimizationException for NaN");
        } catch (OptimizationException e) {
            // expected
        }
    }

    @Test
    public void testInfinityInFunction() throws OptimizationException, FunctionEvaluationException {
        DifferentiableMultivariateVectorialFunction infFunction = new DifferentiableMultivariateVectorialFunction() {
            public double[] value(double[] point) {
                return new double[] { Double.POSITIVE_INFINITY };
            }
            public MultivariateMatrixFunction jacobian() {
                return new MultivariateMatrixFunction() {
                    public double[][] value(double[] point) {
                        return new double[][] { { 1.0 } };
                    }
                };
            }
        };
        double[] target = new double[] { 0.0 };
        double[] start = new double[] { 0.0 };
        try {
            optimizer.optimize(infFunction, target, start);
            fail("Expected OptimizationException for Infinity");
        } catch (OptimizationException e) {
            // expected
        }
    }

    @Test
    public void testMismatchedDimensions() throws OptimizationException, FunctionEvaluationException {
        LinearFunction f = new LinearFunction(1.0, 0.0);
        double[] target = new double[] { 1.0, 2.0 }; // 2 observations
        double[] start = new double[] { 0.0, 0.0, 0.0 }; // 3 parameters
        try {
            optimizer.optimize(f, target, start);
            fail("Expected IllegalArgumentException for dimension mismatch");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testCustomConvergenceChecker() throws OptimizationException, FunctionEvaluationException {
        optimizer.setConvergenceChecker(new SimpleVectorialValueChecker(1.0e-2, 1.0e-2));
        RosenbrockFunction f = new RosenbrockFunction();
        double[] target = new double[] { 0.0, 0.0 };
        double[] start = new double[] { -1.2, 1.0 };
        VectorialPointValuePair optimum = optimizer.optimize(f, target, start);
        // Should converge but with lower precision
        assertNotNull(optimum);
    }

    @Test
    public void testNoConvergenceChecker() throws OptimizationException, FunctionEvaluationException {
        optimizer.setConvergenceChecker(null);
        RosenbrockFunction f = new RosenbrockFunction();
        double[] target = new double[] { 0.0, 0.0 };
        double[] start = new double[] { -1.2, 1.0 };
        VectorialPointValuePair optimum = optimizer.optimize(f, target, start);
        assertNotNull(optimum);
    }
}