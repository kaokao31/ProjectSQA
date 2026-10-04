package org.apache.commons.math.stat.regression;

import org.junit.Assert;
import org.junit.Test;

public class SimpleRegressionTest {

    @Test
    public void testRegression() {
        SimpleRegression regression = new SimpleRegression();
        
        // Test initial state
        Assert.assertEquals(0L, regression.getN());
        Assert.assertEquals(0.0, regression.getIntercept(), 1e-10);
        Assert.assertEquals(0.0, regression.getSlope(), 1e-10);
        Assert.assertEquals(0.0, regression.getSumSquaredErrors(), 1e-10);
        Assert.assertEquals(0.0, regression.getTotalSumSquares(), 1e-10);
        Assert.assertEquals(0.0, regression.getRegressionSumSquares(), 1e-10);
        Assert.assertEquals(0.0, regression.getMeanSquareError(), 1e-10);
        Assert.assertEquals(0.0, regression.getR(), 1e-10);
        Assert.assertEquals(0.0, regression.getRSquare(), 1e-10);
        Assert.assertEquals(0.0, regression.getSlopeStdErr(), 1e-10);
        Assert.assertEquals(0.0, regression.getInterceptStdErr(), 1e-10);
        Assert.assertEquals(0.0, regression.getSlopeConfidenceInterval(), 1e-10);
        
        try {
            regression.getSlopeConfidenceInterval(0.05);
            // depending on implementation, might throw exception or return NaN
        } catch (Exception e) {
            // expected or handled
        }

        // Add single data point
        regression.addData(1.0, 2.0);
        Assert.assertEquals(1L, regression.getN());
        // With 1 point, variance/SSE etc. are typically 0 or NaN
        Assert.assertEquals(0.0, regression.getSumSquaredErrors(), 1e-10);

        // Add more data points to test regression calculations (Math-105 specific area: SSE / sumOfCrossProducts)
        regression.clear();
        Assert.assertEquals(0L, regression.getN());

        double[] x = {sqlVal(1), sqlVal(2), sqlVal(3), sqlVal(4), sqlVal(5)};
        double[] y = {sqlVal(2), sqlVal(4), sqlVal(5), sqlVal(4), sqlVal(5)};
        
        regression.addData(1.0, 2.0);
        regression.addData(2.0, 4.0);
        regression.addData(3.0, 5.0);
        regression.addData(4.0, 4.0);
        regression.addData(5.0, 5.0);

        Assert.assertEquals(5L, regression.getN());
        
        // Trigger predict
        double predicted = regression.predict(3.0);
        Assert.assertTrue(!Double.isNaN(predicted));

        // Trigger removeData
        regression.removeData(1.0, 2.0);
        Assert.assertEquals(4L, regression.getN());

        // Trigger multiple data add
        double[][] data = {
            {1.0, 2.0},
            {2.0, 3.0},
            {3.0, 3.0},
            {4.0, 6.0},
            {5.0, 5.0}
        };
        SimpleRegression regression2 = new SimpleRegression();
        regression2.addData(data);
        Assert.assertEquals(5L, regression2.getN());
        
        // Test statistics getters on regression2
        Assert.assertTrue(!Double.isNaN(regression2.getSlope()));
        Assert.assertTrue(!Double.isNaN(regression2.getIntercept()));
        Assert.assertTrue(!Double.isNaN(regression2.getRSquare()));
        Assert.assertTrue(!Double.isNaN(regression2.getSlopeStdErr()));
        Assert.assertTrue(!Double.isNaN(regression2.getInterceptStdErr()));
        Assert.assertTrue(regression2.getSumSquaredErrors() >= 0.0);
        
        // Confidence interval with alpha
        try {
            regression2.getSlopeConfidenceInterval(0.05);
        } catch (Exception e) {
            // pass
        }
        
        try {
            regression2.getSlopeConfidenceInterval(1.5);
            Assert.fail("Expected IllegalArgumentException for invalid alpha");
        } catch (Exception e) {
            // expected
        }
        
        try {
            regression2.getSlopeConfidenceInterval(-0.1);
            Assert.fail("Expected IllegalArgumentException for invalid alpha");
        } catch (Exception e) {
            // expected
        }

        // Test hasNoVariance scenarios or edge cases
        SimpleRegression regConstantY = new SimpleRegression();
        regConstantY.addData(1.0, 5.0);
        regConstantY.addData(2.0, 5.0);
        regConstantY.addData(3.0, 5.0);
        Assert.assertEquals(0.0, regConstantY.getSlope(), 1e-10);
        Assert.assertEquals(5.0, regConstantY.getIntercept(), 1e-10);

        SimpleRegression regConstantX = new SimpleRegression();
        regConstantX.addData(2.0, 1.0);
        regConstantX.addData(2.0, 2.0);
        regConstantX.addData(2.0, 3.0);
        Assert.assertTrue(Double.isNaN(regConstantX.getSlope()) || regConstantX.getSlope() == 0.0);
    }

    private double sqlVal(double val) {
        return val;
    }
}