package org.apache.commons.math.util;

import org.junit.Test;
import static org.junit.Assert.*;

public class MathUtilsTest {

    @Test
    public void testEqualsDoubleDouble() {
        assertTrue(MathUtils.equals(1.0, 1.0));
        assertFalse(MathUtils.equals(1.0, 2.0));
        assertTrue(MathUtils.equals(Double.NaN, Double.NaN));
        assertFalse(MathUtils.equals(Double.NaN, 1.0));
        assertFalse(MathUtils.equals(1.0, Double.NaN));
        assertTrue(MathUtils.equals(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY));
        assertFalse(MathUtils.equals(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY));
    }

    @Test
    public void testEqualsDoubleArrayDoubleArray() {
        assertTrue(MathUtils.equals(new double[] { 1.0, 2.0 }, new double[] { 1.0, 2.0 }));
        assertNull(MathUtils.equals((double[]) null, (double[]) null)); // Depending on implementation, checking null behavior
        assertFalse(MathUtils.equals(new double[] { 1.0 }, null));
        assertFalse(MathUtils.equals(null, new double[] { 1.0 }));
        assertFalse(MathUtils.equals(new double[] { 1.0 }, new double[] { 1.0, 2.0 }));
        assertFalse(MathUtils.equals(new double[] { 1.0, 2.0 }, new double[] { 1.0, 3.0 }));
        assertTrue(MathUtils.equals(new double[] { Double.NaN }, new double[] { Double.NaN }));
    }

    @Test
    public void testEqualsWithEpsilon() {
        assertTrue(MathUtils.equals(1.0, 1.0001, 0.001));
        assertFalse(MathUtils.equals(1.0, 1.01, 0.001));
        assertTrue(MathUtils.equals(Double.NaN, Double.NaN, 0.001));
    }

    @Test
    public void testAddAndSubtract() {
        assertEquals(5, MathUtils.addAndCheck(2, 3));
        assertEquals(-1, MathUtils.subAndCheck(2, 3));
    }

    @Test(expected = ArithmeticException.class)
    public void testAddAndCheckOverflow() {
        MathUtils.addAndCheck(Integer.MAX_VALUE, 1);
    }

    @Test(expected = ArithmeticException.class)
    public void testAddAndCheckUnderflow() {
        MathUtils.addAndCheck(Integer.MIN_VALUE, -1);
    }

    @Test(expected = ArithmeticException.class)
    public void testSubAndCheckOverflow() {
        MathUtils.subAndCheck(Integer.MAX_VALUE, -1);
    }

    @Test(expected = ArithmeticException.class)
    public void testSubAndCheckUnderflow() {
        MathUtils.subAndCheck(Integer.MIN_VALUE, 1);
    }

    @Test
    public void testMulAndCheck() {
        assertEquals(6, MathUtils.mulAndCheck(2, 3));
    }

    @Test(expected = ArithmeticException.class)
    public void testMulAndCheckOverflow() {
        MathUtils.mulAndCheck(Integer.MAX_VALUE, 2);
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
    public void testBinomial() {
        assertEquals(1L, MathUtils.binomialCoefficient(5, 0));
        assertEquals(5L, MathUtils.binomialCoefficient(5, 1));
        assertEquals(10L, MathUtils.binomialCoefficient(5, 2));
        assertEquals(1L, MathUtils.binomialCoefficient(5, 5));
    }

    @Test
    public void testSign() {
        assertEquals(1, MathUtils.sign(5.0), 0);
        assertEquals(-1, MathUtils.sign(-5.0), 0);
        assertEquals(0, MathUtils.sign(0.0), 0);
        assertEquals(0, MathUtils.sign(Double.NaN), 0);

        assertEquals(1, MathUtils.sign(5L));
        assertEquals(-1, MathUtils.sign(-5L));
        assertEquals(0, MathUtils.sign(0L));

        assertEquals(1, MathUtils.sign(5));
        assertEquals(-1, MathUtils.sign(-5));
        assertEquals(0, MathUtils.sign(0));
    }

    @Test
    public void testHash() {
        assertEquals(1072693248, MathUtils.hash(1.0));
        int[] arr = {1, 2, 3};
        assertTrue(MathUtils.hash(arr) != 0);
        assertTrue(MathUtils.hash(null) == 0);
    }

    @Test
    public void testNormalizeAngle() {
        assertEquals(0.0, MathUtils.normalizeAngle(0.0, 0.0), 1e-12);
        assertEquals(Math.PI, MathUtils.normalizeAngle(3 * Math.PI, 0.0), 1e-12);
    }

    @Test
    public void testReduce() {
        assertEquals(1.0, MathUtils.reduce(5.0, 2.0, 0.0), 1e-12);
    }
}