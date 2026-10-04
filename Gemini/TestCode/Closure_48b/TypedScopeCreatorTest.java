package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TypedScopeCreatorTest {

    private Compiler compiler;
    private AbstractCompiler abstractCompiler;
    private TypedScopeCreator scopeCreator;

    @Before
    public void setUp() {
        compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        compiler.initOptions(options);
        abstractCompiler = compiler;
    }

    @Test
    public void testCreationWithNullCompiler() {
        try {
            new TypedScopeCreator(null);
        } catch (Exception e) {
            // Expected or handled gracefully depending on implementation
        }
    }

    @Test
    public void testCreateScopeEmptyRoot() {
        scopeCreator = new TypedScopeCreator(abstractCompiler);
        Node root = new Node(Token.BLOCK);
        Scope scope = scopeCreator.createScope(root, null);
        assertNotNull(scope);
    }

    @Test
    public void testGlobalScopeCreation() {
        scopeCreator = new TypedScopeCreator(abstractCompiler);
        Node root = new Node(Token.SCRIPT);
        Scope scope = scopeCreator.createScope(root, null);
        assertNotNull(scope);
        assertTrue(scope.isGlobal());
    }

    @Test
    public void testFunctionScopeCreation() {
        scopeCreator = new TypedScopeCreator(abstractCompiler);
        Node scriptRoot = new Node(Token.SCRIPT);
        Node functionNode = new Node(Token.FUNCTION, 
                Node.newString(Token.NAME, "myFunc"), 
                new Node(Token.PARAM_LIST), 
                new Node(Token.BLOCK));
        scriptRoot.addChildToBack(functionNode);

        Scope globalScope = scopeCreator.createScope(scriptRoot, null);
        assertNotNull(globalScope);

        Scope functionScope = scopeCreator.createScope(functionNode, globalScope);
        assertNotNull(functionScope);
        assertTrue(functionScope.isFunctionScope());
    }

    @Test
    public void testVariableDeclarationInScope() {
        scopeCreator = new TypedScopeCreator(abstractCompiler);
        Node scriptRoot = new Node(Token.SCRIPT);
        Node varNode = new Node(Token.VAR, Node.newString(Token.NAME, "x"));
        scriptRoot.addChildToBack(varNode);

        Scope globalScope = scopeCreator.createScope(scriptRoot, null);
        assertNotNull(globalScope);
        assertNotNull(globalScope.getVar("x"));
    }

    @Test
    public void testObjectLitWithGetSet() {
        scopeCreator = new TypedScopeCreator(abstractCompiler);
        
        // Constructing an object literal with getter and setter to trigger specific branches in TypedScopeCreator (like Bug 48 getter/setter handling)
        Node keyGet = Node.newString(Token.GETTER, "a");
        keyGet.addChildToBack(new Node(Token.FUNCTION, Node.newString(Token.NAME, ""), new Node(Token.PARAM_LIST), new Node(Token.BLOCK)));
        
        Node keySet = Node.newString(Token.SETTER, "a");
        keySet.addChildToBack(new Node(Token.FUNCTION, Node.newString(Token.NAME, ""), new Node(Token.PARAM_LIST), new Node(Token.BLOCK)));

        Node objLit = new Node(Token.OBJECTLIT, keyGet, keySet);
        Node varNode = new Node(Token.VAR, objLit);
        varNode.getFirstChild().setString("obj");

        Node scriptRoot = new Node(Token.SCRIPT, varNode);

        Scope globalScope = scopeCreator.createScope(scriptRoot, null);
        assertNotNull(globalScope);
        assertNotNull(globalScope.getVar("obj"));
    }

    @Test
    public void testScopeCreatorWithExistingTypeRegistry() {
        JSTypeRegistry registry = compiler.getTypeRegistry();
        scopeCreator = new TypedScopeCreator(abstractCompiler, "myCodingConvention");
        assertNotNull(scopeCreator);
    }
}