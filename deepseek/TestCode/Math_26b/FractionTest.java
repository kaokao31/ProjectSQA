package org.apache.commons.math3.fraction;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

/**
 * Comprehensive JUnit 4 test suite for the Fraction class.
 * Designed to achieve high code coverage and detect potential faults,
 * especially integer overflow issues (Defects4J Math-26).
 */
public class FractionTest {

    private Fraction zero;
    private Fraction oneHalf;
    private Fraction oneThird;
    private Fraction negativeTwoThirds;
    private Fraction largeFraction;
    private Fraction minValueFraction;

    @Before
    public void setUp() {
        zero = new Fraction(0, 1);
        oneHalf = new Fraction(1, 2);
        oneThird = new Fraction(1, 3);
        negativeTwoThirds = new Fraction(-2, 3);
        largeFraction = new Fraction(Integer.MAX_VALUE, 1);
        minValueFraction = new Fraction(Integer.MIN_VALUE, 1);
    }

    // ========== Constructor Tests ==========

    @Test(expected = ArithmeticException.class)
    public void testConstructorZeroDenominator() {
        new Fraction(1, 0);
    }

    @Test(expected = ArithmeticException.class)
    public void testConstructorZeroDenominatorNegative() {
        new Fraction(-1, 0);
    }

    @Test
    public void testConstructorNormalization() {
        Fraction f = new Fraction(4, 6);
        assertEquals(2, f.getNumerator());
        assertEquals(3, f.getDenominator());
    }

    @Test
    public void testConstructorNegativeDenominator() {
        Fraction f = new Fraction(3, -5);
        assertEquals(-3, f.getNumerator());
        assertEquals(5, f.getDenominator());
    }

    @Test
    public void testConstructorBothNegative() {
        Fraction f = new Fraction(-3, -5);
        assertEquals(3, f.getNumerator());
        assertEquals(5, f.getDenominator());
    }

    @Test
    public void testConstructorInt() {
        Fraction f = new Fraction(5);
        assertEquals(5, f.getNumerator());
        assertEquals(1, f.getDenominator());
    }

    @Test
    public void testConstructorIntZero() {
        Fraction f = new Fraction(0);
        assertEquals(0, f.getNumerator());
        assertEquals(1, f.getDenominator());
    }

    @Test
    public void testConstructorDoubleExact() {
        Fraction f = new Fraction(0.5);
        assertEquals(1, f.getNumerator());
        assertEquals(2, f.getDenominator());
    }

    @Test(expected = ArithmeticException.class)
    public void testConstructorDoubleNaN() {
        new Fraction(Double.NaN);
    }

    @Test(expected = ArithmeticException.class)
    public void testConstructorDoubleInfinity() {
        new Fraction(Double.POSITIVE_INFINITY);
    }

    @Test(expected = ArithmeticException.class)
    public void testConstructorDoubleNegativeInfinity() {
        new Fraction(Double.NEGATIVE_INFINITY);
    }

    @Test
    public void testConstructorDoubleWithMaxDenominator() {
        Fraction f = new Fraction(0.3333333, 1000);
        assertEquals(1, f.getNumerator());
        assertEquals(3, f.getDenominator());
    }

    @Test(expected = ArithmeticException.class)
    public void testConstructorDoubleWithMaxDenominatorZero() {
        new Fraction(0.5, 0);
    }

    // ========== Arithmetic Tests ==========

    @Test
    public void testAddSimple() {
        Fraction result = oneHalf.add(oneThird);
        assertEquals(5, result.getNumerator());
        assertEquals(6, result.getDenominator());
    }

    @Test
    public void testAddNegative() {
        Fraction result = oneHalf.add(negativeTwoThirds);
        assertEquals(-1, result.getNumerator());
        assertEquals(6, result.getDenominator());
    }

    @Test
    public void testAddZero() {
        Fraction result = oneHalf.add(zero);
        assertSame(oneHalf, result); // should return the same instance
    }

    @Test
    public void testAddOverflow() {
        // This should trigger overflow if not using long arithmetic
        Fraction f1 = new Fraction(Integer.MAX_VALUE, 1);
        Fraction f2 = new Fraction(1, 1);
        Fraction result = f1.add(f2);
        // Expected: (MAX_VALUE + 1) / 1 = Integer.MIN_VALUE due to overflow
        // But correct implementation should handle overflow by throwing or using long
        // The bug in Math-26 is that it does not handle overflow, so result may be wrong.
        // We assert that the result is not simply Integer.MIN_VALUE (which would be overflow)
        // Actually, the fixed version should throw ArithmeticException or produce correct result.
        // Since we don't know the exact fix, we just test that it doesn't silently overflow.
        // We'll check that numerator is not Integer.MIN_VALUE (which would be overflow)
        assertTrue("Overflow should not produce Integer.MIN_VALUE", result.getNumerator() != Integer.MIN_VALUE);
    }

    @Test(expected = ArithmeticException.class)
    public void testAddOverflowThrows() {
        Fraction f1 = new Fraction(Integer.MAX_VALUE, 1);
        Fraction f2 = new Fraction(1, 1);
        f1.add(f2); // Should throw ArithmeticException if overflow is detected
    }

    @Test
    public void testSubtractSimple() {
        Fraction result = oneHalf.subtract(oneThird);
        assertEquals(1, result.getNumerator());
        assertEquals(6, result.getDenominator());
    }

    @Test
    public void testSubtractNegative() {
        Fraction result = oneHalf.subtract(negativeTwoThirds);
        assertEquals(7, result.getNumerator());
        assertEquals(6, result.getDenominator());
    }

    @Test
    public void testSubtractZero() {
        Fraction result = oneHalf.subtract(zero);
        assertSame(oneHalf, result);
    }

    @Test
    public void testSubtractOverflow() {
        Fraction f1 = new Fraction(Integer.MIN_VALUE, 1);
        Fraction f2 = new Fraction(1, 1);
        Fraction result = f1.subtract(f2);
        assertTrue("Overflow should not produce Integer.MAX_VALUE", result.getNumerator() != Integer.MAX_VALUE);
    }

    @Test(expected = ArithmeticException.class)
    public void testSubtractOverflowThrows() {
        Fraction f1 = new Fraction(Integer.MIN_VALUE, 1);
        Fraction f2 = new Fraction(1, 1);
        f1.subtract(f2);
    }

    @Test
    public void testMultiplySimple() {
        Fraction result = oneHalf.multiply(oneThird);
        assertEquals(1, result.getNumerator());
        assertEquals(6, result.getDenominator());
    }

    @Test
    public void testMultiplyNegative() {
        Fraction result = oneHalf.multiply(negativeTwoThirds);
        assertEquals(-2, result.getNumerator());
        assertEquals(6, result.getDenominator());
    }

    @Test
    public void testMultiplyZero() {
        Fraction result = oneHalf.multiply(zero);
        assertEquals(0, result.getNumerator());
        assertEquals(1, result.getDenominator());
    }

    @Test
    public void testMultiplyOverflow() {
        Fraction f1 = new Fraction(Integer.MAX_VALUE, 1);
        Fraction f2 = new Fraction(2, 1);
        Fraction result = f1.multiply(f2);
        assertTrue("Overflow should not produce -2", result.getNumerator() != -2);
    }

    @Test(expected = ArithmeticException.class)
    public void testMultiplyOverflowThrows() {
        Fraction f1 = new Fraction(Integer.MAX_VALUE, 1);
        Fraction f2 = new Fraction(2, 1);
        f1.multiply(f2);
    }

    @Test
    public void testDivideSimple() {
        Fraction result = oneHalf.divide(oneThird);
        assertEquals(3, result.getNumerator());
        assertEquals(2, result.getDenominator());
    }

    @Test(expected = ArithmeticException.class)
    public void testDivideByZero() {
        oneHalf.divide(zero);
    }

    @Test
    public void testDivideNegative() {
        Fraction result = oneHalf.divide(negativeTwoThirds);
        assertEquals(-3, result.getNumerator());
        assertEquals(4, result.getDenominator());
    }

    @Test
    public void testDivideOverflow() {
        Fraction f1 = new Fraction(Integer.MAX_VALUE, 1);
        Fraction f2 = new Fraction(1, 2);
        Fraction result = f1.divide(f2);
        assertTrue("Overflow should not produce -2", result.getNumerator() != -2);
    }

    @Test(expected = ArithmeticException.class)
    public void testDivideOverflowThrows() {
        Fraction f1 = new Fraction(Integer.MAX_VALUE, 1);
        Fraction f2 = new Fraction(1, 2);
        f1.divide(f2);
    }

    @Test
    public void testNegate() {
        Fraction result = oneHalf.negate();
        assertEquals(-1, result.getNumerator());
        assertEquals(2, result.getDenominator());
    }

    @Test
    public void testNegateZero() {
        Fraction result = zero.negate();
        assertEquals(0, result.getNumerator());
        assertEquals(1, result.getDenominator());
    }

    @Test
    public void testNegateMinValue() {
        // Integer.MIN_VALUE negate overflows to itself
        Fraction result = minValueFraction.negate();
        // The correct behavior should throw or handle overflow
        // We check that it doesn't silently produce the same value
        assertTrue("Negate of MIN_VALUE should not be MIN_VALUE", result.getNumerator() != Integer.MIN_VALUE);
    }

    @Test(expected = ArithmeticException.class)
    public void testNegateMinValueThrows() {
        minValueFraction.negate();
    }

    @Test
    public void testAbsPositive() {
        Fraction result = oneHalf.abs();
        assertSame(oneHalf, result);
    }

    @Test
    public void testAbsNegative() {
        Fraction result = negativeTwoThirds.abs();
        assertEquals(2, result.getNumerator());
        assertEquals(3, result.getDenominator());
    }

    @Test
    public void testAbsZero() {
        Fraction result = zero.abs();
        assertSame(zero, result);
    }

    @Test
    public void testAbsMinValue() {
        Fraction result = minValueFraction.abs();
        assertTrue("Abs of MIN_VALUE should not be MIN_VALUE", result.getNumerator() != Integer.MIN_VALUE);
    }

    @Test(expected = ArithmeticException.class)
    public void testAbsMinValueThrows() {
        minValueFraction.abs();
    }

    @Test
    public void testReciprocal() {
        Fraction result = oneHalf.reciprocal();
        assertEquals(2, result.getNumerator());
        assertEquals(1, result.getDenominator());
    }

    @Test(expected = ArithmeticException.class)
    public void testReciprocalZero() {
        zero.reciprocal();
    }

    @Test
    public void testReciprocalNegative() {
        Fraction result = negativeTwoThirds.reciprocal();
        assertEquals(-3, result.getNumerator());
        assertEquals(2, result.getDenominator());
    }

    // ========== Comparison Tests ==========

    @Test
    public void testCompareToEqual() {
        assertEquals(0, oneHalf.compareTo(new Fraction(1, 2)));
    }

    @Test
    public void testCompareToLess() {
        assertTrue(oneHalf.compareTo(oneThird) > 0);
    }

    @Test
    public void testCompareToGreater() {
        assertTrue(oneThird.compareTo(oneHalf) < 0);
    }

    @Test
    public void testCompareToNegative() {
        assertTrue(negativeTwoThirds.compareTo(zero) < 0);
    }

    @Test(expected = NullPointerException.class)
    public void testCompareToNull() {
        oneHalf.compareTo(null);
    }

    @Test
    public void testEqualsSame() {
        assertTrue(oneHalf.equals(oneHalf));
    }

    @Test
    public void testEqualsEqual() {
        assertTrue(oneHalf.equals(new Fraction(1, 2)));
    }

    @Test
    public void testEqualsDifferent() {
        assertFalse(oneHalf.equals(oneThird));
    }

    @Test
    public void testEqualsNull() {
        assertFalse(oneHalf.equals(null));
    }

    @Test
    public void testEqualsDifferentObject() {
        assertFalse(oneHalf.equals("string"));
    }

    @Test
    public void testHashCodeConsistency() {
        Fraction f1 = new Fraction(1, 2);
        Fraction f2 = new Fraction(1, 2);
        assertEquals(f1.hashCode(), f2.hashCode());
    }

    @Test
    public void testHashCodeDifferent() {
        assertNotEquals(oneHalf.hashCode(), oneThird.hashCode());
    }

    // ========== Conversion Tests ==========

    @Test
    public void testDoubleValue() {
        assertEquals(0.5, oneHalf.doubleValue(), 1e-15);
    }

    @Test
    public void testDoubleValueNegative() {
        assertEquals(-2.0/3.0, negativeTwoThirds.doubleValue(), 1e-15);
    }

    @Test
    public void testFloatValue() {
        assertEquals(0.5f, oneHalf.floatValue(), 1e-15f);
    }

    @Test
    public void testPercentageValue() {
        assertEquals(50.0, oneHalf.percentageValue(), 1e-15);
    }

    @Test
    public void testPercentageValueNegative() {
        assertEquals(-200.0/3.0, negativeTwoThirds.percentageValue(), 1e-15);
    }

    @Test
    public void testGetNumerator() {
        assertEquals(1, oneHalf.getNumerator());
    }

    @Test
    public void testGetDenominator() {
        assertEquals(2, oneHalf.getDenominator());
    }

    // ========== Edge Cases and Special Values ==========

    @Test
    public void testFractionWithLargeDenominator() {
        Fraction f = new Fraction(1, Integer.MAX_VALUE);
        assertEquals(1, f.getNumerator());
        assertEquals(Integer.MAX_VALUE, f.getDenominator());
    }

    @Test(expected = ArithmeticException.class)
    public void testFractionWithMinValueDenominator() {
        // Denominator cannot be Integer.MIN_VALUE because normalization would overflow
        new Fraction(1, Integer.MIN_VALUE);
    }

    @Test
    public void testToString() {
        assertEquals("1 / 2", oneHalf.toString());
        assertEquals("-2 / 3", negativeTwoThirds.toString());
        assertEquals("0 / 1", zero.toString());
    }

    @Test
    public void testFieldGetter() {
        assertEquals(org.apache.commons.math3.fraction.Fraction.class, oneHalf.getClass());
    }
}