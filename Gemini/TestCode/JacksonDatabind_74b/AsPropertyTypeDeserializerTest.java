package com.fasterxml.jackson.databind.jsontype.impl;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.Assert;
import org.junit.Test;

public class AsPropertyTypeDeserializerTest {

    @Test
    public void testConstructorAndBasicGetters() {
        JavaType baseType = TypeFactory.defaultInstance().constructType(Object.class);
        TypeIdResolver idResolver = new MinimalClassNameIdResolver(baseType, TypeFactory.defaultInstance());
        BeanProperty property = null;

        AsPropertyTypeDeserializer deserializer = new AsPropertyTypeDeserializer(
                baseType, idResolver, "typeProp", true, baseType, JsonTypeInfo.As.PROPERTY
        );

        Assert.assertEquals("typeProp", deserializer.getPropertyName());
        Assert.assertNotNull(deserializer.toString());

        TypeDeserializer clone = deserializer.forProperty(property);
        Assert.assertNotNull(clone);
        Assert.assertEquals(JsonTypeInfo.As.PROPERTY, clone.getTypeInclusion());
    }

    @Test
    public void testDeserializeTypedFromObjectEmptyToken() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType baseType = TypeFactory.defaultInstance().constructType(Object.class);
        TypeIdResolver idResolver = new MinimalClassNameIdResolver(baseType, mapper.getTypeFactory());

        AsPropertyTypeDeserializer deserializer = new AsPropertyTypeDeserializer(
                baseType, idResolver, "type", true, baseType, JsonTypeInfo.As.PROPERTY
        );

        String json = "{}";
        try (JsonParser p = mapper.getFactory().createParser(json)) {
            // Advance to START_OBJECT
            Assert.assertEquals(JsonToken.START_OBJECT, p.nextToken());
            DeserializationContext ctxt = mapper.getDeserializationContext();
            
            // This tests the branch where token is START_OBJECT, but properties are empty or type id missing
            Object result = deserializer.deserializeTypedFromObject(p, ctxt);
            // Depending on configuration/fallback, it might return null or handle missing type
            Assert.assertNull(result);
        }
    }

    @Test
    public void testDeserializeTypedUsingDefaultImpl() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType baseType = TypeFactory.defaultInstance().constructType(String.class);
        TypeIdResolver idResolver = new MinimalClassNameIdResolver(baseType, mapper.getTypeFactory());

        // Default impl set to String.class
        AsPropertyTypeDeserializer deserializer = new AsPropertyTypeDeserializer(
                baseType, idResolver, "type", true, baseType, JsonTypeInfo.As.PROPERTY
        );

        String json = "{\"val\": 123}";
        try (JsonParser p = mapper.getFactory().createParser(json)) {
            Assert.assertEquals(JsonToken.START_OBJECT, p.nextToken());
            DeserializationContext ctxt = mapper.getDeserializationContext();
            
            // Test how it behaves with an object missing the type property when defaultImpl is present
            Object result = deserializer.deserializeTypedFromObject(p, ctxt);
            // It should fall back or handle appropriately without throwing unexpected NPE
            Assert.assertNull(result);
        }
    }

    @Test
    public void testDeserializeTypedFromAny() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType baseType = TypeFactory.defaultInstance().constructType(Object.class);
        TypeIdResolver idResolver = new MinimalClassNameIdResolver(baseType, mapper.getTypeFactory());

        AsPropertyTypeDeserializer deserializer = new AsPropertyTypeDeserializer(
                baseType, idResolver, "type", false, baseType, JsonTypeInfo.As.PROPERTY
        );

        try (JsonParser p = mapper.getFactory().createParser("[]")) {
            Assert.assertEquals(JsonToken.START_ARRAY, p.nextToken());
            DeserializationContext ctxt = mapper.getDeserializationContext();
            
            // Testing deserializeTypedFromAny
            Object result = deserializer.deserializeTypedFromAny(p, ctxt);
            // Usually falls back to standard deserialization or uses default
            Assert.assertNotNull(result);
        }
    }
}