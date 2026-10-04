package org.apache.commons.lang3.math;

import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class NumberUtilsTest {

    @Test
    public void testConstructor() {
        assertNotNull(new NumberUtils());
    }

    // -----------------------------------------------------------------------
    // toInt tests
    // -----------------------------------------------------------------------
    @Test
    public void testToIntString() {
        assertEquals("toInt(null) failed", 0, NumberUtils.toInt(null));
        assertEquals("toInt(\"\") failed", 0, NumberUtils.toInt(""));
        assertEquals("toInt(\"1\") failed", 1, NumberUtils.toInt("1"));
        assertEquals("toInt(\"bad\") failed", 0, NumberUtils.toInt("bad"));
        assertEquals("toInt(\"-123\") failed", -123, NumberUtils.toInt("-123"));
    }

    @Test
    public void testToIntStringI() {
        assertEquals("toInt(null, 1) failed", 1, NumberUtils.toInt(null, 1));
        assertEquals("toInt(\"\", 1) failed", 1, NumberUtils.toInt("", 1));
        assertEquals("toInt(\"1\", 2) failed", 1, NumberUtils.toInt("1", 2));
        assertEquals("toInt(\"bad\", 1) failed", 1, NumberUtils.toInt("bad", 1));
    }

    // -----------------------------------------------------------------------
    // toLong tests
    // -----------------------------------------------------------------------
    @Test
    public void testToLongString() {
        assertEquals("toLong(null) failed", 0L, NumberUtils.toLong(null));
        assertEquals("toLong(\"\") failed", 0L, NumberUtils.toLong(""));
        assertEquals("toLong(\"1\") failed", 1L, NumberUtils.toLong("1"));
        assertEquals("toLong(\"bad\") failed", 0L, NumberUtils.toLong("bad"));
        assertEquals("toLong(\"-123\") failed", -123L, NumberUtils.toLong("-123"));
    }

    @Test
    public void testToLongStringL() {
        assertEquals("toLong(null, 1L) failed", 1L, NumberUtils.toLong(null, 1L));
        assertEquals("toLong(\"\", 1L) failed", 1L, NumberUtils.toLong("", 1L));
        assertEquals("toLong(\"1\", 2L) failed", 1L, NumberUtils.toLong("1", 2L));
        assertEquals("toLong(\"bad\", 1L) failed", 1L, NumberUtils.toLong("bad", 1L));
    }

    // -----------------------------------------------------------------------
    // toFloat tests
    // -----------------------------------------------------------------------
    @Test
    public void testToFloatString() {
        assertEquals("toFloat(null) failed", 0.0f, NumberUtils.toFloat(null), 0.0001f);
        assertEquals("toFloat(\"\") failed", 0.0f, NumberUtils.toFloat(""), 0.0001f);
        assertEquals("toFloat(\"1.1\") failed", 1.1f, NumberUtils.toFloat("1.1"), 0.0001f);
        assertEquals("toFloat(\"bad\") failed", 0.0f, NumberUtils.toFloat("bad"), 0.0001f);
        assertEquals("toFloat(\"-1.1\") failed", -1.1f, NumberUtils.toFloat("-1.1"), 0.0001f);
    }

    @Test
    public void testToFloatStringF() {
        assertEquals("toFloat(null, 1.1f) failed", 1.1f, NumberUtils.toFloat(null, 1.1f), 0.0001f);
        assertEquals("toFloat(\"\", 1.1f) failed", 1.1f, NumberUtils.toFloat("", 1.1f), 0.0001f);
        assertEquals("toFloat(\"1.1\", 2.2f) failed", 1.1f, NumberUtils.toFloat("1.1", 2.2f), 0.0001f);
        assertEquals("toFloat(\"bad\", 1.1f) failed", 1.1f, NumberUtils.toFloat("bad", 1.1f), 0.0001f);
    }

    // -----------------------------------------------------------------------
    // toDouble tests
    // -----------------------------------------------------------------------
    @Test
    public void testToDoubleString() {
        assertEquals("toDouble(null) failed", 0.0d, NumberUtils.toDouble(null), 0.0001d);
        assertEquals("toDouble(\"\") failed", 0.0d, NumberUtils.toDouble(""), 0.0001d);
        assertEquals("toDouble(\"1.1\") failed", 1.1d, NumberUtils.toDouble("1.1"), 0.0001d);
        assertEquals("toDouble(\"bad\") failed", 0.0d, NumberUtils.toDouble("bad"), 0.0001d);
        assertEquals("toDouble(\"-1.1\") failed", -1.1d, NumberUtils.toDouble("-1.1"), 0.0001d);
    }

    @Test
    public void testToDoubleStringD() {
        assertEquals("toDouble(null, 1.1d) failed", 1.1d, NumberUtils.toDouble(null, 1.1d), 0.0001d);
        assertEquals("toDouble(\"\", 1.1d) failed", 1.1d, NumberUtils.toDouble("", 1.1d), 0.0001d);
        assertEquals("toDouble(\"1.1\", 2.2d) failed", 1.1d, NumberUtils.toDouble("1.1", 2.2d), 0.0001d);
        assertEquals("toDouble(\"bad\", 1.1d) failed", 1.1d, NumberUtils.toDouble("bad", 1.1d), 0.0001d);
    }

    // -----------------------------------------------------------------------
    // toByte tests
    // -----------------------------------------------------------------------
    @Test
    public void testToByteString() {
        assertEquals("toByte(null) failed", (byte) 0, NumberUtils.toByte(null));
        assertEquals("toByte(\"\") failed", (byte) 0, NumberUtils.toByte(""));
        assertEquals("toByte(\"1\") failed", (byte) 1, NumberUtils.toByte("1"));
        assertEquals("toByte(\"bad\") failed", (byte) 0, NumberUtils.toByte("bad"));
        assertEquals("toByte(\"-12\") failed", (byte) -12, NumberUtils.toByte("-12"));
    }

    @Test
    public void testToByteStringB() {
        assertEquals("toByte(null, 1) failed", (byte) 1, NumberUtils.toByte(null, (byte) 1));
        assertEquals("toByte(\"\", 1) failed", (byte) 1, NumberUtils.toByte("", (byte) 1));
        assertEquals("toByte(\"1\", 2) failed", (byte) 1, NumberUtils.toByte("1", (byte) 2));
        assertEquals("toByte(\"bad\", 1) failed", (byte) 1, NumberUtils.toByte("bad", (byte) 1));
    }

    // -----------------------------------------------------------------------
    // toShort tests
    // -----------------------------------------------------------------------
    @Test
    public void testToShortString() {
        assertEquals("toShort(null) failed", (short) 0, NumberUtils.toShort(null));
        assertEquals("toShort(\"\") failed", (short) 0, NumberUtils.toShort(""));
        assertEquals("toShort(\"1\") failed", (short) 1, NumberUtils.toShort("1"));
        assertEquals("toShort(\"bad\") failed", (short) 0, NumberUtils.toShort("bad"));
        assertEquals("toShort(\"-123\") failed", (short) -123, NumberUtils.toShort("-123"));
    }

    @Test
    public void testToShortStringS() {
        assertEquals("toShort(null, 1) failed", (short) 1, NumberUtils.toShort(null, (short) 1));
        assertEquals("toShort(\"\", 1) failed", (short) 1, NumberUtils.toShort("", (short) 1));
        assertEquals("toShort(\"1\", 2) failed", (short) 1, NumberUtils.toShort("1", (short) 2));
        assertEquals("toShort(\"bad\", 1) failed", (short) 1, NumberUtils.toShort("bad", (short) 1));
    }

    // -----------------------------------------------------------------------
    // createNumber tests
    // -----------------------------------------------------------------------
    @Test
    public void testCreateNumber() {
        assertNull(NumberUtils.createNumber(null));
        assertEquals(Integer.valueOf(123), NumberUtils.createNumber("123"));
        assertEquals(Integer.valueOf(-123), NumberUtils.createNumber("-123"));
        assertEquals(Integer.valueOf(0), NumberUtils.createNumber("0"));
        assertEquals(Integer.valueOf(-0), NumberUtils.createNumber("-0"));
        assertEquals(Long.valueOf(123456789012345L), NumberUtils.createNumber("123456789012345"));
        assertEquals(new BigInteger("123456789012345678901234567890"), NumberUtils.createNumber("123456789012345678901234567890"));

        assertEquals(Float.valueOf(12.34f), NumberUtils.createNumber("12.34f"));
        assertEquals(Float.valueOf(12.34F), NumberUtils.createNumber("12.34F"));
        assertEquals(Float.valueOf(-12.34f), NumberUtils.createNumber("-12.34f"));
        assertEquals(Float.valueOf(1234f), NumberUtils.createNumber("1234f"));

        assertEquals(Double.valueOf(12.34d), NumberUtils.createNumber("12.34d"));
        assertEquals(Double.valueOf(12.34D), NumberUtils.createNumber("12.34D"));
        assertEquals(Double.valueOf(-12.34d), NumberUtils.createNumber("-12.34d"));
        assertEquals(Double.valueOf(1234d), NumberUtils.createNumber("1234d"));

        assertEquals(Long.valueOf(1234L), NumberUtils.createNumber("1234l"));
        assertEquals(Long.valueOf(1234L), NumberUtils.createNumber("1234L"));
        assertEquals(Long.valueOf(-1234L), NumberUtils.createNumber("-1234L"));

        assertEquals(Float.valueOf(12.34f), NumberUtils.createNumber("12.34"));
        assertEquals(Double.valueOf(Double.MAX_VALUE), NumberUtils.createNumber(Double.toString(Double.MAX_VALUE)));
        assertEquals(new BigDecimal("1.7976931348623157e+309"), NumberUtils.createNumber("1.7976931348623157e+309"));

        assertEquals(Integer.valueOf(0x12ab), NumberUtils.createNumber("0x12ab"));
        assertEquals(Integer.valueOf(0x12AB), NumberUtils.createNumber("0X12AB"));
        assertEquals(Integer.valueOf(-0x12ab), NumberUtils.createNumber("-0x12ab"));
        assertEquals(Integer.valueOf(-0x12AB), NumberUtils.createNumber("-0X12AB"));
        assertEquals(Integer.valueOf(0x12ab), NumberUtils.createNumber("#12ab"));
        assertEquals(Integer.valueOf(-0x12ab), NumberUtils.createNumber("-#12ab"));
        assertEquals(new BigInteger("7fffffffffffffff", 16), NumberUtils.createNumber("0x7fffffffffffffff"));
        assertEquals(new BigInteger("ffffffffffffffff", 16), NumberUtils.createNumber("0xffffffffffffffff"));
        assertEquals(new BigInteger("7fffffffffffffffffffffffffffffff", 16), NumberUtils.createNumber("0x7fffffffffffffffffffffffffffffff"));

        assertEquals(Float.valueOf("1.2e3"), NumberUtils.createNumber("1.2e3f"));
        assertEquals(Double.valueOf("1.2e3"), NumberUtils.createNumber("1.2e3d"));
        assertEquals(Double.valueOf("1.2e300"), NumberUtils.createNumber("1.2e300"));
        assertEquals(Float.valueOf("1.2e3"), NumberUtils.createNumber("1.2e3"));
        assertEquals(Float.valueOf("-1.2e3"), NumberUtils.createNumber("-1.2e3"));
        assertEquals(Float.valueOf("1.2e-3"), NumberUtils.createNumber("1.2e-3"));
        assertEquals(Float.valueOf("1.2e+3"), NumberUtils.createNumber("1.2e+3"));
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
    public void testCreateNumberLeadingSignHexBug() {
        NumberUtils.createNumber("+0x1234");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberLeadingSignHash() {
        NumberUtils.createNumber("+#1234");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberLeadingUnderscore() {
        NumberUtils.createNumber("_1234");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberInvalidHex() {
        NumberUtils.createNumber("0xz");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberJustSign() {
        NumberUtils.createNumber("-");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberJustDot() {
        NumberUtils.createNumber(".");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberInvalidExponent() {
        NumberUtils.createNumber("12e");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberInvalidExponent2() {
        NumberUtils.createNumber("12e+");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberMultipleDots() {
        NumberUtils.createNumber("1.2.3");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberMultipleE() {
        NumberUtils.createNumber("1ee2");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberInvalidSuffix() {
        NumberUtils.createNumber("123a");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberInvalidSuffixWithDecimals() {
        NumberUtils.createNumber("12.3a");
    }

    // -----------------------------------------------------------------------
    // Specific create* helper methods
    // -----------------------------------------------------------------------
    @Test
    public void testCreateFloat() {
        assertNull(NumberUtils.createFloat(null));
        assertEquals(Float.valueOf("12.34"), NumberUtils.createFloat("12.34"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateFloatFailure() {
        NumberUtils.createFloat("bad");
    }

    @Test
    public void testCreateDouble() {
        assertNull(NumberUtils.createDouble(null));
        assertEquals(Double.valueOf("12.34"), NumberUtils.createDouble("12.34"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateDoubleFailure() {
        NumberUtils.createDouble("bad");
    }

    @Test
    public void testCreateInteger() {
        assertNull(NumberUtils.createInteger(null));
        assertEquals(Integer.valueOf("1234"), NumberUtils.createInteger("1234"));
        assertEquals(Integer.valueOf("0x12"), NumberUtils.createInteger("0x12"));
        assertEquals(Integer.valueOf("#12"), NumberUtils.createInteger("#12"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateIntegerFailure() {
        NumberUtils.createInteger("bad");
    }

    @Test
    public void testCreateLong() {
        assertNull(NumberUtils.createLong(null));
        assertEquals(Long.valueOf("1234567890"), NumberUtils.createLong("1234567890"));
        assertEquals(Long.valueOf("0x12"), NumberUtils.createLong("0x12"));
        assertEquals(Long.valueOf("#12"), NumberUtils.createLong("#12"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateLongFailure() {
        NumberUtils.createLong("bad");
    }

    @Test
    public void testCreateBigInteger() {
        assertNull(NumberUtils.createBigInteger(null));
        assertEquals(new BigInteger("12345678901234567890"), NumberUtils.createBigInteger("12345678901234567890"));
        assertEquals(new BigInteger("12", 16), NumberUtils.createBigInteger("0x12"));
        assertEquals(new BigInteger("12", 16), NumberUtils.createBigInteger("0X12"));
        assertEquals(new BigInteger("12", 16), NumberUtils.createBigInteger("#12"));
        assertEquals(new BigInteger("-12", 16), NumberUtils.createBigInteger("-0x12"));
        assertEquals(new BigInteger("-12", 16), NumberUtils.createBigInteger("-0X12"));
        assertEquals(new BigInteger("-12", 16), NumberUtils.createBigInteger("-#12"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateBigIntegerFailure() {
        NumberUtils.createBigInteger("bad");
    }

    @Test
    public void testCreateBigDecimal() {
        assertNull(NumberUtils.createBigDecimal(null));
        assertEquals(new BigDecimal("1234567890.1234567890"), NumberUtils.createBigDecimal("1234567890.1234567890"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateBigDecimalBlank() {
        NumberUtils.createBigDecimal("");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateBigDecimalFailure() {
        NumberUtils.createBigDecimal("bad");
    }

    // -----------------------------------------------------------------------
    // min / max tests (primitive arguments)
    // -----------------------------------------------------------------------
    @Test
    public void testMinLongLongLong() {
        assertEquals(1L, NumberUtils.min(1L, 2L, 3L));
        assertEquals(1L, NumberUtils.min(2L, 1L, 3L));
        assertEquals(1L, NumberUtils.min(3L, 2L, 1L));
        assertEquals(1L, NumberUtils.min(1L, 1L, 1L));
    }

    @Test
    public void testMinIntIntInt() {
        assertEquals(1, NumberUtils.min(1, 2, 3));
        assertEquals(1, NumberUtils.min(2, 1, 3));
        assertEquals(1, NumberUtils.min(3, 2, 1));
        assertEquals(1, NumberUtils.min(1, 1, 1));
    }

    @Test
    public void testMinShortShortShort() {
        assertEquals((short) 1, NumberUtils.min((short) 1, (short) 2, (short) 3));
        assertEquals((short) 1, NumberUtils.min((short) 2, (short) 1, (short) 3));
        assertEquals((short) 1, NumberUtils.min((short) 3, (short) 2, (short) 1));
        assertEquals((short) 1, NumberUtils.min((short) 1, (short) 1, (short) 1));
    }

    @Test
    public void testMinByteByteByte() {
        assertEquals((byte) 1, NumberUtils.min((byte) 1, (byte) 2, (byte) 3));
        assertEquals((byte) 1, NumberUtils.min((byte) 2, (byte) 1, (byte) 3));
        assertEquals((byte) 1, NumberUtils.min((byte) 3, (byte) 2, (byte) 1));
        assertEquals((byte) 1, NumberUtils.min((byte) 1, (byte) 1, (byte) 1));
    }

    @Test
    public void testMinDoubleDoubleDouble() {
        assertEquals(1.1d, NumberUtils.min(1.1d, 2.2d, 3.3d), 0.0001d);
        assertEquals(1.1d, NumberUtils.min(2.2d, 1.1d, 3.3d), 0.0001d);
        assertEquals(1.1d, NumberUtils.min(3.3d, 2.2d, 1.1d), 0.0001d);
        assertEquals(1.1d, NumberUtils.min(1.1d, 1.1d, 1.1d), 0.0001d);
        assertEquals(1.1d, NumberUtils.min(Double.NaN, 1.1d, 2.2d), 0.0001d);
        assertEquals(1.1d, NumberUtils.min(1.1d, Double.NaN, 2.2d), 0.0001d);
        assertEquals(1.1d, NumberUtils.min(2.2d, 1.1d, Double.NaN), 0.0001d);
        assertTrue(Double.isNaN(NumberUtils.min(Double.NaN, Double.NaN, Double.NaN)));
    }

    @Test
    public void testMinFloatFloatFloat() {
        assertEquals(1.1f, NumberUtils.min(1.1f, 2.2f, 3.3f), 0.0001f);
        assertEquals(1.1f, NumberUtils.min(2.2f, 1.1f, 3.3f), 0.0001f);
        assertEquals(1.1f, NumberUtils.min(3.3f, 2.2f, 1.1f), 0.0001f);
        assertEquals(1.1f, NumberUtils.min(1.1f, 1.1f, 1.1f), 0.0001f);
        assertEquals(1.1f, NumberUtils.min(Float.NaN, 1.1f, 2.2f), 0.0001f);
        assertEquals(1.1f, NumberUtils.min(1.1f, Float.NaN, 2.2f), 0.0001f);
        assertEquals(1.1f, NumberUtils.min(2.2f, 1.1f, Float.NaN), 0.0001f);
        assertTrue(Float.isNaN(NumberUtils.min(Float.NaN, Float.NaN, Float.NaN)));
    }

    @Test
    public void testMaxLongLongLong() {
        assertEquals(3L, NumberUtils.max(1L, 2L, 3L));
        assertEquals(3L, NumberUtils.max(2L, 3L, 1L));
        assertEquals(3L, NumberUtils.max(3L, 2L, 1L));
        assertEquals(1L, NumberUtils.max(1L, 1L, 1L));
    }

    @Test
    public void testMaxIntIntInt() {
        assertEquals(3, NumberUtils.max(1, 2, 3));
        assertEquals(3, NumberUtils.max(2, 3, 1));
        assertEquals(3, NumberUtils.max(3, 2, 1));
        assertEquals(1, NumberUtils.max(1, 1, 1));
    }

    @Test
    public void testMaxShortShortShort() {
        assertEquals((short) 3, NumberUtils.max((short) 1, (short) 2, (short) 3));
        assertEquals((short) 3, NumberUtils.max((short) 2, (short) 3, (short) 1));
        assertEquals((short) 3, NumberUtils.max((short) 3, (short) 2, (short) 1));
        assertEquals((short) 1, NumberUtils.max((short) 1, (short) 1, (short) 1));
    }

    @Test
    public void testMaxByteByteByte() {
        assertEquals((byte) 3, NumberUtils.max((byte) 1, (byte) 2, (byte) 3));
        assertEquals((byte) 3, NumberUtils.max((byte) 2, (byte) 3, (byte) 1));
        assertEquals((byte) 3, NumberUtils.max((byte) 3, (byte) 2, (byte) 1));
        assertEquals((byte) 1, NumberUtils.max((byte) 1, (byte) 1, (byte) 1));
    }

    @Test
    public void testMaxDoubleDoubleDouble() {
        assertEquals(3.3d, NumberUtils.max(1.1d, 2.2d, 3.3d), 0.0001d);
        assertEquals(3.3d, NumberUtils.max(2.2d, 3.3d, 1.1d), 0.0001d);
        assertEquals(3.3d, NumberUtils.max(3.3d, 2.2d, 1.1d), 0.0001d);
        assertEquals(1.1d, NumberUtils.max(1.1d, 1.1d, 1.1d), 0.0001d);
        assertEquals(2.2d, NumberUtils.max(Double.NaN, 1.1d, 2.2d), 0.0001d);
        assertEquals(2.2d, NumberUtils.max(1.1d, Double.NaN, 2.2d), 0.0001d);
        assertEquals(2.2d, NumberUtils.max(2.2d, 1.1d, Double.NaN), 0.0001d);
        assertTrue(Double.isNaN(NumberUtils.max(Double.NaN, Double.NaN, Double.NaN)));
    }

    @Test
    public void testMaxFloatFloatFloat() {
        assertEquals(3.3f, NumberUtils.max(1.1f, 2.2f, 3.3f), 0.0001f);
        assertEquals(3.3f, NumberUtils.max(2.2f, 3.3f, 1.1f), 0.0001f);
        assertEquals(3.3f, NumberUtils.max(3.3f, 2.2f, 1.1f), 0.0001f);
        assertEquals(1.1f, NumberUtils.max(1.1f, 1.1f, 1.1f), 0.0001f);
        assertEquals(2.2f, NumberUtils.max(Float.NaN, 1.1f, 2.2f), 0.0001f);
        assertEquals(2.2f, NumberUtils.max(1.1f, Float.NaN, 2.2f), 0.0001f);
        assertEquals(2.2f, NumberUtils.max(2.2f, 1.1f, Float.NaN), 0.0001f);
        assertTrue(Float.isNaN(NumberUtils.max(Float.NaN, Float.NaN, Float.NaN)));
    }

    // -----------------------------------------------------------------------
    // min / max tests (array arguments)
    // -----------------------------------------------------------------------
    @Test
    public void testMinLongArray() {
        assertEquals(1L, NumberUtils.min(new long[]{1L, 2L, 3L}));
        assertEquals(1L, NumberUtils.min(new long[]{3L, 2L, 1L}));
        assertEquals(5L, NumberUtils.min(new long[]{5L}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinLongArrayNull() {
        NumberUtils.min((long[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinLongArrayEmpty() {
        NumberUtils.min(new long[]{});
    }

    @Test
    public void testMinIntArray() {
        assertEquals(1, NumberUtils.min(new int[]{1, 2, 3}));
        assertEquals(1, NumberUtils.min(new int[]{3, 2, 1}));
        assertEquals(5, NumberUtils.min(new int[]{5}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinIntArrayNull() {
        NumberUtils.min((int[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinIntArrayEmpty() {
        NumberUtils.min(new int[]{});
    }

    @Test
    public void testMinShortArray() {
        assertEquals((short) 1, NumberUtils.min(new short[]{1, 2, 3}));
        assertEquals((short) 1, NumberUtils.min(new short[]{3, 2, 1}));
        assertEquals((short) 5, NumberUtils.min(new short[]{5}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinShortArrayNull() {
        NumberUtils.min((short[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinShortArrayEmpty() {
        NumberUtils.min(new short[]{});
    }

    @Test
    public void testMinByteArray() {
        assertEquals((byte) 1, NumberUtils.min(new byte[]{1, 2, 3}));
        assertEquals((byte) 1, NumberUtils.min(new byte[]{3, 2, 1}));
        assertEquals((byte) 5, NumberUtils.min(new byte[]{5}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinByteArrayNull() {
        NumberUtils.min((byte[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinByteArrayEmpty() {
        NumberUtils.min(new byte[]{});
    }

    @Test
    public void testMinDoubleArray() {
        assertEquals(1.1d, NumberUtils.min(new double[]{1.1d, 2.2d, 3.3d}), 0.0001d);
        assertEquals(1.1d, NumberUtils.min(new double[]{3.3d, 2.2d, 1.1d}), 0.0001d);
        assertEquals(1.1d, NumberUtils.min(new double[]{Double.NaN, 1.1d, 2.2d}), 0.0001d);
        assertEquals(1.1d, NumberUtils.min(new double[]{2.2d, 1.1d, Double.NaN}), 0.0001d);
        assertEquals(5.5d, NumberUtils.min(new double[]{5.5d}), 0.0001d);
        assertTrue(Double.isNaN(NumberUtils.min(new double[]{Double.NaN, Double.NaN})));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinDoubleArrayNull() {
        NumberUtils.min((double[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinDoubleArrayEmpty() {
        NumberUtils.min(new double[]{});
    }

    @Test
    public void testMinFloatArray() {
        assertEquals(1.1f, NumberUtils.min(new float[]{1.1f, 2.2f, 3.3f}), 0.0001f);
        assertEquals(1.1f, NumberUtils.min(new float[]{3.3f, 2.2f, 1.1f}), 0.0001f);
        assertEquals(1.1f, NumberUtils.min(new float[]{Float.NaN, 1.1f, 2.2f}), 0.0001f);
        assertEquals(1.1f, NumberUtils.min(new float[]{2.2f, 1.1f, Float.NaN}), 0.0001f);
        assertEquals(5.5f, NumberUtils.min(new float[]{5.5f}), 0.0001f);
        assertTrue(Float.isNaN(NumberUtils.min(new float[]{Float.NaN, Float.NaN})));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinFloatArrayNull() {
        NumberUtils.min((float[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinFloatArrayEmpty() {
        NumberUtils.min(new float[]{});
    }

    @Test
    public void testMaxLongArray() {
        assertEquals(3L, NumberUtils.max(new long[]{1L, 2L, 3L}));
        assertEquals(3L, NumberUtils.max(new long[]{3L, 2L, 1L}));
        assertEquals(5L, NumberUtils.max(new long[]{5L}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxLongArrayNull() {
        NumberUtils.max((long[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxLongArrayEmpty() {
        NumberUtils.max(new long[]{});
    }

    @Test
    public void testMaxIntArray() {
        assertEquals(3, NumberUtils.max(new int[]{1, 2, 3}));
        assertEquals(3, NumberUtils.max(new int[]{3, 2, 1}));
        assertEquals(5, NumberUtils.max(new int[]{5}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxIntArrayNull() {
        NumberUtils.max((int[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxIntArrayEmpty() {
        NumberUtils.max(new int[]{});
    }

    @Test
    public void testMaxShortArray() {
        assertEquals((short) 3, NumberUtils.max(new short[]{1, 2, 3}));
        assertEquals((short) 3, NumberUtils.max(new short[]{3, 2, 1}));
        assertEquals((short) 5, NumberUtils.max(new short[]{5}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxShortArrayNull() {
        NumberUtils.max((short[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxShortArrayEmpty() {
        NumberUtils.max(new short[]{});
    }

    @Test
    public void testMaxByteArray() {
        assertEquals((byte) 3, NumberUtils.max(new byte[]{1, 2, 3}));
        assertEquals((byte) 3, NumberUtils.max(new byte[]{3, 2, 1}));
        assertEquals((byte) 5, NumberUtils.max(new byte[]{5}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxByteArrayNull() {
        NumberUtils.max((byte[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxByteArrayEmpty() {
        NumberUtils.max(new byte[]{});
    }

    @Test
    public void testMaxDoubleArray() {
        assertEquals(3.3d, NumberUtils.max(new double[]{1.1d, 2.2d, 3.3d}), 0.0001d);
        assertEquals(3.3d, NumberUtils.max(new double[]{3.3d, 2.2d, 1.1d}), 0.0001d);
        assertEquals(2.2d, NumberUtils.max(new double[]{Double.NaN, 1.1d, 2.2d}), 0.0001d);
        assertEquals(2.2d, NumberUtils.max(new double[]{2.2d, 1.1d, Double.NaN}), 0.0001d);
        assertEquals(5.5d, NumberUtils.max(new double[]{5.5d}), 0.0001d);
        assertTrue(Double.isNaN(NumberUtils.max(new double[]{Double.NaN, Double.NaN})));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxDoubleArrayNull() {
        NumberUtils.max((double[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxDoubleArrayEmpty() {
        NumberUtils.max(new double[]{});
    }

    @Test
    public void testMaxFloatArray() {
        assertEquals(3.3f, NumberUtils.max(new float[]{1.1f, 2.2f, 3.3f}), 0.0001f);
        assertEquals(3.3f, NumberUtils.max(new float[]{3.3f, 2.2f, 1.1f}), 0.0001f);
        assertEquals(2.2f, NumberUtils.max(new float[]{Float.NaN, 1.1f, 2.2f}), 0.0001f);
        assertEquals(2.2f, NumberUtils.max(new float[]{2.2f, 1.1f, Float.NaN}), 0.0001f);
        assertEquals(5.5f, NumberUtils.max(new float[]{5.5f}), 0.0001f);
        assertTrue(Float.isNaN(NumberUtils.max(new float[]{Float.NaN, Float.NaN})));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxFloatArrayNull() {
        NumberUtils.max((float[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxFloatArrayEmpty() {
        NumberUtils.max(new float[]{});
    }

    // -----------------------------------------------------------------------
    // isDigits tests
    // -----------------------------------------------------------------------
    @Test
    public void testIsDigits() {
        assertFalse(NumberUtils.isDigits(null));
        assertFalse(NumberUtils.isDigits(""));
        assertTrue(NumberUtils.isDigits("12345"));
        assertFalse(NumberUtils.isDigits("1234.5"));
        assertFalse(NumberUtils.isDigits("1234a"));
        assertFalse(NumberUtils.isDigits("-12345"));
    }

    // -----------------------------------------------------------------------
    // isNumber tests (targeting Defects4J Lang number parsing branches)
    // -----------------------------------------------------------------------
    @Test
    public void testIsNumber() {
        assertFalse(NumberUtils.isNumber(null));
        assertFalse(NumberUtils.isNumber(""));
        assertFalse(NumberUtils.isNumber("   "));
        assertTrue(NumberUtils.isNumber("123"));
        assertTrue(NumberUtils.isNumber("-123"));
        assertTrue(NumberUtils.isNumber("+123"));
        assertTrue(NumberUtils.isNumber("123.45"));
        assertTrue(NumberUtils.isNumber("-123.45"));
        assertTrue(NumberUtils.isNumber(".45"));
        assertTrue(NumberUtils.isNumber("-.45"));
        assertTrue(NumberUtils.isNumber("+.45"));
        assertFalse(NumberUtils.isNumber("."));
        assertFalse(NumberUtils.isNumber("-."));
        assertFalse(NumberUtils.isNumber("+."));
        assertFalse(NumberUtils.isNumber("123.45.67"));

        // Exponents
        assertTrue(NumberUtils.isNumber("123e4"));
        assertTrue(NumberUtils.isNumber("123E4"));
        assertTrue(NumberUtils.isNumber("123e+4"));
        assertTrue(NumberUtils.isNumber("123e-4"));
        assertTrue(NumberUtils.isNumber("-123e-4"));
        assertTrue(NumberUtils.isNumber(".123e-4"));
        assertFalse(NumberUtils.isNumber("123e"));
        assertFalse(NumberUtils.isNumber("123e+"));
        assertFalse(NumberUtils.isNumber("123e-"));
        assertFalse(NumberUtils.isNumber("123e4e5"));
        assertFalse(NumberUtils.isNumber("123e4.5"));

        // Hexadecimal
        assertTrue(NumberUtils.isNumber("0x1234"));
        assertTrue(NumberUtils.isNumber("0X1234"));
        assertTrue(NumberUtils.isNumber("-0x1234"));
        assertTrue(NumberUtils.isNumber("-0X1234"));
        assertTrue(NumberUtils.isNumber("+0x1234"));
        assertTrue(NumberUtils.isNumber("+0X1234"));
        assertTrue(NumberUtils.isNumber("0xabcdefABCDEF"));
        assertFalse(NumberUtils.isNumber("0x"));
        assertFalse(NumberUtils.isNumber("-0x"));
        assertFalse(NumberUtils.isNumber("0x1234g"));
        assertFalse(NumberUtils.isNumber("0x1234.5"));

        // Type suffixes
        assertTrue(NumberUtils.isNumber("1234l"));
        assertTrue(NumberUtils.isNumber("1234L"));
        assertTrue(NumberUtils.isNumber("1234f"));
        assertTrue(NumberUtils.isNumber("1234F"));
        assertTrue(NumberUtils.isNumber("1234d"));
        assertTrue(NumberUtils.isNumber("1234D"));
        assertTrue(NumberUtils.isNumber("12.34f"));
        assertTrue(NumberUtils.isNumber("12.34F"));
        assertTrue(NumberUtils.isNumber("12.34d"));
        assertTrue(NumberUtils.isNumber("12.34D"));
        assertTrue(NumberUtils.isNumber("1.2e3f"));
        assertTrue(NumberUtils.isNumber("1.2e3d"));
        assertTrue(NumberUtils.isNumber("1.2e3l"));
        assertFalse(NumberUtils.isNumber("12.34l"));
        assertFalse(NumberUtils.isNumber("1234a"));
        assertFalse(NumberUtils.isNumber("1234-"));
        assertFalse(NumberUtils.isNumber("1234+"));
        assertFalse(NumberUtils.isNumber("l"));
        assertFalse(NumberUtils.isNumber("f"));
        assertFalse(NumberUtils.isNumber("d"));
    }
}