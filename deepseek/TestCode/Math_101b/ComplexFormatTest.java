package org.apache.commons.math.complex;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

public class ComplexFormatTest {

    private ComplexFormat formatter;
    private NumberFormat realFormat;
    private NumberFormat imaginaryFormat;

    @Before
    public void setUp() {
        realFormat = new DecimalFormat("0.0", DecimalFormatSymbols.getInstance(Locale.US));
        imaginaryFormat = new DecimalFormat("0.0", DecimalFormatSymbols.getInstance(Locale.US));
        formatter = new ComplexFormat(realFormat, imaginaryFormat, "i");
    }

    private void assertComplexEquals(Complex expected, Complex actual) {
        assertNotNull(actual);
        assertEquals(expected.getReal(), actual.getReal(), 0.0);
        assertEquals(expected.getImaginary(), actual.getImaginary(), 0.0);
    }

    @Test
    public void testFormatPositive() {
        assertEquals("1.0 + 2.0i", formatter.format(new Complex(1.0, 2.0)));
        assertEquals("0.0 + 0.0i", formatter.format(new Complex(0.0, 0.0)));
        assertEquals("0.0 + 2.0i", formatter.format(new Complex(0.0, 2.0)));
        assertEquals("1.0 + 0.0i", formatter.format(new Complex(1.0, 0.0)));
    }

    @Test
    public void testFormatNegativeImaginary() {
        assertEquals("1.0 - 2.5i", formatter.format(new Complex(1.0, -2.5)));
        assertEquals("-1.0 - 2.5i", formatter.format(new Complex(-1.0, -2.5)));
    }

    @Test
    public void testFormatNegativeReal() {
        assertEquals("-1.0 + 2.0i", formatter.format(new Complex(-1.0, 2.0)));
    }

    @Test
    public void testFormatNegativeZeroImaginary() {
        assertEquals("1.0 - 0.0i", formatter.format(new Complex(1.0, -0.0)));
        assertEquals("-1.0 - 0.0i", formatter.format(new Complex(-1.0, -0.0)));
        assertEquals("0.0 - 0.0i", formatter.format(new Complex(0.0, -0.0)));
    }

    @Test
    public void testFormatWithCustomImaginaryCharacter() {
        ComplexFormat jFormatter = new ComplexFormat(realFormat, imaginaryFormat, "j");
        assertEquals("1.0 + 2.0j", jFormatter.format(new Complex(1.0, 2.0)));
        assertEquals("1.0 - 2.0j", jFormatter.format(new Complex(1.0, -2.0)));
    }

    @Test
    public void testFormatObjectComplex() {
        assertEquals("1.0 - 2.0i", formatter.format((Object) new Complex(1.0, -2.0)));
    }

    @Test
    public void testFormatObjectNumber() {
        assertEquals("2.5 + 0.0i", formatter.format((Object) 2.5));
    }

    @Test
    public void testFormatObjectInvalid() {
        try {
            formatter.format((Object) "not a complex number");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testFormatAppendToExistingStringBuffer() {
        StringBuffer sb = new StringBuffer("prefix:");
        StringBuffer result = formatter.format(new Complex(1.0, 2.0), sb, new FieldPosition(0));
        assertSame(sb, result);
        assertEquals("prefix:1.0 + 2.0i", result.toString());
    }

    @Test
    public void testParseValid() throws Exception {
        assertComplexEquals(new Complex(1.0, 2.0), formatter.parse("1.0 + 2.0i"));
        assertComplexEquals(new Complex(1.0, -2.0), formatter.parse("1.0 - 2.0i"));
        assertComplexEquals(new Complex(-1.0, 2.0), formatter.parse("-1.0 + 2.0i"));
        assertComplexEquals(new Complex(-1.0, -2.0), formatter.parse("-1.0 - 2.0i"));
    }

    @Test
    public void testParseValidWithoutSpaces() throws Exception {
        assertComplexEquals(new Complex(1.0, 2.0), formatter.parse("1.0+2.0i"));
        assertComplexEquals(new Complex(1.0, -2.0), formatter.parse("1.0-2.0i"));
    }

    @Test
    public void testParseWithWhitespace() throws Exception {
        assertComplexEquals(new Complex(1.0, 2.0), formatter.parse("  1.0 + 2.0i  "));
        assertComplexEquals(new Complex(1.0, -2.0), formatter.parse("  1.0 - 2.0i  "));
    }

    @Test
    public void testParseRealOnly() throws Exception {
        assertComplexEquals(new Complex(1.0, 0.0), formatter.parse("1.0"));
        assertComplexEquals(new Complex(-1.0, 0.0), formatter.parse("-1.0"));
    }

    @Test(expected = ParseException.class)
    public void testParseInvalidMissingImaginaryCharacter() throws Exception {
        formatter.parse("1.0 + 2.0");
    }

    @Test(expected = ParseException.class)
    public void testParseInvalidMissingImaginaryValue() throws Exception {
        formatter.parse("1.0 + i");
    }

    @Test(expected = ParseException.class)
    public void testParseInvalidWrongImaginaryCharacter() throws Exception {
        formatter.parse("1.0 + 2.0x");
    }

    @Test(expected = ParseException.class)
    public void testParseInvalidSign() throws Exception {
        formatter.parse("1.0 @ 2.0i");
    }

    @Test(expected = ParseException.class)
    public void testParseInvalidLeadingText() throws Exception {
        formatter.parse("hello");
    }

    @Test
    public void testParsePositionSuccess() {
        ParsePosition pos = new ParsePosition(0);
        Complex c = formatter.parse("1.0 + 2.0i", pos);
        assertComplexEquals(new Complex(1.0, 2.0), c);
        assertEquals(10, pos.getIndex());
    }

    @Test
    public void testParsePositionWithLeadingWhitespace() {
        ParsePosition pos = new ParsePosition(0);
        Complex c = formatter.parse("  1.0 + 2.0i", pos);
        assertComplexEquals(new Complex(1.0, 2.0), c);
        assertEquals(12, pos.getIndex());
    }

    @Test
    public void testParsePositionWithNonZeroInitialIndex() {
        ParsePosition pos = new ParsePosition(5);
        Complex c = formatter.parse("xxxxx1.0 + 2.0i", pos);
        assertComplexEquals(new Complex(1.0, 2.0), c);
        assertEquals(15, pos.getIndex());
    }

    @Test
    public void testParsePositionInvalidSign() {
        ParsePosition pos = new ParsePosition(0);
        Complex c = formatter.parse("1.0 @ 2.0i", pos);
        assertNull(c);
        assertEquals(0, pos.getIndex());
        assertTrue(pos.getErrorIndex() >= 0);
    }

    @Test
    public void testParseObject() {
        Object obj = formatter.parseObject("1.0 + 2.0i", new ParsePosition(0));
        assertTrue(obj instanceof Complex);
        if (obj instanceof Complex) {
            assertComplexEquals(new Complex(1.0, 2.0), (Complex) obj);
        }
    }

    @Test
    public void testGettersAndSetters() {
        NumberFormat newReal = new DecimalFormat("0.00", DecimalFormatSymbols.getInstance(Locale.US));
        NumberFormat newImag = new DecimalFormat("0.00", DecimalFormatSymbols.getInstance(Locale.US));

        formatter.setRealFormat(newReal);
        formatter.setImaginaryFormat(newImag);
        formatter.setImaginaryCharacter("k");

        assertSame(newReal, formatter.getRealFormat());
        assertSame(newImag, formatter.getImaginaryFormat());
        assertEquals("k", formatter.getImaginaryCharacter());
    }

    @Test
    public void testDefaultImaginaryCharacter() {
        ComplexFormat defaultFormatter = new ComplexFormat();
        assertEquals("i", defaultFormatter.getImaginaryCharacter());
    }

    @Test
    public void testGetInstance() {
        assertNotNull(ComplexFormat.getInstance());
        assertEquals("i", ComplexFormat.getInstance().getImaginaryCharacter());
        assertNotNull(ComplexFormat.getInstance(Locale.US));
        assertEquals("i", ComplexFormat.getInstance(Locale.US).getImaginaryCharacter());
    }

    @Test
    public void testGetComplexFormat() {
        NumberFormat format = new DecimalFormat("0.0", DecimalFormatSymbols.getInstance(Locale.US));
        ComplexFormat cf = ComplexFormat.getComplexFormat(format);
        assertNotNull(cf);
        assertEquals("i", cf.getImaginaryCharacter());

        ComplexFormat cf2 = ComplexFormat.getComplexFormat(format, "k");
        assertNotNull(cf2);
        assertEquals("k", cf2.getImaginaryCharacter());
    }

    @Test
    public void testConstructorWithNumberFormatAndImaginaryCharacter() {
        ComplexFormat cf = new ComplexFormat(realFormat, "j");
        assertEquals("j", cf.getImaginaryCharacter());
        assertEquals("1.0 + 2.0j", cf.format(new Complex(1.0, 2.0)));
    }

    @Test
    public void testConstructorWithNumberFormat() {
        ComplexFormat cf = new ComplexFormat(realFormat);
        assertEquals("i", cf.getImaginaryCharacter());
        assertNotNull(cf.getRealFormat());
        assertNotNull(cf.getImaginaryFormat());
    }

    @Test
    public void testConstructorWithTwoNumberFormats() {
        ComplexFormat cf = new ComplexFormat(realFormat, imaginaryFormat);
        assertEquals("i", cf.getImaginaryCharacter());
    }

    @Test
    public void testEqualsAndHashCode() {
        ComplexFormat f1 = new ComplexFormat(realFormat, imaginaryFormat, "i");
        ComplexFormat f2 = new ComplexFormat(realFormat, imaginaryFormat, "i");
        assertTrue(f1.equals(f2));
        assertEquals(f1.hashCode(), f2.hashCode());
        assertFalse(f1.equals(null));
        assertFalse(f1.equals("not a format"));
        assertFalse(f1.equals(new ComplexFormat(realFormat, imaginaryFormat, "j")));
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullImaginaryCharacter() {
        new ComplexFormat((String) null);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullNumberFormat() {
        new ComplexFormat((NumberFormat) null);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullRealFormat() {
        new ComplexFormat(null, imaginaryFormat, "i");
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullImaginaryFormat() {
        new ComplexFormat(realFormat, null, "i");
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullImaginaryCharacterThreeArg() {
        new ComplexFormat(realFormat, imaginaryFormat, null);
    }

    @Test(expected = NullPointerException.class)
    public void testSetImaginaryCharacterNull() {
        formatter.setImaginaryCharacter(null);
    }

    @Test(expected = NullPointerException.class)
    public void testSetRealFormatNull() {
        formatter.setRealFormat(null);
    }

    @Test(expected = NullPointerException.class)
    public void testSetImaginaryFormatNull() {
        formatter.setImaginaryFormat(null);
    }
}