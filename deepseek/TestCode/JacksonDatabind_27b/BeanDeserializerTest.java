package com.fasterxml.jackson.databind.deser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonUnwrapped;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * JUnit 4 test suite for BeanDeserializer.
 * Designed to achieve high code coverage and detect faults,
 * including the NullPointerException triggered by missing @JsonUnwrapped properties
 * (Defects4J bug 27).
 */
public class BeanDeserializerTest {

    private ObjectMapper mapper;

    // -----------------------------------------------------------------------
    // Test POJOs
    // -----------------------------------------------------------------------

    static class Simple {
        public int id;
        public String name;
    }

    static class UnwrappedPart {
        public int x;
        public int y;
    }

    static class ContainerWithUnwrapped {
        public String title;
        @JsonUnwrapped
        public UnwrappedPart part;
    }

    static class ContainerWithUnwrappedAndExtra {
        public String title;
        @JsonUnwrapped
        public UnwrappedPart part;
        public int extra;
    }

    static class ContainerWithNestedUnwrapped {
        public String label;
        @JsonUnwrapped
        public ContainerWithUnwrapped inner;
    }

    // -----------------------------------------------------------------------
    // Setup
    // -----------------------------------------------------------------------

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
    }

    // -----------------------------------------------------------------------
    // Basic deserialization tests
    // -----------------------------------------------------------------------

    @Test
    public void testDeserializeSimpleObject() throws Exception {
        String json = "{\"id\":42,\"name\":\"foo\"}";
        Simple result = mapper.readValue(json, Simple.class);
        assertEquals(42, result.id);
        assertEquals("foo", result.name);
    }

    @Test
    public void testDeserializeSimpleObjectWithNull() throws Exception {
        String json = "{\"id\":null,\"name\":\"bar\"}";
        Simple result = mapper.readValue(json, Simple.class);
        assertEquals(0, result.id);
        assertEquals("bar", result.name);
    }

    @Test
    public void testDeserializeSimpleObjectMissingProperty() throws Exception {
        String json = "{\"id\":1}";
        Simple result = mapper.readValue(json, Simple.class);
        assertEquals(1, result.id);
        assertNull(result.name);
    }

    @Test
    public void testDeserializeEmptyObject() throws Exception {
        String json = "{}";
        Simple result = mapper.readValue(json, Simple.class);
        assertEquals(0, result.id);
        assertNull(result.name);
    }

    // -----------------------------------------------------------------------
    // @JsonUnwrapped tests (targeting bug 27)
    // -----------------------------------------------------------------------

    @Test
    public void testDeserializeWithUnwrappedPresent() throws Exception {
        String json = "{\"title\":\"test\",\"x\":10,\"y\":20}";
        ContainerWithUnwrapped result = mapper.readValue(json, ContainerWithUnwrapped.class);
        assertEquals("test", result.title);
        assertNotNull(result.part);
        assertEquals(10, result.part.x);
        assertEquals(20, result.part.y);
    }

    @Test
    public void testDeserializeWithUnwrappedMissing() throws Exception {
        // JSON does not contain the unwrapped properties (x, y)
        // This should NOT throw a NullPointerException (bug 27)
        String json = "{\"title\":\"test\"}";
        ContainerWithUnwrapped result = mapper.readValue(json, ContainerWithUnwrapped.class);
        assertEquals("test", result.title);
        assertNull(result.part);
    }

    @Test
    public void testDeserializeWithUnwrappedNull() throws Exception {
        // JSON explicitly sets the unwrapped property to null
        String json = "{\"title\":\"test\",\"part\":null}";
        ContainerWithUnwrapped result = mapper.readValue(json, ContainerWithUnwrapped.class);
        assertEquals("test", result.title);
        assertNull(result.part);
    }

    @Test
    public void testDeserializeWithUnwrappedPartial() throws Exception {
        // Only one of the unwrapped fields is provided
        String json = "{\"title\":\"test\",\"x\":5}";
        ContainerWithUnwrapped result = mapper.readValue(json, ContainerWithUnwrapped.class);
        assertEquals("test", result.title);
        assertNotNull(result.part);
        assertEquals(5, result.part.x);
        assertEquals(0, result.part.y);
    }

    @Test
    public void testDeserializeWithUnwrappedAndExtraFields() throws Exception {
        // JSON contains extra fields that are not part of the unwrapped target
        String json = "{\"title\":\"test\",\"x\":1,\"y\":2,\"extra\":100}";
        ContainerWithUnwrappedAndExtra result = mapper.readValue(json, ContainerWithUnwrappedAndExtra.class);
        assertEquals("test", result.title);
        assertNotNull(result.part);
        assertEquals(1, result.part.x);
        assertEquals(2, result.part.y);
        assertEquals(100, result.extra);
    }

    @Test
    public void testDeserializeWithNestedUnwrapped() throws Exception {
        // Nested @JsonUnwrapped
        String json = "{\"label\":\"outer\",\"title\":\"inner\",\"x\":3,\"y\":4}";
        ContainerWithNestedUnwrapped result = mapper.readValue(json, ContainerWithNestedUnwrapped.class);
        assertEquals("outer", result.label);
        assertNotNull(result.inner);
        assertEquals("inner", result.inner.title);
        assertNotNull(result.inner.part);
        assertEquals(3, result.inner.part.x);
        assertEquals(4, result.inner.part.y);
    }

    @Test
    public void testDeserializeWithNestedUnwrappedMissingInner() throws Exception {
        // Nested @JsonUnwrapped with missing inner unwrapped fields
        String json = "{\"label\":\"outer\",\"title\":\"inner\"}";
        ContainerWithNestedUnwrapped result = mapper.readValue(json, ContainerWithNestedUnwrapped.class);
        assertEquals("outer", result.label);
        assertNotNull(result.inner);
        assertEquals("inner", result.inner.title);
        assertNull(result.inner.part);
    }

    // -----------------------------------------------------------------------
    // Edge cases and boundary conditions
    // -----------------------------------------------------------------------

    @Test(expected = JsonProcessingException.class)
    public void testDeserializeInvalidJson() throws Exception {
        String json = "{invalid}";
        mapper.readValue(json, Simple.class);
    }

    @Test(expected = JsonProcessingException.class)
    public void testDeserializeTypeMismatch() throws Exception {
        String json = "{\"id\":\"notanumber\"}";
        mapper.readValue(json, Simple.class);
    }

    @Test
    public void testDeserializeWithNullInput() throws Exception {
        // ObjectMapper.readValue(null, ...) should throw, but we test that it doesn't crash the deserializer
        try {
            mapper.readValue((String) null, Simple.class);
            fail("Expected JsonProcessingException for null input");
        } catch (JsonProcessingException e) {
            // expected
        }
    }

    @Test
    public void testDeserializeWithEmptyString() throws Exception {
        try {
            mapper.readValue("", Simple.class);
            fail("Expected JsonProcessingException for empty string");
        } catch (JsonProcessingException e) {
            // expected
        }
    }

    // -----------------------------------------------------------------------
    // Tests for BeanDeserializer internal behavior (via ObjectMapper)
    // -----------------------------------------------------------------------

    @Test
    public void testDeserializeWithUnknownProperties() throws Exception {
        // By default, unknown properties are ignored
        String json = "{\"id\":1,\"unknown\":\"value\"}";
        Simple result = mapper.readValue(json, Simple.class);
        assertEquals(1, result.id);
        assertNull(result.name);
    }

    @Test
    public void testDeserializeWithDefaultValues() throws Exception {
        // Ensure that default values are used when properties are missing
        String json = "{}";
        Simple result = mapper.readValue(json, Simple.class);
        assertEquals(0, result.id);
        assertNull(result.name);
    }

    @Test
    public void testDeserializeWithBooleanAndNumeric() throws Exception {
        // Additional type coverage
        String json = "{\"id\":true,\"name\":123}";
        // This will fail type conversion, but we test that it doesn't crash
        try {
            mapper.readValue(json, Simple.class);
            fail("Expected JsonProcessingException for type mismatch");
        } catch (JsonProcessingException e) {
            // expected
        }
    }

    // -----------------------------------------------------------------------
    // Stress test with many fields (to exercise loops/branches)
    // -----------------------------------------------------------------------

    static class ManyFields {
        public int a, b, c, d, e, f, g, h, i, j;
        public String s;
    }

    @Test
    public void testDeserializeManyFields() throws Exception {
        StringBuilder sb = new StringBuilder("{");
        for (int k = 0; k < 10; k++) {
            if (k > 0) sb.append(",");
            sb.append("\"").append((char)('a' + k)).append("\":").append(k);
        }
        sb.append(",\"s\":\"hello\"}");
        ManyFields result = mapper.readValue(sb.toString(), ManyFields.class);
        assertEquals(0, result.a);
        assertEquals(1, result.b);
        assertEquals(2, result.c);
        assertEquals(3, result.d);
        assertEquals(4, result.e);
        assertEquals(5, result.f);
        assertEquals(6, result.g);
        assertEquals(7, result.h);
        assertEquals(8, result.i);
        assertEquals(9, result.j);
        assertEquals("hello", result.s);
    }

    @Test
    public void testDeserializeManyFieldsMissingSome() throws Exception {
        String json = "{\"a\":10,\"e\":20,\"j\":30}";
        ManyFields result = mapper.readValue(json, ManyFields.class);
        assertEquals(10, result.a);
        assertEquals(0, result.b);
        assertEquals(0, result.c);
        assertEquals(0, result.d);
        assertEquals(20, result.e);
        assertEquals(0, result.f);
        assertEquals(0, result.g);
        assertEquals(0, result.h);
        assertEquals(0, result.i);
        assertEquals(30, result.j);
        assertNull(result.s);
    }

    // -----------------------------------------------------------------------
    // Test with @JsonUnwrapped and null values in unwrapped fields
    // -----------------------------------------------------------------------

    @Test
    public void testDeserializeUnwrappedWithNullValues() throws Exception {
        // JSON provides null for unwrapped fields
        String json = "{\"title\":\"test\",\"x\":null,\"y\":null}";
        ContainerWithUnwrapped result = mapper.readValue(json, ContainerWithUnwrapped.class);
        assertEquals("test", result.title);
        assertNotNull(result.part);
        assertEquals(0, result.part.x);
        assertEquals(0, result.part.y);
    }

    @Test
    public void testDeserializeUnwrappedWithMixedNull() throws Exception {
        String json = "{\"title\":\"test\",\"x\":5,\"y\":null}";
        ContainerWithUnwrapped result = mapper.readValue(json, ContainerWithUnwrapped.class);
        assertEquals("test", result.title);
        assertNotNull(result.part);
        assertEquals(5, result.part.x);
        assertEquals(0, result.part.y);
    }
}