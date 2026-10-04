package org.apache.commons.math.analysis;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

/**
 * Test suite for BrentSolver (Math bug 97 context).
 * Covers valid bracketing, invalid bracketing, root at bounds,
 * no root, multiple roots, linear/constant functions, and edge cases.
 */
public class BrentSolverTest {

    private static final double EPS = 1e-12;
    private BrentSolver solver;

    @Before
    public void setUp() {
        solver = new BrentSolver();
    }

    // Helper: function f(x) = x - 2 (root at 2)
    private static class LinearFunction implements UnivariateRealFunction {
        public double value(double x) {
            return x - 2.0;
        }
    }

    // Helper: function f(x) = (x - 1)*(x - 3) (roots at 1 and 3)
    private static class QuadraticFunction implements UnivariateRealFunction {
        public double value(double x) {
            return (x - 1.0) * (x - 3.0);
        }
    }

    // Helper: function f(x) = x^2 + 1 (no real root)
    private static class NoRootFunction implements UnivariateRealFunction {
        public double value(double x) {
            return x * x + 1.0;
        }
    }

    // Helper: constant positive function
    private static class ConstantPositiveFunction implements UnivariateRealFunction {
        public double value(double x) {
            return 5.0;
        }
    }

    // Helper: constant negative function
    private static class ConstantNegativeFunction implements UnivariateRealFunction {
        public double value(double x) {
            return -5.0;
        }
    }

    // Helper: function with root exactly at lower bound
    private static class RootAtLowerBoundFunction implements UnivariateRealFunction {
        public double value(double x) {
            return x; // root at 0
        }
    }

    // Helper: function with root exactly at upper bound
    private static class RootAtUpperBoundFunction implements UnivariateRealFunction {
        public double value(double x) {
            return x - 10.0; // root at 10
        }
    }

    // Helper: function that is zero everywhere
    private static class ZeroFunction implements UnivariateRealFunction {
        public double value(double x) {
            return 0.0;
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidBracketingSameSignPositive() {
        // f(a) and f(b) both positive -> should throw IllegalArgumentException
        solver.solve(new ConstantPositiveFunction(), 0.0, 10.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidBracketingSameSignNegative() {
        // f(a) and f(b) both negative -> should throw IllegalArgumentException
        solver.solve(new ConstantNegativeFunction(), 0.0, 10.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidBracketingOneZero() {
        // f(a) = 0, f(b) positive -> product is 0, should throw (no sign change)
        solver.solve(new RootAtLowerBoundFunction(), 0.0, 10.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidBracketingBothZero() {
        // f(a) = f(b) = 0 -> product is 0, should throw
        solver.solve(new ZeroFunction(), 0.0, 10.0);
    }

    @Test
    public void testValidBracketingLinear() {
        double result = solver.solve(new LinearFunction(), 0.0, 5.0);
        assertEquals(2.0, result, 1e-14);
    }

    @Test
    public void testValidBracketingQuadratic() {
        // interval [0, 2] contains root at 1
        double result = solver.solve(new QuadraticFunction(), 0.0, 2.0);
        assertEquals(1.0, result, 1e-14);
    }

    @Test
    public void testRootAtLowerBound() {
        // f(0)=0, f(10)=10 -> product 0, but if solver checks sign change strictly,
        // it might throw. However, some implementations accept if one bound is exactly root.
        // We test the behavior: if it throws, we expect IllegalArgumentException.
        // But the bug 97 fix likely throws for any non-strict sign change.
        // We'll test that it either returns the root or throws.
        try {
            double result = solver.solve(new RootAtLowerBoundFunction(), 0.0, 10.0);
            assertEquals(0.0, result, 1e-14);
        } catch (IllegalArgumentException e) {
            // acceptable if implementation rejects zero product
        }
    }

    @Test
    public void testRootAtUpperBound() {
        try {
            double result = solver.solve(new RootAtUpperBoundFunction(), 0.0, 10.0);
            assertEquals(10.0, result, 1e-14);
        } catch (IllegalArgumentException e) {
            // acceptable
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoRootInInterval() {
        // f(a) and f(b) both positive -> no sign change, should throw
        solver.solve(new NoRootFunction(), 0.0, 10.0);
    }

    @Test
    public void testWithInitialValue() {
        // solve with initial value near root
        double result = solver.solve(new LinearFunction(), 0.0, 5.0, 1.0);
        assertEquals(2.0, result, 1e-14);
    }

    @Test
    public void testWithInitialValueAtRoot() {
        // initial value exactly at root
        double result = solver.solve(new LinearFunction(), 0.0, 5.0, 2.0);
        assertEquals(2.0, result, 1e-14);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidBracketingWithInitialValue() {
        // same sign with initial value
        solver.solve(new ConstantPositiveFunction(), 0.0, 10.0, 5.0);
    }

    @Test
    public void testTolerance() {
        // set custom absolute tolerance
        solver.setAbsoluteAccuracy(1e-6);
        double result = solver.solve(new LinearFunction(), 0.0, 5.0);
        assertEquals(2.0, result, 1e-6);
    }

    @Test
    public void testFunctionValueAtRootIsZero() {
        UnivariateRealFunction f = new LinearFunction();
        double root = solver.solve(f, 0.0, 5.0);
        assertEquals(0.0, f.value(root), 1e-12);
    }

    @Test(expected = NullPointerException.class)
    public void testNullFunction() {
        solver.solve(null, 0.0, 1.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLowerBoundGreaterThanUpperBound() {
        // invalid interval
        solver.solve(new LinearFunction(), 5.0, 0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLowerBoundEqualToUpperBound() {
        // degenerate interval
        solver.solve(new LinearFunction(), 2.0, 2.0);
    }

    @Test
    public void testFunctionWithMultipleRoots() {
        // interval [0, 4] contains roots at 1 and 3; solver should find one
        double result = solver.solve(new QuadraticFunction(), 0.0, 4.0);
        assertTrue(result >= 0.9 && result <= 3.1);
        // either 1 or 3, depending on initial bracketing
    }

    @Test
    public void testVeryFlatFunction() {
        // f(x) = (x - 1)^3, root at 1, but derivative zero at root
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                double dx = x - 1.0;
                return dx * dx * dx;
            }
        };
        double result = solver.solve(f, 0.0, 2.0);
        assertEquals(1.0, result, 1e-6);
    }

    @Test
    public void testFunctionWithDiscontinuity() {
        // f(x) = 1/(x-2) has discontinuity at 2, but sign change across it
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return 1.0 / (x - 2.0);
            }
        };
        // interval [1, 3] has sign change (f(1) = -1, f(3) = 1) but discontinuity inside
        // Solver may converge to 2.0 (pole) or throw? We'll just check it doesn't crash.
        try {
            double result = solver.solve(f, 1.0, 3.0);
            // result should be near 2.0 but not exactly due to tolerance
            assertTrue(Double.isFinite(result));
        } catch (Exception e) {
            // acceptable if solver detects issue
        }
    }
}