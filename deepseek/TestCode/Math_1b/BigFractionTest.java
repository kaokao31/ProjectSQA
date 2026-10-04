package org.apache.commons.math3.fraction;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import java.math.BigInteger;

/**
 * Comprehensive JUnit 4 test suite for BigFraction.
 * Designed to achieve maximum code coverage and trigger known defects.
 */
public class BigFractionTest {

    private BigFraction zero;
    private BigFraction one;
    private BigFraction half;
    private BigFraction third;
    private BigFraction negative;
    private BigFraction largeNum;
    private BigFraction largeDen;

    @Before
    public void setUp() {
        zero = new BigFraction(BigInteger.ZERO, BigInteger.ONE);
        one = new BigFraction(BigInteger.ONE, BigInteger.ONE);
        half = new BigFraction(1, 2);
        third = new BigFraction(1, 3);
        negative = new BigFraction(-1, 2);
        largeNum = new BigFraction(new BigInteger("100000000000000000000"),
                                   BigInteger.ONE);
        largeDen = new BigFraction(BigInteger.ONE,
                                   new BigInteger("100000000000000000000"));
    }

    // ========== Constructor Tests ==========

    @Test(expected = ArithmeticException.class)
    public void testConstructorZeroDenominator() {
        new BigFraction(BigInteger.ONE, BigInteger.ZERO);
    }

    @Test(expected = ArithmeticException.class)
    public void testConstructorZeroDenominatorInt() {
        new BigFraction(1, 0);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullNumerator() {
        new BigFraction(null, BigInteger.ONE);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullDenominator() {
        new BigFraction(BigInteger.ONE, null);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullString() {
        new BigFraction((String) null);
    }

    @Test(expected = NumberFormatException.class)
    public void testConstructorInvalidString() {
        new BigFraction("abc");
    }

    @Test
    public void testConstructorInt() {
        BigFraction f = new BigFraction(5);
        assertEquals(5, f.getNumeratorAsInt());
        assertEquals(1, f.getDenominatorAsInt());
    }

    @Test
    public void testConstructorString() {
        BigFraction f = new BigFraction("2/3");
        assertEquals(2, f.getNumeratorAsInt());
        assertEquals(3, f.getDenominatorAsInt());
    }

    @Test
    public void testConstructorStringInteger() {
        BigFraction f = new BigFraction("10");
        assertEquals(10, f.getNumeratorAsInt());
        assertEquals(1, f.getDenominatorAsInt());
    }

    @Test
    public void testConstructorStringNegative() {
        BigFraction f = new BigFraction("-4/6");
        assertEquals(-2, f.getNumeratorAsInt());
        assertEquals(3, f.getDenominatorAsInt());
    }

    @Test
    public void testConstructorNegativeDenominator() {
        // Bug: denominator should be normalized to positive
        BigFraction f = new BigFraction(1, -2);
        assertEquals(-1, f.getNumeratorAsInt());
        assertEquals(2, f.getDenominatorAsInt());
    }

    // ========== Arithmetic Operations ==========

    @Test
    public void testAdd() {
        BigFraction sum = half.add(third);
        assertEquals(new BigFraction(5, 6), sum);
    }

    @Test
    public void testAddZero() {
        BigFraction sum = half.add(zero);
        assertEquals(half, sum);
    }

    @Test
    public void testAddNegative() {
        BigFraction sum = half.add(negative);
        assertEquals(zero, sum);
    }

    @Test
    public void testAddOverflow() {
        // Large numerator addition to test internal overflow handling
        BigFraction big1 = new BigFraction(Long.MAX_VALUE, 2);
        BigFraction big2 = new BigFraction(Long.MAX_VALUE, 2);
        BigFraction sum = big1.add(big2);
        assertEquals(new BigFraction(Long.MAX_VALUE, 1), sum);
    }

    @Test
    public void testSubtract() {
        BigFraction diff = half.subtract(third);
        assertEquals(new BigFraction(1, 6), diff);
    }

    @Test
    public void testSubtractSame() {
        BigFraction diff = half.subtract(half);
        assertEquals(zero, diff);
    }

    @Test
    public void testMultiply() {
        BigFraction product = half.multiply(third);
        assertEquals(new BigFraction(1, 6), product);
    }

    @Test
    public void testMultiplyByZero() {
        BigFraction product = half.multiply(zero);
        assertEquals(zero, product);
    }

    @Test
    public void testMultiplyLarge() {
        BigFraction product = largeNum.multiply(largeDen);
        assertEquals(one, product);
    }

    @Test
    public void testDivide() {
        BigFraction quotient = half.divide(third);
        assertEquals(new BigFraction(3, 2), quotient);
    }

    @Test(expected = ArithmeticException.class)
    public void testDivideByZero() {
        half.divide(zero);
    }

    @Test
    public void testReciprocal() {
        BigFraction rec = half.reciprocal();
        assertEquals(new BigFraction(2, 1), rec);
    }

    @Test(expected = ArithmeticException.class)
    public void testReciprocalZero() {
        zero.reciprocal();
    }

    // ========== Comparison and Equality ==========

    @Test
    public void testEquals() {
        assertEquals(half, new BigFraction(2, 4));
    }

    @Test
    public void testEqualsNull() {
        assertFalse(half.equals(null));
    }

    @Test
    public void testEqualsDifferentClass() {
        assertFalse(half.equals("not a fraction"));
    }

    @Test
    public void testEqualsDifferentValue() {
        assertFalse(half.equals(third));
    }

    @Test
    public void testHashCode() {
        assertEquals(half.hashCode(), new BigFraction(2, 4).hashCode());
    }

    @Test
    public void testHashCodeDifferent() {
        assertNotEquals(half.hashCode(), one.hashCode());
    }

    @Test
    public void testCompareTo() {
        assertTrue(half.compareTo(third) > 0);
        assertTrue(third.compareTo(half) < 0);
        assertEquals(0, half.compareTo(new BigFraction(2, 4)));
    }

    @Test
    public void testCompareToEdge() {
        // Compare with very small negative
        BigFraction smallNeg = new BigFraction(-1, 1000000);
        assertTrue(smallNeg.compareTo(zero) < 0);
        assertTrue(zero.compareTo(smallNeg) > 0);
    }

    // ========== Conversion Methods ==========

    @Test
    public void testGetNumerator() {
        assertEquals(BigInteger.ONE, half.getNumerator());
    }

    @Test
    public void testGetDenominator() {
        assertEquals(BigInteger.valueOf(2), half.getDenominator());
    }

    @Test
    public void testGetNumeratorAsInt() {
        assertEquals(1, half.getNumeratorAsInt());
    }

    @Test(expected = ArithmeticException.class)
    public void testGetNumeratorAsIntOverflow() {
        BigFraction big = new BigFraction(BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.ONE), BigInteger.ONE);
        big.getNumeratorAsInt();
    }

    @Test
    public void testGetDenominatorAsInt() {
        assertEquals(2, half.getDenominatorAsInt());
    }

    @Test(expected = ArithmeticException.class)
    public void testGetDenominatorAsIntOverflow() {
        BigFraction big = new BigFraction(BigInteger.ONE, BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.ONE));
        big.getDenominatorAsInt();
    }

    @Test
    public void testDoubleValue() {
        assertEquals(0.5, half.doubleValue(), 1e-15);
    }

    @Test
    public void testDoubleValueLarge() {
        BigFraction huge = new BigFraction(new BigInteger("99999999999999999999"),
                                           new BigInteger("100000000000000000000"));
        assertEquals(0.99999999999999999999, huge.doubleValue(), 1e-15);
    }

    @Test
    public void testFloatValue() {
        assertEquals(0.5f, half.floatValue(), 1e-15);
    }

    @Test
    public void testIntValue() {
        assertEquals(0, half.intValue());
        assertEquals(1, one.intValue());
    }

    @Test
    public void testLongValue() {
        assertEquals(0L, half.longValue());
        assertEquals(1L, one.longValue());
    }

    // ========== toString ==========

    @Test
    public void testToString() {
        assertEquals("1 / 2", half.toString());
    }

    @Test
    public void testToStringInteger() {
        assertEquals("5", new BigFraction(5, 1).toString());
    }

    @Test
    public void testToStringNegative() {
        assertEquals("-1 / 2", negative.toString());
    }

    // ========== Reduction and Other ==========

    @Test
    public void testReduce() {
        BigFraction unreduced = new BigFraction(4, 6);
        BigFraction reduced = unreduced.reduce();
        assertEquals(new BigFraction(2, 3), reduced);
    }

    @Test
    public void testReduceAlreadyReduced() {
        BigFraction f = half.reduce();
        assertSame(half, f);  // Should return same instance if already reduced
    }

    @Test
    public void testAbs() {
        assertEquals(half, negative.abs());
    }

    @Test
    public void testNegate() {
        assertEquals(negative, half.negate());
    }

    @Test
    public void testPowPositive() {
        assertEquals(new BigFraction(1, 8), half.pow(3));
    }

    @Test
    public void testPowZero() {
        assertEquals(one, half.pow(0));
    }

    @Test
    public void testPowNegative() {
        assertEquals(new BigFraction(4, 1), half.pow(-2));
    }

    // ========== Edge Cases and Known Defects ==========

    @Test
    public void testLargeOverflowInMultiply() {
        // Potential overflow in cross multiplication if using long internally
        BigFraction a = new BigFraction(Long.MAX_VALUE, 1);
        BigFraction b = new BigFraction(1, Long.MAX_VALUE);
        BigFraction product = a.multiply(b);
        assertEquals(one, product);
    }

    @Test
    public void testNegativeFractionReduction() {
        // Bug: reduction might lose sign
        BigFraction f = new BigFraction(-2, 4);
        assertEquals(-1, f.getNumeratorAsInt());
        assertEquals(2, f.getDenominatorAsInt());
    }

    @Test
    public void testAddLargeDenominator() {
        // Adding fractions with large denominators (over 2^63)
        BigFraction big1 = new BigFraction(BigInteger.ONE,
                                           new BigInteger("1000000000000000000000000000000"));
        BigFraction big2 = new BigFraction(BigInteger.ONE,
                                           new BigInteger("2000000000000000000000000000000"));
        BigFraction sum = big1.add(big2);
        assertEquals(new BigFraction(3, 2000000000000000000000000000000L), sum);
    }

    @Test
    public void testCompareToOverflow() {
        // Compare two fractions that would cause long overflow in subtracted
        BigFraction a = new BigFraction(Long.MIN_VALUE, 1);
        BigFraction b = new BigFraction(1, 1);
        assertTrue(a.compareTo(b) < 0);
    }

    @Test
    public void testConstructorDouble() {
        // Test constructor that takes double (if available)
        // In many implementations, there's a constructor double
        // but not in BigFraction typically; skip if not present.
        // Commenting out as optional.
        // new BigFraction(0.5);
    }

    @Test
    public void testSerialization() {
        // Not a strict requirement, but good for coverage
        BigFraction original = half;
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try {
            java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
            oos.writeObject(original);
            oos.flush();
            java.io.ByteArrayInputStream bis = new java.io.ByteArrayInputStream(bos.toByteArray());
            java.io.ObjectInputStream ois = new java.io.ObjectInputStream(bis);
            BigFraction deserialized = (BigFraction) ois.readObject();
            assertEquals(original, deserialized);
        } catch (Exception e) {
            fail("Serialization failed: " + e.getMessage());
        }
    }
}