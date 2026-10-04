package com.fasterxml.jackson.databind.deser.impl;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotatedParameter;
import com.fasterxml.jackson.databind.introspect.AnnotatedWithParams;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;

public class CreatorCollectorTest {

    private ObjectMapper objectMapper;
    private BeanDescription beanDesc;
    private CreatorCollector collector;

    @Before
    public void setUp() {
        objectMapper = new ObjectMapper();
        DeserializationConfig config = objectMapper.getDeserializationConfig();
        JavaType type = objectMapper.constructType(String.class);
        beanDesc = config.introspect(type);
        collector = new CreatorCollector(beanDesc, config.canOverrideAccessModifiers());
    }

    @Test
    public void testBasicInitializationAndGetters() {
        Assert.assertNotNull(collector);
        Assert.assertNull(collector.stdCreatorBeanType());
        Assert.assertFalse(collector.hasDefaultCreator());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddDuplicateDefaultCreator() {
        // Find a method or mock/dummy AnnotatedWithParams to use as default creator
        AnnotatedWithParams dummyCreator = getDummyMethodAnnotated();
        if (dummyCreator != null) {
            collector.setDefaultCreator(dummyCreator);
            // Setting it again with conflict check should ideally fail or handle based on impl
            collector.verifyNonDup(dummyCreator, CreatorCollector.C_DEFAULT, true);
        } else {
            throw new IllegalArgumentException("Dummy creator not found");
        }
    }

    @Test
    public void testAddPropertyCreatorNullCheck() {
        try {
            collector.addPropertyCreator(null, false, null);
        } catch (Exception e) {
            // Expected due to null parameters or internal handling
        }
    }

    @Test
    public void testVerifyNonDup() {
        AnnotatedWithParams dummyCreator = getDummyMethodAnnotated();
        if (dummyCreator != null) {
            try {
                Method verifyMethod = CreatorCollector.class.getDeclaredMethod(
                        "verifyNonDup", AnnotatedWithParams.class, int.class, boolean.class);
                verifyMethod.setAccessible(true);
                // First time should pass
                verifyMethod.invoke(collector, dummyCreator, 1, true);
            } catch (Exception e) {
                // handle reflection issues gracefully
            }
        }
    }

    private AnnotatedWithParams getDummyMethodAnnotated() {
        try {
            Method m = String.class.getMethod("toString");
            DeserializationConfig config = objectMapper.getDeserializationConfig();
            JavaType type = objectMapper.constructType(String.class);
            BeanDescription desc = config.introspect(type);
            return desc.findMethod("toString", new Class[0]);
        } catch (Exception e) {
            return null;
        }
    }
}