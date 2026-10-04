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

/**
 * Comprehensive Unit Test Suite for {@link NumberUtils}.
 */
public class NumberUtilsTest {

    // -----------------------------------------------------------------------
    // Constructor Test
    // -----------------------------------------------------------------------
    @Test
    public void testConstructor() {
        assertNotNull(new NumberUtils());
    }

    // -----------------------------------------------------------------------
    // toInt Tests
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

    // -----------------------------------------------------------------------
    // toLong Tests
    // -----------------------------------------------------------------------
    @Test
    public void testToLongString() {
        assertEquals(0L, NumberUtils.toLong(null));
        assertEquals(0L, NumberUtils.toLong(""));
        assertEquals(0L, NumberUtils.toLong("abc"));
        assertEquals(123456789012345L, NumberUtils.toLong("123456789012345"));
        assertEquals(-123456789012345L, NumberUtils.toLong("-123456789012345"));
    }

    @Test
    public void testToLongStringL() {
        assertEquals(1L, NumberUtils.toLong(null, 1L));
        assertEquals(1L, NumberUtils.toLong("", 1L));
        assertEquals(1L, NumberUtils.toLong("abc", 1L));
        assertEquals(123456789012345L, NumberUtils.toLong("123456789012345", 1L));
    }

    // -----------------------------------------------------------------------
    // toFloat Tests
    // -----------------------------------------------------------------------
    @Test
    public void testToFloatString() {
        assertEquals(0.0f, NumberUtils.toFloat(null), 0.0001f);
        assertEquals(0.0f, NumberUtils.toFloat(""), 0.0001f);
        assertEquals(0.0f, NumberUtils.toFloat("abc"), 0.0001f);
        assertEquals(12.34f, NumberUtils.toFloat("12.34"), 0.0001f);
        assertEquals(-12.34f, NumberUtils.toFloat("-12.34"), 0.0001f);
    }

    @Test
    public void testToFloatStringF() {
        assertEquals(1.5f, NumberUtils.toFloat(null, 1.5f), 0.0001f);
        assertEquals(1.5f, NumberUtils.toFloat("", 1.5f), 0.0001f);
        assertEquals(1.5f, NumberUtils.toFloat("abc", 1.5f), 0.0001f);
        assertEquals(12.34f, NumberUtils.toFloat("12.34", 1.5f), 0.0001f);
    }

    // -----------------------------------------------------------------------
    // toDouble Tests
    // -----------------------------------------------------------------------
    @Test
    public void testToDoubleString() {
        assertEquals(0.0d, NumberUtils.toDouble(null), 0.0001d);
        assertEquals(0.0d, NumberUtils.toDouble(""), 0.0001d);
        assertEquals(0.0d, NumberUtils.toDouble("abc"), 0.0001d);
        assertEquals(12.34d, NumberUtils.toDouble("12.34"), 0.0001d);
        assertEquals(-12.34d, NumberUtils.toDouble("-12.34"), 0.0001d);
    }

    @Test
    public void testToDoubleStringD() {
        assertEquals(1.5d, NumberUtils.toDouble(null, 1.5d), 0.0001d);
        assertEquals(1.5d, NumberUtils.toDouble("", 1.5d), 0.0001d);
        assertEquals(1.5d, NumberUtils.toDouble("abc", 1.5d), 0.0001d);
        assertEquals(12.34d, NumberUtils.toDouble("12.34", 1.5d), 0.0001d);
    }

    // -----------------------------------------------------------------------
    // toByte Tests
    // -----------------------------------------------------------------------
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

    // -----------------------------------------------------------------------
    // toShort Tests
    // -----------------------------------------------------------------------
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
    // createNumber Tests
    // -----------------------------------------------------------------------
    @Test
    public void testCreateNumber() {
        assertNull(NumberUtils.createNumber(null));
        assertEquals(Integer.valueOf(123), NumberUtils.createNumber("123"));
        assertEquals(Integer.valueOf(-123), NumberUtils.createNumber("-123"));
        assertEquals(Integer.valueOf(0), NumberUtils.createNumber("0"));
        assertEquals(Long.valueOf(1234567890123L), NumberUtils.createNumber("1234567890123"));
        assertEquals(Long.valueOf(-1234567890123L), NumberUtils.createNumber("-1234567890123"));
        assertEquals(new BigInteger("123456789012345678901234567890"),
                NumberUtils.createNumber("123456789012345678901234567890"));

        assertEquals(Float.valueOf(12.34f), NumberUtils.createNumber("12.34f"));
        assertEquals(Float.valueOf(12.34F), NumberUtils.createNumber("12.34F"));
        assertEquals(Float.valueOf(-12.34f), NumberUtils.createNumber("-12.34f"));
        assertEquals(Double.valueOf(12.34d), NumberUtils.createNumber("12.34d"));
        assertEquals(Double.valueOf(12.34D), NumberUtils.createNumber("12.34D"));
        assertEquals(Float.valueOf(12.34f), NumberUtils.createNumber("12.34"));

        assertEquals(Long.valueOf(123L), NumberUtils.createNumber("123L"));
        assertEquals(Long.valueOf(123L), NumberUtils.createNumber("123l"));
        assertEquals(Long.valueOf(-123L), NumberUtils.createNumber("-123L"));

        assertEquals(Integer.valueOf(0x12), NumberUtils.createNumber("0x12"));
        assertEquals(Integer.valueOf(0x12), NumberUtils.createNumber("0X12"));
        assertEquals(Integer.valueOf(0x12), NumberUtils.createNumber("#12"));
        assertEquals(Integer.valueOf(-0x12), NumberUtils.createNumber("-0x12"));
        assertEquals(Integer.valueOf(-0x12), NumberUtils.createNumber("-0X12"));
        assertEquals(Integer.valueOf(-0x12), NumberUtils.createNumber("-#12"));

        assertEquals(new BigDecimal("123456789012345678901234567890.1234567890"),
                NumberUtils.createNumber("123456789012345678901234567890.1234567890"));

        assertEquals(Double.valueOf(1.23e4d), NumberUtils.createNumber("1.23e4d"));
        assertEquals(Double.valueOf(1.23E4d), NumberUtils.createNumber("1.23E4d"));
        assertEquals(Float.valueOf(1.23e4f), NumberUtils.createNumber("1.23e4f"));
        assertEquals(Float.valueOf(1.23E4f), NumberUtils.createNumber("1.23E4f"));
        assertEquals(Double.valueOf(1.23e4), NumberUtils.createNumber("1.23e4"));
        assertEquals(Double.valueOf(1.23E4), NumberUtils.createNumber("1.23E4"));
    }

    @Test
    public void testCreateNumberEdgeCasesAndFaultDetection() {
        // Exponent handling and boundary tests
        try {
            NumberUtils.createNumber("1eE");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException expected) {
            // expected
        } catch (StringIndexOutOfBoundsException e) {
            fail("Should not throw StringIndexOutOfBoundsException for 1eE");
        }

        try {
            NumberUtils.createNumber("1eE1");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException expected) {
            // expected
        }

        try {
            NumberUtils.createNumber("1.0eE1");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException expected) {
            // expected
        }

        try {
            NumberUtils.createNumber("1e");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException expected) {
            // expected
        }

        try {
            NumberUtils.createNumber("1.0e");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException expected) {
            // expected
        }

        try {
            NumberUtils.createNumber("1.0e-");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException expected) {
            // expected
        }

        try {
            NumberUtils.createNumber("1.0e+");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException expected) {
            // expected
        }

        try {
            NumberUtils.createNumber("1.0e-f");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException expected) {
            // expected
        }

        try {
            NumberUtils.createNumber("1.0e+f");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException expected) {
            // expected
        }
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
    public void testCreateNumberIllegalPrefix() {
        NumberUtils.createNumber("--123");
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
    public void testCreateNumberMultipleDecimals() {
        NumberUtils.createNumber("1.2.3");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberMultipleDecimalsWithSuffix() {
        NumberUtils.createNumber("1.2.3f");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberJustDecimal() {
        NumberUtils.createNumber(".");
    }

    // -----------------------------------------------------------------------
    // Individual create methods
    // -----------------------------------------------------------------------
    @Test
    public void testCreateFloat() {
        assertNull(NumberUtils.createFloat(null));
        assertEquals(Float.valueOf(12.34f), NumberUtils.createFloat("12.34"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateFloatFailure() {
        NumberUtils.createFloat("abc");
    }

    @Test
    public void testCreateDouble() {
        assertNull(NumberUtils.createDouble(null));
        assertEquals(Double.valueOf(12.34d), NumberUtils.createDouble("12.34"));
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
        assertEquals(Integer.valueOf(012), NumberUtils.createInteger("012"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateIntegerFailure() {
        NumberUtils.createInteger("abc");
    }

    @Test
    public void testCreateLong() {
        assertNull(NumberUtils.createLong(null));
        assertEquals(Long.valueOf(1234567890123L), NumberUtils.createLong("1234567890123"));
        assertEquals(Long.valueOf(0x12), NumberUtils.createLong("0x12"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateLongFailure() {
        NumberUtils.createLong("abc");
    }

    @Test
    public void testCreateBigInteger() {
        assertNull(NumberUtils.createBigInteger(null));
        assertEquals(new BigInteger("12345678901234567890"), NumberUtils.createBigInteger("12345678901234567890"));
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
    // min & max Tests (long, int, short, byte, double, float)
    // -----------------------------------------------------------------------
    @Test
    public void testMinLong() {
        assertEquals(1L, NumberUtils.min(1L, 2L, 3L));
        assertEquals(1L, NumberUtils.min(3L, 1L, 2L));
        assertEquals(1L, NumberUtils.min(3L, 2L, 1L));
        assertEquals(1L, NumberUtils.min(new long[]{3L, 2L, 1L}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinLongNullArray() {
        NumberUtils.min((long[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinLongEmptyArray() {
        NumberUtils.min(new long[0]);
    }

    @Test
    public void testMinInt() {
        assertEquals(1, NumberUtils.min(1, 2, 3));
        assertEquals(1, NumberUtils.min(3, 1, 2));
        assertEquals(1, NumberUtils.min(3, 2, 1));
        assertEquals(1, NumberUtils.min(new int[]{3, 2, 1}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinIntNullArray() {
        NumberUtils.min((int[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinIntEmptyArray() {
        NumberUtils.min(new int[0]);
    }

    @Test
    public void testMinShort() {
        assertEquals((short) 1, NumberUtils.min((short) 1, (short) 2, (short) 3));
        assertEquals((short) 1, NumberUtils.min((short) 3, (short) 1, (short) 2));
        assertEquals((short) 1, NumberUtils.min((short) 3, (short) 2, (short) 1));
        assertEquals((short) 1, NumberUtils.min(new short[]{(short) 3, (short) 2, (short) 1}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinShortNullArray() {
        NumberUtils.min((short[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinShortEmptyArray() {
        NumberUtils.min(new short[0]);
    }

    @Test
    public void testMinByte() {
        assertEquals((byte) 1, NumberUtils.min((byte) 1, (byte) 2, (byte) 3));
        assertEquals((byte) 1, NumberUtils.min((byte) 3, (byte) 1, (byte) 2));
        assertEquals((byte) 1, NumberUtils.min((byte) 3, (byte) 2, (byte) 1));
        assertEquals((byte) 1, NumberUtils.min(new byte[]{(byte) 3, (byte) 2, (byte) 1}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinByteNullArray() {
        NumberUtils.min((byte[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinByteEmptyArray() {
        NumberUtils.min(new byte[0]);
    }

    @Test
    public void testMinDouble() {
        assertEquals(1.0d, NumberUtils.min(1.0d, 2.0d, 3.0d), 0.0001d);
        assertEquals(1.0d, NumberUtils.min(3.0d, 1.0d, 2.0d), 0.0001d);
        assertEquals(1.0d, NumberUtils.min(3.0d, 2.0d, 1.0d), 0.0001d);
        assertEquals(1.0d, NumberUtils.min(Double.NaN, 1.0d, 2.0d), 0.0001d);
        assertEquals(1.0d, NumberUtils.min(new double[]{3.0d, Double.NaN, 1.0d}), 0.0001d);
        assertTrue(Double.isNaN(NumberUtils.min(Double.NaN, Double.NaN, Double.NaN)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinDoubleNullArray() {
        NumberUtils.min((double[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinDoubleEmptyArray() {
        NumberUtils.min(new double[0]);
    }

    @Test
    public void testMinFloat() {
        assertEquals(1.0f, NumberUtils.min(1.0f, 2.0f, 3.0f), 0.0001f);
        assertEquals(1.0f, NumberUtils.min(3.0f, 1.0f, 2.0f), 0.0001f);
        assertEquals(1.0f, NumberUtils.min(3.0f, 2.0f, 1.0f), 0.0001f);
        assertEquals(1.0f, NumberUtils.min(Float.NaN, 1.0f, 2.0f), 0.0001f);
        assertEquals(1.0f, NumberUtils.min(new float[]{3.0f, Float.NaN, 1.0f}), 0.0001f);
        assertTrue(Float.isNaN(NumberUtils.min(Float.NaN, Float.NaN, Float.NaN)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinFloatNullArray() {
        NumberUtils.min((float[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinFloatEmptyArray() {
        NumberUtils.min(new float[0]);
    }

    @Test
    public void testMaxLong() {
        assertEquals(3L, NumberUtils.max(1L, 2L, 3L));
        assertEquals(3L, NumberUtils.max(3L, 1L, 2L));
        assertEquals(3L, NumberUtils.max(2L, 3L, 1L));
        assertEquals(3L, NumberUtils.max(new long[]{1L, 2L, 3L}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxLongNullArray() {
        NumberUtils.max((long[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxLongEmptyArray() {
        NumberUtils.max(new long[0]);
    }

    @Test
    public void testMaxInt() {
        assertEquals(3, NumberUtils.max(1, 2, 3));
        assertEquals(3, NumberUtils.max(3, 1, 2));
        assertEquals(3, NumberUtils.max(2, 3, 1));
        assertEquals(3, NumberUtils.max(new int[]{1, 2, 3}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxIntNullArray() {
        NumberUtils.max((int[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxIntEmptyArray() {
        NumberUtils.max(new int[0]);
    }

    @Test
    public void testMaxShort() {
        assertEquals((short) 3, NumberUtils.max((short) 1, (short) 2, (short) 3));
        assertEquals((short) 3, NumberUtils.max((short) 3, (short) 1, (short) 2));
        assertEquals((short) 3, NumberUtils.max((short) 2, (short) 3, (short) 1));
        assertEquals((short) 3, NumberUtils.max(new short[]{(short) 1, (short) 2, (short) 3}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxShortNullArray() {
        NumberUtils.max((short[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxShortEmptyArray() {
        NumberUtils.max(new short[0]);
    }

    @Test
    public void testMaxByte() {
        assertEquals((byte) 3, NumberUtils.max((byte) 1, (byte) 2, (byte) 3));
        assertEquals((byte) 3, NumberUtils.max((byte) 3, (byte) 1, (byte) 2));
        assertEquals((byte) 3, NumberUtils.max((byte) 2, (byte) 3, (byte) 1));
        assertEquals((byte) 3, NumberUtils.max(new byte[]{(byte) 1, (byte) 2, (byte) 3}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxByteNullArray() {
        NumberUtils.max((byte[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxByteEmptyArray() {
        NumberUtils.max(new byte[0]);
    }

    @Test
    public void testMaxDouble() {
        assertEquals(3.0d, NumberUtils.max(1.0d, 2.0d, 3.0d), 0.0001d);
        assertEquals(3.0d, NumberUtils.max(3.0d, 1.0d, 2.0d), 0.0001d);
        assertEquals(3.0d, NumberUtils.max(2.0d, 3.0d, 1.0d), 0.0001d);
        assertEquals(2.0d, NumberUtils.max(Double.NaN, 1.0d, 2.0d), 0.0001d);
        assertEquals(3.0d, NumberUtils.max(new double[]{1.0d, Double.NaN, 3.0d}), 0.0001d);
        assertTrue(Double.isNaN(NumberUtils.max(Double.NaN, Double.NaN, Double.NaN)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxDoubleNullArray() {
        NumberUtils.max((double[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxDoubleEmptyArray() {
        NumberUtils.max(new double[0]);
    }

    @Test
    public void testMaxFloat() {
        assertEquals(3.0f, NumberUtils.max(1.0f, 2.0f, 3.0f), 0.0001f);
        assertEquals(3.0f, NumberUtils.max(3.0f, 1.0f, 2.0f), 0.0001f);
        assertEquals(3.0f, NumberUtils.max(2.0f, 3.0f, 1.0f), 0.0001f);
        assertEquals(2.0f, NumberUtils.max(Float.NaN, 1.0f, 2.0f), 0.0001f);
        assertEquals(3.0f, NumberUtils.max(new float[]{1.0f, Float.NaN, 3.0f}), 0.0001f);
        assertTrue(Float.isNaN(NumberUtils.max(Float.NaN, Float.NaN, Float.NaN)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxFloatNullArray() {
        NumberUtils.max((float[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxFloatEmptyArray() {
        NumberUtils.max(new float[0]);
    }

    // -----------------------------------------------------------------------
    // isDigits & isNumber Tests
    // -----------------------------------------------------------------------
    @Test
    public void testIsDigits() {
        assertFalse(NumberUtils.isDigits(null));
        assertFalse(NumberUtils.isDigits(""));
        assertFalse(NumberUtils.isDigits("  "));
        assertFalse(NumberUtils.isDigits("123a"));
        assertFalse(NumberUtils.isDigits("-123"));
        assertTrue(NumberUtils.isDigits("123456"));
    }

    @Test
    public void testIsNumber() {
        assertFalse(NumberUtils.isNumber(null));
        assertFalse(NumberUtils.isNumber(""));
        assertFalse(NumberUtils.isNumber("  "));
        assertFalse(NumberUtils.isNumber("abc"));
        assertFalse(NumberUtils.isNumber("--123"));
        assertFalse(NumberUtils.isNumber("1.2.3"));
        assertFalse(NumberUtils.isNumber("123a"));
        assertFalse(NumberUtils.isNumber("0x"));
        assertFalse(NumberUtils.isNumber("0xG"));
        assertFalse(NumberUtils.isNumber("0x12aG"));
        assertFalse(NumberUtils.isNumber("1e"));
        assertFalse(NumberUtils.isNumber("1e+"));
        assertFalse(NumberUtils.isNumber("1e-"));
        assertFalse(NumberUtils.isNumber("."));
        assertFalse(NumberUtils.isNumber(".."));

        assertTrue(NumberUtils.isNumber("0"));
        assertTrue(NumberUtils.isNumber("123"));
        assertTrue(NumberUtils.isNumber("-123"));
        assertTrue(NumberUtils.isNumber("+123"));
        assertTrue(NumberUtils.isNumber("12.34"));
        assertTrue(NumberUtils.isNumber("-12.34"));
        assertTrue(NumberUtils.isNumber(".34"));
        assertTrue(NumberUtils.isNumber("12."));
        assertTrue(NumberUtils.isNumber("12.34e5"));
        assertTrue(NumberUtils.isNumber("12.34E5"));
        assertTrue(NumberUtils.isNumber("12.34e+5"));
        assertTrue(NumberUtils.isNumber("12.34e-5"));
        assertTrue(NumberUtils.isNumber("1234L"));
        assertTrue(NumberUtils.isNumber("1234l"));
        assertTrue(NumberUtils.isNumber("12.34f"));
        assertTrue(NumberUtils.isNumber("12.34F"));
        assertTrue(NumberUtils.isNumber("12.34d"));
        assertTrue(NumberUtils.isNumber("12.34D"));
        assertTrue(NumberUtils.isNumber("0x1234"));
        assertTrue(NumberUtils.isNumber("-0x1234"));
        assertTrue(NumberUtils.isNumber("0Xabcdef"));
    }
}