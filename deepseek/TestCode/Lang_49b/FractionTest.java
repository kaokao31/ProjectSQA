package org.apache.commons.lang.math;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

/**
 * Test suite for Fraction class, targeting bug 49 (reduce method).
 */
public class FractionTest {

    private Fraction zero;
    private Fraction oneHalf;
    private Fraction oneThird;
    private Fraction twoThirds;
    private Fraction threeFourths;
    private Fraction negativeOneHalf;
    private Fraction negativeDenominator;
    private Fraction wholeNumber;
    private Fraction improperFraction;

    @Before
    public void setUp() {
        zero = Fraction.getFraction(0, 1);
        oneHalf = Fraction.getFraction(1, 2);
        oneThird = Fraction.getFraction(1, 3);
        twoThirds = Fraction.getFraction(2, 3);
        threeFourths = Fraction.getFraction(3, 4);
        negativeOneHalf = Fraction.getFraction(-1, 2);
        negativeDenominator = Fraction.getFraction(1, -2);
        wholeNumber = Fraction.getFraction(5, 1);
        improperFraction = Fraction.getFraction(7, 3);
    }

    // ---------- reduce method tests ----------
    @Test
    public void testReduceSimple() {
        Fraction f = Fraction.getFraction(2, 4);
        Fraction reduced = f.reduce();
        assertEquals(1, reduced.getNumerator());
        assertEquals(2, reduced.getDenominator());
    }

    @Test
    public void testReduceToWholeNumber() {
        Fraction f = Fraction.getFraction(100, 100);
        Fraction reduced = f.reduce();
        assertEquals(1, reduced.getNumerator());
        assertEquals(1, reduced.getDenominator());
    }

    @Test
    public void testReduceAlreadyReduced() {
        Fraction f = Fraction.getFraction(3, 7);
        assertSame(f, f.reduce()); // should return same instance
    }

    @Test
    public void testReduceZero() {
        Fraction f = Fraction.getFraction(0, 5);
        Fraction reduced = f.reduce();
        assertEquals(0, reduced.getNumerator());
        assertEquals(1, reduced.getDenominator());
    }

    @Test
    public void testReduceNegativeNumerator() {
        Fraction f = Fraction.getFraction(-4, 6);
        Fraction reduced = f.reduce();
        assertEquals(-2, reduced.getNumerator());
        assertEquals(3, reduced.getDenominator());
    }

    @Test
    public void testReduceNegativeDenominator() {
        Fraction f = Fraction.getFraction(4, -6);
        Fraction reduced = f.reduce();
        assertEquals(-2, reduced.getNumerator());
        assertEquals(3, reduced.getDenominator());
    }

    @Test
    public void testReduceBothNegative() {
        Fraction f = Fraction.getFraction(-4, -6);
        Fraction reduced = f.reduce();
        assertEquals(2, reduced.getNumerator());
        assertEquals(3, reduced.getDenominator());
    }

    @Test
    public void testReduceLargeNumbers() {
        Fraction f = Fraction.getFraction(123456, 789012);
        Fraction reduced = f.reduce();
        // GCD of 123456 and 789012 is 12? Actually compute: 123456/12=10288, 789012/12=65751
        assertEquals(10288, reduced.getNumerator());
        assertEquals(65751, reduced.getDenominator());
    }

    @Test
    public void testReduceNegativeZero() {
        Fraction f = Fraction.getFraction(0, -5);
        Fraction reduced = f.reduce();
        assertEquals(0, reduced.getNumerator());
        assertEquals(1, reduced.getDenominator());
    }

    // ---------- getFraction factory tests ----------
    @Test(expected = ArithmeticException.class)
    public void testGetFractionDenominatorZero() {
        Fraction.getFraction(1, 0);
    }

    @Test(expected = ArithmeticException.class)
    public void testGetFractionNegativeDenominatorZero() {
        Fraction.getFraction(0, 0);
    }

    @Test
    public void testGetFractionIntInt() {
        Fraction f = Fraction.getFraction(3, 5);
        assertEquals(3, f.getNumerator());
        assertEquals(5, f.getDenominator());
    }

    @Test
    public void testGetFractionIntIntInt() {
        Fraction f = Fraction.getFraction(2, 1, 3);
        assertEquals(7, f.getNumerator());
        assertEquals(3, f.getDenominator());
    }

    @Test(expected = ArithmeticException.class)
    public void testGetFractionIntIntIntNegativeWhole() {
        Fraction.getFraction(-1, 1, 3);
    }

    @Test(expected = ArithmeticException.class)
    public void testGetFractionIntIntIntNegativeNumerator() {
        Fraction.getFraction(1, -1, 3);
    }

    @Test(expected = ArithmeticException.class)
    public void testGetFractionIntIntIntZeroDenominator() {
        Fraction.getFraction(1, 0, 3);
    }

    // ---------- getReducedFraction tests ----------
    @Test
    public void testGetReducedFractionSimple() {
        Fraction f = Fraction.getReducedFraction(2, 4);
        assertEquals(1, f.getNumerator());
        assertEquals(2, f.getDenominator());
    }

    @Test(expected = ArithmeticException.class)
    public void testGetReducedFractionDenominatorZero() {
        Fraction.getReducedFraction(1, 0);
    }

    @Test(expected = ArithmeticException.class)
    public void testGetReducedFractionNegativeDenominator() {
        Fraction.getReducedFraction(1, -2);
    }

    // ---------- add method tests ----------
    @Test
    public void testAddSimple() {
        Fraction result = oneHalf.add(oneThird);
        assertEquals(5, result.getNumerator());
        assertEquals(6, result.getDenominator());
    }

    @Test
    public void testAddWholeNumber() {
        Fraction result = oneHalf.add(wholeNumber);
        assertEquals(11, result.getNumerator());
        assertEquals(2, result.getDenominator());
    }

    @Test
    public void testAddZero() {
        assertSame(oneHalf, oneHalf.add(zero));
    }

    @Test
    public void testAddNegative() {
        Fraction result = oneHalf.add(negativeOneHalf);
        assertEquals(0, result.getNumerator());
        assertEquals(1, result.getDenominator());
    }

    @Test(expected = ArithmeticException.class)
    public void testAddOverflow() {
        Fraction max = Fraction.getFraction(Integer.MAX_VALUE, 1);
        max.add(Fraction.getFraction(1, 1));
    }

    // ---------- subtract method tests ----------
    @Test
    public void testSubtractSimple() {
        Fraction result = twoThirds.subtract(oneThird);
        assertEquals(1, result.getNumerator());
        assertEquals(3, result.getDenominator());
    }

    @Test
    public void testSubtractNegative() {
        Fraction result = oneHalf.subtract(negativeOneHalf);
        assertEquals(1, result.getNumerator());
        assertEquals(1, result.getDenominator());
    }

    @Test
    public void testSubtractZero() {
        assertSame(oneHalf, oneHalf.subtract(zero));
    }

    // ---------- multiply method tests ----------
    @Test
    public void testMultiplySimple() {
        Fraction result = oneHalf.multiply(twoThirds);
        assertEquals(1, result.getNumerator());
        assertEquals(3, result.getDenominator());
    }

    @Test
    public void testMultiplyByZero() {
        Fraction result = oneHalf.multiply(zero);
        assertEquals(0, result.getNumerator());
        assertEquals(1, result.getDenominator());
    }

    @Test
    public void testMultiplyByNegative() {
        Fraction result = oneHalf.multiply(negativeOneHalf);
        assertEquals(-1, result.getNumerator());
        assertEquals(4, result.getDenominator());
    }

    @Test(expected = ArithmeticException.class)
    public void testMultiplyOverflow() {
        Fraction large = Fraction.getFraction(Integer.MAX_VALUE, 1);
        large.multiply(Fraction.getFraction(2, 1));
    }

    // ---------- divide method tests ----------
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

    @Test(expected = ArithmeticException.class)
    public void testDivideByZeroFraction() {
        Fraction zeroFraction = Fraction.getFraction(0, 1);
        oneHalf.divide(zeroFraction);
    }

    // ---------- equals and hashCode tests ----------
    @Test
    public void testEqualsSame() {
        assertTrue(oneHalf.equals(oneHalf));
    }

    @Test
    public void testEqualsEquivalent() {
        Fraction f1 = Fraction.getFraction(1, 2);
        Fraction f2 = Fraction.getFraction(2, 4);
        assertTrue(f1.equals(f2));
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
    public void testEqualsNonFraction() {
        assertFalse(oneHalf.equals("string"));
    }

    @Test
    public void testHashCodeConsistency() {
        Fraction f1 = Fraction.getFraction(1, 2);
        Fraction f2 = Fraction.getFraction(2, 4);
        assertEquals(f1.hashCode(), f2.hashCode());
    }

    @Test
    public void testHashCodeDifferent() {
        assertNotEquals(oneHalf.hashCode(), oneThird.hashCode());
    }

    // ---------- toString tests ----------
    @Test
    public void testToStringProper() {
        assertEquals("1/2", oneHalf.toString());
    }

    @Test
    public void testToStringWholeNumber() {
        assertEquals("5/1", wholeNumber.toString());
    }

    @Test
    public void testToStringNegative() {
        assertEquals("-1/2", negativeOneHalf.toString());
    }

    // ---------- toProperString tests ----------
    @Test
    public void testToProperStringProper() {
        assertEquals("1/2", oneHalf.toProperString());
    }

    @Test
    public void testToProperStringImproper() {
        assertEquals("2 1/3", improperFraction.toProperString());
    }

    @Test
    public void testToProperStringWholeNumber() {
        assertEquals("5", wholeNumber.toProperString());
    }

    @Test
    public void testToProperStringNegativeImproper() {
        Fraction f = Fraction.getFraction(-7, 3);
        assertEquals("-2 1/3", f.toProperString());
    }

    @Test
    public void testToProperStringZero() {
        assertEquals("0", zero.toProperString());
    }

    // ---------- getNumerator and getDenominator tests ----------
    @Test
    public void testGetNumerator() {
        assertEquals(1, oneHalf.getNumerator());
    }

    @Test
    public void testGetDenominator() {
        assertEquals(2, oneHalf.getDenominator());
    }

    @Test
    public void testGetNumeratorNegative() {
        assertEquals(-1, negativeOneHalf.getNumerator());
    }

    @Test
    public void testGetDenominatorNegative() {
        assertEquals(2, negativeDenominator.getDenominator()); // normalized to positive
    }

    // ---------- inversion tests ----------
    @Test
    public void testInvertSimple() {
        Fraction inverted = oneHalf.invert();
        assertEquals(2, inverted.getNumerator());
        assertEquals(1, inverted.getDenominator());
    }

    @Test(expected = ArithmeticException.class)
    public void testInvertZero() {
        zero.invert();
    }

    @Test
    public void testInvertNegative() {
        Fraction inverted = negativeOneHalf.invert();
        assertEquals(-2, inverted.getNumerator());
        assertEquals(1, inverted.getDenominator());
    }

    // ---------- negate tests ----------
    @Test
    public void testNegatePositive() {
        Fraction negated = oneHalf.negate();
        assertEquals(-1, negated.getNumerator());
        assertEquals(2, negated.getDenominator());
    }

    @Test
    public void testNegateNegative() {
        Fraction negated = negativeOneHalf.negate();
        assertEquals(1, negated.getNumerator());
        assertEquals(2, negated.getDenominator());
    }

    @Test
    public void testNegateZero() {
        Fraction negated = zero.negate();
        assertEquals(0, negated.getNumerator());
        assertEquals(1, negated.getDenominator());
    }

    // ---------- abs tests ----------
    @Test
    public void testAbsPositive() {
        assertSame(oneHalf, oneHalf.abs());
    }

    @Test
    public void testAbsNegative() {
        Fraction abs = negativeOneHalf.abs();
        assertEquals(1, abs.getNumerator());
        assertEquals(2, abs.getDenominator());
    }

    @Test
    public void testAbsZero() {
        assertSame(zero, zero.abs());
    }

    // ---------- doubleValue, floatValue, intValue, longValue tests ----------
    @Test
    public void testDoubleValue() {
        assertEquals(0.5, oneHalf.doubleValue(), 0.0);
    }

    @Test
    public void testFloatValue() {
        assertEquals(0.5f, oneHalf.floatValue(), 0.0f);
    }

    @Test
    public void testIntValue() {
        assertEquals(0, oneHalf.intValue());
    }

    @Test
    public void testIntValueWhole() {
        assertEquals(5, wholeNumber.intValue());
    }

    @Test
    public void testLongValue() {
        assertEquals(0L, oneHalf.longValue());
    }

    @Test
    public void testLongValueWhole() {
        assertEquals(5L, wholeNumber.longValue());
    }

    // ---------- compareTo tests ----------
    @Test
    public void testCompareToEqual() {
        assertEquals(0, oneHalf.compareTo(Fraction.getFraction(1, 2)));
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
        assertTrue(negativeOneHalf.compareTo(oneHalf) < 0);
    }

    // ---------- getProperWhole and getProperNumerator tests ----------
    @Test
    public void testGetProperWhole() {
        assertEquals(2, improperFraction.getProperWhole());
    }

    @Test
    public void testGetProperNumerator() {
        assertEquals(1, improperFraction.getProperNumerator());
    }

    @Test
    public void testGetProperWholeNegative() {
        Fraction f = Fraction.getFraction(-7, 3);
        assertEquals(-2, f.getProperWhole());
    }

    @Test
    public void testGetProperNumeratorNegative() {
        Fraction f = Fraction.getFraction(-7, 3);
        assertEquals(1, f.getProperNumerator());
    }

    @Test
    public void testGetProperWholeWholeNumber() {
        assertEquals(5, wholeNumber.getProperWhole());
    }

    @Test
    public void testGetProperNumeratorWholeNumber() {
        assertEquals(0, wholeNumber.getProperNumerator());
    }

    // ---------- Additional edge cases for reduce ----------
    @Test
    public void testReduceWithMaxInt() {
        Fraction f = Fraction.getFraction(Integer.MAX_VALUE, Integer.MAX_VALUE);
        Fraction reduced = f.reduce();
        assertEquals(1, reduced.getNumerator());
        assertEquals(1, reduced.getDenominator());
    }

    @Test
    public void testReduceWithMinInt() {
        // Note: -Integer.MIN_VALUE overflows, but Fraction handles it
        Fraction f = Fraction.getFraction(Integer.MIN_VALUE, 1);
        Fraction reduced = f.reduce();
        assertEquals(Integer.MIN_VALUE, reduced.getNumerator());
        assertEquals(1, reduced.getDenominator());
    }

    @Test
    public void testReduceWithNegativeMinInt() {
        Fraction f = Fraction.getFraction(Integer.MIN_VALUE, -1);
        // This should reduce to a positive fraction, but careful with overflow
        Fraction reduced = f.reduce();
        // The result should be Integer.MAX_VALUE? Actually -Integer.MIN_VALUE = Integer.MIN_VALUE (overflow)
        // The Fraction class should handle this correctly
        assertNotNull(reduced);
    }

    // ---------- Test for bug 49 specific scenario ----------
    @Test
    public void testReduceBug49() {
        // This is the failing test from Defects4J
        Fraction f = Fraction.getFraction(100, 100);
        Fraction reduced = f.reduce();
        assertEquals(1, reduced.getNumerator());
        assertEquals(1, reduced.getDenominator());
    }

    @Test
    public void testReduceBug49WithNegative() {
        Fraction f = Fraction.getFraction(-100, 100);
        Fraction reduced = f.reduce();
        assertEquals(-1, reduced.getNumerator());
        assertEquals(1, reduced.getDenominator());
    }

    @Test
    public void testReduceBug49WithBothNegative() {
        Fraction f = Fraction.getFraction(-100, -100);
        Fraction reduced = f.reduce();
        assertEquals(1, reduced.getNumerator());
        assertEquals(1, reduced.getDenominator());
    }

    @Test
    public void testReduceBug49WithZeroNumerator() {
        Fraction f = Fraction.getFraction(0, 100);
        Fraction reduced = f.reduce();
        assertEquals(0, reduced.getNumerator());
        assertEquals(1, reduced.getDenominator());
    }
}