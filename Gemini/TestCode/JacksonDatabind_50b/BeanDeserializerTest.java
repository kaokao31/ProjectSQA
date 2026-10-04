package com.fasterxml.jackson.databind.deser;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.BasicBeanDescription;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.impl.AsPropertyTypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.impl.ClassNameIdResolver;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.NameTransformer;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class BeanDeserializerTest {

    static class SimpleBean {
        public String name;
        public int age;
    }

    @Test
    public void testConstructionAndCopy() {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.constructType(SimpleBean.class);
        BasicBeanDescription beanDesc = mapper.getDeserializationConfig().introspect(type);
        
        BeanDeserializerBuilder builder = new BeanDeserializerBuilder(beanDesc, mapper.getDeserializationConfig());
        BeanDeserializerModifier modifier = new BeanDeserializerModifier() {};
        
        // Exercise constructor with builder
        BeanDeserializer deserializer = new BeanDeserializer(
                builder, beanDesc, null, null, null, false, false
        );
        
        assertNotNull(deserializer);
        assertFalse(deserializer.isCachable());
        assertTrue(deserializer.supportsUpdate(mapper.getDeserializationConfig()));

        // Test unwrapping
        NameTransformer transformer = NameTransformer.simplePrefix("pre_");
        BeanDeserializer unwrapped = (BeanDeserializer) deserializer.unwrappingDeserializer(transformer);
        assertNotNull(unwrapped);
        assertNotSame(deserializer, unwrapped);

        // Test withIgnorableProperties
        HashSet<String> ignorable = new HashSet<>();
        ignorable.add("age");
        BeanDeserializer withIgnored = (BeanDeserializer) deserializer.withIgnorableProperties(ignorable);
        assertNotNull(withIgnored);

        // Test withBeanId
        BeanDeserializer withId = (BeanDeserializer) deserializer.withBeanId(null);
        assertNotNull(withId);
    }

    @Test
    public void testWithObjectId() {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.constructType(SimpleBean.class);
        BasicBeanDescription beanDesc = mapper.getDeserializationConfig().introspect(type);
        BeanDeserializerBuilder builder = new BeanDeserializerBuilder(beanDesc, mapper.getDeserializationConfig());
        
        BeanDeserializer deserializer = new BeanDeserializer(
                builder, beanDesc, null, null, null, false, false
        );

        BeanDeserializer withObjId = (BeanDeserializer) deserializer.withObjectIdReader(null);
        assertNotNull(withObjId);
    }

    @Test
    public void testAsArrayDeserialization() {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.constructType(SimpleBean.class);
        BasicBeanDescription beanDesc = mapper.getDeserializationConfig().introspect(type);
        BeanDeserializerBuilder builder = new BeanDeserializerBuilder(beanDesc, mapper.getDeserializationConfig());
        
        BeanDeserializer deserializer = new BeanDeserializer(
                builder, beanDesc, null, null, null, false, false
        );

        JsonDeserializer<?> asArray = deserializer.asArrayDeserializer();
        assertNotNull(asArray);
    }
}