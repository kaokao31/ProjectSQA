package org.apache.commons.lang;

import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link org.apache.commons.lang.NumberUtils}.
 */
public class NumberUtilsTest {

    @Test
    public void testConstructor() {
        assertNotNull(new NumberUtils());
    }

    // -----------------------------------------------------------------------
    // stringToInt / toInt tests
    // -----------------------------------------------------------------------

    @Test
    public void testStringToInt() {
        assertEquals("stringToInt(String) 1 failed", 12345, NumberUtils.stringToInt("12345"));
        assertEquals("stringToInt(String) 2 failed", 0, NumberUtils.stringToInt("abc"));
        assertEquals("stringToInt(String) 3 failed", 0, NumberUtils.stringToInt(null));
        assertEquals("stringToInt(String) 4 failed", 0, NumberUtils.stringToInt(""));

        assertEquals("stringToInt(String, int) 1 failed", 12345, NumberUtils.stringToInt("12345", 5));
        assertEquals("stringToInt(String, int) 2 failed", 5, NumberUtils.stringToInt("abc", 5));
        assertEquals("stringToInt(String, int) 3 failed", 5, NumberUtils.stringToInt(null, 5));
        assertEquals("stringToInt(String, int) 4 failed", 5, NumberUtils.stringToInt("", 5));
    }

    @Test
    public void testToInt() {
        assertEquals(12345, NumberUtils.toInt("12345"));
        assertEquals(0, NumberUtils.toInt("abc"));
        assertEquals(0, NumberUtils.toInt(null));
        assertEquals(0, NumberUtils.toInt(""));
        assertEquals(5, NumberUtils.toInt("abc", 5));
        assertEquals(5, NumberUtils.toInt(null, 5));
        assertEquals(12345, NumberUtils.toInt("12345", 5));
    }

    @Test
    public void testToLong() {
        assertEquals(12345L, NumberUtils.toLong("12345"));
        assertEquals(0L, NumberUtils.toLong("abc"));
        assertEquals(0L, NumberUtils.toLong(null));
        assertEquals(0L, NumberUtils.toLong(""));
        assertEquals(5L, NumberUtils.toLong("abc", 5L));
        assertEquals(5L, NumberUtils.toLong(null, 5L));
        assertEquals(12345L, NumberUtils.toLong("12345", 5L));
    }

    @Test
    public void testToFloat() {
        assertEquals(12.345f, NumberUtils.toFloat("12.345"), 0.0001f);
        assertEquals(0.0f, NumberUtils.toFloat("abc"), 0.0001f);
        assertEquals(0.0f, NumberUtils.toFloat(null), 0.0001f);
        assertEquals(0.0f, NumberUtils.toFloat(""), 0.0001f);
        assertEquals(5.5f, NumberUtils.toFloat("abc", 5.5f), 0.0001f);
        assertEquals(5.5f, NumberUtils.toFloat(null, 5.5f), 0.0001f);
        assertEquals(12.345f, NumberUtils.toFloat("12.345", 5.5f), 0.0001f);
    }

    @Test
    public void testToDouble() {
        assertEquals(12.345d, NumberUtils.toDouble("12.345"), 0.0001d);
        assertEquals(0.0d, NumberUtils.toDouble("abc"), 0.0001d);
        assertEquals(0.0d, NumberUtils.toDouble(null), 0.0001d);
        assertEquals(0.0d, NumberUtils.toDouble(""), 0.0001d);
        assertEquals(5.5d, NumberUtils.toDouble("abc", 5.5d), 0.0001d);
        assertEquals(5.5d, NumberUtils.toDouble(null, 5.5d), 0.0001d);
        assertEquals(12.345d, NumberUtils.toDouble("12.345", 5.5d), 0.0001d);
    }

    @Test
    public void testToByte() {
        assertEquals((byte) 123, NumberUtils.toByte("123"));
        assertEquals((byte) 0, NumberUtils.toByte("abc"));
        assertEquals((byte) 0, NumberUtils.toByte(null));
        assertEquals((byte) 0, NumberUtils.toByte(""));
        assertEquals((byte) 5, NumberUtils.toByte("abc", (byte) 5));
        assertEquals((byte) 5, NumberUtils.toByte(null, (byte) 5));
        assertEquals((byte) 123, NumberUtils.toByte("123", (byte) 5));
    }

    @Test
    public void testToShort() {
        assertEquals((short) 12345, NumberUtils.toShort("12345"));
        assertEquals((short) 0, NumberUtils.toShort("abc"));
        assertEquals((short) 0, NumberUtils.toShort(null));
        assertEquals((short) 0, NumberUtils.toShort(""));
        assertEquals((short) 5, NumberUtils.toShort("abc", (short) 5));
        assertEquals((short) 5, NumberUtils.toShort(null, (short) 5));
        assertEquals((short) 12345, NumberUtils.toShort("12345", (short) 5));
    }

    // -----------------------------------------------------------------------
    // createNumber tests
    // -----------------------------------------------------------------------

    @Test
    public void testCreateNumber() {
        assertNull(NumberUtils.createNumber(null));
        assertEquals(Integer.valueOf(12345), NumberUtils.createNumber("12345"));
        assertEquals(Long.valueOf(123456789012L), NumberUtils.createNumber("123456789012"));
        assertEquals(new BigInteger("1234567890123456789012"), NumberUtils.createNumber("1234567890123456789012"));
        assertEquals(Float.valueOf(12.345f), NumberUtils.createNumber("12.345f"));
        assertEquals(Float.valueOf(12.345F), NumberUtils.createNumber("12.345F"));
        assertEquals(Double.valueOf(12.345d), NumberUtils.createNumber("12.345d"));
        assertEquals(Double.valueOf(12.345D), NumberUtils.createNumber("12.345D"));
        assertEquals(Double.valueOf(12.345), NumberUtils.createNumber("12.345"));
        assertEquals(Double.valueOf(1e10), NumberUtils.createNumber("1e10"));
        assertEquals(Float.valueOf(1e10f), NumberUtils.createNumber("1e10f"));
        assertEquals(Double.valueOf(1e10d), NumberUtils.createNumber("1e10d"));
        assertEquals(Float.valueOf(1.1E-70f), NumberUtils.createNumber("1.1E-70F"));
        assertEquals(new BigDecimal("1.1E-700"), NumberUtils.createNumber("1.1E-700F"));
        assertEquals(Long.valueOf(12345L), NumberUtils.createNumber("12345L"));
        assertEquals(Long.valueOf(12345L), NumberUtils.createNumber("12345l"));

        // Hex tests
        assertEquals(Integer.valueOf(0x1234), NumberUtils.createNumber("0x1234"));
        assertEquals(Integer.valueOf(-0x1234), NumberUtils.createNumber("-0x1234"));
        assertEquals(Integer.valueOf(0x1234), NumberUtils.createNumber("#1234"));
        assertEquals(Integer.valueOf(-0x1234), NumberUtils.createNumber("-#1234"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberBlank() {
        NumberUtils.createNumber("");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberWhitespace() {
        NumberUtils.createNumber("   ");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberLeadingHyphens() {
        NumberUtils.createNumber("--1234");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberInvalidHex() {
        NumberUtils.createNumber("0x");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberInvalidHex2() {
        NumberUtils.createNumber("-0x");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberInvalidHex3() {
        NumberUtils.createNumber("#");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberInvalidHex4() {
        NumberUtils.createNumber("-#");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberMultipleDecimals() {
        NumberUtils.createNumber("1.2.3");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberMultipleEs() {
        NumberUtils.createNumber("1e2e3");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberTrailingGarbage() {
        NumberUtils.createNumber("1234a");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberOnlyE() {
        NumberUtils.createNumber("E");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberOnlyExp() {
        NumberUtils.createNumber("1e");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberOnlyExpSign() {
        NumberUtils.createNumber("1e+");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberOnlyExpMinus() {
        NumberUtils.createNumber("1e-");
    }

    @Test(expected = NumberFormatException.class)
    public void testLang457() {
        NumberUtils.createNumber("l");
    }

    @Test(expected = NumberFormatException.class)
    public void testLang457Upper() {
        NumberUtils.createNumber("L");
    }

    @Test(expected = NumberFormatException.class)
    public void testLang457Float() {
        NumberUtils.createNumber("f");
    }

    @Test(expected = NumberFormatException.class)
    public void testLang457Double() {
        NumberUtils.createNumber("d");
    }

    // -----------------------------------------------------------------------
    // Individual create methods tests
    // -----------------------------------------------------------------------

    @Test
    public void testCreateFloat() {
        assertNull(NumberUtils.createFloat(null));
        assertEquals(Float.valueOf("1234.5"), NumberUtils.createFloat("1234.5"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateFloatFailure() {
        NumberUtils.createFloat("abc");
    }

    @Test
    public void testCreateDouble() {
        assertNull(NumberUtils.createDouble(null));
        assertEquals(Double.valueOf("1234.5"), NumberUtils.createDouble("1234.5"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateDoubleFailure() {
        NumberUtils.createDouble("abc");
    }

    @Test
    public void testCreateInteger() {
        assertNull(NumberUtils.createInteger(null));
        assertEquals(Integer.valueOf("12345"), NumberUtils.createInteger("12345"));
        assertEquals(Integer.valueOf("12345"), NumberUtils.createInteger("+12345"));
        assertEquals(Integer.valueOf("-12345"), NumberUtils.createInteger("-12345"));
        assertEquals(Integer.valueOf(0x1234), NumberUtils.createInteger("0x1234"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateIntegerFailure() {
        NumberUtils.createInteger("abc");
    }

    @Test
    public void testCreateLong() {
        assertNull(NumberUtils.createLong(null));
        assertEquals(Long.valueOf("12345"), NumberUtils.createLong("12345"));
        assertEquals(Long.valueOf("12345"), NumberUtils.createLong("+12345"));
        assertEquals(Long.valueOf("-12345"), NumberUtils.createLong("-12345"));
        assertEquals(Long.valueOf(0x1234), NumberUtils.createLong("0x1234"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateLongFailure() {
        NumberUtils.createLong("abc");
    }

    @Test
    public void testCreateBigInteger() {
        assertNull(NumberUtils.createBigInteger(null));
        assertEquals(new BigInteger("12345"), NumberUtils.createBigInteger("12345"));
        assertEquals(new BigInteger("12345"), NumberUtils.createBigInteger("+12345"));
        assertEquals(new BigInteger("-12345"), NumberUtils.createBigInteger("-12345"));
        assertEquals(new BigInteger("1234", 16), NumberUtils.createBigInteger("0x1234"));
        assertEquals(new BigInteger("1234", 16), NumberUtils.createBigInteger("#1234"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateBigIntegerFailure() {
        NumberUtils.createBigInteger("abc");
    }

    @Test
    public void testCreateBigDecimal() {
        assertNull(NumberUtils.createBigDecimal(null));
        assertEquals(new BigDecimal("1234.5"), NumberUtils.createBigDecimal("1234.5"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateBigDecimalFailure() {
        NumberUtils.createBigDecimal("abc");
    }

    // -----------------------------------------------------------------------
    // min / max 3-argument tests
    // -----------------------------------------------------------------------

    @Test
    public void testMinLong() {
        assertEquals(1L, NumberUtils.min(1L, 2L, 3L));
        assertEquals(1L, NumberUtils.min(2L, 1L, 3L));
        assertEquals(1L, NumberUtils.min(3L, 2L, 1L));
    }

    @Test
    public void testMinInt() {
        assertEquals(1, NumberUtils.min(1, 2, 3));
        assertEquals(1, NumberUtils.min(2, 1, 3));
        assertEquals(1, NumberUtils.min(3, 2, 1));
    }

    @Test
    public void testMinShort() {
        assertEquals((short) 1, NumberUtils.min((short) 1, (short) 2, (short) 3));
        assertEquals((short) 1, NumberUtils.min((short) 2, (short) 1, (short) 3));
        assertEquals((short) 1, NumberUtils.min((short) 3, (short) 2, (short) 1));
    }

    @Test
    public void testMinByte() {
        assertEquals((byte) 1, NumberUtils.min((byte) 1, (byte) 2, (byte) 3));
        assertEquals((byte) 1, NumberUtils.min((byte) 2, (byte) 1, (byte) 3));
        assertEquals((byte) 1, NumberUtils.min((byte) 3, (byte) 2, (byte) 1));
    }

    @Test
    public void testMinDouble() {
        assertEquals(1.0, NumberUtils.min(1.0, 2.0, 3.0), 0.0001);
        assertEquals(1.0, NumberUtils.min(2.0, 1.0, 3.0), 0.0001);
        assertEquals(1.0, NumberUtils.min(3.0, 2.0, 1.0), 0.0001);
        assertTrue(Double.isNaN(NumberUtils.min(Double.NaN, 2.0, 3.0)));
        assertTrue(Double.isNaN(NumberUtils.min(1.0, Double.NaN, 3.0)));
        assertTrue(Double.isNaN(NumberUtils.min(1.0, 2.0, Double.NaN)));
    }

    @Test
    public void testMinFloat() {
        assertEquals(1.0f, NumberUtils.min(1.0f, 2.0f, 3.0f), 0.0001f);
        assertEquals(1.0f, NumberUtils.min(2.0f, 1.0f, 3.0f), 0.0001f);
        assertEquals(1.0f, NumberUtils.min(3.0f, 2.0f, 1.0f), 0.0001f);
        assertTrue(Float.isNaN(NumberUtils.min(Float.NaN, 2.0f, 3.0f)));
        assertTrue(Float.isNaN(NumberUtils.min(1.0f, Float.NaN, 3.0f)));
        assertTrue(Float.isNaN(NumberUtils.min(1.0f, 2.0f, Float.NaN)));
    }

    @Test
    public void testMaxLong() {
        assertEquals(3L, NumberUtils.max(1L, 2L, 3L));
        assertEquals(3L, NumberUtils.max(2L, 3L, 1L));
        assertEquals(3L, NumberUtils.max(3L, 2L, 1L));
    }

    @Test
    public void testMaxInt() {
        assertEquals(3, NumberUtils.max(1, 2, 3));
        assertEquals(3, NumberUtils.max(2, 3, 1));
        assertEquals(3, NumberUtils.max(3, 2, 1));
    }

    @Test
    public void testMaxShort() {
        assertEquals((short) 3, NumberUtils.max((short) 1, (short) 2, (short) 3));
        assertEquals((short) 3, NumberUtils.max((short) 2, (short) 3, (short) 1));
        assertEquals((short) 3, NumberUtils.max((short) 3, (short) 2, (short) 1));
    }

    @Test
    public void testMaxByte() {
        assertEquals((byte) 3, NumberUtils.max((byte) 1, (byte) 2, (byte) 3));
        assertEquals((byte) 3, NumberUtils.max((byte) 2, (byte) 3, (byte) 1));
        assertEquals((byte) 3, NumberUtils.max((byte) 3, (byte) 2, (byte) 1));
    }

    @Test
    public void testMaxDouble() {
        assertEquals(3.0, NumberUtils.max(1.0, 2.0, 3.0), 0.0001);
        assertEquals(3.0, NumberUtils.max(2.0, 3.0, 1.0), 0.0001);
        assertEquals(3.0, NumberUtils.max(3.0, 2.0, 1.0), 0.0001);
        assertTrue(Double.isNaN(NumberUtils.max(Double.NaN, 2.0, 3.0)));
        assertTrue(Double.isNaN(NumberUtils.max(1.0, Double.NaN, 3.0)));
        assertTrue(Double.isNaN(NumberUtils.max(1.0, 2.0, Double.NaN)));
    }

    @Test
    public void testMaxFloat() {
        assertEquals(3.0f, NumberUtils.max(1.0f, 2.0f, 3.0f), 0.0001f);
        assertEquals(3.0f, NumberUtils.max(2.0f, 3.0f, 1.0f), 0.0001f);
        assertEquals(3.0f, NumberUtils.max(3.0f, 2.0f, 1.0f), 0.0001f);
        assertTrue(Float.isNaN(NumberUtils.max(Float.NaN, 2.0f, 3.0f)));
        assertTrue(Float.isNaN(NumberUtils.max(1.0f, Float.NaN, 3.0f)));
        assertTrue(Float.isNaN(NumberUtils.max(1.0f, 2.0f, Float.NaN)));
    }

    // -----------------------------------------------------------------------
    // min / max Array tests
    // -----------------------------------------------------------------------

    @Test
    public void testMinLongArray() {
        assertEquals(5L, NumberUtils.min(new long[]{7L, 5L, 9L}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinLongArrayNull() {
        NumberUtils.min((long[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinLongArrayEmpty() {
        NumberUtils.min(new long[0]);
    }

    @Test
    public void testMinIntArray() {
        assertEquals(5, NumberUtils.min(new int[]{7, 5, 9}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinIntArrayNull() {
        NumberUtils.min((int[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinIntArrayEmpty() {
        NumberUtils.min(new int[0]);
    }

    @Test
    public void testMinShortArray() {
        assertEquals((short) 5, NumberUtils.min(new short[]{7, 5, 9}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinShortArrayNull() {
        NumberUtils.min((short[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinShortArrayEmpty() {
        NumberUtils.min(new short[0]);
    }

    @Test
    public void testMinByteArray() {
        assertEquals((byte) 5, NumberUtils.min(new byte[]{7, 5, 9}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinByteArrayNull() {
        NumberUtils.min((byte[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinByteArrayEmpty() {
        NumberUtils.min(new byte[0]);
    }

    @Test
    public void testMinDoubleArray() {
        assertEquals(5.1d, NumberUtils.min(new double[]{7.1d, 5.1d, 9.1d}), 0.0001d);
        assertTrue(Double.isNaN(NumberUtils.min(new double[]{7.1d, Double.NaN, 9.1d})));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinDoubleArrayNull() {
        NumberUtils.min((double[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinDoubleArrayEmpty() {
        NumberUtils.min(new double[0]);
    }

    @Test
    public void testMinFloatArray() {
        assertEquals(5.1f, NumberUtils.min(new float[]{7.1f, 5.1f, 9.1f}), 0.0001f);
        assertTrue(Float.isNaN(NumberUtils.min(new float[]{7.1f, Float.NaN, 9.1f})));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinFloatArrayNull() {
        NumberUtils.min((float[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinFloatArrayEmpty() {
        NumberUtils.min(new float[0]);
    }

    @Test
    public void testMaxLongArray() {
        assertEquals(9L, NumberUtils.max(new long[]{7L, 5L, 9L}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxLongArrayNull() {
        NumberUtils.max((long[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxLongArrayEmpty() {
        NumberUtils.max(new long[0]);
    }

    @Test
    public void testMaxIntArray() {
        assertEquals(9, NumberUtils.max(new int[]{7, 5, 9}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxIntArrayNull() {
        NumberUtils.max((int[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxIntArrayEmpty() {
        NumberUtils.max(new int[0]);
    }

    @Test
    public void testMaxShortArray() {
        assertEquals((short) 9, NumberUtils.max(new short[]{7, 5, 9}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxShortArrayNull() {
        NumberUtils.max((short[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxShortArrayEmpty() {
        NumberUtils.max(new short[0]);
    }

    @Test
    public void testMaxByteArray() {
        assertEquals((byte) 9, NumberUtils.max(new byte[]{7, 5, 9}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxByteArrayNull() {
        NumberUtils.max((byte[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxByteArrayEmpty() {
        NumberUtils.max(new byte[0]);
    }

    @Test
    public void testMaxDoubleArray() {
        assertEquals(9.1d, NumberUtils.max(new double[]{7.1d, 5.1d, 9.1d}), 0.0001d);
        assertTrue(Double.isNaN(NumberUtils.max(new double[]{7.1d, Double.NaN, 9.1d})));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxDoubleArrayNull() {
        NumberUtils.max((double[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxDoubleArrayEmpty() {
        NumberUtils.max(new double[0]);
    }

    @Test
    public void testMaxFloatArray() {
        assertEquals(9.1f, NumberUtils.max(new float[]{7.1f, 5.1f, 9.1f}), 0.0001f);
        assertTrue(Float.isNaN(NumberUtils.max(new float[]{7.1f, Float.NaN, 9.1f})));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxFloatArrayNull() {
        NumberUtils.max((float[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxFloatArrayEmpty() {
        NumberUtils.max(new float[0]);
    }

    // -----------------------------------------------------------------------
    // isDigits / isNumber tests
    // -----------------------------------------------------------------------

    @Test
    public void testIsDigits() {
        assertFalse(NumberUtils.isDigits(null));
        assertFalse(NumberUtils.isDigits(""));
        assertTrue(NumberUtils.isDigits("12345"));
        assertFalse(NumberUtils.isDigits("1234.5"));
        assertFalse(NumberUtils.isDigits("1234a"));
        assertFalse(NumberUtils.isDigits("-1234"));
    }

    @Test
    public void testIsNumber() {
        assertFalse(NumberUtils.isNumber(null));
        assertFalse(NumberUtils.isNumber(""));
        assertTrue(NumberUtils.isNumber("12345"));
        assertTrue(NumberUtils.isNumber("-12345"));
        assertTrue(NumberUtils.isNumber("+12345"));
        assertTrue(NumberUtils.isNumber("1234.5"));
        assertTrue(NumberUtils.isNumber("-1234.5"));
        assertTrue(NumberUtils.isNumber(".5"));
        assertTrue(NumberUtils.isNumber("-.5"));
        assertTrue(NumberUtils.isNumber("1234."));
        assertTrue(NumberUtils.isNumber("-1234."));
        assertTrue(NumberUtils.isNumber("1234e5"));
        assertTrue(NumberUtils.isNumber("1234E5"));
        assertTrue(NumberUtils.isNumber("1234e+5"));
        assertTrue(NumberUtils.isNumber("1234e-5"));
        assertTrue(NumberUtils.isNumber("-1234e-5"));
        assertTrue(NumberUtils.isNumber("1234L"));
        assertTrue(NumberUtils.isNumber("1234l"));
        assertTrue(NumberUtils.isNumber("1234f"));
        assertTrue(NumberUtils.isNumber("1234F"));
        assertTrue(NumberUtils.isNumber("1234d"));
        assertTrue(NumberUtils.isNumber("1234D"));
        assertTrue(NumberUtils.isNumber("0x1234"));
        assertTrue(NumberUtils.isNumber("-0x1234"));
        assertTrue(NumberUtils.isNumber("0X1234"));
        assertTrue(NumberUtils.isNumber("-0X1234"));

        assertFalse(NumberUtils.isNumber("1234a"));
        assertFalse(NumberUtils.isNumber("0x"));
        assertFalse(NumberUtils.isNumber("-0x"));
        assertFalse(NumberUtils.isNumber("0xGHIJ"));
        assertFalse(NumberUtils.isNumber("1.2.3"));
        assertFalse(NumberUtils.isNumber("1e2e3"));
        assertFalse(NumberUtils.isNumber("1e"));
        assertFalse(NumberUtils.isNumber("1e+"));
        assertFalse(NumberUtils.isNumber("1e-"));
        assertFalse(NumberUtils.isNumber("--1234"));
        assertFalse(NumberUtils.isNumber("."));
        assertFalse(NumberUtils.isNumber("-."));
    }

    // -----------------------------------------------------------------------
    // compare tests
    // -----------------------------------------------------------------------

    @Test
    public void testCompareDouble() {
        assertEquals(-1, NumberUtils.compare(-10.0, 10.0));
        assertEquals(0, NumberUtils.compare(10.0, 10.0));
        assertEquals(1, NumberUtils.compare(10.0, -10.0));
        assertEquals(-1, NumberUtils.compare(10.0, Double.NaN));
        assertEquals(1, NumberUtils.compare(Double.NaN, 10.0));
        assertEquals(0, NumberUtils.compare(Double.NaN, Double.NaN));
        assertEquals(-1, NumberUtils.compare(-0.0, +0.0));
        assertEquals(1, NumberUtils.compare(+0.0, -0.0));
    }

    @Test
    public void testCompareFloat() {
        assertEquals(-1, NumberUtils.compare(-10.0f, 10.0f));
        assertEquals(0, NumberUtils.compare(10.0f, 10.0f));
        assertEquals(1, NumberUtils.compare(10.0f, -10.0f));
        assertEquals(-1, NumberUtils.compare(10.0f, Float.NaN));
        assertEquals(1, NumberUtils.compare(Float.NaN, 10.0f));
        assertEquals(0, NumberUtils.compare(Float.NaN, Float.NaN));
        assertEquals(-1, NumberUtils.compare(-0.0f, +0.0f));
        assertEquals(1, NumberUtils.compare(+0.0f, -0.0f));
    }
}