package org.apache.commons.math.fraction;

import org.junit.Assert;
import org.junit.Test;

public class FractionTest {

    @Test
    public void testConstructorInt() {
        Fraction f = new Fraction(5);
        Assert.assertEquals(5, f.getNumerator());
        Assert.assertEquals(1, f.getDenominator());
    }

    @Test
    public void testConstructorIntInt() {
        Fraction f = new Fraction(2, 4);
        Assert.assertEquals(1, f.getNumerator());
        Assert.assertEquals(2, f.getDenominator());

        Fraction fNeg = new Fraction(-2, 4);
        Assert.assertEquals(-1, fNeg.getNumerator());
        Assert.assertEquals(2, fNeg.getDenominator());

        Fraction fNegNeg = new Fraction(-2, -4);
        Assert.assertEquals(1, fNegNeg.getNumerator());
        Assert.assertEquals(2, fNegNeg.getDenominator());

        Fraction fPosNeg = new Fraction(2, -4);
        Assert.assertEquals(-1, fPosNeg.getNumerator());
        Assert.assertEquals(2, fPosNeg.getDenominator());
    }

    @Test(expected = ArithmeticException.class)
    public void testConstructorZeroDenominator() {
        new Fraction(1, 0);
    }

    @Test(expected = ArithmeticException.class)
    public void testConstructorOverflow() {
        new Fraction(Integer.MIN_VALUE, -1);
    }

    @Test
    public void testConstructorDouble() {
        Fraction f = new Fraction(0.5);
        Assert.assertEquals(1, f.getNumerator());
        Assert.assertEquals(2, f.getDenominator());

        Fraction fZero = new Fraction(0.0);
        Assert.assertEquals(0, fZero.getNumerator());
        Assert.assertEquals(1, fZero.getDenominator());

        Fraction fPi = new Fraction(Math.PI, 1.0e-5, 100);
        Assert.assertTrue(fPi.doubleValue() > 3.14 && fPi.doubleValue() < 3.15);
    }

    @Test(expected = ArithmeticException.class)
    public void testConstructorDoubleOverflow() {
        new Fraction(1e100, 1e-10, 1000);
    }

    @Test(expected = ArithmeticException.class)
    public void testConstructorDoubleMaxIterations() {
        new Fraction(Math.PI, 1e-15, 2);
    }

    @Test
    public void testAbs() {
        Fraction f = new Fraction(-2, 3);
        Fraction abs = f.abs();
        Assert.assertEquals(2, abs.getNumerator());
        Assert.assertEquals(3, abs.getDenominator());

        Fraction fPos = new Fraction(2, 3);
        Assert.assertSame(fPos, fPos.abs());
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

    @Test(expected = NullPointerException.class)
    public void testCompareToNull() {
        Fraction f1 = new Fraction(1, 2);
        f1.compareTo(null);
    }

    @Test
    public void testDoubleValue() {
        Fraction f = new Fraction(1, 4);
        Assert.assertEquals(0.25, f.doubleValue(), 1e-15);
    }

    @Test
    public void testEqualsAndHashCode() {
        Fraction f1 = new Fraction(1, 2);
        Fraction f2 = new Fraction(1, 2);
        Fraction f3 = new Fraction(2, 4);
        Fraction f4 = new Fraction(1, 3);
        Object notFraction = "Not a fraction";

        Assert.assertTrue(f1.equals(f1));
        Assert.assertTrue(f1.equals(f2));
        Assert.assertTrue(f1.equals(f3));
        Assert.assertFalse(f1.equals(f4));
        Assert.assertFalse(f1.equals(null));
        Assert.assertFalse(f1.equals(notFraction));

        Assert.assertEquals(f1.hashCode(), f2.hashCode());
    }

    @Test
    public void testFloatValue() {
        Fraction f = new Fraction(1, 4);
        Assert.assertEquals(0.25f, f.floatValue(), 1e-7f);
    }

    @Test
    public void testIntValue() {
        Fraction f = new Fraction(7, 2);
        Assert.assertEquals(3, f.intValue());
    }

    @Test
    public void testLongValue() {
        Fraction f = new Fraction(7, 2);
        Assert.assertEquals(3L, f.longValue());
    }

    @Test
    public void testNegate() {
        Fraction f = new Fraction(1, 2);
        Fraction neg = f.negate();
        Assert.assertEquals(-1, neg.getNumerator());
        Assert.assertEquals(2, neg.getDenominator());
    }

    @Test
    public void testAdd() {
        Fraction f1 = new Fraction(1, 2);
        Fraction f2 = new Fraction(1, 3);
        Fraction sum = f1.add(f2);
        Assert.assertEquals(5, sum.getNumerator());
        Assert.assertEquals(6, sum.getDenominator());

        Fraction fZero = new Fraction(0);
        Assert.assertEquals(f1, f1.add(fZero));
        Assert.assertEquals(f1, fZero.add(f1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNull() {
        Fraction f = new Fraction(1, 2);
        f.add(null);
    }

    @Test
    public void testSubtract() {
        Fraction f1 = new Fraction(1, 2);
        Fraction f2 = new Fraction(1, 3);
        Fraction diff = f1.subtract(f2);
        Assert.assertEquals(1, diff.getNumerator());
        Assert.assertEquals(6, diff.getDenominator());

        Fraction fZero = new Fraction(0);
        Assert.assertEquals(f1, f1.subtract(fZero));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSubtractNull() {
        Fraction f = new Fraction(1, 2);
        f.subtract(null);
    }

    @Test
    public void testMultiply() {
        Fraction f1 = new Fraction(2, 3);
        Fraction f2 = new Fraction(3, 4);
        Fraction prod = f1.multiply(f2);
        Assert.assertEquals(1, prod.getNumerator());
        Assert.assertEquals(2, prod.getDenominator());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMultiplyNull() {
        Fraction f = new Fraction(1, 2);
        f.multiply(null);
    }

    @Test
    public void testDivide() {
        Fraction f1 = new Fraction(2, 3);
        Fraction f2 = new Fraction(4, 3);
        Fraction quot = f1.divide(f2);
        Assert.assertEquals(1, quot.getNumerator());
        Assert.assertEquals(2, quot.getDenominator());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDivideNull() {
        Fraction f = new Fraction(1, 2);
        f.divide(null);
    }

    @Test(expected = ArithmeticException.class)
    public void testDivideByZero() {
        Fraction f1 = new Fraction(1, 2);
        Fraction fZero = new Fraction(0);
        f1.divide(fZero);
    }

    @Test
    public void testToString() {
        Fraction f = new Fraction(1, 2);
        Assert.assertEquals("1 / 2", f.toString());
    }

    @Test
    public void testPercentage() {
        Fraction f = new Fraction(1, 2);
        Assert.assertEquals(50.0, f.percentageValue(), 1e-15);
    }

    @Test
    public void testCompareToBug91() {
        // Specific test targeting potential Fraction comparison/compareTo logic in Math-91
        Fraction f1 = new Fraction(1, 2);
        Fraction f2 = new Fraction(2, 3);
        Assert.assertTrue(f1.compareTo(f2) < 0);
        Assert.assertTrue(f2.compareTo(f1) > 0);
        Assert.assertEquals(0, f1.compareTo(new Fraction(2, 4)));
    }
}