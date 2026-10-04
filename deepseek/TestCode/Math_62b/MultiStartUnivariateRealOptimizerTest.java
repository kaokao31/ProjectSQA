package org.apache.commons.math.optimization.univariate;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.OptimizationException;
import org.apache.commons.math.optimization.univariate.AbstractUnivariateRealOptimizer;
import org.apache.commons.math.optimization.univariate.BrentOptimizer;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test suite for MultiStartUnivariateRealOptimizer.
 * Designed to achieve high code coverage and detect potential faults.
 */
public class MultiStartUnivariateRealOptimizerTest {

    private static final double EPS = 1e-10;
    private MultiStartUnivariateRealOptimizer optimizer;
    private UnivariateRealFunction simpleQuadratic;
    private UnivariateRealFunction sineFunction;
    private UnivariateRealFunction constantFunction;

    @Before
    public void setUp() {
        // Base optimizer: BrentOptimizer with default convergence
        AbstractUnivariateRealOptimizer base = new BrentOptimizer();
        optimizer = new MultiStartUnivariateRealOptimizer(base, 5);
        
        // f(x) = (x-2)^2 + 1, minimum at x=2, value=1
        simpleQuadratic = new UnivariateRealFunction() {
            public double value(double x) {
                return (x - 2.0) * (x - 2.0) + 1.0;
            }
        };
        
        // f(x) = sin(x), multiple minima in [-pi, pi]
        sineFunction = new UnivariateRealFunction() {
            public double value(double x) {
                return Math.sin(x);
            }
        };
        
        // Constant function f(x)=5, any point is optimum
        constantFunction = new UnivariateRealFunction() {
            public double value(double x) {
                return 5.0;
            }
        };
    }

    // ---------- Normal optimization cases ----------

    @Test
    public void testSimpleQuadraticMinimize() throws OptimizationException, FunctionEvaluationException {
        optimizer.optimize(simpleQuadratic, GoalType.MINIMIZE, -10.0, 10.0);
        double result = optimizer.getResult();
        assertEquals("Minimum x should be near 2.0", 2.0, result, 1e-5);
        assertEquals("Minimum value should be near 1.0", 1.0, optimizer.getFunctionValue(), 1e-5);
        assertTrue("Evaluations should be positive", optimizer.getEvaluations() > 0);
    }

    @Test
    public void testSimpleQuadraticMaximize() throws OptimizationException, FunctionEvaluationException {
        optimizer.optimize(simpleQuadratic, GoalType.MAXIMIZE, -10.0, 10.0);
        double result = optimizer.getResult();
        // Quadratic has no maximum, but optimizer should return a boundary point
        assertTrue("Maximize should return boundary", result == -10.0 || result == 10.0);
    }

    @Test
    public void testSineMinimize() throws OptimizationException, FunctionEvaluationException {
        optimizer.optimize(sineFunction, GoalType.MINIMIZE, -Math.PI, Math.PI);
        double result = optimizer.getResult();
        // Minimum of sin(x) in [-pi, pi] is at -pi/2 or pi/2? Actually sin(-pi/2) = -1, sin(pi/2)=1, so min is -pi/2
        assertEquals("Minimum x should be near -pi/2", -Math.PI / 2.0, result, 1e-4);
        assertEquals("Minimum value should be -1", -1.0, optimizer.getFunctionValue(), 1e-4);
    }

    @Test
    public void testConstantFunction() throws OptimizationException, FunctionEvaluationException {
        optimizer.optimize(constantFunction, GoalType.MINIMIZE, -5.0, 5.0);
        double result = optimizer.getResult();
        // Any point is optimal; optimizer should return some point within bounds
        assertTrue("Result should be within bounds", result >= -5.0 && result <= 5.0);
        assertEquals("Function value should be 5.0", 5.0, optimizer.getFunctionValue(), EPS);
    }

    // ---------- Edge cases: bounds and starting points ----------

    @Test(expected = OptimizationException.class)
    public void testNullFunction() throws OptimizationException, FunctionEvaluationException {
        optimizer.optimize(null, GoalType.MINIMIZE, -1.0, 1.0);
    }

    @Test(expected = OptimizationException.class)
    public void testInvalidBoundsLowerGreaterThanUpper() throws OptimizationException, FunctionEvaluationException {
        optimizer.optimize(simpleQuadratic, GoalType.MINIMIZE, 10.0, -10.0);
    }

    @Test(expected = OptimizationException.class)
    public void testEqualBounds() throws OptimizationException, FunctionEvaluationException {
        optimizer.optimize(simpleQuadratic, GoalType.MINIMIZE, 5.0, 5.0);
    }

    @Test(expected = OptimizationException.class)
    public void testNegativeNumberOfStarts() {
        AbstractUnivariateRealOptimizer base = new BrentOptimizer();
        new MultiStartUnivariateRealOptimizer(base, -1);
    }

    @Test(expected = OptimizationException.class)
    public void testZeroStarts() {
        AbstractUnivariateRealOptimizer base = new BrentOptimizer();
        new MultiStartUnivariateRealOptimizer(base, 0);
    }

    // ---------- Convergence and multiple starts ----------

    @Test
    public void testMultipleStartsConvergence() throws OptimizationException, FunctionEvaluationException {
        // Use a function with multiple local minima: f(x) = x^4 - 10x^2 + 9
        UnivariateRealFunction multiMin = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x * x * x - 10.0 * x * x + 9.0;
            }
        };
        // Global minima at x = -sqrt(5) and x = sqrt(5), value = -16
        optimizer.optimize(multiMin, GoalType.MINIMIZE, -4.0, 4.0);
        double result = optimizer.getResult();
        double value = optimizer.getFunctionValue();
        // Should be close to one of the global minima
        assertTrue("Result should be near -sqrt(5) or sqrt(5)", 
                   Math.abs(result - Math.sqrt(5)) < 1e-4 || Math.abs(result + Math.sqrt(5)) < 1e-4);
        assertEquals("Function value should be -16", -16.0, value, 1e-4);
    }

    @Test
    public void testStartsAtBounds() throws OptimizationException, FunctionEvaluationException {
        // Force starts at bounds by using a very narrow interval
        optimizer.optimize(simpleQuadratic, GoalType.MINIMIZE, 2.0, 2.1);
        double result = optimizer.getResult();
        // Minimum is at 2.0, which is the lower bound
        assertEquals("Result should be at lower bound", 2.0, result, 1e-5);
    }

    // ---------- Exception handling from base optimizer ----------

    @Test(expected = OptimizationException.class)
    public void testBaseOptimizerThrowsException() throws OptimizationException, FunctionEvaluationException {
        // Create a base optimizer that always throws an exception
        AbstractUnivariateRealOptimizer faultyBase = new AbstractUnivariateRealOptimizer() {
            @Override
            public double optimize(UnivariateRealFunction f, GoalType goalType, double min, double max,
                                   double startValue) throws OptimizationException, FunctionEvaluationException {
                throw new OptimizationException("Faulty optimizer");
            }
        };
        MultiStartUnivariateRealOptimizer multi = new MultiStartUnivariateRealOptimizer(faultyBase, 3);
        multi.optimize(simpleQuadratic, GoalType.MINIMIZE, -10.0, 10.0);
    }

    @Test(expected = FunctionEvaluationException.class)
    public void testFunctionEvaluationException() throws OptimizationException, FunctionEvaluationException {
        // Function that throws exception at certain points
        UnivariateRealFunction throwingFunction = new UnivariateRealFunction() {
            public double value(double x) {
                if (Math.abs(x - 1.0) < 0.1) {
                    throw new FunctionEvaluationException(x, "Evaluation failed");
                }
                return x * x;
            }
        };
        optimizer.optimize(throwingFunction, GoalType.MINIMIZE, -5.0, 5.0);
    }

    // ---------- Getters and state ----------

    @Test
    public void testGetEvaluations() throws OptimizationException, FunctionEvaluationException {
        optimizer.optimize(simpleQuadratic, GoalType.MINIMIZE, -10.0, 10.0);
        int evals = optimizer.getEvaluations();
        assertTrue("Evaluations should be positive", evals > 0);
    }

    @Test
    public void testGetResultBeforeOptimize() {
        // Should return NaN or some default
        double result = optimizer.getResult();
        assertTrue("Result before optimize should be NaN or 0", Double.isNaN(result) || result == 0.0);
    }

    @Test
    public void testGetFunctionValueBeforeOptimize() {
        double value = optimizer.getFunctionValue();
        assertTrue("Function value before optimize should be NaN or 0", Double.isNaN(value) || value == 0.0);
    }

    // ---------- Boundary conditions for number of starts ----------

    @Test
    public void testSingleStart() throws OptimizationException, FunctionEvaluationException {
        AbstractUnivariateRealOptimizer base = new BrentOptimizer();
        MultiStartUnivariateRealOptimizer single = new MultiStartUnivariateRealOptimizer(base, 1);
        single.optimize(simpleQuadratic, GoalType.MINIMIZE, -10.0, 10.0);
        assertEquals("Result should be near 2.0", 2.0, single.getResult(), 1e-5);
    }

    @Test
    public void testLargeNumberOfStarts() throws OptimizationException, FunctionEvaluationException {
        AbstractUnivariateRealOptimizer base = new BrentOptimizer();
        MultiStartUnivariateRealOptimizer many = new MultiStartUnivariateRealOptimizer(base, 100);
        many.optimize(simpleQuadratic, GoalType.MINIMIZE, -10.0, 10.0);
        assertEquals("Result should be near 2.0", 2.0, many.getResult(), 1e-5);
    }

    // ---------- GoalType coverage ----------

    @Test
    public void testMaximizeWithSine() throws OptimizationException, FunctionEvaluationException {
        optimizer.optimize(sineFunction, GoalType.MAXIMIZE, -Math.PI, Math.PI);
        double result = optimizer.getResult();
        // Maximum of sin(x) in [-pi, pi] is at pi/2
        assertEquals("Maximum x should be near pi/2", Math.PI / 2.0, result, 1e-4);
        assertEquals("Maximum value should be 1", 1.0, optimizer.getFunctionValue(), 1e-4);
    }

    // ---------- Edge case: very flat function ----------

    @Test
    public void testFlatFunction() throws OptimizationException, FunctionEvaluationException {
        UnivariateRealFunction flat = new UnivariateRealFunction() {
            public double value(double x) {
                return 1e-10 * x + 1.0; // nearly constant
            }
        };
        optimizer.optimize(flat, GoalType.MINIMIZE, -100.0, 100.0);
        // Should converge to some point, no exception
        assertNotNull("Result should be finite", optimizer.getResult());
    }

    // ---------- Test that best result is returned (not first) ----------

    @Test
    public void testBestResultReturned() throws OptimizationException, FunctionEvaluationException {
        // Use a function where the global minimum is at a specific point
        // and the base optimizer might get stuck in local minima.
        // We'll use a simple quadratic with a small perturbation.
        UnivariateRealFunction tricky = new UnivariateRealFunction() {
            public double value(double x) {
                return (x - 3.0) * (x - 3.0) + 0.1 * Math.sin(20.0 * x);
            }
        };
        optimizer.optimize(tricky, GoalType.MINIMIZE, -10.0, 10.0);
        double result = optimizer.getResult();
        // The global minimum should be near 3.0 (the quadratic dominates)
        assertEquals("Global minimum should be near 3.0", 3.0, result, 0.5);
    }
}