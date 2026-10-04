package org.apache.commons.math.optimization.direct;

import org.apache.commons.math.analysis.MultivariateRealFunction;
import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.RealPointValuePair;
import org.apache.commons.math.optimization.SimpleScalarChecker;
import org.junit.Test;

import static org.junit.Assert.*;

public class MultiDirectionalTest {

    private static class QuadraticFunction implements MultivariateRealFunction {
        public double value(double[] x) {
            double res = 0.0;
            for (double val : x) {
                res += val * val;
            }
            return res;
        }
    }

    private static class LinearFunction implements MultivariateRealFunction {
        public double value(double[] x) {
            return x[0] + 2.0 * x[1];
        }
    }

    @Test
    public void testDefaultConstructor() {
        MultiDirectional md = new MultiDirectional();
        assertNotNull(md);
    }

    @Test
    public void testParametersConstructor() {
        MultiDirectional md = new MultiDirectional(1.5, 2.5);
        assertNotNull(md);
    }

    @Test
    public void testOptimizeQuadratic() throws Exception {
        MultiDirectional md = new MultiDirectional();
        md.setMaxEvaluations(1000);
        md.setMaxIterations(1000);
        md.setConvergenceChecker(new SimpleScalarChecker(1e-6, 1e-6));

        MultivariateRealFunction func = new QuadraticFunction();
        double[] init = new double[] { 1.0, 1.0 };

        RealPointValuePair result = md.optimize(func, GoalType.MINIMIZE, init);
        assertNotNull(result);
        assertEquals(0.0, result.getValue(), 1e-2);
        for (double coord : result.getPoint()) {
            assertEquals(0.0, coord, 1e-1);
        }
    }

    @Test
    public void testOptimizeLinearMaximize() throws Exception {
        // Test with different coefficients, reflection and expansion paths
        MultiDirectional md = new MultiDirectional(2.0, 0.5);
        md.setMaxEvaluations(500);
        md.setMaxIterations(500);

        MultivariateRealFunction func = new LinearFunction();
        double[] init = new double[] { 0.0, 0.0 };

        RealPointValuePair result = md.optimize(func, GoalType.MAXIMIZE, init);
        assertNotNull(result);
    }

    @Test
    public void testMultiDirectionalIterationLogic() throws Exception {
        // Directly exercise iterateModel which contains the core MultiDirectional logic
        MultiDirectional md = new MultiDirectional(1.0, 2.0);
        md.setMaxEvaluations(100);
        md.setMaxIterations(2);

        MultivariateRealFunction func = new QuadraticFunction();
        double[] init = new double[] { 5.0, 5.0 };

        RealPointValuePair result = md.optimize(func, GoalType.MINIMIZE, init);
        assertNotNull(result);
    }
}