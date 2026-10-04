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
    private AbstractCompiler abstractCompiler;

    @Before
    public void setUp() {
        compiler = new Compiler();
        // Initialize compiler options minimally if needed
        CompilerOptions options = new CompilerOptions();
        compiler.initOptions(options);
        typeRegistry = compiler.getTypeRegistry();
        abstractCompiler = compiler;
    }

    @Test
    public void testConstructionAndBasicBuild() {
        Node fnNode = new Node(Token.FUNCTION);
        String fnName = "myFunc";
        Scope scope = new Scope(null, compiler);

        FunctionTypeBuilder builder = new FunctionTypeBuilder(fnName, compiler, fnNode, fnName, scope);
        assertNotNull(builder);

        // Test method chaining returning this
        assertSame(builder, builder.setSourceFileName("test.js"));
        assertSame(builder, builder.setThisType(null));
        assertSame(builder, builder.setParametersNode(null));
        assertSame(builder, builder.setReturnType(null));
        assertSame(builder, builder.inferStandardAnnotations());
        assertSame(builder, builder.inferFromParameterList(fnNode, null));

        FunctionType fnType = builder.build();
        assertNotNull(fnType);
    }

    @Test
    public void testInferTemplateTypeName() {
        Node fnNode = new Node(Token.FUNCTION);
        Scope scope = new Scope(null, compiler);
        FunctionTypeBuilder builder = new FunctionTypeBuilder("fn", compiler, fnNode, "fn", scope);

        // Test inferTemplateTypeName with various inputs
        JSDocInfo infoWithoutTemplate = new JSDocInfo();
        assertFalse(builder.inferTemplateTypeName(infoWithoutTemplate));

        JSDocInfo infoWithTemplate = new JSDocInfo();
        infoWithTemplate.addTemplateTypeName("T");
        assertTrue(builder.inferTemplateTypeName(infoWithTemplate));
    }

    @Test
    public void testHasEqualOverriddenParams() {
        // Testing parameter mismatch and match logic via inferReturnOrThrow or similar paths if exposed,
        // or directly testing function type builder methods.
        Node fnNode = new Node(Token.FUNCTION);
        Scope scope = new Scope(null, compiler);
        FunctionTypeBuilder builder = new FunctionTypeBuilder("fn", compiler, fnNode, "fn", scope);

        // Dummy object type for overriding
        ObjectType ownerType = typeRegistry.createObjectType("Owner", null);
        
        assertSame(builder, builder.inferInheritance(null));
    }

    @Test
    public void testFunctionTypeBuilderWithJSDoc() {
        Node fnNode = new Node(Token.FUNCTION);
        Scope scope = new Scope(null, compiler);
        FunctionTypeBuilder builder = new FunctionTypeBuilder("fn", compiler, fnNode, "fn", scope);

        JSDocInfo info = new JSDocInfo();
        assertSame(builder, builder.setbaseType(null));
        
        // Build with basic annotations
        FunctionType type = builder
                .setSourceFileName("file.js")
                .inferStandardAnnotations(info)
                .build();
        assertNotNull(type);
    }
}