package org.apache.commons.lang3.math;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Comprehensive JUnit 4 test suite for NumberUtils, targeting bug ID 36
 * (NumberFormatException for "2." in createNumber and isNumber).
 */
public class NumberUtilsTest {

    // ---------- testCreateNumber ----------
    @Test
    public void testCreateNumberNull() {
        assertNull("createNumber(null) should return null", NumberUtils.createNumber(null));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberEmpty() {
        NumberUtils.createNumber("");
    }

    @Test
    public void testCreateNumberInteger() throws Exception {
        assertEquals(Integer.valueOf(123), NumberUtils.createNumber("123"));
    }

    @Test
    public void testCreateNumberLong() throws Exception {
        assertEquals(Long.valueOf(123L), NumberUtils.createNumber("123L"));
    }

    @Test
    public void testCreateNumberFloat() throws Exception {
        assertEquals(Float.valueOf(3.14f), NumberUtils.createNumber("3.14f"));
    }

    @Test
    public void testCreateNumberDouble() throws Exception {
        assertEquals(Double.valueOf(2.0), NumberUtils.createNumber("2."));
    }

    @Test
    public void testCreateNumberHexInteger() throws Exception {
        assertEquals(Integer.valueOf(0xFF), NumberUtils.createNumber("0xFF"));
    }

    @Test
    public void testCreateNumberHexLong() throws Exception {
        assertEquals(Long.valueOf(0xABCDEFL), NumberUtils.createNumber("0xABCDEL"));
    }

    @Test
    public void testCreateNumberNegativeDouble() throws Exception {
        assertEquals(Double.valueOf(-1.5), NumberUtils.createNumber("-1.5"));
    }

    @Test
    public void testCreateNumberScientificNotation() throws Exception {
        assertEquals(Double.valueOf(1e10), NumberUtils.createNumber("1e10"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberInvalid() {
        NumberUtils.createNumber("abc");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberTrailingDotWithL() {
        // "2.l" may be invalid; check expected exception
        NumberUtils.createNumber("2.l");
    }

    // ---------- testIsNumber ----------
    @Test
    public void testIsNumberNull() {
        assertFalse("isNumber(null) should return false", NumberUtils.isNumber(null));
    }

    @Test
    public void testIsNumberEmpty() {
        assertFalse("isNumber(\"\") should return false", NumberUtils.isNumber(""));
    }

    @Test
    public void testIsNumberValidInteger() {
        assertTrue("isNumber(\"123\") should be true", NumberUtils.isNumber("123"));
    }

    @Test
    public void testIsNumberValidLong() {
        assertTrue("isNumber(\"123L\") should be true", NumberUtils.isNumber("123L"));
    }

    @Test
    public void testIsNumberValidFloat() {
        assertTrue("isNumber(\"3.14f\") should be true", NumberUtils.isNumber("3.14f"));
    }

    @Test
    public void testIsNumberValidDoubleTrailingDot() {
        assertTrue("isNumber(\"2.\") should be true", NumberUtils.isNumber("2."));
    }

    @Test
    public void testIsNumberValidHex() {
        assertTrue("isNumber(\"0xFF\") should be true", NumberUtils.isNumber("0xFF"));
    }

    @Test
    public void testIsNumberValidScientific() {
        assertTrue("isNumber(\"1e10\") should be true", NumberUtils.isNumber("1e10"));
    }

    @Test
    public void testIsNumberNegativeDouble() {
        assertTrue("isNumber(\"-1.5\") should be true", NumberUtils.isNumber("-1.5"));
    }

    @Test
    public void testIsNumberInvalidLetter() {
        assertFalse("isNumber(\"abc\") should be false", NumberUtils.isNumber("abc"));
    }

    @Test
    public void testIsNumberInvalidDoubleDot() {
        assertFalse("isNumber(\"1.2.3\") should be false", NumberUtils.isNumber("1.2.3"));
    }

    @Test
    public void testIsNumberInvalidTrailingD() {
        assertFalse("isNumber(\"2.d\") should be false", NumberUtils.isNumber("2.d"));
    }

    // ---------- Additional coverage: edge cases ----------
    @Test
    public void testCreateNumberLeadingZeros() throws Exception {
        assertEquals(Integer.valueOf(7), NumberUtils.createNumber("007"));
    }

    @Test
    public void testCreateNumberMinInteger() throws Exception {
        assertEquals(Integer.valueOf(Integer.MIN_VALUE), NumberUtils.createNumber("-2147483648"));
    }

    @Test
    public void testCreateNumberMaxLong() throws Exception {
        assertEquals(Long.valueOf(Long.MAX_VALUE), NumberUtils.createNumber("9223372036854775807"));
    }

    @Test
    public void testIsNumberHexWithX() {
        assertTrue("isNumber(\"0x12\") should be true", NumberUtils.isNumber("0x12"));
    }

    @Test
    public void testIsNumberNegative() {
        assertTrue("isNumber(\"-123\") should be true", NumberUtils.isNumber("-123"));
    }

    @Test
    public void testCreateNumberDoubleSuffixD() throws Exception {
        assertEquals(Double.valueOf(5.0), NumberUtils.createNumber("5.0D"));
    }

    @Test
    public void testIsNumberDoubleSuffixD() {
        assertTrue("isNumber(\"5.0D\") should be true", NumberUtils.isNumber("5.0D"));
    }
}