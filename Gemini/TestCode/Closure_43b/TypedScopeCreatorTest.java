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
        // Initialize compiler options/state if necessary
        CompilerOptions options = new CompilerOptions();
        compiler.initOptions(options);
    }

    @Test
    public void testCreationWithNullRoot() {
        // Test behavior when node is null or empty
        AbstractCompiler absCompiler = compiler;
        TypedScopeCreator creator = new TypedScopeCreator(absCompiler);
        
        Node root = new Node(Token.BLOCK);
        Scope scope = creator.createScope(root, null);
        assertNotNull(scope);
    }

    @Test
    public void testConstructorAndBasicScope() {
        AbstractCompiler absCompiler = compiler;
        TypedScopeCreator creator = new TypedScopeCreator(absCompiler);
        assertNotNull(creator);

        Node root = new Node(Token.SCRIPT);
        Scope scope = creator.createScope(root, null);
        assertNotNull(scope);
    }

    @Test
    public void testScopeCreationWithVarDeclaration() {
        AbstractCompiler absCompiler = compiler;
        TypedScopeCreator creator = new TypedScopeCreator(absCompiler);

        // Build a simple AST: var x = 10;
        Node nameNode = Node.newString(Token.NAME, "x");
        Node numberNode = Node.newNumber(10.0);
        nameNode.addChildToBack(numberNode);
        
        Node varNode = new Node(Token.VAR, nameNode);
        Node scriptNode = new Node(Token.SCRIPT, varNode);

        Scope scope = creator.createScope(scriptNode, null);
        assertNotNull(scope);
        assertNotNull(scope.getVar("x"));
    }

    @Test
    public void testScopeCreationWithFunction() {
        AbstractCompiler absCompiler = compiler;
        TypedScopeCreator creator = new TypedScopeCreator(absCompiler);

        // Build a simple function AST: function f(a) { var b = a; }
        Node paramNode = Node.newString(Token.NAME, "a");
        Node paramList = new Node(Token.LP, paramNode);
        
        Node varB = Node.newString(Token.NAME, "b");
        Node nameA = Node.newString(Token.NAME, "a");
        varB.addChildToBack(nameA);
        Node varNode = new Node(Token.VAR, varB);
        Node bodyNode = new Node(Token.BLOCK, varNode);

        Node funcName = Node.newString(Token.NAME, "f");
        Node funcNode = new Node(Token.FUNCTION, funcName, paramList, bodyNode);
        Node scriptNode = new Node(Token.SCRIPT, funcNode);

        Scope scope = creator.createScope(scriptNode, null);
        assertNotNull(scope);
        assertNotNull(scope.getVar("f"));
    }

    @Test
    public void testCatchBlockScopeHandling() {
        AbstractCompiler absCompiler = compiler;
        TypedScopeCreator creator = new TypedScopeCreator(absCompiler);

        // try { } catch (e) { var x = e; }
        Node catchName = Node.newString(Token.NAME, "e");
        Node varX = Node.newString(Token.NAME, "x");
        Node refE = Node.newString(Token.NAME, "e");
        varX.addChildToBack(refE);
        Node block = new Node(Token.BLOCK, new Node(Token.VAR, varX));
        
        Node catchNode = new Node(Token.CATCH, catchName, block);
        Node tryNode = new Node(Token.TRY, new Node(Token.BLOCK), catchNode);
        Node scriptNode = new Node(Token.SCRIPT, tryNode);

        Scope scope = creator.createScope(scriptNode, null);
        assertNotNull(scope);
    }
}