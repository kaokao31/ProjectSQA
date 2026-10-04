package org.apache.commons.math.fraction;

import java.math.BigDecimal;
import java.math.BigInteger;
import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.ConvergenceException;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class BigFractionTest {

    @Test
    public void testConstants() {
        assertEquals(BigInteger.ZERO, BigFraction.ZERO.getNumerator());
        assertEquals(BigInteger.ONE, BigFraction.ZERO.getDenominator());

        assertEquals(BigInteger.ONE, BigFraction.ONE.getNumerator());
        assertEquals(BigInteger.ONE, BigFraction.ONE.getDenominator());

        assertEquals(BigInteger.valueOf(-1), BigFraction.MINUS_ONE.getNumerator());
        assertEquals(BigInteger.ONE, BigFraction.MINUS_ONE.getDenominator());

        assertEquals(BigInteger.valueOf(2), BigFraction.TWO.getNumerator());
        assertEquals(BigInteger.ONE, BigFraction.TWO.getDenominator());

        assertEquals(BigInteger.ONE, BigFraction.ONE_HALF.getNumerator());
        assertEquals(BigInteger.valueOf(2), BigFraction.ONE_HALF.getDenominator());

        assertEquals(BigInteger.ONE, BigFraction.ONE_THIRD.getNumerator());
        assertEquals(BigInteger.valueOf(3), BigFraction.ONE_THIRD.getDenominator());

        assertEquals(BigInteger.valueOf(2), BigFraction.TWO_THIRDS.getNumerator());
        assertEquals(BigInteger.valueOf(3), BigFraction.TWO_THIRDS.getDenominator());

        assertEquals(BigInteger.ONE, BigFraction.ONE_QUARTER.getNumerator());
        assertEquals(BigInteger.valueOf(4), BigFraction.ONE_QUARTER.getDenominator());

        assertEquals(BigInteger.valueOf(3), BigFraction.THREE_QUARTERS.getNumerator());
        assertEquals(BigInteger.valueOf(4), BigFraction.THREE_QUARTERS.getDenominator());

        assertEquals(BigInteger.ONE, BigFraction.ONE_FIFTH.getNumerator());
        assertEquals(BigInteger.valueOf(5), BigFraction.ONE_FIFTH.getDenominator());

        assertEquals(BigInteger.valueOf(2), BigFraction.TWO_FIFTHS.getNumerator());
        assertEquals(BigInteger.valueOf(5), BigFraction.TWO_FIFTHS.getDenominator());

        assertEquals(BigInteger.valueOf(3), BigFraction.THREE_FIFTHS.getNumerator());
        assertEquals(BigInteger.valueOf(5), BigFraction.THREE_FIFTHS.getDenominator());

        assertEquals(BigInteger.valueOf(4), BigFraction.FOUR_FIFTHS.getNumerator());
        assertEquals(BigInteger.valueOf(5), BigFraction.FOUR_FIFTHS.getDenominator());
    }

    @Test
    public void testConstructorBigInteger() {
        BigFraction f = new BigFraction(BigInteger.valueOf(5));
        assertEquals(BigInteger.valueOf(5), f.getNumerator());
        assertEquals(BigInteger.ONE, f.getDenominator());

        f = new BigFraction(BigInteger.valueOf(-5));
        assertEquals(BigInteger.valueOf(-5), f.getNumerator());
        assertEquals(BigInteger.ONE, f.getDenominator());

        f = new BigFraction(BigInteger.ZERO);
        assertEquals(BigInteger.ZERO, f.getNumerator());
        assertEquals(BigInteger.ONE, f.getDenominator());
    }

    @Test
    public void testConstructorBigIntegerBigInteger() {
        BigFraction f = new BigFraction(BigInteger.valueOf(4), BigInteger.valueOf(6));
        assertEquals(BigInteger.valueOf(2), f.getNumerator());
        assertEquals(BigInteger.valueOf(3), f.getDenominator());

        f = new BigFraction(BigInteger.valueOf(4), BigInteger.valueOf(-6));
        assertEquals(BigInteger.valueOf(-2), f.getNumerator());
        assertEquals(BigInteger.valueOf(3), f.getDenominator());

        f = new BigFraction(BigInteger.valueOf(-4), BigInteger.valueOf(-6));
        assertEquals(BigInteger.valueOf(2), f.getNumerator());
        assertEquals(BigInteger.valueOf(3), f.getDenominator());

        f = new BigFraction(BigInteger.valueOf(0), BigInteger.valueOf(5));
        assertEquals(BigInteger.ZERO, f.getNumerator());
        assertEquals(BigInteger.ONE, f.getDenominator());

        f = new BigFraction(BigInteger.valueOf(0), BigInteger.valueOf(-5));
        assertEquals(BigInteger.ZERO, f.getNumerator());
        assertEquals(BigInteger.ONE, f.getDenominator());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorBigIntegerZeroDenominator() {
        new BigFraction(BigInteger.ONE, BigInteger.ZERO);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullNumerator() {
        new BigFraction(null, BigInteger.ONE);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullDenominator() {
        new BigFraction(BigInteger.ONE, null);
    }

    @Test
    public void testConstructorInt() {
        BigFraction f = new BigFraction(7);
        assertEquals(BigInteger.valueOf(7), f.getNumerator());
        assertEquals(BigInteger.ONE, f.getDenominator());

        f = new BigFraction(-7);
        assertEquals(BigInteger.valueOf(-7), f.getNumerator());
        assertEquals(BigInteger.ONE, f.getDenominator());

        f = new BigFraction(0);
        assertEquals(BigInteger.ZERO, f.getNumerator());
        assertEquals(BigInteger.ONE, f.getDenominator());
    }

    @Test
    public void testConstructorIntInt() {
        BigFraction f = new BigFraction(6, 8);
        assertEquals(BigInteger.valueOf(3), f.getNumerator());
        assertEquals(BigInteger.valueOf(4), f.getDenominator());

        f = new BigFraction(6, -8);
        assertEquals(BigInteger.valueOf(-3), f.getNumerator());
        assertEquals(BigInteger.valueOf(4), f.getDenominator());

        f = new BigFraction(-6, -8);
        assertEquals(BigInteger.valueOf(3), f.getNumerator());
        assertEquals(BigInteger.valueOf(4), f.getDenominator());

        f = new BigFraction(0, 5);
        assertEquals(BigInteger.ZERO, f.getNumerator());
        assertEquals(BigInteger.ONE, f.getDenominator());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorIntZeroDenominator() {
        new BigFraction(1, 0);
    }

    @Test
    public void testConstructorLong() {
        BigFraction f = new BigFraction(1234567890123L);
        assertEquals(BigInteger.valueOf(1234567890123L), f.getNumerator());
        assertEquals(BigInteger.ONE, f.getDenominator());

        f = new BigFraction(-1234567890123L);
        assertEquals(BigInteger.valueOf(-1234567890123L), f.getNumerator());
        assertEquals(BigInteger.ONE, f.getDenominator());
    }

    @Test
    public void testConstructorLongLong() {
        BigFraction f = new BigFraction(10L, 15L);
        assertEquals(BigInteger.valueOf(2), f.getNumerator());
        assertEquals(BigInteger.valueOf(3), f.getDenominator());

        f = new BigFraction(10L, -15L);
        assertEquals(BigInteger.valueOf(-2), f.getNumerator());
        assertEquals(BigInteger.valueOf(3), f.getDenominator());

        f = new BigFraction(-10L, -15L);
        assertEquals(BigInteger.valueOf(2), f.getNumerator());
        assertEquals(BigInteger.valueOf(3), f.getDenominator());

        f = new BigFraction(0L, 10L);
        assertEquals(BigInteger.ZERO, f.getNumerator());
        assertEquals(BigInteger.ONE, f.getDenominator());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorLongZeroDenominator() {
        new BigFraction(1L, 0L);
    }

    @Test
    public void testConstructorDouble() throws ConvergenceException {
        BigFraction f = new BigFraction(0.5);
        assertEquals(BigInteger.ONE, f.getNumerator());
        assertEquals(BigInteger.valueOf(2), f.getDenominator());

        f = new BigFraction(-0.5);
        assertEquals(BigInteger.valueOf(-1), f.getNumerator());
        assertEquals(BigInteger.valueOf(2), f.getDenominator());

        f = new BigFraction(0.0);
        assertEquals(BigInteger.ZERO, f.getNumerator());
        assertEquals(BigInteger.ONE, f.getDenominator());

        f = new BigFraction(2.0);
        assertEquals(BigInteger.valueOf(2), f.getNumerator());
        assertEquals(BigInteger.ONE, f.getDenominator());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorDoubleNaN() throws ConvergenceException {
        new BigFraction(Double.NaN);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorDoublePositiveInfinity() throws ConvergenceException {
        new BigFraction(Double.POSITIVE_INFINITY);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorDoubleNegativeInfinity() throws ConvergenceException {
        new BigFraction(Double.NEGATIVE_INFINITY);
    }

    @Test
    public void testConstructorDoubleMaxDenominator() throws ConvergenceException {
        BigFraction f = new BigFraction(0.3333333333333333, 10);
        assertEquals(BigInteger.ONE, f.getNumerator());
        assertEquals(BigInteger.valueOf(3), f.getDenominator());

        f = new BigFraction(0.3333333333333333, 1.0e-5, 100);
        assertEquals(BigInteger.ONE, f.getNumerator());
        assertEquals(BigInteger.valueOf(3), f.getDenominator());

        f = new BigFraction(1.0e-15, 1.0e-10, 10);
        assertEquals(BigInteger.ZERO, f.getNumerator());
        assertEquals(BigInteger.ONE, f.getDenominator());
    }

    @Test(expected = FractionConversionException.class)
    public void testConstructorDoubleConvergenceException() throws ConvergenceException {
        new BigFraction(0.3333333333333333, 1.0e-15, 1);
    }

    @Test(expected = FractionConversionException.class)
    public void testConstructorDoubleMaxDenominatorException() throws ConvergenceException {
        new BigFraction(Double.MAX_VALUE, 10);
    }

    @Test
    public void testAbs() {
        BigFraction f1 = new BigFraction(2, 3);
        assertEquals(f1, f1.abs());

        BigFraction f2 = new BigFraction(-2, 3);
        assertEquals(f1, f2.abs());

        BigFraction f3 = new BigFraction(0, 1);
        assertEquals(f3, f3.abs());
    }

    @Test
    public void testAddBigFraction() {
        BigFraction f1 = new BigFraction(1, 2);
        BigFraction f2 = new BigFraction(1, 3);
        BigFraction result = f1.add(f2);
        assertEquals(BigInteger.valueOf(5), result.getNumerator());
        assertEquals(BigInteger.valueOf(6), result.getDenominator());

        result = f1.add(BigFraction.ZERO);
        assertEquals(f1, result);

        result = BigFraction.ZERO.add(f1);
        assertEquals(f1, result);

        BigFraction f3 = new BigFraction(-1, 2);
        result = f1.add(f3);
        assertEquals(BigFraction.ZERO, result);
    }

    @Test(expected = NullPointerException.class)
    public void testAddNullBigFraction() {
        BigFraction f = new BigFraction(1, 2);
        f.add((BigFraction) null);
    }

    @Test
    public void testAddBigInteger() {
        BigFraction f = new BigFraction(1, 2);
        BigFraction result = f.add(BigInteger.valueOf(2));
        assertEquals(BigInteger.valueOf(5), result.getNumerator());
        assertEquals(BigInteger.valueOf(2), result.getDenominator());

        result = f.add(BigInteger.ZERO);
        assertEquals(f, result);
    }

    @Test(expected = NullPointerException.class)
    public void testAddNullBigInteger() {
        BigFraction f = new BigFraction(1, 2);
        f.add((BigInteger) null);
    }

    @Test
    public void testAddIntAndLong() {
        BigFraction f = new BigFraction(1, 2);
        BigFraction result = f.add(2);
        assertEquals(BigInteger.valueOf(5), result.getNumerator());
        assertEquals(BigInteger.valueOf(2), result.getDenominator());

        result = f.add(2L);
        assertEquals(BigInteger.valueOf(5), result.getNumerator());
        assertEquals(BigInteger.valueOf(2), result.getDenominator());
    }

    @Test
    public void testSubtractBigFraction() {
        BigFraction f1 = new BigFraction(1, 2);
        BigFraction f2 = new BigFraction(1, 3);
        BigFraction result = f1.subtract(f2);
        assertEquals(BigInteger.ONE, result.getNumerator());
        assertEquals(BigInteger.valueOf(6), result.getDenominator());

        result = f1.subtract(BigFraction.ZERO);
        assertEquals(f1, result);

        result = BigFraction.ZERO.subtract(f1);
        assertEquals(f1.negate(), result);
    }

    @Test(expected = NullPointerException.class)
    public void testSubtractNullBigFraction() {
        BigFraction f = new BigFraction(1, 2);
        f.subtract((BigFraction) null);
    }

    @Test
    public void testSubtractBigInteger() {
        BigFraction f = new BigFraction(5, 2);
        BigFraction result = f.subtract(BigInteger.valueOf(2));
        assertEquals(BigInteger.ONE, result.getNumerator());
        assertEquals(BigInteger.valueOf(2), result.getDenominator());

        result = f.subtract(BigInteger.ZERO);
        assertEquals(f, result);
    }

    @Test(expected = NullPointerException.class)
    public void testSubtractNullBigInteger() {
        BigFraction f = new BigFraction(1, 2);
        f.subtract((BigInteger) null);
    }

    @Test
    public void testSubtractIntAndLong() {
        BigFraction f = new BigFraction(5, 2);
        BigFraction result = f.subtract(2);
        assertEquals(BigInteger.ONE, result.getNumerator());
        assertEquals(BigInteger.valueOf(2), result.getDenominator());

        result = f.subtract(2L);
        assertEquals(BigInteger.ONE, result.getNumerator());
        assertEquals(BigInteger.valueOf(2), result.getDenominator());
    }

    @Test
    public void testMultiplyBigFraction() {
        BigFraction f1 = new BigFraction(2, 3);
        BigFraction f2 = new BigFraction(3, 4);
        BigFraction result = f1.multiply(f2);
        assertEquals(BigInteger.ONE, result.getNumerator());
        assertEquals(BigInteger.valueOf(2), result.getDenominator());

        result = f1.multiply(BigFraction.ZERO);
        assertEquals(BigFraction.ZERO, result);

        result = BigFraction.ZERO.multiply(f1);
        assertEquals(BigFraction.ZERO, result);

        result = f1.multiply(BigFraction.ONE);
        assertEquals(f1, result);
    }

    @Test(expected = NullPointerException.class)
    public void testMultiplyNullBigFraction() {
        BigFraction f = new BigFraction(1, 2);
        f.multiply((BigFraction) null);
    }

    @Test
    public void testMultiplyBigInteger() {
        BigFraction f = new BigFraction(2, 3);
        BigFraction result = f.multiply(BigInteger.valueOf(3));
        assertEquals(BigInteger.valueOf(2), result.getNumerator());
        assertEquals(BigInteger.ONE, result.getDenominator());

        result = f.multiply(BigInteger.ZERO);
        assertEquals(BigFraction.ZERO, result);
    }

    @Test(expected = NullPointerException.class)
    public void testMultiplyNullBigInteger() {
        BigFraction f = new BigFraction(1, 2);
        f.multiply((BigInteger) null);
    }

    @Test
    public void testMultiplyIntAndLong() {
        BigFraction f = new BigFraction(2, 3);
        BigFraction result = f.multiply(3);
        assertEquals(BigInteger.valueOf(2), result.getNumerator());
        assertEquals(BigInteger.ONE, result.getDenominator());

        result = f.multiply(3L);
        assertEquals(BigInteger.valueOf(2), result.getNumerator());
        assertEquals(BigInteger.ONE, result.getDenominator());

        result = f.multiply(0);
        assertEquals(BigFraction.ZERO, result);

        result = f.multiply(0L);
        assertEquals(BigFraction.ZERO, result);
    }

    @Test
    public void testDivideBigFraction() {
        BigFraction f1 = new BigFraction(1, 2);
        BigFraction f2 = new BigFraction(2, 3);
        BigFraction result = f1.divide(f2);
        assertEquals(BigInteger.valueOf(3), result.getNumerator());
        assertEquals(BigInteger.valueOf(4), result.getDenominator());

        result = BigFraction.ZERO.divide(f1);
        assertEquals(BigFraction.ZERO, result);

        result = f1.divide(BigFraction.ONE);
        assertEquals(f1, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDivideByZeroFraction() {
        BigFraction f1 = new BigFraction(1, 2);
        f1.divide(BigFraction.ZERO);
    }

    @Test(expected = NullPointerException.class)
    public void testDivideNullBigFraction() {
        BigFraction f = new BigFraction(1, 2);
        f.divide((BigFraction) null);
    }

    @Test
    public void testDivideBigInteger() {
        BigFraction f = new BigFraction(2, 3);
        BigFraction result = f.divide(BigInteger.valueOf(2));
        assertEquals(BigInteger.ONE, result.getNumerator());
        assertEquals(BigInteger.valueOf(3), result.getDenominator());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDivideByZeroBigInteger() {
        BigFraction f = new BigFraction(1, 2);
        f.divide(BigInteger.ZERO);
    }

    @Test(expected = NullPointerException.class)
    public void testDivideNullBigInteger() {
        BigFraction f = new BigFraction(1, 2);
        f.divide((BigInteger) null);
    }

    @Test
    public void testDivideIntAndLong() {
        BigFraction f = new BigFraction(2, 3);
        BigFraction result = f.divide(2);
        assertEquals(BigInteger.ONE, result.getNumerator());
        assertEquals(BigInteger.valueOf(3), result.getDenominator());

        result = f.divide(2L);
        assertEquals(BigInteger.ONE, result.getNumerator());
        assertEquals(BigInteger.valueOf(3), result.getDenominator());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDivideByZeroInt() {
        BigFraction f = new BigFraction(1, 2);
        f.divide(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDivideByZeroLong() {
        BigFraction f = new BigFraction(1, 2);
        f.divide(0L);
    }

    @Test
    public void testNegate() {
        BigFraction f1 = new BigFraction(2, 3);
        assertEquals(new BigFraction(-2, 3), f1.negate());

        BigFraction f2 = new BigFraction(-2, 3);
        assertEquals(f1, f2.negate());

        assertEquals(BigFraction.ZERO, BigFraction.ZERO.negate());
    }

    @Test
    public void testReciprocal() {
        BigFraction f = new BigFraction(2, 3);
        assertEquals(new BigFraction(3, 2), f.reciprocal());

        f = new BigFraction(-2, 3);
        assertEquals(new BigFraction(-3, 2), f.reciprocal());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReciprocalZero() {
        BigFraction.ZERO.reciprocal();
    }

    @Test
    public void testPowInt() {
        BigFraction f = new BigFraction(2, 3);
        assertEquals(new BigFraction(8, 27), f.pow(3));
        assertEquals(BigFraction.ONE, f.pow(0));
        assertEquals(new BigFraction(27, 8), f.pow(-3));

        BigFraction zero = BigFraction.ZERO;
        assertEquals(BigFraction.ZERO, zero.pow(3));
        assertEquals(BigFraction.ONE, zero.pow(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPowZeroNegativeExponent() {
        BigFraction.ZERO.pow(-1);
    }

    @Test
    public void testPowLong() {
        BigFraction f = new BigFraction(2, 3);
        assertEquals(new BigFraction(8, 27), f.pow(3L));
        assertEquals(BigFraction.ONE, f.pow(0L));
        assertEquals(new BigFraction(27, 8), f.pow(-3L));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPowLongZeroNegativeExponent() {
        BigFraction.ZERO.pow(-1L);
    }

    @Test
    public void testPowBigInteger() {
        BigFraction f = new BigFraction(2, 3);
        assertEquals(new BigFraction(8, 27), f.pow(BigInteger.valueOf(3)));
        assertEquals(BigFraction.ONE, f.pow(BigInteger.ZERO));
        assertEquals(new BigFraction(27, 8), f.pow(BigInteger.valueOf(-3)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPowBigIntegerZeroNegativeExponent() {
        BigFraction.ZERO.pow(BigInteger.valueOf(-1));
    }

    @Test
    public void testPowDouble() {
        BigFraction f = new BigFraction(4, 9);
        assertEquals(2.0 / 3.0, f.pow(0.5), 1.0e-10);
        assertEquals(1.5, f.pow(-0.5), 1.0e-10);
    }

    @Test
    public void testPrimitiveValues() {
        BigFraction f = new BigFraction(7, 2);
        assertEquals(3.5, f.doubleValue(), 1.0e-10);
        assertEquals(3.5f, f.floatValue(), 1.0e-10f);
        assertEquals(3, f.intValue());
        assertEquals(3L, f.longValue());

        assertEquals(7, f.getNumeratorAsInt());
        assertEquals(2, f.getDenominatorAsInt());
        assertEquals(7L, f.getNumeratorAsLong());
        assertEquals(2L, f.getDenominatorAsLong());

        BigFraction large = new BigFraction(BigInteger.valueOf(Long.MAX_VALUE).multiply(BigInteger.valueOf(2)), BigInteger.ONE);
        assertEquals(0, (large.getNumeratorAsInt() != 0 ? 0 : 0)); // Validates invocation without throwing
        assertNotNull(large.getNumerator());
        assertNotNull(large.getDenominator());
    }

    @Test
    public void testBigDecimalValue() {
        BigFraction f = new BigFraction(1, 2);
        assertEquals(new BigDecimal("0.5"), f.bigDecimalValue());

        BigFraction f2 = new BigFraction(1, 3);
        assertEquals(new BigDecimal("0.33"), f2.bigDecimalValue(2, BigDecimal.ROUND_HALF_UP));
        assertEquals(new BigDecimal("0.33333"), f2.bigDecimalValue(5, BigDecimal.ROUND_DOWN));
        assertEquals(new BigDecimal("0.33"), f2.bigDecimalValue(2, BigDecimal.ROUND_HALF_UP));
    }

    @Test
    public void testCompareTo() {
        BigFraction f1 = new BigFraction(1, 2);
        BigFraction f2 = new BigFraction(2, 3);
        BigFraction f3 = new BigFraction(2, 4);

        assertTrue(f1.compareTo(f2) < 0);
        assertTrue(f2.compareTo(f1) > 0);
        assertEquals(0, f1.compareTo(f3));

        BigFraction neg = new BigFraction(-1, 2);
        assertTrue(neg.compareTo(f1) < 0);
        assertTrue(f1.compareTo(neg) > 0);
    }

    @Test
    public void testEqualsAndHashCode() {
        BigFraction f1 = new BigFraction(1, 2);
        BigFraction f2 = new BigFraction(2, 4);
        BigFraction f3 = new BigFraction(1, 3);

        assertTrue(f1.equals(f1));
        assertTrue(f1.equals(f2));
        assertFalse(f1.equals(f3));
        assertFalse(f1.equals(null));
        assertFalse(f1.equals("Not a fraction"));

        assertEquals(f1.hashCode(), f2.hashCode());
    }

    @Test
    public void testToString() {
        BigFraction f1 = new BigFraction(1, 2);
        assertEquals("1 / 2", f1.toString());

        BigFraction f2 = new BigFraction(5);
        assertEquals("5", f2.toString());

        BigFraction f3 = new BigFraction(0);
        assertEquals("0", f3.toString());

        BigFraction f4 = new BigFraction(-3, 4);
        assertEquals("-3 / 4", f4.toString());
    }

    @Test
    public void testReduce() {
        BigFraction f = new BigFraction(4, 6);
        BigFraction reduced = f.reduce();
        assertEquals(BigInteger.valueOf(2), reduced.getNumerator());
        assertEquals(BigInteger.valueOf(3), reduced.getDenominator());

        BigFraction zero = new BigFraction(0, 5);
        assertEquals(BigFraction.ZERO, zero.reduce());
    }

    @Test
    public void testGetReducedFraction() {
        BigFraction f = BigFraction.getReducedFraction(6, 8);
        assertEquals(BigInteger.valueOf(3), f.getNumerator());
        assertEquals(BigInteger.valueOf(4), f.getDenominator());

        f = BigFraction.getReducedFraction(0, 5);
        assertEquals(BigFraction.ZERO, f);

        f = BigFraction.getReducedFraction(6, -8);
        assertEquals(BigInteger.valueOf(-3), f.getNumerator());
        assertEquals(BigInteger.valueOf(4), f.getDenominator());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetReducedFractionZeroDenominator() {
        BigFraction.getReducedFraction(1, 0);
    }

    @Test
    public void testFractionConversionEdgeCases() {
        try {
            new BigFraction(Double.NaN, 1.0e-5, 100);
            fail("Expected MathIllegalArgumentException/IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }

        try {
            new BigFraction(Double.POSITIVE_INFINITY, 1.0e-5, 100);
            fail("Expected MathIllegalArgumentException/IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }

        try {
            new BigFraction(Double.NEGATIVE_INFINITY, 1.0e-5, 100);
            fail("Expected MathIllegalArgumentException/IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
    }
}