package com.fasterxml.jackson.databind.cfg;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.introspect.*;
import com.fasterxml.jackson.databind.jsontype.*;
import com.fasterxml.jackson.databind.node.*;
import com.fasterxml.jackson.databind.ser.*;
import com.fasterxml.jackson.databind.deser.*;
import com.fasterxml.jackson.databind.util.*;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Comprehensive JUnit 4 test suite for BaseSettings.
 * Designed to achieve high coverage and detect faults (e.g., bug 24).
 */
public class BaseSettingsTest {

    private BaseSettings baseSettings;
    private ObjectMapper objectMapper;
    private ClassIntrospector classIntrospector;
    private AnnotationIntrospector annotationIntrospector;
    private VisibilityChecker<?> visibilityChecker;
    private PropertyNamingStrategy propertyNamingStrategy;
    private SerializationConfig serializationConfig;
    private DeserializationConfig deserializationConfig;

    @Before
    public void setUp() {
        // Create a default BaseSettings instance (using ObjectMapper's default)
        objectMapper = new ObjectMapper();
        baseSettings = objectMapper.getDeserializationConfig().getBaseSettings();
        // For testing with methods, we need to create dummy implementations
        classIntrospector = new BasicClassIntrospector();
        annotationIntrospector = new JacksonAnnotationIntrospector();
        visibilityChecker = new DefaultVisibilityChecker();
        propertyNamingStrategy = PropertyNamingStrategy.LOWER_CAMEL_CASE;
        serializationConfig = objectMapper.getSerializationConfig();
        deserializationConfig = objectMapper.getDeserializationConfig();
    }

    @Test
    public void testWithAppName() {
        BaseSettings modified = baseSettings.withAppName("testApp");
        assertNotNull("withAppName should return non-null", modified);
        assertNotSame("withAppName should return new instance", baseSettings, modified);
        assertEquals("AppName should be set", "testApp", modified.getAppName());
        // Original should remain unchanged
        assertNull("Original appName should be null", baseSettings.getAppName());
    }

    @Test
    public void testWithAppNameNull() {
        BaseSettings modified = baseSettings.withAppName(null);
        assertNotNull("withAppName(null) should return non-null", modified);
        assertNotSame("withAppName(null) should return new instance", baseSettings, modified);
        assertNull("AppName should be null", modified.getAppName());
    }

    @Test
    public void testWithAppNameEmpty() {
        BaseSettings modified = baseSettings.withAppName("");
        assertNotNull("withAppName('') should return non-null", modified);
        assertEquals("AppName should be empty string", "", modified.getAppName());
    }

    @Test
    public void testWithObjectMapper() {
        ObjectMapper newMapper = new ObjectMapper();
        BaseSettings modified = baseSettings.withObjectMapper(newMapper);
        assertNotNull("withObjectMapper should return non-null", modified);
        assertNotSame("withObjectMapper should return new instance", baseSettings, modified);
        assertSame("ObjectMapper should be set", newMapper, modified.getObjectMapper());
        // Original should have different mapper
        assertNotSame("Original ObjectMapper should be different", newMapper, baseSettings.getObjectMapper());
    }

    @Test
    public void testWithObjectMapperNull() {
        BaseSettings modified = baseSettings.withObjectMapper(null);
        assertNotNull("withObjectMapper(null) should return non-null", modified);
        assertNull("ObjectMapper should be null", modified.getObjectMapper());
    }

    @Test
    public void testWithClassIntrospector() {
        BaseSettings modified = baseSettings.withClassIntrospector(classIntrospector);
        assertNotNull("withClassIntrospector should return non-null", modified);
        assertNotSame("withClassIntrospector should return new instance", baseSettings, modified);
        assertSame("ClassIntrospector should be set", classIntrospector, modified.getClassIntrospector());
    }

    @Test
    public void testWithClassIntrospectorNull() {
        BaseSettings modified = baseSettings.withClassIntrospector(null);
        assertNotNull("withClassIntrospector(null) should return non-null", modified);
        assertNull("ClassIntrospector should be null", modified.getClassIntrospector());
    }

    @Test
    public void testWithAnnotationIntrospector() {
        BaseSettings modified = baseSettings.withAnnotationIntrospector(annotationIntrospector);
        assertNotNull("withAnnotationIntrospector should return non-null", modified);
        assertNotSame("withAnnotationIntrospector should return new instance", baseSettings, modified);
        assertSame("AnnotationIntrospector should be set", annotationIntrospector, modified.getAnnotationIntrospector());
    }

    @Test
    public void testWithAnnotationIntrospectorNull() {
        BaseSettings modified = baseSettings.withAnnotationIntrospector(null);
        assertNotNull("withAnnotationIntrospector(null) should return non-null", modified);
        assertNull("AnnotationIntrospector should be null", modified.getAnnotationIntrospector());
    }

    @Test
    public void testWithVisibilityChecker() {
        BaseSettings modified = baseSettings.withVisibilityChecker(visibilityChecker);
        assertNotNull("withVisibilityChecker should return non-null", modified);
        assertNotSame("withVisibilityChecker should return new instance", baseSettings, modified);
        assertSame("VisibilityChecker should be set", visibilityChecker, modified.getVisibilityChecker());
    }

    @Test
    public void testWithVisibilityCheckerNull() {
        BaseSettings modified = baseSettings.withVisibilityChecker(null);
        assertNotNull("withVisibilityChecker(null) should return non-null", modified);
        assertNull("VisibilityChecker should be null", modified.getVisibilityChecker());
    }

    @Test
    public void testWithPropertyNamingStrategy() {
        BaseSettings modified = baseSettings.withPropertyNamingStrategy(propertyNamingStrategy);
        assertNotNull("withPropertyNamingStrategy should return non-null", modified);
        assertNotSame("withPropertyNamingStrategy should return new instance", baseSettings, modified);
        assertSame("PropertyNamingStrategy should be set", propertyNamingStrategy, modified.getPropertyNamingStrategy());
    }

    @Test
    public void testWithPropertyNamingStrategyNull() {
        BaseSettings modified = baseSettings.withPropertyNamingStrategy(null);
        assertNotNull("withPropertyNamingStrategy(null) should return non-null", modified);
        assertNull("PropertyNamingStrategy should be null", modified.getPropertyNamingStrategy());
    }

    @Test
    public void testWithSerializationConfig() {
        BaseSettings modified = baseSettings.withSerializationConfig(serializationConfig);
        assertNotNull("withSerializationConfig should return non-null", modified);
        assertNotSame("withSerializationConfig should return new instance", baseSettings, modified);
        assertSame("SerializationConfig should be set", serializationConfig, modified.getSerializationConfig());
    }

    @Test
    public void testWithSerializationConfigNull() {
        BaseSettings modified = baseSettings.withSerializationConfig(null);
        assertNotNull("withSerializationConfig(null) should return non-null", modified);
        assertNull("SerializationConfig should be null", modified.getSerializationConfig());
    }

    @Test
    public void testWithDeserializationConfig() {
        BaseSettings modified = baseSettings.withDeserializationConfig(deserializationConfig);
        assertNotNull("withDeserializationConfig should return non-null", modified);
        assertNotSame("withDeserializationConfig should return new instance", baseSettings, modified);
        assertSame("DeserializationConfig should be set", deserializationConfig, modified.getDeserializationConfig());
    }

    @Test
    public void testWithDeserializationConfigNull() {
        BaseSettings modified = baseSettings.withDeserializationConfig(null);
        assertNotNull("withDeserializationConfig(null) should return non-null", modified);
        assertNull("DeserializationConfig should be null", modified.getDeserializationConfig());
    }

    @Test
    public void testMultipleWithCalls() {
        // Chain multiple with calls and verify all fields are set correctly
        BaseSettings modified = baseSettings
                .withAppName("multiApp")
                .withObjectMapper(new ObjectMapper())
                .withClassIntrospector(classIntrospector)
                .withAnnotationIntrospector(annotationIntrospector)
                .withVisibilityChecker(visibilityChecker)
                .withPropertyNamingStrategy(propertyNamingStrategy)
                .withSerializationConfig(serializationConfig)
                .withDeserializationConfig(deserializationConfig);

        assertNotNull("Chained with calls should return non-null", modified);
        assertEquals("AppName should be 'multiApp'", "multiApp", modified.getAppName());
        assertNotNull("ObjectMapper should not be null", modified.getObjectMapper());
        assertSame("ClassIntrospector should be set", classIntrospector, modified.getClassIntrospector());
        assertSame("AnnotationIntrospector should be set", annotationIntrospector, modified.getAnnotationIntrospector());
        assertSame("VisibilityChecker should be set", visibilityChecker, modified.getVisibilityChecker());
        assertSame("PropertyNamingStrategy should be set", propertyNamingStrategy, modified.getPropertyNamingStrategy());
        assertSame("SerializationConfig should be set", serializationConfig, modified.getSerializationConfig());
        assertSame("DeserializationConfig should be set", deserializationConfig, modified.getDeserializationConfig());
    }

    @Test
    public void testImmutabilityAfterWith() {
        // Verify that modifying the returned object does not affect the original
        BaseSettings modified = baseSettings.withAppName("newApp");
        // The original should still have null appName
        assertNull("Original should remain unchanged", baseSettings.getAppName());
        // Modify the modified object (if possible) - but BaseSettings is immutable, so we just check that original is untouched
        BaseSettings modified2 = modified.withAppName("anotherApp");
        assertNotSame("Further modification should create new instance", modified, modified2);
        assertEquals("Modified2 should have 'anotherApp'", "anotherApp", modified2.getAppName());
        assertEquals("Modified should still have 'newApp'", "newApp", modified.getAppName());
        assertNull("Original should still have null", baseSettings.getAppName());
    }

    @Test
    public void testCopy() {
        // If copy method exists, test it
        BaseSettings copy = baseSettings.copy();
        assertNotNull("copy should return non-null", copy);
        assertNotSame("copy should return new instance", baseSettings, copy);
        // Verify fields are equal
        assertEquals("AppName should be equal", baseSettings.getAppName(), copy.getAppName());
        assertEquals("ObjectMapper should be equal", baseSettings.getObjectMapper(), copy.getObjectMapper());
        // ... other fields
    }

    @Test(expected = NullPointerException.class)
    public void testWithNullAppNameThrowsException() {
        // Some implementations might throw NPE on null
        baseSettings.withAppName(null);
    }

    @Test
    public void testWithAllNulls() {
        // Test that setting all fields to null works
        BaseSettings modified = baseSettings
                .withAppName(null)
                .withObjectMapper(null)
                .withClassIntrospector(null)
                .withAnnotationIntrospector(null)
                .withVisibilityChecker(null)
                .withPropertyNamingStrategy(null)
                .withSerializationConfig(null)
                .withDeserializationConfig(null);
        assertNotNull("All nulls should return non-null", modified);
        assertNull("AppName should be null", modified.getAppName());
        assertNull("ObjectMapper should be null", modified.getObjectMapper());
        assertNull("ClassIntrospector should be null", modified.getClassIntrospector());
        assertNull("AnnotationIntrospector should be null", modified.getAnnotationIntrospector());
        assertNull("VisibilityChecker should be null", modified.getVisibilityChecker());
        assertNull("PropertyNamingStrategy should be null", modified.getPropertyNamingStrategy());
        assertNull("SerializationConfig should be null", modified.getSerializationConfig());
        assertNull("DeserializationConfig should be null", modified.getDeserializationConfig());
    }

    @Test
    public void testDefaultValues() {
        // Verify that default BaseSettings has expected null/not-null values
        assertNull("Default appName should be null", baseSettings.getAppName());
        assertNotNull("Default ObjectMapper should not be null", baseSettings.getObjectMapper());
        assertNotNull("Default ClassIntrospector should not be null", baseSettings.getClassIntrospector());
        assertNotNull("Default AnnotationIntrospector should not be null", baseSettings.getAnnotationIntrospector());
        assertNotNull("Default VisibilityChecker should not be null", baseSettings.getVisibilityChecker());
        assertNotNull("Default PropertyNamingStrategy should not be null", baseSettings.getPropertyNamingStrategy());
        assertNotNull("Default SerializationConfig should not be null", baseSettings.getSerializationConfig());
        assertNotNull("Default DeserializationConfig should not be null", baseSettings.getDeserializationConfig());
    }

    @Test
    public void testWithSameInstanceReturnsNewInstance() {
        // Verify that even if we pass the same value, a new instance is returned
        ObjectMapper sameMapper = baseSettings.getObjectMapper();
        BaseSettings modified = baseSettings.withObjectMapper(sameMapper);
        assertNotSame("Should return new instance even with same value", baseSettings, modified);
        assertSame("ObjectMapper should be the same object", sameMapper, modified.getObjectMapper());
    }

    @Test
    public void testWithSerializationConfigPreservesOtherFields() {
        // Set a field, then set another, and verify first field is preserved
        BaseSettings withApp = baseSettings.withAppName("preserveTest");
        BaseSettings withSerial = withApp.withSerializationConfig(serializationConfig);
        assertEquals("AppName should be preserved", "preserveTest", withSerial.getAppName());
        assertSame("SerializationConfig should be set", serializationConfig, withSerial.getSerializationConfig());
    }

    @Test
    public void testWithDeserializationConfigPreservesOtherFields() {
        BaseSettings withApp = baseSettings.withAppName("preserveTest2");
        BaseSettings withDeser = withApp.withDeserializationConfig(deserializationConfig);
        assertEquals("AppName should be preserved", "preserveTest2", withDeser.getAppName());
        assertSame("DeserializationConfig should be set", deserializationConfig, withDeser.getDeserializationConfig());
    }

    @Test
    public void testWithClassIntrospectorPreservesOtherFields() {
        BaseSettings withApp = baseSettings.withAppName("preserveTest3");
        BaseSettings withCI = withApp.withClassIntrospector(classIntrospector);
        assertEquals("AppName should be preserved", "preserveTest3", withCI.getAppName());
        assertSame("ClassIntrospector should be set", classIntrospector, withCI.getClassIntrospector());
    }

    @Test
    public void testWithAnnotationIntrospectorPreservesOtherFields() {
        BaseSettings withApp = baseSettings.withAppName("preserveTest4");
        BaseSettings withAI = withApp.withAnnotationIntrospector(annotationIntrospector);
        assertEquals("AppName should be preserved", "preserveTest4", withAI.getAppName());
        assertSame("AnnotationIntrospector should be set", annotationIntrospector, withAI.getAnnotationIntrospector());
    }

    @Test
    public void testWithVisibilityCheckerPreservesOtherFields() {
        BaseSettings withApp = baseSettings.withAppName("preserveTest5");
        BaseSettings withVC = withApp.withVisibilityChecker(visibilityChecker);
        assertEquals("AppName should be preserved", "preserveTest5", withVC.getAppName());
        assertSame("VisibilityChecker should be set", visibilityChecker, withVC.getVisibilityChecker());
    }

    @Test
    public void testWithPropertyNamingStrategyPreservesOtherFields() {
        BaseSettings withApp = baseSettings.withAppName("preserveTest6");
        BaseSettings withPNS = withApp.withPropertyNamingStrategy(propertyNamingStrategy);
        assertEquals("AppName should be preserved", "preserveTest6", withPNS.getAppName());
        assertSame("PropertyNamingStrategy should be set", propertyNamingStrategy, withPNS.getPropertyNamingStrategy());
    }
}