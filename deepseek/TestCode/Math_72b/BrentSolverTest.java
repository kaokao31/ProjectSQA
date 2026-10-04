package org.apache.commons.math.analysis.solvers;

import org.apache.commons.math.MathException;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Comprehensive JUnit 4 test suite for BrentSolver.
 * Targets maximum coverage and fault detection (Defects4J Math-72).
 */
public class BrentSolverTest {

    private BrentSolver solver;
    private static final double EPS = 1e-10;
    private static final double DEFAULT_ABSOLUTE_ACCURACY = 1e-6;

    @Before
    public void setUp() {
        solver = new BrentSolver();
    }

    // ---------- Helper functions ----------
    private static class SinFunction implements UnivariateRealFunction {
        public double value(double x) {
            return Math.sin(x);
        }
    }

    private static class QuadraticFunction implements UnivariateRealFunction {
        public double value(double x) {
            return (x - 2.0) * (x + 1.0); // roots at -1 and 2
        }
    }

    private static class LinearFunction implements UnivariateRealFunction {
        private final double slope;
        private final double intercept;
        LinearFunction(double slope, double intercept) {
            this.slope = slope;
            this.intercept = intercept;
        }
        public double value(double x) {
            return slope * x + intercept;
        }
    }

    private static class ConstantFunction implements UnivariateRealFunction {
        private final double value;
        ConstantFunction(double value) {
            this.value = value;
        }
        public double value(double x) {
            return value;
        }
    }

    // ---------- Basic solve with opposite signs ----------
    @Test
    public void testSolveEndpointsOppositeSigns() throws MathException {
        UnivariateRealFunction f = new SinFunction();
        // sin(x) has root at pi, opposite signs at 2 and 4
        double result = solver.solve(f, 2.0, 4.0);
        assertEquals(Math.PI, result, DEFAULT_ABSOLUTE_ACCURACY);
    }

    @Test
    public void testSolveEndpointsOppositeSignsLinear() throws MathException {
        UnivariateRealFunction f = new LinearFunction(1.0, -3.0); // root at 3
        double result = solver.solve(f, 0.0, 5.0);
        assertEquals(3.0, result, DEFAULT_ABSOLUTE_ACCURACY);
    }

    // ---------- Same sign endpoints should throw ----------
    @Test(expected = IllegalArgumentException.class)
    public void testSolveEndpointsSameSign() throws MathException {
        UnivariateRealFunction f = new QuadraticFunction();
        // f(0)= -2, f(1)= -2? Actually f(0)= -2, f(1)= (1-2)*(1+1)= -2, same sign
        solver.solve(f, 0.0, 1.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSolveEndpointsBothPositive() throws MathException {
        UnivariateRealFunction f = new ConstantFunction(1.0);
        solver.solve(f, -10.0, 10.0);
    }

    // ---------- Function value at one endpoint is zero ----------
    @Test
    public void testSolveMinEndpointIsRoot() throws MathException {
        UnivariateRealFunction f = new LinearFunction(1.0, -2.0); // root at 2
        double result = solver.solve(f, 2.0, 5.0);
        assertEquals(2.0, result, EPS);
    }

    @Test
    public void testSolveMaxEndpointIsRoot() throws MathException {
        UnivariateRealFunction f = new LinearFunction(1.0, -2.0); // root at 2
        double result = solver.solve(f, -1.0, 2.0);
        assertEquals(2.0, result, EPS);
    }

    // ---------- Initial guess provided ----------
    @Test
    public void testSolveWithInitialGuess() throws MathException {
        UnivariateRealFunction f = new SinFunction();
        double result = solver.solve(f, 2.0, 4.0, 3.0);
        assertEquals(Math.PI, result, DEFAULT_ABSOLUTE_ACCURACY);
    }

    // ---------- BUG: initial guess yields zero (Defects4J Math-72) ----------
    @Test
    public void testSolveInitialGuessIsRoot() throws MathException {
        UnivariateRealFunction f = new LinearFunction(1.0, -3.0); // root at 3
        double result = solver.solve(f, 0.0, 5.0, 3.0);
        assertEquals(3.0, result, EPS);
    }

    @Test
    public void testSolveInitialGuessIsRootWithSin() throws MathException {
        UnivariateRealFunction f = new SinFunction();
        double result = solver.solve(f, 2.0, 4.0, Math.PI);
        assertEquals(Math.PI, result, EPS);
    }

    // ---------- No root in interval (should throw) ----------
    @Test(expected = MathException.class)
    public void testSolveNoRootInInterval() throws MathException {
        UnivariateRealFunction f = new ConstantFunction(1.0);
        solver.solve(f, 0.0, 1.0);
    }

    // ---------- Multiple roots (should find one) ----------
    @Test
    public void testSolveMultipleRoots() throws MathException {
        UnivariateRealFunction f = new QuadraticFunction(); // roots at -1 and 2
        double result = solver.solve(f, -2.0, 3.0);
        // Should find either -1 or 2 depending on sign change
        assertTrue(Math.abs(result - (-1.0)) < DEFAULT_ABSOLUTE_ACCURACY ||
                   Math.abs(result - 2.0) < DEFAULT_ABSOLUTE_ACCURACY);
    }

    // ---------- Very small interval ----------
    @Test
    public void testSolveTinyInterval() throws MathException {
        UnivariateRealFunction f = new LinearFunction(1.0, -1.0); // root at 1
        double result = solver.solve(f, 0.9999, 1.0001);
        assertEquals(1.0, result, 1e-4);
    }

    // ---------- Large interval with root near center ----------
    @Test
    public void testSolveLargeInterval() throws MathException {
        UnivariateRealFunction f = new SinFunction();
        double result = solver.solve(f, -1000.0, 1000.0);
        // Should find a root, likely 0 or pi, but due to sign change near 0
        assertTrue(Math.abs(result) < DEFAULT_ABSOLUTE_ACCURACY ||
                   Math.abs(result - Math.PI) < DEFAULT_ABSOLUTE_ACCURACY);
    }

    // ---------- Test with custom tolerance ----------
    @Test
    public void testSolveWithCustomTolerance() throws MathException {
        BrentSolver customSolver = new BrentSolver(1e-12);
        UnivariateRealFunction f = new SinFunction();
        double result = customSolver.solve(f, 2.0, 4.0);
        assertEquals(Math.PI, result, 1e-12);
    }

    // ---------- Edge: function value at initial is zero but endpoints have same sign ----------
    @Test
    public void testSolveInitialRootSameSignEndpoints() throws MathException {
        UnivariateRealFunction f = new QuadraticFunction(); // roots at -1 and 2
        // Interval [0,1] has same sign (both negative), but initial=2 is root
        // The solver should return 2 immediately (bug fix)
        double result = solver.solve(f, 0.0, 1.0, 2.0);
        assertEquals(2.0, result, EPS);
    }

    // ---------- Edge: initial guess outside interval ----------
    @Test(expected = IllegalArgumentException.class)
    public void testSolveInitialOutOfRange() throws MathException {
        UnivariateRealFunction f = new SinFunction();
        solver.solve(f, 2.0, 4.0, 1.0);
    }

    // ---------- Null function ----------
    @Test(expected = IllegalArgumentException.class)
    public void testSolveNullFunction() throws MathException {
        solver.solve(null, 0.0, 1.0);
    }

    // ---------- NaN endpoints ----------
    @Test(expected = IllegalArgumentException.class)
    public void testSolveNaNEndpoints() throws MathException {
        UnivariateRealFunction f = new SinFunction();
        solver.solve(f, Double.NaN, 1.0);
    }

    // ---------- Infinite endpoints ----------
    @Test(expected = IllegalArgumentException.class)
    public void testSolveInfiniteEndpoints() throws MathException {
        UnivariateRealFunction f = new SinFunction();
        solver.solve(f, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
    }

    // ---------- Min > max ----------
    @Test(expected = IllegalArgumentException.class)
    public void testSolveMinGreaterThanMax() throws MathException {
        UnivariateRealFunction f = new SinFunction();
        solver.solve(f, 4.0, 2.0);
    }

    // ---------- Very flat function near root ----------
    @Test
    public void testSolveFlatFunction() throws MathException {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return (x - 1.0) * (x - 1.0) * (x - 1.0); // triple root at 1
            }
        };
        double result = solver.solve(f, 0.0, 2.0);
        assertEquals(1.0, result, 1e-4);
    }

    // ---------- Function with discontinuity (but still sign change) ----------
    @Test
    public void testSolveDiscontinuousFunction() throws MathException {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                if (x < 0) return -1.0;
                else return 1.0;
            }
        };
        double result = solver.solve(f, -1.0, 1.0);
        // Should find root near 0
        assertTrue(result >= -1e-6 && result <= 1e-6);
    }
}