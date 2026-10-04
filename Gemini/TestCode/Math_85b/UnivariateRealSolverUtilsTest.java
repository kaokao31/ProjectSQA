package org.apache.commons.math.analysis.solvers;

import org.apache.commons.math.MathException;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.junit.Assert;
import org.junit.Test;

public class UnivariateRealSolverUtilsTest {

    private static final double TOLERANCE = 1.0E-6;

    // Simple test function: f(x) = x^2 - 4
    // Roots at x = -2 and x = 2
    private static class SquareFunction implements UnivariateRealFunction {
        public double value(double x) {
            return x * x - 4.0;
        }
    }

    // Function with no root in some intervals: f(x) = x^2 + 1
    private static class AlwaysPositiveFunction implements UnivariateRealFunction {
        public double value(double x) {
            return x * x + 1.0;
        }
    }

    // Function for strict sign changes or edge cases
    private static class LinearFunction implements UnivariateRealFunction {
        public double value(double x) {
            return x;
        }
    }

    @Test
    public void testSolveWithFunction() throws MathException {
        UnivariateRealFunction f = new SquareFunction();
        double root = UnivariateRealSolverUtils.solve(f, 0.0, 3.0);
        Assert.assertEquals(2.0, root, TOLERANCE);
    }

    @Test
    public void testSolveWithFunctionAndAccuracy() throws MathException {
        UnivariateRealFunction f = new SquareFunction();
        double root = UnivariateRealSolverUtils.solve(f, 0.0, 3.0, 1.0E-9);
        Assert.assertEquals(2.0, root, 1.0E-8);
    }

    @Test
    public void testBracketWithValidRoot() throws MathException {
        UnivariateRealFunction f = new SquareFunction();
        // Start searching around 1.0, root is at 2.0
        double[] bracket = UnivariateRealSolverUtils.bracket(f, 1.0, 0.0, 3.0);
        Assert.assertNotNull(bracket);
        Assert.assertEquals(2, bracket.length);
        Assert.assertTrue(f.value(bracket[0]) * f.value(bracket[1]) <= 0.0);
    }

    @Test
    public void testBracketNegativeStart() throws MathException {
        UnivariateRealFunction f = new SquareFunction();
        // Start searching around -1.0, root is at -2.0
        double[] bracket = UnivariateRealSolverUtils.bracket(f, -1.0, -3.0, 0.0);
        Assert.assertNotNull(bracket);
        Assert.assertEquals(2, bracket.length);
        Assert.assertTrue(f.value(bracket[0]) * f.value(bracket[1]) <= 0.0);
    }

    @Test
    public void testBracketCenterIsRoot() throws MathException {
        UnivariateRealFunction f = new LinearFunction();
        // Root is at 0.0, start is 0.0
        double[] bracket = UnivariateRealSolverUtils.bracket(f, 0.0, -1.0, 1.0);
        Assert.assertNotNull(bracket);
        Assert.assertEquals(-1.0, bracket[0], TOLERANCE);
        Assert.assertEquals(1.0, bracket[1], TOLERANCE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSolveNullFunction() throws MathException {
        UnivariateRealSolverUtils.solve(null, 0.0, 1.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSolveAccuracyNullFunction() throws MathException {
        UnivariateRealSolverUtils.solve(null, 0.0, 1.0, 0.1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBracketNullFunction() throws MathException {
        UnivariateRealSolverUtils.bracket(null, 1.0, 0.0, 2.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBracketInvalidNumIterations() throws MathException {
        UnivariateRealFunction f = new SquareFunction();
        UnivariateRealSolverUtils.bracket(f, 1.0, 0.0, 2.0, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBracketNegativeNumIterations() throws MathException {
        UnivariateRealFunction f = new SquareFunction();
        UnivariateRealSolverUtils.bracket(f, 1.0, 0.0, 2.0, -5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBracketInvalidInterval() throws MathException {
        UnivariateRealFunction f = new SquareFunction();
        // initial < lowerBound
        UnivariateRealSolverUtils.bracket(f, 0.0, 1.0, 3.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBracketInvalidIntervalUpper() throws MathException {
        UnivariateRealFunction f = new SquareFunction();
        // initial > upperBound
        UnivariateRealSolverUtils.bracket(f, 4.0, 1.0, 3.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBracketInitialEqualToBounds() throws MathException {
        UnivariateRealFunction f = new SquareFunction();
        // initial == lowerBound == upperBound
        UnivariateRealSolverUtils.bracket(f, 1.0, 1.0, 1.0);
    }

    @Test(expected = MathException.class)
    public void testBracketFailureExceededMaximumIterations() throws MathException {
        UnivariateRealFunction f = new AlwaysPositiveFunction();
        // Should not find a root sign change, thus throwing MathException
        UnivariateRealSolverUtils.bracket(f, 1.0, -10.0, 10.0, 5);
    }

    @Test
    public void testSampleFunction() throws MathException {
        UnivariateRealFunction f = new SquareFunction();
        double[] sample = UnivariateRealSolverUtils.sample(f, 0.0, 2.0, 3);
        Assert.assertEquals(3, sample.length);
        Assert.assertEquals(-4.0, sample[0], TOLERANCE);
        Assert.assertEquals(-3.0, sample[1], TOLERANCE);
        Assert.assertEquals(0.0, sample[2], TOLERANCE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSampleInvalidNumberPoints() throws MathException {
        UnivariateRealFunction f = new SquareFunction();
        UnivariateRealSolverUtils.sample(f, 0.0, 2.0, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSampleNegativeNumberPoints() throws MathException {
        UnivariateRealFunction f = new SquareFunction();
        UnivariateRealSolverUtils.sample(f, 0.0, 2.0, -2);
    }

    @Test
    public void testDefaultFactoryMethods() {
        UnivariateRealSolver solver = UnivariateRealSolverUtils.newDefaultSolver();
        Assert.assertNotNull(solver);
    }

    @Test
    public void testMaxFactoryMethod() {
        UnivariateRealFunction f = new SquareFunction();
        // Testing that factory or helper methods instantiate valid objects
        UnivariateRealSolver solver = UnivariateRealSolverUtils.newDefaultSolver();
        Assert.assertNotNull(solver);
    }
}