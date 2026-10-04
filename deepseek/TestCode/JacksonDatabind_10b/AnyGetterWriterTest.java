package com.fasterxml.jackson.databind.ser;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Unit tests for AnyGetterWriter (JacksonDatabind bug #10).
 * These tests exercise the writer through the public Jackson serialization API,
 * covering normal cases, null handling, and exceptional scenarios.
 */
public class AnyGetterWriterTest {

    private ObjectMapper mapper;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
    }

    // ----- Inner bean classes -----

    static class SimpleAnyBean {
        private Map<String, Object> properties = new HashMap<>();

        @JsonAnyGetter
        public Map<String, Object> getProperties() {
            return properties;
        }
    }

    static class NullReturningBean {
        @JsonAnyGetter
        public Map<String, Object> getProperties() {
            return null;
        }
    }

    static class EmptyMapBean {
        @JsonAnyGetter
        public Map<String, Object> getProperties() {
            return new HashMap<>();
        }
    }

    static class ThrowingBean {
        @JsonAnyGetter
        public Map<String, Object> getProperties() {
            throw new RuntimeException("boom");
        }
    }

    // ----- Test cases -----

    @Test
    public void testSimpleAnyGetterSerialization() throws IOException {
        SimpleAnyBean bean = new SimpleAnyBean();
        bean.properties.put("name", "Alice");
        bean.properties.put("age", 30);
        bean.properties.put("active", true);

        String json = mapper.writeValueAsString(bean);
        assertEquals("{\"name\":\"Alice\",\"age\":30,\"active\":true}", json);
    }

    @Test
    public void testNullReturningAnyGetter() throws IOException {
        NullReturningBean bean = new NullReturningBean();
        String json = mapper.writeValueAsString(bean);
        assertEquals("{}", json);
    }

    @Test
    public void testEmptyMapAnyGetter() throws IOException {
        EmptyMapBean bean = new EmptyMapBean();
        String json = mapper.writeValueAsString(bean);
        assertEquals("{}", json);
    }

    @Test
    public void testAnyGetterWithNullValueInMap() throws IOException {
        SimpleAnyBean bean = new SimpleAnyBean();
        bean.properties.put("key", null);
        String json = mapper.writeValueAsString(bean);
        assertEquals("{\"key\":null}", json);
    }

    @Test
    public void testAnyGetterWithNullKeyInMap() {
        SimpleAnyBean bean = new SimpleAnyBean();
        bean.properties.put(null, "value");
        try {
            mapper.writeValueAsString(bean);
            fail("Expected exception for null key in @JsonAnyGetter map");
        } catch (Exception e) {
            // expected
        }
    }

    @Test
    public void testAnyGetterWithNumberValues() throws IOException {
        SimpleAnyBean bean = new SimpleAnyBean();
        bean.properties.put("double", 3.14);
        bean.properties.put("long", 123456789L);
        String json = mapper.writeValueAsString(bean);
        assertEquals("{\"double\":3.14,\"long\":123456789}", json);
    }

    @Test
    public void testAnyGetterWithNestedObjects() throws IOException {
        SimpleAnyBean bean = new SimpleAnyBean();
        Map<String, Object> inner = new HashMap<>();
        inner.put("x", 1);
        bean.properties.put("nested", inner);
        String json = mapper.writeValueAsString(bean);
        assertEquals("{\"nested\":{\"x\":1}}", json);
    }

    @Test
    public void testAnyGetterThrowingException() {
        ThrowingBean bean = new ThrowingBean();
        try {
            mapper.writeValueAsString(bean);
            fail("Expected exception from getter");
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof RuntimeException);
            assertEquals("boom", e.getCause().getMessage());
        }
    }

    @Test
    public void testAnyGetterWithMultipleKeysOrderPreserved() throws IOException {
        SimpleAnyBean bean = new SimpleAnyBean();
        bean.properties.put("a", 1);
        bean.properties.put("b", 2);
        bean.properties.put("c", 3);
        String json = mapper.writeValueAsString(bean);
        // Jackson preserves insertion order for Map entries by default
        assertEquals("{\"a\":1,\"b\":2,\"c\":3}", json);
    }
}