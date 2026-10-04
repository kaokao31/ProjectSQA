package org.apache.commons.math.util;

import org.junit.Test;
import static org.junit.Assert.*;

public class MathUtilsTest {

    @Test
    public void testDistance1_IntArray() {
        int[] p1 = {1, 2, 3};
        int[] p2 = {4, 6, 8};
        // |1-4| + |2-6| + |3-8| = 3 + 4 + 5 = 12
        assertEquals(12.0, MathUtils.distance1(p1, p2), 1e-12);
    }

    @Test
    public void testDistance1_DoubleArray() {
        double[] p1 = {1.0, 2.0};
        double[] p2 = {4.0, 6.0};
        // |1-4| + |2-6| = 3 + 4 = 7
        assertEquals(7.0, MathUtils.distance1(p1, p2), 1e-12);
    }

    @Test
    public void testDistance_IntArray() {
        int[] p1 = {0, 0};
        int[] p2 = {3, 4};
        // sqrt((0-3)^2 + (0-4)^2) = sqrt(9 + 16) = 5
        assertEquals(5.0, MathUtils.distance(p1, p2), 1e-12);
    }

    @Test
    public void testDistance_DoubleArray() {
        double[] p1 = {0.0, 0.0};
        double[] p2 = {3.0, 4.0};
        assertEquals(5.0, MathUtils.distance(p1, p2), 1e-12);
    }

    @Test
    public void testDistanceInf_IntArray() {
        int[] p1 = {1, 5, 2};
        int[] p2 = {4, 2, 8};
        // max(|1-4|, |5-2|, |2-8|) = max(3, 3, 6) = 6
        assertEquals(6.0, MathUtils.distanceInf(p1, p2), 1e-12);
    }

    @Test
    public void testDistanceInf_DoubleArray() {
        double[] p1 = {1.0, 5.0, 2.0};
        double[] p2 = {4.0, 2.0, 8.0};
        assertEquals(6.0, MathUtils.distanceInf(p1, p2), 1e-12);
    }

    @Test
    public void testAddAndCheck_Int() {
        assertEquals(5, MathUtils.addAndCheck(2, 3));
    }

    @Test(expected = ArithmeticException.class)
    public void testAddAndCheck_IntOverflow() {
        MathUtils.addAndCheck(Integer.MAX_VALUE, 1);
    }

    @Test(expected = ArithmeticException.class)
    public void testAddAndCheck_IntUnderflow() {
        MathUtils.addAndCheck(Integer.MIN_VALUE, -1);
    }

    @Test
    public void testAddAndCheck_Long() {
        assertEquals(5L, MathUtils.addAndCheck(2L, 3L));
    }

    @Test(expected = ArithmeticException.class)
    public void testAddAndCheck_LongOverflow() {
        MathUtils.addAndCheck(Long.MAX_VALUE, 1L);
    }

    @Test(expected = ArithmeticException.class)
    public void testAddAndCheck_LongUnderflow() {
        MathUtils.addAndCheck(Long.MIN_VALUE, -1L);
    }

    @Test
    public void testBinomialCoefficient() {
        assertEquals(1L, MathUtils.binomialCoefficient(5, 0));
        assertEquals(5L, MathUtils.binomialCoefficient(5, 1));
        assertEquals(10L, MathUtils.binomialCoefficient(5, 2));
        assertEquals(10L, MathUtils.binomialCoefficient(5, 3));
        assertEquals(5L, MathUtils.binomialCoefficient(5, 4));
        assertEquals(1L, MathUtils.binomialCoefficient(5, 5));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBinomialCoefficient_NegativeN() {
        MathUtils.binomialCoefficient(-1, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBinomialCoefficient_KGreaterThanN() {
        MathUtils.binomialCoefficient(3, 4);
    }

    @Test
    public void testBinomialCoefficientDouble() {
        assertEquals(10.0, MathUtils.binomialCoefficientDouble(5, 2), 1e-12);
    }

    @Test
    public void testBinomialCoefficientLog() {
        assertEquals(Math.log(10.0), MathUtils.binomialCoefficientLog(5, 2), 1e-12);
    }

    @Test
    public void testCheckNotNull() {
        String test = "notNull";
        MathUtils.checkNotNull(test);
        assertTrue(true); // If no exception thrown
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCheckNotNull_Null() {
        MathUtils.checkNotNull(null);
    }

    @Test
    public void testCompareTo() {
        double[] x = {1.0, 2.0};
        double[] y = {1.0, 2.0};
        double[] z = {1.0, 3.0};
        assertTrue(MathUtils.equals(x, y));
        assertFalse(MathUtils.equals(x, z));
    }

    @Test
    public void testCosh() {
        assertEquals(1.0, MathUtils.cosh(0.0), 1e-12);
    }

    @Test
    public void testSinh() {
        assertEquals(0.0, MathUtils.sinh(0.0), 1e-12);
    }

    @Test
    public void testTanh() {
        assertEquals(0.0, MathUtils.tanh(0.0), 1e-12);
    }

    @Test
    public void testIndicator_Byte() {
        assertEquals((byte) 1, MathUtils.indicator((byte) 5));
        assertEquals((byte) -1, MathUtils.indicator((byte) -5));
        assertEquals((byte) 0, MathUtils.indicator((byte) 0));
    }

    @Test
    public void testIndicator_Double() {
        assertEquals(1.0, MathUtils.indicator(5.0), 1e-12);
        assertEquals(-1.0, MathUtils.indicator(-5.0), 1e-12);
        assertEquals(0.0, MathUtils.indicator(0.0), 1e-12);
        assertTrue(Double.isNaN(MathUtils.indicator(Double.NaN)));
    }

    @Test
    public void testIndicator_Float() {
        assertEquals(1.0f, MathUtils.indicator(5.0f), 1e-12);
        assertEquals(-1.0f, MathUtils.indicator(-5.0f), 1e-12);
        assertEquals(0.0f, MathUtils.indicator(0.0f), 1e-12);
        assertTrue(Float.isNaN(MathUtils.indicator(Float.NaN)));
    }

    @Test
    public void testIndicator_Int() {
        assertEquals(1, MathUtils.indicator(5));
        assertEquals(-1, MathUtils.indicator(-5));
        assertEquals(0, MathUtils.indicator(0));
    }

    @Test
    public void testIndicator_Long() {
        assertEquals(1L, MathUtils.indicator(5L));
        assertEquals(-1L, MathUtils.indicator(-5L));
        assertEquals(0L, MathUtils.indicator(0L));
    }

    @Test
    public void testIndicator_Short() {
        assertEquals((short) 1, MathUtils.indicator((short) 5));
        assertEquals((short) -1, MathUtils.indicator((short) -5));
        assertEquals((short) 0, MathUtils.indicator((short) 0));
    }

    @Test
    public void testLog() {
        assertEquals(Math.log(2.0), MathUtils.log(2.0, Math.E), 1e-12);
    }

    @Test
    public void testMulAndCheck_Int() {
        assertEquals(6, MathUtils.mulAndCheck(2, 3));
    }

    @Test(expected = ArithmeticException.class)
    public void testMulAndCheck_IntOverflow() {
        MathUtils.mulAndCheck(Integer.MAX_VALUE, 2);
    }

    @Test
    public void testMulAndCheck_Long() {
        assertEquals(6L, MathUtils.mulAndCheck(2L, 3L));
    }

    @Test(expected = ArithmeticException.class)
    public void testMulAndCheck_LongOverflow() {
        MathUtils.mulAndCheck(Long.MAX_VALUE, 2L);
    }

    @Test
    public void testNormalizeAngle() {
        double normalized = MathUtils.normalizeAngle(Math.PI * 3, 0);
        assertTrue(normalized >= -Math.PI && normalized <= Math.PI);
    }

    @Test
    public void testRound() {
        assertEquals(1.23, MathUtils.round(1.2345, 2), 1e-3);
    }

    @Test
    public void testSign() {
        assertEquals(1.0, MathUtils.sign(5.0), 1e-12);
        assertEquals(-1.0, MathUtils.sign(-5.0), 1e-12);
        assertEquals(0.0, MathUtils.sign(0.0), 1e-12);
        assertTrue(Double.isNaN(MathUtils.sign(Double.NaN)));
    }

    @Test
    public void testSubAndCheck_Int() {
        assertEquals(2, MathUtils.subAndCheck(5, 3));
    }

    @Test(expected = ArithmeticException.class)
    public void testSubAndCheck_IntOverflow() {
        MathUtils.subAndCheck(Integer.MIN_VALUE, 1);
    }

    @Test
    public void testSubAndCheck_Long() {
        assertEquals(2L, MathUtils.subAndCheck(5L, 3L));
    }

    @Test(expected = ArithmeticException.class)
    public void testSubAndCheck_LongOverflow() {
        MathUtils.subAndCheck(Long.MIN_VALUE, 1L);
    }

    @Test
    public void testFactorial() {
        assertEquals(120L, MathUtils.factorial(5));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFactorial_Negative() {
        MathUtils.factorial(-1);
    }

    @Test
    public void testFactorialDouble() {
        assertEquals(120.0, MathUtils.factorialDouble(5), 1e-12);
    }

    @Test
    public void testFactorialLog() {
        assertEquals(Math.log(120.0), MathUtils.factorialLog(5), 1e-12);
    }

    @Test
    public void testGcd() {
        assertEquals(4, MathUtils.gcd(24, 36));
        assertEquals(4, MathUtils.gcd(-24, 36));
        assertEquals(0, MathUtils.gcd(0, 0));
    }

    @Test
    public void testLcm() {
        assertEquals(72, MathUtils.lcm(24, 36));
        assertEquals(0, MathUtils.lcm(0, 0));
    }

    @Test
    public void testHash() {
        assertEquals(Double.valueOf(0.0).hashCode(), MathUtils.hash(0.0));
        double[] arr = {1.0, 2.0};
        assertNotEquals(0, MathUtils.hash(arr));
        assertNull(MathUtils.hash((double[]) null));
    }

    @Test
    public void testEqualsDoubleArray() {
        assertTrue(MathUtils.equals(new double[]{1.0, 2.0}, new double[]{1.0, 2.0}));
        assertFalse(MathUtils.equals(new double[]{1.0, 2.0}, new double[]{1.0, 3.0}));
        assertFalse(MathUtils.equals(new double[]{1.0}, null));
        assertFalse(MathUtils.equals(null, new double[]{1.0}));
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
        assertTrue(MathUtils.equals(new double[]{Double.NaN}, new double[]{Double.NaN}));
    }

    @Test
    public void testEqualsWithEpsilon() {
        assertTrue(MathUtils.equals(1.001, 1.002, 0.01));
        assertFalse(MathUtils.equals(1.0, 2.0, 0.01));
        assertTrue(MathUtils.equals(Double.NaN, Double.NaN, 0.01));
        assertFalse(MathUtils.equals(Double.NaN, 1.0, 0.01));
        assertFalse(MathUtils.equals(1.0, Double.NaN, 0.01));
    }

    @Test
    public void testEqualsIncludingNaN() {
        assertTrue(MathUtils.equalsIncludingNaN(Double.NaN, Double.NaN));
        assertFalse(MathUtils.equalsIncludingNaN(Double.NaN, 1.0));
        assertFalse(MathUtils.equalsIncludingNaN(1.0, Double.NaN));
        assertTrue(MathUtils.equalsIncludingNaN(1.0, 1.0));
    }

    @Test
    public void testEqualsArrayIncludingNaN() {
        assertTrue(MathUtils.equalsIncludingNaN(new double[]{Double.NaN}, new double[]{Double.NaN}));
        assertFalse(MathUtils.equalsIncludingNaN(new double[]{Double.NaN}, new double[]{1.0}));
        assertFalse(MathUtils.equalsIncludingNaN(new double[]{1.0}, null));
        assertFalse(MathUtils.equalsIncludingNaN(null, new double[]{1.0}));
        assertTrue(MathUtils.equalsIncludingNaN((double[]) null, (double[]) null));
    }
}