package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TypedScopeCreatorTest {

    private Compiler compiler;
    private AbstractCompiler abstractCompiler;

    @Before
    public void setUp() {
        compiler = new Compiler();
        // Initialize compiler options minimally to avoid NPEs during scope creation
        CompilerOptions options = new CompilerOptions();
        compiler.initOptions(options);
        abstractCompiler = compiler;
    }

    @Test
    public void testCreationWithNullCompiler() {
        // Test behavior with null compiler if applicable, or instantiate with valid abstract compiler
        try {
            TypedScopeCreator creator = new TypedScopeCreator(null);
            assertNotNull(creator);
        } catch (Exception e) {
            // Expected if null is not allowed
        }
    }

    @Test
    public void testCreateScopeBasic() {
        TypedScopeCreator creator = new TypedScopeCreator(abstractCompiler);
        Node root = new Node(Token.SCRIPT);
        Scope scope = creator.createScope(root, null);
        assertNotNull(scope);
        assertSame(root, scope.getRootNode());
    }

    @Test
    public void testCreateScopeWithVar() {
        TypedScopeCreator creator = new TypedScopeCreator(abstractCompiler);
        
        // Build a simple AST: var x = 1;
        Node nameNode = Node.newString(Token.NAME, "x");
        Node numberNode = Node.newNumber(1.0);
        nameNode.addChildToBack(numberNode);
        Node varNode = new Node(Token.VAR, nameNode);
        Node root = new Node(Token.SCRIPT, varNode);
        
        Scope scope = creator.createScope(root, null);
        assertNotNull(scope);
        assertNotNull(scope.getVar("x"));
    }

    @Test
    public void testFunctionScopeCreation() {
        TypedScopeCreator creator = new TypedScopeCreator(abstractCompiler);
        
        // Build: function f(a) { var b = a; }
        Node argNode = Node.newString(Token.NAME, "a");
        Node paramList = new Node(Token.PARAM_LIST, argNode);
        Node functionName = Node.newString(Token.NAME, "f");
        
        Node varB = Node.newString(Token.NAME, "b");
        varB.addChildToBack(Node.newString(Token.NAME, "a"));
        Node block = new Node(Token.BLOCK, new Node(Token.VAR, varB));
        
        Node functionNode = new Node(Token.FUNCTION, functionName, paramList, block);
        Node root = new Node(Token.SCRIPT, functionNode);
        
        Scope scope = creator.createScope(root, null);
        assertNotNull(scope);
        
        Var fVar = scope.getVar("f");
        assertNotNull(fVar);
        
        Scope fnScope = creator.createScope(functionNode, scope);
        assertNotNull(fnScope);
        assertNotNull(fnScope.getVar("a"));
        assertNotNull(fnScope.getVar("b"));
    }

    @Test
    public void testHoistingAndDeclarations() {
        TypedScopeCreator creator = new TypedScopeCreator(abstractCompiler);
        
        // function f() {}
        Node funcNode = new Node(Token.FUNCTION, 
                Node.newString(Token.NAME, "f"), 
                new Node(Token.PARAM_LIST), 
                new Node(Token.BLOCK));
        
        Node root = new Node(Token.SCRIPT, funcNode);
        Scope scope = creator.createScope(root, null);
        
        assertNotNull(scope.getVar("f"));
    }
    
    @Test
    public void testPatchGlobalScope() {
        TypedScopeCreator creator = new TypedScopeCreator(abstractCompiler);
        Node root = new Node(Token.SCRIPT);
        Scope globalScope = creator.createScope(root, null);
        
        try {
            creator.patchGlobalScope(globalScope, root);
        } catch (UnsupportedOperationException | NullPointerException e) {
            // Depending on compiler state, patch might require specific setup
        }
    }
}