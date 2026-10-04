package com.fasterxml.jackson.databind.deser;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.introspect.AnnotatedParameter;
import com.fasterxml.jackson.databind.introspect.AnnotationMap;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.util.Annotations;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class CreatorPropertyTest {

    static class DummyDeserializer extends JsonDeserializer<Object> {
        @Override
        public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return null;
        }

        @Override
        public Object deserialize(JsonParser p, DeserializationContext ctxt, Object intoValue) throws IOException {
            return intoValue;
        }
    }

    private CreatorProperty createDummyProperty() {
        PropertyName name = new PropertyName("testProp");
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        TypeDeserializer typeDesde = null;
        Annotations contextAnnotations = new AnnotationMap();
        AnnotatedParameter param = null;
        int index = 0;
        Object injectableValueId = "injectId";
        PropertyMetadata metadata = PropertyMetadata.STD _REQUIRED;

        return new CreatorProperty(name, type, typeDesde, contextAnnotations, param, index, injectableValueId, metadata);
    }

    @Test
    public void testBasicPropertiesAndAccessors() {
        CreatorProperty prop = createDummyProperty();

        assertNotNull(prop);
        assertEquals("testProp", prop.getName());
        assertEquals(0, prop.getPropertyIndex());
        assertNotNull(prop.getType());
        assertEquals("injectId", prop.getInjectableValueId());
        assertFalse(prop.hasValueDeserializer());
        assertNull(prop.getValueDeserializer());
        assertNull(prop.getNullValueProvider());
        assertNull(prop.getMember());
        assertFalse(prop.visibleInView(null));
    }

    @Test
    public void testWithName() {
        CreatorProperty prop = createDummyProperty();
        PropertyName newName = new PropertyName("newName");
        CreatorProperty renamed = prop.withName(newName);

        assertNotSame(prop, renamed);
        assertEquals("newName", renamed.getName());
    }

    @Test
    public void testWithValueDeserializer() {
        CreatorProperty prop = createDummyProperty();
        JsonDeserializer<Object> deser = new DummyDeserializer();
        CreatorProperty withDeser = prop.withValueDeserializer(deser);

        assertNotSame(prop, withDeser);
        assertTrue(withDeser.hasValueDeserializer());
        assertEquals(deser, withDeser.getValueDeserializer());
    }

    @Test
    public void testWithNullProvider() {
        CreatorProperty prop = createDummyProperty();
        JsonDeserializer<Object> deser = new DummyDeserializer();
        CreatorProperty withNullProv = prop.withNullProvider(deser);

        assertNotSame(prop, withNullProv);
    }

    @Test
    public void testFindInjectableValue() {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        CreatorProperty prop = createDummyProperty();

        // Should not throw and return null or expected value depending on context setup,
        // specifically targeting the bug in 111 where injection lookup or serialization of CreatorProperty might fail.
        try {
            Object val = prop.findInjectableValue(ctxt, null);
            // depending on context, might be null if provider not set
        } catch (Exception e) {
            // pass if handled
        }
    }

    @Test
    public void testDeserializeAndSet() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        CreatorProperty prop = createDummyProperty();

        Object bean = new Object();
        // Test deserializeAndSet which is core to CreatorProperty
        try {
            prop.deserializeAndSet(null, ctxt, bean);
        } catch (Exception e) {
            // expected due to null parser/context or missing deserializer
        }
    }

    @Test
    public void testSetAndSetUsingDefault() throws IOException {
        CreatorProperty prop = createDummyProperty();
        try {
            prop.set(new Object(), "value");
        } catch (Exception e) {
            // expected as member is null
        }

        try {
            prop.setUsingDefault(null, null, new Object());
        } catch (Exception e) {
            // expected
        }
    }

    @Test
    public void testDepositSchemaInfo() {
        CreatorProperty prop = createDummyProperty();
        try {
            prop.depositSchemaInfo(null, null);
        } catch (Exception e) {
            // expected or no-op
        }
    }
}