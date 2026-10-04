package com.fasterxml.jackson.databind.deser.std;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * JUnit 4 test suite for UntypedObjectDeserializer.
 * Designed to achieve high coverage and reveal potential faults,
 * including the known bug (Defects4J Bug 32) related to empty string
 * deserialization causing NullPointerException.
 */
public class UntypedObjectDeserializerTest {

    private UntypedObjectDeserializer deserializer;
    private ObjectMapper mapper;

    @Before
    public void setUp() {
        deserializer = new UntypedObjectDeserializer();
        mapper = new ObjectMapper();
    }

    // Helper method to deserialize a JSON string to Object
    private Object deserialize(String json) throws Exception {
        JsonParser parser = mapper.getFactory().createParser(json);
        DeserializationContext ctxt = mapper.getDeserializationContext();
        parser.nextToken(); // advance to first token
        return deserializer.deserialize(parser, ctxt);
    }

    // ===== Basic value types =====

    @Test
    public void testNull() throws Exception {
        Object result = deserialize("null");
        assertNull("Null should deserialize to null", result);
    }

    @Test
    public void testEmptyString() throws Exception {
        // This case triggered NullPointerException in Bug 32
        Object result = deserialize("\"\"");
        assertNotNull("Empty string should not produce null", result);
        assertTrue("Empty string should be a String", result instanceof String);
        assertEquals("Empty string value", "", result);
    }

    @Test
    public void testString() throws Exception {
        Object result = deserialize("\"hello\"");
        assertTrue(result instanceof String);
        assertEquals("hello", result);
    }

    @Test
    public void testInteger() throws Exception {
        Object result = deserialize("42");
        assertTrue("Integer should be deserialized as Integer", result instanceof Integer);
        assertEquals(42, result);
    }

    @Test
    public void testLong() throws Exception {
        // Large integer that fits in long but not int
        Object result = deserialize("1234567890123");
        assertTrue("Large integer should be deserialized as Long", result instanceof Long);
        assertEquals(1234567890123L, result);
    }

    @Test
    public void testDouble() throws Exception {
        Object result = deserialize("3.14");
        assertTrue("Double should be deserialized as Double", result instanceof Double);
        assertEquals(3.14, (Double) result, 0.0);
    }

    @Test
    public void testBooleanTrue() throws Exception {
        Object result = deserialize("true");
        assertTrue(result instanceof Boolean);
        assertTrue((Boolean) result);
    }

    @Test
    public void testBooleanFalse() throws Exception {
        Object result = deserialize("false");
        assertTrue(result instanceof Boolean);
        assertFalse((Boolean) result);
    }

    // ===== Array types =====

    @Test
    public void testEmptyArray() throws Exception {
        Object result = deserialize("[]");
        assertTrue("Empty array should be Object[]", result instanceof Object[]);
        assertEquals(0, ((Object[]) result).length);
    }

    @Test
    public void testSimpleArray() throws Exception {
        Object result = deserialize("[1, \"two\", true]");
        assertTrue(result instanceof Object[]);
        Object[] arr = (Object[]) result;
        assertEquals(3, arr.length);
        assertEquals(1, arr[0]);
        assertEquals("two", arr[1]);
        assertEquals(true, arr[2]);
    }

    @Test
    public void testNestedArray() throws Exception {
        Object result = deserialize("[[1,2], [3,4]]");
        assertTrue(result instanceof Object[]);
        Object[] outer = (Object[]) result;
        assertEquals(2, outer.length);
        assertTrue(outer[0] instanceof Object[]);
        assertTrue(outer[1] instanceof Object[]);
        assertEquals(1, ((Object[]) outer[0])[0]);
        assertEquals(4, ((Object[]) outer[1])[1]);
    }

    // ===== Object types =====

    @Test
    public void testEmptyObject() throws Exception {
        Object result = deserialize("{}");
        assertTrue("Empty object should be a Map", result instanceof java.util.Map);
        assertTrue(((java.util.Map<?,?>) result).isEmpty());
    }

    @Test
    public void testSimpleObject() throws Exception {
        Object result = deserialize("{\"a\":1, \"b\":\"text\"}");
        assertTrue(result instanceof java.util.Map);
        java.util.Map<String, Object> map = (java.util.Map<String, Object>) result;
        assertEquals(2, map.size());
        assertEquals(1, map.get("a"));
        assertEquals("text", map.get("b"));
    }

    @Test
    public void testNestedObject() throws Exception {
        Object result = deserialize("{\"outer\":{\"inner\":true}}");
        assertTrue(result instanceof java.util.Map);
        java.util.Map<String, Object> outer = (java.util.Map<String, Object>) result;
        assertTrue(outer.get("outer") instanceof java.util.Map);
        java.util.Map<String, Object> inner = (java.util.Map<String, Object>) outer.get("outer");
        assertEquals(true, inner.get("inner"));
    }

    @Test
    public void testMixedArrayAndObject() throws Exception {
        Object result = deserialize("[{\"x\":1}, [2,3]]");
        assertTrue(result instanceof Object[]);
        Object[] arr = (Object[]) result;
        assertEquals(2, arr.length);
        assertTrue(arr[0] instanceof java.util.Map);
        assertTrue(arr[1] instanceof Object[]);
    }

    // ===== Edge cases and potential fault triggers =====

    @Test
    public void testStringWithSpecialCharacters() throws Exception {
        String json = "\"line1\\nline2\\ttab\"";
        Object result = deserialize(json);
        assertTrue(result instanceof String);
        assertEquals("line1\nline2\ttab", result);
    }

    @Test
    public void testFloatingPointInfinity() throws Exception {
        // Depending on JSON spec, these may be accepted or not. We just test behavior.
        // Might produce Double.POSITIVE_INFINITY or throw.
        try {
            Object result = deserialize("1.0e1000");
            // If it succeeds, it should be Double
            assertTrue(result instanceof Double);
        } catch (Exception e) {
            // It's acceptable to throw for out-of-range values
        }
    }

    @Test
    public void testNegativeInteger() throws Exception {
        Object result = deserialize("-128");
        assertTrue(result instanceof Integer);
        assertEquals(-128, result);
    }

    @Test
    public void testZero() throws Exception {
        Object result = deserialize("0");
        assertTrue(result instanceof Integer);
        assertEquals(0, result);
    }

    @Test
    public void testEmptyStringAsToken() throws Exception {
        // Directly test with parser at VALUE_STRING token with empty text.
        // This simulates the bug scenario more precisely.
        JsonParser parser = mapper.getFactory().createParser("\"\"");
        DeserializationContext ctxt = mapper.getDeserializationContext();
        parser.nextToken(); // VALUE_STRING
        Object result = deserializer.deserialize(parser, ctxt);
        assertNotNull(result);
        assertEquals("", result);
    }

    @Test
    public void testNullTokenWithinArray() throws Exception {
        Object result = deserialize("[null, 1]");
        assertTrue(result instanceof Object[]);
        Object[] arr = (Object[]) result;
        assertNull(arr[0]);
        assertEquals(1, arr[1]);
    }

    @Test
    public void testEmptyStringInArray() throws Exception {
        // This could also trigger the bug if the parser treats empty string specially.
        Object result = deserialize("[\"\"]");
        assertTrue(result instanceof Object[]);
        Object[] arr = (Object[]) result;
        assertEquals(1, arr.length);
        assertEquals("", arr[0]);
    }

    // ===== Tests for possible exception scenarios =====

    @Test(expected = Exception.class)
    public void testMalformedJson() throws Exception {
        // Expect some exception (e.g., JsonParseException)
        deserialize("{invalid}");
    }

    @Test
    public void testVeryLongString() throws Exception {
        // Test with a very long string to ensure no buffer overflow
        StringBuilder sb = new StringBuilder(10000);
        for (int i = 0; i < 5000; i++) {
            sb.append('x');
        }
        String longStr = sb.toString();
        Object result = deserialize("\"" + longStr + "\"");
        assertTrue(result instanceof String);
        assertEquals(longStr, result);
    }

    // ===== Coverage of deserializeWithType (if present) =====
    // Note: UntypedObjectDeserializer may override deserializeWithType.
    // We test it if the method is accessible.

    @Test
    public void testDeserializeWithType() throws Exception {
        // Assuming it delegates to deserialize, we can test with a simple case.
        JsonParser parser = mapper.getFactory().createParser("\"test\"");
        DeserializationContext ctxt = mapper.getDeserializationContext();
        parser.nextToken();
        // The type id is not used in simple cases.
        Object result = deserializer.deserializeWithType(parser, ctxt, null);
        assertEquals("test", result);
    }

    // ===== Additional branch coverage: different token types =====

    @Test
    public void testFieldNameToken() throws Exception {
        // This tests the case where the parser is at FIELD_NAME token before value.
        // Normally deserialize is called after advancing, but we can test with startObject.
        JsonParser parser = mapper.getFactory().createParser("{\"key\":\"val\"}");
        DeserializationContext ctxt = mapper.getDeserializationContext();
        parser.nextToken(); // START_OBJECT
        // Now we are at START_OBJECT, deserialize should process the whole object.
        Object result = deserializer.deserialize(parser, ctxt);
        assertTrue(result instanceof java.util.Map);
        assertEquals("val", ((java.util.Map) result).get("key"));
    }

    @Test
    public void testEndObjectToken() throws Exception {
        // Edge: JSON consisting of only '}' (invalid), but we test parse error handling.
        try {
            deserialize("}");
            fail("Should have thrown an exception for invalid JSON");
        } catch (Exception e) {
            // Expected
        }
    }

    @Test
    public void testValueEmbeddedObject() throws Exception {
        // Test with embedded object notation (unlikely but covers branch)
        // Not typically used in JSON but try a token like VALUE_EMBEDDED_OBJECT? Not applicable.
    }
}