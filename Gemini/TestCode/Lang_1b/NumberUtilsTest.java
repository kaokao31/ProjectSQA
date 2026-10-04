package org.apache.commons.lang3.math;

import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
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
        Constructor<?>[] cons = NumberUtils.class.getDeclaredConstructors();
        assertEquals(1, cons.length);
        assertTrue(Modifier.isPublic(cons[0].getModifiers()));
    }

    //-----------------------------------------------------------------------
    @Test
    public void testToInt() {
        assertEquals(123, NumberUtils.toInt("123"));
        assertEquals(0, NumberUtils.toInt(null));
        assertEquals(0, NumberUtils.toInt(""));
        assertEquals(0, NumberUtils.toInt("   "));
        assertEquals(0, NumberUtils.toInt("abc"));
        assertEquals(123, NumberUtils.toInt("123", 456));
        assertEquals(456, NumberUtils.toInt(null, 456));
        assertEquals(456, NumberUtils.toInt("", 456));
        assertEquals(456, NumberUtils.toInt("abc", 456));
        assertEquals(Integer.MAX_VALUE, NumberUtils.toInt(String.valueOf(Integer.MAX_VALUE)));
        assertEquals(Integer.MIN_VALUE, NumberUtils.toInt(String.valueOf(Integer.MIN_VALUE)));
    }

    @Test
    public void testToLong() {
        assertEquals(123L, NumberUtils.toLong("123"));
        assertEquals(0L, NumberUtils.toLong(null));
        assertEquals(0L, NumberUtils.toLong(""));
        assertEquals(0L, NumberUtils.toLong("   "));
        assertEquals(0L, NumberUtils.toLong("abc"));
        assertEquals(123L, NumberUtils.toLong("123", 456L));
        assertEquals(456L, NumberUtils.toLong(null, 456L));
        assertEquals(456L, NumberUtils.toLong("", 456L));
        assertEquals(456L, NumberUtils.toLong("abc", 456L));
        assertEquals(Long.MAX_VALUE, NumberUtils.toLong(String.valueOf(Long.MAX_VALUE)));
        assertEquals(Long.MIN_VALUE, NumberUtils.toLong(String.valueOf(Long.MIN_VALUE)));
    }

    @Test
    public void testToFloat() {
        assertEquals(123.45f, NumberUtils.toFloat("123.45"), 0.001f);
        assertEquals(0.0f, NumberUtils.toFloat(null), 0.001f);
        assertEquals(0.0f, NumberUtils.toFloat(""), 0.001f);
        assertEquals(0.0f, NumberUtils.toFloat("   "), 0.001f);
        assertEquals(0.0f, NumberUtils.toFloat("abc"), 0.001f);
        assertEquals(123.45f, NumberUtils.toFloat("123.45", 45.6f), 0.001f);
        assertEquals(45.6f, NumberUtils.toFloat(null, 45.6f), 0.001f);
        assertEquals(45.6f, NumberUtils.toFloat("", 45.6f), 0.001f);
        assertEquals(45.6f, NumberUtils.toFloat("abc", 45.6f), 0.001f);
    }

    @Test
    public void testToDouble() {
        assertEquals(123.45d, NumberUtils.toDouble("123.45"), 0.001d);
        assertEquals(0.0d, NumberUtils.toDouble(null), 0.001d);
        assertEquals(0.0d, NumberUtils.toDouble(""), 0.001d);
        assertEquals(0.0d, NumberUtils.toDouble("   "), 0.001d);
        assertEquals(0.0d, NumberUtils.toDouble("abc"), 0.001d);
        assertEquals(123.45d, NumberUtils.toDouble("123.45", 45.6d), 0.001d);
        assertEquals(45.6d, NumberUtils.toDouble(null, 45.6d), 0.001d);
        assertEquals(45.6d, NumberUtils.toDouble("", 45.6d), 0.001d);
        assertEquals(45.6d, NumberUtils.toDouble("abc", 45.6d), 0.001d);
    }

    @Test
    public void testToByte() {
        assertEquals((byte) 123, NumberUtils.toByte("123"));
        assertEquals((byte) 0, NumberUtils.toByte(null));
        assertEquals((byte) 0, NumberUtils.toByte(""));
        assertEquals((byte) 0, NumberUtils.toByte("   "));
        assertEquals((byte) 0, NumberUtils.toByte("abc"));
        assertEquals((byte) 123, NumberUtils.toByte("123", (byte) 45));
        assertEquals((byte) 45, NumberUtils.toByte(null, (byte) 45));
        assertEquals((byte) 45, NumberUtils.toByte("", (byte) 45));
        assertEquals((byte) 45, NumberUtils.toByte("abc", (byte) 45));
        assertEquals(Byte.MAX_VALUE, NumberUtils.toByte(String.valueOf(Byte.MAX_VALUE)));
        assertEquals(Byte.MIN_VALUE, NumberUtils.toByte(String.valueOf(Byte.MIN_VALUE)));
    }

    @Test
    public void testToShort() {
        assertEquals((short) 123, NumberUtils.toShort("123"));
        assertEquals((short) 0, NumberUtils.toShort(null));
        assertEquals((short) 0, NumberUtils.toShort(""));
        assertEquals((short) 0, NumberUtils.toShort("   "));
        assertEquals((short) 0, NumberUtils.toShort("abc"));
        assertEquals((short) 123, NumberUtils.toShort("123", (short) 456));
        assertEquals((short) 456, NumberUtils.toShort(null, (short) 456));
        assertEquals((short) 456, NumberUtils.toShort("", (short) 456));
        assertEquals((short) 456, NumberUtils.toShort("abc", (short) 456));
        assertEquals(Short.MAX_VALUE, NumberUtils.toShort(String.valueOf(Short.MAX_VALUE)));
        assertEquals(Short.MIN_VALUE, NumberUtils.toShort(String.valueOf(Short.MIN_VALUE)));
    }

    //-----------------------------------------------------------------------
    @Test
    public void testCreateNumber() {
        assertNull(NumberUtils.createNumber(null));
        assertEquals(Integer.valueOf(123), NumberUtils.createNumber("123"));
        assertEquals(Long.valueOf(12345678901L), NumberUtils.createNumber("12345678901"));
        assertEquals(new BigInteger("1234567890123456789012"), NumberUtils.createNumber("1234567890123456789012"));

        assertEquals(Float.valueOf(123.45f), NumberUtils.createNumber("123.45f"));
        assertEquals(Float.valueOf(123.45F), NumberUtils.createNumber("123.45F"));
        assertEquals(Double.valueOf(123.45d), NumberUtils.createNumber("123.45d"));
        assertEquals(Double.valueOf(123.45D), NumberUtils.createNumber("123.45D"));
        assertEquals(Double.valueOf(123.45), NumberUtils.createNumber("123.45"));
        assertEquals(Long.valueOf(123L), NumberUtils.createNumber("123L"));
        assertEquals(Long.valueOf(123L), NumberUtils.createNumber("123l"));

        assertEquals(Integer.valueOf(0x12), NumberUtils.createNumber("0x12"));
        assertEquals(Integer.valueOf(0x12), NumberUtils.createNumber("0X12"));
        assertEquals(Integer.valueOf(-0x12), NumberUtils.createNumber("-0x12"));
        assertEquals(Integer.valueOf(-0x12), NumberUtils.createNumber("-0X12"));
        assertEquals(Integer.valueOf(0x12), NumberUtils.createNumber("#12"));
        assertEquals(Integer.valueOf(-0x12), NumberUtils.createNumber("-#12"));

        assertEquals(new BigInteger("7fffffffffffffff", 16), NumberUtils.createNumber("0x7fffffffffffffff"));
        assertEquals(new BigInteger("8000000000000000", 16), NumberUtils.createNumber("0x8000000000000000"));

        assertEquals(Double.valueOf("1.23e4"), NumberUtils.createNumber("1.23e4"));
        assertEquals(Double.valueOf("1.23E4"), NumberUtils.createNumber("1.23E4"));
        assertEquals(Float.valueOf("1.23e4f"), NumberUtils.createNumber("1.23e4f"));
        assertEquals(Double.valueOf("1.23e4d"), NumberUtils.createNumber("1.23e4d"));
        assertEquals(Double.valueOf("1.23e-4"), NumberUtils.createNumber("1.23e-4"));
        assertEquals(Double.valueOf("1.23e+4"), NumberUtils.createNumber("1.23e+4"));

        assertEquals(new BigDecimal("1.23e400"), NumberUtils.createNumber("1.23e400"));
        assertEquals(new BigDecimal("12345678901234567890.12345678901234567890"),
                NumberUtils.createNumber("12345678901234567890.12345678901234567890"));

        assertEquals(Float.valueOf("0.0f"), NumberUtils.createNumber("0.0f"));
        assertEquals(Double.valueOf("0.0d"), NumberUtils.createNumber("0.0d"));
        assertEquals(Double.valueOf("0.0"), NumberUtils.createNumber("0.0"));
        assertEquals(Integer.valueOf(0), NumberUtils.createNumber("0"));
        assertEquals(Long.valueOf(0L), NumberUtils.createNumber("0L"));

        assertEquals(Float.valueOf(".1f"), NumberUtils.createNumber(".1f"));
        assertEquals(Double.valueOf(".1d"), NumberUtils.createNumber(".1d"));
        assertEquals(Double.valueOf(".1"), NumberUtils.createNumber(".1"));

        assertEquals(Float.valueOf("1.f"), NumberUtils.createNumber("1.f"));
        assertEquals(Double.valueOf("1.d"), NumberUtils.createNumber("1.d"));
        assertEquals(Double.valueOf("1."), NumberUtils.createNumber("1."));
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
    public void testCreateNumberInvalidHex() {
        NumberUtils.createNumber("0x");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberInvalidHexNegative() {
        NumberUtils.createNumber("-0x");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberInvalidHexHash() {
        NumberUtils.createNumber("#");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberInvalidHexHashNegative() {
        NumberUtils.createNumber("-#");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberMultipleDecimals() {
        NumberUtils.createNumber("1.2.3");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberMultipleE() {
        NumberUtils.createNumber("1e2e3");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberTrailingGarbage() {
        NumberUtils.createNumber("123a");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberOnlyE() {
        NumberUtils.createNumber("e");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberOnlyDot() {
        NumberUtils.createNumber(".");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberTrailingExponent() {
        NumberUtils.createNumber("123e");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberTrailingExponentSign() {
        NumberUtils.createNumber("123e+");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberNegativeNoDigits() {
        NumberUtils.createNumber("-");
    }

    //-----------------------------------------------------------------------
    @Test
    public void testCreateFloat() {
        assertNull(NumberUtils.createFloat(null));
        assertEquals(Float.valueOf(123.45f), NumberUtils.createFloat("123.45"));
        assertEquals(Float.valueOf(0.0f), NumberUtils.createFloat("0.0"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateFloatFailure() {
        NumberUtils.createFloat("abc");
    }

    @Test
    public void testCreateDouble() {
        assertNull(NumberUtils.createDouble(null));
        assertEquals(Double.valueOf(123.45d), NumberUtils.createDouble("123.45"));
        assertEquals(Double.valueOf(0.0d), NumberUtils.createDouble("0.0"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateDoubleFailure() {
        NumberUtils.createDouble("abc");
    }

    @Test
    public void testCreateInteger() {
        assertNull(NumberUtils.createInteger(null));
        assertEquals(Integer.valueOf(123), NumberUtils.createInteger("123"));
        assertEquals(Integer.valueOf(-123), NumberUtils.createInteger("-123"));
        assertEquals(Integer.valueOf(0x12), NumberUtils.createInteger("0x12"));
        assertEquals(Integer.valueOf(012), NumberUtils.createInteger("012"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateIntegerFailure() {
        NumberUtils.createInteger("abc");
    }

    @Test
    public void testCreateLong() {
        assertNull(NumberUtils.createLong(null));
        assertEquals(Long.valueOf(123L), NumberUtils.createLong("123"));
        assertEquals(Long.valueOf(-123L), NumberUtils.createLong("-123"));
        assertEquals(Long.valueOf(0x12L), NumberUtils.createLong("0x12"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateLongFailure() {
        NumberUtils.createLong("abc");
    }

    @Test
    public void testCreateBigInteger() {
        assertNull(NumberUtils.createBigInteger(null));
        assertEquals(new BigInteger("12345678901234567890"), NumberUtils.createBigInteger("12345678901234567890"));
        assertEquals(new BigInteger("-12345678901234567890"), NumberUtils.createBigInteger("-12345678901234567890"));
        assertEquals(new BigInteger("10", 16), NumberUtils.createBigInteger("0x10"));
        assertEquals(new BigInteger("-10", 16), NumberUtils.createBigInteger("-0x10"));
        assertEquals(new BigInteger("10", 16), NumberUtils.createBigInteger("#10"));
        assertEquals(new BigInteger("-10", 16), NumberUtils.createBigInteger("-#10"));
        assertEquals(new BigInteger("10", 8), NumberUtils.createBigInteger("010"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateBigIntegerFailure() {
        NumberUtils.createBigInteger("abc");
    }

    @Test
    public void testCreateBigDecimal() {
        assertNull(NumberUtils.createBigDecimal(null));
        assertEquals(new BigDecimal("12345678901234567890.1234567890"),
                NumberUtils.createBigDecimal("12345678901234567890.1234567890"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateBigDecimalFailure() {
        NumberUtils.createBigDecimal("abc");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateBigDecimalBlank() {
        NumberUtils.createBigDecimal("   ");
    }

    //-----------------------------------------------------------------------
    @Test
    public void testMinLongArray() {
        assertEquals(1L, NumberUtils.min(new long[]{1L, 2L, 3L}));
        assertEquals(1L, NumberUtils.min(new long[]{3L, 2L, 1L}));
        assertEquals(1L, NumberUtils.min(new long[]{2L, 1L, 3L}));
        assertEquals(-5L, NumberUtils.min(new long[]{-5L, 0L, 5L}));
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
        assertEquals(1, NumberUtils.min(new int[]{1, 2, 3}));
        assertEquals(1, NumberUtils.min(new int[]{3, 2, 1}));
        assertEquals(1, NumberUtils.min(new int[]{2, 1, 3}));
        assertEquals(-5, NumberUtils.min(new int[]{-5, 0, 5}));
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
        assertEquals((short) 1, NumberUtils.min(new short[]{1, 2, 3}));
        assertEquals((short) 1, NumberUtils.min(new short[]{3, 2, 1}));
        assertEquals((short) 1, NumberUtils.min(new short[]{2, 1, 3}));
        assertEquals((short) -5, NumberUtils.min(new short[]{-5, 0, 5}));
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
        assertEquals((byte) 1, NumberUtils.min(new byte[]{1, 2, 3}));
        assertEquals((byte) 1, NumberUtils.min(new byte[]{3, 2, 1}));
        assertEquals((byte) 1, NumberUtils.min(new byte[]{2, 1, 3}));
        assertEquals((byte) -5, NumberUtils.min(new byte[]{-5, 0, 5}));
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
        assertEquals(1.1d, NumberUtils.min(new double[]{1.1d, 2.2d, 3.3d}), 0.001d);
        assertEquals(1.1d, NumberUtils.min(new double[]{3.3d, 2.2d, 1.1d}), 0.001d);
        assertEquals(1.1d, NumberUtils.min(new double[]{2.2d, 1.1d, 3.3d}), 0.001d);
        assertEquals(-5.5d, NumberUtils.min(new double[]{-5.5d, 0.0d, 5.5d}), 0.001d);
        assertTrue(Double.isNaN(NumberUtils.min(new double[]{1.1d, Double.NaN, 3.3d})));
        assertTrue(Double.isNaN(NumberUtils.min(new double[]{Double.NaN, 1.1d, 3.3d})));
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
        assertEquals(1.1f, NumberUtils.min(new float[]{1.1f, 2.2f, 3.3f}), 0.001f);
        assertEquals(1.1f, NumberUtils.min(new float[]{3.3f, 2.2f, 1.1f}), 0.001f);
        assertEquals(1.1f, NumberUtils.min(new float[]{2.2f, 1.1f, 3.3f}), 0.001f);
        assertEquals(-5.5f, NumberUtils.min(new float[]{-5.5f, 0.0f, 5.5f}), 0.001f);
        assertTrue(Float.isNaN(NumberUtils.min(new float[]{1.1f, Float.NaN, 3.3f})));
        assertTrue(Float.isNaN(NumberUtils.min(new float[]{Float.NaN, 1.1f, 3.3f})));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinFloatArrayNull() {
        NumberUtils.min((float[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinFloatArrayEmpty() {
        NumberUtils.min(new float[0]);
    }

    //-----------------------------------------------------------------------
    @Test
    public void testMaxLongArray() {
        assertEquals(3L, NumberUtils.max(new long[]{1L, 2L, 3L}));
        assertEquals(3L, NumberUtils.max(new long[]{3L, 2L, 1L}));
        assertEquals(3L, NumberUtils.max(new long[]{2L, 3L, 1L}));
        assertEquals(5L, NumberUtils.max(new long[]{-5L, 0L, 5L}));
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
        assertEquals(3, NumberUtils.max(new int[]{3, 2, 1}));
        assertEquals(3, NumberUtils.max(new int[]{2, 3, 1}));
        assertEquals(5, NumberUtils.max(new int[]{-5, 0, 5}));
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
        assertEquals((short) 3, NumberUtils.max(new short[]{1, 2, 3}));
        assertEquals((short) 3, NumberUtils.max(new short[]{3, 2, 1}));
        assertEquals((short) 3, NumberUtils.max(new short[]{2, 3, 1}));
        assertEquals((short) 5, NumberUtils.max(new short[]{-5, 0, 5}));
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
        assertEquals((byte) 3, NumberUtils.max(new byte[]{1, 2, 3}));
        assertEquals((byte) 3, NumberUtils.max(new byte[]{3, 2, 1}));
        assertEquals((byte) 3, NumberUtils.max(new byte[]{2, 3, 1}));
        assertEquals((byte) 5, NumberUtils.max(new byte[]{-5, 0, 5}));
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
        assertEquals(3.3d, NumberUtils.max(new double[]{1.1d, 2.2d, 3.3d}), 0.001d);
        assertEquals(3.3d, NumberUtils.max(new double[]{3.3d, 2.2d, 1.1d}), 0.001d);
        assertEquals(3.3d, NumberUtils.max(new double[]{2.2d, 3.3d, 1.1d}), 0.001d);
        assertEquals(5.5d, NumberUtils.max(new double[]{-5.5d, 0.0d, 5.5d}), 0.001d);
        assertTrue(Double.isNaN(NumberUtils.max(new double[]{1.1d, Double.NaN, 3.3d})));
        assertTrue(Double.isNaN(NumberUtils.max(new double[]{Double.NaN, 1.1d, 3.3d})));
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
        assertEquals(3.3f, NumberUtils.max(new float[]{1.1f, 2.2f, 3.3f}), 0.001f);
        assertEquals(3.3f, NumberUtils.max(new float[]{3.3f, 2.2f, 1.1f}), 0.001f);
        assertEquals(3.3f, NumberUtils.max(new float[]{2.2f, 3.3f, 1.1f}), 0.001f);
        assertEquals(5.5f, NumberUtils.max(new float[]{-5.5f, 0.0f, 5.5f}), 0.001f);
        assertTrue(Float.isNaN(NumberUtils.max(new float[]{1.1f, Float.NaN, 3.3f})));
        assertTrue(Float.isNaN(NumberUtils.max(new float[]{Float.NaN, 1.1f, 3.3f})));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxFloatArrayNull() {
        NumberUtils.max((float[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxFloatArrayEmpty() {
        NumberUtils.max(new float[0]);
    }

    //-----------------------------------------------------------------------
    @Test
    public void testMinThreeValues() {
        assertEquals(1L, NumberUtils.min(1L, 2L, 3L));
        assertEquals(1L, NumberUtils.min(2L, 1L, 3L));
        assertEquals(1L, NumberUtils.min(3L, 2L, 1L));

        assertEquals(1, NumberUtils.min(1, 2, 3));
        assertEquals(1, NumberUtils.min(2, 1, 3));
        assertEquals(1, NumberUtils.min(3, 2, 1));

        assertEquals((short) 1, NumberUtils.min((short) 1, (short) 2, (short) 3));
        assertEquals((short) 1, NumberUtils.min((short) 2, (short) 1, (short) 3));
        assertEquals((short) 1, NumberUtils.min((short) 3, (short) 2, (short) 1));

        assertEquals((byte) 1, NumberUtils.min((byte) 1, (byte) 2, (byte) 3));
        assertEquals((byte) 1, NumberUtils.min((byte) 2, (byte) 1, (byte) 3));
        assertEquals((byte) 1, NumberUtils.min((byte) 3, (byte) 2, (byte) 1));

        assertEquals(1.1d, NumberUtils.min(1.1d, 2.2d, 3.3d), 0.001d);
        assertEquals(1.1d, NumberUtils.min(2.2d, 1.1d, 3.3d), 0.001d);
        assertEquals(1.1d, NumberUtils.min(3.3d, 2.2d, 1.1d), 0.001d);
        assertTrue(Double.isNaN(NumberUtils.min(Double.NaN, 1.1d, 2.2d)));
        assertTrue(Double.isNaN(NumberUtils.min(1.1d, Double.NaN, 2.2d)));
        assertTrue(Double.isNaN(NumberUtils.min(1.1d, 2.2d, Double.NaN)));

        assertEquals(1.1f, NumberUtils.min(1.1f, 2.2f, 3.3f), 0.001f);
        assertEquals(1.1f, NumberUtils.min(2.2f, 1.1f, 3.3f), 0.001f);
        assertEquals(1.1f, NumberUtils.min(3.3f, 2.2f, 1.1f), 0.001f);
        assertTrue(Float.isNaN(NumberUtils.min(Float.NaN, 1.1f, 2.2f)));
        assertTrue(Float.isNaN(NumberUtils.min(1.1f, Float.NaN, 2.2f)));
        assertTrue(Float.isNaN(NumberUtils.min(1.1f, 2.2f, Float.NaN)));
    }

    @Test
    public void testMaxThreeValues() {
        assertEquals(3L, NumberUtils.max(1L, 2L, 3L));
        assertEquals(3L, NumberUtils.max(1L, 3L, 2L));
        assertEquals(3L, NumberUtils.max(3L, 2L, 1L));

        assertEquals(3, NumberUtils.max(1, 2, 3));
        assertEquals(3, NumberUtils.max(1, 3, 2));
        assertEquals(3, NumberUtils.max(3, 2, 1));

        assertEquals((short) 3, NumberUtils.max((short) 1, (short) 2, (short) 3));
        assertEquals((short) 3, NumberUtils.max((short) 1, (short) 3, (short) 2));
        assertEquals((short) 3, NumberUtils.max((short) 3, (short) 2, (short) 1));

        assertEquals((byte) 3, NumberUtils.max((byte) 1, (byte) 2, (byte) 3));
        assertEquals((byte) 3, NumberUtils.max((byte) 1, (byte) 3, (byte) 2));
        assertEquals((byte) 3, NumberUtils.max((byte) 3, (byte) 2, (byte) 1));

        assertEquals(3.3d, NumberUtils.max(1.1d, 2.2d, 3.3d), 0.001d);
        assertEquals(3.3d, NumberUtils.max(1.1d, 3.3d, 2.2d), 0.001d);
        assertEquals(3.3d, NumberUtils.max(3.3d, 2.2d, 1.1d), 0.001d);
        assertTrue(Double.isNaN(NumberUtils.max(Double.NaN, 1.1d, 2.2d)));
        assertTrue(Double.isNaN(NumberUtils.max(1.1d, Double.NaN, 2.2d)));
        assertTrue(Double.isNaN(NumberUtils.max(1.1d, 2.2d, Double.NaN)));

        assertEquals(3.3f, NumberUtils.max(1.1f, 2.2f, 3.3f), 0.001f);
        assertEquals(3.3f, NumberUtils.max(1.1f, 3.3f, 2.2f), 0.001f);
        assertEquals(3.3f, NumberUtils.max(3.3f, 2.2f, 1.1f), 0.001f);
        assertTrue(Float.isNaN(NumberUtils.max(Float.NaN, 1.1f, 2.2f)));
        assertTrue(Float.isNaN(NumberUtils.max(1.1f, Float.NaN, 2.2f)));
        assertTrue(Float.isNaN(NumberUtils.max(1.1f, 2.2f, Float.NaN)));
    }

    //-----------------------------------------------------------------------
    @Test
    public void testIsDigits() {
        assertFalse(NumberUtils.isDigits(null));
        assertFalse(NumberUtils.isDigits(""));
        assertTrue(NumberUtils.isDigits("12345"));
        assertFalse(NumberUtils.isDigits("12345.6"));
        assertFalse(NumberUtils.isDigits("12345a"));
        assertFalse(NumberUtils.isDigits("-12345"));
        assertFalse(NumberUtils.isDigits("+12345"));
    }

    @Test
    public void testIsNumber() {
        assertFalse(NumberUtils.isNumber(null));
        assertFalse(NumberUtils.isNumber(""));
        assertFalse(NumberUtils.isNumber("   "));
        assertFalse(NumberUtils.isNumber("abc"));
        assertFalse(NumberUtils.isNumber("."));
        assertFalse(NumberUtils.isNumber("-"));
        assertFalse(NumberUtils.isNumber("+"));
        assertFalse(NumberUtils.isNumber("1.2.3"));
        assertFalse(NumberUtils.isNumber("1e2e3"));
        assertFalse(NumberUtils.isNumber("123e"));
        assertFalse(NumberUtils.isNumber("123e+"));
        assertFalse(NumberUtils.isNumber("123e-"));
        assertFalse(NumberUtils.isNumber("0x"));
        assertFalse(NumberUtils.isNumber("-0x"));
        assertFalse(NumberUtils.isNumber("0xabcdefg"));

        assertTrue(NumberUtils.isNumber("123"));
        assertTrue(NumberUtils.isNumber("-123"));
        assertTrue(NumberUtils.isNumber("+123"));
        assertTrue(NumberUtils.isNumber("123.45"));
        assertTrue(NumberUtils.isNumber("-123.45"));
        assertTrue(NumberUtils.isNumber("+123.45"));
        assertTrue(NumberUtils.isNumber(".123"));
        assertTrue(NumberUtils.isNumber("-.123"));
        assertTrue(NumberUtils.isNumber("+.123"));
        assertTrue(NumberUtils.isNumber("123."));
        assertTrue(NumberUtils.isNumber("-123."));
        assertTrue(NumberUtils.isNumber("+123."));

        assertTrue(NumberUtils.isNumber("123e4"));
        assertTrue(NumberUtils.isNumber("123E4"));
        assertTrue(NumberUtils.isNumber("123e+4"));
        assertTrue(NumberUtils.isNumber("123e-4"));
        assertTrue(NumberUtils.isNumber("-123e4"));
        assertTrue(NumberUtils.isNumber("+123e4"));

        assertTrue(NumberUtils.isNumber("123L"));
        assertTrue(NumberUtils.isNumber("123l"));
        assertTrue(NumberUtils.isNumber("123.45f"));
        assertTrue(NumberUtils.isNumber("123.45F"));
        assertTrue(NumberUtils.isNumber("123.45d"));
        assertTrue(NumberUtils.isNumber("123.45D"));
        assertTrue(NumberUtils.isNumber(".45f"));
        assertTrue(NumberUtils.isNumber(".45d"));
        assertTrue(NumberUtils.isNumber("45.f"));
        assertTrue(NumberUtils.isNumber("45.d"));
        assertTrue(NumberUtils.isNumber("123e4f"));
        assertTrue(NumberUtils.isNumber("123e4d"));

        assertTrue(NumberUtils.isNumber("0x1234"));
        assertTrue(NumberUtils.isNumber("-0x1234"));
        assertTrue(NumberUtils.isNumber("+0x1234"));
        assertTrue(NumberUtils.isNumber("0X1234"));
        assertTrue(NumberUtils.isNumber("-0X1234"));
        assertTrue(NumberUtils.isNumber("+0X1234"));
        assertTrue(NumberUtils.isNumber("0xABCDEF"));
        assertTrue(NumberUtils.isNumber("0xabcdef"));

        assertFalse(NumberUtils.isNumber("0x12.34"));
        assertFalse(NumberUtils.isNumber("0x1234L"));
        assertFalse(NumberUtils.isNumber("1234L5"));
        assertFalse(NumberUtils.isNumber("--123"));
        assertFalse(NumberUtils.isNumber("++123"));
    }

    @Test
    public void testIsParsable() {
        assertFalse(NumberUtils.isParsable(null));
        assertFalse(NumberUtils.isParsable(""));
        assertFalse(NumberUtils.isParsable("   "));
        assertFalse(NumberUtils.isParsable("abc"));
        assertFalse(NumberUtils.isParsable("."));
        assertFalse(NumberUtils.isParsable("-"));
        assertFalse(NumberUtils.isParsable("1.2.3"));
        assertFalse(NumberUtils.isParsable("123L"));
        assertFalse(NumberUtils.isParsable("123f"));
        assertFalse(NumberUtils.isParsable("123d"));
        assertFalse(NumberUtils.isParsable("0x123"));
        assertFalse(NumberUtils.isParsable("123e4"));
        assertFalse(NumberUtils.isParsable("123."));
        assertFalse(NumberUtils.isParsable(".123"));

        assertTrue(NumberUtils.isParsable("123"));
        assertTrue(NumberUtils.isParsable("-123"));
        assertTrue(NumberUtils.isParsable("123.45"));
        assertTrue(NumberUtils.isParsable("-123.45"));
        assertTrue(NumberUtils.isParsable("0"));
        assertTrue(NumberUtils.isParsable("-0"));
    }
}