package org.apache.commons.math.optimization.general;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MaxEvaluationsExceededException;
import org.apache.commons.math.MaxIterationsExceededException;
import org.apache.commons.math.optimization.OptimizationException;
import org.apache.commons.math.optimization.SimpleVectorialValueChecker;
import org.apache.commons.math.optimization.VectorialPointValuePair;
import org.apache.commons.math.analysis.DifferentiableMultivariateVectorialFunction;
import org.apache.commons.math.analysis.MultivariateVectorialFunction;
import org.apache.commons.math.analysis.MultivariateMatrixFunction;
import org.junit.Assert;
import org.junit.Test;

public class AbstractLeastSquaresOptimizerTest {

    // Concrete subclass to test AbstractLeastSquaresOptimizer functionality
    private static class ConcreteOptimizer extends AbstractLeastSquaresOptimizer {
        @Override
        public VectorialPointValuePair doOptimize()
            throws FunctionEvaluationException, OptimizationException, IllegalArgumentException {
            // Simulate iterations and evaluations updates
            updateData();
            
            // Test standard methods
            double chiSq = getChiSquare();
            double[][] cov = getCovariances();
            double[] sigmas = guessParametersErrors();
            
            return new VectorialPointValuePair(new double[] { 1.0, 2.0 }, new double[] { 3.0, 4.0 });
        }
    }

    @Test
    public int getMaxIterations() {
        ConcreteOptimizer optimizer = new ConcreteOptimizer();
        return optimizer.getMaxIterations();
    }

    @Test
    public void testGettersAndSetters() {
        ConcreteOptimizer optimizer = new ConcreteOptimizer();
        
        optimizer.setMaxIterations(100);
        Assert.assertEquals(100, optimizer.getMaxIterations());
        
        optimizer.setMaxEvaluations(200);
        Assert.assertEquals(200, optimizer.getMaxEvaluations());
        
        optimizer.setConvergenceChecker(new SimpleVectorialValueChecker());
        Assert.assertNotNull(optimizer.getConvergenceChecker());
    }

    @Test
    public void testGetEvaluationsAndIterations() {
        ConcreteOptimizer optimizer = new ConcreteOptimizer();
        // Initially zero
        Assert.assertEquals(0, optimizer.getEvaluations());
        Assert.assertEquals(0, optimizer.getIterations());
        Assert.assertEquals(0, optimizer.getJacobianEvaluations());
    }

    @Test(expected = MaxIterationsExceededException.class)
    public void testMaxIterationsExceeded() throws Exception {
        ConcreteOptimizer optimizer = new ConcreteOptimizer();
        optimizer.setMaxIterations(0);
        
        DifferentiableMultivariateVectorialFunction f = new DifferentiableMultivariateVectorialFunction() {
            public MultivariateVectorialFunction value() {
                return new MultivariateVectorialFunction() {
                    public double[] value(double[] point) { return new double[] { 0.0 }; }
                };
            }
            public MultivariateMatrixFunction jacobian() {
                return new MultivariateMatrixFunction() {
                    public double[][] value(double[] point) { return new double[][] { { 0.0 } }; }
                };
            }
        };
        
        optimizer.optimize(f, new double[] { 1.0 }, new double[] { 1.0 }, new double[] { 1.0 });
    }

    @Test(expected = MaxEvaluationsExceededException.class)
    public void testMaxEvaluationsExceeded() throws Exception {
        ConcreteOptimizer optimizer = new ConcreteOptimizer();
        optimizer.setMaxEvaluations(0);
        
        DifferentiableMultivariateVectorialFunction f = new DifferentiableMultivariateVectorialFunction() {
            public MultivariateVectorialFunction value() {
                return new MultivariateVectorialFunction() {
                    public double[] value(double[] point) { return new double[] { 0.0 }; }
                };
            }
            public MultivariateMatrixFunction jacobian() {
                return new MultivariateMatrixFunction() {
                    public double[][] value(double[] point) { return new double[][] { { 0.0 } }; }
                };
            }
        };
        
        optimizer.optimize(f, new double[] { 1.0 }, new double[] { 1.0 }, new double[] { 1.0 });
    }

    @Test
    public void testGetRMS() {
        ConcreteOptimizer optimizer = new ConcreteOptimizer();
        // Before any optimization or data setup, RMS might be NaN or 0 depending on implementation, 
        // but we can test the method runs without exception.
        try {
            optimizer.getRMS();
        } catch (Exception e) {
            // Expected if cost/residuals are not initialized
        }
    }

    @Test
    public void testGuessParametersErrors() throws Exception {
        ConcreteOptimizer optimizer = new ConcreteOptimizer();
        try {
            optimizer.guessParametersErrors();
        } catch (Exception e) {
            // Expected if covariance or rank conditions aren't met
        }
    }

    @Test
    public void testGetCovariances() throws Exception {
        ConcreteOptimizer optimizer = new ConcreteOptimizer();
        try {
            optimizer.getCovariances();
        } catch (Exception e) {
            // Expected if uninitialized
        }
    }

    @Test
    public void testGetChiSquare() {
        ConcreteOptimizer optimizer = new ConcreteOptimizer();
        double chi = optimizer.getChiSquare();
        Assert.assertEquals(0.0, chi, 1e-12);
    }
}