package com.fasterxml.jackson.core.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.junit.Assert.*;

public class UTF8StreamJsonParserTest {

    private JsonFactory factory;

    @Before
    public void setUp() {
        factory = new JsonFactory();
    }

    private JsonParser parse(String json) throws Exception {
        return factory.createParser(json.getBytes("UTF-8"));
    }

    private JsonParser parse(byte[] json) throws Exception {
        return factory.createParser(json);
    }

    private JsonParser parseInputStream(String json) throws Exception {
        return factory.createParser(new ByteArrayInputStream(json.getBytes("UTF-8")));
    }

    private void assertToken(JsonToken expected, JsonToken actual) {
        assertEquals("Unexpected token", expected, actual);
    }

    // Basic object parsing with field names and values
    @Test
    public void testSimpleObject() throws Exception {
        String json = "{\"a\":1,\"b\":\"two\",\"c\":true,\"d\":null}";
        JsonParser p = parse(json);
        assertToken(JsonToken.START_OBJECT, p.nextToken());
        assertToken(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("a", p.getCurrentName());
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
        assertToken(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("b", p.getCurrentName());
        assertToken(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("two", p.getText());
        assertToken(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("c", p.getCurrentName());
        assertToken(JsonToken.VALUE_TRUE, p.nextToken());
        assertTrue(p.getBooleanValue());
        assertToken(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("d", p.getCurrentName());
        assertToken(JsonToken.VALUE_NULL, p.nextToken());
        assertTrue(p.isExpectedStartObjectToken() == false);
        assertToken(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());
        p.close();
    }

    // Empty object
    @Test
    public void testEmptyObject() throws Exception {
        JsonParser p = parse("{}");
        assertToken(JsonToken.START_OBJECT, p.nextToken());
        assertToken(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());
        p.close();
    }

    // Array with mixed types
    @Test
    public void testArray() throws Exception {
        String json = "[1,2.5,\"three\",false,null]";
        JsonParser p = parse(json);
        assertToken(JsonToken.START_ARRAY, p.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(2.5, p.getDoubleValue(), 0.0001);
        assertToken(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("three", p.getText());
        assertToken(JsonToken.VALUE_FALSE, p.nextToken());
        assertFalse(p.getBooleanValue());
        assertToken(JsonToken.VALUE_NULL, p.nextToken());
        assertToken(JsonToken.END_ARRAY, p.nextToken());
        assertNull(p.nextToken());
        p.close();
    }

    // Empty array
    @Test
    public void testEmptyArray() throws Exception {
        JsonParser p = parse("[]");
        assertToken(JsonToken.START_ARRAY, p.nextToken());
        assertToken(JsonToken.END_ARRAY, p.nextToken());
        assertNull(p.nextToken());
        p.close();
    }

    // Nested structures
    @Test
    public void testNestedObjectAndArray() throws Exception {
        String json = "{\"obj\":{\"x\":1},\"arr\":[true,false]}";
        JsonParser p = parse(json);
        assertToken(JsonToken.START_OBJECT, p.nextToken());
        assertToken(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("obj", p.getCurrentName());
        assertToken(JsonToken.START_OBJECT, p.nextToken());
        assertToken(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("x", p.getCurrentName());
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
        assertToken(JsonToken.END_OBJECT, p.nextToken());
        assertToken(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("arr", p.getCurrentName());
        assertToken(JsonToken.START_ARRAY, p.nextToken());
        assertToken(JsonToken.VALUE_TRUE, p.nextToken());
        assertToken(JsonToken.VALUE_FALSE, p.nextToken());
        assertToken(JsonToken.END_ARRAY, p.nextToken());
        assertToken(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());
        p.close();
    }

    // String escaping and unicode
    @Test
    public void testStringEscapesAndUnicode() throws Exception {
        String json = "{\"esc\":\"a\\\"b\\\\c\\/\\b\\f\\n\\r\\t\",\"uni\":\"\\u0041\\u00e9\\u4e2d\"}";
        JsonParser p = parse(json);
        assertToken(JsonToken.START_OBJECT, p.nextToken());
        assertToken(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("esc", p.getCurrentName());
        assertToken(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("a\"b\\c/\b\f\n\r\t", p.getText());
        assertToken(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("uni", p.getCurrentName());
        assertToken(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("Aé中", p.getText());
        assertToken(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());
        p.close();
    }

    // Unicode in field names and raw UTF-8 bytes
    @Test
    public void testUnicodeRawUTF8() throws Exception {
        String json = "{\"ключ\":\"значение\"}";
        JsonParser p = parse(json);
        assertToken(JsonToken.START_OBJECT, p.nextToken());
        assertToken(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("ключ", p.getCurrentName());
        assertToken(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("значение", p.getText());
        assertToken(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());
        p.close();
    }

    // Whitespace handling
    @Test
    public void testWhitespace() throws Exception {
        String json = " \n\t { \n \"a\" : 42 , \"b\" : [ 1, 2 ] \t } \r\n";
        JsonParser p = parse(json);
        assertToken(JsonToken.START_OBJECT, p.nextToken());
        assertToken(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("a", p.getCurrentName());
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(42, p.getIntValue());
        assertToken(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("b", p.getCurrentName());
        assertToken(JsonToken.START_ARRAY, p.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertToken(JsonToken.END_ARRAY, p.nextToken());
        assertToken(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());
        p.close();
    }

    // Numeric variants
    @Test
    public void testNumbers() throws Exception {
        String json = "[0,-1,123,-456,1.5,-2.25,1e10,1.25e-3,2E+4]";
        JsonParser p = parse(json);
        assertToken(JsonToken.START_ARRAY, p.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(0, p.getIntValue());
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(-1, p.getIntValue());
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(123, p.getIntValue());
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(-456, p.getIntValue());
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(1.5, p.getDoubleValue(), 0.0);
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(-2.25, p.getDoubleValue(), 0.0);
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(1e10, p.getDoubleValue(), 0.0);
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(1.25e-3, p.getDoubleValue(), 0.0);
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(2e4, p.getDoubleValue(), 0.0);
        assertToken(JsonToken.END_ARRAY, p.nextToken());
        p.close();
    }

    // Large numbers for long and double
    @Test
    public void testLargeNumbers() throws Exception {
        String json = "[1234567890123456789,1.7976931348623157E308]";
        JsonParser p = parse(json);
        assertToken(JsonToken.START_ARRAY, p.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1234567890123456789L, p.getLongValue());
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(Double.MAX_VALUE, p.getDoubleValue(), 0.0);
        assertToken(JsonToken.END_ARRAY, p.nextToken());
        p.close();
    }

    // Field name and value getters
    @Test
    public void testCurrentNameAndText() throws Exception {
        String json = "{\"name\":\"value\"}";
        JsonParser p = parse(json);
        p.nextToken();
        p.nextToken();
        assertEquals("name", p.getCurrentName());
        assertEquals("name", p.getText());
        p.nextToken();
        assertEquals("value", p.getText());
        p.close();
    }

    // Skip children and current token traversal
    @Test
    public void testSkipChildren() throws Exception {
        String json = "{\"a\":[1,2,3],\"b\":4}";
        JsonParser p = parse(json);
        assertToken(JsonToken.START_OBJECT, p.nextToken());
        assertToken(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("a", p.getCurrentName());
        assertToken(JsonToken.START_ARRAY, p.nextToken());
        assertToken(JsonToken.END_ARRAY, p.skipChildren());
        assertToken(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("b", p.getCurrentName());
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertToken(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());
        p.close();
    }

    // Value retrieval as string
    @Test
    public void testValueAsString() throws Exception {
        String json = "{\"s\":\"abc\",\"n\":123,\"f\":4.5,\"b\":true}";
        JsonParser p = parse(json);
        p.nextToken();
        // "s"
        p.nextToken();
        p.nextToken();
        assertEquals("abc", p.getValueAsString());
        // "n"
        p.nextToken();
        p.nextToken();
        assertEquals("123", p.getValueAsString());
        // "f"
        p.nextToken();
        p.nextToken();
        assertEquals("4.5", p.getValueAsString());
        // "b"
        p.nextToken();
        p.nextToken();
        assertEquals("true", p.getValueAsString());
        p.close();
    }

    // Malformed JSON missing comma
    @Test(expected = Exception.class)
    public void testMissingComma() throws Exception {
        JsonParser p = parse("{\"a\":1 \"b\":2}");
        while (p.nextToken() != null) {
            // consume
        }
        p.close();
    }

    // Malformed JSON trailing comma
    @Test(expected = Exception.class)
    public void testTrailingComma() throws Exception {
        JsonParser p = parse("[1,2,]");
        while (p.nextToken() != null) {
            // consume
        }
        p.close();
    }

    // Incomplete JSON
    @Test(expected = Exception.class)
    public void testTruncatedJson() throws Exception {
        JsonParser p = parse("{\"a\":");
        p.nextToken();
        p.nextToken();
        p.nextToken(); // should fail
        p.close();
    }

    // InputStream source
    @Test
    public void testInputStreamSource() throws Exception {
        String json = "{\"ok\":1}";
        JsonParser p = parseInputStream(json);
        assertToken(JsonToken.START_OBJECT, p.nextToken());
        assertToken(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("ok", p.getCurrentName());
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
        assertToken(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());
        p.close();
    }

    // Duplicate field names are allowed by default
    @Test
    public void testDuplicateFields() throws Exception {
        String json = "{\"a\":1,\"a\":2}";
        JsonParser p = parse(json);
        assertToken(JsonToken.START_OBJECT, p.nextToken());
        assertToken(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("a", p.getCurrentName());
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
        assertToken(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("a", p.getCurrentName());
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(2, p.getIntValue());
        assertToken(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }

    // getCurrentName returns null outside field
    @Test
    public void testCurrentNameOutsideField() throws Exception {
        String json = "[1]";
        JsonParser p = parse(json);
        assertToken(JsonToken.START_ARRAY, p.nextToken());
        assertNull(p.getCurrentName());
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertNull(p.getCurrentName());
        assertToken(JsonToken.END_ARRAY, p.nextToken());
        assertNull(p.getCurrentName());
        p.close();
    }

    // Closed parser state
    @Test
    public void testCloseParser() throws Exception {
        String json = "{}";
        JsonParser p = parse(json);
        assertFalse(p.isClosed());
        p.nextToken();
        p.close();
        assertTrue(p.isClosed());
    }

    // Empty string as field value
    @Test
    public void testEmptyStringValue() throws Exception {
        String json = "{\"a\":\"\"}";
        JsonParser p = parse(json);
        assertToken(JsonToken.START_OBJECT, p.nextToken());
        assertToken(JsonToken.FIELD_NAME, p.nextToken());
        assertToken(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("", p.getText());
        assertToken(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }
}