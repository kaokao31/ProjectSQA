package com.fasterxml.jackson.databind.deser;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.cfg.DeserializerFactoryConfig;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class BasicDeserializerFactoryTest {

    private static class ConcreteDeserializerFactory extends BasicDeserializerFactory {
        public ConcreteDeserializerFactory() {
            super(new DeserializerFactoryConfig());
        }

        @Override
        public DeserializerFactory withConfig(DeserializerFactoryConfig config) {
            return this;
        }
    }

    @Test
    public void testCreateKeyDeserializerWithCustomFactory() throws Exception {
        ConcreteDeserializerFactory factory = new ConcreteDeserializerFactory();
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);

        // This tests key deserializer creation flow which goes through BasicDeserializerFactory
        KeyDeserializer kd = factory.createKeyDeserializer(ctxt, type);
        // String key deserializer should be standard / null for default handling
        assertNull(kd);
    }

    @Test
    public void testFindStdDeserializer() {
        ConcreteDeserializerFactory factory = new ConcreteDeserializerFactory();
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);

        // Exercise finding standard deserializer or delegates
        JsonDeserializer<?> deser = factory.findStdDeserializer(ctxt, type, null);
        assertNull(deser);
    }

    @Test
    public void testPropertyBasedCreator() {
        ConcreteDeserializerFactory factory = new ConcreteDeserializerFactory();
        assertNotNull(factory.withConfig(new DeserializerFactoryConfig()));
    }
}