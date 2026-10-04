package com.fasterxml.jackson.databind.node;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TreeTraversingParser;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Comprehensive JUnit 4 test suite for TreeTraversingParser.
 * Designed to achieve high code coverage and detect potential faults,
 * including the known Defects4J bug #100 (NullNode handling).
 */
public class TreeTraversingParserTest {

    private ObjectMapper mapper;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
    }

    // ============================================================
    // Basic Object Node Traversal
    // ============================================================

    @Test
    public void testSimpleObject() throws IOException {
        JsonNode node = mapper.readTree("{\"name\":\"John\",\"age\":30}");
        TreeTraversingParser parser = new TreeTraversingParser(node);
        assertNull(parser.getCurrentToken()); // before first token

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
        assertNull(parser.nextToken()); // end of stream
    }

    @Test
    public void testEmptyObject() throws IOException {
        JsonNode node = mapper.readTree("{}");
        TreeTraversingParser parser = new TreeTraversingParser(node);
        assertToken(JsonToken.START_OBJECT, parser.nextToken());
        assertToken(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
    }

    // ============================================================
    // Array Node Traversal
    // ============================================================

    @Test
    public void testSimpleArray() throws IOException {
        JsonNode node = mapper.readTree("[1,2,3]");
        TreeTraversingParser parser = new TreeTraversingParser(node);
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
    public void testEmptyArray() throws IOException {
        JsonNode node = mapper.readTree("[]");
        TreeTraversingParser parser = new TreeTraversingParser(node);
        assertToken(JsonToken.START_ARRAY, parser.nextToken());
        assertToken(JsonToken.END_ARRAY, parser.nextToken());
        assertNull(parser.nextToken());
    }

    // ============================================================
    // Nested Structures
    // ============================================================

    @Test
    public void testNestedObject() throws IOException {
        JsonNode node = mapper.readTree("{\"outer\":{\"inner\":\"value\"}}");
        TreeTraversingParser parser = new TreeTraversingParser(node);
        assertToken(JsonToken.START_OBJECT, parser.nextToken());
        assertToken(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("outer", parser.getCurrentName());
        assertToken(JsonToken.START_OBJECT, parser.nextToken());
        assertToken(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("inner", parser.getCurrentName());
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("value", parser.getText());
        assertToken(JsonToken.END_OBJECT, parser.nextToken());
        assertToken(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
    }

    @Test
    public void testNestedArray() throws IOException {
        JsonNode node = mapper.readTree("[[1,2],[3,4]]");
        TreeTraversingParser parser = new TreeTraversingParser(node);
        assertToken(JsonToken.START_ARRAY, parser.nextToken());
        assertToken(JsonToken.START_ARRAY, parser.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(2, parser.getIntValue());
        assertToken(JsonToken.END_ARRAY, parser.nextToken());
        assertToken(JsonToken.START_ARRAY, parser.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(3, parser.getIntValue());
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(4, parser.getIntValue());
        assertToken(JsonToken.END_ARRAY, parser.nextToken());
        assertToken(JsonToken.END_ARRAY, parser.nextToken());
        assertNull(parser.nextToken());
    }

    // ============================================================
    // Value Types
    // ============================================================

    @Test
    public void testBooleanValues() throws IOException {
        JsonNode node = mapper.readTree("{\"a\":true,\"b\":false}");
        TreeTraversingParser parser = new TreeTraversingParser(node);
        assertToken(JsonToken.START_OBJECT, parser.nextToken());
        assertToken(JsonToken.FIELD_NAME, parser.nextToken());
        assertToken(JsonToken.VALUE_TRUE, parser.nextToken());
        assertTrue(parser.getBooleanValue());
        assertToken(JsonToken.FIELD_NAME, parser.nextToken());
        assertToken(JsonToken.VALUE_FALSE, parser.nextToken());
        assertFalse(parser.getBooleanValue());
        assertToken(JsonToken.END_OBJECT, parser.nextToken());
    }

    @Test
    public void testNumericValues() throws IOException {
        JsonNode node = mapper.readTree("{\"int\":42,\"float\":3.14}");
        TreeTraversingParser parser = new TreeTraversingParser(node);
        assertToken(JsonToken.START_OBJECT, parser.nextToken());
        assertToken(JsonToken.FIELD_NAME, parser.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42, parser.getIntValue());
        assertToken(JsonToken.FIELD_NAME, parser.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(3.14, parser.getDoubleValue(), 0.001);
        assertToken(JsonToken.END_OBJECT, parser.nextToken());
    }

    @Test
    public void testStringValues() throws IOException {
        JsonNode node = mapper.readTree("{\"msg\":\"hello\"}");
        TreeTraversingParser parser = new TreeTraversingParser(node);
        assertToken(JsonToken.START_OBJECT, parser.nextToken());
        assertToken(JsonToken.FIELD_NAME, parser.nextToken());
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("hello", parser.getText());
        assertToken(JsonToken.END_OBJECT, parser.nextToken());
    }

    // ============================================================
    // Null Node Handling (Defects4J Bug #100)
    // ============================================================

    @Test
    public void testNullNodeValue() throws IOException {
        // This test targets the known bug: TreeTraversingParser should return VALUE_NULL token
        // and getText() should return null, not "null".
        JsonNode node = mapper.readTree("{\"nullField\":null}");
        TreeTraversingParser parser = new TreeTraversingParser(node);
        assertToken(JsonToken.START_OBJECT, parser.nextToken());
        assertToken(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("nullField", parser.getCurrentName());
        assertToken(JsonToken.VALUE_NULL, parser.nextToken());
        assertNull("getText() should return null for NullNode", parser.getText());
        // Also verify that getValueAsString() returns null
        assertNull(parser.getValueAsString());
        assertToken(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
    }

    @Test
    public void testNullNodeInArray() throws IOException {
        JsonNode node = mapper.readTree("[null]");
        TreeTraversingParser parser = new TreeTraversingParser(node);
        assertToken(JsonToken.START_ARRAY, parser.nextToken());
        assertToken(JsonToken.VALUE_NULL, parser.nextToken());
        assertNull(parser.getText());
        assertToken(JsonToken.END_ARRAY, parser.nextToken());
        assertNull(parser.nextToken());
    }

    @Test
    public void testNullNodeAsRoot() throws IOException {
        JsonNode node = mapper.readTree("null");
        TreeTraversingParser parser = new TreeTraversingParser(node);
        // Root is a NullNode, should produce VALUE_NULL token
        assertToken(JsonToken.VALUE_NULL, parser.nextToken());
        assertNull(parser.getText());
        assertNull(parser.nextToken());
    }

    // ============================================================
    // Edge Cases: Missing Fields, Deep Nesting, Large Numbers
    // ============================================================

    @Test
    public void testMissingFieldAccess() throws IOException {
        JsonNode node = mapper.readTree("{\"a\":1}");
        TreeTraversingParser parser = new TreeTraversingParser(node);
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_INT 1
        // After END_OBJECT, getCurrentName() should return null
        parser.nextToken(); // END_OBJECT
        assertNull(parser.getCurrentName());
    }

    @Test
    public void testDeepNesting() throws IOException {
        // Build a deeply nested object to stress traversal
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (int i = 0; i < 100; i++) {
            sb.append("\"l").append(i).append("\":{");
        }
        sb.append("\"leaf\":true");
        for (int i = 0; i < 100; i++) {
            sb.append("}");
        }
        JsonNode node = mapper.readTree(sb.toString());
        TreeTraversingParser parser = new TreeTraversingParser(node);
        int depth = 0;
        while (parser.nextToken() != null) {
            if (parser.getCurrentToken() == JsonToken.START_OBJECT) depth++;
            if (parser.getCurrentToken() == JsonToken.END_OBJECT) depth--;
        }
        assertEquals(0, depth); // balanced
    }

    @Test
    public void testLargeInteger() throws IOException {
        JsonNode node = mapper.readTree("{\"big\":1234567890123456789}");
        TreeTraversingParser parser = new TreeTraversingParser(node);
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME
        parser.nextToken(); // VALUE_NUMBER_INT
        // Use getLongValue() for large numbers
        assertEquals(1234567890123456789L, parser.getLongValue());
    }

    // ============================================================
    // Context and CurrentName
    // ============================================================

    @Test
    public void testParsingContext() throws IOException {
        JsonNode node = mapper.readTree("{\"x\":[1,2]}");
        TreeTraversingParser parser = new TreeTraversingParser(node);
        parser.nextToken(); // START_OBJECT
        assertTrue(parser.getParsingContext().inObject());
        parser.nextToken(); // FIELD_NAME
        assertEquals("x", parser.getCurrentName());
        parser.nextToken(); // START_ARRAY
        assertTrue(parser.getParsingContext().inArray());
        parser.nextToken(); // VALUE_NUMBER_INT
        assertEquals(1, parser.getIntValue());
        parser.nextToken(); // VALUE_NUMBER_INT
        assertEquals(2, parser.getIntValue());
        parser.nextToken(); // END_ARRAY
        assertTrue(parser.getParsingContext().inObject());
        parser.nextToken(); // END_OBJECT
        assertTrue(parser.getParsingContext().inRoot());
    }

    // ============================================================
    // Helper Methods
    // ============================================================

    private void assertToken(JsonToken expected, JsonToken actual) {
        assertNotNull("Expected token " + expected + " but got null", actual);
        assertEquals("Token mismatch", expected, actual);
    }
}