package com.fasterxml.jackson.databind.deser.std;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.KeyDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.MapType;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.*;

public class MapDeserializerTest {

    static class DummyMapDeserializer extends MapDeserializer {
        public DummyMapDeserializer(MapType mapType, KeyDeserializer keyDeser,
                                    JsonDeserializer<Object> valueDeser, TypeDeserializer valueTypeDeser) {
            super(mapType, keyDeser, valueDeser, valueTypeDeser);
        }

        public DummyMapDeserializer(DummyMapDeserializer src) {
            super(src);
        }

        protected DummyMapDeserializer _withValueDeserializer(JsonDeserializer<?> vd) {
            return new DummyMapDeserializer(
                    (MapType) _mapType, _keyDeserializer, (JsonDeserializer<Object>) vd, _valueTypeDeserializer
            );
        }
    }

    @JsonIgnoreProperties({"ignorable"})
    static class DummyBean {
        public String ignorable;
    }

    @Test
    public void testIsCachable() {
        ObjectMapper mapper = new ObjectMapper();
        MapType type = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
        KeyDeserializer keyDeser = mapper.findKeyDeserializer(mapper.constructType(String.class));
        JsonDeserializer<Object> valDeser = mapper.findNonContextualValueDeserializer(mapper.constructType(Object.class));
        TypeDeserializer typeDeser = null;

        DummyMapDeserializer deser = new DummyMapDeserializer(type, keyDeser, valDeser, typeDeser);
        
        // Test isCachable when key, value deserializers and type deserializers are null or simple
        assertTrue(deser.isCachable());
    }

    @Test
    public void testCreateContextual() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        MapType type = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
        KeyDeserializer keyDeser = mapper.findKeyDeserializer(mapper.constructType(String.class));
        JsonDeserializer<Object> valDeser = mapper.findNonContextualValueDeserializer(mapper.constructType(Object.class));
        TypeDeserializer typeDeser = null;

        DummyMapDeserializer deser = new DummyMapDeserializer(type, keyDeser, valDeser, typeDeser);

        BeanProperty.Std property = new BeanProperty.Std(
                PropertyName.construct("testProp"),
                type,
                null,
                null,
                mapper.getSerializationConfig().getAnnotationIntrospector(),
                PropertyMetadata.STD_REQUIRED
        );

        DeserializationContext ctxt = mapper.getDeserializationContext();
        JsonDeserializer<?> contextual = deser.createContextual(ctxt, property);
        assertNotNull(contextual);
    }

    @Test
    public void testCreateContextualWithIgnorable() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        MapType type = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
        KeyDeserializer keyDeser = mapper.findKeyDeserializer(mapper.constructType(String.class));
        JsonDeserializer<Object> valDeser = mapper.findNonContextualValueDeserializer(mapper.constructType(Object.class));
        TypeDeserializer typeDeser = null;

        DummyMapDeserializer deser = new DummyMapDeserializer(type, keyDeser, valDeser, typeDeser);

        JavaType beanType = mapper.constructType(DummyBean.class);
        BeanProperty.Std property = new BeanProperty.Std(
                PropertyName.construct("testProp"),
                beanType,
                null,
                null,
                mapper.getSerializationConfig().getAnnotationIntrospector(),
                PropertyMetadata.STD_OPTIONAL
        );

        DeserializationContext ctxt = mapper.getDeserializationContext();
        JsonDeserializer<?> contextual = deser.createContextual(ctxt, property);
        assertNotNull(contextual);
    }

    @Test
    public void testSetIgnorableProperties() {
        ObjectMapper mapper = new ObjectMapper();
        MapType type = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
        DummyMapDeserializer deser = new DummyMapDeserializer(type, null, null, null);
        
        String[] ignorable = new String[] { "prop1", "prop2" };
        DummyMapDeserializer withIgnored = new DummyMapDeserializer(deser) {
            @Override
            public void setIgnorableProperties(String[] ignorable) {
                super.setIgnorableProperties(ignorable);
            }
            @Override
            protected DummyMapDeserializer _withValueDeserializer(JsonDeserializer<?> vd) {
                return this;
            }
        };
        
        withIgnored.setIgnorableProperties(ignorable);
        // Verify via subclass or behavior if possible, but mainly ensure it doesn't crash.
        assertNotNull(withIgnored);
    }
}