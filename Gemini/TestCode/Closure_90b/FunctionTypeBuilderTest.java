package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.ObjectType;
import com.google.javascript.rhino.jstype.FunctionType;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class FunctionTypeBuilderTest {

    private Compiler compiler;
    private JSTypeRegistry registry;
    private FunctionTypeBuilder builder;

    @Before
    public void setUp() {
        compiler = new Compiler();
        registry = compiler.getTypeRegistry();
        builder = new FunctionTypeBuilder("someFunction", compiler, null, null, null);
    }

    @Test
    public void testInitWithoutClassName() {
        Node info = new Node(Token.JSDOC_INFO);
        Node n = Node.newString(Token.NAME, "myFunc");
        
        FunctionTypeBuilder resultBuilder = builder.init(n, "someSource", info);
        assertNotNull(resultBuilder);
    }

    @Test
    public void testWithThisTypeObjectType() {
        ObjectType objectType = registry.createNativeObjectType(
            com.google.javascript.rhino.jstype.JSTypeNative.OBJECT_TYPE
        );
        FunctionTypeBuilder resultBuilder = builder.withThisType(objectType);
        assertNotNull(resultBuilder);
    }

    @Test
    public void testWithThisTypeNull() {
        FunctionTypeBuilder resultBuilder = builder.withThisType(null);
        assertNotNull(resultBuilder);
    }

    @Test
    public void testWithDocumentedParameter() {
        Node info = new Node(Token.JSDOC_INFO);
        FunctionTypeBuilder resultBuilder = builder.inferParameterTypes(info);
        assertNotNull(resultBuilder);
    }

    @Test
    public void testWithNoParameters() {
        FunctionTypeBuilder resultBuilder = builder.inferParameterTypes(null);
        assertNotNull(resultBuilder);
    }

    @Test
    public void testBuildWithoutReg() {
        // Test building a basic function type when missing optional context
        FunctionType fnType = builder.build();
        assertNotNull(fnType);
    }

    @Test
    public void testAsNonNullType() {
        ObjectType objectType = registry.createNativeObjectType(
            com.google.javascript.rhino.jstype.JSTypeNative.OBJECT_TYPE
        );
        FunctionTypeBuilder resultBuilder = builder.withReceiverType(objectType);
        assertNotNull(resultBuilder);
    }
}