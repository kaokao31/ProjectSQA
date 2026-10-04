package com.fasterxml.jackson.databind.deser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.*;

public class BasicDeserializerFactoryTest {

    private ObjectMapper mapper;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
    }

    // Simple bean for basic deserialization
    static class SimpleBean {
        public int x;
        public String name;
        // default constructor needed for Jackson
        public SimpleBean() {}
        public SimpleBean(int x, String name) { this.x = x; this.name = name; }
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SimpleBean that = (SimpleBean) o;
            return x == that.x && Objects.equals(name, that.name);
        }
        @Override
        public int hashCode() { return Objects.hash(x, name); }
    }

    // Generic bean that was problematic in bug 25
    static class GenericBean<T> {
        public T value;
        public GenericBean() {}
        public GenericBean(T value) { this.value = value; }
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            GenericBean<?> that = (GenericBean<?>) o;
            return Objects.equals(value, that.value);
        }
        @Override
        public int hashCode() { return Objects.hash(value); }
    }

    // Bean with nested generic property
    static class WrapperBean {
        public GenericBean<String> wrapped;
        public WrapperBean() {}
        public WrapperBean(GenericBean<String> wrapped) { this.wrapped = wrapped; }
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            WrapperBean that = (WrapperBean) o;
            return Objects.equals(wrapped, that.wrapped);
        }
        @Override
        public int hashCode() { return Objects.hash(wrapped); }
    }

    // Bean with primitive and wrapper edges
    static class EdgeBean {
        public int minInt = Integer.MIN_VALUE;
        public int maxInt = Integer.MAX_VALUE;
        public double nanDouble = Double.NaN;
        public String emptyString = "";
        public Object nullField = null;
        public EdgeBean() {}
    }

    // ========== Test Cases ==========

    @Test
    public void testSimpleBeanDeserialization() throws IOException {
        String json = "{\"x\":42,\"name\":\"test\"}";
        SimpleBean result = mapper.readValue(json, SimpleBean.class);
        assertEquals(42, result.x);
        assertEquals("test", result.name);
    }

    @Test
    public void testGenericBeanDeserialization() throws IOException {
        // This triggers the bug in Defects4J JacksonDatabind-25
        // when type variables are not properly resolved
        TypeReference<GenericBean<String>> typeRef = new TypeReference<GenericBean<String>>() {};
        GenericBean<String> result = mapper.readValue("{\"value\":\"hello\"}", typeRef);
        assertEquals("hello", result.value);
    }

    @Test
    public void testGenericBeanWithInteger() throws IOException {
        TypeReference<GenericBean<Integer>> typeRef = new TypeReference<GenericBean<Integer>>() {};
        GenericBean<Integer> result = mapper.readValue("{\"value\":123}", typeRef);
        assertEquals(Integer.valueOf(123), result.value);
    }

    @Test
    public void testNestedGenericBean() throws IOException {
        String json = "{\"wrapped\":{\"value\":\"nested\"}}";
        WrapperBean result = mapper.readValue(json, WrapperBean.class);
        assertEquals("nested", result.wrapped.value);
    }

    @Test
    public void testNullInput() throws IOException {
        // Deserializing from null should not throw; return null
        SimpleBean result = mapper.readValue("null", SimpleBean.class);
        assertNull(result);
    }

    @Test
    public void testEmptyJsonObject() throws IOException {
        SimpleBean result = mapper.readValue("{}", SimpleBean.class);
        assertEquals(0, result.x);
        assertNull(result.name);
    }

    @Test
    public void testBoundaryValues() throws IOException {
        String json = "{\"minInt\":-2147483648,\"maxInt\":2147483647,\"nanDouble\":NaN,\"emptyString\":\"\",\"nullField\":null}";
        EdgeBean result = mapper.readValue(json, EdgeBean.class);
        assertEquals(Integer.MIN_VALUE, result.minInt);
        assertEquals(Integer.MAX_VALUE, result.maxInt);
        assertTrue(Double.isNaN(result.nanDouble));
        assertEquals("", result.emptyString);
        assertNull(result.nullField);
    }

    @Test
    public void testDeserializationWithMissingProperty() throws IOException {
        // Should use defaults
        String json = "{\"name\":\"only\"}";
        SimpleBean result = mapper.readValue(json, SimpleBean.class);
        assertEquals(0, result.x);
        assertEquals("only", result.name);
    }

    @Test(expected = IOException.class)
    public void testInvalidJson() throws IOException {
        mapper.readValue("{invalid}", SimpleBean.class);
    }

    @Test
    public void testGenericListDeserialization() throws IOException {
        // This also exercises type resolution
        TypeReference<List<String>> typeRef = new TypeReference<List<String>>() {};
        List<String> result = mapper.readValue("[\"a\",\"b\",\"c\"]", typeRef);
        assertEquals(Arrays.asList("a","b","c"), result);
    }

    @Test
    public void testGenericMapDeserialization() throws IOException {
        TypeReference<Map<String, Integer>> typeRef = new TypeReference<Map<String, Integer>>() {};
        Map<String, Integer> result = mapper.readValue("{\"one\":1,\"two\":2}", typeRef);
        assertEquals(Integer.valueOf(1), result.get("one"));
        assertEquals(Integer.valueOf(2), result.get("two"));
    }
}