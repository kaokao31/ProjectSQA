package com.fasterxml.jackson.databind.deser;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class BasicDeserializerFactoryTest {

    private BasicDeserializerFactory factory;
    private DeserializationConfig config;
    private TypeFactory typeFactory;

    @Before
    public void setUp() {
        factory = new BasicDeserializerFactory();
        // Use default ObjectMapper to get config and type factory
        ObjectMapper mapper = new ObjectMapper();
        config = mapper.getDeserializationConfig();
        typeFactory = mapper.getTypeFactory();
    }

    // Test creation of deserializer for simple types
    @Test
    public void testCreateDeserializerForString() throws JsonMappingException {
        JavaType stringType = typeFactory.constructType(String.class);
        JsonDeserializer<?> deser = factory.createDeserializer(config, stringType, null);
        assertNotNull("Deserializer for String should not be null", deser);
        assertTrue("Deserializer should be an instance of StdDeserializer", deser instanceof StdDeserializer);
    }

    @Test
    public void testCreateDeserializerForInteger() throws JsonMappingException {
        JavaType intType = typeFactory.constructType(Integer.class);
        JsonDeserializer<?> deser = factory.createDeserializer(config, intType, null);
        assertNotNull("Deserializer for Integer should not be null", deser);
    }

    @Test
    public void testCreateDeserializerForList() throws JsonMappingException {
        JavaType listType = typeFactory.constructCollectionType(ArrayList.class, String.class);
        JsonDeserializer<?> deser = factory.createDeserializer(config, listType, null);
        assertNotNull("Deserializer for List<String> should not be null", deser);
    }

    @Test
    public void testCreateDeserializerForMap() throws JsonMappingException {
        JavaType mapType = typeFactory.constructMapType(HashMap.class, String.class, Integer.class);
        JsonDeserializer<?> deser = factory.createDeserializer(config, mapType, null);
        assertNotNull("Deserializer for Map<String,Integer> should not be null", deser);
    }

    @Test
    public void testCreateDeserializerForCustomBean() throws JsonMappingException {
        JavaType beanType = typeFactory.constructType(SimpleBean.class);
        JsonDeserializer<?> deser = factory.createDeserializer(config, beanType, null);
        assertNotNull("Deserializer for SimpleBean should not be null", deser);
    }

    // Test creation with null type (should throw exception)
    @Test(expected = IllegalArgumentException.class)
    public void testCreateDeserializerWithNullType() throws JsonMappingException {
        factory.createDeserializer(config, null, null);
    }

    // Test creation with unknown type (e.g., abstract class)
    @Test(expected = JsonMappingException.class)
    public void testCreateDeserializerForAbstractType() throws JsonMappingException {
        JavaType abstractType = typeFactory.constructType(AbstractList.class);
        factory.createDeserializer(config, abstractType, null);
    }

    // Test creation for array types
    @Test
    public void testCreateDeserializerForArray() throws JsonMappingException {
        JavaType arrayType = typeFactory.constructArrayType(String.class);
        JsonDeserializer<?> deser = factory.createDeserializer(config, arrayType, null);
        assertNotNull("Deserializer for String[] should not be null", deser);
    }

    // Test creation for enum type
    @Test
    public void testCreateDeserializerForEnum() throws JsonMappingException {
        JavaType enumType = typeFactory.constructType(TestEnum.class);
        JsonDeserializer<?> deser = factory.createDeserializer(config, enumType, null);
        assertNotNull("Deserializer for TestEnum should not be null", deser);
    }

    // Test creation for Object type
    @Test
    public void testCreateDeserializerForObject() throws JsonMappingException {
        JavaType objectType = typeFactory.constructType(Object.class);
        JsonDeserializer<?> deser = factory.createDeserializer(config, objectType, null);
        assertNotNull("Deserializer for Object should not be null", deser);
    }

    // Test creation with custom deserializer (via annotation) - not directly testable without annotations
    // Instead test that factory returns a deserializer for a type that has a custom deserializer registered
    // This requires a module, but we can test the fallback behavior

    // Test that createBeanDeserializer returns a BeanDeserializer for a simple bean
    @Test
    public void testCreateBeanDeserializer() throws JsonMappingException {
        JavaType beanType = typeFactory.constructType(SimpleBean.class);
        JsonDeserializer<?> deser = factory.createBeanDeserializer(config, beanType, null);
        assertNotNull("Bean deserializer should not be null", deser);
        // BeanDeserializer is a specific type, but we can check it's not null
    }

    // Test that createBeanDeserializer returns null for non-bean types (like String)
    @Test
    public void testCreateBeanDeserializerForNonBean() throws JsonMappingException {
        JavaType stringType = typeFactory.constructType(String.class);
        JsonDeserializer<?> deser = factory.createBeanDeserializer(config, stringType, null);
        assertNull("Bean deserializer for String should be null", deser);
    }

    // Test _findCustomDeserializer (if accessible) - we can use reflection to test private methods
    // But for simplicity, we assume it's package-private and test via createDeserializer with a custom module
    // Since we don't have a custom module, we skip

    // Test handling of type with type parameters (generics)
    @Test
    public void testCreateDeserializerForParameterizedType() throws JsonMappingException {
        JavaType paramType = typeFactory.constructParametricType(SomeGenericClass.class, String.class);
        JsonDeserializer<?> deser = factory.createDeserializer(config, paramType, null);
        assertNotNull("Deserializer for parameterized type should not be null", deser);
    }

    // Test edge case: empty bean (no properties)
    @Test
    public void testCreateDeserializerForEmptyBean() throws JsonMappingException {
        JavaType emptyBeanType = typeFactory.constructType(EmptyBean.class);
        JsonDeserializer<?> deser = factory.createDeserializer(config, emptyBeanType, null);
        assertNotNull("Deserializer for EmptyBean should not be null", deser);
    }

    // Test that factory handles null config gracefully (should throw)
    @Test(expected = IllegalArgumentException.class)
    public void testCreateDeserializerWithNullConfig() throws JsonMappingException {
        JavaType stringType = typeFactory.constructType(String.class);
        factory.createDeserializer(null, stringType, null);
    }

    // Test that factory handles null property (should be fine)
    @Test
    public void testCreateDeserializerWithNullProperty() throws JsonMappingException {
        JavaType stringType = typeFactory.constructType(String.class);
        JsonDeserializer<?> deser = factory.createDeserializer(config, stringType, null);
        assertNotNull("Deserializer with null property should not be null", deser);
    }

    // Helper classes
    static class SimpleBean {
        public String name;
        public int value;
    }

    static class EmptyBean {
        // no properties
    }

    static class SomeGenericClass<T> {
        public T data;
    }

    enum TestEnum {
        A, B, C
    }
}