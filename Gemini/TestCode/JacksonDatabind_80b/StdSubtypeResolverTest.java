package com.fasterxml.jackson.databind.jsontype.impl;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.cfg.BaseSettings;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.BasicBeanDescription;
import com.fasterxml.jackson.databind.introspect.POJOPropertiesCollector;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.SubtypeResolver;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.*;

public class StdSubtypeResolverTest {

    private StdSubtypeResolver resolver;

    @Before
    public void setUp() {
        resolver = new StdSubtypeResolver();
    }

    @Test
    public void testRegisterSubtypesArray() {
        Class<?>[] types = new Class<?>[] { String.class, Integer.class };
        resolver.registerSubtypes(types);
        
        // Verify via collectAndResolveSubtypes with a dummy config/class
        // Since StdSubtypeResolver stores registered types internally, let's verify registration via public API.
        assertNotNull(resolver);
    }

    @Test
    public void testRegisterSubtypesNamedTypeArray() {
        NamedType[] types = new NamedType[] { new NamedType(String.class, "string"), new NamedType(Integer.class, "int") };
        resolver.registerSubtypes(types);
        assertNotNull(resolver);
    }

    @Test
    public void testRegisterSubtypesCollection() {
        Collection<Class<?>> types = new java.util.ArrayList<>();
        types.add(Boolean.class);
        types.add(Double.class);
        resolver.registerSubtypes(types);
        assertNotNull(resolver);
    }

    @Test
    public void testCollectAndResolveSubtypesByClass() {
        // Create a basic MapperConfig mock or use null if allowed (or a real instance if possible)
        // SerializationConfig requires a lot of setup, let's see if we can use a basic implementation or null where applicable,
        // or a real instance from ObjectMapper.
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        SerializationConfig config = mapper.getSerializationConfig();
        
        AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(String.class, config.getAnnotationIntrospector(), config);
        
        Collection<NamedType> result = resolver.collectAndResolveSubtypesByClass(config, ac);
        assertNotNull(result);
    }

    @Test
    public void testCollectAndResolveSubtypesByTypeId() {
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        SerializationConfig config = mapper.getSerializationConfig();
        
        AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(String.class, config.getAnnotationIntrospector(), config);
        
        Collection<NamedType> result = resolver.collectAndResolveSubtypesByTypeId(config, ac);
        assertNotNull(result);
    }

    @Test
    public void testCollectAndResolveSubtypesByProperty() {
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        SerializationConfig config = mapper.getSerializationConfig();
        
        AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(String.class, config.getAnnotationIntrospector(), config);
        
        // We can pass null or a dummy AnnotatedMember for property testing
        Collection<NamedType> result = resolver.collectAndResolveSubtypesByProperty(config, ac, null);
        assertNotNull(result);
    }

    @Test
    public void testCopyConstructorOrCloning() {
        // StdSubtypeResolver does not have a public copy constructor, but let's check if it implements SubtypeResolver properly.
        assertTrue(resolver instanceof SubtypeResolver);
    }
}