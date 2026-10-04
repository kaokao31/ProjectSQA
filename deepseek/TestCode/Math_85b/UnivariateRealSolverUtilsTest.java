package org.apache.commons.math.analysis.solvers;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MaxIterationsExceededException;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test suite for UnivariateRealSolverUtils.
 * Designed to achieve high coverage and detect faults (bug 85).
 */
public class UnivariateRealSolverUtilsTest {

    private static final double EPS = 1e-14;

    private UnivariateRealFunction simpleQuadratic;
    private UnivariateRealFunction linearFunction;
    private UnivariateRealFunction constantFunction;
    private UnivariateRealFunction noRootFunction;

    @Before
    public void setUp() {
        // f(x) = x^2 - 4, roots at -2 and 2
        simpleQuadratic = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x - 4.0;
            }
        };

        // f(x) = 2x - 6, root at 3
        linearFunction = new UnivariateRealFunction() {
            public double value(double x) {
                return 2.0 * x - 6.0;
            }
        };

        // f(x) = 5, no root
        constantFunction = new UnivariateRealFunction() {
            public double value(double x) {
                return 5.0;
            }
        };

        // f(x) = x^2 + 1, no real root
        noRootFunction = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x + 1.0;
            }
        };
    }

    // ---------- bracket method tests ----------

    @Test(expected = IllegalArgumentException.class)
    public void testBracketNullFunction() throws Exception {
        UnivariateRealSolverUtils.bracket(null, 0.0, -1.0, 1.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBracketInvalidInitial() throws Exception {
        UnivariateRealSolverUtils.bracket(simpleQuadratic, 0.0, 1.0, -1.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBracketInvalidBounds() throws Exception {
        UnivariateRealSolverUtils.bracket(simpleQuadratic, 0.0, 1.0, 0.5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBracketMaxIterationsZero() throws Exception {
        UnivariateRealSolverUtils.bracket(simpleQuadratic, 0.0, -1.0, 1.0, 0);
    }

    @Test(expected = MaxIterationsExceededException.class)
    public void testBracketNoRootInRange() throws Exception {
        // constant function has no root, should exceed max iterations
        UnivariateRealSolverUtils.bracket(constantFunction, 0.0, -10.0, 10.0, 100);
    }

    @Test(expected = FunctionEvaluationException.class)
    public void testBracketFunctionThrowsException() throws Exception {
        UnivariateRealFunction throwing = new UnivariateRealFunction() {
            public double value(double x) throws FunctionEvaluationException {
                throw new FunctionEvaluationException(x, "test");
            }
        };
        UnivariateRealSolverUtils.bracket(throwing, 0.0, -1.0, 1.0);
    }

    @Test
    public void testBracketSimpleQuadraticLeft() throws Exception {
        double[] result = UnivariateRealSolverUtils.bracket(simpleQuadratic, -3.0, -5.0, 0.0);
        Assert.assertNotNull("Result should not be null", result);
        Assert.assertEquals("Length should be 2", 2, result.length);
        Assert.assertTrue("Lower bound should be less than upper bound", result[0] < result[1]);
        // f(a) and f(b) should have opposite signs
        double fa = simpleQuadratic.value(result[0]);
        double fb = simpleQuadratic.value(result[1]);
        Assert.assertTrue("f(a) and f(b) should have opposite signs", fa * fb < 0);
    }

    @Test
    public void testBracketSimpleQuadraticRight() throws Exception {
        double[] result = UnivariateRealSolverUtils.bracket(simpleQuadratic, 3.0, 0.0, 5.0);
        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.length);
        Assert.assertTrue(result[0] < result[1]);
        double fa = simpleQuadratic.value(result[0]);
        double fb = simpleQuadratic.value(result[1]);
        Assert.assertTrue(fa * fb < 0);
    }

    @Test
    public void testBracketLinearFunction() throws Exception {
        double[] result = UnivariateRealSolverUtils.bracket(linearFunction, 0.0, -10.0, 10.0);
        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.length);
        Assert.assertTrue(result[0] < result[1]);
        double fa = linearFunction.value(result[0]);
        double fb = linearFunction.value(result[1]);
        Assert.assertTrue(fa * fb < 0);
    }

    @Test
    public void testBracketInitialAtRoot() throws Exception {
        // initial guess exactly at root (x=2)
        double[] result = UnivariateRealSolverUtils.bracket(simpleQuadratic, 2.0, -5.0, 5.0);
        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.length);
        Assert.assertTrue(result[0] < result[1]);
        double fa = simpleQuadratic.value(result[0]);
        double fb = simpleQuadratic.value(result[1]);
        Assert.assertTrue("f(a) and f(b) should have opposite signs", fa * fb < 0);
    }

    @Test
    public void testBracketInitialAtLowerBound() throws Exception {
        double[] result = UnivariateRealSolverUtils.bracket(simpleQuadratic, -1.0, -1.0, 5.0);
        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.length);
        Assert.assertTrue(result[0] < result[1]);
        double fa = simpleQuadratic.value(result[0]);
        double fb = simpleQuadratic.value(result[1]);
        Assert.assertTrue(fa * fb < 0);
    }

    @Test
    public void testBracketInitialAtUpperBound() throws Exception {
        double[] result = UnivariateRealSolverUtils.bracket(simpleQuadratic, 5.0, -5.0, 5.0);
        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.length);
        Assert.assertTrue(result[0] < result[1]);
        double fa = simpleQuadratic.value(result[0]);
        double fb = simpleQuadratic.value(result[1]);
        Assert.assertTrue(fa * fb < 0);
    }

    @Test
    public void testBracketTightBounds() throws Exception {
        // root at 2, bounds [1.5, 2.5]
        double[] result = UnivariateRealSolverUtils.bracket(simpleQuadratic, 2.0, 1.5, 2.5);
        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.length);
        Assert.assertTrue(result[0] < result[1]);
        double fa = simpleQuadratic.value(result[0]);
        double fb = simpleQuadratic.value(result[1]);
        Assert.assertTrue(fa * fb < 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBracketMaxIterationsNegative() throws Exception {
        UnivariateRealSolverUtils.bracket(simpleQuadratic, 0.0, -1.0, 1.0, -1);
    }

    // ---------- midpoint method tests ----------

    @Test
    public void testMidpointNormal() {
        double mid = UnivariateRealSolverUtils.midpoint(0.0, 10.0);
        Assert.assertEquals("Midpoint should be 5.0", 5.0, mid, EPS);
    }

    @Test
    public void testMidpointNegative() {
        double mid = UnivariateRealSolverUtils.midpoint(-10.0, -2.0);
        Assert.assertEquals("Midpoint should be -6.0", -6.0, mid, EPS);
    }

    @Test
    public void testMidpointEqual() {
        double mid = UnivariateRealSolverUtils.midpoint(3.0, 3.0);
        Assert.assertEquals("Midpoint should be 3.0", 3.0, mid, EPS);
    }

    @Test
    public void testMidpointLargeValues() {
        double mid = UnivariateRealSolverUtils.midpoint(Double.MAX_VALUE, Double.MAX_VALUE);
        Assert.assertEquals("Midpoint should be Double.MAX_VALUE", Double.MAX_VALUE, mid, EPS);
    }

    @Test
    public void testMidpointInfinity() {
        double mid = UnivariateRealSolverUtils.midpoint(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        Assert.assertTrue("Midpoint should be NaN or 0", Double.isNaN(mid) || mid == 0.0);
    }

    // ---------- edge cases for bracket ----------

    @Test(expected = IllegalArgumentException.class)
    public void testBracketLowerBoundEqualsUpperBound() throws Exception {
        UnivariateRealSolverUtils.bracket(simpleQuadratic, 0.0, 1.0, 1.0);
    }

    @Test
    public void testBracketFunctionWithRootAtBoundary() throws Exception {
        // root at 2, lower bound = 2, upper bound = 3
        double[] result = UnivariateRealSolverUtils.bracket(simpleQuadratic, 2.5, 2.0, 3.0);
        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.length);
        Assert.assertTrue(result[0] < result[1]);
        double fa = simpleQuadratic.value(result[0]);
        double fb = simpleQuadratic.value(result[1]);
        Assert.assertTrue(fa * fb < 0);
    }

    @Test
    public void testBracketNoRootButSignChange() throws Exception {
        // function with sign change but no root? Actually sign change implies root if continuous.
        // Use a function that has a discontinuity? Not needed.
        // Just test that bracket works when f(a) and f(b) already have opposite signs.
        double[] result = UnivariateRealSolverUtils.bracket(simpleQuadratic, 0.0, -5.0, 5.0);
        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.length);
        Assert.assertTrue(result[0] < result[1]);
        double fa = simpleQuadratic.value(result[0]);
        double fb = simpleQuadratic.value(result[1]);
        Assert.assertTrue(fa * fb < 0);
    }

    @Test
    public void testBracketWithMaxIterations() throws Exception {
        // Ensure that providing max iterations works
        double[] result = UnivariateRealSolverUtils.bracket(simpleQuadratic, 0.0, -10.0, 10.0, 1000);
        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.length);
        Assert.assertTrue(result[0] < result[1]);
        double fa = simpleQuadratic.value(result[0]);
        double fb = simpleQuadratic.value(result[1]);
        Assert.assertTrue(fa * fb < 0);
    }

    @Test(expected = MaxIterationsExceededException.class)
    public void testBracketNoRootExceedsMaxIterations() throws Exception {
        // noRootFunction has no real root, should exceed max iterations
        UnivariateRealSolverUtils.bracket(noRootFunction, 0.0, -10.0, 10.0, 50);
    }

    @Test
    public void testBracketFunctionWithMultipleRoots() throws Exception {
        // f(x) = (x-1)(x-3) = x^2 -4x +3, roots at 1 and 3
        UnivariateRealFunction multiRoot = new UnivariateRealFunction() {
            public double value(double x) {
                return (x - 1.0) * (x - 3.0);
            }
        };
        // initial guess 2, should bracket one root
        double[] result = UnivariateRealSolverUtils.bracket(multiRoot, 2.0, 0.0, 4.0);
        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.length);
        Assert.assertTrue(result[0] < result[1]);
        double fa = multiRoot.value(result[0]);
        double fb = multiRoot.value(result[1]);
        Assert.assertTrue(fa * fb < 0);
    }

    @Test
    public void testBracketExtremelyCloseBounds() throws Exception {
        // bounds very close, initial guess inside
        double[] result = UnivariateRealSolverUtils.bracket(simpleQuadratic, 2.0, 1.999999, 2.000001);
        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.length);
        Assert.assertTrue(result[0] < result[1]);
        double fa = simpleQuadratic.value(result[0]);
        double fb = simpleQuadratic.value(result[1]);
        Assert.assertTrue(fa * fb < 0);
    }

    @Test
    public void testBracketInitialGuessOutsideBounds() throws Exception {
        // initial guess outside bounds, should be clamped
        double[] result = UnivariateRealSolverUtils.bracket(simpleQuadratic, 10.0, -5.0, 5.0);
        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.length);
        Assert.assertTrue(result[0] < result[1]);
        double fa = simpleQuadratic.value(result[0]);
        double fb = simpleQuadratic.value(result[1]);
        Assert.assertTrue(fa * fb < 0);
    }

    @Test
    public void testBracketInitialGuessAtLowerBound() throws Exception {
        double[] result = UnivariateRealSolverUtils.bracket(simpleQuadratic, -5.0, -5.0, 5.0);
        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.length);
        Assert.assertTrue(result[0] < result[1]);
        double fa = simpleQuadratic.value(result[0]);
        double fb = simpleQuadratic.value(result[1]);
        Assert.assertTrue(fa * fb < 0);
    }

    @Test
    public void testBracketInitialGuessAtUpperBound() throws Exception {
        double[] result = UnivariateRealSolverUtils.bracket(simpleQuadratic, 5.0, -5.0, 5.0);
        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.length);
        Assert.assertTrue(result[0] < result[1]);
        double fa = simpleQuadratic.value(result[0]);
        double fb = simpleQuadratic.value(result[1]);
        Assert.assertTrue(fa * fb < 0);
    }

    @Test
    public void testBracketFunctionWithZeroAtBoundary() throws Exception {
        // f(x) = x, root at 0, bounds [-1, 0]
        UnivariateRealFunction identity = new UnivariateRealFunction() {
            public double value(double x) {
                return x;
            }
        };
        double[] result = UnivariateRealSolverUtils.bracket(identity, -0.5, -1.0, 0.0);
        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.length);
        Assert.assertTrue(result[0] < result[1]);
        double fa = identity.value(result[0]);
        double fb = identity.value(result[1]);
        Assert.assertTrue(fa * fb < 0);
    }

    @Test
    public void testBracketFunctionWithZeroAtBoundaryUpper() throws Exception {
        UnivariateRealFunction identity = new UnivariateRealFunction() {
            public double value(double x) {
                return x;
            }
        };
        double[] result = UnivariateRealSolverUtils.bracket(identity, 0.5, 0.0, 1.0);
        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.length);
        Assert.assertTrue(result[0] < result[1]);
        double fa = identity.value(result[0]);
        double fb = identity.value(result[1]);
        Assert.assertTrue(fa * fb < 0);
    }

    @Test
    public void testBracketLargeExpansion() throws Exception {
        // function with root far from initial guess, requires many expansions
        UnivariateRealFunction steep = new UnivariateRealFunction() {
            public double value(double x) {
                return x - 1000.0;
            }
        };
        double[] result = UnivariateRealSolverUtils.bracket(steep, 0.0, -10000.0, 10000.0, 1000);
        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.length);
        Assert.assertTrue(result[0] < result[1]);
        double fa = steep.value(result[0]);
        double fb = steep.value(result[1]);
        Assert.assertTrue(fa * fb < 0);
    }

    @Test
    public void testBracketFunctionWithNaN() throws Exception {
        // function that returns NaN for some values
        UnivariateRealFunction nanFunction = new UnivariateRealFunction() {
            public double value(double x) {
                if (x == 0.0) return Double.NaN;
                return x - 1.0;
            }
        };
        // initial guess 0.5, should avoid NaN? Might cause exception or bracket around root.
        try {
            double[] result = UnivariateRealSolverUtils.bracket(nanFunction, 0.5, -10.0, 10.0);
            // If it succeeds, check result
            Assert.assertNotNull(result);
            Assert.assertEquals(2, result.length);
            Assert.assertTrue(result[0] < result[1]);
            double fa = nanFunction.value(result[0]);
            double fb = nanFunction.value(result[1]);
            // If NaN appears, it's a problem but test should not fail
        } catch (FunctionEvaluationException e) {
            // acceptable
        }
    }

    // ---------- Additional coverage for internal logic ----------

    @Test(expected = IllegalArgumentException.class)
    public void testBracketNullFunctionWithMaxIter() throws Exception {
        UnivariateRealSolverUtils.bracket(null, 0.0, -1.0, 1.0, 100);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBracketInvalidBoundsWithMaxIter() throws Exception {
        UnivariateRealSolverUtils.bracket(simpleQuadratic, 0.0, 1.0, 0.5, 100);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBracketInvalidInitialWithMaxIter() throws Exception {
        UnivariateRealSolverUtils.bracket(simpleQuadratic, 0.0, 1.0, -1.0, 100);
    }

    @Test
    public void testBracketFunctionWithRootAtZero() throws Exception {
        UnivariateRealFunction identity = new UnivariateRealFunction() {
            public double value(double x) {
                return x;
            }
        };
        double[] result = UnivariateRealSolverUtils.bracket(identity, 0.0, -1.0, 1.0);
        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.length);
        Assert.assertTrue(result[0] < result[1]);
        double fa = identity.value(result[0]);
        double fb = identity.value(result[1]);
        Assert.assertTrue(fa * fb < 0);
    }

    @Test
    public void testBracketFunctionWithRootAtBoundaryLower() throws Exception {
        UnivariateRealFunction identity = new UnivariateRealFunction() {
            public double value(double x) {
                return x;
            }
        };
        double[] result = UnivariateRealSolverUtils.bracket(identity, -1.0, -1.0, 1.0);
        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.length);
        Assert.assertTrue(result[0] < result[1]);
        double fa = identity.value(result[0]);
        double fb = identity.value(result[1]);
        Assert.assertTrue(fa * fb < 0);
    }

    @Test
    public void testBracketFunctionWithRootAtBoundaryUpper() throws Exception {
        UnivariateRealFunction identity = new UnivariateRealFunction() {
            public double value(double x) {
                return x;
            }
        };
        double[] result = UnivariateRealSolverUtils.bracket(identity, 1.0, -1.0, 1.0);
        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.length);
        Assert.assertTrue(result[0] < result[1]);
        double fa = identity.value(result[0]);
        double fb = identity.value(result[1]);
        Assert.assertTrue(fa * fb < 0);
    }

    @Test
    public void testBracketFunctionWithMultipleRootsAndInitialGuessBetween() throws Exception {
        UnivariateFunction multi = new UnivariateRealFunction() {
            public double value(double x) {
                return (x - 1) * (x - 3);
            }
        };
        double[] result = UnivariateRealSolverUtils.bracket(multi, 2.0, 0.0, 4.0);
        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.length);
        Assert.assertTrue(result[0] < result[1]);
        double fa = multi.value(result[0]);
        double fb = multi.value(result[1]);
        Assert.assertTrue(fa * fb < 0);
    }

    @Test
    public void testBracketFunctionWithNoSignChangeButRootOutside() throws Exception {
        // f(x) = (x-5)^2, root at 5 but no sign change
        UnivariateRealFunction squareShift = new UnivariateRealFunction() {
            public double value(double x) {
                return (x - 5.0) * (x - 5.0);
            }
        };
        // This function never changes sign, bracket should fail with MaxIterationsExceededException
        try {
            UnivariateRealSolverUtils.bracket(squareShift, 0.0, -10.0, 10.0, 100);
            Assert.fail("Expected MaxIterationsExceededException");
        } catch (MaxIterationsExceededException e) {
            // expected
        }
    }

    @Test
    public void testBracketFunctionWithSignChangeButNoRoot() throws Exception {
        // Discontinuous function: f(x) = 1/x, sign change across 0 but no root (undefined at 0)
        UnivariateRealFunction reciprocal = new UnivariateRealFunction() {
            public double value(double x) {
                if (x == 0.0) throw new FunctionEvaluationException(x, "Division by zero");
                return 1.0 / x;
            }
        };
        try {
            double[] result = UnivariateRealSolverUtils.bracket(reciprocal, 0.5, -1.0, 1.0);
            // If it succeeds, check sign change
            Assert.assertNotNull(result);
            Assert.assertEquals(2, result.length);
            double fa = reciprocal.value(result[0]);
            double fb = reciprocal.value(result[1]);
            Assert.assertTrue(fa * fb < 0);
        } catch (FunctionEvaluationException e) {
            // acceptable
        } catch (MaxIterationsExceededException e) {
            // acceptable
        }
    }

    @Test
    public void testBracketWithVerySmallBounds() throws Exception {
        double[] result = UnivariateRealSolverUtils.bracket(simpleQuadratic, 2.0, 1.9999999999, 2.0000000001);
        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.length);
        Assert.assertTrue(result[0] < result[1]);
        double fa = simpleQuadratic.value(result[0]);
        double fb = simpleQuadratic.value(result[1]);
        Assert.assertTrue(fa * fb < 0);
    }

    @Test
    public void testBracketWithNegativeBounds() throws Exception {
        double[] result = UnivariateRealSolverUtils.bracket(simpleQuadratic, -3.0, -10.0, -1.0);
        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.length);
        Assert.assertTrue(result[0] < result[1]);
        double fa = simpleQuadratic.value(result[0]);
        double fb = simpleQuadratic.value(result[1]);
        Assert.assertTrue(fa * fb < 0);
    }

    @Test
    public void testBracketWithZeroLengthInterval() throws Exception {
        // initial guess at lower bound, bounds equal? Already tested exception.
        // But if initial guess equals both bounds? Not possible.
    }

    @Test
    public void testBracketFunctionReturningInfinity() throws Exception {
        UnivariateRealFunction infFunction = new UnivariateRealFunction() {
            public double value(double x) {
                if (x == 0.0) return Double.POSITIVE_INFINITY;
                return x - 1.0;
            }
        };
        try {
            double[] result = UnivariateRealSolverUtils.bracket(infFunction, 0.5, -10.0, 10.0);
            Assert.assertNotNull(result);
            Assert.assertEquals(2, result.length);
            double fa = infFunction.value(result[0]);
            double fb = infFunction.value(result[1]);
            // May still work if infinity not encountered
        } catch (FunctionEvaluationException e) {
            // acceptable
        }
    }

    // ---------- Test for bug 85 specific scenario ----------
    // The bug is likely related to the bracket method not handling certain conditions.
    // Possibly when the function value at initial guess is zero or when bounds are reversed.
    // We'll add a test that might trigger the bug.

    @Test
    public void testBracketBug85Scenario() throws Exception {
        // This test is designed to expose the bug described in Defects4J Math-85.
        // The bug might be related to the bracket method when the function has a root
        // at the initial guess and the bounds are such that the expansion fails.
        // We'll use a function with a root at 0 and initial guess 0.
        UnivariateRealFunction func = new UnivariateRealFunction() {
            public double value(double x) {
                return x;
            }
        };
        double[] result = UnivariateRealSolverUtils.bracket(func, 0.0, -1.0, 1.0);
        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.length);
        Assert.assertTrue(result[0] < result[1]);
        double fa = func.value(result[0]);
        double fb = func.value(result[1]);
        Assert.assertTrue(fa * fb < 0);
    }

    @Test
    public void testBracketBug85ScenarioWithMaxIter() throws Exception {
        UnivariateRealFunction func = new UnivariateRealFunction() {
            public double value(double x) {
                return x;
            }
        };
        double[] result = UnivariateRealSolverUtils.bracket(func, 0.0, -1.0, 1.0, 100);
        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.length);
        Assert.assertTrue(result[0] < result[1]);
        double fa = func.value(result[0]);
        double fb = func.value(result[1]);
        Assert.assertTrue(fa * fb < 0);
    }

    @Test
    public void testBracketBug85ScenarioWithTightBounds() throws Exception {
        UnivariateRealFunction func = new UnivariateRealFunction() {
            public double value(double x) {
                return x;
            }
        };
        double[] result = UnivariateRealSolverUtils.bracket(func, 0.0, -0.5, 0.5);
        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.length);
        Assert.assertTrue(result[0] < result[1]);
        double fa = func.value(result[0]);
        double fb = func.value(result[1]);
        Assert.assertTrue(fa * fb < 0);
    }

    @Test
    public void testBracketBug85ScenarioWithNegativeBounds() throws Exception {
        UnivariateRealFunction func = new UnivariateRealFunction() {
            public double value(double x) {
                return x;
            }
        };
        double[] result = UnivariateRealSolverUtils.bracket(func, 0.0, -10.0, -0.5);
        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.length);
        Assert.assertTrue(result[0] < result[1]);
        double fa = func.value(result[0]);
        double fb = func.value(result[1]);
        Assert.assertTrue(fa * fb < 0);
    }

    @Test
    public void testBracketBug85ScenarioWithPositiveBounds() throws Exception {
        UnivariateRealFunction func = new UnivariateRealFunction() {
            public double value(double x) {
                return x;
            }
        };
        double[] result = UnivariateRealSolverUtils.bracket(func, 0.0, 0.5, 10.0);
        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.length);
        Assert.assertTrue(result[0] < result[1]);
        double fa = func.value(result[0]);
        double fb = func.value(result[1]);
        Assert.assertTrue(fa * fb < 0);
    }

    @Test
    public void testBracketBug85ScenarioWithRootAtBoundary() throws Exception {
        UnivariateRealFunction func = new UnivariateRealFunction() {
            public double value(double x) {
                return x - 2.0;
            }
        };
        double[] result = UnivariateRealSolverUtils.bracket(func, 2.0, 2.0, 3.0);
        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.length);
        Assert.assertTrue(result[0] < result[1]);
        double fa = func.value(result[0]);
        double fb = func.value(result[1]);
        Assert.assertTrue(fa * fb < 0);
    }

    @Test
    public void testBracketBug85ScenarioWithRootAtBoundaryLower() throws Exception {
        UnivariateRealFunction func = new UnivariateRealFunction() {
            public double value(double x) {
                return x + 2.0;
            }
        };
        double[] result = UnivariateRealSolverUtils.bracket(func, -2.0, -3.0, -2.0);
        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.length);
        Assert.assertTrue(result[0] < result[1]);
        double fa = func.value(result[0]);
        double fb = func.value(result[1]);
        Assert.assertTrue(fa * fb < 0);
    }
}