package org.apache.commons.math.optimization.univariate;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.ConvergenceChecker;
import org.apache.commons.math.optimization.univariate.BrentOptimizer;
import org.apache.commons.math.optimization.univariate.UnivariateRealFunction;
import org.apache.commons.math.optimization.univariate.UnivariateRealPointValuePair;
import org.apache.commons.math.exception.TooManyEvaluationsException;
import org.apache.commons.math.exception.MathIllegalArgumentException;

/**
 * Test suite for BrentOptimizer, targeting high coverage and fault detection.
 */
public class BrentOptimizerTest {

    private BrentOptimizer optimizer;
    private static final double EPS = 1e-10;

    @Before
    public void setUp() {
        // Default optimizer with absolute tolerance 1e-10 and relative tolerance 1e-14
        optimizer = new BrentOptimizer(1e-10, 1e-14);
    }

    // ---------- Basic optimization tests ----------

    @Test
    public void testSimpleQuadratic() {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return (x - 2.0) * (x - 2.0);
            }
        };
        UnivariateRealPointValuePair result = optimizer.optimize(100, f, GoalType.MINIMIZE, -10.0, 10.0);
        assertEquals(2.0, result.getPoint(), 1e-8);
        assertEquals(0.0, result.getValue(), 1e-8);
    }

    @Test
    public void testSimpleQuadraticMaximize() {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return -(x - 2.0) * (x - 2.0) + 5.0;
            }
        };
        UnivariateRealPointValuePair result = optimizer.optimize(100, f, GoalType.MAXIMIZE, -10.0, 10.0);
        assertEquals(2.0, result.getPoint(), 1e-8);
        assertEquals(5.0, result.getValue(), 1e-8);
    }

    @Test
    public void testLinearFunction() {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return 3.0 * x + 2.0;
            }
        };
        // Minimizing a linear function over [0,10] should give left endpoint
        UnivariateRealPointValuePair result = optimizer.optimize(100, f, GoalType.MINIMIZE, 0.0, 10.0);
        assertEquals(0.0, result.getPoint(), 1e-8);
        assertEquals(2.0, result.getValue(), 1e-8);
    }

    @Test
    public void testLinearFunctionMaximize() {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return 3.0 * x + 2.0;
            }
        };
        UnivariateRealPointValuePair result = optimizer.optimize(100, f, GoalType.MAXIMIZE, 0.0, 10.0);
        assertEquals(10.0, result.getPoint(), 1e-8);
        assertEquals(32.0, result.getValue(), 1e-8);
    }

    // ---------- Constant function (bug trigger) ----------

    @Test
    public void testConstantFunction() {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return 5.0;
            }
        };
        // Should converge to some point within the interval
        UnivariateRealPointValuePair result = optimizer.optimize(100, f, GoalType.MINIMIZE, -5.0, 5.0);
        assertTrue(result.getPoint() >= -5.0 && result.getPoint() <= 5.0);
        assertEquals(5.0, result.getValue(), 1e-8);
    }

    @Test
    public void testConstantFunctionMaximize() {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return -3.0;
            }
        };
        UnivariateRealPointValuePair result = optimizer.optimize(100, f, GoalType.MAXIMIZE, 0.0, 1.0);
        assertTrue(result.getPoint() >= 0.0 && result.getPoint() <= 1.0);
        assertEquals(-3.0, result.getValue(), 1e-8);
    }

    // ---------- Edge cases: bounds ----------

    @Test(expected = MathIllegalArgumentException.class)
    public void testMinGreaterThanMax() {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x;
            }
        };
        optimizer.optimize(100, f, GoalType.MINIMIZE, 10.0, -10.0);
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testMinEqualsMax() {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x;
            }
        };
        optimizer.optimize(100, f, GoalType.MINIMIZE, 5.0, 5.0);
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testNullFunction() {
        optimizer.optimize(100, null, GoalType.MINIMIZE, -1.0, 1.0);
    }

    // ---------- Convergence with very small tolerance ----------

    @Test
    public void testTightTolerance() {
        BrentOptimizer tight = new BrentOptimizer(1e-12, 1e-16);
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return (x - 1.23456789) * (x - 1.23456789);
            }
        };
        UnivariateRealPointValuePair result = tight.optimize(200, f, GoalType.MINIMIZE, -10.0, 10.0);
        assertEquals(1.23456789, result.getPoint(), 1e-10);
    }

    // ---------- Multiple minima (should find global within bounds) ----------

    @Test
    public void testMultipleMinima() {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return Math.sin(x);
            }
        };
        // Minimize sin(x) over [0, 2*PI] -> minimum at 3*PI/2
        UnivariateRealPointValuePair result = optimizer.optimize(100, f, GoalType.MINIMIZE, 0.0, 2.0 * Math.PI);
        assertEquals(3.0 * Math.PI / 2.0, result.getPoint(), 1e-5);
        assertEquals(-1.0, result.getValue(), 1e-5);
    }

    // ---------- Evaluations and iterations ----------

    @Test
    public void testEvaluationsCount() {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return (x - 3.0) * (x - 3.0);
            }
        };
        optimizer.optimize(100, f, GoalType.MINIMIZE, 0.0, 10.0);
        int evals = optimizer.getEvaluations();
        assertTrue("Evaluations should be positive", evals > 0);
        assertTrue("Evaluations should not exceed max", evals <= 100);
    }

    @Test
    public void testIterationsCount() {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return (x - 3.0) * (x - 3.0);
            }
        };
        optimizer.optimize(100, f, GoalType.MINIMIZE, 0.0, 10.0);
        int iters = optimizer.getIterations();
        assertTrue("Iterations should be non-negative", iters >= 0);
    }

    // ---------- Too many evaluations ----------

    @Test(expected = TooManyEvaluationsException.class)
    public void testTooManyEvaluations() {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                // Very flat function to force many iterations
                return 1e-12 * (x - 1000.0) * (x - 1000.0) + 1.0;
            }
        };
        // Use very tight tolerance and low max evaluations
        BrentOptimizer tight = new BrentOptimizer(1e-15, 1e-20);
        tight.optimize(5, f, GoalType.MINIMIZE, -1000.0, 1000.0);
    }

    // ---------- Function with zero at minimum ----------

    @Test
    public void testZeroAtMinimum() {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return (x + 5.0) * (x + 5.0);
            }
        };
        UnivariateRealPointValuePair result = optimizer.optimize(100, f, GoalType.MINIMIZE, -10.0, 0.0);
        assertEquals(-5.0, result.getPoint(), 1e-8);
        assertEquals(0.0, result.getValue(), 1e-8);
    }

    // ---------- Function with very small range ----------

    @Test
    public void testSmallRange() {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return (x - 1e-5) * (x - 1e-5);
            }
        };
        UnivariateRealPointValuePair result = optimizer.optimize(100, f, GoalType.MINIMIZE, -1e-4, 1e-4);
        assertEquals(1e-5, result.getPoint(), 1e-8);
        assertEquals(0.0, result.getValue(), 1e-12);
    }

    // ---------- Function with large range ----------

    @Test
    public void testLargeRange() {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return (x - 1e6) * (x - 1e6);
            }
        };
        UnivariateRealPointValuePair result = optimizer.optimize(200, f, GoalType.MINIMIZE, -1e7, 1e7);
        assertEquals(1e6, result.getPoint(), 1e-3);
        assertEquals(0.0, result.getValue(), 1e-6);
    }

    // ---------- Function with NaN or infinite? Not required but safe ----------

    @Test
    public void testFunctionWithNaN() {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                if (x == 0.0) return Double.NaN;
                return x * x;
            }
        };
        // Should avoid NaN and converge to a valid point
        UnivariateRealPointValuePair result = optimizer.optimize(100, f, GoalType.MINIMIZE, -10.0, 10.0);
        assertFalse(Double.isNaN(result.getPoint()));
        assertFalse(Double.isNaN(result.getValue()));
    }

    // ---------- Test with custom convergence checker ----------

    @Test
    public void testCustomConvergenceChecker() {
        ConvergenceChecker<UnivariateRealPointValuePair> checker = 
            new ConvergenceChecker<UnivariateRealPointValuePair>() {
                public boolean converged(int iteration, UnivariateRealPointValuePair previous, 
                                         UnivariateRealPointValuePair current) {
                    // Stop when relative change in value is less than 1e-6
                    double prevVal = previous.getValue();
                    double currVal = current.getValue();
                    double delta = Math.abs(currVal - prevVal);
                    double absPrev = Math.abs(prevVal);
                    return delta <= 1e-6 * Math.max(1.0, absPrev);
                }
            };
        BrentOptimizer custom = new BrentOptimizer(checker, 1e-10, 1e-14);
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return (x - 3.0) * (x - 3.0);
            }
        };
        UnivariateRealPointValuePair result = custom.optimize(100, f, GoalType.MINIMIZE, 0.0, 10.0);
        assertEquals(3.0, result.getPoint(), 1e-6);
    }

    // ---------- Test that optimizer returns best point even if not converged ----------

    @Test
    public void testBestPointOnMaxEvaluations() {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return (x - 1000.0) * (x - 1000.0);
            }
        };
        // Use very few evaluations to force early stop
        BrentOptimizer limited = new BrentOptimizer(1e-10, 1e-14);
        UnivariateRealPointValuePair result = limited.optimize(10, f, GoalType.MINIMIZE, -10000.0, 10000.0);
        // Should return some point within bounds
        assertNotNull(result);
        assertTrue(result.getPoint() >= -10000.0 && result.getPoint() <= 10000.0);
    }

    // ---------- Test with negative bounds ----------

    @Test
    public void testNegativeBounds() {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return (x + 7.0) * (x + 7.0);
            }
        };
        UnivariateRealPointValuePair result = optimizer.optimize(100, f, GoalType.MINIMIZE, -20.0, -5.0);
        assertEquals(-7.0, result.getPoint(), 1e-8);
        assertEquals(0.0, result.getValue(), 1e-8);
    }

    // ---------- Test with zero tolerance (should still converge) ----------

    @Test
    public void testZeroTolerance() {
        BrentOptimizer zeroTol = new BrentOptimizer(0.0, 0.0);
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return (x - 1.0) * (x - 1.0);
            }
        };
        UnivariateRealPointValuePair result = zeroTol.optimize(100, f, GoalType.MINIMIZE, -10.0, 10.0);
        assertEquals(1.0, result.getPoint(), 1e-8);
    }
}