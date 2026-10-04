package org.apache.commons.math.analysis.solvers;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.analysis.QuinticFunction;
import org.apache.commons.math.analysis.SinFunction;
import org.apache.commons.math.analysis.Expm1Function;
import org.apache.commons.math.analysis.MonitoredFunction;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test suite for BrentSolver, targeting high coverage and fault detection.
 * Based on Defects4J bug 73.
 */
public class BrentSolverTest {

    private BrentSolver solver;
    private static final double EPS = 1e-10;

    @Before
    public void setUp() {
        solver = new BrentSolver();
    }

    // ---------- Basic root finding ----------

    @Test
    public void testSinZero() throws Exception {
        UnivariateRealFunction f = new SinFunction();
        double result = solver.solve(f, 3.0, 4.0);
        assertEquals(Math.PI, result, 1e-6);
    }

    @Test
    public void testQuinticZero() throws Exception {
        UnivariateRealFunction f = new QuinticFunction();
        double result = solver.solve(f, -0.5, 0.5);
        assertEquals(0.0, result, 1e-6);
    }

    @Test
    public void testExpm1Zero() throws Exception {
        UnivariateRealFunction f = new Expm1Function();
        double result = solver.solve(f, -1.0, 1.0);
        assertEquals(0.0, result, 1e-6);
    }

    // ---------- Edge cases with initial guess ----------

    @Test
    public void testInitialGuessOnRoot() throws Exception {
        UnivariateRealFunction f = new SinFunction();
        double result = solver.solve(f, 2.0, 4.0, Math.PI);
        assertEquals(Math.PI, result, 1e-6);
    }

    @Test
    public void testInitialGuessLeftOfRoot() throws Exception {
        UnivariateRealFunction f = new SinFunction();
        double result = solver.solve(f, 2.0, 4.0, 3.0);
        assertEquals(Math.PI, result, 1e-6);
    }

    @Test
    public void testInitialGuessRightOfRoot() throws Exception {
        UnivariateRealFunction f = new SinFunction();
        double result = solver.solve(f, 2.0, 4.0, 3.5);
        assertEquals(Math.PI, result, 1e-6);
    }

    // ---------- No bracketing (should throw) ----------

    @Test(expected = IllegalArgumentException.class)
    public void testNoBracketingBothPositive() throws Exception {
        UnivariateRealFunction f = new SinFunction();
        solver.solve(f, 0.5, 1.0); // sin(0.5) > 0, sin(1.0) > 0
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoBracketingBothNegative() throws Exception {
        UnivariateRealFunction f = new SinFunction();
        solver.solve(f, 4.0, 5.0); // sin(4) < 0, sin(5) < 0
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoBracketingWithInitialGuess() throws Exception {
        UnivariateRealFunction f = new SinFunction();
        solver.solve(f, 0.5, 1.0, 0.75);
    }

    // ---------- Endpoint is root ----------

    @Test
    public void testLeftEndpointIsRoot() throws Exception {
        UnivariateRealFunction f = new SinFunction();
        double result = solver.solve(f, 0.0, 1.0);
        assertEquals(0.0, result, 1e-6);
    }

    @Test
    public void testRightEndpointIsRoot() throws Exception {
        UnivariateRealFunction f = new SinFunction();
        double result = solver.solve(f, 3.0, Math.PI);
        assertEquals(Math.PI, result, 1e-6);
    }

    // ---------- Very flat function ----------

    @Test
    public void testFlatFunction() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return (x - 1.0) * (x - 1.0) - 1e-12;
            }
        };
        double result = solver.solve(f, 0.0, 2.0);
        assertEquals(1.0 + 1e-6, result, 1e-6);
    }

    // ---------- Function with multiple roots ----------

    @Test
    public void testMultipleRootsInInterval() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return Math.sin(x);
            }
        };
        // Interval contains 0 and pi, but solver should find one
        double result = solver.solve(f, -1.0, 4.0);
        assertTrue(Math.abs(result) < 1e-6 || Math.abs(result - Math.PI) < 1e-6);
    }

    // ---------- Tolerance and max evaluations ----------

    @Test
    public void testAbsoluteTolerance() throws Exception {
        solver.setAbsoluteAccuracy(1e-12);
        UnivariateRealFunction f = new SinFunction();
        double result = solver.solve(f, 3.0, 4.0);
        assertEquals(Math.PI, result, 1e-12);
    }

    @Test(expected = FunctionEvaluationException.class)
    public void testMaxEvaluationsExceeded() throws Exception {
        solver.setMaximalIterationCount(5);
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return Math.sin(x) - 0.5;
            }
        };
        solver.solve(f, 0.0, 1.0); // root at ~0.5236, but few iterations may fail
    }

    // ---------- Function that throws exception ----------

    @Test(expected = FunctionEvaluationException.class)
    public void testFunctionThrowsException() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) throws FunctionEvaluationException {
                throw new FunctionEvaluationException(x);
            }
        };
        solver.solve(f, 0.0, 1.0);
    }

    // ---------- Null function ----------

    @Test(expected = IllegalArgumentException.class)
    public void testNullFunction() throws Exception {
        solver.solve(null, 0.0, 1.0);
    }

    // ---------- Invalid interval ----------

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidInterval() throws Exception {
        UnivariateRealFunction f = new SinFunction();
        solver.solve(f, 1.0, 0.0);
    }

    // ---------- Very small interval ----------

    @Test
    public void testTinyInterval() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x - 1e-10;
            }
        };
        double result = solver.solve(f, 0.0, 1e-9);
        assertEquals(1e-10, result, 1e-15);
    }

    // ---------- Large interval ----------

    @Test
    public void testLargeInterval() throws Exception {
        UnivariateRealFunction f = new SinFunction();
        double result = solver.solve(f, -100.0, 100.0);
        assertTrue(Math.abs(result) < 1e-6 || Math.abs(result - Math.PI) < 1e-6);
    }

    // ---------- Test that triggers known bug (Defects4J 73) ----------
    // Bug: When endpoints have same sign but initial guess is provided,
    // solver may incorrectly return a value without checking bracketing.
    // The following test should throw IllegalArgumentException.

    @Test(expected = IllegalArgumentException.class)
    public void testBug73NoBracketingWithInitialGuess() throws Exception {
        UnivariateRealFunction f = new SinFunction();
        // Both endpoints positive, initial guess also positive
        solver.solve(f, 0.5, 1.0, 0.75);
    }

    // Additional test: initial guess outside interval
    @Test(expected = IllegalArgumentException.class)
    public void testInitialGuessOutsideInterval() throws Exception {
        UnivariateRealFunction f = new SinFunction();
        solver.solve(f, 3.0, 4.0, 2.5);
    }

    // ---------- Monitored function to check evaluations ----------

    @Test
    public void testMonitoredFunction() throws Exception {
        MonitoredFunction f = new MonitoredFunction(new SinFunction());
        solver.solve(f, 3.0, 4.0);
        assertTrue(f.getCalls() > 0);
    }

    // ---------- Function with zero at both endpoints ----------

    @Test
    public void testBothEndpointsZero() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * (x - 1.0);
            }
        };
        double result = solver.solve(f, 0.0, 1.0);
        assertTrue(result == 0.0 || result == 1.0);
    }

    // ---------- Function that is not continuous (but solver may still work) ----------

    @Test
    public void testDiscontinuousFunction() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                if (x < 0.5) return -1.0;
                else return 1.0;
            }
        };
        double result = solver.solve(f, 0.0, 1.0);
        assertEquals(0.5, result, 1e-6);
    }

    // ---------- Test with default solver settings ----------

    @Test
    public void testDefaultSolver() throws Exception {
        BrentSolver defaultSolver = new BrentSolver();
        UnivariateRealFunction f = new SinFunction();
        double result = defaultSolver.solve(f, 3.0, 4.0);
        assertEquals(Math.PI, result, 1e-6);
    }
}