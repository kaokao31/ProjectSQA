package com.fasterxml.jackson.databind.jsontype.impl;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.Test;

import static org.junit.Assert.*;

public class AsWrapperTypeDeserializerTest {

    @Test
    public void testConstructorsAndCopy() {
        JavaType baseType = TypeFactory.defaultInstance().constructType(String.class);
        TypeIdResolver idRes = new TypeNameIdResolver(baseType, TypeFactory.defaultInstance());
        BeanProperty property = null;

        AsWrapperTypeDeserializer des1 = new AsWrapperTypeDeserializer(
                baseType, idRes, "typeProp", true, baseType
        );
        assertEquals(JsonTypeInfo.As.WRAPPER_OBJECT, des1.getTypeInclusion());

        AsWrapperTypeDeserializer des2 = new AsWrapperTypeDeserializer(des1, property);
        assertEquals(JsonTypeInfo.As.WRAPPER_OBJECT, des2.getTypeInclusion());

        TypeDeserializer clone = des1.forProperty(property);
        assertNotNull(clone);
    }

    @Test
    public void testDeserializeTypedFromObjectWithNullToken() throws Exception {
        JavaType baseType = TypeFactory.defaultInstance().constructType(String.class);
        TypeIdResolver idRes = new TypeNameIdResolver(baseType, TypeFactory.defaultInstance());
        AsWrapperTypeDeserializer des = new AsWrapperTypeDeserializer(
                baseType, idRes, "typeProp", true, baseType
        );

        ObjectMapper mapper = new ObjectMapper();
        JsonParser jp = mapper.getFactory().createParser("{}");
        // Advance to END_OBJECT or similar to test various conditions
        while (jp.nextToken() != JsonToken.END_OBJECT) {
            // consume
        }

        DeserializationContext ctxt = mapper.getDeserializationContext();
        
        try {
            des.deserializeTypedFromObject(jp, ctxt);
            fail("Expected exception for missing wrapper object");
        } catch (MismatchedInputException | JsonMappingException e) {
            // Expected
        }
    }

    @Test
    public void testDefaultTypingAndInclusion() {
        JavaType baseType = TypeFactory.defaultInstance().constructType(Integer.class);
        TypeIdResolver idRes = new TypeNameIdResolver(baseType, TypeFactory.defaultInstance());
        AsWrapperTypeDeserializer des = new AsWrapperTypeDeserializer(
                baseType, idRes, "typeProp", false, baseType
        );

        assertEquals(JsonTypeInfo.As.WRAPPER_OBJECT, des.getTypeInclusion());
    }
}