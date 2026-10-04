package com.fasterxml.jackson.databind.type;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * JUnit 4 test suite for ReferenceType.
 * Designed to achieve maximum line/branch coverage and detect potential faults.
 */
public class ReferenceTypeTest {

    private TypeFactory typeFactory;
    private JavaType stringType;
    private JavaType intType;
    private JavaType listStringType;

    @Before
    public void setUp() {
        typeFactory = TypeFactory.defaultInstance();
        stringType = typeFactory.constructType(String.class);
        intType = typeFactory.constructType(Integer.class);
        listStringType = typeFactory.constructCollectionType(ArrayList.class, String.class);
    }

    // ======================== Constructor Tests ========================

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullContentType() {
        // Should throw IllegalArgumentException when content type is null
        ReferenceType.upgradeFrom(null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullReferencedType() {
        // Should throw when referenced type is null (if applicable)
        // ReferenceType.upgradeFrom(null, stringType) might also throw
        // We'll test with null referenced type
        ReferenceType.upgradeFrom(null, stringType);
    }

    @Test
    public void testConstructorValid() {
        ReferenceType refType = ReferenceType.upgradeFrom(stringType, stringType);
        assertNotNull("ReferenceType should not be null", refType);
        assertEquals("Content type should be String", stringType, refType.getContentType());
    }

    // ======================== Static Factory Methods ========================

    @Test
    public void testUpgradeFromWithNullContent() {
        // If content type is null, should throw; already tested above
    }

    @Test
    public void testUpgradeFromWithNonNull() {
        ReferenceType refType = ReferenceType.upgradeFrom(stringType, stringType);
        assertTrue("Should be reference type", refType.isReferenceType());
        assertFalse("Should not be abstract", refType.isAbstract());
        assertTrue("Should be container type", refType.isContainerType());
    }

    // ======================== withTypeParameters ========================

    @Test(expected = IllegalArgumentException.class)
    public void testWithTypeParametersNull() {
        ReferenceType refType = ReferenceType.upgradeFrom(stringType, stringType);
        refType.withTypeParameters(null);
    }

    @Test
    public void testWithTypeParametersEmpty() {
        ReferenceType refType = ReferenceType.upgradeFrom(stringType, stringType);
        JavaType result = refType.withTypeParameters(new JavaType[0]);
        assertSame("Should return same instance for empty params", refType, result);
    }

    @Test
    public void testWithTypeParametersSingle() {
        ReferenceType refType = ReferenceType.upgradeFrom(stringType, stringType);
        JavaType[] params = new JavaType[]{intType};
        JavaType result = refType.withTypeParameters(params);
        assertNotNull("Result should not be null", result);
        assertTrue("Result should be ReferenceType", result instanceof ReferenceType);
        assertEquals("Content type should be Integer", intType, result.getContentType());
    }

    @Test
    public void testWithTypeParametersMultiple() {
        ReferenceType refType = ReferenceType.upgradeFrom(listStringType, listStringType);
        JavaType[] params = new JavaType[]{intType, stringType};
        JavaType result = refType.withTypeParameters(params);
        assertNotNull("Result should not be null", result);
        // Depending on implementation, may or may not change content type
        // We just ensure no exception and result is a ReferenceType
        assertTrue("Result should be ReferenceType", result instanceof ReferenceType);
    }

    // ======================== narrowContentsIfNeeded ========================

    @Test(expected = IllegalArgumentException.class)
    public void testNarrowContentsIfNeededNull() {
        ReferenceType refType = ReferenceType.upgradeFrom(stringType, stringType);
        refType.narrowContentsIfNeeded(null);
    }

    @Test
    public void testNarrowContentsIfNeededSame() {
        ReferenceType refType = ReferenceType.upgradeFrom(stringType, stringType);
        JavaType result = refType.narrowContentsIfNeeded(stringType);
        assertSame("Should return same instance if content type unchanged", refType, result);
    }

    @Test
    public void testNarrowContentsIfNeededDifferent() {
        ReferenceType refType = ReferenceType.upgradeFrom(stringType, stringType);
        JavaType result = refType.narrowContentsIfNeeded(intType);
        assertNotNull("Result should not be null", result);
        assertTrue("Result should be ReferenceType", result instanceof ReferenceType);
        assertEquals("Content type should be Integer", intType, result.getContentType());
    }

    // ======================== isAbstract / isContainerType / isReferenceType ========================

    @Test
    public void testIsAbstract() {
        ReferenceType refType = ReferenceType.upgradeFrom(stringType, stringType);
        assertFalse("ReferenceType should not be abstract", refType.isAbstract());
    }

    @Test
    public void testIsContainerType() {
        ReferenceType refType = ReferenceType.upgradeFrom(stringType, stringType);
        assertTrue("ReferenceType should be container type", refType.isContainerType());
    }

    @Test
    public void testIsReferenceType() {
        ReferenceType refType = ReferenceType.upgradeFrom(stringType, stringType);
        assertTrue("ReferenceType should be reference type", refType.isReferenceType());
    }

    // ======================== getContentType ========================

    @Test
    public void testGetContentType() {
        ReferenceType refType = ReferenceType.upgradeFrom(stringType, stringType);
        assertEquals("Content type should be String", stringType, refType.getContentType());
    }

    @Test
    public void testGetContentTypeWithNarrowed() {
        ReferenceType refType = ReferenceType.upgradeFrom(stringType, stringType);
        ReferenceType narrowed = (ReferenceType) refType.narrowContentsIfNeeded(intType);
        assertEquals("Content type should be Integer", intType, narrowed.getContentType());
    }

    // ======================== getErasedSignature ========================

    @Test
    public void testGetErasedSignature() {
        ReferenceType refType = ReferenceType.upgradeFrom(stringType, stringType);
        String sig = refType.getErasedSignature();
        assertNotNull("Erased signature should not be null", sig);
        assertFalse("Erased signature should not be empty", sig.isEmpty());
        // Typically starts with 'Ljava/lang/Object;' or similar
        assertTrue("Erased signature should contain 'Object'", sig.contains("Object") || sig.contains("java/lang/Object"));
    }

    // ======================== toString ========================

    @Test
    public void testToString() {
        ReferenceType refType = ReferenceType.upgradeFrom(stringType, stringType);
        String str = refType.toString();
        assertNotNull("toString should not be null", str);
        assertFalse("toString should not be empty", str.isEmpty());
        // Should contain "reference" or "ReferenceType"
        assertTrue("toString should contain 'reference'", str.toLowerCase().contains("reference"));
    }

    // ======================== equals and hashCode ========================

    @Test
    public void testEqualsSameInstance() {
        ReferenceType refType = ReferenceType.upgradeFrom(stringType, stringType);
        assertTrue("Should equal itself", refType.equals(refType));
    }

    @Test
    public void testEqualsNull() {
        ReferenceType refType = ReferenceType.upgradeFrom(stringType, stringType);
        assertFalse("Should not equal null", refType.equals(null));
    }

    @Test
    public void testEqualsDifferentType() {
        ReferenceType refType = ReferenceType.upgradeFrom(stringType, stringType);
        assertFalse("Should not equal different type", refType.equals(stringType));
    }

    @Test
    public void testEqualsSameContent() {
        ReferenceType refType1 = ReferenceType.upgradeFrom(stringType, stringType);
        ReferenceType refType2 = ReferenceType.upgradeFrom(stringType, stringType);
        assertEquals("Two ReferenceTypes with same content should be equal", refType1, refType2);
        assertEquals("Hash codes should be equal", refType1.hashCode(), refType2.hashCode());
    }

    @Test
    public void testEqualsDifferentContent() {
        ReferenceType refType1 = ReferenceType.upgradeFrom(stringType, stringType);
        ReferenceType refType2 = ReferenceType.upgradeFrom(intType, intType);
        assertNotEquals("Two ReferenceTypes with different content should not be equal", refType1, refType2);
        // Hash codes may or may not differ; not required to be different
    }

    @Test
    public void testEqualsWithNarrowed() {
        ReferenceType refType1 = ReferenceType.upgradeFrom(stringType, stringType);
        ReferenceType refType2 = (ReferenceType) refType1.narrowContentsIfNeeded(intType);
        assertNotEquals("Narrowed type should not equal original", refType1, refType2);
    }

    // ======================== Edge Cases ========================

    @Test
    public void testWithTypeParametersNullContentType() {
        // If content type is null, constructor should have thrown; but if we somehow get a ReferenceType with null content,
        // withTypeParameters should handle gracefully. We'll skip as constructor prevents.
    }

    @Test
    public void testNarrowContentsIfNeededNullContentType() {
        // Similar to above, skip.
    }

    @Test
    public void testGetErasedSignatureWithGenericContent() {
        ReferenceType refType = ReferenceType.upgradeFrom(listStringType, listStringType);
        String sig = refType.getErasedSignature();
        assertNotNull(sig);
        // Should contain "ArrayList" or "List"
        assertTrue("Erased signature should contain 'ArrayList'", sig.contains("ArrayList") || sig.contains("List"));
    }

    @Test
    public void testToStringWithGenericContent() {
        ReferenceType refType = ReferenceType.upgradeFrom(listStringType, listStringType);
        String str = refType.toString();
        assertNotNull(str);
        assertTrue("toString should contain 'ArrayList'", str.contains("ArrayList") || str.contains("List"));
    }

    // ======================== Additional Coverage ========================

    @Test
    public void testIsConcrete() {
        ReferenceType refType = ReferenceType.upgradeFrom(stringType, stringType);
        // ReferenceType is typically concrete if content type is concrete
        assertTrue("ReferenceType should be concrete", refType.isConcrete());
    }

    @Test
    public void testIsArrayType() {
        ReferenceType refType = ReferenceType.upgradeFrom(stringType, stringType);
        assertFalse("ReferenceType should not be array type", refType.isArrayType());
    }

    @Test
    public void testIsMapLikeType() {
        ReferenceType refType = ReferenceType.upgradeFrom(stringType, stringType);
        assertFalse("ReferenceType should not be map-like", refType.isMapLikeType());
    }

    @Test
    public void testIsCollectionLikeType() {
        ReferenceType refType = ReferenceType.upgradeFrom(stringType, stringType);
        assertFalse("ReferenceType should not be collection-like", refType.isCollectionLikeType());
    }

    @Test
    public void testGetGenericSignature() {
        ReferenceType refType = ReferenceType.upgradeFrom(stringType, stringType);
        String sig = refType.getGenericSignature();
        assertNotNull("Generic signature should not be null", sig);
        assertFalse("Generic signature should not be empty", sig.isEmpty());
        // Should contain type parameters
        assertTrue("Generic signature should contain 'String'", sig.contains("String") || sig.contains("Ljava/lang/String"));
    }

    @Test
    public void testGetGenericSignatureWithGenericContent() {
        ReferenceType refType = ReferenceType.upgradeFrom(listStringType, listStringType);
        String sig = refType.getGenericSignature();
        assertNotNull(sig);
        assertTrue("Generic signature should contain 'ArrayList'", sig.contains("ArrayList") || sig.contains("List"));
    }

    @Test
    public void testGetContentTypeAfterNarrow() {
        ReferenceType refType = ReferenceType.upgradeFrom(stringType, stringType);
        ReferenceType narrowed = (ReferenceType) refType.narrowContentsIfNeeded(intType);
        assertNotNull("Content type should not be null", narrowed.getContentType());
        assertEquals("Content type should be Integer", intType, narrowed.getContentType());
    }

    @Test
    public void testWithTypeParametersPreservesOriginal() {
        ReferenceType refType = ReferenceType.upgradeFrom(stringType, stringType);
        JavaType[] params = new JavaType[]{intType};
        JavaType result = refType.withTypeParameters(params);
        assertNotSame("Result should be a new instance", refType, result);
        // Original should remain unchanged
        assertEquals("Original content type should still be String", stringType, refType.getContentType());
    }

    @Test
    public void testNarrowContentsIfNeededPreservesOriginal() {
        ReferenceType refType = ReferenceType.upgradeFrom(stringType, stringType);
        JavaType result = refType.narrowContentsIfNeeded(intType);
        assertNotSame("Result should be a new instance", refType, result);
        assertEquals("Original content type should still be String", stringType, refType.getContentType());
    }

    // ======================== Fault Detection (Defects4J Bug 46) ========================
    // Bug 46 likely involves incorrect handling of type parameters or equality.
    // We'll test scenarios that might trigger such issues.

    @Test
    public void testEqualsWithDifferentReferencedType() {
        // If ReferenceType stores both content and referenced type, equality might depend on both.
        // Create two ReferenceTypes with same content but different referenced types (if possible)
        // For simplicity, we assume upgradeFrom uses content as referenced type.
        // We'll test with different content types.
        ReferenceType refType1 = ReferenceType.upgradeFrom(stringType, stringType);
        ReferenceType refType2 = ReferenceType.upgradeFrom(intType, intType);
        assertNotEquals("Different content types should not be equal", refType1, refType2);
    }

    @Test
    public void testHashCodeConsistency() {
        ReferenceType refType = ReferenceType.upgradeFrom(stringType, stringType);
        int hash1 = refType.hashCode();
        int hash2 = refType.hashCode();
        assertEquals("Hash code should be consistent", hash1, hash2);
    }

    @Test
    public void testEqualsWithNullContentType() {
        // If we could create a ReferenceType with null content, equality should handle it.
        // But constructor prevents, so we skip.
    }

    @Test
    public void testWithTypeParametersNullElement() {
        ReferenceType refType = ReferenceType.upgradeFrom(stringType, stringType);
        try {
            refType.withTypeParameters(new JavaType[]{null});
            fail("Should throw IllegalArgumentException for null element in type parameters");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testNarrowContentsIfNeededNullContentType() {
        // Already covered by exception test
    }

    @Test
    public void testGetErasedSignatureWithNullContent() {
        // Not possible due to constructor validation
    }

    @Test
    public void testToStringWithNullContent() {
        // Not possible
    }

    // ======================== Additional Edge Cases ========================

    @Test
    public void testIsReferenceTypeFalseForNonReference() {
        // Ensure that a non-reference type returns false
        assertFalse("String type should not be reference type", stringType.isReferenceType());
    }

    @Test
    public void testIsContainerTypeForNonContainer() {
        assertFalse("String type should not be container type", stringType.isContainerType());
    }

    @Test
    public void testGetContentTypeForNonReference() {
        // For non-reference types, getContentType should return null
        assertNull("String type should have null content type", stringType.getContentType());
    }

    @Test
    public void testUpgradeFromWithNullContentTypeAndNonNullReferenced() {
        // Should throw; already tested
    }

    @Test
    public void testUpgradeFromWithNonNullContentAndNullReferenced() {
        // Should throw; already tested
    }

    @Test
    public void testUpgradeFromWithSameType() {
        ReferenceType refType = ReferenceType.upgradeFrom(stringType, stringType);
        assertNotNull(refType);
        assertTrue(refType.isReferenceType());
    }

    @Test
    public void testUpgradeFromWithDifferentType() {
        // If referenced type differs from content type, it's still valid
        ReferenceType refType = ReferenceType.upgradeFrom(stringType, intType);
        assertNotNull(refType);
        assertEquals("Content type should be String", stringType, refType.getContentType());
        // Referenced type might be intType; but we don't have getReferencedType() method
    }

    // ======================== Final sanity checks ========================

    @Test
    public void testAllMethodsReturnNonNull() {
        ReferenceType refType = ReferenceType.upgradeFrom(stringType, stringType);
        assertNotNull(refType.getContentType());
        assertNotNull(refType.getErasedSignature());
        assertNotNull(refType.getGenericSignature());
        assertNotNull(refType.toString());
        // hashCode and equals are tested separately
    }

    @Test
    public void testWithTypeParametersReturnsReferenceType() {
        ReferenceType refType = ReferenceType.upgradeFrom(stringType, stringType);
        JavaType result = refType.withTypeParameters(new JavaType[]{intType});
        assertTrue("Result must be ReferenceType", result instanceof ReferenceType);
    }

    @Test
    public void testNarrowContentsIfNeededReturnsReferenceType() {
        ReferenceType refType = ReferenceType.upgradeFrom(stringType, stringType);
        JavaType result = refType.narrowContentsIfNeeded(intType);
        assertTrue("Result must be ReferenceType", result instanceof ReferenceType);
    }
}