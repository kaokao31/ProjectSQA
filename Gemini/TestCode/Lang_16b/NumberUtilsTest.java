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

/**
 * Unit tests for {@link NumberUtils}.
 */
public class NumberUtilsTest {

    @Test
    public void testConstructor() {
        assertNotNull(new NumberUtils());
        Constructor<?>[] cons = NumberUtils.class.getDeclaredConstructors();
        assertEquals(1, cons.length);
        assertTrue(Modifier.isPublic(cons[0].getModifiers()));
        assertTrue(Modifier.isPublic(NumberUtils.class.getModifiers()));
        assertFalse(Modifier.isFinal(NumberUtils.class.getModifiers()));
    }

    //-----------------------------------------------------------------------
    @Test
    public void testToIntString() {
        assertEquals(12345, NumberUtils.toInt("12345"));
        assertEquals(0, NumberUtils.toInt("abc"));
        assertEquals(0, NumberUtils.toInt(null));
        assertEquals(0, NumberUtils.toInt(""));
        assertEquals(0, NumberUtils.toInt("   "));
        assertEquals(Integer.MAX_VALUE, NumberUtils.toInt(String.valueOf(Integer.MAX_VALUE)));
        assertEquals(Integer.MIN_VALUE, NumberUtils.toInt(String.valueOf(Integer.MIN_VALUE)));
    }

    @Test
    public void testToIntStringI() {
        assertEquals(12345, NumberUtils.toInt("12345", 5));
        assertEquals(5, NumberUtils.toInt("abc", 5));
        assertEquals(5, NumberUtils.toInt(null, 5));
        assertEquals(5, NumberUtils.toInt("", 5));
        assertEquals(5, NumberUtils.toInt("   ", 5));
    }

    @Test
    public void testToLongString() {
        assertEquals(12345L, NumberUtils.toLong("12345"));
        assertEquals(0L, NumberUtils.toLong("abc"));
        assertEquals(0L, NumberUtils.toLong(null));
        assertEquals(0L, NumberUtils.toLong(""));
        assertEquals(0L, NumberUtils.toLong("   "));
        assertEquals(Long.MAX_VALUE, NumberUtils.toLong(String.valueOf(Long.MAX_VALUE)));
        assertEquals(Long.MIN_VALUE, NumberUtils.toLong(String.valueOf(Long.MIN_VALUE)));
    }

    @Test
    public void testToLongStringL() {
        assertEquals(12345L, NumberUtils.toLong("12345", 5L));
        assertEquals(5L, NumberUtils.toLong("abc", 5L));
        assertEquals(5L, NumberUtils.toLong(null, 5L));
        assertEquals(5L, NumberUtils.toLong("", 5L));
        assertEquals(5L, NumberUtils.toLong("   ", 5L));
    }

    @Test
    public void testToFloatString() {
        assertEquals(123.45f, NumberUtils.toFloat("123.45"), 0.001f);
        assertEquals(0.0f, NumberUtils.toFloat("abc"), 0.001f);
        assertEquals(0.0f, NumberUtils.toFloat(null), 0.001f);
        assertEquals(0.0f, NumberUtils.toFloat(""), 0.001f);
        assertEquals(0.0f, NumberUtils.toFloat("   "), 0.001f);
    }

    @Test
    public void testToFloatStringF() {
        assertEquals(123.45f, NumberUtils.toFloat("123.45", 5.1f), 0.001f);
        assertEquals(5.1f, NumberUtils.toFloat("abc", 5.1f), 0.001f);
        assertEquals(5.1f, NumberUtils.toFloat(null, 5.1f), 0.001f);
        assertEquals(5.1f, NumberUtils.toFloat("", 5.1f), 0.001f);
        assertEquals(5.1f, NumberUtils.toFloat("   ", 5.1f), 0.001f);
    }

    @Test
    public void testToDoubleString() {
        assertEquals(123.45d, NumberUtils.toDouble("123.45"), 0.001d);
        assertEquals(0.0d, NumberUtils.toDouble("abc"), 0.001d);
        assertEquals(0.0d, NumberUtils.toDouble(null), 0.001d);
        assertEquals(0.0d, NumberUtils.toDouble(""), 0.001d);
        assertEquals(0.0d, NumberUtils.toDouble("   "), 0.001d);
    }

    @Test
    public void testToDoubleStringD() {
        assertEquals(123.45d, NumberUtils.toDouble("123.45", 5.1d), 0.001d);
        assertEquals(5.1d, NumberUtils.toDouble("abc", 5.1d), 0.001d);
        assertEquals(5.1d, NumberUtils.toDouble(null, 5.1d), 0.001d);
        assertEquals(5.1d, NumberUtils.toDouble("", 5.1d), 0.001d);
        assertEquals(5.1d, NumberUtils.toDouble("   ", 5.1d), 0.001d);
    }

    @Test
    public void testToByteString() {
        assertEquals((byte) 123, NumberUtils.toByte("123"));
        assertEquals((byte) 0, NumberUtils.toByte("abc"));
        assertEquals((byte) 0, NumberUtils.toByte(null));
        assertEquals((byte) 0, NumberUtils.toByte(""));
        assertEquals((byte) 0, NumberUtils.toByte("   "));
    }

    @Test
    public void testToByteStringB() {
        assertEquals((byte) 123, NumberUtils.toByte("123", (byte) 5));
        assertEquals((byte) 5, NumberUtils.toByte("abc", (byte) 5));
        assertEquals((byte) 5, NumberUtils.toByte(null, (byte) 5));
        assertEquals((byte) 5, NumberUtils.toByte("", (byte) 5));
        assertEquals((byte) 5, NumberUtils.toByte("   ", (byte) 5));
    }

    @Test
    public void testToShortString() {
        assertEquals((short) 12345, NumberUtils.toShort("12345"));
        assertEquals((short) 0, NumberUtils.toShort("abc"));
        assertEquals((short) 0, NumberUtils.toShort(null));
        assertEquals((short) 0, NumberUtils.toShort(""));
        assertEquals((short) 0, NumberUtils.toShort("   "));
    }

    @Test
    public void testToShortStringS() {
        assertEquals((short) 12345, NumberUtils.toShort("12345", (short) 5));
        assertEquals((short) 5, NumberUtils.toShort("abc", (short) 5));
        assertEquals((short) 5, NumberUtils.toShort(null, (short) 5));
        assertEquals((short) 5, NumberUtils.toShort("", (short) 5));
        assertEquals((short) 5, NumberUtils.toShort("   ", (short) 5));
    }

    //-----------------------------------------------------------------------
    @Test
    public void testCreateNumber() {
        assertNull(NumberUtils.createNumber(null));
        
        assertEquals(Integer.valueOf(12345), NumberUtils.createNumber("12345"));
        assertEquals(Integer.valueOf(-12345), NumberUtils.createNumber("-12345"));
        assertEquals(Long.valueOf(123456789012345L), NumberUtils.createNumber("123456789012345"));
        assertEquals(Long.valueOf(-123456789012345L), NumberUtils.createNumber("-123456789012345"));
        assertEquals(new BigInteger("123456789012345678901234567890"), NumberUtils.createNumber("123456789012345678901234567890"));
        
        // Float & Double
        assertEquals(Float.valueOf(123.45f), NumberUtils.createNumber("123.45f"));
        assertEquals(Float.valueOf(123.45F), NumberUtils.createNumber("123.45F"));
        assertEquals(Float.valueOf(-123.45f), NumberUtils.createNumber("-123.45f"));
        assertEquals(Double.valueOf(123.45d), NumberUtils.createNumber("123.45d"));
        assertEquals(Double.valueOf(123.45D), NumberUtils.createNumber("123.45D"));
        assertEquals(Double.valueOf(-123.45d), NumberUtils.createNumber("-123.45d"));
        assertEquals(Double.valueOf(123.45), NumberUtils.createNumber("123.45"));
        assertEquals(Double.valueOf(-123.45), NumberUtils.createNumber("-123.45"));

        // Long & Integer with suffix
        assertEquals(Long.valueOf(12345L), NumberUtils.createNumber("12345L"));
        assertEquals(Long.valueOf(12345L), NumberUtils.createNumber("12345l"));
        assertEquals(Long.valueOf(-12345L), NumberUtils.createNumber("-12345L"));
        
        // Hexadecimal tests (including bug Lang-16 triggers: 0X / -0X / -0x)
        assertEquals(Integer.valueOf(0x1234), NumberUtils.createNumber("0x1234"));
        assertEquals(Integer.valueOf(0x1234), NumberUtils.createNumber("0X1234"));
        assertEquals(Integer.valueOf(0xFA), NumberUtils.createNumber("0xFA"));
        assertEquals(Integer.valueOf(0xFA), NumberUtils.createNumber("0XFA"));
        assertEquals(Integer.valueOf(0xfade), NumberUtils.createNumber("0Xfade"));
        assertEquals(Integer.valueOf(0xfade), NumberUtils.createNumber("0xfade"));
        assertEquals(Integer.valueOf(-0x1234), NumberUtils.createNumber("-0x1234"));
        assertEquals(Integer.valueOf(-0x1234), NumberUtils.createNumber("-0X1234"));
        assertEquals(Integer.valueOf(-0xfade), NumberUtils.createNumber("-0Xfade"));
        assertEquals(Integer.valueOf(-0xfade), NumberUtils.createNumber("-0xfade"));
        assertEquals(Integer.valueOf(0x1234), NumberUtils.createNumber("#1234"));
        assertEquals(Integer.valueOf(-0x1234), NumberUtils.createNumber("-#1234"));

        // BigInteger and BigDecimal
        assertEquals(new BigInteger("12345678901234567890"), NumberUtils.createNumber("12345678901234567890"));
        assertEquals(new BigDecimal("12345678901234567890.123456789"), NumberUtils.createNumber("12345678901234567890.123456789"));
        
        // Exponents
        assertEquals(Float.valueOf(1.2e3f), NumberUtils.createNumber("1.2e3f"));
        assertEquals(Double.valueOf(1.2e3d), NumberUtils.createNumber("1.2e3d"));
        assertEquals(Double.valueOf(1.2e3), NumberUtils.createNumber("1.2e3"));
        assertEquals(Double.valueOf(-1.2e3), NumberUtils.createNumber("-1.2e3"));
        assertEquals(Double.valueOf(1.2e-3), NumberUtils.createNumber("1.2e-3"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberBlank() {
        NumberUtils.createNumber("");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberWhitespace() {
        NumberUtils.createNumber(" ");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberZeroXOnly() {
        NumberUtils.createNumber("0x");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberNegativeZeroXOnly() {
        NumberUtils.createNumber("-0x");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberNegativeZeroUpperXOnly() {
        NumberUtils.createNumber("-0X");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberHashOnly() {
        NumberUtils.createNumber("#");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberNegativeHashOnly() {
        NumberUtils.createNumber("-#");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberInvalidExponent() {
        NumberUtils.createNumber("1.2e");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberMultipleDecimals() {
        NumberUtils.createNumber("1.2.3");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberInvalidCharacters() {
        NumberUtils.createNumber("123a45");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberInvalidSuffix() {
        NumberUtils.createNumber("12345z");
    }

    //-----------------------------------------------------------------------
    @Test
    public void testCreateFloat() {
        assertNull(NumberUtils.createFloat(null));
        assertEquals(Float.valueOf("123.45"), NumberUtils.createFloat("123.45"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateFloatFailure() {
        NumberUtils.createFloat("invalid");
    }

    @Test
    public void testCreateDouble() {
        assertNull(NumberUtils.createDouble(null));
        assertEquals(Double.valueOf("123.45"), NumberUtils.createDouble("123.45"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateDoubleFailure() {
        NumberUtils.createDouble("invalid");
    }

    @Test
    public void testCreateInteger() {
        assertNull(NumberUtils.createInteger(null));
        assertEquals(Integer.valueOf("12345"), NumberUtils.createInteger("12345"));
        assertEquals(Integer.valueOf(0x12), NumberUtils.createInteger("0x12"));
        assertEquals(Integer.valueOf(0x12), NumberUtils.createInteger("#12"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateIntegerFailure() {
        NumberUtils.createInteger("invalid");
    }

    @Test
    public void testCreateLong() {
        assertNull(NumberUtils.createLong(null));
        assertEquals(Long.valueOf("1234567890"), NumberUtils.createLong("1234567890"));
        assertEquals(Long.valueOf(0x12), NumberUtils.createLong("0x12"));
        assertEquals(Long.valueOf(0x12), NumberUtils.createLong("#12"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateLongFailure() {
        NumberUtils.createLong("invalid");
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
        NumberUtils.createBigInteger("invalid");
    }

    @Test
    public void testCreateBigDecimal() {
        assertNull(NumberUtils.createBigDecimal(null));
        assertEquals(new BigDecimal("12345678901234567890.123456789"), NumberUtils.createBigDecimal("12345678901234567890.123456789"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateBigDecimalFailure() {
        NumberUtils.createBigDecimal("invalid");
    }

    //-----------------------------------------------------------------------
    @Test
    public void testMinLongArray() {
        assertEquals(5L, NumberUtils.min(new long[]{5L}));
        assertEquals(1L, NumberUtils.min(new long[]{5L, 3L, 1L, 2L, 4L}));
        assertEquals(-5L, NumberUtils.min(new long[]{5L, -5L, 0L}));
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
        assertEquals(5, NumberUtils.min(new int[]{5}));
        assertEquals(1, NumberUtils.min(new int[]{5, 3, 1, 2, 4}));
        assertEquals(-5, NumberUtils.min(new int[]{5, -5, 0}));
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
        assertEquals((short) 5, NumberUtils.min(new short[]{5}));
        assertEquals((short) 1, NumberUtils.min(new short[]{5, 3, 1, 2, 4}));
        assertEquals((short) -5, NumberUtils.min(new short[]{5, -5, 0}));
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
        assertEquals((byte) 5, NumberUtils.min(new byte[]{5}));
        assertEquals((byte) 1, NumberUtils.min(new byte[]{5, 3, 1, 2, 4}));
        assertEquals((byte) -5, NumberUtils.min(new byte[]{5, -5, 0}));
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
        assertEquals(5.5d, NumberUtils.min(new double[]{5.5d}), 0.0001d);
        assertEquals(1.1d, NumberUtils.min(new double[]{5.5d, 3.3d, 1.1d, 2.2d, 4.4d}), 0.0001d);
        assertEquals(-5.5d, NumberUtils.min(new double[]{5.5d, -5.5d, 0.0d}), 0.0001d);
        assertTrue(Double.isNaN(NumberUtils.min(new double[]{1.0d, Double.NaN})));
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
        assertEquals(5.5f, NumberUtils.min(new float[]{5.5f}), 0.0001f);
        assertEquals(1.1f, NumberUtils.min(new float[]{5.5f, 3.3f, 1.1f, 2.2f, 4.4f}), 0.0001f);
        assertEquals(-5.5f, NumberUtils.min(new float[]{5.5f, -5.5f, 0.0f}), 0.0001f);
        assertTrue(Float.isNaN(NumberUtils.min(new float[]{1.0f, Float.NaN})));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinFloatArrayNull() {
        NumberUtils.min((float[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinFloatArrayEmpty() {
        NumberUtils.min(new float[]{});
    }

    //-----------------------------------------------------------------------
    @Test
    public void testMaxLongArray() {
        assertEquals(5L, NumberUtils.max(new long[]{5L}));
        assertEquals(5L, NumberUtils.max(new long[]{1L, 3L, 5L, 2L, 4L}));
        assertEquals(5L, NumberUtils.max(new long[]{-5L, 5L, 0L}));
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
        assertEquals(5, NumberUtils.max(new int[]{5}));
        assertEquals(5, NumberUtils.max(new int[]{1, 3, 5, 2, 4}));
        assertEquals(5, NumberUtils.max(new int[]{-5, 5, 0}));
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
        assertEquals((short) 5, NumberUtils.max(new short[]{5}));
        assertEquals((short) 5, NumberUtils.max(new short[]{1, 3, 5, 2, 4}));
        assertEquals((short) 5, NumberUtils.max(new short[]{-5, 5, 0}));
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
        assertEquals((byte) 5, NumberUtils.max(new byte[]{5}));
        assertEquals((byte) 5, NumberUtils.max(new byte[]{1, 3, 5, 2, 4}));
        assertEquals((byte) 5, NumberUtils.max(new byte[]{-5, 5, 0}));
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
        assertEquals(5.5d, NumberUtils.max(new double[]{5.5d}), 0.0001d);
        assertEquals(5.5d, NumberUtils.max(new double[]{1.1d, 3.3d, 5.5d, 2.2d, 4.4d}), 0.0001d);
        assertEquals(5.5d, NumberUtils.max(new double[]{-5.5d, 5.5d, 0.0d}), 0.0001d);
        assertTrue(Double.isNaN(NumberUtils.max(new double[]{1.0d, Double.NaN})));
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
        assertEquals(5.5f, NumberUtils.max(new float[]{5.5f}), 0.0001f);
        assertEquals(5.5f, NumberUtils.max(new float[]{1.1f, 3.3f, 5.5f, 2.2f, 4.4f}), 0.0001f);
        assertEquals(5.5f, NumberUtils.max(new float[]{-5.5f, 5.5f, 0.0f}), 0.0001f);
        assertTrue(Float.isNaN(NumberUtils.max(new float[]{1.0f, Float.NaN})));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxFloatArrayNull() {
        NumberUtils.max((float[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxFloatArrayEmpty() {
        NumberUtils.max(new float[]{});
    }

    //-----------------------------------------------------------------------
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
        assertEquals(1.1d, NumberUtils.min(1.1d, 2.2d, 3.3d), 0.0001d);
        assertEquals(1.1d, NumberUtils.min(2.2d, 1.1d, 3.3d), 0.0001d);
        assertEquals(1.1d, NumberUtils.min(3.3d, 2.2d, 1.1d), 0.0001d);
        assertTrue(Double.isNaN(NumberUtils.min(Double.NaN, 1.1d, 2.2d)));
        assertTrue(Double.isNaN(NumberUtils.min(1.1d, Double.NaN, 2.2d)));
        assertTrue(Double.isNaN(NumberUtils.min(1.1d, 2.2d, Double.NaN)));
    }

    @Test
    public void testMinFloat() {
        assertEquals(1.1f, NumberUtils.min(1.1f, 2.2f, 3.3f), 0.0001f);
        assertEquals(1.1f, NumberUtils.min(2.2f, 1.1f, 3.3f), 0.0001f);
        assertEquals(1.1f, NumberUtils.min(3.3f, 2.2f, 1.1f), 0.0001f);
        assertTrue(Float.isNaN(NumberUtils.min(Float.NaN, 1.1f, 2.2f)));
        assertTrue(Float.isNaN(NumberUtils.min(1.1f, Float.NaN, 2.2f)));
        assertTrue(Float.isNaN(NumberUtils.min(1.1f, 2.2f, Float.NaN)));
    }

    //-----------------------------------------------------------------------
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
        assertEquals(3.3d, NumberUtils.max(1.1d, 2.2d, 3.3d), 0.0001d);
        assertEquals(3.3d, NumberUtils.max(2.2d, 3.3d, 1.1d), 0.0001d);
        assertEquals(3.3d, NumberUtils.max(3.3d, 2.2d, 1.1d), 0.0001d);
        assertTrue(Double.isNaN(NumberUtils.max(Double.NaN, 1.1d, 2.2d)));
        assertTrue(Double.isNaN(NumberUtils.max(1.1d, Double.NaN, 2.2d)));
        assertTrue(Double.isNaN(NumberUtils.max(1.1d, 2.2d, Double.NaN)));
    }

    @Test
    public void testMaxFloat() {
        assertEquals(3.3f, NumberUtils.max(1.1f, 2.2f, 3.3f), 0.0001f);
        assertEquals(3.3f, NumberUtils.max(2.2f, 3.3f, 1.1f), 0.0001f);
        assertEquals(3.3f, NumberUtils.max(3.3f, 2.2f, 1.1f), 0.0001f);
        assertTrue(Float.isNaN(NumberUtils.max(Float.NaN, 1.1f, 2.2f)));
        assertTrue(Float.isNaN(NumberUtils.max(1.1f, Float.NaN, 2.2f)));
        assertTrue(Float.isNaN(NumberUtils.max(1.1f, 2.2f, Float.NaN)));
    }

    //-----------------------------------------------------------------------
    @Test
    public void testIsDigits() {
        assertFalse(NumberUtils.isDigits(null));
        assertFalse(NumberUtils.isDigits(""));
        assertFalse(NumberUtils.isDigits("  "));
        assertTrue(NumberUtils.isDigits("12345"));
        assertFalse(NumberUtils.isDigits("12345.6"));
        assertFalse(NumberUtils.isDigits("-12345"));
        assertFalse(NumberUtils.isDigits("12345a"));
    }

    @Test
    public void testIsNumber() {
        assertFalse(NumberUtils.isNumber(null));
        assertFalse(NumberUtils.isNumber(""));
        assertFalse(NumberUtils.isNumber("   "));
        assertFalse(NumberUtils.isNumber("."));
        assertFalse(NumberUtils.isNumber("-"));
        assertFalse(NumberUtils.isNumber("--1"));
        assertFalse(NumberUtils.isNumber("1.2.3"));
        assertFalse(NumberUtils.isNumber("1eE2"));
        assertFalse(NumberUtils.isNumber("1e+2+3"));
        assertFalse(NumberUtils.isNumber("1e"));
        assertFalse(NumberUtils.isNumber("1e+"));
        assertFalse(NumberUtils.isNumber("1e-"));
        assertFalse(NumberUtils.isNumber("0x"));
        assertFalse(NumberUtils.isNumber("-0x"));
        assertFalse(NumberUtils.isNumber("0xz"));

        assertTrue(NumberUtils.isNumber("123"));
        assertTrue(NumberUtils.isNumber("-123"));
        assertTrue(NumberUtils.isNumber("+123"));
        assertTrue(NumberUtils.isNumber("123.45"));
        assertTrue(NumberUtils.isNumber("-123.45"));
        assertTrue(NumberUtils.isNumber(".45"));
        assertTrue(NumberUtils.isNumber("-.45"));
        assertTrue(NumberUtils.isNumber("1234L"));
        assertTrue(NumberUtils.isNumber("1234l"));
        assertTrue(NumberUtils.isNumber("1234f"));
        assertTrue(NumberUtils.isNumber("1234F"));
        assertTrue(NumberUtils.isNumber("1234d"));
        assertTrue(NumberUtils.isNumber("1234D"));
        assertTrue(NumberUtils.isNumber("1.2e3"));
        assertTrue(NumberUtils.isNumber("1.2e-3"));
        assertTrue(NumberUtils.isNumber("1.2e+3"));
        assertTrue(NumberUtils.isNumber("1.2e3f"));
        assertTrue(NumberUtils.isNumber("1.2e3d"));
        assertTrue(NumberUtils.isNumber("0x1234"));
        assertTrue(NumberUtils.isNumber("-0x1234"));
        assertTrue(NumberUtils.isNumber("+0x1234"));
        assertTrue(NumberUtils.isNumber("0xabcdef"));
        assertTrue(NumberUtils.isNumber("0xABCDEF"));
        assertTrue(NumberUtils.isNumber("0XABCDEF"));
    }
}