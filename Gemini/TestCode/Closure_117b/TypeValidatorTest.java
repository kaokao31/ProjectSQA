package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.ObjectType;
import com.google.javascript.rhino.jstype.TemplateTypeMap;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * JUnit 4 test suite for TypeValidator (Closure Bug 117 context).
 */
public class TypeValidatorTest {

    private AbstractCompiler compiler;
    private TypeValidator typeValidator;
    private JSTypeRegistry typeRegistry;

    @Before
    public void setUp() {
        compiler = new Compiler();
        typeRegistry = compiler.getTypeRegistry();
        typeValidator = new TypeValidator(compiler);
    }

    @Test
    public void testGetNativeType() {
        assertNotNull(typeValidator.getNativeType(JSTypeRegistry.OBJECT_TYPE));
    }

    @Test
    public void testExpectObject() {
        Node n = new Node(Token.NAME);
        JSType stringType = typeRegistry.getNativeType(JSTypeRegistry.STRING_TYPE);
        
        // This should trigger type warning/mismatch logic in TypeValidator
        typeValidator.expectObject(null, n, stringType, "msg");
    }

    @Test
    public void testExpectNotNull() {
        Node n = new Node(Token.NAME);
        JSType nullType = typeRegistry.getNativeType(JSTypeRegistry.NULL_TYPE);
        
        typeValidator.expectNotNull(null, n, nullType, "msg", typeRegistry.getNativeType(JSTypeRegistry.OBJECT_TYPE));
    }

    @Test
    public void testExpectArgument() {
        Node n = new Node(Token.NAME);
        JSType stringType = typeRegistry.getNativeType(JSTypeRegistry.STRING_TYPE);
        JSType numberType = typeRegistry.getNativeType(JSTypeRegistry.NUMBER_TYPE);

        typeValidator.expectArgument(null, n, stringType, numberType, "msg", 0);
    }

    @Test
    public void testRegisterSyntheticFunction() {
        Node n = new Node(Token.FUNCTION);
        typeValidator.registerSyntheticFunction(null, n);
    }

    @Test
    public void testSetLastOwnProperty() {
        typeValidator.setLastOwnProperty("propName");
    }

    @Test
    public void testGetWarningCount() {
        // Initially zero
        assertEquals(0, typeValidator.getWarningCount());
    }

    @Test
    public void testGetErrorCount() {
        // Initially zero
        assertEquals(0, typeValidator.getErrorCount());
    }

    @Test
    public void testAreTypesEquivalent() {
        JSType t1 = typeRegistry.getNativeType(JSTypeRegistry.STRING_TYPE);
        JSType t2 = typeRegistry.getNativeType(JSTypeRegistry.STRING_TYPE);
        JSType t3 = typeRegistry.getNativeType(JSTypeRegistry.NUMBER_TYPE);

        assertTrue(typeValidator.areTypesEquivalent(t1, t2));
        assertFalse(typeValidator.areTypesEquivalent(t1, t3));
        assertFalse(typeValidator.areTypesEquivalent(t1, null));
        assertFalse(typeValidator.areTypesEquivalent(null, t1));
        assertTrue(typeValidator.areTypesEquivalent(null, null));
    }
}