package com.fasterxml.jackson.databind.jsontype.impl;

import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ClassNameIdResolverTest {

    @Test
    public void testGetMechanism() {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.constructType(String.class);
        TypeFactory tf = mapper.getTypeFactory();
        ClassNameIdResolver resolver = new ClassNameIdResolver(type, tf);

        Assert.assertEquals(com.fasterxml.jackson.annotation.JsonTypeInfo.Id.CLASS, resolver.getMechanism());
    }

    @Test
    public void testIdFromValue() {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.constructType(Object.class);
        TypeFactory tf = mapper.getTypeFactory();
        ClassNameIdResolver resolver = new ClassNameIdResolver(type, tf);

        String id = resolver.idFromValue("hello");
        Assert.assertEquals(String.class.getName(), id);
    }

    @Test
    public void testIdFromValueAndType() {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.constructType(Object.class);
        TypeFactory tf = mapper.getTypeFactory();
        ClassNameIdResolver resolver = new ClassNameIdResolver(type, tf);

        String id = resolver.idFromValueAndType("hello", Integer.class);
        Assert.assertEquals(Integer.class.getName(), id);
    }

    @Test
    public void testTypeFromIdWithContext() {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.constructType(List.class);
        TypeFactory tf = mapper.getTypeFactory();
        ClassNameIdResolver resolver = new ClassNameIdResolver(type, tf);

        DatabindContext context = mapper.getDeserializationContext();
        JavaType resolvedType = resolver.typeFromId(context, ArrayList.class.getName());
        Assert.assertNotNull(resolvedType);
        Assert.assertTrue(resolvedType.isCollectionLikeType());
    }

    @Test
    public void testTypeFromIdString() {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.constructType(Object.class);
        TypeFactory tf = mapper.getTypeFactory();
        ClassNameIdResolver resolver = new ClassNameIdResolver(type, tf);

        JavaType resolvedType = resolver.typeFromId(null, java.util.HashMap.class.getName());
        Assert.assertNotNull(resolvedType);
        Assert.assertTrue(resolvedType.isMapLikeType());
    }

    @Test
    public void testGetDescForKnownTypeIds() {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.constructType(Object.class);
        TypeFactory tf = mapper.getTypeFactory();
        ClassNameIdResolver resolver = new ClassNameIdResolver(type, tf);

        Assert.assertEquals("class name", resolver.getDescForKnownTypeIds());
    }

    @Test
    public void testRegisterSubtype() {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.constructType(Object.class);
        TypeFactory tf = mapper.getTypeFactory();
        ClassNameIdResolver resolver = new ClassNameIdResolver(type, tf);

        // registerSubtype usually doesn't do much for ClassNameIdResolver, but let's test it doesn't crash
        resolver.registerSubtype(String.class, "string");
        Assert.assertEquals(String.class.getName(), resolver.idFromValue("test"));
    }

    @Test
    public void testQueueSubtypeHandlingBug88Scenario() {
        // Specifically targeting potential edge cases around bug 88 (generic type handling / primitive / void or special types)
        ObjectMapper mapper = new ObjectMapper();
        // Disable or enable features that affect type serialization
        mapper.configure(MapperFeature.USE_STATIC_TYPING, true);
        
        JavaType baseType = mapper.constructType(Object.class);
        ClassNameIdResolver resolver = new ClassNameIdResolver(baseType, mapper.getTypeFactory());

        // Test with a standard user-defined or collection type
        String id = resolver.idFromValue(new java.util.Date());
        Assert.assertEquals(java.util.Date.class.getName(), id);

        JavaType res = resolver.typeFromId(null, java.util.Date.class.getName());
        Assert.assertEquals(java.util.Date.class, res.getRawClass());
    }
}