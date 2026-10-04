package com.fasterxml.jackson.databind.type;

import org.junit.Before;
import org.junit.Test;
import java.util.List;
import java.util.ArrayList;
import static org.junit.Assert.*;

public class ReferenceTypeTest {

    private TypeFactory typeFactory;
    private ClassStack classStack;
    private TypeBindings emptyBindings;

    @Before
    public void setUp() {
        typeFactory = TypeFactory.defaultInstance();
        classStack = new ClassStack(ReferenceType.class);
        emptyBindings = TypeBindings.emptyBindings();
    }

    @Test
    public void testUpgradeFromWithNullSubType() {
        // Create a ReferenceType and then upgrade with null subType
        JavaType refType = typeFactory.constructType(ReferenceType.class);
        JavaType upgraded = ((ReferenceType) refType).upgradeFrom(refType, null);
        assertNull("upgraded type should be null when subType is null", upgraded);
    }

    @Test
    public void testUpgradeFromWithNonNullSubType() {
        JavaType refType = typeFactory.constructType(ReferenceType.class);
        JavaType subType = typeFactory.constructType(String.class);
        JavaType upgraded = ((ReferenceType) refType).upgradeFrom(refType, subType);
        assertNotNull("upgraded type should not be null", upgraded);
        assertTrue("upgraded type should be ReferenceType", upgraded instanceof ReferenceType);
        assertEquals("upgraded type should have correct content type", subType, upgraded.getContentType());
    }

    @Test
    public void testNarrowContentsWithSameType() {
        ReferenceType refType = (ReferenceType) typeFactory.constructSpecializedType(
            typeFactory.constructReferenceType(List.class, emptyBindings, null, null),
            ArrayList.class
        );
        // When narrowing with same type, should return this
        assertSame("should return same instance", refType, refType.narrowContentsBy(refType.getContentType()));
    }

    @Test
    public void testNarrowContentsWithDifferentType() {
        JavaType baseContentType = typeFactory.constructType(CharSequence.class);
        JavaType specializedContentType = typeFactory.constructType(String.class);
        
        // Create a reference type with base content
        ReferenceType refType = (ReferenceType) typeFactory.constructSpecializedType(
            typeFactory.constructReferenceType(List.class, emptyBindings, null, null),
            baseContentType
        );
        
        JavaType narrowed = refType.narrowContentsBy(specializedContentType);
        assertNotNull("narrowed type should not be null", narrowed);
        assertTrue("narrowed type should be ReferenceType", narrowed instanceof ReferenceType);
        assertEquals("narrowed content type should match", specializedContentType, narrowed.getContentType());
    }

    @Test
    public void testToStringWithVariousBindings() {
        // Create ReferenceType with String content type
        JavaType refType = typeFactory.constructReferenceType(ArrayList.class, 
            TypeBindings.create(List.class, typeFactory.constructType(String.class)), 
            null, null);
        
        String toString = refType.toString();
        assertNotNull("toString should not be null", toString);
        assertTrue("toString should contain reference type info", 
            toString.contains("ArrayList") || toString.contains("java.util.ArrayList"));
    }

    @Test
    public void testEqualsAndHashCode() {
        JavaType type1 = typeFactory.constructType(ReferenceType.class);
        JavaType type2 = typeFactory.constructType(ReferenceType.class);
        
        assertEquals("identical types should be equal", type1, type2);
        assertEquals("identical types should have same hash", type1.hashCode(), type2.hashCode());
        
        // Different type should not be equal
        JavaType differentType = typeFactory.constructType(String.class);
        assertNotEquals("different types should not be equal", type1, differentType);
    }

    @Test
    public void testIsReferenceType() {
        JavaType refType = typeFactory.constructType(ReferenceType.class);
        assertTrue("ReferenceType should return true for isReferenceType", refType.isReferenceType());
        
        JavaType nonRefType = typeFactory.constructType(String.class);
        assertFalse("non-reference type should return false", nonRefType.isReferenceType());
    }

    @Test
    public void testGetErasedSignature() {
        ReferenceType refType = (ReferenceType) typeFactory.constructType(ArrayList.class);
        String erasedSignature = refType.getErasedSignature();
        assertNotNull("erased signature should not be null", erasedSignature);
        assertTrue("erased signature should contain type info", !erasedSignature.isEmpty());
    }

    @Test
    public void testGetGenericSignature() {
        // Create a generic ReferenceType with type parameters
        JavaType stringType = typeFactory.constructType(String.class);
        TypeBindings bindings = TypeBindings.create(ArrayList.class, stringType);
        ReferenceType refType = (ReferenceType) typeFactory.constructReferenceType(ArrayList.class, bindings, null, null);
        
        String genericSignature = refType.getGenericSignature(stringType);
        assertNotNull("generic signature should not be null", genericSignature);
        assertTrue("generic signature should contain type parameter info", 
            genericSignature.contains("Ljava/lang/String;") || genericSignature.isEmpty());
    }

    @Test
    public void testWithValueHandler() {
        ReferenceType refType = (ReferenceType) typeFactory.constructType(Integer.class);
        Object valueHandler = new Object();
        JavaType withHandler = refType.withValueHandler(valueHandler);
        assertNotNull("type with value handler should not be null", withHandler);
        assertTrue("should still be ReferenceType", withHandler instanceof ReferenceType);
    }

    @Test
    public void testWithContentValueHandler() {
        ReferenceType refType = (ReferenceType) typeFactory.constructType(List.class);
        Object contentHandler = new Object();
        JavaType withHandler = refType.withContentValueHandler(contentHandler);
        assertNotNull("type with content value handler should not be null", withHandler);
        assertTrue("should still be ReferenceType", withHandler instanceof ReferenceType);
    }

    @Test
    public void testWithTypeHandler() {
        ReferenceType refType = (ReferenceType) typeFactory.constructType(Double.class);
        Object typeHandler = new Object();
        JavaType withHandler = refType.withTypeHandler(typeHandler);
        assertNotNull("type with type handler should not be null", withHandler);
        assertTrue("should still be ReferenceType", withHandler instanceof ReferenceType);
    }

    @Test
    public void testWithContentTypeHandler() {
        ReferenceType refType = (ReferenceType) typeFactory.constructType(Boolean.class);
        Object contentTypeHandler = new Object();
        JavaType withHandler = refType.withContentTypeHandler(contentTypeHandler);
        assertNotNull("type with content type handler should not be null", withHandler);
        assertTrue("should still be ReferenceType", withHandler instanceof ReferenceType);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testWithStaticTypingThrowsException() {
        ReferenceType refType = (ReferenceType) typeFactory.constructType(Long.class);
        refType.withStaticTyping();
    }

    @Test
    public void testForceTypeResolution() {
        ReferenceType refType = (ReferenceType) typeFactory.constructType(Float.class);
        JavaType resolved = refType.forcedTypeResolution();
        // Should not throw and should return a valid type
        assertNotNull("resolved type should not be null", resolved);
    }

    @Test
    public void testUpgradeFromWhenMatchBoundIfSoTrue() {
        // Create a ReferenceType with a specific content type
        JavaType contentType = typeFactory.constructType(String.class);
        ReferenceType refType = (ReferenceType) typeFactory.constructReferenceType(
            List.class, TypeBindings.emptyBindings(), null, null
        );
        
        // Test upgradeFrom with matchBoundIfSo: true (as used in the actual code)
        JavaType upgraded = refType.upgradeFrom(contentType, contentType);
        assertNotNull("upgraded type should not be null", upgraded);
        assertTrue("upgraded type should be ReferenceType", upgraded instanceof ReferenceType);
        
        ReferenceType upgradedRef = (ReferenceType) upgraded;
        assertEquals("content type should match", contentType, upgradedRef.getContentType());
    }

    @Test
    public void testNarrowContentsWithNull() {
        ReferenceType refType = (ReferenceType) typeFactory.constructType(List.class);
        try {
            refType.narrowContentsBy(null);
            fail("should throw IllegalArgumentException for null content type");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testIsAbstractType() {
        // A concrete type should not be abstract
        JavaType concreteType = typeFactory.constructType(String.class);
        assertFalse("String should not be abstract", concreteType.isAbstract());
        
        // An abstract type should be abstract
        JavaType abstractType = typeFactory.constructType(CharSequence.class);
        assertTrue("CharSequence should be abstract", abstractType.isAbstract());
    }

    @Test
    public void testHasContentType() {
        ReferenceType withContent = (ReferenceType) typeFactory.constructReferenceType(
            List.class, 
            TypeBindings.create(List.class, typeFactory.constructType(String.class)),
            null, null
        );
        assertTrue("type with content should have content type", withContent.hasContentType());
        
        // Verify getContentType returns appropriate value
        JavaType contentType = withContent.getContentType();
        assertNotNull("content type should not be null when hasContentType returns true", contentType);
    }

    @Test
    public void testIsContainerType() {
        // Reference types may or may not be container types depending on implementation
        JavaType refType = typeFactory.constructType(ArrayList.class);
        assertTrue("List type should be container type", refType.isContainerType());
    }

    @Test
    public void testInterfacesWithMultipleImplementations() {
        // Create ReferenceType for a generic interface
        JavaType interfaceType = typeFactory.constructType(List.class);
        assertNotNull("interface type should not be null", interfaceType);
        assertTrue("should be interface", interfaceType.isInterface());
        
        // Verify methods like findSuperType work
        JavaType superType = interfaceType.findSuperType(Iterable.class);
        assertNotNull("should find Iterable supertype", superType);
    }
}