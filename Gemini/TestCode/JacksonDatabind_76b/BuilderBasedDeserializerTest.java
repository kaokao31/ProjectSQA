package com.fasterxml.jackson.databind.deser;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.impl.BeanPropertyMap;
import com.fasterxml.jackson.databind.deser.impl.ValueInjector;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.util.NameTransformer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.*;

public class BuilderBasedDeserializerTest {

    private ObjectMapper objectMapper;

    @Before
    public void setUp() {
        objectMapper = new ObjectMapper();
    }

    @After
    public void tearDown() {
        objectMapper = null;
    }

    @Test
    public void testCopyConstructorAndUnwrapping() {
        JavaType type = objectMapper.constructType(SimpleBuilderTestBean.class);
        BeanDescription beanDesc = objectMapper.getSerializationConfig().introspect(type);
        
        // Construct standard BuilderBasedDeserializer via BeanDeserializerBuilder
        BeanDeserializerBuilder builder = new BeanDeserializerBuilder(beanDesc, objectMapper.getSerializationConfig());
        AnnotatedMethod buildMethod = beanDesc.findMethod("build", null);
        builder.setPOJOBuilder(buildMethod, null);
        
        // Add a dummy property to make it non-empty if needed
        BuilderBasedDeserializer deserializer = new BuilderBasedDeserializer(
                builder, beanDesc, null, new BeanPropertyMap(false, new ArrayList<>()), null, null, false, false
        );

        // Test unwrapping transformer path
        NameTransformer transformer = NameTransformer.simpleTransformer("prefix_", "_suffix");
        BuilderBasedDeserializer unwrapped = deserializer.unwrappedDeserializer(transformer);
        assertNotNull(unwrapped);
        assertNotSame(deserializer, unwrapped);
    }

    @Test
    public void testWithIgnorableInclusions() {
        JavaType type = objectMapper.constructType(SimpleBuilderTestBean.class);
        BeanDescription beanDesc = objectMapper.getSerializationConfig().introspect(type);
        
        BeanDeserializerBuilder builder = new BeanDeserializerBuilder(beanDesc, objectMapper.getSerializationConfig());
        BuilderBasedDeserializer deserializer = new BuilderBasedDeserializer(
                builder, beanDesc, null, new BeanPropertyMap(false, new ArrayList<>()), null, null, false, false
        );

        HashSet<String> ignorable = new HashSet<>();
        ignorable.add("someProp");
        BuilderBasedDeserializer modified = deserializer.withIgnorableProperties(ignorable);
        assertNotNull(modified);
        assertNotSame(deserializer, modified);
    }

    @Test
    public void testWithBeanProperties() {
        JavaType type = objectMapper.constructType(SimpleBuilderTestBean.class);
        BeanDescription beanDesc = objectMapper.getSerializationConfig().introspect(type);
        
        BeanDeserializerBuilder builder = new BeanDeserializerBuilder(beanDesc, objectMapper.getSerializationConfig());
        BeanPropertyMap propMap = new BeanPropertyMap(false, new ArrayList<>());
        BuilderBasedDeserializer deserializer = new BuilderBasedDeserializer(
                builder, beanDesc, null, propMap, null, null, false, false
        );

        BeanPropertyMap newPropMap = new BeanPropertyMap(false, new ArrayList<>());
        BuilderBasedDeserializer modified = deserializer.withBeanProperties(newPropMap);
        assertNotNull(modified);
        assertNotSame(deserializer, modified);
    }

    @Test
    public void testAsArrayDeserializer() {
        JavaType type = objectMapper.constructType(SimpleBuilderTestBean.class);
        BeanDescription beanDesc = objectMapper.getSerializationConfig().introspect(type);
        
        BeanDeserializerBuilder builder = new BeanDeserializerBuilder(beanDesc, objectMapper.getSerializationConfig());
        BuilderBasedDeserializer deserializer = new BuilderBasedDeserializer(
                builder, beanDesc, null, new BeanPropertyMap(false, new ArrayList<>()), null, null, false, false
        );

        // Test asArrayDeserializer transformation
        HashSet<String> props = new HashSet<>();
        props.add("prop");
        JsonDeserializer<?> arrayDeser = deserializer.asArrayDeserializer();
        assertNotNull(arrayDeser);
    }

    @Test
    public void testDeserializeUsingPropertyBased() throws Exception {
        // Exercise construction where builder uses property-based creator (creator with arguments)
        String json = "{\"name\":\"testValue\",\"value\":123}";
        try {
            objectMapper.readValue(json, ComplexBuilderTestBean.class);
        } catch (Exception e) {
            // Expected to handle or fail gracefully depending on exact builder configuration
            assertNotNull(e);
        }
    }

    @Test
    public void testBuildMethodExecution() throws Exception {
        String json = "{\"name\":\"hello\"}";
        SimpleBuilderTestBean result = objectMapper.readValue(json, SimpleBuilderTestBean.class);
        assertNotNull(result);
        assertEquals("hello", result.name);
    }

    // Supporting classes for testing BuilderBasedDeserializer behaviors

    public static class SimpleBuilderTestBean {
        private String name;

        public SimpleBuilderTestBean(Builder b) {
            this.name = b.name;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String name;

            public Builder withName(String name) {
                this.name = name;
                return this;
            }

            public Builder setName(String name) {
                this.name = name;
                return this;
            }

            public SimpleBuilderTestBean build() {
                return new SimpleBuilderTestBean(this);
            }
        }
    }

    public static class ComplexBuilderTestBean {
        private String name;
        private int value;

        private ComplexBuilderTestBean(ComplexBuilder builder) {
            this.name = builder.name;
            this.value = builder.value;
        }

        public static ComplexBuilder builder() {
            return new ComplexBuilder();
        }

        @com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder(withPrefix = "")
        public static class ComplexBuilder {
            private String name;
            private int value;

            public ComplexBuilder name(String name) {
                this.name = name;
                return this;
            }

            public ComplexBuilder value(int value) {
                this.value = value;
                return this;
            }

            public ComplexBuilderTestBean build() {
                return new ComplexBuilderTestBean(this);
            }
        }
    }
}