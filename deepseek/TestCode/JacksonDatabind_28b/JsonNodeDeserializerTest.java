package com.fasterxml.jackson.databind.deser.std;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import com.fasterxml.jackson.core.*;

public class JsonNodeDeserializerTest {
    private ObjectMapper mapper;
    private JsonNodeDeserializer deserializer;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
        deserializer = JsonNodeDeserializer.instance;
    }

    // Test deserialization of null JSON value
    @Test
    public void testDeserializeNull() throws Exception {
        JsonNode node = mapper.readTree("null");
        assertTrue(node instanceof NullNode);
        assertSame(NullNode.getInstance(), node);
    }

    // Test deserialization of string value
    @Test
    public void testDeserializeString() throws Exception {
        JsonNode node = mapper.readTree("\"hello\"");
        assertTrue(node instanceof TextNode);
        assertEquals("hello", node.textValue());
    }

    // Test deserialization of integer number
    @Test
    public void testDeserializeNumberInt() throws Exception {
        JsonNode node = mapper.readTree("42");
        assertTrue(node instanceof NumericNode);
        assertEquals(42, node.intValue());
    }

    // Test deserialization of floating point number
    @Test
    public void testDeserializeNumberFloat() throws Exception {
        JsonNode node = mapper.readTree("3.14");
        assertTrue(node instanceof NumericNode);
        assertEquals(3.14, node.doubleValue(), 0.0001);
    }

    // Test deserialization of boolean true
    @Test
    public void testDeserializeBooleanTrue() throws Exception {
        JsonNode node = mapper.readTree("true");
        assertTrue(node instanceof BooleanNode);
        assertTrue(node.booleanValue());
    }

    // Test deserialization of boolean false
    @Test
    public void testDeserializeBooleanFalse() throws Exception {
        JsonNode node = mapper.readTree("false");
        assertTrue(node instanceof BooleanNode);
        assertFalse(node.booleanValue());
    }

    // Test deserialization of an object
    @Test
    public void testDeserializeObject() throws Exception {
        JsonNode node = mapper.readTree("{\"a\":1}");
        assertTrue(node instanceof ObjectNode);
        ObjectNode obj = (ObjectNode) node;
        assertEquals(1, obj.size());
        JsonNode a = obj.get("a");
        assertTrue(a instanceof NumericNode);
        assertEquals(1, a.intValue());
    }

    // Test deserialization of an array
    @Test
    public void testDeserializeArray() throws Exception {
        JsonNode node = mapper.readTree("[1,2,3]");
        assertTrue(node instanceof ArrayNode);
        ArrayNode arr = (ArrayNode) node;
        assertEquals(3, arr.size());
        assertEquals(1, arr.get(0).intValue());
        assertEquals(2, arr.get(1).intValue());
        assertEquals(3, arr.get(2).intValue());
    }

    // Test deserialization of nested object
    @Test
    public void testDeserializeNested() throws Exception {
        JsonNode node = mapper.readTree("{\"a\":{\"b\":2}}");
        assertTrue(node instanceof ObjectNode);
        ObjectNode obj = (ObjectNode) node;
        JsonNode a = obj.get("a");
        assertTrue(a instanceof ObjectNode);
        ObjectNode aObj = (ObjectNode) a;
        JsonNode b = aObj.get("b");
        assertTrue(b instanceof NumericNode);
        assertEquals(2, b.intValue());
    }

    // Test deserialization of empty object
    @Test
    public void testDeserializeEmptyObject() throws Exception {
        JsonNode node = mapper.readTree("{}");
        assertTrue(node instanceof ObjectNode);
        assertEquals(0, ((ObjectNode) node).size());
    }

    // Test deserialization of empty array
    @Test
    public void testDeserializeEmptyArray() throws Exception {
        JsonNode node = mapper.readTree("[]");
        assertTrue(node instanceof ArrayNode);
        assertEquals(0, ((ArrayNode) node).size());
    }

    // Test getNullValue returns NullNode
    @Test
    public void testGetNullValue() throws Exception {
        JsonNode nullValue = deserializer.getNullValue(null);
        assertNotNull(nullValue);
        assertTrue(nullValue instanceof NullNode);
        assertSame(NullNode.getInstance(), nullValue);
    }

    // Test getEmptyValue returns NullNode
    @Test
    public void testGetEmptyValue() throws Exception {
        JsonNode emptyValue = deserializer.getEmptyValue(null);
        assertNotNull(emptyValue);
        assertTrue(emptyValue instanceof NullNode);
        assertSame(NullNode.getInstance(), emptyValue);
    }

    // Test that missing property in a POJO with JsonNode field gets NullNode
    @Test
    public void testNullValueForMissingProperty() throws Exception {
        String json = "{}";
        Wrapper w = mapper.readValue(json, Wrapper.class);
        assertNotNull(w.value);
        assertTrue(w.value instanceof NullNode);
        assertSame(NullNode.getInstance(), w.value);
    }

    // Test that explicit null JSON value in a property gets NullNode
    @Test
    public void testNullValueForNullProperty() throws Exception {
        String json = "{\"value\":null}";
        Wrapper w = mapper.readValue(json, Wrapper.class);
        assertNotNull(w.value);
        assertTrue(w.value instanceof NullNode);
        assertSame(NullNode.getInstance(), w.value);
    }

    // Helper class to test property deserialization
    static class Wrapper {
        public JsonNode value;
    }
}