package com.fasterxml.jackson.databind;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.*;

/**
 * JUnit 4 test suite for ObjectMapper, targeting high coverage and fault detection
 * (including Defects4J bug 61: default typing with Object fields causing StackOverflow).
 */
public class ObjectMapperTest {

    private ObjectMapper mapper;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
        // Disable default typing for most tests; enable explicitly in bug-specific tests.
        mapper.disable(SerializationFeature.INDENT_OUTPUT);
    }

    // ---------- Basic serialization/deserialization ----------

    @Test
    public void testSimpleBeanRoundTrip() throws IOException {
        SimpleBean bean = new SimpleBean("Alice", 30);
        String json = mapper.writeValueAsString(bean);
        SimpleBean result = mapper.readValue(json, SimpleBean.class);
        assertEquals(bean.name, result.name);
        assertEquals(bean.age, result.age);
    }

    @Test
    public void testNullBean() throws IOException {
        String json = mapper.writeValueAsString(null);
        assertNull(mapper.readValue(json, Object.class));
    }

    @Test
    public void testEmptyBean() throws IOException {
        EmptyBean bean = new EmptyBean();
        String json = mapper.writeValueAsString(bean);
        EmptyBean result = mapper.readValue(json, EmptyBean.class);
        assertNotNull(result);
    }

    // ---------- Polymorphic types with annotations ----------

    @Test
    public void testPolymorphicAnnotation() throws IOException {
        Animal dog = new Dog("Rex");
        String json = mapper.writeValueAsString(dog);
        Animal result = mapper.readValue(json, Animal.class);
        assertTrue(result instanceof Dog);
        assertEquals("Rex", ((Dog) result).name);
    }

    @Test
    public void testPolymorphicList() throws IOException {
        List<Animal> animals = Arrays.asList(new Dog("Fido"), new Cat("Whiskers"));
        String json = mapper.writeValueAsString(animals);
        List<Animal> result = mapper.readValue(json, 
                mapper.getTypeFactory().constructCollectionType(List.class, Animal.class));
        assertEquals(2, result.size());
        assertTrue(result.get(0) instanceof Dog);
        assertTrue(result.get(1) instanceof Cat);
    }

    // ---------- Default typing (bug 61 related) ----------

    @Test
    public void testDefaultTypingWithObjectField() throws IOException {
        // Enable default typing for all types (including Object)
        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(Object.class)
                .build();
        ObjectMapper mapperWithDT = new ObjectMapper();
        mapperWithDT.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL);

        Container container = new Container();
        container.value = "Hello";
        String json = mapperWithDT.writeValueAsString(container);
        Container result = mapperWithDT.readValue(json, Container.class);
        assertEquals("Hello", result.value);
    }

    @Test
    public void testDefaultTypingWithObjectArray() throws IOException {
        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(Object[].class)
                .build();
        ObjectMapper mapperWithDT = new ObjectMapper();
        mapperWithDT.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL);

        Object[] array = new Object[]{"a", 42, true};
        String json = mapperWithDT.writeValueAsString(array);
        Object[] result = mapperWithDT.readValue(json, Object[].class);
        assertEquals(3, result.length);
        assertEquals("a", result[0]);
        assertEquals(42, result[1]);
        assertEquals(true, result[2]);
    }

    @Test
    public void testDefaultTypingWithNestedObject() throws IOException {
        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(Object.class)
                .build();
        ObjectMapper mapperWithDT = new ObjectMapper();
        mapperWithDT.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL);

        NestedContainer nested = new NestedContainer();
        nested.inner = new Container();
        nested.inner.value = "nested";
        String json = mapperWithDT.writeValueAsString(nested);
        NestedContainer result = mapperWithDT.readValue(json, NestedContainer.class);
        assertEquals("nested", result.inner.value);
    }

    @Test(expected = Exception.class)
    public void testDefaultTypingWithRecursiveStructure() throws IOException {
        // This may trigger StackOverflow in buggy versions; we expect an exception or completion.
        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(Object.class)
                .build();
        ObjectMapper mapperWithDT = new ObjectMapper();
        mapperWithDT.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL);

        RecursiveContainer rec = new RecursiveContainer();
        rec.self = rec;  // self-reference
        String json = mapperWithDT.writeValueAsString(rec);
        // If it completes, try deserialization (may also fail)
        RecursiveContainer result = mapperWithDT.readValue(json, RecursiveContainer.class);
        // If we reach here, assert something
        assertNotNull(result);
    }

    // ---------- Edge cases ----------

    @Test
    public void testEmptyString() throws IOException {
        String json = "\"\"";
        String result = mapper.readValue(json, String.class);
        assertEquals("", result);
    }

    @Test
    public void testIntegerBoundary() throws IOException {
        int value = Integer.MAX_VALUE;
        String json = mapper.writeValueAsString(value);
        int result = mapper.readValue(json, Integer.class);
        assertEquals(value, result);
    }

    @Test
    public void testLongMinValue() throws IOException {
        long value = Long.MIN_VALUE;
        String json = mapper.writeValueAsString(value);
        long result = mapper.readValue(json, Long.class);
        assertEquals(value, result);
    }

    @Test
    public void testDoubleNaN() throws IOException {
        double value = Double.NaN;
        String json = mapper.writeValueAsString(value);
        double result = mapper.readValue(json, Double.class);
        assertTrue(Double.isNaN(result));
    }

    @Test
    public void testBooleanTrue() throws IOException {
        String json = "true";
        Boolean result = mapper.readValue(json, Boolean.class);
        assertTrue(result);
    }

    // ---------- Helper classes (inner static) ----------

    static class SimpleBean {
        public String name;
        public int age;

        public SimpleBean() {}
        public SimpleBean(String name, int age) { this.name = name; this.age = age; }
    }

    static class EmptyBean {
        // no fields
    }

    // Polymorphic types with annotations
    @com.fasterxml.jackson.annotation.JsonTypeInfo(use = com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME, include = com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY, property = "type")
    @com.fasterxml.jackson.annotation.JsonSubTypes({
        @com.fasterxml.jackson.annotation.JsonSubTypes.Type(value = Dog.class, name = "dog"),
        @com.fasterxml.jackson.annotation.JsonSubTypes.Type(value = Cat.class, name = "cat")
    })
    static abstract class Animal {
    }

    static class Dog extends Animal {
        public String name;
        public Dog() {}
        public Dog(String name) { this.name = name; }
    }

    static class Cat extends Animal {
        public String name;
        public Cat() {}
        public Cat(String name) { this.name = name; }
    }

    // For default typing tests
    static class Container {
        public Object value;
    }

    static class NestedContainer {
        public Container inner;
    }

    static class RecursiveContainer {
        public RecursiveContainer self;
    }
}