package org.apache.commons.math3.fraction;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

/**
 * Test suite for Fraction class (Bug 27).
 * Designed to achieve high coverage and detect overflow-related faults.
 */
public class FractionTest {

    // Test constructor with zero denominator
    @Test(expected = ArithmeticException.class)
    public void testConstructorZeroDenominator() {
        new Fraction(1, 0);
    }

    // Test constructor with zero numerator
    @Test
    public void testConstructorZeroNumerator() {
        Fraction f = new Fraction(0, 5);
        assertEquals(0, f.getNumerator());
        assertEquals(1, f.getDenominator()); // normalized
    }

    // Test constructor normalization: negative denominator
    @Test
    public void testConstructorNegativeDenominator() {
        Fraction f = new Fraction(3, -4);
        assertEquals(-3, f.getNumerator());
        assertEquals(4, f.getDenominator());
    }

    // Test constructor normalization: both negative
    @Test
    public void testConstructorBothNegative() {
        Fraction f = new Fraction(-5, -10);
        assertEquals(1, f.getNumerator());
        assertEquals(2, f.getDenominator());
    }

    // Test constructor reduction
    @Test
    public void testConstructorReduction() {
        Fraction f = new Fraction(6, 8);
        assertEquals(3, f.getNumerator());
        assertEquals(4, f.getDenominator());
    }

    // Test constructor with Integer.MIN_VALUE (overflow edge)
    @Test(expected = ArithmeticException.class)
    public void testConstructorMinValueDenominator() {
        new Fraction(1, Integer.MIN_VALUE);
    }

    @Test(expected = ArithmeticException.class)
    public void testConstructorMinValueNumerator() {
        new Fraction(Integer.MIN_VALUE, 1);
    }

    // Test add: simple
    @Test
    public void testAddSimple() {
        Fraction a = new Fraction(1, 2);
        Fraction b = new Fraction(1, 3);
        Fraction result = a.add(b);
        assertEquals(5, result.getNumerator());
        assertEquals(6, result.getDenominator());
    }

    // Test add: overflow detection (bug 27 scenario)
    @Test(expected = ArithmeticException.class)
    public void testAddOverflow() {
        Fraction a = new Fraction(Integer.MAX_VALUE, 1);
        Fraction b = new Fraction(1, 1);
        a.add(b); // should overflow
    }

    // Test add: negative
    @Test
    public void testAddNegative() {
        Fraction a = new Fraction(-1, 2);
        Fraction b = new Fraction(1, 4);
        Fraction result = a.add(b);
        assertEquals(-1, result.getNumerator());
        assertEquals(4, result.getDenominator());
    }

    // Test subtract: simple
    @Test
    public void testSubtractSimple() {
        Fraction a = new Fraction(3, 4);
        Fraction b = new Fraction(1, 4);
        Fraction result = a.subtract(b);
        assertEquals(1, result.getNumerator());
        assertEquals(2, result.getDenominator());
    }

    // Test subtract: overflow
    @Test(expected = ArithmeticException.class)
    public void testSubtractOverflow() {
        Fraction a = new Fraction(Integer.MIN_VALUE, 1);
        Fraction b = new Fraction(1, 1);
        a.subtract(b); // should overflow
    }

    // Test multiply: simple
    @Test
    public void testMultiplySimple() {
        Fraction a = new Fraction(2, 3);
        Fraction b = new Fraction(3, 4);
        Fraction result = a.multiply(b);
        assertEquals(1, result.getNumerator());
        assertEquals(2, result.getDenominator());
    }

    // Test multiply: overflow
    @Test(expected = ArithmeticException.class)
    public void testMultiplyOverflow() {
        Fraction a = new Fraction(Integer.MAX_VALUE, 1);
        Fraction b = new Fraction(2, 1);
        a.multiply(b);
    }

    // Test multiply: negative
    @Test
    public void testMultiplyNegative() {
        Fraction a = new Fraction(-2, 3);
        Fraction b = new Fraction(3, -4);
        Fraction result = a.multiply(b);
        assertEquals(1, result.getNumerator());
        assertEquals(2, result.getDenominator());
    }

    // Test divide: simple
    @Test
    public void testDivideSimple() {
        Fraction a = new Fraction(1, 2);
        Fraction b = new Fraction(3, 4);
        Fraction result = a.divide(b);
        assertEquals(2, result.getNumerator());
        assertEquals(3, result.getDenominator());
    }

    // Test divide: overflow
    @Test(expected = ArithmeticException.class)
    public void testDivideOverflow() {
        Fraction a = new Fraction(1, Integer.MAX_VALUE);
        Fraction b = new Fraction(1, 2);
        a.divide(b); // multiplication overflow
    }

    // Test divide by zero
    @Test(expected = ArithmeticException.class)
    public void testDivideByZero() {
        Fraction a = new Fraction(1, 2);
        Fraction b = new Fraction(0, 1);
        a.divide(b);
    }

    // Test abs
    @Test
    public void testAbsPositive() {
        Fraction f = new Fraction(3, 4);
        assertSame(f, f.abs()); // same instance if already positive
    }

    @Test
    public void testAbsNegative() {
        Fraction f = new Fraction(-3, 4);
        Fraction abs = f.abs();
        assertEquals(3, abs.getNumerator());
        assertEquals(4, abs.getDenominator());
    }

    @Test
    public void testAbsZero() {
        Fraction f = new Fraction(0, 1);
        assertSame(f, f.abs());
    }

    // Test negate
    @Test
    public void testNegate() {
        Fraction f = new Fraction(3, 4);
        Fraction neg = f.negate();
        assertEquals(-3, neg.getNumerator());
        assertEquals(4, neg.getDenominator());
    }

    @Test
    public void testNegateNegative() {
        Fraction f = new Fraction(-3, 4);
        Fraction neg = f.negate();
        assertEquals(3, neg.getNumerator());
        assertEquals(4, neg.getDenominator());
    }

    // Test reciprocal
    @Test
    public void testReciprocal() {
        Fraction f = new Fraction(3, 4);
        Fraction rec = f.reciprocal();
        assertEquals(4, rec.getNumerator());
        assertEquals(3, rec.getDenominator());
    }

    @Test(expected = ArithmeticException.class)
    public void testReciprocalZero() {
        Fraction f = new Fraction(0, 1);
        f.reciprocal();
    }

    // Test equals and hashCode
    @Test
    public void testEqualsSame() {
        Fraction a = new Fraction(1, 2);
        Fraction b = new Fraction(1, 2);
        assertTrue(a.equals(b));
        assertTrue(b.equals(a));
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void testEqualsDifferent() {
        Fraction a = new Fraction(1, 2);
        Fraction b = new Fraction(2, 4);
        assertTrue(a.equals(b)); // equal after reduction
    }

    @Test
    public void testEqualsNull() {
        Fraction a = new Fraction(1, 2);
        assertFalse(a.equals(null));
    }

    @Test
    public void testEqualsDifferentClass() {
        Fraction a = new Fraction(1, 2);
        assertFalse(a.equals("string"));
    }

    // Test toString
    @Test
    public void testToString() {
        Fraction f = new Fraction(3, 4);
        assertEquals("3 / 4", f.toString());
    }

    @Test
    public void testToStringNegative() {
        Fraction f = new Fraction(-3, 4);
        assertEquals("-3 / 4", f.toString());
    }

    @Test
    public void testToStringInteger() {
        Fraction f = new Fraction(5, 1);
        assertEquals("5", f.toString());
    }

    // Test compareTo
    @Test
    public void testCompareToEqual() {
        Fraction a = new Fraction(1, 2);
        Fraction b = new Fraction(2, 4);
        assertEquals(0, a.compareTo(b));
    }

    @Test
    public void testCompareToLess() {
        Fraction a = new Fraction(1, 4);
        Fraction b = new Fraction(1, 2);
        assertTrue(a.compareTo(b) < 0);
    }

    @Test
    public void testCompareToGreater() {
        Fraction a = new Fraction(3, 4);
        Fraction b = new Fraction(1, 2);
        assertTrue(a.compareTo(b) > 0);
    }

    // Test doubleValue, floatValue, intValue, longValue
    @Test
    public void testDoubleValue() {
        Fraction f = new Fraction(1, 2);
        assertEquals(0.5, f.doubleValue(), 1e-15);
    }

    @Test
    public void testFloatValue() {
        Fraction f = new Fraction(1, 3);
        assertEquals(1.0f/3.0f, f.floatValue(), 1e-7);
    }

    @Test
    public void testIntValue() {
        Fraction f = new Fraction(7, 3);
        assertEquals(2, f.intValue());
    }

    @Test
    public void testLongValue() {
        Fraction f = new Fraction(7, 3);
        assertEquals(2L, f.longValue());
    }

    // Test getNumerator and getDenominator
    @Test
    public void testGetters() {
        Fraction f = new Fraction(4, 6);
        assertEquals(2, f.getNumerator());
        assertEquals(3, f.getDenominator());
    }

    // Test edge: large numbers that do not overflow
    @Test
    public void testLargeButSafe() {
        Fraction a = new Fraction(Integer.MAX_VALUE / 2, 1);
        Fraction b = new Fraction(1, 2);
        Fraction result = a.multiply(b);
        assertEquals(Integer.MAX_VALUE / 2, result.getNumerator());
        assertEquals(2, result.getDenominator());
    }

    // Test edge: subtraction with large numbers
    @Test(expected = ArithmeticException.class)
    public void testSubtractOverflowLarge() {
        Fraction a = new Fraction(Integer.MAX_VALUE, 1);
        Fraction b = new Fraction(-1, 1);
        a.subtract(b); // should overflow
    }

    // Test add with negative denominator
    @Test
    public void testAddWithNegativeDenominator() {
        Fraction a = new Fraction(1, -2);
        Fraction b = new Fraction(1, 3);
        Fraction result = a.add(b);
        assertEquals(-1, result.getNumerator());
        assertEquals(6, result.getDenominator());
    }
}