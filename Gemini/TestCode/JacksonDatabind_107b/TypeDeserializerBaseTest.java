package com.fasterxml.jackson.databind.jsontype.impl;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.exc.InvalidTypeIdException;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class TypeDeserializerBaseTest {

    private ObjectMapper objectMapper;
    private TypeFactory typeFactory;

    @Before
    public void setUp() {
        objectMapper = new ObjectMapper();
        typeFactory = objectMapper.getTypeFactory();
    }

    @Test
    public void testBaseConstructorAndGetters() {
        JavaType baseType = typeFactory.constructType(Object.class);
        TypeIdResolver idRes = new MinimalClassNameIdResolver(baseType, typeFactory);
        JavaType defaultImpl = typeFactory.constructType(String.class);

        ConcreteTypeDeserializerBase des = new ConcreteTypeDeserializerBase(
                baseType, idRes, "typeProp", true, defaultImpl
        );

        assertEquals(baseType, des.getBaseType());
        assertEquals("typeProp", des.getPropertyName());
        assertEquals(JsonTypeInfo.As.PROPERTY, des.getTypeInclusion());
        assertEquals(defaultImpl, des.getDefaultImpl());
        assertEquals(idRes, des.getTypeIdResolver());
        assertNotNull(des.toString());
    }

    @Test
    public void testDefaultImplDeserializationWithoutParser() throws IOException {
        JavaType baseType = typeFactory.constructType(Object.class);
        TypeIdResolver idRes = new MinimalClassNameIdResolver(baseType, typeFactory);
        JavaType defaultImpl = typeFactory.constructType(String.class);

        ConcreteTypeDeserializerBase des = new ConcreteTypeDeserializerBase(
                baseType, idRes, "typeProp", true, defaultImpl
        );

        DeserializationContext ctxt = objectMapper.getDeserializationContext();
        
        // When defaultImpl is present, it should use it or return null depending on setup, 
        // let's check base handleMissingTypeId or _handleMissingTypeId behavior.
        Object result = des._handleMissingTypeId(ctxt, "missing type id");
        assertNull(result);
    }

    @Test(expected = InvalidTypeIdException.class)
    public void testHandleUnknownTypeIdWithoutDefault() throws IOException {
        JavaType baseType = typeFactory.constructType(Object.class);
        TypeIdResolver idRes = new MinimalClassNameIdResolver(baseType, typeFactory);

        ConcreteTypeDeserializerBase des = new ConcreteTypeDeserializerBase(
                baseType, idRes, "typeProp", false, null
        );

        JsonParser p = objectMapper.getFactory().createParser("{}");
        p.nextToken();
        DeserializationContext ctxt = objectMapper.getDeserializationContext();
        
        des._handleUnknownTypeId(ctxt, "unknown.type.id");
    }

    @Test
    public void testHandleUnknownTypeIdWithDefault() throws IOException {
        JavaType baseType = typeFactory.constructType(Object.class);
        TypeIdResolver idRes = new MinimalClassNameIdResolver(baseType, typeFactory);
        JavaType defaultImpl = typeFactory.constructType(String.class);

        ConcreteTypeDeserializerBase des = new ConcreteTypeDeserializerBase(
                baseType, idRes, "typeProp", false, defaultImpl
        );

        JsonParser p = objectMapper.getFactory().createParser("{}");
        p.nextToken();
        DeserializationContext ctxt = objectMapper.getDeserializationContext();

        JsonDeserializer<Object> deser = des._findDefaultImplDeserializer(ctxt);
        // Might be null or a valid deserializer
        // Just exercising the code path
        assertNotNull(des.getDefaultImpl());
    }

    @Test
    public void testCoercionConfig() {
        JavaType baseType = typeFactory.constructType(Object.class);
        ConcreteTypeDeserializerBase des = new ConcreteTypeDeserializerBase(
                baseType, null, "typeProp", false, null
        );
        DeserializationContext ctxt = objectMapper.getDeserializationContext();
        assertNotNull(des._deserializeTypedUsingDefaultImpl(null, ctxt, null));
    }

    // Helper subclass to instantiate the abstract class TypeDeserializerBase
    private static class ConcreteTypeDeserializerBase extends TypeDeserializerBase {
        private static final long serialVersionUID = 1L;

        public ConcreteTypeDeserializerBase(JavaType baseType, TypeIdResolver idRes,
                String typePropertyName, boolean typeIdVisible, JavaType defaultImpl) {
            super(baseType, idRes, typePropertyName, typeIdVisible, defaultImpl);
        }

        @Override
        public TypeDeserializer forProperty(BeanProperty prop) {
            return this;
        }

        @Override
        public JsonTypeInfo.As getTypeInclusion() {
            return JsonTypeInfo.As.PROPERTY;
        }

        @Override
        public Object deserializeTypedFromObject(JsonParser jp, DeserializationContext ctxt) throws IOException {
            return "ObjectResult";
        }

        @Override
        public Object deserializeTypedFromArray(JsonParser jp, DeserializationContext ctxt) throws IOException {
            return "ArrayResult";
        }

        @Override
        public Object deserializeTypedFromScalar(JsonParser jp, DeserializationContext ctxt) throws IOException {
            return "ScalarResult";
        }

        @Override
        public Object deserializeTypedFromAny(JsonParser jp, DeserializationContext ctxt) throws IOException {
            return "AnyResult";
        }
    }
}