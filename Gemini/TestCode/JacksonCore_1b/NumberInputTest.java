package com.fasterxml.jackson.core.io;

import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.*;

public class NumberInputTest {

    @Test
    public void testConstructor() {
        NumberInput input = new NumberInput();
        assertNotNull(input);
    }

    // ==========================================
    // parseInt(char[], int, int) tests
    // ==========================================

    @Test
    public void testParseIntCharArrayPositiveLengths() {
        char[] buffer = "0123456789".toCharArray();
        assertEquals(0, NumberInput.parseInt(buffer, 0, 1));
        assertEquals(1, NumberInput.parseInt(buffer, 1, 1));
        assertEquals(12, NumberInput.parseInt(buffer, 1, 2));
        assertEquals(123, NumberInput.parseInt(buffer, 1, 3));
        assertEquals(1234, NumberInput.parseInt(buffer, 1, 4));
        assertEquals(12345, NumberInput.parseInt(buffer, 1, 5));
        assertEquals(123456, NumberInput.parseInt(buffer, 1, 6));
        assertEquals(1234567, NumberInput.parseInt(buffer, 1, 7));
        assertEquals(12345678, NumberInput.parseInt(buffer, 1, 8));
        assertEquals(123456789, NumberInput.parseInt(buffer, 1, 9));
    }

    @Test
    public void testParseIntCharArrayLargeAndMax() {
        String maxIntStr = String.valueOf(Integer.MAX_VALUE);
        char[] maxChars = maxIntStr.toCharArray();
        assertEquals(Integer.MAX_VALUE, NumberInput.parseInt(maxChars, 0, maxChars.length));

        String almostMaxStr = "2147483646";
        assertEquals(2147483646, NumberInput.parseInt(almostMaxStr.toCharArray(), 0, almostMaxStr.length()));
    }

    @Test
    public void testParseIntCharArrayNegative() {
        String minIntStr = String.valueOf(Integer.MIN_VALUE);
        char[] minChars = minIntStr.toCharArray();
        assertEquals(Integer.MIN_VALUE, NumberInput.parseInt(minChars, 0, minChars.length));

        String negStr = "-12345";
        char[] negChars = negStr.toCharArray();
        assertEquals(-12345, NumberInput.parseInt(negChars, 0, negChars.length));

        String negOneStr = "-1";
        assertEquals(-1, NumberInput.parseInt(negOneStr.toCharArray(), 0, negOneStr.length()));
    }

    @Test(expected = NumberFormatException.class)
    public void testParseIntCharArrayEmptyThrows() {
        NumberInput.parseInt(new char[0], 0, 0);
    }

    // ==========================================
    // parseInt(String) tests
    // ==========================================

    @Test
    public void testParseIntStringLengths() {
        assertEquals(0, NumberInput.parseInt("0"));
        assertEquals(1, NumberInput.parseInt("1"));
        assertEquals(12, NumberInput.parseInt("12"));
        assertEquals(123, NumberInput.parseInt("123"));
        assertEquals(1234, NumberInput.parseInt("1234"));
        assertEquals(12345, NumberInput.parseInt("12345"));
        assertEquals(123456, NumberInput.parseInt("123456"));
        assertEquals(1234567, NumberInput.parseInt("1234567"));
        assertEquals(12345678, NumberInput.parseInt("12345678"));
        assertEquals(123456789, NumberInput.parseInt("123456789"));
        assertEquals(Integer.MAX_VALUE, NumberInput.parseInt(String.valueOf(Integer.MAX_VALUE)));
    }

    @Test
    public void testParseIntStringNegativeLengths() {
        assertEquals(-1, NumberInput.parseInt("-1"));
        assertEquals(-12, NumberInput.parseInt("-12"));
        assertEquals(-123, NumberInput.parseInt("-123"));
        assertEquals(-1234, NumberInput.parseInt("-1234"));
        assertEquals(-12345, NumberInput.parseInt("-12345"));
        assertEquals(-123456, NumberInput.parseInt("-123456"));
        assertEquals(-1234567, NumberInput.parseInt("-1234567"));
        assertEquals(-12345678, NumberInput.parseInt("-12345678"));
        assertEquals(-123456789, NumberInput.parseInt("-123456789"));
        assertEquals(-1000000000, NumberInput.parseInt("-1000000000"));
        assertEquals(Integer.MIN_VALUE, NumberInput.parseInt(String.valueOf(Integer.MIN_VALUE)));
    }

    @Test(expected = NumberFormatException.class)
    public void testParseIntStringInvalidEmpty() {
        NumberInput.parseInt("");
    }

    @Test(expected = NumberFormatException.class)
    public void testParseIntStringInvalidOnlyMinus() {
        NumberInput.parseInt("-");
    }

    @Test(expected = NumberFormatException.class)
    public void testParseIntStringInvalidOnlyPlus() {
        NumberInput.parseInt("+");
    }

    @Test(expected = NumberFormatException.class)
    public void testParseIntStringNonDigits() {
        NumberInput.parseInt("12a34");
    }

    @Test(expected = NumberFormatException.class)
    public void testParseIntStringLeadingGarbage() {
        NumberInput.parseInt("a123");
    }

    // ==========================================
    // parseLong(char[], int, int) tests
    // ==========================================

    @Test
    public void testParseLongCharArray() {
        String s = "123456789012345";
        char[] ch = s.toCharArray();
        assertEquals(123456789012345L, NumberInput.parseLong(ch, 0, ch.length));

        String s9 = "123456789";
        assertEquals(123456789L, NumberInput.parseLong(s9.toCharArray(), 0, s9.length()));

        String s10 = "1234567890";
        assertEquals(1234567890L, NumberInput.parseLong(s10.toCharArray(), 0, s10.length()));

        String maxLongStr = String.valueOf(Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, NumberInput.parseLong(maxLongStr.toCharArray(), 0, maxLongStr.length()));

        String minLongStr = String.valueOf(Long.MIN_VALUE);
        assertEquals(Long.MIN_VALUE, NumberInput.parseLong(minLongStr.toCharArray(), 0, minLongStr.length()));
    }

    @Test
    public void testParseLongCharArraySmallLengths() {
        for (int i = 0; i <= 9; i++) {
            String valStr = String.valueOf(i);
            assertEquals((long) i, NumberInput.parseLong(valStr.toCharArray(), 0, valStr.length()));
        }
    }

    // ==========================================
    // parseLong(String) tests
    // ==========================================

    @Test
    public void testParseLongString() {
        assertEquals(0L, NumberInput.parseLong("0"));
        assertEquals(123456789012345L, NumberInput.parseLong("123456789012345"));
        assertEquals(-123456789012345L, NumberInput.parseLong("-123456789012345"));
        assertEquals(Long.MAX_VALUE, NumberInput.parseLong(String.valueOf(Long.MAX_VALUE)));
        assertEquals(Long.MIN_VALUE, NumberInput.parseLong(String.valueOf(Long.MIN_VALUE)));
        assertEquals(999999999L, NumberInput.parseLong("999999999"));
        assertEquals(-999999999L, NumberInput.parseLong("-999999999"));
    }

    // ==========================================
    // inLongRange(char[], int, int, boolean) & inLongRange(String, boolean)
    // ==========================================

    @Test
    public void testInLongRangeCharArray() {
        char[] max = String.valueOf(Long.MAX_VALUE).toCharArray();
        assertTrue(NumberInput.inLongRange(max, 0, max.length, false));

        char[] min = String.valueOf(Long.MIN_VALUE).substring(1).toCharArray();
        assertTrue(NumberInput.inLongRange(min, 0, min.length, true));

        // Shorter than 19 is always in range
        char[] shortDigits = "1234567890".toCharArray();
        assertTrue(NumberInput.inLongRange(shortDigits, 0, shortDigits.length, false));

        // Longer than 19 is always out of range
        char[] longDigits = "12345678901234567890".toCharArray();
        assertFalse(NumberInput.inLongRange(longDigits, 0, longDigits.length, false));

        // Compare 19-char edge cases
        // MAX_VALUE is 9223372036854775807
        char[] overMax = "9223372036854775808".toCharArray();
        assertFalse(NumberInput.inLongRange(overMax, 0, overMax.length, false));
        assertTrue(NumberInput.inLongRange(overMax, 0, overMax.length, true)); // fits MIN_VALUE

        char[] overMin = "9223372036854775809".toCharArray();
        assertFalse(NumberInput.inLongRange(overMin, 0, overMin.length, true));

        char[] smallerFirstDigit = "8223372036854775807".toCharArray();
        assertTrue(NumberInput.inLongRange(smallerFirstDigit, 0, smallerFirstDigit.length, false));

        char[] largerFirstDigit = "9323372036854775807".toCharArray();
        assertFalse(NumberInput.inLongRange(largerFirstDigit, 0, largerFirstDigit.length, false));
    }

    @Test
    public void testInLongRangeString() {
        String max = String.valueOf(Long.MAX_VALUE);
        assertTrue(NumberInput.inLongRange(max, false));

        String min = String.valueOf(Long.MIN_VALUE).substring(1);
        assertTrue(NumberInput.inLongRange(min, true));

        assertTrue(NumberInput.inLongRange("12345", false));
        assertFalse(NumberInput.inLongRange("12345678901234567890", false));

        assertFalse(NumberInput.inLongRange("9223372036854775808", false));
        assertTrue(NumberInput.inLongRange("9223372036854775808", true));
        assertFalse(NumberInput.inLongRange("9223372036854775809", true));

        assertTrue(NumberInput.inLongRange("1223372036854775807", false));
        assertFalse(NumberInput.inLongRange("9923372036854775807", false));
    }

    // ==========================================
    // parseAsInt(String, int) tests
    // ==========================================

    @Test
    public void testParseAsInt() {
        assertEquals(123, NumberInput.parseAsInt("123", 0));
        assertEquals(-123, NumberInput.parseAsInt("-123", 0));
        assertEquals(456, NumberInput.parseAsInt("+456", 0));
        assertEquals(789, NumberInput.parseAsInt("  789  ", 0));
        assertEquals(42, NumberInput.parseAsInt(null, 42));
        assertEquals(42, NumberInput.parseAsInt("", 42));
        assertEquals(42, NumberInput.parseAsInt("   ", 42));
        assertEquals(42, NumberInput.parseAsInt("+", 42));
        assertEquals(42, NumberInput.parseAsInt("-", 42));
        assertEquals(42, NumberInput.parseAsInt("123.456", 42));
        assertEquals(42, NumberInput.parseAsInt("abc", 42));
        assertEquals(42, NumberInput.parseAsInt("123456789012345", 42)); // overflow int
        assertEquals(Integer.MAX_VALUE, NumberInput.parseAsInt(String.valueOf(Integer.MAX_VALUE), 0));
        assertEquals(Integer.MIN_VALUE, NumberInput.parseAsInt(String.valueOf(Integer.MIN_VALUE), 0));
    }

    @Test
    public void testParseAsIntLeadingZerosAndSigns() {
        assertEquals(0, NumberInput.parseAsInt("+0", 99));
        assertEquals(0, NumberInput.parseAsInt("-0", 99));
        assertEquals(5, NumberInput.parseAsInt("+5", 99));
        assertEquals(-5, NumberInput.parseAsInt("-5", 99));
        assertEquals(99, NumberInput.parseAsInt("+-5", 99));
    }

    // ==========================================
    // parseAsLong(String, long) tests
    // ==========================================

    @Test
    public void testParseAsLong() {
        assertEquals(1234567890123L, NumberInput.parseAsLong("1234567890123", 0L));
        assertEquals(-1234567890123L, NumberInput.parseAsLong("-1234567890123", 0L));
        assertEquals(456L, NumberInput.parseAsLong("+456", 0L));
        assertEquals(789L, NumberInput.parseAsLong("  789  ", 0L));
        assertEquals(42L, NumberInput.parseAsLong(null, 42L));
        assertEquals(42L, NumberInput.parseAsLong("", 42L));
        assertEquals(42L, NumberInput.parseAsLong("   ", 42L));
        assertEquals(42L, NumberInput.parseAsLong("+", 42L));
        assertEquals(42L, NumberInput.parseAsLong("-", 42L));
        assertEquals(42L, NumberInput.parseAsLong("123.456", 42L));
        assertEquals(42L, NumberInput.parseAsLong("abc", 42L));
        assertEquals(42L, NumberInput.parseAsLong("99999999999999999999999999", 42L)); // overflow long
        assertEquals(Long.MAX_VALUE, NumberInput.parseAsLong(String.valueOf(Long.MAX_VALUE), 0L));
        assertEquals(Long.MIN_VALUE, NumberInput.parseAsLong(String.valueOf(Long.MIN_VALUE), 0L));
    }

    // ==========================================
    // parseAsDouble(String, double) tests
    // ==========================================

    @Test
    public void testParseAsDouble() {
        assertEquals(123.456, NumberInput.parseAsDouble("123.456", 0.0), 0.0001);
        assertEquals(-123.456, NumberInput.parseAsDouble("-123.456", 0.0), 0.0001);
        assertEquals(789.0, NumberInput.parseAsDouble("  789.0  ", 0.0), 0.0001);
        assertEquals(42.5, NumberInput.parseAsDouble(null, 42.5), 0.0001);
        assertEquals(42.5, NumberInput.parseAsDouble("", 42.5), 0.0001);
        assertEquals(42.5, NumberInput.parseAsDouble("   ", 42.5), 0.0001);
        assertEquals(42.5, NumberInput.parseAsDouble("invalid", 42.5), 0.0001);
        assertEquals(1.23e4, NumberInput.parseAsDouble("1.23e4", 0.0), 0.0001);
    }

    // ==========================================
    // parseDouble(String) tests
    // ==========================================

    @Test
    public void testParseDouble() {
        assertEquals(123.456, NumberInput.parseDouble("123.456"), 0.0001);
        assertEquals(-123.456, NumberInput.parseDouble("-123.456"), 0.0001);
        assertEquals(0.0, NumberInput.parseDouble("0"), 0.0001);
        assertEquals(0.0, NumberInput.parseDouble("0.0"), 0.0001);
        assertEquals(2.2250738585072012e-308, NumberInput.parseDouble(NumberInput.NASTY_SMALL_DOUBLE), 1e-320);
    }

    @Test(expected = NumberFormatException.class)
    public void testParseDoubleInvalidThrows() {
        NumberInput.parseDouble("not-a-number");
    }

    // ==========================================
    // parseBigDecimal tests
    // ==========================================

    @Test
    public void testParseBigDecimalString() {
        assertEquals(new BigDecimal("123.456"), NumberInput.parseBigDecimal("123.456"));
        assertEquals(new BigDecimal("-123.456"), NumberInput.parseBigDecimal("-123.456"));
        assertEquals(new BigDecimal("0"), NumberInput.parseBigDecimal("0"));
        assertEquals(new BigDecimal("1e10"), NumberInput.parseBigDecimal("1e10"));
    }

    @Test(expected = NumberFormatException.class)
    public void testParseBigDecimalStringInvalid() {
        NumberInput.parseBigDecimal("not_a_number");
    }

    @Test
    public void testParseBigDecimalCharArray() {
        char[] ch = "123.456".toCharArray();
        assertEquals(new BigDecimal("123.456"), NumberInput.parseBigDecimal(ch));
    }

    @Test
    public void testParseBigDecimalCharArrayOffset() {
        char[] ch = "prefix123.456suffix".toCharArray();
        assertEquals(new BigDecimal("123.456"), NumberInput.parseBigDecimal(ch, 6, 7));
    }

    @Test(expected = NumberFormatException.class)
    public void testParseBigDecimalCharArrayInvalid() {
        char[] ch = "invalid".toCharArray();
        NumberInput.parseBigDecimal(ch, 0, ch.length);
    }
}