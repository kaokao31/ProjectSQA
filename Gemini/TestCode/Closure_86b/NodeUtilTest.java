package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;

import static org.junit.Assert.*;

public class NodeUtilTest {

    @Test
    public void testIsImmutableValue() {
        // Test various nodes with isImmutableValue
        Node stringNode = Node.newString("hello");
        Node numberNode = Node.newNumber(10.0);
        Node trueNode = new Node(Token.TRUE);
        Node falseNode = new Node(Token.FALSE);
        Node nullNode = new Node(Token.NULL);
        Node voidNode = new Node(Token.VOID, Node.newNumber(0));
        Node nameNode = Node.newString(Token.NAME, "undefined");
        nameNode.putString("undefined");
        
        assertTrue(NodeUtil.isImmutableValue(stringNode));
        assertTrue(NodeUtil.isImmutableValue(numberNode));
        assertTrue(NodeUtil.isImmutableValue(trueNode));
        assertTrue(NodeUtil.isImmutableValue(falseNode));
        assertTrue(NodeUtil.isImmutableValue(nullNode));
        assertTrue(NodeUtil.isImmutableValue(voidNode));
        assertTrue(NodeUtil.isImmutableValue(nameNode));

        Node mutableNode = new Node(Token.OBJECTLIT);
        assertFalse(NodeUtil.isImmutableValue(mutableNode));
    }

    @Test
    public void testIsExpressionNode() {
        Node exprNode = new Node(Token.EXPR_RESULT);
        assertTrue(NodeUtil.isExpressionNode(exprNode));

        Node otherNode = new Node(Token.BLOCK);
        assertFalse(NodeUtil.isExpressionNode(otherNode));
    }

    @Test
    public void testIsStatement() {
        Node exprNode = new Node(Token.EXPR_RESULT);
        assertTrue(NodeUtil.isStatement(exprNode));

        Node blockNode = new Node(Token.BLOCK);
        assertTrue(NodeUtil.isStatement(blockNode));

        Node addNode = new Node(Token.ADD, Node.newNumber(1), Node.newNumber(2));
        assertFalse(NodeUtil.isStatement(addNode));
    }

    @Test
    public void testEvaluatesToLocalValue() {
        // Check evaluatesToLocalValue branches
        Node immNode = Node.newString("test");
        assertTrue(NodeUtil.evaluatesToLocalValue(immNode));

        Node thisNode = new Node(Token.THIS);
        assertTrue(NodeUtil.evaluatesToLocalValue(thisNode));

        Node callNode = new Node(Token.CALL);
        assertFalse(NodeUtil.evaluatesToLocalValue(callNode));

        Node functionNode = new Node(Token.FUNCTION);
        assertTrue(NodeUtil.evaluatesToLocalValue(functionNode));

        Node unknownNode = new Node(Token.DEBUGGER);
        assertFalse(NodeUtil.evaluatesToLocalValue(unknownNode));
    }

    @Test
    public void testMayHaveSideEffects() {
        Node numberNode = Node.newNumber(5);
        assertFalse(NodeUtil.mayHaveSideEffects(numberNode));

        Node assignNode = new Node(Token.ASSIGN, Node.newString(Token.NAME, "x"), Node.newNumber(1));
        assertTrue(NodeUtil.mayHaveSideEffects(assignNode));
        
        Node throwNode = new Node(Token.THROW, Node.newString("error"));
        assertTrue(NodeUtil.mayHaveSideEffects(throwNode));
    }

    @Test
    public void testIsLiteralValue() {
        Node litNum = Node.newNumber(123);
        assertTrue(NodeUtil.isLiteralValue(litNum));

        Node arrayLit = new Node(Token.ARRAYLIT, Node.newNumber(1));
        assertTrue(NodeUtil.isLiteralValue(arrayLit));

        Node notLit = new Node(Token.NAME, "a");
        assertFalse(NodeUtil.isLiteralValue(notLit));
    }

    @Test
    public void testIsFunction() {
        Node funcNode = new Node(Token.FUNCTION);
        assertTrue(NodeUtil.isFunction(funcNode));

        Node notFunc = new Node(Token.BLOCK);
        assertFalse(NodeUtil.isFunction(notFunc));
    }

    @Test
    public void testGetFunctionBody() {
        Node name = Node.newString(Token.NAME, "f");
        Node params = new Node(Token.LP);
        Node body = new Node(Token.BLOCK);
        Node funcNode = new Node(Token.FUNCTION, name, params, body);

        assertSame(body, NodeUtil.getFunctionBody(funcNode));

        Node invalidFunc = new Node(Token.FUNCTION);
        assertNull(NodeUtil.getFunctionBody(invalidFunc));
    }

    @Test
    public void testIsVar() {
        Node varNode = new Node(Token.VAR);
        assertTrue(NodeUtil.isVar(varNode));

        Node notVar = new Node(Token.EXPR_RESULT);
        assertFalse(NodeUtil.isVar(notVar));
    }

    @Test
    public void testGetConditionExpression() {
        Node cond = Node.newNumber(1);
        Node ifNode = new Node(Token.IF, cond, new Node(Token.BLOCK));
        assertSame(cond, NodeUtil.getConditionExpression(ifNode));

        Node whileNode = new Node(Token.WHILE, cond, new Node(Token.BLOCK));
        assertSame(cond, NodeUtil.getConditionExpression(whileNode));

        Node notConditional = new Node(Token.BLOCK);
        assertNull(NodeUtil.getConditionExpression(notConditional));
    }

    @Test
    public void testIsCall() {
        Node callNode = new Node(Token.CALL);
        assertTrue(NodeUtil.isCall(callNode));

        Node notCall = new Node(Token.NAME, "x");
        assertFalse(NodeUtil.isCall(notCall));
    }

    @Test
    public void testReferencesThis() {
        Node thisNode = new Node(Token.THIS);
        assertTrue(NodeUtil.referencesThis(thisNode));

        Node otherNode = new Node(Token.NAME, "x");
        assertFalse(NodeUtil.referencesThis(otherNode));
    }

    @Test
    public void testIsValidDefineValue() {
        Node num = Node.newNumber(10);
        assertTrue(NodeUtil.isValidDefineValue(num, null));

        Node str = Node.newString("abc");
        assertTrue(NodeUtil.isValidDefineValue(str, null));

        Node name = Node.newString(Token.NAME, "x");
        assertFalse(NodeUtil.isValidDefineValue(name, null));
    }

    @Test
    public void testIsName() {
        Node nameNode = new Node(Token.NAME, "x");
        assertTrue(NodeUtil.isName(nameNode));

        Node notName = Node.newNumber(1);
        assertFalse(NodeUtil.isName(notName));
    }

    @Test
    public void testIsGet() {
        Node getProp = new Node(Token.GETPROP, Node.newString("a"), Node.newString("b"));
        assertTrue(NodeUtil.isGet(getProp));

        Node getElem = new Node(Token.GETELEM, Node.newString("a"), Node.newString("b"));
        assertTrue(NodeUtil.isGet(getElem));

        Node notGet = new Node(Token.NAME, "a");
        assertFalse(NodeUtil.isGet(notGet));
    }

    @Test
    public void testGetRootOfQualifiedName() {
        Node nameNode = Node.newString(Token.NAME, "a");
        Node getProp = new Node(Token.GETPROP, nameNode, Node.newString("b"));
        
        assertSame(nameNode, NodeUtil.getRootOfQualifiedName(getProp));
        assertSame(nameNode, NodeUtil.getRootOfQualifiedName(nameNode));
        
        Node numNode = Node.newNumber(1);
        assertNull(NodeUtil.getRootOfQualifiedName(numNode));
    }

    @Test
    public void testIsLocalValueBug86Target() {
        // Specifically targeting Closure 86 edge cases around side effects and local values
        Node nameNode = Node.newString(Token.NAME, "a");
        assertFalse(NodeUtil.isLocalValue(nameNode));

        Node immNode = Node.newNumber(5);
        assertTrue(NodeUtil.isLocalValue(immNode));

        Node callNode = new Node(Token.NEW, Node.newString(Token.NAME, "Object"));
        // Depending on strict evaluation, new calls might or might not be local values.
        // Let's test standard call / new evaluation paths.
        boolean localNew = NodeUtil.isLocalValue(callNode);
        assertFalse(localNew);
    }
}