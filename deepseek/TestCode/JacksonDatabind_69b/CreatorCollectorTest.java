package com.fasterxml.jackson.databind.deser.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test suite for CreatorCollector, focusing on bug #69:
 * null values for properties with default values should use the default.
 */
public class CreatorCollectorTest {

    private ObjectMapper mapper;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
    }

    // POJO with constructor parameter having a default value
    static class BeanWithDefault {
        private final String name;

        @JsonCreator
        public BeanWithDefault(@JsonProperty(value = "name", defaultValue = "defaultName") String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    // POJO with constructor parameter without default
    static class BeanWithoutDefault {
        private final String name;

        @JsonCreator
        public BeanWithoutDefault(@JsonProperty("name") String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    // POJO with primitive int default
    static class BeanWithPrimitiveDefault {
        private final int count;

        @JsonCreator
        public BeanWithPrimitiveDefault(@JsonProperty(value = "count", defaultValue = "42") int count) {
            this.count = count;
        }

        public int getCount() {
            return count;
        }
    }

    // POJO with wrapper Integer default
    static class BeanWithWrapperDefault {
        private final Integer value;

        @JsonCreator
        public BeanWithWrapperDefault(@JsonProperty(value = "value", defaultValue = "100") Integer value) {
            this.value = value;
        }

        public Integer getValue() {
            return value;
        }
    }

    @Test
    public void testNullValueForPropertyWithDefault() throws Exception {
        // JSON provides null for property that has a default value
        String json = "{\"name\":null}";
        BeanWithDefault bean = mapper.readValue(json, BeanWithDefault.class);
        // Bug #69: should use default "defaultName", not null
        assertEquals("defaultName", bean.getName());
    }

    @Test
    public void testMissingPropertyWithDefault() throws Exception {
        // JSON missing the property entirely
        String json = "{}";
        BeanWithDefault bean = mapper.readValue(json, BeanWithDefault.class);
        assertEquals("defaultName", bean.getName());
    }

    @Test
    public void testNonNullValueForPropertyWithDefault() throws Exception {
        // JSON provides explicit non-null value
        String json = "{\"name\":\"custom\"}";
        BeanWithDefault bean = mapper.readValue(json, BeanWithDefault.class);
        assertEquals("custom", bean.getName());
    }

    @Test
    public void testNullValueForPropertyWithoutDefault() throws Exception {
        // Property without default: null should remain null
        String json = "{\"name\":null}";
        BeanWithoutDefault bean = mapper.readValue(json, BeanWithoutDefault.class);
        assertNull(bean.getName());
    }

    @Test
    public void testMissingPropertyWithoutDefault() throws Exception {
        // Missing property without default: should be null (since no default)
        String json = "{}";
        BeanWithoutDefault bean = mapper.readValue(json, BeanWithoutDefault.class);
        assertNull(bean.getName());
    }

    @Test
    public void testNullValueForPrimitiveDefault() throws Exception {
        // Primitive int with default: null should trigger default
        String json = "{\"count\":null}";
        BeanWithPrimitiveDefault bean = mapper.readValue(json, BeanWithPrimitiveDefault.class);
        assertEquals(42, bean.getCount());
    }

    @Test
    public void testMissingPrimitiveDefault() throws Exception {
        // Missing primitive with default
        String json = "{}";
        BeanWithPrimitiveDefault bean = mapper.readValue(json, BeanWithPrimitiveDefault.class);
        assertEquals(42, bean.getCount());
    }

    @Test
    public void testNonNullPrimitiveDefault() throws Exception {
        // Explicit non-null primitive
        String json = "{\"count\":7}";
        BeanWithPrimitiveDefault bean = mapper.readValue(json, BeanWithPrimitiveDefault.class);
        assertEquals(7, bean.getCount());
    }

    @Test
    public void testNullValueForWrapperDefault() throws Exception {
        // Wrapper Integer with default: null should use default
        String json = "{\"value\":null}";
        BeanWithWrapperDefault bean = mapper.readValue(json, BeanWithWrapperDefault.class);
        assertEquals(Integer.valueOf(100), bean.getValue());
    }

    @Test
    public void testMissingWrapperDefault() throws Exception {
        // Missing wrapper with default
        String json = "{}";
        BeanWithWrapperDefault bean = mapper.readValue(json, BeanWithWrapperDefault.class);
        assertEquals(Integer.valueOf(100), bean.getValue());
    }

    @Test
    public void testNonNullWrapperDefault() throws Exception {
        // Explicit non-null wrapper
        String json = "{\"value\":200}";
        BeanWithWrapperDefault bean = mapper.readValue(json, BeanWithWrapperDefault.class);
        assertEquals(Integer.valueOf(200), bean.getValue());
    }

    // Additional edge case: default value is empty string
    static class BeanWithEmptyDefault {
        private final String text;

        @JsonCreator
        public BeanWithEmptyDefault(@JsonProperty(value = "text", defaultValue = "") String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }
    }

    @Test
    public void testNullValueForEmptyDefault() throws Exception {
        String json = "{\"text\":null}";
        BeanWithEmptyDefault bean = mapper.readValue(json, BeanWithEmptyDefault.class);
        assertEquals("", bean.getText());
    }

    @Test
    public void testMissingEmptyDefault() throws Exception {
        String json = "{}";
        BeanWithEmptyDefault bean = mapper.readValue(json, BeanWithEmptyDefault.class);
        assertEquals("", bean.getText());
    }
}