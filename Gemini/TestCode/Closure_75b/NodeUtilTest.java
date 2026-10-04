package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;

import static org.junit.Assert.*;

public class NodeUtilTest {

    @Test
    public void testGetBooleanValueNumber() {
        // Test numeric literal values
        Node zero = Node.newNumber(0.0);
        assertEquals(Boolean.FALSE, NodeUtil.getBooleanValue(zero));

        Node positive = Node.newNumber(5.5);
        assertEquals(Boolean.TRUE, NodeUtil.getBooleanValue(positive));

        Node negative = Node.newNumber(-1.0);
        assertEquals(Boolean.TRUE, NodeUtil.getBooleanValue(negative));

        Node nan = Node.newNumber(Double.NaN);
        assertEquals(Boolean.FALSE, NodeUtil.getBooleanValue(nan));
    }

    @Test
    public void testGetBooleanValueString() {
        // Test string literal values
        Node emptyStr = Node.newString("");
        assertEquals(Boolean.FALSE, NodeUtil.getBooleanValue(emptyStr));

        Node nonEmptyStr = Node.newString("hello");
        assertEquals(Boolean.TRUE, NodeUtil.getBooleanValue(nonEmptyStr));
    }

    @Test
    public void testGetBooleanValueBoolean() {
        // Test boolean literal values
        Node trueNode = Node.newBoolean(true);
        assertEquals(Boolean.TRUE, NodeUtil.getBooleanValue(trueNode));

        Node falseNode = Node.newBoolean(false);
        assertEquals(Boolean.FALSE, NodeUtil.getBooleanValue(falseNode));
    }

    @Test
    public void testGetBooleanValueNullAndVoid() {
        // Test NULL and VOID
        Node nullNode = new Node(Token.NULL);
        assertEquals(Boolean.FALSE, NodeUtil.getBooleanValue(nullNode));

        Node voidNode = new Node(Token.VOID);
        assertEquals(Boolean.FALSE, NodeUtil.getBooleanValue(voidNode));
    }

    @Test
    public void testGetBooleanValueArrayAndObject() {
        // Test ARRAY and OBJECT literals
        Node arrayNode = new Node(Token.ARRAYLIT);
        assertEquals(Boolean.TRUE, NodeUtil.getBooleanValue(arrayNode));

        Node objNode = new Node(Token.OBJECTLIT);
        assertEquals(Boolean.TRUE, NodeUtil.getBooleanValue(objNode));
    }

    @Test
    public void testGetBooleanValueOther() {
        // Test an unknown/unsupported node type for getBooleanValue
        Node otherNode = new Node(Token.DEBUGGER);
        assertNull(NodeUtil.getBooleanValue(otherNode));
    }

    @Test
    public void testIsImmutableValue() {
        assertTrue(NodeUtil.isImmutableValue(Node.newNumber(1.0)));
        assertTrue(NodeUtil.isImmutableValue(Node.newString("test")));
        assertTrue(NodeUtil.isImmutableValue(Node.newBoolean(true)));
        
        Node nullNode = new Node(Token.NULL);
        assertTrue(NodeUtil.isImmutableValue(nullNode));

        Node notImmutable = new Node(Token.NAME);
        assertFalse(NodeUtil.isImmutableValue(notImmutable));
    }

    @Test
    public void testMayHaveSideEffects() {
        Node numberNode = Node.newNumber(10);
        assertFalse(NodeUtil.mayHaveSideEffects(numberNode));

        Node assignNode = new Node(Token.ASSIGN);
        assertTrue(NodeUtil.mayHaveSideEffects(assignNode));
    }

    @Test
    public void testIsLiteralValue() {
        Node numberNode = Node.newNumber(5);
        assertTrue(NodeUtil.isLiteralValue(numberNode, true));

        Node nameNode = Node.newString(Token.NAME, "a");
        assertFalse(NodeUtil.isLiteralValue(nameNode, true));
    }
}