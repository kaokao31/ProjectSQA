package org.apache.commons.lang.math;

import java.math.BigDecimal;
import java.math.BigInteger;
import org.junit.Test;
import static org.junit.Assert.*;

public class NumberUtilsTest {

    @Test
    public void testConstructor() {
        assertNotNull(new NumberUtils());
    }

    @Test
    public void testToInt() {
        assertEquals(0, NumberUtils.toInt(null));
        assertEquals(0, NumberUtils.toInt(""));
        assertEquals(0, NumberUtils.toInt("   "));
        assertEquals(123, NumberUtils.toInt("123"));
        assertEquals(0, NumberUtils.toInt("abc"));
        assertEquals(5, NumberUtils.toInt("abc", 5));
        assertEquals(10, NumberUtils.toInt("10", 5));
        assertEquals(0, NumberUtils.stringToInt(null));
        assertEquals(123, NumberUtils.stringToInt("123"));
        assertEquals(5, NumberUtils.stringToInt("abc", 5));
    }

    @Test
    public void testToLong() {
        assertEquals(0L, NumberUtils.toLong(null));
        assertEquals(0L, NumberUtils.toLong(""));
        assertEquals(123L, NumberUtils.toLong("123"));
        assertEquals(0L, NumberUtils.toLong("abc"));
        assertEquals(5L, NumberUtils.toLong("abc", 5L));
        assertEquals(10L, NumberUtils.toLong("10", 5L));
    }

    @Test
    public void testToFloat() {
        assertEquals(0.0f, NumberUtils.toFloat(null), 0.0001f);
        assertEquals(0.0f, NumberUtils.toFloat(""), 0.0001f);
        assertEquals(1.23f, NumberUtils.toFloat("1.23"), 0.0001f);
        assertEquals(0.0f, NumberUtils.toFloat("abc"), 0.0001f);
        assertEquals(5.5f, NumberUtils.toFloat("abc", 5.5f), 0.0001f);
        assertEquals(1.0f, NumberUtils.toFloat("1.0", 5.5f), 0.0001f);
    }

    @Test
    public void testToDouble() {
        assertEquals(0.0d, NumberUtils.toDouble(null), 0.0001d);
        assertEquals(0.0d, NumberUtils.toDouble(""), 0.0001d);
        assertEquals(1.23d, NumberUtils.toDouble("1.23"), 0.0001d);
        assertEquals(0.0d, NumberUtils.toDouble("abc"), 0.0001d);
        assertEquals(5.5d, NumberUtils.toDouble("abc", 5.5d), 0.0001d);
        assertEquals(1.0d, NumberUtils.toDouble("1.0", 5.5d), 0.0001d);
    }

    @Test
    public void testToByte() {
        assertEquals((byte) 0, NumberUtils.toByte(null));
        assertEquals((byte) 0, NumberUtils.toByte(""));
        assertEquals((byte) 123, NumberUtils.toByte("123"));
        assertEquals((byte) 0, NumberUtils.toByte("abc"));
        assertEquals((byte) 5, NumberUtils.toByte("abc", (byte) 5));
        assertEquals((byte) 10, NumberUtils.toByte("10", (byte) 5));
    }

    @Test
    public void testToShort() {
        assertEquals((short) 0, NumberUtils.toShort(null));
        assertEquals((short) 0, NumberUtils.toShort(""));
        assertEquals((short) 123, NumberUtils.toShort("123"));
        assertEquals((short) 0, NumberUtils.toShort("abc"));
        assertEquals((short) 5, NumberUtils.toShort("abc", (short) 5));
        assertEquals((short) 10, NumberUtils.toShort("10", (short) 5));
    }

    @Test
    public void testCreateNumber() {
        assertNull(NumberUtils.createNumber(null));
        assertEquals(Integer.valueOf(123), NumberUtils.createNumber("123"));
        assertEquals(Long.valueOf(1234567890123L), NumberUtils.createNumber("1234567890123"));
        assertEquals(new BigInteger("1234567890123456789012"), NumberUtils.createNumber("1234567890123456789012"));
        assertEquals(Float.valueOf(1.23f), NumberUtils.createNumber("1.23f"));
        assertEquals(Float.valueOf(1.23F), NumberUtils.createNumber("1.23F"));
        assertEquals(Double.valueOf(1.23d), NumberUtils.createNumber("1.23d"));
        assertEquals(Double.valueOf(1.23D), NumberUtils.createNumber("1.23D"));
        assertEquals(Double.valueOf(1.23), NumberUtils.createNumber("1.23"));
        assertEquals(Long.valueOf(123), NumberUtils.createNumber("123L"));
        assertEquals(Long.valueOf(123), NumberUtils.createNumber("123l"));
        assertEquals(Long.valueOf(-123), NumberUtils.createNumber("-123L"));
        assertEquals(Long.valueOf(-123), NumberUtils.createNumber("-123l"));

        // Hex numbers
        assertEquals(Integer.valueOf(0x12), NumberUtils.createNumber("0x12"));
        assertEquals(Integer.valueOf(0x12), NumberUtils.createNumber("0X12"));
        assertEquals(Integer.valueOf(0x12), NumberUtils.createNumber("#12"));
        assertEquals(Integer.valueOf(-0x12), NumberUtils.createNumber("-0x12"));
        assertEquals(Integer.valueOf(-0x12), NumberUtils.createNumber("-0X12"));
        assertEquals(Integer.valueOf(-0x12), NumberUtils.createNumber("-#12"));

        // Exponent notations
        assertEquals(Double.valueOf(1.23e2), NumberUtils.createNumber("1.23e2"));
        assertEquals(Double.valueOf(1.23E2), NumberUtils.createNumber("1.23E2"));
        assertEquals(Float.valueOf(1.23e2f), NumberUtils.createNumber("1.23e2f"));
        assertEquals(Double.valueOf(1.23e2d), NumberUtils.createNumber("1.23e2d"));
        assertEquals(Double.valueOf(1.23e-2), NumberUtils.createNumber("1.23e-2"));

        // BigInteger and BigDecimal
        assertEquals(new BigDecimal("1.23e200"), NumberUtils.createNumber("1.23e200"));
        assertEquals(new BigDecimal("1.23456789012345678901234567890"), NumberUtils.createNumber("1.23456789012345678901234567890"));
    }

    @Test
    public void testLang300() {
        Number n1 = NumberUtils.createNumber("1l");
        assertEquals(Long.valueOf(1L), n1);

        Number n2 = NumberUtils.createNumber("1L");
        assertEquals(Long.valueOf(1L), n2);

        Number n3 = NumberUtils.createNumber("-1l");
        assertEquals(Long.valueOf(-1L), n3);

        Number n4 = NumberUtils.createNumber("-1L");
        assertEquals(Long.valueOf(-1L), n4);
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
    public void testCreateNumberLeadingHyphenOnly() {
        NumberUtils.createNumber("-");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberLeadingOxOnly() {
        NumberUtils.createNumber("0x");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberLeadingMinusOxOnly() {
        NumberUtils.createNumber("-0x");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberInvalidExponent() {
        NumberUtils.createNumber("12e");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberInvalidMantissa() {
        NumberUtils.createNumber(".e9");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberInvalidNumber() {
        NumberUtils.createNumber("123a");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberInvalidHex() {
        NumberUtils.createNumber("0xG12");
    }

    @Test
    public void testCreateFloat() {
        assertNull(NumberUtils.createFloat(null));
        assertEquals(Float.valueOf(1.23f), NumberUtils.createFloat("1.23"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateFloatInvalid() {
        NumberUtils.createFloat("abc");
    }

    @Test
    public void testCreateDouble() {
        assertNull(NumberUtils.createDouble(null));
        assertEquals(Double.valueOf(1.23d), NumberUtils.createDouble("1.23"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateDoubleInvalid() {
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
    public void testCreateIntegerInvalid() {
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
    public void testCreateLongInvalid() {
        NumberUtils.createLong("abc");
    }

    @Test
    public void testCreateBigInteger() {
        assertNull(NumberUtils.createBigInteger(null));
        assertEquals(new BigInteger("1234567890123456789012"), NumberUtils.createBigInteger("1234567890123456789012"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateBigIntegerInvalid() {
        NumberUtils.createBigInteger("abc");
    }

    @Test
    public void testCreateBigDecimal() {
        assertNull(NumberUtils.createBigDecimal(null));
        assertEquals(new BigDecimal("1234567890.1234567890"), NumberUtils.createBigDecimal("1234567890.1234567890"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateBigDecimalInvalid() {
        NumberUtils.createBigDecimal("abc");
    }

    @Test
    public void testMinMaxLongArray() {
        long[] array = new long[] { 5L, 3L, 9L, -2L, 7L };
        assertEquals(-2L, NumberUtils.min(array));
        assertEquals(9L, NumberUtils.max(array));

        long[] single = new long[] { 42L };
        assertEquals(42L, NumberUtils.min(single));
        assertEquals(42L, NumberUtils.max(single));
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
    public void testMinMaxIntArray() {
        int[] array = new int[] { 5, 3, 9, -2, 7 };
        assertEquals(-2, NumberUtils.min(array));
        assertEquals(9, NumberUtils.max(array));

        int[] single = new int[] { 42 };
        assertEquals(42, NumberUtils.min(single));
        assertEquals(42, NumberUtils.max(single));
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
    public void testMinMaxShortArray() {
        short[] array = new short[] { (short) 5, (short) 3, (short) 9, (short) -2, (short) 7 };
        assertEquals((short) -2, NumberUtils.min(array));
        assertEquals((short) 9, NumberUtils.max(array));

        short[] single = new short[] { (short) 42 };
        assertEquals((short) 42, NumberUtils.min(single));
        assertEquals((short) 42, NumberUtils.max(single));
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
    public void testMinMaxByteArray() {
        byte[] array = new byte[] { (byte) 5, (byte) 3, (byte) 9, (byte) -2, (byte) 7 };
        assertEquals((byte) -2, NumberUtils.min(array));
        assertEquals((byte) 9, NumberUtils.max(array));

        byte[] single = new byte[] { (byte) 42 };
        assertEquals((byte) 42, NumberUtils.min(single));
        assertEquals((byte) 42, NumberUtils.max(single));
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
    public void testMinMaxDoubleArray() {
        double[] array = new double[] { 5.5, 3.3, Double.NaN, 9.9, -2.2, 7.7 };
        assertEquals(-2.2, NumberUtils.min(array), 0.0001);
        assertEquals(9.9, NumberUtils.max(array), 0.0001);

        double[] allNaN = new double[] { Double.NaN, Double.NaN };
        assertTrue(Double.isNaN(NumberUtils.min(allNaN)));
        assertTrue(Double.isNaN(NumberUtils.max(allNaN)));

        double[] single = new double[] { 42.0 };
        assertEquals(42.0, NumberUtils.min(single), 0.0001);
        assertEquals(42.0, NumberUtils.max(single), 0.0001);
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
    public void testMinMaxFloatArray() {
        float[] array = new float[] { 5.5f, 3.3f, Float.NaN, 9.9f, -2.2f, 7.7f };
        assertEquals(-2.2f, NumberUtils.min(array), 0.0001f);
        assertEquals(9.9f, NumberUtils.max(array), 0.0001f);

        float[] allNaN = new float[] { Float.NaN, Float.NaN };
        assertTrue(Float.isNaN(NumberUtils.min(allNaN)));
        assertTrue(Float.isNaN(NumberUtils.max(allNaN)));

        float[] single = new float[] { 42.0f };
        assertEquals(42.0f, NumberUtils.min(single), 0.0001f);
        assertEquals(42.0f, NumberUtils.max(single), 0.0001f);
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

    @Test
    public void testMinThreeValues() {
        assertEquals(1L, NumberUtils.min(1L, 2L, 3L));
        assertEquals(1L, NumberUtils.min(3L, 1L, 2L));
        assertEquals(1L, NumberUtils.min(3L, 2L, 1L));

        assertEquals(1, NumberUtils.min(1, 2, 3));
        assertEquals(1, NumberUtils.min(3, 1, 2));
        assertEquals(1, NumberUtils.min(3, 2, 1));

        assertEquals((short) 1, NumberUtils.min((short) 1, (short) 2, (short) 3));
        assertEquals((short) 1, NumberUtils.min((short) 3, (short) 1, (short) 2));
        assertEquals((short) 1, NumberUtils.min((short) 3, (short) 2, (short) 1));

        assertEquals((byte) 1, NumberUtils.min((byte) 1, (byte) 2, (byte) 3));
        assertEquals((byte) 1, NumberUtils.min((byte) 3, (byte) 1, (byte) 2));
        assertEquals((byte) 1, NumberUtils.min((byte) 3, (byte) 2, (byte) 1));

        assertEquals(1.0d, NumberUtils.min(1.0d, 2.0d, 3.0d), 0.0001d);
        assertEquals(1.0d, NumberUtils.min(3.0d, 1.0d, 2.0d), 0.0001d);
        assertEquals(1.0d, NumberUtils.min(3.0d, 2.0d, 1.0d), 0.0001d);
        assertEquals(1.0d, NumberUtils.min(Double.NaN, 1.0d, 2.0d), 0.0001d);
        assertEquals(1.0d, NumberUtils.min(1.0d, Double.NaN, 2.0d), 0.0001d);
        assertEquals(1.0d, NumberUtils.min(2.0d, 1.0d, Double.NaN), 0.0001d);
        assertTrue(Double.isNaN(NumberUtils.min(Double.NaN, Double.NaN, Double.NaN)));

        assertEquals(1.0f, NumberUtils.min(1.0f, 2.0f, 3.0f), 0.0001f);
        assertEquals(1.0f, NumberUtils.min(3.0f, 1.0f, 2.0f), 0.0001f);
        assertEquals(1.0f, NumberUtils.min(3.0f, 2.0f, 1.0f), 0.0001f);
        assertEquals(1.0f, NumberUtils.min(Float.NaN, 1.0f, 2.0f), 0.0001f);
        assertEquals(1.0f, NumberUtils.min(1.0f, Float.NaN, 2.0f), 0.0001f);
        assertEquals(1.0f, NumberUtils.min(2.0f, 1.0f, Float.NaN), 0.0001f);
        assertTrue(Float.isNaN(NumberUtils.min(Float.NaN, Float.NaN, Float.NaN)));
    }

    @Test
    public void testMaxThreeValues() {
        assertEquals(3L, NumberUtils.max(1L, 2L, 3L));
        assertEquals(3L, NumberUtils.max(3L, 1L, 2L));
        assertEquals(3L, NumberUtils.max(2L, 3L, 1L));

        assertEquals(3, NumberUtils.max(1, 2, 3));
        assertEquals(3, NumberUtils.max(3, 1, 2));
        assertEquals(3, NumberUtils.max(2, 3, 1));

        assertEquals((short) 3, NumberUtils.max((short) 1, (short) 2, (short) 3));
        assertEquals((short) 3, NumberUtils.max((short) 3, (short) 1, (short) 2));
        assertEquals((short) 3, NumberUtils.max((short) 2, (short) 3, (short) 1));

        assertEquals((byte) 3, NumberUtils.max((byte) 1, (byte) 2, (byte) 3));
        assertEquals((byte) 3, NumberUtils.max((byte) 3, (byte) 1, (byte) 2));
        assertEquals((byte) 3, NumberUtils.max((byte) 2, (byte) 3, (byte) 1));

        assertEquals(3.0d, NumberUtils.max(1.0d, 2.0d, 3.0d), 0.0001d);
        assertEquals(3.0d, NumberUtils.max(3.0d, 1.0d, 2.0d), 0.0001d);
        assertEquals(3.0d, NumberUtils.max(2.0d, 3.0d, 1.0d), 0.0001d);
        assertEquals(2.0d, NumberUtils.max(Double.NaN, 1.0d, 2.0d), 0.0001d);
        assertEquals(2.0d, NumberUtils.max(1.0d, Double.NaN, 2.0d), 0.0001d);
        assertEquals(2.0d, NumberUtils.max(2.0d, 1.0d, Double.NaN), 0.0001d);
        assertTrue(Double.isNaN(NumberUtils.max(Double.NaN, Double.NaN, Double.NaN)));

        assertEquals(3.0f, NumberUtils.max(1.0f, 2.0f, 3.0f), 0.0001f);
        assertEquals(3.0f, NumberUtils.max(3.0f, 1.0f, 2.0f), 0.0001f);
        assertEquals(3.0f, NumberUtils.max(2.0f, 3.0f, 1.0f), 0.0001f);
        assertEquals(2.0f, NumberUtils.max(Float.NaN, 1.0f, 2.0f), 0.0001f);
        assertEquals(2.0f, NumberUtils.max(1.0f, Float.NaN, 2.0f), 0.0001f);
        assertEquals(2.0f, NumberUtils.max(2.0f, 1.0f, Float.NaN), 0.0001f);
        assertTrue(Float.isNaN(NumberUtils.max(Float.NaN, Float.NaN, Float.NaN)));
    }

    @Test
    public void testCompareDouble() {
        assertEquals(-1, NumberUtils.compare(-1.0d, 0.0d));
        assertEquals(0, NumberUtils.compare(0.0d, 0.0d));
        assertEquals(1, NumberUtils.compare(1.0d, 0.0d));
        assertEquals(-1, NumberUtils.compare(-0.0d, +0.0d));
        assertEquals(1, NumberUtils.compare(+0.0d, -0.0d));
        assertEquals(1, NumberUtils.compare(Double.NaN, 1.0d));
        assertEquals(-1, NumberUtils.compare(1.0d, Double.NaN));
        assertEquals(0, NumberUtils.compare(Double.NaN, Double.NaN));
    }

    @Test
    public void testCompareFloat() {
        assertEquals(-1, NumberUtils.compare(-1.0f, 0.0f));
        assertEquals(0, NumberUtils.compare(0.0f, 0.0f));
        assertEquals(1, NumberUtils.compare(1.0f, 0.0f));
        assertEquals(-1, NumberUtils.compare(-0.0f, +0.0f));
        assertEquals(1, NumberUtils.compare(+0.0f, -0.0f));
        assertEquals(1, NumberUtils.compare(Float.NaN, 1.0f));
        assertEquals(-1, NumberUtils.compare(1.0f, Float.NaN));
        assertEquals(0, NumberUtils.compare(Float.NaN, Float.NaN));
    }

    @Test
    public void testIsDigits() {
        assertFalse(NumberUtils.isDigits(null));
        assertFalse(NumberUtils.isDigits(""));
        assertTrue(NumberUtils.isDigits("12345"));
        assertFalse(NumberUtils.isDigits("123.45"));
        assertFalse(NumberUtils.isDigits("123a"));
        assertFalse(NumberUtils.isDigits("-123"));
    }

    @Test
    public void testIsNumber() {
        assertFalse(NumberUtils.isNumber(null));
        assertFalse(NumberUtils.isNumber(""));
        assertFalse(NumberUtils.isNumber("   "));
        assertFalse(NumberUtils.isNumber("-"));
        assertFalse(NumberUtils.isNumber("--1"));
        assertFalse(NumberUtils.isNumber("1.2.3"));
        assertFalse(NumberUtils.isNumber("12e"));
        assertFalse(NumberUtils.isNumber("12e+"));
        assertFalse(NumberUtils.isNumber("12e+a"));
        assertFalse(NumberUtils.isNumber(".e9"));
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
        assertTrue(NumberUtils.isNumber("1.23e4"));
        assertTrue(NumberUtils.isNumber("1.23E4"));
        assertTrue(NumberUtils.isNumber("1.23e+4"));
        assertTrue(NumberUtils.isNumber("1.23e-4"));
        assertTrue(NumberUtils.isNumber("123L"));
        assertTrue(NumberUtils.isNumber("123l"));
        assertTrue(NumberUtils.isNumber("123f"));
        assertTrue(NumberUtils.isNumber("123F"));
        assertTrue(NumberUtils.isNumber("123d"));
        assertTrue(NumberUtils.isNumber("123D"));
        assertTrue(NumberUtils.isNumber("0x123"));
        assertTrue(NumberUtils.isNumber("-0x123"));
        assertTrue(NumberUtils.isNumber("0XABC"));
        assertTrue(NumberUtils.isNumber("-0XABC"));

        assertFalse(NumberUtils.isNumber("123L2"));
        assertFalse(NumberUtils.isNumber("1.23e"));
        assertFalse(NumberUtils.isNumber("1.23e+"));
        assertFalse(NumberUtils.isNumber("1.23e-"));
        assertFalse(NumberUtils.isNumber("0x1.2"));
    }
}