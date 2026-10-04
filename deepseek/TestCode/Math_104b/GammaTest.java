package org.apache.commons.math.special;

import org.junit.Test;
import org.junit.Assert;

/**
 * Test suite for the Gamma class, targeting high coverage and fault detection.
 * Designed for JUnit 4 and JDK 8.
 */
public class GammaTest {

    private static final double DELTA = 1e-15;

    // ---------- logGamma tests ----------

    @Test
    public void testLogGammaPositive() {
        // Known values from mathematical tables or reliable sources
        Assert.assertEquals("logGamma(1.0)", 0.0, Gamma.logGamma(1.0), DELTA);
        Assert.assertEquals("logGamma(2.0)", 0.0, Gamma.logGamma(2.0), DELTA);
        Assert.assertEquals("logGamma(3.0)", Math.log(2.0), Gamma.logGamma(3.0), DELTA);
        Assert.assertEquals("logGamma(4.0)", Math.log(6.0), Gamma.logGamma(4.0), DELTA);
        Assert.assertEquals("logGamma(5.0)", Math.log(24.0), Gamma.logGamma(5.0), DELTA);
        Assert.assertEquals("logGamma(0.5)", Math.log(Math.sqrt(Math.PI)), Gamma.logGamma(0.5), 1e-14);
        Assert.assertEquals("logGamma(10.0)", Math.log(362880.0), Gamma.logGamma(10.0), 1e-12);
    }

    @Test
    public void testLogGammaLarge() {
        // Large argument to test Lanczos approximation
        double value = Gamma.logGamma(100.0);
        Assert.assertTrue("logGamma(100) should be finite", Double.isFinite(value));
        // Compare with Stirling approximation: logGamma(x) ≈ x*log(x) - x + 0.5*log(2π/x)
        double x = 100.0;
        double expected = x * Math.log(x) - x + 0.5 * Math.log(2.0 * Math.PI / x);
        Assert.assertEquals("logGamma(100) approximation", expected, value, 1e-10);
    }

    @Test
    public void testLogGammaNegativeNonInteger() {
        // Negative non-integer arguments should be finite (reflection formula)
        double value = Gamma.logGamma(-0.5);
        Assert.assertTrue("logGamma(-0.5) should be finite", Double.isFinite(value));
        // Known: Gamma(-0.5) = -2*sqrt(pi), so logGamma(-0.5) = log(2*sqrt(pi))? Actually log|Gamma|.
        // We'll just check it's not NaN.
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLogGammaNegativeInteger() {
        // Negative integer arguments should throw exception (pole)
        Gamma.logGamma(-1.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLogGammaZero() {
        // Zero argument should throw exception (pole)
        Gamma.logGamma(0.0);
    }

    @Test
    public void testLogGammaNaN() {
        // NaN input should propagate
        double result = Gamma.logGamma(Double.NaN);
        Assert.assertTrue("logGamma(NaN) should be NaN", Double.isNaN(result));
    }

    @Test
    public void testLogGammaPositiveInfinity() {
        double result = Gamma.logGamma(Double.POSITIVE_INFINITY);
        Assert.assertTrue("logGamma(+Inf) should be +Inf", Double.isInfinite(result) && result > 0);
    }

    // ---------- regularizedGammaP tests ----------

    @Test
    public void testRegularizedGammaP() {
        // Known values: regularizedGammaP(1, x) = 1 - exp(-x)
        double a = 1.0;
        double x = 2.0;
        double expected = 1.0 - Math.exp(-x);
        Assert.assertEquals("regularizedGammaP(1,2)", expected, Gamma.regularizedGammaP(a, x), 1e-15);

        // regularizedGammaP(2, 1) = 1 - (1+1)*exp(-1) = 1 - 2/e ≈ 0.2642411176571153
        a = 2.0;
        x = 1.0;
        expected = 1.0 - (1.0 + x) * Math.exp(-x);
        Assert.assertEquals("regularizedGammaP(2,1)", expected, Gamma.regularizedGammaP(a, x), 1e-15);

        // Edge: x=0 => 0 for a>0
        Assert.assertEquals("regularizedGammaP(2,0)", 0.0, Gamma.regularizedGammaP(2.0, 0.0), DELTA);

        // Edge: large x => 1
        Assert.assertEquals("regularizedGammaP(2,100)", 1.0, Gamma.regularizedGammaP(2.0, 100.0), 1e-10);
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
    public void testRegularizedGammaPNaN() {
        double result = Gamma.regularizedGammaP(Double.NaN, 1.0);
        Assert.assertTrue("NaN a", Double.isNaN(result));
        result = Gamma.regularizedGammaP(1.0, Double.NaN);
        Assert.assertTrue("NaN x", Double.isNaN(result));
    }

    // ---------- regularizedGammaQ tests ----------

    @Test
    public void testRegularizedGammaQ() {
        // Q = 1 - P
        double a = 2.0;
        double x = 1.0;
        double p = Gamma.regularizedGammaP(a, x);
        double q = Gamma.regularizedGammaQ(a, x);
        Assert.assertEquals("P+Q=1", 1.0, p + q, 1e-15);

        // Edge: x=0 => Q=1 for a>0
        Assert.assertEquals("regularizedGammaQ(2,0)", 1.0, Gamma.regularizedGammaQ(2.0, 0.0), DELTA);

        // Edge: large x => Q=0
        Assert.assertEquals("regularizedGammaQ(2,100)", 0.0, Gamma.regularizedGammaQ(2.0, 100.0), 1e-10);
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
    public void testRegularizedGammaQNaN() {
        double result = Gamma.regularizedGammaQ(Double.NaN, 1.0);
        Assert.assertTrue("NaN a", Double.isNaN(result));
        result = Gamma.regularizedGammaQ(1.0, Double.NaN);
        Assert.assertTrue("NaN x", Double.isNaN(result));
    }

    // ---------- Additional edge cases for bug detection ----------

    @Test
    public void testLogGammaNearPoles() {
        // Very close to negative integer (but not exactly)
        double value = Gamma.logGamma(-0.999999999999);
        Assert.assertTrue("logGamma near -1 should be finite", Double.isFinite(value));
        // Should be large positive due to reflection
        Assert.assertTrue("logGamma near -1 should be large", value > 1e10);
    }

    @Test
    public void testRegularizedGammaPConvergence() {
        // Test with small a and x to ensure series converges
        double a = 0.1;
        double x = 0.1;
        double result = Gamma.regularizedGammaP(a, x);
        Assert.assertTrue("Result should be between 0 and 1", result >= 0.0 && result <= 1.0);
        Assert.assertTrue("Result should be finite", Double.isFinite(result));
    }

    @Test
    public void testRegularizedGammaQConvergence() {
        double a = 0.1;
        double x = 0.1;
        double result = Gamma.regularizedGammaQ(a, x);
        Assert.assertTrue("Result should be between 0 and 1", result >= 0.0 && result <= 1.0);
        Assert.assertTrue("Result should be finite", Double.isFinite(result));
    }

    @Test
    public void testLogGammaConsistency() {
        // logGamma(x+1) = log(x) + logGamma(x)
        double x = 5.0;
        double lhs = Gamma.logGamma(x + 1.0);
        double rhs = Math.log(x) + Gamma.logGamma(x);
        Assert.assertEquals("Recurrence relation", lhs, rhs, 1e-14);
    }

    @Test
    public void testRegularizedGammaPRecurrence() {
        // For a > 0, regularizedGammaP(a+1, x) = regularizedGammaP(a, x) - (x^a * exp(-x))/(Gamma(a+1))
        // We'll test numerically
        double a = 2.5;
        double x = 1.2;
        double p_a = Gamma.regularizedGammaP(a, x);
        double p_a1 = Gamma.regularizedGammaP(a + 1.0, x);
        double term = Math.pow(x, a) * Math.exp(-x) / Math.exp(Gamma.logGamma(a + 1.0));
        Assert.assertEquals("Recurrence for P", p_a - term, p_a1, 1e-12);
    }
}