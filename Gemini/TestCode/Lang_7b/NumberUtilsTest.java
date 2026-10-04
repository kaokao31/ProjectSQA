package org.apache.commons.lang3.math;

import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link NumberUtils}.
 */
public class NumberUtilsTest {

    @Test
    public void testConstructor() {
        assertNotNull(new NumberUtils());
    }

    // -----------------------------------------------------------------------
    @Test
    public void testToIntString() {
        assertEquals(0, NumberUtils.toInt(null));
        assertEquals(0, NumberUtils.toInt(""));
        assertEquals(0, NumberUtils.toInt("abc"));
        assertEquals(123, NumberUtils.toInt("123"));
        assertEquals(-123, NumberUtils.toInt("-123"));
    }

    @Test
    public void testToIntStringI() {
        assertEquals(1, NumberUtils.toInt(null, 1));
        assertEquals(1, NumberUtils.toInt("", 1));
        assertEquals(1, NumberUtils.toInt("abc", 1));
        assertEquals(123, NumberUtils.toInt("123", 1));
        assertEquals(-123, NumberUtils.toInt("-123", 1));
    }

    @Test
    public void testToLongString() {
        assertEquals(0L, NumberUtils.toLong(null));
        assertEquals(0L, NumberUtils.toLong(""));
        assertEquals(0L, NumberUtils.toLong("abc"));
        assertEquals(123L, NumberUtils.toLong("123"));
        assertEquals(-123L, NumberUtils.toLong("-123"));
    }

    @Test
    public void testToLongStringL() {
        assertEquals(1L, NumberUtils.toLong(null, 1L));
        assertEquals(1L, NumberUtils.toLong("", 1L));
        assertEquals(1L, NumberUtils.toLong("abc", 1L));
        assertEquals(123L, NumberUtils.toLong("123", 1L));
        assertEquals(-123L, NumberUtils.toLong("-123", 1L));
    }

    @Test
    public void testToFloatString() {
        assertEquals(0.0f, NumberUtils.toFloat(null), 0.0001f);
        assertEquals(0.0f, NumberUtils.toFloat(""), 0.0001f);
        assertEquals(0.0f, NumberUtils.toFloat("abc"), 0.0001f);
        assertEquals(123.45f, NumberUtils.toFloat("123.45"), 0.0001f);
        assertEquals(-123.45f, NumberUtils.toFloat("-123.45"), 0.0001f);
    }

    @Test
    public void testToFloatStringF() {
        assertEquals(1.1f, NumberUtils.toFloat(null, 1.1f), 0.0001f);
        assertEquals(1.1f, NumberUtils.toFloat("", 1.1f), 0.0001f);
        assertEquals(1.1f, NumberUtils.toFloat("abc", 1.1f), 0.0001f);
        assertEquals(123.45f, NumberUtils.toFloat("123.45", 1.1f), 0.0001f);
    }

    @Test
    public void testToDoubleString() {
        assertEquals(0.0d, NumberUtils.toDouble(null), 0.0001d);
        assertEquals(0.0d, NumberUtils.toDouble(""), 0.0001d);
        assertEquals(0.0d, NumberUtils.toDouble("abc"), 0.0001d);
        assertEquals(123.45d, NumberUtils.toDouble("123.45"), 0.0001d);
        assertEquals(-123.45d, NumberUtils.toDouble("-123.45"), 0.0001d);
    }

    @Test
    public void testToDoubleStringD() {
        assertEquals(1.1d, NumberUtils.toDouble(null, 1.1d), 0.0001d);
        assertEquals(1.1d, NumberUtils.toDouble("", 1.1d), 0.0001d);
        assertEquals(1.1d, NumberUtils.toDouble("abc", 1.1d), 0.0001d);
        assertEquals(123.45d, NumberUtils.toDouble("123.45", 1.1d), 0.0001d);
    }

    @Test
    public void testToByteString() {
        assertEquals((byte) 0, NumberUtils.toByte(null));
        assertEquals((byte) 0, NumberUtils.toByte(""));
        assertEquals((byte) 0, NumberUtils.toByte("abc"));
        assertEquals((byte) 123, NumberUtils.toByte("123"));
        assertEquals((byte) -123, NumberUtils.toByte("-123"));
    }

    @Test
    public void testToByteStringB() {
        assertEquals((byte) 1, NumberUtils.toByte(null, (byte) 1));
        assertEquals((byte) 1, NumberUtils.toByte("", (byte) 1));
        assertEquals((byte) 1, NumberUtils.toByte("abc", (byte) 1));
        assertEquals((byte) 123, NumberUtils.toByte("123", (byte) 1));
    }

    @Test
    public void testToShortString() {
        assertEquals((short) 0, NumberUtils.toShort(null));
        assertEquals((short) 0, NumberUtils.toShort(""));
        assertEquals((short) 0, NumberUtils.toShort("abc"));
        assertEquals((short) 123, NumberUtils.toShort("123"));
        assertEquals((short) -123, NumberUtils.toShort("-123"));
    }

    @Test
    public void testToShortStringS() {
        assertEquals((short) 1, NumberUtils.toShort(null, (short) 1));
        assertEquals((short) 1, NumberUtils.toShort("", (short) 1));
        assertEquals((short) 1, NumberUtils.toShort("abc", (short) 1));
        assertEquals((short) 123, NumberUtils.toShort("123", (short) 1));
    }

    // -----------------------------------------------------------------------
    @Test
    public void testCreateNumber() {
        assertNull(NumberUtils.createNumber(null));
        assertEquals(Integer.valueOf(123), NumberUtils.createNumber("123"));
        assertEquals(Long.valueOf(123456789012345L), NumberUtils.createNumber("123456789012345"));
        assertEquals(new BigInteger("123456789012345678901234567890"), NumberUtils.createNumber("123456789012345678901234567890"));
        assertEquals(Float.valueOf(123.45f), NumberUtils.createNumber("123.45f"));
        assertEquals(Float.valueOf(123.45f), NumberUtils.createNumber("123.45F"));
        assertEquals(Double.valueOf(123.45d), NumberUtils.createNumber("123.45d"));
        assertEquals(Double.valueOf(123.45d), NumberUtils.createNumber("123.45D"));
        assertEquals(Double.valueOf(123.45), NumberUtils.createNumber("123.45"));
        assertEquals(Double.valueOf(1.23e5), NumberUtils.createNumber("1.23e5"));
        assertEquals(Double.valueOf(1.23E5), NumberUtils.createNumber("1.23E5"));
        assertEquals(Float.valueOf(1.23e5f), NumberUtils.createNumber("1.23e5f"));
        assertEquals(Float.valueOf(1.23E5f), NumberUtils.createNumber("1.23E5F"));
        assertEquals(Double.valueOf(1.23e5d), NumberUtils.createNumber("1.23e5d"));
        assertEquals(Double.valueOf(1.23E5d), NumberUtils.createNumber("1.23E5D"));
        assertEquals(Long.valueOf(123L), NumberUtils.createNumber("123L"));
        assertEquals(Long.valueOf(123L), NumberUtils.createNumber("123l"));
        assertEquals(Integer.valueOf(0x12), NumberUtils.createNumber("0x12"));
        assertEquals(Integer.valueOf(0x12), NumberUtils.createNumber("0X12"));
        assertEquals(Integer.valueOf(-0x12), NumberUtils.createNumber("-0x12"));
        assertEquals(Integer.valueOf(-0x12), NumberUtils.createNumber("-0X12"));
        assertEquals(Integer.valueOf(0x12), NumberUtils.createNumber("#12"));
        assertEquals(Integer.valueOf(-0x12), NumberUtils.createNumber("-#12"));
        assertEquals(new BigInteger("7FFFFFFFFFFFFFFF", 16), NumberUtils.createNumber("0x7FFFFFFFFFFFFFFF"));
        assertEquals(new BigInteger("FFFFFFFFFFFFFFFF", 16), NumberUtils.createNumber("0xFFFFFFFFFFFFFFFF"));
        assertEquals(new BigInteger("FFFFFFFFFFFFFFFF", 16).negate(), NumberUtils.createNumber("-0xFFFFFFFFFFFFFFFF"));
        assertEquals(new BigInteger("7FFFFFFFFFFFFFFF", 16), NumberUtils.createNumber("#7FFFFFFFFFFFFFFF"));
        assertEquals(new BigInteger("7FFFFFFFFFFFFFFF", 16).negate(), NumberUtils.createNumber("-#7FFFFFFFFFFFFFFF"));
        assertEquals(Double.valueOf(.5d), NumberUtils.createNumber(".5"));
        assertEquals(Float.valueOf(.5f), NumberUtils.createNumber(".5f"));
        assertEquals(Double.valueOf(.5d), NumberUtils.createNumber(".5d"));
        assertEquals(Double.valueOf(1.0), NumberUtils.createNumber("1."));
        assertEquals(Float.valueOf(1.0f), NumberUtils.createNumber("1.f"));
        assertEquals(Double.valueOf(1.0d), NumberUtils.createNumber("1.d"));
        assertEquals(new BigDecimal("1.23456789012345678901234567890"), NumberUtils.createNumber("1.23456789012345678901234567890"));
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
        NumberUtils.createNumber("--12345");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberLeadingHyphensHex() {
        NumberUtils.createNumber("--0x12345");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberLeadingHyphensHash() {
        NumberUtils.createNumber("--#12345");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberHexWithoutDigits() {
        NumberUtils.createNumber("0x");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberNegativeHexWithoutDigits() {
        NumberUtils.createNumber("-0x");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberHashWithoutDigits() {
        NumberUtils.createNumber("#");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberNegativeHashWithoutDigits() {
        NumberUtils.createNumber("-#");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberInvalidExponent1() {
        NumberUtils.createNumber("1.23e-");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberInvalidExponent2() {
        NumberUtils.createNumber("1.23e");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberInvalidExponent3() {
        NumberUtils.createNumber("1.23e+");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberMultipleDecimalPoints() {
        NumberUtils.createNumber("1.2.3");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberTrailingGarbage() {
        NumberUtils.createNumber("123a");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberInvalidQualifier() {
        NumberUtils.createNumber("123q");
    }

    @Test
    public void testCreateFloat() {
        assertNull(NumberUtils.createFloat(null));
        assertEquals(Float.valueOf(123.45f), NumberUtils.createFloat("123.45"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateFloatFailure() {
        NumberUtils.createFloat("abc");
    }

    @Test
    public void testCreateDouble() {
        assertNull(NumberUtils.createDouble(null));
        assertEquals(Double.valueOf(123.45d), NumberUtils.createDouble("123.45"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateDoubleFailure() {
        NumberUtils.createDouble("abc");
    }

    @Test
    public void testCreateInteger() {
        assertNull(NumberUtils.createInteger(null));
        assertEquals(Integer.valueOf(123), NumberUtils.createInteger("123"));
        assertEquals(Integer.valueOf(0x12), NumberUtils.createInteger("0x12"));
        assertEquals(Integer.valueOf(0x12), NumberUtils.createInteger("#12"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateIntegerFailure() {
        NumberUtils.createInteger("abc");
    }

    @Test
    public void testCreateLong() {
        assertNull(NumberUtils.createLong(null));
        assertEquals(Long.valueOf(123L), NumberUtils.createLong("123"));
        assertEquals(Long.valueOf(0x12L), NumberUtils.createLong("0x12"));
        assertEquals(Long.valueOf(0x12L), NumberUtils.createLong("#12"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateLongFailure() {
        NumberUtils.createLong("abc");
    }

    @Test
    public void testCreateBigInteger() {
        assertNull(NumberUtils.createBigInteger(null));
        assertEquals(new BigInteger("12345678901234567890"), NumberUtils.createBigInteger("12345678901234567890"));
        assertEquals(new BigInteger("123", 10), NumberUtils.createBigInteger("123"));
        assertEquals(new BigInteger("12", 16), NumberUtils.createBigInteger("0x12"));
        assertEquals(new BigInteger("12", 16), NumberUtils.createBigInteger("0X12"));
        assertEquals(new BigInteger("12", 16), NumberUtils.createBigInteger("#12"));
        assertEquals(new BigInteger("-12", 16), NumberUtils.createBigInteger("-0x12"));
        assertEquals(new BigInteger("-12", 16), NumberUtils.createBigInteger("-0X12"));
        assertEquals(new BigInteger("-12", 16), NumberUtils.createBigInteger("-#12"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateBigIntegerFailure() {
        NumberUtils.createBigInteger("abc");
    }

    @Test
    public void testCreateBigDecimal() {
        assertNull(NumberUtils.createBigDecimal(null));
        assertEquals(new BigDecimal("12345678901234567890.123456789"), NumberUtils.createBigDecimal("12345678901234567890.123456789"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateBigDecimalBlank() {
        NumberUtils.createBigDecimal("");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateBigDecimalFailure() {
        NumberUtils.createBigDecimal("abc");
    }

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
        assertEquals(1.0d, NumberUtils.min(1.0d, 2.0d, 3.0d), 0.0001d);
        assertEquals(1.0d, NumberUtils.min(2.0d, 1.0d, 3.0d), 0.0001d);
        assertEquals(1.0d, NumberUtils.min(3.0d, 2.0d, 1.0d), 0.0001d);
        assertEquals(1.0d, NumberUtils.min(Double.NaN, 1.0d, 2.0d), 0.0001d);
        assertEquals(1.0d, NumberUtils.min(1.0d, Double.NaN, 2.0d), 0.0001d);
        assertEquals(1.0d, NumberUtils.min(2.0d, 1.0d, Double.NaN), 0.0001d);
    }

    @Test
    public void testMinFloat() {
        assertEquals(1.0f, NumberUtils.min(1.0f, 2.0f, 3.0f), 0.0001f);
        assertEquals(1.0f, NumberUtils.min(2.0f, 1.0f, 3.0f), 0.0001f);
        assertEquals(1.0f, NumberUtils.min(3.0f, 2.0f, 1.0f), 0.0001f);
        assertEquals(1.0f, NumberUtils.min(Float.NaN, 1.0f, 2.0f), 0.0001f);
        assertEquals(1.0f, NumberUtils.min(1.0f, Float.NaN, 2.0f), 0.0001f);
        assertEquals(1.0f, NumberUtils.min(2.0f, 1.0f, Float.NaN), 0.0001f);
    }

    @Test
    public void testMinLongArray() {
        assertEquals(1L, NumberUtils.min(new long[]{3L, 2L, 1L}));
        assertEquals(1L, NumberUtils.min(new long[]{1L}));
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
        assertEquals(1, NumberUtils.min(new int[]{3, 2, 1}));
        assertEquals(1, NumberUtils.min(new int[]{1}));
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
        assertEquals((short) 1, NumberUtils.min(new short[]{(short) 3, (short) 2, (short) 1}));
        assertEquals((short) 1, NumberUtils.min(new short[]{(short) 1}));
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
        assertEquals((byte) 1, NumberUtils.min(new byte[]{(byte) 3, (byte) 2, (byte) 1}));
        assertEquals((byte) 1, NumberUtils.min(new byte[]{(byte) 1}));
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
        assertEquals(1.0d, NumberUtils.min(new double[]{3.0d, 2.0d, 1.0d}), 0.0001d);
        assertEquals(1.0d, NumberUtils.min(new double[]{1.0d}), 0.0001d);
        assertEquals(1.0d, NumberUtils.min(new double[]{Double.NaN, 2.0d, 1.0d}), 0.0001d);
        assertEquals(Double.NaN, NumberUtils.min(new double[]{Double.NaN, Double.NaN}), 0.0001d);
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
        assertEquals(1.0f, NumberUtils.min(new float[]{3.0f, 2.0f, 1.0f}), 0.0001f);
        assertEquals(1.0f, NumberUtils.min(new float[]{1.0f}), 0.0001f);
        assertEquals(1.0f, NumberUtils.min(new float[]{Float.NaN, 2.0f, 1.0f}), 0.0001f);
        assertEquals(Float.NaN, NumberUtils.min(new float[]{Float.NaN, Float.NaN}), 0.0001f);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinFloatArrayNull() {
        NumberUtils.min((float[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinFloatArrayEmpty() {
        NumberUtils.min(new float[0]);
    }

    // -----------------------------------------------------------------------
    @Test
    public void testMaxLong() {
        assertEquals(3L, NumberUtils.max(1L, 2L, 3L));
        assertEquals(3L, NumberUtils.max(2L, 3L, 1L));
        assertEquals(3L, NumberUtils.max(3L, 1L, 2L));
    }

    @Test
    public void testMaxInt() {
        assertEquals(3, NumberUtils.max(1, 2, 3));
        assertEquals(3, NumberUtils.max(2, 3, 1));
        assertEquals(3, NumberUtils.max(3, 1, 2));
    }

    @Test
    public void testMaxShort() {
        assertEquals((short) 3, NumberUtils.max((short) 1, (short) 2, (short) 3));
        assertEquals((short) 3, NumberUtils.max((short) 2, (short) 3, (short) 1));
        assertEquals((short) 3, NumberUtils.max((short) 3, (short) 1, (short) 2));
    }

    @Test
    public void testMaxByte() {
        assertEquals((byte) 3, NumberUtils.max((byte) 1, (byte) 2, (byte) 3));
        assertEquals((byte) 3, NumberUtils.max((byte) 2, (byte) 3, (byte) 1));
        assertEquals((byte) 3, NumberUtils.max((byte) 3, (byte) 1, (byte) 2));
    }

    @Test
    public void testMaxDouble() {
        assertEquals(3.0d, NumberUtils.max(1.0d, 2.0d, 3.0d), 0.0001d);
        assertEquals(3.0d, NumberUtils.max(2.0d, 3.0d, 1.0d), 0.0001d);
        assertEquals(3.0d, NumberUtils.max(3.0d, 1.0d, 2.0d), 0.0001d);
        assertEquals(2.0d, NumberUtils.max(Double.NaN, 1.0d, 2.0d), 0.0001d);
        assertEquals(2.0d, NumberUtils.max(1.0d, Double.NaN, 2.0d), 0.0001d);
        assertEquals(2.0d, NumberUtils.max(2.0d, 1.0d, Double.NaN), 0.0001d);
    }

    @Test
    public void testMaxFloat() {
        assertEquals(3.0f, NumberUtils.max(1.0f, 2.0f, 3.0f), 0.0001f);
        assertEquals(3.0f, NumberUtils.max(2.0f, 3.0f, 1.0f), 0.0001f);
        assertEquals(3.0f, NumberUtils.max(3.0f, 1.0f, 2.0f), 0.0001f);
        assertEquals(2.0f, NumberUtils.max(Float.NaN, 1.0f, 2.0f), 0.0001f);
        assertEquals(2.0f, NumberUtils.max(1.0f, Float.NaN, 2.0f), 0.0001f);
        assertEquals(2.0f, NumberUtils.max(2.0f, 1.0f, Float.NaN), 0.0001f);
    }

    @Test
    public void testMaxLongArray() {
        assertEquals(3L, NumberUtils.max(new long[]{1L, 2L, 3L}));
        assertEquals(1L, NumberUtils.max(new long[]{1L}));
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
        assertEquals(3, NumberUtils.max(new int[]{1, 2, 3}));
        assertEquals(1, NumberUtils.max(new int[]{1}));
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
        assertEquals((short) 3, NumberUtils.max(new short[]{(short) 1, (short) 2, (short) 3}));
        assertEquals((short) 1, NumberUtils.max(new short[]{(short) 1}));
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
        assertEquals((byte) 3, NumberUtils.max(new byte[]{(byte) 1, (byte) 2, (byte) 3}));
        assertEquals((byte) 1, NumberUtils.max(new byte[]{(byte) 1}));
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
        assertEquals(3.0d, NumberUtils.max(new double[]{1.0d, 2.0d, 3.0d}), 0.0001d);
        assertEquals(1.0d, NumberUtils.max(new double[]{1.0d}), 0.0001d);
        assertEquals(2.0d, NumberUtils.max(new double[]{Double.NaN, 2.0d, 1.0d}), 0.0001d);
        assertEquals(Double.NaN, NumberUtils.max(new double[]{Double.NaN, Double.NaN}), 0.0001d);
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
        assertEquals(3.0f, NumberUtils.max(new float[]{1.0f, 2.0f, 3.0f}), 0.0001f);
        assertEquals(1.0f, NumberUtils.max(new float[]{1.0f}), 0.0001f);
        assertEquals(2.0f, NumberUtils.max(new float[]{Float.NaN, 2.0f, 1.0f}), 0.0001f);
        assertEquals(Float.NaN, NumberUtils.max(new float[]{Float.NaN, Float.NaN}), 0.0001f);
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
    @Test
    public void testIsDigits() {
        assertFalse(NumberUtils.isDigits(null));
        assertFalse(NumberUtils.isDigits(""));
        assertFalse(NumberUtils.isDigits("  "));
        assertFalse(NumberUtils.isDigits("123.45"));
        assertFalse(NumberUtils.isDigits("-123"));
        assertFalse(NumberUtils.isDigits("123a"));
        assertTrue(NumberUtils.isDigits("123"));
        assertTrue(NumberUtils.isDigits("0"));
    }

    @Test
    public void testIsNumber() {
        assertFalse(NumberUtils.isNumber(null));
        assertFalse(NumberUtils.isNumber(""));
        assertFalse(NumberUtils.isNumber("  "));
        assertFalse(NumberUtils.isNumber("foo"));
        assertFalse(NumberUtils.isNumber("--123"));
        assertFalse(NumberUtils.isNumber("123-"));
        assertFalse(NumberUtils.isNumber("1.2.3"));
        assertFalse(NumberUtils.isNumber("1e2e3"));
        assertFalse(NumberUtils.isNumber("1e"));
        assertFalse(NumberUtils.isNumber("1e+"));
        assertFalse(NumberUtils.isNumber("1e-"));
        assertFalse(NumberUtils.isNumber("."));
        assertFalse(NumberUtils.isNumber("0x"));
        assertFalse(NumberUtils.isNumber("-0x"));
        assertFalse(NumberUtils.isNumber("0xxyz"));

        assertTrue(NumberUtils.isNumber("123"));
        assertTrue(NumberUtils.isNumber("-123"));
        assertTrue(NumberUtils.isNumber("+123"));
        assertTrue(NumberUtils.isNumber("123.45"));
        assertTrue(NumberUtils.isNumber("-123.45"));
        assertTrue(NumberUtils.isNumber(".45"));
        assertTrue(NumberUtils.isNumber("-.45"));
        assertTrue(NumberUtils.isNumber("123."));
        assertTrue(NumberUtils.isNumber("-123."));
        assertTrue(NumberUtils.isNumber("123e4"));
        assertTrue(NumberUtils.isNumber("123E4"));
        assertTrue(NumberUtils.isNumber("123e+4"));
        assertTrue(NumberUtils.isNumber("123e-4"));
        assertTrue(NumberUtils.isNumber("-123e-4"));
        assertTrue(NumberUtils.isNumber("123L"));
        assertTrue(NumberUtils.isNumber("123l"));
        assertTrue(NumberUtils.isNumber("123.45f"));
        assertTrue(NumberUtils.isNumber("123.45F"));
        assertTrue(NumberUtils.isNumber("123.45d"));
        assertTrue(NumberUtils.isNumber("123.45D"));
        assertTrue(NumberUtils.isNumber("0x1234"));
        assertTrue(NumberUtils.isNumber("0X1234"));
        assertTrue(NumberUtils.isNumber("0xabcdef"));
        assertTrue(NumberUtils.isNumber("0xABCDEF"));
        assertTrue(NumberUtils.isNumber("-0x1234"));
        assertTrue(NumberUtils.isNumber("-0X1234"));
    }
}