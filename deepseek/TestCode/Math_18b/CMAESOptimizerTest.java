package org.apache.commons.math3.optimization.direct;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.optimization.GoalType;
import org.apache.commons.math3.optimization.PointValuePair;
import org.apache.commons.math3.optimization.InitialGuess;
import org.apache.commons.math3.optimization.SimpleBounds;
import org.apache.commons.math3.optimization.SimpleValueChecker;
import org.apache.commons.math3.random.RandomAdaptor;
import org.apache.commons.math3.random.MersenneTwister;

public class CMAESOptimizerTest {

    private CMAESOptimizer optimizer;
    private static final double EPS = 1e-10;

    @Before
    public void setUp() {
        optimizer = new CMAESOptimizer();
    }

    @Test(expected = NullPointerException.class)
    public void testOptimizeWithNullFunction() {
        optimizer.optimize(100, null, GoalType.MINIMIZE, new double[]{1.0});
    }

    @Test(expected = NullPointerException.class)
    public void testOptimizeWithNullGoalType() {
        optimizer.optimize(100, new MultivariateFunction() {
            @Override
            public double value(double[] point) {
                return point[0] * point[0];
            }
        }, null, new double[]{1.0});
    }

    @Test(expected = NullPointerException.class)
    public void testOptimizeWithNullStartPoint() {
        optimizer.optimize(100, new MultivariateFunction() {
            @Override
            public double value(double[] point) {
                return point[0] * point[0];
            }
        }, GoalType.MINIMIZE, null);
    }

    @Test(expected = org.apache.commons.math3.exception.DimensionMismatchException.class)
    public void testOptimizeWithMismatchedBounds() {
        optimizer.optimize(100, new MultivariateFunction() {
            @Override
            public double value(double[] point) {
                return point[0] * point[0];
            }
        }, GoalType.MINIMIZE, new double[]{1.0},
        new double[]{0.0}, new double[]{1.0, 2.0}); 
    }

    @Test
    public void testOptimizeSimpleQuadraticMinimize() {
        MultivariateFunction f = new MultivariateFunction() {
            @Override
            public double value(double[] point) {
                return point[0] * point[0] + point[1] * point[1];
            }
        };

        PointValuePair result = optimizer.optimize(1000, f, GoalType.MINIMIZE,
                new double[]{10.0, 10.0},
                new double[]{-100.0, -100.0},
                new double[]{100.0, 100.0});

        assertNotNull(result);
        assertEquals(0.0, result.getValue(), 1e-5);
        assertArrayEquals(new double[]{0.0, 0.0}, result.getPoint(), 1e-5);
    }

    @Test
    public void testOptimizeSimpleQuadraticMaximize() {
        MultivariateFunction f = new MultivariateFunction() {
            @Override
            public double value(double[] point) {
                return - (point[0] * point[0] + point[1] * point[1]);
            }
        };

        PointValuePair result = optimizer.optimize(1000, f, GoalType.MAXIMIZE,
                new double[]{10.0, 10.0},
                new double[]{-100.0, -100.0},
                new double[]{100.0, 100.0});

        assertNotNull(result);
        assertEquals(0.0, result.getValue(), 1e-5);
        assertArrayEquals(new double[]{0.0, 0.0}, result.getPoint(), 1e-5);
    }

    @Test
    public void testOptimizeWithBoundariesAtTheLimit() {
        MultivariateFunction f = new MultivariateFunction() {
            @Override
            public double value(double[] point) {
                return point[0] * point[0] + Math.abs(point[1] - 5.0);
            }
        };

        PointValuePair result = optimizer.optimize(1000, f, GoalType.MINIMIZE,
                new double[]{0.0, 5.0},
                new double[]{-100.0, -100.0},
                new double[]{100.0, 100.0});

        assertNotNull(result);
        assertTrue(result.getValue() >= 0.0);
        assertArrayEquals(new double[]{0.0, 5.0}, result.getPoint(), 1.0);
    }

    @Test
    public void testOptimizeWithOneElement() {
        MultivariateFunction f = new MultivariateFunction() {
            @Override
            public double value(double[] point) {
                return Math.pow(point[0] - 3.0, 2);
            }
        };

        PointValuePair result = optimizer.optimize(100, f, GoalType.MINIMIZE,
                new double[]{10.0},
                new double[]{-10.0},
                new double[]{10.0});

        assertNotNull(result);
        assertEquals(0.0, result.getValue(), 1.0);
        assertEquals(3.0, result.getPoint()[0], 1.0);
    }

    @Test
    public void testOptimizeWithTightBounds() {
        MultivariateFunction f = new MultivariateFunction() {
            @Override
            public double value(double[] point) {
                return point[0] * point[0];
            }
        };

        PointValuePair result = optimizer.optimize(100, f, GoalType.MINIMIZE,
                new double[]{0.5},
                new double[]{0.0},
                new double[]{1.0});

        assertNotNull(result);
        assertTrue(result.getValue() >= 0.0);
        assertEquals(0.0, result.getPoint()[0], 1.0);
    }

    @Test
    public void testOptimizeWithInvalidStartPointInsideBounds() {
        MultivariateFunction f = new MultivariateFunction() {
            @Override
            public double value(double[] point) {
                return point[0] * point[0] * point[0];
            }
        };

        PointValuePair result = optimizer.optimize(100, f, GoalType.MINIMIZE,
                new double[]{5.0},
                new double[]{-10.0},
                new double[]{10.0});

        assertNotNull(result);
        assertTrue(result.getPoint()[0] >= -10.0);
        assertTrue(result.getPoint()[0] <= 10.0);
    }

    @Test
    public void testOptimizeWithRosenbrockFunction() {
        MultivariateFunction f = new MultivariateFunction() {
            @Override
            public double value(double[] point) {
                double x = point[0];
                double y = point[1];
                return (1 - x) * (1 - x) + 100 * (y - x * x) * (y - x * x);
            }
        };

        PointValuePair result = optimizer.optimize(1000, f, GoalType.MINIMIZE,
                new double[]{-1.0, 2.0},
                new double[]{-10.0, -10.0},
                new double[]{10.0, 10.0});

        assertNotNull(result);
        assertArrayEquals(new double[]{1.0, 1.0}, result.getPoint(), 1.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOptimizeWithInvalidBoundsOrder() {
        optimizer.optimize(100, new MultivariateFunction() {
            @Override
            public double value(double[] point) {
                return point[0];
            }
        }, GoalType.MINIMIZE, new double[]{0.5},
        new double[]{1.0}, new double[]{0.0});
    }

    @Test
    public void testOptimizeWithNegativeValues() {
        MultivariateFunction f = new MultivariateFunction() {
            @Override
            public double value(double[] point) {
                return point[0] * point[0] + point[1] * point[1] - 10;
            }
        };

        PointValuePair result = optimizer.optimize(1000, f, GoalType.MINIMIZE,
                new double[]{5.0, 5.0},
                new double[]{-10.0, -10.0},
                new double[]{10.0, 10.0});

        assertNotNull(result);
        assertEquals(-10.0, result.getValue(), 1.0);
        assertArrayEquals(new double[]{0.0, 0.0}, result.getPoint(), 1.0);
    }

    @Test
    public void testOptimizeWithCustomChecker() {
        optimizer = new CMAESOptimizer(100, 1e-10, true, 0, 10,
                new RandomAdaptor(new MersenneTwister(1234L)),
                true, new SimpleValueChecker(1e-10, 1e-10));

        MultivariateFunction f = new MultivariateFunction() {
            @Override
            public double value(double[] point) {
                return point[0] * point[0] * point[0] - point[0] * 2;
            }
        };

        PointValuePair result = optimizer.optimize(1000, f, GoalType.MINIMIZE,
                new double[]{0.5},
                new double[]{-10.0},
                new double[]{10.0});

        assertNotNull(result);
    }

    @Test
    public void testOptimizeWithExtremeBounds() {
        MultivariateFunction f = new MultivariateFunction() {
            @Override
            public double value(double[] point) {
                return point[0] * point[0];
            }
        };

        PointValuePair result = optimizer.optimize(1000, f, GoalType.MINIMIZE,
                new double[]{0.0},
                new double[]{-Double.MAX_VALUE / 100},
                new double[]{Double.MAX_VALUE / 100});

        assertNotNull(result);
        assertEquals(0.0, result.getValue(), Double.MAX_VALUE / 10);
    }

    @Test
    public void testOptimizeWithNonZeroInitialGuess() {
        MultivariateFunction f = new MultivariateFunction() {
            @Override
            public double value(double[] point) {
                return (point[0] - 10.0) * (point[0] - 10.0);
            }
        };

        PointValuePair result = optimizer.optimize(1000, f, GoalType.MINIMIZE,
                new double[]{-5.0},
                new double[]{-100.0},
                new double[]{100.0});

        assertNotNull(result);
        assertEquals(0.0, result.getValue(), 1.0);
        assertEquals(10.0, result.getPoint()[0], 1.0);
    }
}