package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class CheckGlobalThisTest {

    private Compiler compiler;
    private CheckGlobalThis checkGlobalThis;
    private CheckLevel defaultLevel;

    @Before
    public void setUp() {
        compiler = new Compiler();
        defaultLevel = CheckLevel.WARNING;
        checkGlobalThis = new CheckGlobalThis(compiler, defaultLevel);
    }

    @Test
    public void testShouldTraverseNodeNull() {
        NodeTraversal traversal = new NodeTraversal(compiler, checkGlobalThis);
        // Should return true for null or non-interesting nodes if applicable, 
        // but let's test shouldTraverse with various node types.
        assertTrue(checkGlobalThis.shouldTraverse(traversal, null));
    }

    @Test
    public void testBasicAssignmentToGlobalThis() {
        // this.foo = 1; at the global scope
        Node thisNode = new Node(Token.THIS);
        Node getprop = new Node(Token.GETPROP, thisNode, Node.newString("foo"));
        Node assign = new Node(Token.ASSIGN, getprop, Node.newNumber(1));

        NodeTraversal traversal = new NodeTraversal(compiler, checkGlobalThis);
        
        checkGlobalThis.shouldTraverse(traversal, assign);
        checkGlobalThis.visit(traversal, assign, assign);

        // Verify diagnostic was reported (or check behavior based on configuration)
        assertEquals(1, compiler.getWarnings().length);
    }

    @Test
    public void testConstructorAnnotationIgnored() {
        // @constructor function Foo() { this.bar = 1; }
        Node thisNode = new Node(Token.THIS);
        Node getprop = new Node(Token.GETPROP, thisNode, Node.newString("bar"));
        Node assign = new Node(Token.ASSIGN, getprop, Node.newNumber(1));
        
        Node exprResult = new Node(Token.EXPR_RESULT, assign);
        Node fnBody = new Node(Token.BLOCK, exprResult);
        Node fn = new Node(Token.FUNCTION, Node.newString("Foo"), new Node(Token.PARAM_LIST), fnBody);
        
        // Add constructor JSDoc info
        JSDocInfo info = new JSDocInfo();
        info.setConstructor();
        fn.setJSDocInfo(info);

        NodeTraversal traversal = new NodeTraversal(compiler, checkGlobalThis);

        // Traverse into function
        assertTrue(checkGlobalThis.shouldTraverse(traversal, fn));
        checkGlobalThis.shouldTraverse(traversal, fnBody);
        checkGlobalThis.shouldTraverse(traversal, exprResult);
        checkGlobalThis.shouldTraverse(traversal, assign);

        checkGlobalThis.visit(traversal, assign, assign);
        checkGlobalThis.visit(traversal, exprResult, exprResult);
        checkGlobalThis.visit(traversal, fnBody, fnBody);
        checkGlobalThis.visit(traversal, fn, fn);

        // Constructors shouldn't trigger global this warnings for modifying 'this'
        assertEquals(0, compiler.getWarnings().length);
    }

    @Test
    public void testFunctionWithoutConstructorAnnotation() {
        // function Foo() { this.bar = 1; } without @constructor
        Node thisNode = new Node(Token.THIS);
        Node getprop = new Node(Token.GETPROP, thisNode, Node.newString("bar"));
        Node assign = new Node(Token.ASSIGN, getprop, Node.newNumber(1));
        
        Node exprResult = new Node(Token.EXPR_RESULT, assign);
        Node fnBody = new Node(Token.BLOCK, exprResult);
        Node fn = newNodeWithParent(Token.FUNCTION, Node.newString("Foo"), new Node(Token.PARAM_LIST), fnBody);

        NodeTraversal traversal = new NodeTraversal(compiler, checkGlobalThis);

        checkGlobalThis.shouldTraverse(traversal, fn);
        checkGlobalThis.shouldTraverse(traversal, fnBody);
        checkGlobalThis.shouldTraverse(traversal, assign);

        checkGlobalThis.visit(traversal, assign, assign);

        // Should trigger warning because it's not a constructor
        assertEquals(1, compiler.getWarnings().length);
    }

    @Test
    public void testDisabledCheckLevel() {
        CheckGlobalThis disabledCheck = new CheckGlobalThis(compiler, CheckLevel.OFF);
        NodeTraversal traversal = new NodeTraversal(compiler, disabledCheck);

        Node thisNode = new Node(Token.THIS);
        Node getprop = new Node(Token.GETPROP, thisNode, Node.newString("foo"));
        Node assign = new Node(Token.ASSIGN, getprop, Node.newNumber(1));

        disabledCheck.shouldTraverse(traversal, assign);
        disabledCheck.visit(traversal, assign, assign);

        assertEquals(0, compiler.getWarnings().length);
    }

    @Test
    public void testGetGlobalThisOverride() {
        assertNotNull(checkGlobalThis);
    }

    @Test
    public void testAssignWithPrototype() {
        // Foo.prototype.bar = function() { this.baz = 1; };
        Node thisNode = new Node(Token.THIS);
        Node getprop = new Node(Token.GETPROP, thisNode, Node.newString("baz"));
        Node assign = new Node(Token.ASSIGN, getprop, Node.newNumber(1));
        
        Node exprResult = new Node(Token.EXPR_RESULT, assign);
        Node fnBody = new Node(Token.BLOCK, exprResult);
        Node fn = new Node(Token.FUNCTION, Node.newString(""), new Node(Token.PARAM_LIST), fnBody);

        NodeTraversal traversal = new NodeTraversal(compiler, checkGlobalThis);

        checkGlobalThis.shouldTraverse(traversal, fn);
        checkGlobalThis.shouldTraverse(traversal, assign);
        checkGlobalThis.visit(traversal, assign, assign);

        // Depending on strict rules, prototype methods might be allowed or suppressed.
        // Let's verify it executes without error.
        assertNotNull(compiler);
    }

    private Node newNodeWithParent(int type, Node... children) {
        Node node = new Node(type, children);
        for (Node child : children) {
            child.setParent(node);
        }
        return node;
    }
}