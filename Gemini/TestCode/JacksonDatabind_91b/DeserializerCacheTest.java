package com.fasterxml.jackson.databind.deser;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class DeserializerCacheTest {

    private DeserializerCache cache;
    private ObjectMapper objectMapper;

    @Before
    public void setUp() {
        cache = new DeserializerCache();
        objectMapper = new ObjectMapper();
    }

    @Test
    public void testCacheSizeAndFlush() {
        assertEquals(0, cache.cachedDeserializersCount());
        cache.flushCachedDeserializers();
        assertEquals(0, cache.cachedDeserializersCount());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFindValueDeserializerNullType() throws JsonMappingException {
        DeserializationContext ctxt = objectMapper.getDeserializationContext();
        cache.findValueDeserializer(ctxt, null, null);
    }

    @Test
    public void testFindValueDeserializerBasic() throws JsonMappingException {
        DeserializationContext ctxt = objectMapper.getDeserializationContext();
        JavaType type = objectMapper.constructType(String.class);
        
        JsonDeserializer<Object> deser1 = cache.findValueDeserializer(ctxt, null, type);
        assertNotNull(deser1);
        
        // Second call should hit the cache
        JsonDeserializer<Object> deser2 = cache.findValueDeserializer(ctxt, null, type);
        assertNotNull(deser2);
        assertSame(deser1, deser2);
        
        assertTrue(cache.cachedDeserializersCount() > 0);
    }

    @Test
    public void testHasValueDeserializerBasic() throws JsonMappingException {
        DeserializationContext ctxt = objectMapper.getDeserializationContext();
        JavaType type = objectMapper.constructType(String.class);
        
        boolean hasDeser = cache.hasValueDeserializerFor(ctxt, null, type);
        assertTrue(hasDeser);
    }

    @Test
    public void testFindKeyDeserializerBasic() throws JsonMappingException {
        DeserializationContext ctxt = objectMapper.getDeserializationContext();
        JavaType type = objectMapper.constructType(String.class);
        
        KeyDeserializer keyDeser = cache.findKeyDeserializer(ctxt, null, type);
        assertNotNull(keyDeser);
    }

    @Test
    public void testMapKeyDeserializerBug91Context() throws Exception {
        // Jackson Databind Bug 91 involves handling of value types in Maps where 
        // value type contains custom key/value definition or has potential for 
        // non-simple types that might be cached or checked incorrectly.
        DeserializationContext ctxt = objectMapper.getDeserializationContext();
        
        TypeFactory tf = objectMapper.getTypeFactory();
        // Construct a MapType where the value type has some annotations or is a custom bean
        JavaType keyType = tf.constructType(String.class);
        JavaType valueType = tf.constructType(DummyBean.class);
        MapType mapType = tf.constructMapType(HashMap.class, keyType, valueType);

        // This triggers `hasValueDeserializerFor` or `findValueDeserializer` paths 
        // specifically targeting map value serializers/deserializers cache checks.
        boolean hasDeser = cache.hasValueDeserializerFor(ctxt, null, mapType);
        assertTrue(hasDeser);

        JsonDeserializer<Object> mapDeser = cache.findValueDeserializer(ctxt, null, mapType);
        assertNotNull(mapDeser);
        
        // Verify caching works correctly for complex types
        JsonDeserializer<Object> mapDeserCached = cache.findValueDeserializer(ctxt, null, mapType);
        assertSame(mapDeser, mapDeserCached);
    }

    @Test
    public void testUnknownTypeHandling() {
        DeserializationContext ctxt = objectMapper.getDeserializationContext();
        // Construct a type that might not have a default deserializer easily
        JavaType unknownType = objectMapper.constructType(Unconstructable.class);
        
        try {
            cache.findValueDeserializer(ctxt, null, unknownType);
        } catch (Exception e) {
            // Expected for unconstructable/unknown types
        }
    }

    // Dummy classes for testing specific type scenarios
    public static class DummyBean {
        public String value;
    }

    private static abstract class Unconstructable {
        private Unconstructable() {}
    }
}