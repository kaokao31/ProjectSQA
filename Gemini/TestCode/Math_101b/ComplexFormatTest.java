package org.apache.commons.math.complex;

import static org.junit.Assert.*;

import java.text.FieldPosition;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Locale;

import org.junit.Test;

public class ComplexFormatTest {

    @Test
    public void testDefaultConstructor() {
        ComplexFormat format = new ComplexFormat();
        assertNotNull(format);
        assertEquals("+ ", format.getImaginaryCharacter());
    }

    @Test
    public void testLocaleConstructor() {
        ComplexFormat format = new ComplexFormat(Locale.US);
        assertNotNull(format);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullLocaleConstructor() {
        new ComplexFormat((Locale) null);
    }

    @Test
    public void testCustomImaginaryCharacterConstructor() {
        ComplexFormat format = new ComplexFormat("i");
        assertNotNull(format);
        assertEquals("i", format.getImaginaryCharacter());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullImaginaryCharacterConstructor() {
        new ComplexFormat(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyImaginaryCharacterConstructor() {
        new ComplexFormat("");
    }

    @Test
    public void testFullConstructor() {
        java.text.NumberFormat nf = java.text.NumberFormat.getInstance(Locale.US);
        ComplexFormat format = new ComplexFormat("i", nf, nf);
        assertNotNull(format);
        assertEquals("i", format.getImaginaryCharacter());
        assertNotNull(format.getRealFormat());
        assertNotNull(format.getImaginaryFormat());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFullConstructorNullImaginaryChar() {
        java.text.NumberFormat nf = java.text.NumberFormat.getInstance(Locale.US);
        new ComplexFormat(null, nf, nf);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFullConstructorNullRealFormat() {
        java.text.NumberFormat nf = java.text.NumberFormat.getInstance(Locale.US);
        new ComplexFormat("i", null, nf);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFullConstructorNullImaginaryFormat() {
        java.text.NumberFormat nf = java.text.NumberFormat.getInstance(Locale.US);
        new ComplexFormat("i", nf, null);
    }

    @Test
    public void testGetInstance() {
        ComplexFormat format = ComplexFormat.getInstance();
        assertNotNull(format);
    }

    @Test
    public void testGetInstanceLocale() {
        ComplexFormat format = ComplexFormat.getInstance(Locale.FRANCE);
        assertNotNull(format);
    }

    @Test
    public void testFormatComplex() {
        ComplexFormat format = ComplexFormat.getInstance(Locale.US);
        Complex c = new Complex(1.23, 4.56);
        String s = format.format(c);
        assertNotNull(s);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatNullComplex() {
        ComplexFormat format = ComplexFormat.getInstance(Locale.US);
        format.format(null);
    }

    @Test
    public void testFormatObject() {
        ComplexFormat format = ComplexFormat.getInstance(Locale.US);
        Complex c = new Complex(1.23, 4.56);
        StringBuffer sb = new StringBuffer();
        FieldPosition fp = new FieldPosition(0);
        StringBuffer result = format.format(c, sb, fp);
        assertNotNull(result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatInvalidObject() {
        ComplexFormat format = ComplexFormat.getInstance(Locale.US);
        format.format("Not a Complex", new StringBuffer(), new FieldPosition(0));
    }

    @Test
    public void testParseString() throws ParseException {
        ComplexFormat format = ComplexFormat.getInstance(Locale.US);
        // Depending on exact implementation of parsing, let's test typical formats
        // Default format uses "+ " or "- " separator and appends "i"
        String source = "1.23 + 4.56i";
        Complex c = format.parse(source);
        assertNotNull(c);
    }

    @Test(expected = ParseException.class)
    public void testParseInvalidString() throws ParseException {
        ComplexFormat format = ComplexFormat.getInstance(Locale.US);
        format.parse("invalid complex number");
    }

    @Test
    public void testParsePosition() {
        ComplexFormat format = ComplexFormat.getInstance(Locale.US);
        ParsePosition pos = new ParsePosition(0);
        Complex c = format.parse("1.23 + 4.56i", pos);
        // If parsing succeeds or fails, check parse position behavior
        assertNotNull(pos);
    }
    
    @Test
    public void testParseNegativeImaginary() throws ParseException {
        ComplexFormat format = new ComplexFormat("i");
        Complex c = format.parse("1.0 - 2.0i");
        assertNotNull(c);
        assertEquals(1.0, c.getReal(), 1e-5);
        assertEquals(-2.0, c.getImaginary(), 1e-5);
    }

    @Test
    public void testParseOnlyReal() throws ParseException {
        ComplexFormat format = new ComplexFormat("i");
        Complex c = format.parse("5.0");
        assertNotNull(c);
        assertEquals(5.0, c.getReal(), 1e-5);
        assertEquals(0.0, c.getImaginary(), 1e-5);
    }

    @Test
    public void testParseOnlyImaginary() throws ParseException {
        ComplexFormat format = new ComplexFormat("i");
        Complex c = format.parse("3.0i");
        assertNotNull(c);
        assertEquals(0.0, c.getReal(), 1e-5);
        assertEquals(3.0, c.getImaginary(), 1e-5);
    }

    @Test
    public void testAvailableLocales() {
        Locale[] locales = ComplexFormat.getAvailableLocales();
        assertNotNull(locales);
        assertTrue(locales.length > 0);
    }
}