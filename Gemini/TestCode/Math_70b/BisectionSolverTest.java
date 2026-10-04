package org.apache.commons.math.analysis.solvers;

import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.analysis.SinFunction;
import org.apache.commons.math.analysis.QuinticFunction;
import org.junit.Assert;
import org.junit.Test;

public class BisectionSolverTest {

    @Test
    public void testBisectionWithSinFunction() throws Exception {
        UnivariateRealFunction f = new SinFunction();
        UnivariateRealSolver solver = new BisectionSolver(f);

        double result = solver.solve(3.0, 4.0);
        Assert.assertEquals(Math.PI, result, 1E-6);

        result = solver.solve(f, 3.0, 4.0, 3.14);
        Assert.assertEquals(Math.PI, result, 1E-6);

        result = solver.solve(3.0, 4.0, 1E-6);
        Assert.assertEquals(Math.PI, result, 1E-6);

        result = solver.solve(f, 3.0, 4.0);
        Assert.assertEquals(Math.PI, result, 1E-6);
    }

    @Test
    public void testBisectionWithQuinticFunction() throws Exception {
        UnivariateRealFunction f = new QuinticFunction();
        UnivariateRealSolver solver = new BisectionSolver();

        double result = solver.solve(f, -0.2, 0.2);
        Assert.assertEquals(0.0, result, 1E-6);

        result = solver.solve(f, -0.1, 0.3);
        Assert.assertEquals(0.0, result, 1E-6);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidInterval() throws Exception {
        UnivariateRealFunction f = new QuinticFunction();
        UnivariateRealSolver solver = new BisectionSolver(f);
        // Interval where function doesn't change sign (both positive or both negative)
        solver.solve(2.0, 3.0);
    }

    @Test
    public void testConstructorWithFunction() {
        UnivariateRealFunction f = new QuinticFunction();
        BisectionSolver solver = new BisectionSolver(f);
        Assert.assertNotNull(solver);
    }

    @Test
    public void testConstructorWithoutFunction() {
        BisectionSolver solver = new BisectionSolver();
        Assert.assertNotNull(solver);
    }
}