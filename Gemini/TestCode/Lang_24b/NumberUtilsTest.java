package org.apache.commons.lang3.math;

import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.Assert.*;

public class NumberUtilsTest {

    @Test
    public void testConstructor() {
        assertNotNull(new NumberUtils());
    }

    // -----------------------------------------------------------------------
    @Test
    public void testToInt() {
        assertEquals(0, NumberUtils.toInt(null));
        assertEquals(0, NumberUtils.toInt(""));
        assertEquals(0, NumberUtils.toInt("  "));
        assertEquals(123, NumberUtils.toInt("123"));
        assertEquals(0, NumberUtils.toInt("abc"));
        assertEquals(-123, NumberUtils.toInt("-123"));

        assertEquals(5, NumberUtils.toInt(null, 5));
        assertEquals(5, NumberUtils.toInt("", 5));
        assertEquals(5, NumberUtils.toInt("  ", 5));
        assertEquals(123, NumberUtils.toInt("123", 5));
        assertEquals(5, NumberUtils.toInt("abc", 5));
        assertEquals(-123, NumberUtils.toInt("-123", 5));
    }

    @Test
    public void testToLong() {
        assertEquals(0L, NumberUtils.toLong(null));
        assertEquals(0L, NumberUtils.toLong(""));
        assertEquals(0L, NumberUtils.toLong("  "));
        assertEquals(123456789012L, NumberUtils.toLong("123456789012"));
        assertEquals(0L, NumberUtils.toLong("abc"));
        assertEquals(-123456789012L, NumberUtils.toLong("-123456789012"));

        assertEquals(5L, NumberUtils.toLong(null, 5L));
        assertEquals(5L, NumberUtils.toLong("", 5L));
        assertEquals(5L, NumberUtils.toLong("  ", 5L));
        assertEquals(123456789012L, NumberUtils.toLong("123456789012", 5L));
        assertEquals(5L, NumberUtils.toLong("abc", 5L));
        assertEquals(-123456789012L, NumberUtils.toLong("-123456789012", 5L));
    }

    @Test
    public void testToFloat() {
        assertEquals(0.0f, NumberUtils.toFloat(null), 0.0001f);
        assertEquals(0.0f, NumberUtils.toFloat(""), 0.0001f);
        assertEquals(0.0f, NumberUtils.toFloat("  "), 0.0001f);
        assertEquals(1.23f, NumberUtils.toFloat("1.23"), 0.0001f);
        assertEquals(0.0f, NumberUtils.toFloat("abc"), 0.0001f);

        assertEquals(5.5f, NumberUtils.toFloat(null, 5.5f), 0.0001f);
        assertEquals(5.5f, NumberUtils.toFloat("", 5.5f), 0.0001f);
        assertEquals(5.5f, NumberUtils.toFloat("  ", 5.5f), 0.0001f);
        assertEquals(1.23f, NumberUtils.toFloat("1.23", 5.5f), 0.0001f);
        assertEquals(5.5f, NumberUtils.toFloat("abc", 5.5f), 0.0001f);
    }

    @Test
    public void testToDouble() {
        assertEquals(0.0d, NumberUtils.toDouble(null), 0.0001d);
        assertEquals(0.0d, NumberUtils.toDouble(""), 0.0001d);
        assertEquals(0.0d, NumberUtils.toDouble("  "), 0.0001d);
        assertEquals(1.2345d, NumberUtils.toDouble("1.2345"), 0.0001d);
        assertEquals(0.0d, NumberUtils.toDouble("abc"), 0.0001d);

        assertEquals(5.55d, NumberUtils.toDouble(null, 5.55d), 0.0001d);
        assertEquals(5.55d, NumberUtils.toDouble("", 5.55d), 0.0001d);
        assertEquals(5.55d, NumberUtils.toDouble("  ", 5.55d), 0.0001d);
        assertEquals(1.2345d, NumberUtils.toDouble("1.2345", 5.55d), 0.0001d);
        assertEquals(5.55d, NumberUtils.toDouble("abc", 5.55d), 0.0001d);
    }

    @Test
    public void testToByte() {
        assertEquals((byte) 0, NumberUtils.toByte(null));
        assertEquals((byte) 0, NumberUtils.toByte(""));
        assertEquals((byte) 0, NumberUtils.toByte("  "));
        assertEquals((byte) 123, NumberUtils.toByte("123"));
        assertEquals((byte) 0, NumberUtils.toByte("abc"));

        assertEquals((byte) 5, NumberUtils.toByte(null, (byte) 5));
        assertEquals((byte) 5, NumberUtils.toByte("", (byte) 5));
        assertEquals((byte) 5, NumberUtils.toByte("  ", (byte) 5));
        assertEquals((byte) 123, NumberUtils.toByte("123", (byte) 5));
        assertEquals((byte) 5, NumberUtils.toByte("abc", (byte) 5));
    }

    @Test
    public void testToShort() {
        assertEquals((short) 0, NumberUtils.toShort(null));
        assertEquals((short) 0, NumberUtils.toShort(""));
        assertEquals((short) 0, NumberUtils.toShort("  "));
        assertEquals((short) 1234, NumberUtils.toShort("1234"));
        assertEquals((short) 0, NumberUtils.toShort("abc"));

        assertEquals((short) 5, NumberUtils.toShort(null, (short) 5));
        assertEquals((short) 5, NumberUtils.toShort("", (short) 5));
        assertEquals((short) 5, NumberUtils.toShort("  ", (short) 5));
        assertEquals((short) 1234, NumberUtils.toShort("1234", (short) 5));
        assertEquals((short) 5, NumberUtils.toShort("abc", (short) 5));
    }

    // -----------------------------------------------------------------------
    @Test
    public void testCreateNumber() {
        assertNull(NumberUtils.createNumber(null));
        assertEquals(Integer.valueOf(123), NumberUtils.createNumber("123"));
        assertEquals(Long.valueOf(12345678901L), NumberUtils.createNumber("12345678901"));
        assertEquals(new BigInteger("123456789012345678901234567890"),
                NumberUtils.createNumber("123456789012345678901234567890"));

        assertEquals(Float.valueOf(12.34f), NumberUtils.createNumber("12.34f"));
        assertEquals(Float.valueOf(12.34F), NumberUtils.createNumber("12.34F"));
        assertEquals(Double.valueOf(12.34d), NumberUtils.createNumber("12.34d"));
        assertEquals(Double.valueOf(12.34D), NumberUtils.createNumber("12.34D"));
        assertEquals(Long.valueOf(1234L), NumberUtils.createNumber("1234l"));
        assertEquals(Long.valueOf(1234L), NumberUtils.createNumber("1234L"));

        assertEquals(Double.valueOf(12.34), NumberUtils.createNumber("12.34"));
        assertEquals(Double.valueOf("1.23e-4"), NumberUtils.createNumber("1.23e-4"));
        assertEquals(Double.valueOf("1.23E+4"), NumberUtils.createNumber("1.23E+4"));
        assertEquals(Float.valueOf("1.23e-4f"), NumberUtils.createNumber("1.23e-4f"));

        assertEquals(Integer.valueOf(0x12ab), NumberUtils.createNumber("0x12ab"));
        assertEquals(Integer.valueOf(0x12ab), NumberUtils.createNumber("0X12ab"));
        assertEquals(Integer.valueOf(-0x12ab), NumberUtils.createNumber("-0x12ab"));
        assertEquals(Integer.valueOf(-0x12ab), NumberUtils.createNumber("-0X12ab"));
        assertEquals(Integer.valueOf(0x12ab), NumberUtils.createNumber("#12ab"));
        assertEquals(Integer.valueOf(-0x12ab), NumberUtils.createNumber("-#12ab"));

        // Large hex values
        assertEquals(new BigInteger("123456789abcdef0", 16), NumberUtils.createNumber("0x123456789abcdef0"));
        assertEquals(new BigInteger("-123456789abcdef0", 16), NumberUtils.createNumber("-0x123456789abcdef0"));

        // BigDecimal fallback
        assertEquals(new BigDecimal("1.1E-700"), NumberUtils.createNumber("1.1E-700"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberBlank() {
        NumberUtils.createNumber("   ");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberEmpty() {
        NumberUtils.createNumber("");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberInvalidHex() {
        NumberUtils.createNumber("0x");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberInvalidNegativeHex() {
        NumberUtils.createNumber("-0x");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberInvalidFormat() {
        NumberUtils.createNumber("12.34.56");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberInvalidExponent() {
        NumberUtils.createNumber("12e34e56");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberLeadingHyphenOnly() {
        NumberUtils.createNumber("-");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberTrailingQualifierWithoutNumber() {
        NumberUtils.createNumber("e-1f");
    }

    // -----------------------------------------------------------------------
    @Test
    public void testCreateFloat() {
        assertNull(NumberUtils.createFloat(null));
        assertEquals(Float.valueOf(1.234f), NumberUtils.createFloat("1.234"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateFloatFailure() {
        NumberUtils.createFloat("abc");
    }

    @Test
    public void testCreateDouble() {
        assertNull(NumberUtils.createDouble(null));
        assertEquals(Double.valueOf(1.234d), NumberUtils.createDouble("1.234"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateDoubleFailure() {
        NumberUtils.createDouble("abc");
    }

    @Test
    public void testCreateInteger() {
        assertNull(NumberUtils.createInteger(null));
        assertEquals(Integer.valueOf(1234), NumberUtils.createInteger("1234"));
        assertEquals(Integer.valueOf(0x12), NumberUtils.createInteger("0x12"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateIntegerFailure() {
        NumberUtils.createInteger("abc");
    }

    @Test
    public void testCreateLong() {
        assertNull(NumberUtils.createLong(null));
        assertEquals(Long.valueOf(123456789012L), NumberUtils.createLong("123456789012"));
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
        assertEquals(new BigDecimal("12345678901234567890.123456789"),
                NumberUtils.createBigDecimal("12345678901234567890.123456789"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateBigDecimalFailure() {
        NumberUtils.createBigDecimal("abc");
    }

    // -----------------------------------------------------------------------
    @Test
    public void testMinMaxLong() {
        assertEquals(5L, NumberUtils.min(5L, 6L, 7L));
        assertEquals(5L, NumberUtils.min(6L, 5L, 7L));
        assertEquals(5L, NumberUtils.min(7L, 6L, 5L));

        assertEquals(7L, NumberUtils.max(5L, 6L, 7L));
        assertEquals(7L, NumberUtils.max(6L, 7L, 5L));
        assertEquals(7L, NumberUtils.max(7L, 6L, 5L));

        long[] array = new long[]{3L, 1L, 2L};
        assertEquals(1L, NumberUtils.min(array));
        assertEquals(3L, NumberUtils.max(array));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinLongArrayNull() {
        NumberUtils.min((long[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinLongArrayEmpty() {
        NumberUtils.min(new long[0]);
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
    public void testMinMaxInt() {
        assertEquals(5, NumberUtils.min(5, 6, 7));
        assertEquals(5, NumberUtils.min(6, 5, 7));
        assertEquals(5, NumberUtils.min(7, 6, 5));

        assertEquals(7, NumberUtils.max(5, 6, 7));
        assertEquals(7, NumberUtils.max(6, 7, 5));
        assertEquals(7, NumberUtils.max(7, 6, 5));

        int[] array = new int[]{3, 1, 2};
        assertEquals(1, NumberUtils.min(array));
        assertEquals(3, NumberUtils.max(array));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinIntArrayNull() {
        NumberUtils.min((int[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinIntArrayEmpty() {
        NumberUtils.min(new int[0]);
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
    public void testMinMaxShort() {
        assertEquals((short) 5, NumberUtils.min((short) 5, (short) 6, (short) 7));
        assertEquals((short) 5, NumberUtils.min((short) 6, (short) 5, (short) 7));
        assertEquals((short) 5, NumberUtils.min((short) 7, (short) 6, (short) 5));

        assertEquals((short) 7, NumberUtils.max((short) 5, (short) 6, (short) 7));
        assertEquals((short) 7, NumberUtils.max((short) 6, (short) 7, (short) 5));
        assertEquals((short) 7, NumberUtils.max((short) 7, (short) 6, (short) 5));

        short[] array = new short[]{3, 1, 2};
        assertEquals((short) 1, NumberUtils.min(array));
        assertEquals((short) 3, NumberUtils.max(array));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinShortArrayNull() {
        NumberUtils.min((short[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinShortArrayEmpty() {
        NumberUtils.min(new short[0]);
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
    public void testMinMaxByte() {
        assertEquals((byte) 5, NumberUtils.min((byte) 5, (byte) 6, (byte) 7));
        assertEquals((byte) 5, NumberUtils.min((byte) 6, (byte) 5, (byte) 7));
        assertEquals((byte) 5, NumberUtils.min((byte) 7, (byte) 6, (byte) 5));

        assertEquals((byte) 7, NumberUtils.max((byte) 5, (byte) 6, (byte) 7));
        assertEquals((byte) 7, NumberUtils.max((byte) 6, (byte) 7, (byte) 5));
        assertEquals((byte) 7, NumberUtils.max((byte) 7, (byte) 6, (byte) 5));

        byte[] array = new byte[]{3, 1, 2};
        assertEquals((byte) 1, NumberUtils.min(array));
        assertEquals((byte) 3, NumberUtils.max(array));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinByteArrayNull() {
        NumberUtils.min((byte[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinByteArrayEmpty() {
        NumberUtils.min(new byte[0]);
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
    public void testMinMaxDouble() {
        assertEquals(5.0d, NumberUtils.min(5.0d, 6.0d, 7.0d), 0.0001d);
        assertEquals(5.0d, NumberUtils.min(6.0d, 5.0d, 7.0d), 0.0001d);
        assertEquals(5.0d, NumberUtils.min(7.0d, 6.0d, 5.0d), 0.0001d);
        assertEquals(5.0d, NumberUtils.min(Double.NaN, 6.0d, 5.0d), 0.0001d);
        assertEquals(5.0d, NumberUtils.min(5.0d, Double.NaN, 7.0d), 0.0001d);
        assertEquals(5.0d, NumberUtils.min(6.0d, 5.0d, Double.NaN), 0.0001d);

        assertEquals(7.0d, NumberUtils.max(5.0d, 6.0d, 7.0d), 0.0001d);
        assertEquals(7.0d, NumberUtils.max(6.0d, 7.0d, 5.0d), 0.0001d);
        assertEquals(7.0d, NumberUtils.max(7.0d, 6.0d, 5.0d), 0.0001d);
        assertEquals(7.0d, NumberUtils.max(Double.NaN, 6.0d, 7.0d), 0.0001d);
        assertEquals(7.0d, NumberUtils.max(5.0d, Double.NaN, 7.0d), 0.0001d);
        assertEquals(6.0d, NumberUtils.max(6.0d, 5.0d, Double.NaN), 0.0001d);

        double[] array = new double[]{3.0d, 1.0d, 2.0d, Double.NaN};
        assertEquals(1.0d, NumberUtils.min(array), 0.0001d);
        assertEquals(3.0d, NumberUtils.max(array), 0.0001d);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinDoubleArrayNull() {
        NumberUtils.min((double[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinDoubleArrayEmpty() {
        NumberUtils.min(new double[0]);
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
    public void testMinMaxFloat() {
        assertEquals(5.0f, NumberUtils.min(5.0f, 6.0f, 7.0f), 0.0001f);
        assertEquals(5.0f, NumberUtils.min(6.0f, 5.0f, 7.0f), 0.0001f);
        assertEquals(5.0f, NumberUtils.min(7.0f, 6.0f, 5.0f), 0.0001f);
        assertEquals(5.0f, NumberUtils.min(Float.NaN, 6.0f, 5.0f), 0.0001f);
        assertEquals(5.0f, NumberUtils.min(5.0f, Float.NaN, 7.0f), 0.0001f);
        assertEquals(5.0f, NumberUtils.min(6.0f, 5.0f, Float.NaN), 0.0001f);

        assertEquals(7.0f, NumberUtils.max(5.0f, 6.0f, 7.0f), 0.0001f);
        assertEquals(7.0f, NumberUtils.max(6.0f, 7.0f, 5.0f), 0.0001f);
        assertEquals(7.0f, NumberUtils.max(7.0f, 6.0f, 5.0f), 0.0001f);
        assertEquals(7.0f, NumberUtils.max(Float.NaN, 6.0f, 7.0f), 0.0001f);
        assertEquals(7.0f, NumberUtils.max(5.0f, Float.NaN, 7.0f), 0.0001f);
        assertEquals(6.0f, NumberUtils.max(6.0f, 5.0f, Float.NaN), 0.0001f);

        float[] array = new float[]{3.0f, 1.0f, 2.0f, Float.NaN};
        assertEquals(1.0f, NumberUtils.min(array), 0.0001f);
        assertEquals(3.0f, NumberUtils.max(array), 0.0001f);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinFloatArrayNull() {
        NumberUtils.min((float[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinFloatArrayEmpty() {
        NumberUtils.min(new float[0]);
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
        assertTrue(NumberUtils.isDigits("12345"));
        assertFalse(NumberUtils.isDigits("1234.5"));
        assertFalse(NumberUtils.isDigits("abc"));
        assertFalse(NumberUtils.isDigits("12a34"));
    }

    // -----------------------------------------------------------------------
    @Test
    public void testIsNumber() {
        assertFalse(NumberUtils.isNumber(null));
        assertFalse(NumberUtils.isNumber(""));
        assertFalse(NumberUtils.isNumber(" "));
        assertFalse(NumberUtils.isNumber("abc"));
        assertFalse(NumberUtils.isNumber("."));
        assertFalse(NumberUtils.isNumber("-"));
        assertFalse(NumberUtils.isNumber("+"));
        assertFalse(NumberUtils.isNumber("--1"));
        assertFalse(NumberUtils.isNumber("1.2.3"));
        assertFalse(NumberUtils.isNumber("1e2e3"));
        assertFalse(NumberUtils.isNumber("1a"));
        assertFalse(NumberUtils.isNumber("0x"));
        assertFalse(NumberUtils.isNumber("-0x"));
        assertFalse(NumberUtils.isNumber("0xgarbage"));

        assertTrue(NumberUtils.isNumber("123"));
        assertTrue(NumberUtils.isNumber("-123"));
        assertTrue(NumberUtils.isNumber("+123"));
        assertTrue(NumberUtils.isNumber("123.45"));
        assertTrue(NumberUtils.isNumber(".45"));
        assertTrue(NumberUtils.isNumber("-.45"));
        assertTrue(NumberUtils.isNumber("+.45"));
        assertTrue(NumberUtils.isNumber("123."));
        assertTrue(NumberUtils.isNumber("-123."));

        assertTrue(NumberUtils.isNumber("1e1"));
        assertTrue(NumberUtils.isNumber("1e+1"));
        assertTrue(NumberUtils.isNumber("1e-1"));
        assertTrue(NumberUtils.isNumber("1.1e1"));
        assertTrue(NumberUtils.isNumber("1.1e+1"));
        assertTrue(NumberUtils.isNumber("1.1e-1"));
        assertTrue(NumberUtils.isNumber(".1e1"));
        assertTrue(NumberUtils.isNumber("-.1e1"));

        assertFalse(NumberUtils.isNumber("1e"));
        assertFalse(NumberUtils.isNumber("1e+"));
        assertFalse(NumberUtils.isNumber("1e-"));

        assertTrue(NumberUtils.isNumber("123L"));
        assertTrue(NumberUtils.isNumber("123l"));
        assertTrue(NumberUtils.isNumber("123f"));
        assertTrue(NumberUtils.isNumber("123F"));
        assertTrue(NumberUtils.isNumber("123d"));
        assertTrue(NumberUtils.isNumber("123D"));
        assertTrue(NumberUtils.isNumber("123.45f"));
        assertTrue(NumberUtils.isNumber("123.45F"));
        assertTrue(NumberUtils.isNumber("123.45d"));
        assertTrue(NumberUtils.isNumber("123.45D"));
        assertTrue(NumberUtils.isNumber(".45f"));
        assertTrue(NumberUtils.isNumber(".45d"));
        assertTrue(NumberUtils.isNumber("123.f"));
        assertTrue(NumberUtils.isNumber("123.d"));

        // Lang-664 cases: Exponent with type qualifiers
        assertTrue(NumberUtils.isNumber("1e1f"));
        assertTrue(NumberUtils.isNumber("1e-1f"));
        assertTrue(NumberUtils.isNumber("1e+1f"));
        assertTrue(NumberUtils.isNumber("1e1F"));
        assertTrue(NumberUtils.isNumber("1e1d"));
        assertTrue(NumberUtils.isNumber("1e1D"));
        assertTrue(NumberUtils.isNumber("1.1e-1f"));
        assertTrue(NumberUtils.isNumber("1.1e+1d"));
        assertTrue(NumberUtils.isNumber("1e1L"));
        assertTrue(NumberUtils.isNumber("1e1l"));
        assertFalse(NumberUtils.isNumber("1.1L"));
        assertFalse(NumberUtils.isNumber("1.1l"));
        assertFalse(NumberUtils.isNumber("1eL"));
        assertFalse(NumberUtils.isNumber("1ef"));
        assertFalse(NumberUtils.isNumber("1e-f"));
        assertFalse(NumberUtils.isNumber("1e+d"));

        // Hexadecimal checks
        assertTrue(NumberUtils.isNumber("0x1234"));
        assertTrue(NumberUtils.isNumber("0X1234"));
        assertTrue(NumberUtils.isNumber("-0x1234"));
        assertTrue(NumberUtils.isNumber("-0X1234"));
        assertTrue(NumberUtils.isNumber("0xabcdef"));
        assertTrue(NumberUtils.isNumber("0xABCDEF"));
        assertFalse(NumberUtils.isNumber("0x1234L"));
        assertFalse(NumberUtils.isNumber("0x1234f"));
        assertFalse(NumberUtils.isNumber("0x1.2"));
    }
}