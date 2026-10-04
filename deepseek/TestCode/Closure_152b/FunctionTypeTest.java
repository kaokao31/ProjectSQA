package com.google.javascript.rhino.jstype;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.google.javascript.rhino.ErrorReporter;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.SimpleErrorReporter;
import com.google.javascript.rhino.Token;

public class FunctionTypeTest {
    private JSTypeRegistry registry;
    private ErrorReporter errorReporter;

    @Before
    public void setUp() {
        errorReporter = new SimpleErrorReporter();
        registry = new JSTypeRegistry(errorReporter);
    }

    @Test
    public void testIsSubtypeOfFunctionType() {
        JSType returnType = registry.getNativeType(JSTypeNative.BOOLEAN_TYPE);
        Node parameters = new Node(Token.PARAM_LIST);
        FunctionType funcType = registry.createFunctionType(returnType, parameters);

        assertTrue(funcType.isSubtype(funcType));

        JSType functionType = registry.getNativeType(JSTypeNative.FUNCTION_TYPE);
        assertTrue(funcType.isSubtype(functionType));

        JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
        assertFalse(funcType.isSubtype(numberType));
    }

    @Test
    public void testIsSubtypeOfUnionType() {
        JSType returnType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
        Node parameters = new Node(Token.PARAM_LIST);
        FunctionType funcType = registry.createFunctionType(returnType, parameters);

        JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
        JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
        JSType unionType = registry.createUnionType(numberType, stringType);

        assertFalse(funcType.isSubtype(unionType));
    }

    @Test
    public void testHasInstanceOfWithNoTypeShouldReturnFalse() {
        // This test targets the bug in Defects4J Closure bug 152
        // where hasInstanceOf incorrectly returns true for NO_TYPE.
        FunctionType arrayType = (FunctionType) registry.getNativeType(JSTypeNative.ARRAY_TYPE);
        JSType noType = registry.getNativeType(JSTypeNative.NO_TYPE);
        assertFalse("hasInstanceOf should return false for NO_TYPE", arrayType.hasInstanceOf(noType));
    }

    @Test
    public void testIsConstructor() {
        FunctionType ctor = registry.createConstructorType("Foo", null, null, null);
        assertTrue(ctor.isConstructor());
        assertFalse(ctor.isInterface());

        JSType returnType = registry.getNativeType(JSTypeNative.VOID_TYPE);
        Node params = new Node(Token.PARAM_LIST);
        FunctionType func = registry.createFunctionType(returnType, params);
        assertFalse(func.isConstructor());
    }

    @Test
    public void testGetReturnType() {
        JSType returnType = registry.getNativeType(JSTypeNative.STRING_TYPE);
        Node params = new Node(Token.PARAM_LIST);
        FunctionType func = registry.createFunctionType(returnType, params);
        assertSame(returnType, func.getReturnType());
    }

    @Test
    public void testGetParametersNotNull() {
        JSType returnType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
        Node params = new Node(Token.PARAM_LIST);
        FunctionType func = registry.createFunctionType(returnType, params);
        assertNotNull(func.getParameters());
    }

    @Test(expected = NullPointerException.class)
    public void testHasInstanceOfWithNullThrows() {
        FunctionType arrayType = (FunctionType) registry.getNativeType(JSTypeNative.ARRAY_TYPE);
        arrayType.hasInstanceOf(null);
    }

    @Test
    public void testIsSubtypeOfObjectType() {
        JSType returnType = registry.getNativeType(JSTypeNative.BOOLEAN_TYPE);
        Node parameters = new Node(Token.PARAM_LIST);
        FunctionType funcType = registry.createFunctionType(returnType, parameters);

        JSType objectType = registry.getNativeType(JSTypeNative.OBJECT_TYPE);
        assertTrue(funcType.isSubtype(objectType));
    }
}