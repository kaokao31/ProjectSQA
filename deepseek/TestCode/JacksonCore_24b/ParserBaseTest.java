package com.fasterxml.jackson.core.base;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.JsonParseException;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.*;

/**
 * Comprehensive JUnit 4 test suite for ParserBase (JacksonCore bug 24 context).
 * Tests number parsing edge cases, leading zeros, overflow, and invalid inputs.
 */
public class ParserBaseTest {

    private JsonFactory factory;

    @Before
    public void setUp() {
        factory = new JsonFactory();
    }

    // Helper: parse a JSON number string and return the parser positioned at the value token.
    private JsonParser parseNumber(String numberStr) throws Exception {
        // The input must be a valid JSON value; numbers are valid.
        JsonParser parser = factory.createParser(numberStr);
        assertNotNull("Parser should not be null", parser);
        // Advance to the first token (the number)
        JsonToken token = parser.nextToken();
        assertNotNull("Expected a token", token);
        assertTrue("Expected a value token", token.isNumeric());
        return parser;
    }

    // Helper: parse an invalid number string and expect JsonParseException.
    private void assertInvalidNumber(String numberStr) throws Exception {
        try {
            JsonParser parser = factory.createParser(numberStr);
            parser.nextToken();
            fail("Expected JsonParseException for invalid number: " + numberStr);
        } catch (JsonParseException e) {
            // expected
        }
    }

    // ==================== Valid integer numbers ====================

    @Test
    public void testParseIntPositive() throws Exception {
        JsonParser parser = parseNumber("42");
        assertEquals(42, parser.getIntValue());
        assertEquals(42L, parser.getLongValue());
        assertEquals(42.0, parser.getDoubleValue(), 0.0);
        assertEquals(BigDecimal.valueOf(42), parser.getDecimalValue());
        assertEquals("42", parser.getValueAsString());
    }

    @Test
    public void testParseIntNegative() throws Exception {
        JsonParser parser = parseNumber("-123");
        assertEquals(-123, parser.getIntValue());
        assertEquals(-123L, parser.getLongValue());
        assertEquals(-123.0, parser.getDoubleValue(), 0.0);
        assertEquals(BigDecimal.valueOf(-123), parser.getDecimalValue());
    }

    @Test
    public void testParseIntZero() throws Exception {
        JsonParser parser = parseNumber("0");
        assertEquals(0, parser.getIntValue());
        assertEquals(0L, parser.getLongValue());
        assertEquals(0.0, parser.getDoubleValue(), 0.0);
        assertEquals(BigDecimal.ZERO, parser.getDecimalValue());
    }

    @Test
    public void testParseIntMaxValue() throws Exception {
        JsonParser parser = parseNumber(String.valueOf(Integer.MAX_VALUE));
        assertEquals(Integer.MAX_VALUE, parser.getIntValue());
        assertEquals((long) Integer.MAX_VALUE, parser.getLongValue());
    }

    @Test
    public void testParseIntMinValue() throws Exception {
        JsonParser parser = parseNumber(String.valueOf(Integer.MIN_VALUE));
        assertEquals(Integer.MIN_VALUE, parser.getIntValue());
        assertEquals((long) Integer.MIN_VALUE, parser.getLongValue());
    }

    @Test
    public void testParseIntOverflow() throws Exception {
        // Number larger than Integer.MAX_VALUE should throw on getIntValue
        JsonParser parser = parseNumber("2147483648"); // MAX+1
        try {
            parser.getIntValue();
            fail("Expected JsonParseException for overflow");
        } catch (JsonParseException e) {
            // expected
        }
        // But getLongValue should work
        assertEquals(2147483648L, parser.getLongValue());
    }

    @Test
    public void testParseLongMaxValue() throws Exception {
        JsonParser parser = parseNumber(String.valueOf(Long.MAX_VALUE));
        assertEquals(Long.MAX_VALUE, parser.getLongValue());
        // getIntValue should throw
        try {
            parser.getIntValue();
            fail("Expected JsonParseException for overflow");
        } catch (JsonParseException e) {
            // expected
        }
    }

    @Test
    public void testParseLongMinValue() throws Exception {
        JsonParser parser = parseNumber(String.valueOf(Long.MIN_VALUE));
        assertEquals(Long.MIN_VALUE, parser.getLongValue());
    }

    @Test
    public void testParseLongOverflow() throws Exception {
        // Number larger than Long.MAX_VALUE
        JsonParser parser = parseNumber("9223372036854775808"); // MAX+1
        try {
            parser.getLongValue();
            fail("Expected JsonParseException for overflow");
        } catch (JsonParseException e) {
            // expected
        }
        // getDoubleValue may work
        assertEquals(9.223372036854776E18, parser.getDoubleValue(), 0.0);
    }

    // ==================== Leading zeros (should be invalid per JSON spec) ====================

    @Test
    public void testParseIntLeadingZero() throws Exception {
        assertInvalidNumber("00");
    }

    @Test
    public void testParseIntLeadingZeroNonZero() throws Exception {
        assertInvalidNumber("01");
    }

    @Test
    public void testParseIntMultipleLeadingZeros() throws Exception {
        assertInvalidNumber("000");
    }

    @Test
    public void testParseIntLeadingZeroWithDigits() throws Exception {
        assertInvalidNumber("00123");
    }

    @Test
    public void testParseIntNegativeLeadingZero() throws Exception {
        assertInvalidNumber("-01");
    }

    // ==================== Valid floating-point numbers ====================

    @Test
    public void testParseDoublePositive() throws Exception {
        JsonParser parser = parseNumber("3.14");
        assertEquals(3.14, parser.getDoubleValue(), 1e-9);
        assertEquals(BigDecimal.valueOf(3.14), parser.getDecimalValue());
        // getIntValue should throw for non-integer
        try {
            parser.getIntValue();
            fail("Expected JsonParseException for non-integer");
        } catch (JsonParseException e) {
            // expected
        }
    }

    @Test
    public void testParseDoubleNegative() throws Exception {
        JsonParser parser = parseNumber("-2.5");
        assertEquals(-2.5, parser.getDoubleValue(), 1e-9);
    }

    @Test
    public void testParseDoubleZero() throws Exception {
        JsonParser parser = parseNumber("0.0");
        assertEquals(0.0, parser.getDoubleValue(), 0.0);
        assertEquals(BigDecimal.ZERO, parser.getDecimalValue());
    }

    @Test
    public void testParseDoubleLeadingZeros() throws Exception {
        // Leading zeros before decimal point are invalid
        assertInvalidNumber("00.5");
    }

    @Test
    public void testParseDoubleWithDecimalPoint() throws Exception {
        JsonParser parser = parseNumber("1.0");
        assertEquals(1.0, parser.getDoubleValue(), 0.0);
    }

    @Test
    public void testParseDoubleWithExponent() throws Exception {
        JsonParser parser = parseNumber("1e5");
        assertEquals(1e5, parser.getDoubleValue(), 0.0);
        assertEquals(BigDecimal.valueOf(1e5), parser.getDecimalValue());
    }

    @Test
    public void testParseDoubleWithExponentUpperCase() throws Exception {
        JsonParser parser = parseNumber("1E5");
        assertEquals(1e5, parser.getDoubleValue(), 0.0);
    }

    @Test
    public void testParseDoubleWithPositiveExponent() throws Exception {
        JsonParser parser = parseNumber("1e+5");
        assertEquals(1e5, parser.getDoubleValue(), 0.0);
    }

    @Test
    public void testParseDoubleWithNegativeExponent() throws Exception {
        JsonParser parser = parseNumber("1e-5");
        assertEquals(1e-5, parser.getDoubleValue(), 1e-9);
    }

    @Test
    public void testParseDoubleWithExponentAndDecimal() throws Exception {
        JsonParser parser = parseNumber("1.5e2");
        assertEquals(150.0, parser.getDoubleValue(), 0.0);
    }

    @Test
    public void testParseDoubleWithLeadingZeroAndExponent() throws Exception {
        // "0e5" is valid (zero with exponent)
        JsonParser parser = parseNumber("0e5");
        assertEquals(0.0, parser.getDoubleValue(), 0.0);
    }

    @Test
    public void testParseDoubleWithLargeExponent() throws Exception {
        JsonParser parser = parseNumber("1e1000");
        // Should be parsed as Infinity or large double
        double val = parser.getDoubleValue();
        assertTrue(Double.isInfinite(val) || val > 1e300);
    }

    @Test
    public void testParseDoubleWithNegativeExponentSmall() throws Exception {
        JsonParser parser = parseNumber("1e-1000");
        double val = parser.getDoubleValue();
        assertEquals(0.0, val, 1e-300);
    }

    // ==================== Invalid number formats ====================

    @Test
    public void testParseInvalidEmptyString() throws Exception {
        assertInvalidNumber("");
    }

    @Test
    public void testParseInvalidPlusSign() throws Exception {
        assertInvalidNumber("+1");
    }

    @Test
    public void testParseInvalidMinusOnly() throws Exception {
        assertInvalidNumber("-");
    }

    @Test
    public void testParseInvalidDecimalPointOnly() throws Exception {
        assertInvalidNumber(".");
    }

    @Test
    public void testParseInvalidTrailingDecimalPoint() throws Exception {
        assertInvalidNumber("5.");
    }

    @Test
    public void testParseInvalidLeadingDecimalPoint() throws Exception {
        assertInvalidNumber(".5");
    }

    @Test
    public void testParseInvalidDoubleDecimalPoint() throws Exception {
        assertInvalidNumber("0.0.0");
    }

    @Test
    public void testParseInvalidExponentOnly() throws Exception {
        assertInvalidNumber("e5");
    }

    @Test
    public void testParseInvalidExponentWithSignOnly() throws Exception {
        assertInvalidNumber("e+5");
    }

    @Test
    public void testParseInvalidExponentNoDigits() throws Exception {
        assertInvalidNumber("1e");
    }

    @Test
    public void testParseInvalidExponentWithSignNoDigits() throws Exception {
        assertInvalidNumber("1e+");
    }

    @Test
    public void testParseInvalidExponentWithDecimalNoDigits() throws Exception {
        assertInvalidNumber("1.e5");
    }

    @Test
    public void testParseInvalidLeadingZerosWithExponent() throws Exception {
        assertInvalidNumber("01e5");
    }

    @Test
    public void testParseInvalidLeadingZerosWithDecimalAndExponent() throws Exception {
        assertInvalidNumber("00.5e2");
    }

    @Test
    public void testParseInvalidNegativeZeroWithLeadingZero() throws Exception {
        assertInvalidNumber("-00");
    }

    @Test
    public void testParseInvalidAlphabetic() throws Exception {
        assertInvalidNumber("abc");
    }

    @Test
    public void testParseInvalidMixed() throws Exception {
        assertInvalidNumber("12a3");
    }

    // ==================== Negative zero ====================

    @Test
    public void testParseNegativeZero() throws Exception {
        JsonParser parser = parseNumber("-0");
        assertEquals(0, parser.getIntValue());
        assertEquals(0L, parser.getLongValue());
        assertEquals(-0.0, parser.getDoubleValue(), 0.0);
        assertEquals(BigDecimal.ZERO, parser.getDecimalValue());
    }

    @Test
    public void testParseNegativeZeroFloat() throws Exception {
        JsonParser parser = parseNumber("-0.0");
        assertEquals(-0.0, parser.getDoubleValue(), 0.0);
        assertEquals(BigDecimal.ZERO, parser.getDecimalValue());
    }

    // ==================== getValueAsInt with default ====================

    @Test
    public void testGetValueAsIntWithDefault() throws Exception {
        JsonParser parser = parseNumber("42");
        assertEquals(42, parser.getValueAsInt(0));
    }

    @Test
    public void testGetValueAsIntWithDefaultOnFloat() throws Exception {
        JsonParser parser = parseNumber("3.14");
        // Should return default because not an integer
        assertEquals(0, parser.getValueAsInt(0));
    }

    // ==================== Token type verification ====================

    @Test
    public void testTokenTypeInt() throws Exception {
        JsonParser parser = parseNumber("123");
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.getCurrentToken());
    }

    @Test
    public void testTokenTypeFloat() throws Exception {
        JsonParser parser = parseNumber("1.5");
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.getCurrentToken());
    }

    @Test
    public void testTokenTypeFloatWithExponent() throws Exception {
        JsonParser parser = parseNumber("1e2");
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.getCurrentToken());
    }

    // ==================== Edge cases for large numbers ====================

    @Test
    public void testParseVeryLargeLong() throws Exception {
        JsonParser parser = parseNumber("9223372036854775807"); // Long.MAX_VALUE
        assertEquals(Long.MAX_VALUE, parser.getLongValue());
    }

    @Test
    public void testParseVeryLargeDouble() throws Exception {
        JsonParser parser = parseNumber("1.7976931348623157E308"); // Double.MAX_VALUE
        assertEquals(Double.MAX_VALUE, parser.getDoubleValue(), 0.0);
    }

    @Test
    public void testParseVerySmallDouble() throws Exception {
        JsonParser parser = parseNumber("4.9E-324"); // Double.MIN_VALUE
        assertEquals(Double.MIN_VALUE, parser.getDoubleValue(), 0.0);
    }

    // ==================== BigDecimal precision ====================

    @Test
    public void testParseBigDecimalPrecision() throws Exception {
        JsonParser parser = parseNumber("0.12345678901234567890");
        BigDecimal expected = new BigDecimal("0.12345678901234567890");
        assertEquals(expected, parser.getDecimalValue());
    }

    @Test
    public void testParseBigDecimalLarge() throws Exception {
        JsonParser parser = parseNumber("123456789012345678901234567890.1234567890");
        BigDecimal expected = new BigDecimal("123456789012345678901234567890.1234567890");
        assertEquals(expected, parser.getDecimalValue());
    }
}