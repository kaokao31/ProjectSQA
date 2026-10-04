package org.apache.commons.math3.optimization.direct;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.exception.NotANumberException;
import org.apache.commons.math3.exception.NumberIsTooLargeException;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.optimization.GoalType;
import org.apache.commons.math3.optimization.SimpleValueChecker;
import org.apache.commons.math3.random.MersenneTwister;
import org.junit.Test;

import static org.junit.Assert.*;

public class CMAESOptimizerTest {

    @Test
    public void testCMAESOptimizerSimple() {
        CMAESOptimizer optimizer = new CMAESOptimizer(
                15, 1.0, true, 10, 1,
                new MersenneTwister(123), false,
                new SimpleValueChecker(1e-6, 1e-6)
        );

        MultivariateFunction objectiveFunction = new MultivariateFunction() {
            public double value(double[] point) {
                double sum = 0.0;
                for (double p : point) {
                    sum += p * p;
                }
                return sum;
            }
        };

        double[] startPoint = {new Double(1.0), new Double(1.0)};
        double[] lowerBound = {-5.0, -5.0};
        double[] upperBound = {5.0, 5.0};

        org.apache.commons.math3.optimization.PointValuePair result = optimizer.optimize(
                10000, objectiveFunction, GoalType.MINIMIZE, startPoint, lowerBound, upperBound
        );

        assertNotNull(result);
        assertEquals(0.0, result.getValue(), 0.05);
    }

    @Test
    public void testBoundaryFixingBug20() {
        // Bug 20 in Commons Math involves repairing/fixing parameters when boundaries are set
        // and the optimal or initial point falls outside or requires scaling.
        CMAESOptimizer optimizer = new CMAESOptimizer();

        MultivariateFunction objectiveFunction = new MultivariateFunction() {
            public double value(double[] point) {
                double sum = 0.0;
                for (double p : point) {
                    sum += (p - 3.0) * (p - 3.0);
                }
                return sum;
            }
        };

        double[] startPoint = {0.0, 0.0};
        double[] lowerBound = {2.0, 2.0};
        double[] upperBound = {5.0, 5.0};

        org.apache.commons.math3.optimization.PointValuePair result = optimizer.optimize(
                1000, objectiveFunction, GoalType.MINIMIZE, startPoint, lowerBound, upperBound
        );

        assertNotNull(result);
        assertTrue(result.getPoint()[0] >= 2.0);
        assertTrue(result.getPoint()[1] >= 2.0);
    }

    @Test(expected = NumberIsTooSmallException.class)
    public void testSigmaTooSmall() {
        double[] sigma = { -0.1 };
        CMAESOptimizer optimizer = new CMAESOptimizer(10, sigma);
        
        MultivariateFunction objectiveFunction = new MultivariateFunction() {
            public double value(double[] point) {
                return point[0] * point[0];
            }
        };

        optimizer.optimize(
                100, objectiveFunction, GoalType.MINIMIZE, new double[] { 0.0 }, new double[] { -1.0 }, new double[] { 1.0 }
        );
    }

    @Test(expected = OutOfRangeException.class)
    public void testSigmaOutOfRange() {
        // Sigma larger than upper - lower bound
        double[] sigma = { 10.0 };
        CMAESOptimizer optimizer = new CMAESOptimizer(10, sigma);

        MultivariateFunction objectiveFunction = new MultivariateFunction() {
            public double value(double[] point) {
                return point[0] * point[0];
            }
        };

        optimizer.optimize(
                100, objectiveFunction, GoalType.MINIMIZE, new double[] { 0.0 }, new double[] { -1.0 }, new double[] { 1.0 }
        );
    }
}