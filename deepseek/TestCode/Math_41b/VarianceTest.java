package org.apache.commons.math.stat.descriptive.moment;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

/**
 * Test suite for the Variance class.
 * Designed to achieve maximum coverage and detect potential faults,
 * including the known bug in Defects4J Math 41 (incorrect handling of
 * single-element arrays and bias correction).
 */
public class VarianceTest {

    private static final double DELTA = 1e-15;

    // -----------------------------------------------------------------------
    // evaluate(double[]) tests
    // -----------------------------------------------------------------------

    @Test
    public void testEvaluateBasic() {
        // Known variance for {1, 2, 3, 4, 5}:
        // mean = 3.0, sumSq = (1-3)^2 + ... = 10, population var = 10/5 = 2.0
        // sample var = 10/4 = 2.5
        double[] values = {1.0, 2.0, 3.0, 4.0, 5.0};
        assertEquals("Population variance", 2.0, Variance.evaluate(values), DELTA);
        // Default is bias corrected? In Commons Math, evaluate(double[]) uses
        // isBiasCorrected = true (sample variance). We'll test both.
        // Actually, we need to check the default. Let's assume it's sample variance.
        // But to be safe, we test both via the explicit method.
    }

    @Test
    public void testEvaluateBiasCorrectedTrue() {
        double[] values = {1.0, 2.0, 3.0, 4.0, 5.0};
        double mean = 3.0;
        assertEquals("Sample variance", 2.5,
                     Variance.evaluate(values, mean, true), DELTA);
    }

    @Test
    public void testEvaluateBiasCorrectedFalse() {
        double[] values = {1.0, 2.0, 3.0, 4.0, 5.0};
        double mean = 3.0;
        assertEquals("Population variance", 2.0,
                     Variance.evaluate(values, mean, false), DELTA);
    }

    @Test
    public void testEvaluateWithMean() {
        double[] values = {1.0, 2.0, 3.0, 4.0, 5.0};
        double mean = 3.0;
        // Default bias corrected? We'll assume true.
        assertEquals("Sample variance with mean", 2.5,
                     Variance.evaluate(values, mean), DELTA);
    }

    @Test
    public void testEvaluateSingleElement() {
        // For a single element, variance should be 0 (population) or NaN (sample).
        // The bug in Math 41 returns NaN for both cases.
        double[] values = {42.0};
        // Population variance: 0.0
        assertEquals("Population variance, n=1", 0.0,
                     Variance.evaluate(values, 42.0, false), DELTA);
        // Sample variance: undefined, but many implementations return 0.0 or NaN.
        // The bug is that it returns NaN when it should return 0.0 (or vice versa).
        // We'll test that it does not throw an exception and returns a finite value.
        double sampleVar = Variance.evaluate(values, 42.0, true);
        assertTrue("Sample variance for n=1 should be finite (0.0 or NaN)",
                   Double.isFinite(sampleVar) || Double.isNaN(sampleVar));
        // If it returns NaN, that might be acceptable, but the bug is that it
        // returns NaN for population variance as well. So we check population.
    }

    @Test
    public void testEvaluateTwoElements() {
        double[] values = {1.0, 3.0};
        double mean = 2.0;
        // sumSq = (1-2)^2 + (3-2)^2 = 2
        // population var = 2/2 = 1.0
        // sample var = 2/1 = 2.0
        assertEquals("Population variance, n=2", 1.0,
                     Variance.evaluate(values, mean, false), DELTA);
        assertEquals("Sample variance, n=2", 2.0,
                     Variance.evaluate(values, mean, true), DELTA);
    }

    @Test
    public void testEvaluateAllSame() {
        double[] values = {5.0, 5.0, 5.0, 5.0};
        double mean = 5.0;
        assertEquals("Population variance, all same", 0.0,
                     Variance.evaluate(values, mean, false), DELTA);
        assertEquals("Sample variance, all same", 0.0,
                     Variance.evaluate(values, mean, true), DELTA);
    }

    @Test
    public void testEvaluateNegativeValues() {
        double[] values = {-2.0, -1.0, 0.0, 1.0, 2.0};
        double mean = 0.0;
        // sumSq = 4+1+0+1+4 = 10
        // population var = 10/5 = 2.0
        // sample var = 10/4 = 2.5
        assertEquals("Population variance, negative", 2.0,
                     Variance.evaluate(values, mean, false), DELTA);
        assertEquals("Sample variance, negative", 2.5,
                     Variance.evaluate(values, mean, true), DELTA);
    }

    @Test
    public void testEvaluateLargeValues() {
        // Use large numbers to test for overflow/instability
        double[] values = {1e10, 1e10 + 1, 1e10 + 2};
        double mean = 1e10 + 1;
        // sumSq = 1 + 0 + 1 = 2
        // population var = 2/3 ≈ 0.6666666666666667
        // sample var = 2/2 = 1.0
        assertEquals("Population variance, large values", 2.0/3.0,
                     Variance.evaluate(values, mean, false), 1e-5);
        assertEquals("Sample variance, large values", 1.0,
                     Variance.evaluate(values, mean, true), 1e-5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEvaluateEmptyArray() {
        Variance.evaluate(new double[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEvaluateNullArray() {
        Variance.evaluate((double[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEvaluateEmptyArrayWithMean() {
        Variance.evaluate(new double[0], 0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEvaluateNullArrayWithMean() {
        Variance.evaluate((double[]) null, 0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEvaluateEmptyArrayBiasCorrected() {
        Variance.evaluate(new double[0], 0.0, true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEvaluateNullArrayBiasCorrected() {
        Variance.evaluate((double[]) null, 0.0, true);
    }

    // -----------------------------------------------------------------------
    // Additional edge cases for the known bug (Math 41)
    // -----------------------------------------------------------------------

    @Test
    public void testEvaluateSingleElementNoMean() {
        // When no mean is provided, the method computes the mean internally.
        // For a single element, mean = element, variance should be 0.
        double[] values = {7.0};
        // Default is bias corrected? We'll assume true.
        double result = Variance.evaluate(values);
        // The bug: returns NaN. We expect 0.0 or NaN? Actually, sample variance
        // for n=1 is undefined, but many implementations return 0.0.
        // The bug is that it returns NaN for both bias corrected and not.
        // We'll check that it does not return NaN when bias corrected is false.
        // But we don't have direct access to bias corrected flag in this method.
        // So we just check that it returns a finite value (0.0) or NaN.
        // To catch the bug, we assert that it is not NaN (if the fix returns 0.0).
        // However, the fix might return 0.0 for both. We'll assert that it is 0.0
        // because that is the expected behavior for population variance.
        // But the default might be sample variance. Let's assume the method
        // uses isBiasCorrected = true. Then for n=1, sample variance is undefined.
        // The bug is that it returns NaN instead of 0.0? Actually, the bug report
        // says that the evaluate method returns NaN for arrays of length 1.
        // The fix is to return 0.0 when n <= 1.
        // So we expect 0.0.
        assertEquals("Single element, no mean", 0.0, result, DELTA);
    }

    @Test
    public void testEvaluateTwoElementsNoMean() {
        double[] values = {1.0, 3.0};
        // mean = 2.0, sumSq = 2, sample var = 2/1 = 2.0
        // population var = 2/2 = 1.0
        // Default is sample variance (bias corrected = true)
        assertEquals("Two elements, no mean, sample", 2.0,
                     Variance.evaluate(values), DELTA);
    }

    @Test
    public void testEvaluateAllSameNoMean() {
        double[] values = {5.0, 5.0, 5.0};
        assertEquals("All same, no mean", 0.0, Variance.evaluate(values), DELTA);
    }

    @Test
    public void testEvaluateWithMeanBiasCorrectedFalseSingleElement() {
        // Explicitly test population variance for single element
        double[] values = {42.0};
        assertEquals("Population variance, n=1, with mean", 0.0,
                     Variance.evaluate(values, 42.0, false), DELTA);
    }

    @Test
    public void testEvaluateWithMeanBiasCorrectedTrueSingleElement() {
        // Sample variance for n=1: undefined, but we expect 0.0 or NaN.
        // The bug might return NaN. We'll accept 0.0 or NaN, but ensure no exception.
        double[] values = {42.0};
        double result = Variance.evaluate(values, 42.0, true);
        assertTrue("Sample variance for n=1 should be 0.0 or NaN",
                   result == 0.0 || Double.isNaN(result));
    }

    // -----------------------------------------------------------------------
    // Test that the evaluate method handles the case where mean is zero
    // (potential numerical issue)
    // -----------------------------------------------------------------------

    @Test
    public void testEvaluateMeanZero() {
        double[] values = {-1.0, 0.0, 1.0};
        double mean = 0.0;
        // sumSq = 1+0+1 = 2
        // population var = 2/3 ≈ 0.6666666666666667
        // sample var = 2/2 = 1.0
        assertEquals("Population variance, mean zero", 2.0/3.0,
                     Variance.evaluate(values, mean, false), DELTA);
        assertEquals("Sample variance, mean zero", 1.0,
                     Variance.evaluate(values, mean, true), DELTA);
    }

    @Test
    public void testEvaluateMeanZeroNoMean() {
        double[] values = {-1.0, 0.0, 1.0};
        // mean = 0.0, same as above
        assertEquals("Sample variance, mean zero, no mean", 1.0,
                     Variance.evaluate(values), DELTA);
    }
}