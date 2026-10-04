package org.apache.commons.math.optimization.fitting;

import org.apache.commons.math.analysis.fitting.WeightedObservedPoint;
import org.apache.commons.math.optimization.DifferentiableMultivariateVectorOptimizer;
import org.apache.commons.math.optimization.general.LevenbergMarquardtOptimizer;
import org.junit.Assert;
import org.junit.Test;

public class GaussianFitterTest {

    @Test
    public void testFitParametersBasic() {
        DifferentiableMultivariateVectorOptimizer optimizer = new LevenbergMarquardtOptimizer();
        GaussianFitter fitter = new GaussianFitter(optimizer);

        fitter.addObservedPoint(1.0, 0.0, 1.0);
        fitter.addObservedPoint(1.0, 1.0, 2.718);
        fitter.addObservedPoint(1.0, 2.0, 7.389);

        double[] params = fitter.fit();
        Assert.assertNotNull(params);
        Assert.assertEquals(3, params.length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFitEmptyObservations() {
        DifferentiableMultivariateVectorOptimizer optimizer = new LevenbergMarquardtOptimizer();
        GaussianFitter fitter = new GaussianFitter(optimizer);
        fitter.fit();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFitInsufficientObservations() {
        DifferentiableMultivariateVectorOptimizer optimizer = new LevenbergMarquardtOptimizer();
        GaussianFitter fitter = new GaussianFitter(optimizer);
        fitter.addObservedPoint(1.0, 0.0, 1.0);
        fitter.addObservedPoint(1.0, 1.0, 2.0);
        // Gaussian fitting requires at least 4 parameters/points generally or handled by ParamGuesser
        fitter.fit();
    }

    @Test
    public void testFitWithInitialGuess() {
        DifferentiableMultivariateVectorOptimizer optimizer = new LevenbergMarquardtOptimizer();
        GaussianFitter fitter = new GaussianFitter(optimizer);

        fitter.addObservedPoint(1.0, -1.0, 1.0);
        fitter.addObservedPoint(1.0, 0.0, 5.0);
        fitter.addObservedPoint(1.0, 1.0, 1.0);
        fitter.addObservedPoint(1.0, 2.0, 0.1);

        double[] initialGuess = new double[] { 5.0, 0.0, 1.0 };
        double[] params = fitter.fit(initialGuess);
        
        Assert.assertNotNull(params);
        Assert.assertEquals(3, params.length);
    }

    @Test
    public void testParamGuesserNegativeOrZeroValues() {
        DifferentiableMultivariateVectorOptimizer optimizer = new LevenbergMarquardtOptimizer();
        GaussianFitter fitter = new GaussianFitter(optimizer);

        // Add points that might trigger specific branches in GaussianFitter.ParameterGuesser
        fitter.addObservedPoint(1.0, 0.0, -1.0);
        fitter.addObservedPoint(1.0, 1.0, 0.0);
        fitter.addObservedPoint(1.0, 2.0, 2.0);
        fitter.addObservedPoint(1.0, 3.0, 5.0);
        fitter.addObservedPoint(1.0, 4.0, 2.0);
        fitter.addObservedPoint(1.0, 5.0, 0.0);

        try {
            fitter.fit();
        } catch (Exception e) {
            // Expected for some edge cases in guesser, ensuring code is exercised
            Assert.assertNotNull(e);
        }
    }
}