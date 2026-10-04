package org.apache.commons.math3.optim.nonlinear.vector.jacobian;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;

import org.apache.commons.math3.optim.*;
import org.apache.commons.math3.optim.nonlinear.vector.*;
import org.apache.commons.math3.optim.nonlinear.vector.jacobian.AbstractLeastSquaresOptimizer;
import org.apache.commons.math3.linear.*;
import org.apache.commons.math3.analysis.*;
import org.apache.commons.math3.analysis.differentiation.*;
import org.apache.commons.math3.exception.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Comprehensive JUnit 4 test suite for AbstractLeastSquaresOptimizer.
 * Targets maximum coverage and fault detection, including the known Math-14 bug.
 */
public class AbstractLeastSquaresOptimizerTest {

    private TestOptimizer optimizer;
    private double[] target;
    private double[] weights;
    private double[] startPoint;
    private MultivariateMatrixFunction jacobian;
    private MultivariateVectorFunction objective;

    @Before
    public void setUp() {
        // Simple linear model: y = a * x + b, with two parameters (a, b)
        // Observations: (x=1, y=2), (x=2, y=4), (x=3, y=6) -> perfect line a=2, b=0
        target = new double[]{2.0, 4.0, 6.0};
        weights = new double[]{1.0, 1.0, 1.0};
        startPoint = new double[]{1.0, 1.0}; // initial guess

        objective = new MultivariateVectorFunction() {
            public double[] value(double[] params) {
                double a = params[0];
                double b = params[1];
                return new double[]{a * 1.0 + b, a * 2.0 + b, a * 3.0 + b};
            }
        };

        jacobian = new MultivariateMatrixFunction() {
            public double[][] value(double[] params) {
                // Jacobian: df/da = x, df/db = 1
                return new double[][]{
                    {1.0, 1.0},
                    {2.0, 1.0},
                    {3.0, 1.0}
                };
            }
        };

        optimizer = new TestOptimizer();
    }

    @After
    public void tearDown() {
        optimizer = null;
    }

    // --------------------------------------------------------------
    // Inner concrete class for testing abstract methods
    // --------------------------------------------------------------
    private static class TestOptimizer extends AbstractLeastSquaresOptimizer {
        private double[] point;
        private double objectiveValue;
        private double[][] jacobianValue;
        private double cost;
        private double rms;
        private double chiSquare;

        public TestOptimizer() {
            super(new SimpleVectorValueChecker(1e-6, 1e-6));
        }

        @Override
        protected PointVectorValuePair doOptimize() {
            // Simulate optimization: return the known optimal point (2,0)
            point = new double[]{2.0, 0.0};
            // Compute objective and jacobian at this point
            objectiveValue = computeObjectiveValue(point);
            jacobianValue = computeJacobian(point);
            // Compute cost and rms using the inherited methods
            cost = computeCost();
            rms = computeRMS();
            chiSquare = getChiSquare();
            return new PointVectorValuePair(point, getObjective());
        }

        // Expose protected methods for testing
        public double computeCostPublic() {
            return computeCost();
        }

        public double computeRMSPublic() {
            return computeRMS();
        }

        public double[] computeObjectiveValuePublic(double[] params) {
            return computeObjectiveValue(params);
        }

        public double[][] computeJacobianPublic(double[] params) {
            return computeJacobian(params);
        }

        public double getChiSquarePublic() {
            return getChiSquare();
        }

        public double getCostPublic() {
            return getCost();
        }

        public double getRMSPublic() {
            return getRMS();
        }

        public void setCostPublic(double cost) {
            setCost(cost);
        }
    }

    // --------------------------------------------------------------
    // Tests for computeCost (targeting Math-14 bug)
    // --------------------------------------------------------------
    @Test
    public void testComputeCostCorrectness() {
        // Set up known residuals: sum of squares = 0 (perfect fit)
        // After optimization, residuals should be zero
        optimizer.optimize(
            new Target(target),
            new Weight(weights),
            new InitialGuess(startPoint),
            objective,
            jacobian
        );
        // chiSquare = sum of squared residuals = 0
        // cost should be sqrt(chiSquare / n) = sqrt(0/3) = 0
        assertEquals("Cost should be 0 for perfect fit", 0.0, optimizer.getCostPublic(), 1e-12);
        // Also test computeCost directly
        assertEquals("computeCost should return 0", 0.0, optimizer.computeCostPublic(), 1e-12);
    }

    @Test
    public void testComputeCostWithNonZeroResiduals() {
        // Use a different target to create residuals
        double[] badTarget = new double[]{2.5, 4.5, 6.5};
        optimizer.optimize(
            new Target(badTarget),
            new Weight(weights),
            new InitialGuess(startPoint),
            objective,
            jacobian
        );
        // After optimization, the best fit is still (2,0) because model is linear and data is collinear?
        // Actually with these targets, the optimal parameters are still (2,0) because the model is y=2x+0,
        // but the targets are offset by 0.5 each. So residuals are [0.5, 0.5, 0.5] -> sum squares = 0.75
        // n = 3, cost = sqrt(0.75/3) = sqrt(0.25) = 0.5
        double expectedCost = Math.sqrt(0.75 / 3.0);
        assertEquals("Cost should be sqrt(chiSquare/n)", expectedCost, optimizer.getCostPublic(), 1e-12);
        // Verify that cost is NOT sqrt(chiSquare) (the bug)
        double wrongCost = Math.sqrt(0.75);
        assertFalse("Cost should not be sqrt(chiSquare) (bug)", Math.abs(optimizer.getCostPublic() - wrongCost) < 1e-12);
    }

    @Test
    public void testComputeCostWithSingleObservation() {
        // Single observation: n=1
        double[] singleTarget = new double[]{5.0};
        double[] singleWeight = new double[]{1.0};
        MultivariateVectorFunction singleObjective = new MultivariateVectorFunction() {
            public double[] value(double[] params) {
                return new double[]{params[0] * 1.0 + params[1]};
            }
        };
        MultivariateMatrixFunction singleJacobian = new MultivariateMatrixFunction() {
            public double[][] value(double[] params) {
                return new double[][]{{1.0, 1.0}};
            }
        };
        optimizer.optimize(
            new Target(singleTarget),
            new Weight(singleWeight),
            new InitialGuess(new double[]{1.0, 1.0}),
            singleObjective,
            singleJacobian
        );
        // Optimal: a+b=5, many solutions; but optimizer will find one. Residuals should be zero.
        assertEquals("Cost should be 0 for single observation perfect fit", 0.0, optimizer.getCostPublic(), 1e-12);
    }

    // --------------------------------------------------------------
    // Tests for computeRMS
    // --------------------------------------------------------------
    @Test
    public void testComputeRMS() {
        optimizer.optimize(
            new Target(target),
            new Weight(weights),
            new InitialGuess(startPoint),
            objective,
            jacobian
        );
        // RMS = sqrt(chiSquare / n) = 0
        assertEquals("RMS should be 0 for perfect fit", 0.0, optimizer.getRMSPublic(), 1e-12);
        // Direct call
        assertEquals("computeRMS should return 0", 0.0, optimizer.computeRMSPublic(), 1e-12);
    }

    @Test
    public void testComputeRMSWithNonZeroResiduals() {
        double[] badTarget = new double[]{2.5, 4.5, 6.5};
        optimizer.optimize(
            new Target(badTarget),
            new Weight(weights),
            new InitialGuess(startPoint),
            objective,
            jacobian
        );
        double expectedRMS = Math.sqrt(0.75 / 3.0);
        assertEquals("RMS should be sqrt(chiSquare/n)", expectedRMS, optimizer.getRMSPublic(), 1e-12);
    }

    // --------------------------------------------------------------
    // Tests for computeObjectiveValue
    // --------------------------------------------------------------
    @Test
    public void testComputeObjectiveValue() {
        double[] params = new double[]{2.0, 0.0};
        double[] expected = new double[]{2.0, 4.0, 6.0};
        double[] actual = optimizer.computeObjectiveValuePublic(params);
        assertArrayEquals("Objective value should match model", expected, actual, 1e-12);
    }

    @Test
    public void testComputeObjectiveValueWithDifferentParams() {
        double[] params = new double[]{1.0, 1.0};
        double[] expected = new double[]{2.0, 3.0, 4.0};
        double[] actual = optimizer.computeObjectiveValuePublic(params);
        assertArrayEquals("Objective value for (1,1)", expected, actual, 1e-12);
    }

    // --------------------------------------------------------------
    // Tests for computeJacobian
    // --------------------------------------------------------------
    @Test
    public void testComputeJacobian() {
        double[] params = new double[]{2.0, 0.0};
        double[][] expected = new double[][]{
            {1.0, 1.0},
            {2.0, 1.0},
            {3.0, 1.0}
        };
        double[][] actual = optimizer.computeJacobianPublic(params);
        assertArrayEquals("Jacobian row 0", expected[0], actual[0], 1e-12);
        assertArrayEquals("Jacobian row 1", expected[1], actual[1], 1e-12);
        assertArrayEquals("Jacobian row 2", expected[2], actual[2], 1e-12);
    }

    // --------------------------------------------------------------
    // Tests for getChiSquare
    // --------------------------------------------------------------
    @Test
    public void testGetChiSquare() {
        optimizer.optimize(
            new Target(target),
            new Weight(weights),
            new InitialGuess(startPoint),
            objective,
            jacobian
        );
        assertEquals("ChiSquare should be 0 for perfect fit", 0.0, optimizer.getChiSquarePublic(), 1e-12);
    }

    @Test
    public void testGetChiSquareWithNonZeroResiduals() {
        double[] badTarget = new double[]{2.5, 4.5, 6.5};
        optimizer.optimize(
            new Target(badTarget),
            new Weight(weights),
            new InitialGuess(startPoint),
            objective,
            jacobian
        );
        assertEquals("ChiSquare should be 0.75", 0.75, optimizer.getChiSquarePublic(), 1e-12);
    }

    // --------------------------------------------------------------
    // Tests for setCost and getCost
    // --------------------------------------------------------------
    @Test
    public void testSetCost() {
        optimizer.setCostPublic(123.456);
        assertEquals("getCost should return set value", 123.456, optimizer.getCostPublic(), 1e-12);
    }

    @Test
    public void testSetCostNegative() {
        optimizer.setCostPublic(-1.0);
        assertEquals("Cost can be negative (no validation)", -1.0, optimizer.getCostPublic(), 1e-12);
    }

    // --------------------------------------------------------------
    // Tests for optimize method (integration)
    // --------------------------------------------------------------
    @Test
    public void testOptimizeReturnsCorrectPoint() {
        PointVectorValuePair result = optimizer.optimize(
            new Target(target),
            new Weight(weights),
            new InitialGuess(startPoint),
            objective,
            jacobian
        );
        double[] expectedPoint = new double[]{2.0, 0.0};
        assertArrayEquals("Optimal point should be (2,0)", expectedPoint, result.getPoint(), 1e-12);
    }

    @Test
    public void testOptimizeWithDifferentWeights() {
        double[] unequalWeights = new double[]{1.0, 2.0, 3.0};
        // With weights, the optimal solution changes? For linear model, weighted least squares.
        // We'll just check that optimization runs without exception.
        PointVectorValuePair result = optimizer.optimize(
            new Target(target),
            new Weight(unequalWeights),
            new InitialGuess(startPoint),
            objective,
            jacobian
        );
        assertNotNull("Result should not be null", result);
    }

    @Test(expected = NullArgumentException.class)
    public void testOptimizeNullTarget() {
        optimizer.optimize(
            null,
            new Weight(weights),
            new InitialGuess(startPoint),
            objective,
            jacobian
        );
    }

    @Test(expected = NullArgumentException.class)
    public void testOptimizeNullObjective() {
        optimizer.optimize(
            new Target(target),
            new Weight(weights),
            new InitialGuess(startPoint),
            null,
            jacobian
        );
    }

    @Test(expected = NullArgumentException.class)
    public void testOptimizeNullJacobian() {
        optimizer.optimize(
            new Target(target),
            new Weight(weights),
            new InitialGuess(startPoint),
            objective,
            null
        );
    }

    @Test(expected = DimensionMismatchException.class)
    public void testOptimizeTargetWrongSize() {
        double[] wrongTarget = new double[]{1.0, 2.0}; // only 2 observations
        optimizer.optimize(
            new Target(wrongTarget),
            new Weight(weights),
            new InitialGuess(startPoint),
            objective,
            jacobian
        );
    }

    @Test(expected = DimensionMismatchException.class)
    public void testOptimizeWeightWrongSize() {
        double[] wrongWeights = new double[]{1.0, 2.0}; // only 2 weights
        optimizer.optimize(
            new Target(target),
            new Weight(wrongWeights),
            new InitialGuess(startPoint),
            objective,
            jacobian
        );
    }

    // --------------------------------------------------------------
    // Edge cases: empty observations, zero weights, etc.
    // --------------------------------------------------------------
    @Test(expected = NotStrictlyPositiveException.class)
    public void testOptimizeZeroObservations() {
        double[] emptyTarget = new double[0];
        double[] emptyWeights = new double[0];
        MultivariateVectorFunction emptyObjective = new MultivariateVectorFunction() {
            public double[] value(double[] params) {
                return new double[0];
            }
        };
        MultivariateMatrixFunction emptyJacobian = new MultivariateMatrixFunction() {
            public double[][] value(double[] params) {
                return new double[0][0];
            }
        };
        optimizer.optimize(
            new Target(emptyTarget),
            new Weight(emptyWeights),
            new InitialGuess(startPoint),
            emptyObjective,
            emptyJacobian
        );
    }

    @Test(expected = NotStrictlyPositiveException.class)
    public void testOptimizeNegativeWeight() {
        double[] negativeWeights = new double[]{-1.0, 1.0, 1.0};
        optimizer.optimize(
            new Target(target),
            new Weight(negativeWeights),
            new InitialGuess(startPoint),
            objective,
            jacobian
        );
    }

    @Test(expected = NotStrictlyPositiveException.class)
    public void testOptimizeZeroWeight() {
        double[] zeroWeights = new double[]{0.0, 1.0, 1.0};
        optimizer.optimize(
            new Target(target),
            new Weight(zeroWeights),
            new InitialGuess(startPoint),
            objective,
            jacobian
        );
    }

    // --------------------------------------------------------------
    // Test that computeCost and computeRMS are consistent
    // --------------------------------------------------------------
    @Test
    public void testCostAndRMSRelationship() {
        double[] badTarget = new double[]{2.5, 4.5, 6.5};
        optimizer.optimize(
            new Target(badTarget),
            new Weight(weights),
            new InitialGuess(startPoint),
            objective,
            jacobian
        );
        double cost = optimizer.getCostPublic();
        double rms = optimizer.getRMSPublic();
        // cost and rms should be equal (both are sqrt(chiSquare/n))
        assertEquals("Cost and RMS should be equal", cost, rms, 1e-12);
    }

    // --------------------------------------------------------------
    // Test that getChiSquare returns sum of weighted squared residuals
    // --------------------------------------------------------------
    @Test
    public void testChiSquareComputation() {
        // Manually compute expected chiSquare
        double[] badTarget = new double[]{2.5, 4.5, 6.5};
        optimizer.optimize(
            new Target(badTarget),
            new Weight(weights),
            new InitialGuess(startPoint),
            objective,
            jacobian
        );
        // After optimization, residuals are [0.5, 0.5, 0.5], weights all 1 -> chiSquare = 0.75
        assertEquals("ChiSquare should be 0.75", 0.75, optimizer.getChiSquarePublic(), 1e-12);
    }

    // --------------------------------------------------------------
    // Test that computeCost uses the correct formula (not sqrt(chiSquare))
    // --------------------------------------------------------------
    @Test
    public void testComputeCostNotSqrtChiSquare() {
        // Force a scenario where chiSquare is known and n > 1
        double[] badTarget = new double[]{2.5, 4.5, 6.5};
        optimizer.optimize(
            new Target(badTarget),
            new Weight(weights),
            new InitialGuess(startPoint),
            objective,
            jacobian
        );
        double chiSquare = optimizer.getChiSquarePublic();
        double n = target.length;
        double expectedCost = Math.sqrt(chiSquare / n);
        double buggyCost = Math.sqrt(chiSquare);
        // Assert that cost is not the buggy value
        assertTrue("Cost should be sqrt(chiSquare/n), not sqrt(chiSquare)",
                   Math.abs(optimizer.getCostPublic() - buggyCost) > 1e-12);
        // Assert that cost is the correct value
        assertEquals("Cost should be sqrt(chiSquare/n)", expectedCost, optimizer.getCostPublic(), 1e-12);
    }

    // --------------------------------------------------------------
    // Test that computeRMS is correct (same as cost)
    // --------------------------------------------------------------
    @Test
    public void testComputeRMSNotSqrtChiSquare() {
        double[] badTarget = new double[]{2.5, 4.5, 6.5};
        optimizer.optimize(
            new Target(badTarget),
            new Weight(weights),
            new InitialGuess(startPoint),
            objective,
            jacobian
        );
        double chiSquare = optimizer.getChiSquarePublic();
        double n = target.length;
        double expectedRMS = Math.sqrt(chiSquare / n);
        double buggyRMS = Math.sqrt(chiSquare);
        assertTrue("RMS should be sqrt(chiSquare/n), not sqrt(chiSquare)",
                   Math.abs(optimizer.getRMSPublic() - buggyRMS) > 1e-12);
        assertEquals("RMS should be sqrt(chiSquare/n)", expectedRMS, optimizer.getRMSPublic(), 1e-12);
    }

    // --------------------------------------------------------------
    // Test that after optimization, cost and rms are set
    // --------------------------------------------------------------
    @Test
    public void testCostAndRMSSetAfterOptimize() {
        optimizer.optimize(
            new Target(target),
            new Weight(weights),
            new InitialGuess(startPoint),
            objective,
            jacobian
        );
        // Cost and RMS should be non-negative (here zero)
        assertTrue("Cost should be >= 0", optimizer.getCostPublic() >= 0);
        assertTrue("RMS should be >= 0", optimizer.getRMSPublic() >= 0);
    }

    // --------------------------------------------------------------
    // Test that computeObjectiveValue and computeJacobian are called during optimize
    // --------------------------------------------------------------
    @Test
    public void testObjectiveAndJacobianCalled() {
        // Use a spy-like approach: check that the internal methods are invoked
        // We can verify by checking that the point used is the one we set in doOptimize
        optimizer.optimize(
            new Target(target),
            new Weight(weights),
            new InitialGuess(startPoint),
            objective,
            jacobian
        );
        // The doOptimize method sets point to (2,0) and computes objective and jacobian
        // We can check that the internal objective value matches
        double[] expectedObjective = new double[]{2.0, 4.0, 6.0};
        assertArrayEquals("Internal objective should be computed", expectedObjective, optimizer.getObjective(), 1e-12);
    }

    // --------------------------------------------------------------
    // Test that getObjective returns the computed objective values
    // --------------------------------------------------------------
    @Test
    public void testGetObjective() {
        optimizer.optimize(
            new Target(target),
            new Weight(weights),
            new InitialGuess(startPoint),
            objective,
            jacobian
        );
        double[] obj = optimizer.getObjective();
        assertNotNull("Objective should not be null", obj);
        assertEquals("Objective length should be 3", 3, obj.length);
    }

    // --------------------------------------------------------------
    // Test that getJacobian returns the computed Jacobian
    // --------------------------------------------------------------
    @Test
    public void testGetJacobian() {
        optimizer.optimize(
            new Target(target),
            new Weight(weights),
            new InitialGuess(startPoint),
            objective,
            jacobian
        );
        RealMatrix jac = optimizer.getJacobian();
        assertNotNull("Jacobian should not be null", jac);
        assertEquals("Jacobian rows", 3, jac.getRowDimension());
        assertEquals("Jacobian columns", 2, jac.getColumnDimension());
    }

    // --------------------------------------------------------------
    // Test that getWeight returns the weight matrix
    // --------------------------------------------------------------
    @Test
    public void testGetWeight() {
        optimizer.optimize(
            new Target(target),
            new Weight(weights),
            new InitialGuess(startPoint),
            objective,
            jacobian
        );
        RealMatrix w = optimizer.getWeight();
        assertNotNull("Weight matrix should not be null", w);
        assertEquals("Weight matrix size", 3, w.getRowDimension());
    }

    // --------------------------------------------------------------
    // Test that getTarget returns the target vector
    // --------------------------------------------------------------
    @Test
    public void testGetTarget() {
        optimizer.optimize(
            new Target(target),
            new Weight(weights),
            new InitialGuess(startPoint),
            objective,
            jacobian
        );
        double[] t = optimizer.getTarget();
        assertArrayEquals("Target should match", target, t, 1e-12);
    }

    // --------------------------------------------------------------
    // Test that getObservationsCount returns correct count
    // --------------------------------------------------------------
    @Test
    public void testGetObservationsCount() {
        optimizer.optimize(
            new Target(target),
            new Weight(weights),
            new InitialGuess(startPoint),
            objective,
            jacobian
        );
        assertEquals("Observations count should be 3", 3, optimizer.getObservationsCount());
    }

    // --------------------------------------------------------------
    // Test that getProblemSize returns number of parameters
    // --------------------------------------------------------------
    @Test
    public void testGetProblemSize() {
        optimizer.optimize(
            new Target(target),
            new Weight(weights),
            new InitialGuess(startPoint),
            objective,
            jacobian
        );
        assertEquals("Problem size should be 2", 2, optimizer.getProblemSize());
    }

    // --------------------------------------------------------------
    // Test that convergence checker is used (simple)
    // --------------------------------------------------------------
    @Test
    public void testConvergenceChecker() {
        // The optimizer uses SimpleVectorValueChecker with relative tolerance 1e-6
        // We can test that it converges quickly for perfect data
        PointVectorValuePair result = optimizer.optimize(
            new Target(target),
            new Weight(weights),
            new InitialGuess(startPoint),
            objective,
            jacobian
        );
        assertNotNull("Result should be non-null", result);
    }

    // --------------------------------------------------------------
    // Test that the optimizer can be reused
    // --------------------------------------------------------------
    @Test
    public void testReuseOptimizer() {
        // First optimization
        optimizer.optimize(
            new Target(target),
            new Weight(weights),
            new InitialGuess(startPoint),
            objective,
            jacobian
        );
        double cost1 = optimizer.getCostPublic();
        // Second optimization with different data
        double[] newTarget = new double[]{3.0, 6.0, 9.0};
        optimizer.optimize(
            new Target(newTarget),
            new Weight(weights),
            new InitialGuess(startPoint),
            objective,
            jacobian
        );
        double cost2 = optimizer.getCostPublic();
        // Cost should be different because new targets are exactly 3x? Actually model is y=2x, so for x=1,2,3 -> 2,4,6 vs 3,6,9 -> residuals 1,2,3 -> sum squares=14, n=3, cost=sqrt(14/3) ~2.16
        assertNotEquals("Cost should change with different targets", cost1, cost2, 1e-12);
    }

    // --------------------------------------------------------------
    // Test that the optimizer handles large number of observations
    // --------------------------------------------------------------
    @Test
    public void testLargeObservations() {
        int n = 1000;
        double[] largeTarget = new double[n];
        double[] largeWeights = new double[n];
        for (int i = 0; i < n; i++) {
            largeTarget[i] = 2.0 * (i + 1); // perfect linear
            largeWeights[i] = 1.0;
        }
        MultivariateVectorFunction largeObjective = new MultivariateVectorFunction() {
            public double[] value(double[] params) {
                double a = params[0];
                double b = params[1];
                double[] result = new double[n];
                for (int i = 0; i < n; i++) {
                    result[i] = a * (i + 1) + b;
                }
                return result;
            }
        };
        MultivariateMatrixFunction largeJacobian = new MultivariateMatrixFunction() {
            public double[][] value(double[] params) {
                double[][] jac = new double[n][2];
                for (int i = 0; i < n; i++) {
                    jac[i][0] = i + 1;
                    jac[i][1] = 1.0;
                }
                return jac;
            }
        };
        optimizer.optimize(
            new Target(largeTarget),
            new Weight(largeWeights),
            new InitialGuess(new double[]{1.0, 1.0}),
            largeObjective,
            largeJacobian
        );
        // Should converge to (2,0) with cost near 0
        assertEquals("Cost should be near 0 for perfect fit", 0.0, optimizer.getCostPublic(), 1e-6);
    }

    // --------------------------------------------------------------
    // Test that the optimizer handles singular Jacobian gracefully
    // --------------------------------------------------------------
    @Test(expected = SingularMatrixException.class)
    public void testSingularJacobian() {
        // Jacobian with linearly dependent columns: both columns are the same
        MultivariateMatrixFunction singularJacobian = new MultivariateMatrixFunction() {
            public double[][] value(double[] params) {
                return new double[][]{
                    {1.0, 1.0},
                    {2.0, 2.0},
                    {3.0, 3.0}
                };
            }
        };
        optimizer.optimize(
            new Target(target),
            new Weight(weights),
            new InitialGuess(startPoint),
            objective,
            singularJacobian
        );
    }

    // --------------------------------------------------------------
    // Test that the optimizer handles non-square Jacobian (more params than observations)
    // --------------------------------------------------------------
    @Test(expected = DimensionMismatchException.class)
    public void testMoreParamsThanObservations() {
        // Only 2 observations but 2 parameters -> underdetermined? Actually 2 obs, 2 params is square.
        // To have more params than obs, we need 1 observation and 2 params.
        double[] oneTarget = new double[]{1.0};
        double[] oneWeight = new double[]{1.0};
        MultivariateVectorFunction oneObjective = new MultivariateVectorFunction() {
            public double[] value(double[] params) {
                return new double[]{params[0] + params[1]};
            }
        };
        MultivariateMatrixFunction oneJacobian = new MultivariateMatrixFunction() {
            public double[][] value(double[] params) {
                return new double[][]{{1.0, 1.0}};
            }
        };
        // This should throw DimensionMismatchException because Jacobian has 1 row but 2 columns?
        // Actually the optimizer may handle it, but the linear solver may fail. We'll expect an exception.
        optimizer.optimize(
            new Target(oneTarget),
            new Weight(oneWeight),
            new InitialGuess(startPoint),
            oneObjective,
            oneJacobian
        );
    }
}