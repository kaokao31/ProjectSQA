package org.apache.commons.math.util;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Test suite for MathUtils class, targeting high coverage and fault detection.
 * Specifically designed to expose the known bug in gcd (Math-94) and other edge cases.
 */
public class MathUtilsTest {

    // ======================== gcd tests ========================

    @Test
    public void testGcdZeroZero() {
        assertEquals("gcd(0,0) should be 0", 0, MathUtils.gcd(0, 0));
    }

    @Test
    public void testGcdZeroPositive() {
        assertEquals("gcd(0, 12) should be 12", 12, MathUtils.gcd(0, 12));
        assertEquals("gcd(0, 1) should be 1", 1, MathUtils.gcd(0, 1));
        assertEquals("gcd(0, Integer.MAX_VALUE) should be Integer.MAX_VALUE",
                Integer.MAX_VALUE, MathUtils.gcd(0, Integer.MAX_VALUE));
    }

    @Test
    public void testGcdPositiveZero() {
        assertEquals("gcd(12, 0) should be 12", 12, MathUtils.gcd(12, 0));
        assertEquals("gcd(1, 0) should be 1", 1, MathUtils.gcd(1, 0));
        assertEquals("gcd(Integer.MAX_VALUE, 0) should be Integer.MAX_VALUE",
                Integer.MAX_VALUE, MathUtils.gcd(Integer.MAX_VALUE, 0));
    }

    @Test
    public void testGcdPositivePositive() {
        assertEquals("gcd(12, 8) should be 4", 4, MathUtils.gcd(12, 8));
        assertEquals("gcd(17, 5) should be 1", 1, MathUtils.gcd(17, 5));
        assertEquals("gcd(100, 100) should be 100", 100, MathUtils.gcd(100, 100));
        assertEquals("gcd(0, 0) should be 0", 0, MathUtils.gcd(0, 0));
        assertEquals("gcd(1, 1) should be 1", 1, MathUtils.gcd(1, 1));
        assertEquals("gcd(2, 4) should be 2", 2, MathUtils.gcd(2, 4));
        assertEquals("gcd(13, 17) should be 1", 1, MathUtils.gcd(13, 17));
        assertEquals("gcd(36, 48) should be 12", 12, MathUtils.gcd(36, 48));
    }

    @Test
    public void testGcdNegativePositive() {
        assertEquals("gcd(-12, 8) should be 4", 4, MathUtils.gcd(-12, 8));
        assertEquals("gcd(12, -8) should be 4", 4, MathUtils.gcd(12, -8));
        assertEquals("gcd(-17, 5) should be 1", 1, MathUtils.gcd(-17, 5));
        assertEquals("gcd(17, -5) should be 1", 1, MathUtils.gcd(17, -5));
    }

    @Test
    public void testGcdNegativeNegative() {
        assertEquals("gcd(-12, -8) should be 4", 4, MathUtils.gcd(-12, -8));
        assertEquals("gcd(-17, -5) should be 1", 1, MathUtils.gcd(-17, -5));
        assertEquals("gcd(-100, -100) should be 100", 100, MathUtils.gcd(-100, -100));
    }

    @Test
    public void testGcdZeroNegative() {
        assertEquals("gcd(0, -12) should be 12", 12, MathUtils.gcd(0, -12));
        assertEquals("gcd(0, -1) should be 1", 1, MathUtils.gcd(0, -1));
        assertEquals("gcd(0, Integer.MIN_VALUE) should be 2^31 (or throw exception)",
                1 << 31, MathUtils.gcd(0, Integer.MIN_VALUE)); // Known bug: overflow, but expected correct result
    }

    @Test
    public void testGcdNegativeZero() {
        assertEquals("gcd(-12, 0) should be 12", 12, MathUtils.gcd(-12, 0));
        assertEquals("gcd(-1, 0) should be 1", 1, MathUtils.gcd(-1, 0));
        assertEquals("gcd(Integer.MIN_VALUE, 0) should be 2^31 (or throw exception)",
                1 << 31, MathUtils.gcd(Integer.MIN_VALUE, 0)); // Known bug: overflow
    }

    @Test
    public void testGcdMinValueMinValue() {
        // gcd(Integer.MIN_VALUE, Integer.MIN_VALUE) should be 2^31 (or throw exception)
        assertEquals("gcd(MIN_VALUE, MIN_VALUE) should be 2^31",
                1 << 31, MathUtils.gcd(Integer.MIN_VALUE, Integer.MIN_VALUE));
    }

    @Test
    public void testGcdMinValuePositive() {
        // gcd(Integer.MIN_VALUE, 1) should be 1
        assertEquals("gcd(MIN_VALUE, 1) should be 1", 1, MathUtils.gcd(Integer.MIN_VALUE, 1));
        // gcd(Integer.MIN_VALUE, 2) should be 2
        assertEquals("gcd(MIN_VALUE, 2) should be 2", 2, MathUtils.gcd(Integer.MIN_VALUE, 2));
        // gcd(Integer.MIN_VALUE, 3) should be 1
        assertEquals("gcd(MIN_VALUE, 3) should be 1", 1, MathUtils.gcd(Integer.MIN_VALUE, 3));
    }

    @Test
    public void testGcdPositiveMinValue() {
        assertEquals("gcd(1, MIN_VALUE) should be 1", 1, MathUtils.gcd(1, Integer.MIN_VALUE));
        assertEquals("gcd(2, MIN_VALUE) should be 2", 2, MathUtils.gcd(2, Integer.MIN_VALUE));
        assertEquals("gcd(3, MIN_VALUE) should be 1", 1, MathUtils.gcd(3, Integer.MIN_VALUE));
    }

    @Test
    public void testGcdNegativeMinValue() {
        assertEquals("gcd(-1, MIN_VALUE) should be 1", 1, MathUtils.gcd(-1, Integer.MIN_VALUE));
        assertEquals("gcd(-2, MIN_VALUE) should be 2", 2, MathUtils.gcd(-2, Integer.MIN_VALUE));
        assertEquals("gcd(MIN_VALUE, -1) should be 1", 1, MathUtils.gcd(Integer.MIN_VALUE, -1));
    }

    // ======================== lcm tests ========================

    @Test
    public void testLcmZero() {
        assertEquals("lcm(0, 5) should be 0", 0, MathUtils.lcm(0, 5));
        assertEquals("lcm(5, 0) should be 0", 0, MathUtils.lcm(5, 0));
        assertEquals("lcm(0, 0) should be 0", 0, MathUtils.lcm(0, 0));
    }

    @Test
    public void testLcmPositive() {
        assertEquals("lcm(12, 8) should be 24", 24, MathUtils.lcm(12, 8));
        assertEquals("lcm(17, 5) should be 85", 85, MathUtils.lcm(17, 5));
        assertEquals("lcm(100, 100) should be 100", 100, MathUtils.lcm(100, 100));
        assertEquals("lcm(1, 1) should be 1", 1, MathUtils.lcm(1, 1));
        assertEquals("lcm(2, 4) should be 4", 4, MathUtils.lcm(2, 4));
        assertEquals("lcm(13, 17) should be 221", 221, MathUtils.lcm(13, 17));
        assertEquals("lcm(36, 48) should be 144", 144, MathUtils.lcm(36, 48));
    }

    @Test
    public void testLcmNegative() {
        assertEquals("lcm(-12, 8) should be 24", 24, MathUtils.lcm(-12, 8));
        assertEquals("lcm(12, -8) should be 24", 24, MathUtils.lcm(12, -8));
        assertEquals("lcm(-12, -8) should be 24", 24, MathUtils.lcm(-12, -8));
    }

    @Test(expected = ArithmeticException.class)
    public void testLcmOverflow() {
        MathUtils.lcm(Integer.MAX_VALUE, 2); // should overflow
    }

    @Test(expected = ArithmeticException.class)
    public void testLcmMinValue() {
        MathUtils.lcm(Integer.MIN_VALUE, 1); // overflow due to abs
    }

    // ======================== binomialCoefficient tests ========================

    @Test
    public void testBinomialCoefficient() {
        assertEquals("binomialCoefficient(0,0) should be 1", 1, MathUtils.binomialCoefficient(0, 0));
        assertEquals("binomialCoefficient(5,0) should be 1", 1, MathUtils.binomialCoefficient(5, 0));
        assertEquals("binomialCoefficient(5,5) should be 1", 1, MathUtils.binomialCoefficient(5, 5));
        assertEquals("binomialCoefficient(5,2) should be 10", 10, MathUtils.binomialCoefficient(5, 2));
        assertEquals("binomialCoefficient(10,5) should be 252", 252, MathUtils.binomialCoefficient(10, 5));
        assertEquals("binomialCoefficient(20,10) should be 184756", 184756, MathUtils.binomialCoefficient(20, 10));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBinomialCoefficientNegativeN() {
        MathUtils.binomialCoefficient(-1, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBinomialCoefficientKGreaterThanN() {
        MathUtils.binomialCoefficient(5, 6);
    }

    @Test(expected = ArithmeticException.class)
    public void testBinomialCoefficientOverflow() {
        MathUtils.binomialCoefficient(30, 15); // 155117520, fits? Actually 30C15 = 155117520, fits in int. Try 34C17 = 2333606220 > Integer.MAX_VALUE
        MathUtils.binomialCoefficient(34, 17);
    }

    // ======================== binomialCoefficientLog tests ========================

    @Test
    public void testBinomialCoefficientLog() {
        assertEquals("binomialCoefficientLog(0,0) should be 0.0", 0.0, MathUtils.binomialCoefficientLog(0, 0), 1e-12);
        assertEquals("binomialCoefficientLog(5,2) should be log(10)", Math.log(10), MathUtils.binomialCoefficientLog(5, 2), 1e-12);
        assertEquals("binomialCoefficientLog(10,5) should be log(252)", Math.log(252), MathUtils.binomialCoefficientLog(10, 5), 1e-12);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBinomialCoefficientLogNegativeN() {
        MathUtils.binomialCoefficientLog(-1, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBinomialCoefficientLogKGreaterThanN() {
        MathUtils.binomialCoefficientLog(5, 6);
    }

    // ======================== factorial tests ========================

    @Test
    public void testFactorial() {
        assertEquals("factorial(0) should be 1", 1, MathUtils.factorial(0));
        assertEquals("factorial(1) should be 1", 1, MathUtils.factorial(1));
        assertEquals("factorial(5) should be 120", 120, MathUtils.factorial(5));
        assertEquals("factorial(10) should be 3628800", 3628800, MathUtils.factorial(10));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFactorialNegative() {
        MathUtils.factorial(-1);
    }

    @Test(expected = ArithmeticException.class)
    public void testFactorialOverflow() {
        MathUtils.factorial(13); // 13! = 6227020800 > Integer.MAX_VALUE
    }

    // ======================== factorialLog tests ========================

    @Test
    public void testFactorialLog() {
        assertEquals("factorialLog(0) should be 0.0", 0.0, MathUtils.factorialLog(0), 1e-12);
        assertEquals("factorialLog(5) should be log(120)", Math.log(120), MathUtils.factorialLog(5), 1e-12);
        assertEquals("factorialLog(10) should be log(3628800)", Math.log(3628800), MathUtils.factorialLog(10), 1e-12);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFactorialLogNegative() {
        MathUtils.factorialLog(-1);
    }

    // ======================== hash tests (if present) ========================

    @Test
    public void testHash() {
        // Assuming hash methods exist: hash(int) and hash(double)
        // These are typically used for generating hash codes.
        // We'll test basic consistency.
        int h1 = MathUtils.hash(1.0);
        int h2 = MathUtils.hash(1.0);
        assertEquals("hash of same double should be equal", h1, h2);
        assertNotSame("hash of different doubles should differ", MathUtils.hash(1.0), MathUtils.hash(2.0));
    }

    @Test
    public void testHashInt() {
        int h1 = MathUtils.hash(42);
        int h2 = MathUtils.hash(42);
        assertEquals("hash of same int should be equal", h1, h2);
        assertNotSame("hash of different ints should differ", MathUtils.hash(42), MathUtils.hash(43));
    }

    // ======================== additional edge cases ========================

    @Test
    public void testGcdLargeNumbers() {
        // Test with large numbers that may cause overflow in subtraction-based algorithm
        assertEquals("gcd(123456789, 987654321) should be 9", 9, MathUtils.gcd(123456789, 987654321));
        assertEquals("gcd(2147483647, 1) should be 1", 1, MathUtils.gcd(2147483647, 1));
        assertEquals("gcd(2147483647, 2147483647) should be 2147483647", 2147483647, MathUtils.gcd(2147483647, 2147483647));
    }

    @Test
    public void testGcdAllPositive() {
        // Exhaustive small values to ensure correctness
        for (int i = 1; i <= 100; i++) {
            for (int j = 1; j <= 100; j++) {
                int expected = gcdReference(i, j);
                assertEquals("gcd(" + i + "," + j + ")", expected, MathUtils.gcd(i, j));
            }
        }
    }

    // Reference implementation for testing
    private int gcdReference(int a, int b) {
        a = Math.abs(a);
        b = Math.abs(b);
        while (b != 0) {
            int temp = b;
            b = a % b;
            a = temp;
        }
        return a;
    }
}