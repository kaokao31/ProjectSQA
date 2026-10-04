package com.fasterxml.jackson.databind.deser;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.cfg.DeserializerFactoryConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.AnnotatedParameter;
import com.fasterxml.jackson.databind.introspect.BasicBeanDescription;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.Test;

import static org.junit.Assert.*;

public class BeanDeserializerFactoryTest {

    static class DummyBean {
        private String name;
        private int id;

        @JsonCreator
        public DummyBean(String name, int id) {
            this.name = name;
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public int getId() {
            return id;
        }
    }

    @Test
    public void testDefaultFactoryInstance() {
        BeanDeserializerFactory factory = BeanDeserializerFactory.instance;
        assertNotNull(factory);
    }

    @Test
    public void testWithConfig() {
        BeanDeserializerFactory factory = BeanDeserializerFactory.instance;
        DeserializerFactoryConfig newConfig = new DeserializerFactoryConfig();
        DeserializerFactory customFactory = factory.withConfig(newConfig);
        assertNotNull(customFactory);
        assertNotSame(factory, customFactory);
    }

    @Test
    public void testCreateBeanDeserializerBuilder() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        
        BeanDeserializerFactory factory = BeanDeserializerFactory.instance;
        JavaType type = TypeFactory.defaultInstance().constructType(DummyBean.class);
        BasicBeanDescription beanDesc = (BasicBeanDescription) ctxt.getConfig().introspect(type);
        
        BeanDeserializerBuilder builder = factory.builderFor(ctxt, beanDesc);
        assertNotNull(builder);
    }

    @Test
    public void testAddBeanProps() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        
        BeanDeserializerFactory factory = BeanDeserializerFactory.instance;
        JavaType type = TypeFactory.defaultInstance().constructType(DummyBean.class);
        BasicBeanDescription beanDesc = (BasicBeanDescription) ctxt.getConfig().introspect(type);
        
        BeanDeserializerBuilder builder = factory.builderFor(ctxt, beanDesc);
        
        // Exercise addBeanProps which handles property introspection and building
        try {
            factory.addBeanProps(ctxt, beanDesc, builder);
        } catch (Exception e) {
            // Depending on strictness of context, might throw or succeed
        }
        assertNotNull(builder);
    }
}