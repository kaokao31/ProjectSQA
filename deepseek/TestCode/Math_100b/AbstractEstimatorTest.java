package org.apache.commons.math.estimation;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Arrays;

/**
 * Test suite for AbstractEstimator (Commons Math bug 100).
 * Exercises all concrete methods and aims to trigger potential faults.
 */
public class AbstractEstimatorTest {

    private static final double EPS = 1e-10;

    // Concrete implementation of AbstractEstimator for testing
    private static class SimpleEstimator extends AbstractEstimator {
        private int jacobianEvaluations = 0;
        private int evaluations = 0;
        private double[][] jacobian;
        private double[] residuals;

        @Override
        protected void initializeEstimate() {
            // No-op: parameters and measurements are set externally
        }

        @Override
        protected void updateJacobian() {
            jacobianEvaluations++;
            evaluations++;
            int n = getMeasurements().length;
            int m = getParameters().length;
            jacobian = new double[n][m];
            for (int i = 0; i < n; i++) {
                WeightedMeasurement wm = getMeasurements()[i];
                // Assume measurement is SimpleMeasurement with x stored
                double x = ((SimpleMeasurement) wm).x;
                // For linear model y = a*x + b, Jacobian row = [x, 1]
                jacobian[i][0] = x;
                jacobian[i][1] = 1.0;
            }
        }

        @Override
        public int getJacobianEvaluations() {
            return jacobianEvaluations;
        }

        @Override
        public int getEvaluations() {
            return evaluations;
        }

        @Override
        protected void incrementJacobianEvaluationsCounter() {
            jacobianEvaluations++;
        }

        // Expose protected methods for testing
        public void callUpdateResidualsAndCost() {
            updateResidualsAndCost();
        }

        public double[][] getInternalJacobian() {
            return jacobian;
        }

        public double[] getInternalResiduals() {
            return residuals;
        }
    }

    // Simple WeightedMeasurement implementation
    private static class SimpleMeasurement extends WeightedMeasurement {
        final double x;
        final double y;

        SimpleMeasurement(double weight, double x, double y) {
            super(weight, y); // measured value is y
            this.x = x;
            this.y = y;
        }

        @Override
        public double getResidual() {
            // Residual = measured - theoretical
            // Theoretical: a*x + b
            EstimatedParameter[] params = getParameters();
            double a = params[0].getEstimate();
            double b = params[1].getEstimate();
            return y - (a * x + b);
        }
    }

    private SimpleEstimator estimator;
    private EstimatedParameter[] parameters;
    private WeightedMeasurement[] measurements;

    @Before
    public void setUp() {
        estimator = new SimpleEstimator();
        parameters = new EstimatedParameter[] {
            new EstimatedParameter("a", 0.0),
            new EstimatedParameter("b", 0.0)
        };
        // Linear data: y = 2*x + 3
        measurements = new WeightedMeasurement[] {
            new SimpleMeasurement(1.0, 0.0, 3.0),
            new SimpleMeasurement(1.0, 1.0, 5.0),
            new SimpleMeasurement(1.0, 2.0, 7.0),
            new SimpleMeasurement(1.0, 3.0, 9.0)
        };
        estimator.setParameters(parameters);
        estimator.setMeasurements(measurements);
    }

    @Test
    public void testEstimateLinearProblem() {
        estimator.estimate();
        assertEquals(2.0, parameters[0].getEstimate(), 1e-10);
        assertEquals(3.0, parameters[1].getEstimate(), 1e-10);
        assertTrue(estimator.getEvaluations() > 0);
        assertTrue(estimator.getJacobianEvaluations() > 0);
    }

    @Test(expected = EstimationException.class)
    public void testEstimateWithFewerMeasurementsThanParameters() {
        // Only 1 measurement for 2 parameters -> underdetermined
        estimator.setMeasurements(new WeightedMeasurement[] {
            new SimpleMeasurement(1.0, 0.0, 3.0)
        });
        estimator.estimate();
    }

    @Test(expected = EstimationException.class)
    public void testEstimateWithZeroMeasurements() {
        estimator.setMeasurements(new WeightedMeasurement[0]);
        estimator.estimate();
    }

    @Test(expected = NullPointerException.class)
    public void testEstimateWithNullParameters() {
        estimator.setParameters(null);
        estimator.estimate();
    }

    @Test(expected = EstimationException.class)
    public void testEstimateWithNegativeWeight() {
        estimator.setMeasurements(new WeightedMeasurement[] {
            new SimpleMeasurement(-1.0, 0.0, 3.0),
            new SimpleMeasurement(1.0, 1.0, 5.0)
        });
        estimator.estimate();
    }

    @Test
    public void testGetChiSquare() {
        estimator.estimate();
        double chiSquare = estimator.getChiSquare();
        assertTrue(chiSquare >= 0);
        // Perfect fit should give chiSquare near 0
        assertEquals(0.0, chiSquare, 1e-10);
    }

    @Test
    public void testGetRMS() {
        estimator.estimate();
        double rms = estimator.getRMS();
        assertTrue(rms >= 0);
        assertEquals(0.0, rms, 1e-10);
    }

    @Test
    public void testGetCovariances() {
        estimator.estimate();
        double[][] cov = estimator.getCovariances();
        assertNotNull(cov);
        assertEquals(2, cov.length);
        assertEquals(2, cov[0].length);
        // Covariances should be finite
        for (double[] row : cov) {
            for (double v : row) {
                assertFalse(Double.isNaN(v));
                assertFalse(Double.isInfinite(v));
            }
        }
    }

    @Test
    public void testInitializeEstimate() {
        // initializeEstimate is called by estimate, but we can test indirectly
        estimator.estimate();
        // After estimation, parameters should have been updated
        assertNotEquals(0.0, parameters[0].getEstimate());
        assertNotEquals(0.0, parameters[1].getEstimate());
    }

    @Test
    public void testUpdateResidualsAndCost() {
        // Set initial estimates
        parameters[0].setEstimate(1.0);
        parameters[1].setEstimate(2.0);
        estimator.callUpdateResidualsAndCost();
        // Cost should be sqrt(sum of weighted squared residuals)
        double cost = estimator.getCost();
        assertTrue(cost > 0);
        // Residuals should be computed
        double[] residuals = estimator.getInternalResiduals();
        assertNotNull(residuals);
        assertEquals(measurements.length, residuals.length);
    }

    @Test
    public void testBoundaryValues() {
        // Large x values
        estimator.setMeasurements(new WeightedMeasurement[] {
            new SimpleMeasurement(1.0, 1e10, 2e10 + 3),
            new SimpleMeasurement(1.0, -1e10, -2e10 + 3)
        });
        estimator.estimate();
        assertEquals(2.0, parameters[0].getEstimate(), 1e-5);
        assertEquals(3.0, parameters[1].getEstimate(), 1e-5);
    }

    @Test(expected = EstimationException.class)
    public void testEstimateWithSingularJacobian() {
        // All measurements have same x -> Jacobian columns linearly dependent
        estimator.setMeasurements(new WeightedMeasurement[] {
            new SimpleMeasurement(1.0, 1.0, 5.0),
            new SimpleMeasurement(1.0, 1.0, 5.0),
            new SimpleMeasurement(1.0, 1.0, 5.0)
        });
        estimator.estimate();
    }

    @Test
    public void testGetJacobianEvaluations() {
        assertEquals(0, estimator.getJacobianEvaluations());
        estimator.estimate();
        assertTrue(estimator.getJacobianEvaluations() > 0);
    }

    @Test
    public void testGetEvaluations() {
        assertEquals(0, estimator.getEvaluations());
        estimator.estimate();
        assertTrue(estimator.getEvaluations() > 0);
    }
}