package org.apache.commons.math.optimization.univariate;

import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.exception.MathUserException;
import org.apache.commons.math.exception.NullArgumentException;
import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.RealPointValuePair;
import org.apache.commons.math.random.MersenneTwister;
import org.apache.commons.math.random.RandomGenerator;
import org.junit.Assert;
import org.junit.Test;

import java.util.Comparator;

public class MultiStartUnivariateRealOptimizerTest {

    private static final double EPSILON = 1e-7;

    private static class DummyOptimizer implements UnivariateRealOptimizer {
        private int evaluations = 0;
        private int iterations = 0;
        private RealPointValuePair optimum;

        public DummyOptimizer(RealPointValuePair optimum) {
            this.optimum = optimum;
        }

        public void setMaxEvaluations(int maxEvaluations) {}
        public int getMaxEvaluations() { return 100; }
        public int getEvaluations() { return evaluations; }
        public void setMaxIterations(int maxIterations) {}
        public int getMaxIterations() { return 100; }
        public int getIterations() { return iterations; }

        public RealPointValuePair optimize(UnivariateRealFunction f, GoalType goal, double min, double max) throws MathUserException {
            evaluations++;
            iterations++;
            // Return a result based on min to simulate different starts giving different results
            double point = min + (max - min) * 0.5;
            double value = f.value(point);
            return new RealPointValuePair(point, value);
        }

        public RealPointValuePair optimize(UnivariateRealFunction f, GoalType goal, double min, double max, double startValue) throws MathUserException {
            evaluations++;
            iterations++;
            double value = f.value(startValue);
            return new RealPointValuePair(startValue, value);
        }

        public RealPointValuePair getOptimum() {
            return optimum;
        }

        public void setConvergenceChecker(org.apache.commons.math.optimization.ConvergenceChecker<RealPointValuePair> checker) {}
        public org.apache.commons.math.optimization.ConvergenceChecker<RealPointValuePair> getConvergenceChecker() { return null; }
    }

    private static class QuadraticFunction implements UnivariateRealFunction {
        public double value(double x) {
            return (x - 2.0) * (x - 2.0);
        }
    }

    @Test(expected = NullArgumentException.class)
    public void testNullOptimizer() throws Exception {
        UnivariateRealFunction f = new QuadraticFunction();
        RandomGenerator rng = new MersenneTwister();
        new MultiStartUnivariateRealOptimizer(null, 10, rng);
    }

    @Test(expected = NullArgumentException.class)
    public void testNullRng() throws Exception {
        UnivariateRealFunction f = new QuadraticFunction();
        RealPointValuePair dummyOpt = new RealPointValuePair(0.0, 0.0);
        UnivariateRealOptimizer underlying = new DummyOptimizer(dummyOpt);
        new MultiStartUnivariateRealOptimizer(underlying, 10, null);
    }

    @Test
    public void testGettersAndSetters() {
        RealPointValuePair dummyOpt = new RealPointValuePair(0.0, 0.0);
        UnivariateRealOptimizer underlying = new DummyOptimizer(dummyOpt);
        RandomGenerator rng = new MersenneTwister();
        MultiStartUnivariateRealOptimizer optimizer = new MultiStartUnivariateRealOptimizer(underlying, 5, rng);

        optimizer.setMaxEvaluations(200);
        Assert.assertEquals(200, optimizer.getMaxEvaluations());

        optimizer.setMaxIterations(150);
        Assert.assertEquals(150, optimizer.getMaxIterations());

        Assert.assertNull(optimizer.getConvergenceChecker());
        optimizer.setConvergenceChecker(null);
        Assert.assertNull(optimizer.getConvergenceChecker());
    }

    @Test
    public void testOptimizeMinimization() throws Exception {
        UnivariateRealFunction f = new QuadraticFunction();
        RealPointValuePair dummyOpt = new RealPointValuePair(0.0, 0.0);
        UnivariateRealOptimizer underlying = new DummyOptimizer(dummyOpt);
        RandomGenerator rng = new MersenneTwister(123);

        MultiStartUnivariateRealOptimizer optimizer = new MultiStartUnivariateRealOptimizer(underlying, 5, rng);
        RealPointValuePair result = optimizer.optimize(f, GoalType.MINIMIZE, 0.0, 5.0);

        Assert.assertNotNull(result);
        RealPointValuePair[] optima = optimizer.getOptima();
        Assert.assertNotNull(optima);
        Assert.assertEquals(5, optima.length);
        
        // Check evaluations tracking
        Assert.assertTrue(optimizer.getEvaluations() > 0);
        Assert.assertTrue(optimizer.getIterations() > 0);
    }

    @Test
    public void testOptimizeWithStartValue() throws Exception {
        UnivariateRealFunction f = new QuadraticFunction();
        RealPointValuePair dummyOpt = new RealPointValuePair(0.0, 0.0);
        UnivariateRealOptimizer underlying = new DummyOptimizer(dummyOpt);
        RandomGenerator rng = new MersenneTwister(123);

        MultiStartUnivariateRealOptimizer optimizer = new MultiStartUnivariateRealOptimizer(underlying, 3, rng);
        RealPointValuePair result = optimizer.optimize(f, GoalType.MINIMIZE, 0.0, 5.0, 2.5);

        Assert.assertNotNull(result);
        RealPointValuePair[] optima = optimizer.getOptima();
        Assert.assertEquals(3, optima.length);
    }

    @Test(expected = IllegalStateException.class)
    public void testGetOptimaBeforeOptimize() {
        RealPointValuePair dummyOpt = new RealPointValuePair(0.0, 0.0);
        UnivariateRealOptimizer underlying = new DummyOptimizer(dummyOpt);
        RandomGenerator rng = new MersenneTwister();
        MultiStartUnivariateRealOptimizer optimizer = new MultiStartUnivariateRealOptimizer(underlying, 5, rng);
        optimizer.getOptima();
    }

    @Test
    public void testMaximizationSorting() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return - (x - 2.0) * (x - 2.0) + 4.0; // max at x = 2.0
            }
        };
        RealPointValuePair dummyOpt = new RealPointValuePair(0.0, 0.0);
        UnivariateRealOptimizer underlying = new DummyOptimizer(dummyOpt);
        RandomGenerator rng = new MersenneTwister(456);

        MultiStartUnivariateRealOptimizer optimizer = new MultiStartUnivariateRealOptimizer(underlying, 4, rng);
        RealPointValuePair result = optimizer.optimize(f, GoalType.MAXIMIZE, -5.0, 5.0);

        Assert.assertNotNull(result);
        RealPointValuePair[] optima = optimizer.getOptima();
        // Verify sorting for MAXIMIZE
        for (int i = 0; i < optima.length - 1; i++) {
            Assert.assertTrue(optima[i].getValue() >= optima[i+1].getValue());
        }
    }
}