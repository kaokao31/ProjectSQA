package org.apache.commons.math.fraction;

import org.junit.Test;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;

public class BigFractionTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private BigFraction half;
    private BigFraction third;
    private BigFraction twoThirds;
    private BigFraction zero;
    private BigFraction one;

    @Before
    public void setUp() {
        half = new BigFraction(1, 2);
        third = new BigFraction(1, 3);
        twoThirds = new BigFraction(2, 3);
        zero = new BigFraction(0, 1);
        one = new BigFraction(1, 1);
    }

    // Constructor tests
    @Test
    public void testConstructorZeroDenominator() {
        thrown.expect(ArithmeticException.class);
        new BigFraction(1, 0);
    }

    @Test
    public void testConstructorNullNumerator() {
        thrown.expect(NullPointerException.class);
        new BigFraction(null, BigInteger.ONE);
    }

    @Test
    public void testConstructorNullDenominator() {
        thrown.expect(NullPointerException.class);
        new BigFraction(BigInteger.ONE, null);
    }

    @Test
    public void testConstructorNormal() {
        BigFraction f = new BigFraction(1, 2);
        Assert.assertEquals(BigInteger.ONE, f.getNumerator());
        Assert.assertEquals(BigInteger.valueOf(2), f.getDenominator());
    }

    @Test
    public void testConstructorNegativeNumerator() {
        BigFraction f = new BigFraction(-1, 2);
        Assert.assertEquals(BigInteger.valueOf(-1), f.getNumerator());
        Assert.assertEquals(BigInteger.valueOf(2), f.getDenominator());
    }

    @Test
    public void testConstructorNegativeDenominator() {
        // Denominator negative should be normalized to positive denominator
        BigFraction f = new BigFraction(1, -2);
        Assert.assertEquals(BigInteger.valueOf(-1), f.getNumerator());
        Assert.assertEquals(BigInteger.valueOf(2), f.getDenominator());
    }

    @Test
    public void testConstructorReduce() {
        BigFraction f = new BigFraction(2, 4);
        Assert.assertEquals(BigInteger.ONE, f.getNumerator());
        Assert.assertEquals(BigInteger.valueOf(2), f.getDenominator());
    }

    @Test
    public void testConstructorInt() {
        BigFraction f = new BigFraction(5);
        Assert.assertEquals(BigInteger.valueOf(5), f.getNumerator());
        Assert.assertEquals(BigInteger.ONE, f.getDenominator());
    }

    @Test
    public void testConstructorBigInteger() {
        BigFraction f = new BigFraction(BigInteger.TEN);
        Assert.assertEquals(BigInteger.TEN, f.getNumerator());
        Assert.assertEquals(BigInteger.ONE, f.getDenominator());
    }

    // Arithmetic operations
    @Test
    public void testAdd() {
        BigFraction result = half.add(third);
        Assert.assertEquals(new BigFraction(5, 6), result);
    }

    @Test
    public void testAddWithZero() {
        BigFraction result = half.add(zero);
        Assert.assertEquals(half, result);
    }

    @Test
    public void testSubtract() {
        BigFraction result = half.subtract(third);
        Assert.assertEquals(new BigFraction(1, 6), result);
    }

    @Test
    public void testSubtractFromZero() {
        BigFraction result = zero.subtract(half);
        Assert.assertEquals(new BigFraction(-1, 2), result);
    }

    @Test
    public void testMultiply() {
        BigFraction result = half.multiply(twoThirds);
        Assert.assertEquals(new BigFraction(1, 3), result);
    }

    @Test
    public void testMultiplyByZero() {
        BigFraction result = half.multiply(zero);
        Assert.assertEquals(zero, result);
    }

    @Test
    public void testDivide() {
        BigFraction result = half.divide(third);
        Assert.assertEquals(new BigFraction(3, 2), result);
    }

    @Test
    public void testDivideByZero() {
        thrown.expect(ArithmeticException.class);
        half.divide(zero);
    }

    @Test
    public void testPowPositiveExponent() {
        BigFraction result = half.pow(2);
        Assert.assertEquals(new BigFraction(1, 4), result);
    }

    @Test
    public void testPowZeroExponent() {
        BigFraction result = half.pow(0);
        Assert.assertEquals(one, result);
    }

    @Test
    public void testPowNegativeExponent() {
        BigFraction result = half.pow(-1);
        Assert.assertEquals(new BigFraction(2, 1), result);
    }

    @Test
    public void testPowNegativeExponentWithZeroNumerator() {
        // 0^(-1) should throw ArithmeticException
        thrown.expect(ArithmeticException.class);
        zero.pow(-1);
    }

    @Test
    public void testPowNegativeExponentWithNonZeroNumerator() {
        BigFraction result = new BigFraction(2, 3).pow(-2);
        Assert.assertEquals(new BigFraction(9, 4), result);
    }

    // equals and hashCode
    @Test
    public void testEqualsReflexive() {
        Assert.assertEquals(half, half);
    }

    @Test
    public void testEqualsSymmetric() {
        BigFraction f1 = new BigFraction(1, 2);
        BigFraction f2 = new BigFraction(1, 2);
        Assert.assertEquals(f1, f2);
        Assert.assertEquals(f2, f1);
    }

    @Test
    public void testEqualsNull() {
        Assert.assertNotEquals(half, null);
    }

    @Test
    public void testEqualsDifferentClass() {
        Assert.assertNotEquals(half, "string");
    }

    @Test
    public void testEqualsDifferentNumerator() {
        Assert.assertNotEquals(half, third);
    }

    @Test
    public void testEqualsReducedForm() {
        BigFraction f1 = new BigFraction(2, 4);
        BigFraction f2 = new BigFraction(1, 2);
        Assert.assertEquals(f1, f2);
    }

    @Test
    public void testHashCodeConsistency() {
        BigFraction f1 = new BigFraction(1, 2);
        BigFraction f2 = new BigFraction(1, 2);
        Assert.assertEquals(f1.hashCode(), f2.hashCode());
    }

    @Test
    public void testHashCodeDifferent() {
        Assert.assertNotEquals(half.hashCode(), third.hashCode());
    }

    // Additional edge cases
    @Test
    public void testLargeNumbers() {
        BigInteger largeNum = BigInteger.valueOf(Long.MAX_VALUE).multiply(BigInteger.valueOf(100));
        BigInteger largeDen = BigInteger.valueOf(Long.MAX_VALUE).multiply(BigInteger.valueOf(200));
        BigFraction f = new BigFraction(largeNum, largeDen);
        // Should reduce to 1/2
        Assert.assertEquals(new BigFraction(1, 2), f);
    }

    @Test
    public void testNegativeDenominatorNormalization() {
        BigFraction f = new BigFraction(1, -3);
        Assert.assertEquals(BigInteger.valueOf(-1), f.getNumerator());
        Assert.assertEquals(BigInteger.valueOf(3), f.getDenominator());
    }

    @Test
    public void testBothNegative() {
        BigFraction f = new BigFraction(-1, -2);
        Assert.assertEquals(BigInteger.ONE, f.getNumerator());
        Assert.assertEquals(BigInteger.valueOf(2), f.getDenominator());
    }

    @Test
    public void testGetNumeratorDenominator() {
        Assert.assertEquals(BigInteger.ONE, half.getNumerator());
        Assert.assertEquals(BigInteger.valueOf(2), half.getDenominator());
    }

    @Test
    public void testToString() {
        Assert.assertEquals("1 / 2", half.toString());
    }

    @Test
    public void testAbs() {
        BigFraction negHalf = new BigFraction(-1, 2);
        Assert.assertEquals(half, negHalf.abs());
    }

    @Test
    public void testNegate() {
        BigFraction negHalf = new BigFraction(-1, 2);
        Assert.assertEquals(negHalf, half.negate());
    }

    @Test
    public void testReciprocal() {
        BigFraction reciprocal = half.reciprocal();
        Assert.assertEquals(new BigFraction(2, 1), reciprocal);
    }

    @Test
    public void testReciprocalOfZero() {
        thrown.expect(ArithmeticException.class);
        zero.reciprocal();
    }

    @Test
    public void testCompareTo() {
        Assert.assertTrue(half.compareTo(third) > 0);
        Assert.assertTrue(third.compareTo(half) < 0);
        Assert.assertEquals(0, half.compareTo(new BigFraction(1, 2)));
    }

    @Test
    public void testDoubleValue() {
        Assert.assertEquals(0.5, half.doubleValue(), 1e-15);
    }

    @Test
    public void testFloatValue() {
        Assert.assertEquals(0.5f, half.floatValue(), 1e-15);
    }

    @Test
    public void testIntValue() {
        Assert.assertEquals(0, half.intValue());
        Assert.assertEquals(2, new BigFraction(2, 1).intValue());
    }

    @Test
    public void testLongValue() {
        Assert.assertEquals(0L, half.longValue());
        Assert.assertEquals(2L, new BigFraction(2, 1).longValue());
    }
}