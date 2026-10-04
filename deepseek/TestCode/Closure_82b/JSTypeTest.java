package com.google.javascript.rhino.jstype;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class JSTypeTest {

    private JSType noType;
    private JSType unknownType;
    private JSType allType;
    private JSType voidType;
    private JSType nullType;

    @Before
    public void setUp() {
        noType = NoType.NO_TYPE;
        unknownType = UnknownType.UNKNOWN;
        allType = AllType.ALL;
        voidType = VoidType.VOID;
        nullType = NullType.NULL;
    }

    @Test
    public void testIsSubtypeWithNull() {
        // Bug 82: isSubtype should handle null argument without NPE
        assertFalse(noType.isSubtype(null));
        assertFalse(unknownType.isSubtype(null));
        assertFalse(allType.isSubtype(null));
        assertFalse(voidType.isSubtype(null));
        assertFalse(nullType.isSubtype(null));
    }

    @Test
    public void testIsSubtypeOfWithNull() {
        assertFalse(noType.isSubtypeOf(null));
        assertFalse(unknownType.isSubtypeOf(null));
        assertFalse(allType.isSubtypeOf(null));
        assertFalse(voidType.isSubtypeOf(null));
        assertFalse(nullType.isSubtypeOf(null));
    }

    @Test
    public void testIsEquivalentToWithNull() {
        assertFalse(noType.isEquivalentTo(null));
        assertFalse(unknownType.isEquivalentTo(null));
        assertFalse(allType.isEquivalentTo(null));
        assertFalse(voidType.isEquivalentTo(null));
        assertFalse(nullType.isEquivalentTo(null));
    }

    @Test
    public void testRestrictByNotNullOrUndefined() {
        assertNotNull(noType.restrictByNotNullOrUndefined());
        assertNotNull(unknownType.restrictByNotNullOrUndefined());
        assertNotNull(allType.restrictByNotNullOrUndefined());
        assertNotNull(voidType.restrictByNotNullOrUndefined());
        assertNotNull(nullType.restrictByNotNullOrUndefined());
    }

    @Test
    public void testIsNoType() {
        assertTrue(noType.isNoType());
        assertFalse(unknownType.isNoType());
        assertFalse(allType.isNoType());
        assertFalse(voidType.isNoType());
        assertFalse(nullType.isNoType());
    }

    @Test
    public void testIsUnknownType() {
        assertTrue(unknownType.isUnknownType());
        assertFalse(noType.isUnknownType());
        assertFalse(allType.isUnknownType());
        assertFalse(voidType.isUnknownType());
        assertFalse(nullType.isUnknownType());
    }

    @Test
    public void testIsAllType() {
        assertTrue(allType.isAllType());
        assertFalse(noType.isAllType());
        assertFalse(unknownType.isAllType());
        assertFalse(voidType.isAllType());
        assertFalse(nullType.isAllType());
    }

    @Test
    public void testIsVoidType() {
        assertTrue(voidType.isVoidType());
        assertFalse(noType.isVoidType());
        assertFalse(unknownType.isVoidType());
        assertFalse(allType.isVoidType());
        assertFalse(nullType.isVoidType());
    }

    @Test
    public void testIsNullType() {
        assertTrue(nullType.isNullType());
        assertFalse(noType.isNullType());
        assertFalse(unknownType.isNullType());
        assertFalse(allType.isNullType());
        assertFalse(voidType.isNullType());
    }

    @Test
    public void testIsSubtypeOfItself() {
        assertTrue(noType.isSubtypeOf(noType));
        assertTrue(unknownType.isSubtypeOf(unknownType));
        assertTrue(allType.isSubtypeOf(allType));
        assertTrue(voidType.isSubtypeOf(voidType));
        assertTrue(nullType.isSubtypeOf(nullType));
    }

    @Test
    public void testIsSubtypeOfAllType() {
        assertTrue(noType.isSubtypeOf(allType));
        assertTrue(unknownType.isSubtypeOf(allType));
        assertTrue(voidType.isSubtypeOf(allType));
        assertTrue(nullType.isSubtypeOf(allType));
    }

    @Test
    public void testIsSubtypeOfUnknownType() {
        assertTrue(noType.isSubtypeOf(unknownType));
        assertTrue(allType.isSubtypeOf(unknownType));
        assertTrue(voidType.isSubtypeOf(unknownType));
        assertTrue(nullType.isSubtypeOf(unknownType));
    }

    @Test
    public void testIsSubtypeOfNoType() {
        assertTrue(noType.isSubtypeOf(noType));
        assertFalse(unknownType.isSubtypeOf(noType));
        assertFalse(allType.isSubtypeOf(noType));
        assertFalse(voidType.isSubtypeOf(noType));
        assertFalse(nullType.isSubtypeOf(noType));
    }

    @Test
    public void testIsSubtypeOfVoidType() {
        assertTrue(voidType.isSubtypeOf(voidType));
        assertFalse(noType.isSubtypeOf(voidType));
        assertFalse(unknownType.isSubtypeOf(voidType));
        assertFalse(allType.isSubtypeOf(voidType));
        assertFalse(nullType.isSubtypeOf(voidType));
    }

    @Test
    public void testIsSubtypeOfNullType() {
        assertTrue(nullType.isSubtypeOf(nullType));
        assertFalse(noType.isSubtypeOf(nullType));
        assertFalse(unknownType.isSubtypeOf(nullType));
        assertFalse(allType.isSubtypeOf(nullType));
        assertFalse(voidType.isSubtypeOf(nullType));
    }

    @Test
    public void testIsEquivalentToItself() {
        assertTrue(noType.isEquivalentTo(noType));
        assertTrue(unknownType.isEquivalentTo(unknownType));
        assertTrue(allType.isEquivalentTo(allType));
        assertTrue(voidType.isEquivalentTo(voidType));
        assertTrue(nullType.isEquivalentTo(nullType));
    }

    @Test
    public void testIsEquivalentToDifferentTypes() {
        assertFalse(noType.isEquivalentTo(unknownType));
        assertFalse(noType.isEquivalentTo(allType));
        assertFalse(noType.isEquivalentTo(voidType));
        assertFalse(noType.isEquivalentTo(nullType));
        assertFalse(unknownType.isEquivalentTo(allType));
        assertFalse(unknownType.isEquivalentTo(voidType));
        assertFalse(unknownType.isEquivalentTo(nullType));
        assertFalse(allType.isEquivalentTo(voidType));
        assertFalse(allType.isEquivalentTo(nullType));
        assertFalse(voidType.isEquivalentTo(nullType));
    }

    @Test
    public void testHashCodeNotNull() {
        assertNotNull(noType.hashCode());
        assertNotNull(unknownType.hashCode());
        assertNotNull(allType.hashCode());
        assertNotNull(voidType.hashCode());
        assertNotNull(nullType.hashCode());
    }

    @Test
    public void testToStringNotNull() {
        assertNotNull(noType.toString());
        assertNotNull(unknownType.toString());
        assertNotNull(allType.toString());
        assertNotNull(voidType.toString());
        assertNotNull(nullType.toString());
    }
}