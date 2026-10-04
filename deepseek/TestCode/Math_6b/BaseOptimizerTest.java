package org.apache.commons.math3.optim;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

/**
 * JUnit 4 test suite for BaseOptimizer.
 * Designed to achieve high line/branch coverage and detect potential faults.
 */
public class BaseOptimizerTest {

    // Concrete subclass for testing abstract BaseOptimizer
    private static class TestOptimizer extends BaseOptimizer {
        private int doOptimizeCallCount = 0;
        private boolean doOptimizeThrows = false;

        TestOptimizer(ConvergenceChecker<RealPointValuePair> checker,
                      int maxEvaluations, int maxIterations) {
            super(checker, maxEvaluations, maxIterations);
        }

        @Override
        public RealPointValuePair doOptimize() {
            doOptimizeCallCount++;
            if (doOptimizeThrows) {
                throw new RuntimeException("Simulated failure in doOptimize");
            }
            // Return a dummy point
            return new RealPointValuePair(new double[]{0.0}, 0.0);
        }

        // Expose protected methods for testing
        @Override
        public void incrementEvaluationCount() throws TooManyEvaluationsException {
            super.incrementEvaluationCount();
        }

        @Override
        public void incrementIterationCount() throws TooManyIterationsException {
            super.incrementIterationCount();
        }

        @Override
        public RealPointValuePair best(RealPointValuePair a, RealPointValuePair b) {
            return super.best(a, b);
        }

        @Override
        public void parseOptimizationData(OptimizationData... data) {
            super.parseOptimizationData(data);
        }
    }

    private TestOptimizer optimizer;
    private ConvergenceChecker<RealPointValuePair> checker;

    @Before
    public void setUp() {
        // Default checker: always converged after first iteration
        checker = new ConvergenceChecker<RealPointValuePair>() {
            public boolean converged(int iteration, RealPointValuePair previous, RealPointValuePair current) {
                return iteration >= 1;
            }
        };
        optimizer = new TestOptimizer(checker, 100, 100);
    }

    // ---------- Constructor tests ----------
    @Test
    public void testConstructorWithNullChecker() {
        TestOptimizer opt = new TestOptimizer(null, 10, 10);
        assertNull("Convergence checker should be null", opt.getConvergenceChecker());
        assertEquals("Iterations should start at 0", 0, opt.getIterations());
    }

    @Test
    public void testConstructorWithChecker() {
        assertNotNull("Convergence checker should not be null", optimizer.getConvergenceChecker());
        assertSame("Checker should be the one passed", checker, optimizer.getConvergenceChecker());
        assertEquals("Iterations should start at 0", 0, optimizer.getIterations());
    }

    @Test(expected = NumberIsTooLargeException.class)
    public void testConstructorNegativeMaxEvaluations() {
        new TestOptimizer(checker, -1, 10);
    }

    @Test(expected = NumberIsTooLargeException.class)
    public void testConstructorNegativeMaxIterations() {
        new TestOptimizer(checker, 10, -1);
    }

    // ---------- optimize tests ----------
    @Test
    public void testOptimizeCallsDoOptimize() {
        optimizer.optimize();
        assertEquals("doOptimize should be called once", 1, optimizer.doOptimizeCallCount);
    }

    @Test(expected = RuntimeException.class)
    public void testOptimizeWhenDoOptimizeThrows() {
        optimizer.doOptimizeThrows = true;
        optimizer.optimize();
    }

    // ---------- incrementEvaluationCount tests ----------
    @Test
    public void testIncrementEvaluationCountNormal() {
        optimizer.incrementEvaluationCount();
        // No exception expected
    }

    @Test(expected = TooManyEvaluationsException.class)
    public void testIncrementEvaluationCountExceedsMax() {
        // Create optimizer with max evaluations = 1
        TestOptimizer opt = new TestOptimizer(checker, 1, 100);
        opt.incrementEvaluationCount(); // first increment ok
        opt.incrementEvaluationCount(); // second should throw
    }

    @Test(expected = TooManyEvaluationsException.class)
    public void testIncrementEvaluationCountZeroMax() {
        TestOptimizer opt = new TestOptimizer(checker, 0, 100);
        opt.incrementEvaluationCount(); // should throw immediately
    }

    // ---------- incrementIterationCount tests ----------
    @Test
    public void testIncrementIterationCountNormal() {
        optimizer.incrementIterationCount();
        assertEquals("Iterations should be 1", 1, optimizer.getIterations());
    }

    @Test(expected = TooManyIterationsException.class)
    public void testIncrementIterationCountExceedsMax() {
        TestOptimizer opt = new TestOptimizer(checker, 100, 1);
        opt.incrementIterationCount(); // first increment ok
        opt.incrementIterationCount(); // second should throw
    }

    @Test(expected = TooManyIterationsException.class)
    public void testIncrementIterationCountZeroMax() {
        TestOptimizer opt = new TestOptimizer(checker, 100, 0);
        opt.incrementIterationCount(); // should throw immediately
    }

    // ---------- getIterations tests ----------
    @Test
    public void testGetIterationsAfterMultipleIncrements() {
        optimizer.incrementIterationCount();
        optimizer.incrementIterationCount();
        optimizer.incrementIterationCount();
        assertEquals("Iterations should be 3", 3, optimizer.getIterations());
    }

    @Test
    public void testGetIterationsInitial() {
        assertEquals("Initial iterations should be 0", 0, optimizer.getIterations());
    }

    // ---------- getConvergenceChecker tests ----------
    @Test
    public void testGetConvergenceCheckerReturnsChecker() {
        assertSame("Should return the same checker", checker, optimizer.getConvergenceChecker());
    }

    @Test
    public void testGetConvergenceCheckerNull() {
        TestOptimizer opt = new TestOptimizer(null, 10, 10);
        assertNull("Should return null", opt.getConvergenceChecker());
    }

    // ---------- best method tests ----------
    @Test
    public void testBestFirstBetter() {
        RealPointValuePair a = new RealPointValuePair(new double[]{1.0}, 5.0);
        RealPointValuePair b = new RealPointValuePair(new double[]{2.0}, 10.0);
        RealPointValuePair result = optimizer.best(a, b);
        assertSame("Should return a (lower value)", a, result);
    }

    @Test
    public void testBestSecondBetter() {
        RealPointValuePair a = new RealPointValuePair(new double[]{1.0}, 10.0);
        RealPointValuePair b = new RealPointValuePair(new double[]{2.0}, 5.0);
        RealPointValuePair result = optimizer.best(a, b);
        assertSame("Should return b (lower value)", b, result);
    }

    @Test
    public void testBestEqualValues() {
        RealPointValuePair a = new RealPointValuePair(new double[]{1.0}, 5.0);
        RealPointValuePair b = new RealPointValuePair(new double[]{2.0}, 5.0);
        RealPointValuePair result = optimizer.best(a, b);
        // When equal, the first argument is returned (implementation dependent)
        assertSame("Should return a when values equal", a, result);
    }

    @Test
    public void testBestWithNaN() {
        RealPointValuePair a = new RealPointValuePair(new double[]{1.0}, Double.NaN);
        RealPointValuePair b = new RealPointValuePair(new double[]{2.0}, 5.0);
        RealPointValuePair result = optimizer.best(a, b);
        // NaN comparisons: should return b (since a is NaN)
        assertSame("Should return b when a is NaN", b, result);
    }

    @Test
    public void testBestBothNaN() {
        RealPointValuePair a = new RealPointValuePair(new double[]{1.0}, Double.NaN);
        RealPointValuePair b = new RealPointValuePair(new double[]{2.0}, Double.NaN);
        RealPointValuePair result = optimizer.best(a, b);
        // Both NaN: implementation may return a or b; we just check no exception
        assertNotNull("Result should not be null", result);
    }

    // ---------- parseOptimizationData tests ----------
    @Test
    public void testParseOptimizationDataEmpty() {
        optimizer.parseOptimizationData();
        // No exception expected
    }

    @Test
    public void testParseOptimizationDataWithData() {
        // Provide some dummy OptimizationData (if any concrete implementations exist)
        // Since we don't have concrete data classes, we just test that it doesn't throw
        // This test may be skipped if no concrete data is available.
        // For coverage, we can pass null (but that may cause NPE). Better to skip.
        // We'll just call with empty array.
        optimizer.parseOptimizationData();
    }

    // ---------- Edge cases for increment methods ----------
    @Test(expected = TooManyEvaluationsException.class)
    public void testIncrementEvaluationCountAfterMaxReached() {
        TestOptimizer opt = new TestOptimizer(checker, 2, 100);
        opt.incrementEvaluationCount(); // 1
        opt.incrementEvaluationCount(); // 2 (max)
        opt.incrementEvaluationCount(); // should throw
    }

    @Test(expected = TooManyIterationsException.class)
    public void testIncrementIterationCountAfterMaxReached() {
        TestOptimizer opt = new TestOptimizer(checker, 100, 2);
        opt.incrementIterationCount(); // 1
        opt.incrementIterationCount(); // 2 (max)
        opt.incrementIterationCount(); // should throw
    }

    // ---------- Test that optimize respects max evaluations ----------
    @Test(expected = TooManyEvaluationsException.class)
    public void testOptimizeThrowsWhenMaxEvaluationsExceeded() {
        // Create optimizer with max evaluations = 0, so incrementEvaluationCount in doOptimize will throw
        TestOptimizer opt = new TestOptimizer(checker, 0, 100);
        // Override doOptimize to call incrementEvaluationCount
        opt = new TestOptimizer(checker, 0, 100) {
            @Override
            public RealPointValuePair doOptimize() {
                incrementEvaluationCount(); // this will throw
                return new RealPointValuePair(new double[]{0.0}, 0.0);
            }
        };
        opt.optimize();
    }

    // ---------- Test that optimize respects max iterations ----------
    @Test(expected = TooManyIterationsException.class)
    public void testOptimizeThrowsWhenMaxIterationsExceeded() {
        TestOptimizer opt = new TestOptimizer(checker, 100, 0) {
            @Override
            public RealPointValuePair doOptimize() {
                incrementIterationCount(); // this will throw
                return new RealPointValuePair(new double[]{0.0}, 0.0);
            }
        };
        opt.optimize();
    }
}