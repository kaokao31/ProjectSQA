package org.apache.commons.math3.optimization.univariate;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.optimization.GoalType;
import org.apache.commons.math3.optimization.ConvergenceChecker;
import org.junit.Test;
import org.junit.Assert;

public class BrentOptimizerTest {

    @Test
    public void testConstructorValidations() {
        // Test standard constructors and parameter validations
        try {
            new BrentOptimizer(-1.0, 1e-10);
            Assert.fail("Expected NotStrictlyPositiveException for negative relative tolerance");
        } catch (Exception e) {
            // expected
        }

        try {
            new BrentOptimizer(1e-10, -1.0);
            Assert.fail("Expected NotStrictlyPositiveException for negative absolute tolerance");
        } catch (Exception e) {
            // expected
        }

        try {
            new BrentOptimizer(1e-10, 0.0);
            Assert.fail("Expected NotStrictlyPositiveException for zero absolute tolerance");
        } catch (Exception e) {
            // expected
        }

        try {
            new BrentOptimizer(-1.0, 1e-10, 1e-15);
            Assert.fail("Expected NotStrictlyPositiveException");
        } catch (Exception e) {
            // expected
        }

        BrentOptimizer opt1 = new BrentOptimizer(1e-8, 1e-9);
        Assert.assertNotNull(opt1);

        BrentOptimizer opt2 = new BrentOptimizer(1e-8, 1e-9, 1e-10);
        Assert.assertNotNull(opt2);
    }

    @Test
    public void testOptimizeMinimization() {
        // f(x) = x^2, minimum at x = 0
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return x * x;
            }
        };

        BrentOptimizer optimizer = new BrentOptimizer(1e-11, 1e-14);
        UnivariatePointValuePair result = optimizer.optimize(200, f, GoalType.MINIMIZE, -1.0, 2.0);
        
        Assert.assertEquals(0.0, result.getPoint(), 1e-6);
        Assert.assertEquals(0.0, result.getValue(), 1e-6);
    }

    @Test
    public void testOptimizeMaximization() {
        // f(x) = - (x - 1)^2 + 5, maximum at x = 1
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return -Math.pow(x - 1.0, 2.0) + 5.0;
            }
        };

        BrentOptimizer optimizer = new BrentOptimizer(1e-11, 1e-14);
        UnivariatePointValuePair result = optimizer.optimize(200, f, GoalType.MAXIMIZE, 0.0, 2.0);
        
        Assert.assertEquals(1.0, result.getPoint(), 1e-5);
        Assert.assertEquals(5.0, result.getValue(), 1e-5);
    }

    @Test
    public void testFlatFunctionOrBoundaryOptima() {
        // Test where minimum or maximum might be hit or evaluated with specific branches
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return Math.sin(x);
            }
        };

        BrentOptimizer optimizer = new BrentOptimizer(1e-8, 1e-10);
        UnivariatePointValuePair result = optimizer.optimize(100, f, GoalType.MINIMIZE, 3.0, 4.0);
        Assert.assertTrue(result.getPoint() >= 3.0 && result.getPoint() <= 4.0);
    }

    @Test
    public void testSmallInterval() {
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return x * x - 4 * x + 4;
            }
        };

        BrentOptimizer optimizer = new BrentOptimizer(1e-8, 1e-10);
        UnivariatePointValuePair result = optimizer.optimize(50, f, GoalType.MINIMIZE, 1.999, 2.001);
        Assert.assertEquals(2.0, result.getPoint(), 1e-3);
    }

    @Test
    public void testCubicFunction() {
        // f(x) = x3 - 2x - 5
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return x * x * x - 2 * x - 5;
            }
        };

        BrentOptimizer optimizer = new BrentOptimizer(1e-9, 1e-11);
        UnivariatePointValuePair result = optimizer.optimize(100, f, GoalType.MINIMIZE, 0.0, 2.0);
        Assert.assertNotNull(result);
    }
}