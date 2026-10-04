package org.apache.commons.math3.optimization.direct;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.optimization.GoalType;
import org.apache.commons.math3.optimization.PointValuePair;
import org.apache.commons.math3.optimization.SimpleValueChecker;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.util.FastMath;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Comprehensive JUnit 4 test suite for CMAESOptimizer.
 * Targets high code coverage and fault detection (Defects4J Math-19).
 */
public class CMAESOptimizerTest {

    private CMAESOptimizer optimizer;
    private static final double EPS = 1e-10;

    @Before
    public void setUp() {
        optimizer = new CMAESOptimizer();
    }

    // ---------- Basic optimization tests ----------

    @Test
    public void testSphereOptimize() {
        MultivariateFunction sphere = new MultivariateFunction() {
            @Override
            public double value(double[] point) {
                double sum = 0;
                for (double v : point) {
                    sum += v * v;
                }
                return sum;
            }
        };
        double[] start = {1.0, 1.0, 1.0};
        double[] sigma = {0.1, 0.1, 0.1};
        PointValuePair result = optimizer.optimize(1000, sphere, GoalType.MINIMIZE, start, sigma);
        double[] point = result.getPoint();
        for (double v : point) {
            Assert.assertEquals(0.0, v, 1e-2);
        }
        Assert.assertEquals(0.0, result.getValue(), 1e-2);
    }

    @Test
    public void testRosenbrockOptimize() {
        MultivariateFunction rosenbrock = new MultivariateFunction() {
            @Override
            public double value(double[] point) {
                double x = point[0];
                double y = point[1];
                return (1 - x) * (1 - x) + 100 * (y - x * x) * (y - x * x);
            }
        };
        double[] start = {-1.0, 1.0};
        double[] sigma = {0.5, 0.5};
        PointValuePair result = optimizer.optimize(2000, rosenbrock, GoalType.MINIMIZE, start, sigma);
        double[] point = result.getPoint();
        Assert.assertEquals(1.0, point[0], 1e-2);
        Assert.assertEquals(1.0, point[1], 1e-2);
        Assert.assertEquals(0.0, result.getValue(), 1e-2);
    }

    // ---------- Bounds handling tests ----------

    @Test(expected = NullPointerException.class)
    public void testNullLowerBounds() {
        MultivariateFunction f = p -> p[0] * p[0];
        double[] start = {1.0};
        double[] sigma = {0.1};
        optimizer.optimize(100, f, GoalType.MINIMIZE, start, sigma, null, new double[]{1.0});
    }

    @Test(expected = NullPointerException.class)
    public void testNullUpperBounds() {
        MultivariateFunction f = p -> p[0] * p[0];
        double[] start = {1.0};
        double[] sigma = {0.1};
        optimizer.optimize(100, f, GoalType.MINIMIZE, start, sigma, new double[]{-1.0}, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLowerGreaterThanUpper() {
        MultivariateFunction f = p -> p[0] * p[0];
        double[] start = {0.0};
        double[] sigma = {0.1};
        optimizer.optimize(100, f, GoalType.MINIMIZE, start, sigma, new double[]{1.0}, new double[]{-1.0});
    }

    @Test
    public void testBoundsAtOptimum() {
        // Optimum at boundary: minimize x^2 with lower bound 0, upper bound 1
        MultivariateFunction f = p -> p[0] * p[0];
        double[] start = {0.5};
        double[] sigma = {0.2};
        double[] lower = {0.0};
        double[] upper = {1.0};
        PointValuePair result = optimizer.optimize(500, f, GoalType.MINIMIZE, start, sigma, lower, upper);
        double[] point = result.getPoint();
        Assert.assertEquals(0.0, point[0], 1e-2);
        Assert.assertEquals(0.0, result.getValue(), 1e-2);
    }

    @Test
    public void testBoundsTight() {
        // Minimize (x-2)^2 with bounds [1.9, 2.1]
        MultivariateFunction f = p -> (p[0] - 2.0) * (p[0] - 2.0);
        double[] start = {2.0};
        double[] sigma = {0.05};
        double[] lower = {1.9};
        double[] upper = {2.1};
        PointValuePair result = optimizer.optimize(500, f, GoalType.MINIMIZE, start, sigma, lower, upper);
        double[] point = result.getPoint();
        Assert.assertTrue("Point should be within bounds", point[0] >= 1.9 && point[0] <= 2.1);
        Assert.assertEquals(0.0, result.getValue(), 1e-2);
    }

    // ---------- Sigma handling tests ----------

    @Test(expected = NullPointerException.class)
    public void testNullSigma() {
        MultivariateFunction f = p -> p[0] * p[0];
        double[] start = {1.0};
        optimizer.optimize(100, f, GoalType.MINIMIZE, start, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeSigma() {
        MultivariateFunction f = p -> p[0] * p[0];
        double[] start = {1.0};
        double[] sigma = {-0.1};
        optimizer.optimize(100, f, GoalType.MINIMIZE, start, sigma);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testZeroSigma() {
        MultivariateFunction f = p -> p[0] * p[0];
        double[] start = {1.0};
        double[] sigma = {0.0};
        optimizer.optimize(100, f, GoalType.MINIMIZE, start, sigma);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSigmaLengthMismatch() {
        MultivariateFunction f = p -> p[0] * p[0];
        double[] start = {1.0, 2.0};
        double[] sigma = {0.1};
        optimizer.optimize(100, f, GoalType.MINIMIZE, start, sigma);
    }

    // ---------- Population size tests ----------

    @Test(expected = IllegalArgumentException.class)
    public void testNegativePopulationSize() {
        MultivariateFunction f = p -> p[0] * p[0];
        double[] start = {1.0};
        double[] sigma = {0.1};
        optimizer = new CMAESOptimizer(10, 1.0, false, -5, 0, new MersenneTwister(), false, new SimpleValueChecker(1e-6, 1e-6));
        optimizer.optimize(100, f, GoalType.MINIMIZE, start, sigma);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testZeroPopulationSize() {
        MultivariateFunction f = p -> p[0] * p[0];
        double[] start = {1.0};
        double[] sigma = {0.1};
        optimizer = new CMAESOptimizer(10, 1.0, false, 0, 0, new MersenneTwister(), false, new SimpleValueChecker(1e-6, 1e-6));
        optimizer.optimize(100, f, GoalType.MINIMIZE, start, sigma);
    }

    // ---------- Convergence and stopping tests ----------

    @Test
    public void testConvergenceWithTightTolerance() {
        MultivariateFunction f = p -> p[0] * p[0] + p[1] * p[1];
        double[] start = {10.0, -10.0};
        double[] sigma = {1.0, 1.0};
        optimizer = new CMAESOptimizer(100, 1.0, false, 10, 0, new MersenneTwister(), false, new SimpleValueChecker(1e-12, 1e-12));
        PointValuePair result = optimizer.optimize(10000, f, GoalType.MINIMIZE, start, sigma);
        Assert.assertEquals(0.0, result.getValue(), 1e-10);
    }

    @Test
    public void testMaxIterationsReached() {
        MultivariateFunction f = p -> p[0] * p[0];
        double[] start = {100.0};
        double[] sigma = {10.0};
        // Very small max iterations to force early stop
        PointValuePair result = optimizer.optimize(5, f, GoalType.MINIMIZE, start, sigma);
        // Should return some point (not necessarily optimal)
        Assert.assertNotNull(result);
        Assert.assertNotNull(result.getPoint());
    }

    // ---------- Edge cases ----------

    @Test
    public void testDimensionOne() {
        MultivariateFunction f = p -> (p[0] - 3.0) * (p[0] - 3.0);
        double[] start = {0.0};
        double[] sigma = {0.5};
        PointValuePair result = optimizer.optimize(200, f, GoalType.MINIMIZE, start, sigma);
        Assert.assertEquals(3.0, result.getPoint()[0], 1e-2);
        Assert.assertEquals(0.0, result.getValue(), 1e-2);
    }

    @Test
    public void testLargeDimension() {
        int dim = 10;
        MultivariateFunction f = new MultivariateFunction() {
            @Override
            public double value(double[] point) {
                double sum = 0;
                for (int i = 0; i < dim; i++) {
                    sum += (i + 1) * point[i] * point[i];
                }
                return sum;
            }
        };
        double[] start = new double[dim];
        double[] sigma = new double[dim];
        for (int i = 0; i < dim; i++) {
            start[i] = 1.0;
            sigma[i] = 0.2;
        }
        PointValuePair result = optimizer.optimize(2000, f, GoalType.MINIMIZE, start, sigma);
        double[] point = result.getPoint();
        for (int i = 0; i < dim; i++) {
            Assert.assertEquals(0.0, point[i], 1e-1);
        }
        Assert.assertEquals(0.0, result.getValue(), 1e-1);
    }

    @Test
    public void testFunctionWithFlatRegion() {
        // Function that is constant in a region: f(x) = 0 for x in [0,1], else (x-0.5)^2
        MultivariateFunction f = new MultivariateFunction() {
            @Override
            public double value(double[] point) {
                double x = point[0];
                if (x >= 0 && x <= 1) {
                    return 0.0;
                } else {
                    return (x - 0.5) * (x - 0.5);
                }
            }
        };
        double[] start = {2.0};
        double[] sigma = {0.5};
        PointValuePair result = optimizer.optimize(500, f, GoalType.MINIMIZE, start, sigma);
        double x = result.getPoint()[0];
        Assert.assertTrue("Point should be in [0,1]", x >= 0 && x <= 1);
        Assert.assertEquals(0.0, result.getValue(), 1e-2);
    }

    // ---------- Known bug trigger (Defects4J Math-19) ----------

    @Test
    public void testBoundaryOptimumWithSigmaTooLarge() {
        // Bug scenario: when optimum is at boundary and sigma is large relative to bounds,
        // the optimizer may produce NaN or fail.
        MultivariateFunction f = p -> p[0] * p[0];
        double[] start = {0.5};
        double[] sigma = {10.0}; // large sigma
        double[] lower = {0.0};
        double[] upper = {1.0};
        PointValuePair result = optimizer.optimize(500, f, GoalType.MINIMIZE, start, sigma, lower, upper);
        double[] point = result.getPoint();
        Assert.assertFalse("Point should not contain NaN", Double.isNaN(point[0]));
        Assert.assertTrue("Point should be within bounds", point[0] >= 0.0 && point[0] <= 1.0);
        Assert.assertEquals(0.0, result.getValue(), 1e-2);
    }

    @Test
    public void testBoundaryOptimumWithTightBoundsAndSmallSigma() {
        // Another boundary scenario: optimum at lower bound, tight bounds, small sigma
        MultivariateFunction f = p -> (p[0] + 1.0) * (p[0] + 1.0);
        double[] start = {0.0};
        double[] sigma = {0.01};
        double[] lower = {-1.0};
        double[] upper = {0.0};
        PointValuePair result = optimizer.optimize(500, f, GoalType.MINIMIZE, start, sigma, lower, upper);
        double[] point = result.getPoint();
        Assert.assertEquals(-1.0, point[0], 1e-2);
        Assert.assertEquals(0.0, result.getValue(), 1e-2);
    }

    // ---------- Additional coverage: internal methods via public API ----------

    @Test
    public void testMaximizeGoalType() {
        MultivariateFunction f = p -> -(p[0] * p[0] + p[1] * p[1]);
        double[] start = {1.0, 1.0};
        double[] sigma = {0.5, 0.5};
        PointValuePair result = optimizer.optimize(500, f, GoalType.MAXIMIZE, start, sigma);
        double[] point = result.getPoint();
        Assert.assertEquals(0.0, point[0], 1e-2);
        Assert.assertEquals(0.0, point[1], 1e-2);
        Assert.assertEquals(0.0, result.getValue(), 1e-2);
    }

    @Test
    public void testWithCustomRandomGenerator() {
        optimizer = new CMAESOptimizer(10, 1.0, false, 10, 0, new MersenneTwister(12345), false, new SimpleValueChecker(1e-6, 1e-6));
        MultivariateFunction f = p -> p[0] * p[0];
        double[] start = {5.0};
        double[] sigma = {1.0};
        PointValuePair result = optimizer.optimize(200, f, GoalType.MINIMIZE, start, sigma);
        Assert.assertEquals(0.0, result.getValue(), 1e-2);
    }

    @Test
    public void testWithActiveCMA() {
        optimizer = new CMAESOptimizer(10, 1.0, true, 10, 0, new MersenneTwister(), false, new SimpleValueChecker(1e-6, 1e-6));
        MultivariateFunction f = p -> p[0] * p[0] + p[1] * p[1];
        double[] start = {2.0, -2.0};
        double[] sigma = {0.5, 0.5};
        PointValuePair result = optimizer.optimize(500, f, GoalType.MINIMIZE, start, sigma);
        Assert.assertEquals(0.0, result.getValue(), 1e-2);
    }

    @Test
    public void testWithDiagonalOnly() {
        optimizer = new CMAESOptimizer(10, 1.0, false, 10, 0, new MersenneTwister(), true, new SimpleValueChecker(1e-6, 1e-6));
        MultivariateFunction f = p -> p[0] * p[0] + 2 * p[1] * p[1];
        double[] start = {1.0, 1.0};
        double[] sigma = {0.2, 0.2};
        PointValuePair result = optimizer.optimize(500, f, GoalType.MINIMIZE, start, sigma);
        Assert.assertEquals(0.0, result.getValue(), 1e-2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStartPointLengthMismatchWithBounds() {
        MultivariateFunction f = p -> p[0] * p[0];
        double[] start = {1.0, 2.0};
        double[] sigma = {0.1, 0.1};
        double[] lower = {0.0};
        double[] upper = {1.0};
        optimizer.optimize(100, f, GoalType.MINIMIZE, start, sigma, lower, upper);
    }

    @Test
    public void testNoBoundsProvided() {
        MultivariateFunction f = p -> p[0] * p[0];
        double[] start = {10.0};
        double[] sigma = {1.0};
        PointValuePair result = optimizer.optimize(200, f, GoalType.MINIMIZE, start, sigma);
        Assert.assertEquals(0.0, result.getValue(), 1e-2);
    }

    @Test
    public void testSigmaScaling() {
        // Test with different sigma values per dimension
        MultivariateFunction f = p -> p[0] * p[0] + 100 * p[1] * p[1];
        double[] start = {1.0, 1.0};
        double[] sigma = {0.5, 5.0}; // larger sigma for steeper dimension
        PointValuePair result = optimizer.optimize(1000, f, GoalType.MINIMIZE, start, sigma);
        Assert.assertEquals(0.0, result.getValue(), 1e-1);
    }
}