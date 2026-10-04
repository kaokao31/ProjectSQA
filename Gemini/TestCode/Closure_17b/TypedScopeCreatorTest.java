package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TypedScopeCreatorTest {

    private Compiler compiler;
    private AbstractCompiler abstractCompiler;

    @Before
    public void setUp() {
        compiler = new Compiler();
        // Initialize compiler options minimally to prevent NPEs during scopes creation
        CompilerOptions options = new CompilerOptions();
        compiler.initOptions(options);
        abstractCompiler = compiler;
    }

    @Test
    public void testConstructorAndBasicCreation() {
        TypedScopeCreator creator = new TypedScopeCreator(abstractCompiler);
        assertNotNull(creator);

        // Test creating a global scope with a simple script node
        Node scriptNode = new Node(Token.SCRIPT);
        Scope globalScope = creator.createScope(scriptNode, null);
        assertNotNull(globalScope);
        assertTrue(globalScope.isGlobal());
    }

    @Test
    public void testFunctionScopeCreation() {
        TypedScopeCreator creator = new TypedScopeCreator(abstractCompiler);
        
        Node scriptNode = new Node(Token.SCRIPT);
        Scope globalScope = creator.createScope(scriptNode, null);

        // function f() {}
        Node nameNode = Node.newString(Token.NAME, "f");
        Node argsNode = new Node(Token.PARAM_LIST);
        Node bodyNode = new Node(Token.BLOCK);
        Node funcNode = new Node(Token.FUNCTION, nameNode, argsNode, bodyNode);
        scriptNode.addChildToFront(funcNode);

        Scope functionScope = creator.createScope(funcNode, globalScope);
        assertNotNull(functionScope);
        assertTrue(functionScope.isFunctionScope());
        assertEquals("f", functionScope.getFunctionName().getString());
    }

    @Test
    public void testObjectLitDeclarationWithGetSet() {
        TypedScopeCreator creator = new TypedScopeCreator(compiler);
        
        // Constructing an object literal with getter/setter which often triggers 
        // complex handling in Closure's TypedScopeCreator (especially around bug ID 17 
        // regarding Node.GETTER_DEF / Node.SETTER_DEF / ObjectLit and type inference)
        Node keyGet = Node.newString(Token.GETTER_DEF, "a");
        Node funcNode = new Node(Token.FUNCTION, Node.newString(Token.NAME, ""), new Node(Token.PARAM_LIST), new Node(Token.BLOCK));
        keyGet.addChildToFront(funcNode);

        Node objLit = new Node(Token.OBJECTLIT, keyGet);
        Node varNode = new Node(Token.VAR, Node.newString(Token.NAME, "x"));
        varNode.getFirstChild().addChildToFront(objLit);

        Node script = new Node(Token.SCRIPT, varNode);
        compiler.parse(script); // Mock parsing state if needed or let creator handle AST

        Scope scope = creator.createScope(script, null);
        assertNotNull(scope);
        assertNotNull(scope.getSlot("x"));
    }

    @Test
    public void testGetReactiveScopeEdgeCases() {
        TypedScopeCreator creator = new TypedScopeCreator(abstractCompiler);
        Node scriptNode = new Node(Token.SCRIPT);
        Scope scope = creator.createScope(scriptNode, null);

        // Test working with catch block or block scopes
        Node block = new Node(Token.BLOCK);
        Scope blockScope = creator.createScope(block, scope);
        assertNotNull(blockScope);
    }
}