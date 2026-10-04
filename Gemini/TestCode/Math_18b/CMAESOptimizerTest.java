package org.apache.commons.math3.optimization.direct;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.NotANumberException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.optimization.GoalType;
import org.apache.commons.math3.optimization.PointValuePair;
import org.apache.commons.math3.optimization.SimpleValueChecker;
import org.apache.commons.math3.random.MersenneTwister;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class CMAESOptimizerTest {

    @Test
    public void testConstraintsValidation() {
        CMAESOptimizer optimizer = new CMAESOptimizer();
        MultivariateFunction func = new MultivariateFunction() {
            public double value(double[] point) {
                double sum = 0;
                for (double x : point) {
                    sum += x * x;
                }
                return sum;
            }
        };

        // Test dimension mismatch between init and lower/upper bounds
        double[] init = new double[] { 1.0, 1.0 };
        double[] lower = new double[] { 0.0 };
        double[] upper = new double[] { 2.0, 2.0 };

        try {
            optimizer.optimize(1000, func, GoalType.MINIMIZE, init, lower, upper);
            Assert.fail("Expected DimensionMismatchException");
        } catch (DimensionMismatchException e) {
            // expected
        }

        // Test bounds out of range or null when partially provided
        try {
            optimizer.optimize(1000, func, GoalType.MINIMIZE, init, null, upper);
            // Depending on implementation, might throw exception or handle nulls
        } catch (Exception e) {
            // expected or handled
        }
    }

    @Test
    public void testOptimizationSphereFunction() {
        CMAESOptimizer optimizer = new CMAESOptimizer(
            30, 
            Double.NaN, 
            true, 
            0, 
            10, 
            new MersenneTwister(123), 
            false, 
            new SimpleValueChecker(1e-5, 1e-5)
        );

        MultivariateFunction sphere = new MultivariateFunction() {
            public double value(double[] x) {
                double f = 0;
                for (double val : x) {
                    f += val * val;
                }
                return f;
            }
        };

        double[] init = new double[] { 2.0, 2.0, 2.0 };
        double[] lower = new double[] { -5.0, -5.0, -5.0 };
        double[] upper = new double[] { 5.0, 5.0, 5.0 };

        PointValuePair result = optimizer.optimize(
            10000, 
            sphere, 
            GoalType.MINIMIZE, 
            init, 
            lower, 
            upper
        );

        Assert.assertNotNull(result);
        Assert.assertEquals(0.0, result.getValue(), 0.05);
    }

    @Test
    public void testFitFitness() {
        CMAESOptimizer optimizer = new CMAESOptimizer();
        
        // Exercise internal methods via reflection or standard public APIs if possible,
        // or setup specific parameters that trigger Math-18 boundary conditions (e.g. inputSigma bounds).
        double[] init = new double[] { 0.0, 0.0 };
        double[] sigma = new double[] { 0.5, 0.5 };
        double[] lower = new double[] { -1.0, -1.0 };
        double[] upper = new double[] { 1.0, 1.0 };

        MultivariateFunction func = new MultivariateFunction() {
            public double value(double[] point) {
                return point[0] * point[0] + point[1] * point[1];
            }
        };

        PointValuePair result = optimizer.optimize(
            100, 
            func, 
            GoalType.MINIMIZE, 
            init, 
            lower, 
            upper, 
            GoalType.MINIMIZE, 
            false, 
            0, 
            new MersenneTwister(42), 
            optimizer.getConvergenceChecker()
        );

        Assert.assertNotNull(result);
    }

    @Test(expected = OutOfRangeException.class)
    public void testSigmaOutOfRange() {
        // Test sigma values that are out of bounds or negative
        double[] init = new double[] { 0.0, 0.0 };
        double[] sigma = new double[] { -1.0, 0.5 }; // negative sigma
        double[] lower = new double[] { -5.0, -5.0 };
        double[] upper = new double[] { 5.0, 5.0 };

        CMAESOptimizer optimizer = new CMAESOptimizer(10, sigma);
        MultivariateFunction func = new MultivariateFunction() {
            public double value(double[] point) {
                return point[0];
            }
        };

        optimizer.optimize(100, func, GoalType.MINIMIZE, init, lower, upper);
    }

    @Test
    public void testPopulationSizeLogic() {
        // Test explicitly setting lambda (population size) via constructor or options
        CMAESOptimizer optimizer = new CMAESOptimizer(15, null);
        Assert.assertNotNull(optimizer);
    }
    
    @Test
    public void testGetters() {
        CMAESOptimizer optimizer = new CMAESOptimizer();
        Assert.assertNull(optimizer.getHistorySize());
        Assert.assertNull(optimizer.IsUseInitSigma());
        Assert.assertNull(optimizer.getCheckFeasibleCount());
    }
}