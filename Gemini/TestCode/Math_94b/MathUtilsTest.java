package org.apache.commons.math.util;

import org.junit.Assert;
import org.junit.Test;

public class MathUtilsTest {

    @Test
    public void testGcdEdgeCases() {
        // Test GCD with zero and negative values which often trigger boundary/sign bugs in Math94
        Assert.assertEquals(5, MathUtils.gcd(5, 0));
        Assert.assertEquals(5, MathUtils.gcd(0, 5));
        Assert.assertEquals(0, MathUtils.gcd(0, 0));
        Assert.assertEquals(5, MathUtils.gcd(-5, 0));
        Assert.assertEquals(5, MathUtils.gcd(0, -5));
        Assert.assertEquals(5, MathUtils.gcd(-5, -5));
        Assert.assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, Integer.MIN_VALUE));
        Assert.assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 0));
        Assert.assertEquals(1, MathUtils.gcd(0, Integer.MIN_VALUE));
    }

    @Test
    public void testFactorialEdgeCases() {
        Assert.assertEquals(1L, MathUtils.factorial(0));
        Assert.assertEquals(1L, MathUtils.factorial(1));
        Assert.assertEquals(3628800L, MathUtils.factorial(10));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFactorialNegative() {
        MathUtils.factorial(-1);
    }

    @Test(expected = ArithmeticException.class)
    public void testFactorialTooLarge() {
        MathUtils.factorial(21);
    }

    @Test
    public void testAddAndMultiplyAndSubAndNormalize() {
        // Basic sanity checks for common utility methods
        Assert.assertEquals(5, MathUtils.addAndCheck(2, 3));
        Assert.assertEquals(6, MathUtils.mulAndCheck(2, 3));
        Assert.assertEquals(-1, MathUtils.subAndCheck(2, 3));
        
        Assert.assertEquals(0.0, MathUtils.normalizeAngle(0.0, 0.0), 1e-12);
    }

    @Test
    public void testEqualsDoubleArray() {
        double[] arr1 = {1.0, 2.0, Double.NaN};
        double[] arr2 = {1.0, 2.0, Double.NaN};
        double[] arr3 = {1.0, 2.0, 3.0};

        Assert.assertTrue(MathUtils.equals(arr1, arr2));
        Assert.assertFalse(MathUtils.equals(arr1, arr3));
        Assert.assertFalse(MathUtils.equals(arr1, null));
        Assert.assertFalse(MathUtils.equals(null, arr1));
        Assert.assertTrue(MathUtils.equals((double[]) null, (double[]) null));
    }
}