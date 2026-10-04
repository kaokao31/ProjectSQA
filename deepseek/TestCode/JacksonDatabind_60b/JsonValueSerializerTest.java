package com.fasterxml.jackson.databind.ser.std;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class JsonValueSerializerTest {

    public static class BasicBean {
        @JsonValue
        public String value() {
            return "basic";
        }
    }

    public static class FieldBean {
        @JsonValue
        public String value = "field";
    }

    public static class NullBean {
        @JsonValue
        public String value() {
            return null;
        }
    }

    public static class IntegerBean {
        @JsonValue
        public int value() {
            return 42;
        }
    }

    public static class BooleanBean {
        @JsonValue
        public boolean value() {
            return true;
        }
    }

    public static class ListBean {
        @JsonValue
        public List<Integer> value() {
            return Arrays.asList(1, 2, 3);
        }
    }

    public static class MapBean {
        @JsonValue
        public Map<String, Integer> value() {
            Map<String, Integer> map = new LinkedHashMap<String, Integer>();
            map.put("a", 1);
            return map;
        }
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.WRAPPER_ARRAY)
    @JsonTypeName("TypeInfoValue")
    public static class TypeInfoValue {
        @JsonValue
        public String value() {
            return "tv";
        }
    }

    public static class WrappedValue {
        @JsonValue
        public String value() {
            return "wrapped";
        }
    }

    public static class TypedWrapper {
        @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.WRAPPER_ARRAY)
        public Object value;

        public TypedWrapper() {
        }

        public TypedWrapper(Object v) {
            this.value = v;
        }
    }

    public static class ExceptionBean {
        @JsonValue
        public String value() throws Exception {
            throw new IllegalStateException("boom");
        }
    }

    @Test
    public void testSimpleJsonValue() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        assertEquals("\"basic\"", mapper.writeValueAsString(new BasicBean()));
    }

    @Test
    public void testFieldJsonValue() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        assertEquals("\"field\"", mapper.writeValueAsString(new FieldBean()));
    }

    @Test
    public void testNullJsonValue() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        assertEquals("null", mapper.writeValueAsString(new NullBean()));
    }

    @Test
    public void testIntegerJsonValue() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        assertEquals("42", mapper.writeValueAsString(new IntegerBean()));
    }

    @Test
    public void testBooleanJsonValue() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        assertEquals("true", mapper.writeValueAsString(new BooleanBean()));
    }

    @Test
    public void testListJsonValue() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        assertEquals("[1,2,3]", mapper.writeValueAsString(new ListBean()));
    }

    @Test
    public void testMapJsonValue() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        assertEquals("{\"a\":1}", mapper.writeValueAsString(new MapBean()));
    }

    @Test
    public void testJsonValueWithTypeInfo() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        assertEquals("[\"TypeInfoValue\",\"tv\"]",
                mapper.writeValueAsString(new TypeInfoValue()));
    }

    @Test
    public void testPolymorphicTypeInfoViaWrapper() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(new TypedWrapper(new WrappedValue()));
        assertTrue("Expected type id for wrapped @JsonValue, got: " + json,
                json.contains("WrappedValue"));
        assertTrue("Expected serialized @JsonValue content, got: " + json,
                json.contains("\"wrapped\""));
    }

    @Test
    public void testExceptionInJsonValueIsWrapped() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValueAsString(new ExceptionBean());
            fail("Should have thrown JsonMappingException");
        } catch (JsonMappingException e) {
            assertNotNull(e.getCause());
            assertEquals("boom", e.getCause().getMessage());
        }
    }
}