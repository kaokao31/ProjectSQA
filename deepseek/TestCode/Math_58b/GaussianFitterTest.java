package org.apache.commons.math.optimization.fitting;

import org.apache.commons.math.exception.NullArgumentException;
import org.apache.commons.math.exception.NumberIsTooSmallException;
import org.apache.commons.math.optimization.general.LevenbergMarquardtOptimizer;
import org.junit.Assert;
import org.junit.Test;

public class GaussianFitterTest {

    private static final double TOL = 1e-5;

    private GaussianFitter createFitter() {
        return new GaussianFitter(new LevenbergMarquardtOptimizer());
    }

    private void addData(GaussianFitter fitter, double[] x, double[] y) {
        for (int i = 0; i < x.length; i++) {
            fitter.addObservedPoint(x[i], y[i]);
        }
    }

    private double sigmaFromFwhm(double fwhm) {
        return fwhm / (2.0 * Math.sqrt(2.0 * Math.log(2.0)));
    }

    @Test
    public void testFitWithThreePoints() {
        GaussianFitter fitter = createFitter();
        fitter.addObservedPoint(1.0, 1.0);
        fitter.addObservedPoint(2.0, 2.0);
        fitter.addObservedPoint(3.0, 1.0);

        double[] parameters = fitter.fit();

        Assert.assertEquals(3, parameters.length);
        Assert.assertEquals(2.0, parameters[0], 0.05);
        Assert.assertEquals(2.0, parameters[1], 0.05);
        Assert.assertEquals(0.849, parameters[2], 0.05);
        Assert.assertTrue(parameters[2] > 0);
    }

    @Test
    public void testFitWithUnsortedData() {
        GaussianFitter fitter = createFitter();
        fitter.addObservedPoint(3.0, 1.0);
        fitter.addObservedPoint(1.0, 1.0);
        fitter.addObservedPoint(2.0, 2.0);

        double[] parameters = fitter.fit();

        Assert.assertEquals(2.0, parameters[0], 0.05);
        Assert.assertEquals(2.0, parameters[1], 0.05);
        Assert.assertEquals(0.849, parameters[2], 0.1);
        Assert.assertTrue(parameters[2] > 0);
    }

    @Test
    public void testFitWithAsymmetricGaussian() {
        final double norm = 5.0;
        final double mean = 3.0;
        final double sigma = 2.0;

        double[] x = new double[17];
        double[] y = new double[x.length];
        for (int i = 0; i < x.length; i++) {
            x[i] = -5.0 + i;
            double dx = x[i] - mean;
            y[i] = norm * Math.exp(-dx * dx / (2.0 * sigma * sigma));
        }

        GaussianFitter fitter = createFitter();
        addData(fitter, x, y);

        double[] parameters = fitter.fit();

        Assert.assertEquals(norm, parameters[0], 0.1);
        Assert.assertEquals(mean, parameters[1], 0.1);
        Assert.assertEquals(sigma, parameters[2], 0.1);
        Assert.assertTrue(parameters[2] > 0);
    }

    @Test
    public void testParameterGuesser() {
        WeightedObservedPoint[] observations = new WeightedObservedPoint[] {
            new WeightedObservedPoint(1.0, 1.0, 1.0),
            new WeightedObservedPoint(1.0, 2.0, 2.0),
            new WeightedObservedPoint(1.0, 3.0, 1.0)
        };

        double[] guess = new GaussianFitter.ParameterGuesser(observations).guess();

        Assert.assertEquals(3, guess.length);
        Assert.assertEquals(2.0, guess[0], 1e-10);
        Assert.assertEquals(2.0, guess[1], 1e-10);
        Assert.assertEquals(sigmaFromFwhm(1.0), guess[2], 1e-10);
    }

    @Test(expected = NullArgumentException.class)
    public void testParameterGuesserNull() {
        new GaussianFitter.ParameterGuesser(null);
    }

    @Test(expected = NumberIsTooSmallException.class)
    public void testParameterGuesserTooFewPoints() {
        WeightedObservedPoint[] observations = new WeightedObservedPoint[] {
            new WeightedObservedPoint(1.0, 1.0, 1.0),
            new WeightedObservedPoint(1.0, 2.0, 2.0)
        };
        new GaussianFitter.ParameterGuesser(observations);
    }

    @Test(expected = NumberIsTooSmallException.class)
    public void testFitTooFewPoints() {
        GaussianFitter fitter = createFitter();
        fitter.addObservedPoint(1.0, 1.0);
        fitter.addObservedPoint(2.0, 2.0);
        fitter.fit();
    }

    @Test(expected = NumberIsTooSmallException.class)
    public void testFitNoPoints() {
        GaussianFitter fitter = createFitter();
        fitter.fit();
    }
}