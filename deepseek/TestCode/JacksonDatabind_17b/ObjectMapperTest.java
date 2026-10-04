package com.fasterxml.jackson.databind;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.Assert.*;

/**
 * Comprehensive JUnit 4 test suite for ObjectMapper targeting high coverage and fault detection,
 * including the specific bug related to @JsonUnwrapped with null values (Defects4J bug 17).
 */
public class ObjectMapperTest {

    private ObjectMapper mapper;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
    }

    // ---------- Basic serialization/deserialization ----------

    @Test
    public void testSerializeSimpleBean() throws Exception {
        SimpleBean bean = new SimpleBean("test", 42);
        String json = mapper.writeValueAsString(bean);
        assertTrue(json.contains("\"name\":\"test\""));
        assertTrue(json.contains("\"value\":42"));
    }

    @Test
    public void testDeserializeSimpleBean() throws Exception {
        String json = "{\"name\":\"test\",\"value\":42}";
        SimpleBean bean = mapper.readValue(json, SimpleBean.class);
        assertEquals("test", bean.name);
        assertEquals(42, bean.value);
    }

    @Test
    public void testSerializeNullValue() throws Exception {
        NullBean bean = new NullBean(null);
        String json = mapper.writeValueAsString(bean);
        assertTrue(json.contains("\"value\":null"));
    }

    @Test
    public void testDeserializeNullValue() throws Exception {
        String json = "{\"value\":null}";
        NullBean bean = mapper.readValue(json, NullBean.class);
        assertNull(bean.value);
    }

    @Test
    public void testSerializeEmptyString() throws Exception {
        SimpleBean bean = new SimpleBean("", 0);
        String json = mapper.writeValueAsString(bean);
        assertTrue(json.contains("\"name\":\"\""));
    }

    @Test
    public void testDeserializeEmptyString() throws Exception {
        String json = "{\"name\":\"\",\"value\":0}";
        SimpleBean bean = mapper.readValue(json, SimpleBean.class);
        assertEquals("", bean.name);
    }

    @Test
    public void testSerializeSpecialCharacters() throws Exception {
        SimpleBean bean = new SimpleBean("line1\nline2\t\"quote\"", 1);
        String json = mapper.writeValueAsString(bean);
        assertTrue(json.contains("\\n"));
        assertTrue(json.contains("\\t"));
        assertTrue(json.contains("\\\""));
    }

    @Test
    public void testDeserializeSpecialCharacters() throws Exception {
        String json = "{\"name\":\"line1\\nline2\\t\\\"quote\\\"\",\"value\":1}";
        SimpleBean bean = mapper.readValue(json, SimpleBean.class);
        assertEquals("line1\nline2\t\"quote\"", bean.name);
    }

    // ---------- Null input / edge cases ----------

    @Test(expected = NullPointerException.class)
    public void testWriteValueAsStringNullBean() throws Exception {
        mapper.writeValueAsString(null);
    }

    @Test(expected = NullPointerException.class)
    public void testReadValueNullString() throws Exception {
        mapper.readValue((String) null, SimpleBean.class);
    }

    @Test(expected = IOException.class)
    public void testReadValueEmptyString() throws Exception {
        mapper.readValue("", SimpleBean.class);
    }

    @Test(expected = IOException.class)
    public void testReadValueMalformedJson() throws Exception {
        mapper.readValue("{invalid}", SimpleBean.class);
    }

    // ---------- @JsonUnwrapped tests (bug 17 related) ----------

    @Test
    public void testSerializeUnwrappedNullValue() throws Exception {
        WrapperBean wrapper = new WrapperBean();
        wrapper.inner = null;
        String json = mapper.writeValueAsString(wrapper);
        // Should not throw NPE; inner null should be serialized as empty or null fields
        assertNotNull(json);
    }

    @Test
    public void testDeserializeUnwrappedNullValue() throws Exception {
        String json = "{\"name\":\"test\",\"value\":null}";
        WrapperBean wrapper = mapper.readValue(json, WrapperBean.class);
        assertNotNull(wrapper);
        assertNull(wrapper.inner);
    }

    @Test
    public void testDeserializeUnwrappedWithNullInnerFields() throws Exception {
        // JSON with null for unwrapped fields
        String json = "{\"name\":null,\"value\":null}";
        WrapperBean wrapper = mapper.readValue(json, WrapperBean.class);
        assertNotNull(wrapper);
        assertNull(wrapper.inner);
    }

    @Test
    public void testSerializeUnwrappedWithNonNullInner() throws Exception {
        WrapperBean wrapper = new WrapperBean();
        wrapper.inner = new InnerBean("hello", 99);
        String json = mapper.writeValueAsString(wrapper);
        assertTrue(json.contains("\"name\":\"hello\""));
        assertTrue(json.contains("\"value\":99"));
    }

    @Test
    public void testDeserializeUnwrappedWithNonNullInner() throws Exception {
        String json = "{\"name\":\"hello\",\"value\":99}";
        WrapperBean wrapper = mapper.readValue(json, WrapperBean.class);
        assertNotNull(wrapper.inner);
        assertEquals("hello", wrapper.inner.name);
        assertEquals(99, wrapper.inner.value);
    }

    // ---------- Polymorphic type handling ----------

    @Test
    public void testSerializePolymorphic() throws Exception {
        Animal dog = new Dog("Rex");
        String json = mapper.writeValueAsString(dog);
        assertTrue(json.contains("\"@type\":\"dog\""));
        assertTrue(json.contains("\"name\":\"Rex\""));
    }

    @Test
    public void testDeserializePolymorphic() throws Exception {
        String json = "{\"@type\":\"dog\",\"name\":\"Rex\"}";
        Animal animal = mapper.readValue(json, Animal.class);
        assertTrue(animal instanceof Dog);
        assertEquals("Rex", ((Dog) animal).name);
    }

    // ---------- Date handling ----------

    @Test
    public void testSerializeDate() throws Exception {
        DateBean bean = new DateBean(new Date(0));
        String json = mapper.writeValueAsString(bean);
        assertTrue(json.contains("\"date\":"));
    }

    @Test
    public void testDeserializeDate() throws Exception {
        String json = "{\"date\":\"1970-01-01T00:00:00.000+0000\"}";
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
        DateBean bean = mapper.readValue(json, DateBean.class);
        assertEquals(new Date(0), bean.date);
    }

    // ---------- Configuration / features ----------

    @Test
    public void testFailOnUnknownProperties() throws Exception {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
        String json = "{\"name\":\"test\",\"unknown\":\"value\"}";
        try {
            mapper.readValue(json, SimpleBean.class);
            fail("Should have thrown UnrecognizedPropertyException");
        } catch (UnrecognizedPropertyException e) {
            // expected
        }
    }

    @Test
    public void testIgnoreUnknownProperties() throws Exception {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        String json = "{\"name\":\"test\",\"unknown\":\"value\"}";
        SimpleBean bean = mapper.readValue(json, SimpleBean.class);
        assertEquals("test", bean.name);
    }

    @Test
    public void testIndentOutput() throws Exception {
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        SimpleBean bean = new SimpleBean("test", 1);
        String json = mapper.writeValueAsString(bean);
        assertTrue(json.contains("\n"));
    }

    // ---------- Collections ----------

    @Test
    public void testSerializeList() throws Exception {
        List<SimpleBean> list = Arrays.asList(new SimpleBean("a", 1), new SimpleBean("b", 2));
        String json = mapper.writeValueAsString(list);
        assertTrue(json.startsWith("["));
        assertTrue(json.endsWith("]"));
    }

    @Test
    public void testDeserializeList() throws Exception {
        String json = "[{\"name\":\"a\",\"value\":1},{\"name\":\"b\",\"value\":2}]";
        List<SimpleBean> list = mapper.readValue(json, mapper.getTypeFactory().constructCollectionType(List.class, SimpleBean.class));
        assertEquals(2, list.size());
        assertEquals("a", list.get(0).name);
    }

    @Test
    public void testSerializeMap() throws Exception {
        Map<String, Integer> map = new HashMap<>();
        map.put("one", 1);
        map.put("two", 2);
        String json = mapper.writeValueAsString(map);
        assertTrue(json.contains("\"one\":1"));
    }

    @Test
    public void testDeserializeMap() throws Exception {
        String json = "{\"one\":1,\"two\":2}";
        Map<String, Integer> map = mapper.readValue(json, Map.class);
        assertEquals(2, map.size());
        assertEquals(1, map.get("one"));
    }

    // ---------- Helper classes ----------

    static class SimpleBean {
        public String name;
        public int value;

        public SimpleBean() {}
        public SimpleBean(String name, int value) {
            this.name = name;
            this.value = value;
        }
    }

    static class NullBean {
        public String value;

        public NullBean() {}
        public NullBean(String value) {
            this.value = value;
        }
    }

    static class InnerBean {
        public String name;
        public int value;

        public InnerBean() {}
        public InnerBean(String name, int value) {
            this.name = name;
            this.value = value;
        }
    }

    static class WrapperBean {
        @JsonUnwrapped
        public InnerBean inner;
    }

    // Polymorphic types
    @com.fasterxml.jackson.annotation.JsonTypeInfo(use = com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME, property = "@type")
    @com.fasterxml.jackson.annotation.JsonSubTypes({
        @com.fasterxml.jackson.annotation.JsonSubTypes.Type(value = Dog.class, name = "dog")
    })
    static abstract class Animal {
        public String name;
    }

    static class Dog extends Animal {
        public Dog() {}
        public Dog(String name) { this.name = name; }
    }

    static class DateBean {
        public Date date;

        public DateBean() {}
        public DateBean(Date date) { this.date = date; }
    }
}