package com.fasterxml.jackson.core.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonToken;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;

import static org.junit.Assert.*;

/**
 * Comprehensive JUnit 4 test suite for ReaderBasedJsonParser.
 * Designed to achieve high line/branch coverage and detect potential faults.
 */
public class ReaderBasedJsonParserTest {

    private JsonFactory factory;

    @Before
    public void setUp() {
        factory = new JsonFactory();
    }

    // ==================== Basic Token Parsing ====================

    @Test
    public void testEmptyInput() throws IOException {
        ReaderBasedJsonParser parser = (ReaderBasedJsonParser) factory.createParser(new StringReader(""));
        assertNull("No token should be available for empty input", parser.nextToken());
    }

    @Test
    public void testWhitespaceOnly() throws IOException {
        ReaderBasedJsonParser parser = (ReaderBasedJsonParser) factory.createParser(new StringReader("   \n\t\r  "));
        assertNull("Whitespace-only input should yield no token", parser.nextToken());
    }

    @Test
    public void testSimpleString() throws IOException {
        ReaderBasedJsonParser parser = (ReaderBasedJsonParser) factory.createParser(new StringReader("\"hello\""));
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("hello", parser.getText());
    }

    @Test
    public void testSimpleNumberInteger() throws IOException {
        ReaderBasedJsonParser parser = (ReaderBasedJsonParser) factory.createParser(new StringReader("42"));
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42, parser.getIntValue());
    }

    @Test
    public void testSimpleNumberFloat() throws IOException {
        ReaderBasedJsonParser parser = (ReaderBasedJsonParser) factory.createParser(new StringReader("3.14"));
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(3.14, parser.getDoubleValue(), 1e-9);
    }

    @Test
    public void testBooleanTrue() throws IOException {
        ReaderBasedJsonParser parser = (ReaderBasedJsonParser) factory.createParser(new StringReader("true"));
        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
    }

    @Test
    public void testBooleanFalse() throws IOException {
        ReaderBasedJsonParser parser = (ReaderBasedJsonParser) factory.createParser(new StringReader("false"));
        assertEquals(JsonToken.VALUE_FALSE, parser.nextToken());
    }

    @Test
    public void testNullLiteral() throws IOException {
        ReaderBasedJsonParser parser = (ReaderBasedJsonParser) factory.createParser(new StringReader("null"));
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
    }

    // ==================== Object Parsing ====================

    @Test
    public void testEmptyObject() throws IOException {
        ReaderBasedJsonParser parser = (ReaderBasedJsonParser) factory.createParser(new StringReader("{}"));
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
    }

    @Test
    public void testSimpleObject() throws IOException {
        ReaderBasedJsonParser parser = (ReaderBasedJsonParser) factory.createParser(new StringReader("{\"a\":1}"));
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("a", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
    }

    @Test
    public void testNestedObject() throws IOException {
        ReaderBasedJsonParser parser = (ReaderBasedJsonParser) factory.createParser(new StringReader("{\"outer\":{\"inner\":\"value\"}}"));
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("outer", parser.getCurrentName());
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("inner", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("value", parser.getText());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
    }

    // ==================== Array Parsing ====================

    @Test
    public void testEmptyArray() throws IOException {
        ReaderBasedJsonParser parser = (ReaderBasedJsonParser) factory.createParser(new StringReader("[]"));
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
    }

    @Test
    public void testSimpleArray() throws IOException {
        ReaderBasedJsonParser parser = (ReaderBasedJsonParser) factory.createParser(new StringReader("[1,2,3]"));
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(2, parser.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(3, parser.getIntValue());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
    }

    @Test
    public void testMixedArray() throws IOException {
        ReaderBasedJsonParser parser = (ReaderBasedJsonParser) factory.createParser(new StringReader("[true, null, \"text\", 3.14]"));
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("text", parser.getText());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(3.14, parser.getDoubleValue(), 1e-9);
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
    }

    // ==================== String Edge Cases ====================

    @Test
    public void testStringWithEscapedCharacters() throws IOException {
        ReaderBasedJsonParser parser = (ReaderBasedJsonParser) factory.createParser(new StringReader("\"\\\"\\\\\\/\\b\\f\\n\\r\\t\""));
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("\"\\/\b\f\n\r\t", parser.getText());
    }

    @Test
    public void testStringWithUnicodeEscape() throws IOException {
        ReaderBasedJsonParser parser = (ReaderBasedJsonParser) factory.createParser(new StringReader("\"\\u0041\""));
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("A", parser.getText());
    }

    @Test
    public void testStringWithSurrogatePair() throws IOException {
        ReaderBasedJsonParser parser = (ReaderBasedJsonParser) factory.createParser(new StringReader("\"\\uD83D\\uDE00\""));
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("\uD83D\uDE00", parser.getText());
    }

    @Test
    public void testEmptyString() throws IOException {
        ReaderBasedJsonParser parser = (ReaderBasedJsonParser) factory.createParser(new StringReader("\"\""));
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("", parser.getText());
    }

    // ==================== Number Edge Cases ====================

    @Test
    public void testNegativeNumber() throws IOException {
        ReaderBasedJsonParser parser = (ReaderBasedJsonParser) factory.createParser(new StringReader("-123"));
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(-123, parser.getIntValue());
    }

    @Test
    public void testNumberWithLeadingZero() throws IOException {
        // Leading zero is not allowed in JSON (except for 0 itself)
        try {
            ReaderBasedJsonParser parser = (ReaderBasedJsonParser) factory.createParser(new StringReader("0123"));
            parser.nextToken();
            fail("Expected JsonParseException for leading zero");
        } catch (JsonParseException e) {
            // expected
        }
    }

    @Test
    public void testNumberWithExponent() throws IOException {
        ReaderBasedJsonParser parser = (ReaderBasedJsonParser) factory.createParser(new StringReader("1.5e10"));
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(1.5e10, parser.getDoubleValue(), 1e-5);
    }

    @Test
    public void testNumberNegativeExponent() throws IOException {
        ReaderBasedJsonParser parser = (ReaderBasedJsonParser) factory.createParser(new StringReader("2.5e-3"));
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(2.5e-3, parser.getDoubleValue(), 1e-9);
    }

    @Test
    public void testLargeInteger() throws IOException {
        ReaderBasedJsonParser parser = (ReaderBasedJsonParser) factory.createParser(new StringReader("1234567890123456789"));
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        // Use getLongValue() for large numbers
        assertEquals(1234567890123456789L, parser.getLongValue());
    }

    @Test
    public void testMinMaxInteger() throws IOException {
        ReaderBasedJsonParser parser = (ReaderBasedJsonParser) factory.createParser(new StringReader(String.valueOf(Integer.MAX_VALUE)));
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(Integer.MAX_VALUE, parser.getIntValue());

        parser = (ReaderBasedJsonParser) factory.createParser(new StringReader(String.valueOf(Integer.MIN_VALUE)));
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(Integer.MIN_VALUE, parser.getIntValue());
    }

    // ==================== Error Handling ====================

    @Test(expected = JsonParseException.class)
    public void testInvalidJsonTruncated() throws IOException {
        ReaderBasedJsonParser parser = (ReaderBasedJsonParser) factory.createParser(new StringReader("{\"a\":"));
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME
        parser.nextToken(); // should throw because value is missing
    }

    @Test(expected = JsonParseException.class)
    public void testInvalidJsonUnexpectedToken() throws IOException {
        ReaderBasedJsonParser parser = (ReaderBasedJsonParser) factory.createParser(new StringReader("{]"));
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // should throw because ']' is not valid
    }

    @Test(expected = JsonParseException.class)
    public void testInvalidJsonUnclosedString() throws IOException {
        ReaderBasedJsonParser parser = (ReaderBasedJsonParser) factory.createParser(new StringReader("\"unclosed"));
        parser.nextToken(); // should throw
    }

    @Test(expected = JsonParseException.class)
    public void testInvalidJsonIllegalEscape() throws IOException {
        ReaderBasedJsonParser parser = (ReaderBasedJsonParser) factory.createParser(new StringReader("\"\\x\""));
        parser.nextToken(); // should throw
    }

    @Test(expected = JsonParseException.class)
    public void testInvalidJsonMultipleRoots() throws IOException {
        ReaderBasedJsonParser parser = (ReaderBasedJsonParser) factory.createParser(new StringReader("1 2"));
        parser.nextToken(); // first number
        parser.nextToken(); // should throw because second root is not allowed
    }

    // ==================== Complex Structures ====================

    @Test
    public void testDeepNesting() throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (int i = 0; i < 100; i++) {
            sb.append("\"k").append(i).append("\":{");
        }
        sb.append("\"end\":1");
        for (int i = 0; i < 100; i++) {
            sb.append("}");
        }
        ReaderBasedJsonParser parser = (ReaderBasedJsonParser) factory.createParser(new StringReader(sb.toString()));
        // Just ensure it parses without exception
        while (parser.nextToken() != null) {
            // consume all tokens
        }
    }

    @Test
    public void testLargeArray() throws IOException {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < 1000; i++) {
            if (i > 0) sb.append(",");
            sb.append(i);
        }
        sb.append("]");
        ReaderBasedJsonParser parser = (ReaderBasedJsonParser) factory.createParser(new StringReader(sb.toString()));
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        int count = 0;
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            assertEquals(count, parser.getIntValue());
            count++;
        }
        assertEquals(1000, count);
    }

    // ==================== Token Navigation ====================

    @Test
    public void testSkipChildren() throws IOException {
        ReaderBasedJsonParser parser = (ReaderBasedJsonParser) factory.createParser(new StringReader("{\"a\":[1,2],\"b\":\"c\"}"));
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("a", parser.getCurrentName());
        // skip the array
        parser.nextToken(); // START_ARRAY
        parser.skipChildren(); // should skip to END_ARRAY
        assertEquals(JsonToken.END_ARRAY, parser.getCurrentToken());
        // next token should be next field name
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("b", parser.getCurrentName());
    }

    @Test
    public void testGetCurrentToken() throws IOException {
        ReaderBasedJsonParser parser = (ReaderBasedJsonParser) factory.createParser(new StringReader("123"));
        assertNull(parser.getCurrentToken());
        parser.nextToken();
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.getCurrentToken());
    }

    // ==================== Fault Detection (Bug 19 related) ====================

    @Test
    public void testNumberWithLeadingZeroInArray() throws IOException {
        // This may trigger a bug if parser incorrectly accepts leading zeros in certain contexts
        try {
            ReaderBasedJsonParser parser = (ReaderBasedJsonParser) factory.createParser(new StringReader("[0123]"));
            parser.nextToken(); // START_ARRAY
            parser.nextToken(); // should throw or return number? According to spec, should throw.
            fail("Expected JsonParseException for leading zero in array");
        } catch (JsonParseException e) {
            // expected
        }
    }

    @Test
    public void testStringWithControlCharacter() throws IOException {
        // Control characters (U+0000 through U+001F) must be escaped in JSON strings
        try {
            ReaderBasedJsonParser parser = (ReaderBasedJsonParser) factory.createParser(new StringReader("\"\u0000\""));
            parser.nextToken();
            fail("Expected JsonParseException for unescaped control character");
        } catch (JsonParseException e) {
            // expected
        }
    }

    @Test
    public void testVeryLongString() throws IOException {
        // Stress test for internal buffer handling
        StringBuilder sb = new StringBuilder("\"");
        for (int i = 0; i < 10000; i++) {
            sb.append("a");
        }
        sb.append("\"");
        ReaderBasedJsonParser parser = (ReaderBasedJsonParser) factory.createParser(new StringReader(sb.toString()));
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals(10000, parser.getText().length());
    }

    @Test
    public void testNumberWithMultipleDots() throws IOException {
        try {
            ReaderBasedJsonParser parser = (ReaderBasedJsonParser) factory.createParser(new StringReader("1.2.3"));
            parser.nextToken();
            fail("Expected JsonParseException for number with multiple dots");
        } catch (JsonParseException e) {
            // expected
        }
    }

    @Test
    public void testNegativeZero() throws IOException {
        ReaderBasedJsonParser parser = (ReaderBasedJsonParser) factory.createParser(new StringReader("-0"));
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(0, parser.getIntValue());
        // Ensure it's negative zero? In JSON, -0 is allowed and should be treated as 0.
    }

    @Test
    public void testNumberWithExponentAndDot() throws IOException {
        ReaderBasedJsonParser parser = (ReaderBasedJsonParser) factory.createParser(new StringReader("1.e10"));
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(1.0e10, parser.getDoubleValue(), 1e-5);
    }

    @Test
    public void testNumberStartingWithDot() throws IOException {
        try {
            ReaderBasedJsonParser parser = (ReaderBasedJsonParser) factory.createParser(new StringReader(".5"));
            parser.nextToken();
            fail("Expected JsonParseException for number starting with dot");
        } catch (JsonParseException e) {
            // expected
        }
    }

    @Test
    public void testNumberEndingWithDot() throws IOException {
        try {
            ReaderBasedJsonParser parser = (ReaderBasedJsonParser) factory.createParser(new StringReader("5."));
            parser.nextToken();
            fail("Expected JsonParseException for number ending with dot");
        } catch (JsonParseException e) {
            // expected
        }
    }

    @Test
    public void testEmptyFieldName() throws IOException {
        // Empty field names are allowed in JSON
        ReaderBasedJsonParser parser = (ReaderBasedJsonParser) factory.createParser(new StringReader("{\"\":1}"));
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
    }

    @Test
    public void testDuplicateFieldNames() throws IOException {
        // Duplicate field names are allowed by parser (not rejected)
        ReaderBasedJsonParser parser = (ReaderBasedJsonParser) factory.createParser(new StringReader("{\"a\":1,\"a\":2}"));
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("a", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("a", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(2, parser.getIntValue());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
    }

    // ==================== Additional Coverage ====================

    @Test
    public void testGetTextAfterEndOfInput() throws IOException {
        ReaderBasedJsonParser parser = (ReaderBasedJsonParser) factory.createParser(new StringReader("true"));
        parser.nextToken(); // VALUE_TRUE
        parser.nextToken(); // null
        // getText() should return null or empty? Typically returns null.
        assertNull(parser.getText());
    }

    @Test
    public void testGetIntValueOnNonNumber() throws IOException {
        ReaderBasedJsonParser parser = (ReaderBasedJsonParser) factory.createParser(new StringReader("\"text\""));
        parser.nextToken();
        try {
            parser.getIntValue();
            fail("Expected exception when calling getIntValue on string token");
        } catch (Exception e) {
            // expected (JsonParseException or similar)
        }
    }

    @Test
    public void testGetDoubleValueOnNonNumber() throws IOException {
        ReaderBasedJsonParser parser = (ReaderBasedJsonParser) factory.createParser(new StringReader("false"));
        parser.nextToken();
        try {
            parser.getDoubleValue();
            fail("Expected exception when calling getDoubleValue on boolean token");
        } catch (Exception e) {
            // expected
        }
    }

    @Test
    public void testGetCurrentNameOnNonField() throws IOException {
        ReaderBasedJsonParser parser = (ReaderBasedJsonParser) factory.createParser(new StringReader("[1]"));
        parser.nextToken(); // START_ARRAY
        assertNull(parser.getCurrentName());
        parser.nextToken(); // VALUE_NUMBER_INT
        assertNull(parser.getCurrentName());
    }

    @Test
    public void testHasCurrentToken() throws IOException {
        ReaderBasedJsonParser parser = (ReaderBasedJsonParser) factory.createParser(new StringReader("{}"));
        assertFalse(parser.hasCurrentToken());
        parser.nextToken();
        assertTrue(parser.hasCurrentToken());
        parser.nextToken();
        assertTrue(parser.hasCurrentToken());
        parser.nextToken();
        assertFalse(parser.hasCurrentToken());
    }

    @Test
    public void testClearCurrentToken() throws IOException {
        ReaderBasedJsonParser parser = (ReaderBasedJsonParser) factory.createParser(new StringReader("1"));
        parser.nextToken();
        assertTrue(parser.hasCurrentToken());
        parser.clearCurrentToken();
        assertFalse(parser.hasCurrentToken());
    }

    @Test
    public void testGetTokenLocation() throws IOException {
        ReaderBasedJsonParser parser = (ReaderBasedJsonParser) factory.createParser(new StringReader("{\n\"key\":\n123}"));
        parser.nextToken(); // START_OBJECT at line 1 col 1
        assertEquals(1, parser.getTokenLocation().getLineNr());
        assertEquals(1, parser.getTokenLocation().getColumnNr());
        parser.nextToken(); // FIELD_NAME at line 2 col 1
        assertEquals(2, parser.getTokenLocation().getLineNr());
        assertEquals(1, parser.getTokenLocation().getColumnNr());
    }

    @Test
    public void testGetCurrentLocation() throws IOException {
        ReaderBasedJsonParser parser = (ReaderBasedJsonParser) factory.createParser(new StringReader("123"));
        parser.nextToken();
        // current location should be after the token
        assertNotNull(parser.getCurrentLocation());
    }

    @Test
    public void testClose() throws IOException {
        ReaderBasedJsonParser parser = (ReaderBasedJsonParser) factory.createParser(new StringReader("{}"));
        parser.close();
        // After close, nextToken should return null
        assertNull(parser.nextToken());
    }

    @Test
    public void testVersion() {
        ReaderBasedJsonParser parser = null;
        try {
            parser = (ReaderBasedJsonParser) factory.createParser(new StringReader("1"));
        } catch (IOException e) {
            fail("Unexpected exception");
        }
        assertNotNull(parser.version());
    }
}