package org.apache.commons.math.optimization;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.analysis.sinc;
import org.apache.commons.math.optimization.univariate.BrentOptimizer;
import org.apache.commons.math.optimization.univariate.MultiStartUnivariateRealOptimizer;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test suite for MultiStartUnivariateRealOptimizer.
 * Designed to achieve high coverage and detect potential faults (e.g., bug 67).
 */
public class MultiStartUnivariateRealOptimizerTest {

    private MultiStartUnivariateRealOptimizer optimizer;
    private static final double EPS = 1e-10;
    private static final double TOL = 1e-8;

    @Before
    public void setUp() {
        // Use BrentOptimizer as the underlying optimizer with default convergence checker
        optimizer = new MultiStartUnivariateRealOptimizer(
            new BrentOptimizer(TOL, 1e-14),
            10, // number of starts
            new UniformRealRandomGenerator(-10.0, 10.0)
        );
    }

    // ---------- Basic optimization tests ----------

    @Test
    public void testMinimizeSin() throws FunctionEvaluationException, OptimizationException {
        UnivariateRealFunction f = new sinc();
        optimizer.setMaxEvaluations(1000);
        optimizer.setGoalType(GoalType.MINIMIZE);
        optimizer.setMaximalIterationCount(100);
        UnivariateRealPointValuePair result = optimizer.optimize(f);
        // sin(x) minimum near -pi/2 or 3pi/2, but multiple starts should find global min near -pi/2
        double x = result.getPoint();
        double val = result.getValue();
        // Check that value is close to -1 (sin(-pi/2) = -1)
        assertEquals(-1.0, val, 0.1);
        // Check that point is near -pi/2 (mod 2pi)
        assertTrue(Math.abs(x + Math.PI/2) < 1.0 || Math.abs(x - 3*Math.PI/2) < 1.0);
    }

    @Test
    public void testMaximizeSin() throws FunctionEvaluationException, OptimizationException {
        UnivariateRealFunction f = new sinc();
        optimizer.setMaxEvaluations(1000);
        optimizer.setGoalType(GoalType.MAXIMIZE);
        optimizer.setMaximalIterationCount(100);
        UnivariateRealPointValuePair result = optimizer.optimize(f);
        double x = result.getPoint();
        double val = result.getValue();
        // sin(x) maximum near pi/2
        assertEquals(1.0, val, 0.1);
        assertTrue(Math.abs(x - Math.PI/2) < 1.0 || Math.abs(x + 3*Math.PI/2) < 1.0);
    }

    @Test
    public void testQuadraticMinimize() throws FunctionEvaluationException, OptimizationException {
        // f(x) = x^2 + 1, minimum at 0, value 1
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x + 1.0;
            }
        };
        optimizer.setMaxEvaluations(500);
        optimizer.setGoalType(GoalType.MINIMIZE);
        optimizer.setMaximalIterationCount(100);
        UnivariateRealPointValuePair result = optimizer.optimize(f);
        assertEquals(0.0, result.getPoint(), 0.1);
        assertEquals(1.0, result.getValue(), 0.1);
    }

    // ---------- Edge cases and boundary conditions ----------

    @Test(expected = OptimizationException.class)
    public void testNullFunction() throws FunctionEvaluationException, OptimizationException {
        optimizer.setMaxEvaluations(100);
        optimizer.setGoalType(GoalType.MINIMIZE);
        optimizer.optimize(null);
    }

    @Test(expected = OptimizationException.class)
    public void testNullGoalType() throws FunctionEvaluationException, OptimizationException {
        UnivariateRealFunction f = new sinc();
        optimizer.setMaxEvaluations(100);
        optimizer.setGoalType(null);
        optimizer.optimize(f);
    }

    @Test(expected = OptimizationException.class)
    public void testNegativeMaxEvaluations() throws FunctionEvaluationException, OptimizationException {
        UnivariateRealFunction f = new sinc();
        optimizer.setMaxEvaluations(-1);
        optimizer.setGoalType(GoalType.MINIMIZE);
        optimizer.optimize(f);
    }

    @Test(expected = OptimizationException.class)
    public void testZeroMaxEvaluations() throws FunctionEvaluationException, OptimizationException {
        UnivariateRealFunction f = new sinc();
        optimizer.setMaxEvaluations(0);
        optimizer.setGoalType(GoalType.MINIMIZE);
        optimizer.optimize(f);
    }

    @Test(expected = OptimizationException.class)
    public void testNegativeMaxIterations() throws FunctionEvaluationException, OptimizationException {
        UnivariateRealFunction f = new sinc();
        optimizer.setMaxEvaluations(100);
        optimizer.setGoalType(GoalType.MINIMIZE);
        optimizer.setMaximalIterationCount(-5);
        optimizer.optimize(f);
    }

    @Test(expected = OptimizationException.class)
    public void testZeroStarts() throws FunctionEvaluationException, OptimizationException {
        // Create optimizer with 0 starts
        MultiStartUnivariateRealOptimizer zeroStarts = new MultiStartUnivariateRealOptimizer(
            new BrentOptimizer(TOL, 1e-14),
            0,
            new UniformRealRandomGenerator(-10.0, 10.0)
        );
        zeroStarts.setMaxEvaluations(100);
        zeroStarts.setGoalType(GoalType.MINIMIZE);
        zeroStarts.optimize(new sinc());
    }

    // ---------- Evaluation count and convergence checker ----------

    @Test
    public void testGetEvaluations() throws FunctionEvaluationException, OptimizationException {
        UnivariateRealFunction f = new sinc();
        optimizer.setMaxEvaluations(1000);
        optimizer.setGoalType(GoalType.MINIMIZE);
        optimizer.optimize(f);
        int evals = optimizer.getEvaluations();
        assertTrue("Evaluations should be positive", evals > 0);
        assertTrue("Evaluations should not exceed max", evals <= 1000);
    }

    @Test
    public void testGetConvergenceChecker() {
        assertNotNull(optimizer.getConvergenceChecker());
    }

    @Test
    public void testSetConvergenceChecker() {
        optimizer.setConvergenceChecker(new SimpleUnivariateRealOptimizer.ConvergenceChecker() {
            public boolean converged(int iteration, UnivariateRealPointValuePair previous, UnivariateRealPointValuePair current) {
                return Math.abs(previous.getValue() - current.getValue()) < 1e-6;
            }
        });
        assertNotNull(optimizer.getConvergenceChecker());
    }

    // ---------- Fault detection: bug 67 related tests ----------

    @Test
    public void testMultipleStartsDifferentResults() throws FunctionEvaluationException, OptimizationException {
        // Use a function with multiple local minima: f(x) = x^4 - 10x^2 + 9
        // Local minima at x = -sqrt(5) and x = sqrt(5), global min at both (value -16)
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x * x * x - 10.0 * x * x + 9.0;
            }
        };
        optimizer.setMaxEvaluations(2000);
        optimizer.setGoalType(GoalType.MINIMIZE);
        optimizer.setMaximalIterationCount(200);
        UnivariateRealPointValuePair result = optimizer.optimize(f);
        double val = result.getValue();
        // Global minimum value is -16 at x = +/- sqrt(5) ≈ ±2.236
        assertEquals(-16.0, val, 0.5);
        double x = result.getPoint();
        assertTrue(Math.abs(x - Math.sqrt(5)) < 0.5 || Math.abs(x + Math.sqrt(5)) < 0.5);
    }

    @Test
    public void testConvergenceFailure() throws FunctionEvaluationException, OptimizationException {
        // Use a function that is hard to converge: f(x) = 1/x near 0
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return 1.0 / x;
            }
        };
        optimizer.setMaxEvaluations(100);
        optimizer.setGoalType(GoalType.MINIMIZE);
        optimizer.setMaximalIterationCount(50);
        try {
            optimizer.optimize(f);
            fail("Should have thrown OptimizationException due to divergence");
        } catch (OptimizationException e) {
            // expected
        }
    }

    @Test
    public void testFunctionEvaluationException() {
        // Function that throws exception for certain inputs
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) throws FunctionEvaluationException {
                if (x < 0) {
                    throw new FunctionEvaluationException(x, "Negative x not allowed");
                }
                return x * x;
            }
        };
        optimizer.setMaxEvaluations(100);
        optimizer.setGoalType(GoalType.MINIMIZE);
        optimizer.setMaximalIterationCount(50);
        try {
            optimizer.optimize(f);
            fail("Should have thrown FunctionEvaluationException");
        } catch (FunctionEvaluationException e) {
            // expected
        } catch (OptimizationException e) {
            // also acceptable if wrapped
        }
    }

    // ---------- Additional coverage: getters/setters ----------

    @Test
    public void testGetSetMaxEvaluations() {
        optimizer.setMaxEvaluations(500);
        assertEquals(500, optimizer.getMaxEvaluations());
    }

    @Test
    public void testGetSetGoalType() {
        optimizer.setGoalType(GoalType.MAXIMIZE);
        assertEquals(GoalType.MAXIMIZE, optimizer.getGoalType());
    }

    @Test
    public void testGetSetMaximalIterationCount() {
        optimizer.setMaximalIterationCount(200);
        assertEquals(200, optimizer.getMaximalIterationCount());
    }

    @Test
    public void testGetEvaluationsBeforeOptimize() {
        assertEquals(0, optimizer.getEvaluations());
    }

    // ---------- Helper inner class for sinc ----------
    private static class sinc implements UnivariateRealFunction {
        public double value(double x) {
            if (Math.abs(x) < 1e-15) {
                return 1.0;
            }
            return Math.sin(x) / x;
        }
    }

    // Simple random generator for uniform distribution (used in constructor)
    private static class UniformRealRandomGenerator implements RandomGenerator {
        private final double lower;
        private final double upper;
        private final java.util.Random rand = new java.util.Random();

        public UniformRealRandomGenerator(double lower, double upper) {
            this.lower = lower;
            this.upper = upper;
        }

        public double nextDouble() {
            return lower + (upper - lower) * rand.nextDouble();
        }
    }
}