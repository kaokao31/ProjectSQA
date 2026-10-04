package com.google.javascript.rhino.jstype;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.ObjectType;
import com.google.javascript.rhino.jstype.PrototypeObjectType;
import org.junit.Before;
import org.junit.Test;

/**
 * Test suite for PrototypeObjectType.
 * Targets high coverage and fault detection (e.g., Defects4J Closure-39).
 */
public class PrototypeObjectTypeTest {

    private JSTypeRegistry registry;
    private PrototypeObjectType baseType;
    private PrototypeObjectType subtype;

    @Before
    public void setUp() {
        registry = new JSTypeRegistry(null);
        baseType = new PrototypeObjectType(
            registry, "BaseType", null,
            registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE));
        subtype = new PrototypeObjectType(
            registry, "SubType", baseType,
            registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE));
    }

    // ==================== Constructor & Basic Properties ====================

    @Test
    public void testConstructorWithNullImplicitPrototype() {
        PrototypeObjectType type = new PrototypeObjectType(
            registry, "Test", null, null);
        assertNotNull(type);
        assertFalse(type.hasProperty("any"));
        assertTrue(type.getOwnPropertyNames().isEmpty());
    }

    @Test
    public void testConstructorWithImplicitPrototype() {
        assertNotNull(baseType);
        assertEquals("BaseType", baseType.getReferenceName());
        assertFalse(baseType.isAnonymous());
    }

    // ==================== hasProperty & hasOwnProperty ====================

    @Test
    public void testHasPropertyOnOwnProperty() {
        subtype.defineDeclaredProperty("x", registry.getNativeType(JSTypeNative.NUMBER_TYPE), null);
        assertTrue(subtype.hasProperty("x"));
        assertTrue(subtype.hasOwnProperty("x"));
        assertFalse(baseType.hasProperty("x"));
        assertFalse(baseType.hasOwnProperty("x"));
    }

    @Test
    public void testHasPropertyOnInheritedProperty() {
        baseType.defineDeclaredProperty("y", registry.getNativeType(JSTypeNative.STRING_TYPE), null);
        assertTrue(subtype.hasProperty("y"));
        assertFalse(subtype.hasOwnProperty("y"));
    }

    @Test
    public void testHasPropertyOnNonExistentProperty() {
        assertFalse(baseType.hasProperty("nonexistent"));
        assertFalse(baseType.hasOwnProperty("nonexistent"));
    }

    @Test
    public void testHasPropertyAfterDeletingProperty() {
        baseType.defineDeclaredProperty("z", registry.getNativeType(JSTypeNative.BOOLEAN_TYPE), null);
        assertTrue(baseType.hasOwnProperty("z"));
        baseType.removeProperty("z");
        assertFalse(baseType.hasOwnProperty("z"));
        assertFalse(baseType.hasProperty("z"));
    }

    // Potential NPE tests for Defects4J Closure-39
    @Test
    public void testHasOwnPropertyOnNullPrototype() {
        PrototypeObjectType noProto = new PrototypeObjectType(
            registry, "NoProto", null, null);
        // Should not throw NPE
        assertFalse(noProto.hasOwnProperty("anything"));
    }

    @Test
    public void testHasPropertyOnNullPropertyName() {
        // According to JSDoc, property name may be null? Defensive check.
        // But likely it is never null. However test for robustness.
        assertFalse(baseType.hasProperty(null));
    }

    // ==================== getPropertyType ====================

    @Test
    public void testGetPropertyTypeOwn() {
        baseType.defineDeclaredProperty("a", registry.getNativeType(JSTypeNative.NUMBER_TYPE), null);
        assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE),
                     baseType.getPropertyType("a"));
    }

    @Test
    public void testGetPropertyTypeInherited() {
        baseType.defineDeclaredProperty("b", registry.getNativeType(JSTypeNative.STRING_TYPE), null);
        assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE),
                     subtype.getPropertyType("b"));
    }

    @Test
    public void testGetPropertyTypeNonExistent() {
        assertNull(baseType.getPropertyType("nonexistent"));
    }

    // ==================== getOwnPropertyNames ====================

    @Test
    public void testGetOwnPropertyNamesEmpty() {
        assertTrue(baseType.getOwnPropertyNames().isEmpty());
    }

    @Test
    public void testGetOwnPropertyNamesWithMultiple() {
        baseType.defineDeclaredProperty("p1", registry.getNativeType(JSTypeNative.NUMBER_TYPE), null);
        baseType.defineDeclaredProperty("p2", registry.getNativeType(JSTypeNative.STRING_TYPE), null);
        assertEquals(2, baseType.getOwnPropertyNames().size());
        assertTrue(baseType.getOwnPropertyNames().contains("p1"));
        assertTrue(baseType.getOwnPropertyNames().contains("p2"));
    }

    // ==================== defineDeclaredProperty / defineInferredProperty ====================

    @Test(expected = NullPointerException.class)
    public void testDefineDeclaredPropertyWithNullType() {
        baseType.defineDeclaredProperty("prop", null, null);
    }

    @Test
    public void testDefineDeclaredPropertyOverride() {
        baseType.defineDeclaredProperty("c", registry.getNativeType(JSTypeNative.NUMBER_TYPE), null);
        assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE),
                     baseType.getPropertyType("c"));
        // Now override with different type
        baseType.defineDeclaredProperty("c", registry.getNativeType(JSTypeNative.STRING_TYPE), null);
        // Override is allowed but type should be updated
        assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE),
                     baseType.getPropertyType("c"));
    }

    @Test
    public void testDefineInferredProperty() {
        baseType.defineInferredProperty("d", registry.getNativeType(JSTypeNative.NUMBER_TYPE), null);
        assertTrue(baseType.hasOwnProperty("d"));
    }

    // ==================== toString ====================

    @Test
    public void testToStringNamed() {
        assertEquals("BaseType", baseType.toString());
    }

    @Test
    public void testToStringAnonymous() {
        PrototypeObjectType anon = new PrototypeObjectType(
            registry, null, null,
            registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE));
        assertTrue(anon.isAnonymous());
        // Anonymous type toString likely "{ ... }" or something
        String str = anon.toString();
        assertNotNull(str);
        assertTrue(str.startsWith("{"));
    }

    // ==================== isSubtype ====================

    @Test
    public void testIsSubtypeOfObject() {
        ObjectType objectType = registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE);
        assertTrue(baseType.isSubtype(objectType));
    }

    @Test
    public void testIsSubtypeOfNativeType() {
        assertTrue(baseType.isSubtype(registry.getNativeType(JSTypeNative.OBJECT_PROTOTYPE)));
    }

    // ==================== equals ====================

    @Test
    public void testEqualsSameInstance() {
        assertTrue(baseType.equals(baseType));
    }

    @Test
    public void testEqualsDifferentInstancesSameName() {
        PrototypeObjectType anotherBase = new PrototypeObjectType(
            registry, "BaseType", null,
            registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE));
        assertTrue(baseType.equals(anotherBase));
    }

    @Test
    public void testEqualsNull() {
        assertFalse(baseType.equals(null));
    }

    @Test
    public void testEqualsDifferentClass() {
        assertFalse(baseType.equals(new Object()));
    }

    // ==================== For potential bug paths ====================

    @Test
    public void testPropertyIterationWithInheritedAndOwn() {
        baseType.defineDeclaredProperty("inheritedProp", registry.getNativeType(JSTypeNative.NUMBER_TYPE), null);
        subtype.defineDeclaredProperty("ownProp", registry.getNativeType(JSTypeNative.BOOLEAN_TYPE), null);
        assertTrue(subtype.hasProperty("inheritedProp"));
        assertTrue(subtype.hasOwnProperty("ownProp"));
        assertEquals(1, subtype.getOwnPropertyNames().size());
        assertEquals("ownProp", subtype.getOwnPropertyNames().iterator().next());
    }

    @Test
    public void testGetPropertyTypeWithInheritedAndOverride() {
        baseType.defineDeclaredProperty("overridden", registry.getNativeType(JSTypeNative.NUMBER_TYPE), null);
        subtype.defineDeclaredProperty("overridden", registry.getNativeType(JSTypeNative.STRING_TYPE), null);
        assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE),
                     subtype.getPropertyType("overridden"));
        // Inherited type should not be visible
        assertFalse(baseType.getPropertyType("overridden").equals(subtype.getPropertyType("overridden")));
    }

    @Test
    public void testRemovePropertyNonExistent() {
        // Should not throw
        baseType.removeProperty("nonexistent");
    }

    @Test
    public void testHasOwnPropertyAfterRemoveOnInherited() {
        baseType.defineDeclaredProperty("removable", registry.getNativeType(JSTypeNative.NUMBER_TYPE), null);
        // Subtype does not have own copy, so remove should fail or do nothing
        subtype.removeProperty("removable");
        // Property should still exist on base
        assertTrue(baseType.hasOwnProperty("removable"));
        assertTrue(subtype.hasProperty("removable"));
    }

    @Test
    public void testDefinePropertyWithNullName() {
        // JSTypeRegistry may throw, but test for defensive behavior if any
        try {
            baseType.defineDeclaredProperty(null, registry.getNativeType(JSTypeNative.NUMBER_TYPE), null);
        } catch (NullPointerException | IllegalArgumentException e) {
            // Expected
        }
    }

    // Additional tests for edge cases
    @Test
    public void testGetOwnPropertyNamesOnAnonymousType() {
        PrototypeObjectType anon = new PrototypeObjectType(
            registry, null, null,
            registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE));
        anon.defineDeclaredProperty("anonProp", registry.getNativeType(JSTypeNative.NUMBER_TYPE), null);
        assertEquals(1, anon.getOwnPropertyNames().size());
        assertTrue(anon.getOwnPropertyNames().contains("anonProp"));
    }

    @Test
    public void testConstructorWithNullReferenceName() {
        PrototypeObjectType noName = new PrototypeObjectType(
            registry, null, null,
            registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE));
        assertTrue(noName.isAnonymous());
        assertNull(noName.getReferenceName());
    }

    @Test
    public void testIsUnknownType() {
        // By default, not unknown
        assertFalse(baseType.isUnknownType());
    }

    @Test
    public void testIsArrayType() {
        assertFalse(baseType.isArrayType());
    }
}