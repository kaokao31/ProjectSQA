package org.apache.commons.math.util;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Test suite for MathUtils class (Commons Math 2.2) targeting bug #93.
 * Achieves high line and branch coverage, including edge cases and fault detection.
 */
public class MathUtilsTest {

    // ---------- factorial(int) ----------
    @Test
    public void testFactorialZero() {
        assertEquals(1.0, MathUtils.factorial(0), 1e-15);
    }

    @Test
    public void testFactorialOne() {
        assertEquals(1.0, MathUtils.factorial(1), 1e-15);
    }

    @Test
    public void testFactorialSmall() {
        assertEquals(2.0, MathUtils.factorial(2), 1e-15);
        assertEquals(6.0, MathUtils.factorial(3), 1e-15);
        assertEquals(24.0, MathUtils.factorial(4), 1e-15);
        assertEquals(120.0, MathUtils.factorial(5), 1e-15);
    }

    @Test
    public void testFactorialLarge() {
        // 20! = 2.43290200817664E18
        assertEquals(2.43290200817664E18, MathUtils.factorial(20), 1e10);
        // 170! is near Double.MAX_VALUE
        assertTrue(Double.isFinite(MathUtils.factorial(170)));
        // 171! overflows to Infinity
        assertEquals(Double.POSITIVE_INFINITY, MathUtils.factorial(171), 0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFactorialNegative() {
        // Bug #93: factorial should throw IllegalArgumentException for negative n
        MathUtils.factorial(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFactorialNegativeLarge() {
        MathUtils.factorial(-100);
    }

    // ---------- factorialDouble(int) ----------
    @Test
    public void testFactorialDoubleZero() {
        assertEquals(1.0, MathUtils.factorialDouble(0), 1e-15);
    }

    @Test
    public void testFactorialDoubleOne() {
        assertEquals(1.0, MathUtils.factorialDouble(1), 1e-15);
    }

    @Test
    public void testFactorialDoubleSmall() {
        assertEquals(6.0, MathUtils.factorialDouble(3), 1e-15);
    }

    @Test
    public void testFactorialDoubleLarge() {
        assertTrue(Double.isFinite(MathUtils.factorialDouble(170)));
        assertEquals(Double.POSITIVE_INFINITY, MathUtils.factorialDouble(171), 0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFactorialDoubleNegative() {
        MathUtils.factorialDouble(-1);
    }

    // ---------- factorialLog(int) ----------
    @Test
    public void testFactorialLogZero() {
        assertEquals(0.0, MathUtils.factorialLog(0), 1e-15);
    }

    @Test
    public void testFactorialLogOne() {
        assertEquals(0.0, MathUtils.factorialLog(1), 1e-15);
    }

    @Test
    public void testFactorialLogSmall() {
        assertEquals(Math.log(6.0), MathUtils.factorialLog(3), 1e-15);
    }

    @Test
    public void testFactorialLogLarge() {
        assertTrue(MathUtils.factorialLog(1000) > 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFactorialLogNegative() {
        MathUtils.factorialLog(-1);
    }

    // ---------- binomialCoefficient(int, int) ----------
    @Test
    public void testBinomialCoefficientZeroK() {
        assertEquals(1, MathUtils.binomialCoefficient(5, 0));
    }

    @Test
    public void testBinomialCoefficientEqual() {
        assertEquals(1, MathUtils.binomialCoefficient(5, 5));
    }

    @Test
    public void testBinomialCoefficientSmall() {
        assertEquals(10, MathUtils.binomialCoefficient(5, 2));
        assertEquals(10, MathUtils.binomialCoefficient(5, 3));
    }

    @Test
    public void testBinomialCoefficientLarge() {
        // 30 choose 15 = 155117520
        assertEquals(155117520L, MathUtils.binomialCoefficient(30, 15));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBinomialCoefficientNegativeN() {
        MathUtils.binomialCoefficient(-1, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBinomialCoefficientNegativeK() {
        MathUtils.binomialCoefficient(5, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBinomialCoefficientKGreaterThanN() {
        MathUtils.binomialCoefficient(3, 5);
    }

    @Test(expected = ArithmeticException.class)
    public void testBinomialCoefficientOverflow() {
        // 67 choose 30 overflows long
        MathUtils.binomialCoefficient(67, 30);
    }

    // ---------- binomialCoefficientDouble(int, int) ----------
    @Test
    public void testBinomialCoefficientDoubleZeroK() {
        assertEquals(1.0, MathUtils.binomialCoefficientDouble(5, 0), 1e-15);
    }

    @Test
    public void testBinomialCoefficientDoubleEqual() {
        assertEquals(1.0, MathUtils.binomialCoefficientDouble(5, 5), 1e-15);
    }

    @Test
    public void testBinomialCoefficientDoubleSmall() {
        assertEquals(10.0, MathUtils.binomialCoefficientDouble(5, 2), 1e-15);
    }

    @Test
    public void testBinomialCoefficientDoubleLarge() {
        // 1000 choose 500 is huge but finite
        assertTrue(Double.isFinite(MathUtils.binomialCoefficientDouble(1000, 500)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBinomialCoefficientDoubleNegativeN() {
        MathUtils.binomialCoefficientDouble(-1, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBinomialCoefficientDoubleNegativeK() {
        MathUtils.binomialCoefficientDouble(5, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBinomialCoefficientDoubleKGreaterThanN() {
        MathUtils.binomialCoefficientDouble(3, 5);
    }

    // ---------- binomialCoefficientLog(int, int) ----------
    @Test
    public void testBinomialCoefficientLogZeroK() {
        assertEquals(0.0, MathUtils.binomialCoefficientLog(5, 0), 1e-15);
    }

    @Test
    public void testBinomialCoefficientLogEqual() {
        assertEquals(0.0, MathUtils.binomialCoefficientLog(5, 5), 1e-15);
    }

    @Test
    public void testBinomialCoefficientLogSmall() {
        assertEquals(Math.log(10.0), MathUtils.binomialCoefficientLog(5, 2), 1e-15);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBinomialCoefficientLogNegativeN() {
        MathUtils.binomialCoefficientLog(-1, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBinomialCoefficientLogNegativeK() {
        MathUtils.binomialCoefficientLog(5, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBinomialCoefficientLogKGreaterThanN() {
        MathUtils.binomialCoefficientLog(3, 5);
    }

    // ---------- gcd(int, int) ----------
    @Test
    public void testGcdZero() {
        assertEquals(5, MathUtils.gcd(0, 5));
        assertEquals(5, MathUtils.gcd(5, 0));
        assertEquals(0, MathUtils.gcd(0, 0));
    }

    @Test
    public void testGcdPositive() {
        assertEquals(6, MathUtils.gcd(12, 18));
        assertEquals(1, MathUtils.gcd(17, 19));
    }

    @Test
    public void testGcdNegative() {
        assertEquals(6, MathUtils.gcd(-12, 18));
        assertEquals(6, MathUtils.gcd(12, -18));
        assertEquals(6, MathUtils.gcd(-12, -18));
    }

    @Test
    public void testGcdLarge() {
        // Integer.MIN_VALUE is tricky
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 1));
        assertEquals(1, MathUtils.gcd(1, Integer.MIN_VALUE));
        // gcd(2^31-1, 2^31-2) = 1
        assertEquals(1, MathUtils.gcd(Integer.MAX_VALUE, Integer.MAX_VALUE - 1));
    }

    // ---------- lcm(int, int) ----------
    @Test
    public void testLcmZero() {
        assertEquals(0, MathUtils.lcm(0, 5));
        assertEquals(0, MathUtils.lcm(5, 0));
    }

    @Test
    public void testLcmPositive() {
        assertEquals(36, MathUtils.lcm(12, 18));
        assertEquals(323, MathUtils.lcm(17, 19));
    }

    @Test
    public void testLcmNegative() {
        assertEquals(36, MathUtils.lcm(-12, 18));
        assertEquals(36, MathUtils.lcm(12, -18));
        assertEquals(36, MathUtils.lcm(-12, -18));
    }

    @Test(expected = ArithmeticException.class)
    public void testLcmOverflow() {
        // lcm(Integer.MAX_VALUE, Integer.MAX_VALUE) overflows
        MathUtils.lcm(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    // ---------- mulAndCheck(int, int) ----------
    @Test
    public void testMulAndCheckNormal() {
        assertEquals(6, MathUtils.mulAndCheck(2, 3));
        assertEquals(-6, MathUtils.mulAndCheck(-2, 3));
        assertEquals(0, MathUtils.mulAndCheck(0, 100));
    }

    @Test(expected = ArithmeticException.class)
    public void testMulAndCheckOverflowPositive() {
        MathUtils.mulAndCheck(Integer.MAX_VALUE, 2);
    }

    @Test(expected = ArithmeticException.class)
    public void testMulAndCheckOverflowNegative() {
        MathUtils.mulAndCheck(Integer.MIN_VALUE, 2);
    }

    @Test(expected = ArithmeticException.class)
    public void testMulAndCheckOverflowMinValue() {
        MathUtils.mulAndCheck(Integer.MIN_VALUE, -1);
    }

    // ---------- addAndCheck(int, int) ----------
    @Test
    public void testAddAndCheckNormal() {
        assertEquals(5, MathUtils.addAndCheck(2, 3));
        assertEquals(-1, MathUtils.addAndCheck(-2, 1));
        assertEquals(0, MathUtils.addAndCheck(0, 0));
    }

    @Test(expected = ArithmeticException.class)
    public void testAddAndCheckOverflowPositive() {
        MathUtils.addAndCheck(Integer.MAX_VALUE, 1);
    }

    @Test(expected = ArithmeticException.class)
    public void testAddAndCheckOverflowNegative() {
        MathUtils.addAndCheck(Integer.MIN_VALUE, -1);
    }

    // ---------- subAndCheck(int, int) ----------
    @Test
    public void testSubAndCheckNormal() {
        assertEquals(-1, MathUtils.subAndCheck(2, 3));
        assertEquals(5, MathUtils.subAndCheck(2, -3));
        assertEquals(0, MathUtils.subAndCheck(0, 0));
    }

    @Test(expected = ArithmeticException.class)
    public void testSubAndCheckOverflowPositive() {
        MathUtils.subAndCheck(Integer.MAX_VALUE, -1);
    }

    @Test(expected = ArithmeticException.class)
    public void testSubAndCheckOverflowNegative() {
        MathUtils.subAndCheck(Integer.MIN_VALUE, 1);
    }

    // ---------- mulAndCheck(long, long) ----------
    @Test
    public void testMulAndCheckLongNormal() {
        assertEquals(6L, MathUtils.mulAndCheck(2L, 3L));
        assertEquals(-6L, MathUtils.mulAndCheck(-2L, 3L));
        assertEquals(0L, MathUtils.mulAndCheck(0L, 100L));
    }

    @Test(expected = ArithmeticException.class)
    public void testMulAndCheckLongOverflowPositive() {
        MathUtils.mulAndCheck(Long.MAX_VALUE, 2L);
    }

    @Test(expected = ArithmeticException.class)
    public void testMulAndCheckLongOverflowNegative() {
        MathUtils.mulAndCheck(Long.MIN_VALUE, 2L);
    }

    @Test(expected = ArithmeticException.class)
    public void testMulAndCheckLongOverflowMinValue() {
        MathUtils.mulAndCheck(Long.MIN_VALUE, -1L);
    }

    // ---------- addAndCheck(long, long) ----------
    @Test
    public void testAddAndCheckLongNormal() {
        assertEquals(5L, MathUtils.addAndCheck(2L, 3L));
        assertEquals(-1L, MathUtils.addAndCheck(-2L, 1L));
        assertEquals(0L, MathUtils.addAndCheck(0L, 0L));
    }

    @Test(expected = ArithmeticException.class)
    public void testAddAndCheckLongOverflowPositive() {
        MathUtils.addAndCheck(Long.MAX_VALUE, 1L);
    }

    @Test(expected = ArithmeticException.class)
    public void testAddAndCheckLongOverflowNegative() {
        MathUtils.addAndCheck(Long.MIN_VALUE, -1L);
    }

    // ---------- subAndCheck(long, long) ----------
    @Test
    public void testSubAndCheckLongNormal() {
        assertEquals(-1L, MathUtils.subAndCheck(2L, 3L));
        assertEquals(5L, MathUtils.subAndCheck(2L, -3L));
        assertEquals(0L, MathUtils.subAndCheck(0L, 0L));
    }

    @Test(expected = ArithmeticException.class)
    public void testSubAndCheckLongOverflowPositive() {
        MathUtils.subAndCheck(Long.MAX_VALUE, -1L);
    }

    @Test(expected = ArithmeticException.class)
    public void testSubAndCheckLongOverflowNegative() {
        MathUtils.subAndCheck(Long.MIN_VALUE, 1L);
    }

    // ---------- sign methods (if present) ----------
    // Not all versions have sign; skip to avoid compilation issues.

    // ---------- hash and equals (not static) ----------
    // MathUtils is a utility class with no instance methods; skip.
}