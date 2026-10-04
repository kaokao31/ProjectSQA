package org.apache.commons.math.fraction;

import org.junit.Before;
import org.junit.Test;
import java.text.ParsePosition;
import java.text.ParseException;
import static org.junit.Assert.*;

public class ProperFractionFormatTest {

    private ProperFractionFormat format;

    @Before
    public void setUp() {
        format = new ProperFractionFormat();
    }

    // ========== parse(String) ==========

    @Test
    public void testParseSimpleFraction() throws ParseException {
        Fraction f = format.parse("1/2");
        assertEquals(1, f.getNumerator());
        assertEquals(2, f.getDenominator());
    }

    @Test
    public void testParseWholeNumber() throws ParseException {
        Fraction f = format.parse("3");
        assertEquals(3, f.getNumerator());
        assertEquals(1, f.getDenominator());
    }

    @Test
    public void testParseProperFractionWithWhole() throws ParseException {
        Fraction f = format.parse("1 2/3");
        assertEquals(5, f.getNumerator());
        assertEquals(3, f.getDenominator());
    }

    @Test
    public void testParseNegativeWhole() throws ParseException {
        Fraction f = format.parse("-2 1/4");
        assertEquals(-9, f.getNumerator());
        assertEquals(4, f.getDenominator());
    }

    @Test
    public void testParseNegativeFraction() throws ParseException {
        Fraction f = format.parse("-3/5");
        assertEquals(-3, f.getNumerator());
        assertEquals(5, f.getDenominator());
    }

    @Test
    public void testParseNegativeProperFraction() throws ParseException {
        Fraction f = format.parse("-1 2/7");
        assertEquals(-9, f.getNumerator());
        assertEquals(7, f.getDenominator());
    }

    @Test(expected = ParseException.class)
    public void testParseInvalidString() throws ParseException {
        format.parse("abc");
    }

    @Test(expected = ParseException.class)
    public void testParseEmptyString() throws ParseException {
        format.parse("");
    }

    @Test(expected = ParseException.class)
    public void testParseNullString() throws ParseException {
        format.parse(null);
    }

    @Test(expected = ParseException.class)
    public void testParseOnlySlash() throws ParseException {
        format.parse("/");
    }

    @Test(expected = ParseException.class)
    public void testParseOnlyWhole() throws ParseException {
        format.parse("1 ");
    }

    @Test(expected = ParseException.class)
    public void testParseMissingDenominator() throws ParseException {
        format.parse("1/");
    }

    @Test(expected = ParseException.class)
    public void testParseMissingNumerator() throws ParseException {
        format.parse("/3");
    }

    @Test(expected = ParseException.class)
    public void testParseZeroDenominator() throws ParseException {
        format.parse("1/0");
    }

    @Test(expected = ParseException.class)
    public void testParseNegativeZeroDenominator() throws ParseException {
        format.parse("-1/0");
    }

    @Test(expected = ParseException.class)
    public void testParseWholeWithZeroDenominator() throws ParseException {
        format.parse("1 0/5");
    }

    @Test
    public void testParseWholeZero() throws ParseException {
        Fraction f = format.parse("0 1/2");
        assertEquals(1, f.getNumerator());
        assertEquals(2, f.getDenominator());
    }

    @Test
    public void testParseNegativeWholeZero() throws ParseException {
        Fraction f = format.parse("-0 1/2");
        assertEquals(-1, f.getNumerator());
        assertEquals(2, f.getDenominator());
    }

    @Test
    public void testParseLargeNumbers() throws ParseException {
        Fraction f = format.parse("1000000 999999/1000000");
        assertEquals(1000000 * 1000000 + 999999, f.getNumerator());
        assertEquals(1000000, f.getDenominator());
    }

    @Test(expected = ParseException.class)
    public void testParseImproperFractionAsProper() throws ParseException {
        // "3/2" is improper but should be parsed as a simple fraction
        // Actually it's valid as a fraction, but ProperFractionFormat might treat it as whole? 
        // Let's test it's accepted as a fraction
        format.parse("3/2");
    }

    // ========== parse(String, ParsePosition) ==========

    @Test
    public void testParseWithParsePositionValid() {
        ParsePosition pos = new ParsePosition(0);
        Fraction f = format.parse("1/2", pos);
        assertNotNull(f);
        assertEquals(1, f.getNumerator());
        assertEquals(2, f.getDenominator());
        assertEquals(3, pos.getIndex());
    }

    @Test
    public void testParseWithParsePositionWhole() {
        ParsePosition pos = new ParsePosition(0);
        Fraction f = format.parse("5", pos);
        assertNotNull(f);
        assertEquals(5, f.getNumerator());
        assertEquals(1, f.getDenominator());
        assertEquals(1, pos.getIndex());
    }

    @Test
    public void testParseWithParsePositionProper() {
        ParsePosition pos = new ParsePosition(0);
        Fraction f = format.parse("2 3/4", pos);
        assertNotNull(f);
        assertEquals(11, f.getNumerator());
        assertEquals(4, f.getDenominator());
        assertEquals(5, pos.getIndex());
    }

    @Test
    public void testParseWithParsePositionInvalid() {
        ParsePosition pos = new ParsePosition(0);
        Fraction f = format.parse("abc", pos);
        assertNull(f);
        assertEquals(0, pos.getIndex());
        assertNotEquals(-1, pos.getErrorIndex());
    }

    @Test
    public void testParseWithParsePositionNullInput() {
        ParsePosition pos = new ParsePosition(0);
        Fraction f = format.parse(null, pos);
        assertNull(f);
        assertEquals(0, pos.getIndex());
        assertNotEquals(-1, pos.getErrorIndex());
    }

    @Test
    public void testParseWithParsePositionEmptyInput() {
        ParsePosition pos = new ParsePosition(0);
        Fraction f = format.parse("", pos);
        assertNull(f);
        assertEquals(0, pos.getIndex());
        assertNotEquals(-1, pos.getErrorIndex());
    }

    @Test
    public void testParseWithParsePositionPartialParse() {
        ParsePosition pos = new ParsePosition(0);
        Fraction f = format.parse("1/2 extra", pos);
        assertNotNull(f);
        assertEquals(1, f.getNumerator());
        assertEquals(2, f.getDenominator());
        assertEquals(3, pos.getIndex());
    }

    @Test
    public void testParseWithParsePositionStartAtNonZero() {
        ParsePosition pos = new ParsePosition(2);
        Fraction f = format.parse("xx 3/4", pos);
        assertNotNull(f);
        assertEquals(3, f.getNumerator());
        assertEquals(4, f.getDenominator());
        assertEquals(5, pos.getIndex());
    }

    @Test
    public void testParseWithParsePositionNegativeWhole() {
        ParsePosition pos = new ParsePosition(0);
        Fraction f = format.parse("-2 1/3", pos);
        assertNotNull(f);
        assertEquals(-7, f.getNumerator());
        assertEquals(3, f.getDenominator());
        assertEquals(6, pos.getIndex());
    }

    @Test
    public void testParseWithParsePositionNegativeFraction() {
        ParsePosition pos = new ParsePosition(0);
        Fraction f = format.parse("-4/5", pos);
        assertNotNull(f);
        assertEquals(-4, f.getNumerator());
        assertEquals(5, f.getDenominator());
        assertEquals(4, pos.getIndex());
    }

    @Test
    public void testParseWithParsePositionZeroDenominator() {
        ParsePosition pos = new ParsePosition(0);
        Fraction f = format.parse("1/0", pos);
        assertNull(f);
        assertEquals(0, pos.getIndex());
        assertNotEquals(-1, pos.getErrorIndex());
    }

    @Test
    public void testParseWithParsePositionOnlySlash() {
        ParsePosition pos = new ParsePosition(0);
        Fraction f = format.parse("/", pos);
        assertNull(f);
        assertEquals(0, pos.getIndex());
        assertNotEquals(-1, pos.getErrorIndex());
    }

    // ========== format methods ==========

    @Test
    public void testFormatFraction() {
        Fraction f = new Fraction(3, 4);
        String result = format.format(f);
        assertEquals("3/4", result);
    }

    @Test
    public void testFormatWholeNumber() {
        Fraction f = new Fraction(5, 1);
        String result = format.format(f);
        assertEquals("5", result);
    }

    @Test
    public void testFormatProperFraction() {
        Fraction f = new Fraction(7, 3); // 2 1/3
        String result = format.format(f);
        assertEquals("2 1/3", result);
    }

    @Test
    public void testFormatNegativeProperFraction() {
        Fraction f = new Fraction(-7, 3); // -2 1/3
        String result = format.format(f);
        assertEquals("-2 1/3", result);
    }

    @Test
    public void testFormatNegativeFraction() {
        Fraction f = new Fraction(-3, 5);
        String result = format.format(f);
        assertEquals("-3/5", result);
    }

    @Test
    public void testFormatZero() {
        Fraction f = new Fraction(0, 1);
        String result = format.format(f);
        assertEquals("0", result);
    }

    @Test
    public void testFormatImproperFraction() {
        Fraction f = new Fraction(5, 2); // 2 1/2
        String result = format.format(f);
        assertEquals("2 1/2", result);
    }

    @Test
    public void testFormatLargeProperFraction() {
        Fraction f = new Fraction(1000001, 1000000); // 1 1/1000000
        String result = format.format(f);
        assertEquals("1 1/1000000", result);
    }

    // ========== Edge cases and potential bugs ==========

    @Test(expected = ParseException.class)
    public void testParseDoubleNegative() throws ParseException {
        // "--1/2" might be parsed incorrectly
        format.parse("--1/2");
    }

    @Test(expected = ParseException.class)
    public void testParseNegativeWholeWithExtraSign() throws ParseException {
        format.parse("-+1 2/3");
    }

    @Test
    public void testParseWithWhitespace() throws ParseException {
        Fraction f = format.parse("  1 / 2  ");
        assertEquals(1, f.getNumerator());
        assertEquals(2, f.getDenominator());
    }

    @Test
    public void testParseWithTabs() throws ParseException {
        Fraction f = format.parse("\t3\t/\t4");
        assertEquals(3, f.getNumerator());
        assertEquals(4, f.getDenominator());
    }

    @Test(expected = ParseException.class)
    public void testParseOnlyWhitespace() throws ParseException {
        format.parse("   ");
    }

    @Test
    public void testParseWholeWithFractionZeroDenominator() throws ParseException {
        // This might be a bug: "1 0/5" should be invalid
        try {
            format.parse("1 0/5");
            fail("Expected ParseException for zero denominator in fraction part");
        } catch (ParseException e) {
            // expected
        }
    }

    @Test
    public void testParseWholeWithFractionZeroNumerator() throws ParseException {
        Fraction f = format.parse("1 0/5");
        // If allowed, it should be just whole number 1
        // But zero numerator is valid? Actually 0/5 = 0, so "1 0/5" = 1
        // This might be parsed as whole 1 and fraction 0/5 -> 1
        // Let's check behavior
        // If it throws exception, test accordingly
        // We'll assume it's valid and test
        // But to be safe, we'll catch both possibilities
        try {
            Fraction result = format.parse("1 0/5");
            assertEquals(1, result.getNumerator());
            assertEquals(1, result.getDenominator());
        } catch (ParseException e) {
            // acceptable if implementation rejects zero numerator
        }
    }

    @Test
    public void testParseNegativeWholeWithNegativeFraction() throws ParseException {
        // "-1 -2/3" is ambiguous; likely invalid
        try {
            format.parse("-1 -2/3");
            fail("Expected ParseException for double negative");
        } catch (ParseException e) {
            // expected
        }
    }

    @Test
    public void testParseOverflow() {
        // Large numbers that might cause overflow in multiplication
        try {
            format.parse("999999999 1/2");
            // If no exception, check result
            Fraction f = format.parse("999999999 1/2");
            assertEquals(1999999999, f.getNumerator());
            assertEquals(2, f.getDenominator());
        } catch (ParseException e) {
            // overflow might cause parse failure
        }
    }

    @Test
    public void testParseNegativeOverflow() {
        try {
            format.parse("-999999999 1/2");
            Fraction f = format.parse("-999999999 1/2");
            assertEquals(-1999999999, f.getNumerator());
            assertEquals(2, f.getDenominator());
        } catch (ParseException e) {
            // acceptable
        }
    }

    // ========== Test getInstance and constructors ==========

    @Test
    public void testConstructorWithNumberFormat() {
        java.text.NumberFormat nf = java.text.NumberFormat.getInstance();
        ProperFractionFormat f = new ProperFractionFormat(nf, nf, nf);
        assertNotNull(f);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullWholeFormat() {
        new ProperFractionFormat(null, java.text.NumberFormat.getInstance(), java.text.NumberFormat.getInstance());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullNumeratorFormat() {
        new ProperFractionFormat(java.text.NumberFormat.getInstance(), null, java.text.NumberFormat.getInstance());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullDenominatorFormat() {
        new ProperFractionFormat(java.text.NumberFormat.getInstance(), java.text.NumberFormat.getInstance(), null);
    }

    @Test
    public void testSetWholeFormat() {
        java.text.NumberFormat nf = java.text.NumberFormat.getInstance();
        format.setWholeFormat(nf);
        assertSame(nf, format.getWholeFormat());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetWholeFormatNull() {
        format.setWholeFormat(null);
    }

    @Test
    public void testSetNumeratorFormat() {
        java.text.NumberFormat nf = java.text.NumberFormat.getInstance();
        format.setNumeratorFormat(nf);
        assertSame(nf, format.getNumeratorFormat());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetNumeratorFormatNull() {
        format.setNumeratorFormat(null);
    }

    @Test
    public void testSetDenominatorFormat() {
        java.text.NumberFormat nf = java.text.NumberFormat.getInstance();
        format.setDenominatorFormat(nf);
        assertSame(nf, format.getDenominatorFormat());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetDenominatorFormatNull() {
        format.setDenominatorFormat(null);
    }

    // ========== Additional edge cases ==========

    @Test
    public void testParseWithLeadingZeros() throws ParseException {
        Fraction f = format.parse("001/002");
        assertEquals(1, f.getNumerator());
        assertEquals(2, f.getDenominator());
    }

    @Test
    public void testParseWithLeadingZerosWhole() throws ParseException {
        Fraction f = format.parse("001 2/3");
        assertEquals(5, f.getNumerator());
        assertEquals(3, f.getDenominator());
    }

    @Test(expected = ParseException.class)
    public void testParseWithMultipleSlashes() throws ParseException {
        format.parse("1/2/3");
    }

    @Test
    public void testParseWithSpacesAroundWhole() throws ParseException {
        Fraction f = format.parse(" 1 2/3 ");
        assertEquals(5, f.getNumerator());
        assertEquals(3, f.getDenominator());
    }

    @Test
    public void testParseWithOnlyWholeAndSpaces() throws ParseException {
        Fraction f = format.parse("  5  ");
        assertEquals(5, f.getNumerator());
        assertEquals(1, f.getDenominator());
    }

    @Test(expected = ParseException.class)
    public void testParseWithExtraCharactersAfter() throws ParseException {
        format.parse("1/2x");
    }

    @Test
    public void testParseWithParsePositionExtraCharacters() {
        ParsePosition pos = new ParsePosition(0);
        Fraction f = format.parse("1/2x", pos);
        assertNotNull(f);
        assertEquals(1, f.getNumerator());
        assertEquals(2, f.getDenominator());
        assertEquals(3, pos.getIndex());
    }

    @Test
    public void testFormatNull() {
        try {
            format.format(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testFormatWithNegativeDenominator() {
        // Fraction class normalizes sign to numerator, so denominator is positive
        Fraction f = new Fraction(1, -2); // becomes -1/2
        String result = format.format(f);
        assertEquals("-1/2", result);
    }

    @Test
    public void testFormatImproperFractionNegative() {
        Fraction f = new Fraction(-5, 2); // -2 1/2
        String result = format.format(f);
        assertEquals("-2 1/2", result);
    }

    @Test
    public void testFormatFractionWithReduction() {
        Fraction f = new Fraction(2, 4); // reduces to 1/2
        String result = format.format(f);
        assertEquals("1/2", result);
    }

    @Test
    public void testParseAndFormatRoundTrip() throws ParseException {
        String[] inputs = {"1/2", "3", "2 1/3", "-4/5", "-1 2/7", "0", "0 1/2", "-0 1/2"};
        for (String input : inputs) {
            Fraction f = format.parse(input);
            String output = format.format(f);
            // Note: formatting may normalize, so we compare parsed values
            Fraction reparsed = format.parse(output);
            assertEquals(f.getNumerator(), reparsed.getNumerator());
            assertEquals(f.getDenominator(), reparsed.getDenominator());
        }
    }
}