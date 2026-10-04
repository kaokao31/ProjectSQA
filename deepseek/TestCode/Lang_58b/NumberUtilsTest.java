package org.apache.commons.lang.math;

import org.junit.Test;
import static org.junit.Assert.*;

public class NumberUtilsTest {

    // ---------- createNumber ----------

    @Test
    public void testCreateNumber_Lang300() {
        // Trigger the bug: lowercase 'l' suffix should be valid
        Number num = NumberUtils.createNumber("1l");
        assertNotNull("createNumber(\"1l\") should not be null", num);
        assertTrue("Result should be a Long", num instanceof Long);
        assertEquals("Value should be 1L", 1L, num.longValue());
    }

    @Test
    public void testCreateNumber_ValidLong() {
        Number num = NumberUtils.createNumber("123L");
        assertNotNull(num);
        assertTrue(num instanceof Long);
        assertEquals(123L, num.longValue());
    }

    @Test
    public void testCreateNumber_ValidLongLowercase() {
        Number num = NumberUtils.createNumber("456l");
        assertNotNull(num);
        assertTrue(num instanceof Long);
        assertEquals(456L, num.longValue());
    }

    @Test
    public void testCreateNumber_ValidFloat() {
        Number num = NumberUtils.createNumber("3.14f");
        assertNotNull(num);
        assertTrue(num instanceof Float);
        assertEquals(3.14f, num.floatValue(), 0.001f);
    }

    @Test
    public void testCreateNumber_ValidDouble() {
        Number num = NumberUtils.createNumber("2.71828");
        assertNotNull(num);
        assertTrue(num instanceof Double);
        assertEquals(2.71828, num.doubleValue(), 0.00001);
    }

    @Test
    public void testCreateNumber_ValidHexInteger() {
        Number num = NumberUtils.createNumber("0xFF");
        assertNotNull(num);
        assertTrue(num instanceof Integer);
        assertEquals(255, num.intValue());
    }

    @Test
    public void testCreateNumber_ValidHexLong() {
        Number num = NumberUtils.createNumber("0xABCDEFL");
        assertNotNull(num);
        assertTrue(num instanceof Long);
        assertEquals(0xABCDEFL, num.longValue());
    }

    @Test
    public void testCreateNumber_ValidOctal() {
        Number num = NumberUtils.createNumber("0755");
        assertNotNull(num);
        assertTrue(num instanceof Integer);
        assertEquals(493, num.intValue());
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_EmptyString() {
        NumberUtils.createNumber("");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_NullString() {
        NumberUtils.createNumber(null);
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_InvalidString() {
        NumberUtils.createNumber("abc");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_DoubleDots() {
        NumberUtils.createNumber("1..2");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_InvalidSuffix() {
        NumberUtils.createNumber("123x");
    }

    // ---------- isNumber ----------

    @Test
    public void testIsNumber_Valid() {
        assertTrue(NumberUtils.isNumber("123"));
        assertTrue(NumberUtils.isNumber("123L"));
        assertTrue(NumberUtils.isNumber("123l"));
        assertTrue(NumberUtils.isNumber("3.14f"));
        assertTrue(NumberUtils.isNumber("2.718"));
        assertTrue(NumberUtils.isNumber("0xFF"));
        assertTrue(NumberUtils.isNumber("0755"));
    }

    @Test
    public void testIsNumber_Invalid() {
        assertFalse(NumberUtils.isNumber(""));
        assertFalse(NumberUtils.isNumber(null));
        assertFalse(NumberUtils.isNumber("abc"));
        assertFalse(NumberUtils.isNumber("1..2"));
        assertFalse(NumberUtils.isNumber("1.2.3"));
    }

    // ---------- createInteger ----------

    @Test
    public void testCreateInteger() {
        assertEquals(Integer.valueOf(100), NumberUtils.createInteger("100"));
        assertEquals(Integer.valueOf(0), NumberUtils.createInteger("0"));
        assertEquals(Integer.valueOf(-50), NumberUtils.createInteger("-50"));
        assertEquals(Integer.valueOf(255), NumberUtils.createInteger("0xFF"));
        assertEquals(Integer.valueOf(10), NumberUtils.createInteger("012"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateInteger_Null() {
        NumberUtils.createInteger(null);
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateInteger_Empty() {
        NumberUtils.createInteger("");
    }

    // ---------- createLong ----------

    @Test
    public void testCreateLong() {
        assertEquals(Long.valueOf(100L), NumberUtils.createLong("100"));
        assertEquals(Long.valueOf(0L), NumberUtils.createLong("0"));
        assertEquals(Long.valueOf(-50L), NumberUtils.createLong("-50"));
        assertEquals(Long.valueOf(255L), NumberUtils.createLong("0xFF"));
        assertEquals(Long.valueOf(10L), NumberUtils.createLong("012"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateLong_Null() {
        NumberUtils.createLong(null);
    }

    // ---------- createFloat ----------

    @Test
    public void testCreateFloat() {
        assertEquals(Float.valueOf(3.14f), NumberUtils.createFloat("3.14"));
        assertEquals(Float.valueOf(1.0f), NumberUtils.createFloat("1"));
        assertEquals(Float.valueOf(-2.5f), NumberUtils.createFloat("-2.5"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateFloat_Null() {
        NumberUtils.createFloat(null);
    }

    // ---------- createDouble ----------

    @Test
    public void testCreateDouble() {
        assertEquals(Double.valueOf(3.14), NumberUtils.createDouble("3.14"));
        assertEquals(Double.valueOf(1.0), NumberUtils.createDouble("1"));
        assertEquals(Double.valueOf(-2.5), NumberUtils.createDouble("-2.5"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateDouble_Null() {
        NumberUtils.createDouble(null);
    }

    // ---------- toInt ----------

    @Test
    public void testToInt_Valid() {
        assertEquals(123, NumberUtils.toInt("123"));
        assertEquals(0, NumberUtils.toInt("0"));
        assertEquals(-5, NumberUtils.toInt("-5"));
    }

    @Test
    public void testToInt_InvalidWithDefault() {
        assertEquals(0, NumberUtils.toInt(null, 0));
        assertEquals(0, NumberUtils.toInt("", 0));
        assertEquals(-1, NumberUtils.toInt("abc", -1));
    }

    // ---------- toLong ----------

    @Test
    public void testToLong_Valid() {
        assertEquals(123L, NumberUtils.toLong("123"));
        assertEquals(0L, NumberUtils.toLong("0"));
        assertEquals(-5L, NumberUtils.toLong("-5"));
    }

    @Test
    public void testToLong_InvalidWithDefault() {
        assertEquals(0L, NumberUtils.toLong(null, 0L));
        assertEquals(10L, NumberUtils.toLong("abc", 10L));
    }

    // ---------- toFloat ----------

    @Test
    public void testToFloat_Valid() {
        assertEquals(3.14f, NumberUtils.toFloat("3.14"), 0.001f);
        assertEquals(0.0f, NumberUtils.toFloat("0"), 0.001f);
        assertEquals(-2.5f, NumberUtils.toFloat("-2.5"), 0.001f);
    }

    @Test
    public void testToFloat_InvalidWithDefault() {
        assertEquals(0.0f, NumberUtils.toFloat(null, 0.0f), 0.001f);
        assertEquals(5.0f, NumberUtils.toFloat("abc", 5.0f), 0.001f);
    }

    // ---------- toDouble ----------

    @Test
    public void testToDouble_Valid() {
        assertEquals(3.14, NumberUtils.toDouble("3.14"), 0.001);
        assertEquals(0.0, NumberUtils.toDouble("0"), 0.001);
        assertEquals(-2.5, NumberUtils.toDouble("-2.5"), 0.001);
    }

    @Test
    public void testToDouble_InvalidWithDefault() {
        assertEquals(0.0, NumberUtils.toDouble(null, 0.0), 0.001);
        assertEquals(1.0, NumberUtils.toDouble("xyz", 1.0), 0.001);
    }

    // ---------- isDigits ----------

    @Test
    public void testIsDigits() {
        assertTrue(NumberUtils.isDigits("123"));
        assertTrue(NumberUtils.isDigits("0"));
        assertTrue(NumberUtils.isDigits("000"));
        assertFalse(NumberUtils.isDigits("12.3"));
        assertFalse(NumberUtils.isDigits("abc"));
        assertFalse(NumberUtils.isDigits(""));
        assertFalse(NumberUtils.isDigits(null));
    }

    // ---------- Edge cases ----------

    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_EmptyBrackets() {
        // Potential invalid format
        NumberUtils.createNumber("");
    }

    @Test
    public void testCreateNumber_MaxLong() {
        Number num = NumberUtils.createNumber(Long.MAX_VALUE + "L");
        assertNotNull(num);
        assertTrue(num instanceof Long);
        assertEquals(Long.MAX_VALUE, num.longValue());
    }

    @Test
    public void testCreateNumber_MinLong() {
        Number num = NumberUtils.createNumber((Long.MIN_VALUE) + "L");
        assertNotNull(num);
        assertTrue(num instanceof Long);
        assertEquals(Long.MIN_VALUE, num.longValue());
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_OverflowLong() {
        // This string is larger than Long.MAX_VALUE, should throw
        NumberUtils.createNumber("9223372036854775808L");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_InvalidNegativeHex() {
        NumberUtils.createNumber("-0x1");
    }
}