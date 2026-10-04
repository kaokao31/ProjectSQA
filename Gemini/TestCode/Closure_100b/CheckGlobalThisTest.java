package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class CheckGlobalThisTest {

    private AbstractCompiler compiler;
    private CheckGlobalThis checkGlobalThis;
    private CodingConvention convention;

    @Before
    public void setUp() {
        compiler = new Compiler();
        convention = new DefaultCodingConvention();
        checkGlobalThis = new CheckGlobalThis(compiler, CheckLevel.WARNING);
    }

    @Test
    public void testShouldTraverseNullNode() {
        // Should handle null or empty traversal gracefully
        NodeTraversal traversal = new NodeTraversal(compiler, checkGlobalThis);
        boolean result = checkGlobalThis.shouldTraverse(traversal, null, null);
        assertFalse(result);
    }

    @Test
    public void testShouldTraverseNonScriptOrModule() {
        NodeTraversal traversal = new NodeTraversal(compiler, checkGlobalThis);
        Node node = new Node(Token.EXPR_RESULT);
        assertTrue(checkGlobalThis.shouldTraverse(traversal, node, null));
    }

    @Test
    public void testShouldTraverseScriptWithoutGlobalThisCheck() {
        compiler.-1 = CheckLevel.OFF;
        CheckGlobalThis checker = new CheckGlobalThis(compiler, CheckLevel.OFF);
        NodeTraversal traversal = new NodeTraversal(compiler, checker);
        Node script = new Node(Token.SCRIPT);
        assertFalse(checker.shouldTraverse(traversal, script, null));
    }

    @Test
    public void testShouldTraverseScriptWithGlobalThisCheck() {
        CheckGlobalThis checker = new CheckGlobalThis(compiler, CheckLevel.WARNING);
        NodeTraversal traversal = new NodeTraversal(compiler, checker);
        Node script = new Node(Token.SCRIPT);
        assertTrue(checker.shouldTraverse(traversal, script, null));
    }

    @Test
    public void testVisitFunctionWithoutThisAlias() {
        NodeTraversal traversal = new NodeTraversal(compiler, checkGlobalThis);
        Node function = Node.newFunction("f", Node.newToken(Token.NAME, "f"), new Node(Token.PARAM_LIST), new Node(Token.BLOCK));
        
        // Should not throw and should handle normal function nodes
        checkGlobalThis.visit(traversal, function, null);
    }

    @Test
    public void testVisitGetPropGlobalThis() {
        // this.foo = 1; at the global scope (outside constructor/prototype)
        NodeTraversal traversal = new NodeTraversal(compiler, checkGlobalThis);
        
        Node assign = new Node(Token.ASSIGN,
                Node.newString(Token.GETPROP, "this.foo"),
                Node.newNumber(1));
        
        Node expr = new Node(Token.EXPR_RESULT, assign);
        Node script = new Node(Token.SCRIPT, expr);
        
        traversal.traverse(script);
    }

    @Test
    public void testVisitWithPrototype() {
        // Foo.prototype.bar = function() { this.baz = 2; };
        NodeTraversal traversal = new NodeTraversal(compiler, checkGlobalThis);

        Node getProp = new Node(Token.GETPROP,
                new Node(Token.GETPROP, Node.newString(Token.NAME, "Foo"), Node.newString(Token.STRING, "prototype")),
                Node.newString(Token.STRING, "bar"));
        
        Node fn = Node.newFunction("", Node.newToken(Token.NAME, ""), new Node(Token.PARAM_LIST), new Node(Token.BLOCK));
        Node assign = new Node(Token.ASSIGN, getProp, fn);
        Node expr = new Node(Token.EXPR_RESULT, assign);
        Node script = new Node(Token.SCRIPT, expr);

        traversal.traverse(script);
    }

    @Test
    public void testGetGlobalThisLevel() {
        assertEquals(CheckLevel.WARNING, checkGlobalThis.getGlobalThisLevel());
    }

    @Test
    public void testConstructorAnnotation() {
        // Testing function with @constructor annotation
        NodeTraversal traversal = new NodeTraversal(compiler, checkGlobalThis);
        
        Node fn = Node.newFunction("MyClass", Node.newToken(Token.NAME, "MyClass"), new Node(Token.PARAM_LIST), new Node(Token.BLOCK));
        JSDocInfo info = new JSDocInfo();
        info.setConstructor(true);
        fn.setJSDocInfo(info);

        Node expr = new Node(Token.EXPR_RESULT, fn);
        Node script = new Node(Token.SCRIPT, expr);

        traversal.traverse(script);
    }
}