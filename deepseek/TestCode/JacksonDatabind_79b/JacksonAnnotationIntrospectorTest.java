package com.fasterxml.jackson.databind.introspect;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectIdInfo;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;

public class JacksonAnnotationIntrospectorTest {

    private JacksonAnnotationIntrospector introspector;
    private ObjectMapper mapper;

    @Before
    public void setUp() {
        introspector = new JacksonAnnotationIntrospector();
        mapper = new ObjectMapper();
    }

    // Helper to get AnnotatedField by name
    private AnnotatedField getField(Class<?> cls, String fieldName) {
        AnnotatedClass ac = mapper.getSerializationConfig().introspectClassAnnotations(cls);
        for (AnnotatedField field : ac.fields()) {
            if (field.getName().equals(fieldName)) {
                return field;
            }
        }
        return null;
    }

    // Helper to get AnnotatedClass
    private AnnotatedClass getAnnotatedClass(Class<?> cls) {
        return mapper.getSerializationConfig().introspectClassAnnotations(cls);
    }

    // Test classes ----------------------------------------------------------
    static class WithJsonProperty {
        @JsonProperty("propName")
        public String value;
    }

    static class WithoutJsonProperty {
        public String value;
    }

    static class WithJsonIgnore {
        @JsonIgnore
        public String value;
    }

    static class WithJsonFilter {
        @JsonFilter("theFilter")
        public String value;
    }

    static class WithJsonIdentityInfo {
        @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
        public String value;
    }

    static class WithJsonManagedReference {
        @JsonManagedReference("parent")
        public String value;
    }

    static class WithJsonBackReference {
        @JsonBackReference("parent")
        public String value;
    }

    static class WithJsonSerializeUsing {
        @JsonSerialize(using = MySerializer.class)
        public String value;
    }

    static class WithJsonDeserializeUsing {
        @JsonDeserialize(using = MyDeserializer.class)
        public String value;
    }

    static class MySerializer extends JsonSerializer<String> {
        @Override
        public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString(value);
        }
    }

    static class MyDeserializer extends JsonDeserializer<String> {
        @Override
        public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return p.getText();
        }
    }

    // -----------------------------------------------------------------------
    // findNameForSerialization / findNameForDeserialization
    // -----------------------------------------------------------------------
    @Test
    public void testFindNameForSerializationWithJsonProperty() {
        AnnotatedField field = getField(WithJsonProperty.class, "value");
        assertEquals("propName", introspector.findNameForSerialization(field));
    }

    @Test
    public void testFindNameForSerializationWithoutJsonProperty() {
        AnnotatedField field = getField(WithoutJsonProperty.class, "value");
        assertNull(introspector.findNameForSerialization(field));
    }

    @Test
    public void testFindNameForDeserializationWithJsonProperty() {
        AnnotatedField field = getField(WithJsonProperty.class, "value");
        assertEquals("propName", introspector.findNameForDeserialization(field));
    }

    @Test
    public void testFindNameForDeserializationWithoutJsonProperty() {
        AnnotatedField field = getField(WithoutJsonProperty.class, "value");
        assertNull(introspector.findNameForDeserialization(field));
    }

    @Test(expected = NullPointerException.class)
    public void testFindNameForSerializationNull() {
        introspector.findNameForSerialization(null);
    }

    @Test(expected = NullPointerException.class)
    public void testFindNameForDeserializationNull() {
        introspector.findNameForDeserialization(null);
    }

    // -----------------------------------------------------------------------
    // hasIgnoreMarker
    // -----------------------------------------------------------------------
    @Test
    public void testHasIgnoreMarkerWithJsonIgnore() {
        AnnotatedField field = getField(WithJsonIgnore.class, "value");
        assertTrue(introspector.hasIgnoreMarker(field));
    }

    @Test
    public void testHasIgnoreMarkerWithoutJsonIgnore() {
        AnnotatedField field = getField(WithoutJsonProperty.class, "value");
        assertFalse(introspector.hasIgnoreMarker(field));
    }

    @Test(expected = NullPointerException.class)
    public void testHasIgnoreMarkerNull() {
        introspector.hasIgnoreMarker(null);
    }

    // -----------------------------------------------------------------------
    // findFilterId
    // -----------------------------------------------------------------------
    @Test
    public void testFindFilterIdWithJsonFilter() {
        AnnotatedClass ac = getAnnotatedClass(WithJsonFilter.class);
        assertEquals("theFilter", introspector.findFilterId(ac));
    }

    @Test
    public void testFindFilterIdWithoutJsonFilter() {
        AnnotatedClass ac = getAnnotatedClass(WithoutJsonProperty.class);
        assertNull(introspector.findFilterId(ac));
    }

    @Test(expected = NullPointerException.class)
    public void testFindFilterIdNull() {
        introspector.findFilterId(null);
    }

    // -----------------------------------------------------------------------
    // findObjectIdInfo
    // -----------------------------------------------------------------------
    @Test
    public void testFindObjectIdInfoWithPropertyGenerator() {
        AnnotatedClass ac = getAnnotatedClass(WithJsonIdentityInfo.class);
        ObjectIdInfo info = introspector.findObjectIdInfo(ac);
        assertNotNull(info);
        assertEquals("id", info.getPropertyName());
        assertEquals(ObjectIdGenerators.PropertyGenerator.class, info.getGeneratorType());
    }

    @Test
    public void testFindObjectIdInfoWithoutJsonIdentityInfo() {
        AnnotatedClass ac = getAnnotatedClass(WithoutJsonProperty.class);
        assertNull(introspector.findObjectIdInfo(ac));
    }

    @Test(expected = NullPointerException.class)
    public void testFindObjectIdInfoNull() {
        introspector.findObjectIdInfo(null);
    }

    // -----------------------------------------------------------------------
    // findReferenceType
    // -----------------------------------------------------------------------
    @Test
    public void testFindReferenceTypeWithManagedReference() {
        AnnotatedField field = getField(WithJsonManagedReference.class, "value");
        ReferenceType ref = introspector.findReferenceType(field);
        assertNotNull(ref);
        assertEquals(ReferenceType.Type.MANAGED_REFERENCE, ref.getType());
        assertEquals("parent", ref.getReferenceName());
    }

    @Test
    public void testFindReferenceTypeWithBackReference() {
        AnnotatedField field = getField(WithJsonBackReference.class, "value");
        ReferenceType ref = introspector.findReferenceType(field);
        assertNotNull(ref);
        assertEquals(ReferenceType.Type.BACK_REFERENCE, ref.getType());
        assertEquals("parent", ref.getReferenceName());
    }

    @Test
    public void testFindReferenceTypeWithoutReference() {
        AnnotatedField field = getField(WithoutJsonProperty.class, "value");
        assertNull(introspector.findReferenceType(field));
    }

    @Test(expected = NullPointerException.class)
    public void testFindReferenceTypeNull() {
        introspector.findReferenceType(null);
    }

    // -----------------------------------------------------------------------
    // findSerializer / findDeserializer
    // -----------------------------------------------------------------------
    @Test
    public void testFindSerializerWithJsonSerializeUsing() {
        AnnotatedField field = getField(WithJsonSerializeUsing.class, "value");
        Object ser = introspector.findSerializer(field);
        assertEquals(MySerializer.class, ser);
    }

    @Test
    public void testFindSerializerWithoutJsonSerializeUsing() {
        AnnotatedField field = getField(WithoutJsonProperty.class, "value");
        assertNull(introspector.findSerializer(field));
    }

    @Test
    public void testFindDeserializerWithJsonDeserializeUsing() {
        AnnotatedField field = getField(WithJsonDeserializeUsing.class, "value");
        Object deser = introspector.findDeserializer(field);
        assertEquals(MyDeserializer.class, deser);
    }

    @Test
    public void testFindDeserializerWithoutJsonDeserializeUsing() {
        AnnotatedField field = getField(WithoutJsonProperty.class, "value");
        assertNull(introspector.findDeserializer(field));
    }

    // -----------------------------------------------------------------------
    // Additional tests for edge cases and coverage
    // -----------------------------------------------------------------------
    static class WithEmptyJsonProperty {
        @JsonProperty("")
        public String value;
    }

    @Test
    public void testFindNameForSerializationWithEmptyJsonProperty() {
        AnnotatedField field = getField(WithEmptyJsonProperty.class, "value");
        assertEquals("", introspector.findNameForSerialization(field));
    }

    @Test
    public void testFindNameForDeserializationWithEmptyJsonProperty() {
        AnnotatedField field = getField(WithEmptyJsonProperty.class, "value");
        assertEquals("", introspector.findNameForDeserialization(field));
    }

    static class WithJsonIgnoreOnGetter {
        @JsonIgnore
        public String getValue() {
            return "ignore";
        }
    }

    @Test
    public void testHasIgnoreMarkerOnMethod() {
        AnnotatedClass ac = getAnnotatedClass(WithJsonIgnoreOnGetter.class);
        for (AnnotatedMethod method : ac.memberMethods()) {
            if (method.getName().equals("getValue")) {
                assertTrue(introspector.hasIgnoreMarker(method));
                return;
            }
        }
        fail("Method not found");
    }
}