package org.apache.commons.math3.optimization.univariate;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.optimization.GoalType;
import org.apache.commons.math3.optimization.univariate.BrentOptimizer;
import org.apache.commons.math3.optimization.univariate.UnivariateObjectiveFunction;
import org.apache.commons.math3.optimization.univariate.SearchInterval;
import org.apache.commons.math3.optimization.univariate.UnivariatePointValuePair;
import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class BrentOptimizerTest {

    private BrentOptimizer optimizer;
    private static final double ABS_TOL = 1e-10;
    private static final double REL_TOL = 1e-10;
    private static final double DELTA = 1e-6;

    @Before
    public void setUp() {
        optimizer = new BrentOptimizer(ABS_TOL, REL_TOL);
    }

    @Test
    public void testSimpleQuadraticMinimize() {
        UnivariateFunction f = x -> (x - 2.0) * (x - 2.0);
        UnivariateObjectiveFunction obj = new UnivariateObjectiveFunction(f);
        SearchInterval interval = new SearchInterval(0.0, 4.0, 1.0);
        UnivariatePointValuePair result = optimizer.optimize(1000, obj, GoalType.MINIMIZE, interval);
        assertEquals(2.0, result.getPoint(), DELTA);
        assertEquals(0.0, result.getValue(), DELTA);
        assertTrue(optimizer.getEvaluations() <= 1000);
    }

    @Test
    public void testSimpleQuadraticMaximize() {
        UnivariateFunction f = x -> -(x - 2.0) * (x - 2.0) + 5.0;
        UnivariateObjectiveFunction obj = new UnivariateObjectiveFunction(f);
        SearchInterval interval = new SearchInterval(0.0, 4.0, 1.0);
        UnivariatePointValuePair result = optimizer.optimize(1000, obj, GoalType.MAXIMIZE, interval);
        assertEquals(2.0, result.getPoint(), DELTA);
        assertEquals(5.0, result.getValue(), DELTA);
    }

    @Test
    public void testConstantFunction() {
        UnivariateFunction f = x -> 42.0;
        UnivariateObjectiveFunction obj = new UnivariateObjectiveFunction(f);
        SearchInterval interval = new SearchInterval(-10.0, 10.0, 0.0);
        UnivariatePointValuePair result = optimizer.optimize(1000, obj, GoalType.MINIMIZE, interval);
        assertTrue(result.getPoint() >= -10.0 && result.getPoint() <= 10.0);
        assertEquals(42.0, result.getValue(), DELTA);
        // Also test maximize
        result = optimizer.optimize(1000, obj, GoalType.MAXIMIZE, interval);
        assertTrue(result.getPoint() >= -10.0 && result.getPoint() <= 10.0);
        assertEquals(42.0, result.getValue(), DELTA);
    }

    @Test
    public void testMinAtBound() {
        UnivariateFunction f = x -> x * x;
        UnivariateObjectiveFunction obj = new UnivariateObjectiveFunction(f);
        SearchInterval interval = new SearchInterval(0.0, 1.0, 0.5);
        UnivariatePointValuePair result = optimizer.optimize(1000, obj, GoalType.MINIMIZE, interval);
        assertEquals(0.0, result.getPoint(), DELTA);
        assertEquals(0.0, result.getValue(), DELTA);
    }

    @Test
    public void testMaxAtBound() {
        UnivariateFunction f = x -> -x * x + 1.0;
        UnivariateObjectiveFunction obj = new UnivariateObjectiveFunction(f);
        SearchInterval interval = new SearchInterval(-1.0, 0.0, -0.5);
        UnivariatePointValuePair result = optimizer.optimize(1000, obj, GoalType.MAXIMIZE, interval);
        assertEquals(0.0, result.getPoint(), DELTA);
        assertEquals(1.0, result.getValue(), DELTA);
    }

    @Test
    public void testFlatRegion() {
        // Function flat on [0,1], increasing outside
        UnivariateFunction f = x -> {
            if (x < 0.0) return (x + 1.0) * (x + 1.0);
            if (x > 1.0) return (x - 1.0) * (x - 1.0);
            return 0.0;
        };
        UnivariateObjectiveFunction obj = new UnivariateObjectiveFunction(f);
        SearchInterval interval = new SearchInterval(-2.0, 2.0, 0.5);
        UnivariatePointValuePair result = optimizer.optimize(1000, obj, GoalType.MINIMIZE, interval);
        assertTrue(result.getPoint() >= 0.0 && result.getPoint() <= 1.0);
        assertEquals(0.0, result.getValue(), DELTA);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullFunction() {
        optimizer.optimize(1000, null, GoalType.MINIMIZE, new SearchInterval(-1.0, 1.0, 0.0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullGoalType() {
        UnivariateFunction f = x -> x;
        optimizer.optimize(1000, new UnivariateObjectiveFunction(f), null, new SearchInterval(-1.0, 1.0, 0.0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullInterval() {
        UnivariateFunction f = x -> x;
        optimizer.optimize(1000, new UnivariateObjectiveFunction(f), GoalType.MINIMIZE, (SearchInterval) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidBounds() {
        UnivariateFunction f = x -> x;
        optimizer.optimize(1000, new UnivariateObjectiveFunction(f), GoalType.MINIMIZE, new SearchInterval(1.0, -1.0, 0.0));
    }

    @Test(expected = TooManyEvaluationsException.class)
    public void testTooManyEvaluations() {
        UnivariateFunction f = x -> (x - 2.0) * (x - 2.0);
        optimizer.optimize(5, new UnivariateObjectiveFunction(f), GoalType.MINIMIZE, new SearchInterval(0.0, 4.0, 1.0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeAbsoluteTolerance() {
        new BrentOptimizer(-1.0, REL_TOL);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeRelativeTolerance() {
        new BrentOptimizer(ABS_TOL, -1.0);
    }

    @Test
    public void testZeroTolerance() {
        BrentOptimizer zeroTolOptimizer = new BrentOptimizer(0.0, 0.0);
        UnivariateFunction f = x -> (x - 2.0) * (x - 2.0);
        UnivariatePointValuePair result = zeroTolOptimizer.optimize(1000, new UnivariateObjectiveFunction(f), GoalType.MINIMIZE, new SearchInterval(0.0, 4.0, 1.0));
        assertEquals(2.0, result.getPoint(), 1e-12);
        assertEquals(0.0, result.getValue(), 1e-12);
    }

    @Test
    public void testStartValueAtMin() {
        UnivariateFunction f = x -> (x - 3.0) * (x - 3.0);
        UnivariateObjectiveFunction obj = new UnivariateObjectiveFunction(f);
        SearchInterval interval = new SearchInterval(0.0, 5.0, 3.0);
        UnivariatePointValuePair result = optimizer.optimize(1000, obj, GoalType.MINIMIZE, interval);
        assertEquals(3.0, result.getPoint(), DELTA);
        assertEquals(0.0, result.getValue(), DELTA);
    }

    @Test
    public void testStartValueAtBound() {
        UnivariateFunction f = x -> x * x;
        UnivariateObjectiveFunction obj = new UnivariateObjectiveFunction(f);
        SearchInterval interval = new SearchInterval(0.0, 1.0, 0.0);
        UnivariatePointValuePair result = optimizer.optimize(1000, obj, GoalType.MINIMIZE, interval);
        assertEquals(0.0, result.getPoint(), DELTA);
        assertEquals(0.0, result.getValue(), DELTA);
    }

    @Test
    public void testMultipleCalls() {
        UnivariateFunction f1 = x -> (x - 1.0) * (x - 1.0);
        UnivariateFunction f2 = x -> (x + 1.0) * (x + 1.0);
        UnivariatePointValuePair r1 = optimizer.optimize(1000, new UnivariateObjectiveFunction(f1), GoalType.MINIMIZE, new SearchInterval(-2.0, 2.0, 0.0));
        assertEquals(1.0, r1.getPoint(), DELTA);
        assertEquals(0.0, r1.getValue(), DELTA);
        UnivariatePointValuePair r2 = optimizer.optimize(1000, new UnivariateObjectiveFunction(f2), GoalType.MINIMIZE, new SearchInterval(-2.0, 2.0, 0.0));
        assertEquals(-1.0, r2.getPoint(), DELTA);
        assertEquals(0.0, r2.getValue(), DELTA);
    }

    @Test
    public void testGetEvaluations() {
        UnivariateFunction f = x -> (x - 2.0) * (x - 2.0);
        optimizer.optimize(1000, new UnivariateObjectiveFunction(f), GoalType.MINIMIZE, new SearchInterval(0.0, 4.0, 1.0));
        assertTrue(optimizer.getEvaluations() > 0);
        assertTrue(optimizer.getEvaluations() <= 1000);
    }

    @Test
    public void testLinearFunctionMinimize() {
        UnivariateFunction f = x -> x;
        UnivariateObjectiveFunction obj = new UnivariateObjectiveFunction(f);
        SearchInterval interval = new SearchInterval(-1.0, 1.0, 0.0);
        UnivariatePointValuePair result = optimizer.optimize(1000, obj, GoalType.MINIMIZE, interval);
        assertEquals(-1.0, result.getPoint(), DELTA);
        assertEquals(-1.0, result.getValue(), DELTA);
    }

    @Test
    public void testLinearFunctionMaximize() {
        UnivariateFunction f = x -> x;
        UnivariateObjectiveFunction obj = new UnivariateObjectiveFunction(f);
        SearchInterval interval = new SearchInterval(-1.0, 1.0, 0.0);
        UnivariatePointValuePair result = optimizer.optimize(1000, obj, GoalType.MAXIMIZE, interval);
        assertEquals(1.0, result.getPoint(), DELTA);
        assertEquals(1.0, result.getValue(), DELTA);
    }

    @Test
    public void testSymmetricQuadratic() {
        UnivariateFunction f = x -> x * x;
        UnivariateObjectiveFunction obj = new UnivariateObjectiveFunction(f);
        SearchInterval interval = new SearchInterval(-5.0, 5.0, 0.0);
        UnivariatePointValuePair result = optimizer.optimize(1000, obj, GoalType.MINIMIZE, interval);
        assertEquals(0.0, result.getPoint(), DELTA);
        assertEquals(0.0, result.getValue(), DELTA);
    }

    @Test
    public void testFunctionWithZeroDerivativeAtMin() {
        // f(x) = (x-2)^4, minimum at 2, derivative zero
        UnivariateFunction f = x -> Math.pow(x - 2.0, 4);
        UnivariateObjectiveFunction obj = new UnivariateObjectiveFunction(f);
        SearchInterval interval = new SearchInterval(0.0, 4.0, 1.0);
        UnivariatePointValuePair result = optimizer.optimize(1000, obj, GoalType.MINIMIZE, interval);
        assertEquals(2.0, result.getPoint(), 1e-4);
        assertEquals(0.0, result.getValue(), 1e-4);
    }

    @Test
    public void testOptimizeWithMaxEvalAndStartValue() {
        UnivariateFunction f = x -> (x - 3.0) * (x - 3.0);
        UnivariateObjectiveFunction obj = new UnivariateObjectiveFunction(f);
        UnivariatePointValuePair result = optimizer.optimize(500, obj, GoalType.MINIMIZE, 0.0, 5.0, 2.0);
        assertEquals(3.0, result.getPoint(), DELTA);
        assertEquals(0.0, result.getValue(), DELTA);
    }

    @Test
    public void testOptimizeWithoutStartValue() {
        UnivariateFunction f = x -> (x - 3.0) * (x - 3.0);
        UnivariateObjectiveFunction obj = new UnivariateObjectiveFunction(f);
        UnivariatePointValuePair result = optimizer.optimize(500, obj, GoalType.MINIMIZE, 0.0, 5.0);
        assertEquals(3.0, result.getPoint(), DELTA);
        assertEquals(0.0, result.getValue(), DELTA);
    }
}