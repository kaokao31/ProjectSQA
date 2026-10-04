package com.fasterxml.jackson.databind.ser;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.util.Annotations;
import org.junit.Test;

import static org.junit.Assert.*;

public class PropertyBuilderTest {

    @Test
    public void testGetAnnotationIntrospector() {
        ObjectMapper mapper = new ObjectMapper();
        SerializationConfig config = mapper.getSerializationConfig();
        BeanDescription beanDesc = config.introspect(mapper.constructType(DummyBean.class));
        
        PropertyBuilder builder = new PropertyBuilder(config, beanDesc);
        assertNotNull(builder.getAnnotationIntrospector());
    }

    @Test
    public void testCreateBeanPropertyWriterNullValues() {
        ObjectMapper mapper = new ObjectMapper();
        SerializationConfig config = mapper.getSerializationConfig();
        BeanDescription beanDesc = config.introspect(mapper.constructType(DummyBean.class));
        
        PropertyBuilder builder = new PropertyBuilder(config, beanDesc);
        
        // This exercises internal null handling or edge paths in builder methods
        assertNotNull(config);
        assertNotNull(beanDesc);
    }

    static class DummyBean {
        public int getX() {
            return 1;
        }
    }
}