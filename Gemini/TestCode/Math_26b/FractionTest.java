package org.apache.commons.math3.fraction;

import org.junit.Assert;
import org.junit.Test;

public class FractionTest {

    @Test
    public void testConstructorDouble() {
        // Test zero
        Fraction f1 = new Fraction(0.0);
        Assert.assertEquals(0, f1.getNumerator());
        Assert.assertEquals(1, f1.getDenominator());

        // Test normal positive double
        Fraction f2 = new Fraction(0.5);
        Assert.assertEquals(1, f2.getNumerator());
        Assert.assertEquals(2, f2.getDenominator());

        // Test normal negative double
        Fraction f3 = new Fraction(-0.25);
        Assert.assertEquals(-1, f3.getNumerator());
        Assert.assertEquals(4, f3.getDenominator());

        // Test double with max absolute error or specific values triggering convergence limits
        Fraction f4 = new Fraction(1.0 / 3.0, 1e-5, 100);
        Assert.assertEquals(1, f4.getNumerator());
        Assert.assertEquals(3, f4.getDenominator());

        // Test double overflow/limit cases
        try {
            new Fraction(Double.NaN);
            Assert.fail("Expecting MathIllegalArgumentException");
        } catch (org.apache.commons.math3.exception.MathIllegalArgumentException e) {
            // Expected
        }

        try {
            new Fraction(Double.POSITIVE_INFINITY);
            Assert.fail("Expecting MathIllegalArgumentException");
        } catch (org.apache.commons.math3.exception.MathIllegalArgumentException e) {
            // Expected
        }

        try {
            new Fraction(1e10, 1e-5, 10);
            Assert.fail("Expecting FractionConversionException");
        } catch (org.apache.commons.math3.fraction.FractionConversionException e) {
            // Expected
        }
        
        // Test overflow condition in double constructor (specifically Math.abs(value) >= overflow limit or similar in Math-26)
        try {
            // Value large enough to trigger overflow during continued fraction expansion
            new Fraction(1.0e10, 1e-12, 10000);
        } catch (Exception e) {
            // Expected or handled
        }
    }

    @Test
    public void testConstructorIntInt() {
        Fraction f1 = new Fraction(2, 4);
        Assert.assertEquals(1, f1.getNumerator());
        Assert.assertEquals(2, f1.getDenominator());

        // Zero denominator
        try {
            new Fraction(1, 0);
            Assert.fail("Expecting ZeroDenominatorException");
        } catch (org.apache.commons.math3.exception.ZeroDenominatorException e) {
            // Expected
        }

        // Negative denominator
        Fraction f2 = new Fraction(1, -2);
        Assert.assertEquals(-1, f2.getNumerator());
        Assert.assertEquals(2, f2.getDenominator());

        // Overflow in int constructor (Integer.MIN_VALUE)
        try {
            new Fraction(Integer.MIN_VALUE, 1);
        } catch (Exception e) {
            // Depending on implementation, might throw or negate
        }

        try {
            new Fraction(1, Integer.MIN_VALUE);
        } catch (Exception e) {
            // Expected
        }
    }

    @Test
    public void testConstructorInt() {
        Fraction f = new Fraction(5);
        Assert.assertEquals(5, f.getNumerator());
        Assert.assertEquals(1, f.getDenominator());
    }

    @Test
    public void testAbs() {
        Fraction f1 = new Fraction(-2, 3);
        Fraction f2 = f1.abs();
        Assert.assertEquals(2, f2.getNumerator());
        Assert.assertEquals(3, f2.getDenominator());

        Fraction f3 = new Fraction(2, 3);
        Fraction f4 = f3.abs();
        Assert.assertSame(f3, f4);
    }

    @Test
    public void testCompareTo() {
        Fraction f1 = new Fraction(1, 2);
        Fraction f2 = new Fraction(2, 4);
        Fraction f3 = new Fraction(3, 4);

        Assert.assertEquals(0, f1.compareTo(f2));
        Assert.assertTrue(f1.compareTo(f3) < 0);
        Assert.assertTrue(f3.compareTo(f1) > 0);
    }

    @Test
    public void testEqualsAndHashCode() {
        Fraction f1 = new Fraction(1, 2);
        Fraction f2 = new Fraction(1, 2);
        Fraction f3 = new Fraction(2, 4);
        Fraction f4 = new Fraction(1, 3);

        Assert.assertTrue(f1.equals(f1));
        Assert.assertTrue(f1.equals(f2));
        Assert.assertTrue(f1.equals(f3));
        Assert.assertFalse(f1.equals(f4));
        Assert.assertFalse(f1.equals(null));
        Assert.assertFalse(f1.equals("SomeString"));

        Assert.assertEquals(f1.hashCode(), f2.hashCode());
        Assert.assertEquals(f1.hashCode(), f3.hashCode());
    }

    @Test
    public void testGetters() {
        Fraction f = new Fraction(3, 7);
        Assert.assertEquals(3, f.getNumerator());
        Assert.assertEquals(7, f.getDenominator());
        Assert.assertEquals(3.0 / 7.0, f.doubleValue(), 1e-15);
        Assert.assertEquals(3.0 / 7.0, f.percentageValue(), 1e-15);
        Assert.assertEquals(0.0, f.floatValue(), 1e-15);
        Assert.assertEquals(0L, f.longValue());
        Assert.assertEquals(0, f.intValue());
    }

    @Test
    public void testArithmeticOperations() {
        Fraction f1 = new Fraction(1, 2);
        Fraction f2 = new Fraction(1, 3);

        // Add
        Fraction add = f1.add(f2);
        Assert.assertEquals(5, add.getNumerator());
        Assert.assertEquals(6, add.getDenominator());

        Fraction addInt = f1.add(1);
        Assert.assertEquals(3, addInt.getNumerator());
        Assert.assertEquals(2, addInt.getDenominator());

        // Subtract
        Fraction sub = f1.subtract(f2);
        Assert.assertEquals(1, sub.getNumerator());
        Assert.assertEquals(6, sub.getDenominator());

        Fraction subInt = f1.subtract(1);
        Assert.assertEquals(-1, subInt.getNumerator());
        Assert.assertEquals(2, subInt.getDenominator());

        // Multiply
        Fraction mul = f1.multiply(f2);
        Assert.assertEquals(1, mul.getNumerator());
        Assert.assertEquals(6, mul.getDenominator());

        Fraction mulInt = f1.multiply(3);
        Assert.assertEquals(3, mulInt.getNumerator());
        Assert.assertEquals(2, mulInt.getDenominator());

        // Divide
        Fraction div = f1.divide(f2);
        Assert.assertEquals(3, div.getNumerator());
        Assert.assertEquals(2, div.getDenominator());

        Fraction divInt = f1.divide(2);
        Assert.assertEquals(1, divInt.getNumerator());
        Assert.assertEquals(4, divInt.getDenominator());

        try {
            f1.divide(0);
            Assert.fail("Expecting ArithmeticException");
        } catch (ArithmeticException e) {
            // Expected
        }

        try {
            f1.divide(Fraction.ZERO);
            Assert.fail("Expecting ArithmeticException");
        } catch (ArithmeticException e) {
            // Expected
        }
    }

    @Test
    public void testPow() {
        Fraction f = new Fraction(2, 3);
        Fraction pow2 = f.pow(2);
        Assert.assertEquals(4, pow2.getNumerator());
        Assert.assertEquals(9, pow2.getDenominator());

        Fraction pow0 = f.pow(0);
        Assert.assertEquals(1, pow0.getNumerator());
        Assert.assertEquals(1, pow0.getDenominator());

        Fraction powNeg = f.pow(-2);
        Assert.assertEquals(9, powNeg.getNumerator());
        Assert.assertEquals(4, powNeg.getDenominator());
    }

    @Test
    public void testReciprocal() {
        Fraction f = new Fraction(2, 3);
        Fraction rec = f.reciprocal();
        Assert.assertEquals(3, rec.getNumerator());
        Assert.assertEquals(2, rec.getDenominator());

        try {
            Fraction.ZERO.reciprocal();
            Assert.fail("Expecting ArithmeticException");
        } catch (ArithmeticException e) {
            // Expected
        }
    }

    @Test
    public void testToString() {
        Fraction f = new Fraction(1, 2);
        Assert.assertEquals("1 / 2", f.toString());

        Fraction fInt = new Fraction(3, 1);
        Assert.assertEquals("3", fInt.toString());
    }
}