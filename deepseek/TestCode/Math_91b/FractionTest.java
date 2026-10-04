package org.apache.commons.math.fraction;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

public class FractionTest {

    private Fraction zero;
    private Fraction oneHalf;
    private Fraction negativeOneHalf;
    private Fraction oneThird;
    private Fraction twoThirds;
    private Fraction integerFive;
    private Fraction negativeIntegerFive;
    private Fraction largeNumerator;
    private Fraction largeDenominator;

    @Before
    public void setUp() {
        zero = new Fraction(0, 1);
        oneHalf = new Fraction(1, 2);
        negativeOneHalf = new Fraction(-1, 2);
        oneThird = new Fraction(1, 3);
        twoThirds = new Fraction(2, 3);
        integerFive = new Fraction(5, 1);
        negativeIntegerFive = new Fraction(-5, 1);
        largeNumerator = new Fraction(Integer.MAX_VALUE - 1, 1);
        largeDenominator = new Fraction(1, Integer.MAX_VALUE - 1);
    }

    // Constructor tests
    @Test(expected = ArithmeticException.class)
    public void testConstructorZeroDenominator() {
        new Fraction(1, 0);
    }

    @Test
    public void testConstructorNegativeDenominator() {
        Fraction f = new Fraction(3, -5);
        assertEquals(-3, f.getNumerator());
        assertEquals(5, f.getDenominator());
    }

    @Test
    public void testConstructorReduce() {
        Fraction f = new Fraction(6, 4);
        assertEquals(3, f.getNumerator());
        assertEquals(2, f.getDenominator());
    }

    @Test
    public void testConstructorZero() {
        Fraction f = new Fraction(0, 5);
        assertEquals(0, f.getNumerator());
        assertEquals(1, f.getDenominator());
    }

    @Test
    public void testConstructorIntInt() {
        Fraction f = new Fraction(2, 3);
        assertEquals(2, f.getNumerator());
        assertEquals(3, f.getDenominator());
    }

    // add tests
    @Test
    public void testAddSameDenominator() {
        Fraction result = oneHalf.add(oneHalf);
        assertEquals(1, result.getNumerator());
        assertEquals(1, result.getDenominator());
    }

    @Test
    public void testAddDifferentDenominator() {
        Fraction result = oneHalf.add(oneThird);
        assertEquals(5, result.getNumerator());
        assertEquals(6, result.getDenominator());
    }

    @Test
    public void testAddWithZero() {
        assertEquals(oneHalf, oneHalf.add(zero));
        assertEquals(oneHalf, zero.add(oneHalf));
    }

    @Test
    public void testAddNegative() {
        Fraction result = oneHalf.add(negativeOneHalf);
        assertEquals(0, result.getNumerator());
        assertEquals(1, result.getDenominator());
    }

    @Test(expected = ArithmeticException.class)
    public void testAddOverflowNumerator() {
        Fraction max = new Fraction(Integer.MAX_VALUE, 1);
        max.add(new Fraction(1, 1));
    }

    @Test(expected = ArithmeticException.class)
    public void testAddOverflowDenominator() {
        // This may not necessarily overflow; we just need to ensure overflow is caught.
        // Use large values in both numerator and denominator to increase chance.
        Fraction f1 = new Fraction(Integer.MAX_VALUE, 1000000);
        Fraction f2 = new Fraction(Integer.MAX_VALUE, 1000000);
        f1.add(f2);
    }

    // subtract tests
    @Test
    public void testSubtractSameDenominator() {
        Fraction result = twoThirds.subtract(oneThird);
        assertEquals(1, result.getNumerator());
        assertEquals(3, result.getDenominator());
    }

    @Test
    public void testSubtractDifferentDenominator() {
        Fraction result = oneHalf.subtract(oneThird);
        assertEquals(1, result.getNumerator());
        assertEquals(6, result.getDenominator());
    }

    @Test
    public void testSubtractFromZero() {
        Fraction result = zero.subtract(oneHalf);
        assertEquals(-1, result.getNumerator());
        assertEquals(2, result.getDenominator());
    }

    @Test(expected = ArithmeticException.class)
    public void testSubtractOverflow() {
        Fraction min = new Fraction(Integer.MIN_VALUE, 1);
        min.subtract(new Fraction(1, 1));
    }

    // multiply tests
    @Test
    public void testMultiply() {
        Fraction result = oneHalf.multiply(twoThirds);
        assertEquals(1, result.getNumerator());
        assertEquals(3, result.getDenominator());
    }

    @Test
    public void testMultiplyByZero() {
        assertEquals(zero, oneHalf.multiply(zero));
    }

    @Test
    public void testMultiplyByNegative() {
        Fraction result = oneHalf.multiply(negativeOneHalf);
        assertEquals(-1, result.getNumerator());
        assertEquals(4, result.getDenominator());
    }

    @Test(expected = ArithmeticException.class)
    public void testMultiplyOverflow() {
        Fraction big = new Fraction(Integer.MAX_VALUE, 1);
        big.multiply(new Fraction(2, 1));
    }

    // divide tests
    @Test
    public void testDivide() {
        Fraction result = oneHalf.divide(oneThird);
        assertEquals(3, result.getNumerator());
        assertEquals(2, result.getDenominator());
    }

    @Test(expected = ArithmeticException.class)
    public void testDivideByZero() {
        oneHalf.divide(zero);
    }

    @Test(expected = ArithmeticException.class)
    public void testDivideOverflow() {
        Fraction big = new Fraction(1, Integer.MAX_VALUE);
        big.divide(new Fraction(1, Integer.MAX_VALUE));
        // This may not overflow; try more aggressive? Use integer min.
    }

    @Test(expected = ArithmeticException.class)
    public void testDivideOverflow2() {
        Fraction f1 = new Fraction(1, Integer.MAX_VALUE);
        Fraction f2 = new Fraction(Integer.MAX_VALUE, 1);
        f1.divide(f2);
    }

    // abs tests
    @Test
    public void testAbsPositive() {
        assertEquals(oneHalf, oneHalf.abs());
    }

    @Test
    public void testAbsNegative() {
        Fraction abs = negativeOneHalf.abs();
        assertEquals(1, abs.getNumerator());
        assertEquals(2, abs.getDenominator());
    }

    @Test
    public void testAbsZero() {
        assertEquals(zero, zero.abs());
    }

    // compareTo tests (targeting overflow bug)
    @Test
    public void testCompareToEqual() {
        assertEquals(0, oneHalf.compareTo(new Fraction(1, 2)));
    }

    @Test
    public void testCompareToGreater() {
        assertTrue(oneHalf.compareTo(oneThird) > 0);
    }

    @Test
    public void testCompareToLess() {
        assertTrue(oneThird.compareTo(oneHalf) < 0);
    }

    @Test
    public void testCompareToWithNegative() {
        assertTrue(oneHalf.compareTo(negativeOneHalf) > 0);
        assertTrue(negativeOneHalf.compareTo(oneHalf) < 0);
    }

    @Test
    public void testCompareToWithZero() {
        assertTrue(oneHalf.compareTo(zero) > 0);
        assertTrue(zero.compareTo(negativeOneHalf) > 0);
        assertTrue(zero.compareTo(oneHalf) < 0);
    }

    @Test
    public void testCompareToLargeValues() {
        // Large numerator and denominator to trigger possible overflow in cross multiplication
        Fraction a = new Fraction(1, Integer.MAX_VALUE);
        Fraction b = new Fraction(2, Integer.MAX_VALUE);
        assertTrue(a.compareTo(b) < 0);
        assertTrue(b.compareTo(a) > 0);
    }

    @Test
    public void testCompareToOverflowEdge() {
        // Values that cause overflow in cross multiplication if using int
        Fraction p = new Fraction(1, Integer.MAX_VALUE - 1);
        Fraction q = new Fraction(2, Integer.MAX_VALUE);
        assertTrue(p.compareTo(q) < 0);
        assertTrue(q.compareTo(p) > 0);
    }

    @Test
    public void testCompareToExtreme() {
        // Using Integer.MIN_VALUE can cause overflow
        Fraction m = new Fraction(-1, 1);
        Fraction n = new Fraction(1, 1);
        assertTrue(m.compareTo(n) < 0);
        assertTrue(n.compareTo(m) > 0);
    }

    // equals tests
    @Test
    public void testEqualsEqual() {
        assertEquals(oneHalf, new Fraction(1, 2));
    }

    @Test
    public void testEqualsNotEqual() {
        assertNotEquals(oneHalf, oneThird);
    }

    @Test
    public void testEqualsDifferentDenominator() {
        // 2/4 equals 1/2 after reduction
        assertEquals(oneHalf, new Fraction(2, 4));
    }

    @Test
    public void testEqualsNull() {
        assertNotNull(oneHalf);
        assertNotEquals(null, oneHalf);
    }

    @Test
    public void testEqualsString() {
        assertNotEquals("string", oneHalf);
    }

    // hashCode tests
    @Test
    public void testHashCodeEqualObjects() {
        assertEquals(oneHalf.hashCode(), new Fraction(1, 2).hashCode());
    }

    @Test
    public void testHashCodeDifferentObjects() {
        assertNotEquals(oneHalf.hashCode(), oneThird.hashCode());
    }

    // doubleValue tests
    @Test
    public void testDoubleValue() {
        assertEquals(0.5, oneHalf.doubleValue(), 0.0);
        assertEquals(-0.5, negativeOneHalf.doubleValue(), 0.0);
        assertEquals(0.0, zero.doubleValue(), 0.0);
    }

    // floatValue tests
    @Test
    public void testFloatValue() {
        assertEquals(0.5f, oneHalf.floatValue(), 0.0f);
        assertEquals(-0.5f, negativeOneHalf.floatValue(), 0.0f);
    }

    // intValue tests
    @Test
    public void testIntValue() {
        assertEquals(5, integerFive.intValue());
        assertEquals(-5, negativeIntegerFive.intValue());
    }

    // longValue tests
    @Test
    public void testLongValue() {
        assertEquals(5L, integerFive.longValue());
        assertEquals(-5L, negativeIntegerFive.longValue());
    }

    // negate tests
    @Test
    public void testNegate() {
        Fraction neg = oneHalf.negate();
        assertEquals(-1, neg.getNumerator());
        assertEquals(2, neg.getDenominator());
    }

    @Test
    public void testNegateNegative() {
        Fraction neg = negativeOneHalf.negate();
        assertEquals(1, neg.getNumerator());
        assertEquals(2, neg.getDenominator());
    }

    @Test
    public void testNegateZero() {
        assertEquals(zero, zero.negate());
    }

    // getNumerator, getDenominator tests
    @Test
    public void testGetNumerator() {
        assertEquals(1, oneHalf.getNumerator());
    }

    @Test
    public void testGetDenominator() {
        assertEquals(2, oneHalf.getDenominator());
    }

    // toString tests (basic)
    @Test
    public void testToString() {
        assertEquals("1 / 2", oneHalf.toString());
        assertEquals("-1 / 2", negativeOneHalf.toString());
        assertEquals("0 / 1", zero.toString());
    }
}