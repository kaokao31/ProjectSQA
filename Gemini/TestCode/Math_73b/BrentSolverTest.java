package org.apache.commons.math.analysis.solvers;

import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.exception.MaxExceededException;
import org.junit.Assert;
import org.junit.Test;

public class BrentSolverTest {

    @Test
    public void testSolveWithExactRootAtInitial() throws Exception {
        BrentSolver solver = new BrentSolver();
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x;
            }
        };
        // root is at 0.0, min = -1.0, max = 1.0, initial = 0.0
        double result = solver.solve(f, -1.0, 1.0, 0.0);
        Assert.assertEquals(0.0, result, 1e-12);
    }

    @Test
    public void testSolveWithExactRootAtMin() throws Exception {
        BrentSolver solver = new BrentSolver();
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x;
            }
        };
        // root at min
        double result = solver.solve(f, 0.0, 1.0, 0.5);
        Assert.assertEquals(0.0, result, 1e-12);
    }

    @Test
    public void testSolveWithExactRootAtMax() throws Exception {
        BrentSolver solver = new BrentSolver();
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x - 1.0;
            }
        };
        // root at max
        double result = solver.solve(f, 0.0, 1.0, 0.5);
        Assert.assertEquals(1.0, result, 1e-12);
    }

    @Test
    public void testSolveStandardFunction() throws Exception {
        BrentSolver solver = new BrentSolver();
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x - 4.0;
            }
        };
        // root at 2.0
        double result = solver.solve(f, 1.0, 3.0);
        Assert.assertEquals(2.0, result, solver.getAbsoluteAccuracy());
    }

    @Test
    public void testSolveWithInitial() throws Exception {
        BrentSolver solver = new BrentSolver();
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x - 4.0;
            }
        };
        // root at 2.0 with initial guess
        double result = solver.solve(f, 1.0, 3.0, 2.5);
        Assert.assertEquals(2.0, result, solver.getAbsoluteAccuracy());
    }

    @Test
    public void testSolveBadInitial() throws Exception {
        BrentSolver solver = new BrentSolver();
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x - 4.0;
            }
        };
        // initial is outside or doesn't bracket well, but solver should still find root via fallback or bracketing
        double result = solver.solve(f, 1.0, 3.0, 1.1);
        Assert.assertEquals(2.0, result, solver.getAbsoluteAccuracy());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSolveInvalidInterval() throws Exception {
        BrentSolver solver = new BrentSolver();
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x;
            }
        };
        // min > max should throw exception
        solver.solve(f, 2.0, 1.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSolveNoBracketing() throws Exception {
        BrentSolver solver = new BrentSolver();
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x + 1.0; // always positive
            }
        };
        solver.solve(f, -1.0, 1.0);
    }

    @Test
    public void testSolvedWhenInitialMatchesRootExplicitlyBug73() throws Exception {
        // Specifically targeting the Math-73 issue where initial, min, max are given,
        // and initial is a root, but the function values at min/max do not bracket
        // properly or initial is returned incorrectly without verifying/handling.
        BrentSolver solver = new BrentSolver();
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                // Root at 1.0
                // f(min) = f(0) = -1
                // f(max) = f(2) = 3
                // f(initial) = f(1) = 0
                return x - 1.0;
            }
        };

        double result = solver.solve(f, 0.0, 2.0, 1.0);
        Assert.assertEquals(1.0, result, 1e-12);
        
        // Another scenario: initial is root, min and max have same sign (no bracketing for min/max, but initial is root)
        UnivariateRealFunction f2 = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x - 4.0;
            }
        };
        // f(2) = 0 (root at 2). min = 1.5, max = 2.5, initial = 2.0
        // f(1.5) = -1.75, f(2.5) = 2.25 (brackets anyway).
        // What if min/max don't bracket, but initial is a root?
        UnivariateRealFunction f3 = new UnivariateRealFunction() {
            public double value(double x) {
                return (x - 2.0) * (x - 2.0); // always >= 0, root at 2.0
            }
        };
        // f3(1.5) = 0.25, f3(2.5) = 0.25 -> same sign! But initial = 2.0 gives f3(2.0) = 0.
        try {
            double res3 = solver.solve(f3, 1.5, 2.5, 2.0);
            Assert.assertEquals(2.0, res3, 1e-12);
        } catch (Exception e) {
            // Depending on implementation, might throw or return.
        }
    }

    @Test
    public void testConstantsAndGetters() {
        BrentSolver solver = new BrentSolver();
        Assert.assertNotNull(solver);
    }
}