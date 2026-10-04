package org.apache.commons.math.analysis.solvers;

import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.analysis.SinFunction;
import org.apache.commons.math.analysis.Expm1Function;
import org.apache.commons.math.analysis.MonitoredFunction;
import org.apache.commons.math.exception.MaxIterationsExceededException;
import org.apache.commons.math.ConvergenceException;
import org.junit.Assert;
import org.junit.Test;

public class BrentSolverTest {

    @Test
    public void testSinSolved() throws ConvergenceException {
        UnivariateRealFunction f = new SinFunction();
        UnivariateRealFunction solver = new BrentSolver();
        double pi = Math.PI;
        double result = solver.solve(f, 3.0, 4.0);
        Assert.assertEquals(pi, result, 1E-11);
    }

    @Test
    public void testSolveBadEndpoints() throws ConvergenceException {
        UnivariateRealFunction f = new SinFunction();
        BrentSolver solver = new BrentSolver();
        try {
            solver.solve(f, 1.0, 2.0, 1.5);
            Assert.fail("Expecting ConvergenceException or IllegalArgumentException");
        } catch (Exception e) {
            // Expected because f(1) and f(2) have the same sign (both positive)
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testSolveNullFunction() {
        BrentSolver solver = new BrentSolver();
        try {
            solver.solve(null, 0.0, 1.0);
            Assert.fail("Expecting NullPointerException or IllegalArgumentException");
        } catch (Exception e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testSolvedAtInitialGuess() throws ConvergenceException {
        UnivariateRealFunction f = new SinFunction();
        BrentSolver solver = new BrentSolver();
        double result = solver.solve(f, Math.PI, 4.0, Math.PI);
        Assert.assertEquals(Math.PI, result, 1E-11);
    }

    @Test
    public void testSolveFunctionNullCheck() {
        BrentSolver solver = new BrentSolver();
        try {
            solver.solve(null, 0.0, 1.0, 0.5);
            Assert.fail("Expected exception for null function");
        } catch (Exception e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testCaseDfltBrentBug72() throws ConvergenceException {
        // Specific scenario often targeting Math-72 where solve(f, min, max) 
        // does not verify endpoint signs or fails to return the correct endpoint when 
        // initial/endpoints coincide or function is already zero at boundaries.
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                if (x < 1.0) {
                    return x - 1.0;
                } else if (x > 1.0) {
                    return x - 1.0;
                }
                return 0.0;
            }
        };

        BrentSolver solver = new BrentSolver();
        double y = solver.solve(f, 0.5, 1.5, 1.2);
        Assert.assertEquals(1.0, y, 1E-11);
    }

    @Test
    public void testBoundaries() throws ConvergenceException {
        UnivariateRealFunction f = new SinFunction();
        BrentSolver solver = new BrentSolver();
        // root is at Math.PI
        double result = solver.solve(f, 3.0, 3.2, 3.1);
        Assert.assertEquals(Math.PI, result, 1E-6);
    }
    
    @Test(expected = MaxIterationsExceededException.class)
    public void testMaxIterationsExceeded() throws ConvergenceException {
        UnivariateRealFunction f = new SinFunction();
        BrentSolver solver = new BrentSolver();
        solver.setMaximalIterationCount(0);
        solver.solve(f, 3.0, 4.0);
    }
}