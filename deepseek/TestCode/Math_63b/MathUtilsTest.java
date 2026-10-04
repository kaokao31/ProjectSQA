package org.apache.commons.math.util;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Test suite for MathUtils (Defects4J Math-63).
 * Covers all public methods, with special focus on the buggy equals(double[], double[]) method.
 */
public class MathUtilsTest {

    // ---------- equals(double, double) with tolerance ----------
    @Test
    public void testEqualsDoubleDouble() {
        assertTrue("Equal values", MathUtils.equals(1.0, 1.0, 1e-10));
        assertTrue("Within tolerance", MathUtils.equals(1.0, 1.0000000001, 1e-9));
        assertFalse("Outside tolerance", MathUtils.equals(1.0, 1.1, 0.01));
        assertFalse("NaN vs number", MathUtils.equals(Double.NaN, 1.0, 0.1));
        assertFalse("Number vs NaN", MathUtils.equals(1.0, Double.NaN, 0.1));
        // NaN vs NaN: typically false even with tolerance
        assertFalse("NaN vs NaN", MathUtils.equals(Double.NaN, Double.NaN, 0.1));
        // Infinity
        assertTrue("Infinity equal", MathUtils.equals(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 0.0));
        assertFalse("Different infinities", MathUtils.equals(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, 0.0));
        // Zero vs negative zero
        assertTrue("Zero vs negative zero", MathUtils.equals(0.0, -0.0, 0.0));
    }

    // ---------- equals(double[], double[]) (buggy method) ----------
    @Test
    public void testEqualsDoubleArray() {
        double[] a = {1.0, 2.0, 3.0};
        double[] b = {1.0, 2.0, 3.0};
        assertTrue("Identical arrays", MathUtils.equals(a, b));

        double[] c = {1.0, 2.0, 3.0};
        double[] d = {1.0, 2.0, 4.0};
        assertFalse("Different values", MathUtils.equals(c, d));

        // NaN handling: buggy version returns true, correct should return false
        double[] nanArray1 = {Double.NaN, 1.0};
        double[] nanArray2 = {Double.NaN, 1.0};
        assertFalse("NaN arrays should not be equal", MathUtils.equals(nanArray1, nanArray2));

        // One NaN, one not
        double[] nanArray3 = {Double.NaN, 1.0};
        double[] noNanArray = {0.0, 1.0};
        assertFalse("NaN vs number", MathUtils.equals(nanArray3, noNanArray));

        // Null handling
        assertTrue("Both null", MathUtils.equals(null, null));
        assertFalse("First null", MathUtils.equals(null, new double[]{1.0}));
        assertFalse("Second null", MathUtils.equals(new double[]{1.0}, null));

        // Empty arrays
        assertTrue("Empty arrays", MathUtils.equals(new double[0], new double[0]));
        assertFalse("Empty vs non-empty", MathUtils.equals(new double[0], new double[]{1.0}));

        // Different lengths
        assertFalse("Different lengths", MathUtils.equals(new double[]{1.0}, new double[]{1.0, 2.0}));

        // Infinity
        double[] infArray1 = {Double.POSITIVE_INFINITY};
        double[] infArray2 = {Double.POSITIVE_INFINITY};
        assertTrue("Infinity arrays", MathUtils.equals(infArray1, infArray2));
        double[] negInfArray = {Double.NEGATIVE_INFINITY};
        assertFalse("Different infinities", MathUtils.equals(infArray1, negInfArray));
    }

    // ---------- hash(double) ----------
    @Test
    public void testHashDouble() {
        assertEquals("Hash of 0.0", 0, MathUtils.hash(0.0));
        assertEquals("Hash of -0.0", 0, MathUtils.hash(-0.0)); // same as 0.0
        assertNotEquals("Hash of NaN", MathUtils.hash(Double.NaN), MathUtils.hash(1.0));
        // Consistency
        assertEquals("Consistent hash", MathUtils.hash(1.0), MathUtils.hash(1.0));
    }

    // ---------- hash(double[]) ----------
    @Test
    public void testHashDoubleArray() {
        double[] a = {1.0, 2.0};
        double[] b = {1.0, 2.0};
        assertEquals("Same array hash", MathUtils.hash(a), MathUtils.hash(b));
        double[] c = {1.0, 3.0};
        assertNotEquals("Different array hash", MathUtils.hash(a), MathUtils.hash(c));
        // Null
        assertEquals("Null hash", 0, MathUtils.hash((double[]) null));
        // Empty
        assertEquals("Empty array hash", 0, MathUtils.hash(new double[0]));
    }

    // ---------- compareTo(double, double, double) ----------
    @Test
    public void testCompareTo() {
        assertEquals("Equal within eps", 0, MathUtils.compareTo(1.0, 1.0000000001, 1e-9));
        assertEquals("Less than", -1, MathUtils.compareTo(1.0, 2.0, 0.1));
        assertEquals("Greater than", 1, MathUtils.compareTo(2.0, 1.0, 0.1));
        // NaN handling: NaN is considered greater than any number? In Commons Math, compareTo treats NaN as greater.
        assertEquals("NaN vs number", 1, MathUtils.compareTo(Double.NaN, 1.0, 0.1));
        assertEquals("Number vs NaN", -1, MathUtils.compareTo(1.0, Double.NaN, 0.1));
        assertEquals("NaN vs NaN", 0, MathUtils.compareTo(Double.NaN, Double.NaN, 0.1));
    }

    // ---------- round(double, int) ----------
    @Test
    public void testRoundDouble() {
        assertEquals("Round to 0 decimals", 3.0, MathUtils.round(3.14159, 0), 1e-10);
        assertEquals("Round to 2 decimals", 3.14, MathUtils.round(3.14159, 2), 1e-10);
        assertEquals("Round up", 3.15, MathUtils.round(3.145, 2), 1e-10);
        assertEquals("Round negative", -3.14, MathUtils.round(-3.14159, 2), 1e-10);
        // Large number of decimals
        assertEquals("Round many decimals", 3.14159, MathUtils.round(3.14159, 5), 1e-10);
        // Special values
        assertEquals("Round NaN", Double.NaN, MathUtils.round(Double.NaN, 2), 0.0);
        assertEquals("Round infinity", Double.POSITIVE_INFINITY, MathUtils.round(Double.POSITIVE_INFINITY, 2), 0.0);
    }

    // ---------- round(float, int) ----------
    @Test
    public void testRoundFloat() {
        assertEquals("Round float to 0 decimals", 3.0f, MathUtils.round(3.14159f, 0), 1e-10f);
        assertEquals("Round float to 2 decimals", 3.14f, MathUtils.round(3.14159f, 2), 1e-10f);
        assertEquals("Round float up", 3.15f, MathUtils.round(3.145f, 2), 1e-10f);
        assertEquals("Round negative float", -3.14f, MathUtils.round(-3.14159f, 2), 1e-10f);
        assertEquals("Round float NaN", Float.NaN, MathUtils.round(Float.NaN, 2), 0.0f);
        assertEquals("Round float infinity", Float.POSITIVE_INFINITY, MathUtils.round(Float.POSITIVE_INFINITY, 2), 0.0f);
    }

    // ---------- sign(double) ----------
    @Test
    public void testSign() {
        assertEquals("Positive sign", 1.0, MathUtils.sign(5.0), 1e-10);
        assertEquals("Negative sign", -1.0, MathUtils.sign(-5.0), 1e-10);
        assertEquals("Zero sign", 0.0, MathUtils.sign(0.0), 1e-10);
        assertEquals("Negative zero sign", 0.0, MathUtils.sign(-0.0), 1e-10);
        assertEquals("NaN sign", Double.NaN, MathUtils.sign(Double.NaN), 0.0);
    }

    // ---------- addAndCheck(int, int) ----------
    @Test
    public void testAddAndCheckInt() {
        assertEquals("Normal add", 5, MathUtils.addAndCheck(2, 3));
        assertEquals("Negative add", -5, MathUtils.addAndCheck(-2, -3));
        // Overflow
        try {
            MathUtils.addAndCheck(Integer.MAX_VALUE, 1);
            fail("Expected ArithmeticException for overflow");
        } catch (ArithmeticException e) {
            // expected
        }
        try {
            MathUtils.addAndCheck(Integer.MIN_VALUE, -1);
            fail("Expected ArithmeticException for underflow");
        } catch (ArithmeticException e) {
            // expected
        }
    }

    // ---------- addAndCheck(long, long) ----------
    @Test
    public void testAddAndCheckLong() {
        assertEquals("Normal long add", 5L, MathUtils.addAndCheck(2L, 3L));
        assertEquals("Negative long add", -5L, MathUtils.addAndCheck(-2L, -3L));
        // Overflow
        try {
            MathUtils.addAndCheck(Long.MAX_VALUE, 1L);
            fail("Expected ArithmeticException for long overflow");
        } catch (ArithmeticException e) {
            // expected
        }
        try {
            MathUtils.addAndCheck(Long.MIN_VALUE, -1L);
            fail("Expected ArithmeticException for long underflow");
        } catch (ArithmeticException e) {
            // expected
        }
    }

    // ---------- subAndCheck(int, int) ----------
    @Test
    public void testSubAndCheckInt() {
        assertEquals("Normal sub", 2, MathUtils.subAndCheck(5, 3));
        assertEquals("Negative sub", -2, MathUtils.subAndCheck(-5, -3));
        // Overflow
        try {
            MathUtils.subAndCheck(Integer.MIN_VALUE, 1);
            fail("Expected ArithmeticException for overflow");
        } catch (ArithmeticException e) {
            // expected
        }
        try {
            MathUtils.subAndCheck(Integer.MAX_VALUE, -1);
            fail("Expected ArithmeticException for underflow");
        } catch (ArithmeticException e) {
            // expected
        }
    }

    // ---------- subAndCheck(long, long) ----------
    @Test
    public void testSubAndCheckLong() {
        assertEquals("Normal long sub", 2L, MathUtils.subAndCheck(5L, 3L));
        assertEquals("Negative long sub", -2L, MathUtils.subAndCheck(-5L, -3L));
        try {
            MathUtils.subAndCheck(Long.MIN_VALUE, 1L);
            fail("Expected ArithmeticException for long overflow");
        } catch (ArithmeticException e) {
            // expected
        }
        try {
            MathUtils.subAndCheck(Long.MAX_VALUE, -1L);
            fail("Expected ArithmeticException for long underflow");
        } catch (ArithmeticException e) {
            // expected
        }
    }

    // ---------- mulAndCheck(int, int) ----------
    @Test
    public void testMulAndCheckInt() {
        assertEquals("Normal mul", 6, MathUtils.mulAndCheck(2, 3));
        assertEquals("Negative mul", -6, MathUtils.mulAndCheck(-2, 3));
        // Overflow
        try {
            MathUtils.mulAndCheck(Integer.MAX_VALUE, 2);
            fail("Expected ArithmeticException for overflow");
        } catch (ArithmeticException e) {
            // expected
        }
        try {
            MathUtils.mulAndCheck(Integer.MIN_VALUE, 2);
            fail("Expected ArithmeticException for underflow");
        } catch (ArithmeticException e) {
            // expected
        }
    }

    // ---------- mulAndCheck(long, long) ----------
    @Test
    public void testMulAndCheckLong() {
        assertEquals("Normal long mul", 6L, MathUtils.mulAndCheck(2L, 3L));
        assertEquals("Negative long mul", -6L, MathUtils.mulAndCheck(-2L, 3L));
        try {
            MathUtils.mulAndCheck(Long.MAX_VALUE, 2L);
            fail("Expected ArithmeticException for long overflow");
        } catch (ArithmeticException e) {
            // expected
        }
        try {
            MathUtils.mulAndCheck(Long.MIN_VALUE, 2L);
            fail("Expected ArithmeticException for long underflow");
        } catch (ArithmeticException e) {
            // expected
        }
    }

    // ---------- binomialCoefficient(int, int) ----------
    @Test
    public void testBinomialCoefficient() {
        assertEquals("C(5,2)", 10, MathUtils.binomialCoefficient(5, 2));
        assertEquals("C(5,5)", 1, MathUtils.binomialCoefficient(5, 5));
        assertEquals("C(5,0)", 1, MathUtils.binomialCoefficient(5, 0));
        // Overflow
        try {
            MathUtils.binomialCoefficient(100, 50);
            fail("Expected ArithmeticException for overflow");
        } catch (ArithmeticException e) {
            // expected
        }
        // Negative or k>n
        try {
            MathUtils.binomialCoefficient(5, 6);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    // ---------- binomialCoefficientLog(int, int) ----------
    @Test
    public void testBinomialCoefficientLog() {
        double log = MathUtils.binomialCoefficientLog(5, 2);
        assertEquals("Log C(5,2)", Math.log(10), log, 1e-10);
        assertEquals("Log C(5,0)", 0.0, MathUtils.binomialCoefficientLog(5, 0), 1e-10);
        assertEquals("Log C(5,5)", 0.0, MathUtils.binomialCoefficientLog(5, 5), 1e-10);
        // Negative or k>n
        try {
            MathUtils.binomialCoefficientLog(5, 6);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    // ---------- factorial(int) ----------
    @Test
    public void testFactorial() {
        assertEquals("0!", 1, MathUtils.factorial(0));
        assertEquals("1!", 1, MathUtils.factorial(1));
        assertEquals("5!", 120, MathUtils.factorial(5));
        // Overflow
        try {
            MathUtils.factorial(13);
            fail("Expected ArithmeticException for overflow");
        } catch (ArithmeticException e) {
            // expected
        }
        // Negative
        try {
            MathUtils.factorial(-1);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    // ---------- factorialLog(int) ----------
    @Test
    public void testFactorialLog() {
        assertEquals("Log 0!", 0.0, MathUtils.factorialLog(0), 1e-10);
        assertEquals("Log 1!", 0.0, MathUtils.factorialLog(1), 1e-10);
        assertEquals("Log 5!", Math.log(120), MathUtils.factorialLog(5), 1e-10);
        // Negative
        try {
            MathUtils.factorialLog(-1);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    // ---------- gcd(int, int) ----------
    @Test
    public void testGcd() {
        assertEquals("gcd(12,8)", 4, MathUtils.gcd(12, 8));
        assertEquals("gcd(0,5)", 5, MathUtils.gcd(0, 5));
        assertEquals("gcd(5,0)", 5, MathUtils.gcd(5, 0));
        assertEquals("gcd(0,0)", 0, MathUtils.gcd(0, 0));
        assertEquals("gcd(-12,8)", 4, MathUtils.gcd(-12, 8));
        assertEquals("gcd(12,-8)", 4, MathUtils.gcd(12, -8));
        assertEquals("gcd(-12,-8)", 4, MathUtils.gcd(-12, -8));
        // Large numbers
        assertEquals("gcd(1071,462)", 21, MathUtils.gcd(1071, 462));
    }

    // ---------- lcm(int, int) ----------
    @Test
    public void testLcm() {
        assertEquals("lcm(12,8)", 24, MathUtils.lcm(12, 8));
        assertEquals("lcm(0,5)", 0, MathUtils.lcm(0, 5));
        assertEquals("lcm(5,0)", 0, MathUtils.lcm(5, 0));
        // Overflow
        try {
            MathUtils.lcm(Integer.MAX_VALUE, 2);
            fail("Expected ArithmeticException for overflow");
        } catch (ArithmeticException e) {
            // expected
        }
    }

    // ---------- normalizeArray(double[], double) ----------
    @Test
    public void testNormalizeArray() {
        double[] values = {1.0, 2.0, 3.0};
        double[] normalized = MathUtils.normalizeArray(values, 6.0);
        double sum = 0;
        for (double v : normalized) sum += v;
        assertEquals("Sum equals target", 6.0, sum, 1e-10);
        // Proportions preserved
        assertEquals("First element", 1.0, normalized[0], 1e-10);
        assertEquals("Second element", 2.0, normalized[1], 1e-10);
        assertEquals("Third element", 3.0, normalized[2], 1e-10);

        // Negative target
        double[] negNormalized = MathUtils.normalizeArray(values, -6.0);
        double negSum = 0;
        for (double v : negNormalized) negSum += v;
        assertEquals("Negative sum", -6.0, negSum, 1e-10);

        // Zero sum array
        double[] zeroSum = {1.0, -1.0};
        try {
            MathUtils.normalizeArray(zeroSum, 1.0);
            fail("Expected ArithmeticException for zero sum");
        } catch (ArithmeticException e) {
            // expected
        }

        // Single element
        double[] single = {5.0};
        double[] normSingle = MathUtils.normalizeArray(single, 10.0);
        assertEquals("Single element", 10.0, normSingle[0], 1e-10);
    }

    // ---------- equals(double, double) without tolerance (exact) ----------
    @Test
    public void testEqualsDoubleExact() {
        assertTrue("Exact equal", MathUtils.equals(1.0, 1.0));
        assertFalse("Different", MathUtils.equals(1.0, 1.0000000001));
        // NaN
        assertFalse("NaN vs NaN exact", MathUtils.equals(Double.NaN, Double.NaN));
        assertFalse("NaN vs number", MathUtils.equals(Double.NaN, 1.0));
        // Zero vs negative zero
        assertTrue("Zero vs negative zero exact", MathUtils.equals(0.0, -0.0));
        // Infinity
        assertTrue("Infinity exact", MathUtils.equals(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY));
        assertFalse("Different infinities exact", MathUtils.equals(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY));
    }

    // ---------- equals(double, double, double) with tolerance (already tested) but also test exact tolerance ----------
    @Test
    public void testEqualsDoubleDoubleDouble() {
        // Already covered in testEqualsDoubleDouble, but add more edge cases
        assertTrue("Tolerance zero exact", MathUtils.equals(1.0, 1.0, 0.0));
        assertFalse("Tolerance zero different", MathUtils.equals(1.0, 1.0000000001, 0.0));
        // Negative tolerance? Should treat as absolute? Usually throws? Not sure, but we can test.
        // In Commons Math, tolerance is absolute, so negative tolerance might be treated as zero? We'll skip.
    }

    // ---------- Additional edge cases for equals(double[], double[]) ----------
    @Test
    public void testEqualsDoubleArrayEdgeCases() {
        // Both arrays contain Double.POSITIVE_INFINITY and Double.NEGATIVE_INFINITY
        double[] infPos = {Double.POSITIVE_INFINITY};
        double[] infPos2 = {Double.POSITIVE_INFINITY};
        assertTrue("Positive infinity arrays", MathUtils.equals(infPos, infPos2));
        double[] infNeg = {Double.NEGATIVE_INFINITY};
        assertFalse("Positive vs negative infinity", MathUtils.equals(infPos, infNeg));

        // Arrays with -0.0 and 0.0
        double[] zeroArray = {0.0};
        double[] negZeroArray = {-0.0};
        assertTrue("Zero vs negative zero array", MathUtils.equals(zeroArray, negZeroArray));

        // Mixed NaN and numbers
        double[] mixed1 = {Double.NaN, 1.0, 2.0};
        double[] mixed2 = {Double.NaN, 1.0, 2.0};
        assertFalse("Mixed NaN arrays", MathUtils.equals(mixed1, mixed2));
    }

    // ---------- equals(double, double) with NaN (exact) ----------
    @Test
    public void testEqualsDoubleExactNaN() {
        // This is the core bug: exact equals should treat NaN as not equal to itself
        assertFalse("NaN != NaN exact", MathUtils.equals(Double.NaN, Double.NaN));
        assertFalse("NaN != number", MathUtils.equals(Double.NaN, 0.0));
    }
}