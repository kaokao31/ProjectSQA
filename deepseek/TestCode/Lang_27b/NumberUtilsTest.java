package org.apache.commons.lang3.math;

import junit.framework.TestCase;
import org.junit.Test;
import static org.junit.Assert.*;

public class NumberUtilsTest {

    // Test createNumber method to cover various formats and edge cases
    @Test
    public void testCreateNumberNull() {
        assertNull("null input should return null", NumberUtils.createNumber(null));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberEmptyString() {
        NumberUtils.createNumber("");
    }

    @Test
    public void testCreateNumberHex() {
        assertEquals("0x0 should be Integer 0", Integer.valueOf(0), NumberUtils.createNumber("0x0"));
        assertEquals("0X0 should be Integer 0", Integer.valueOf(0), NumberUtils.createNumber("0X0"));
        assertEquals("0x10 should be Integer 16", Integer.valueOf(16), NumberUtils.createNumber("0x10"));
        assertEquals("#10 should be Integer 16", Integer.valueOf(16), NumberUtils.createNumber("#10"));
        assertEquals("-0x10 should be Integer -16", Integer.valueOf(-16), NumberUtils.createNumber("-0x10"));
        assertEquals("0xABCDEF should be Integer", Integer.valueOf(0xABCDEF), NumberUtils.createNumber("0xABCDEF"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberHexEmpty() {
        // Trigger the bug: StringIndexOutOfBoundsException -> Defects4J #27
        NumberUtils.createNumber("0x");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberHexEmptyAlt() {
        NumberUtils.createNumber("0X");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberHexEmptyHash() {
        NumberUtils.createNumber("#");
    }

    @Test
    public void testCreateNumberOctal() {
        assertEquals("07 should be Integer 7", Integer.valueOf(7), NumberUtils.createNumber("07"));
        assertEquals("010 should be Integer 8", Integer.valueOf(8), NumberUtils.createNumber("010"));
        assertEquals("-011 should be Integer -9", Integer.valueOf(-9), NumberUtils.createNumber("-011"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberOctalInvalid() {
        NumberUtils.createNumber("08"); // contains digit 8 in octal
    }

    @Test
    public void testCreateNumberDecimal() {
        assertEquals("123 should be Integer 123", Integer.valueOf(123), NumberUtils.createNumber("123"));
        assertEquals("-456 should be Long -456", Long.valueOf(-456), NumberUtils.createNumber("-456"));
        assertEquals("123.0 should be Float 123.0", Float.valueOf(123.0f), NumberUtils.createNumber("123.0"));
        assertEquals("-456.789 should be Double -456.789", Double.valueOf(-456.789), NumberUtils.createNumber("-456.789"));
    }

    @Test
    public void testCreateNumberWithExponent() {
        assertEquals("1e2 should be Double 100.0", Double.valueOf(100.0), NumberUtils.createNumber("1e2"));
        assertEquals("-2.5e-1 should be Double -0.25", Double.valueOf(-0.25), NumberUtils.createNumber("-2.5e-1"));
        assertEquals("10E3 should be Double 10000.0", Double.valueOf(10000.0), NumberUtils.createNumber("10E3"));
    }

    @Test
    public void testCreateNumberWithSign() {
        assertEquals("+100 should be Integer 100", Integer.valueOf(100), NumberUtils.createNumber("+100"));
        assertEquals("-100 should be Integer -100", Integer.valueOf(-100), NumberUtils.createNumber("-100"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberMultipleDots() {
        NumberUtils.createNumber("1.2.3");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberInvalidChars() {
        NumberUtils.createNumber("12a34");
    }

    @Test
    public void testCreateNumberLongSuffix() {
        assertEquals("1L should be Long 1", Long.valueOf(1), NumberUtils.createNumber("1L"));
        assertEquals("-2L should be Long -2", Long.valueOf(-2), NumberUtils.createNumber("-2L"));
    }

    @Test
    public void testCreateNumberFloatSuffix() {
        assertEquals("1.5f should be Float 1.5", Float.valueOf(1.5f), NumberUtils.createNumber("1.5f"));
        assertEquals("-2.0F should be Float -2.0", Float.valueOf(-2.0f), NumberUtils.createNumber("-2.0F"));
    }

    @Test
    public void testCreateNumberDoubleSuffix() {
        assertEquals("1.5d should be Double 1.5", Double.valueOf(1.5), NumberUtils.createNumber("1.5d"));
        assertEquals("-2.0D should be Double -2.0", Double.valueOf(-2.0), NumberUtils.createNumber("-2.0D"));
    }

    // Test isNumber method
    @Test
    public void testIsNumberNull() {
        assertFalse("null should not be number", NumberUtils.isNumber(null));
    }

    @Test
    public void testIsNumberEmpty() {
        assertFalse("empty string should not be number", NumberUtils.isNumber(""));
    }

    @Test
    public void testIsNumberHex() {
        assertTrue("0x0 should be number", NumberUtils.isNumber("0x0"));
        assertTrue("#10 should be number", NumberUtils.isNumber("#10"));
        assertFalse("0x should not be number", NumberUtils.isNumber("0x"));
        assertFalse("# should not be number", NumberUtils.isNumber("#"));
    }

    @Test
    public void testIsNumberDecimal() {
        assertTrue("123 should be number", NumberUtils.isNumber("123"));
        assertTrue("-456.789 should be number", NumberUtils.isNumber("-456.789"));
        assertFalse("12.34.56 should not be number", NumberUtils.isNumber("12.34.56"));
    }

    @Test
    public void testIsNumberOctal() {
        assertTrue("07 should be number", NumberUtils.isNumber("07"));
        assertFalse("08 should not be number", NumberUtils.isNumber("08"));
    }

    @Test
    public void testIsNumberWithSuffix() {
        assertTrue("1L should be number", NumberUtils.isNumber("1L"));
        assertTrue("1.5f should be number", NumberUtils.isNumber("1.5f"));
        assertTrue("2.0d should be number", NumberUtils.isNumber("2.0d"));
        assertFalse("1.2.3f should not be number", NumberUtils.isNumber("1.2.3f"));
    }

    // Additional tests for other methods can be added, but focus on createNumber
    @Test
    public void testToInt() {
        assertEquals(0, NumberUtils.toInt(null, 0));
        assertEquals(123, NumberUtils.toInt("123", 0));
        assertEquals(-1, NumberUtils.toInt("abc", -1));
    }

    @Test
    public void testToLong() {
        assertEquals(0L, NumberUtils.toLong(null, 0L));
        assertEquals(123L, NumberUtils.toLong("123", 0L));
        assertEquals(-1L, NumberUtils.toLong("abc", -1L));
    }

    @Test
    public void testToFloat() {
        assertEquals(0.0f, NumberUtils.toFloat(null, 0.0f), 0.0);
        assertEquals(1.5f, NumberUtils.toFloat("1.5", 0.0f), 0.0);
        assertEquals(-1.0f, NumberUtils.toFloat("abc", -1.0f), 0.0);
    }

    @Test
    public void testToDouble() {
        assertEquals(0.0, NumberUtils.toDouble(null, 0.0), 0.0);
        assertEquals(2.5, NumberUtils.toDouble("2.5", 0.0), 0.0);
        assertEquals(-1.0, NumberUtils.toDouble("abc", -1.0), 0.0);
    }

    @Test
    public void testCreateNumberPerformance() {
        // Not a real performance test, but ensures no infinite loop or crash
        NumberUtils.createNumber("123");
        NumberUtils.createNumber("0x10");
        NumberUtils.createNumber("1.5e10");
    }

    // Explicit test for the reported bug (Defects4J #27)
    @Test(expected = NumberFormatException.class)
    public void testCreateNumberBug27() {
        // This call previously caused StringIndexOutOfBoundsException
        NumberUtils.createNumber("0x");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberBug27Alternative() {
        NumberUtils.createNumber("0X");
    }

    // Additional edge cases
    @Test(expected = NumberFormatException.class)
    public void testCreateNumberHexWithOnlyPrefix() {
        NumberUtils.createNumber("-0x");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberHashWithOnlyPrefix() {
        NumberUtils.createNumber("#");
    }

    @Test
    public void testCreateNumberMaxLong() {
        assertEquals("9223372036854775807 should be Long MAX", Long.valueOf(Long.MAX_VALUE), NumberUtils.createNumber("9223372036854775807"));
    }

    @Test
    public void testCreateNumberMinLong() {
        assertEquals("-9223372036854775808 should be Long MIN", Long.valueOf(Long.MIN_VALUE), NumberUtils.createNumber("-9223372036854775808"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberOverflowLong() {
        NumberUtils.createNumber("9223372036854775808"); // too large for long
    }

    @Test
    public void testCreateNumberFloatPrecision() {
        assertEquals("3.4028235e38 should be float max", Float.valueOf(Float.MAX_VALUE), NumberUtils.createNumber("3.4028235e38"));
    }

    @Test
    public void testCreateNumberDoublePrecision() {
        assertEquals("1.7976931348623157e308 should be double max", Double.valueOf(Double.MAX_VALUE), NumberUtils.createNumber("1.7976931348623157e308"));
    }
}