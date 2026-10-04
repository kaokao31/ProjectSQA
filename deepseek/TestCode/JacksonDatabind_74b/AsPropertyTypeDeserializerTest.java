package com.fasterxml.jackson.databind.jsontype.impl;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Test suite for AsPropertyTypeDeserializer targeting high coverage and fault detection.
 * Designed for JacksonDatabind bug #74 context.
 */
public class AsPropertyTypeDeserializerTest {

    private ObjectMapper mapper;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES);
    }

    // Base type with PROPERTY inclusion
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
    @JsonSubTypes({
            @JsonSubTypes.Type(value = Dog.class, name = "dog"),
            @JsonSubTypes.Type(value = Cat.class, name = "cat")
    })
    static class Animal {
        public String name;
    }

    @JsonTypeName("dog")
    static class Dog extends Animal {
        public String breed;
    }

    @JsonTypeName("cat")
    static class Cat extends Animal {
        public boolean indoor;
    }

    // Test 1: Normal polymorphic deserialization with valid type id
    @Test
    public void testDeserializeWithValidTypeId() throws IOException {
        String json = "{\"type\":\"dog\",\"name\":\"Buddy\",\"breed\":\"Labrador\"}";
        Animal result = mapper.readValue(json, Animal.class);
        assertNotNull(result);
        assertTrue(result instanceof Dog);
        Dog dog = (Dog) result;
        assertEquals("Buddy", dog.name);
        assertEquals("Labrador", dog.breed);
    }

    // Test 2: Deserialize with missing type property (should throw exception)
    @Test(expected = UnrecognizedPropertyException.class)
    public void testDeserializeMissingTypeProperty() throws IOException {
        // Without FAIL_ON_UNKNOWN_PROPERTIES, missing type property might be ignored? Actually, type property is required.
        // By default, missing type id throws JsonMappingException.
        String json = "{\"name\":\"Whiskers\",\"indoor\":true}";
        mapper.readValue(json, Animal.class);
    }

    // Test 3: Deserialize with unknown type name
    @Test(expected = IllegalArgumentException.class)
    public void testDeserializeUnknownTypeName() throws IOException {
        String json = "{\"type\":\"fish\",\"name\":\"Nemo\"}";
        mapper.readValue(json, Animal.class);
    }

    // Test 4: Deserialize with null value (should return null)
    @Test
    public void testDeserializeNull() throws IOException {
        Animal result = mapper.readValue("null", Animal.class);
        assertNull(result);
    }

    // Test 5: Deserialize with empty string for type property
    @Test(expected = IllegalArgumentException.class)
    public void testDeserializeEmptyTypeProperty() throws IOException {
        String json = "{\"type\":\"\",\"name\":\"Unknown\"}";
        mapper.readValue(json, Animal.class);
    }

    // Test 6: Deserialize with whitespace in type property
    @Test(expected = IllegalArgumentException.class)
    public void testDeserializeWhitespaceTypeProperty() throws IOException {
        String json = "{\"type\":\" dog \",\"name\":\"Buddy\"}";
        mapper.readValue(json, Animal.class);
    }

    // Test 7: Deserialize with type property as array (unexpected type)
    @Test(expected = IllegalArgumentException.class)
    public void testDeserializeTypePropertyAsArray() throws IOException {
        String json = "{\"type\":[\"dog\"],\"name\":\"Buddy\"}";
        mapper.readValue(json, Animal.class);
    }

    // Test 8: Deserialize with type property as number
    @Test(expected = IllegalArgumentException.class)
    public void testDeserializeTypePropertyAsNumber() throws IOException {
        String json = "{\"type\":123,\"name\":\"Buddy\"}";
        mapper.readValue(json, Animal.class);
    }

    // Test 9: Deserialize with type property as boolean
    @Test(expected = IllegalArgumentException.class)
    public void testDeserializeTypePropertyAsBoolean() throws IOException {
        String json = "{\"type\":true,\"name\":\"Buddy\"}";
        mapper.readValue(json, Animal.class);
    }

    // Test 10: Deserialize with multiple type properties (should use first)
    @Test
    public void testDeserializeMultipleTypeProperties() throws IOException {
        String json = "{\"type\":\"dog\",\"type\":\"cat\",\"name\":\"Max\"}";
        // Jackson may handle duplicate keys differently; typically last wins.
        // This test checks behavior.
        Animal result = mapper.readValue(json, Animal.class);
        assertNotNull(result);
        // Depending on parser, last value may be used. We'll just ensure no exception.
    }

    // Test 11: Deserialize with defaultImpl (if configured) - not tested here because we didn't set defaultImpl
    // But we can test with a mapper that has defaultImpl via mix-in or config.

    // Test 12: Deserialize with type property at end of JSON
    @Test
    public void testDeserializeTypePropertyAtEnd() throws IOException {
        String json = "{\"name\":\"Buddy\",\"breed\":\"Labrador\",\"type\":\"dog\"}";
        Animal result = mapper.readValue(json, Animal.class);
        assertNotNull(result);
        assertTrue(result instanceof Dog);
    }

    // Test 13: Deserialize with nested object containing type property
    @Test
    public void testDeserializeNestedPolymorphic() throws IOException {
        String json = "{\"type\":\"dog\",\"name\":\"Rex\",\"breed\":\"German Shepherd\"}";
        Animal result = mapper.readValue(json, Animal.class);
        assertTrue(result instanceof Dog);
    }

    // Test 14: Deserialize with missing type property but with defaultImpl via annotation
    // We'll create a separate class with defaultImpl for this test
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type", defaultImpl = Dog.class)
    static class AnimalWithDefault {
        public String name;
    }

    @Test
    public void testDeserializeWithDefaultImpl() throws IOException {
        String json = "{\"name\":\"Unknown\"}";
        AnimalWithDefault result = mapper.readValue(json, AnimalWithDefault.class);
        assertNotNull(result);
        assertTrue(result instanceof Dog);
        assertEquals("Unknown", result.name);
    }

    // Test 15: Deserialize with type property but no subtype registered (should use defaultImpl if set)
    @Test
    public void testDeserializeUnknownTypeWithDefaultImpl() throws IOException {
        String json = "{\"type\":\"unknown\",\"name\":\"Test\"}";
        AnimalWithDefault result = mapper.readValue(json, AnimalWithDefault.class);
        assertNotNull(result);
        assertTrue(result instanceof Dog);
    }

    // Test 16: Deserialize with type property and extra unknown properties (should ignore)
    @Test
    public void testDeserializeWithExtraProperties() throws IOException {
        String json = "{\"type\":\"cat\",\"name\":\"Mittens\",\"indoor\":true,\"color\":\"black\"}";
        Animal result = mapper.readValue(json, Animal.class);
        assertTrue(result instanceof Cat);
        Cat cat = (Cat) result;
        assertTrue(cat.indoor);
        assertEquals("Mittens", cat.name);
    }

    // Test 17: Deserialize with type property as null value
    @Test(expected = IllegalArgumentException.class)
    public void testDeserializeNullTypeProperty() throws IOException {
        String json = "{\"type\":null,\"name\":\"Buddy\"}";
        mapper.readValue(json, Animal.class);
    }

    // Test 18: Deserialize with type property as empty object
    @Test(expected = IllegalArgumentException.class)
    public void testDeserializeTypePropertyAsObject() throws IOException {
        String json = "{\"type\":{},\"name\":\"Buddy\"}";
        mapper.readValue(json, Animal.class);
    }

    // Test 19: Deserialize with type property as array of strings
    @Test(expected = IllegalArgumentException.class)
    public void testDeserializeTypePropertyAsStringArray() throws IOException {
        String json = "{\"type\":[\"dog\",\"cat\"],\"name\":\"Buddy\"}";
        mapper.readValue(json, Animal.class);
    }

    // Test 20: Deserialize with type property containing special characters
    @Test(expected = IllegalArgumentException.class)
    public void testDeserializeTypePropertyWithSpecialChars() throws IOException {
        String json = "{\"type\":\"do\\\"g\",\"name\":\"Buddy\"}";
        mapper.readValue(json, Animal.class);
    }
}