package org.apache.commons.math3.optimization.direct;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.NumberIsTooLargeException;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.optimization.ConvergenceChecker;
import org.apache.commons.math3.optimization.GoalType;
import org.apache.commons.math3.optimization.PointValuePair;
import org.apache.commons.math3.optimization.SimpleValueChecker;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CMAESOptimizerTest {

    private static final double DEFAULT_TOLERANCE = 1e-8;
    private static final int DIM = 2;
    private MultivariateFunction sphere;
    private ConvergenceChecker<PointValuePair> checker;

    @Before
    public void setUp() {
        sphere = new MultivariateFunction() {
            @Override
            public double value(double[] point) {
                double sum = 0;
                for (double v : point) {
                    sum += v * v;
                }
                return sum;
            }
        };
        checker = new SimpleValueChecker(1e-12, 1e-12);
    }

    private CMAESOptimizer createDefaultOptimizer() {
        RandomGenerator random = new MersenneTwister(12345);
        return new CMAESOptimizer(10, new double[]{0.5, 0.5}, 10000, 1e-12, true, 0, 0, false, random, false);
    }

    @Test
    public void testOptimizeSimple() {
        CMAESOptimizer optimizer = createDefaultOptimizer();
        PointValuePair result = optimizer.optimize(10000, sphere, GoalType.MINIMIZE, new double[]{1.0, 1.0});
        double[] point = result.getPoint();
        Assert.assertEquals(0.0, point[0], 0.1);
        Assert.assertEquals(0.0, point[1], 0.1);
        Assert.assertTrue(result.getValue() < 1e-6);
    }

    @Test
    public void testOptimizeWithBounds() {
        CMAESOptimizer optimizer = createDefaultOptimizer();
        double[] lower = {-5.0, -5.0};
        double[] upper = {5.0, 5.0};
        PointValuePair result = optimizer.optimize(10000, sphere, GoalType.MINIMIZE, new double[]{1.0, 1.0}, lower, upper);
        double[] point = result.getPoint();
        Assert.assertTrue(point[0] >= lower[0] && point[0] <= upper[0]);
        Assert.assertTrue(point[1] >= lower[1] && point[1] <= upper[1]);
        Assert.assertEquals(0.0, point[0], 0.1);
        Assert.assertEquals(0.0, point[1], 0.1);
    }

    @Test
    public void testOptimizeWithInitialGuess() {
        CMAESOptimizer optimizer = createDefaultOptimizer();
        double[] startPoint = {2.0, -3.0};
        PointValuePair result = optimizer.optimize(10000, sphere, GoalType.MINIMIZE, startPoint);
        double[] point = result.getPoint();
        Assert.assertEquals(0.0, point[0], 0.1);
        Assert.assertEquals(0.0, point[1], 0.1);
    }

    @Test
    public void testOptimizeWithCustomSigma() {
        RandomGenerator random = new MersenneTwister(12345);
        double[] sigma = {0.1, 0.2};
        CMAESOptimizer optimizer = new CMAESOptimizer(10, sigma, 10000, 1e-12, true, 0, 0, false, random, false);
        PointValuePair result = optimizer.optimize(10000, sphere, GoalType.MINIMIZE, new double[]{1.0, 1.0});
        double[] point = result.getPoint();
        Assert.assertEquals(0.0, point[0], 0.1);
        Assert.assertEquals(0.0, point[1], 0.1);
    }

    @Test
    public void testOptimizeWithLambda() {
        RandomGenerator random = new MersenneTwister(12345);
        CMAESOptimizer optimizer = new CMAESOptimizer(20, new double[]{0.5, 0.5}, 10000, 1e-12, true, 0, 0, false, random, false);
        PointValuePair result = optimizer.optimize(10000, sphere, GoalType.MINIMIZE, new double[]{1.0, 1.0});
        double[] point = result.getPoint();
        Assert.assertEquals(0.0, point[0], 0.1);
        Assert.assertEquals(0.0, point[1], 0.1);
    }

    @Test
    public void testOptimizeWithActiveFalse() {
        RandomGenerator random = new MersenneTwister(12345);
        CMAESOptimizer optimizer = new CMAESOptimizer(10, new double[]{0.5, 0.5}, 10000, 1e-12, false, 0, 0, false, random, false);
        PointValuePair result = optimizer.optimize(10000, sphere, GoalType.MINIMIZE, new double[]{1.0, 1.0});
        double[] point = result.getPoint();
        Assert.assertEquals(0.0, point[0], 0.1);
        Assert.assertEquals(0.0, point[1], 0.1);
    }

    @Test
    public void testOptimizeWithDiagonalOnly() {
        RandomGenerator random = new MersenneTwister(12345);
        CMAESOptimizer optimizer = new CMAESOptimizer(10, new double[]{0.5, 0.5}, 10000, 1e-12, true, 1, 0, false, random, false);
        PointValuePair result = optimizer.optimize(10000, sphere, GoalType.MINIMIZE, new double[]{1.0, 1.0});
        double[] point = result.getPoint();
        Assert.assertEquals(0.0, point[0], 0.1);
        Assert.assertEquals(0.0, point[1], 0.1);
    }

    @Test
    public void testOptimizeWithFeasableCheck() {
        RandomGenerator random = new MersenneTwister(12345);
        CMAESOptimizer optimizer = new CMAESOptimizer(10, new double[]{0.5, 0.5}, 10000, 1e-12, true, 0, 1, false, random, false);
        PointValuePair result = optimizer.optimize(10000, sphere, GoalType.MINIMIZE, new double[]{1.0, 1.0});
        double[] point = result.getPoint();
        Assert.assertEquals(0.0, point[0], 0.1);
        Assert.assertEquals(0.0, point[1], 0.1);
    }

    @Test
    public void testOptimizeWithStopOnWarnings() {
        RandomGenerator random = new MersenneTwister(12345);
        CMAESOptimizer optimizer = new CMAESOptimizer(10, new double[]{0.5, 0.5}, 10000, 1e-12, true, 0, 0, true, random, false);
        PointValuePair result = optimizer.optimize(10000, sphere, GoalType.MINIMIZE, new double[]{1.0, 1.0});
        double[] point = result.getPoint();
        Assert.assertEquals(0.0, point[0], 0.1);
        Assert.assertEquals(0.0, point[1], 0.1);
    }

    @Test
    public void testOptimizeWithStatistics() {
        RandomGenerator random = new MersenneTwister(12345);
        CMAESOptimizer optimizer = new CMAESOptimizer(10, new double[]{0.5, 0.5}, 10000, 1e-12, true, 0, 0, false, random, true);
        PointValuePair result = optimizer.optimize(10000, sphere, GoalType.MINIMIZE, new double[]{1.0, 1.0});
        double[] point = result.getPoint();
        Assert.assertEquals(0.0, point[0], 0.1);
        Assert.assertEquals(0.0, point[1], 0.1);
    }

    @Test(expected = NullPointerException.class)
    public void testOptimizeWithNullFunction() {
        CMAESOptimizer optimizer = createDefaultOptimizer();
        optimizer.optimize(10000, null, GoalType.MINIMIZE, new double[]{1.0, 1.0});
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testOptimizeWithInvalidSigma() {
        RandomGenerator random = new MersenneTwister(12345);
        double[] sigma = {-0.1, 0.2};
        new CMAESOptimizer(10, sigma, 10000, 1e-12, true, 0, 0, false, random, false);
    }

    @Test(expected = NotStrictlyPositiveException.class)
    public void testOptimizeWithInvalidLambda() {
        RandomGenerator random = new MersenneTwister(12345);
        new CMAESOptimizer(0, new double[]{0.5, 0.5}, 10000, 1e-12, true, 0, 0, false, random, false);
    }

    @Test
    public void testOptimizeWithLargeDimension() {
        int dim = 10;
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
        RandomGenerator random = new MersenneTwister(12345);
        double[] sigma = new double[dim];
        double[] start = new double[dim];
        for (int i = 0; i < dim; i++) {
            sigma[i] = 0.5;
            start[i] = 1.0;
        }
        CMAESOptimizer optimizer = new CMAESOptimizer(4 + (int) (3 * Math.log(dim)), sigma, 100000, 1e-12, true, 0, 0, false, random, false);
        PointValuePair result = optimizer.optimize(100000, sphere, GoalType.MINIMIZE, start);
        double[] point = result.getPoint();
        for (int i = 0; i < dim; i++) {
            Assert.assertEquals(0.0, point[i], 0.5);
        }
        Assert.assertTrue(result.getValue() < 1e-4);
    }

    @Test
    public void testOptimizeWithBoundaryOptimum() {
        // Function with minimum at boundary: f(x) = (x-5)^2, bounds [0,5]
        MultivariateFunction boundaryFunc = new MultivariateFunction() {
            @Override
            public double value(double[] point) {
                return (point[0] - 5.0) * (point[0] - 5.0);
            }
        };
        RandomGenerator random = new MersenneTwister(12345);
        CMAESOptimizer optimizer = new CMAESOptimizer(10, new double[]{1.0}, 10000, 1e-12, true, 0, 0, false, random, false);
        double[] lower = {0.0};
        double[] upper = {5.0};
        PointValuePair result = optimizer.optimize(10000, boundaryFunc, GoalType.MINIMIZE, new double[]{2.0}, lower, upper);
        double[] point = result.getPoint();
        Assert.assertEquals(5.0, point[0], 0.1);
        Assert.assertTrue(result.getValue() < 1e-6);
    }

    @Test
    public void testOptimizeWithNaNInFunction() {
        MultivariateFunction nanFunc = new MultivariateFunction() {
            @Override
            public double value(double[] point) {
                if (point[0] > 0.5) {
                    return Double.NaN;
                }
                return point[0] * point[0];
            }
        };
        RandomGenerator random = new MersenneTwister(12345);
        CMAESOptimizer optimizer = new CMAESOptimizer(10, new double[]{0.5}, 10000, 1e-12, true, 0, 0, false, random, false);
        // Should not throw, but may produce suboptimal result
        PointValuePair result = optimizer.optimize(10000, nanFunc, GoalType.MINIMIZE, new double[]{0.0});
        Assert.assertNotNull(result);
    }

    @Test
    public void testOptimizeWithInfiniteBounds() {
        double[] lower = {Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY};
        double[] upper = {Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY};
        CMAESOptimizer optimizer = createDefaultOptimizer();
        PointValuePair result = optimizer.optimize(10000, sphere, GoalType.MINIMIZE, new double[]{1.0, 1.0}, lower, upper);
        double[] point = result.getPoint();
        Assert.assertEquals(0.0, point[0], 0.1);
        Assert.assertEquals(0.0, point[1], 0.1);
    }

    @Test
    public void testOptimizeWithZeroRangeBounds() {
        double[] lower = {0.0, 0.0};
        double[] upper = {0.0, 0.0};
        CMAESOptimizer optimizer = createDefaultOptimizer();
        PointValuePair result = optimizer.optimize(10000, sphere, GoalType.MINIMIZE, new double[]{0.0, 0.0}, lower, upper);
        double[] point = result.getPoint();
        Assert.assertArrayEquals(new double[]{0.0, 0.0}, point, 1e-12);
    }

    @Test(expected = OutOfRangeException.class)
    public void testOptimizeWithInitialPointOutsideBounds() {
        double[] lower = {0.0, 0.0};
        double[] upper = {1.0, 1.0};
        CMAESOptimizer optimizer = createDefaultOptimizer();
        optimizer.optimize(10000, sphere, GoalType.MINIMIZE, new double[]{2.0, 2.0}, lower, upper);
    }

    @Test
    public void testOptimizeWithNullBounds() {
        CMAESOptimizer optimizer = createDefaultOptimizer();
        PointValuePair result = optimizer.optimize(10000, sphere, GoalType.MINIMIZE, new double[]{1.0, 1.0}, null, null);
        double[] point = result.getPoint();
        Assert.assertEquals(0.0, point[0], 0.1);
        Assert.assertEquals(0.0, point[1], 0.1);
    }

    @Test
    public void testOptimizeRespectsMaxEvaluations() {
        CMAESOptimizer optimizer = createDefaultOptimizer();
        int maxEval = 100;
        PointValuePair result = optimizer.optimize(maxEval, sphere, GoalType.MINIMIZE, new double[]{1.0, 1.0});
        Assert.assertTrue(optimizer.getEvaluations() <= maxEval);
    }

    @Test
    public void testOptimizeWithConvergenceChecker() {
        ConvergenceChecker<PointValuePair> tightChecker = new SimpleValueChecker(1e-15, 1e-15);
        CMAESOptimizer optimizer = createDefaultOptimizer();
        optimizer.setConvergenceChecker(tightChecker);
        PointValuePair result = optimizer.optimize(10000, sphere, GoalType.MINIMIZE, new double[]{1.0, 1.0});
        double[] point = result.getPoint();
        Assert.assertEquals(0.0, point[0], 0.1);
        Assert.assertEquals(0.0, point[1], 0.1);
    }

    @Test(expected = DimensionMismatchException.class)
    public void testOptimizeWithMismatchedSigma() {
        RandomGenerator random = new MersenneTwister(12345);
        double[] sigma = {0.5}; // dimension 1, but start point dimension 2
        new CMAESOptimizer(10, sigma, 10000, 1e-12, true, 0, 0, false, random, false);
    }

    @Test(expected = NumberIsTooLargeException.class)
    public void testOptimizeWithTooLargeSigma() {
        RandomGenerator random = new MersenneTwister(12345);
        double[] sigma = {10.0, 10.0}; // sigma larger than typical bounds
        new CMAESOptimizer(10, sigma, 10000, 1e-12, true, 0, 0, false, random, false);
    }

    @Test(expected = NumberIsTooSmallException.class)
    public void testOptimizeWithTooSmallSigma() {
        RandomGenerator random = new MersenneTwister(12345);
        double[] sigma = {1e-20, 1e-20};
        new CMAESOptimizer(10, sigma, 10000, 1e-12, true, 0, 0, false, random, false);
    }
}