package org.apache.commons.math3.util;

import org.junit.Test;
import org.junit.Assert;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.exception.NotPositiveException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.exception.NonMonotonicSequenceException;

public class MathArraysTest {

    @Test
    public void testScale() {
        double[] val = {1.0, 2.0, 3.0};
        double[] scaled = MathArrays.scale(2.0, val);
        Assert.assertNotNull(scaled);
        Assert.assertNotSame(val, scaled);
        Assert.assertEquals(2.0, scaled[0], 1e-15);
        Assert.assertEquals(4.0, scaled[1], 1e-15);
        Assert.assertEquals(6.0, scaled[2], 1e-15);
    }

    @Test
    public void testScaleInPlace() {
        double[] val = {1.0, 2.0, 3.0};
        double[] scaled = MathArrays.scaleInPlace(2.0, val);
        Assert.assertNotNull(scaled);
        Assert.assertSame(val, scaled);
        Assert.assertEquals(2.0, val[0], 1e-15);
        Assert.assertEquals(4.0, val[1], 1e-15);
        Assert.assertEquals(6.0, val[2], 1e-15);
    }

    @Test
    public void testEchevinDistances() {
        double[] p1 = {1.0, 2.0};
        double[] p2 = {4.0, 6.0};
        double distSq = MathArrays.distanceSq(p1, p2);
        Assert.assertEquals(25.0, distSq, 1e-15);

        double dist = MathArrays.distance(p1, p2);
        Assert.assertEquals(5.0, dist, 1e-15);
    }

    @Test
    public void testCosine() {
        double[] p1 = {1.0, 0.0};
        double[] p2 = {0.0, 1.0};
        double cos = MathArrays.cosine(p1, p2);
        Assert.assertEquals(0.0, cos, 1e-15);

        double[] p3 = {1.0, 2.0};
        double cosSelf = MathArrays.cosine(p3, p3);
        Assert.assertEquals(1.0, cosSelf, 1e-15);
    }

    @Test(expected = DimensionMismatchException.class)
    public void testCosineDimensionMismatch() {
        double[] p1 = {1.0, 2.0};
        double[] p2 = {1.0};
        MathArrays.cosine(p1, p2);
    }

    @Test(expected = NullArgumentException.class)
    public void testCosineNullArg1() {
        MathArrays.cosine(null, new double[2]);
    }

    @Test(expected = NullArgumentException.class)
    public void testCosineNullArg2() {
        MathArrays.cosine(new double[2], null);
    }

    @Test
    public void testCheckEqualLength() {
        double[] a = {1.0, 2.0};
        double[] b = {3.0, 4.0};
        MathArrays.checkEqualLength(a, b); // should pass
    }

    @Test(expected = DimensionMismatchException.class)
    public void testCheckEqualLengthFailure() {
        double[] a = {1.0, 2.0};
        double[] b = {3.0};
        MathArrays.checkEqualLength(a, b);
    }

    @Test
    public void testCheckPositive() {
        double[] a = {1.0, 2.0};
        MathArrays.checkPositive(a); // should pass
    }

    @Test(expected = NotStrictlyPositiveException.class)
    public void testCheckPositiveFailure() {
        double[] a = {1.0, 0.0};
        MathArrays.checkPositive(a);
    }

    @Test
    public void testCheckNonNegative() {
        double[] a = {0.0, 2.0};
        MathArrays.checkNonNegative(a); // should pass
    }

    @Test(expected = NotPositiveException.class)
    public void testCheckNonNegativeFailure() {
        double[] a = {1.0, -0.1};
        MathArrays.checkNonNegative(a);
    }

    @Test
    public void testCheckNormalized() {
        double[] a = {0.5, 0.5};
        MathArrays.checkNormalized(a); // should pass
    }

    @Test(expected = MathArrays.OrderException.class)
    public void testCheckNormalizedFailure() {
        double[] a = {0.5, 0.6};
        MathArrays.checkNormalized(a);
    }

    @Test
    public void testSortInPlace() {
        double[] x = {3.0, 1.0, 2.0};
        double[] y = {30.0, 10.0, 20.0};
        MathArrays.sortInPlace(x, y);
        Assert.assertEquals(1.0, x[0], 1e-15);
        Assert.assertEquals(2.0, x[1], 1e-15);
        Assert.assertEquals(3.0, x[2], 1e-15);
        Assert.assertEquals(10.0, y[0], 1e-15);
        Assert.assertEquals(20.0, y[1], 1e-15);
        Assert.assertEquals(30.0, y[2], 1e-15);
    }

    @Test
    public void testSortInPlaceMultipleArrays() {
        double[] x = {2.0, 1.0};
        double[][] y = {{20.0, 10.0}, {200.0, 100.0}};
        MathArrays.sortInPlace(x, y);
        Assert.assertEquals(1.0, x[0], 1e-15);
        Assert.assertEquals(10.0, y[0][0], 1e-15);
        Assert.assertEquals(100.0, y[1][0], 1e-15);
    }

    @Test(expected = NullArgumentException.class)
    public void testSortInPlaceNullX() {
        MathArrays.sortInPlace(null, new double[2]);
    }

    @Test(expected = DimensionMismatchException.class)
    public void testSortInPlaceDimensionMismatch() {
        double[] x = {1.0, 2.0};
        double[] y = {1.0};
        MathArrays.sortInPlace(x, y);
    }

    @Test
    public void testCwiseProduct() {
        double[] a = {1.0, 2.0, 3.0};
        double[] b = {4.0, 5.0, 6.0};
        double[] res = MathArrays.ebeMultiply(a, b);
        Assert.assertEquals(4.0, res[0], 1e-15);
        Assert.assertEquals(10.0, res[1], 1e-15);
        Assert.assertEquals(18.0, res[2], 1e-15);
    }

    @Test
    public void testCwiseDivide() {
        double[] a = {4.0, 10.0, 18.0};
        double[] b = {1.0, 2.0, 3.0};
        double[] res = MathArrays.ebeDivide(a, b);
        Assert.assertEquals(4.0, res[0], 1e-15);
        Assert.assertEquals(5.0, res[1], 1e-15);
        Assert.assertEquals(6.0, res[2], 1e-15);
    }

    @Test
    public void testL1Distance() {
        double[] p1 = {1.0, 2.0};
        double[] p2 = {4.0, 6.0};
        Assert.assertEquals(7.0, MathArrays.distance1(p1, p2), 1e-15);
    }

    @Test
    public void testLinfDistance() {
        double[] p1 = {1.0, 2.0};
        double[] p2 = {4.0, 6.0};
        Assert.assertEquals(4.0, MathArrays.distanceInf(p1, p2), 1e-15);
    }

    @Test
    public void testLinearCombination2() {
        double a1 = 1.0;
        double b1 = 2.0;
        double a2 = 3.0;
        double b2 = 4.0;
        double res = MathArrays.linearCombination(a1, b1, a2, b2);
        Assert.assertEquals(14.0, res, 1e-15);
    }

    @Test
    public void testLinearCombination3() {
        double res = MathArrays.linearCombination(1.0, 1.0, 2.0, 2.0, 3.0, 3.0);
        Assert.assertEquals(14.0, res, 1e-15);
    }

    @Test
    public void testLinearCombination4() {
        double res = MathArrays.linearCombination(1.0, 1.0, 2.0, 2.0, 3.0, 3.0, 4.0, 4.0);
        Assert.assertEquals(30.0, res, 1e-15);
    }

    @Test
    public void testLinearCombinationArray() {
        double[] a = {1.0, 2.0, 3.0};
        double[] b = {1.0, 2.0, 3.0};
        Assert.assertEquals(14.0, MathArrays.linearCombination(a, b), 1e-15);
    }

    @Test(expected = DimensionMismatchException.class)
    public void testLinearCombinationArrayMismatch() {
        double[] a = {1.0, 2.0};
        double[] b = {1.0};
        MathArrays.linearCombination(a, b);
    }

    @Test
    public void testCheckNonNegativeInt() {
        int[] a = {0, 1, 2};
        MathArrays.checkNonNegative(a);
    }

    @Test(expected = NotPositiveException.class)
    public void testCheckNonNegativeIntFailure() {
        int[] a = {0, -1, 2};
        MathArrays.checkNonNegative(a);
    }

    @Test
    public void testCheckStrictlyPositiveInt() {
        int[] a = {1, 2, 3};
        MathArrays.checkStrictlyPositive(a);
    }

    @Test(expected = NotStrictlyPositiveException.class)
    public void testCheckStrictlyPositiveIntFailure() {
        int[] a = {0, 1, 2};
        MathArrays.checkStrictlyPositive(a);
    }

    @Test
    public void testVerifyValues() {
        double[] values = {1.0, 2.0, 3.0};
        boolean verified = MathArrays.verifyValues(values, 0, 3);
        Assert.assertTrue(verified);
    }

    @Test(expected = NullArgumentException.class)
    public void testVerifyValuesNull() {
        MathArrays.verifyValues(null, 0, 0);
    }

    @Test(expected = NumberIsTooSmallException.class)
    public void testVerifyValuesTooSmall() {
        double[] values = {1.0};
        MathArrays.verifyValues(values, 0, 0);
    }

    @Test
    public void testIsMonotonic() {
        double[] inc = {1.0, 2.0, 2.0, 3.0};
        Assert.assertTrue(MathArrays.isMonotonic(inc, MathArrays.OrderDirection.INCREASING, true));
        Assert.assertFalse(MathArrays.isMonotonic(inc, MathArrays.OrderDirection.INCREASING, false));

        double[] strictInc = {1.0, 2.0, 3.0};
        Assert.assertTrue(MathArrays.isMonotonic(strictInc, MathArrays.OrderDirection.INCREASING, false));

        double[] dec = {3.0, 2.0, 2.0, 1.0};
        Assert.assertTrue(MathArrays.isMonotonic(dec, MathArrays.OrderDirection.DECREASING, true));
        Assert.assertFalse(MathArrays.isMonotonic(dec, MathArrays.OrderDirection.DECREASING, false));
    }

    @Test(expected = NonMonotonicSequenceException.class)
    public void testCheckOrderIncreasing() {
        double[] val = {1.0, 3.0, 2.0};
        MathArrays.checkOrder(val, MathArrays.OrderDirection.INCREASING, false);
    }

    @Test(expected = NonMonotonicSequenceException.class)
    public void testCheckOrderDecreasing() {
        double[] val = {3.0, 1.0, 2.0};
        MathArrays.checkOrder(val, MathArrays.OrderDirection.DECREASING, false);
    }

    @Test
    public void testCheckOrderValid() {
        double[] val = {1.0, 2.0, 3.0};
        MathArrays.checkOrder(val, MathArrays.OrderDirection.INCREASING, false);
        MathArrays.checkOrder(val, MathArrays.OrderDirection.INCREASING, true);
    }

    @Test
    public void testConvolve() {
        double[] x = {1.0, 2.0};
        double[] y = {3.0, 4.0};
        double[] conv = MathArrays.convolve(x, y);
        Assert.assertNotNull(conv);
        Assert.assertEquals(3.0, conv[0], 1e-15);
        Assert.assertEquals(10.0, conv[1], 1e-15);
        Assert.assertEquals(8.0, conv[2], 1e-15);
    }

    @Test(expected = NullArgumentException.class)
    public void testConvolveNullX() {
        MathArrays.convolve(null, new double[2]);
    }

    @Test(expected = NullArgumentException.class)
    public void testConvolveNullY() {
        MathArrays.convolve(new double[2], null);
    }
}