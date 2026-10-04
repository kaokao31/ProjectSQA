package org.apache.commons.lang.math;

import org.junit.Test;
import static org.junit.Assert.*;

public class FractionTest {

    @Test
    public void testReduce() {
        // This specifically targets the Lang-49 bug where reduce() returns an incorrect
        // numerator/denominator when the fraction is already reduced or has gcd issues
        // related to negative numbers or Integer.MIN_VALUE.
        Fraction f = Fraction.getFraction(2, 200);
        Fraction reduced = f.reduce();
        assertEquals(1, reduced.getNumerator());
        assertEquals(100, reduced.getDenominator());
    }

    @Test
    public void testGetFraction_int_int() {
        Fraction f = Fraction.getFraction(3, 5);
        assertEquals(3, f.getNumerator());
        assertEquals(5, f.getDenominator());
    }

    @Test(expected = ArithmeticException.class)
    public void testGetFraction_zeroDenominator() {
        Fraction.getFraction(1, 0);
    }

    @Test(expected = ArithmeticException.class)
    public void testGetFraction_denominatorMinValue() {
        Fraction.getFraction(1, Integer.MIN_VALUE);
    }

    @Test
    public void testGetFraction_int() {
        Fraction f = Fraction.getFraction(5);
        assertEquals(5, f.getNumerator());
        assertEquals(1, f.getDenominator());
    }

    @Test
    public void testGetFraction_String() {
        Fraction f = Fraction.getFraction("2/3");
        assertEquals(2, f.getNumerator());
        assertEquals(3, f.getDenominator());

        Fraction f2 = Fraction.getFraction("1 2/3");
        assertEquals(5, f2.getNumerator());
        assertEquals(3, f2.getDenominator());

        Fraction f3 = Fraction.getFraction("3");
        assertEquals(3, f3.getNumerator());
        assertEquals(1, f3.getDenominator());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetFraction_nullString() {
        Fraction.getFraction(null);
    }

    @Test
    public void testGetReducedFraction() {
        Fraction f = Fraction.getReducedFraction(50, 100);
        assertEquals(1, f.getNumerator());
        assertEquals(2, f.getDenominator());

        Fraction fZero = Fraction.getReducedFraction(0, 10);
        assertEquals(0, fZero.getNumerator());
        assertEquals(1, fZero.getDenominator());
    }

    @Test(expected = ArithmeticException.class)
    public void testGetReducedFraction_zeroDenominator() {
        Fraction.getReducedFraction(1, 0);
    }

    @Test
    public void testGetReducedFraction_minValues() {
        Fraction f = Fraction.getReducedFraction(Integer.MIN_VALUE, Integer.MIN_VALUE);
        assertEquals(1, f.getNumerator());
        assertEquals(1, f.getDenominator());
    }

    @Test
    public void testAccessors() {
        Fraction f = Fraction.getFraction(-7, 8);
        assertEquals(-7, f.getNumerator());
        assertEquals(8, f.getDenominator());
        assertEquals(-7, f.getProperNumerator());
        assertEquals(0, f.getProperWhole());
        assertEquals(-0.875, f.doubleValue(), 0.00001);
        assertEquals(-0.875f, f.floatValue(), 0.00001f);
        assertEquals(-7L, f.longValue());
        assertEquals(-7, f.intValue());
    }

    @Test
    public void testArithmetic() {
        Fraction f1 = Fraction.getFraction(1, 2);
        Fraction f2 = Fraction.getFraction(1, 3);

        Fraction add = f1.add(f2);
        assertEquals(5, add.getNumerator());
        assertEquals(6, add.getDenominator());

        Fraction sub = f1.subtract(f2);
        assertEquals(1, sub.getNumerator());
        assertEquals(6, sub.getDenominator());

        Fraction mul = f1.multiplyBy(f2);
        assertEquals(1, mul.getNumerator());
        assertEquals(6, mul.getDenominator());

        Fraction div = f1.divideBy(f2);
        assertEquals(3, div.getNumerator());
        assertEquals(2, div.getDenominator());

        Fraction pow = f1.pow(2);
        assertEquals(1, pow.getNumerator());
        assertEquals(4, pow.getDenominator());

        Fraction abs = Fraction.getFraction(-1, 2).abs();
        assertEquals(1, abs.getNumerator());
        assertEquals(2, abs.getDenominator());

        Fraction inv = f1.invert();
        assertEquals(2, inv.getNumerator());
        assertEquals(1, inv.getDenominator());

        Fraction negate = f1.negate();
        assertEquals(-1, negate.getNumerator());
        assertEquals(2, negate.getDenominator());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAdd_null() {
        Fraction.getFraction(1, 2).add(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSubtract_null() {
        Fraction.getFraction(1, 2).subtract(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMultiplyBy_null() {
        Fraction.getFraction(1, 2).multiplyBy(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDivideBy_null() {
        Fraction.getFraction(1, 2).divideBy(null);
    }

    @Test(expected = ArithmeticException.class)
    public void testDivideBy_zero() {
        Fraction.getFraction(1, 2).divideBy(Fraction.ZERO);
    }

    @Test
    public void testEqualsAndHashCode() {
        Fraction f1 = Fraction.getFraction(1, 2);
        Fraction f2 = Fraction.getFraction(1, 2);
        Fraction f3 = Fraction.getFraction(1, 3);

        assertTrue(f1.equals(f2));
        assertFalse(f1.equals(f3));
        assertFalse(f1.equals(null));
        assertFalse(f1.equals("1/2"));
        assertEquals(f1.hashCode(), f2.hashCode());
    }

    @Test
    public void testCompareTo() {
        Fraction f1 = Fraction.getFraction(1, 3);
        Fraction f2 = Fraction.getFraction(1, 2);
        Fraction f3 = Fraction.getFraction(1, 3);

        assertTrue(f1.compareTo(f2) < 0);
        assertTrue(f2.compareTo(f1) > 0);
        assertEquals(0, f1.compareTo(f3));

        try {
            f1.compareTo(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testToString() {
        Fraction f = Fraction.getFraction(1, 2);
        assertEquals("1/2", f.toString());

        Fraction f2 = Fraction.getFraction(4, 2);
        assertEquals("2", f2.toProperString());
    }
}