package org.apache.commons.math.analysis.solvers;

import org.junit.Test;
import org.junit.Before;
import org.junit.Assert;

import org.apache.commons.math.analysis.UnivariateFunction;
import org.apache.commons.math.exception.TooManyEvaluationsException;
import org.apache.commons.math.exception.NoBracketingException;
import org.apache.commons.math.exception.NullArgumentException;

/**
 * Comprehensive JUnit 4 test suite for BaseSecantSolver.
 * Tests cover normal operation, edge cases, boundary conditions,
 * and scenarios designed to trigger potential faults (e.g., Math-48 bug).
 */
public class BaseSecantSolverTest {

    private static final double DEFAULT_ABSOLUTE_ACCURACY = 1e-6;
    private static final double DEFAULT_RELATIVE_ACCURACY = 1e-14;
    private static final int DEFAULT_MAX_EVALUATIONS = 100;

    private BaseSecantSolver solver;

    @Before
    public void setUp() {
        // Use SecantSolver as a concrete implementation of BaseSecantSolver
        solver = new SecantSolver(DEFAULT_ABSOLUTE_ACCURACY);
    }

    // ========== Basic root finding ==========

    @Test
    public void testLinearFunction() {
        UnivariateFunction f = new UnivariateFunction() {
            @Override
            public double value(double x) {
                return 2.0 * x - 4.0; // root at x=2
            }
        };
        double result = solver.solve(DEFAULT_MAX_EVALUATIONS, f, 0.0, 5.0);
        Assert.assertEquals(2.0, result, DEFAULT_ABSOLUTE_ACCURACY);
    }

    @Test
    public void testQuadraticFunction() {
        UnivariateFunction f = new UnivariateFunction() {
            @Override
            public double value(double x) {
                return (x - 3.0) * (x + 1.0); // roots at -1 and 3
            }
        };
        double result = solver.solve(DEFAULT_MAX_EVALUATIONS, f, 0.0, 5.0);
        Assert.assertEquals(3.0, result, DEFAULT_ABSOLUTE_ACCURACY);
    }

    @Test
    public void testRootAtEndpoint() {
        UnivariateFunction f = new UnivariateFunction() {
            @Override
            public double value(double x) {
                return x - 1.0; // root at x=1
            }
        };
        double result = solver.solve(DEFAULT_MAX_EVALUATIONS, f, 1.0, 5.0);
        Assert.assertEquals(1.0, result, DEFAULT_ABSOLUTE_ACCURACY);
    }

    @Test
    public void testRootAtZero() {
        UnivariateFunction f = new UnivariateFunction() {
            @Override
            public double value(double x) {
                return x; // root at 0
            }
        };
        double result = solver.solve(DEFAULT_MAX_EVALUATIONS, f, -1.0, 1.0);
        Assert.assertEquals(0.0, result, DEFAULT_ABSOLUTE_ACCURACY);
    }

    // ========== Edge cases: flat function (potential bug trigger) ==========

    @Test
    public void testFlatFunctionNearRoot() {
        // f(x) = (x-1)^3 has root at 1 but derivative zero -> secant may struggle
        UnivariateFunction f = new UnivariateFunction() {
            @Override
            public double value(double x) {
                double diff = x - 1.0;
                return diff * diff * diff;
            }
        };
        double result = solver.solve(DEFAULT_MAX_EVALUATIONS, f, 0.0, 2.0);
        Assert.assertEquals(1.0, result, 1e-4); // allow slightly larger tolerance due to flatness
    }

    @Test
    public void testFunctionWithVerySmallSlope() {
        // f(x) = 1e-6 * (x - 5) -> root at 5, but slope is tiny
        UnivariateFunction f = new UnivariateFunction() {
            @Override
            public double value(double x) {
                return 1e-6 * (x - 5.0);
            }
        };
        double result = solver.solve(DEFAULT_MAX_EVALUATIONS, f, 0.0, 10.0);
        Assert.assertEquals(5.0, result, 1e-3);
    }

    // ========== No root / exception cases ==========

    @Test(expected = TooManyEvaluationsException.class)
    public void testNoRootConstantPositive() {
        UnivariateFunction f = new UnivariateFunction() {
            @Override
            public double value(double x) {
                return 1.0; // always positive, no root
            }
        };
        solver.solve(DEFAULT_MAX_EVALUATIONS, f, -1.0, 1.0);
    }

    @Test(expected = TooManyEvaluationsException.class)
    public void testNoRootConstantNegative() {
        UnivariateFunction f = new UnivariateFunction() {
            @Override
            public double value(double x) {
                return -1.0; // always negative, no root
            }
        };
        solver.solve(DEFAULT_MAX_EVALUATIONS, f, -1.0, 1.0);
    }

    @Test(expected = NullArgumentException.class)
    public void testNullFunction() {
        solver.solve(DEFAULT_MAX_EVALUATIONS, null, 0.0, 1.0);
    }

    // ========== Boundary conditions ==========

    @Test
    public void testVerySmallInterval() {
        UnivariateFunction f = new UnivariateFunction() {
            @Override
            public double value(double x) {
                return x - 0.5;
            }
        };
        double result = solver.solve(DEFAULT_MAX_EVALUATIONS, f, 0.4999, 0.5001);
        Assert.assertEquals(0.5, result, 1e-6);
    }

    @Test
    public void testLargeInterval() {
        UnivariateFunction f = new UnivariateFunction() {
            @Override
            public double value(double x) {
                return x - 1000.0;
            }
        };
        double result = solver.solve(DEFAULT_MAX_EVALUATIONS, f, -1e6, 1e6);
        Assert.assertEquals(1000.0, result, 1e-3);
    }

    @Test
    public void testNegativeInterval() {
        UnivariateFunction f = new UnivariateFunction() {
            @Override
            public double value(double x) {
                return x + 5.0; // root at -5
            }
        };
        double result = solver.solve(DEFAULT_MAX_EVALUATIONS, f, -10.0, -2.0);
        Assert.assertEquals(-5.0, result, DEFAULT_ABSOLUTE_ACCURACY);
    }

    // ========== Multiple evaluations / convergence ==========

    @Test
    public void testWithStartValue() {
        UnivariateFunction f = new UnivariateFunction() {
            @Override
            public double value(double x) {
                return Math.sin(x); // roots at multiples of pi
            }
        };
        double result = solver.solve(DEFAULT_MAX_EVALUATIONS, f, 2.0, 4.0, 3.0);
        Assert.assertEquals(Math.PI, result, 1e-6);
    }

    @Test
    public void testHighAccuracy() {
        solver = new SecantSolver(1e-12);
        UnivariateFunction f = new UnivariateFunction() {
            @Override
            public double value(double x) {
                return x * x - 2.0; // root at sqrt(2)
            }
        };
        double result = solver.solve(DEFAULT_MAX_EVALUATIONS, f, 1.0, 2.0);
        Assert.assertEquals(Math.sqrt(2.0), result, 1e-10);
    }

    // ========== Specific bug trigger (Math-48) ==========

    @Test
    public void testBug48FlatFunction() {
        // This function caused the solver to exceed max evaluations in the buggy version
        UnivariateFunction f = new UnivariateFunction() {
            @Override
            public double value(double x) {
                return (x - 1.0) * (x - 1.0) * (x - 1.0) - 1e-12;
            }
        };
        double result = solver.solve(DEFAULT_MAX_EVALUATIONS, f, 0.0, 2.0);
        Assert.assertEquals(1.0 + Math.cbrt(1e-12), result, 1e-6);
    }

    // ========== Additional edge cases ==========

    @Test
    public void testFunctionWithDiscontinuity() {
        // f(x) = 1/(x-2) has a discontinuity at 2, but root? Actually no root.
        // This tests that solver handles non-root cases gracefully.
        UnivariateFunction f = new UnivariateFunction() {
            @Override
            public double value(double x) {
                return 1.0 / (x - 2.0);
            }
        };
        try {
            solver.solve(DEFAULT_MAX_EVALUATIONS, f, 0.0, 1.0);
            Assert.fail("Expected TooManyEvaluationsException");
        } catch (TooManyEvaluationsException e) {
            // expected
        }
    }

    @Test
    public void testFunctionWithMultipleRoots() {
        UnivariateFunction f = new UnivariateFunction() {
            @Override
            public double value(double x) {
                return Math.sin(x);
            }
        };
        double result = solver.solve(DEFAULT_MAX_EVALUATIONS, f, 2.0, 4.0);
        Assert.assertEquals(Math.PI, result, 1e-6);
    }

    @Test
    public void testFunctionWithRootAtBoundary() {
        UnivariateFunction f = new UnivariateFunction() {
            @Override
            public double value(double x) {
                return x - 5.0;
            }
        };
        double result = solver.solve(DEFAULT_MAX_EVALUATIONS, f, 5.0, 10.0);
        Assert.assertEquals(5.0, result, DEFAULT_ABSOLUTE_ACCURACY);
    }

    @Test
    public void testFunctionWithNegativeRoot() {
        UnivariateFunction f = new UnivariateFunction() {
            @Override
            public double value(double x) {
                return x + 10.0;
            }
        };
        double result = solver.solve(DEFAULT_MAX_EVALUATIONS, f, -20.0, -5.0);
        Assert.assertEquals(-10.0, result, DEFAULT_ABSOLUTE_ACCURACY);
    }

    @Test
    public void testFunctionWithVeryLargeValues() {
        UnivariateFunction f = new UnivariateFunction() {
            @Override
            public double value(double x) {
                return 1e10 * (x - 1e-5);
            }
        };
        double result = solver.solve(DEFAULT_MAX_EVALUATIONS, f, 0.0, 1.0);
        Assert.assertEquals(1e-5, result, 1e-3);
    }

    @Test
    public void testFunctionWithVerySmallValues() {
        UnivariateFunction f = new UnivariateFunction() {
            @Override
            public double value(double x) {
                return 1e-10 * (x - 1e5);
            }
        };
        double result = solver.solve(DEFAULT_MAX_EVALUATIONS, f, 0.0, 1e6);
        Assert.assertEquals(1e5, result, 1e-1);
    }

    // ========== Test with different solver configurations ==========

    @Test
    public void testWithRelativeAccuracy() {
        solver = new SecantSolver(DEFAULT_RELATIVE_ACCURACY, DEFAULT_ABSOLUTE_ACCURACY);
        UnivariateFunction f = new UnivariateFunction() {
            @Override
            public double value(double x) {
                return x - 100.0;
            }
        };
        double result = solver.solve(DEFAULT_MAX_EVALUATIONS, f, 0.0, 200.0);
        Assert.assertEquals(100.0, result, 1e-6);
    }

    @Test
    public void testWithVeryLowMaxEvaluations() {
        solver = new SecantSolver(DEFAULT_ABSOLUTE_ACCURACY);
        UnivariateFunction f = new UnivariateFunction() {
            @Override
            public double value(double x) {
                return x - 1.0;
            }
        };
        try {
            solver.solve(2, f, 0.0, 10.0); // too few evaluations
            Assert.fail("Expected TooManyEvaluationsException");
        } catch (TooManyEvaluationsException e) {
            // expected
        }
    }

    // ========== Null argument tests ==========

    @Test(expected = NullArgumentException.class)
    public void testNullFunctionWithStartValue() {
        solver.solve(DEFAULT_MAX_EVALUATIONS, null, 0.0, 1.0, 0.5);
    }

    @Test(expected = NullArgumentException.class)
    public void testNullFunctionWithAllArgs() {
        solver.solve(DEFAULT_MAX_EVALUATIONS, null, 0.0, 1.0, 0.5, 1e-6, 1e-14);
    }

    // ========== Test with function that returns NaN ==========

    @Test(expected = TooManyEvaluationsException.class)
    public void testFunctionReturningNaN() {
        UnivariateFunction f = new UnivariateFunction() {
            @Override
            public double value(double x) {
                return Double.NaN;
            }
        };
        solver.solve(DEFAULT_MAX_EVALUATIONS, f, 0.0, 1.0);
    }

    // ========== Test with function that returns Infinity ==========

    @Test(expected = TooManyEvaluationsException.class)
    public void testFunctionReturningInfinity() {
        UnivariateFunction f = new UnivariateFunction() {
            @Override
            public double value(double x) {
                return Double.POSITIVE_INFINITY;
            }
        };
        solver.solve(DEFAULT_MAX_EVALUATIONS, f, 0.0, 1.0);
    }

    // ========== Test with identical bounds ==========

    @Test(expected = IllegalArgumentException.class)
    public void testIdenticalBounds() {
        UnivariateFunction f = new UnivariateFunction() {
            @Override
            public double value(double x) {
                return x - 1.0;
            }
        };
        solver.solve(DEFAULT_MAX_EVALUATIONS, f, 2.0, 2.0);
    }

    // ========== Test with reversed bounds ==========

    @Test
    public void testReversedBounds() {
        UnivariateFunction f = new UnivariateFunction() {
            @Override
            public double value(double x) {
                return x - 3.0;
            }
        };
        double result = solver.solve(DEFAULT_MAX_EVALUATIONS, f, 5.0, 1.0);
        Assert.assertEquals(3.0, result, DEFAULT_ABSOLUTE_ACCURACY);
    }

    // ========== Test with function that has root at both bounds ==========

    @Test
    public void testRootAtBothBounds() {
        UnivariateFunction f = new UnivariateFunction() {
            @Override
            public double value(double x) {
                return (x - 1.0) * (x - 2.0);
            }
        };
        double result = solver.solve(DEFAULT_MAX_EVALUATIONS, f, 1.0, 2.0);
        // Should converge to one of the roots, likely 1 or 2 depending on initial guess
        Assert.assertTrue(result == 1.0 || result == 2.0);
    }
}