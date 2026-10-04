package com.fasterxml.jackson.databind.deser;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for DeserializerCache focusing on branch coverage,
 * edge cases, and the known bug (JacksonDatabind #91) involving
 * generic type parameter caching.
 */
public class DeserializerCacheTest {

    private DeserializerCache cache;
    private DeserializationContext ctxt;
    private TypeFactory typeFactory;

    @Before
    public void setUp() {
        cache = new DeserializerCache();
        // Use a standard ObjectMapper to obtain a DeserializationContext
        ObjectMapper mapper = new ObjectMapper();
        ctxt = mapper.getDeserializationContext();
        typeFactory = TypeFactory.defaultInstance();
    }

    // ---------- Basic existence and null handling ----------

    @Test(expected = IllegalArgumentException.class)
    public void testFindValueDeserializerNullType() throws JsonMappingException {
        cache.findValueDeserializer(ctxt, null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFindValueDeserializerNullContext() throws JsonMappingException {
        JavaType stringType = typeFactory.constructType(String.class);
        cache.findValueDeserializer(null, stringType, null);
    }

    @Test
    public void testHasValueDeserializerNull() {
        assertFalse(cache.hasValueDeserializer(null));
    }

    // ---------- Caching behavior for simple types ----------

    @Test
    public void testCacheStoresAndReusesDeserializer() throws JsonMappingException {
        JavaType stringType = typeFactory.constructType(String.class);
        JsonDeserializer<?> first = cache.findValueDeserializer(ctxt, stringType, null);
        assertNotNull("Deserializer should not be null for String", first);

        JsonDeserializer<?> second = cache.findValueDeserializer(ctxt, stringType, null);
        assertSame("Cached deserializer should be reused", first, second);
    }

    @Test
    public void testHasValueDeserializerAfterFind() throws JsonMappingException {
        JavaType intType = typeFactory.constructType(Integer.class);
        assertFalse("Initially type is not cached", cache.hasValueDeserializer(intType));
        cache.findValueDeserializer(ctxt, intType, null);
        assertTrue("After find, type should be cached", cache.hasValueDeserializer(intType));
    }

    // ---------- Generic type parameter handling (Bug #91) ----------

    @Test
    public void testCacheDistinguishesParameterizedTypes() throws JsonMappingException {
        // Create types: List<String> and List<Integer> (same raw type, different content)
        CollectionType stringListType = typeFactory.constructCollectionType(ArrayList.class, String.class);
        CollectionType integerListType = typeFactory.constructCollectionType(ArrayList.class, Integer.class);

        JsonDeserializer<?> deserStringList = cache.findValueDeserializer(ctxt, stringListType, null);
        JsonDeserializer<?> deserIntegerList = cache.findValueDeserializer(ctxt, integerListType, null);

        assertNotNull("Deserializer for List<String>", deserStringList);
        assertNotNull("Deserializer for List<Integer>", deserIntegerList);

        // The cache must not return the same instance for different parameterizations
        // (even though the raw container deserializer might be shared internally, the
        // content deserializers differ; this test ensures the cache uses the full JavaType
        // key, not just the raw class)
        assertNotSame("List<String> and List<Integer> should have different cached entries",
                      deserStringList, deserIntegerList);

        // Also verify that the correct deserializer is returned on repeated calls
        JsonDeserializer<?> cachedStringList = cache.findValueDeserializer(ctxt, stringListType, null);
        assertSame("List<String> deserializer should be reused", deserStringList, cachedStringList);
    }

    @Test
    public void testCacheForRawTypeAfterParameterizedLookup() throws JsonMappingException {
        // First look up List.class (raw type without parameters)
        JavaType rawListType = typeFactory.uncheckedSimpleType(List.class);
        JsonDeserializer<?> rawDeser = cache.findValueDeserializer(ctxt, rawListType, null);
        assertNotNull("Raw List deserializer", rawDeser);

        // Now look up parameterized List<String> – must not reuse raw deserializer
        CollectionType stringListType = typeFactory.constructCollectionType(ArrayList.class, String.class);
        JsonDeserializer<?> paramDeser = cache.findValueDeserializer(ctxt, stringListType, null);
        assertNotNull("Parameterized List<String> deserializer", paramDeser);
        assertNotSame("Raw and parameterized deserializers should differ", rawDeser, paramDeser);
    }

    // ---------- Null property handling ----------

    @Test
    public void testFindValueDeserializerWithNullProperty() throws JsonMappingException {
        JavaType type = typeFactory.constructType(Double.class);
        JsonDeserializer<?> deser = cache.findValueDeserializer(ctxt, type, null);
        assertNotNull("Deserializer should exist even without property", deser);
    }

    // ---------- Exception handling during deserialization lookup ----------

    @Test(expected = JsonMappingException.class)
    public void testFindValueDeserializerWithInvalidType() throws JsonMappingException {
        // Attempt to get a deserializer for an abstract type that cannot be instantiated
        JavaType abstractType = typeFactory.constructType(AbstractClass.class);
        cache.findValueDeserializer(ctxt, abstractType, null);
    }

    // A small abstract class to trigger exception
    private abstract static class AbstractClass {
    }

    // ---------- Cache concurrency note ----------
    // (Not directly testable in a single-threaded unit test, but we ensure
    //  the basic operations complete without crashes)
}