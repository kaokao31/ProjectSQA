package com.fasterxml.jackson.databind.introspect;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.AnnotatedParameter;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospector;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.ClassUtil;

import org.junit.Before;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for JacksonAnnotationIntrospector, targeting bug #33 and general coverage.
 */
public class JacksonAnnotationIntrospectorTest {

    private JacksonAnnotationIntrospector introspector;
    private TypeFactory typeFactory;

    @Before
    public void setUp() {
        introspector = new JacksonAnnotationIntrospector();
        typeFactory = TypeFactory.defaultInstance();
    }

    // --- Helper classes with annotations ---

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
    static class TypeInfoOnClass {
        public int value;
    }

    static class TypeInfoOnField {
        @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
        public Object field;
    }

    static class TypeInfoOnMethod {
        @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
        public Object getValue() { return null; }
    }

    @JsonSubTypes({@JsonSubTypes.Type(value = SubType.class, name = "sub")})
    static class BaseType {
        public int base;
    }

    @JsonTypeName("sub")
    static class SubType extends BaseType {
        public int sub;
    }

    @JsonIgnoreProperties({"ignored1", "ignored2"})
    static class IgnoredProperties {
        public int kept;
        public int ignored1;
        public int ignored2;
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
    static class WithIdentity {
        public int id;
    }

    static class WithUnwrapped {
        @JsonUnwrapped
        public Inner inner;
    }

    static class Inner {
        public int x;
    }

    static class WithReference {
        @JsonManagedReference("ref")
        public List<WithReference> children;

        @JsonBackReference("ref")
        public WithReference parent;
    }

    // --- Tests for findTypeInfo (bug #33 related) ---

    @Test
    public void testFindTypeInfoOnClass() {
        AnnotatedClass ac = AnnotatedClass.construct(TypeInfoOnClass.class, introspector, null);
        // findTypeInfo should return non-null for class-level annotation
        assertNotNull("TypeInfo should be found on class", introspector.findTypeInfo(ac));
    }

    @Test
    public void testFindTypeInfoOnField() {
        AnnotatedClass ac = AnnotatedClass.construct(TypeInfoOnField.class, introspector, null);
        AnnotatedField field = ac.fields().next();
        // Bug #33: findTypeInfo on field might return null incorrectly
        assertNotNull("TypeInfo should be found on field", introspector.findTypeInfo(field));
    }

    @Test
    public void testFindTypeInfoOnMethod() {
        AnnotatedClass ac = AnnotatedClass.construct(TypeInfoOnMethod.class, introspector, null);
        AnnotatedMethod method = ac.memberMethods().next();
        assertNotNull("TypeInfo should be found on method", introspector.findTypeInfo(method));
    }

    @Test
    public void testFindTypeInfoOnNull() {
        assertNull("findTypeInfo(null) should return null", introspector.findTypeInfo(null));
    }

    // --- Tests for findSubtypes ---

    @Test
    public void testFindSubtypesOnClass() {
        AnnotatedClass ac = AnnotatedClass.construct(BaseType.class, introspector, null);
        List<JavaType> subtypes = introspector.findSubtypes(ac);
        assertNotNull("Subtypes should be found", subtypes);
        assertFalse("Subtypes list should not be empty", subtypes.isEmpty());
    }

    @Test
    public void testFindSubtypesOnNull() {
        assertNull("findSubtypes(null) should return null", introspector.findSubtypes(null));
    }

    // --- Tests for findPropertiesToIgnore ---

    @Test
    public void testFindPropertiesToIgnore() {
        AnnotatedClass ac = AnnotatedClass.construct(IgnoredProperties.class, introspector, null);
        String[] ignored = introspector.findPropertiesToIgnore(ac, false);
        assertNotNull("Ignored properties should be found", ignored);
        assertTrue("Should contain 'ignored1'", contains(ignored, "ignored1"));
        assertTrue("Should contain 'ignored2'", contains(ignored, "ignored2"));
        assertFalse("Should not contain 'kept'", contains(ignored, "kept"));
    }

    @Test
    public void testFindPropertiesToIgnoreForNull() {
        assertNull("findPropertiesToIgnore(null) should return null",
                introspector.findPropertiesToIgnore(null, false));
    }

    // --- Tests for findObjectIdInfo ---

    @Test
    public void testFindObjectIdInfo() {
        AnnotatedClass ac = AnnotatedClass.construct(WithIdentity.class, introspector, null);
        assertNotNull("ObjectIdInfo should be found", introspector.findObjectIdInfo(ac));
    }

    @Test
    public void testFindObjectIdInfoOnNull() {
        assertNull("findObjectIdInfo(null) should return null", introspector.findObjectIdInfo(null));
    }

    // --- Tests for findUnwrappingName ---

    @Test
    public void testFindUnwrappingName() {
        AnnotatedClass ac = AnnotatedClass.construct(WithUnwrapped.class, introspector, null);
        AnnotatedField field = ac.fields().next();
        String name = introspector.findUnwrappingName(field);
        assertNotNull("Unwrapping name should be found", name);
        // Default prefix/suffix? Actually @JsonUnwrapped without prefix/suffix returns empty string
        assertEquals("Unwrapping name should be empty string", "", name);
    }

    @Test
    public void testFindUnwrappingNameOnNull() {
        assertNull("findUnwrappingName(null) should return null", introspector.findUnwrappingName(null));
    }

    // --- Tests for findReferenceType ---

    @Test
    public void testFindReferenceTypeManaged() {
        AnnotatedClass ac = AnnotatedClass.construct(WithReference.class, introspector, null);
        // Find the 'children' field
        AnnotatedField field = ac.fields().next(); // first field is children (alphabetical order? but we assume)
        // Actually fields order is not guaranteed, so we need to find by name
        for (AnnotatedField f : ac.fields()) {
            if (f.getName().equals("children")) {
                assertEquals("Should be MANAGED_REFERENCE",
                        AnnotationIntrospector.ReferenceProperty.Type.MANAGED_REFERENCE,
                        introspector.findReferenceType(f).getType());
                break;
            }
        }
    }

    @Test
    public void testFindReferenceTypeBack() {
        AnnotatedClass ac = AnnotatedClass.construct(WithReference.class, introspector, null);
        for (AnnotatedField f : ac.fields()) {
            if (f.getName().equals("parent")) {
                assertEquals("Should be BACK_REFERENCE",
                        AnnotationIntrospector.ReferenceProperty.Type.BACK_REFERENCE,
                        introspector.findReferenceType(f).getType());
                break;
            }
        }
    }

    @Test
    public void testFindReferenceTypeOnNull() {
        assertNull("findReferenceType(null) should return null", introspector.findReferenceType(null));
    }

    // --- Tests for findSerializer / findDeserializer ---

    @Test
    public void testFindSerializerOnNull() {
        assertNull("findSerializer(null) should return null", introspector.findSerializer(null));
    }

    @Test
    public void testFindDeserializerOnNull() {
        assertNull("findDeserializer(null) should return null", introspector.findDeserializer(null));
    }

    // --- Tests for hasIgnoreMarker ---

    @Test
    public void testHasIgnoreMarkerOnNull() {
        assertFalse("hasIgnoreMarker(null) should return false", introspector.hasIgnoreMarker(null));
    }

    // --- Tests for findEnumValue ---

    @Test
    public void testFindEnumValueOnNull() {
        assertNull("findEnumValue(null) should return null", introspector.findEnumValue(null));
    }

    // --- Tests for findPropertyIndex ---

    @Test
    public void testFindPropertyIndexOnNull() {
        assertNull("findPropertyIndex(null) should return null", introspector.findPropertyIndex(null));
    }

    // --- Tests for findSerializationType / findDeserializationType ---

    @Test
    public void testFindSerializationTypeOnNull() {
        assertNull("findSerializationType(null) should return null", introspector.findSerializationType(null));
    }

    @Test
    public void testFindDeserializationTypeOnNull() {
        assertNull("findDeserializationType(null) should return null", introspector.findDeserializationType(null));
    }

    // --- Helper ---

    private boolean contains(String[] arr, String value) {
        for (String s : arr) {
            if (value.equals(s)) return true;
        }
        return false;
    }
}