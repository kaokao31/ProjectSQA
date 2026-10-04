package org.apache.commons.math.optimization.direct;

import org.apache.commons.math.analysis.MultivariateFunction;
import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.PointValuePair;
import org.apache.commons.math.optimization.direct.BOBYQAOptimizer;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * JUnit 4 test suite for BOBYQAOptimizer, targeting high coverage and fault detection
 * (including dimension 1 bug from Defects4J Math-38).
 */
public class BOBYQAOptimizerTest {

    private static final double DEFAULT_TOLERANCE = 1e-8;
    private static final int DEFAULT_MAX_EVALS = 1000;

    // ---------------------- Simple 1D tests ----------------------

    @Test
    public void testDimensionOne() {
        // Simple 1D: f(x) = x^2, minimum at 0.0
        BOBYQAOptimizer optimizer = new BOBYQAOptimizer(5); // 5 interpolation points (>= n+2)
        MultivariateFunction f = new MultivariateFunction() {
            @Override
            public double value(double[] point) {
                return point[0] * point[0];
            }
        };
        double[] startPoint = new double[] { 5.0 };
        double[] lower = new double[] { -10.0 };
        double[] upper = new double[] { 10.0 };

        try {
            PointValuePair result = optimizer.optimize(
                    DEFAULT_MAX_EVALS,
                    f,
                    GoalType.MINIMIZE,
                    startPoint,
                    lower,
                    upper
            );
            assertNotNull("Result should not be null", result);
            double expectedX = 0.0;
            double expectedF = 0.0;
            assertEquals("X coordinate should be near 0", expectedX, result.getPoint()[0], 1e-6);
            assertEquals("Function value should be near 0", expectedF, result.getValue(), 1e-6);
        } catch (Exception e) {
            fail("Optimizer should not throw exception for dimension 1, but got: " + e.getMessage());
        }
    }

    @Test
    public void testDimensionOneNegativeStart() {
        BOBYQAOptimizer optimizer = new BOBYQAOptimizer(5);
        MultivariateFunction f = new MultivariateFunction() {
            @Override
            public double value(double[] point) {
                return (point[0] - 3.0) * (point[0] - 3.0) + 1.0;
            }
        };
        double[] startPoint = new double[] { -5.0 };
        double[] lower = new double[] { -20.0 };
        double[] upper = new double[] { 20.0 };

        try {
            PointValuePair result = optimizer.optimize(
                    DEFAULT_MAX_EVALS, f, GoalType.MINIMIZE, startPoint, lower, upper);
            assertEquals("X coordinate should be near 3", 3.0, result.getPoint()[0], 1e-6);
            assertEquals("Function value should be near 1", 1.0, result.getValue(), 1e-6);
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    // ---------------------- 2D tests ----------------------

    @Test
    public void testDimensionTwoSimpleQuadratic() {
        BOBYQAOptimizer optimizer = new BOBYQAOptimizer(7); // interpolation points >= n+2 = 4
        MultivariateFunction f = new MultivariateFunction() {
            @Override
            public double value(double[] point) {
                return point[0] * point[0] + point[1] * point[1];
            }
        };
        double[] startPoint = new double[] { 3.0, 4.0 };
        double[] lower = new double[] { -10.0, -10.0 };
        double[] upper = new double[] { 10.0, 10.0 };

        PointValuePair result = optimizer.optimize(
                DEFAULT_MAX_EVALS, f, GoalType.MINIMIZE, startPoint, lower, upper);
        assertNotNull(result);
        assertEquals(0.0, result.getPoint()[0], 1e-6);
        assertEquals(0.0, result.getPoint()[1], 1e-6);
        assertEquals(0.0, result.getValue(), 1e-6);
    }

    @Test
    public void testDimensionTwoWithLinearTerms() {
        BOBYQAOptimizer optimizer = new BOBYQAOptimizer(10);
        MultivariateFunction f = new MultivariateFunction() {
            @Override
            public double value(double[] point) {
                // f(x,y) = (x-1)^2 + (y+2)^2 + 5
                double dx = point[0] - 1.0;
                double dy = point[1] + 2.0;
                return dx * dx + dy * dy + 5.0;
            }
        };
        double[] startPoint = new double[] { 0.0, 0.0 };
        double[] lower = new double[] { -5.0, -5.0 };
        double[] upper = new double[] { 5.0, 5.0 };

        PointValuePair result = optimizer.optimize(
                DEFAULT_MAX_EVALS, f, GoalType.MINIMIZE, startPoint, lower, upper);
        assertEquals(1.0, result.getPoint()[0], 1e-5);
        assertEquals(-2.0, result.getPoint()[1], 1e-5);
        assertEquals(5.0, result.getValue(), 1e-5);
    }

    // ---------------------- Boundary edge cases ----------------------

    @Test
    public void testBoundaryAtMinimum() {
        // Minimum lies on boundary: x in [2, 10], function (x-1)^2 -> minimum at x=2
        BOBYQAOptimizer optimizer = new BOBYQAOptimizer(5);
        MultivariateFunction f = new MultivariateFunction() {
            @Override
            public double value(double[] point) {
                double dx = point[0] - 1.0;
                return dx * dx;
            }
        };
        double[] startPoint = new double[] { 5.0 };
        double[] lower = new double[] { 2.0 };
        double[] upper = new double[] { 10.0 };

        PointValuePair result = optimizer.optimize(
                DEFAULT_MAX_EVALS, f, GoalType.MINIMIZE, startPoint, lower, upper);
        assertEquals(2.0, result.getPoint()[0], 1e-6);
        assertEquals(1.0, result.getValue(), 1e-6);
    }

    @Test
    public void testBoundaryAtMaximum() {
        // Minimum lies on boundary: x in [-10, -1], function (x+2)^2 -> minimum at x=-1
        BOBYQAOptimizer optimizer = new BOBYQAOptimizer(5);
        MultivariateFunction f = new MultivariateFunction() {
            @Override
            public double value(double[] point) {
                double dx = point[0] + 2.0;
                return dx * dx;
            }
        };
        double[] startPoint = new double[] { -5.0 };
        double[] lower = new double[] { -10.0 };
        double[] upper = new double[] { -1.0 };

        PointValuePair result = optimizer.optimize(
                DEFAULT_MAX_EVALS, f, GoalType.MINIMIZE, startPoint, lower, upper);
        assertEquals(-1.0, result.getPoint()[0], 1e-5);
        assertEquals(1.0, result.getValue(), 1e-5);
    }

    // ---------------------- No bounds test ----------------------

    @Test
    public void testWithoutBounds() {
        // BOBYQA requires bounds, but we can use large bounds as surrogate.
        BOBYQAOptimizer optimizer = new BOBYQAOptimizer(6);
        MultivariateFunction f = new MultivariateFunction() {
            @Override
            public double value(double[] point) {
                return point[0] * point[0] + point[1] * point[1] + point[2] * point[2];
            }
        };
        double[] startPoint = new double[] { 1.0, 2.0, 3.0 };
        double[] lower = new double[] { -100.0, -100.0, -100.0 };
        double[] upper = new double[] { 100.0, 100.0, 100.0 };

        PointValuePair result = optimizer.optimize(
                DEFAULT_MAX_EVALS, f, GoalType.MINIMIZE, startPoint, lower, upper);
        assertEquals(0.0, result.getPoint()[0], 1e-6);
        assertEquals(0.0, result.getPoint()[1], 1e-6);
        assertEquals(0.0, result.getPoint()[2], 1e-6);
        assertEquals(0.0, result.getValue(), 1e-6);
    }

    @Test
    public void testRosenbrock() {
        // Rosenbrock function: f(x,y) = (1-x)^2 + 100*(y-x^2)^2, minimum at (1,1)
        BOBYQAOptimizer optimizer = new BOBYQAOptimizer(12);
        MultivariateFunction f = new MultivariateFunction() {
            @Override
            public double value(double[] point) {
                double x = point[0];
                double y = point[1];
                double term1 = 1.0 - x;
                double term2 = y - x * x;
                return term1 * term1 + 100.0 * term2 * term2;
            }
        };
        double[] startPoint = new double[] { -1.0, 1.0 };
        double[] lower = new double[] { -10.0, -10.0 };
        double[] upper = new double[] { 10.0, 10.0 };

        PointValuePair result = optimizer.optimize(
                5000, f, GoalType.MINIMIZE, startPoint, lower, upper);
        assertEquals(1.0, result.getPoint()[0], 1e-3);
        assertEquals(1.0, result.getPoint()[1], 1e-3);
        assertEquals(0.0, result.getValue(), 1e-3);
    }

    // ---------------------- Test with extreme values ----------------------

    @Test
    public void testExtremeBoundaries() {
        // Test with very small interval and starting point at bound
        BOBYQAOptimizer optimizer = new BOBYQAOptimizer(5);
        MultivariateFunction f = new MultivariateFunction() {
            @Override
            public double value(double[] point) {
                return point[0] * point[0];
            }
        };
        double[] startPoint = new double[] { 0.5 };
        double[] lower = new double[] { 0.0 };
        double[] upper = new double[] { 1.0 };

        PointValuePair result = optimizer.optimize(
                DEFAULT_MAX_EVALS, f, GoalType.MINIMIZE, startPoint, lower, upper);
        assertEquals(0.0, result.getPoint()[0], 1e-6);
        assertEquals(0.0, result.getValue(), 1e-6);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidBoundsOrder() {
        // Lower bound greater than upper bound should throw exception
        BOBYQAOptimizer optimizer = new BOBYQAOptimizer(5);
        MultivariateFunction f = new MultivariateFunction() {
            @Override
            public double value(double[] point) {
                return point[0];
            }
        };
        double[] startPoint = new double[] { 0.0 };
        double[] lower = new double[] { 10.0 };
        double[] upper = new double[] { -10.0 };
        // This call should throw IllegalArgumentException
        optimizer.optimize(DEFAULT_MAX_EVALS, f, GoalType.MINIMIZE, startPoint, lower, upper);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDimensionMismatchBounds() {
        // Bounds length != dimension
        BOBYQAOptimizer optimizer = new BOBYQAOptimizer(5);
        MultivariateFunction f = new MultivariateFunction() {
            @Override
            public double value(double[] point) {
                return point[0] * point[0];
            }
        };
        double[] startPoint = new double[] { 0.0 };
        double[] lower = new double[] { -10.0, -10.0 }; // 2 bounds for 1D
        double[] upper = new double[] { 10.0, 10.0 };
        optimizer.optimize(DEFAULT_MAX_EVALS, f, GoalType.MINIMIZE, startPoint, lower, upper);
    }
}