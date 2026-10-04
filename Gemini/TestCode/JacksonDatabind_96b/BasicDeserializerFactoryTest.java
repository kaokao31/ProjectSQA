package com.fasterxml.jackson.databind.deser;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.cfg.DeserializerFactoryConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class BasicDeserializerFactoryTest {

    private ConcreteBasicDeserializerFactory factory;
    private ObjectMapper objectMapper;
    private DeserializationContext context;

    @Before
    public void setUp() {
        factory = new ConcreteBasicDeserializerFactory();
        objectMapper = new ObjectMapper();
        context = objectMapper.getDeserializationContext();
    }

    @Test
    public void testWithConfig() {
        DeserializerFactoryConfig config = new DeserializerFactoryConfig();
        BasicDeserializerFactory modifiedFactory = factory.withConfig(config);
        assertNotNull(modifiedFactory);
        assertNotSame(factory, modifiedFactory);
    }

    @Test
    public void testMapKeyDeserializer() {
        try {
            JavaType type = TypeFactory.defaultInstance().constructType(String.class);
            factory.createKeyDeserializer(context, type);
        } catch (Exception e) {
            // Expected depending on strict configuration, just verifying execution path
        }
    }

    @Test
    public void testFindTypeDeserializer() {
        try {
            JavaType type = TypeFactory.defaultInstance().constructType(String.class);
            TypeDeserializer typeDeserializer = factory.findTypeDeserializer(context.getConfig(), type);
            // Typically null for basic types without annotations
            assertNull(typeDeserializer);
        } catch (Exception e) {
            // Handled
        }
    }

    /**
    * Concrete implementation of abstract BasicDeserializerFactory to enable testing.
    */
    private static class ConcreteBasicDeserializerFactory extends BasicDeserializerFactory {
        private static final long serialVersionUID = 1L;

        public ConcreteBasicDeserializerFactory() {
            super(new DeserializerFactoryConfig());
        }

        @Override
        public DeserializerFactory withConfig(DeserializerFactoryConfig config) {
            return new ConcreteBasicDeserializerFactory();
        }

        @Override
        public JsonDeserializer<?> createBeanDeserializer(DeserializationContext ctxt, JavaType type, BeanDescription beanDesc) throws JsonMappingException {
            return null;
        }

        @Override
        public JsonDeserializer<?> createBuilderBasedDeserializer(DeserializationContext ctxt, JavaType valueType, BeanDescription beanDesc, Class<?> builderClass, AnnotatedMethod buildMethod) throws JsonMappingException {
            return null;
        }
    }
}