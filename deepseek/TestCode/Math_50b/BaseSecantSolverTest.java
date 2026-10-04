```java
package org.apache.commons.math.analysis.solvers;

import org.apache.commons.math.analysis.QuinticFunction;
import org.apache.commons.math.analysis.SinFunction;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.analysis.XMinus5Function;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class BaseSecantSolverTest {

    private static final double EPSILON = 1e-10;
    private static final double DEFAULT_ABSOLUTE_ACCURACY = 1e-6;
    private BaseSecantSolver solver;

    @Before
    public void setUp() {
        solver = new BaseSecantSolver(DEFAULT_ABSOLUTE_ACCURACY, 
                                      BaseSecantSolver.Method.REGULA_FALSI);
    }

    @Test
    public void testSinFunctionBracketing() {
        UnivariateRealFunction f = new SinFunction();
        double result = solver.solve(100, f, 3.0, 4.0);
        assertEquals(Math.PI, result, 1e-6);
    }

    @Test
    public void testQuinticFunctionZeroBracketing() {
        UnivariateRealFunction f = new QuinticFunction();
        double result = solver.solve(100, f, 0.0, 2.0);
        assertEquals(0.0, result, 1e-6);
    }

    @Test
    public void testLinearFunctionExactZero() {
        UnivariateRealFunction f = new XMinus5Function();
        double result = solver.solve(100, f, 4.0, 6.0);
        assertEquals(5.0, result, 1e-6);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullFunctionThrowsException() {
        solver.solve(100, null, 1.0, 2.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidBracketingInterval() {
        UnivariateRealFunction f = new SinFunction();
        solver.solve(100, f, 1.0, 2.0);
    }

    @Test
    public void testIncreasingFunctionWithPositiveRoot() {
        UnivariateRealFunction f = x -> x - 10;
        double result = solver.solve(100, f, 8.0, 12.0);
        assertEquals(10.0, result, 1e-6);
    }

    @Test
    public void testFunctionWithFlatRegion() {
        UnivariateRealFunction f = x -> {
            if (x > 0 && x < 0.5) return Math.exp(-1000 * (x - 0.25) * (x - 0.25));
            if (x >= 0.5 && x < 1) return Math.exp(-1000 * (x - 0.75) * (x - 0.75));
            return x * (x - 1);
        };
        double result = solver.solve(1000, f, 3.0, 8.0);
        assertTrue(result >= 3.0 && result <= 8.0);
        assertTrue(Math.abs(f.value(result)) < 1e-3);
    }

    @Test
    public void testHighAccuracyRequired() {
        UnivariateRealFunction f = x -> Math.pow(x, 3) - 27;
        BaseSecantSolver highAccuracySolver = new BaseSecantSolver(1e-12, 
                                            BaseSecantSolver.Method.REGULA_FALSI);
        double result = highAccuracySolver.solve(1000, f, 2.0, 4.0);
        assertEquals(3.0, result, 1e-12);
    }

    @Test
    public void testRootAtBoundaryLow() {
        UnivariateRealFunction f = x -> x;
        double result = solver.solve(100, f, 0.0, 1.0);
        assertEquals(0.0, result, 1e-6);
    }

    @Test
    public void testRootAtBoundaryHigh() {
        UnivariateRealFunction f = x -> x - 2;
        double result = solver.solve(100, f, 1.5, 2.0);
        assertEquals(2.0, result, 1e-6);
    }

    @Test
    public void testMultipleEvaluationsExactSolution() {
        UnivariateRealFunction f = x -> Math.sin(x) - 0.5;
        double result = solver.solve(100, f, 0.0, 1.0);
        assertEquals(Math.asin(0.5), result, 1e-6);
    }

    @Test(expected = ArithmeticException.class)
    public void testSolverExceedsMaxEvaluations() {
        UnivariateRealFunction f = x -> {
            // Create a function that converges slowly
            if (x < 0) return -1;
            if (x > 1) return 1;
            return Math.exp(-1000 * (x - 0.5) * (x - 0.5));
        };
        BaseSecantSolver smallMaxSolver = new BaseSecantSolver(1e-6, 
                                             BaseSecantSolver.Method.REGULA_FALSI);
        smallMaxSolver.solve(5, f, -2.0, 2.0);
    }

    @Test
    public void testMultipleRootsChoosesClosest() {
        UnivariateRealFunction f = x -> x * (x - 1) * (x + 1);
        double result = solver.solve(1000, f, -0.5, 0.5);
        assertEquals(0.0, result, 1e-6);
    }

    @Test
    public void testFunctionWithNarrowBracketingInterval() {
        UnivariateRealFunction f = x -> Math.exp(x) - 100;
        double result = solver.solve(1000, f, 4.5, 4.7);
        assertEquals(Math.log(100), result, 1e-6);
    }

    @Test
    public void testNegativeQuadrantRoot() {
        UnivariateRealFunction f = x -> x + 5;
        double result = solver.solve(100, f, -6.0, -4.0);
        assertEquals(-5.0, result, 1e-6);
    }
}

class QuinticFunction implements UnivariateRealFunction {
    @Override
    public double value(double x) {
        return x * x * x * x * x - 1;
    }
}

class SinFunction implements UnivariateRealFunction {
    @Override
    public double value(double x) {
        return Math.sin(x);
    }
}

class XMinus5Function implements UnivariateRealFunction {
    @Override
    public double value(double x) {
        return x - 5;
    }
}