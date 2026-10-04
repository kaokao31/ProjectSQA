package com.fasterxml.jackson.core.util;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * JUnit 4 test suite for JsonParserSequence, targeting maximum coverage
 * and fault detection (Defects4J JacksonCore bug 16).
 */
public class JsonParserSequenceTest {

    private JsonFactory factory;

    @Before
    public void setUp() {
        factory = new JsonFactory();
    }

    // Helper to create a JsonParser from a JSON string
    private JsonParser createParser(String json) throws IOException {
        return factory.createParser(json);
    }

    // Test basic token iteration across two parsers
    @Test
    public void testBasicTokenIteration() throws IOException {
        JsonParser first = createParser("{\"a\":1}");
        JsonParser second = createParser("{\"b\":2}");
        JsonParserSequence seq = new JsonParserSequence(first, second);

        assertTrue(seq.hasNext()); // should have tokens
        assertEquals(JsonToken.START_OBJECT, seq.nextToken());
        assertEquals(JsonToken.FIELD_NAME, seq.nextToken());
        assertEquals("a", seq.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, seq.nextToken());
        assertEquals(1, seq.getIntValue());
        assertEquals(JsonToken.END_OBJECT, seq.nextToken());

        // Now switch to second parser
        assertEquals(JsonToken.START_OBJECT, seq.nextToken());
        assertEquals(JsonToken.FIELD_NAME, seq.nextToken());
        assertEquals("b", seq.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, seq.nextToken());
        assertEquals(2, seq.getIntValue());
        assertEquals(JsonToken.END_OBJECT, seq.nextToken());

        assertNull(seq.nextToken()); // no more tokens
        assertFalse(seq.hasNext());
    }

    // Test that getCurrentToken() works correctly after switching parsers
    @Test
    public void testGetCurrentTokenAfterSwitch() throws IOException {
        JsonParser first = createParser("1");
        JsonParser second = createParser("2");
        JsonParserSequence seq = new JsonParserSequence(first, second);

        assertEquals(JsonToken.VALUE_NUMBER_INT, seq.nextToken());
        assertEquals(1, seq.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, seq.getCurrentToken());

        assertEquals(JsonToken.VALUE_NUMBER_INT, seq.nextToken());
        assertEquals(2, seq.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, seq.getCurrentToken());

        assertNull(seq.nextToken());
        assertNull(seq.getCurrentToken());
    }

    // Test hasCurrentToken() behavior
    @Test
    public void testHasCurrentToken() throws IOException {
        JsonParser first = createParser("true");
        JsonParserSequence seq = new JsonParserSequence(first, createParser("false"));

        assertFalse(seq.hasCurrentToken()); // before any nextToken
        seq.nextToken();
        assertTrue(seq.hasCurrentToken());
        assertEquals(JsonToken.VALUE_TRUE, seq.getCurrentToken());

        seq.nextToken();
        assertTrue(seq.hasCurrentToken());
        assertEquals(JsonToken.VALUE_FALSE, seq.getCurrentToken());

        seq.nextToken();
        assertFalse(seq.hasCurrentToken());
    }

    // Test that nextToken returns null when all parsers exhausted
    @Test
    public void testExhaustedSequence() throws IOException {
        JsonParser first = createParser("null");
        JsonParser second = createParser("null");
        JsonParserSequence seq = new JsonParserSequence(first, second);

        assertNotNull(seq.nextToken());
        assertNotNull(seq.nextToken());
        assertNull(seq.nextToken());
        assertNull(seq.nextToken()); // should still be null
    }

    // Test with empty first parser (no tokens)
    @Test
    public void testEmptyFirstParser() throws IOException {
        JsonParser first = createParser(""); // empty, no tokens
        JsonParser second = createParser("42");
        JsonParserSequence seq = new JsonParserSequence(first, second);

        // first parser yields no tokens, so sequence should start with second
        assertEquals(JsonToken.VALUE_NUMBER_INT, seq.nextToken());
        assertEquals(42, seq.getIntValue());
        assertNull(seq.nextToken());
    }

    // Test with empty second parser
    @Test
    public void testEmptySecondParser() throws IOException {
        JsonParser first = createParser("true");
        JsonParser second = createParser(""); // empty
        JsonParserSequence seq = new JsonParserSequence(first, second);

        assertEquals(JsonToken.VALUE_TRUE, seq.nextToken());
        assertNull(seq.nextToken()); // second has no tokens
    }

    // Test close() method
    @Test
    public void testClose() throws IOException {
        JsonParser first = createParser("1");
        JsonParser second = createParser("2");
        JsonParserSequence seq = new JsonParserSequence(first, second);

        seq.close();
        // After close, nextToken should return null
        assertNull(seq.nextToken());
        // Also check that underlying parsers are closed (optional)
        assertTrue(first.isClosed());
        assertTrue(second.isClosed());
    }

    // Test that getCurrentToken() returns null after close
    @Test
    public void testGetCurrentTokenAfterClose() throws IOException {
        JsonParser first = createParser("1");
        JsonParserSequence seq = new JsonParserSequence(first, createParser("2"));
        seq.close();
        assertNull(seq.getCurrentToken());
    }

    // Test with multiple parsers (more than two)
    @Test
    public void testMultipleParsers() throws IOException {
        JsonParser p1 = createParser("1");
        JsonParser p2 = createParser("2");
        JsonParser p3 = createParser("3");
        JsonParserSequence seq = new JsonParserSequence(p1, p2, p3);

        assertEquals(JsonToken.VALUE_NUMBER_INT, seq.nextToken());
        assertEquals(1, seq.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, seq.nextToken());
        assertEquals(2, seq.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, seq.nextToken());
        assertEquals(3, seq.getIntValue());
        assertNull(seq.nextToken());
    }

    // Test that getCurrentName() works across parsers
    @Test
    public void testGetCurrentNameAcrossParsers() throws IOException {
        JsonParser first = createParser("{\"x\":1}");
        JsonParser second = createParser("{\"y\":2}");
        JsonParserSequence seq = new JsonParserSequence(first, second);

        seq.nextToken(); // START_OBJECT
        seq.nextToken(); // FIELD_NAME
        assertEquals("x", seq.getCurrentName());
        seq.nextToken(); // VALUE_NUMBER_INT
        seq.nextToken(); // END_OBJECT

        seq.nextToken(); // START_OBJECT (second)
        seq.nextToken(); // FIELD_NAME
        assertEquals("y", seq.getCurrentName());
    }

    // Test that getText() works across parsers
    @Test
    public void testGetTextAcrossParsers() throws IOException {
        JsonParser first = createParser("\"hello\"");
        JsonParser second = createParser("\"world\"");
        JsonParserSequence seq = new JsonParserSequence(first, second);

        assertEquals(JsonToken.VALUE_STRING, seq.nextToken());
        assertEquals("hello", seq.getText());
        assertEquals(JsonToken.VALUE_STRING, seq.nextToken());
        assertEquals("world", seq.getText());
        assertNull(seq.nextToken());
    }

    // Test that skipChildren() works (if applicable)
    @Test
    public void testSkipChildren() throws IOException {
        JsonParser first = createParser("{\"a\":[1,2]}");
        JsonParser second = createParser("{\"b\":3}");
        JsonParserSequence seq = new JsonParserSequence(first, second);

        seq.nextToken(); // START_OBJECT
        seq.nextToken(); // FIELD_NAME "a"
        seq.nextToken(); // START_ARRAY
        // skip children of array
        assertEquals(JsonToken.START_ARRAY, seq.getCurrentToken());
        seq.skipChildren();
        // after skip, should be at END_ARRAY? Actually skipChildren moves to matching END_ARRAY
        assertEquals(JsonToken.END_ARRAY, seq.getCurrentToken());
        seq.nextToken(); // END_OBJECT of first
        // now second parser
        assertEquals(JsonToken.START_OBJECT, seq.nextToken());
        assertEquals(JsonToken.FIELD_NAME, seq.nextToken());
        assertEquals("b", seq.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, seq.nextToken());
        assertEquals(3, seq.getIntValue());
        assertEquals(JsonToken.END_OBJECT, seq.nextToken());
        assertNull(seq.nextToken());
    }

    // Test that hasNext() returns false after exhaustion
    @Test
    public void testHasNextAfterExhaustion() throws IOException {
        JsonParser first = createParser("1");
        JsonParserSequence seq = new JsonParserSequence(first, createParser("2"));
        assertTrue(seq.hasNext());
        seq.nextToken();
        assertTrue(seq.hasNext());
        seq.nextToken();
        assertFalse(seq.hasNext());
    }

    // Test that getCurrentLocation() works (basic)
    @Test
    public void testGetCurrentLocation() throws IOException {
        JsonParser first = createParser("1");
        JsonParserSequence seq = new JsonParserSequence(first, createParser("2"));
        seq.nextToken();
        assertNotNull(seq.getCurrentLocation());
        seq.nextToken();
        assertNotNull(seq.getCurrentLocation());
        seq.nextToken();
        // after exhaustion, location may be null or last location; just check not null
        // assertNull(seq.getCurrentLocation()); // depends on implementation
    }

    // Test that getTokenLocation() works
    @Test
    public void testGetTokenLocation() throws IOException {
        JsonParser first = createParser("1");
        JsonParserSequence seq = new JsonParserSequence(first, createParser("2"));
        seq.nextToken();
        assertNotNull(seq.getTokenLocation());
        seq.nextToken();
        assertNotNull(seq.getTokenLocation());
    }

    // Test that version() returns non-null
    @Test
    public void testVersion() {
        JsonParserSequence seq = new JsonParserSequence();
        assertNotNull(seq.version());
    }

    // Test that getCodec() returns null by default
    @Test
    public void testGetCodec() {
        JsonParserSequence seq = new JsonParserSequence();
        assertNull(seq.getCodec());
    }

    // Test that setCodec works
    @Test
    public void testSetCodec() {
        JsonParserSequence seq = new JsonParserSequence();
        // setCodec is inherited from JsonParser; we can set to null or a codec
        seq.setCodec(null);
        assertNull(seq.getCodec());
    }

    // Test that canReadTypeId() returns false
    @Test
    public void testCanReadTypeId() {
        JsonParserSequence seq = new JsonParserSequence();
        assertFalse(seq.canReadTypeId());
    }

    // Test that canReadObjectId() returns false
    @Test
    public void testCanReadObjectId() {
        JsonParserSequence seq = new JsonParserSequence();
        assertFalse(seq.canReadObjectId());
    }
}