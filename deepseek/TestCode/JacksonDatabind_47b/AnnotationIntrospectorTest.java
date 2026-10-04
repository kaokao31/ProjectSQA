package com.fasterxml.jackson.databind;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.AnnotatedParameter;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospector;
import com.fasterxml.jackson.databind.introspect.AnnotationMap;
import com.fasterxml.jackson.databind.introspect.ClassIntrospector;
import com.fasterxml.jackson.databind.introspect.MemberKey;
import com.fasterxml.jackson.databind.introspect.ObjectIdInfo;
import com.fasterxml.jackson.databind.introspect.PropertyIgnorals;
import com.fasterxml.jackson.databind.introspect.ReferenceProperty;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.ClassUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class AnnotationIntrospectorTest {

    // ------------------------------------------------------------
    // Helper: a concrete stub that returns default values for all abstract methods
    // ------------------------------------------------------------
    static class TestIntrospector extends AnnotationIntrospector {
        @Override
        public Class<?> findSerializer(Annotated a) { return null; }
        @Override
        public Class<?> findKeySerializer(Annotated a) { return null; }
        @Override
        public Class<?> findContentSerializer(Annotated a) { return null; }
        @Override
        public Class<?> findDeserializer(Annotated a) { return null; }
        @Override
        public Class<?> findKeyDeserializer(Annotated a) { return null; }
        @Override
        public Class<?> findContentDeserializer(Annotated a) { return null; }
        @Override
        public String findEnumValue(Class<?> enumType) { return null; }
        @Override
        public String findEnumValue(Enum<?> enumValue) { return null; }
        @Override
        public boolean hasIgnoreMarker(AnnotatedMember member) { return false; }
        @Override
        public Boolean findIgnoreUnknown(Annotated a) { return null; }
        @Override
        public Boolean findIgnoreUnknown(AnnotatedClass ac) { return null; }
        @Override
        public Object findNullSerializer(Annotated a) { return null; }
        @Override
        public PropertyIgnorals findPropertyIgnorals(Annotated a) { return null; }
        @Override
        public String findPropertyDescription(Annotated a) { return null; }
        @Override
        public String findPropertyDefaultValue(Annotated a) { return null; }
        @Override
        public String findPropertyFormat(Annotated a) { return null; }
        @Override
        public Boolean findPropertyType(Annotated a) { return null; }
        @Override
        public ReferenceProperty findReferenceType(Annotated a) { return null; }
        @Override
        public Boolean findUnwrappingName(Annotated a) { return null; }
        @Override
        public String[] findPropertiesToIgnore(Annotated a) { return null; }
        @Override
        public Boolean findFilterName(Annotated a) { return null; }
        @Override
        public Object findSerializer(Annotated a) { return null; } // Object version
    }

    // ------------------------------------------------------------
    // Helper: create AnnotatedField from a field of a class
    // ------------------------------------------------------------
    private AnnotatedField annotatedField(Class<?> cls, String fieldName) throws Exception {
        Field field = cls.getDeclaredField(fieldName);
        AnnotationMap annMap = new AnnotationMap();
        for (Annotation ann : field.getDeclaredAnnotations()) {
            annMap.add(ann);
        }
        return new AnnotatedField(null, field, annMap);
    }

    // ------------------------------------------------------------
    // Helper: create AnnotatedClass from a class
    // ------------------------------------------------------------
    private AnnotatedClass annotatedClass(Class<?> cls) throws Exception {
        // Use ClassIntrospector to create a basic AnnotatedClass
        // For simplicity, we use a minimal approach
        AnnotationMap classAnn = new AnnotationMap();
        for (Annotation ann : cls.getDeclaredAnnotations()) {
            classAnn.add(ann);
        }
        // We need a TypeFactory and VisibilityChecker; use defaults
        TypeFactory tf = TypeFactory.defaultInstance();
        VisibilityChecker<?> vc = VisibilityChecker.Std.defaultInstance();
        // AnnotatedClass constructor: (TypeFactory, VisibilityChecker, AnnotationMap, Class<?>, boolean)
        return new AnnotatedClass(tf, vc, classAnn, cls, false);
    }

    // ------------------------------------------------------------
    // Tests for default behavior using TestIntrospector
    // ------------------------------------------------------------
    @Test
    public void testFindSerializerReturnsNullByDefault() {
        AnnotationIntrospector ai = new TestIntrospector();
        assertNull(ai.findSerializer((Annotated) null));
        // With a dummy Annotated (we can create a minimal one)
        // Since Annotated is abstract, we use a simple field
        // We'll just test null input for now
    }

    @Test
    public void testFindDeserializerReturnsNullByDefault() {
        AnnotationIntrospector ai = new TestIntrospector();
        assertNull(ai.findDeserializer((Annotated) null));
    }

    @Test
    public void testHasIgnoreMarkerReturnsFalseByDefault() {
        AnnotationIntrospector ai = new TestIntrospector();
        assertFalse(ai.hasIgnoreMarker((AnnotatedMember) null));
    }

    @Test
    public void testFindPropertyIgnoralsReturnsNullByDefault() {
        AnnotationIntrospector ai = new TestIntrospector();
        assertNull(ai.findPropertyIgnorals((Annotated) null));
    }

    @Test
    public void testFindEnumValueReturnsNullByDefault() {
        AnnotationIntrospector ai = new TestIntrospector();
        assertNull(ai.findEnumValue((Class<?>) null));
        assertNull(ai.findEnumValue((Enum<?>) null));
    }

    @Test
    public void testFindIgnoreUnknownReturnsNullByDefault() {
        AnnotationIntrospector ai = new TestIntrospector();
        assertNull(ai.findIgnoreUnknown((Annotated) null));
        assertNull(ai.findIgnoreUnknown((AnnotatedClass) null));
    }

    @Test
    public void testFindPropertiesToIgnoreReturnsNullByDefault() {
        AnnotationIntrospector ai = new TestIntrospector();
        assertNull(ai.findPropertiesToIgnore((Annotated) null));
    }

    @Test
    public void testFindNullSerializerReturnsNullByDefault() {
        AnnotationIntrospector ai = new TestIntrospector();
        assertNull(ai.findNullSerializer((Annotated) null));
    }

    // ------------------------------------------------------------
    // Tests with concrete JacksonAnnotationIntrospector and real annotations
    // ------------------------------------------------------------
    @Test
    public void testJacksonAnnotationIntrospectorFindSerializerWithJsonProperty() throws Exception {
        AnnotationIntrospector ai = new JacksonAnnotationIntrospector();
        AnnotatedField field = annotatedField(BeanWithJsonProperty.class, "name");
        // findSerializer should return null because @JsonProperty does not define a custom serializer
        assertNull(ai.findSerializer(field));
    }

    @Test
    public void testJacksonAnnotationIntrospectorHasIgnoreMarkerWithJsonIgnore() throws Exception {
        AnnotationIntrospector ai = new JacksonAnnotationIntrospector();
        AnnotatedField field = annotatedField(BeanWithJsonIgnore.class, "ignored");
        assertTrue(ai.hasIgnoreMarker(field));
    }

    @Test
    public void testJacksonAnnotationIntrospectorHasIgnoreMarkerWithoutJsonIgnore() throws Exception {
        AnnotationIntrospector ai = new JacksonAnnotationIntrospector();
        AnnotatedField field = annotatedField(BeanWithJsonProperty.class, "name");
        assertFalse(ai.hasIgnoreMarker(field));
    }

    @Test
    public void testJacksonAnnotationIntrospectorFindPropertyIgnoralsWithJsonIgnoreProperties() throws Exception {
        AnnotationIntrospector ai = new JacksonAnnotationIntrospector();
        AnnotatedClass ac = annotatedClass(BeanWithJsonIgnoreProperties.class);
        PropertyIgnorals ignorals = ai.findPropertyIgnorals(ac);
        assertNotNull("findPropertyIgnorals should return non-null for @JsonIgnoreProperties", ignorals);
        // Verify that the ignored property is present
        String[] ignored = ignorals.getIgnored();
        assertNotNull(ignored);
        assertEquals(1, ignored.length);
        assertEquals("ignoredField", ignored[0]);
    }

    @Test
    public void testJacksonAnnotationIntrospectorFindPropertyIgnoralsWithoutAnnotation() throws Exception {
        AnnotationIntrospector ai = new JacksonAnnotationIntrospector();
        AnnotatedClass ac = annotatedClass(BeanWithJsonProperty.class);
        PropertyIgnorals ignorals = ai.findPropertyIgnorals(ac);
        assertNull("findPropertyIgnorals should return null when no @JsonIgnoreProperties", ignorals);
    }

    // ------------------------------------------------------------
    // Edge cases: null inputs should not throw exceptions
    // ------------------------------------------------------------
    @Test
    public void testNullInputsForAllMethods() {
        AnnotationIntrospector ai = new TestIntrospector();
        // These methods accept Annotated or AnnotatedMember; we pass null
        assertNull(ai.findSerializer((Annotated) null));
        assertNull(ai.findKeySerializer((Annotated) null));
        assertNull(ai.findContentSerializer((Annotated) null));
        assertNull(ai.findDeserializer((Annotated) null));
        assertNull(ai.findKeyDeserializer((Annotated) null));
        assertNull(ai.findContentDeserializer((Annotated) null));
        assertNull(ai.findEnumValue((Class<?>) null));
        assertNull(ai.findEnumValue((Enum<?>) null));
        assertFalse(ai.hasIgnoreMarker((AnnotatedMember) null));
        assertNull(ai.findIgnoreUnknown((Annotated) null));
        assertNull(ai.findIgnoreUnknown((AnnotatedClass) null));
        assertNull(ai.findNullSerializer((Annotated) null));
        assertNull(ai.findPropertyIgnorals((Annotated) null));
        assertNull(ai.findPropertyDescription((Annotated) null));
        assertNull(ai.findPropertyDefaultValue((Annotated) null));
        assertNull(ai.findPropertyFormat((Annotated) null));
        assertNull(ai.findPropertyType((Annotated) null));
        assertNull(ai.findReferenceType((Annotated) null));
        assertNull(ai.findUnwrappingName((Annotated) null));
        assertNull(ai.findPropertiesToIgnore((Annotated) null));
        assertNull(ai.findFilterName((Annotated) null));
        assertNull(ai.findSerializer((Annotated) null)); // Object version
    }

    // ------------------------------------------------------------
    // Helper beans for annotation tests
    // ------------------------------------------------------------
    static class BeanWithJsonProperty {
        @JsonProperty("name")
        public String name;
    }

    static class BeanWithJsonIgnore {
        @JsonIgnore
        public String ignored;
        public String notIgnored;
    }

    @JsonIgnoreProperties("ignoredField")
    static class BeanWithJsonIgnoreProperties {
        public String ignoredField;
        public String keptField;
    }
}