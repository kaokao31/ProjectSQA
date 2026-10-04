package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TypedScopeCreatorTest {

    private Compiler compiler;
    private TypedScopeCreator scopeCreator;

    @Before
    public void setUp() {
        compiler = new Compiler();
        // Initialize compiler with default options to support basic scope creation and type checking
        CompilerOptions options = new CompilerOptions();
        compiler.initOptions(options);
        scopeCreator = new TypedScopeCreator(compiler);
    }

    @Test
    public void testCreateScopeEmptyRoot() {
        Node root = new Node(Token.BLOCK);
        Scope scope = scopeCreator.createScope(root, null);
        assertNotNull(scope);
        assertEquals(compiler.getCodingConvention(), scopeCreator.getCodingConvention());
    }

    @Test
    public void testGlobalScopeCreation() {
        Node script = new Node(Token.SCRIPT);
        Node varNode = new Node(Token.VAR, Node.newString(Token.NAME, "x"));
        script.addChildToBack(varNode);

        Scope scope = scopeCreator.createScope(script, null);
        assertNotNull(scope);
        assertTrue(scope.isGlobal());
        assertNotNull(scope.getVar("x"));
    }

    @Test
    public void testFunctionScopeCreation() {
        Node script = new Node(Token.SCRIPT);
        Node nameNode = Node.newString(Token.NAME, "f");
        Node argsNode = new Node(Token.LP);
        Node bodyNode = new Node(Token.BLOCK);
        Node functionNode = new Node(Token.FUNCTION, nameNode, argsNode, bodyNode);
        script.addChildToBack(functionNode);

        Scope globalScope = scopeCreator.createScope(script, null);
        assertNotNull(globalScope);
        
        Scope functionScope = scopeCreator.createScope(functionNode, globalScope);
        assertNotNull(functionScope);
        assertTrue(functionScope.isFunctionScope());
        assertEquals(functionNode, functionScope.getRootNode());
    }

    @Test
    public void testCatchScopeCreation() {
        Node script = new Node(Token.SCRIPT);
        Node catchName = Node.newString(Token.NAME, "e");
        Node catchBlock = new Node(Token.BLOCK);
        Node catchNode = new Node(Token.CATCH, catchName, catchBlock);
        script.addChildToBack(catchNode);

        Scope globalScope = scopeCreator.createScope(script, null);
        assertNotNull(globalScope);
        
        Scope catchScope = scopeCreator.createScope(catchNode, globalScope);
        assertNotNull(catchScope);
    }

    @Test
    public void testWithScopeCreation() {
        Node script = new Node(Token.SCRIPT);
        Node objNode = new Node(Token.NAME, "obj");
        Node bodyNode = new Node(Token.BLOCK);
        Node withNode = new Node(Token.WITH, objNode, bodyNode);
        script.addChildToBack(withNode);

        Scope globalScope = scopeCreator.createScope(script, null);
        assertNotNull(globalScope);
        
        Scope withScope = scopeCreator.createScope(withNode, globalScope);
        assertNotNull(withScope);
    }

    @Test
    public void testFunctionWithParameters() {
        Node script = new Node(Token.SCRIPT);
        Node nameNode = Node.newString(Token.NAME, "add");
        Node argsNode = new Node(Token.LP);
        argsNode.addChildToBack(Node.newString(Token.NAME, "a"));
        argsNode.addChildToBack(Node.newString(Token.NAME, "b"));
        Node bodyNode = new Node(Token.BLOCK);
        Node functionNode = new Node(Token.FUNCTION, nameNode, argsNode, bodyNode);
        script.addChildToBack(functionNode);

        Scope globalScope = scopeCreator.createScope(script, null);
        Scope functionScope = scopeCreator.createScope(functionNode, globalScope);
        
        assertNotNull(functionScope.getVar("a"));
        assertNotNull(functionScope.getVar("b"));
    }

    @Test
    public void testVarRedclaration() {
        Node script = new Node(Token.SCRIPT);
        Node var1 = new Node(Token.VAR, Node.newString(Token.NAME, "x"));
        Node var2 = new Node(Token.VAR, Node.newString(Token.NAME, "x"));
        script.addChildToBack(var1);
        script.addChildToBack(var2);

        Scope scope = scopeCreator.createScope(script, null);
        assertNotNull(scope);
        assertNotNull(scope.getVar("x"));
    }

    @Test
    public void testGetRegistry() {
        JSTypeRegistry registry = compiler.getTypeRegistry();
        assertNotNull(registry);
    }
}