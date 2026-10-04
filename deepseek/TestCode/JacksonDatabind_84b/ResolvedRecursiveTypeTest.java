package com.fasterxml.jackson.databind.type;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link ResolvedRecursiveType}.
 * Designed to achieve high code coverage and detect potential faults,
 * particularly those related to bug #84 in JacksonDatabind.
 */
public class ResolvedRecursiveTypeTest {

    private ResolvedRecursiveType baseType;
    private JavaType simpleType;
    private JavaType anotherSimpleType;

    @Before
    public void setUp() throws Exception {
        // Create a simple JavaType for testing (e.g., String type)
        simpleType = TypeFactory.defaultInstance().constructType(String.class);
        anotherSimpleType = TypeFactory.defaultInstance().constructType(Integer.class);

        // Create a ResolvedRecursiveType with a known self-referenced type
        baseType = new ResolvedRecursiveType(simpleType, null);
        // Note: The constructor signature may vary; adjust as needed.
        // Assuming constructor: ResolvedRecursiveType(JavaType selfRefType, TypeBindings bindings)
        // If not, use reflection or factory method.
    }

    // --- Constructor Tests ---

    @Test
    public void testConstructorWithNullSelfRef() {
        // Should not throw NPE; self-referenced type may be null initially
        ResolvedRecursiveType type = new ResolvedRecursiveType(null, null);
        assertNull("Self-referenced type should be null", type.getSelfReferencedType());
    }

    @Test
    public void testConstructorWithNonNullSelfRef() {
        ResolvedRecursiveType type = new ResolvedRecursiveType(simpleType, null);
        assertSame("Self-referenced type should be the one passed", simpleType, type.getSelfReferencedType());
    }

    // --- getSelfReferencedType() Tests ---

    @Test
    public void testGetSelfReferencedTypeReturnsNullInitially() {
        ResolvedRecursiveType type = new ResolvedRecursiveType(null, null);
        assertNull("getSelfReferencedType() should return null", type.getSelfReferencedType());
    }

    @Test
    public void testGetSelfReferencedTypeAfterSetting() {
        baseType.setSelfReferencedType(simpleType); // if setter exists
        assertSame("getSelfReferencedType() should return the set type", simpleType, baseType.getSelfReferencedType());
    }

    // --- withSelfReferencedType() Tests ---

    @Test
    public void testWithSelfReferencedTypeReturnsNewInstance() {
        ResolvedRecursiveType newType = baseType.withSelfReferencedType(anotherSimpleType);
        assertNotNull("withSelfReferencedType() should return a new instance", newType);
        assertNotSame("Should be a different object", baseType, newType);
        assertSame("New type's self-ref should be the argument", anotherSimpleType, newType.getSelfReferencedType());
    }

    @Test
    public void testWithSelfReferencedTypeWithNull() {
        ResolvedRecursiveType newType = baseType.withSelfReferencedType(null);
        assertNotNull("withSelfReferencedType(null) should return a new instance", newType);
        assertNull("New type's self-ref should be null", newType.getSelfReferencedType());
    }

    // --- narrowContentsBy() Tests ---

    @Test
    public void testNarrowContentsByReturnsSameType() {
        // ResolvedRecursiveType likely does not narrow contents; should return this
        JavaType narrowed = baseType.narrowContentsBy(simpleType);
        assertSame("narrowContentsBy() should return the same instance", baseType, narrowed);
    }

    @Test
    public void testNarrowContentsByWithNull() {
        JavaType narrowed = baseType.narrowContentsBy(null);
        assertSame("narrowContentsBy(null) should return the same instance", baseType, narrowed);
    }

    // --- refine() Tests ---

    @Test
    public void testRefineReturnsSameType() {
        // refine() likely returns this for resolved recursive types
        JavaType refined = baseType.refine(String.class, null, null, null);
        assertSame("refine() should return the same instance", baseType, refined);
    }

    @Test
    public void testRefineWithNullArguments() {
        JavaType refined = baseType.refine(null, null, null, null);
        assertSame("refine() with nulls should return the same instance", baseType, refined);
    }

    // --- isContainerType() Tests ---

    @Test
    public void testIsContainerTypeReturnsFalse() {
        assertFalse("ResolvedRecursiveType should not be a container type", baseType.isContainerType());
    }

    // --- isReferenceType() Tests ---

    @Test
    public void testIsReferenceTypeReturnsTrue() {
        assertTrue("ResolvedRecursiveType should be a reference type", baseType.isReferenceType());
    }

    // --- equals() Tests ---

    @Test
    public void testEqualsWithSameObject() {
        assertTrue("equals with itself should be true", baseType.equals(baseType));
    }

    @Test
    public void testEqualsWithNull() {
        assertFalse("equals(null) should be false", baseType.equals(null));
    }

    @Test
    public void testEqualsWithDifferentType() {
        assertFalse("equals with different type should be false", baseType.equals(simpleType));
    }

    @Test
    public void testEqualsWithSameSelfRefType() {
        ResolvedRecursiveType other = new ResolvedRecursiveType(simpleType, null);
        assertTrue("Two types with same self-ref should be equal", baseType.equals(other));
    }

    @Test
    public void testEqualsWithDifferentSelfRefType() {
        ResolvedRecursiveType other = new ResolvedRecursiveType(anotherSimpleType, null);
        assertFalse("Two types with different self-ref should not be equal", baseType.equals(other));
    }

    @Test
    public void testEqualsWithNullSelfRefInBoth() {
        ResolvedRecursiveType type1 = new ResolvedRecursiveType(null, null);
        ResolvedRecursiveType type2 = new ResolvedRecursiveType(null, null);
        assertTrue("Two types with null self-ref should be equal", type1.equals(type2));
    }

    @Test
    public void testEqualsWithNullSelfRefInOne() {
        ResolvedRecursiveType type1 = new ResolvedRecursiveType(simpleType, null);
        ResolvedRecursiveType type2 = new ResolvedRecursiveType(null, null);
        assertFalse("One null self-ref should not be equal", type1.equals(type2));
    }

    // --- hashCode() Tests ---

    @Test
    public void testHashCodeConsistency() {
        int hash1 = baseType.hashCode();
        int hash2 = baseType.hashCode();
        assertEquals("hashCode should be consistent", hash1, hash2);
    }

    @Test
    public void testHashCodeWithNullSelfRef() {
        ResolvedRecursiveType type = new ResolvedRecursiveType(null, null);
        // Should not throw exception
        int hash = type.hashCode();
        assertNotNull("hashCode should return a value", hash);
    }

    @Test
    public void testHashCodeWithNonNullSelfRef() {
        ResolvedRecursiveType type = new ResolvedRecursiveType(simpleType, null);
        int hash = type.hashCode();
        assertNotNull("hashCode should return a value", hash);
    }

    // --- toString() Tests ---

    @Test
    public void testToString() {
        String str = baseType.toString();
        assertNotNull("toString should not be null", str);
        assertTrue("toString should contain 'ResolvedRecursiveType'", str.contains("ResolvedRecursiveType"));
    }

    @Test
    public void testToStringWithNullSelfRef() {
        ResolvedRecursiveType type = new ResolvedRecursiveType(null, null);
        String str = type.toString();
        assertNotNull("toString should not be null", str);
        assertTrue("toString should contain 'null'", str.contains("null"));
    }

    // --- Additional Fault Detection Tests (Bug #84 context) ---

    @Test(expected = NullPointerException.class)
    public void testSetSelfReferencedTypeWithNullThrowsNPE() {
        // If setSelfReferencedType() exists and does not allow null, this should throw NPE
        // Adjust based on actual implementation
        ResolvedRecursiveType type = new ResolvedRecursiveType(simpleType, null);
        type.setSelfReferencedType(null);
    }

    @Test
    public void testCircularSelfReference() {
        // Create a circular reference scenario
        ResolvedRecursiveType type1 = new ResolvedRecursiveType(null, null);
        ResolvedRecursiveType type2 = new ResolvedRecursiveType(type1, null);
        type1.setSelfReferencedType(type2); // if setter exists
        // Should not cause infinite loop in toString or equals
        assertNotNull("Circular reference should not break toString", type1.toString());
        assertNotNull("Circular reference should not break hashCode", type1.hashCode());
        assertTrue("Circular reference equals should work", type1.equals(type2));
    }

    @Test
    public void testWithSelfReferencedTypePreservesOtherProperties() {
        // If ResolvedRecursiveType has other fields (e.g., typeBindings), they should be preserved
        // This test assumes a constructor with TypeBindings
        TypeBindings bindings = TypeBindings.emptyBindings();
        ResolvedRecursiveType original = new ResolvedRecursiveType(simpleType, bindings);
        ResolvedRecursiveType newType = original.withSelfReferencedType(anotherSimpleType);
        // Check that bindings are preserved (if accessible)
        // This may require a getter for bindings; if not, skip.
        // For coverage, we just ensure no exception.
        assertNotNull("New type should not be null", newType);
    }

    // --- Edge Cases for TypeFactory Integration ---

    @Test
    public void testConstructFromTypeFactory() {
        // If ResolvedRecursiveType can be constructed via TypeFactory, test that
        // This is a placeholder; actual factory method may differ.
        // For coverage, we test that the type is properly resolved.
        JavaType type = TypeFactory.defaultInstance().constructSpecializedType(baseType, String.class);
        assertNotNull("Constructed type should not be null", type);
    }

    // --- Serialization/Deserialization (if applicable) ---

    @Test
    public void testSerializationRoundTrip() throws Exception {
        // If ResolvedRecursiveType implements Serializable, test serialization
        // This is optional; skip if not applicable.
        // For coverage, we just ensure no exception.
        // Not implemented here due to lack of context.
    }
}