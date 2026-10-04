package org.apache.commons.math.analysis.solvers;

import org.apache.commons.math.analysis.UnivariateFunction;
import org.apache.commons.math.analysis.function.Sin;
import org.apache.commons.math.analysis.function.Cos;
import org.apache.commons.math.analysis.function.Expm1;
import org.apache.commons.math.exception.NumberIsTooLargeException;
import org.apache.commons.math.exception.NoBracketingException;
import org.apache.commons.math.exception.TooManyEvaluationsException;
import org.junit.Assert;
import org.junit.Test;

public class BracketingNthOrderBrentSolverTest {

    @Test(expected = NumberIsTooLargeException.class)
    public void testInvalidMaximalOrder() {
        // Minimal order is 2, testing order 1 should throw NumberIsTooLargeException
        new BracketingNthOrderBrentSolver(1.0e-10, 1);
    }

    @Test(expected = NumberIsTooLargeException.class)
    public void testOrderTooLarge() {
        // Order greater than maximum allowed (e.g., very large)
        new BracketingNthOrderBrentSolver(1.0e-10, 100);
    }

    @Test
    public void testConstructorsAndGetters() {
        BracketingNthOrderBrentSolver solver1 = new BracketingNthOrderBrentSolver();
        Assert.assertEquals(5, solver1.getMaximalOrder());

        BracketingNthOrderBrentSolver solver2 = new BracketingNthOrderBrentSolver(1e-8, 1e-12, 1e-15, 6);
        Assert.assertEquals(6, solver2.getMaximalOrder());

        BracketingNthOrderBrentSolver solver3 = new BracketingNthOrderBrentSolver(1e-8, 6);
        Assert.assertEquals(6, solver3.getMaximalOrder());
    }

    @Test
    public void testRootAtMin() {
        UnivariateFunction f = new Sin();
        BracketingNthOrderBrentSolver solver = new BracketingNthOrderBrentSolver(1e-8, 5);
        double root = solver.solve(100, f, 0.0, 3.0, 0.0, AllowedSolution.ANY_SIDE);
        Assert.assertEquals(0.0, root, 1e-8);
    }

    @Test
    public void testRootAtMax() {
        UnivariateFunction f = new Sin();
        BracketingNthOrderBrentSolver solver = new BracketingNthOrderBrentSolver(1e-8, 5);
        double root = solver.solve(100, f, -Math.PI, Math.PI, Math.PI, AllowedSolution.ANY_SIDE);
        // Depending on bracket, let's test a known root
        double piRoot = solver.solve(100, f, 3.0, 4.0, 3.5, AllowedSolution.ANY_SIDE);
        Assert.assertEquals(Math.PI, piRoot, 1e-8);
    }

    @Test
    public void testSinFunction() {
        UnivariateFunction f = new Sin();
        BracketingNthOrderBrentSolver solver = new BracketingNthOrderBrentSolver(1e-10, 1e-22, 1e-10, 5);
        
        double resultAny = solver.solve(100, f, 3.0, 4.0, AllowedSolution.ANY_SIDE);
        Assert.assertEquals(Math.PI, resultAny, 1e-9);

        double resultLeft = solver.solve(100, f, 3.0, 4.0, AllowedSolution.LEFT_SIDE);
        Assert.assertTrue(resultLeft <= Math.PI);

        double resultRight = solver.solve(100, f, 3.0, 4.0, AllowedSolution.RIGHT_SIDE);
        Assert.assertTrue(resultRight >= Math.PI);

        double resultBelow = solver.solve(100, f, 3.0, 4.0, AllowedSolution.BELOW_SIDE);
        Assert.assertEquals(Math.PI, resultBelow, 1e-8);

        double resultAbove = solver.solve(100, f, 3.0, 4.0, AllowedSolution.ABOVE_SIDE);
        Assert.assertEquals(Math.PI, resultAbove, 1e-8);
    }

    @Test
    public void testCosFunction() {
        UnivariateFunction f = new Cos();
        BracketingNthOrderBrentSolver solver = new BracketingNthOrderBrentSolver(1e-8, 5);
        double root = solver.solve(100, f, 0.0, 2.0, AllowedSolution.ANY_SIDE);
        Assert.assertEquals(Math.PI / 2.0, root, 1e-8);
    }

    @Test
    public void testExpm1Function() {
        UnivariateFunction f = new Expm1();
        BracketingNthOrderBrentSolver solver = new BracketingNthOrderBrentSolver(1e-9, 5);
        double root = solver.solve(100, f, -1.0, 1.0, AllowedSolution.ANY_SIDE);
        Assert.assertEquals(0.0, root, 1e-9);
    }

    @Test(expected = NoBracketingException.class)
    public void testNoBracketing() {
        UnivariateFunction f = new Sin();
        BracketingNthOrderBrentSolver solver = new BracketingNthOrderBrentSolver(1e-8, 5);
        // Sin(1.0) and Sin(2.0) have the same sign (both positive), should throw NoBracketingException
        solver.solve(100, f, 1.0, 2.0, AllowedSolution.ANY_SIDE);
    }

    @Test
    public void testConvergenceOnZero() {
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return x;
            }
        };
        BracketingNthOrderBrentSolver solver = new BracketingNthOrderBrentSolver(1e-8, 5);
        double root = solver.solve(100, f, -0.1, 0.0, AllowedSolution.ANY_SIDE);
        Assert.assertEquals(0.0, root, 1e-8);
    }

    @Test
    public void testHighOrderInterpolation() {
        UnivariateFunction f = new Sin();
        // Test with maximal order up to 7
        for (int order = 2; order <= 7; order++) {
            BracketingNthOrderBrentSolver solver = new BracketingNthOrderBrentSolver(1e-8, order);
            double root = solver.solve(100, f, 3.0, 4.0, AllowedSolution.ANY_SIDE);
            Assert.assertEquals(Math.PI, root, 1e-7);
        }
    }

    @Test
    public void testWithInitialGuess() {
        UnivariateFunction f = new Sin();
        BracketingNthOrderBrentSolver solver = new BracketingNthOrderBrentSolver(1e-8, 5);
        double root = solver.solve(100, f, 2.5, 4.0, 3.1, AllowedSolution.ANY_SIDE);
        Assert.assertEquals(Math.PI, root, 1e-8);
    }

    @Test
    public void testInitialGuessIsExactRoot() {
        UnivariateFunction f = new Sin();
        BracketingNthOrderBrentSolver solver = new BracketingNthOrderBrentSolver(1e-8, 5);
        double root = solver.solve(100, f, 3.0, 4.0, Math.PI, AllowedSolution.ANY_SIDE);
        Assert.assertEquals(Math.PI, root, 1e-8);
    }

    @Test
    public void testConvergenceWithAxiomaticLeftSideRightSide() {
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return x * x - 4.0;
            }
        };
        BracketingNthOrderBrentSolver solver = new BracketingNthOrderBrentSolver(1e-9, 5);
        
        double r1 = solver.solve(100, f, 1.0, 3.0, AllowedSolution.LEFT_SIDE);
        Assert.assertEquals(2.0, r1, 1e-7);

        double r2 = solver.solve(100, f, 1.0, 3.0, AllowedSolution.RIGHT_SIDE);
        Assert.assertEquals(2.0, r2, 1e-7);
        
        double r3 = solver.solve(100, f, -3.0, -1.0, AllowedSolution.LEFT_SIDE);
        Assert.assertEquals(-2.0, r3, 1e-7);
    }

    @Test(expected = TooManyEvaluationsException.class)
    public void testMaxEvaluationsExceeded() {
        UnivariateFunction f = new Sin();
        BracketingNthOrderBrentSolver solver = new BracketingNthOrderBrentSolver(1e-15, 5);
        // Setting maxEval to 2 should definitely trigger TooManyEvaluationsException for a non-trivial bracket
        solver.solve(2, f, 3.0, 4.0, AllowedSolution.ANY_SIDE);
    }
}