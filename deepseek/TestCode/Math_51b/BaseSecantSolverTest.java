package org.apache.commons.math.analysis.solvers;

import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.analysis.function.Identity;
import org.apache.commons.math.analysis.function.Sin;
import org.apache.commons.math.analysis.function.Constant;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test suite for BaseSecantSolver (Math-51 bug context).
 * Covers edge cases, branch coverage, and the specific bug where
 * the solver fails when the initial guess is exactly the root.
 */
public class BaseSecantSolverTest {

    private static final double EPSILON = 1e-10;
    private BaseSecantSolver solver;

    @Before
    public void setUp() {
        // Use SecantSolver as concrete implementation for testing
        solver = new SecantSolver(EPSILON);
    }

    // ========== Bug-specific test: initial guess is the root ==========
    @Test
    public void testInitialGuessIsRoot() {
        // f(x) = x - 2, root at x=2
        UnivariateRealFunction f = new Identity() {
            @Override
            public double value(double x) {
                return x - 2.0;
            }
        };
        double result = solver.solve(100, f, 0.0, 5.0, 2.0);
        assertEquals("Solver should return the initial guess when it is the root",
                     2.0, result, EPSILON);
    }

    // ========== Normal convergence ==========
    @Test
    public void testNormalConvergence() {
        // f(x) = sin(x) - 0.5, root near 0.5236
        UnivariateRealFunction f = new Sin() {
            @Override
            public double value(double x) {
                return Math.sin(x) - 0.5;
            }
        };
        double result = solver.solve(100, f, 0.0, 1.0, 0.5);
        assertEquals(0.5235987755982988, result, 1e-6);
    }

    // ========== Root at lower bound ==========
    @Test
    public void testRootAtLowerBound() {
        UnivariateRealFunction f = new Identity() {
            @Override
            public double value(double x) {
                return x; // root at 0
            }
        };
        double result = solver.solve(100, f, -1.0, 0.0, -0.5);
        assertEquals(0.0, result, EPSILON);
    }

    // ========== Root at upper bound ==========
    @Test
    public void testRootAtUpperBound() {
        UnivariateRealFunction f = new Identity() {
            @Override
            public double value(double x) {
                return x - 3.0; // root at 3
            }
        };
        double result = solver.solve(100, f, 0.0, 3.0, 1.5);
        assertEquals(3.0, result, EPSILON);
    }

    // ========== No root in interval (should throw exception) ==========
    @Test(expected = org.apache.commons.math.exception.NoBracketingException.class)
    public void testNoBracketing() {
        // f(x) = 1 (constant positive), no root
        UnivariateRealFunction f = new Constant(1.0);
        solver.solve(100, f, -1.0, 1.0, 0.0);
    }

    // ========== Function with same sign at bounds but root inside (bracketing failure) ==========
    @Test(expected = org.apache.commons.math.exception.NoBracketingException.class)
    public void testSameSignAtBounds() {
        // f(x) = (x-1)^2 + 0.1, always positive, no root
        UnivariateRealFunction f = new UnivariateRealFunction() {
            @Override
            public double value(double x) {
                return Math.pow(x - 1.0, 2) + 0.1;
            }
        };
        solver.solve(100, f, 0.0, 2.0, 1.0);
    }

    // ========== Very flat function near root ==========
    @Test
    public void testFlatFunction() {
        // f(x) = (x-1)^3, root at 1, flat derivative
        UnivariateRealFunction f = new UnivariateRealFunction() {
            @Override
            public double value(double x) {
                return Math.pow(x - 1.0, 3);
            }
        };
        double result = solver.solve(100, f, 0.0, 2.0, 1.5);
        assertEquals(1.0, result, 1e-6);
    }

    // ========== Multiple roots (should converge to one) ==========
    @Test
    public void testMultipleRoots() {
        // f(x) = sin(x), roots at 0, pi, etc.
        UnivariateRealFunction f = new Sin();
        double result = solver.solve(100, f, 2.0, 4.0, 3.0);
        assertEquals(Math.PI, result, 1e-6);
    }

    // ========== Test with different solving methods ==========
    @Test
    public void testRegulaFalsi() {
        solver = new RegulaFalsiSolver(EPSILON);
        UnivariateRealFunction f = new Identity() {
            @Override
            public double value(double x) {
                return x - 2.0;
            }
        };
        double result = solver.solve(100, f, 0.0, 5.0, 2.0);
        assertEquals(2.0, result, EPSILON);
    }

    @Test
    public void testIllinois() {
        solver = new IllinoisSolver(EPSILON);
        UnivariateRealFunction f = new Identity() {
            @Override
            public double value(double x) {
                return x - 2.0;
            }
        };
        double result = solver.solve(100, f, 0.0, 5.0, 2.0);
        assertEquals(2.0, result, EPSILON);
    }

    @Test
    public void testPegasus() {
        solver = new PegasusSolver(EPSILON);
        UnivariateRealFunction f = new Identity() {
            @Override
            public double value(double x) {
                return x - 2.0;
            }
        };
        double result = solver.solve(100, f, 0.0, 5.0, 2.0);
        assertEquals(2.0, result, EPSILON);
    }

    // ========== Edge case: very small interval ==========
    @Test
    public void testTinyInterval() {
        UnivariateRealFunction f = new Identity() {
            @Override
            public double value(double x) {
                return x - 1e-12;
            }
        };
        double result = solver.solve(100, f, 0.0, 1e-10, 5e-11);
        assertEquals(1e-12, result, 1e-12);
    }

    // ========== Edge case: large values ==========
    @Test
    public void testLargeValues() {
        UnivariateRealFunction f = new Identity() {
            @Override
            public double value(double x) {
                return x - 1e10;
            }
        };
        double result = solver.solve(100, f, 0.0, 2e10, 1e10);
        assertEquals(1e10, result, 1e-6);
    }

    // ========== Null function (should throw) ==========
    @Test(expected = NullPointerException.class)
    public void testNullFunction() {
        solver.solve(100, null, 0.0, 1.0, 0.5);
    }

    // ========== Max evaluations exceeded ==========
    @Test(expected = org.apache.commons.math.exception.TooManyEvaluationsException.class)
    public void testMaxEvaluationsExceeded() {
        // Use a function that converges very slowly
        UnivariateRealFunction f = new UnivariateRealFunction() {
            @Override
            public double value(double x) {
                return Math.signum(x - 0.5); // step function, no root
            }
        };
        solver.solve(10, f, 0.0, 1.0, 0.3); // only 10 evaluations allowed
    }
}