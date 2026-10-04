package com.fasterxml.jackson.databind.introspect;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Converter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.List;

public class JacksonAnnotationIntrospectorTest {

    private JacksonAnnotationIntrospector introspector;
    private ObjectMapper objectMapper;

    @Before
    public void setUp() {
        introspector = new JacksonAnnotationIntrospector();
        objectMapper = new ObjectMapper();
    }

    @Test
    public void testFindDeserializationConverter() throws Exception {
        Method m = DummyClass.class.getMethod("dummyMethod");
        // Testing finding a converter when the annotation might or might not be present
        Object conv = introspector.findDeserializationConverter(AnnotatedMethod.construct(null, m, null, null));
        // DummyClass.dummyMethod has no annotation, should return null
        Assert.assertNull(conv);
    }

    @Test
    public void testFindSerializationConverter() throws Exception {
        Method m = DummyClass.class.getMethod("dummyMethod");
        Object conv = introspector.findSerializationConverter(AnnotatedMethod.construct(null, m, null, null));
        Assert.assertNull(conv);
    }

    @Test
    public void testFindCreatorBinding() throws Exception {
        Method m = DummyClass.class.getMethod("dummyMethod");
        AnnotatedMethod am = AnnotatedMethod.construct(null, m, null, null);
        JsonCreator.Mode mode = introspector.findCreatorBinding(am);
        // Should handle null or default gracefully
        // Depending on annotations, it might return null
        Assert.assertNull(mode);
    }

    @Test
    public void testRefProperties() {
        Method m;
        try {
            m = DummyClass.class.getMethod("dummyMethod");
            AnnotatedMethod am = AnnotatedMethod.construct(null, m, null, null);
            Assert.assertNull(introspector.findReferenceType(am));
            Assert.assertFalse(introspector.isUnwrappingDeserializer(am));
            Assert.assertFalse(introspector.hasIgnoreMarker(am));
        } catch (NoSuchMethodException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testFindSerializationType() throws Exception {
        Method m = DummyClass.class.getMethod("dummyMethod");
        AnnotatedMethod am = AnnotatedMethod.construct(null, m, null, null);
        TypeFactory tf = objectMapper.getTypeFactory();
        JavaType type = tf.constructType(String.class);
        
        // This exercises type refinement logic in JacksonAnnotationIntrospector which is often tied to bug 81 (handling of JavaTypes with Creators/Converters)
        JavaType refined = introspector.refineSerializationType(objectMapper.getDeserializationConfig(), am, type);
        Assert.assertNotNull(refined);
    }

    @Test
    public void testRefineDeserializationType() throws Exception {
        Method m = DummyClass.class.getMethod("dummyMethod");
        AnnotatedMethod am = AnnotatedMethod.construct(null, m, null, null);
        TypeFactory tf = objectMapper.getTypeFactory();
        JavaType type = tf.constructType(String.class);

        JavaType refined = introspector.refineDeserializationType(objectMapper.getDeserializationConfig(), am, type);
        Assert.assertNotNull(refined);
    }

    @Test
    public void testFindNullifyingMethod() throws Exception {
        Method m = DummyClass.class.getMethod("dummyMethod");
        AnnotatedMethod am = AnnotatedMethod.construct(null, m, null, null);
        Assert.assertNull(introspector.findNullSerializer(am));
    }

    // Dummy class for reflection
    static class DummyClass {
        public void dummyMethod() {
        }
    }
}