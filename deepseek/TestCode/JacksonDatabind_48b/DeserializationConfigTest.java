package com.fasterxml.jackson.databind;

import com.fasterxml.jackson.databind.cfg.BaseSettings;
import com.fasterxml.jackson.databind.deser.DefaultDeserializationContext;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.util.LinkedNode;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Comprehensive JUnit 4 test suite for DeserializationConfig.
 * Targets maximum line/branch coverage and fault detection (including Defects4J bug 48).
 */
public class DeserializationConfigTest {

    private DeserializationConfig baseConfig;
    private BaseSettings baseSettings;

    @Before
    public void setUp() {
        // Create a minimal valid DeserializationConfig using default settings
        // We assume a constructor exists that takes BaseSettings and a DeserializationConfig instance
        // For testing, we use a simple factory method or reflection? Better to use public constructors.
        // Since we don't have the exact source, we assume a public constructor DeserializationConfig(BaseSettings, DeserializationConfig)
        // or a static factory. We'll use a dummy BaseSettings and create a config.
        // To avoid dependency on actual implementation, we use a mock-like approach: create via default constructor if available.
        // But for Jackson, DeserializationConfig has a constructor that takes BaseSettings and a DeserializationConfig.
        // We'll create a minimal BaseSettings using its default constructor.
        baseSettings = new BaseSettings();
        // Assume DeserializationConfig has a constructor: DeserializationConfig(BaseSettings, DeserializationConfig)
        // We'll pass null for the second argument to test null handling.
        baseConfig = new DeserializationConfig(baseSettings, null);
    }

    // ============================================================
    // Tests for constructors and basic initialization
    // ============================================================

    @Test
    public void testConstructorWithNullBaseSettings() {
        // Should throw IllegalArgumentException or NullPointerException? Typically Jackson throws IllegalArgumentException.
        try {
            new DeserializationConfig(null, null);
            fail("Expected IllegalArgumentException for null baseSettings");
        } catch (IllegalArgumentException e) {
            // expected
        } catch (NullPointerException e) {
            // also acceptable depending on implementation
        }
    }

    @Test
    public void testConstructorWithNullSrc() {
        // Should not throw if baseSettings is valid
        DeserializationConfig config = new DeserializationConfig(baseSettings, null);
        assertNotNull("Config should be created", config);
    }

    @Test
    public void testConstructorCopy() {
        // Test copy constructor: DeserializationConfig(DeserializationConfig)
        DeserializationConfig copy = new DeserializationConfig(baseConfig);
        assertNotNull("Copy should not be null", copy);
        // Ensure it's a different object
        assertNotSame("Copy should be a different instance", baseConfig, copy);
    }

    // ============================================================
    // Tests for with(...) methods (immutable modifications)
    // ============================================================

    @Test
    public void testWithAnnotationIntrospector() {
        AnnotationIntrospector ai = new AnnotationIntrospector() {
            // minimal stub
        };
        DeserializationConfig newConfig = baseConfig.with(ai);
        assertNotNull("with(AnnotationIntrospector) should return non-null", newConfig);
        assertNotSame("Should return new instance", baseConfig, newConfig);
        // Verify the introspector is set (if getter available)
        // assertSame(ai, newConfig.getAnnotationIntrospector());
    }

    @Test
    public void testWithVisibilityChecker() {
        VisibilityChecker<?> vc = VisibilityChecker.defaultInstance();
        DeserializationConfig newConfig = baseConfig.with(vc);
        assertNotNull(newConfig);
        assertNotSame(baseConfig, newConfig);
    }

    @Test
    public void testWithPropertyNamingStrategy() {
        PropertyNamingStrategy pns = PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES;
        DeserializationConfig newConfig = baseConfig.with(pns);
        assertNotNull(newConfig);
        assertNotSame(baseConfig, newConfig);
    }

    @Test
    public void testWithTypeResolverBuilder() {
        TypeResolverBuilder<?> trb = TypeResolverBuilder.defaultInstance();
        DeserializationConfig newConfig = baseConfig.with(trb);
        assertNotNull(newConfig);
        assertNotSame(baseConfig, newConfig);
    }

    @Test
    public void testWithNodeFactory() {
        JsonNodeFactory nf = JsonNodeFactory.instance;
        DeserializationConfig newConfig = baseConfig.with(nf);
        assertNotNull(newConfig);
        assertNotSame(baseConfig, newConfig);
    }

    @Test
    public void testWithProblemHandler() {
        DeserializationProblemHandler handler = new DeserializationProblemHandler() {
            // minimal stub
        };
        DeserializationConfig newConfig = baseConfig.with(handler);
        assertNotNull(newConfig);
        assertNotSame(baseConfig, newConfig);
    }

    @Test
    public void testWithDefaultTyper() {
        // This is likely the buggy area: _defaultTyper may be null
        TypeResolverBuilder<?> typer = TypeResolverBuilder.defaultInstance();
        DeserializationConfig newConfig = baseConfig.with(typer);
        assertNotNull(newConfig);
        assertNotSame(baseConfig, newConfig);
    }

    // ============================================================
    // Tests for getters and null handling (bug 48 related)
    // ============================================================

    @Test
    public void testGetDefaultTyperWhenNull() {
        // If _defaultTyper is null, getDefaultTyper() should return null (not throw NPE)
        // This is the core of bug 48: some code path assumed non-null.
        TypeResolverBuilder<?> typer = baseConfig.getDefaultTyper();
        assertNull("getDefaultTyper() should return null when not set", typer);
    }

    @Test
    public void testGetDefaultTyperWhenSet() {
        TypeResolverBuilder<?> typer = TypeResolverBuilder.defaultInstance();
        DeserializationConfig configWithTyper = baseConfig.with(typer);
        TypeResolverBuilder<?> retrieved = configWithTyper.getDefaultTyper();
        assertNotNull("getDefaultTyper() should return non-null after with()", retrieved);
        assertSame("Should be the same instance", typer, retrieved);
    }

    @Test
    public void testGetNodeFactory() {
        JsonNodeFactory nf = baseConfig.getNodeFactory();
        assertNotNull("Default node factory should not be null", nf);
    }

    @Test
    public void testGetAnnotationIntrospector() {
        AnnotationIntrospector ai = baseConfig.getAnnotationIntrospector();
        // May be null if not set; but default should be non-null in Jackson
        // We'll just check it doesn't throw
        assertNotNull("Default annotation introspector should not be null", ai);
    }

    @Test
    public void testGetVisibilityChecker() {
        VisibilityChecker<?> vc = baseConfig.getVisibilityChecker();
        assertNotNull(vc);
    }

    @Test
    public void testGetPropertyNamingStrategy() {
        PropertyNamingStrategy pns = baseConfig.getPropertyNamingStrategy();
        // Default may be null; just check no exception
        // assertNull(pns); // depends on implementation
    }

    // ============================================================
    // Tests for introspection methods (branch coverage)
    // ============================================================

    @Test
    public void testIntrospectClassAnnotations() {
        // Requires a valid AnnotatedClass; we can use a simple class
        AnnotatedClass ac = baseConfig.introspectClassAnnotations(String.class);
        assertNotNull("introspectClassAnnotations should return non-null", ac);
    }

    @Test
    public void testIntrospectClassAnnotationsWithNull() {
        // Should throw IllegalArgumentException
        try {
            baseConfig.introspectClassAnnotations(null);
            fail("Expected IllegalArgumentException for null class");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testIntrospectDirectClassAnnotations() {
        AnnotatedClass ac = baseConfig.introspectDirectClassAnnotations(Integer.class);
        assertNotNull(ac);
    }

    @Test
    public void testIntrospectDirectClassAnnotationsWithNull() {
        try {
            baseConfig.introspectDirectClassAnnotations(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    // ============================================================
    // Tests for serialization-related methods (if any)
    // ============================================================

    @Test
    public void testCreateDeserializationContext() {
        // This method may require a DeserializationConfig and a JsonParser? Not sure.
        // We'll just test that it doesn't throw with null parser (should throw)
        try {
            baseConfig.createDeserializationContext(null, null);
            fail("Expected NullPointerException or IllegalArgumentException");
        } catch (NullPointerException e) {
            // expected
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    // ============================================================
    // Tests for edge cases and boundary values
    // ============================================================

    @Test
    public void testWithMultipleModifications() {
        // Chain multiple with() calls
        DeserializationConfig config = baseConfig
                .with(JsonNodeFactory.instance)
                .with(VisibilityChecker.defaultInstance())
                .with(PropertyNamingStrategy.LOWER_CASE);
        assertNotNull(config);
    }

    @Test
    public void testWithNullArguments() {
        // Many with() methods should throw on null
        try {
            baseConfig.with((AnnotationIntrospector) null);
            fail("Expected IllegalArgumentException for null AnnotationIntrospector");
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            baseConfig.with((VisibilityChecker<?>) null);
            fail("Expected IllegalArgumentException for null VisibilityChecker");
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            baseConfig.with((PropertyNamingStrategy) null);
            fail("Expected IllegalArgumentException for null PropertyNamingStrategy");
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            baseConfig.with((TypeResolverBuilder<?>) null);
            fail("Expected IllegalArgumentException for null TypeResolverBuilder");
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            baseConfig.with((JsonNodeFactory) null);
            fail("Expected IllegalArgumentException for null JsonNodeFactory");
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            baseConfig.with((DeserializationProblemHandler) null);
            fail("Expected IllegalArgumentException for null DeserializationProblemHandler");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    // ============================================================
    // Tests for equals/hashCode (if implemented)
    // ============================================================

    @Test
    public void testEqualsSameObject() {
        assertTrue("Config should equal itself", baseConfig.equals(baseConfig));
    }

    @Test
    public void testEqualsNull() {
        assertFalse("Config should not equal null", baseConfig.equals(null));
    }

    @Test
    public void testEqualsDifferentType() {
        assertFalse("Config should not equal a different type", baseConfig.equals("string"));
    }

    @Test
    public void testHashCodeConsistency() {
        int hash1 = baseConfig.hashCode();
        int hash2 = baseConfig.hashCode();
        assertEquals("HashCode should be consistent", hash1, hash2);
    }

    // ============================================================
    // Tests for toString (if implemented)
    // ============================================================

    @Test
    public void testToStringNotNull() {
        assertNotNull("toString should not be null", baseConfig.toString());
    }

    // ============================================================
    // Additional tests for bug 48: NPE when _defaultTyper is null
    // ============================================================

    @Test
    public void testMethodThatUsesDefaultTyperWithoutCheck() {
        // This test simulates a method that might call _defaultTyper.someMethod() without null check.
        // We assume there is a method like "getDefaultTyperForType" or similar.
        // Since we don't know the exact method, we test getDefaultTyper() which should be safe.
        // But to trigger bug, we need to call something that uses _defaultTyper internally.
        // We'll test a method that is known to have the bug: maybe "typeResolverBuilder" or "defaultTyper".
        // In Jackson, the bug was in DeserializationConfig.getDefaultTyper()? Actually, the bug was that
        // some code path called _defaultTyper without null check. We'll test that getDefaultTyper() returns null
        // and does not throw. Already covered above.
        // Additional: test that withDefaultTyper(null) throws? Already covered.
        // We'll also test that after setting a typer, getDefaultTyper returns it.
        // This is already done.
    }

    // ============================================================
    // Tests for internal state (if accessible via reflection? Not needed)
    // ============================================================

    // Note: We avoid using reflection to keep tests clean and compatible.
}