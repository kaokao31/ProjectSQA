package com.google.javascript.rhino.jstype;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class UnionTypeTest {

    private JSTypeRegistry registry;
    private SimpleErrorReporter errorReporter;

    @Before
    public void setUp() {
        errorReporter = new SimpleErrorReporter();
        registry = new JSTypeRegistry(errorReporter);
    }

    @Test
    public void testUnionTypeCreationAndBasicProperties() {
        JSType numType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
        JSType strType = registry.getNativeType(JSTypeNative.STRING_TYPE);

        UnionType unionType = new UnionType(registry, numType, strType);

        assertTrue(unionType.isUnionType());
        assertFalse(unionType.isNoType());
        assertFalse(unionType.isAllType());

        // Test contains
        assertTrue(unionType.contains(numType));
        assertTrue(unionType.contains(strType));
        assertFalse(unionType.contains(registry.getNativeType(JSTypeNative.BOOLEAN_TYPE)));
    }

    @Test
    public void testUnionTypeAlternates() {
        JSType numType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
        JSType strType = registry.getNativeType(JSTypeNative.STRING_TYPE);
        JSType boolType = registry.getNativeType(JSTypeNative.BOOLEAN_TYPE);

        UnionType unionType = new UnionType(registry, numType, strType);
        
        // Add another type using createUnionType to flatten/build properly
        JSType complexUnion = registry.createUnionType(unionType, boolType);

        assertTrue(complexUnion.isUnionType());
        assertEquals(3, ((UnionType) complexUnion).getAlternates().size());
    }

    @Test
    public void testRestrictByNotNullOrUndefined() {
        JSType numType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
        JSType nullType = registry.getNativeType(JSTypeNative.NULL_TYPE);
        JSType voidType = registry.getNativeType(JSTypeNative.VOID_TYPE);

        UnionType unionType = new UnionType(registry, numType, nullType, voidType);

        JSType restricted = unionType.restrictByNotNullOrUndefined();
        assertEquals(numType, restricted);
    }

    @Test
    public void testIsSubtype() {
        JSType numType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
        JSType strType = registry.getNativeType(JSTypeNative.STRING_TYPE);
        JSType numStrUnion = new UnionType(registry, numType, strType);

        assertTrue(numType.isSubtype(numStrUnion));
        assertTrue(strType.isSubtype(numStrUnion));
        assertTrue(numStrUnion.isSubtype(numStrUnion));

        JSType boolType = registry.getNativeType(JSTypeNative.BOOLEAN_TYPE);
        assertFalse(boolType.isSubtype(numStrUnion));
    }

    @Test
    public void testFindCommonSuperType() {
        JSType numType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
        JSType strType = registry.getNativeType(JSTypeNative.STRING_TYPE);
        UnionType unionType = new UnionType(registry, numType, strType);

        JSType boolType = registry.getNativeType(JSTypeNative.BOOLEAN_TYPE);
        JSType common = unionType.getLeastSupertype(boolType);
        assertNotNull(common);
    }

    @Test
    public void testResolve() {
        JSType numType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
        UnionType unionType = new UnionType(registry, numType);
        
        // Calling resolve should not throw exceptions
        unionType.resolve(errorReporter, registry);
        assertTrue(unionType.isResolved());
    }

    @Test
    public void testToObjectType() {
        JSType numType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
        JSType strType = registry.getNativeType(JSTypeNative.STRING_TYPE);
        UnionType unionType = new UnionType(registry, numType, strType);

        // Union type is generally not an object type directly unless all alternates are objects, 
        // or it handles toObjectType safely.
        JSType objType = unionType.toObjectType();
        // Depending on implementation, it might be null or an object representation
        // Just executing the code path for coverage and potential NPE checks in D4J Bug 104
        assertNull(objType);
    }

    @Test
    public void testGetJSTypeWithUnionAndNull() {
        JSType numType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
        JSType nullType = registry.getNativeType(JSTypeNative.NULL_TYPE);
        
        UnionType unionType = new UnionType(registry, numType, nullType);
        
        // Test behaviors specifically related to how UnionType interacts with null/undefined 
        // which is often the core of Closure Bug 104 (e.g., restrictedByType, meet, etc.)
        JSType restricted = unionType.getRestrictedTypeGivenNonNull();
        assertNotNull(restricted);
    }
}