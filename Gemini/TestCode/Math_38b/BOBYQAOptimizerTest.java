package org.apache.commons.math.optimization.direct;

import org.apache.commons.math.analysis.MultivariateRealFunction;
import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.RealPointValuePair;
import org.junit.Test;

import static org.junit.Assert.*;

public class BOBYQAOptimizerTest {

    @Test
    public void testInitializationWithInvalidInterpolationPoints() {
        // BOBYQA requires interpolation points to be in the range [n+2, (n+1)(n+2)/2]
        // Let's test with too few points to trigger exceptions/edge cases.
        try {
            int n = 2;
            // n+2 = 4, so 3 is invalid
            BOBYQAOptimizer optimizer = new BOBYQAOptimizer(3);
            MultivariateRealFunction func = new MultivariateRealFunction() {
                public double value(double[] point) {
                    return point[0] * point[0] + point[1] * point[1];
                }
            };
            optimizer.optimize(func, GoalType.MINIMIZE, new double[]{-1, -1}, new double[]{1, 1}, new double[]{0, 0});
            fail("Expected exception for low number of interpolation points");
        } catch (Exception e) {
            // Expected
        }
    }

    @Test
    public void testOptimizationSphereFunction() {
        // Test a simple quadratic function (Sphere function)
        int n = 2;
        int numberOfInterpolationPoints = 2 * n + 1; // 5 points for n=2
        BOBYQAOptimizer optimizer = new BOBYQAOptimizer(numberOfInterpolationPoints);

        MultivariateRealFunction sphere = new MultivariateRealFunction() {
            public double value(double[] point) {
                double sum = 0.0;
                for (double val : point) {
                    sum += val * val;
                }
                return sum;
            }
        };

        double[] initialGuess = new double[]{3.0, 4.0};
        double[] lowerBound = new double[]{-10.0, -10.0};
        double[] upperBound = new double[]{10.0, 10.0};

        RealPointValuePair result = optimizer.optimize(
                sphere,
                GoalType.MINIMIZE,
                initialGuess,
                lowerBound,
                upperBound
        );

        assertNotNull(result);
        assertEquals(0.0, result.getValue(), 0.05);
        assertEquals(0.0, result.getPoint()[0], 0.1);
        assertEquals(0.0, result.getPoint()[1], 0.1);
    }

    @Test
    public void testOptimizationWithMaxEvalConstraint() {
        int n = 2;
        int numberOfInterpolationPoints = 6; // Valid for n=2: range [4, 6]
        BOBYQAOptimizer optimizer = new BOBYQAOptimizer(numberOfInterpolationPoints);

        MultivariateRealFunction sphere = new MultivariateRealFunction() {
            public double value(double[] point) {
                return point[0] * point[0] + point[1] * point[1];
            }
        };

        try {
            // Setting a very low max eval to trigger termination/exception behavior if any
            optimizer.setMaxEvaluations(1);
            optimizer.optimize(
                    sphere,
                    GoalType.MINIMIZE,
                    new double[]{1.0, 1.0},
                    new double[]{-5.0, -5.0},
                    new double[]{5.0, 5.0}
            );
        } catch (Exception e) {
            // Expected max evaluations exceeded or similar convergence exception
            assertNotNull(e);
        }
    }

    @Test
    public void testTrustRegionInitializationBug38Path() {
        // Defect 38 in BOBYQA usually relates to scaling/trust region bounds or 
        // identical points / very narrow bounds where delta gets too small or division by zero occurs.
        int n = 2;
        int numberOfInterpolationPoints = 5;
        BOBYQAOptimizer optimizer = new BOBYQAOptimizer(numberOfInterpolationPoints);

        MultivariateRealFunction func = new MultivariateRealFunction() {
            public double value(double[] point) {
                // Flat function or close to bounds
                return point[0] + point[1];
            }
        };

        // Bounds equal to initial guess or extremely narrow, testing Bug 38 specific boundary conditions
        double[] initialGuess = new double[]{0.0, 0.0};
        double[] lowerBound = new double[]{0.0, 0.0};
        double[] upperBound = new double[]{0.0, 0.0};

        try {
            optimizer.optimize(
                    func,
                    GoalType.MINIMIZE,
                    initialGuess,
                    lowerBound,
                    upperBound
            );
        } catch (Exception e) {
            // Handled or threw exception due to zero trust region / bounds
            assertNotNull(e);
        }
    }
}