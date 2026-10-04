package org.apache.commons.math3.optimization.fitting;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import org.apache.commons.math3.optimization.general.LevenbergMarquardtOptimizer;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.exception.ConvergenceException;

public class HarmonicFitterTest {

    private HarmonicFitter fitter;
    private static final double EPS = 1e-6;

    @Before
    public void setUp() {
        // Use default optimizer (Levenberg-Marquardt) with default parameters
        fitter = new HarmonicFitter(new LevenbergMarquardtOptimizer());
    }

    // ========== Basic functional tests ==========

    @Test
    public void testPerfectSinusoid() {
        // y = 3.0 * sin(2.0 * x + 1.0)
        double a = 3.0, b = 2.0, c = 1.0;
        WeightedObservedPoints obs = new WeightedObservedPoints();
        for (double x = 0.0; x < 10.0; x += 0.1) {
            obs.add(x, a * Math.sin(b * x + c));
        }
        double[] fitted = fitter.fit(obs.toList());
        assertEquals(a, fitted[0], 1e-4);
        assertEquals(b, fitted[1], 1e-4);
        assertEquals(c, fitted[2], 1e-4);
    }

    @Test
    public void testSinusoidWithNoise() {
        // y = 2.0 * sin(1.5 * x + 0.5) + small noise
        double a = 2.0, b = 1.5, c = 0.5;
        WeightedObservedPoints obs = new WeightedObservedPoints();
        for (double x = 0.0; x < 10.0; x += 0.2) {
            double noise = 0.01 * (Math.random() - 0.5);
            obs.add(x, a * Math.sin(b * x + c) + noise);
        }
        double[] fitted = fitter.fit(obs.toList());
        assertEquals(a, fitted[0], 0.1);
        assertEquals(b, fitted[1], 0.1);
        assertEquals(c, fitted[2], 0.1);
    }

    @Test
    public void testZeroAmplitude() {
        // y = 0 (constant zero)
        WeightedObservedPoints obs = new WeightedObservedPoints();
        for (double x = 0.0; x < 5.0; x += 0.5) {
            obs.add(x, 0.0);
        }
        double[] fitted = fitter.fit(obs.toList());
        assertEquals(0.0, fitted[0], 1e-6);
        // frequency and phase may be arbitrary, but amplitude should be near zero
    }

    @Test
    public void testConstantOffset() {
        // y = 5 (constant)
        WeightedObservedPoints obs = new WeightedObservedPoints();
        for (double x = 0.0; x < 5.0; x += 0.5) {
            obs.add(x, 5.0);
        }
        double[] fitted = fitter.fit(obs.toList());
        // Amplitude should be near zero, offset not modeled, but amplitude should be small
        assertTrue(Math.abs(fitted[0]) < 1e-4);
    }

    // ========== Edge cases and boundary conditions ==========

    @Test(expected = MathIllegalArgumentException.class)
    public void testInsufficientPoints() {
        // Need at least 4 points for 3 parameters
        WeightedObservedPoints obs = new WeightedObservedPoints();
        obs.add(0.0, 1.0);
        obs.add(1.0, 2.0);
        obs.add(2.0, 3.0);
        fitter.fit(obs.toList());
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testNullObservationList() {
        fitter.fit(null);
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testEmptyObservationList() {
        WeightedObservedPoints obs = new WeightedObservedPoints();
        fitter.fit(obs.toList());
    }

    @Test(expected = ConvergenceException.class)
    public void testNonHarmonicData() {
        // Linear data: y = x
        WeightedObservedPoints obs = new WeightedObservedPoints();
        for (double x = 0.0; x < 10.0; x += 0.5) {
            obs.add(x, x);
        }
        // This should fail to converge because data is not sinusoidal
        fitter.fit(obs.toList());
    }

    @Test
    public void testVerySmallFrequency() {
        // y = 1.0 * sin(1e-6 * x + 0.0)
        double a = 1.0, b = 1e-6, c = 0.0;
        WeightedObservedPoints obs = new WeightedObservedPoints();
        for (double x = 0.0; x < 1000.0; x += 10.0) {
            obs.add(x, a * Math.sin(b * x + c));
        }
        double[] fitted = fitter.fit(obs.toList());
        assertEquals(a, fitted[0], 1e-2);
        assertEquals(b, fitted[1], 1e-8);
        assertEquals(c, fitted[2], 1e-2);
    }

    @Test
    public void testVeryLargeAmplitude() {
        // y = 1e6 * sin(2.0 * x + 0.0)
        double a = 1e6, b = 2.0, c = 0.0;
        WeightedObservedPoints obs = new WeightedObservedPoints();
        for (double x = 0.0; x < 10.0; x += 0.1) {
            obs.add(x, a * Math.sin(b * x + c));
        }
        double[] fitted = fitter.fit(obs.toList());
        assertEquals(a, fitted[0], 1e3);
        assertEquals(b, fitted[1], 1e-4);
        assertEquals(c, fitted[2], 1e-4);
    }

    @Test
    public void testNegativeAmplitude() {
        // y = -2.0 * sin(1.0 * x + 0.0)  -> amplitude should be 2.0 with phase shift pi
        double a = -2.0, b = 1.0, c = 0.0;
        WeightedObservedPoints obs = new WeightedObservedPoints();
        for (double x = 0.0; x < 10.0; x += 0.2) {
            obs.add(x, a * Math.sin(b * x + c));
        }
        double[] fitted = fitter.fit(obs.toList());
        // Amplitude should be positive 2.0, phase shifted by pi
        assertEquals(2.0, Math.abs(fitted[0]), 1e-4);
        assertEquals(b, fitted[1], 1e-4);
        // Phase should be either 0 or pi (mod 2pi)
        double phaseMod = fitted[2] % (2 * Math.PI);
        assertTrue(Math.abs(phaseMod) < 1e-4 || Math.abs(phaseMod - Math.PI) < 1e-4);
    }

    @Test
    public void testPhaseShift() {
        // y = 1.0 * sin(1.0 * x + Math.PI/2)
        double a = 1.0, b = 1.0, c = Math.PI / 2;
        WeightedObservedPoints obs = new WeightedObservedPoints();
        for (double x = 0.0; x < 10.0; x += 0.2) {
            obs.add(x, a * Math.sin(b * x + c));
        }
        double[] fitted = fitter.fit(obs.toList());
        assertEquals(a, fitted[0], 1e-4);
        assertEquals(b, fitted[1], 1e-4);
        assertEquals(c, fitted[2], 1e-4);
    }

    // ========== Tests targeting potential bugs (Defects4J Math-25) ==========

    @Test
    public void testDataWithMultiplePeriods() {
        // Ensure fit works over many periods
        double a = 5.0, b = 3.0, c = 2.0;
        WeightedObservedPoints obs = new WeightedObservedPoints();
        for (double x = 0.0; x < 20.0 * Math.PI / b; x += 0.1) {
            obs.add(x, a * Math.sin(b * x + c));
        }
        double[] fitted = fitter.fit(obs.toList());
        assertEquals(a, fitted[0], 1e-3);
        assertEquals(b, fitted[1], 1e-3);
        assertEquals(c, fitted[2], 1e-3);
    }

    @Test
    public void testDataWithUnevenSampling() {
        // Randomly spaced points
        double a = 2.5, b = 1.2, c = 0.8;
        WeightedObservedPoints obs = new WeightedObservedPoints();
        double[] xs = {0.0, 0.3, 0.7, 1.1, 1.6, 2.0, 2.5, 3.0, 3.6, 4.2, 4.9, 5.5};
        for (double x : xs) {
            obs.add(x, a * Math.sin(b * x + c));
        }
        double[] fitted = fitter.fit(obs.toList());
        assertEquals(a, fitted[0], 0.5);
        assertEquals(b, fitted[1], 0.5);
        assertEquals(c, fitted[2], 0.5);
    }

    @Test
    public void testSinglePeriodData() {
        // Exactly one period
        double a = 1.0, b = 2.0 * Math.PI / 5.0, c = 0.0;
        WeightedObservedPoints obs = new WeightedObservedPoints();
        for (double x = 0.0; x <= 5.0; x += 0.1) {
            obs.add(x, a * Math.sin(b * x + c));
        }
        double[] fitted = fitter.fit(obs.toList());
        assertEquals(a, fitted[0], 1e-4);
        assertEquals(b, fitted[1], 1e-4);
        assertEquals(c, fitted[2], 1e-4);
    }

    @Test(expected = TooManyEvaluationsException.class)
    public void testTooFewPointsForConvergence() {
        // Only 4 points, barely enough but may cause convergence issues
        WeightedObservedPoints obs = new WeightedObservedPoints();
        obs.add(0.0, 0.0);
        obs.add(1.0, 1.0);
        obs.add(2.0, 0.0);
        obs.add(3.0, -1.0);
        // This might not converge due to poor conditioning
        fitter.fit(obs.toList());
    }

    @Test
    public void testAllSameXValues() {
        // All x values identical -> should fail or produce degenerate fit
        WeightedObservedPoints obs = new WeightedObservedPoints();
        for (int i = 0; i < 10; i++) {
            obs.add(1.0, i);
        }
        try {
            double[] fitted = fitter.fit(obs.toList());
            // If it doesn't throw, amplitude should be near zero
            assertTrue(Math.abs(fitted[0]) < 1e-4);
        } catch (Exception e) {
            // Accept any exception as valid behavior
            assertTrue(e instanceof MathIllegalArgumentException ||
                       e instanceof ConvergenceException);
        }
    }

    @Test
    public void testNegativeFrequencyGuess() {
        // The optimizer should handle negative frequency by converting to positive
        double a = 2.0, b = -1.5, c = 0.3;
        WeightedObservedPoints obs = new WeightedObservedPoints();
        for (double x = 0.0; x < 10.0; x += 0.2) {
            obs.add(x, a * Math.sin(b * x + c));
        }
        double[] fitted = fitter.fit(obs.toList());
        // Frequency should be positive (absolute value)
        assertTrue(fitted[1] > 0);
        assertEquals(Math.abs(b), fitted[1], 1e-4);
        // Amplitude should be positive
        assertTrue(fitted[0] > 0);
    }

    @Test
    public void testLargePhase() {
        // Phase > 2*PI
        double a = 1.0, b = 1.0, c = 100.0;
        WeightedObservedPoints obs = new WeightedObservedPoints();
        for (double x = 0.0; x < 10.0; x += 0.2) {
            obs.add(x, a * Math.sin(b * x + c));
        }
        double[] fitted = fitter.fit(obs.toList());
        assertEquals(a, fitted[0], 1e-4);
        assertEquals(b, fitted[1], 1e-4);
        // Phase may be wrapped, but sin should match
        assertEquals(Math.sin(c), Math.sin(fitted[2]), 1e-4);
    }

    @Test
    public void testWeightedObservations() {
        // Use weighted observations (all weight 1.0 by default)
        WeightedObservedPoints obs = new WeightedObservedPoints();
        for (double x = 0.0; x < 5.0; x += 0.2) {
            obs.add(x, Math.sin(x));
        }
        double[] fitted = fitter.fit(obs.toList());
        assertEquals(1.0, fitted[0], 1e-4);
        assertEquals(1.0, fitted[1], 1e-4);
        assertEquals(0.0, fitted[2], 1e-4);
    }

    @Test
    public void testCustomOptimizer() {
        // Use a different optimizer instance
        HarmonicFitter customFitter = new HarmonicFitter(new LevenbergMarquardtOptimizer());
        WeightedObservedPoints obs = new WeightedObservedPoints();
        for (double x = 0.0; x < 5.0; x += 0.1) {
            obs.add(x, 2.0 * Math.sin(3.0 * x + 0.5));
        }
        double[] fitted = customFitter.fit(obs.toList());
        assertEquals(2.0, fitted[0], 1e-4);
        assertEquals(3.0, fitted[1], 1e-4);
        assertEquals(0.5, fitted[2], 1e-4);
    }
}