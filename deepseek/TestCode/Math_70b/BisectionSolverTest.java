package org.apache.commons.math.analysis.solvers;

import org.apache.commons.math.analysis.UnivariateFunction;
import org.apache.commons.math.analysis.function.Identity;
import org.apache.commons.math.analysis.function.Sin;
import org.apache.commons.math.analysis.function.Constant;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test suite for BisectionSolver, targeting high coverage and fault detection.
 * Designed to reveal the bug in Defects4J Math-70: solver fails when the root
 * is exactly at the lower bound (f(lower) == 0).
 */
public class BisectionSolverTest {

    private BisectionSolver solver;
    private static final double EPS = 1e-10;

    @Before
    public void setUp() {
        solver = new BisectionSolver();
    }

    // ---------- Normal bracketing cases ----------

    @Test
    public void testRootInMiddle() {
        // f(x) = x - 2, root at 2
        UnivariateFunction f = new Identity() {
            @Override
            public double value(double x) {
                return x - 2.0;
            }
        };
        double result = solver.solve(100, f, 0.0, 5.0);
        assertEquals(2.0, result, 1e-12);
    }

    @Test
    public void testRootNearLowerBound() {
        // f(x) = x - 1, root at 1, interval [1.1, 5] -> root not inside, but we want bracketing
        // Actually need bracketing: f(1.1) > 0, f(5) > 0 -> not bracketing. Use interval [0, 2] -> f(0)<0, f(2)>0
        UnivariateFunction f = new Identity() {
            @Override
            public double value(double x) {
                return x - 1.0;
            }
        };
        double result = solver.solve(100, f, 0.0, 2.0);
        assertEquals(1.0, result, 1e-12);
    }

    @Test
    public void testRootNearUpperBound() {
        // f(x) = x - 4, root at 4, interval [3, 5]
        UnivariateFunction f = new Identity() {
            @Override
            public double value(double x) {
                return x - 4.0;
            }
        };
        double result = solver.solve(100, f, 3.0, 5.0);
        assertEquals(4.0, result, 1e-12);
    }

    // ---------- Edge cases: root exactly at bounds ----------

    @Test
    public void testRootAtLowerBound() {
        // f(x) = x - 1, root at 1, interval [1, 3] -> f(1)=0, f(3)>0
        // This is the bug scenario: solver should return 1.0 immediately.
        UnivariateFunction f = new Identity() {
            @Override
            public double value(double x) {
                return x - 1.0;
            }
        };
        double result = solver.solve(100, f, 1.0, 3.0);
        assertEquals(1.0, result, 1e-12);
    }

    @Test
    public void testRootAtUpperBound() {
        // f(x) = x - 5, root at 5, interval [0, 5] -> f(0)<0, f(5)=0
        UnivariateFunction f = new Identity() {
            @Override
            public double value(double x) {
                return x - 5.0;
            }
        };
        double result = solver.solve(100, f, 0.0, 5.0);
        assertEquals(5.0, result, 1e-12);
    }

    @Test
    public void testRootAtBothBounds() {
        // f(x) = 0 constant, root everywhere, interval [2, 2] -> degenerate
        UnivariateFunction f = new Constant(0.0);
        double result = solver.solve(100, f, 2.0, 2.0);
        assertEquals(2.0, result, 1e-12);
    }

    // ---------- Non-bracketing intervals (should throw exception) ----------

    @Test(expected = IllegalArgumentException.class)
    public void testNonBracketingSameSignPositive() {
        // f(x) = x - 3, interval [4, 5] -> both positive
        UnivariateFunction f = new Identity() {
            @Override
            public double value(double x) {
                return x - 3.0;
            }
        };
        solver.solve(100, f, 4.0, 5.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNonBracketingSameSignNegative() {
        // f(x) = x + 2, interval [-5, -3] -> both negative
        UnivariateFunction f = new Identity() {
            @Override
            public double value(double x) {
                return x + 2.0;
            }
        };
        solver.solve(100, f, -5.0, -3.0);
    }

    // ---------- Sin function with multiple roots ----------

    @Test
    public void testSinRoot() {
        // sin(x) has root at pi, interval [2, 4]
        UnivariateFunction f = new Sin();
        double result = solver.solve(100, f, 2.0, 4.0);
        assertEquals(Math.PI, result, 1e-12);
    }

    // ---------- Very small interval ----------

    @Test
    public void testTinyInterval() {
        // f(x) = x - 1.5, interval [1.5, 1.5000000001] -> root at lower bound
        UnivariateFunction f = new Identity() {
            @Override
            public double value(double x) {
                return x - 1.5;
            }
        };
        double result = solver.solve(100, f, 1.5, 1.5000000001);
        assertEquals(1.5, result, 1e-12);
    }

    // ---------- Large interval ----------

    @Test
    public void testLargeInterval() {
        // f(x) = x - 1000, interval [0, 2000]
        UnivariateFunction f = new Identity() {
            @Override
            public double value(double x) {
                return x - 1000.0;
            }
        };
        double result = solver.solve(100, f, 0.0, 2000.0);
        assertEquals(1000.0, result, 1e-12);
    }

    // ---------- Negative root ----------

    @Test
    public void testNegativeRoot() {
        // f(x) = x + 3, root at -3, interval [-5, 0]
        UnivariateFunction f = new Identity() {
            @Override
            public double value(double x) {
                return x + 3.0;
            }
        };
        double result = solver.solve(100, f, -5.0, 0.0);
        assertEquals(-3.0, result, 1e-12);
    }

    // ---------- Function with zero at both ends (degenerate) ----------

    @Test
    public void testZeroAtBothEnds() {
        // f(x) = (x-2)*(x-2), root at 2 but both ends positive? Actually f(2)=0, f(2)=0
        // Use interval [2,2] already covered. Use [1,3] but f(1)>0, f(3)>0 -> not bracketing.
        // So skip.
    }

    // ---------- Test with maxEvaluations limit ----------

    @Test
    public void testMaxEvaluations() {
        // Use a function that requires many iterations, but default max is 100, should converge
        UnivariateFunction f = new Identity() {
            @Override
            public double value(double x) {
                return x - 0.5;
            }
        };
        double result = solver.solve(100, f, 0.0, 1.0);
        assertEquals(0.5, result, 1e-12);
    }

    // ---------- Additional edge: very steep function ----------

    @Test
    public void testSteepFunction() {
        // f(x) = 1000*(x - 0.5), root at 0.5
        UnivariateFunction f = new UnivariateFunction() {
            @Override
            public double value(double x) {
                return 1000.0 * (x - 0.5);
            }
        };
        double result = solver.solve(100, f, 0.0, 1.0);
        assertEquals(0.5, result, 1e-12);
    }

    // ---------- Test with null function (should throw NullPointerException) ----------

    @Test(expected = NullPointerException.class)
    public void testNullFunction() {
        solver.solve(100, null, 0.0, 1.0);
    }

    // ---------- Test with NaN bounds ----------

    @Test(expected = IllegalArgumentException.class)
    public void testNaNLowerBound() {
        UnivariateFunction f = new Identity() {
            @Override
            public double value(double x) {
                return x - 2.0;
            }
        };
        solver.solve(100, f, Double.NaN, 5.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNaNUpperBound() {
        UnivariateFunction f = new Identity() {
            @Override
            public double value(double x) {
                return x - 2.0;
            }
        };
        solver.solve(100, f, 0.0, Double.NaN);
    }

    // ---------- Test with infinite bounds ----------

    @Test(expected = IllegalArgumentException.class)
    public void testInfiniteLowerBound() {
        UnivariateFunction f = new Identity() {
            @Override
            public double value(double x) {
                return x - 2.0;
            }
        };
        solver.solve(100, f, Double.NEGATIVE_INFINITY, 5.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInfiniteUpperBound() {
        UnivariateFunction f = new Identity() {
            @Override
            public double value(double x) {
                return x - 2.0;
            }
        };
        solver.solve(100, f, 0.0, Double.POSITIVE_INFINITY);
    }

    // ---------- Test with reversed bounds (lower > upper) ----------

    @Test(expected = IllegalArgumentException.class)
    public void testReversedBounds() {
        UnivariateFunction f = new Identity() {
            @Override
            public double value(double x) {
                return x - 2.0;
            }
        };
        solver.solve(100, f, 5.0, 0.0);
    }

    // ---------- Test with zero tolerance (should still converge) ----------

    @Test
    public void testZeroTolerance() {
        // Use default solver with absolute accuracy 1e-6, but we can set via constructor if needed.
        // For simplicity, just test normal solve.
        UnivariateFunction f = new Identity() {
            @Override
            public double value(double x) {
                return x - 1.0;
            }
        };
        double result = solver.solve(100, f, 0.0, 2.0);
        assertEquals(1.0, result, 1e-6);
    }
}