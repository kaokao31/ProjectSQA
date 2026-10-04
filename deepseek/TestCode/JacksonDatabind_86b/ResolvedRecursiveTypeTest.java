package com.fasterxml.jackson.databind.type;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class ResolvedRecursiveTypeTest {

    private TypeFactory typeFactory;
    private ResolvedRecursiveType recursiveType;
    private JavaType simpleType;

    @Before
    public void setUp() throws Exception {
        typeFactory = TypeFactory.defaultInstance();
        // Create a simple type for testing
        simpleType = typeFactory.constructType(String.class);
        // Create a ResolvedRecursiveType with a placeholder
        recursiveType = new ResolvedRecursiveType(String.class, TypeBindings.emptyBindings());
    }

    // Test constructor and basic getters
    @Test
    public void testConstructorAndBasicGetters() {
        assertNotNull("ResolvedRecursiveType should not be null", recursiveType);
        assertEquals("Raw type should be String.class", String.class, recursiveType.getRawClass());
        assertTrue("Should be a concrete type", recursiveType.isConcrete());
        assertFalse("Should not be a container type", recursiveType.isContainerType());
        assertNull("Self-referenced type should initially be null", recursiveType.getSelfReferencedType());
    }

    // Test setSelfReferencedType and getSelfReferencedType
    @Test
    public void testSetAndGetSelfReferencedType() {
        JavaType selfRef = typeFactory.constructType(Integer.class);
        recursiveType.setSelfReferencedType(selfRef);
        assertNotNull("Self-referenced type should not be null after set", recursiveType.getSelfReferencedType());
        assertSame("Self-referenced type should be the same object", selfRef, recursiveType.getSelfReferencedType());
    }

    // Test setSelfReferencedType with null (should be allowed)
    @Test
    public void testSetSelfReferencedTypeNull() {
        recursiveType.setSelfReferencedType(null);
        assertNull("Self-referenced type should be null after setting null", recursiveType.getSelfReferencedType());
    }

    // Test withTypeParameters - should return a new ResolvedRecursiveType with updated bindings
    @Test
    public void testWithTypeParameters() {
        JavaType[] typeParams = new JavaType[] { typeFactory.constructType(Integer.class) };
        ResolvedRecursiveType newType = (ResolvedRecursiveType) recursiveType.withTypeParameters(typeParams);
        assertNotNull("withTypeParameters should return a non-null type", newType);
        assertNotSame("Should be a different object", recursiveType, newType);
        // Verify that the new type has the same raw class
        assertEquals("Raw class should be preserved", String.class, newType.getRawClass());
        // Verify that the new type has the same self-referenced type (null initially)
        assertNull("Self-referenced type should be null in new type", newType.getSelfReferencedType());
    }

    // Test withTypeParameters with empty array
    @Test
    public void testWithTypeParametersEmpty() {
        JavaType[] emptyParams = new JavaType[0];
        ResolvedRecursiveType newType = (ResolvedRecursiveType) recursiveType.withTypeParameters(emptyParams);
        assertNotNull("withTypeParameters with empty array should return a non-null type", newType);
        assertEquals("Raw class should be preserved", String.class, newType.getRawClass());
    }

    // Test narrowContentsBy - should return a new ResolvedRecursiveType with narrowed content
    @Test
    public void testNarrowContentsBy() {
        JavaType narrowed = typeFactory.constructType(CharSequence.class);
        ResolvedRecursiveType narrowedType = (ResolvedRecursiveType) recursiveType.narrowContentsBy(narrowed);
        assertNotNull("narrowContentsBy should return a non-null type", narrowedType);
        assertNotSame("Should be a different object", recursiveType, narrowedType);
        // Verify that the new type has the same raw class
        assertEquals("Raw class should be preserved", String.class, narrowedType.getRawClass());
    }

    // Test narrowContentsBy with null (should handle gracefully)
    @Test(expected = IllegalArgumentException.class)
    public void testNarrowContentsByNull() {
        recursiveType.narrowContentsBy(null);
    }

    // Test isContainerType - should always return false for ResolvedRecursiveType
    @Test
    public void testIsContainerType() {
        assertFalse("ResolvedRecursiveType should not be a container type", recursiveType.isContainerType());
    }

    // Test getErasedSignature
    @Test
    public void testGetErasedSignature() {
        String signature = recursiveType.getErasedSignature();
        assertNotNull("Erased signature should not be null", signature);
        // For String, erased signature is "Ljava/lang/String;"
        assertTrue("Erased signature should contain 'String'", signature.contains("String"));
    }

    // Test getGenericSignature
    @Test
    public void testGetGenericSignature() {
        String signature = recursiveType.getGenericSignature();
        assertNotNull("Generic signature should not be null", signature);
        // For a simple ResolvedRecursiveType without type parameters, generic signature equals erased signature
        assertEquals("Generic signature should equal erased signature for simple type",
                recursiveType.getErasedSignature(), signature);
    }

    // Test toString
    @Test
    public void testToString() {
        String str = recursiveType.toString();
        assertNotNull("toString should not be null", str);
        assertTrue("toString should contain 'ResolvedRecursiveType'", str.contains("ResolvedRecursiveType"));
        assertTrue("toString should contain 'String'", str.contains("String"));
    }

    // Test equals and hashCode
    @Test
    public void testEqualsAndHashCode() {
        ResolvedRecursiveType sameType = new ResolvedRecursiveType(String.class, TypeBindings.emptyBindings());
        ResolvedRecursiveType differentType = new ResolvedRecursiveType(Integer.class, TypeBindings.emptyBindings());

        assertTrue("Same type should be equal", recursiveType.equals(sameType));
        assertEquals("Hash codes should be equal for equal objects", recursiveType.hashCode(), sameType.hashCode());

        assertFalse("Different raw class should not be equal", recursiveType.equals(differentType));
        assertFalse("Should not be equal to null", recursiveType.equals(null));
        assertFalse("Should not be equal to a different class", recursiveType.equals("string"));
    }

    // Test equals with self-referenced type set
    @Test
    public void testEqualsWithSelfReferencedType() {
        ResolvedRecursiveType type1 = new ResolvedRecursiveType(String.class, TypeBindings.emptyBindings());
        ResolvedRecursiveType type2 = new ResolvedRecursiveType(String.class, TypeBindings.emptyBindings());
        JavaType selfRef = typeFactory.constructType(Integer.class);
        type1.setSelfReferencedType(selfRef);
        type2.setSelfReferencedType(selfRef);
        assertTrue("Types with same self-referenced type should be equal", type1.equals(type2));
        assertEquals("Hash codes should match", type1.hashCode(), type2.hashCode());

        // Different self-referenced type
        type2.setSelfReferencedType(typeFactory.constructType(Double.class));
        assertFalse("Types with different self-referenced types should not be equal", type1.equals(type2));
    }

    // Test hashCode consistency
    @Test
    public void testHashCodeConsistency() {
        int hash1 = recursiveType.hashCode();
        int hash2 = recursiveType.hashCode();
        assertEquals("Hash code should be consistent", hash1, hash2);
    }

    // Test that setSelfReferencedType can be called multiple times
    @Test
    public void testSetSelfReferencedTypeMultipleTimes() {
        JavaType ref1 = typeFactory.constructType(Integer.class);
        JavaType ref2 = typeFactory.constructType(Double.class);
        recursiveType.setSelfReferencedType(ref1);
        assertSame("First set", ref1, recursiveType.getSelfReferencedType());
        recursiveType.setSelfReferencedType(ref2);
        assertSame("Second set should overwrite", ref2, recursiveType.getSelfReferencedType());
    }

    // Test withTypeParameters preserves self-referenced type
    @Test
    public void testWithTypeParametersPreservesSelfRef() {
        JavaType selfRef = typeFactory.constructType(Integer.class);
        recursiveType.setSelfReferencedType(selfRef);
        JavaType[] typeParams = new JavaType[] { typeFactory.constructType(Long.class) };
        ResolvedRecursiveType newType = (ResolvedRecursiveType) recursiveType.withTypeParameters(typeParams);
        assertNotNull("New type should have self-referenced type", newType.getSelfReferencedType());
        assertSame("Self-referenced type should be preserved", selfRef, newType.getSelfReferencedType());
    }

    // Test narrowContentsBy preserves self-referenced type
    @Test
    public void testNarrowContentsByPreservesSelfRef() {
        JavaType selfRef = typeFactory.constructType(Integer.class);
        recursiveType.setSelfReferencedType(selfRef);
        JavaType narrowed = typeFactory.constructType(CharSequence.class);
        ResolvedRecursiveType narrowedType = (ResolvedRecursiveType) recursiveType.narrowContentsBy(narrowed);
        assertNotNull("Narrowed type should have self-referenced type", narrowedType.getSelfReferencedType());
        assertSame("Self-referenced type should be preserved", selfRef, narrowedType.getSelfReferencedType());
    }

    // Test that getErasedSignature and getGenericSignature work after setting self-referenced type
    @Test
    public void testSignaturesAfterSetSelfRef() {
        JavaType selfRef = typeFactory.constructType(Integer.class);
        recursiveType.setSelfReferencedType(selfRef);
        String erasedSig = recursiveType.getErasedSignature();
        String genericSig = recursiveType.getGenericSignature();
        assertNotNull("Erased signature should not be null", erasedSig);
        assertNotNull("Generic signature should not be null", genericSig);
        // For a simple recursive type, signatures should still be based on raw class
        assertTrue("Erased signature should contain 'String'", erasedSig.contains("String"));
        assertTrue("Generic signature should contain 'String'", genericSig.contains("String"));
    }

    // Test that isConcrete returns true
    @Test
    public void testIsConcrete() {
        assertTrue("ResolvedRecursiveType should be concrete", recursiveType.isConcrete());
    }

    // Test that isAbstract returns false
    @Test
    public void testIsAbstract() {
        assertFalse("ResolvedRecursiveType should not be abstract", recursiveType.isAbstract());
    }

    // Test that isArrayType returns false
    @Test
    public void testIsArrayType() {
        assertFalse("ResolvedRecursiveType should not be an array type", recursiveType.isArrayType());
    }

    // Test that isEnumType returns false
    @Test
    public void testIsEnumType() {
        assertFalse("ResolvedRecursiveType should not be an enum type", recursiveType.isEnumType());
    }

    // Test that isInterface returns false
    @Test
    public void testIsInterface() {
        assertFalse("ResolvedRecursiveType should not be an interface", recursiveType.isInterface());
    }

    // Test that isPrimitive returns false
    @Test
    public void testIsPrimitive() {
        assertFalse("ResolvedRecursiveType should not be primitive", recursiveType.isPrimitive());
    }

    // Test that isFinal returns true for String (final class)
    @Test
    public void testIsFinal() {
        assertTrue("String is final, so ResolvedRecursiveType should be final", recursiveType.isFinal());
    }

    // Test that isThrowable returns false
    @Test
    public void testIsThrowable() {
        assertFalse("ResolvedRecursiveType should not be throwable", recursiveType.isThrowable());
    }

    // Test that isCollectionLikeType returns false
    @Test
    public void testIsCollectionLikeType() {
        assertFalse("ResolvedRecursiveType should not be collection-like", recursiveType.isCollectionLikeType());
    }

    // Test that isMapLikeType returns false
    @Test
    public void testIsMapLikeType() {
        assertFalse("ResolvedRecursiveType should not be map-like", recursiveType.isMapLikeType());
    }

    // Test that getContentType returns null
    @Test
    public void testGetContentType() {
        assertNull("Content type should be null", recursiveType.getContentType());
    }

    // Test that getKeyType returns null
    @Test
    public void testGetKeyType() {
        assertNull("Key type should be null", recursiveType.getKeyType());
    }

    // Test that getBindings returns the bindings passed in constructor
    @Test
    public void testGetBindings() {
        TypeBindings bindings = recursiveType.getBindings();
        assertNotNull("Bindings should not be null", bindings);
        assertEquals("Bindings should be empty", TypeBindings.emptyBindings(), bindings);
    }

    // Test that getGenericSignature returns correct signature when type parameters are present
    @Test
    public void testGetGenericSignatureWithTypeParams() {
        // Create a ResolvedRecursiveType with type parameters (e.g., List<String>)
        // But ResolvedRecursiveType is typically used for recursive types, so we simulate
        // by constructing a type with bindings
        TypeBindings bindings = TypeBindings.create(String.class, typeFactory.constructType(Integer.class));
        ResolvedRecursiveType typeWithParams = new ResolvedRecursiveType(String.class, bindings);
        String genericSig = typeWithParams.getGenericSignature();
        assertNotNull("Generic signature should not be null", genericSig);
        // For String with one type parameter, generic signature should include <Ljava/lang/Integer;>
        assertTrue("Generic signature should contain type parameter", genericSig.contains("Integer"));
    }

    // Test that getErasedSignature does not include type parameters
    @Test
    public void testGetErasedSignatureWithTypeParams() {
        TypeBindings bindings = TypeBindings.create(String.class, typeFactory.constructType(Integer.class));
        ResolvedRecursiveType typeWithParams = new ResolvedRecursiveType(String.class, bindings);
        String erasedSig = typeWithParams.getErasedSignature();
        assertNotNull("Erased signature should not be null", erasedSig);
        // Erased signature should not contain type parameters
        assertFalse("Erased signature should not contain type parameters", erasedSig.contains("Integer"));
    }

    // Test that toString includes type parameters if present
    @Test
    public void testToStringWithTypeParams() {
        TypeBindings bindings = TypeBindings.create(String.class, typeFactory.constructType(Integer.class));
        ResolvedRecursiveType typeWithParams = new ResolvedRecursiveType(String.class, bindings);
        String str = typeWithParams.toString();
        assertTrue("toString should contain type parameters", str.contains("Integer"));
    }

    // Test equals with different bindings
    @Test
    public void testEqualsWithDifferentBindings() {
        TypeBindings bindings1 = TypeBindings.create(String.class, typeFactory.constructType(Integer.class));
        TypeBindings bindings2 = TypeBindings.create(String.class, typeFactory.constructType(Double.class));
        ResolvedRecursiveType type1 = new ResolvedRecursiveType(String.class, bindings1);
        ResolvedRecursiveType type2 = new ResolvedRecursiveType(String.class, bindings2);
        assertFalse("Types with different bindings should not be equal", type1.equals(type2));
    }

    // Test that hashCode differs for different bindings
    @Test
    public void testHashCodeWithDifferentBindings() {
        TypeBindings bindings1 = TypeBindings.create(String.class, typeFactory.constructType(Integer.class));
        TypeBindings bindings2 = TypeBindings.create(String.class, typeFactory.constructType(Double.class));
        ResolvedRecursiveType type1 = new ResolvedRecursiveType(String.class, bindings1);
        ResolvedRecursiveType type2 = new ResolvedRecursiveType(String.class, bindings2);
        assertNotEquals("Hash codes should differ for different bindings", type1.hashCode(), type2.hashCode());
    }

    // Test that setSelfReferencedType with a recursive type (cycle) does not cause infinite loop
    @Test
    public void testSetSelfReferencedTypeRecursiveCycle() {
        ResolvedRecursiveType recursiveType2 = new ResolvedRecursiveType(Integer.class, TypeBindings.emptyBindings());
        // Create a cycle: recursiveType -> recursiveType2 -> recursiveType
        recursiveType.setSelfReferencedType(recursiveType2);
        recursiveType2.setSelfReferencedType(recursiveType);
        // Just ensure no stack overflow and getSelfReferencedType returns the set value
        assertSame("recursiveType's self-ref should be recursiveType2", recursiveType2, recursiveType.getSelfReferencedType());
        assertSame("recursiveType2's self-ref should be recursiveType", recursiveType, recursiveType2.getSelfReferencedType());
    }

    // Test that withTypeParameters returns a new instance with same self-ref even if self-ref is a cycle
    @Test
    public void testWithTypeParametersWithCycle() {
        ResolvedRecursiveType recursiveType2 = new ResolvedRecursiveType(Integer.class, TypeBindings.emptyBindings());
        recursiveType.setSelfReferencedType(recursiveType2);
        recursiveType2.setSelfReferencedType(recursiveType);
        JavaType[] typeParams = new JavaType[] { typeFactory.constructType(Long.class) };
        ResolvedRecursiveType newType = (ResolvedRecursiveType) recursiveType.withTypeParameters(typeParams);
        // The new type should have the same self-ref (recursiveType2) but not the cycle? Actually it should preserve the reference.
        assertSame("New type's self-ref should be recursiveType2", recursiveType2, newType.getSelfReferencedType());
    }

    // Test that narrowContentsBy with a recursive type does not cause issues
    @Test
    public void testNarrowContentsByWithCycle() {
        ResolvedRecursiveType recursiveType2 = new ResolvedRecursiveType(Integer.class, TypeBindings.emptyBindings());
        recursiveType.setSelfReferencedType(recursiveType2);
        recursiveType2.setSelfReferencedType(recursiveType);
        JavaType narrowed = typeFactory.constructType(CharSequence.class);
        ResolvedRecursiveType narrowedType = (ResolvedRecursiveType) recursiveType.narrowContentsBy(narrowed);
        assertSame("Narrowed type's self-ref should be recursiveType2", recursiveType2, narrowedType.getSelfReferencedType());
    }
}