package org.apache.commons.math3.optimization.univariate;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.exception.NumberIsTooLargeException;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.optimization.GoalType;
import org.junit.Assert;
import org.junit.Test;

public class BrentOptimizerTest {

    @Test
    public void testConstructorValidParameters() {
        BrentOptimizer optimizer1 = new BrentOptimizer(1e-8, 1e-9);
        Assert.assertNotNull(optimizer1);

        BrentOptimizer optimizer2 = new BrentOptimizer(1e-8, 1e-9, 1e-11);
        Assert.assertNotNull(optimizer2);
    }

    @Test(expected = NotStrictlyPositiveException.class)
    public void testConstructorInvalidRel() {
        new BrentOptimizer(-1.0, 1e-9);
    }

    @Test(expected = NotStrictlyPositiveException.class)
    public void testConstructorInvalidAbs() {
        new BrentOptimizer(1e-8, 0.0);
    }

    @Test(expected = NotStrictlyPositiveException.class)
    public void testConstructorInvalidAbsolute() {
        new BrentOptimizer(1e-8, 1e-9, 0.0);
    }

    @Test
    public void testOptimizeMinimization() {
        BrentOptimizer optimizer = new BrentOptimizer(1e-11, 1e-14);
        // f(x) = (x - 2)^2 + 1, minimum at x = 2
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return (x - 2.0) * (x - 2.0) + 1.0;
            }
        };

        UnivariatePointValuePair result = optimizer.optimize(100, f, GoalType.MINIMIZE, 0.0, 4.0);
        Assert.assertEquals(2.0, result.getPoint(), 1e-6);
        Assert.assertEquals(1.0, result.getValue(), 1e-6);
    }

    @Test
    public void testOptimizeMaximization() {
        BrentOptimizer optimizer = new BrentOptimizer(1e-11, 1e-14);
        // f(x) = -((x - 2)^2) + 1, maximum at x = 2
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return -((x - 2.0) * (x - 2.0)) + 1.0;
            }
        };

        UnivariatePointValuePair result = optimizer.optimize(100, f, GoalType.MAXIMIZE, 0.0, 4.0);
        Assert.assertEquals(2.0, result.getPoint(), 1e-6);
        Assert.assertEquals(1.0, result.getValue(), 1e-6);
    }

    @Test
    public void testOptimizeWithStartValueMinimization() {
        BrentOptimizer optimizer = new BrentOptimizer(1e-11, 1e-14);
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return Math.sin(x);
            }
        };

        // Minimum of sin(x) in [3, 5] is around 4.712389 (3*pi/2)
        UnivariatePointValuePair result = optimizer.optimize(200, f, GoalType.MINIMIZE, 3.0, 5.0, 4.0);
        Assert.assertNotNull(result);
        Assert.assertEquals(3.0 * Math.PI / 2.0, result.getPoint(), 1e-4);
    }

    @Test
    public void testOptimizeWithStartValueMaximization() {
        BrentOptimizer optimizer = new BrentOptimizer(1e-11, 1e-14);
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return Math.sin(x);
            }
        };

        // Maximum of sin(x) in [0, 3] is around pi/2
        UnivariatePointValuePair result = optimizer.optimize(200, f, GoalType.MAXIMIZE, 0.0, 3.0, 1.5);
        Assert.assertNotNull(result);
        Assert.assertEquals(Math.PI / 2.0, result.getPoint(), 1e-4);
    }

    @Test
    public void testFunctionEvaluationAtBounds() {
        BrentOptimizer optimizer = new BrentOptimizer(1e-8, 1e-9);
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return x * x;
            }
        };

        // Minimum at boundary or tight interval
        UnivariatePointValuePair result = optimizer.optimize(50, f, GoalType.MINIMIZE, -0.1, 0.1, 0.0);
        Assert.assertEquals(0.0, result.getPoint(), 1e-5);
    }

    @Test
    public void testFlatFunction() {
        BrentOptimizer optimizer = new BrentOptimizer(1e-8, 1e-9);
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return 1.0;
            }
        };

        UnivariatePointValuePair result = optimizer.optimize(50, f, GoalType.MINIMIZE, 0.0, 1.0, 0.5);
        Assert.assertNotNull(result);
    }

    @Test
    public void testCubicFunctionSearch() {
        BrentOptimizer optimizer = new BrentOptimizer(1e-10, 1e-12);
        // f(x) = x^3 - 2x - 5
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return x * x * x - 2.0 * x - 5.0;
            }
        };

        UnivariatePointValuePair result = optimizer.optimize(100, f, GoalType.MINIMIZE, 1.0, 3.0);
        Assert.assertNotNull(result);
    }

    @Test
    public void testAtypicalIntervalOrderOrValues() {
        BrentOptimizer optimizer = new BrentOptimizer(1e-8, 1e-9);
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return Math.abs(x - 1.5);
            }
        };

        UnivariatePointValuePair result = optimizer.optimize(100, f, GoalType.MINIMIZE, 0.0, 3.0, 1.0);
        Assert.assertEquals(1.5, result.getPoint(), 1e-4);
    }
}