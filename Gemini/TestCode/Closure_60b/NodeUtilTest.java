package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;

import static org.junit.Assert.*;

public class NodeUtilTest {

    @Test
    public void testIsImmutableValue() {
        // Test basic immutable values
        assertTrue(NodeUtil.isImmutableValue(Node.newNumber(5.0)));
        assertTrue(NodeUtil.isImmutableValue(Node.newString("test")));
        assertTrue(NodeUtil.isImmutableValue(Node.newString(Token.TRUE, "")));
        assertTrue(NodeUtil.isImmutableValue(Node.newString(Token.FALSE, "")));
        assertTrue(NodeUtil.isImmutableValue(Node.newString(Token.NULL, "")));

        // Test void 0
        Node voidNode = new Node(Token.VOID, Node.newNumber(0));
        assertTrue(NodeUtil.isImmutableValue(voidNode));

        // Test negative numbers (NEG expression)
        Node negNode = new Node(Token.NEG, Node.newNumber(5.0));
        assertTrue(NodeUtil.isImmutableValue(negNode));

        // Test non-immutable
        assertFalse(NodeUtil.isImmutableValue(Node.newString(Token.NAME, "a")));
        assertFalse(NodeUtil.isImmutableValue(new Node(Token.OBJECTlit)));
    }

    @Test
    public void testMayHaveSideEffects() {
        // Pure nodes
        Node numberNode = Node.newNumber(10);
        assertFalse(NodeUtil.mayHaveSideEffects(numberNode));

        // Side-effect nodes (Assignment, Call, New, etc.)
        Node assignNode = new Node(Token.ASSIGN, Node.newString(Token.NAME, "a"), Node.newNumber(1));
        assertTrue(NodeUtil.mayHaveSideEffects(assignNode));

        Node incNode = new Node(Token.INC, Node.newString(Token.NAME, "a"));
        assertTrue(NodeUtil.mayHaveSideEffects(incNode));

        Node callNode = new Node(Token.CALL, Node.newString(Token.NAME, "alert"));
        assertTrue(NodeUtil.mayHaveSideEffects(callNode));
    }

    @Test
    public void testIsLiteralValue() {
        assertTrue(NodeUtil.isLiteralValue(Node.newNumber(1.0)));
        assertTrue(NodeUtil.isLiteralValue(Node.newString("hello")));
        
        Node arrayLit = new Node(Token.ARRAYLIT, Node.newNumber(1), Node.newNumber(2));
        assertTrue(NodeUtil.isLiteralValue(arrayLit));

        Node objLit = new Node(Token.OBJECTlit);
        assertTrue(NodeUtil.isLiteralValue(objLit));

        Node nameNode = Node.newString(Token.NAME, "undefined");
        assertTrue(NodeUtil.isLiteralValue(nameNode)); // 'undefined' name is treated as literal in Closure context usually

        Node nonLiteral = new Node(Token.ADD, Node.newNumber(1), Node.newNumber(2));
        assertFalse(NodeUtil.isLiteralValue(nonLiteral));
    }

    @Test
    public void testGetRootOfExpression() {
        Node name = Node.newString(Token.NAME, "x");
        Node getprop = new Node(Token.GETPROP, name, Node.newString("y"));
        Node call = new Node(Token.CALL, getprop);

        assertSame(call, NodeUtil.getRootOfExpression(call));
        assertSame(getprop, NodeUtil.getRootOfExpression(getprop));
    }

    @Test
    public void testIsFunction() {
        Node funcNode = new Node(Token.FUNCTION);
        assertTrue(NodeUtil.isFunction(funcNode));
        assertFalse(NodeUtil.isFunction(Node.newNumber(1)));
    }

    @Test
    public void testGetFunctionName() {
        Node nameNode = Node.newString(Token.NAME, "myFunc");
        Node funcNode = new Node(Token.FUNCTION, nameNode, new Node(Token.PARAM_LIST), new Node(Token.BLOCK));
        
        assertEquals("myFunc", NodeUtil.getFunctionName(funcNode));

        Node anonymousFunc = new Node(Token.FUNCTION, Node.newString(Token.NAME, ""), new Node(Token.PARAM_LIST), new Node(Token.BLOCK));
        assertEquals("", NodeUtil.getFunctionName(anonymousFunc));
    }

    @Test
    public void testGetFunctionOrExpressionName() {
        Node nameNode = Node.newString(Token.NAME, "foo");
        assertEquals("foo", NodeUtil.getFunctionOrExpressionName(nameNode));

        Node getProp = new Node(Token.GETPROP, Node.newString(Token.NAME, "a"), Node.newString("b"));
        assertEquals("a.b", NodeUtil.getFunctionOrExpressionName(getProp));
    }

    @Test
    public void testIsStatement() {
        Node exprResult = new Node(Token.EXPR_RESULT, Node.newNumber(1));
        assertTrue(NodeUtil.isStatement(exprResult));

        Node name = Node.newString(Token.NAME, "x");
        assertFalse(NodeUtil.isStatement(name));
    }

    @Test
    public void testIsValidDefineValue() {
        Node num = Node.newNumber(5);
        assertTrue(NodeUtil.isValidDefineValue(num, null));

        Node str = Node.newString("abc");
        assertTrue(NodeUtil.isValidDefineValue(str, null));

        Node op = new Node(Token.ADD, Node.newNumber(1), Node.newNumber(2));
        assertTrue(NodeUtil.isValidDefineValue(op, null));

        Node sideEffect = new Node(Token.CALL, Node.newString(Token.NAME, "foo"));
        assertFalse(NodeUtil.isValidDefineValue(sideEffect, null));
    }

    @Test
    public void testBooleanCheckMethods() {
        Node trueNode = Node.newString(Token.TRUE, "");
        Node falseNode = Node.newString(Token.FALSE, "");

        assertTrue(NodeUtil.isBooleanLiteralValue(trueNode));
        assertTrue(NodeUtil.isBooleanLiteralValue(falseNode));
        assertFalse(NodeUtil.isBooleanLiteralValue(Node.newNumber(1)));
    }

    @Test
    public void testGetNumberValue() {
        Node numNode = Node.newNumber(42.0);
        assertEquals(Double.valueOf(42.0), NodeUtil.getNumberValue(numNode));

        Node trueNode = Node.newString(Token.TRUE, "");
        assertEquals(Double.valueOf(1.0), NodeUtil.getNumberValue(trueNode));

        Node falseNode = Node.newString(Token.FALSE, "");
        assertEquals(Double.valueOf(0.0), NodeUtil.getNumberValue(falseNode));

        Node nullNode = Node.newString(Token.NULL, "");
        assertEquals(Double.valueOf(0.0), NodeUtil.getNumberValue(nullNode));

        Node nonNum = Node.newString("not-a-number");
        assertNull(NodeUtil.getNumberValue(nonNum));
    }

    @Test
    public void testContainsType() {
        Node block = new Node(Token.BLOCK, Node.newNumber(1), Node.newString(Token.NAME, "a"));
        assertTrue(NodeUtil.containsType(block, Token.NAME));
        assertFalse(NodeUtil.containsType(block, Token.FUNCTION));
    }

    @Test
    public void testReferencesThis() {
        Node thisNode = new Node(Token.THIS);
        assertTrue(NodeUtil.referencesThis(thisNode));

        Node block = new Node(Token.BLOCK, thisNode);
        assertTrue(NodeUtil.referencesThis(block));

        Node other = Node.newNumber(1);
        assertFalse(NodeUtil.referencesThis(other));
    }

    @Test
    public void testIsControlStructure() {
        Node ifNode = new Node(Token.IF);
        assertTrue(NodeUtil.isControlStructure(ifNode));

        Node whileNode = new Node(Token.WHILE);
        assertTrue(NodeUtil.isControlStructure(whileNode));

        Node num = Node.newNumber(1);
        assertFalse(NodeUtil.isControlStructure(num));
    }

    @Test
    public void testIsLoop() {
        Node forNode = new Node(Token.FOR);
        assertTrue(NodeUtil.isLoop(forNode));

        Node whileNode = new Node(Token.WHILE);
        assertTrue(NodeUtil.isLoop(whileNode));

        Node doNode = new Node(Token.DO);
        assertTrue(NodeUtil.isLoop(doNode));

        Node ifNode = new Node(Token.IF);
        assertFalse(NodeUtil.isLoop(ifNode));
    }

    @Test
    public void testGetConditionExpression() {
        Node cond = Node.newString(Token.TRUE, "");
        Node ifNode = new Node(Token.IF, cond, new Node(Token.BLOCK));
        assertSame(cond, NodeUtil.getConditionExpression(ifNode));

        Node nonControl = Node.newNumber(1);
        assertNull(NodeUtil.getConditionExpression(nonControl));
    }

    @Test
    public void testInformationExtractorsAndHelpers() {
        Node name = Node.newString(Token.NAME, "arguments");
        assertTrue(NodeUtil.isName(name));
        assertFalse(NodeUtil.isNumber(name));

        Node num = Node.newNumber(1.0);
        assertTrue(NodeUtil.isNumber(num));

        Node str = Node.newString("str");
        assertTrue(NodeUtil.isString(str));
    }

    @Test
    public void testGetImpureParameterIndex() {
        Node funcNode = new Node(Token.FUNCTION, 
                Node.newString(Token.NAME, "f"), 
                new Node(Token.PARAM_LIST, Node.newString(Token.NAME, "a")), 
                new Node(Token.BLOCK));
        
        // No side effect call in params
        assertEquals(-1, NodeUtil.getImpureParameterIndex(funcNode));
    }
}