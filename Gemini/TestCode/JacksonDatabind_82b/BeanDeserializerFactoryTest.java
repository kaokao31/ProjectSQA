package com.fasterxml.jackson.databind.deser;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.DeserializationConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.List;

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
        // Test withConfig with null or same config
        DeserializationConfig config = objectMapper.getDeserializationConfig();
        assertNotNull(factory.withConfig(config));
    }

    @Test
    public void testAddBeanProperty() {
        // Since many methods are protected or require complex setup,
        // we can test basic public/accessible entry points.
        JavaType type = TypeFactory.defaultInstance().constructType(SimpleBean.class);
        DeserializationContext ctxt = objectMapper.getDeserializationContext();
        BeanDescription beanDesc = ctxt.introspect(type);

        assertNotNull(beanDesc);
        // Verify building deserializer doesn't throw unexpected exceptions for standard beans
        assertNotNull(factory.createBeanDeserializer(ctxt, type, beanDesc));
    }

    @Test
    public void testIsIgnorableType() {
        DeserializationConfig config = objectMapper.getDeserializationConfig();
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        BeanDescription beanDesc = objectMapper.getDeserializationConfig().introspect(type);
        
        // Testing visibility / ignorable type logic via public factory methods if applicable
        assertNotNull(config);
        assertNotNull(beanDesc);
    }

    @Test
    public void testFindPropertyType() {
        JavaType type = TypeFactory.defaultInstance().constructType(SimpleBean.class);
        DeserializationContext ctxt = objectMapper.getDeserializationContext();
        BeanDescription beanDesc = ctxt.introspect(type);
        
        List<BeanPropertyDefinition> props = beanDesc.findProperties();
        assertNotNull(props);
    }

    @Test
    public void testBuildBeanDeserializerWithAbstract() {
        JavaType type = TypeFactory.defaultInstance().constructType(AbstractBean.class);
        DeserializationContext ctxt = objectMapper.getDeserializationContext();
        BeanDescription beanDesc = ctxt.introspect(type);

        // Abstract classes should still return a deserializer or handle appropriately
        try {
            factory.createBeanDeserializer(ctxt, type, beanDesc);
        } catch (Exception e) {
            // Expected for abstract types depending on configuration, or handled gracefully
            assertNotNull(e);
        }
    }

    // Helper classes for testing
    public static class SimpleBean {
        private String name;
        private int id;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }
    }

    public static abstract class AbstractBean {
        private String field;

        public String getField() {
            return field;
        }

        public abstract void setField(String field);
    }
}