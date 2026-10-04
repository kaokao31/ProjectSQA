package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import com.google.javascript.jscomp.FunctionTypeBuilder;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.jstype.*;
import org.junit.Before;
import org.junit.Test;

/**
 * Test suite for FunctionTypeBuilder, targeting bug 90 in Defects4J.
 * These tests focus on edge cases and unknown type handling.
 */
public class FunctionTypeBuilderTest {

    private Compiler compiler;
    private JSTypeRegistry registry;
    private JSType unknownType;
    private JSType numberType;
    private JSType stringType;

    @Before
    public void setUp() {
        compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        compiler.initOptions(options);
        compiler.init(compiler.getSourceFileCache(), null, options);
        registry = compiler.getTypeRegistry();
        unknownType = registry.getNativeType(JSTypeNative.UNKNOWN_TYPE);
        numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
        stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    }

    // Helper to create a simple function node
    private Node createSimpleFunctionNode(String name) {
        Node fnNode = new Node(Token.FUNCTION);
        fnNode.addChildToFront(new Node(Token.NAME, name));
        fnNode.addChildToBack(new Node(Token.PARAM_LIST));
        fnNode.addChildToBack(new Node(Token.BLOCK));
        return fnNode;
    }

    @Test
    public void testBuildMinimalFunction() {
        Node fnNode = createSimpleFunctionNode("f");
        FunctionTypeBuilder builder =
            new FunctionTypeBuilder(fnNode, compiler, registry)
                .withReturnType(unknownType)
                .withParameters(JSTypeObjectField.EMPTY_LIST);
        FunctionType result = builder.build();
        assertNotNull("Function type should not be null", result);
        assertEquals("Return type should be unknown", unknownType, result.getReturnType());
        assertEquals("Should have no parameters", 0, result.getParameters().size());
    }

    @Test
    public void testBuildWithOneParameter() {
        Node fnNode = createSimpleFunctionNode("g");
        ImmutableList<JSType> paramTypes = ImmutableList.of(numberType);
        FunctionTypeBuilder builder =
            new FunctionTypeBuilder(fnNode, compiler, registry)
                .withReturnType(stringType)
                .withParameters(paramTypes);
        FunctionType result = builder.build();
        assertNotNull(result);
        assertEquals(stringType, result.getReturnType());
        assertEquals(1, result.getParameters().size());
        assertEquals(numberType, result.getParameters().get(0));
    }

    @Test
    public void testBuildWithUnknownParameterType() {
        Node fnNode = createSimpleFunctionNode("h");
        // This is the bug-triggering scenario: unknown type as parameter type
        ImmutableList<JSType> paramTypes = ImmutableList.of(unknownType);
        FunctionTypeBuilder builder =
            new FunctionTypeBuilder(fnNode, compiler, registry)
                .withReturnType(numberType)
                .withParameters(paramTypes);
        // Should not throw NullPointerException (bug 90 fix)
        FunctionType result = builder.build();
        assertNotNull(result);
        assertEquals(1, result.getParameters().size());
        assertEquals(unknownType, result.getParameters().get(0));
    }

    @Test
    public void testBuildWithNullReturnType() {
        Node fnNode = createSimpleFunctionNode("i");
        FunctionTypeBuilder builder =
            new FunctionTypeBuilder(fnNode, compiler, registry)
                .withReturnType(null);
        try {
            FunctionType result = builder.build();
            // Depending on implementation, could either throw or default
            assertNotNull("If no exception, result should not be null", result);
        } catch (NullPointerException e) {
            // This may indicate a bug; we should capture it
            fail("Null return type should not cause NPE");
        }
    }

    @Test
    public void testBuildWithNullParameterList() {
        Node fnNode = createSimpleFunctionNode("j");
        FunctionTypeBuilder builder =
            new FunctionTypeBuilder(fnNode, compiler, registry)
                .withReturnType(unknownType)
                .withParameters(null);
        try {
            FunctionType result = builder.build();
            assertNotNull(result);
            assertEquals("Parameters should be empty when null", 0, result.getParameters().size());
        } catch (NullPointerException e) {
            // This could be a bug if not handled
            fail("Null parameter list should be treated as empty");
        }
    }

    @Test
    public void testBuildMultipleParametersMixedTypes() {
        Node fnNode = createSimpleFunctionNode("k");
        ImmutableList<JSType> paramTypes = ImmutableList.of(numberType, stringType, unknownType);
        FunctionTypeBuilder builder =
            new FunctionTypeBuilder(fnNode, compiler, registry)
                .withReturnType(unknownType)
                .withParameters(paramTypes);
        FunctionType result = builder.build();
        assertNotNull(result);
        assertEquals(3, result.getParameters().size());
        assertEquals(numberType, result.getParameters().get(0));
        assertEquals(stringType, result.getParameters().get(1));
        assertEquals(unknownType, result.getParameters().get(2));
    }

    @Test
    public void testBuildWithEmptyParameterList() {
        Node fnNode = createSimpleFunctionNode("empty");
        ImmutableList<JSType> emptyParams = ImmutableList.of();
        FunctionTypeBuilder builder =
            new FunctionTypeBuilder(fnNode, compiler, registry)
                .withReturnType(numberType)
                .withParameters(emptyParams);
        FunctionType result = builder.build();
        assertNotNull(result);
        assertEquals(0, result.getParameters().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuildWithNullFunctionNode() {
        // This should throw as function node cannot be null
        new FunctionTypeBuilder(null, compiler, registry);
    }
}