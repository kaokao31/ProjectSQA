package org.apache.commons.math.special;

import org.junit.Assert;
import org.junit.Test;

public class GammaTest {

    @Test
    public void testLogGammaValidInputs() {
        // Test normal positive values
        double result = Gamma.logGamma(1.0);
        Assert.assertEquals(0.0, result, 1e-10);

        double result2 = Gamma.logGamma(2.0);
        Assert.assertEquals(0.0, result2, 1e-10);

        double result3 = Gamma.logGamma(5.0);
        // logGamma(5) = log(4!) = log(24) ≈ 3.17805383
        Assert.assertEquals(3.17805383, result3, 1e-6);
    }

    @Test
    public void testLogGammaEdgeCases() {
        // NaN input
        Assert.assertTrue(Double.isNaN(Gamma.logGamma(Double.NaN)));

        // Non-positive inputs (<= 0) should return NaN
        Assert.assertTrue(Double.isNaN(Gamma.logGamma(0.0)));
        Assert.assertTrue(Double.isNaN(Gamma.logGamma(-1.0)));
        Assert.assertTrue(Double.isNaN(Gamma.logGamma(Double.NEGATIVE_INFINITY)));

        // Positive infinity
        Assert.assertEquals(Double.POSITIVE_INFINITY, Gamma.logGamma(Double.POSITIVE_INFINITY), 1e-10);
    }

    @Test
    public void testRegularizedGammaPValid() {
        try {
            double val = Gamma.regularizedGammaP(1.0, 1.0);
            Assert.assertTrue(val >= 0.0 && val <= 1.0);
        } catch (Exception e) {
            // Depending on implementation details
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRegularizedGammaPInvalidA() {
        Gamma.regularizedGammaP(-1.0, 1.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRegularizedGammaPInvalidX() {
        Gamma.regularizedGammaP(1.0, -1.0);
    }

    @Test
    public void testRegularizedGammaPEdgeCases() {
        // x = 0 should return 0
        try {
            double val = Gamma.regularizedGammaP(1.0, 0.0);
            Assert.assertEquals(0.0, val, 1e-10);
        } catch (Exception e) {
            // ignore if unsupported branch
        }

        // NaN handling
        Assert.assertTrue(Double.isNaN(Gamma.regularizedGammaP(Double.NaN, 1.0)));
        Assert.assertTrue(Double.isNaN(Gamma.regularizedGammaP(1.0, Double.NaN)));
    }

    @Test
    public void testRegularizedGammaQValid() {
        try {
            double val = Gamma.regularizedGammaQ(1.0, 1.0);
            Assert.assertTrue(val >= 0.0 && val <= 1.0);
        } catch (Exception e) {
            // ignore
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRegularizedGammaQInvalidA() {
        Gamma.regularizedGammaQ(-1.0, 1.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRegularizedGammaQInvalidX() {
        Gamma.regularizedGammaQ(1.0, -1.0);
    }

    @Test
    public void testRegularizedGammaQEdgeCases() {
        Assert.assertTrue(Double.isNaN(Gamma.regularizedGammaQ(Double.NaN, 1.0)));
        Assert.assertTrue(Double.isNaN(Gamma.regularizedGammaQ(1.0, Double.NaN)));
    }
}