package com.fasterxml.jackson.databind.deser;

import com.fasterxml.jackson.annotation.JsonDeserialize;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test suite for BuilderBasedDeserializer, targeting bug #76 (NPE on missing/null builder properties).
 */
public class BuilderBasedDeserializerTest {

    // --- Test POJO with builder ---

    @JsonDeserialize(builder = SimpleBuilder.class)
    static class SimpleValue {
        private final int id;
        private final String name;

        SimpleValue(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() { return id; }
        public String getName() { return name; }
    }

    @JsonPOJOBuilder(withPrefix = "")
    static class SimpleBuilder {
        private int id;
        private String name;

        public SimpleBuilder id(int id) {
            this.id = id;
            return this;
        }

        public SimpleBuilder name(String name) {
            this.name = name;
            return this;
        }

        public SimpleValue build() {
            return new SimpleValue(id, name);
        }
    }

    // --- Test POJO with nullable builder property ---

    @JsonDeserialize(builder = NullableBuilder.class)
    static class NullableValue {
        private final String value;

        NullableValue(String value) {
            this.value = value;
        }

        public String getValue() { return value; }
    }

    @JsonPOJOBuilder(withPrefix = "")
    static class NullableBuilder {
        private String value;

        public NullableBuilder value(String value) {
            this.value = value;
            return this;
        }

        public NullableValue build() {
            return new NullableValue(value);
        }
    }

    // --- Test POJO with builder that returns null from a setter (simulating bug trigger) ---

    @JsonDeserialize(builder = BuggyBuilder.class)
    static class BuggyValue {
        private final String data;

        BuggyValue(String data) {
            this.data = data;
        }

        public String getData() { return data; }
    }

    @JsonPOJOBuilder(withPrefix = "")
    static class BuggyBuilder {
        private String data;

        // This setter returns null to simulate the buggy path where builder method returns null
        @JsonProperty("data")
        public BuggyBuilder setData(String data) {
            this.data = data;
            return null;  // Intentionally return null to trigger NPE in BuilderBasedDeserializer
        }

        public BuggyValue build() {
            return new BuggyValue(data);
        }
    }

    private final ObjectMapper mapper = new ObjectMapper();

    // --- Test 1: Normal deserialization with all fields ---
    @Test
    public void testNormalDeserialization() throws Exception {
        String json = "{\"id\": 42, \"name\": \"test\"}";
        SimpleValue value = mapper.readValue(json, SimpleValue.class);
        assertEquals(42, value.getId());
        assertEquals("test", value.getName());
    }

    // --- Test 2: Missing optional property (name) ---
    @Test
    public void testMissingOptionalProperty() throws Exception {
        String json = "{\"id\": 1}";
        SimpleValue value = mapper.readValue(json, SimpleValue.class);
        assertEquals(1, value.getId());
        assertNull(value.getName());
    }

    // --- Test 3: Null value for property ---
    @Test
    public void testNullPropertyValue() throws Exception {
        String json = "{\"id\": 2, \"name\": null}";
        SimpleValue value = mapper.readValue(json, SimpleValue.class);
        assertEquals(2, value.getId());
        assertNull(value.getName());
    }

    // --- Test 4: Empty JSON object ---
    @Test
    public void testEmptyObject() throws Exception {
        String json = "{}";
        SimpleValue value = mapper.readValue(json, SimpleValue.class);
        assertEquals(0, value.getId());
        assertNull(value.getName());
    }

    // --- Test 5: Null JSON (should fail) ---
    @Test(expected = com.fasterxml.jackson.databind.exc.MismatchedInputException.class)
    public void testNullJson() throws Exception {
        mapper.readValue("null", SimpleValue.class);
    }

    // --- Test 6: Builder setter returns null (bug #76 trigger) ---
    @Test
    public void testBuilderSetterReturnsNull() throws Exception {
        String json = "{\"data\": \"hello\"}";
        // This should not throw NullPointerException after fix
        BuggyValue value = mapper.readValue(json, BuggyValue.class);
        assertEquals("hello", value.getData());
    }

    // --- Test 7: Builder setter returns null with missing property ---
    @Test
    public void testBuilderSetterReturnsNullMissingProperty() throws Exception {
        String json = "{}";
        BuggyValue value = mapper.readValue(json, BuggyValue.class);
        assertNull(value.getData());
    }

    // --- Test 8: Nullable builder property with explicit null ---
    @Test
    public void testNullablePropertyExplicitNull() throws Exception {
        String json = "{\"value\": null}";
        NullableValue value = mapper.readValue(json, NullableValue.class);
        assertNull(value.getValue());
    }

    // --- Test 9: Nullable builder property missing ---
    @Test
    public void testNullablePropertyMissing() throws Exception {
        String json = "{}";
        NullableValue value = mapper.readValue(json, NullableValue.class);
        assertNull(value.getValue());
    }

    // --- Test 10: Multiple properties with mixed nulls ---
    @Test
    public void testMixedNulls() throws Exception {
        String json = "{\"id\": 10, \"name\": null}";
        SimpleValue value = mapper.readValue(json, SimpleValue.class);
        assertEquals(10, value.getId());
        assertNull(value.getName());
    }
}