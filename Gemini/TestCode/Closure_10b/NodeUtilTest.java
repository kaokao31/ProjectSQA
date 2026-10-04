package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;

import static org.junit.Assert.*;

public class NodeUtilTest {

    @Test
    public void testMayBeStringBasic() {
        // Test MayBeString with various node types
        Node stringNode = Node.newString("hello");
        assertTrue(NodeUtil.mayBeString(stringNode));

        Node numberNode = Node.newNumber(123);
        assertFalse(NodeUtil.mayBeString(numberNode));

        Node addNode = new Node(Token.ADD, Node.newString("a"), Node.newString("b"));
        assertTrue(NodeUtil.mayBeString(addNode));

        Node qmarkNode = new Node(Token.QMARK, Node.newNumber(1), Node.newString("a"), Node.newString("b"));
        assertTrue(NodeUtil.mayBeString(qmarkNode));
    }

    @Test
    public void testMayBeStringEmptyOrNull() {
        assertFalse(NodeUtil.mayBeString(null));
        
        Node emptyBlock = new Node(Token.BLOCK);
        assertFalse(NodeUtil.mayBeString(emptyBlock));
    }

    @Test
    public void testAllResultsMatch() {
        Node trueNode = new Node(Token.TRUE);
        Node falseNode = new Node(Token.FALSE);

        Node andNode = new Node(Token.AND, trueNode, trueNode);
        assertTrue(NodeUtil.allResultsMatch(andNode, NodeUtil.BooleanPredicate.TRUE));

        Node orNode = new Node(Token.OR, falseNode, falseNode);
        assertTrue(NodeUtil.allResultsMatch(orNode, NodeUtil.BooleanPredicate.FALSE));
    }

    @Test
    public void testIsImmutableValue() {
        assertTrue(NodeUtil.isImmutableValue(Node.newNumber(5)));
        assertTrue(NodeUtil.isImmutableValue(Node.newString("test")));
        assertTrue(NodeUtil.isImmutableValue(new Node(Token.TRUE)));
        assertTrue(NodeUtil.isImmutableValue(new Node(Token.FALSE)));
        assertTrue(NodeUtil.isImmutableValue(new Node(Token.NULL)));

        assertFalse(NodeUtil.isImmutableValue(new Node(Token.OBJECTLIT)));
    }

    @Test
    public void testGetBooleanValue() {
        Node trueNode = new Node(Token.TRUE);
        assertEquals(Boolean.TRUE, NodeUtil.getBooleanValue(trueNode));

        Node falseNode = new Node(Token.FALSE);
        assertEquals(Boolean.FALSE, NodeUtil.getBooleanValue(falseNode));

        Node numNode = Node.newNumber(1.0);
        assertEquals(Boolean.TRUE, NodeUtil.getBooleanValue(numNode));

        Node zeroNumNode = Node.newNumber(0.0);
        assertEquals(Boolean.FALSE, NodeUtil.getBooleanValue(zeroNumNode));

        Node strNode = Node.newString("abc");
        assertEquals(Boolean.TRUE, NodeUtil.getBooleanValue(strNode));

        Node emptyStrNode = Node.newString("");
        assertEquals(Boolean.FALSE, NodeUtil.getBooleanValue(emptyStrNode));

        Node nullNode = new Node(Token.NULL);
        assertEquals(Boolean.FALSE, NodeUtil.getBooleanValue(nullNode));

        Node unknownNode = new Node(Token.BLOCK);
        assertNull(NodeUtil.getBooleanValue(unknownNode));
    }

    @Test
    public void testIsNaN() {
        Node nanNode = Node.newNumber(Double.NaN);
        assertTrue(NodeUtil.isNaN(nanNode));

        Node normalNum = Node.newNumber(10.0);
        assertFalse(NodeUtil.isNaN(normalNum));

        Node strNode = Node.newString("NaN");
        assertFalse(NodeUtil.isNaN(strNode));
    }

    @Test
    public void testNodeOps() {
        Node nameNode = Node.newString(Token.NAME, "a");
        assertTrue(NodeUtil.isName(nameNode));

        Node assignNode = new Node(Token.ASSIGN, nameNode, Node.newNumber(1));
        assertTrue(NodeUtil.isAssignment(assignNode));

        Node varNode = new Node(Token.VAR, nameNode);
        assertTrue(NodeUtil.isVar(varNode));
    }
}