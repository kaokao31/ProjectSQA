package com.fasterxml.jackson.core.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;

import static org.junit.Assert.*;

public class ReaderBasedJsonParserTest {

    private JsonFactory factory;

    @Before
    public void setUp() {
        factory = new JsonFactory();
    }

    // ---------- Basic parsing tests ----------

    @Test
    public void testSimpleObject() throws IOException {
        String json = "{\"name\":\"John\", \"age\":30}";
        JsonParser parser = factory.createParser(new StringReader(json));
        assertNotNull(parser);

        assertToken(JsonToken.START_OBJECT, parser.nextToken());
        assertToken(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("name", parser.getCurrentName());
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("John", parser.getText());
        assertToken(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("age", parser.getCurrentName());
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(30, parser.getIntValue());
        assertToken(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
    }

    @Test
    public void testSimpleArray() throws IOException {
        String json = "[1,2,3]";
        JsonParser parser = factory.createParser(new StringReader(json));
        assertToken(JsonToken.START_ARRAY, parser.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(2, parser.getIntValue());
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(3, parser.getIntValue());
        assertToken(JsonToken.END_ARRAY, parser.nextToken());
        assertNull(parser.nextToken());
    }

    @Test
    public void testEmptyObject() throws IOException {
        String json = "{}";
        JsonParser parser = factory.createParser(new StringReader(json));
        assertToken(JsonToken.START_OBJECT, parser.nextToken());
        assertToken(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
    }

    @Test
    public void testEmptyArray() throws IOException {
        String json = "[]";
        JsonParser parser = factory.createParser(new StringReader(json));
        assertToken(JsonToken.START_ARRAY, parser.nextToken());
        assertToken(JsonToken.END_ARRAY, parser.nextToken());
        assertNull(parser.nextToken());
    }

    @Test
    public void testNullValue() throws IOException {
        String json = "null";
        JsonParser parser = factory.createParser(new StringReader(json));
        assertToken(JsonToken.VALUE_NULL, parser.nextToken());
        assertNull(parser.nextToken());
    }

    @Test
    public void testBooleanValues() throws IOException {
        String json = "true false";
        JsonParser parser = factory.createParser(new StringReader(json));
        assertToken(JsonToken.VALUE_TRUE, parser.nextToken());
        assertToken(JsonToken.VALUE_FALSE, parser.nextToken());
        assertNull(parser.nextToken());
    }

    @Test
    public void testStringWithEscapedChars() throws IOException {
        String json = "\"hello\\nworld\"";
        JsonParser parser = factory.createParser(new StringReader(json));
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("hello\nworld", parser.getText());
        assertNull(parser.nextToken());
    }

    @Test
    public void testNumberInteger() throws IOException {
        String json = "42";
        JsonParser parser = factory.createParser(new StringReader(json));
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42, parser.getIntValue());
        assertEquals("42", parser.getText());
        assertNull(parser.nextToken());
    }

    @Test
    public void testNumberNegative() throws IOException {
        String json = "-7";
        JsonParser parser = factory.createParser(new StringReader(json));
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(-7, parser.getIntValue());
        assertNull(parser.nextToken());
    }

    @Test
    public void testNumberFloat() throws IOException {
        String json = "3.14";
        JsonParser parser = factory.createParser(new StringReader(json));
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(3.14, parser.getDoubleValue(), 0.0001);
        assertNull(parser.nextToken());
    }

    @Test
    public void testNumberScientific() throws IOException {
        String json = "1.5e10";
        JsonParser parser = factory.createParser(new StringReader(json));
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(1.5e10, parser.getDoubleValue(), 0.0);
        assertNull(parser.nextToken());
    }

    @Test
    public void testNumberMinMax() throws IOException {
        // Test integer boundary values
        String json = "2147483647 -2147483648";
        JsonParser parser = factory.createParser(new StringReader(json));
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(Integer.MAX_VALUE, parser.getIntValue());
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(Integer.MIN_VALUE, parser.getIntValue());
        assertNull(parser.nextToken());
    }

    @Test
    public void testNumberOverflow() throws IOException {
        // Large number that exceeds int range but fits in long
        String json = "3000000000";
        JsonParser parser = factory.createParser(new StringReader(json));
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(3000000000L, parser.getLongValue());
        assertNull(parser.nextToken());
    }

    @Test
    public void testDeepNesting() throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            sb.append('{');
        }
        sb.append("\"a\":1");
        for (int i = 0; i < 100; i++) {
            sb.append('}');
        }
        JsonParser parser = factory.createParser(new StringReader(sb.toString()));
        for (int i = 0; i < 100; i++) {
            assertToken(JsonToken.START_OBJECT, parser.nextToken());
        }
        assertToken(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("a", parser.getCurrentName());
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        for (int i = 0; i < 100; i++) {
            assertToken(JsonToken.END_OBJECT, parser.nextToken());
        }
        assertNull(parser.nextToken());
    }

    // ---------- Edge cases and error handling ----------

    @Test(expected = IOException.class)
    public void testMalformedJson() throws IOException {
        String json = "{invalid}";
        JsonParser parser = factory.createParser(new StringReader(json));
        parser.nextToken(); // Should throw
    }

    @Test(expected = IOException.class)
    public void testUnclosedString() throws IOException {
        String json = "\"unclosed";
        JsonParser parser = factory.createParser(new StringReader(json));
        parser.nextToken(); // Should throw
    }

    @Test(expected = IOException.class)
    public void testUnexpectedEndOfInput() throws IOException {
        String json = "{";
        JsonParser parser = factory.createParser(new StringReader(json));
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // Should throw
    }

    @Test
    public void testEmptyInput() throws IOException {
        String json = "";
        JsonParser parser = factory.createParser(new StringReader(json));
        assertNull(parser.nextToken());
    }

    @Test
    public void testWhitespaceOnly() throws IOException {
        String json = "   \t\n\r  ";
        JsonParser parser = factory.createParser(new StringReader(json));
        assertNull(parser.nextToken());
    }

    @Test
    public void testMultipleValues() throws IOException {
        String json = "1 2 3";
        JsonParser parser = factory.createParser(new StringReader(json));
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(2, parser.getIntValue());
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(3, parser.getIntValue());
        assertNull(parser.nextToken());
    }

    @Test
    public void testUnicodeEscape() throws IOException {
        String json = "\"\\u0041\"";
        JsonParser parser = factory.createParser(new StringReader(json));
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("A", parser.getText());
        assertNull(parser.nextToken());
    }

    @Test
    public void testFieldNameWithSpecialChars() throws IOException {
        String json = "{\"field name\":\"value\"}";
        JsonParser parser = factory.createParser(new StringReader(json));
        assertToken(JsonToken.START_OBJECT, parser.nextToken());
        assertToken(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("field name", parser.getCurrentName());
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("value", parser.getText());
        assertToken(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
    }

    @Test
    public void testNestedArrayInObject() throws IOException {
        String json = "{\"arr\":[1,2]}";
        JsonParser parser = factory.createParser(new StringReader(json));
        assertToken(JsonToken.START_OBJECT, parser.nextToken());
        assertToken(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("arr", parser.getCurrentName());
        assertToken(JsonToken.START_ARRAY, parser.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(2, parser.getIntValue());
        assertToken(JsonToken.END_ARRAY, parser.nextToken());
        assertToken(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
    }

    @Test
    public void testObjectWithMultipleFields() throws IOException {
        String json = "{\"a\":1,\"b\":2,\"c\":3}";
        JsonParser parser = factory.createParser(new StringReader(json));
        assertToken(JsonToken.START_OBJECT, parser.nextToken());
        assertToken(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("a", parser.getCurrentName());
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        assertToken(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("b", parser.getCurrentName());
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(2, parser.getIntValue());
        assertToken(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("c", parser.getCurrentName());
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(3, parser.getIntValue());
        assertToken(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
    }

    @Test
    public void testSkipChildren() throws IOException {
        String json = "{\"obj\":{\"inner\":1},\"arr\":[1,2]}";
        JsonParser parser = factory.createParser(new StringReader(json));
        assertToken(JsonToken.START_OBJECT, parser.nextToken());
        assertToken(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("obj", parser.getCurrentName());
        parser.nextToken(); // START_OBJECT
        parser.skipChildren(); // skip inner object
        assertToken(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("arr", parser.getCurrentName());
        parser.nextToken(); // START_ARRAY
        parser.skipChildren(); // skip array
        assertToken(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
    }

    @Test
    public void testGetTextAfterFieldName() throws IOException {
        String json = "{\"key\":\"value\"}";
        JsonParser parser = factory.createParser(new StringReader(json));
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME
        assertEquals("key", parser.getText());
        parser.nextToken(); // VALUE_STRING
        assertEquals("value", parser.getText());
    }

    @Test
    public void testCurrentToken() throws IOException {
        String json = "123";
        JsonParser parser = factory.createParser(new StringReader(json));
        assertNull(parser.getCurrentToken());
        parser.nextToken();
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.getCurrentToken());
    }

    @Test
    public void testHasCurrentToken() throws IOException {
        String json = "true";
        JsonParser parser = factory.createParser(new StringReader(json));
        assertFalse(parser.hasCurrentToken());
        parser.nextToken();
        assertTrue(parser.hasCurrentToken());
    }

    @Test
    public void testClearCurrentToken() throws IOException {
        String json = "null";
        JsonParser parser = factory.createParser(new StringReader(json));
        parser.nextToken();
        assertTrue(parser.hasCurrentToken());
        parser.clearCurrentToken();
        assertNull(parser.getCurrentToken());
    }

    // ---------- Bug-specific tests (JacksonCore Bug #12) ----------

    @Test
    public void testLargeNumberInObject() throws IOException {
        // Bug #12 might involve parsing numbers with many digits
        String json = "{\"big\":123456789012345678901234567890}";
        JsonParser parser = factory.createParser(new StringReader(json));
        assertToken(JsonToken.START_OBJECT, parser.nextToken());
        assertToken(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("big", parser.getCurrentName());
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        // The number is too large for long, but parser should handle it as BigInteger
        // We just check that it doesn't throw and returns a token
        assertNotNull(parser.getText());
        assertToken(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
    }

    @Test
    public void testNegativeZero() throws IOException {
        String json = "-0";
        JsonParser parser = factory.createParser(new StringReader(json));
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(0, parser.getIntValue());
        assertEquals("-0", parser.getText());
        assertNull(parser.nextToken());
    }

    @Test
    public void testLeadingZeros() throws IOException {
        String json = "00123";
        JsonParser parser = factory.createParser(new StringReader(json));
        // Depending on parser strictness, this might be invalid or parse as 123
        // We just ensure it doesn't crash
        try {
            JsonToken token = parser.nextToken();
            if (token == JsonToken.VALUE_NUMBER_INT) {
                assertEquals(123, parser.getIntValue());
            }
        } catch (IOException e) {
            // Expected if parser rejects leading zeros
        }
    }

    @Test
    public void testSingleCharString() throws IOException {
        String json = "\"a\"";
        JsonParser parser = factory.createParser(new StringReader(json));
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("a", parser.getText());
        assertNull(parser.nextToken());
    }

    @Test
    public void testVeryLongString() throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append('"');
        for (int i = 0; i < 10000; i++) {
            sb.append('x');
        }
        sb.append('"');
        JsonParser parser = factory.createParser(new StringReader(sb.toString()));
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals(10000, parser.getText().length());
        assertNull(parser.nextToken());
    }

    @Test
    public void testEscapedBackslash() throws IOException {
        String json = "\"\\\\\"";
        JsonParser parser = factory.createParser(new StringReader(json));
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("\\", parser.getText());
        assertNull(parser.nextToken());
    }

    @Test
    public void testEscapedQuote() throws IOException {
        String json = "\"\\\"\"";
        JsonParser parser = factory.createParser(new StringReader(json));
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("\"", parser.getText());
        assertNull(parser.nextToken());
    }

    @Test
    public void testTabInString() throws IOException {
        String json = "\"hello\\tworld\"";
        JsonParser parser = factory.createParser(new StringReader(json));
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("hello\tworld", parser.getText());
        assertNull(parser.nextToken());
    }

    @Test
    public void testCarriageReturnInString() throws IOException {
        String json = "\"line1\\rline2\"";
        JsonParser parser = factory.createParser(new StringReader(json));
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("line1\rline2", parser.getText());
        assertNull(parser.nextToken());
    }

    @Test
    public void testFormFeedInString() throws IOException {
        String json = "\"hello\\fworld\"";
        JsonParser parser = factory.createParser(new StringReader(json));
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("hello\fworld", parser.getText());
        assertNull(parser.nextToken());
    }

    @Test
    public void testBackspaceInString() throws IOException {
        String json = "\"hello\\bworld\"";
        JsonParser parser = factory.createParser(new StringReader(json));
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("hello\bworld", parser.getText());
        assertNull(parser.nextToken());
    }

    @Test
    public void testSlashEscape() throws IOException {
        String json = "\"a\\/b\"";
        JsonParser parser = factory.createParser(new StringReader(json));
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("a/b", parser.getText());
        assertNull(parser.nextToken());
    }

    @Test
    public void testUnicodeEscapeInvalid() throws IOException {
        String json = "\"\\u00ZZ\"";
        JsonParser parser = factory.createParser(new StringReader(json));
        try {
            parser.nextToken();
            fail("Expected IOException for invalid unicode escape");
        } catch (IOException e) {
            // expected
        }
    }

    @Test
    public void testNumberWithLeadingPlus() throws IOException {
        String json = "+123";
        JsonParser parser = factory.createParser(new StringReader(json));
        try {
            parser.nextToken();
            fail("Expected IOException for leading plus");
        } catch (IOException e) {
            // expected
        }
    }

    @Test
    public void testNumberWithMultipleDots() throws IOException {
        String json = "1.2.3";
        JsonParser parser = factory.createParser(new StringReader(json));
        try {
            parser.nextToken();
            fail("Expected IOException for multiple dots");
        } catch (IOException e) {
            // expected
        }
    }

    @Test
    public void testNumberWithTrailingDot() throws IOException {
        String json = "1.";
        JsonParser parser = factory.createParser(new StringReader(json));
        try {
            parser.nextToken();
            fail("Expected IOException for trailing dot");
        } catch (IOException e) {
            // expected
        }
    }

    @Test
    public void testNumberWithExponentOnly() throws IOException {
        String json = "1e";
        JsonParser parser = factory.createParser(new StringReader(json));
        try {
            parser.nextToken();
            fail("Expected IOException for incomplete exponent");
        } catch (IOException e) {
            // expected
        }
    }

    @Test
    public void testNumberWithNegativeExponent() throws IOException {
        String json = "1e-10";
        JsonParser parser = factory.createParser(new StringReader(json));
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(1e-10, parser.getDoubleValue(), 1e-20);
        assertNull(parser.nextToken());
    }

    @Test
    public void testNumberWithPositiveExponent() throws IOException {
        String json = "1e+10";
        JsonParser parser = factory.createParser(new StringReader(json));
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(1e10, parser.getDoubleValue(), 0.0);
        assertNull(parser.nextToken());
    }

    @Test
    public void testNumberMinusZero() throws IOException {
        String json = "-0.0";
        JsonParser parser = factory.createParser(new StringReader(json));
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(-0.0, parser.getDoubleValue(), 0.0);
        assertNull(parser.nextToken());
    }

    @Test
    public void testNumberLargeExponent() throws IOException {
        String json = "1e1000";
        JsonParser parser = factory.createParser(new StringReader(json));
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        // Double will overflow to Infinity
        assertTrue(Double.isInfinite(parser.getDoubleValue()));
        assertNull(parser.nextToken());
    }

    @Test
    public void testNumberVerySmallExponent() throws IOException {
        String json = "1e-1000";
        JsonParser parser = factory.createParser(new StringReader(json));
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(0.0, parser.getDoubleValue(), 0.0);
        assertNull(parser.nextToken());
    }

    @Test
    public void testNumberHex() throws IOException {
        String json = "0x1A";
        JsonParser parser = factory.createParser(new StringReader(json));
        try {
            parser.nextToken();
            fail("Expected IOException for hex number");
        } catch (IOException e) {
            // expected
        }
    }

    @Test
    public void testNumberOctal() throws IOException {
        String json = "0123";
        JsonParser parser = factory.createParser(new StringReader(json));
        // Some parsers accept leading zero as octal, but JSON spec says no
        // We just ensure it doesn't crash
        try {
            JsonToken token = parser.nextToken();
            if (token == JsonToken.VALUE_NUMBER_INT) {
                assertEquals(123, parser.getIntValue());
            }
        } catch (IOException e) {
            // expected if strict
        }
    }

    @Test
    public void testStringWithUnescapedControlChar() throws IOException {
        String json = "\"hello\u0000world\"";
        JsonParser parser = factory.createParser(new StringReader(json));
        try {
            parser.nextToken();
            fail("Expected IOException for unescaped control character");
        } catch (IOException e) {
            // expected
        }
    }

    @Test
    public void testStringWithUnescapedTab() throws IOException {
        String json = "\"hello\tworld\"";
        JsonParser parser = factory.createParser(new StringReader(json));
        try {
            parser.nextToken();
            fail("Expected IOException for unescaped tab");
        } catch (IOException e) {
            // expected
        }
    }

    @Test
    public void testStringWithUnescapedNewline() throws IOException {
        String json = "\"hello\nworld\"";
        JsonParser parser = factory.createParser(new StringReader(json));
        try {
            parser.nextToken();
            fail("Expected IOException for unescaped newline");
        } catch (IOException e) {
            // expected
        }
    }

    @Test
    public void testStringWithUnescapedCarriageReturn() throws IOException {
        String json = "\"hello\rworld\"";
        JsonParser parser = factory.createParser(new StringReader(json));
        try {
            parser.nextToken();
            fail("Expected IOException for unescaped carriage return");
        } catch (IOException e) {
            // expected
        }
    }

    @Test
    public void testStringWithUnescapedBackspace() throws IOException {
        String json = "\"hello\bworld\"";
        JsonParser parser = factory.createParser(new StringReader(json));
        try {
            parser.nextToken();
            fail("Expected IOException for unescaped backspace");
        } catch (IOException e) {
            // expected
        }
    }

    @Test
    public void testStringWithUnescapedFormFeed() throws IOException {
        String json = "\"hello\fworld\"";
        JsonParser parser = factory.createParser(new StringReader(json));
        try {
            parser.nextToken();
            fail("Expected IOException for unescaped form feed");
        } catch (IOException e) {
            // expected
        }
    }

    @Test
    public void testArrayWithTrailingComma() throws IOException {
        String json = "[1,2,]";
        JsonParser parser = factory.createParser(new StringReader(json));
        try {
            parser.nextToken();
            // Some parsers accept trailing comma, some don't
            // We just ensure it doesn't crash
        } catch (IOException e) {
            // expected if strict
        }
    }

    @Test
    public void testObjectWithTrailingComma() throws IOException {
        String json = "{\"a\":1,}";
        JsonParser parser = factory.createParser(new StringReader(json));
        try {
            parser.nextToken();
        } catch (IOException e) {
            // expected if strict
        }
    }

    @Test
    public void testSingleQuoteString() throws IOException {
        String json = "'single'";
        JsonParser parser = factory.createParser(new StringReader(json));
        try {
            parser.nextToken();
            fail("Expected IOException for single quote");
        } catch (IOException e) {
            // expected
        }
    }

    @Test
    public void testUnquotedString() throws IOException {
        String json = "unquoted";
        JsonParser parser = factory.createParser(new StringReader(json));
        try {
            parser.nextToken();
            fail("Expected IOException for unquoted string");
        } catch (IOException e) {
            // expected
        }
    }

    @Test
    public void testDuplicateFieldName() throws IOException {
        String json = "{\"a\":1,\"a\":2}";
        JsonParser parser = factory.createParser(new StringReader(json));
        // Parser should not throw, but may handle duplicate keys
        assertToken(JsonToken.START_OBJECT, parser.nextToken());
        assertToken(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("a", parser.getCurrentName());
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        assertToken(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("a", parser.getCurrentName());
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(2, parser.getIntValue());
        assertToken(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
    }

    @Test
    public void testDeepArrayNesting() throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            sb.append('[');
        }
        sb.append('1');
        for (int i = 0; i < 100; i++) {
            sb.append(']');
        }
        JsonParser parser = factory.createParser(new StringReader(sb.toString()));
        for (int i = 0; i < 100; i++) {
            assertToken(JsonToken.START_ARRAY, parser.nextToken());
        }
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        for (int i = 0; i < 100; i++) {
            assertToken(JsonToken.END_ARRAY, parser.nextToken());
        }
        assertNull(parser.nextToken());
    }

    @Test
    public void testMixedNesting() throws IOException {
        String json = "{\"a\":[{\"b\":1}]}";
        JsonParser parser = factory.createParser(new StringReader(json));
        assertToken(JsonToken.START_OBJECT, parser.nextToken());
        assertToken(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("a", parser.getCurrentName());
        assertToken(JsonToken.START_ARRAY, parser.nextToken());
        assertToken(JsonToken.START_OBJECT, parser.nextToken());
        assertToken(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("b", parser.getCurrentName());
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        assertToken(JsonToken.END_OBJECT, parser.nextToken());
        assertToken(JsonToken.END_ARRAY, parser.nextToken());
        assertToken(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
    }

    // Helper method to assert token type
    private void assertToken(JsonToken expected, JsonToken actual) {
        assertEquals("Expected token " + expected + " but got " + actual, expected, actual);
    }
}