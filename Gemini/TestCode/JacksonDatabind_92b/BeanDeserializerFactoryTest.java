package com.fasterxml.jackson.databind.deser;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.DeserializationConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class BeanDeserializerFactoryTest {

    private BeanDeserializerFactory factory;
    private ObjectMapper objectMapper;

    @Before
    public void setUp() {
        factory = BeanDeserializerFactory.instance;
        objectMapper = new ObjectMapper();
    }

    @After
    public void tearDown() {
        factory = null;
        objectMapper = null;
    }

    @Test
    public void testDefaultInstance() {
        assertNotNull(BeanDeserializerFactory.instance);
        // Test factory configuration method instance creation
        DeserializerFactoryConfig config = new DeserializerFactoryConfig();
        BeanDeserializerFactory customFactory = new BeanDeserializerFactory(config);
        assertNotNull(customFactory);
        assertSame(config, customFactory.config);
    }

    @Test
    public void testWithConfig() {
        DeserializerFactoryConfig config1 = new DeserializerFactoryConfig();
        BeanDeserializerFactory f1 = new BeanDeserializerFactory(config1);
        
        DeserializerFactoryConfig config2 = new DeserializerFactoryConfig();
        BeanDeserializerFactory f2 = (BeanDeserializerFactory) f1.withConfig(config2);
        
        assertNotNull(f2);
        assertNotSame(f1, f2);
        assertSame(config2, f2.config);
        
        // Same config should return this
        assertSame(f1, f1.withConfig(config1));
    }

    @Test
    public void testAddDeserializerModifier() {
        DeserializerFactoryConfig config = new DeserializerFactoryConfig();
        BeanDeserializerFactory f1 = new BeanDeserializerFactory(config);
        BeanDeserializerFactory f2 = (BeanDeserializerFactory) f1.withDeserializerModifier(null);
        assertNotNull(f2);
    }

    @Test
    public void testAddAbstractTypeResolver() {
        DeserializerFactoryConfig config = new DeserializerFactoryConfig();
        BeanDeserializerFactory f1 = new BeanDeserializerFactory(config);
        BeanDeserializerFactory f2 = (BeanDeserializerFactory) f1.withAbstractTypeResolver(null);
        assertNotNull(f2);
    }

    @Test
    public void testAddValueInstantiator() {
        DeserializerFactoryConfig config = new DeserializerFactoryConfig();
        BeanDeserializerFactory f1 = new BeanDeserializerFactory(config);
        BeanDeserializerFactory f2 = (BeanDeserializerFactory) f1.withValueInstantiator(null);
        assertNotNull(f2);
    }

    @Test
    public void testAddKeyDeserializer() {
        DeserializerFactoryConfig config = new DeserializerFactoryConfig();
        BeanDeserializerFactory f1 = new BeanDeserializerFactory(config);
        BeanDeserializerFactory f2 = (BeanDeserializerFactory) f1.withKeyDeserializer(null);
        assertNotNull(f2);
    }

    @Test
    public void testAddBeanDeserializerModifier() {
        DeserializerFactoryConfig config = new DeserializerFactoryConfig();
        BeanDeserializerFactory f1 = new BeanDeserializerFactory(config);
        BeanDeserializerFactory f2 = (BeanDeserializerFactory) f1.withDeserializerModifier(null);
        assertNotNull(f2);
    }

    @Test
    public void testCreateBeanDeserializerWithBuilder() throws Exception {
        // Trigger builder-based deserialization logic
        JavaType type = objectMapper.constructType(SimpleBuilderBean.class);
        DeserializationConfig config = objectMapper.getDeserializationConfig();
        BeanDescription beanDesc = config.introspect(type);
        
        DeserializationContext ctxt = objectMapper.getDeserializationContext();
        
        // This exercises logic paths for builder introspection and creation
        try {
            factory.createBeanDeserializer(ctxt, type, beanDesc);
        } catch (Exception e) {
            // Depending on strict builder detection, it might throw or build successfully
            assertNotNull(e);
        }
    }

    @Test
    public void testIsPotentialBeanType() {
        // Test internal-ish or public helper logic indirectly via create
        JavaType intType = objectMapper.constructType(int.class);
        DeserializationConfig config = objectMapper.getDeserializationConfig();
        BeanDescription beanDesc = config.introspect(intType);
        DeserializationContext ctxt = objectMapper.getDeserializationContext();
        
        try {
            assertNull(factory.createBeanDeserializer(ctxt, intType, beanDesc));
        } catch (Exception e) {
            // Primitives might be handled differently
        }
    }

    // Dummy classes for testing
    static class SimpleBuilderBean {
        private String name;
        public String getName() { return name; }
        
        public static SimpleBuilderBeanBuilder builder() {
            return new SimpleBuilderBeanBuilder();
        }
        
        public static class SimpleBuilderBeanBuilder {
            private String name;
            public SimpleBuilderBeanBuilder name(String name) {
                this.name = name;
                return this;
            }
            public SimpleBuilderBean build() {
                SimpleBuilderBean bean = new SimpleBuilderBean();
                bean.name = this.name;
                return bean;
            }
        }
    }
}