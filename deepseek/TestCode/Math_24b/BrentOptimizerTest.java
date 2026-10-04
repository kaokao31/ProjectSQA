package org.apache.commons.math3.optimization.univariate;

import org.junit.Test;
import org.junit.Before;
import org.junit.Assert;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.exception.TooManyEvaluationsException;

public class BrentOptimizerTest {

    private BrentOptimizer optimizer;
    private static final double DEFAULT_ABSOLUTE_TOLERANCE = 1e-10;
    private static final double DEFAULT_RELATIVE_TOLERANCE = 1e-10;

    @Before
    public void setUp() {
        optimizer = new BrentOptimizer(DEFAULT_RELATIVE_TOLERANCE, DEFAULT_ABSOLUTE_TOLERANCE);
    }

    @Test
    public void testSinMin() {
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return Math.sin(x);
            }
        };
        double expected = 3 * Math.PI / 2;
        double result = optimizer.optimize(100, f, GOAL_TYPE.MINIMIZE, 0, 2 * Math.PI, expected);
        Assert.assertEquals(-1.0, f.value(result), 1e-5);
        Assert.assertEquals(expected, result, 1e-5);
    }

    @Test
    public void testQuadratic() {
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return (x - 2) * (x - 2);
            }
        };
        double result = optimizer.optimize(100, f, GOAL_TYPE.MINIMIZE, -10, 10, 0);
        Assert.assertEquals(2.0, result, 1e-5);
        Assert.assertEquals(0.0, f.value(result), 1e-10);
    }

    @Test
    public void testConstantFunction() {
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return 5.0;
            }
        };
        double start = 3.0;
        double result = optimizer.optimize(100, f, GOAL_TYPE.MINIMIZE, -10, 10, start);
        // Should return something close to start (within tolerance)
        Assert.assertEquals(start, result, 1e-5);
        Assert.assertEquals(5.0, f.value(result), 1e-10);
    }

    @Test
    public void testLinearDecreasing() {
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return -x;
            }
        };
        double lower = -5.0;
        double upper = 5.0;
        double result = optimizer.optimize(100, f, GOAL_TYPE.MINIMIZE, lower, upper, 0);
        // Minimum of -x on [-5,5] is at upper bound (5)
        Assert.assertEquals(upper, result, 1e-5);
        Assert.assertEquals(-upper, f.value(result), 1e-10);
    }

    @Test
    public void testLinearIncreasing() {
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return x;
            }
        };
        double lower = -5.0;
        double upper = 5.0;
        double result = optimizer.optimize(100, f, GOAL_TYPE.MINIMIZE, lower, upper, 0);
        // Minimum of x on [-5,5] is at lower bound (-5)
        Assert.assertEquals(lower, result, 1e-5);
        Assert.assertEquals(lower, f.value(result), 1e-10);
    }

    @Test(expected = TooManyEvaluationsException.class)
    public void testTooManyEvaluations() {
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return Math.sin(x);
            }
        };
        // Very tight tolerance and low max evaluations
        BrentOptimizer tightOptimizer = new BrentOptimizer(1e-20, 1e-20);
        tightOptimizer.optimize(5, f, GOAL_TYPE.MINIMIZE, 0, Math.PI, 1.0);
    }

    @Test(expected = NullPointerException.class)
    public void testNullFunction() {
        optimizer.optimize(100, null, GOAL_TYPE.MINIMIZE, -10, 10, 0);
    }

    @Test
    public void testStartAtBoundary() {
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return (x - 3) * (x - 3);
            }
        };
        double lower = 0.0;
        double upper = 10.0;
        // Start at lower bound
        double result = optimizer.optimize(100, f, GOAL_TYPE.MINIMIZE, lower, upper, lower);
        Assert.assertEquals(3.0, result, 1e-5);
        // Start at upper bound
        result = optimizer.optimize(100, f, GOAL_TYPE.MINIMIZE, lower, upper, upper);
        Assert.assertEquals(3.0, result, 1e-5);
    }

    @Test
    public void testFlatRegion() {
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                if (x < 0) return -x;
                if (x > 1) return x - 1;
                return 0; // flat on [0,1]
            }
        };
        double result = optimizer.optimize(100, f, GOAL_TYPE.MINIMIZE, -10, 10, 0.5);
        // Should return a point in [0,1] where function is 0
        Assert.assertTrue(result >= 0.0 && result <= 1.0);
        Assert.assertEquals(0.0, f.value(result), 1e-10);
    }

    @Test
    public void testInitialPointIsMinimum() {
        // Bug 24 scenario: function value at initial point is the global minimum
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return (x - 1) * (x - 1);
            }
        };
        double start = 1.0;
        double result = optimizer.optimize(100, f, GOAL_TYPE.MINIMIZE, -10, 10, start);
        Assert.assertEquals(1.0, result, 1e-10);
        Assert.assertEquals(0.0, f.value(result), 1e-10);
    }

    @Test
    public void testEvaluationsCount() {
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return (x - 2) * (x - 2);
            }
        };
        optimizer.optimize(100, f, GOAL_TYPE.MINIMIZE, -10, 10, 0);
        int evalCount = optimizer.getEvaluations();
        Assert.assertTrue("Evaluations should be positive", evalCount > 0);
        Assert.assertTrue("Evaluations should not exceed max", evalCount <= 100);
    }

    @Test
    public void testMultipleMinima() {
        // Function with two minima: sin(x) on [0, 4*pi]
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return Math.sin(x);
            }
        };
        double result = optimizer.optimize(100, f, GOAL_TYPE.MINIMIZE, 0, 4 * Math.PI, 2 * Math.PI);
        // Should converge to 3*pi/2 (first minimum) or 7*pi/2 (second minimum)
        // Since start is 2*pi, likely 3*pi/2
        Assert.assertTrue("Result should be near a minimum", 
            Math.abs(result - 3 * Math.PI / 2) < 1e-5 || Math.abs(result - 7 * Math.PI / 2) < 1e-5);
        Assert.assertEquals(-1.0, f.value(result), 1e-5);
    }

    @Test
    public void testNegativeTolerances() {
        // Tolerances should be non-negative; BrentOptimizer may accept negative but treat as zero?
        // We test that it still works
        BrentOptimizer negOptimizer = new BrentOptimizer(-1e-5, -1e-5);
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return (x - 2) * (x - 2);
            }
        };
        double result = negOptimizer.optimize(100, f, GOAL_TYPE.MINIMIZE, -10, 10, 0);
        Assert.assertEquals(2.0, result, 1e-5);
    }
}