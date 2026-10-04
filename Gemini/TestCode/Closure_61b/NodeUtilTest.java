package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;

import static org.junit.Assert.*;

public class NodeUtilTest {

    @Test
    public void testFunctionCallsAndSideEffects() {
        // Test function call side effect checks, particularly targeting Closure Bug 61
        // Bug 61 often involves function calls and whether they are considered side-effect free,
        // specifically Math methods and standard built-ins when function side effect analysis runs.
        
        // Math.sin(1) - typically considered side-effect free in Closure compiler
        Node mathName = Node.newString(Token.NAME, "Math");
        Node sinProp = Node.newString(Token.GETPROP, "sin");
        sinProp.addChildToFront(mathName);
        Node callNode = new Node(Token.CALL, sinProp, Node.newNumber(1.0));

        // Evaluate function side effects using NodeUtil
        // NodeUtil.functionCallHasSideEffects(Node callNode) or similar method depending on exact version
        boolean hasSideEffects = NodeUtil.functionCallHasSideEffects(callNode);
        // Math.sin is usually pure
        assertFalse(hasSideEffects);

        // Regular user-defined function call - should have side effects
        Node userFunc = Node.newString(Token.NAME, "foo");
        Node userCall = new Node(Token.CALL, userFunc);
        assertTrue(NodeUtil.functionCallHasSideEffects(userCall));
    }

    @Test
    public void testIsImmutableValue() {
        assertTrue(NodeUtil.isImmutableValue(Node.newNumber(5.0)));
        assertTrue(NodeUtil.isImmutableValue(Node.newString("hello")));
        assertTrue(NodeUtil.isImmutableValue(Node.newString(Token.TRUE, "true")));
        assertTrue(NodeUtil.isImmutableValue(Node.newString(Token.FALSE, "false")));
        assertTrue(NodeUtil.isImmutableValue(Node.newString(Token.NULL, "null")));
        
        Node mutable = new Node(Token.ARRAYLIT);
        assertFalse(NodeUtil.isImmutableValue(mutable));
    }

    @Test
    public void testMayHaveSideEffects() {
        Node numberNode = Node.newNumber(10);
        assertFalse(NodeUtil.mayHaveSideEffects(numberNode));

        Node assignNode = new Node(Token.ASSIGN, Node.newString(Token.NAME, "x"), Node.newNumber(1));
        assertTrue(NodeUtil.mayHaveSideEffects(assignNode));
    }

    @Test
    public void testIsLiteralValue() {
        Node litNum = Node.newNumber(123);
        assertTrue(NodeUtil.isLiteralValue(litNum, true));

        Node notLit = new Node(Token.NAME, "a");
        assertFalse(NodeUtil.isLiteralValue(notLit, true));
    }

    @Test
    public void testGetRootOfQualifiedName() {
        Node nameNode = Node.newString(Token.NAME, "a");
        Node getProp = Node.newString(Token.GETPROP, "b");
        getProp.addChildToFront(nameNode);

        assertSame(nameNode, NodeUtil.getRootOfQualifiedName(getProp));
        assertSame(nameNode, NodeUtil.getRootOfQualifiedName(nameNode));
        assertNull(NodeUtil.getRootOfQualifiedName(null));
    }

    @Test
    public void testIsFunction() {
        Node funcNode = new Node(Token.FUNCTION);
        assertTrue(NodeUtil.isFunction(funcNode));
        assertFalse(NodeUtil.isFunction(Node.newNumber(1)));
    }
}