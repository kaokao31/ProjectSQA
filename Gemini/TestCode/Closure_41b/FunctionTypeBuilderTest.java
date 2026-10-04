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
    private JSTypeRegistry typeRegistry;
    private FunctionTypeBuilder builder;

    @Before
    public void setUp() {
        compiler = new Compiler();
        // Initialize compiler options if necessary
        CompilerOptions options = new CompilerOptions();
        compiler.initOptions(options);
        typeRegistry = compiler.getTypeRegistry();
        
        // A dummy source file for the builder
        SourceFile sourceFile = SourceFile.fromCode("testcode", "");
        builder = new FunctionTypeBuilder("testFn", compiler, sourceFile, "testcode", null);
    }

    @Test
    public void testBasicFunctionTypeBuilding() {
        Node fnNode = new Node(Token.FUNCTION);
        builder.setFunctionName(Node.newString("testFn"), fnNode);
        
        FunctionType fnType = builder.build();
        assertNotNull(fnType);
        assertEquals("testFn", fnType.getReferenceName());
    }

    @Test
    public void testWithInterfaceThisType() {
        Node fnNode = new Node(Token.FUNCTION);
        builder.setFunctionName(Node.newString("testFn"), fnNode);
        
        // Create a dummy object type for 'this'
        ObjectType objType = typeRegistry.createObjectType("MyInterface", null);
        objType.setInterface();
        
        FunctionTypeBuilder resultBuilder = builder.withThisType(objType);
        assertNotNull(resultBuilder);
        
        FunctionType fnType = builder.build();
        assertNotNull(fnType);
    }

    @Test
    public void testWithRecordThisType() {
        Node fnNode = new Node(Token.FUNCTION);
        builder.setFunctionName(Node.newString("testFn"), fnNode);
        
        ObjectType recordType = typeRegistry.createRecordType(null);
        
        FunctionTypeBuilder resultBuilder = builder.withThisType(recordType);
        assertNotNull(resultBuilder);
        
        FunctionType fnType = builder.build();
        assertNotNull(fnType);
    }

    @Test
    public void testWithNullThisType() {
        Node fnNode = new Node(Token.FUNCTION);
        builder.setFunctionName(Node.newString("testFn"), fnNode);
        
        FunctionTypeBuilder resultBuilder = builder.withThisType(null);
        assertNotNull(resultBuilder);
    }

    @Test
    public void testInferFromParameter() {
        Node fnNode = new Node(Token.FUNCTION);
        builder.setFunctionName(Node.newString("testFn"), fnNode);
        
        JSType stringType = typeRegistry.getNativeType(com.google.javascript.rhino.jstype.JSTypeNative.STRING_TYPE);
        
        // Trigger parameter inference paths
        builder.inferParameterTypes(fnNode, null);
        FunctionType fnType = builder.build();
        assertNotNull(fnType);
    }

    @Test
    public void testFunctionTypeBuilderMethodChaining() {
        Node fnNode = new Node(Token.FUNCTION);
        JSType stringType = typeRegistry.getNativeType(com.google.javascript.rhino.jstype.JSTypeNative.STRING_TYPE);

        FunctionType fnType = builder
                .setFunctionName(Node.newString("chainedFn"), fnNode)
                .withParamsNode(null)
                .withReturnType(stringType)
                .build();

        assertNotNull(fnType);
        assertEquals("chainedFn", fnType.getReferenceName());
    }
}