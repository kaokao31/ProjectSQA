package org.apache.commons.lang3.math;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.Before;

public class NumberUtilsTest {

    private NumberUtils numberUtils;

    @Before
    public void setUp() {
        numberUtils = new NumberUtils();
    }

    // ========== createNumber tests ==========
    @Test
    public void testCreateNumber_Null() {
        assertNull("createNumber(null) should return null", NumberUtils.createNumber(null));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_EmptyString() {
        NumberUtils.createNumber("");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_InvalidHex() {
        NumberUtils.createNumber("0xGGG");
    }

    @Test
    public void testCreateNumber_Integer() {
        assertEquals("createNumber(\"123\")", Integer.valueOf(123), NumberUtils.createNumber("123"));
    }

    @Test
    public void testCreateNumber_Long() {
        assertEquals("createNumber(\"123L\")", Long.valueOf(123L), NumberUtils.createNumber("123L"));
    }

    @Test
    public void testCreateNumber_Float() {
        assertEquals("createNumber(\"12.3f\")", Float.valueOf(12.3f), NumberUtils.createNumber("12.3f"));
    }

    @Test
    public void testCreateNumber_Double() {
        assertEquals("createNumber(\"12.3\")", Double.valueOf(12.3), NumberUtils.createNumber("12.3"));
    }

    @Test
    public void testCreateNumber_BigDecimal() {
        assertEquals("createNumber(\"1.23e3\")", new java.math.BigDecimal("1.23e3"), NumberUtils.createNumber("1.23e3"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_TrailingDot() {
        NumberUtils.createNumber("123.");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_LeadingDot() {
        NumberUtils.createNumber(".123");
    }

    @Test
    public void testCreateNumber_Hex0x() {
        assertEquals("createNumber(\"0xFF\")", Integer.valueOf(255), NumberUtils.createNumber("0xFF"));
    }

    @Test
    public void testCreateNumber_HexHash() {
        assertEquals("createNumber(\"#FF\")", Integer.valueOf(255), NumberUtils.createNumber("#FF"));
    }

    @Test
    public void testCreateNumber_Negative() {
        assertEquals("createNumber(\"-123\")", Integer.valueOf(-123), NumberUtils.createNumber("-123"));
    }

    @Test
    public void testCreateNumber_PlusSign() {
        assertEquals("createNumber(\"+123\")", Integer.valueOf(123), NumberUtils.createNumber("+123"));
    }

    // ========== isNumber tests ==========
    @Test
    public void testIsNumber_Null() {
        assertFalse("isNumber(null) should return false", NumberUtils.isNumber(null));
    }

    @Test
    public void testIsNumber_Empty() {
        assertFalse("isNumber(\"\") should return false", NumberUtils.isNumber(""));
    }

    @Test
    public void testIsNumber_ValidInteger() {
        assertTrue("isNumber(\"123\")", NumberUtils.isNumber("123"));
    }

    @Test
    public void testIsNumber_ValidHex() {
        assertTrue("isNumber(\"0xAbc\")", NumberUtils.isNumber("0xAbc"));
    }

    @Test
    public void testIsNumber_InvalidHex() {
        assertFalse("isNumber(\"0xG\")", NumberUtils.isNumber("0xG"));
    }

    @Test
    public void testIsNumber_ValidFloat() {
        assertTrue("isNumber(\"12.3\")", NumberUtils.isNumber("12.3"));
    }

    @Test
    public void testIsNumber_ValidDouble() {
        assertTrue("isNumber(\"1.23e-3\")", NumberUtils.isNumber("1.23e-3"));
    }

    @Test
    public void testIsNumber_InvalidDouble() {
        assertFalse("isNumber(\"1.23e\")", NumberUtils.isNumber("1.23e"));
    }

    @Test
    public void testIsNumber_ValidLong() {
        assertTrue("isNumber(\"123L\")", NumberUtils.isNumber("123L"));
    }

    @Test
    public void testIsNumber_InvalidLong() {
        assertFalse("isNumber(\"123Lb\")", NumberUtils.isNumber("123Lb"));
    }

    @Test
    public void testIsNumber_ValidNegative() {
        assertTrue("isNumber(\"-123\")", NumberUtils.isNumber("-123"));
    }

    @Test
    public void testIsNumber_ValidPlus() {
        assertTrue("isNumber(\"+123\")", NumberUtils.isNumber("+123"));
    }

    @Test
    public void testIsNumber_WithLeadingZero() {
        assertTrue("isNumber(\"0123\")", NumberUtils.isNumber("0123"));
    }

    @Test
    public void testIsNumber_OnlyDot() {
        assertFalse("isNumber(\".\")", NumberUtils.isNumber("."));
    }

    @Test
    public void testIsNumber_TrailingDot() {
        assertFalse("isNumber(\"123.\")", NumberUtils.isNumber("123."));
    }

    @Test
    public void testIsNumber_LeadingDot() {
        assertTrue("isNumber(\".5\")", NumberUtils.isNumber(".5")); // valid depending on impl
    }

    // ========== toInt tests ==========
    @Test
    public void testToInt_Null() {
        assertEquals("toInt(null)", 0, NumberUtils.toInt(null));
    }

    @Test
    public void testToInt_NullDefault() {
        assertEquals("toInt(null, 5)", 5, NumberUtils.toInt(null, 5));
    }

    @Test
    public void testToInt_Valid() {
        assertEquals("toInt(\"42\")", 42, NumberUtils.toInt("42"));
    }

    @Test(expected = NumberFormatException.class)
    public void testToInt_Invalid() {
        NumberUtils.toInt("abc");
    }

    @Test
    public void testToInt_EmptyString() {
        assertEquals("toInt(\"\")", 0, NumberUtils.toInt(""));
    }

    // ========== toLong tests ==========
    @Test
    public void testToLong_Null() {
        assertEquals("toLong(null)", 0L, NumberUtils.toLong(null));
    }

    @Test
    public void testToLong_Valid() {
        assertEquals("toLong(\"9999999999\")", 9999999999L, NumberUtils.toLong("9999999999"));
    }

    // ========== toFloat tests ==========
    @Test
    public void testToFloat_Null() {
        assertEquals("toFloat(null)", 0.0f, NumberUtils.toFloat(null), 0.0f);
    }

    @Test
    public void testToFloat_Valid() {
        assertEquals("toFloat(\"3.14\")", 3.14f, NumberUtils.toFloat("3.14"), 0.001f);
    }

    // ========== toDouble tests ==========
    @Test
    public void testToDouble_Null() {
        assertEquals("toDouble(null)", 0.0d, NumberUtils.toDouble(null), 0.0d);
    }

    @Test
    public void testToDouble_Valid() {
        assertEquals("toDouble(\"2.71828\")", 2.71828d, NumberUtils.toDouble("2.71828"), 0.00001d);
    }

    // ========== isDigits tests ==========
    @Test
    public void testIsDigits_Null() {
        assertFalse("isDigits(null)", NumberUtils.isDigits(null));
    }

    @Test
    public void testIsDigits_Empty() {
        assertFalse("isDigits(\"\")", NumberUtils.isDigits(""));
    }

    @Test
    public void testIsDigits_Valid() {
        assertTrue("isDigits(\"123\")", NumberUtils.isDigits("123"));
    }

    @Test
    public void testIsDigits_Invalid() {
        assertFalse("isDigits(\"12a3\")", NumberUtils.isDigits("12a3"));
    }

    @Test
    public void testIsDigits_WithSign() {
        assertFalse("isDigits(\"+123\")", NumberUtils.isDigits("+123"));
    }

    // ========== compare methods if present ==========
    // (assuming compare(int, int) exists)
    @Test
    public void testCompare_Equal() {
        assertEquals("compare(5,5) should be 0", 0, NumberUtils.compare(5, 5));
    }

    @Test
    public void testCompare_Less() {
        assertTrue("compare(3,8) should be negative", NumberUtils.compare(3, 8) < 0);
    }

    @Test
    public void testCompare_Greater() {
        assertTrue("compare(10,2) should be positive", NumberUtils.compare(10, 2) > 0);
    }

    // ========== min/max methods if present ==========
    @Test
    public void testMin_IntArray() {
        assertEquals("min(3,1,2)", 1, NumberUtils.min(3, 1, 2));
        assertEquals("min(3,1,2)", 1, NumberUtils.min(new int[]{3,1,2}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMin_EmptyArray() {
        NumberUtils.min(new int[0]);
    }

    @Test
    public void testMax_IntArray() {
        assertEquals("max(3,1,2)", 3, NumberUtils.max(3, 1, 2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMax_EmptyArray() {
        NumberUtils.max(new int[0]);
    }

    // ========== Additional boundary tests ==========
    @Test
    public void testCreateNumber_MaxInteger() {
        assertEquals("createNumber(\"2147483647\")", Integer.valueOf(Integer.MAX_VALUE), NumberUtils.createNumber("2147483647"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_OverflowInteger() {
        NumberUtils.createNumber("2147483648");
    }

    @Test
    public void testCreateNumber_MinInteger() {
        assertEquals("createNumber(\"-2147483648\")", Integer.valueOf(Integer.MIN_VALUE), NumberUtils.createNumber("-2147483648"));
    }

    @Test
    public void testIsNumber_WithUnderscore() {
        // depends on Java 7+ but may not be supported
        assertFalse("isNumber(\"1_000\")", NumberUtils.isNumber("1_000"));
    }
}