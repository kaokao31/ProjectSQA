package org.apache.commons.math.analysis.solvers;

import org.apache.commons.math.analysis.UnivariateFunction;
import org.junit.Test;
import static org.junit.Assert.*;

public class BracketingNthOrderBrentSolverTest {

    private static final double EPS = 1e-10;

    private BracketingNthOrderBrentSolver createSolver() {
        return new BracketingNthOrderBrentSolver();
    }

    private BracketingNthOrderBrentSolver createSolver(double absoluteAccuracy, double relativeAccuracy) {
        return new BracketingNthOrderBrentSolver(relativeAccuracy, absoluteAccuracy);
    }

    private BracketingNthOrderBrentSolver createSolver(double absoluteAccuracy, double relativeAccuracy, double functionValueAccuracy) {
        return new BracketingNthOrderBrentSolver(relativeAccuracy, absoluteAccuracy, functionValueAccuracy);
    }

    private BracketingNthOrderBrentSolver createSolver(int maxEval, double absoluteAccuracy, double relativeAccuracy, double functionValueAccuracy) {
        return new BracketingNthOrderBrentSolver(maxEval, absoluteAccuracy, relativeAccuracy, functionValueAccuracy);
    }

    @Test
    public void testLinearRootPositiveSlope() {
        BracketingNthOrderBrentSolver solver = createSolver();
        UnivariateFunction f = x -> 2.0 * x - 4.0;  // root at x=2
        double root = solver.solve(100, f, -10, 10);
        assertEquals(2.0, root, 1e-6);
    }

    @Test
    public void testLinearRootNegativeSlope() {
        BracketingNthOrderBrentSolver solver = createSolver();
        UnivariateFunction f = x -> -2.0 * x + 4.0;  // root at x=2
        double root = solver.solve(100, f, -10, 10);
        assertEquals(2.0, root, 1e-6);
    }

    @Test
    public void testQuadraticRoots() {
        BracketingNthOrderBrentSolver solver = createSolver();
        UnivariateFunction f = x -> (x - 1.0) * (x - 3.0);  // roots at 1 and 3
        double root = solver.solve(100, f, 0, 2);
        assertEquals(1.0, root, 1e-6);
    }

    @Test
    public void testCubicRoot() {
        BracketingNthOrderBrentSolver solver = createSolver();
        UnivariateFunction f = x -> x * x * x - 8;  // root at x=2
        double root = solver.solve(100, f, 0, 5);
        assertEquals(2.0, root, 1e-6);
    }

    @Test
    public void testRootAtLeftBoundary() {
        BracketingNthOrderBrentSolver solver = createSolver();
        UnivariateFunction f = x -> x - 1.0;  // root at x=1
        double root = solver.solve(100, f, 1, 2);
        assertEquals(1.0, root, 1e-6);
    }

    @Test
    public void testRootAtRightBoundary() {
        BracketingNthOrderBrentSolver solver = createSolver();
        UnivariateFunction f = x -> x - 1.0;  // root at x=1
        double root = solver.solve(100, f, 0, 1);
        assertEquals(1.0, root, 1e-6);
    }

    @Test
    public void testStartValueProvided() {
        BracketingNthOrderBrentSolver solver = createSolver();
        UnivariateFunction f = x -> Math.sin(x);  // root at 0
        double root = solver.solve(100, f, -1, 3, 0.5);
        assertEquals(0.0, root, 1e-6);
    }

    @Test
    public void testNoBracketingException() {
        BracketingNthOrderBrentSolver solver = createSolver();
        UnivariateFunction f = x -> x * x + 1;  // always positive
        try {
            solver.solve(100, f, -1, 1);
            fail("Expected NoBracketingException");
        } catch (org.apache.commons.math.exception.NoBracketingException e) {
            // expected
        }
    }

    @Test
    public void testTooManyEvaluations() {
        BracketingNthOrderBrentSolver solver = createSolver();
        UnivariateFunction f = x -> Math.sin(x);
        try {
            solver.solve(2, f, -1, 3);  // too few max evaluations
            fail("Expected TooManyEvaluationsException");
        } catch (org.apache.commons.math.exception.TooManyEvaluationsException e) {
            // expected
        }
    }

    @Test
    public void testInvalidInterval() {
        BracketingNthOrderBrentSolver solver = createSolver();
        UnivariateFunction f = x -> x;
        try {
            solver.solve(100, f, 5, -5);
            fail("Expected NumberIsTooLargeException");
        } catch (org.apache.commons.math.exception.NumberIsTooLargeException e) {
            // expected
        }
    }

    @Test
    public void testFunctionValueAccuracy() {
        BracketingNthOrderBrentSolver solver = createSolver(1e-12, 1e-15, 1e-12);
        UnivariateFunction f = x -> x * x - 2;  // root sqrt(2)
        double root = solver.solve(100, f, 0, 2);
        assertEquals(Math.sqrt(2), root, 1e-8);
    }

    @Test
    public void testFlatFunctionWithDoubleRoot() {
        BracketingNthOrderBrentSolver solver = createSolver();
        UnivariateFunction f = x -> (x - 0.5) * (x - 0.5);  // double root at 0.5
        double root = solver.solve(100, f, 0, 1);
        assertEquals(0.5, root, 1e-6);
    }

    @Test
    public void testHighOrderConvergence() {
        BracketingNthOrderBrentSolver solver = createSolver(1e-10, 1e-15, 1e-10);
        UnivariateFunction f = x -> (x - 3.0) * (x - 3.0) * (x - 3.0);  // triple root
        double root = solver.solve(100, f, 0, 5);
        assertEquals(3.0, root, 1e-6);
    }

    @Test
    public void testMaxOrderIsFiveByDefault() {
        BracketingNthOrderBrentSolver solver = createSolver();
        assertEquals(5, solver.getMaximalOrder());
    }

    @Test
    public void testCustomConstructorWithOrder() {
        BracketingNthOrderBrentSolver solver = new BracketingNthOrderBrentSolver(1e-6, 1e-12, 1e-10, 5);
        assertEquals(5, solver.getMaximalOrder());
    }

    @Test
    public void testGetEvaluations() {
        BracketingNthOrderBrentSolver solver = createSolver();
        UnivariateFunction f = x -> x - 1;
        double root = solver.solve(100, f, -5, 5);
        assertTrue(solver.getEvaluations() > 0);
        assertEquals(1.0, root, 1e-6);
    }

    @Test
    public void testFunctionWithLocalFlatRegion() {
        BracketingNthOrderBrentSolver solver = createSolver();
        UnivariateFunction f = x -> {
            // function with a flat region near 0 but still a root
            if (x > -0.1 && x < 0.1) return 0.0;
            return x;
        };
        double root = solver.solve(100, f, -1, 1);
        assertEquals(0.0, root, 1e-6);
    }

    @Test
    public void testSolverWithMaxEvaluations() {
        BracketingNthOrderBrentSolver solver = createSolver(1000, 1e-10, 1e-15, 1e-10);
        UnivariateFunction f = x -> Math.cos(x) - x;  // root ~0.739
        double root = solver.solve(1000, f, 0, 1);
        assertEquals(0.7390851332, root, 1e-8);
    }

    @Test
    public void testDefaultAbsoluteAccuracy() {
        BracketingNthOrderBrentSolver solver = createSolver();
        assertTrue(solver.getAbsoluteAccuracy() > 0);
    }

    @Test
    public void testRelativeAccuracy() {
        BracketingNthOrderBrentSolver solver = createSolver(1e-6, 1e-12, 1e-10);
        assertTrue(solver.getRelativeAccuracy() > 0);
    }

    @Test
    public void testFunctionValueAccuracyGetter() {
        BracketingNthOrderBrentSolver solver = createSolver(1e-6, 1e-12, 1e-10);
        assertEquals(1e-10, solver.getFunctionValueAccuracy(), 1e-15);
    }

    @Test
    public void testZeroEvaluationCountOnCreation() {
        BracketingNthOrderBrentSolver solver = createSolver();
        assertEquals(0, solver.getEvaluations());
    }

    @Test
    public void testSinRootNearPi() {
        BracketingNthOrderBrentSolver solver = createSolver();
        UnivariateFunction f = x -> Math.sin(x);
        double root = solver.solve(100, f, 2, 4);
        assertEquals(Math.PI, root, 1e-6);
    }
}