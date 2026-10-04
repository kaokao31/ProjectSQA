package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;

import static org.junit.Assert.*;

public class NodeUtilTest {

    @Test
    public void testIsImmutableValue() {
        // Test various nodes with isImmutableValue
        Node nullNode = null;
        assertFalse(NodeUtil.isImmutableValue(nullNode));

        Node trueNode = new Node(Token.TRUE);
        assertTrue(NodeUtil.isImmutableValue(trueNode));

        Node falseNode = new Node(Token.FALSE);
        assertTrue(NodeUtil.isImmutableValue(falseNode));

        Node numberNode = Node.newNumber(5.0);
        assertTrue(NodeUtil.isImmutableValue(numberNode));

        Node stringNode = Node.newString("test");
        assertTrue(NodeUtil.isImmutableValue(stringNode));

        Node nameNode = Node.newString(Token.NAME, "undefined");
        assertTrue(NodeUtil.isImmutableValue(nameNode));

        Node voidNode = new Node(Token.VOID, Node.newNumber(0));
        assertTrue(NodeUtil.isImmutableValue(voidNode));

        Node negNum = new Node(Token.NEG, Node.newNumber(5.0));
        assertTrue(NodeUtil.isImmutableValue(negNum));

        Node otherNode = new Node(Token.BLOCK);
        assertFalse(NodeUtil.isImmutableValue(otherNode));
    }

    @Test
    public void testMayHaveSideEffects() {
        assertFalse(NodeUtil.mayHaveSideEffects(null));

        Node numberNode = Node.newNumber(10);
        assertFalse(NodeUtil.mayHaveSideEffects(numberNode));

        Node assignNode = new Node(Token.ASSIGN, Node.newString(Token.NAME, "x"), Node.newNumber(1));
        assertTrue(NodeUtil.mayHaveSideEffects(assignNode));

        Node callNode = new Node(Token.CALL, Node.newString(Token.NAME, "alert"));
        assertTrue(NodeUtil.mayHaveSideEffects(callNode));
    }

    @Test
    public void testIsExpressionNode() {
        Node expr = new Node(Token.EXPR_RESULT);
        assertTrue(NodeUtil.isExpressionNode(expr));

        Node other = new Node(Token.BLOCK);
        assertFalse(NodeUtil.isExpressionNode(other));
        assertFalse(NodeUtil.isExpressionNode(null));
    }

    @Test
    public void testGetConditionExpression() {
        Node ifNode = new Node(Token.IF, Node.newString(Token.NAME, "cond"), new Node(Token.BLOCK));
        Node cond = NodeUtil.getConditionExpression(ifNode);
        assertNotNull(cond);
        assertEquals(Token.NAME, cond.getType());

        Node invalidNode = new Node(Token.BLOCK);
        assertNull(NodeUtil.getConditionExpression(invalidNode));
        assertNull(NodeUtil.getConditionExpression(null));
    }

    @Test
    public void testIsFunction() {
        Node fn = new Node(Token.FUNCTION);
        assertTrue(NodeUtil.isFunction(fn));

        assertFalse(NodeUtil.isFunction(null));
        assertFalse(NodeUtil.isFunction(new Node(Token.BLOCK)));
    }

    @Test
    public void testGetFunctionBody() {
        Node nameNode = Node.newString(Token.NAME, "f");
        Node paramList = new Node(Token.LP);
        Node bodyNode = new Node(Token.BLOCK);
        Node fn = new Node(Token.FUNCTION, nameNode, paramList, bodyNode);

        assertSame(bodyNode, NodeUtil.getFunctionBody(fn));
        assertNull(NodeUtil.getFunctionBody(null));
        assertNull(NodeUtil.getFunctionBody(new Node(Token.BLOCK)));
    }

    @Test
    public void testReferencesThis() {
        Node thisNode = new Node(Token.THIS);
        assertTrue(NodeUtil.referencesThis(thisNode));

        Node block = new Node(Token.BLOCK, thisNode);
        assertTrue(NodeUtil.referencesThis(block));

        Node safeBlock = new Node(Token.BLOCK, Node.newNumber(1));
        assertFalse(NodeUtil.referencesThis(safeBlock));
        assertFalse(NodeUtil.referencesThis(null));
    }

    @Test
    public void testIsControlStructure() {
        assertTrue(NodeUtil.isControlStructure(new Node(Token.IF)));
        assertTrue(NodeUtil.isControlStructure(new Node(Token.WHILE)));
        assertTrue(NodeUtil.isControlStructure(new Node(Token.FOR)));
        assertFalse(NodeUtil.isControlStructure(new Node(Token.ASSIGN)));
        assertFalse(NodeUtil.isControlStructure(null));
    }

    @Test
    public void testIsStatementBlock() {
        assertTrue(NodeUtil.isStatementBlock(new Node(Token.BLOCK)));
        assertFalse(NodeUtil.isStatementBlock(new Node(Token.EXPR_RESULT)));
        assertFalse(NodeUtil.isStatementBlock(null));
    }

    @Test
    public void testGetRootOfExpression() {
        Node leaf = Node.newNumber(1);
        Node parent = new Node(Token.EXPR_RESULT, leaf);
        assertSame(parent, NodeUtil.getRootOfExpression(leaf));
        assertNull(NodeUtil.getRootOfExpression(null));
    }

    @Test
    public void testIsLiteralValue() {
        assertTrue(NodeUtil.isLiteralValue(Node.newNumber(1)));
        assertTrue(NodeUtil.isLiteralValue(new Node(Token.TRUE)));
        assertFalse(NodeUtil.isLiteralValue(new Node(Token.NAME)));
        assertFalse(NodeUtil.isLiteralValue(null));
    }

    @Test
    public void testGetAssignedValue() {
        Node name = Node.newString(Token.NAME, "x");
        Node val = Node.newNumber(5);
        Node assign = new Node(Token.ASSIGN, name, val);

        assertSame(val, NodeUtil.getAssignedValue(assign));
        assertNull(NodeUtil.getAssignedValue(new Node(Token.EXPR_RESULT)));
        assertNull(NodeUtil.getAssignedValue(null));
    }

    @Test
    public void testIsName() {
        assertTrue(NodeUtil.isName(Node.newString(Token.NAME, "a")));
        assertFalse(NodeUtil.isName(Node.newNumber(1)));
        assertFalse(NodeUtil.isName(null));
    }

    @Test
    public void testIsVar() {
        assertTrue(NodeUtil.isVar(new Node(Token.VAR)));
        assertFalse(NodeUtil.isVar(Node.newNumber(1)));
        assertFalse(NodeUtil.isVar(null));
    }
}