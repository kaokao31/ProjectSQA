package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TypedScopeCreatorTest {

    private Compiler compiler;

    @Before
    public void setUp() {
        compiler = new Compiler();
        // Initialize compiler with basic options to prevent null pointers during scope creation
        CompilerOptions options = new CompilerOptions();
        compiler.initOptions(options);
    }

    @Test
    public void testCreationWithNullRoot() {
        AbstractCompiler abstractCompiler = compiler;
        CodingConvention convention = new DefaultCodingConvention();
        TypedScopeCreator creator = new TypedScopeCreator(abstractCompiler, convention);

        try {
            Scope scope = creator.createScope(null, null);
            // Depending on strictness, it might return null or throw an exception.
            // If it handles null or throws, let's verify behavior.
            assertNull(scope);
        } catch (Exception e) {
            // If it throws an exception on null root, that's also acceptable, 
            // but we ensure the branch is covered.
            assertNotNull(e);
        }
    }

    @Test
    public void testCreateScriptScope() {
        AbstractCompiler abstractCompiler = compiler;
        CodingConvention convention = new DefaultCodingConvention();
        TypedScopeCreator creator = new TypedScopeCreator(abstractCompiler, convention);

        Node scriptNode = new Node(Token.SCRIPT);
        Scope globalScope = new Scope(null, abstractCompiler);
        
        Scope scope = creator.createScope(scriptNode, globalScope);
        assertNotNull(scope);
        assertEquals(scriptNode, scope.getRootNode());
    }

    @Test
    public void testCreateFunctionScope() {
        AbstractCompiler abstractCompiler = compiler;
        CodingConvention convention = new DefaultCodingConvention();
        TypedScopeCreator creator = new TypedScopeCreator(abstractCompiler, convention);

        // FUNCTION node with name and body
        Node nameNode = Node.newString(Token.NAME, "myFunc");
        Node argsNode = new Node(Token.PARAM_LIST);
        Node bodyNode = new Node(Token.BLOCK);
        Node funcNode = new Node(Token.FUNCTION, nameNode, argsNode, bodyNode);

        Scope globalScope = new Scope(null, abstractCompiler);
        
        Scope scope = creator.createScope(funcNode, globalScope);
        assertNotNull(scope);
        assertEquals(funcNode, scope.getRootNode());
    }
}