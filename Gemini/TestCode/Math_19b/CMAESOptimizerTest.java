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
import org.junit.Test;

import static org.junit.Assert.*;

public class CMAESOptimizerTest {

    @Test
    public void testConstraintsOutOfRangeLower() {
        // Math-19 bug pattern often involves bounds checking and boundary checks for input parameters/constraints
        CMAESOptimizer optimizer = new CMAESOptimizer();
        MultivariateFunction func = new MultivariateFunction() {
            public double value(double[] point) {
                return point[0] * point[0];
            }
        };

        double[] startPoint = new double[] { 0.0 };
        double[] lowerBound = new double[] { 1.0 }; // startPoint < lowerBound
        double[] upperBound = new double[] { 5.0 };
        double[] inputSigma = new double[] { 0.1 };

        try {
            optimizer.optimize(100, func, GoalType.MINIMIZE, startPoint, lowerBound, upperBound, inputSigma);
            fail("Expected OutOfRangeException or similar boundary exception");
        } catch (Exception e) {
            // Expected exception due to startPoint out of bounds
            assertNotNull(e);
        }
    }

    @Test
    public void testConstraintsOutOfRangeUpper() {
        CMAESOptimizer optimizer = new CMAESOptimizer();
        MultivariateFunction func = new MultivariateFunction() {
            public double value(double[] point) {
                return point[0] * point[0];
            }
        };

        double[] startPoint = new double[] { 6.0 };
        double[] lowerBound = new double[] { 1.0 };
        double[] upperBound = new double[] { 5.0 }; // startPoint > upperBound
        double[] inputSigma = new double[] { 0.1 };

        try {
            optimizer.optimize(100, func, GoalType.MINIMIZE, startPoint, lowerBound, upperBound, inputSigma);
            fail("Expected OutOfRangeException or similar boundary exception");
        } catch (Exception e) {
            assertNotNull(e);
        }
    }

    @Test
    public void testSigmaOutOfRange() {
        CMAESOptimizer optimizer = new CMAESOptimizer();
        MultivariateFunction func = new MultivariateFunction() {
            public double value(double[] point) {
                return point[0] * point[0];
            }
        };

        double[] startPoint = new double[] { 2.0 };
        double[] lowerBound = new double[] { 1.0 };
        double[] upperBound = new double[] { 5.0 };
        double[] inputSigma = new double[] { 5.0 }; // sigma >= upperBound - lowerBound (should trigger exception in Math-19)

        try {
            optimizer.optimize(100, func, GoalType.MINIMIZE, startPoint, lowerBound, upperBound, inputSigma);
            fail("Expected OutOfRangeException for sigma");
        } catch (Exception e) {
            assertNotNull(e);
        }
    }

    @Test
    public void testSigmaNegative() {
        CMAESOptimizer optimizer = new CMAESOptimizer();
        MultivariateFunction func = new MultivariateFunction() {
            public double value(double[] point) {
                return point[0] * point[0];
            }
        };

        double[] startPoint = new double[] { 2.0 };
        double[] lowerBound = new double[] { 1.0 };
        double[] upperBound = new double[] { 5.0 };
        double[] inputSigma = new double[] { -0.1 }; // Negative sigma

        try {
            optimizer.optimize(100, func, GoalType.MINIMIZE, startPoint, lowerBound, upperBound, inputSigma);
            fail("Expected exception for negative sigma");
        } catch (Exception e) {
            assertNotNull(e);
        }
    }

    @Test
    public void testDimensionsMismatchBounds() {
        CMAESOptimizer optimizer = new CMAESOptimizer();
        MultivariateFunction func = new MultivariateFunction() {
            public double value(double[] point) {
                return point[0] * point[0] + point[1] * point[1];
            }
        };

        double[] startPoint = new double[] { 2.0, 2.0 };
        double[] lowerBound = new double[] { 1.0 }; // Mismatched dimension
        double[] upperBound = new double[] { 5.0, 5.0 };
        double[] inputSigma = new double[] { 0.5, 0.5 };

        try {
            optimizer.optimize(100, func, GoalType.MINIMIZE, startPoint, lowerBound, upperBound, inputSigma);
            fail("Expected DimensionMismatchException");
        } catch (DimensionMismatchException e) {
            assertNotNull(e);
        } catch (Exception e) {
            assertNotNull(e);
        }
    }

    @Test
    public void testDimensionsMismatchSigma() {
        CMAESOptimizer optimizer = new CMAESOptimizer();
        MultivariateFunction func = new MultivariateFunction() {
            public double value(double[] point) {
                return point[0] * point[0];
            }
        };

        double[] startPoint = new double[] { 2.0 };
        double[] lowerBound = new double[] { 1.0 };
        double[] upperBound = new double[] { 5.0 };
        double[] inputSigma = new double[] { 0.5, 0.5 }; // Mismatched dimension

        try {
            optimizer.optimize(100, func, GoalType.MINIMIZE, startPoint, lowerBound, upperBound, inputSigma);
            fail("Expected DimensionMismatchException");
        } catch (DimensionMismatchException e) {
            assertNotNull(e);
        } catch (Exception e) {
            assertNotNull(e);
        }
    }

    @Test
    public void testOptimizationSphereFunction() {
        // Standard successful optimization path
        CMAESOptimizer optimizer = new CMAESOptimizer(
                15,
                Double.NaN,
                true,
                0,
                7,
                new MersenneTwister(123),
                false,
                new SimpleValueChecker(1e-12, 1e-13)
        );

        MultivariateFunction sphere = new MultivariateFunction() {
            public double value(double[] x) {
                double sum = 0;
                for (double val : x) {
                    sum += val * val;
                }
                return sum;
            }
        };

        double[] startPoint = new double[] { 3.0, 4.0 };
        double[] lowerBound = new double[] { -10.0, -10.0 };
        double[] upperBound = new double[] { 10.0, 10.0 };
        double[] inputSigma = new double[] { 1.0, 1.0 };

        PointValuePair result = optimizer.optimize(
                1000,
                sphere,
                GoalType.MINIMIZE,
                startPoint,
                lowerBound,
                upperBound,
                inputSigma
        );

        assertNotNull(result);
        assertTrue(result.getValue() < 1e-3);
    }

    @Test
    public void testNullBoundsOptimization() {
        // Optimization without explicit bounds (null bounds)
        CMAESOptimizer optimizer = new CMAESOptimizer();

        MultivariateFunction sphere = new MultivariateFunction() {
            public double value(double[] x) {
                double sum = 0;
                for (double val : x) {
                    sum += val * val;
                }
                return sum;
            }
        };

        double[] startPoint = new double[] { 1.0, 1.0 };
        double[] inputSigma = new double[] { 0.3, 0.3 };

        PointValuePair result = optimizer.optimize(
                500,
                sphere,
                GoalType.MINIMIZE,
                startPoint,
                null,
                null,
                inputSigma
        );

        assertNotNull(result);
        assertTrue(result.getValue() < 0.1);
    }
}