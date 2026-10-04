package com.fasterxml.jackson.databind.deser.impl;

import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.PropertyMetadata;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.util.Annotations;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

public class BeanPropertyMapTest {

    private static class DummyBeanProperty implements BeanProperty {
        private final String name;

        public DummyBeanProperty(String name) {
            this.name = name;
        }

        @Override public String getName() { return name; }
        @Override public PropertyName getFullName() { return new PropertyName(name); }
        @Override public JavaType getType() { return null; }
        @Override public PropertyName getWrapperName() { return null; }
        @Override public boolean isRequired() { return false; }
        @Override public Annotations getMemberAnnotations() { return null; }
        @Override public AnnotatedMember getMember() { return null; }
        @Override public PropertyMetadata getMetadata() { return PropertyMetadata.STD_REQUIRED_OR_OPTIONAL; }
        @Override public boolean depositSchemaProperty(com.fasterxml.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor objectVisitor, SerializerProvider provider) throws com.fasterxml.jackson.databind.JsonMappingException { return false; }
        @Override public <A extends java.lang.annotation.Annotation> A getAnnotation(Class<A> aClass) { return null; }
        @Override public <A extends java.lang.annotation.Annotation> A getContextAnnotation(Class<A> aClass) { return null; }
        @Override public void depositSchemaProperty(com.fasterxml.jackson.databind.node.ObjectNode osc, SerializerProvider provider) throws com.fasterxml.jackson.databind.JsonMappingException {}
        @Override public com.fasterxml.jackson.databind.PropertyMetadata getMetadata(DeserializationConfig config) { return null; }
    }

    private static class DummyDeserializer extends com.fasterxml.jackson.databind.JsonDeserializer<Object> {
        @Override
        public Object deserialize(com.fasterxml.jackson.databind.JsonParser p, com.fasterxml.jackson.databind.DeserializationContext ctxt) {
            return null;
        }
    }

    private SettableBeanProperty createDummyProperty(String name) {
        JavaType type = com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class);
        return new SettableBeanProperty.Delegating(
                new com.fasterxml.jackson.databind.deser.SettableBeanProperty(new PropertyName(name), type, null, null) {
                    @Override public com.fasterxml.jackson.databind.deser.SettableBeanProperty withName(PropertyName newName) { return this; }
                    @Override public com.fasterxml.jackson.databind.deser.SettableBeanProperty withValueDeserializer(com.fasterxml.jackson.databind.JsonDeserializer<?> des) { return this; }
                    @Override public <A extends java.lang.annotation.Annotation> A getAnnotation(Class<A> aclass) { return null; }
                    @Override public AnnotatedMember getMember() { return null; }
                    @Override public void deserializeAndSet(com.fasterxml.jackson.databind.JsonParser p, com.fasterxml.jackson.databind.DeserializationContext ctxt, Object instance) {}
                    @Override public Object deserializeSetAndReturn(com.fasterxml.jackson.databind.JsonParser p, com.fasterxml.jackson.databind.DeserializationContext ctxt, Object instance) { return null; }
                    @Override public void set(Object instance, Object value) {}
                    @Override public Object setAndReturn(Object instance, Object value) { return null; }
                }
        );
    }

    @Test
    public void testBeanPropertyMapOperations() {
        List<SettableBeanProperty> props = new ArrayList<>();
        SettableBeanProperty p1 = createDummyProperty("propA");
        SettableBeanProperty p2 = createDummyProperty("propB");
        SettableBeanProperty p3 = createDummyProperty("propC");
        
        props.add(p1);
        props.add(p2);
        props.add(p3);

        BeanPropertyMap map = new BeanPropertyMap(false, props);

        Assert.assertNotNull(map);
        Assert.assertEquals(3, map.size());

        // Test finding properties
        Assert.assertNotNull(map.find("propA"));
        Assert.assertNotNull(map.find("propB"));
        Assert.assertNotNull(map.find("propC"));
        Assert.assertNull(map.find("nonExistent"));

        // Test iterator
        Iterator<SettableBeanProperty> it = map.iterator();
        Assert.assertNotNull(it);
        int count = 0;
        while (it.hasNext()) {
            Assert.assertNotNull(it.next());
            count++;
        }
        Assert.assertEquals(3, count);

        // Test withCaseInsensitive
        BeanPropertyMap ciMap = map.withCaseInsensitive(true);
        Assert.assertNotNull(ciMap);
        
        BeanPropertyMap ciMap2 = ciMap.withCaseInsensitive(true);
        Assert.assertSame(ciMap, ciMap2);

        // Test remove
        BeanPropertyMap removedMap = map.remove(p2);
        Assert.assertNotNull(removedMap);
        Assert.assertEquals(2, removedMap.size());
        Assert.assertNull(removedMap.find("propB"));

        // Test rename
        PropertyName newName = new PropertyName("propANew");
        BeanPropertyMap renamedMap = map.rename(p1.withName(newName), newName);
        Assert.assertNotNull(renamedMap);

        // Test toString
        String str = map.toString();
        Assert.assertNotNull(str);
    }

    @Test
    public void testEmptyAndSinglePropertyMaps() {
        BeanPropertyMap emptyMap = new BeanPropertyMap(false, Collections.emptyList());
        Assert.assertEquals(0, emptyMap.size());
        Assert.assertNull(emptyMap.find("any"));

        List<SettableBeanProperty> singleList = Collections.singletonList(createDummyProperty("only"));
        BeanPropertyMap singleMap = new BeanPropertyMap(false, singleList);
        Assert.assertEquals(1, singleMap.size());
        Assert.assertNotNull(singleMap.find("only"));
        Assert.assertNull(singleMap.find("other"));
    }

    @Test
    public void testDuplicateProperties() {
        List<SettableBeanProperty> props = new ArrayList<>();
        props.add(createDummyProperty("dup"));
        props.add(createDummyProperty("dup"));

        BeanPropertyMap map = new BeanPropertyMap(false, props);
        Assert.assertNotNull(map);
    }
}