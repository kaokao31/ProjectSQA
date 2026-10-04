package org.apache.commons.math.util;

import org.junit.Test;
import static org.junit.Assert.*;

public class MathUtilsTest {

    @Test
    public void testAddAndCheckLong() {
        assertEquals(5L, MathUtils.addAndCheck(2L, 3L));
        assertEquals(Long.MAX_VALUE, MathUtils.addAndCheck(Long.MAX_VALUE, 0L));
        assertEquals(Long.MIN_VALUE, MathUtils.addAndCheck(Long.MIN_VALUE, 0L));
    }

    @Test(expected = ArithmeticException.class)
    public void testAddAndCheckLongOverflowPositive() {
        MathUtils.addAndCheck(Long.MAX_VALUE, 1L);
    }

    @Test(expected = ArithmeticException.class)
    public void testAddAndCheckLongOverflowNegative() {
        MathUtils.addAndCheck(Long.MIN_VALUE, -1L);
    }

    @Test
    public void testAddAndCheckInt() {
        assertEquals(5, MathUtils.addAndCheck(2, 3));
        assertEquals(Integer.MAX_VALUE, MathUtils.addAndCheck(Integer.MAX_VALUE, 0));
        assertEquals(Integer.MIN_VALUE, MathUtils.addAndCheck(Integer.MIN_VALUE, 0));
    }

    @Test(expected = ArithmeticException.class)
    public void testAddAndCheckIntOverflowPositive() {
        MathUtils.addAndCheck(Integer.MAX_VALUE, 1);
    }

    @Test(expected = ArithmeticException.class)
    public void testAddAndCheckIntOverflowNegative() {
        MathUtils.addAndCheck(Integer.MIN_VALUE, -1);
    }

    @Test
    public void testBinomialCoefficient() {
        assertEquals(1L, MathUtils.binomialCoefficient(5, 0));
        assertEquals(5L, MathUtils.binomialCoefficient(5, 1));
        assertEquals(10L, MathUtils.binomialCoefficient(5, 2));
        assertEquals(10L, MathUtils.binomialCoefficient(5, 3));
        assertEquals(5L, MathUtils.binomialCoefficient(5, 4));
        assertEquals(1L, MathUtils.binomialCoefficient(5, 5));
        assertEquals(1L, MathUtils.binomialCoefficient(0, 0));
        assertEquals(1L, MathUtils.binomialCoefficient(66, 0));
        assertEquals(66L, MathUtils.binomialCoefficient(66, 1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBinomialCoefficientNLessThanK() {
        MathUtils.binomialCoefficient(3, 5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBinomialCoefficientNegativeN() {
        MathUtils.binomialCoefficient(-1, 0);
    }

    @Test
    public void testBinomialCoefficientDouble() {
        assertEquals(1.0, MathUtils.binomialCoefficientDouble(5, 0), 1e-10);
        assertEquals(10.0, MathUtils.binomialCoefficientDouble(5, 2), 1e-10);
        assertEquals(1.0, MathUtils.binomialCoefficientDouble(0, 0), 1e-10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBinomialCoefficientDoubleNLessThanK() {
        MathUtils.binomialCoefficientDouble(3, 5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBinomialCoefficientDoubleNegativeN() {
        MathUtils.binomialCoefficientDouble(-1, 0);
    }

    @Test
    public void testBinomialCoefficientLog() {
        assertEquals(Math.log(10.0), MathUtils.binomialCoefficientLog(5, 2), 1e-10);
        assertEquals(0.0, MathUtils.binomialCoefficientLog(5, 0), 1e-10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBinomialCoefficientLogNLessThanK() {
        MathUtils.binomialCoefficientLog(3, 5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBinomialCoefficientLogNegativeN() {
        MathUtils.binomialCoefficientLog(-1, 0);
    }

    @Test
    public void testCompareTo() {
        assertEquals(0, MathUtils.compareTo(1.0, 1.0, 1e-5));
        assertTrue(MathUtils.compareTo(1.0, 2.0, 1e-5) < 0);
        assertTrue(MathUtils.compareTo(2.0, 1.0, 1e-5) > 0);
        assertEquals(0, MathUtils.compareTo(Double.NaN, Double.NaN, 1e-5));
        assertTrue(MathUtils.compareTo(Double.NaN, 1.0, 1e-5) > 0);
        assertTrue(MathUtils.compareTo(1.0, Double.NaN, 1e-5) < 0);
    }

    @Test
    public void testCosh() {
        assertEquals(1.0, MathUtils.cosh(0.0), 1e-10);
        assertTrue(Double.isInfinite(MathUtils.cosh(Double.POSITIVE_INFINITY)));
        assertTrue(Double.isInfinite(MathUtils.cosh(Double.NEGATIVE_INFINITY)));
        assertTrue(Double.isNaN(MathUtils.cosh(Double.NaN)));
    }

    @Test
    public void testEqualsDoubleArray() {
        assertTrue(MathUtils.equals(null, null));
        assertFalse(MathUtils.equals(new double[]{1.0}, null));
        assertFalse(MathUtils.equals(null, new double[]{1.0}));
        assertTrue(MathUtils.equals(new double[]{1.0, 2.0}, new double[]{1.0, 2.0}));
        assertFalse(MathUtils.equals(new double[]{1.0, 2.0}, new double[]{1.0, 3.0}));
        assertFalse(MathUtils.equals(new double[]{1.0, 2.0}, new double[]{1.0}));
        assertTrue(MathUtils.equals(new double[]{Double.NaN}, new double[]{Double.NaN}));
    }

    @Test
    public void testEqualsDoubleScalar() {
        assertTrue(MathUtils.equals(1.0, 1.0));
        assertTrue(MathUtils.equals(Double.NaN, Double.NaN));
        assertFalse(MathUtils.equals(1.0, 2.0));
        assertFalse(MathUtils.equals(1.0, Double.NaN));
        assertFalse(MathUtils.equals(Double.NaN, 1.0));
    }

    @Test
    public void testEqualsWithDelta() {
        assertTrue(MathUtils.equals(1.0, 1.05, 0.1));
        assertFalse(MathUtils.equals(1.0, 1.2, 0.1));
        assertTrue(MathUtils.equals(Double.NaN, Double.NaN, 0.1));
        assertFalse(MathUtils.equals(Double.NaN, 1.0, 0.1));
    }

    @Test
    public void testFactorial() {
        assertEquals(1L, MathUtils.factorial(0));
        assertEquals(1L, MathUtils.factorial(1));
        assertEquals(120L, MathUtils.factorial(5));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFactorialNegative() {
        MathUtils.factorial(-1);
    }

    @Test
    public void testFactorialDouble() {
        assertEquals(1.0, MathUtils.factorialDouble(0), 1e-10);
        assertEquals(120.0, MathUtils.factorialDouble(5), 1e-10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFactorialDoubleNegative() {
        MathUtils.factorialDouble(-1);
    }

    @Test
    public void testFactorialLog() {
        assertEquals(0.0, MathUtils.factorialLog(0), 1e-10);
        assertEquals(Math.log(120.0), MathUtils.factorialLog(5), 1e-10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFactorialLogNegative() {
        MathUtils.factorialLog(-1);
    }

    @Test
    public void testGcd() {
        assertEquals(2, MathUtils.gcd(2, 4));
        assertEquals(2, MathUtils.gcd(-2, 4));
        assertEquals(2, MathUtils.gcd(2, -4));
        assertEquals(0, MathUtils.gcd(0, 0));
        assertEquals(5, MathUtils.gcd(5, 0));
        assertEquals(5, MathUtils.gcd(0, 5));
        assertEquals(Integer.MAX_VALUE, MathUtils.gcd(Integer.MIN_VALUE, 0)); // depending on impl
    }

    @Test
    public void testHash() {
        assertEquals(Double.valueOf(1.0).hashCode(), MathUtils.hash(1.0));
        assertEquals(0, MathUtils.hash(Double.NaN));
        assertEquals(0, MathUtils.hash(null));
        assertEquals(MathUtils.hash(new double[]{1.0, 2.0}), MathUtils.hash(new double[]{1.0, 2.0}));
    }

    @Test
    public void testIndicator() {
        assertEquals(1.0, MathUtils.indicator(5.0), 1e-10);
        assertEquals(-1.0, MathUtils.indicator(-5.0), 1e-10);
        assertEquals(1.0, MathUtils.indicator(0.0), 1e-10);
        assertTrue(Double.isNaN(MathUtils.indicator(Double.NaN)));

        assertEquals(1.47, MathUtils.indicator(1.47), 1e-10); // Wait, float/byte/int/long/short overloads
        assertEquals(1, MathUtils.indicator(5));
        assertEquals(-1, MathUtils.indicator(-5));
        assertEquals(1, MathUtils.indicator(0));

        assertEquals(1L, MathUtils.indicator(5L));
        assertEquals(-1L, MathUtils.indicator(-5L));
        assertEquals(1L, MathUtils.indicator(0L));

        assertEquals(1.0f, MathUtils.indicator(5.0f), 1e-10f);
        assertEquals(-1.0f, MathUtils.indicator(-5.0f), 1e-10f);
        assertEquals(1.0f, MathUtils.indicator(0.0f), 1e-10f);

        assertEquals((byte) 1, MathUtils.indicator((byte) 5));
        assertEquals((byte) -1, MathUtils.indicator((byte) -5));
        assertEquals((byte) 1, MathUtils.indicator((byte) 0));

        assertEquals((short) 1, MathUtils.indicator((short) 5));
        assertEquals((short) -1, MathUtils.indicator((short) -5));
        assertEquals((short) 1, MathUtils.indicator((short) 0));
    }

    @Test
    public void testLcm() {
        assertEquals(4, MathUtils.lcm(2, 4));
        assertEquals(0, MathUtils.lcm(0, 4));
        assertEquals(0, MathUtils.lcm(2, 0));
        assertEquals(2, MathUtils.lcm(-2, 2));
    }

    @Test(expected = ArithmeticException.class)
    public void testLcmOverflow() {
        MathUtils.lcm(Integer.MAX_VALUE, Integer.MAX_VALUE - 1);
    }

    @Test
    public void testLog() {
        assertEquals(Math.log(2.0), MathUtils.log(Math.E, 2.0), 1e-10);
    }

    @Test
    public void testMulAndCheck() {
        assertEquals(6L, MathUtils.mulAndCheck(2L, 3L));
        assertEquals(6, MathUtils.mulAndCheck(2, 3));
    }

    @Test(expected = ArithmeticException.class)
    public void testMulAndCheckLongOverflow() {
        MathUtils.mulAndCheck(Long.MAX_VALUE, 2L);
    }

    @Test(expected = ArithmeticException.class)
    public void testMulAndCheckIntOverflow() {
        MathUtils.mulAndCheck(Integer.MAX_VALUE, 2);
    }

    @Test
    public void testNextAfter() {
        assertTrue(MathUtils.nextAfter(1.0, 2.0) > 1.0);
        assertTrue(MathUtils.nextAfter(2.0, 1.0) < 2.0);
        assertEquals(1.0, MathUtils.nextAfter(1.0, 1.0), 1e-10);
    }

    @Test
    public void testNormalizeAngle() {
        assertEquals(0.0, MathUtils.normalizeAngle(0.0, 0.0), 1e-10);
        assertEquals(Math.PI, MathUtils.normalizeAngle(3 * Math.PI, 0.0), 1e-10);
    }

    @Test
    public void testPow() {
        assertEquals(8L, MathUtils.pow(2L, 3));
        assertEquals(1L, MathUtils.pow(2L, 0));
        assertEquals(0L, MathUtils.pow(0L, 3));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPowNegativeExponent() {
        MathUtils.pow(2L, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPowZeroBaseNegativeExponent() {
        MathUtils.pow(0L, -1);
    }

    @Test
    public void testRound() {
        assertEquals(1.24, MathUtils.round(1.235, 2), 1e-10);
        assertEquals(1.24f, MathUtils.round(1.235f, 2), 1e-10f);
    }

    @Test
    public void testSign() {
        assertEquals(1.0, MathUtils.sign(5.0), 1e-10);
        assertEquals(-1.0, MathUtils.sign(-5.0), 1e-10);
        assertEquals(0.0, MathUtils.sign(0.0), 1e-10);
        assertTrue(Double.isNaN(MathUtils.sign(Double.NaN)));

        assertEquals(1, MathUtils.sign(5));
        assertEquals(-1, MathUtils.sign(-5));
        assertEquals(0, MathUtils.sign(0));

        assertEquals(1L, MathUtils.sign(5L));
        assertEquals(-1L, MathUtils.sign(-5L));
        assertEquals(0L, MathUtils.sign(0L));

        assertEquals(1.0f, MathUtils.sign(5.0f), 1e-10f);
        assertEquals(-1.0f, MathUtils.sign(-5.0f), 1e-10f);
        assertEquals(0.0f, MathUtils.sign(0.0f), 1e-10f);

        assertEquals((byte) 1, MathUtils.sign((byte) 5));
        assertEquals((byte) -1, MathUtils.sign((byte) -5));
        assertEquals((byte) 0, MathUtils.sign((byte) 0));

        assertEquals((short) 1, MathUtils.sign((short) 5));
        assertEquals((short) -1, MathUtils.sign((short) -5));
        assertEquals((short) 0, MathUtils.sign((short) 0));
    }

    @Test
    public void testSinh() {
        assertEquals(0.0, MathUtils.sinh(0.0), 1e-10);
        assertTrue(Double.isInfinite(MathUtils.sinh(Double.POSITIVE_INFINITY)));
        assertTrue(Double.isNaN(MathUtils.sinh(Double.NaN)));
    }

    @Test
    public void testSubAndCheck() {
        assertEquals(1L, MathUtils.subAndCheck(3L, 2L));
        assertEquals(1, MathUtils.subAndCheck(3, 2));
    }

    @Test(expected = ArithmeticException.class)
    public void testSubAndCheckLongOverflow() {
        MathUtils.subAndCheck(Long.MIN_VALUE, 1L);
    }

    @Test(expected = ArithmeticException.class)
    public void testSubAndCheckIntOverflow() {
        MathUtils.subAndCheck(Integer.MIN_VALUE, 1);
    }

    @Test
    public void testSymmetrize() {
        assertEquals(1.0, MathUtils.normalizeAngle(1.0, 1.0), 1e-10);
    }

    @Test
    public void testCheckNotNull() {
        Double[] array = new Double[]{1.0};
        MathUtils.checkNotNull(array);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCheckNotNullNull() {
        MathUtils.checkNotNull(null);
    }
}