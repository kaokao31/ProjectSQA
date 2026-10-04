package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class CheckGlobalThisTest {

    private Compiler compiler;
    private CheckGlobalThis checker;
    private ComposeWarningsGuard warningsGuard;

    @Before
    public void setUp() {
        compiler = new Compiler();
        // Configure compiler options minimally for AST traversal
        CompilerOptions options = new CompilerOptions();
        compiler.initOptions(options);
        
        // CheckGlobalThis takes CheckLevel as third parameter, 
        // e.g., CheckLevel.WARNING or CheckLevel.ERROR
        checker = new CheckGlobalThis(compiler, compiler.getCodingConvention(), CheckLevel.WARNING);
    }

    @Test
    public void testNullTraversal() {
        // Passing null or empty node should not throw exception
        Node root = null;
        NodeTraversal traversal = new NodeTraversal(compiler, checker);
        checker.visit(traversal, null, null);
    }

    @Test
    public void testSimpleFunctionWithoutGlobalThis() {
        // function f() { var x = 1; }
        Node block = new Node(Token.BLOCK);
        Node name = Node.newString(Token.NAME, "f");
        Node funcParams = new Node(Token.PARAM_LIST);
        Node funcBody = new Node(Token.BLOCK);
        Node func = new Node(Token.FUNCTION, name, funcParams, funcBody);
        block.addChildrenToBack(func);

        NodeTraversal traversal = new NodeTraversal(compiler, checker);
        checker.visit(traversal, func, block);
    }

    @Test
    public void testGlobalThisUsage() {
        // this.a = 1; at the global scope (should trigger warning if configured)
        Node getProp = new Node(Token.GETPROP, new Node(Token.THIS), Node.newString(Token.STRING, "a"));
        Node assign = new Node(Token.ASSIGN, getProp, Node.newNumber(1.0));
        Node expr = new Node(Token.EXPR_RESULT, assign);

        NodeTraversal traversal = new NodeTraversal(compiler, checker);
        // Should traverse and check
        checker.visit(traversal, expr, null);
    }

    @Test
    public void testPrototypeAssignment() {
        // Foo.prototype.bar = function() { this.x = 2; };
        // Should not trigger global this warning because it's on a prototype.
        Node prototypeNode = new Node(Token.GETPROP, 
                Node.newString(Token.NAME, "Foo"), 
                Node.newString(Token.STRING, "prototype"));
        Node getProp = new Node(Token.GETPROP, prototypeNode, Node.newString(Token.STRING, "bar"));
        
        Node funcBody = new Node(Token.BLOCK);
        Node thisAssign = new Node(Token.ASSIGN, 
                new Node(Token.GETPROP, new Node(Token.THIS), Node.newString(Token.STRING, "x")), 
                Node.newNumber(2.0));
        funcBody.addChildrenToBack(new Node(Token.EXPR_RESULT, thisAssign));

        Node func = new Node(Token.FUNCTION, Node.newString(Token.NAME, ""), new Node(Token.PARAM_LIST), funcBody);
        Node assign = new Node(Token.ASSIGN, getProp, func);
        Node expr = new Node(Token.EXPR_RESULT, assign);

        NodeTraversal traversal = new NodeTraversal(compiler, checker);
        // Should handle prototype structures without warning
        checker.visit(traversal, expr, null);
    }

    @Test
    public void testShouldTraverse() {
        NodeTraversal traversal = new NodeTraversal(compiler, checker);
        Node node = new Node(Token.BLOCK);
        boolean result = checker.shouldTraverse(traversal, node, null);
        assertTrue(result);
    }
}