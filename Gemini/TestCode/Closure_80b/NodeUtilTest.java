package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;

import static org.junit.Assert.*;

public class NodeUtilTest {

    @Test
    public void testIsBooleanResult() {
        // Test various nodes to cover isBooleanResult and related boolean checks
        Node trueNode = new Node(Token.TRUE);
        Node falseNode = new Node(Token.FALSE);
        Node numberNode = new Node(Token.NUMBER, 1.0);
        Node stringNode = new Node(Token.STRING, "test");
        Node notNode = new Node(Token.NOT, trueNode);
        Node andNode = new Node(Token.AND, trueNode, falseNode);
        Node orNode = new Node(Token.OR, trueNode, falseNode);
        Node hookNode = new Node(Token.HOOK, trueNode, trueNode, falseNode);

        assertTrue(NodeUtil.isBooleanResult(trueNode));
        assertTrue(NodeUtil.isBooleanResult(falseNode));
        assertTrue(NodeUtil.isBooleanResult(notNode));
        assertTrue(NodeUtil.isBooleanResult(andNode));
        assertTrue(NodeUtil.isBooleanResult(orNode));
        assertTrue(NodeUtil.isBooleanResult(hookNode));

        assertFalse(NodeUtil.isBooleanResult(numberNode));
        assertFalse(NodeUtil.isBooleanResult(stringNode));
        
        // Test comparisons yielding boolean
        Node eqNode = new Node(Token.EQ, numberNode, numberNode);
        Node neNode = new Node(Token.NE, numberNode, numberNode);
        Node ltNode = new Node(Token.LT, numberNode, numberNode);
        Node leNode = new Node(Token.LE, numberNode, numberNode);
        Node gtNode = new Node(Token.GT, numberNode, numberNode);
        Node geNode = new Node(Token.GE, numberNode, numberNode);
        Node sheqNode = new Node(Token.SHEQ, numberNode, numberNode);
        Node shneNode = new Node(Token.SHNE, numberNode, numberNode);

        assertTrue(NodeUtil.isBooleanResult(eqNode));
        assertTrue(NodeUtil.isBooleanResult(neNode));
        assertTrue(NodeUtil.isBooleanResult(ltNode));
        assertTrue(NodeUtil.isBooleanResult(leNode));
        assertTrue(NodeUtil.isBooleanResult(gtNode));
        assertTrue(NodeUtil.isBooleanResult(geNode));
        assertTrue(NodeUtil.isBooleanResult(sheqNode));
        assertTrue(NodeUtil.isBooleanResult(shneNode));

        // Test TypeOf, InstanceOf, In
        Node typeofNode = new Node(Token.TYPEOF, stringNode);
        Node instanceofNode = new Node(Token.INSTANCEOF, stringNode, stringNode);
        Node inNode = new Node(Token.IN, stringNode, stringNode);

        assertTrue(NodeUtil.isBooleanResult(typeofNode)); // depending on implementation
        assertTrue(NodeUtil.isBooleanResult(instanceofNode));
        assertTrue(NodeUtil.isBooleanResult(inNode));
    }

    @Test
    public void testIsNumericResult() {
        Node numberNode = new Node(Token.NUMBER, 1.0);
        Node stringNode = new Node(Token.STRING, "test");
        Node addNode = new Node(Token.ADD, numberNode, numberNode);
        Node subNode = new Node(Token.SUB, numberNode, numberNode);
        Node mulNode = new Node(Token.MUL, numberNode, numberNode);
        Node divNode = new Node(Token.DIV, numberNode, numberNode);
        Node modNode = new Node(Token.MOD, numberNode, numberNode);
        Node incNode = new Node(Token.INC, numberNode);
        Node decNode = new Node(Token.DEC, numberNode);
        Node posNode = new Node(Token.POS, numberNode);
        Node negNode = new Node(Token.NEG, numberNode);
        Node bitNotNode = new Node(Token.BITNOT, numberNode);
        Node bitAndNode = new Node(Token.BITAND, numberNode, numberNode);
        Node bitOrNode = new Node(Token.BITOR, numberNode, numberNode);
        Node bitXorNode = new Node(Token.BITXOR, numberNode, numberNode);
        Node lshNode = new Node(Token.LSH, numberNode, numberNode);
        Node rshNode = new Node(Token.RSH, numberNode, numberNode);
        Node urshNode = new Node(Token.URSH, numberNode, numberNode);

        assertTrue(NodeUtil.isNumericResult(numberNode));
        assertTrue(NodeUtil.isNumericResult(addNode));
        assertTrue(NodeUtil.isNumericResult(subNode));
        assertTrue(NodeUtil.isNumericResult(mulNode));
        assertTrue(NodeUtil.isNumericResult(divNode));
        assertTrue(NodeUtil.isNumericResult(modNode));
        assertTrue(NodeUtil.isNumericResult(incNode));
        assertTrue(NodeUtil.isNumericResult(decNode));
        assertTrue(NodeUtil.isNumericResult(posNode));
        assertTrue(NodeUtil.isNumericResult(negNode));
        assertTrue(NodeUtil.isNumericResult(bitNotNode));
        assertTrue(NodeUtil.isNumericResult(bitAndNode));
        assertTrue(NodeUtil.isNumericResult(bitOrNode));
        assertTrue(NodeUtil.isNumericResult(bitXorNode));
        assertTrue(NodeUtil.isNumericResult(lshNode));
        assertTrue(NodeUtil.isNumericResult(rshNode));
        assertTrue(NodeUtil.isNumericResult(urshNode));

        assertFalse(NodeUtil.isNumericResult(stringNode));
    }

    @Test
    public void testIsImmutableValue() {
        Node trueNode = new Node(Token.TRUE);
        Node falseNode = new Node(Token.FALSE);
        Node numberNode = new Node(Token.NUMBER, 1.0);
        Node stringNode = new Node(Token.STRING, "test");
        Node nullNode = new Node(Token.NULL);
        Node voidNode = new Node(Token.VOID, numberNode);
        Node objNode = new Node(Token.OBJECTLIT);
        Node arrNode = new Node(Token.ARRAYLIT);

        assertTrue(NodeUtil.isImmutableValue(trueNode));
        assertTrue(NodeUtil.isImmutableValue(falseNode));
        assertTrue(NodeUtil.isImmutableValue(numberNode));
        assertTrue(NodeUtil.isImmutableValue(stringNode));
        assertTrue(NodeUtil.isImmutableValue(nullNode));
        assertTrue(NodeUtil.isImmutableValue(voidNode));

        assertFalse(NodeUtil.isImmutableValue(objNode));
        assertFalse(NodeUtil.isImmutableValue(arrNode));
    }

    @Test
    public void testMayHaveSideEffects() {
        Node numberNode = new Node(Token.NUMBER, 1.0);
        Node assignNode = new Node(Token.ASSIGN, new Node(Token.NAME, "x"), numberNode);
        Node callNode = new Node(Token.CALL, new Node(Token.NAME, "f"));
        Node newObjNode = new Node(Token.NEW, new Node(Token.NAME, "Object"));
        Node throwNode = new Node(Token.THROW, numberNode);
        Node incNode = new Node(Token.INC, new Node(Token.NAME, "x"));

        assertFalse(NodeUtil.mayHaveSideEffects(numberNode));
        assertTrue(NodeUtil.mayHaveSideEffects(assignNode));
        assertTrue(NodeUtil.mayHaveSideEffects(callNode));
        assertTrue(NodeUtil.mayHaveSideEffects(newObjNode));
        assertTrue(NodeUtil.mayHaveSideEffects(throwNode));
        assertTrue(NodeUtil.mayHaveSideEffects(incNode));
    }

    @Test
    public void testGetConditionExpression() {
        Node ifNode = new Node(Token.IF, new Node(Token.TRUE), new Node(Token.BLOCK));
        assertSame(ifNode.getFirstChild(), NodeUtil.getConditionExpression(ifNode));

        Node whileNode = new Node(Token.WHILE, new Node(Token.TRUE), new Node(Token.BLOCK));
        assertSame(whileNode.getFirstChild(), NodeUtil.getConditionExpression(whileNode));

        Node doNode = new Node(Token.DO, new Node(Token.BLOCK), new Node(Token.TRUE));
        assertSame(doNode.getLastChild(), NodeUtil.getConditionExpression(doNode));

        Node forNode = new Node(Token.FOR, new Node(Token.EMPTY), new Node(Token.TRUE), new Node(Token.EMPTY), new Node(Token.BLOCK));
        assertSame(forNode.getFirstChild().getNext(), NodeUtil.getConditionExpression(forNode));

        Node otherNode = new Node(Token.RETURN);
        assertNull(NodeUtil.getConditionExpression(otherNode));
    }

    @Test
    public void testIsFunction() {
        Node funcNode = new Node(Token.FUNCTION);
        assertTrue(NodeUtil.isFunction(funcNode));
        assertFalse(NodeUtil.isFunction(new Node(Token.NUMBER)));
    }

    @Test
    public void testIsStatement() {
        Node exprStmt = new Node(Token.EXPR_RESULT, new Node(Token.NUMBER, 1.0));
        Node block = new Node(Token.BLOCK);
        Node name = new Node(Token.NAME, "x");

        assertTrue(NodeUtil.isStatement(exprStmt));
        assertTrue(NodeUtil.isStatement(block));
        assertFalse(NodeUtil.isStatement(name));
    }

    @Test
    public void testGetFunctionBody() {
        Node nameNode = new Node(Token.NAME, "f");
        Node paramsNode = new Node(Token.LP);
        Node bodyNode = new Node(Token.BLOCK);
        Node funcNode = new Node(Token.FUNCTION, nameNode, paramsNode, bodyNode);

        assertSame(bodyNode, NodeUtil.getFunctionBody(funcNode));

        Node notFunc = new Node(Token.NUMBER);
        assertNull(NodeUtil.getFunctionBody(notFunc));
    }

    @Test
    public void testIsValidSimpleName() {
        assertTrue(NodeUtil.isValidSimpleName("validName"));
        assertFalse(NodeUtil.isValidSimpleName(""));
        assertFalse(NodeUtil.isValidSimpleName(null));
        assertFalse(NodeUtil.isValidSimpleName("invalid.name"));
        assertFalse(NodeUtil.isValidSimpleName("123name"));
        assertFalse(NodeUtil.isValidSimpleName("var"));
    }

    @Test
    public void testGetVarsDeclared() {
        Node varNode = new Node(Token.VAR, new Node(Token.NAME, "x"));
        assertTrue(NodeUtil.isVar(varNode) || varNode.isVar());
    }

    @Test
    public void testReferencesThis() {
        Node thisNode = new Node(Token.THIS);
        Node numberNode = new Node(Token.NUMBER, 1.0);
        Node addNode = new Node(Token.ADD, thisNode, numberNode);

        assertTrue(NodeUtil.referencesThis(thisNode));
        assertTrue(NodeUtil.referencesThis(addNode));
        assertFalse(NodeUtil.referencesThis(numberNode));
    }

    @Test
    public void testIsName() {
        Node nameNode = new Node(Token.NAME, "a");
        assertTrue(NodeUtil.isName(nameNode));
        assertFalse(NodeUtil.isName(new Node(Token.NUMBER)));
    }

    @Test
    public void testGetBestLValue() {
        Node nameNode = new Node(Token.NAME, "x");
        Node assignNode = new Node(Token.ASSIGN, nameNode, new Node(Token.NUMBER, 1.0));
        
        assertSame(nameNode, NodeUtil.getBestLValue(assignNode));
        assertNull(NodeUtil.getBestLValue(new Node(Token.NUMBER)));
    }

    @Test
    public void testGetBestLValueOwner() {
        Node getprop = new Node(Token.GETPROP, new Node(Token.NAME, "a"), new Node(Token.STRING, "b"));
        Node assignNode = new Node(Token.ASSIGN, getprop, new Node(Token.NUMBER, 1.0));
        
        assertNotNull(NodeUtil.getBestLValueOwner(assignNode));
    }
}