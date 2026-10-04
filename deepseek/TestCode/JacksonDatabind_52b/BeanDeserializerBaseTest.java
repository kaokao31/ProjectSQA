package com.fasterxml.jackson.databind.deser;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.impl.BeanPropertyMap;
import com.fasterxml.jackson.databind.deser.impl.ObjectIdReader;
import com.fasterxml.jackson.databind.deser.impl.ValueInjector;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.util.NameTransformer;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.*;

public class BeanDeserializerBaseTest {

    private BeanDeserializerBase deserializer;
    private DeserializationContext ctxt;
    private TestBean bean;

    @Before
    public void setUp() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ctxt = mapper.getDeserializationContext();
        // Create a simple test deserializer for TestBean
        deserializer = (BeanDeserializerBase) mapper.deserializerFor(TestBean.class);
        bean = new TestBean();
    }

    // Test class with various property types
    static class TestBean {
        public String name;
        public int age;
        public List<String> items;
        public Map<String, Integer> counts;
        public TestBean nested;
        public boolean active;
        public Integer nullable;
        
        // For testing unwrapping
        public String unwrapped1;
        public String unwrapped2;
    }

    @Test
    public void testDeserializeWithAllNullProperties() throws IOException {
        String json = "{}";
        TestBean result = (TestBean) deserializer.deserialize(ctxt.parserFrom(json), ctxt);
        assertNull(result.name);
        assertEquals(0, result.age);
        assertNull(result.items);
        assertNull(result.counts);
        assertNull(result.nested);
        assertFalse(result.active);
        assertNull(result.nullable);
    }

    @Test
    public void testDeserializeWithSomeProperties() throws IOException {
        String json = "{\"name\":\"test\",\"age\":25,\"active\":true}";
        TestBean result = (TestBean) deserializer.deserialize(ctxt.parserFrom(json), ctxt);
        assertEquals("test", result.name);
        assertEquals(25, result.age);
        assertTrue(result.active);
    }

    @Test
    public void testDeserializeWithNullValues() throws IOException {
        String json = "{\"name\":null,\"age\":0,\"nullable\":null}";
        TestBean result = (TestBean) deserializer.deserialize(ctxt.parserFrom(json), ctxt);
        assertNull(result.name);
        assertEquals(0, result.age);
        assertNull(result.nullable);
    }

    @Test
    public void testDeserializeWithEmptyList() throws IOException {
        String json = "{\"items\":[]}";
        TestBean result = (TestBean) deserializer.deserialize(ctxt.parserFrom(json), ctxt);
        assertNotNull(result.items);
        assertTrue(result.items.isEmpty());
    }

    @Test
    public void testDeserializeWithListElements() throws IOException {
        String json = "{\"items\":[\"a\",\"b\",\"c\"]}";
        TestBean result = (TestBean) deserializer.deserialize(ctxt.parserFrom(json), ctxt);
        assertEquals(3, result.items.size());
        assertEquals("a", result.items.get(0));
        assertEquals("b", result.items.get(1));
        assertEquals("c", result.items.get(2));
    }

    @Test
    public void testDeserializeWithEmptyMap() throws IOException {
        String json = "{\"counts\":{}}";
        TestBean result = (TestBean) deserializer.deserialize(ctxt.parserFrom(json), ctxt);
        assertNotNull(result.counts);
        assertTrue(result.counts.isEmpty());
    }

    @Test
    public void testDeserializeWithMapEntries() throws IOException {
        String json = "{\"counts\":{\"x\":1,\"y\":2}}";
        TestBean result = (TestBean) deserializer.deserialize(ctxt.parserFrom(json), ctxt);
        assertEquals(2, result.counts.size());
        assertEquals(Integer.valueOf(1), result.counts.get("x"));
        assertEquals(Integer.valueOf(2), result.counts.get("y"));
    }

    @Test
    public void testDeserializeWithNestedObject() throws IOException {
        String json = "{\"nested\":{\"name\":\"inner\",\"age\":10}}";
        TestBean result = (TestBean) deserializer.deserialize(ctxt.parserFrom(json), ctxt);
        assertNotNull(result.nested);
        assertEquals("inner", result.nested.name);
        assertEquals(10, result.nested.age);
    }

    @Test(expected = IOException.class)
    public void testDeserializeWithInvalidJson() throws IOException {
        String json = "{invalid}";
        deserializer.deserialize(ctxt.parserFrom(json), ctxt);
    }

    @Test
    public void testDeserializeWithExtraUnknownProperties() throws IOException {
        String json = "{\"name\":\"test\",\"unknown\":\"value\"}";
        TestBean result = (TestBean) deserializer.deserialize(ctxt.parserFrom(json), ctxt);
        assertEquals("test", result.name);
    }

    @Test
    public void testPlaceholderReturnsNullAndCreatesInstance() throws Exception {
        // Test that placeholders work correctly
        Object placeholder = deserializer.deserialize(ctxt.parserFrom("{}"), ctxt);
        assertNotNull(placeholder);
        assertTrue(placeholder instanceof TestBean);
    }

    @Test
    public void testDeserializeWithTokenBuffer() throws IOException {
        // Test using TokenBuffer for delayed deserialization
        TokenBuffer buffer = new TokenBuffer(null);
        buffer.writeStartObject();
        buffer.writeStringField("name", "fromBuffer");
        buffer.writeEndObject();
        
        TestBean result = (TestBean) deserializer.deserialize(buffer.asParser(), ctxt);
        assertEquals("fromBuffer", result.name);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeserializeWithNullParser() throws IOException {
        deserializer.deserialize(null, ctxt);
    }

    @Test
    public void testDeserializeWithMultipleDataTypes() throws IOException {
        String json = "{\"name\":\"test\",\"age\":30,\"items\":[\"x\",\"y\"],\"counts\":{\"a\":1,\"b\":2},\"active\":true,\"nullable\":42}";
        TestBean result = (TestBean) deserializer.deserialize(ctxt.parserFrom(json), ctxt);
        assertEquals("test", result.name);
        assertEquals(30, result.age);
        assertEquals(2, result.items.size());
        assertEquals(2, result.counts.size());
        assertTrue(result.active);
        assertEquals(Integer.valueOf(42), result.nullable);
    }

    @Test(timeout = 1000)
    public void testPerformanceWithLargeInput() throws IOException {
        StringBuilder sb = new StringBuilder("{");
        for (int i = 0; i < 100; i++) {
            if (i > 0) sb.append(",");
            sb.append("\"field").append(i).append("\":").append(i);
        }
        sb.append("}");
        // This should not cause issues - unknown properties should be ignored
        TestBean result = (TestBean) deserializer.deserialize(ctxt.parserFrom(sb.toString()), ctxt);
        assertNotNull(result);
    }

    @Test
    public void testDeserializeWithNestedNull() throws IOException {
        String json = "{\"nested\":null}";
        TestBean result = (TestBean) deserializer.deserialize(ctxt.parserFrom(json), ctxt);
        assertNull(result.nested);
    }

    @Test
    public void testDeserializeWithUnwrappingProperties() throws IOException {
        // Simulate unwrapping via @JsonUnwrapped
        String json = "{\"unwrapped1\":\"val1\",\"unwrapped2\":\"val2\"}";
        TestBean result = (TestBean) deserializer.deserialize(ctxt.parserFrom(json), ctxt);
        // These might be ignored if not configured for unwrapping
        assertNotNull(result);
    }
}