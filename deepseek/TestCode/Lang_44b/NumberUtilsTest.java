package org.apache.commons.lang;

import junit.framework.TestCase;
import org.junit.Test;
import static org.junit.Assert.*;
import java.math.BigDecimal;
import java.math.BigInteger;

public class NumberUtilsTest {

    // ===== createNumber() tests =====

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberEmptyString() {
        NumberUtils.createNumber("");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberOnlyPlus() {
        NumberUtils.createNumber("+");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberOnlyMinus() {
        NumberUtils.createNumber("-");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberPlusZero() {
        NumberUtils.createNumber("+0");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberTrailingPlus() {
        NumberUtils.createNumber("0+");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberTrailingMinus() {
        NumberUtils.createNumber("0-");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberHexPrefixOnly() {
        NumberUtils.createNumber("0x");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberHexPrefixOnlyUpperCase() {
        NumberUtils.createNumber("0X");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberNegativeHexPrefixOnly() {
        NumberUtils.createNumber("-0x");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberExponentialDot() {
        NumberUtils.createNumber("0e");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberExponentialDotUpperCase() {
        NumberUtils.createNumber("0E");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberInvalidExponential() {
        NumberUtils.createNumber("1e");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberDoubleExponential() {
        NumberUtils.createNumber("1ee2");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberTrailingDot() {
        NumberUtils.createNumber("1.");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberLeadingDot() {
        NumberUtils.createNumber(".1");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberDoubleNegativeSign() {
        NumberUtils.createNumber("--1");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberDoublePlusSign() {
        NumberUtils.createNumber("++1");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberMultipleDecimalPoints() {
        NumberUtils.createNumber("1.2.3");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberNegativeMultipleDecimalPoints() {
        NumberUtils.createNumber("-1.2.3");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberInvalidCharacter() {
        NumberUtils.createNumber("12a34");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberHexWithInvalidSuffix() {
        NumberUtils.createNumber("0x1G");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberOctalWithInvalidDigits() {
        NumberUtils.createNumber("08");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberLargeOctalWithInvalidDigits() {
        NumberUtils.createNumber("-0128");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberNegativeHexPrefixWithSign() {
        NumberUtils.createNumber("+-0x1");
    }

    // Valid createNumber tests

    @Test
    public void testCreateNumberNull() {
        assertNull(NumberUtils.createNumber(null));
    }

    @Test
    public void testCreateNumberInteger() {
        assertEquals(Integer.valueOf(123), NumberUtils.createNumber("123"));
        assertEquals(Integer.valueOf(-123), NumberUtils.createNumber("-123"));
        assertEquals(Integer.valueOf(0), NumberUtils.createNumber("0"));
    }

    @Test
    public void testCreateNumberLong() {
        assertEquals(Long.valueOf(1234567890123L), NumberUtils.createNumber("1234567890123"));
        assertEquals(Long.valueOf(-1234567890123L), NumberUtils.createNumber("-1234567890123"));
    }

    @Test
    public void testCreateNumberFloat() {
        assertEquals(Float.valueOf(1.23f), NumberUtils.createNumber("1.23f"));
        assertEquals(Float.valueOf(-1.23f), NumberUtils.createNumber("-1.23F"));
        assertEquals(Float.valueOf(0.0f), NumberUtils.createNumber("0.0f"));
        assertEquals(Float.valueOf(Float.MAX_VALUE), NumberUtils.createNumber("340282346638528859811704183484516925440.0f"));
    }

    @Test
    public void testCreateNumberDouble() {
        assertEquals(Double.valueOf(3.1415), NumberUtils.createNumber("3.1415"));
        assertEquals(Double.valueOf(-3.1415), NumberUtils.createNumber("-3.1415"));
        assertEquals(Double.valueOf(1.0), NumberUtils.createNumber("1d"));
        assertEquals(Double.valueOf(1.0), NumberUtils.createNumber("1D"));
        assertEquals(Double.valueOf(0.0), NumberUtils.createNumber("0.0"));
        assertEquals(Double.valueOf(Double.MAX_VALUE), NumberUtils.createNumber("1.7976931348623157e+308"));
    }

    @Test
    public void testCreateNumberBigDecimal() {
        assertEquals(new BigDecimal("12345678901234567890.1234567890"), NumberUtils.createNumber("12345678901234567890.1234567890"));
        assertEquals(new BigDecimal("-12345678901234567890.1234567890"), NumberUtils.createNumber("-12345678901234567890.1234567890"));
    }

    @Test
    public void testCreateNumberBigInteger() {
        assertEquals(new BigInteger("123456789012345678901234567890"), NumberUtils.createNumber("123456789012345678901234567890"));
        assertEquals(new BigInteger("-123456789012345678901234567890"), NumberUtils.createNumber("-123456789012345678901234567890"));
    }

    @Test
    public void testCreateNumberHex() {
        assertEquals(Integer.valueOf(255), NumberUtils.createNumber("0xFF"));
        assertEquals(Integer.valueOf(255), NumberUtils.createNumber("0XFF"));
        assertEquals(Integer.valueOf(-255), NumberUtils.createNumber("-0xFF"));
        assertEquals(Integer.valueOf(0), NumberUtils.createNumber("0x0"));
    }

    @Test
    public void testCreateNumberOctal() {
        assertEquals(Integer.valueOf(83), NumberUtils.createNumber("0123"));
        assertEquals(Integer.valueOf(-83), NumberUtils.createNumber("-0123"));
    }

    @Test
    public void testCreateNumberNegativeZero() {
        assertEquals(Integer.valueOf(0), NumberUtils.createNumber("-0"));
        assertEquals(Long.valueOf(0L), NumberUtils.createNumber("-0L"));
    }

    @Test
    public void testCreateNumberNegativeFlota() {
        assertEquals(Float.valueOf(-0.0f), NumberUtils.createNumber("-0f"));
        assertEquals(Double.valueOf(-0.0d), NumberUtils.createNumber("-0d"));
    }

    @Test
    public void testCreateNumberLongWithL() {
        assertEquals(Long.valueOf(100L), NumberUtils.createNumber("100L"));
        assertEquals(Long.valueOf(-100L), NumberUtils.createNumber("-100l"));
    }

    @Test
    public void testCreateNumberFloatWithF() {
        assertEquals(Float.valueOf(100.0f), NumberUtils.createNumber("100f"));
        assertEquals(Float.valueOf(100.0f), NumberUtils.createNumber("100F"));
    }

    @Test
    public void testCreateNumberDoubleWithD() {
        assertEquals(Double.valueOf(100.0), NumberUtils.createNumber("100d"));
        assertEquals(Double.valueOf(100.0), NumberUtils.createNumber("100D"));
    }

    @Test
    public void testCreateNumberExponential() {
        assertEquals(Double.valueOf(1.0e10), NumberUtils.createNumber("1e10"));
        assertEquals(Double.valueOf(-1.0e10), NumberUtils.createNumber("-1e10"));
        assertEquals(Double.valueOf(1.23e10), NumberUtils.createNumber("1.23e10"));
        assertEquals(Double.valueOf(1.23E10), NumberUtils.createNumber("1.23E10"));
    }

    @Test
    public void testCreateNumberExponentialWithSign() {
        assertEquals(Double.valueOf(1.0e10), NumberUtils.createNumber("1e+10"));
        assertEquals(Double.valueOf(1.0e-10), NumberUtils.createNumber("1e-10"));
    }

    @Test
    public void testCreateNumberUnderlineInLiteral() {
        // underscores in numeric literals (JDK 7+) are not supported by NumberUtils
        assertNull(NumberUtils.createNumber("1_000"));
    }

    // ===== isNumber() tests =====

    @Test
    public void testIsNumberNull() {
        assertFalse(NumberUtils.isNumber(null));
    }

    @Test
    public void testIsNumberEmptyString() {
        assertFalse(NumberUtils.isNumber(""));
    }

    @Test
    public void testIsNumberOnlySign() {
        assertFalse(NumberUtils.isNumber("+"));
        assertFalse(NumberUtils.isNumber("-"));
    }

    @Test
    public void testIsNumberTrailingSign() {
        assertFalse(NumberUtils.isNumber("0+"));
        assertFalse(NumberUtils.isNumber("0-"));
    }

    @Test
    public void testIsNumberHexPrefixOnly() {
        assertFalse(NumberUtils.isNumber("0x"));
        assertFalse(NumberUtils.isNumber("0X"));
    }

    @Test
    public void testIsNumberTrailingDot() {
        assertFalse(NumberUtils.isNumber("1."));
    }

    @Test
    public void testIsNumberLeadingDot() {
        assertFalse(NumberUtils.isNumber(".1"));
    }

    @Test
    public void testIsNumberMultipleDecimal() {
        assertFalse(NumberUtils.isNumber("1.2.3"));
    }

    @Test
    public void testIsNumberInvalidHexChar() {
        assertFalse(NumberUtils.isNumber("0x1G"));
    }

    @Test
    public void testIsNumberOctalWith8() {
        assertFalse(NumberUtils.isNumber("08"));
    }

    @Test
    public void testIsNumberNegativeHexPrefixOnly() {
        assertFalse(NumberUtils.isNumber("-0x"));
    }

    @Test
    public void testIsNumberDoubleSign() {
        assertFalse(NumberUtils.isNumber("--1"));
    }

    @Test
    public void testIsNumberValid() {
        assertTrue(NumberUtils.isNumber("123"));
        assertTrue(NumberUtils.isNumber("-123"));
        assertTrue(NumberUtils.isNumber("0"));
        assertTrue(NumberUtils.isNumber("1.23"));
        assertTrue(NumberUtils.isNumber("-1.23"));
        assertTrue(NumberUtils.isNumber("1.23e10"));
        assertTrue(NumberUtils.isNumber("1e10"));
        assertTrue(NumberUtils.isNumber("0xFF"));
        assertTrue(NumberUtils.isNumber("-0xFF"));
        assertTrue(NumberUtils.isNumber("0123"));
        assertTrue(NumberUtils.isNumber("-0123"));
        assertTrue(NumberUtils.isNumber("1L"));
        assertTrue(NumberUtils.isNumber("1l"));
        assertTrue(NumberUtils.isNumber("1F"));
        assertTrue(NumberUtils.isNumber("1f"));
        assertTrue(NumberUtils.isNumber("1D"));
        assertTrue(NumberUtils.isNumber("1d"));
    }

    // ===== createInteger() tests =====

    @Test
    public void testCreateIntegerNull() {
        assertNull(NumberUtils.createInteger(null));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateIntegerEmpty() {
        NumberUtils.createInteger("");
    }

    @Test
    public void testCreateIntegerValid() {
        assertEquals(Integer.valueOf(42), NumberUtils.createInteger("42"));
        assertEquals(Integer.valueOf(-42), NumberUtils.createInteger("-42"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateIntegerOverflow() {
        NumberUtils.createInteger("9999999999");
    }

    // ===== createLong() tests =====

    @Test
    public void testCreateLongNull() {
        assertNull(NumberUtils.createLong(null));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateLongEmpty() {
        NumberUtils.createLong("");
    }

    @Test
    public void testCreateLongValid() {
        assertEquals(Long.valueOf(42L), NumberUtils.createLong("42"));
        assertEquals(Long.valueOf(-42L), NumberUtils.createLong("-42"));
    }

    // ===== createFloat() tests =====

    @Test
    public void testCreateFloatNull() {
        assertNull(NumberUtils.createFloat(null));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateFloatEmpty() {
        NumberUtils.createFloat("");
    }

    @Test
    public void testCreateFloatValid() {
        assertEquals(Float.valueOf(3.14f), NumberUtils.createFloat("3.14"));
        assertEquals(Float.valueOf(-3.14f), NumberUtils.createFloat("-3.14"));
    }

    // ===== createDouble() tests =====

    @Test
    public void testCreateDoubleNull() {
        assertNull(NumberUtils.createDouble(null));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateDoubleEmpty() {
        NumberUtils.createDouble("");
    }

    @Test
    public void testCreateDoubleValid() {
        assertEquals(Double.valueOf(1.23), NumberUtils.createDouble("1.23"));
        assertEquals(Double.valueOf(-1.23), NumberUtils.createDouble("-1.23"));
    }

    // ===== createBigDecimal() tests =====

    @Test
    public void testCreateBigDecimalNull() {
        assertNull(NumberUtils.createBigDecimal(null));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateBigDecimalEmpty() {
        NumberUtils.createBigDecimal("");
    }

    @Test
    public void testCreateBigDecimalValid() {
        assertEquals(new BigDecimal("1.234"), NumberUtils.createBigDecimal("1.234"));
        assertEquals(new BigDecimal("-1.234"), NumberUtils.createBigDecimal("-1.234"));
    }

    // ===== createBigInteger() tests =====

    @Test
    public void testCreateBigIntegerNull() {
        assertNull(NumberUtils.createBigInteger(null));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateBigIntegerEmpty() {
        NumberUtils.createBigInteger("");
    }

    @Test
    public void testCreateBigIntegerValid() {
        assertEquals(new BigInteger("12345678901234567890"), NumberUtils.createBigInteger("12345678901234567890"));
        assertEquals(new BigInteger("-12345678901234567890"), NumberUtils.createBigInteger("-12345678901234567890"));
    }

    // ===== Specific Defects4J bug test =====

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testLang457() {
        // This test triggers a StringIndexOutOfBoundsException in createNumber
        // for bug 44 (Defects4J). The exact input that causes the bug is "0+"
        // or similar. Here we test multiple known triggers.
        try {
            NumberUtils.createNumber("0+");
        } catch (NumberFormatException e) {
            // If NumberFormatException is thrown instead, it's a different behavior
            fail("Expected StringIndexOutOfBoundsException");
        }
    }
}