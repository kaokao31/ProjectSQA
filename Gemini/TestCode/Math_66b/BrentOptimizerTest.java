package org.apache.commons.math.optimization.univariate;

import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.ConvergenceException;
import org.apache.commons.math.FunctionEvaluationException;
import org.junit.Test;
import static org.junit.Assert.*;

public class BrentOptimizerTest {

    @Test
    public void testMinimizeParabolicFunction() throws Exception {
        BrentOptimizer optimizer = new BrentOptimizer();
        
        // f(x) = (x - 2)^2 + 1, minimum at x = 2
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) throws FunctionEvaluationException {
                return (x - 2.0) * (x - 2.0) + 1.0;
            }
        };

        double min = optimizer.optimize(f, GoalType.MINIMIZE, 0.0, 4.0);
        assertEquals(2.0, min, 1e-7);
        assertTrue(optimizer.getEvaluations() > 0);
        assertTrue(optimizer.getIterationCount() >= 0);
    }

    @Test
    public void testMaximizeParabolicFunction() throws Exception {
        BrentOptimizer optimizer = new BrentOptimizer();
        
        // f(x) = -(x - 2)^2 + 10, maximum at x = 2
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) throws FunctionEvaluationException {
                return -((x - 2.0) * (x - 2.0)) + 10.0;
            }
        };

        double max = optimizer.optimize(f, GoalType.MAXIMIZE, 0.0, 4.0);
        assertEquals(2.0, max, 1e-7);
    }

    @Test
    public void testOptimizeWithInitialValue() throws Exception {
        BrentOptimizer optimizer = new BrentOptimizer();
        
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) throws FunctionEvaluationException {
                return Math.sin(x);
            }
        };

        // Minimum of sin(x) on [3, 5] is around 3.14159... (pi)
        double min = optimizer.optimize(f, GoalType.MINIMIZE, 3.0, 5.0, 3.5);
        assertEquals(Math.PI * 1.5, min, 1e-5);
    }

    @Test
    public void testAbsoluteAccuracyConfiguration() {
        BrentOptimizer optimizer = new BrentOptimizer();
        optimizer.setAbsoluteAccuracy(1e-11);
        assertEquals(1e-11, optimizer.getAbsoluteAccuracy(), 1e-14);
    }

    @Test
    public void testRelativeAccuracyConfiguration() {
        BrentOptimizer optimizer = new BrentOptimizer();
        optimizer.setRelativeAccuracy(1e-9);
        assertEquals(1e-9, optimizer.getRelativeAccuracy(), 1e-14);
    }

    @Test(expected = ConvergenceException.class)
    public void testMaxEvaluationsExceeded() throws Exception {
        BrentOptimizer optimizer = new BrentOptimizer();
        optimizer.setMaxEvaluations(2);

        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) throws FunctionEvaluationException {
                return Math.exp(x);
            }
        };

        optimizer.optimize(f, GoalType.MINIMIZE, -10.0, 10.0);
    }

    @Test
    public void testFlatFunction() throws Exception {
        BrentOptimizer optimizer = new BrentOptimizer();
        
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) throws FunctionEvaluationException {
                return 5.0;
            }
        };

        double min = optimizer.optimize(f, GoalType.MINIMIZE, 0.0, 1.0);
        assertTrue(min >= 0.0 && min <= 1.0);
    }
}