package org.apache.commons.math.optimization;

import org.apache.commons.math.random.MersenneTwister;
import org.apache.commons.math.random.RandomGenerator;
import org.junit.Assert;
import org.junit.Test;

public class MultiStartUnivariateRealOptimizerTest {

    private static final double EPSILON = 1e-7;

    private static class DummyOptimizer implements UnivariateRealOptimizer {
        private int evaluations = 0;
        private int iterationCount = 0;
        private double optimum = 0.0;
        private double result = 0.0;

        @Override
        public void setMaxEvaluations(int maxEvaluations) {}

        @Override
        public int getMaxEvaluations() {
            return 100;
        }

        @Override
        public int getEvaluations() {
            return evaluations;
        }

        @Override
        public void setMaximalIterationCount(int count) {}

        @Override
        public int getMaximalIterationCount() {
            return 100;
        }

        @Override
        public int getMaximalIterations() {
            return 100;
        }

        @Override
        public void setMaximalIterations(int count) {}

        @Override
        public void resetMaximalIterationCount() {}

        @Override
        public void setAbsoluteAccuracy(double accuracy) {}

        @Override
        public double getAbsoluteAccuracy() {
            return 1e-6;
        }

        @Override
        public void resetAbsoluteAccuracy() {}

        @Override
        public void setRelativeAccuracy(double accuracy) {}

        @Override
        public double getRelativeAccuracy() {
            return 1e-6;
        }

        @Override
        public void resetRelativeAccuracy() {}

        @Override
        public void addMaximalIterationCount(int count) {}

        @Override
        public void incrementIterationsCounter() throws MaxIterationsExceededException {
            iterationCount++;
        }

        @Override
        public int getIterationCount() {
            return iterationCount;
        }

        @Override
        public double getResult() {
            return result;
        }

        @Override
        public double getOptimum() {
            return optimum;
        }

        @Override
        public double optimize(UnivariateRealFunction f, GoalType goal, double min, double max)
                throws MaxIterationsExceededException, FunctionEvaluationException {
            evaluations++;
            // Simple deterministic function depending on start/min/max
            result = min + (max - min) * 0.5;
            try {
                optimum = f.value(result);
            } catch (Exception e) {
                throw new FunctionEvaluationException(e, result);
            }
            return result;
        }

        @Override
        public double optimize(UnivariateRealFunction f, GoalType goal, double min, double max, double startValue)
                throws MaxIterationsExceededException, FunctionEvaluationException {
            evaluations++;
            result = startValue;
            try {
                optimum = f.value(result);
            } catch (Exception e) {
                throw new FunctionEvaluationException(e, result);
            }
            return result;
        }
    }

    @Test
    public void testConstructorsAndGetters() {
        UnivariateRealOptimizer underlying = new DummyOptimizer();
        RandomGenerator rng = new MersenneTwister(0L);
        MultiStartUnivariateRealOptimizer multi = new MultiStartUnivariateRealOptimizer(underlying, 5, rng);

        Assert.assertEquals(5, multi.getStarts());
        Assert.assertEquals(rng, multi.getGenerators());

        // Test methods delegated or inherited
        multi.setMaxEvaluations(50);
        Assert.assertEquals(50, multi.getMaxEvaluations());
        
        multi.setMaximalIterationCount(10);
        Assert.assertEquals(10, multi.getMaximalIterationCount());

        multi.setAbsoluteAccuracy(1e-4);
        Assert.assertEquals(1e-4, multi.getAbsoluteAccuracy(), EPSILON);

        multi.setRelativeAccuracy(1e-4);
        Assert.assertEquals(1e-4, multi.getRelativeAccuracy(), EPSILON);
        
        multi.resetAbsoluteAccuracy();
        multi.resetRelativeAccuracy();
        multi.resetMaximalIterationCount();
        multi.addMaximalIterationCount(1);
    }

    @Test(expected = NullPointerException.class)
    public void testOptimizeNullFunction() throws Exception {
        UnivariateRealOptimizer underlying = new DummyOptimizer();
        RandomGenerator rng = new MersenneTwister(0L);
        MultiStartUnivariateRealOptimizer multi = new MultiStartUnivariateRealOptimizer(underlying, 3, rng);

        multi.optimize(null, GoalType.MINIMIZE, 0.0, 1.0);
    }

    @Test
    public void testOptimizeSuccess() throws Exception {
        UnivariateRealOptimizer underlying = new DummyOptimizer();
        RandomGenerator rng = new MersenneTwister(0L);
        MultiStartUnivariateRealOptimizer multi = new MultiStartUnivariateRealOptimizer(underlying, 3, rng);

        UnivariateRealFunction f = new UnivariateRealFunction() {
            @Override
            public double value(double x) throws FunctionEvaluationException {
                return (x - 2.0) * (x - 2.0);
            }
        };

        double result = multi.optimize(f, GoalType.MINIMIZE, 0.0, 5.0);
        Assert.assertTrue(result >= 0.0 && result <= 5.0);
        Assert.assertNotNull(multi.getOptimum());
        Assert.assertNotNull(multi.getResult());

        double[] optima = multi.getOptima();
        Assert.assertNotNull(optima);
        Assert.assertEquals(3, optima.length);

        double[] searchValues = multi.getSearchValues();
        Assert.assertNotNull(searchValues);
        Assert.assertEquals(3, searchValues.length);
    }

    @Test
    public void testOptimizeWithStartValue() throws Exception {
        UnivariateRealOptimizer underlying = new DummyOptimizer();
        RandomGenerator rng = new MersenneTwister(0L);
        MultiStartUnivariateRealOptimizer multi = new MultiStartUnivariateRealOptimizer(underlying, 3, rng);

        UnivariateRealFunction f = new UnivariateRealFunction() {
            @Override
            public double value(double x) throws FunctionEvaluationException {
                return x * x;
            }
        };

        double result = multi.optimize(f, GoalType.MINIMIZE, 0.0, 10.0, 5.0);
        Assert.assertTrue(result >= 0.0 && result <= 10.0);
        
        double[] optima = multi.getOptima();
        Assert.assertEquals(3, optima.length);
        
        double[] searchValues = multi.getSearchValues();
        Assert.assertEquals(3, searchValues.length);
    }

    @Test(expected = IllegalStateException.class)
    public void testGetOptimaBeforeOptimize() {
        UnivariateRealOptimizer underlying = new DummyOptimizer();
        RandomGenerator rng = new MersenneTwister(0L);
        MultiStartUnivariateRealOptimizer multi = new MultiStartUnivariateRealOptimizer(underlying, 3, rng);
        multi.getOptima();
    }

    @Test(expected = IllegalStateException.class)
    public void testGetSearchValuesBeforeOptimize() {
        UnivariateRealOptimizer underlying = new DummyOptimizer();
        RandomGenerator rng = new MersenneTwister(0L);
        MultiStartUnivariateRealOptimizer multi = new MultiStartUnivariateRealOptimizer(underlying, 3, rng);
        multi.getSearchValues();
    }

    @Test
    public void testMaximizationAndSorting() throws Exception {
        UnivariateRealOptimizer underlying = new DummyOptimizer();
        RandomGenerator rng = new MersenneTwister(0L);
        MultiStartUnivariateRealOptimizer multi = new MultiStartUnivariateRealOptimizer(underlying, 4, rng);

        UnivariateRealFunction f = new UnivariateRealFunction() {
            @Override
            public double value(double x) throws FunctionEvaluationException {
                // Negative parabola to test maximization sorting
                return - (x - 2.0) * (x - 2.0);
            }
        };

        double result = multi.optimize(f, GoalType.MAXIMIZE, -5.0, 5.0);
        Assert.assertTrue(result >= -5.0 && result <= 5.0);
        
        double[] optima = multi.getOptima();
        Assert.assertEquals(4, optima.length);
        // Verify sorting for MAXIMIZE (best values first, i.e., highest values)
        for (int i = 0; i < optima.length - 1; i++) {
            Assert.assertTrue(optima[i] >= optima[i + 1]);
        }
    }

    @Test
    public void testMinimizationAndSorting() throws Exception {
        UnivariateRealOptimizer underlying = new DummyOptimizer();
        RandomGenerator rng = new MersenneTwister(0L);
        MultiStartUnivariateRealOptimizer multi = new MultiStartUnivariateRealOptimizer(underlying, 4, rng);

        UnivariateRealFunction f = new UnivariateRealFunction() {
            @Override
            public double value(double x) throws FunctionEvaluationException {
                return (x - 2.0) * (x - 2.0);
            }
        };

        double result = multi.optimize(f, GoalType.MINIMIZE, -5.0, 5.0);
        Assert.assertTrue(result >= -5.0 && result <= 5.0);
        
        double[] optima = multi.getOptima();
        Assert.assertEquals(4, optima.length);
        // Verify sorting for MINIMIZE (best values first, i.e., lowest values)
        for (int i = 0; i < optima.length - 1; i++) {
            Assert.assertTrue(optima[i] <= optima[i + 1]);
        }
    }
}