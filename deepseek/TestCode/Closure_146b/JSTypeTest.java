package com.google.javascript.rhino.jstype;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class JSTypeTest {

    private JSTypeRegistry registry;
    private JSType numberType;
    private JSType stringType;
    private JSType booleanType;
    private JSType unknownType;

    @Before
    public void setUp() {
        registry = new JSTypeRegistry();
        numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
        stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
        booleanType = registry.getNativeType(JSTypeNative.BOOLEAN_TYPE);
        unknownType = registry.getNativeType(JSTypeNative.UNKNOWN_TYPE);
    }

    @Test
    public void testIsSubtypeWithNull() {
        // Should not throw NPE and must return false
        try {
            assertFalse(numberType.isSubtype(null));
            assertFalse(stringType.isSubtype(null));
            assertFalse(booleanType.isSubtype(null));
        } catch (NullPointerException e) {
            fail("isSubtype(null) should not throw NullPointerException");
        }
    }

    @Test
    public void testIsSubtypeWithDifferentTypes() {
        assertFalse(numberType.isSubtype(stringType));
        assertFalse(stringType.isSubtype(numberType));
        assertFalse(booleanType.isSubtype(numberType));
    }

    @Test
    public void testIsSubtypeWithSameType() {
        assertTrue(numberType.isSubtype(numberType));
        assertTrue(stringType.isSubtype(stringType));
    }

    @Test
    public void testIsSubtypeWithUnknownType() {
        assertTrue(numberType.isSubtype(unknownType));
        assertTrue(stringType.isSubtype(unknownType));
    }

    @Test
    public void testIsEquivalentToWithNull() {
        // Should not throw NPE and must return false
        try {
            assertFalse(numberType.isEquivalentTo(null));
            assertFalse(stringType.isEquivalentTo(null));
        } catch (NullPointerException e) {
            fail("isEquivalentTo(null) should not throw NullPointerException");
        }
    }

    @Test
    public void testIsEquivalentToSameType() {
        assertTrue(numberType.isEquivalentTo(numberType));
        assertTrue(stringType.isEquivalentTo(stringType));
    }

    @Test
    public void testIsEquivalentToDifferentTypes() {
        assertFalse(numberType.isEquivalentTo(stringType));
        assertFalse(stringType.isEquivalentTo(booleanType));
    }

    @Test
    public void testIsEquivalentToUnknownType() {
        assertTrue(numberType.isEquivalentTo(unknownType));
        assertTrue(unknownType.isEquivalentTo(numberType));
    }

    @Test
    public void testIsObject() {
        JSType objectType = registry.getNativeType(JSTypeNative.OBJECT_TYPE);
        assertTrue(objectType.isObject());
        assertFalse(numberType.isObject());
        assertFalse(booleanType.isObject());
    }

    @Test
    public void testIsFunction() {
        JSType functionType = registry.getNativeType(JSTypeNative.FUNCTION_FUNCTION_TYPE);
        assertTrue(functionType.isFunction());
        assertFalse(numberType.isFunction());
    }

    @Test
    public void testIsArrayType() {
        JSType arrayType = registry.getNativeType(JSTypeNative.ARRAY_TYPE);
        assertTrue(arrayType.isArrayType());
        assertFalse(numberType.isArrayType());
    }

    @Test
    public void testIsRecordType() {
        assertFalse(numberType.isRecordType());
        assertFalse(stringType.isRecordType());
    }

    @Test
    public void testToString() {
        assertNotNull(numberType.toString());
        assertNotNull(unknownType.toString());
    }

    @Test
    public void testIsNoType() {
        JSType noType = registry.getNativeType(JSTypeNative.NO_TYPE);
        assertTrue(noType.isNoType());
        assertFalse(numberType.isNoType());
    }

    @Test
    public void testIsUnknownType() {
        assertTrue(unknownType.isUnknownType());
        assertFalse(numberType.isUnknownType());
    }

    @Test
    public void testIsSubtypeWithNullArguments() {
        // Additional null-safety checks
        try {
            registry.getNativeType(JSTypeNative.NULL_TYPE).isSubtype(null);
            registry.getNativeType(JSTypeNative.VOID_TYPE).isSubtype(null);
        } catch (NullPointerException e) {
            fail("isSubtype(null) on NULL_TYPE or VOID_TYPE should not throw");
        }
    }

    @Test
    public void testIsEquivalentToNullType() {
        JSType nullType = registry.getNativeType(JSTypeNative.NULL_TYPE);
        assertFalse(nullType.isEquivalentTo(null));
    }
}