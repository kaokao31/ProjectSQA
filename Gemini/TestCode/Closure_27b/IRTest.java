package com.google.javascript.rhino;

import org.junit.Test;
import static org.junit.Assert.*;

public class IRTest {

    @Test
    public void testBasicNodeCreation() {
        Node nameNode = IR.name("testName");
        assertNotNull(nameNode);
        assertEquals(Token.NAME, nameNode.getType());
        assertEquals("testName", nameNode.getString());

        Node numberNode = IR.number(42.0);
        assertNotNull(numberNode);
        assertEquals(Token.NUMBER, numberNode.getType());
        assertEquals(42.0, numberNode.getDouble(), 0.001);

        Node stringNode = IR.string("hello");
        assertNotNull(stringNode);
        assertEquals(Token.STRING, stringNode.getType());
        assertEquals("hello", stringNode.getString());
    }

    @Test
    public void testBlockAndScript() {
        Node child = IR.empty();
        Node block = IR.block(child);
        assertNotNull(block);
        assertEquals(Token.BLOCK, block.getType());
        assertSame(child, block.getFirstChild());

        Node script = IR.script(child);
        assertNotNull(script);
        assertEquals(Token.SCRIPT, script.getType());
        assertSame(child, script.getFirstChild());
    }

    @Test
    public void testFunctionAndReturn() {
        Node name = IR.name("foo");
        Node paramList = IR.paramList();
        Node body = IR.block();
        Node func = IR.function(name, paramList, body);

        assertNotNull(func);
        assertEquals(Token.FUNCTION, func.getType());
        assertSame(name, func.getFirstChild());
        assertSame(paramList, name.getNext());
        assertSame(body, paramList.getNext());

        Node ret = IR.returnNode();
        assertNotNull(ret);
        assertEquals(Token.RETURN, ret.getType());
        assertNull(ret.getFirstChild());

        Node retVal = IR.returnNode(IR.number(1.0));
        assertNotNull(retVal);
        assertEquals(Token.RETURN, retVal.getType());
        assertNotNull(retVal.getFirstChild());
    }

    @Test
    public void testTryCatchFinally() {
        Node tryBody = IR.block();
        Node catchBlock = IR.block();
        Node finallyBlock = IR.block();

        Node tryNode = IR.tryCatch(tryBody, catchBlock);
        assertNotNull(tryNode);
        assertEquals(Token.TRY, tryNode.getType());
        assertSame(tryBody, tryNode.getFirstChild());
        assertSame(catchBlock, tryBody.getNext());

        Node tryFinallyNode = IR.tryFinally(tryBody, finallyBlock);
        assertNotNull(tryFinallyNode);
        assertEquals(Token.TRY, tryFinallyNode.getType());
        assertSame(tryBody, tryFinallyNode.getFirstChild());
        assertSame(finallyBlock, tryBody.getNext());
    }

    @Test
    public void testConditionalAndControlFlow() {
        Node cond = IR.name("cond");
        Node thenBody = IR.block();
        Node elseBody = IR.block();

        Node ifNode = IR.ifNode(cond, thenBody, elseBody);
        assertNotNull(ifNode);
        assertEquals(Token.IF, ifNode.getType());
        assertSame(cond, ifNode.getFirstChild());
        assertSame(thenBody, cond.getNext());
        assertSame(elseBody, thenBody.getNext());

        Node whileNode = IR.whileNode(cond, thenBody);
        assertNotNull(whileNode);
        assertEquals(Token.WHILE, whileNode.getType());

        Node doNode = IR.doNode(thenBody, cond);
        assertNotNull(doNode);
        assertEquals(Token.DO, doNode.getType());
    }

    @Test
    public void testSwitchAndCase() {
        Node expr = IR.name("expr");
        Node caseNode = IR.caseNode(IR.number(1.0), IR.block());
        assertNotNull(caseNode);
        assertEquals(Token.CASE, caseNode.getType());

        Node defaultNode = IR.defaultCase(IR.block());
        assertNotNull(defaultNode);
        assertEquals(Token.DEFAULT, defaultNode.getType());

        Node switchNode = IR.switchNode(expr, caseNode, defaultNode);
        assertNotNull(switchNode);
        assertEquals(Token.SWITCH, switchNode.getType());
    }

    @Test
    public void testObjectAndArrayLiterals() {
        Node obj = IR.objectLit();
        assertNotNull(obj);
        assertEquals(Token.OBJECTLIT, obj.getType());

        Node arr = IR.arrayLit();
        assertNotNull(arr);
        assertEquals(Token.ARRAYLIT, arr.getType());

        Node prop = IR.propGet(IR.name("a"), IR.string("b"));
        assertNotNull(prop);
        assertEquals(Token.GETPROP, prop.getType());

        Node elem = IR.elemGet(IR.name("a"), IR.number(0.0));
        assertNotNull(elem);
        assertEquals(Token.GETELEM, elem.getType());
    }

    @Test
    public void testCallAndNew() {
        Node target = IR.name("foo");
        Node call = IR.call(target, IR.number(1.0));
        assertNotNull(call);
        assertEquals(Token.CALL, call.getType());
        assertSame(target, call.getFirstChild());

        Node newExpr = IR.newnode(target, IR.string("bar"));
        assertNotNull(newExpr);
        assertEquals(Token.NEW, newExpr.getType());
        assertSame(target, newExpr.getFirstChild());
    }

    @Test
    public void testUnaryAndBinaryOperators() {
        Node name = IR.name("x");

        Node notNode = IR.not(name);
        assertEquals(Token.NOT, notNode.getType());

        Node negNode = IR.neg(name);
        assertEquals(Token.NEG, negNode.getType());

        Node posNode = IR.pos(name);
        assertEquals(Token.POS, posNode.getType());

        Node addNode = IR.add(name, IR.number(1.0));
        assertEquals(Token.ADD, addNode.getType());

        Node subNode = IR.sub(name, IR.number(1.0));
        assertEquals(Token.SUB, subNode.getType());

        Node mulNode = IR.mul(name, IR.number(2.0));
        assertEquals(Token.MUL, mulNode.getType());

        Node divNode = IR.div(name, IR.number(2.0));
        assertEquals(Token.DIV, divNode.getType());

        Node modNode = IR.mod(name, IR.number(2.0));
        assertEquals(Token.MOD, modNode.getType());
    }

    @Test
    public void testComparisonsAndLogic() {
        Node a = IR.name("a");
        Node b = IR.name("b");

        assertEquals(Token.EQ, IR.eq(a, b).getType());
        assertEquals(Token.NE, IR.ne(a, b).getType());
        assertEquals(Token.SHEQ, IR.sheq(a, b).getType());
        assertEquals(Token.SHNE, IR.shne(a, b).getType());
        assertEquals(Token.LT, IR.lt(a, b).getType());
        assertEquals(Token.LE, IR.le(a, b).getType());
        assertEquals(Token.GT, IR.gt(a, b).getType());
        assertEquals(Token.GE, IR.ge(a, b).getType());
        assertEquals(Token.AND, IR.and(a, b).getType());
        assertEquals(Token.OR, IR.or(a, b).getType());
    }

    @Test
    public void testAssignmentsAndUpdates() {
        Node lhs = IR.name("x");
        Node rhs = IR.number(5.0);

        assertEquals(Token.ASSIGN, IR.assign(lhs, rhs).getType());
        assertEquals(Token.ASSIGN_ADD, IR.assignAdd(lhs, rhs).getType());
        assertEquals(Token.ASSIGN_SUB, IR.assignSub(lhs, rhs).getType());
        assertEquals(Token.INC, IR.inc(lhs, Token.POST _INC).getType());
        assertEquals(Token.DEC, IR.dec(lhs, Token.PRE_DEC).getType());
    }

    @Test
    public void testDeclarationsAndMisc() {
        Node name = IR.name("x");
        assertEquals(Token.VAR, IR.var(name).getType());
        assertEquals(Token.CONST, IR.constNode(name).getType());
        assertEquals(Token.LET, IR.let(name).getType());
        assertEquals(Token.EXPR_RESULT, IR.expressionNode(name).getType());
        assertEquals(Token.BREAK, IR.breakNode().getType());
        assertEquals(Token.CONTINUE, IR.continueNode().getType());
        assertEquals(Token.DEBUGGER, IR.debugger().getType());
        assertEquals(Token.EMPTY, IR.empty().getType());
        assertEquals(Token.THIS, IR.thisNode().getType());
    }

    @Test
    public void testThrowAndLabels() {
        Node ex = IR.name("e");
        Node throwNode = IR.throwNode(ex);
        assertEquals(Token.THROW, throwNode.getType());
        assertSame(ex, throwNode.getFirstChild());

        Node labelNode = IR.label(IR.string("lbl"), IR.block());
        assertEquals(Token.LABEL, labelNode.getType());

        Node labelName = IR.labelName("lbl");
        assertEquals(Token.LABEL_NAME, labelName.getType());
    }
}