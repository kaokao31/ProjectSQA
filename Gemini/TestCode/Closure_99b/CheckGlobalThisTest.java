package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class CheckGlobalThisTest {

    private Compiler compiler;
    private CheckGlobalThis checker;
    private CheckLevel defaultLevel;

    @Before
    public void setUp() {
        compiler = new Compiler();
        defaultLevel = CheckLevel.WARNING;
        checker = new CheckGlobalThis(compiler, defaultLevel);
    }

    @Test
    public void testShouldTraverseNullRoot() {
        NodeTraversal traversal = new NodeTraversal(compiler, checker);
        boolean result = checker.shouldTraverse(traversal, null, null);
        assertFalse(result);
    }

    @Test
    public void testShouldTraverseFunctionNode() {
        NodeTraversal traversal = new NodeTraversal(compiler, checker);
        Node functionNode = new Node(Token.FUNCTION);
        boolean result = checker.shouldTraverse(traversal, null, functionNode);
        assertTrue(result);
    }

    @Test
    public void testShouldTraverseAssignmentToPrototype() {
        NodeTraversal traversal = new NodeTraversal(compiler, checker);
        
        // A.prototype = ...
        Node qName = Node.newString(Token.NAME, "A");
        Node getProp = Node.newString(Token.GETPROP, "prototype");
        getProp.addChildToFront(qName);
        
        Node assign = new Node(Token.ASSIGN, getProp, Node.newNumber(1));
        
        boolean result = checker.shouldTraverse(traversal, null, assign);
        assertFalse(result);
    }

    @Test
    public void testShouldTraverseAssignmentToPrototypeMember() {
        NodeTraversal traversal = new NodeTraversal(compiler, checker);
        
        // A.prototype.foo = ...
        Node qName = Node.newString(Token.NAME, "A");
        Node getPropProto = Node.newString(Token.GETPROP, "prototype");
        getPropProto.addChildToFront(qName);
        
        Node getPropFoo = Node.newString(Token.GETPROP, "foo");
        getPropFoo.addChildToFront(getPropProto);
        
        Node assign = new Node(Token.ASSIGN, getPropFoo, Node.newNumber(1));
        
        boolean result = checker.shouldTraverse(traversal, null, assign);
        assertFalse(result);
    }

    @Test
    public void testShouldTraverseNonAssignment() {
        NodeTraversal traversal = new NodeTraversal(compiler, checker);
        Node node = new Node(Token.EXPR_RESULT, Node.newNumber(1));
        boolean result = checker.shouldTraverse(traversal, null, node);
        assertTrue(result);
    }

    @Test
    public void testVisitWithoutFunction() {
        NodeTraversal traversal = new NodeTraversal(compiler, checker);
        Node thisNode = new Node(Token.THIS);
        
        // Visiting outside of any function scope
        checker.visit(traversal, thisNode, null);
        // Should not throw and should not report since it's not inside a function
    }

    @Test
    public void testVisitInsideFunctionWithoutGlobalThisAnnotation() {
        NodeTraversal traversal = new NodeTraversal(compiler, checker);
        
        // function f() { this.x = 1; }
        Node nameNode = Node.newString(Token.NAME, "f");
        Node paramList = new Node(Token.LP);
        Node assign = new Node(Token.ASSIGN, 
                Node.newString(Token.GETPROP, "x"), 
                Node.newNumber(1));
        Node block = new Node(Token.BLOCK, assign);
        Node functionNode = new Node(Token.FUNCTION, nameNode, paramList, block);
        
        // Push function onto scope/traversal stack via shouldTraverse
        checker.shouldTraverse(traversal, null, functionNode);
        
        Node thisNode = new Node(Token.THIS);
        Node getProp = Node.newString(Token.GETPROP, "x");
        getProp.addChildToFront(thisNode);
        
        checker.visit(traversal, thisNode, block);
        // Should report a warning for global this usage
    }

    @Test
    public void testVisitInsideFunctionWithGlobalThisAnnotation() {
        NodeTraversal traversal = new NodeTraversal(compiler, checker);
        
        // /** @constructor */ function f() { this.x = 1; }
        Node nameNode = Node.newString(Token.NAME, "f");
        Node paramList = new Node(Token.LP);
        Node assign = new Node(Token.ASSIGN, 
                Node.newString(Token.GETPROP, "x"), 
                Node.newNumber(1));
        Node block = new Node(Token.BLOCK, assign);
        Node functionNode = new Node(Token.FUNCTION, nameNode, paramList, block);
        
        JSDocInfo docInfo = new JSDocInfo();
        docInfo.setConstructor(true);
        functionNode.setJSDocInfo(docInfo);
        
        checker.shouldTraverse(traversal, null, functionNode);
        
        Node thisNode = new Node(Token.THIS);
        checker.visit(traversal, thisNode, block);
        // Constructor, so 'this' is allowed
    }

    @Test
    public void testGetGlobalThisLevel() {
        CheckGlobalThis check = new CheckGlobalThis(compiler, CheckLevel.ERROR);
        assertEquals(CheckLevel.ERROR, check.level);
    }
}