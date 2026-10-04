package org.apache.commons.math.fraction;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.text.ParseException;
import java.text.ParsePosition;

public class ProperFractionFormatTest {

    private ProperFractionFormat format;

    @Before
    public void setUp() {
        format = new ProperFractionFormat();
    }

    @Test
    public void testConstructorWithCustomFormats() {
        java.text.NumberFormat nf = java.text.NumberFormat.getInstance();
        ProperFractionFormat customFormat = new ProperFractionFormat(nf);
        assertNotNull(customFormat);

        ProperFractionFormat customBoth = new ProperFractionFormat(nf, nf, nf);
        assertNotNull(customBoth);
    }

    @Test
    public void testFormatFractionProper() {
        // Test zero
        Fraction fZero = new Fraction(0, 1);
        StringBuffer sb = new StringBuffer();
        format.format(fZero, sb, new java.text.FieldPosition(0));
        assertEquals("0 / 1", sb.toString());

        // Test proper fraction (numerator < denominator)
        Fraction fProper = new Fraction(1, 2);
        sb = new StringBuffer();
        format.format(fProper, sb, new java.text.FieldPosition(0));
        assertEquals("1 / 2", sb.toString());

        // Test improper fraction with whole part
        Fraction fImproper = new Fraction(5, 3); // 1 and 2/3
        sb = new StringBuffer();
        format.format(fImproper, sb, new java.text.FieldPosition(0));
        assertEquals("1 2 / 3", sb.toString());

        // Test negative improper fraction
        Fraction fNegative = new Fraction(-5, 3);
        sb = new StringBuffer();
        format.format(fNegative, sb, new java.text.FieldPosition(0));
        assertEquals("-1 2 / 3", sb.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatNonFractionThrowsException() {
        format.format("not a fraction", new StringBuffer(), new java.text.FieldPosition(0));
    }

    @Test
    public void testParseProperFraction() throws ParseException {
        // Standard proper fraction
        Fraction f1 = format.parse("1 / 2", new ParsePosition(0));
        assertNotNull(f1);
        assertEquals(1, f1.getNumerator());
        assertEquals(2, f1.getDenominator());

        // Whole number and fraction
        Fraction f2 = format.parse("1 1 / 2", new ParsePosition(0));
        assertNotNull(f2);
        assertEquals(3, f2.getNumerator());
        assertEquals(2, f2.getDenominator());

        // Negative whole number and fraction
        Fraction f3 = format.parse("-1 1 / 2", new ParsePosition(0));
        assertNotNull(f3);
        assertEquals(-3, f3.getNumerator());
        assertEquals(2, f3.getDenominator());

        // Only whole number
        Fraction f4 = format.parse("5", new ParsePosition(0));
        assertNotNull(f4);
        assertEquals(5, f4.getNumerator());
        assertEquals(1, f4.getDenominator());
    }

    @Test
    public void testParseFailures() {
        // Invalid syntax should set parse position error index and return null
        ParsePosition pos = new ParsePosition(0);
        Fraction f = format.parse("1 /", pos);
        assertNull(f);
        assertTrue(pos.getErrorIndex() >= 0);

        // Parse starting from invalid position or parsing completely invalid string
        pos = new ParsePosition(0);
        Fraction f2 = format.parse("abc", pos);
        assertNull(f2);
        assertTrue(pos.getErrorIndex() >= 0);
    }

    @Test
    public void testParseStringObject() throws ParseException {
        Object obj = format.parseObject("1 1 / 2", new ParsePosition(0));
        assertNotNull(obj);
        assertTrue(obj instanceof Fraction);
        Fraction f = (Fraction) obj;
        assertEquals(3, f.getNumerator());
        assertEquals(2, f.getDenominator());
    }

    @Test(expected = ParseException.class)
    public void testParseObjectFailure() throws ParseException {
        format.parseObject("invalid-fraction", new ParsePosition(0));
    }

    private void assertNotNull(Object obj) {
        Assert.assertNotNull(obj);
    }

    private void assertEquals(long expected, long actual) {
        Assert.assertEquals(expected, actual);
    }

    private void assertEquals(String expected, String actual) {
        Assert.assertEquals(expected, actual);
    }

    private void assertTrue(boolean condition) {
        Assert.assertTrue(condition);
    }

    private void assertNull(Object obj) {
        Assert.assertNull(obj);
    }
}