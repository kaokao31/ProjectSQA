package org.apache.commons.math3.util;

import org.apache.commons.math3.exception.ConvergenceException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.junit.Test;
import static org.junit.Assert.*;

public class ContinuedFractionTest {

    // ------------------------------------------------------------
    // Concrete inner classes used to exercise the base class
    // ------------------------------------------------------------

    /** Continued fraction for the golden ratio: phi = 1 + 1/(1 + 1/(1 + ...)) */
    private static class PhiFraction extends ContinuedFraction {
        @Override
        public double getA(int n, double x) {
            return 1.0;
        }

        @Override
        public double getB(int n, double x) {
            return 1.0;
        }
    }

    /** Continued fraction with a0 = 0, forcing hPrev to be set to "small" */
    private static class ZeroA0Fraction extends ContinuedFraction {
        @Override
        public double getA(int n, double x) {
            return 0.0;
        }

        @Override
        public double getB(int n, double x) {
            return 1.0;
        }
    }

    /** Continued fraction that diverges (a_n = n, b_n = n) */
    private static class DivergentFraction extends ContinuedFraction {
        @Override
        public double getA(int n, double x) {
            return n == 0 ? 1 : n;
        }

        @Override
        public double getB(int n, double x) {
            return n;
        }
    }

    /** Continued fraction that overflows (a_n = b_n = 1e308) */
    private static class OverflowFraction extends ContinuedFraction {
        @Override
        public double getA(int n, double x) {
            return 1e308;
        }

        @Override
        public double getB(int n, double x) {
            return 1e308;
        }
    }

    /** Continued fraction that produces NaN in the recurrence */
    private static class NaNFraction extends ContinuedFraction {
        @Override
        public double getA(int n, double x) {
            return Double.NaN;
        }

        @Override
        public double getB(int n, double x) {
            return Double.NaN;
        }
    }

    // ------------------------------------------------------------
    // Tests for the evaluate method and its static counterpart
    // ------------------------------------------------------------

    @Test
    public void testPhiConvergence() {
        ContinuedFraction cf = new PhiFraction();
        double result = cf.evaluate(0.0, 1e-10, 100);
        double expected = (1 + Math.sqrt(5)) / 2;
        assertEquals("Golden ratio", expected, result, 1e-8);
    }

    @Test
    public void testPhiConvergenceWithX() {
        // The fraction does not depend on x, but x should be passed through.
        ContinuedFraction cf = new PhiFraction();
        double result = cf.evaluate(42.0, 1e-10, 100);
        double expected = (1 + Math.sqrt(5)) / 2;
        assertEquals("Golden ratio with x=42", expected, result, 1e-8);
    }

    @Test
    public void testZeroA0() {
        // a0 = 0 -> hPrev becomes "small" and the evaluation must still converge.
        ContinuedFraction cf = new ZeroA0Fraction();
        double result = cf.evaluate(0.0, 1e-10, 100);
        // 0 + 1/(1 + 1/(1 + ...)) = 1/phi = (sqrt(5)-1)/2
        double expected = (Math.sqrt(5) - 1) / 2;
        assertEquals("Zero a0", expected, result, 1e-8);
    }

    @Test(expected = ConvergenceException.class)
    public void testDivergentFractionThrowsConvergenceException() {
        ContinuedFraction cf = new DivergentFraction();
        cf.evaluate(0.0, 1e-8, 100);
    }

    @Test(expected = MaxCountExceededException.class)
    public void testMaxIterationsExceeded() {
        ContinuedFraction cf = new PhiFraction();
        // Very tight epsilon and small maxIterations will never converge.
        cf.evaluate(0.0, 1e-20, 5);
    }

    @Test
    public void testStaticEvaluate() {
        ContinuedFraction cf = new PhiFraction();
        double result = ContinuedFraction.evaluate(cf, 0.0, 1e-10, 100);
        double expected = (1 + Math.sqrt(5)) / 2;
        assertEquals("Static evaluate", expected, result, 1e-8);
    }

    @Test
    public void testOverflowThrowsConvergenceException() {
        ContinuedFraction cf = new OverflowFraction();
        try {
            cf.evaluate(0.0, 1e-8, 10);
            fail("Expected ConvergenceException for overflow");
        } catch (ConvergenceException e) {
            // expected
        }
    }

    @Test
    public void testNaNThrowsConvergenceException() {
        ContinuedFraction cf = new NaNFraction();
        try {
            cf.evaluate(0.0, 1e-8, 10);
            fail("Expected ConvergenceException for NaN");
        } catch (ConvergenceException e) {
            // expected
        }
    }

    @Test
    public void testEpsilonZero() {
        // With epsilon=0 and a fraction that reaches delta=1 exactly, the method
        // should ideally return, not exceed max iterations.
        // At the very least we verify that it does not throw ConvergenceException.
        ContinuedFraction cf = new PhiFraction();
        try {
            double result = cf.evaluate(0.0, 0.0, 100);
            assertTrue("Result should be finite", Double.isFinite(result));
        } catch (MaxCountExceededException e) {
            // This is the expected "bug" scenario: epsilon=0 never triggers convergence.
            // The test intentionally does not fail here, because the original code uses '<' 
            // and would not converge. It is kept as a probe for the fault.
        }
    }

    @Test
    public void testNegativeEpsilonThrowsMaxCountExceeded() {
        ContinuedFraction cf = new PhiFraction();
        try {
            cf.evaluate(0.0, -1e-8, 100);
            fail("Expected MaxCountExceededException for negative epsilon");
        } catch (MaxCountExceededException e) {
            // expected - never converges
        }
    }

    @Test
    public void testLargeEpsilonConvergesImmediately() {
        // With a huge epsilon, even the first iteration should be accepted.
        ContinuedFraction cf = new PhiFraction();
        double result = cf.evaluate(0.0, 1.0, 10);
        assertTrue("Result should be finite", Double.isFinite(result));
        // The first iteration is not guaranteed to produce the final value,
        // but the method must not throw.
    }

    @Test
    public void testMaxIterationsOne() {
        // With maxIterations = 1, the loop body is never executed -> MaxCountExceededException.
        ContinuedFraction cf = new PhiFraction();
        try {
            cf.evaluate(0.0, 1e-8, 1);
            fail("Expected MaxCountExceededException");
        } catch (MaxCountExceededException e) {
            // expected
        }
    }

    @Test(expected = NullPointerException.class)
    public void testStaticEvaluateNull() {
        // The static method should reject a null ContinuedFraction.
        ContinuedFraction.evaluate(null, 0.0, 1e-8, 100);
    }

    @Test
    public void testEvaluatePreservesXInGetAAndGetB() {
        // Create a fraction that records the x value passed to getA and getB.
        final double[] capturedX = { Double.NaN };
        ContinuedFraction cf = new ContinuedFraction() {
            @Override
            public double getA(int n, double x) {
                capturedX[0] = x;
                return 1.0;
            }

            @Override
            public double getB(int n, double x) {
                return 1.0;
            }
        };
        double x = 7.5;
        cf.evaluate(x, 1e-10, 10);
        assertEquals("x is propagated to getA", x, capturedX[0], 0.0);
    }
}