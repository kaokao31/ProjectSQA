package com.fasterxml.jackson.databind.deser;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.impl.BeanAsArrayDeserializer;
import com.fasterxml.jackson.databind.deser.impl.ObjectIdReader;
import com.fasterxml.jackson.databind.deser.impl.PropertyBasedCreator;
import com.fasterxml.jackson.databind.deser.impl.PropertyValueBuffer;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.NameTransformer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class BeanDeserializerBaseTest {

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
    public void testDummyConcreteImplementationMethods() {
        JavaType type = TypeFactory.defaultInstance().constructType(DummyBean.class);
        BeanDescription beanDesc = objectMapper.getSerializationConfig().introspect(type);
        
        // Construct a simple BeanDeserializer via ObjectMapper to get a concrete instance
        JsonDeserializer<?> deser = objectMapper.getDeserializerProvider().findValueDeserializer(
                objectMapper.getDeserializationConfig(), type, type);
        
        assertTrue(deser instanceof BeanDeserializerBase);
        BeanDeserializerBase base = (BeanDeserializerBase) deser;

        // Test various getters and base methods that are implemented
        assertNotNull(base.getValueType());
        assertFalse(base.isCachable());
        assertFalse(base.isCaseInsensitive());
        assertEquals(type, base.getValueClass() != null ? TypeFactory.defaultInstance().constructType(base.getValueClass()) : base.getValueType());
        
        // Test handling of ignored properties setup
        BeanDeserializerBase withIgnored = base.withIgnoredProperties(new HashSet<>());
        assertNotNull(withIgnored);

        // Test ObjectId reader
        assertNull(base.getObjectIdReader());

        // Test unwrapping
        BeanDeserializerBase unwrapped = base.unwrappingDeserializer(NameTransformer.NOP);
        assertNotNull(unwrapped);

        // Test bean as array
        BeanDeserializerBase asArray = base.asArrayDeserializer();
        assertNotNull(asArray);
    }

    @Test
    public void testFindProperty() {
        JavaType type = TypeFactory.defaultInstance().constructType(DummyBean.class);
        JsonDeserializer<?> deser = objectMapper.getDeserializerProvider().findValueDeserializer(
                objectMapper.getDeserializationConfig(), type, type);
        
        BeanDeserializerBase base = (BeanDeserializerBase) deser;
        
        // Find existing property
        SettableBeanProperty prop = base.findProperty("name");
        assertNotNull(prop);
        assertEquals("name", prop.getName());

        // Find non-existing property
        SettableBeanProperty missing = base.findProperty("nonExistentProperty");
        assertNull(missing);

        // Find property with null string
        SettableBeanProperty nullProp = base.findProperty((String) null);
        assertNull(nullProp);
    }

    @Test
    public void testDeserializationWithView() {
        JavaType type = TypeFactory.defaultInstance().constructType(DummyBean.class);
        JsonDeserializer<?> deser = objectMapper.getDeserializerProvider().findValueDeserializer(
                objectMapper.getDeserializationConfig(), type, type);
        
        BeanDeserializerBase base = (BeanDeserializerBase) deser;
        BeanDeserializerBase withView = base.withBeanProperties(base.properties);
        assertNotNull(withView);
    }

    @Test
    public void testObjectIdHandling() {
        JavaType type = TypeFactory.defaultInstance().constructType(DummyBean.class);
        JsonDeserializer<?> deser = objectMapper.getDeserializerProvider().findValueDeserializer(
                objectMapper.getDeserializationConfig(), type, type);
        BeanDeserializerBase base = (BeanDeserializerBase) deser;

        // Verify that standard object id handling returns null or defaults gracefully when not configured
        assertNull(base.getObjectIdReader());
    }

    @Test
    public void testCreateWithNulls() {
        JavaType type = TypeFactory.defaultInstance().constructType(DummyBean.class);
        JsonDeserializer<?> deser = objectMapper.getDeserializerProvider().findValueDeserializer(
                objectMapper.getDeserializationConfig(), type, type);
        BeanDeserializerBase base = (BeanDeserializerBase) deser;

        assertNotNull(base.handledType());
    }

    // Dummy bean class to exercise BeanDeserializer generation and inspection
    public static class DummyBean {
        public String name;
        public int id;

        public DummyBean() {}

        public DummyBean(String name, int id) {
            this.name = name;
            this.id = id;
        }

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
}