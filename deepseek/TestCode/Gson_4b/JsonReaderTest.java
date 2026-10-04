package com.example;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

public class JsonReaderTest {
    private JsonReader reader;

    @Before
    public void setUp() {
        reader = new JsonReader();
    }

    @Test
    public void testReadNullInput() {
        assertNull(reader.read(null));
    }

    @Test
    public void testReadEmptyString() {
        assertNull(reader.read(""));
    }

    @Test
    public void testReadValidJsonObject() {
        String json = "{\"name\":\"John\",\"age\":30}";
        Object result = reader.read(json);
        assertNotNull(result);
        assertTrue(result instanceof java.util.Map);
        java.util.Map<String, Object> map = (java.util.Map<String, Object>) result;
        assertEquals("John", map.get("name"));
        assertEquals(30, map.get("age"));
    }

    @Test
    public void testReadValidJsonArray() {
        String json = "[1,2,3]";
        Object result = reader.read(json);
        assertNotNull(result);
        assertTrue(result instanceof java.util.List);
        java.util.List<Object> list = (java.util.List<Object>) result;
        assertEquals(3, list.size());
        assertEquals(1, list.get(0));
    }

    @Test(expected = JsonParseException.class)
    public void testReadMalformedJson() {
        reader.read("{invalid}");
    }

    @Test(expected = JsonParseException.class)
    public void testReadUnclosedObject() {
        reader.read("{\"key\":\"value\"");
    }

    @Test(expected = JsonParseException.class)
    public void testReadUnexpectedToken() {
        reader.read("[1,2,]");
    }

    @Test
    public void testReadNestedStructures() {
        String json = "{\"outer\":{\"inner\":\"value\"}}";
        Object result = reader.read(json);
        assertNotNull(result);
        java.util.Map<String, Object> map = (java.util.Map<String, Object>) result;
        assertTrue(map.get("outer") instanceof java.util.Map);
    }

    @Test
    public void testReadWithEscapedCharacters() {
        String json = "{\"text\":\"hello\\nworld\"}";
        Object result = reader.read(json);
        assertNotNull(result);
        java.util.Map<String, Object> map = (java.util.Map<String, Object>) result;
        assertEquals("hello\nworld", map.get("text"));
    }

    @Test
    public void testReadBooleanAndNullValues() {
        String json = "{\"flag\":true,\"empty\":null}";
        Object result = reader.read(json);
        assertNotNull(result);
        java.util.Map<String, Object> map = (java.util.Map<String, Object>) result;
        assertEquals(true, map.get("flag"));
        assertNull(map.get("empty"));
    }

    @Test
    public void testReadNumericBoundaries() {
        String json = "{\"max\":2147483647,\"min\":-2147483648}";
        Object result = reader.read(json);
        assertNotNull(result);
        java.util.Map<String, Object> map = (java.util.Map<String, Object>) result;
        assertEquals(2147483647, map.get("max"));
        assertEquals(-2147483648, map.get("min"));
    }

    @Test(expected = JsonParseException.class)
    public void testReadTrailingCommaInObject() {
        reader.read("{\"a\":1,}");
    }

    @Test(expected = JsonParseException.class)
    public void testReadSingleQuote() {
        reader.read("{'key':'value'}");
    }
}