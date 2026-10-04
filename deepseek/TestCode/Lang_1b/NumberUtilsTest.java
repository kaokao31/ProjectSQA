package org.apache.commons.lang3.math;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Test suite for NumberUtils class (Apache Commons Lang 3.6).
 * Covers all public methods with maximum branch coverage and fault detection.
 */
public class NumberUtilsTest {

    // ==================== createNumber ====================

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberNull() {
        NumberUtils.createNumber(null);
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberEmpty() {
        NumberUtils.createNumber("");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberBlank() {
        NumberUtils.createNumber("   ");
    }

    @Test
    public void testCreateNumberInteger() {
        assertEquals(Integer.valueOf(42), NumberUtils.createNumber("42"));
        assertEquals(Integer.valueOf(-0), NumberUtils.createNumber("-0"));
        assertEquals(Integer.valueOf(0), NumberUtils.createNumber("0"));
        assertEquals(Integer.valueOf(Integer.MAX_VALUE), NumberUtils.createNumber("2147483647"));
        assertEquals(Integer.valueOf(Integer.MIN_VALUE), NumberUtils.createNumber("-2147483648"));
    }

    @Test
    public void testCreateNumberLong() {
        assertEquals(Long.valueOf(1234567890123L), NumberUtils.createNumber("1234567890123"));
        assertEquals(Long.valueOf(-999999999999L), NumberUtils.createNumber("-999999999999"));
        assertEquals(Long.valueOf(Long.MAX_VALUE), NumberUtils.createNumber("9223372036854775807"));
        assertEquals(Long.valueOf(Long.MIN_VALUE), NumberUtils.createNumber("-9223372036854775808"));
    }

    @Test
    public void testCreateNumberFloat() {
        assertEquals(Float.valueOf(3.14f), NumberUtils.createNumber("3.14"));
        assertEquals(Float.valueOf(-2.5f), NumberUtils.createNumber("-2.5"));
        assertEquals(Float.valueOf(0.0f), NumberUtils.createNumber("0.0"));
        assertEquals(Float.valueOf(Float.MAX_VALUE), NumberUtils.createNumber("3.4028235E38"));
        assertEquals(Float.valueOf(Float.MIN_VALUE), NumberUtils.createNumber("1.4E-45"));
    }

    @Test
    public void testCreateNumberDouble() {
        assertEquals(Double.valueOf(1.23456789), NumberUtils.createNumber("1.23456789"));
        assertEquals(Double.valueOf(-0.001), NumberUtils.createNumber("-0.001"));
        assertEquals(Double.valueOf(Double.MAX_VALUE), NumberUtils.createNumber("1.7976931348623157E308"));
        assertEquals(Double.valueOf(Double.MIN_VALUE), NumberUtils.createNumber("4.9E-324"));
    }

    @Test
    public void testCreateNumberHex() {
        assertEquals(Integer.valueOf(255), NumberUtils.createNumber("0xFF"));
        assertEquals(Integer.valueOf(-255), NumberUtils.createNumber("-0xFF"));
        assertEquals(Long.valueOf(0xABCDEF123L), NumberUtils.createNumber("0xABCDEF123"));
        assertEquals(Integer.valueOf(0), NumberUtils.createNumber("0x0"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberInvalidHex() {
        NumberUtils.createNumber("0xGGG");
    }

    @Test
    public void testCreateNumberWithLeadingZeros() {
        assertEquals(Integer.valueOf(5), NumberUtils.createNumber("005"));
        assertEquals(Integer.valueOf(-5), NumberUtils.createNumber("-005"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberMultipleDots() {
        NumberUtils.createNumber("1.2.3");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberTrailingDot() {
        NumberUtils.createNumber("123.");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberLeadingDot() {
        NumberUtils.createNumber(".5");
    }

    @Test
    public void testCreateNumberScientificNotation() {
        assertEquals(Double.valueOf(1e10), NumberUtils.createNumber("1e10"));
        assertEquals(Double.valueOf(-1e-5), NumberUtils.createNumber("-1e-5"));
        assertEquals(Double.valueOf(2.5E+3), NumberUtils.createNumber("2.5E+3"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberInvalidScientific() {
        NumberUtils.createNumber("1e");
    }

    @Test
    public void testCreateNumberWithSign() {
        assertEquals(Integer.valueOf(5), NumberUtils.createNumber("+5"));
        assertEquals(Integer.valueOf(-5), NumberUtils.createNumber("-5"));
    }

    // ==================== isNumber ====================

    @Test
    public void testIsNumberNull() {
        assertFalse(NumberUtils.isNumber(null));
    }

    @Test
    public void testIsNumberEmpty() {
        assertFalse(NumberUtils.isNumber(""));
    }

    @Test
    public void testIsNumberValidInteger() {
        assertTrue(NumberUtils.isNumber("123"));
        assertTrue(NumberUtils.isNumber("-123"));
        assertTrue(NumberUtils.isNumber("+123"));
        assertTrue(NumberUtils.isNumber("0"));
    }

    @Test
    public void testIsNumberValidHex() {
        assertTrue(NumberUtils.isNumber("0xFF"));
        assertTrue(NumberUtils.isNumber("-0xFF"));
        assertTrue(NumberUtils.isNumber("0xABCDEF"));
    }

    @Test
    public void testIsNumberValidFloat() {
        assertTrue(NumberUtils.isNumber("3.14"));
        assertTrue(NumberUtils.isNumber("-2.5"));
        assertTrue(NumberUtils.isNumber("1e10"));
        assertTrue(NumberUtils.isNumber("1.5e-3"));
    }

    @Test
    public void testIsNumberInvalid() {
        assertFalse(NumberUtils.isNumber("abc"));
        assertFalse(NumberUtils.isNumber("12 3"));
        assertFalse(NumberUtils.isNumber("0xGG"));
        assertFalse(NumberUtils.isNumber("1.2.3"));
        assertFalse(NumberUtils.isNumber("."));
        assertFalse(NumberUtils.isNumber("-"));
        assertFalse(NumberUtils.isNumber("+"));
    }

    // ==================== toInt ====================

    @Test
    public void testToIntNull() {
        assertEquals(0, NumberUtils.toInt(null));
        assertEquals(5, NumberUtils.toInt(null, 5));
    }

    @Test
    public void testToIntValid() {
        assertEquals(42, NumberUtils.toInt("42"));
        assertEquals(-1, NumberUtils.toInt("-1"));
        assertEquals(0, NumberUtils.toInt("0"));
    }

    @Test
    public void testToIntInvalid() {
        assertEquals(0, NumberUtils.toInt("abc"));
        assertEquals(10, NumberUtils.toInt("abc", 10));
    }

    @Test
    public void testToIntEmpty() {
        assertEquals(0, NumberUtils.toInt(""));
        assertEquals(7, NumberUtils.toInt("", 7));
    }

    // ==================== toLong ====================

    @Test
    public void testToLongNull() {
        assertEquals(0L, NumberUtils.toLong(null));
        assertEquals(100L, NumberUtils.toLong(null, 100L));
    }

    @Test
    public void testToLongValid() {
        assertEquals(1234567890123L, NumberUtils.toLong("1234567890123"));
        assertEquals(-1L, NumberUtils.toLong("-1"));
    }

    @Test
    public void testToLongInvalid() {
        assertEquals(0L, NumberUtils.toLong("notanumber"));
        assertEquals(42L, NumberUtils.toLong("notanumber", 42L));
    }

    // ==================== toFloat ====================

    @Test
    public void testToFloatNull() {
        assertEquals(0.0f, NumberUtils.toFloat(null), 0.0f);
        assertEquals(3.14f, NumberUtils.toFloat(null, 3.14f), 0.0f);
    }

    @Test
    public void testToFloatValid() {
        assertEquals(2.5f, NumberUtils.toFloat("2.5"), 0.0f);
        assertEquals(-1.0f, NumberUtils.toFloat("-1.0"), 0.0f);
    }

    @Test
    public void testToFloatInvalid() {
        assertEquals(0.0f, NumberUtils.toFloat("abc"), 0.0f);
        assertEquals(1.5f, NumberUtils.toFloat("abc", 1.5f), 0.0f);
    }

    // ==================== toDouble ====================

    @Test
    public void testToDoubleNull() {
        assertEquals(0.0, NumberUtils.toDouble(null), 0.0);
        assertEquals(2.718, NumberUtils.toDouble(null, 2.718), 0.0);
    }

    @Test
    public void testToDoubleValid() {
        assertEquals(1.23, NumberUtils.toDouble("1.23"), 0.0);
        assertEquals(-0.5, NumberUtils.toDouble("-0.5"), 0.0);
    }

    @Test
    public void testToDoubleInvalid() {
        assertEquals(0.0, NumberUtils.toDouble("xyz"), 0.0);
        assertEquals(99.9, NumberUtils.toDouble("xyz", 99.9), 0.0);
    }

    // ==================== isDigits ====================

    @Test
    public void testIsDigitsNull() {
        assertFalse(NumberUtils.isDigits(null));
    }

    @Test
    public void testIsDigitsEmpty() {
        assertFalse(NumberUtils.isDigits(""));
    }

    @Test
    public void testIsDigitsValid() {
        assertTrue(NumberUtils.isDigits("123"));
        assertTrue(NumberUtils.isDigits("0"));
        assertTrue(NumberUtils.isDigits("007"));
    }

    @Test
    public void testIsDigitsInvalid() {
        assertFalse(NumberUtils.isDigits("12.3"));
        assertFalse(NumberUtils.isDigits("-123"));
        assertFalse(NumberUtils.isDigits("abc"));
        assertFalse(NumberUtils.isDigits("12 3"));
    }

    // ==================== compare ====================

    @Test
    public void testCompareInt() {
        assertTrue(NumberUtils.compare(1, 2) < 0);
        assertTrue(NumberUtils.compare(2, 1) > 0);
        assertEquals(0, NumberUtils.compare(0, 0));
        assertTrue(NumberUtils.compare(Integer.MAX_VALUE, Integer.MIN_VALUE) > 0);
    }

    @Test
    public void testCompareLong() {
        assertTrue(NumberUtils.compare(1L, 2L) < 0);
        assertTrue(NumberUtils.compare(2L, 1L) > 0);
        assertEquals(0, NumberUtils.compare(0L, 0L));
        assertTrue(NumberUtils.compare(Long.MAX_VALUE, Long.MIN_VALUE) > 0);
    }

    @Test
    public void testCompareShort() {
        assertTrue(NumberUtils.compare((short)1, (short)2) < 0);
        assertTrue(NumberUtils.compare((short)2, (short)1) > 0);
        assertEquals(0, NumberUtils.compare((short)0, (short)0));
    }

    @Test
    public void testCompareByte() {
        assertTrue(NumberUtils.compare((byte)1, (byte)2) < 0);
        assertTrue(NumberUtils.compare((byte)2, (byte)1) > 0);
        assertEquals(0, NumberUtils.compare((byte)0, (byte)0));
    }

    // ==================== min / max ====================

    @Test
    public void testMinInt() {
        assertEquals(1, NumberUtils.min(1, 2, 3));
        assertEquals(-5, NumberUtils.min(-5, 0, 10));
        assertEquals(Integer.MIN_VALUE, NumberUtils.min(Integer.MIN_VALUE, 0, Integer.MAX_VALUE));
    }

    @Test
    public void testMaxInt() {
        assertEquals(3, NumberUtils.max(1, 2, 3));
        assertEquals(10, NumberUtils.max(-5, 0, 10));
        assertEquals(Integer.MAX_VALUE, NumberUtils.min(Integer.MIN_VALUE, 0, Integer.MAX_VALUE));
    }

    @Test
    public void testMinLong() {
        assertEquals(1L, NumberUtils.min(1L, 2L, 3L));
        assertEquals(-5L, NumberUtils.min(-5L, 0L, 10L));
    }

    @Test
    public void testMaxLong() {
        assertEquals(3L, NumberUtils.max(1L, 2L, 3L));
        assertEquals(10L, NumberUtils.max(-5L, 0L, 10L));
    }

    @Test
    public void testMinShort() {
        assertEquals((short)1, NumberUtils.min((short)1, (short)2, (short)3));
        assertEquals((short)-5, NumberUtils.min((short)-5, (short)0, (short)10));
    }

    @Test
    public void testMaxShort() {
        assertEquals((short)3, NumberUtils.max((short)1, (short)2, (short)3));
        assertEquals((short)10, NumberUtils.max((short)-5, (short)0, (short)10));
    }

    @Test
    public void testMinByte() {
        assertEquals((byte)1, NumberUtils.min((byte)1, (byte)2, (byte)3));
        assertEquals((byte)-5, NumberUtils.min((byte)-5, (byte)0, (byte)10));
    }

    @Test
    public void testMaxByte() {
        assertEquals((byte)3, NumberUtils.max((byte)1, (byte)2, (byte)3));
        assertEquals((byte)10, NumberUtils.max((byte)-5, (byte)0, (byte)10));
    }

    @Test
    public void testMinFloat() {
        assertEquals(1.0f, NumberUtils.min(1.0f, 2.0f, 3.0f), 0.0f);
        assertEquals(-5.0f, NumberUtils.min(-5.0f, 0.0f, 10.0f), 0.0f);
        assertEquals(Float.NaN, NumberUtils.min(Float.NaN, 1.0f, 2.0f), 0.0f);
    }

    @Test
    public void testMaxFloat() {
        assertEquals(3.0f, NumberUtils.max(1.0f, 2.0f, 3.0f), 0.0f);
        assertEquals(10.0f, NumberUtils.max(-5.0f, 0.0f, 10.0f), 0.0f);
        assertEquals(Float.NaN, NumberUtils.max(Float.NaN, 1.0f, 2.0f), 0.0f);
    }

    @Test
    public void testMinDouble() {
        assertEquals(1.0, NumberUtils.min(1.0, 2.0, 3.0), 0.0);
        assertEquals(-5.0, NumberUtils.min(-5.0, 0.0, 10.0), 0.0);
        assertEquals(Double.NaN, NumberUtils.min(Double.NaN, 1.0, 2.0), 0.0);
    }

    @Test
    public void testMaxDouble() {
        assertEquals(3.0, NumberUtils.max(1.0, 2.0, 3.0), 0.0);
        assertEquals(10.0, NumberUtils.max(-5.0, 0.0, 10.0), 0.0);
        assertEquals(Double.NaN, NumberUtils.max(Double.NaN, 1.0, 2.0), 0.0);
    }

    // ==================== isCreatable (alias for isNumber) ====================

    @Test
    public void testIsCreatableNull() {
        assertFalse(NumberUtils.isCreatable(null));
    }

    @Test
    public void testIsCreatableValid() {
        assertTrue(NumberUtils.isCreatable("123"));
        assertTrue(NumberUtils.isCreatable("0xFF"));
    }

    @Test
    public void testIsCreatableInvalid() {
        assertFalse(NumberUtils.isCreatable("abc"));
    }

    // ==================== isParsable ====================

    @Test
    public void testIsParsableNull() {
        assertFalse(NumberUtils.isParsable(null));
    }

    @Test
    public void testIsParsableEmpty() {
        assertFalse(NumberUtils.isParsable(""));
    }

    @Test
    public void testIsParsableValid() {
        assertTrue(NumberUtils.isParsable("123"));
        assertTrue(NumberUtils.isParsable("-123"));
        assertTrue(NumberUtils.isParsable("3.14"));
        assertTrue(NumberUtils.isParsable("-2.5"));
    }

    @Test
    public void testIsParsableInvalid() {
        assertFalse(NumberUtils.isParsable("0xFF"));
        assertFalse(NumberUtils.isParsable("1e10"));
        assertFalse(NumberUtils.isParsable("abc"));
        assertFalse(NumberUtils.isParsable("12 3"));
    }
}