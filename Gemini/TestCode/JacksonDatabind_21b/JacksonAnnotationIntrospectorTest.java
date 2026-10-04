package com.fasterxml.jackson.databind.introspect;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.util.NameTransformer;
import org.junit.Before;
import org.junit.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.junit.Assert.*;

public class JacksonAnnotationIntrospectorTest {

    private JacksonAnnotationIntrospector introspector;

    @Before
    public void setUp() {
        introspector = new JacksonAnnotationIntrospector();
    }

    // Dummy classes and annotations for testing introspection

    @JsonRootName(value = "customRoot", namespace = "customNs")
    private static class RootNamedClass {}

    @JsonRootName(value = "")
    private static class EmptyRootNamedClass {}

    @JsonIgnoreProperties(value = {"ignoredProp1", "ignoredProp2"}, ignoreUnknown = true, allowSetters = true, allowGetters = false)
    private static class IgnoredPropertiesClass {}

    private static class DummyPropertyClass {
        @JsonProperty(value = "explicitName", required = true, index = 5, namespace = "propNs")
        public String property;

        @JsonSerialize(using = JsonSerializer.None.class)
        public String serializedProp;

        @JsonDeserialize(using = JsonDeserializer.None.class)
        public String deserializedProp;
    }

    @Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    private @interface CustomAnnotation {}

    @CustomAnnotation
    private static class AnnotatedWithCustom {}

    @Test
    public void testFindRootName() {
        // Test class with explicit non-empty root name and namespace
        PropertyName rootName1 = introspector.findRootName(AnnotatedClassUtil.classInfo(RootNamedClass.class));
        assertNotNull(rootName1);
        assertEquals("customRoot", rootName1.getSimpleName());
        assertEquals("customNs", rootName1.getNamespace());

        // Test class with empty root name (should result in empty simple name)
        PropertyName rootName2 = introspector.findRootName(AnnotatedClassUtil.classInfo(EmptyRootNamedClass.class));
        assertNotNull(rootName2);
        assertTrue(rootName2.isEmpty());

        // Test class without JsonRootName
        PropertyName rootName3 = introspector.findRootName(AnnotatedClassUtil.classInfo(Object.class));
        assertNull(rootName3);
        
        // Test null input
        assertNull(introspector.findRootName(null));
    }

    @Test
    public void testFindPropertiesName() throws Exception {
        AnnotatedField field = new AnnotatedField(
                DummyPropertyClass.class.getDeclaredField("property"), null);
        
        PropertyName propName = introspector.findNameForSerialization(field);
        assertNotNull(propName);
        assertEquals("explicitName", propName.getSimpleName());
        assertEquals("propNs", propName.getNamespace());

        PropertyName deserializationName = introspector.findNameForDeserialization(field);
        assertNotNull(deserializationName);
        assertEquals("explicitName", deserializationName.getSimpleName());
    }

    @Test
    public void testFindPropertyIgnorals() {
        // Test class level @JsonIgnoreProperties
        JsonIgnoreProperties.Value ignorals = introspector.findPropertyIgnorals(
                AnnotatedClassUtil.classInfo(IgnoredPropertiesClass.class));
        
        assertNotNull(ignorals);
        assertEquals(2, ignorals.getIgnored().size());
        assertTrue(ignorals.getIgnoreUnknown());
        assertTrue(ignorals.getAllowSetters());
        assertFalse(ignorals.getAllowGetters());

        // Test null/absent
        JsonIgnoreProperties.Value emptyIgnorals = introspector.findPropertyIgnorals(
                AnnotatedClassUtil.classInfo(Object.class));
        assertNull(emptyIgnorals);
    }

    @Test
    public void testIsAnnotation() {
        assertTrue(introspector.isAnnotationBundle(DummyPropertyClass.class.getAnnotation(JsonProperty.class) != null ? 
                DummyPropertyClass.class.getAnnotation(JsonProperty.class).annotationType().getAnnotation(Target.class) : 
                CustomAnnotation.class.getAnnotation(CustomAnnotation.class)));
        
        // General check for standard annotation
        assertTrue(introspector.isAnnotationBundle(CustomAnnotation.class.getAnnotation(Target.class)));
    }

    @Test
    public void testFindSerializationType() throws Exception {
        AnnotatedField field = new AnnotatedField(
                DummyPropertyClass.class.getDeclaredField("serializedProp"), null);
        
        // Should handle basic serialization annotations
        assertNull(introspector.findSerializationType(field));
    }

    @Test
    public void testFindSerializationContainerType() throws Exception {
        AnnotatedField field = new AnnotatedField(
                DummyPropertyClass.class.getDeclaredField("serializedProp"), null);
        assertNull(introspector.findSerializationContainerType(field));
    }

    @Test
    public void testFindDeserializationType() throws Exception {
        AnnotatedField field = new AnnotatedField(
                DummyPropertyClass.class.getDeclaredField("deserializedProp"), null);
        assertNull(introspector.findDeserializationType(field, null));
    }

    @Test
    public void testFindDeserializationKeyType() throws Exception {
        AnnotatedField field = new AnnotatedField(
                DummyPropertyClass.class.getDeclaredField("deserializedProp"), null);
        assertNull(introspector.findKeyDeserializer(field));
    }

    @Test
    public void testFindDeserializationContentType() throws Exception {
        AnnotatedField field = new AnnotatedField(
                DummyPropertyClass.class.getDeclaredField("deserializedProp"), null);
        assertNull(introspector.findContentDeserializer(field));
    }

    @Test
    public void testFindAutoDetect() {
        assertNotNull(introspector.findAutoDetectVisibility(
                AnnotatedClassUtil.classInfo(Object.class), null));
    }

    @Test
    public void testVersion() {
        assertNotNull(introspector.version());
    }

    @Test
    public void testFindNullSerializer() throws Exception {
        AnnotatedField field = new AnnotatedField(
                DummyPropertyClass.class.getDeclaredField("serializedProp"), null);
        assertNull(introspector.findNullSerializer(field));
    }

    @Test
    public void testFindObjectIdInfo() {
        assertNull(introspector.findObjectIdInfo(AnnotatedClassUtil.classInfo(Object.class)));
        assertNull(introspector.findObjectReferenceInfo(AnnotatedClassUtil.classInfo(Object.class), null));
    }

    @Test
    public void testFindTypeResolver() {
        AnnotatedClass ac = AnnotatedClassUtil.classInfo(Object.class);
        assertNull(introspector.findTypeResolver(null, ac, null));
        assertNull(introspector.findPropertyTypeResolver(null, ac, null));
        assertNull(introspector.findPropertyContentTypeResolver(null, ac, null));
    }

    @Test
    public void testFindUnwrappingNameTransformer() throws Exception {
        AnnotatedMember member = new AnnotatedField(
                DummyPropertyClass.class.getDeclaredField("property"), null);
        assertNull(introspector.findUnwrappingNameTransformer(member));
    }

    @Test
    public void testHasIgnoreMarker() throws Exception {
        AnnotatedMember member = new AnnotatedField(
                DummyPropertyClass.class.getDeclaredField("property"), null);
        assertFalse(introspector.hasIgnoreMarker(member));
    }

    @Test
    public void testFindInjectableValueId() throws Exception {
        AnnotatedMember member = new AnnotatedField(
                DummyPropertyClass.class.getDeclaredField("property"), null);
        assertNull(introspector.findInjectableValueId(member));
    }

    @Test
    public void testFindNamingStrategy() {
        assertNull(introspector.findNamingStrategy(AnnotatedClassUtil.classInfo(Object.class)));
    }

    @Test
    public void testFindEnumValues() {
        // Test with non-enum class or class without enum value annotations
        assertNull(introspector.findEnumValues(Object.class, null, null));
    }

    // Helper utility to construct AnnotatedClass instances for tests
    private static class AnnotatedClassUtil {
        public static AnnotatedClass classInfo(Class<?> cls) {
            return AnnotatedClass.constructWithoutSuperTypes(cls, null, null);
        }
    }
}