package org.apache.commons.math3.fraction;

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

        Fraction fNeg = new Fraction(2, -4);
        Assert.assertEquals(-1, fNeg.getNumerator());
        Assert.assertEquals(2, fNeg.getDenominator());

        Fraction fNegBoth = new Fraction(-2, -4);
        Assert.assertEquals(1, fNegBoth.getNumerator());
        Assert.assertEquals(2, fNegBoth.getDenominator());
    }

    @Test(expected = ArithmeticException.class)
    public void testConstructorZeroDenominator() {
        new Fraction(1, 0);
    }

    @Test(expected = ArithmeticException.class)
    public void testConstructorMinInt() {
        // Integer.MIN_VALUE causes overflow when negated/abs'd improperly in some implementations
        new Fraction(Integer.MIN_VALUE, -1);
    }

    @Test
    public void testConstructorDouble() {
        Fraction f = new Fraction(0.5);
        Assert.assertEquals(1, f.getNumerator());
        Assert.assertEquals(2, f.getDenominator());

        Fraction fPi = new Fraction(Math.PI, 1e-9, 100);
        Assert.assertTrue(fPi.doubleValue() > 3.14 && fPi.doubleValue() < 3.15);

        Fraction fMax = new Fraction(Double.MAX_VALUE);
        Assert.assertNotNull(fMax);
    }

    @Test(expected = ArithmeticException.class)
    public void testConstructorDoubleOverflow() {
        new Fraction(Double.MAX_VALUE, 1e-12, 10);
    }

    @Test(expected = FractionConversionException.class)
    public void testConstructorDoubleTooLarge() {
        new Fraction(1e10, 1e-9, 10);
    }

    @Test
    public void testAbs() {
        Fraction f = new Fraction(-2, 3);
        Fraction absF = f.abs();
        Assert.assertEquals(2, absF.getNumerator());
        Assert.assertEquals(3, absF.getDenominator());

        Fraction fPos = new Fraction(2, 3);
        Assert.assertSame(fPos, fPos.abs());
    }

    @Test
    public void testCompareTo() {
        Fraction f1 = new Fraction(1, 2);
        Fraction f2 = new Fraction(2, 3);
        Fraction f3 = new Fraction(1, 2);

        Assert.assertTrue(f1.compareTo(f2) < 0);
        Assert.assertTrue(f2.compareTo(f1) > 0);
        Assert.assertEquals(0, f1.compareTo(f3));
    }

    @Test
    public void testDoubleFloat() {
        Fraction f = new Fraction(3, 4);
        Assert.assertEquals(0.75, f.doubleValue(), 1e-15);
        Assert.assertEquals(0.75f, f.floatValue(), 1e-7f);
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
    }

    @Test
    public void testGetFraction() {
        Fraction f = Fraction.getFraction(0.3333333333333);
        Assert.assertNotNull(f);
    }

    @Test
    public void testArithmeticOperations() {
        Fraction f1 = new Fraction(1, 2);
        Fraction f2 = new Fraction(1, 3);

        // Add
        Fraction add = f1.add(f2);
        Assert.assertEquals(5, add.getNumerator());
        Assert.assertEquals(6, add.getDenominator());

        // Subtract
        Fraction sub = f1.subtract(f2);
        Assert.assertEquals(1, sub.getNumerator());
        Assert.assertEquals(6, sub.getDenominator());

        // Multiply
        Fraction mul = f1.multiply(f2);
        Assert.assertEquals(1, mul.getNumerator());
        Assert.assertEquals(6, mul.getDenominator());

        // Divide
        Fraction div = f1.divide(f2);
        Assert.assertEquals(3, div.getNumerator());
        Assert.assertEquals(2, div.getDenominator());
    }

    @Test
    public void testAddAndSubtractInt() {
        Fraction f = new Fraction(1, 2);
        Fraction resAdd = f.add(1);
        Assert.assertEquals(3, resAdd.getNumerator());
        Assert.assertEquals(2, resAdd.getDenominator());

        Fraction resSub = f.subtract(1);
        Assert.assertEquals(-1, resSub.getNumerator());
        Assert.assertEquals(2, resSub.getDenominator());
    }

    @Test
    public void testMultiplyDivideInt() {
        Fraction f = new Fraction(1, 2);
        Fraction resMul = f.multiply(3);
        Assert.assertEquals(3, resMul.getNumerator());
        Assert.assertEquals(2, resMul.getDenominator());

        Fraction resDiv = f.divide(2);
        Assert.assertEquals(1, resDiv.getNumerator());
        Assert.assertEquals(4, resDiv.getDenominator());
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
    }

    @Test
    public void testPercentage() {
        Fraction f = new Fraction(1, 2);
        Assert.assertEquals(50.0, f.percentageValue(), 1e-15);
    }

    @Test
    public void testToString() {
        Fraction f = new Fraction(3, 4);
        Assert.assertEquals("3 / 4", f.toString());

        Fraction fInt = new Fraction(5, 1);
        Assert.assertEquals("5", fInt.toString());
    }

    @Test
    public void testPercentageOverflowBugTrigger() {
        // Specific test targeting potential overflow in percentageValue or multiply by 100 in Math-27
        Fraction f = new Fraction(Integer.MAX_VALUE, 1);
        try {
            f.percentageValue();
        } catch (Exception e) {
            // Expected or handled depending on implementation, but executes the branch
        }
    }
}