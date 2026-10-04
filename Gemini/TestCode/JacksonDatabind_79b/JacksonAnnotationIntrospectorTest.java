package com.fasterxml.jackson.databind.introspect;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.List;

import static org.junit.Assert.*;

public class JacksonAnnotationIntrospectorTest {

    private JacksonAnnotationIntrospector introspector;
    private ObjectMapper objectMapper;

    @Before
    public void setUp() {
        introspector = new JacksonAnnotationIntrospector();
        objectMapper = new ObjectMapper();
    }

    @Test
    public void testVersion() {
        assertNotNull(introspector.version());
    }

    @Test
    public void testFindRootName() {
        AnnotatedClass ac = AnnotatedClass.construct(RootNamedClass.class, objectMapper.getDeserializationConfig(), null);
        PropertyName name = introspector.findRootName(ac);
        // Depending on whether @JsonRootName is present, we test the code path
        assertNotNull(introspector.findRootName(null));
        assertNull(name);
    }

    @Test
    public void testFindSubTypes() {
        AnnotatedClass ac = AnnotatedClass.construct(BaseClass.class, objectMapper.getDeserializationConfig(), null);
        List<NamedType> subTypes = introspector.findSubTypes(ac);
        assertNull(subTypes); // BaseClass has no @JsonSubTypes directly on it in this example, or tests null handling
        
        AnnotatedClass acWithSub = AnnotatedClass.construct(SubTypedClass.class, objectMapper.getDeserializationConfig(), null);
        List<NamedType> subTypes2 = introspector.findSubTypes(acWithSub);
        assertNotNull(subTypes2);
        assertEquals(1, subTypes2.size());
        assertEquals(SubImpl.class, subTypes2.get(0).getType());
    }

    @Test
    public void testFindTypeName() {
        AnnotatedClass ac = AnnotatedClass.construct(SubImpl.class, objectMapper.getDeserializationConfig(), null);
        String typeName = introspector.findTypeName(ac);
        assertEquals("subImpl", typeName);

        AnnotatedClass acNoName = AnnotatedClass.construct(BaseClass.class, objectMapper.getDeserializationConfig(), null);
        assertNull(introspector.findTypeName(acNoName));
    }

    @Test
    public void testFindObjectIdInfo() {
        AnnotatedClass ac = AnnotatedClass.construct(BaseClass.class, objectMapper.getDeserializationConfig(), null);
        assertNull(introspector.findObjectIdInfo(ac));
        assertNull(introspector.findObjectReferenceInfo(ac, null));
    }

    @Test
    public void testFindSerializationType() {
        AnnotatedField af = getField(ClassWithProperties.class, "stringValue");
        JavaType type = introspector.findSerializationType(af);
        assertNull(type);
    }

    @Test
    public void testFindSerializationKeyType() {
        AnnotatedField af = getField(ClassWithProperties.class, "stringValue");
        JavaType type = introspector.findSerializationKeyType(af, TypeFactory.defaultInstance().constructType(String.class));
        assertNull(type);
    }

    @Test
    public void testFindSerializationContentType() {
        AnnotatedField af = getField(ClassWithProperties.class, "stringValue");
        JavaType type = introspector.findSerializationContentType(af, TypeFactory.defaultInstance().constructType(String.class));
        assertNull(type);
    }

    @Test
    public void testFindSerializationTyping() {
        AnnotatedField af = getField(ClassWithProperties.class, "stringValue");
        assertNull(introspector.findSerializationTyping(af));
    }

    @Test
    public void testFindSerializationConverter() {
        AnnotatedField af = getField(ClassWithProperties.class, "stringValue");
        assertNull(introspector.findSerializationConverter(af));
        assertNull(introspector.findSerializationContentConverter(af));
    }

    @Test
    public void testFindErrorHandlingAndProperties() {
        AnnotatedClass ac = AnnotatedClass.construct(ClassWithProperties.class, objectMapper.getDeserializationConfig(), null);
        assertNull(introspector.findPOJOBuilder(ac));
        assertNull(introspector.findPOJOBuilderConfig(ac));
        assertNull(introspector.findCreatorBinding(ac));
    }

    @Test
    public void testFindPropertyDescription() {
        AnnotatedField af = getField(ClassWithProperties.class, "stringValue");
        assertNull(introspector.findPropertyDescription(af));
    }

    @Test
    public void testFindIntegerIndex() {
        AnnotatedField af = getField(ClassWithProperties.class, "stringValue");
        assertNull(introspector.findIntegerIndex(af));
    }

    @Test
    public void testFindFormat() {
        AnnotatedField af = getField(ClassWithProperties.class, "stringValue");
        assertNotNull(introspector.findFormat(af));
    }

    @Test
    public void testFindReferenceType() {
        AnnotatedField af = getField(ClassWithProperties.class, "stringValue");
        assertNull(introspector.findReferenceType(af));
    }

    @Test
    public void testFindUnwrappingNameTransformer() {
        AnnotatedField af = getField(ClassWithProperties.class, "stringValue");
        assertNull(introspector.findUnwrappingNameTransformer(af));
    }

    @Test
    public void testHasIgnoreMarker() {
        AnnotatedField af = getField(ClassWithProperties.class, "stringValue");
        assertFalse(introspector.hasIgnoreMarker(af));
    }

    @Test
    public void testIsAsynchronous() {
        AnnotatedClass ac = AnnotatedClass.construct(ClassWithProperties.class, objectMapper.getDeserializationConfig(), null);
        assertFalse(introspector.isAnnotationBundle(ac.getAnnotation(JsonProperty.class)));
    }

    @Test
    public void testFindInjectableValueId() {
        AnnotatedField af = getField(ClassWithProperties.class, "stringValue");
        assertNull(introspector.findInjectableValueId(af));
    }

    @Test
    public void testFindAutoDetect() {
        AnnotatedClass ac = AnnotatedClass.construct(ClassWithProperties.class, objectMapper.getDeserializationConfig(), null);
        assertNotNull(introspector.findAutoDetectVisibility(ac, null));
    }

    @Test
    public void testFindNullSerializer() {
        AnnotatedField af = getField(ClassWithProperties.class, "stringValue");
        assertNull(introspector.findNullSerializer(af));
    }

    @Test
    public void testFindContentSerializer() {
        AnnotatedField af = getField(ClassWithProperties.class, "stringValue");
        assertNull(introspector.findContentSerializer(af));
    }

    @Test
    public void testFindKeySerializer() {
        AnnotatedField af = getField(ClassWithProperties.class, "stringValue");
        assertNull(introspector.findKeySerializer(af));
    }

    @Test
    public void testFindSerializer() {
        AnnotatedField af = getField(ClassWithProperties.class, "stringValue");
        assertNull(introspector.findSerializer(af));
    }

    // Helper method to retrieve an AnnotatedField
    private AnnotatedField getField(Class<?> cls, String fieldName) {
        try {
            Field f = cls.getDeclaredField(fieldName);
            AnnotatedClass ac = AnnotatedClass.construct(cls, objectMapper.getDeserializationConfig(), null);
            return new AnnotatedField(ac, f, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Support classes for testing introspector methods
    static class RootNamedClass {
    }

    @JsonSubTypes({
        @JsonSubTypes.Type(value = SubImpl.class, name = "subImpl")
    })
    static class SubTypedClass {
    }

    static class BaseClass {
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
    @com.fasterxml.jackson.annotation.JsonTypeName("subImpl")
    static class SubImpl extends BaseClass {
    }

    static class ClassWithProperties {
        @JsonProperty
        public String stringValue;
    }
}