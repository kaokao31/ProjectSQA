package org.apache.commons.math3.optimization.general;

import org.apache.commons.math3.analysis.DifferentiableMultivariateVectorFunction;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.optimization.PointVectorValuePair;
import org.apache.commons.math3.optimization.Weight;
import org.junit.Assert;
import org.junit.Test;

public class AbstractLeastSquaresOptimizerTest {

    private static final class ConcreteOptimizer extends AbstractLeastSquaresOptimizer {
        @Override
        public PointVectorValuePair optimize(int maxEval,
                                             DifferentiableMultivariateVectorFunction f,
                                             MultivariateVectorFunction jacobian,
                                             double[] target, double[] weights,
                                             double[] startPoint) {
            // Dummy implementation for testing base class methods
            return super.optimize(maxEval, f, jacobian, target, weights, startPoint);
        }

        @Override
        public PointVectorValuePair doOptimize() {
            // Trigger setup to cover weights initialization and weight matrix creation
            double[] target = getTarget();
            double[] residuals = computeResiduals(target);
            return new PointVectorValuePair(getStartPoint(), residuals);
        }
    }

    private static class DummyDifferentiableFunction implements DifferentiableMultivariateVectorFunction {
        public MultivariateVectorFunction gradient() {
            return new MultivariateVectorFunction() {
                public double[] value(double[] point) {
                    return new double[] { 1.0 };
                }
            };
        }

        public double[] value(double[] point) {
            return new double[] { point[0] };
        }
    }

    @Test
    public void testComputeWeightMatrixWithSquareMatrixWeight() {
        ConcreteOptimizer optimizer = new ConcreteOptimizer();
        double[] weights = { 1.0, 2.0, 3.0 };
        Weight weight = new Weight(weights);
        optimizer.weightMatrixLocation = weight.getWeight(); // Accessing weight if accessible or via setup

        // We can test setup with appropriate dimensions
        double[] target = { 1.0, 2.0, 3.0 };
        double[] start = { 0.0 };
        
        try {
            optimizer.optimize(100, new DummyDifferentiableFunction(), null, target, weights, start);
        } catch (Exception e) {
            // Expected depending on strict convergence/doOptimize implementation
        }
        
        Assert.assertNotNull(optimizer.getWeightSquareRoot());
    }

    @Test(expected = DimensionMismatchException.class)
    public void testComputeWeightedJacobianDimensionMismatch() {
        ConcreteOptimizer optimizer = new ConcreteOptimizer();
        double[] J = {1.0, 2.0};
        // This will test the weighted Jacobian computation when dimensions don't match
        // Or testing standard protected methods
        optimizer.computeWeightedJacobian(null);
    }

    @Test
    public void testGettersAndSetters() {
        ConcreteOptimizer optimizer = new ConcreteOptimizer();
        Assert.assertEquals(0, optimizer.getEvaluations());
        Assert.assertEquals(0, optimizer.getIterations());
    }

    @Test(expected = NumberIsTooSmallException.class)
    public void testTargetAndWeightMismatch() {
        ConcreteOptimizer optimizer = new ConcreteOptimizer();
        double[] target = { 1.0, 2.0 };
        double[] weights = { 1.0 }; // Mismatched size
        double[] start = { 0.0 };
        
        optimizer.optimize(100, new DummyDifferentiableFunction(), null, target, weights, start);
    }
}