package org.apache.commons.lang3.math;

import static org.junit.Assert.*;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Comprehensive JUnit 4 test suite for NumberUtils.
 */
public class NumberUtilsTest {

    // ========== createNumber ==========

    @Test
    public void testCreateNumberInteger() {
        assertEquals(Integer.valueOf(42), NumberUtils.createNumber("42"));
        assertEquals(Integer.valueOf(-42), NumberUtils.createNumber("-42"));
        assertEquals(Integer.valueOf(0), NumberUtils.createNumber("0"));
    }

    @Test
    public void testCreateNumberLong() {
        assertEquals(Long.valueOf(Long.MAX_VALUE), NumberUtils.createNumber(String.valueOf(Long.MAX_VALUE)));
        assertEquals(Long.valueOf(Long.MIN_VALUE), NumberUtils.createNumber(String.valueOf(Long.MIN_VALUE)));
        assertEquals(Long.valueOf(0L), NumberUtils.createNumber("0L"));
    }

    @Test
    public void testCreateNumberFloat() {
        assertEquals(Float.valueOf(1.23f), NumberUtils.createNumber("1.23"));
        assertEquals(Float.valueOf(-1.23f), NumberUtils.createNumber("-1.23"));
        assertEquals(Float.valueOf(1e10f), NumberUtils.createNumber("1e10"));
    }

    @Test
    public void testCreateNumberDouble() {
        assertEquals(Double.valueOf(1.23d), NumberUtils.createNumber("1.23d"));
        assertEquals(Double.valueOf(-1.23d), NumberUtils.createNumber("-1.23d"));
        assertEquals(Double.valueOf(1e10d), NumberUtils.createNumber("1e10d"));
    }

    @Test
    public void testCreateNumberBigInteger() {
        assertEquals(new BigInteger("12345678901234567890"), NumberUtils.createNumber("12345678901234567890"));
        assertEquals(new BigInteger("-12345678901234567890"), NumberUtils.createNumber("-12345678901234567890"));
    }

    @Test
    public void testCreateNumberBigDecimal() {
        assertEquals(new BigDecimal("1.2345678901234567890"), NumberUtils.createNumber("1.2345678901234567890"));
        assertEquals(new BigDecimal("-1.2345678901234567890"), NumberUtils.createNumber("-1.2345678901234567890"));
    }

    @Test
    public void testCreateNumberHex() {
        // Bug ID 16: "0Xfade" must be valid
        assertEquals(Integer.valueOf(0xfade), NumberUtils.createNumber("0Xfade"));
        assertEquals(Integer.valueOf(0xabcdef), NumberUtils.createNumber("0Xabcdef"));
        assertEquals(Integer.valueOf(0xABCDEF), NumberUtils.createNumber("0xABCDEF"));
        assertEquals(Long.valueOf(0x1234567890L), NumberUtils.createNumber("0x1234567890"));
        assertEquals(Integer.valueOf(0), NumberUtils.createNumber("0x0"));
        assertEquals(Long.valueOf(0xFFFFFFFFL), NumberUtils.createNumber("0xFFFFFFFF"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberInvalidEmpty() {
        NumberUtils.createNumber("");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberInvalidNull() {
        NumberUtils.createNumber(null);
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberInvalidHexPrefixOnly() {
        NumberUtils.createNumber("0x");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberInvalidChars() {
        NumberUtils.createNumber("12zz");
    }

    // ========== createInteger ==========

    @Test
    public void testCreateInteger() {
        assertEquals(Integer.valueOf(0), NumberUtils.createInteger("0"));
        assertEquals(Integer.valueOf(127), NumberUtils.createInteger("127"));
        assertEquals(Integer.valueOf(-128), NumberUtils.createInteger("-128"));
        assertNull(NumberUtils.createInteger(null));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateIntegerInvalid() {
        NumberUtils.createInteger("notanumber");
    }

    // ========== createLong ==========

    @Test
    public void testCreateLong() {
        assertEquals(Long.valueOf(0L), NumberUtils.createLong("0"));
        assertEquals(Long.valueOf(Long.MAX_VALUE), NumberUtils.createLong(String.valueOf(Long.MAX_VALUE)));
        assertEquals(Long.valueOf(Long.MIN_VALUE), NumberUtils.createLong(String.valueOf(Long.MIN_VALUE)));
        assertNull(NumberUtils.createLong(null));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateLongInvalid() {
        NumberUtils.createLong("1L"); // L suffix not supported by Long.parseLong
    }

    // ========== createBigInteger ==========

    @Test
    public void testCreateBigInteger() {
        assertEquals(BigInteger.ZERO, NumberUtils.createBigInteger("0"));
        assertEquals(new BigInteger("999999999999999999999999999999"), NumberUtils.createBigInteger("999999999999999999999999999999"));
        assertNull(NumberUtils.createBigInteger(null));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateBigIntegerInvalid() {
        NumberUtils.createBigInteger("abc");
    }

    // ========== createFloat ==========

    @Test
    public void testCreateFloat() {
        assertEquals(Float.valueOf(0.0f), NumberUtils.createFloat("0.0"));
        assertEquals(Float.valueOf(Float.MAX_VALUE), NumberUtils.createFloat(String.valueOf(Float.MAX_VALUE)));
        assertEquals(Float.valueOf(Float.MIN_VALUE), NumberUtils.createFloat(String.valueOf(Float.MIN_VALUE)));
        assertNull(NumberUtils.createFloat(null));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateFloatInvalid() {
        NumberUtils.createFloat("notafloat");
    }

    // ========== createDouble ==========

    @Test
    public void testCreateDouble() {
        assertEquals(Double.valueOf(0.0d), NumberUtils.createDouble("0.0"));
        assertEquals(Double.valueOf(Double.MAX_VALUE), NumberUtils.createDouble(String.valueOf(Double.MAX_VALUE)));
        assertEquals(Double.valueOf(Double.MIN_VALUE), NumberUtils.createDouble(String.valueOf(Double.MIN_VALUE)));
        assertNull(NumberUtils.createDouble(null));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateDoubleInvalid() {
        NumberUtils.createDouble("notadouble");
    }

    // ========== createBigDecimal ==========

    @Test
    public void testCreateBigDecimal() {
        assertEquals(BigDecimal.ZERO, NumberUtils.createBigDecimal("0"));
        assertEquals(new BigDecimal("1.23E-10"), NumberUtils.createBigDecimal("1.23E-10"));
        assertNull(NumberUtils.createBigDecimal(null));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateBigDecimalInvalid() {
        NumberUtils.createBigDecimal("NaN");
    }

    // ========== isNumber ==========

    @Test
    public void testIsNumberTrue() {
        assertTrue(NumberUtils.isNumber("123"));
        assertTrue(NumberUtils.isNumber("-123"));
        assertTrue(NumberUtils.isNumber("1.23"));
        assertTrue(NumberUtils.isNumber("0xAB"));
        assertTrue(NumberUtils.isNumber("0Xab"));
        assertTrue(NumberUtils.isNumber("0x1234567890abcdef"));
        assertTrue(NumberUtils.isNumber("123L"));
        assertTrue(NumberUtils.isNumber("1.23d"));
        assertTrue(NumberUtils.isNumber("1e10"));
        assertTrue(NumberUtils.isNumber("0.0"));
    }

    @Test
    public void testIsNumberFalse() {
        assertFalse(NumberUtils.isNumber(""));
        assertFalse(NumberUtils.isNumber(null));
        assertFalse(NumberUtils.isNumber("0x"));
        assertFalse(NumberUtils.isNumber("0X"));
        assertFalse(NumberUtils.isNumber("12L3"));
        assertFalse(NumberUtils.isNumber("+-123"));
        assertFalse(NumberUtils.isNumber("12.34.56"));
        assertFalse(NumberUtils.isNumber("abc"));
    }

    // ========== isDigits ==========

    @Test
    public void testIsDigitsTrue() {
        assertTrue(NumberUtils.isDigits("0"));
        assertTrue(NumberUtils.isDigits("123"));
        assertTrue(NumberUtils.isDigits("000"));
    }

    @Test
    public void testIsDigitsFalse() {
        assertFalse(NumberUtils.isDigits(""));
        assertFalse(NumberUtils.isDigits(null));
        assertFalse(NumberUtils.isDigits("12.3"));
        assertFalse(NumberUtils.isDigits("12a"));
        assertFalse(NumberUtils.isDigits("-12"));
    }

    // ========== max/min (int, long, float, double, short, byte) ==========

    @Test
    public void testMaxInt() {
        assertEquals(5, NumberUtils.max(new int[]{1, 2, 3, 5, 0}));
        assertEquals(Integer.MAX_VALUE, NumberUtils.max(new int[]{Integer.MAX_VALUE, 0, -1}));
    }

    @Test
    public void testMinInt() {
        assertEquals(-3, NumberUtils.min(new int[]{1, 2, -3, 5, 0}));
        assertEquals(Integer.MIN_VALUE, NumberUtils.min(new int[]{Integer.MIN_VALUE, 0, 1}));
    }

    @Test
    public void testMaxLong() {
        assertEquals(10L, NumberUtils.max(new long[]{1L, 10L, 5L}));
        assertEquals(Long.MAX_VALUE, NumberUtils.max(new long[]{Long.MAX_VALUE, 0L, -1L}));
    }

    @Test
    public void testMinLong() {
        assertEquals(-5L, NumberUtils.min(new long[]{1L, 10L, -5L}));
        assertEquals(Long.MIN_VALUE, NumberUtils.min(new long[]{Long.MIN_VALUE, 0L, 1L}));
    }

    @Test
    public void testMaxFloat() {
        assertEquals(3.14f, NumberUtils.max(1.0f, 3.14f, 0.0f), 1e-10f);
        assertEquals(Float.NaN, NumberUtils.max(Float.NaN, 0.0f, 1.0f), 0.0f);
    }

    @Test
    public void testMinFloat() {
        assertEquals(-1.0f, NumberUtils.min(-1.0f, 0.0f, 2.0f), 1e-10f);
        assertEquals(Float.NaN, NumberUtils.min(Float.NaN, 0.0f, 1.0f), 0.0f);
    }

    @Test
    public void testMaxDouble() {
        assertEquals(3.14, NumberUtils.max(1.0, 3.14, 0.0), 1e-10);
        assertEquals(Double.NaN, NumberUtils.max(Double.NaN, 0.0, 1.0), 0.0);
    }

    @Test
    public void testMinDouble() {
        assertEquals(-1.0, NumberUtils.min(-1.0, 0.0, 2.0), 1e-10);
        assertEquals(Double.NaN, NumberUtils.min(Double.NaN, 0.0, 1.0), 0.0);
    }

    @Test
    public void testMaxShort() {
        assertEquals((short) 10, NumberUtils.max((short) 1, (short) 10, (short) 5));
        assertEquals(Short.MAX_VALUE, NumberUtils.max(Short.MAX_VALUE, (short) 0, (short) -1));
    }

    @Test
    public void testMinShort() {
        assertEquals((short) -5, NumberUtils.min((short) 1, (short) 10, (short) -5));
        assertEquals(Short.MIN_VALUE, NumberUtils.min(Short.MIN_VALUE, (short) 0, (short) 1));
    }

    @Test
    public void testMaxByte() {
        assertEquals((byte) 10, NumberUtils.max((byte) 1, (byte) 10, (byte) 5));
        assertEquals(Byte.MAX_VALUE, NumberUtils.max(Byte.MAX_VALUE, (byte) 0, (byte) -1));
    }

    @Test
    public void testMinByte() {
        assertEquals((byte) -5, NumberUtils.min((byte) 1, (byte) 10, (byte) -5));
        assertEquals(Byte.MIN_VALUE, NumberUtils.min(Byte.MIN_VALUE, (byte) 0, (byte) 1));
    }

    // ========== compare (long, int, short, byte) ==========

    @Test
    public void testCompareLong() {
        assertTrue(NumberUtils.compare(1L, 2L) < 0);
        assertTrue(NumberUtils.compare(2L, 1L) > 0);
        assertEquals(0, NumberUtils.compare(1L, 1L));
    }

    @Test
    public void testCompareInt() {
        assertTrue(NumberUtils.compare(1, 2) < 0);
        assertTrue(NumberUtils.compare(2, 1) > 0);
        assertEquals(0, NumberUtils.compare(1, 1));
    }

    @Test
    public void testCompareShort() {
        assertTrue(NumberUtils.compare((short) 1, (short) 2) < 0);
        assertTrue(NumberUtils.compare((short) 2, (short) 1) > 0);
        assertEquals(0, NumberUtils.compare((short) 1, (short) 1));
    }

    @Test
    public void testCompareByte() {
        assertTrue(NumberUtils.compare((byte) 1, (byte) 2) < 0);
        assertTrue(NumberUtils.compare((byte) 2, (byte) 1) > 0);
        assertEquals(0, NumberUtils.compare((byte) 1, (byte) 1));
    }

    // ========== toXXX conversion methods ==========

    @Test
    public void testToInt() {
        assertEquals(123, NumberUtils.toInt("123"));
        assertEquals(0, NumberUtils.toInt("abc"));
        assertEquals(0, NumberUtils.toInt(null));
        assertEquals(456, NumberUtils.toInt("abc", 456));
    }

    @Test
    public void testToLong() {
        assertEquals(123L, NumberUtils.toLong("123"));
        assertEquals(0L, NumberUtils.toLong("abc"));
        assertEquals(0L, NumberUtils.toLong(null));
        assertEquals(456L, NumberUtils.toLong("abc", 456L));
    }

    @Test
    public void testToFloat() {
        assertEquals(1.23f, NumberUtils.toFloat("1.23"), 1e-10);
        assertEquals(0.0f, NumberUtils.toFloat("abc"), 0.0);
        assertEquals(0.0f, NumberUtils.toFloat(null), 0.0);
        assertEquals(4.56f, NumberUtils.toFloat("abc", 4.56f), 1e-10);
    }

    @Test
    public void testToDouble() {
        assertEquals(1.23, NumberUtils.toDouble("1.23"), 1e-10);
        assertEquals(0.0, NumberUtils.toDouble("abc"), 0.0);
        assertEquals(0.0, NumberUtils.toDouble(null), 0.0);
        assertEquals(4.56, NumberUtils.toDouble("abc", 4.56), 1e-10);
    }

    @Test
    public void testToByte() {
        assertEquals((byte) 127, NumberUtils.toByte("127"));
        assertEquals((byte) 0, NumberUtils.toByte("abc"));
        assertEquals((byte) 0, NumberUtils.toByte(null));
        assertEquals((byte) -128, NumberUtils.toByte("abc", (byte) -128));
    }

    @Test
    public void testToShort() {
        assertEquals((short) 32767, NumberUtils.toShort("32767"));
        assertEquals((short) 0, NumberUtils.toShort("abc"));
        assertEquals((short) 0, NumberUtils.toShort(null));
        assertEquals((short) -32768, NumberUtils.toShort("abc", (short) -32768));
    }

    // ========== Additional edge cases for createNumber ==========

    @Test
    public void testCreateNumberNegativeHex() {
        assertEquals(Integer.valueOf(-0xFF), NumberUtils.createNumber("-0xFF"));
        assertEquals(Integer.valueOf(-0xff), NumberUtils.createNumber("-0xff"));
    }

    @Test
    public void testCreateNumberHexWithSuffix() {
        // Hex with L suffix should be valid
        assertEquals(Long.valueOf(0xABCDEFL), NumberUtils.createNumber("0XABCDEFL"));
        assertEquals(Long.valueOf(0xabcdefL), NumberUtils.createNumber("0xabcdefL"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberHexWithInvalidSuffix() {
        NumberUtils.createNumber("0x1FgL"); // g not hex
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberMultipleDecimalPoints() {
        NumberUtils.createNumber("1.2.3");
    }
}