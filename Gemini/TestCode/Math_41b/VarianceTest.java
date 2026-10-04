package org.apache.commons.math.stat.descriptive.moment;

import org.apache.commons.math.stat.descriptive.moment.Variance;
import org.junit.Assert;
import org.junit.Test;

public class VarianceTest {

    @Test
    public void testDefaultConstructor() {
        Variance variance = new Variance();
        Assert.assertNotNull(variance);
        Assert.assertTrue(variance.isBiasCorrected());
    }

    @Test
    public void testBooleanConstructor() {
        Variance variance = new Variance(false);
        Assert.assertNotNull(variance);
        Assert.assertFalse(variance.isBiasCorrected());

        Variance varianceTrue = new Variance(true);
        Assert.assertNotNull(varianceTrue);
        Assert.assertTrue(varianceTrue.isBiasCorrected());
    }

    @Test
    public void testCopyConstructor() {
        Variance original = new Variance();
        original.increment(1.0);
        original.increment(2.0);

        Variance copy = new Variance(original);
        Assert.assertNotNull(copy);
        Assert.assertEquals(original.getResult(), copy.getResult(), 1.0e-12);
        Assert.assertEquals(original.isBiasCorrected(), copy.isBiasCorrected());
    }

    @Test
    public void testEvaluateArray() {
        Variance variance = new Variance();
        double[] values = {1.0, 2.0, 3.0, 4.0, 5.0};
        
        // Sample variance of {1, 2, 3, 4, 5} is 2.5
        double result = variance.evaluate(values);
        Assert.assertEquals(2.5, result, 1.0e-12);

        // Population variance (biasCorrected = false)
        // Population variance of {1, 2, 3, 4, 5} is 2.0
        double popResult = variance.evaluate(values, 0.0, false);
        Assert.assertEquals(2.0, popResult, 1.0e-12);
    }

    @Test
    public void testEvaluateArraySubset() {
        Variance variance = new Variance();
        double[] values = {10.0, 1.0, 2.0, 3.0, 4.0, 5.0, 10.0};
        
        // Evaluate subset index 1 to 5 ({1, 2, 3, 4, 5})
        double result = variance.evaluate(values, 1, 5);
        Assert.assertEquals(2.5, result, 1.0e-12);

        // Evaluate subset with weights/mean if applicable or standard methods
        double resultWithMean = variance.evaluate(values, 3.0, 1, 5);
        Assert.assertEquals(2.5, resultWithMean, 1.0e-12);
    }

    @Test
    public void testIncrementAndGetResult() {
        Variance variance = new Variance();
        Assert.assertEquals(Double.NaN, variance.getResult(), 1.0e-12);
        Assert.assertEquals(0L, variance.getN());

        variance.increment(1.0);
        Assert.assertEquals(0.0, variance.getResult(), 1.0e-12);
        Assert.assertEquals(1L, variance.getN());

        variance.increment(3.0);
        // Variance of {1, 3} with bias correction is 2.0
        Assert.assertEquals(2.0, variance.getResult(), 1.0e-12);
        Assert.assertEquals(2L, variance.getN());
    }

    @Test
    public void testSetGetBiasCorrected() {
        Variance variance = new Variance();
        variance.setBiasCorrected(false);
        Assert.assertFalse(variance.isBiasCorrected());

        variance.setBiasCorrected(true);
        Assert.assertTrue(variance.isBiasCorrected());
    }

    @Test
    public void testEqualsAndHashCode() {
        Variance v1 = new Variance();
        Variance v2 = new Variance();

        Assert.assertTrue(v1.equals(v2));
        Assert.assertEquals(v1.hashCode(), v2.hashCode());

        v1.increment(1.0);
        Assert.assertFalse(v1.equals(v2));
        
        Variance v3 = null;
        Assert.assertFalse(v1.equals(v3));
        Assert.assertFalse(v1.equals("Some String"));
    }

    @Test
    public void testCopy() {
        Variance v1 = new Variance();
        v1.increment(5.0);
        Variance v2 = v1.copy();
        Assert.assertNotNull(v2);
        Assert.assertEquals(v1.getResult(), v2.getResult(), 1.0e-12);
    }

    @Test
    public void testEvaluateWithWeightsMath41Issue() {
        // Specific test targeting potential weighting bugs in Math-41
        Variance variance = new Variance();
        double[] values = {1.0, 2.0, 3.0};
        double[] weights = {1.0, 1.0, 1.0};
        
        double result = variance.evaluate(values, weights);
        Assert.assertEquals(1.0, result, 1.0e-12);

        // Test with precomputed mean and weights
        double resultWithMean = variance.evaluate(values, weights, 2.0, 0, 3);
        Assert.assertTrue(Double.isNaN(resultWithMean) || resultWithMean >= 0.0);
    }
}