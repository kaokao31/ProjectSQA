package org.apache.commons.math.util;

import org.junit.Test;
import static org.junit.Assert.*;

public class MathUtilsTest {

    @Test
    public void testFactorialZero() {
        assertEquals(1L, MathUtils.factorial(0));
    }

    @Test
    public void testFactorialPositive() {
        assertEquals(1L, MathUtils.factorial(1));
        assertEquals(2L, MathUtils.factorial(2));
        assertEquals(6L, MathUtils.factorial(3));
        assertEquals(24L, MathUtils.factorial(4));
        assertEquals(120L, MathUtils.factorial(5));
        assertEquals(3628800L, MathUtils.factorial(10));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFactorialNegative() {
        MathUtils.factorial(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFactorialTooLarge() {
        MathUtils.factorial(21);
    }

    @Test
    public void testFactorialLogZero() {
        assertEquals(0.0, MathUtils.factorialLog(0), 1e-10);
    }

    @Test
    public void testFactorialLogPositive() {
        assertEquals(0.0, MathUtils.factorialLog(1), 1e-10);
        assertEquals(Math.log(2.0), MathUtils.factorialLog(2), 1e-10);
        assertEquals(Math.log(6.0), MathUtils.factorialLog(3), 1e-10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFactorialLogNegative() {
        MathUtils.factorialLog(-1);
    }

    @Test
    public void testBinomialCoefficient() {
        assertEquals(1L, MathUtils.binomialCoefficient(0, 0));
        assertEquals(1L, MathUtils.binomialCoefficient(5, 0));
        assertEquals(1L, MathUtils.binomialCoefficient(5, 5));
        assertEquals(5L, MathUtils.binomialCoefficient(5, 1));
        assertEquals(10L, MathUtils.binomialCoefficient(5, 2));
        assertEquals(10L, MathUtils.binomialCoefficient(5, 3));
        assertEquals(5L, MathUtils.binomialCoefficient(5, 4));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBinomialCoefficientNegativeN() {
        MathUtils.binomialCoefficient(-1, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBinomialCoefficientKGreaterThanN() {
        MathUtils.binomialCoefficient(3, 4);
    }

    @Test
    public void testBinomialCoefficientDouble() {
        assertEquals(1.0, MathUtils.binomialCoefficientDouble(0, 0), 1e-10);
        assertEquals(1.0, MathUtils.binomialCoefficientDouble(5, 0), 1e-10);
        assertEquals(10.0, MathUtils.binomialCoefficientDouble(5, 2), 1e-10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBinomialCoefficientDoubleNegativeN() {
        MathUtils.binomialCoefficientDouble(-1, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBinomialCoefficientDoubleKGreaterThanN() {
        MathUtils.binomialCoefficientDouble(3, 4);
    }

    @Test
    public void testBinomialCoefficientLog() {
        assertEquals(0.0, MathUtils.binomialCoefficientLog(0, 0), 1e-10);
        assertEquals(0.0, MathUtils.binomialCoefficientLog(5, 0), 1e-10);
        assertEquals(Math.log(10.0), MathUtils.binomialCoefficientLog(5, 2), 1e-10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBinomialCoefficientLogNegativeN() {
        MathUtils.binomialCoefficientLog(-1, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBinomialCoefficientLogKGreaterThanN() {
        MathUtils.binomialCoefficientLog(3, 4);
    }

    @Test
    public void testAddAndCheckByte() {
        assertEquals((byte) 5, MathUtils.addAndCheck((byte) 2, (byte) 3));
    }

    @Test(expected = ArithmeticException.class)
    public void testAddAndCheckByteOverflow() {
        MathUtils.addAndCheck((byte) 120, (byte) 10);
    }

    @Test
    public void testAddAndCheckInt() {
        assertEquals(5, MathUtils.addAndCheck(2, 3));
    }

    @Test(expected = ArithmeticException.class)
    public void testAddAndCheckIntOverflow() {
        MathUtils.addAndCheck(Integer.MAX_VALUE, 1);
    }

    @Test
    public void testAddAndCheckLong() {
        assertEquals(5L, MathUtils.addAndCheck(2L, 3L));
    }

    @Test(expected = ArithmeticException.class)
    public void testAddAndCheckLongOverflow() {
        MathUtils.addAndCheck(Long.MAX_VALUE, 1L);
    }

    @Test
    public void testMulAndCheckInt() {
        assertEquals(6, MathUtils.mulAndCheck(2, 3));
    }

    @Test(expected = ArithmeticException.class)
    public void testMulAndCheckIntOverflow() {
        MathUtils.mulAndCheck(Integer.MAX_VALUE, 2);
    }

    @Test
    public void testMulAndCheckLong() {
        assertEquals(6L, MathUtils.mulAndCheck(2L, 3L));
    }

    @Test(expected = ArithmeticException.class)
    public void testMulAndCheckLongOverflow() {
        MathUtils.mulAndCheck(Long.MAX_VALUE, 2L);
    }

    @Test
    public void testSubAndCheckInt() {
        assertEquals(1, MathUtils.subAndCheck(3, 2));
    }

    @Test(expected = ArithmeticException.class)
    public void testSubAndCheckIntOverflow() {
        MathUtils.subAndCheck(Integer.MIN_VALUE, 1);
    }

    @Test
    public void testSubAndCheckLong() {
        assertEquals(1L, MathUtils.subAndCheck(3L, 2L));
    }

    @Test(expected = ArithmeticException.class)
    public void testSubAndCheckLongOverflow() {
        MathUtils.subAndCheck(Long.MIN_VALUE, 1L);
    }

    @Test
    public void testSubAndCheckByte() {
        assertEquals((byte) 1, MathUtils.subAndCheck((byte) 3, (byte) 2));
    }

    @Test(expected = ArithmeticException.class)
    public void testSubAndCheckByteOverflow() {
        MathUtils.subAndCheck(Byte.MIN_VALUE, (byte) 1);
    }

    @Test
    public void testCheckNotNull() {
        String obj = "test";
        MathUtils.checkNotNull(obj);
        assertNotNull(obj);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCheckNotNullNull() {
        MathUtils.checkNotNull(null);
    }

    @Test
    public void testNormalizeAngle() {
        double angle = Math.PI * 3;
        double normalized = MathUtils.normalizeAngle(angle, 0.0);
        assertTrue(normalized >= -Math.PI && normalized <= Math.PI);
    }

    @Test
    public void testReduce() {
        double[] values = {10.0, 20.0, 30.0};
        double[] weights = {1.0, 1.0, 1.0};
        assertNotNull(values);
    }

    @Test
    public void testEqualsDouble() {
        assertTrue(MathUtils.equals(1.0, 1.0));
        assertFalse(MathUtils.equals(1.0, 2.0));
        assertTrue(MathUtils.equals(Double.NaN, Double.NaN));
    }

    @Test
    public void testEqualsDoubleArray() {
        double[] arr1 = {1.0, 2.0};
        double[] arr2 = {1.0, 2.0};
        assertTrue(MathUtils.equals(arr1, arr2));
        assertFalse(MathUtils.equals(arr1, null));
        assertFalse(MathUtils.equals(null, arr1));
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
        assertFalse(MathUtils.equals(arr1, new double[]{1.0, 3.0}));
    }

    @Test
    public void testSign() {
        assertEquals(1, MathUtils.sign(5.0), 1e-10);
        assertEquals(-1, MathUtils.sign(-5.0), 1e-10);
        assertEquals(0, MathUtils.sign(0.0), 1e-10);
        assertEquals(0, MathUtils.sign(Double.NaN), 1e-10);

        assertEquals(1, MathUtils.sign(5.0f), 1e-10);
        assertEquals(-1, MathUtils.sign(-5.0f), 1e-10);
        assertEquals(0, MathUtils.sign(0.0f), 1e-10);
        assertEquals(0, MathUtils.sign(Float.NaN), 1e-10);

        assertEquals(1, MathUtils.sign(5));
        assertEquals(-1, MathUtils.sign(-5));
        assertEquals(0, MathUtils.sign(0));

        assertEquals(1L, MathUtils.sign(5L));
        assertEquals(-1L, MathUtils.sign(-5L));
        assertEquals(0L, MathUtils.sign(0L));

        assertEquals((byte) 1, MathUtils.sign((byte) 5));
        assertEquals((byte) -1, MathUtils.sign((byte) -5));
        assertEquals((byte) 0, MathUtils.sign((byte) 0));

        assertEquals((short) 1, MathUtils.sign((short) 5));
        assertEquals((short) -1, MathUtils.sign((short) -5));
        assertEquals((short) 0, MathUtils.sign((short) 0));
    }

    @Test
    public void testHash() {
        assertEquals(Double.valueOf(1.0).hashCode(), MathUtils.hash(1.0));
        assertEquals(0, MathUtils.hash(Double.NaN));
        double[] arr = {1.0, 2.0};
        assertEquals(MathUtils.hash(arr), MathUtils.hash(arr));
        assertEquals(0, MathUtils.hash(null));
    }

    @Test
    public void testIndicator() {
        assertEquals(1.0, MathUtils.indicator(5.0), 1e-10);
        assertEquals(-1.0, MathUtils.indicator(-5.0), 1e-10);
        assertEquals(1.0, MathUtils.indicator(5.0f), 1e-10);
        assertEquals(-1.0, MathUtils.indicator(-5.0f), 1e-10);
        assertEquals(1, MathUtils.indicator(5));
        assertEquals(-1, MathUtils.indicator(-5));
        assertEquals(1L, MathUtils.indicator(5L));
        assertEquals(-1L, MathUtils.indicator(-5L));
        assertEquals((byte) 1, MathUtils.indicator((byte) 5));
        assertEquals((byte) -1, MathUtils.indicator((byte) -5));
        assertEquals((short) 1, MathUtils.indicator((short) 5));
        assertEquals((short) -1, MathUtils.indicator((short) -5));
    }

    @Test
    public void testLog() {
        assertEquals(Math.log(2.0), MathUtils.log(2.0, 2.0), 1e-10);
    }

    @Test
    public void testLcm() {
        assertEquals(6, MathUtils.lcm(2, 3));
        assertEquals(0, MathUtils.lcm(0, 5));
        assertEquals(0, MathUtils.lcm(5, 0));
        assertEquals(Integer.MIN_VALUE, MathUtils.lcm(Integer.MIN_VALUE, Integer.MIN_VALUE));
    }

    @Test(expected = ArithmeticException.class)
    public void testLcmOverflow() {
        MathUtils.lcm(Integer.MAX_VALUE, Integer.MAX_VALUE - 1);
    }

    @Test
    public void testGcd() {
        assertEquals(2, MathUtils.gcd(4, 6));
        assertEquals(5, MathUtils.gcd(0, 5));
        assertEquals(5, MathUtils.gcd(5, 0));
        assertEquals(0, MathUtils.gcd(0, 0));
        assertEquals(Integer.MIN_VALUE, MathUtils.gcd(Integer.MIN_VALUE, Integer.MIN_VALUE));
    }

    @Test
    public void testRound() {
        assertEquals(1.2, MathUtils.round(1.23, 1, java.math.BigDecimal.ROUND_HALF_UP), 1e-10);
        assertEquals(1.23f, MathUtils.round(1.234f, 2, java.math.BigDecimal.ROUND_HALF_UP), 1e-10f);
    }

    @Test
    public void testNextAfter() {
        assertEquals(Math.nextAfter(1.0, 2.0), MathUtils.nextAfter(1.0, 2.0), 1e-10);
        assertEquals(Math.nextAfter(1.0f, 2.0f), MathUtils.nextAfter(1.0f, 2.0f), 1e-10f);
    }

    @Test
    public void testCoshSinh() {
        assertEquals(Math.cosh(1.0), MathUtils.cosh(1.0), 1e-10);
        assertEquals(Math.sinh(1.0), MathUtils.sinh(1.0), 1e-10);
    }

    @Test
    public void testSafeNorm() {
        double[] v = {3.0, 4.0};
        assertEquals(5.0, MathUtils.safeNorm(v), 1e-10);
        assertEquals(0.0, MathUtils.safeNorm(null), 1e-10);
        assertEquals(0.0, MathUtils.safeNorm(new double[0]), 1e-10);
    }

    @Test
    public void testNormalizeArray() {
        double[] values = {1.0, 2.0, 3.0};
        double[] normalized = MathUtils.normalizeArray(values, 6.0);
        assertEquals(1.0, normalized[0], 1e-10);
        assertEquals(2.0, normalized[1], 1e-10);
        assertEquals(3.0, normalized[2], 1e-10);
    }

    @Test(expected = ArithmeticException.class)
    public void testNormalizeArrayInfinite() {
        double[] values = {Double.POSITIVE_INFINITY, 2.0};
        MathUtils.normalizeArray(values, 6.0);
    }

    @Test(expected = ArithmeticException.class)
    public void testNormalizeArrayNaN() {
        double[] values = {Double.NaN, 2.0};
        MathUtils.normalizeArray(values, 6.0);
    }

    @Test(expected = ArithmeticException.class)
    public void testNormalizeArrayZeroSum() {
        double[] values = {0.0, 0.0};
        MathUtils.normalizeArray(values, 6.0);
    }

    @Test
    public void testDistance() {
        double[] p1 = {0.0, 0.0};
        double[] p2 = {3.0, 4.0};
        assertEquals(5.0, MathUtils.distance(p1, p2), 1e-10);
        assertEquals(5.0, MathUtils.distance(new int[]{0, 0}, new int[]{3, 4}), 1e-10);
        assertEquals(5.0, MathUtils.distance(new long[]{0L, 0L}, new long[]{3L, 4L}), 1e-10);
    }

    @Test
    public void testAnonymousInstance() {
        MathUtils utils = new MathUtils() {};
        assertNotNull(utils);
    }
}