package com.fasterxml.jackson.databind.deser.std;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonMappingException;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class StringArrayDeserializerTest {
    private ObjectMapper mapper;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
    }

    @Test
    public void testDeserializeEmptyArray() throws Exception {
        String json = "[]";
        String[] result = mapper.readValue(json, String[].class);
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test
    public void testDeserializeSingleString() throws Exception {
        String json = "[\"hello\"]";
        String[] result = mapper.readValue(json, String[].class);
        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals("hello", result[0]);
    }

    @Test
    public void testDeserializeMultipleStrings() throws Exception {
        String json = "[\"a\",\"b\",\"c\"]";
        String[] result = mapper.readValue(json, String[].class);
        assertNotNull(result);
        assertEquals(3, result.length);
        assertArrayEquals(new String[]{"a","b","c"}, result);
    }

    @Test
    public void testDeserializeWithNulls() throws Exception {
        String json = "[\"a\",null,\"c\"]";
        String[] result = mapper.readValue(json, String[].class);
        assertNotNull(result);
        assertEquals(3, result.length);
        assertEquals("a", result[0]);
        assertNull(result[1]);
        assertEquals("c", result[2]);
    }

    @Test
    public void testDeserializeEmptyString() throws Exception {
        String json = "[\"\"]";
        String[] result = mapper.readValue(json, String[].class);
        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals("", result[0]);
    }

    @Test
    public void testDeserializeWithSpecialCharacters() throws Exception {
        String json = "[\"line1\\nline2\",\"tab\\ttest\"]";
        String[] result = mapper.readValue(json, String[].class);
        assertNotNull(result);
        assertEquals(2, result.length);
        assertEquals("line1\nline2", result[0]);
        assertEquals("tab\ttest", result[1]);
    }

    @Test(expected = JsonMappingException.class)
    public void testDeserializeNonArray() throws Exception {
        String json = "\"not an array\"";
        mapper.readValue(json, String[].class);
    }

    @Test(expected = JsonMappingException.class)
    public void testDeserializeInvalidElementType() throws Exception {
        String json = "[123]";
        mapper.readValue(json, String[].class);
    }

    @Test
    public void testDeserializeLargeArray() throws Exception {
        int size = 1000;
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < size; i++) {
            if (i > 0) sb.append(",");
            sb.append("\"str").append(i).append("\"");
        }
        sb.append("]");
        String json = sb.toString();
        String[] result = mapper.readValue(json, String[].class);
        assertNotNull(result);
        assertEquals(size, result.length);
        assertEquals("str0", result[0]);
        assertEquals("str999", result[999]);
    }

    @Test
    public void testDeserializeWithWhitespace() throws Exception {
        String json = "  [  \"a\" , \"b\" ]  ";
        String[] result = mapper.readValue(json, String[].class);
        assertNotNull(result);
        assertEquals(2, result.length);
        assertArrayEquals(new String[]{"a","b"}, result);
    }

    @Test
    public void testDeserializeArrayOfNulls() throws Exception {
        String json = "[null,null]";
        String[] result = mapper.readValue(json, String[].class);
        assertNotNull(result);
        assertEquals(2, result.length);
        assertNull(result[0]);
        assertNull(result[1]);
    }

    @Test
    public void testDeserializeSingleEmptyString() throws Exception {
        String json = "[\"\"]";
        String[] result = mapper.readValue(json, String[].class);
        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals("", result[0]);
    }

    @Test(expected = JsonMappingException.class)
    public void testDeserializeNestedArray() throws Exception {
        String json = "[[\"a\"]]";
        mapper.readValue(json, String[].class);
    }

    @Test
    public void testDeserializeLongString() throws Exception {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            sb.append("x");
        }
        String longStr = sb.toString();
        String json = "[\"" + longStr + "\"]";
        String[] result = mapper.readValue(json, String[].class);
        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals(longStr, result[0]);
    }

    @Test
    public void testDeserializeUnicode() throws Exception {
        String json = "[\"\\u0041\",\"\\u00e9\"]";
        String[] result = mapper.readValue(json, String[].class);
        assertNotNull(result);
        assertEquals(2, result.length);
        assertEquals("A", result[0]);
        assertEquals("é", result[1]);
    }

    @Test
    public void testDeserializeEscapedQuotes() throws Exception {
        String json = "[\"\\\"quote\\\"\"]";
        String[] result = mapper.readValue(json, String[].class);
        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals("\"quote\"", result[0]);
    }
}