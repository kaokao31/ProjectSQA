package com.google.javascript.rhino.jstype;

import org.junit.Test;
import static org.junit.Assert.*;

import com.google.javascript.rhino.ErrorReporter;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.ObjectType;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.NoType;
import com.google.javascript.rhino.jstype.NullType;
import com.google.javascript.rhino.jstype.VoidType;
import com.google.javascript.rhino.jstype.UnknownType;
import com.google.javascript.rhino.jstype.NamedType;
import com.google.javascript.rhino.jstype.UnionType;
import com.google.javascript.rhino.jstype.ParameterizedType;
import com.google.javascript.rhino.SimpleErrorReporter;

import java.util.Collections;

public class JSTypeTest {

    @Test
    public void testIsSubtypeBasicChecks() {
        ErrorReporter errorReporter = new SimpleErrorReporter();
        JSTypeRegistry registry = new JSTypeRegistry(errorReporter);

        JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
        JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
        JSType anyType = registry.getNativeType(JSTypeNative.ALL_TYPE);
        JSType unknownType = registry.getNativeType(JSTypeNative.UNKNOWN_TYPE);
        JSType noType = registry.getNativeType(JSTypeNative.NO_TYPE);
        JSType voidType = registry.getNativeType(JSTypeNative.VOID_TYPE);
        JSType nullType = registry.getNativeType(JSTypeNative.NULL_TYPE);

        // Reflexivity
        assertTrue(numberType.isSubtype(numberType));
        assertTrue(anyType.isSubtype(anyType));

        // NO_TYPE is a subtype of everything
        assertTrue(noType.isSubtype(numberType));
        assertTrue(noType.isSubtype(stringType));
        assertTrue(noType.isSubtype(anyType));

        // Everything is a subtype of ALL_TYPE
        assertTrue(numberType.isSubtype(anyType));
        assertTrue(stringType.isSubtype(anyType));
        assertTrue(nullType.isSubtype(anyType));
        assertTrue(voidType.isSubtype(anyType));

        // UNKNOWN_TYPE interactions
        assertTrue(unknownType.isSubtype(numberType));
        assertTrue(numberType.isSubtype(unknownType));
        assertTrue(unknownType.isSubtype(anyType));
    }

    @Test
    public void testNullUndefinedSubtypes() {
        ErrorReporter errorReporter = new SimpleErrorReporter();
        JSTypeRegistry registry = new JSTypeRegistry(errorReporter);

        JSType voidType = registry.getNativeType(JSTypeNative.VOID_TYPE);
        JSType nullType = registry.getNativeType(JSTypeNative.NULL_TYPE);
        JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);

        assertTrue(nullType.isSubtype(nullType));
        assertTrue(voidType.isSubtype(voidType));

        assertFalse(nullType.isSubtype(numberType));
        assertFalse(voidType.isSubtype(numberType));
        assertFalse(numberType.isSubtype(nullType));
        assertFalse(numberType.isSubtype(voidType));
    }

    @Test
    public void testUnionTypeSubtyping() {
        ErrorReporter errorReporter = new SimpleErrorReporter();
        JSTypeRegistry registry = new JSTypeRegistry(errorReporter);

        JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
        JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
        JSType booleanType = registry.getNativeType(JSTypeNative.BOOLEAN_TYPE);

        JSType numStrUnion = registry.createUnionType(numberType, stringType);

        // A component of a union is a subtype of the union
        assertTrue(numberType.isSubtype(numStrUnion));
        assertTrue(stringType.isSubtype(numStrUnion));

        // Union is not a subtype of its component
        assertFalse(numStrUnion.isSubtype(numberType));

        // Union subtype of another union containing it plus more
        JSType numStrBoolUnion = registry.createUnionType(numberType, stringType, booleanType);
        assertTrue(numStrUnion.isSubtype(numStrBoolUnion));
        assertFalse(numStrBoolUnion.isSubtype(numStrUnion));
    }

    @Test
    public void testFunctionTypeSubtyping() {
        ErrorReporter errorReporter = new SimpleErrorReporter();
        JSTypeRegistry registry = new JSTypeRegistry(errorReporter);

        FunctionType fn1 = registry.createFunctionType(
                registry.getNativeType(JSTypeNative.NUMBER_TYPE),
                null,
                null
        );

        FunctionType fn2 = registry.createFunctionType(
                registry.getNativeType(JSTypeNative.NUMBER_TYPE),
                Collections.singletonList(registry.getNativeType(JSTypeNative.STRING_TYPE)),
                null
        );

        assertTrue(fn1.isSubtype(fn1));
        assertTrue(fn2.isSubtype(fn2));
    }

    @Test
    public void testObjectTypeSubtyping() {
        ErrorReporter errorReporter = new SimpleErrorReporter();
        JSTypeRegistry registry = new JSTypeRegistry(errorReporter);

        ObjectType obj1 = registry.createAnonymousObjectType(null);
        ObjectType obj2 = registry.createAnonymousObjectType(null);

        assertTrue(obj1.isSubtype(obj1));
        assertTrue(obj1.isSubtype(registry.getNativeType(JSTypeNative.OBJECT_TYPE)));
    }

    @Test
    public void testIsEquivalentTo() {
        ErrorReporter errorReporter = new SimpleErrorReporter();
        JSTypeRegistry registry = new JSTypeRegistry(errorReporter);

        JSType numberType1 = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
        JSType numberType2 = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
        JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);

        assertTrue(numberType1.isEquivalentTo(numberType2));
        assertFalse(numberType1.isEquivalentTo(stringType));
        assertFalse(numberType1.isEquivalentTo(null));
    }

    @Test
    public void testGetRestrictedType() {
        ErrorReporter errorReporter = new SimpleErrorReporter();
        JSTypeRegistry registry = new JSTypeRegistry(errorReporter);

        JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
        JSType restricted = numberType.getRestrictedTypeGivenNonNull();
        assertNotNull(restricted);
    }

    @Test
    public void testVisitedFlagAndHashCode() {
        ErrorReporter errorReporter = new SimpleErrorReporter();
        JSTypeRegistry registry = new JSTypeRegistry(errorReporter);

        JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
        assertNotNull(numberType.hashCode());
        assertNotNull(numberType.toString());
    }

    @Test
    public void testHaltClearing() {
        ErrorReporter errorReporter = new SimpleErrorReporter();
        JSTypeRegistry registry = new JSTypeRegistry(errorReporter);
        JSType.clearCaches();
        JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
        assertNotNull(numberType);
    }
}