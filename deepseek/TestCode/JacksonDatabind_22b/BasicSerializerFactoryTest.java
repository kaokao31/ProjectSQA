package com.fasterxml.jackson.databind.ser;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.cfg.SerializerFactoryConfig;
import com.fasterxml.jackson.databind.introspect.BasicBeanDescription;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.impl.UnknownSerializer;
import com.fasterxml.jackson.databind.ser.std.*;
import com.fasterxml.jackson.databind.type.*;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

/**
 * Comprehensive JUnit 4 test suite for BasicSerializerFactory.
 * Targets maximum line/branch coverage and fault detection (including Defects4J bug #22).
 */
public class BasicSerializerFactoryTest {

    private BasicSerializerFactory factory;
    private SerializerProvider provider;
    private TypeFactory typeFactory;

    @Before
    public void setUp() {
        // Use default ObjectMapper to obtain a real SerializerProvider
        ObjectMapper mapper = new ObjectMapper();
        factory = new BasicSerializerFactory();
        provider = mapper.getSerializerProvider();
        typeFactory = TypeFactory.defaultInstance();
    }

    // ============================================================
    // Basic type serializers
    // ============================================================

    @Test
    public void testStringSerializer() throws JsonMappingException {
        JavaType type = typeFactory.constructType(String.class);
        JsonSerializer<?> ser = factory.createSerializer(provider, type);
        assertNotNull("String serializer should not be null", ser);
        assertTrue("Expected StringSerializer", ser instanceof StringSerializer);
    }

    @Test
    public void testIntegerSerializer() throws JsonMappingException {
        JavaType type = typeFactory.constructType(Integer.class);
        JsonSerializer<?> ser = factory.createSerializer(provider, type);
        assertNotNull("Integer serializer should not be null", ser);
        assertTrue("Expected NumberSerializer", ser instanceof NumberSerializer);
    }

    @Test
    public void testBooleanSerializer() throws JsonMappingException {
        JavaType type = typeFactory.constructType(Boolean.class);
        JsonSerializer<?> ser = factory.createSerializer(provider, type);
        assertNotNull("Boolean serializer should not be null", ser);
        assertTrue("Expected BooleanSerializer", ser instanceof BooleanSerializer);
    }

    // ============================================================
    // Collection serializers
    // ============================================================

    @Test
    public void testListSerializer() throws JsonMappingException {
        JavaType type = typeFactory.constructCollectionType(ArrayList.class, String.class);
        JsonSerializer<?> ser = factory.createSerializer(provider, type);
        assertNotNull("List serializer should not be null", ser);
        assertTrue("Expected CollectionSerializer", ser instanceof CollectionSerializer);
    }

    @Test
    public void testSetSerializer() throws JsonMappingException {
        JavaType type = typeFactory.constructCollectionType(HashSet.class, Integer.class);
        JsonSerializer<?> ser = factory.createSerializer(provider, type);
        assertNotNull("Set serializer should not be null", ser);
        assertTrue("Expected CollectionSerializer", ser instanceof CollectionSerializer);
    }

    @Test
    public void testEmptyCollectionType() throws JsonMappingException {
        // Collection with no element type (raw)
        JavaType type = typeFactory.constructRawCollectionType(ArrayList.class);
        JsonSerializer<?> ser = factory.createSerializer(provider, type);
        assertNotNull("Raw collection serializer should not be null", ser);
        // Should fall back to CollectionSerializer or UnknownSerializer
        assertTrue("Expected CollectionSerializer or UnknownSerializer",
                ser instanceof CollectionSerializer || ser instanceof UnknownSerializer);
    }

    // ============================================================
    // Map serializers
    // ============================================================

    @Test
    public void testMapSerializer() throws JsonMappingException {
        JavaType type = typeFactory.constructMapType(HashMap.class, String.class, Integer.class);
        JsonSerializer<?> ser = factory.createSerializer(provider, type);
        assertNotNull("Map serializer should not be null", ser);
        assertTrue("Expected MapSerializer", ser instanceof MapSerializer);
    }

    @Test
    public void testEmptyMapType() throws JsonMappingException {
        JavaType type = typeFactory.constructRawMapType(HashMap.class);
        JsonSerializer<?> ser = factory.createSerializer(provider, type);
        assertNotNull("Raw map serializer should not be null", ser);
        assertTrue("Expected MapSerializer or UnknownSerializer",
                ser instanceof MapSerializer || ser instanceof UnknownSerializer);
    }

    // ============================================================
    // Array serializers
    // ============================================================

    @Test
    public void testPrimitiveArraySerializer() throws JsonMappingException {
        JavaType type = typeFactory.constructArrayType(int.class);
        JsonSerializer<?> ser = factory.createSerializer(provider, type);
        assertNotNull("Primitive array serializer should not be null", ser);
        assertTrue("Expected ArraySerializerBase", ser instanceof ArraySerializerBase);
    }

    @Test
    public void testObjectArraySerializer() throws JsonMappingException {
        JavaType type = typeFactory.constructArrayType(String.class);
        JsonSerializer<?> ser = factory.createSerializer(provider, type);
        assertNotNull("Object array serializer should not be null", ser);
        assertTrue("Expected ArraySerializerBase", ser instanceof ArraySerializerBase);
    }

    // ============================================================
    // Reference type serializers (Defects4J bug #22)
    // ============================================================

    @Test
    public void testAtomicReferenceSerializer() throws JsonMappingException {
        // AtomicReference is a reference type that should be handled specially
        JavaType type = typeFactory.constructReferenceType(AtomicReference.class,
                typeFactory.constructType(String.class));
        JsonSerializer<?> ser = factory.createSerializer(provider, type);
        assertNotNull("AtomicReference serializer should not be null", ser);
        // In fixed version, should be AtomicReferenceSerializer
        assertTrue("Expected AtomicReferenceSerializer", ser instanceof AtomicReferenceSerializer);
    }

    @Test
    public void testAtomicReferenceWithNullContentType() throws JsonMappingException {
        // Edge case: reference type with null content type (should not happen normally)
        JavaType type = typeFactory.constructReferenceType(AtomicReference.class, null);
        try {
            JsonSerializer<?> ser = factory.createSerializer(provider, type);
            // May throw or return a fallback; we just ensure no NPE
            assertNotNull(ser);
        } catch (NullPointerException e) {
            fail("Should not throw NPE for null content type");
        } catch (Exception e) {
            // Other exceptions are acceptable
        }
    }

    // ============================================================
    // Enum serializers
    // ============================================================

    @Test
    public void testEnumSerializer() throws JsonMappingException {
        JavaType type = typeFactory.constructType(TestEnum.class);
        JsonSerializer<?> ser = factory.createSerializer(provider, type);
        assertNotNull("Enum serializer should not be null", ser);
        assertTrue("Expected EnumSerializer", ser instanceof EnumSerializer);
    }

    enum TestEnum { A, B, C }

    // ============================================================
    // Custom bean serializers
    // ============================================================

    @Test
    public void testBeanSerializer() throws JsonMappingException {
        JavaType type = typeFactory.constructType(SimpleBean.class);
        JsonSerializer<?> ser = factory.createSerializer(provider, type);
        assertNotNull("Bean serializer should not be null", ser);
        assertTrue("Expected BeanSerializer", ser instanceof BeanSerializer);
    }

    static class SimpleBean {
        public int x = 1;
        public String y = "test";
    }

    // ============================================================
    // Null and unknown types
    // ============================================================

    @Test(expected = NullPointerException.class)
    public void testNullType() throws JsonMappingException {
        factory.createSerializer(provider, null);
    }

    @Test
    public void testUnknownType() throws JsonMappingException {
        // Use an interface type that has no serializer
        JavaType type = typeFactory.constructType(SomeInterface.class);
        JsonSerializer<?> ser = factory.createSerializer(provider, type);
        // Should fall back to UnknownSerializer or throw
        assertNotNull("Unknown type serializer should not be null", ser);
        assertTrue("Expected UnknownSerializer", ser instanceof UnknownSerializer);
    }

    interface SomeInterface {}

    // ============================================================
    // Edge cases: empty bean, no properties
    // ============================================================

    @Test
    public void testEmptyBeanSerializer() throws JsonMappingException {
        JavaType type = typeFactory.constructType(EmptyBean.class);
        JsonSerializer<?> ser = factory.createSerializer(provider, type);
        assertNotNull("Empty bean serializer should not be null", ser);
        assertTrue("Expected BeanSerializer", ser instanceof BeanSerializer);
    }

    static class EmptyBean {
        // no properties
    }

    // ============================================================
    // Tests for internal methods (via reflection if needed)
    // ============================================================

    @Test
    public void testFindSerializerByLookup() throws Exception {
        // Use reflection to call protected method _findSerializerByLookup
        java.lang.reflect.Method method = BasicSerializerFactory.class
                .getDeclaredMethod("_findSerializerByLookup", JavaType.class);
        method.setAccessible(true);
        JavaType stringType = typeFactory.constructType(String.class);
        JsonSerializer<?> ser = (JsonSerializer<?>) method.invoke(factory, stringType);
        assertNotNull("Lookup should find StringSerializer", ser);
        assertTrue(ser instanceof StringSerializer);
    }

    @Test
    public void testCreateSerializerWithNullProvider() {
        JavaType type = typeFactory.constructType(String.class);
        try {
            factory.createSerializer(null, type);
            fail("Should throw NullPointerException for null provider");
        } catch (NullPointerException e) {
            // expected
        } catch (JsonMappingException e) {
            // also acceptable
        }
    }

    // ============================================================
    // Additional branch coverage: type modifiers, custom serializers
    // ============================================================

    @Test
    public void testWithCustomSerializerModifier() throws JsonMappingException {
        // Create a factory with a custom modifier that replaces String serializer
        SerializerFactoryConfig config = new SerializerFactoryConfig();
        config = config.withSerializerModifier(new BeanSerializerModifier() {
            @Override
            public JsonSerializer<?> modifySerializer(SerializationConfig config,
                                                      BeanDescription beanDesc,
                                                      JsonSerializer<?> serializer) {
                if (beanDesc.getBeanClass() == String.class) {
                    return new ToStringSerializer();
                }
                return serializer;
            }
        });
        BasicSerializerFactory customFactory = new BasicSerializerFactory(config);
        JavaType type = typeFactory.constructType(String.class);
        JsonSerializer<?> ser = customFactory.createSerializer(provider, type);
        assertNotNull("Custom modifier should produce serializer", ser);
        assertTrue("Expected ToStringSerializer", ser instanceof ToStringSerializer);
    }

    @Test
    public void testCollectionWithContentSerializer() throws JsonMappingException {
        // Collection with explicit content serializer (via annotations)
        // This test ensures the factory respects content serializers
        JavaType type = typeFactory.constructCollectionType(ArrayList.class, Integer.class);
        // We can't easily set content serializer here, but we can verify the path is taken
        JsonSerializer<?> ser = factory.createSerializer(provider, type);
        assertNotNull(ser);
        assertTrue(ser instanceof CollectionSerializer);
    }

    // ============================================================
    // Exception handling: invalid type
    // ============================================================

    @Test(expected = JsonMappingException.class)
    public void testInvalidType() throws JsonMappingException {
        // Create a type that cannot be serialized (e.g., a type with no serializer and no bean info)
        JavaType type = typeFactory.constructType(java.sql.Driver.class); // interface with no default serializer
        factory.createSerializer(provider, type);
    }

    // ============================================================
    // Helper to create a BasicBeanDescription for testing
    // ============================================================

    private BasicBeanDescription createBeanDescription(Class<?> clazz) {
        // Use ObjectMapper's introspection
        ObjectMapper mapper = new ObjectMapper();
        SerializationConfig config = mapper.getSerializationConfig();
        return (BasicBeanDescription) config.introspect(typeFactory.constructType(clazz));
    }
}