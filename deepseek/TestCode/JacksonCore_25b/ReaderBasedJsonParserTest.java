package com.fasterxml.jackson.core.json;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.JsonParser.NumberType;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.Assert.*;

public class ReaderBasedJsonParserTest {

    private JsonFactory factory;

    @Before
    public void setUp() {
        factory = new JsonFactory();
        // Ensure standard configuration without any lenient features
        factory.configure(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS, false);
        factory.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, false);
        factory.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, false);
    }

    // --------------------------------------------------------
    // Helper methods
    // --------------------------------------------------------
    private JsonParser createParser(String json) throws IOException {
        return factory.createParser(new StringReader(json));
    }

    private void assertToken(JsonToken expected, JsonToken actual) {
        assertEquals("Unexpected token", expected, actual);
    }

    // --------------------------------------------------------
    // Basic parsing tests
    // --------------------------------------------------------
    @Test
    public void testSimpleString() throws IOException {
        JsonParser p = createParser("\"hello\"");
        assertToken(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("hello", p.getText());
        assertNull(p.nextToken());
    }

    @Test
    public void testEmptyString() throws IOException {
        JsonParser p = createParser("\"\"");
        assertToken(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("", p.getText());
        assertNull(p.nextToken());
    }

    @Test
    public void testSimpleNumberInteger() throws IOException {
        JsonParser p = createParser("42");
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(42, p.getIntValue());
        assertEquals(42L, p.getLongValue());
        assertEquals(BigInteger.valueOf(42), p.getBigIntegerValue());
        assertEquals(JsonParser.NumberType.INT, p.getNumberType());
    }

    @Test
    public void testSimpleNumberLong() throws IOException {
        // Value that fits in long but exceeds int range
        String json = String.valueOf((long) Integer.MAX_VALUE + 1);
        JsonParser p = createParser(json);
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(2147483648L, p.getLongValue());
        assertEquals(JsonParser.NumberType.LONG, p.getNumberType());
    }

    @Test
    public void testSimpleNumberBigInteger() throws IOException {
        // Value that exceeds long range
        String json = "123456789012345678901234567890";
        JsonParser p = createParser(json);
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(new BigInteger(json), p.getBigIntegerValue());
        assertEquals(JsonParser.NumberType.BIG_INTEGER, p.getNumberType());
    }

    @Test
    public void testSimpleNumberFloat() throws IOException {
        JsonParser p = createParser("3.14");
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(3.14, p.getDoubleValue(), 1e-9);
        assertEquals(JsonParser.NumberType.DOUBLE, p.getNumberType());
    }

    @Test
    public void testSimpleNumberDecimal() throws IOException {
        String json = "0.0000000000000000000000001";
        JsonParser p = createParser(json);
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(new BigDecimal(json), p.getDecimalValue());
    }

    @Test
    public void testNegativeNumber() throws IOException {
        JsonParser p = createParser("-100");
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(-100, p.getIntValue());
    }

    @Test
    public void testNegativeFloat() throws IOException {
        JsonParser p = createParser("-3.14");
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(-3.14, p.getDoubleValue(), 1e-9);
    }

    @Test
    public void testExponentNumber() throws IOException {
        JsonParser p = createParser("1e10");
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(1e10, p.getDoubleValue(), 1e-5);
    }

    @Test
    public void testExponentNegative() throws IOException {
        JsonParser p = createParser("1.5e-3");
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(0.0015, p.getDoubleValue(), 1e-9);
    }

    @Test
    public void testExponentWithPlus() throws IOException {
        JsonParser p = createParser("2E+5");
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(200000.0, p.getDoubleValue(), 0);
    }

    @Test
    public void testNull() throws IOException {
        JsonParser p = createParser("null");
        assertToken(JsonToken.VALUE_NULL, p.nextToken());
        assertNull(p.getText());
    }

    @Test
    public void testTrue() throws IOException {
        JsonParser p = createParser("true");
        assertToken(JsonToken.VALUE_TRUE, p.nextToken());
        assertTrue(p.getBooleanValue());
    }

    @Test
    public void testFalse() throws IOException {
        JsonParser p = createParser("false");
        assertToken(JsonToken.VALUE_FALSE, p.nextToken());
        assertFalse(p.getBooleanValue());
    }

    // --------------------------------------------------------
    // Array tests
    // --------------------------------------------------------
    @Test
    public void testEmptyArray() throws IOException {
        JsonParser p = createParser("[]");
        assertToken(JsonToken.START_ARRAY, p.nextToken());
        assertToken(JsonToken.END_ARRAY, p.nextToken());
        assertNull(p.nextToken());
    }

    @Test
    public void testSimpleArray() throws IOException {
        JsonParser p = createParser("[1, \"two\", true]");
        assertToken(JsonToken.START_ARRAY, p.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
        assertToken(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("two", p.getText());
        assertToken(JsonToken.VALUE_TRUE, p.nextToken());
        assertToken(JsonToken.END_ARRAY, p.nextToken());
        assertNull(p.nextToken());
    }

    @Test
    public void testNestedArrays() throws IOException {
        JsonParser p = createParser("[[1, 2], [3]]");
        assertToken(JsonToken.START_ARRAY, p.nextToken());
        assertToken(JsonToken.START_ARRAY, p.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(2, p.getIntValue());
        assertToken(JsonToken.END_ARRAY, p.nextToken());
        assertToken(JsonToken.START_ARRAY, p.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(3, p.getIntValue());
        assertToken(JsonToken.END_ARRAY, p.nextToken());
        assertToken(JsonToken.END_ARRAY, p.nextToken());
        assertNull(p.nextToken());
    }

    // --------------------------------------------------------
    // Object tests
    // --------------------------------------------------------
    @Test
    public void testEmptyObject() throws IOException {
        JsonParser p = createParser("{}");
        assertToken(JsonToken.START_OBJECT, p.nextToken());
        assertToken(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());
    }

    @Test
    public void testSimpleObject() throws IOException {
        JsonParser p = createParser("{\"name\": \"John\", \"age\": 30}");
        assertToken(JsonToken.START_OBJECT, p.nextToken());
        assertToken(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("name", p.getCurrentName());
        assertToken(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("John", p.getText());
        assertToken(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("age", p.getCurrentName());
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(30, p.getIntValue());
        assertToken(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());
    }

    @Test
    public void testNestedObjects() throws IOException {
        JsonParser p = createParser("{\"outer\": {\"inner\": 42}}");
        assertToken(JsonToken.START_OBJECT, p.nextToken());
        assertToken(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("outer", p.getCurrentName());
        assertToken(JsonToken.START_OBJECT, p.nextToken());
        assertToken(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("inner", p.getCurrentName());
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(42, p.getIntValue());
        assertToken(JsonToken.END_OBJECT, p.nextToken());
        assertToken(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());
    }

    // --------------------------------------------------------
    // Edge cases for number parsing (targeting bug 25)
    // --------------------------------------------------------
    @Test(expected = JsonParseException.class)
    public void testLeadingZerosInt() throws IOException {
        // Leading zeros in integer part should be invalid
        JsonParser p = createParser("001");
        p.nextToken();
    }

    @Test(expected = JsonParseException.class)
    public void testLeadingZerosNegative() throws IOException {
        JsonParser p = createParser("-001");
        p.nextToken();
    }

    @Test
    public void testZeroOnly() throws IOException {
        JsonParser p = createParser("0");
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(0, p.getIntValue());
    }

    @Test
    public void testNegativeZero() throws IOException {
        JsonParser p = createParser("-0");
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(0, p.getIntValue());
    }

    @Test
    public void testDecimalWithTrailingZeros() throws IOException {
        JsonParser p = createParser("1.000");
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(1.0, p.getDoubleValue(), 0);
    }

    @Test
    public void testManyDecimalPlaces() throws IOException {
        // Potential bug: very long decimal could cause slow parsing or overflow
        String json = "0." + repeat("0", 9999) + "1";
        JsonParser p = createParser(json);
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        BigDecimal expected = new BigDecimal(json);
        assertEquals(expected, p.getDecimalValue());
    }

    @Test
    public void testLargeExponent() throws IOException {
        // Number with large exponent
        String json = "1e1000";
        JsonParser p = createParser(json);
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        // Should parse as BigDecimal or double infinity? double overflow
        // We'll just check it's a float and get decimal value without exception
        BigDecimal value = p.getDecimalValue();
        assertNotNull(value);
    }

    @Test
    public void testNegativeLargeExponent() throws IOException {
        String json = "1e-1000";
        JsonParser p = createParser(json);
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        BigDecimal value = p.getDecimalValue();
        assertNotNull(value);
        assertEquals(0, value.compareTo(BigDecimal.ZERO));
    }

    @Test
    public void testNumberWithLeadingPlus() throws IOException {
        // Plus sign not allowed by default
        JsonParser p = createParser("+42");
        try {
            p.nextToken();
            fail("Expected JsonParseException for leading plus");
        } catch (JsonParseException e) {
            // expected
        }
    }

    // --------------------------------------------------------
    // Mixed content and boundary
    // --------------------------------------------------------
    @Test
    public void testMultipleTokens() throws IOException {
        JsonParser p = createParser("  null  42  \"end\"  ");
        assertToken(JsonToken.VALUE_NULL, p.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(42, p.getIntValue());
        assertToken(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("end", p.getText());
        assertNull(p.nextToken());
    }

    @Test
    public void testWhitespaceHandling() throws IOException {
        JsonParser p = createParser("  \n\r\t  \"spaced\"  ");
        assertToken(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("spaced", p.getText());
    }

    @Test
    public void testStreamClose() throws IOException {
        JsonParser p = createParser("[1,2]");
        p.close();
        // After close, nextToken should return null
        assertNull(p.nextToken());
    }

    // --------------------------------------------------------
    // Error cases
    // --------------------------------------------------------
    @Test(expected = JsonParseException.class)
    public void testMalformedJsonUnexpectedChar() throws IOException {
        JsonParser p = createParser("{invalid}");
        while (p.nextToken() != null) ;
    }

    @Test(expected = JsonParseException.class)
    public void testUnclosedString() throws IOException {
        JsonParser p = createParser("\"no end");
        p.nextToken();
    }

    @Test(expected = JsonParseException.class)
    public void testSingleQuoteNotAllowed() throws IOException {
        JsonParser p = createParser("{'key': 'value'}");
        p.nextToken();
    }

    @Test(expected = JsonParseException.class)
    public void testUnquotedFieldNameNotAllowed() throws IOException {
        JsonParser p = createParser("{key: 1}");
        p.nextToken();
    }

    @Test(expected = JsonParseException.class)
    public void testTrailingCommaInObject() throws IOException {
        JsonParser p = createParser("{\"a\":1,}");
        p.nextToken();
    }

    @Test(expected = JsonParseException.class)
    public void testTrailingCommaInArray() throws IOException {
        JsonParser p = createParser("[1,]");
        p.nextToken();
    }

    // --------------------------------------------------------
    // Helper to repeat string
    // --------------------------------------------------------
    private String repeat(String s, int count) {
        StringBuilder sb = new StringBuilder(s.length() * count);
        for (int i = 0; i < count; i++) {
            sb.append(s);
        }
        return sb.toString();
    }
}