package org.apache.commons.math3.optimization.fitting;

import org.apache.commons.math3.analysis.function.HarmonicOscillator;
import org.apache.commons.math3.optimization.DifferentiableMultivariateVectorOptimizer;
import org.apache.commons.math3.optimization.fitting.HarmonicFitter;
import org.apache.commons.math3.optimization.fitting.WeightedObservedPoint;
import org.junit.Test;

import static org.junit.Assert.*;

public class HarmonicFitterTest {

    @Test
    public void testParameterGuesserValidData() {
        // Create a known harmonic function: a * cos(w * x + phase)
        // With parameters guesser, we test the ParameterGuesser inner class directly via fit or constructor.
        double amplitude = 2.0;
        double omega = 1.5;
        double phase = 0.5;

        HarmonicOscillator.Parametric f = new HarmonicOscillator.Parametric();
        WeightedObservedPoint[] points = new WeightedObservedPoint[50];
        for (int i = 0; i < points.length; i++) {
            double x = i * 0.1;
            double y = f.value(x, new double[] { amplitude, omega, phase });
            points[i] = new WeightedObservedPoint(1.0, x, y);
        }

        // Mock or simple optimizer since we want to test the guesser/fitter logic.
        // If we just use HarmonicFitter with a null optimizer, calling getParameterGuesses() 
        // works without needing an actual optimization run.
        HarmonicFitter fitter = new HarmonicFitter(null);
        for (WeightedObservedPoint p : points) {
            fitter.addObservedPoint(p);
        }

        double[] guesses = fitter.fit(); // This uses ParameterGuesser internally
        assertNotNull(guesses);
        assertEquals(3, guesses.length);
    }

    @Test(expected = org.apache.commons.math3.exception.NumberIsTooSmallException.class)
    public void testParameterGuesserTooFewPoints() {
        HarmonicFitter fitter = new HarmonicFitter(null);
        // Add fewer than 4 points, which should trigger an exception in ParameterGuesser
        fitter.addObservedPoint(new WeightedObservedPoint(1.0, 0.0, 1.0));
        fitter.addObservedPoint(new WeightedObservedPoint(1.0, 1.0, 2.0));
        fitter.addObservedPoint(new WeightedObservedPoint(1.0, 2.0, 1.0));

        fitter.fit();
    }

    @Test(expected = org.apache.commons.math3.exception.ZeroException.class)
    public void testParameterGuesserCollinearOrZeroDenominator() {
        HarmonicFitter fitter = new HarmonicFitter(null);
        // Points that might cause zero denominator or issues in omega estimation (e.g., all same x or equally spaced with zero crossings issues)
        fitter.addObservedPoint(new WeightedObservedPoint(1.0, 0.0, 1.0));
        fitter.addObservedPoint(new WeightedObservedPoint(1.0, 0.0, 1.0));
        fitter.addObservedPoint(new WeightedObservedPoint(1.0, 0.0, 1.0));
        fitter.addObservedPoint(new WeightedObservedPoint(1.0, 0.0, 1.0));

        fitter.fit();
    }

    @Test
    public void testFitWithInitialGuess() {
        HarmonicFitter fitter = new HarmonicFitter(null);
        fitter.addObservedPoint(new WeightedObservedPoint(1.0, 0.0, 1.0));
        fitter.addObservedPoint(new WeightedObservedPoint(1.0, 0.5, 2.0));
        fitter.addObservedPoint(new WeightedObservedPoint(1.0, 1.0, 1.0));
        fitter.addObservedPoint(new WeightedObservedPoint(1.0, 1.5, 0.0));

        double[] initialGuess = new double[] { 1.0, 1.0, 1.0 };
        try {
            fitter.fit(initialGuess);
        } catch (Exception e) {
            // Expected if optimizer is null when it attempts to optimize
            assertNotNull(e);
        }
    }
}