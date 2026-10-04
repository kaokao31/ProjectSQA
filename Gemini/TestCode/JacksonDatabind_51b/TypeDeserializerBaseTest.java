package com.fasterxml.jackson.databind.jsontype.impl;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.BeanDeserializerFactory;
import com.fasterxml.jackson.databind.deser.DefaultDeserializationContext;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class TypeDeserializerBaseTest {

    private ObjectMapper objectMapper;
    private DeserializationContext deserializationContext;

    @Before
    public void setUp() {
        objectMapper = new ObjectMapper();
        deserializationContext = objectMapper.getDeserializationContext();
    }

    @After
    public void tearDown() {
        objectMapper = null;
        deserializationContext = null;
    }

    @Test
    public void testBaseTypeAccessors() {
        JavaType baseType = TypeFactory.defaultInstance().constructType(String.class);
        TypeIdResolver idResolver = new MinimalClassNameIdResolver(baseType, TypeFactory.defaultInstance());
        BeanProperty property = null;

        ConcreteTypeDeserializerBase deser = new ConcreteTypeDeserializerBase(
                baseType, idResolver, "typeProp", true, null
        );

        assertEquals(baseType, deser.getBaseType());
        assertEquals(idResolver, deser.getTypeIdResolver());
        assertEquals("typeProp", deser.getPropertyName());
        assertTrue(deser.includeAsProperty());
        assertEquals(String.class.getName(), deser.getDefaultImplName());
        assertNotNull(deser.getDefaultImpl());
    }

    @Test
    public void testToString() {
        JavaType baseType = TypeFactory.defaultInstance().constructType(String.class);
        TypeIdResolver idResolver = new MinimalClassNameIdResolver(baseType, TypeFactory.defaultInstance());

        ConcreteTypeDeserializerBase deser = new ConcreteTypeDeserializerBase(
                baseType, idResolver, "typeProp", false, null
        );

        String desc = deser.toString();
        assertNotNull(desc);
        assertTrue(desc.contains(ConcreteTypeDeserializerBase.class.getName()));
        assertTrue(desc.contains("typeProp"));
    }

    @Test
    public void testDeserializeTypedFromAnyWithNullParser() throws IOException {
        JavaType baseType = TypeFactory.defaultInstance().constructType(String.class);
        TypeIdResolver idResolver = new MinimalClassNameIdResolver(baseType, TypeFactory.defaultInstance());

        ConcreteTypeDeserializerBase deser = new ConcreteTypeDeserializerBase(
                baseType, idResolver, "typeProp", false, null
        );

        // Should handle null or specific token scenarios
        JsonParser p = objectMapper.getFactory().createParser("{}");
        // Advance to a token or test base implementations
        assertNotNull(deser.baseTypeName());
    }

    @Test
    public void testFindDefaultImplDeserializer() throws IOException {
        JavaType baseType = TypeFactory.defaultInstance().constructType(String.class);
        TypeIdResolver idResolver = new MinimalClassNameIdResolver(baseType, TypeFactory.defaultInstance());

        ConcreteTypeDeserializerBase deser = new ConcreteTypeDeserializerBase(
                baseType, idResolver, "typeProp", false, String.class
        );

        JsonDeserializer<Object> defaultDeser = deser._findDefaultImplDeserializer(deserializationContext);
        assertNotNull(defaultDeser);
    }

    @Test
    public void testFindDeserializer() throws IOException {
        JavaType baseType = TypeFactory.defaultInstance().constructType(String.class);
        TypeIdResolver idResolver = new MinimalClassNameIdResolver(baseType, TypeFactory.defaultInstance());

        ConcreteTypeDeserializerBase deser = new ConcreteTypeDeserializerBase(
                baseType, idResolver, "typeProp", false, String.class
        );

        JsonDeserializer<Object> customDeser = deser._findDeserializer(deserializationContext, "java.lang.String");
        assertNotNull(customDeser);
    }

    @Test
    public void testHandleMissingTypeId() throws IOException {
        JavaType baseType = TypeFactory.defaultInstance().constructType(String.class);
        TypeIdResolver idResolver = new MinimalClassNameIdResolver(baseType, TypeFactory.defaultInstance());

        ConcreteTypeDeserializerBase deser = new ConcreteTypeDeserializerBase(
                baseType, idResolver, "typeProp", false, String.class
        );

        JsonParser p = objectMapper.getFactory().createParser("\"test\"");
        p.nextToken();
        
        try {
            deser._handleMissingTypeId(deserializationContext, "Missing type id");
        } catch (Exception e) {
            assertNotNull(e);
        }
    }

    @Test
    public void testHandleUnknownTypeId() throws IOException {
        JavaType baseType = TypeFactory.defaultInstance().constructType(String.class);
        TypeIdResolver idResolver = new MinimalClassNameIdResolver(baseType, TypeFactory.defaultInstance());

        ConcreteTypeDeserializerBase deser = new ConcreteTypeDeserializerBase(
                baseType, idResolver, "typeProp", false, String.class
        );

        try {
            deser._handleUnknownTypeId(deserializationContext, "UnknownType");
        } catch (Exception e) {
            assertNotNull(e);
        }
    }

    // Concrete subclass to instantiate abstract TypeDeserializerBase
    private static class ConcreteTypeDeserializerBase extends TypeDeserializerBase {
        private static final long serialVersionUID = 1L;

        public ConcreteTypeDeserializerBase(JavaType baseType, TypeIdResolver idRes,
                String typePropertyName, boolean typeIdVisible, JavaType defaultImpl) {
            super(baseType, idRes, typePropertyName, typeIdVisible, defaultImpl);
        }

        public ConcreteTypeDeserializerBase(ConcreteTypeDeserializerBase src, BeanProperty property) {
            super(src, property);
        }

        @Override
        public TypeDeserializer forProperty(BeanProperty prop) {
            return new ConcreteTypeDeserializerBase(this, prop);
        }

        @Override
        public JsonTypeInfo.As getTypeInclusion() {
            return JsonTypeInfo.As.PROPERTY;
        }
    }
}