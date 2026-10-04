package org.apache.commons.math3.optim;

import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.exception.TooManyIterationsException;
import org.apache.commons.math3.util.Incrementor;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test suite for {@link BaseOptimizer}, designed for maximum coverage and fault detection
 * under JUnit 4 and Defects4J constraints.
 */
public class BaseOptimizerTest {

    /**
     * Concrete subclass of BaseOptimizer used for testing the abstract class.
     */
    private static class ConcreteOptimizer extends BaseOptimizer<PointValuePair> {
        protected ConcreteOptimizer() {
            super();
        }

        protected ConcreteOptimizer(MaxEval maxEval, MaxIter maxIter) {
            super(maxEval, maxIter);
        }

        @Override
        public PointValuePair optimize(OptimizationData... optData) {
            // Simulate an iterative/evaluative process that uses the incrementors
            super.parseOptimizationData(optData);
            
            // Increment evaluation and iteration to test callbacks and limits
            try {
                super.incrementEvaluationCount();
                super.incrementIterationCount();
            } catch (Exception e) {
                // Ignore for mock execution flow unless testing limits
            }
            
            return new PointValuePair(new double[] { 0.0 }, 0.0);
        }
    }

    @Test
    public void testDefaultConstructor() {
        ConcreteOptimizer optimizer = new ConcreteOptimizer();
        Assert.assertEquals(0, optimizer.getMaxEvaluations());
        Assert.assertEquals(0, optimizer.getEvaluations());
        Assert.assertEquals(0, optimizer.getMaxIterations());
        Assert.assertEquals(0, optimizer.getIterations());
    }

    @Test
    public void testParameterizedConstructorWithLimits() {
        MaxEval maxEval = new MaxEval(100);
        MaxIter maxIter = new MaxIter(50);
        ConcreteOptimizer optimizer = new ConcreteOptimizer(maxEval, maxIter);

        Assert.assertEquals(100, optimizer.getMaxEvaluations());
        Assert.assertEquals(0, optimizer.getEvaluations());
        Assert.assertEquals(50, optimizer.getMaxIterations());
        Assert.assertEquals(0, optimizer.getIterations());
    }

    @Test
    public void testOptimizeWithOptimizationData() {
        ConcreteOptimizer optimizer = new ConcreteOptimizer();
        MaxEval maxEval = new MaxEval(10);
        MaxIter maxIter = new MaxIter(5);

        PointValuePair result = optimizer.optimize(maxEval, maxIter);
        Assert.assertNotNull(result);
        Assert.assertEquals(10, optimizer.getMaxEvaluations());
        Assert.assertEquals(5, optimizer.getMaxIterations());
    }

    @Test
    public void testParseOptimizationDataNull() {
        ConcreteOptimizer optimizer = new ConcreteOptimizer();
        // Should handle null gracefully or via super implementation (if allowed)
        // Passing null array or containing null elements
        OptimizationData[] data = null;
        try {
            optimizer.optimize(data);
            // If null is accepted or handled by parseOptimizationData
        } catch (NullPointerException e) {
            // Expected if null array is strictly forbidden, but let's check behavior
        }
    }

    @Test
    public void testParseOptimizationDataWithNullElement() {
        ConcreteOptimizer optimizer = new ConcreteOptimizer();
        OptimizationData[] data = new OptimizationData[] { null };
        try {
            optimizer.optimize(data);
        } catch (NullPointerException e) {
            // Expected behavior when parsing a null optimization data item
        }
    }

    @Test(expected = TooManyEvaluationsException.class)
    public void testMaxEvaluationsExceeded() {
        // MaxEval set to 1, but we trigger increment twice
        MaxEval maxEval = new MaxEval(1);
        ConcreteOptimizer optimizer = new ConcreteOptimizer(maxEval, new MaxIter(10));
        
        optimizer.optimize(new MaxEval(1));
        // Manually force evaluation increments exceeding max
        optimizer.incrementEvaluationCount();
        optimizer.incrementEvaluationCount();
    }

    @Test(expected = TooManyIterationsException.class)
    public void testMaxIterationsExceeded() {
        // MaxIter set to 1, but we trigger increment twice
        MaxIter maxIter = new MaxIter(1);
        ConcreteOptimizer optimizer = new ConcreteOptimizer(new MaxEval(10), maxIter);
        
        optimizer.optimize(new MaxIter(1));
        // Manually force iteration increments exceeding max
        optimizer.incrementIterationCount();
        optimizer.incrementIterationCount();
    }

    @Test
    public void testCallbacksOnMaxEvalAndMaxIter() {
        final boolean[] evalCallbackCalled = {false};
        final boolean[] iterCallbackCalled = {false};

        ConcreteOptimizer optimizer = new ConcreteOptimizer() {
            @Override
            protected void evaluationsUpdateCallback() {
                evalCallbackCalled[0] = true;
            }

            @Override
            protected void iterationsUpdateCallback() {
                iterCallbackCalled[0] = true;
            }
        };

        optimizer.optimize(new MaxEval(10), new MaxIter(10));
        
        // Call increments which should trigger callbacks if implemented
        try {
            optimizer.incrementEvaluationCount();
        } catch (Exception e) {}

        try {
            optimizer.incrementIterationCount();
        } catch (Exception e) {}

        Assert.assertTrue(evalCallbackCalled[0]);
        Assert.assertTrue(iterCallbackCalled[0]);
    }

    @Test
    public void testGetCounterGetters() {
        ConcreteOptimizer optimizer = new ConcreteOptimizer(new MaxEval(42), new MaxIter(17));
        Incrementor evalCount = optimizer.getEvaluationsCount();
        Incrementor iterCount = optimizer.getIterationsCount();

        Assert.assertNotNull(evalCount);
        Assert.assertNotNull(iterCount);
        Assert.assertEquals(42, evalCount.getMaximalCount());
        Assert.assertEquals(17, iterCount.getMaximalCount());
    }
}