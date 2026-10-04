package org.apache.commons.math.optimization.direct;

import org.apache.commons.math.analysis.MultivariateRealFunction;
import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.RealPointValuePair;
import org.apache.commons.math.optimization.SimpleRealPointChecker;
import org.apache.commons.math.optimization.SimpleScalarValueChecker;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test suite for MultiDirectional optimizer.
 * Generated to maximize line/branch coverage and reveal potential faults.
 */
public class MultiDirectionalTest {

    private MultiDirectional optimizer;
    private final double defaultAbsoluteTolerance = 1e-10;
    private final int defaultMaxIterations = 1000;

    @Before
    public void setUp() {
        optimizer = new MultiDirectional();
        optimizer.setMaxIterations(defaultMaxIterations);
        optimizer.setMaxEvaluations(2000);
        optimizer.setConvergenceChecker(new SimpleScalarValueChecker(1e-14, 1e-14));
    }

    /**
     * Test optimization of a simple quadratic function (2D).
     */
    @Test
    public void testSimpleQuadratic() {
        MultivariateRealFunction f = new MultivariateRealFunction() {
            public double value(double[] point) {
                double x = point[0];
                double y = point[1];
                return (x - 1.0) * (x - 1.0) + (y - 2.0) * (y - 2.0);
            }
        };

        RealPointValuePair result = optimizer.optimize(f, GoalType.MINIMIZE, new double[] {0.0, 0.0});
        double[] point = result.getPoint();
        double value = result.getValue();

        // Check that optimizer found near the optimum (1,2)
        assertEquals("x coordinate", 1.0, point[0], 0.05);
        assertEquals("y coordinate", 2.0, point[1], 0.05);
        assertTrue("Objective value near zero", value < 1e-3);
        assertTrue("Iterations should be at least 1", optimizer.getIterations() > 0);
    }

    /**
     * Test with a function that has a constant gradient (linear function).
     */
    @Test
    public void testLinearFunction() {
        MultivariateRealFunction f = new MultivariateRealFunction() {
            public double value(double[] point) {
                return point[0] + point[1];
            }
        };
        optimizer.setConvergenceChecker(new SimpleRealPointChecker(1e-12, 1e-12));
        RealPointValuePair result = optimizer.optimize(f, GoalType.MINIMIZE, new double[] {0.0, 0.0});
        // Linear functions are unbounded, but optimizer should converge to something.
        assertNotNull("Result should not be null", result);
        assertTrue("Iterations done", optimizer.getIterations() >= 0);
    }

    /**
     * Test with all zero initial guess.
     */
    @Test
    public void testZeroStart() {
        MultivariateRealFunction f = new MultivariateRealFunction() {
            public double value(double[] point) {
                return point[0] * point[0] + point[1] * point[1];
            }
        };
        RealPointValuePair result = optimizer.optimize(f, GoalType.MINIMIZE, new double[] {0.0, 0.0});
        double[] point = result.getPoint();
        assertEquals("x coordinate near 0", 0.0, point[0], 1e-3);
        assertEquals("y coordinate near 0", 0.0, point[1], 1e-3);
        assertTrue("Objective value near 0", result.getValue() < 1e-6);
    }

    /**
     * Test with high dimensionality (to stress internal data structures).
     */
    @Test
    public void testHighDimension() {
        final int dim = 10;
        MultivariateRealFunction f = new MultivariateRealFunction() {
            public double value(double[] point) {
                double sum = 0.0;
                for (int i = 0; i < point.length; i++) {
                    sum += point[i] * point[i];
                }
                return sum;
            }
        };
        double[] start = new double[dim];
        for (int i = 0; i < dim; i++) {
            start[i] = 1.0;
        }
        optimizer.setMaxIterations(2000);
        RealPointValuePair result = optimizer.optimize(f, GoalType.MINIMIZE, start);
        double[] point = result.getPoint();
        for (int i = 0; i < dim; i++) {
            assertEquals("Coordinate " + i, 0.0, point[i], 1e-2);
        }
        assertTrue("Objective near zero", result.getValue() < 1e-4);
    }

    /**
     * Test that optimizer throws exception on null function.
     */
    @Test(expected = org.apache.commons.math.exception.MathIllegalArgumentException.class)
    public void testNullFunction() {
        optimizer.optimize(null, GoalType.MINIMIZE, new double[] {0.0});
    }

    /**
     * Test that optimizer throws exception on null goal type.
     */
    @Test(expected = org.apache.commons.math.exception.MathIllegalArgumentException.class)
    public void testNullGoalType() {
        MultivariateRealFunction f = new MultivariateRealFunction() {
            public double value(double[] point) { return 1.0; }
        };
        optimizer.optimize(f, null, new double[] {0.0});
    }

    /**
     * Test that optimizer throws exception on null starting point.
     */
    @Test(expected = org.apache.commons.math.exception.MathIllegalArgumentException.class)
    public void testNullStartPoint() {
        MultivariateRealFunction f = new MultivariateRealFunction() {
            public double value(double[] point) { return 1.0; }
        };
        optimizer.optimize(f, GoalType.MINIMIZE, null);
    }

    /**
     * Test with very small absolute tolerance to force many iterations.
     */
    @Test(timeout = 5000)
    public void testTightTolerance() {
        optimizer.setConvergenceChecker(new SimpleScalarValueChecker(1e-20, 1e-20));
        optimizer.setMaxIterations(10000);
        MultivariateRealFunction f = new MultivariateRealFunction() {
            public double value(double[] point) {
                return (point[0] - 0.5) * (point[0] - 0.5);
            }
        };
        RealPointValuePair result = optimizer.optimize(f, GoalType.MINIMIZE, new double[] {0.0});
        assertEquals("x coordinate", 0.5, result.getPoint()[0], 1e-4);
    }

    /**
     * Test behavior when maximum evaluations are exhausted.
     */
    @Test(expected = org.apache.commons.math.optimization.OptimizationException.class)
    public void testMaxEvaluationsReached() {
        optimizer.setMaxEvaluations(1); // very low
        MultivariateRealFunction f = new MultivariateRealFunction() {
            public double value(double[] point) {
                return point[0] * point[0];
            }
        };
        optimizer.optimize(f, GoalType.MINIMIZE, new double[] {10.0});
    }

    /**
     * Test with a function that returns NaN at start.
     */
    @Test
    public void testNaNStartValue() {
        // The optimizer should handle NaN in evaluation.
        MultivariateRealFunction f = new MultivariateRealFunction() {
            public double value(double[] point) {
                return Double.NaN;
            }
        };
        try {
            optimizer.optimize(f, GoalType.MINIMIZE, new double[] {0.0});
            fail("Expected OptimizationException due to NaN");
        } catch (org.apache.commons.math.optimization.OptimizationException e) {
            // expected
        } catch (Exception e) {
            // any exception is acceptable
        }
    }

    /**
     * Test maximize goal with a simple negative quadratic.
     */
    @Test
    public void testMaximize() {
        MultivariateRealFunction f = new MultivariateRealFunction() {
            public double value(double[] point) {
                return - (point[0] * point[0] + point[1] * point[1]);
            }
        };
        RealPointValuePair result = optimizer.optimize(f, GoalType.MAXIMIZE, new double[] {2.0, -3.0});
        double[] point = result.getPoint();
        assertEquals("x coordinate near 0", 0.0, point[0], 0.1);
        assertEquals("y coordinate near 0", 0.0, point[1], 0.1);
        assertTrue("Objective value near 0", result.getValue() > -1e-4);
    }

    /**
     * Test with a non-smooth function (discontinuous at point).
     */
    @Test
    public void testDiscontinuousFunction() {
        MultivariateRealFunction f = new MultivariateRealFunction() {
            public double value(double[] point) {
                if (point[0] < 0) {
                    return point[0] * point[0] + 100;
                } else {
                    return point[0] * point[0];
                }
            }
        };
        RealPointValuePair result = optimizer.optimize(f, GoalType.MINIMIZE, new double[] {1.0});
        // Should find the minimum near 0 (with value 0)
        assertEquals("x coordinate near 0", 0.0, result.getPoint()[0], 0.1);
    }

    /**
     * Test that the optimizer can be reused with different checkers.
     */
    @Test
    public void testResetChecker() {
        MultivariateRealFunction f = new MultivariateRealFunction() {
            public double value(double[] point) {
                return point[0] * point[0];
            }
        };
        // First optimization with default checker
        optimizer.optimize(f, GoalType.MINIMIZE, new double[] {5.0});
        // Change checker and run again
        optimizer.setConvergenceChecker(new SimpleRealPointChecker(1e-8, 1e-8));
        RealPointValuePair result = optimizer.optimize(f, GoalType.MINIMIZE, new double[] {-3.0});
        assertEquals("x near 0", 0.0, result.getPoint()[0], 1e-6);
    }

    /**
     * Test with a function that has multiple local minima (Rosenbrock-like).
     */
    @Test(timeout = 10000)
    public void testRosenbrock() {
        // Rosenbrock function: f(x,y) = (1-x)^2 + 100*(y-x^2)^2
        MultivariateRealFunction f = new MultivariateRealFunction() {
            public double value(double[] point) {
                double x = point[0];
                double y = point[1];
                return (1.0 - x) * (1.0 - x) + 100.0 * (y - x * x) * (y - x * x);
            }
        };
        optimizer.setMaxIterations(5000);
        optimizer.setConvergenceChecker(new SimpleScalarValueChecker(1e-12, 1e-12));
        RealPointValuePair result = optimizer.optimize(f, GoalType.MINIMIZE, new double[] {-1.0, 1.0});
        double[] point = result.getPoint();
        // Expecting near (1,1)
        assertEquals("x near 1", 1.0, point[0], 0.1);
        assertEquals("y near 1", 1.0, point[1], 0.1);
        assertTrue("Objective value small", result.getValue() < 1);
    }

    /**
     * Test that iteration count is recorded properly.
     */
    @Test
    public void testIterationCount() {
        MultivariateRealFunction f = new MultivariateRealFunction() {
            public double value(double[] point) {
                return point[0] * point[0] + 1;
            }
        };
        optimizer.setMaxIterations(50);
        optimizer.optimize(f, GoalType.MINIMIZE, new double[] {100.0});
        int iterations = optimizer.getIterations();
        assertTrue("Iterations should be between 1 and 50", iterations >= 1 && iterations <= 50);
    }

    /**
     * Test to exercise the branch where the simplex evaluation count is zero.
     */
    @Test(expected = org.apache.commons.math.optimization.OptimizationException.class)
    public void testZeroEvaluations() {
        optimizer.setMaxEvaluations(0);
        MultivariateRealFunction f = new MultivariateRealFunction() {
            public double value(double[] point) {
                return 0.0;
            }
        };
        optimizer.optimize(f, GoalType.MINIMIZE, new double[] {0.0});
    }

    /**
     * Test that the optimizer can handle a 1D function.
     */
    @Test
    public void testOneDimensional() {
        MultivariateRealFunction f = new MultivariateRealFunction() {
            public double value(double[] point) {
                return (point[0] - 3.0) * (point[0] - 3.0);
            }
        };
        RealPointValuePair result = optimizer.optimize(f, GoalType.MINIMIZE, new double[] {0.0});
        assertEquals("x coordinate near 3", 3.0, result.getPoint()[0], 0.05);
        assertTrue("Objective near 0", result.getValue() < 1e-4);
    }
}