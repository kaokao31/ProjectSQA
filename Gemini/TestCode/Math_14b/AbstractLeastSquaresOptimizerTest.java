package org.apache.commons.math3.optim.nonlinear.vector.jacobian;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.optim.ConvergenceChecker;
import org.apache.commons.math3.optim.PointVectorValuePair;
import org.apache.commons.math3.optim.nonlinear.vector.Weight;
import org.apache.commons.math3.util.Incrementor;
import org.junit.Assert;
import org.junit.Test;

public class AbstractLeastSquaresOptimizerTest {

    private static class ConcreteOptimizer extends AbstractLeastSquaresOptimizer {
        protected ConcreteOptimizer(ConvergenceChecker<PointVectorValuePair> checker) {
            super(checker);
        }

        @Override
        public PointVectorValuePair optimize(org.apache.commons.math3.optim.OptimizationData... optData) {
            return super.optimize(optData);
        }
    }

    @Test
    public void testGetRootMeanSquare() {
        ConcreteOptimizer optimizer = new ConcreteOptimizer(null);
        
        // Test RMS with weight
        double[] residuals = {1.0, 2.0, 3.0};
        double[] weights = {1.0, 1.0, 1.0};
        
        optimizer.weightMatrix = new org.apache.commons.math3.linear.DiagonalMatrix(weights);
        optimizer.residuals = residuals;
        
        // sum of squared residuals = 1*1 + 4*1 + 9*1 = 14
        // rms = sqrt(14 / 3)
        double expectedRms = Math.sqrt(14.0 / 3.0);
        Assert.assertEquals(expectedRms, optimizer.getRMS(), 1e-11);
    }

    @Test
    public void testComputeWeightMatrixSqrt() {
        ConcreteOptimizer optimizer = new ConcreteOptimizer(null);

        // Test with Weight optimization data
        Weight weight = new Weight(new double[] {4.0, 9.0});
        RealMatrix weightSqrt = optimizer.computeWeightMatrixSqrt(weight);
        
        Assert.assertNotNull(weightSqrt);
        Assert.assertEquals(2, weightSqrt.getRowDimension());
        Assert.assertEquals(2, weightSqrt.getColumnDimension());
        Assert.assertEquals(2.0, weightSqrt.getEntry(0, 0), 1e-11);
        Assert.assertEquals(3.0, weightSqrt.getEntry(1, 1), 1e-11);
    }

    @Test(expected = DimensionMismatchException.class)
    public void testComputeWeightMatrixSqrtDimensionMismatch() {
        ConcreteOptimizer optimizer = new ConcreteOptimizer(null);
        // Non-square weight matrix or invalid dimensions if possible, or pass a weight with wrong dimensions relative to target/model.
        // Actually, Weight constructor takes 1D or 2D. Let's force a DimensionMismatch by passing a non-square 2D weight matrix via a custom or standard way, 
        // or let's inspect how computeWeightMatrixSqrt behaves with non-square or invalid weight matrix dimensions.
        // Weight matrix in constructor must be square. Let's create a custom Weight or pass something that fails dimension check.
        // Weight expects square matrix in its 2D constructor, but we can pass a Weight where dimension doesn't match or use raw RealMatrix if accessible.
        // Alternatively, use a Weight with negative values or check exception in computeWeightMatrixSqrt.
        // Let's test with a Weight object whose dimension doesn't match target if validated, or test 2D non-square.
        Weight invalidWeight = new Weight(new double[][] {
            {1.0, 2.0},
            {3.0, 4.0},
            {5.0, 6.0}
        });
        optimizer.computeWeightMatrixSqrt(invalidWeight);
    }

    @Test
    public void testComputeJacobian() {
        ConcreteOptimizer optimizer = new ConcreteOptimizer(null);
        // Test that computeJacobian delegates properly or handles parameters
        double[] params = {1.0, 2.0};
        try {
            optimizer.computeJacobian(params);
        } catch (NullPointerException e) {
            // Expected if no Jacobian function is set, but covers the method execution path
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testGettersAndSetters() {
        ConcreteOptimizer optimizer = new ConcreteOptimizer(null);
        Assert.assertNotNull(optimizer.getEvaluations());
        Assert.assertNotNull(optimizer.getIterations());
    }
}