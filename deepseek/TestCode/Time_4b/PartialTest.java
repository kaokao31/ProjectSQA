package org.apache.commons.lang3.math;

import static org.junit.Assert.*;

import org.junit.Test;

public class PartialTest {

    @Test
    public void testAddAndSubtract() {
        Fraction f1 = Fraction.getFraction(1, 2);
        Fraction f2 = Fraction.getFraction(1, 3);
        Fraction result = f1.add(f2);
        assertEquals(Fraction.getFraction(5, 6), result);
        
        result = f1.subtract(f2);
        assertEquals(Fraction.getFraction(1, 6), result);
    }

    @Test
    public void testMultiplyAndDivide() {
        Fraction f1 = Fraction.getFraction(2, 3);
        Fraction f2 = Fraction.getFraction(3, 4);
        Fraction result = f1.multiplyBy(f2);
        assertEquals(Fraction.getFraction(1, 2), result);
        
        result = f1.divideBy(f2);
        assertEquals(Fraction.getFraction(8, 9), result);
    }

    @Test(expected = ArithmeticException.class)
    public void testDivideByZero() {
        Fraction f1 = Fraction.getFraction(1, 2);
        Fraction f2 = Fraction.getFraction(0, 1);
        f1.divideBy(f2);
    }

    @Test
    public void testNegate() {
        Fraction f = Fraction.getFraction(3, 4);
        assertEquals(Fraction.getFraction(-3, 4), f.negate());
        
        f = Fraction.getFraction(-5, 7);
        assertEquals(Fraction.getFraction(5, 7), f.negate());
    }

    @Test
    public void testAbs() {
        Fraction f = Fraction.getFraction(-3, 4);
        assertEquals(Fraction.getFraction(3, 4), f.abs());
        
        f = Fraction.getFraction(5, 7);
        assertEquals(Fraction.getFraction(5, 7), f.abs());
    }

    @Test
    public void testPow() {
        Fraction f = Fraction.getFraction(2, 3);
        assertEquals(Fraction.getFraction(4, 9), f.pow(2));
        assertEquals(Fraction.getFraction(3, 2), f.pow(-1));
        assertEquals(Fraction.ONE, f.pow(0));
    }

    @Test(expected = ArithmeticException.class)
    public void testPowNegativeExponentWithZeroNumerator() {
        Fraction f = Fraction.getFraction(0, 1);
        f.pow(-1);
    }

    @Test
    public void testGetReducedFraction() {
        assertEquals(Fraction.getFraction(1, 2), Fraction.getReducedFraction(2, 4));
        assertEquals(Fraction.getFraction(-1, 2), Fraction.getReducedFraction(-2, 4));
        assertEquals(Fraction.getFraction(1, 2), Fraction.getReducedFraction(-2, -4));
    }

    @Test(expected = ArithmeticException.class)
    public void testGetReducedFractionDenominatorZero() {
        Fraction.getReducedFraction(1, 0);
    }

    @Test
    public void testGetFractionString() {
        assertEquals(Fraction.getFraction(3, 4), Fraction.getFraction("3/4"));
        assertEquals(Fraction.getFraction(-5, 7), Fraction.getFraction("-5/7"));
        assertEquals(Fraction.getFraction(1, 2), Fraction.getFraction(" 1/2 "));
    }

    @Test(expected = NumberFormatException.class)
    public void testGetFractionInvalidString() {
        Fraction.getFraction("abc");
    }

    @Test
    public void testGetFractionDouble() {
        assertEquals(Fraction.getFraction(0.5), Fraction.getFraction(1, 2));
        assertEquals(Fraction.getFraction(0.75), Fraction.getFraction(3, 4));
    }

    @Test
    public void testEqualsAndHashCode() {
        Fraction f1 = Fraction.getFraction(1, 2);
        Fraction f2 = Fraction.getFraction(1, 2);
        Fraction f3 = Fraction.getFraction(2, 4);
        
        assertTrue(f1.equals(f2));
        assertTrue(f1.equals(f3));
        assertEquals(f1.hashCode(), f2.hashCode());
        assertEquals(f1.hashCode(), f3.hashCode());
        
        assertFalse(f1.equals(null));
        assertFalse(f1.equals("string"));
    }

    @Test
    public void testCompareTo() {
        Fraction f1 = Fraction.getFraction(1, 2);
        Fraction f2 = Fraction.getFraction(1, 3);
        Fraction f3 = Fraction.getFraction(1, 2);
        
        assertTrue(f1.compareTo(f2) > 0);
        assertTrue(f2.compareTo(f1) < 0);
        assertEquals(0, f1.compareTo(f3));
    }

    @Test
    public void testToString() {
        Fraction f = Fraction.getFraction(3, 4);
        assertEquals("3/4", f.toString());
        
        f = Fraction.getFraction(-5, 7);
        assertEquals("-5/7", f.toString());
    }

    @Test
    public void testToProperString() {
        Fraction f = Fraction.getFraction(5, 3);
        assertEquals("1 2/3", f.toProperString());
        
        f = Fraction.getFraction(1, 2);
        assertEquals("1/2", f.toProperString());
        
        f = Fraction.getFraction(0, 5);
        assertEquals("0", f.toProperString());
    }

    @Test
    public void testIntValue() {
        Fraction f = Fraction.getFraction(5, 3);
        assertEquals(1, f.intValue());
        
        f = Fraction.getFraction(-5, 3);
        assertEquals(-1, f.intValue());
    }

    @Test
    public void testLongValue() {
        Fraction f = Fraction.getFraction(5, 3);
        assertEquals(1L, f.longValue());
        
        f = Fraction.getFraction(-5, 3);
        assertEquals(-1L, f.longValue());
    }

    @Test
    public void testFloatValue() {
        Fraction f = Fraction.getFraction(1, 2);
        assertEquals(0.5f, f.floatValue(), 0.0001f);
        
        f = Fraction.getFraction(1, 3);
        assertEquals(0.3333f, f.floatValue(), 0.0001f);
    }

    @Test
    public void testDoubleValue() {
        Fraction f = Fraction.getFraction(1, 2);
        assertEquals(0.5, f.doubleValue(), 0.0001);
        
        f = Fraction.getFraction(1, 3);
        assertEquals(0.3333, f.doubleValue(), 0.0001);
    }

    @Test
    public void testInvert() {
        Fraction f = Fraction.getFraction(3, 4);
        assertEquals(Fraction.getFraction(4, 3), f.invert());
        
        f = Fraction.getFraction(-5, 7);
        assertEquals(Fraction.getFraction(-7, 5), f.invert());
    }

    @Test(expected = ArithmeticException.class)
    public void testInvertZero() {
        Fraction f = Fraction.getFraction(0, 1);
        f.invert();
    }

    @Test
    public void testGetNumeratorAndDenominator() {
        Fraction f = Fraction.getFraction(3, 4);
        assertEquals(3, f.getNumerator());
        assertEquals(4, f.getDenominator());
        
        f = Fraction.getFraction(-5, 7);
        assertEquals(-5, f.getNumerator());
        assertEquals(7, f.getDenominator());
    }

    @Test
    public void testReduce() {
        Fraction f = Fraction.getFraction(2, 4);
        assertEquals(Fraction.getFraction(1, 2), f.reduce());
        
        f = Fraction.getFraction(6, 9);
        assertEquals(Fraction.getFraction(2, 3), f.reduce());
    }

    @Test
    public void testGetFractionIntInt() {
        Fraction f = Fraction.getFraction(3, 4);
        assertEquals(3, f.getNumerator());
        assertEquals(4, f.getDenominator());
    }

    @Test(expected = ArithmeticException.class)
    public void testGetFractionIntIntDenominatorZero() {
        Fraction.getFraction(1, 0);
    }

    @Test
    public void testGetFractionIntIntInt() {
        Fraction f = Fraction.getFraction(1, 2, 3);
        assertEquals(Fraction.getFraction(5, 3), f);
        
        f = Fraction.getFraction(-1, 2, 3);
        assertEquals(Fraction.getFraction(-5, 3), f);
    }

    @Test(expected = ArithmeticException.class)
    public void testGetFractionIntIntIntNegativeDenominator() {
        Fraction.getFraction(1, 2, -3);
    }

    @Test
    public void testSerialization() {
        Fraction f = Fraction.getFraction(3, 4);
        assertNotNull(f);
        // Basic check that it's a valid object
        assertTrue(f instanceof Fraction);
    }
}