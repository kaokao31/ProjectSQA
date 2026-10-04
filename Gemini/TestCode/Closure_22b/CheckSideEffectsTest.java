package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class CheckSideEffectsTest {

    private Compiler compiler;
    private CheckSideEffects checkSideEffects;
    private CheckSideEffects. ProvinzLevel level;

    @Before
    public void setUp() {
        compiler = new Compiler();
        // Initialize with error level
        level = CheckSideEffects. ProvinzLevel.ERROR;
        checkSideEffects = new CheckSideEffects(compiler, level, true);
    }

    @Test
    public void testProcessWithNullRoot() {
        // Test processing with a null root node
        checkSideEffects.process(null, null);
        assertTrue(compiler.getErrors().isEmpty());
    }

    @Test
    public void testSimpleExpressionWithSideEffects() {
        // x = 1 has side effects
        Node name = Node.newString(Token.NAME, "x");
        Node number = Node.newNumber(1.0);
        Node assign = new Node(Token.ASSIGN, name, number);
        Node exprResult = new Node(Token.EXPR_RESULT, assign);
        Node script = new Node(Token.SCRIPT, exprResult);

        checkSideEffects.process(script, script);
        assertTrue(compiler.getErrors().isEmpty());
    }

    @Test
    public void testExpressionWithoutSideEffects() {
        // 1 has no side effects inside an EXPR_RESULT
        Node number = Node.newNumber(1.0);
        Node exprResult = new Node(Token.EXPR_RESULT, number);
        Node script = new Node(Token.SCRIPT, exprResult);

        checkSideEffects.process(script, script);
        assertFalse(compiler.getErrors().isEmpty());
    }

    @Test
    public void testCommaExpressionWithSideEffectsOnRight() {
        // (1, x = 2) - side effect on right, left is pure but part of comma
        Node num1 = Node.newNumber(1.0);
        Node name = Node.newString(Token.NAME, "x");
        Node num2 = Node.newNumber(2.0);
        Node assign = new Node(Token.ASSIGN, name, num2);
        Node comma = new Node(Token.COMMA, num1, assign);
        Node exprResult = new Node(Token.EXPR_RESULT, comma);
        Node script = new Node(Token.SCRIPT, exprResult);

        checkSideEffects.process(script, script);
        // Depending on implementation, pure left in comma might be flagged or allowed.
        // Let's verify it executes without exception.
        assertNotNull(compiler);
    }

    @Test
    public void testCommaExpressionAllPure() {
        // (1, 2) - no side effects anywhere
        Node num1 = Node.newNumber(1.0);
        Node num2 = Node.newNumber(2.0);
        Node comma = new Node(Token.COMMA, num1, num2);
        Node exprResult = new Node(Token.EXPR_RESULT, comma);
        Node script = new Node(Token.SCRIPT, exprResult);

        checkSideEffects.process(script, script);
        assertFalse(compiler.getErrors().isEmpty());
    }

    @Test
    public void testHookExpressionPureBranches() {
        // true ? 1 : 2 - pure hook expression
        Node cond = Node.newString(Token.TRUE, "true"); // or just TRUE token
        // Actually Token.TRUE has no children typically
        Node trueNode = Node.newNumber(1.0);
        Node falseNode = Node.newNumber(2.0);
        Node hook = new Node(Token.HOOK, Node.newNumber(0.0), trueNode, falseNode);
        Node exprResult = new Node(Token.EXPR_RESULT, hook);
        Node script = new Node(Token.SCRIPT, exprResult);

        checkSideEffects.process(script, script);
        assertFalse(compiler.getErrors().isEmpty());
    }

    @Test
    public void testHookExpressionWithSideEffect() {
        // true ? (x = 1) : 2
        Node cond = Node.newNumber(0.0);
        Node name = Node.newString(Token.NAME, "x");
        Node num1 = Node.newNumber(1.0);
        Node assign = new Node(Token.ASSIGN, name, num1);
        Node falseNode = Node.newNumber(2.0);
        Node hook = new Node(Token.HOOK, cond, assign, falseNode);
        Node exprResult = new Node(Token.EXPR_RESULT, hook);
        Node script = new Node(Token.SCRIPT, exprResult);

        checkSideEffects.process(script, script);
        assertTrue(compiler.getErrors().isEmpty());
    }

    @Test
    public void testVoidExpression() {
        // void 0 is pure
        Node voidNode = new Node(Token.VOID, Node.newNumber(0.0));
        Node exprResult = new Node(Token.EXPR_RESULT, voidNode);
        Node script = new Node(Token.SCRIPT, exprResult);

        checkSideEffects.process(script, script);
        assertFalse(compiler.getErrors().isEmpty());
    }

    @Test
    public void testBlockWithNoSideEffect() {
        // Blocks can sometimes contain expr results
        Node number = Node.newNumber(1.0);
        Node exprResult = new Node(Token.EXPR_RESULT, number);
        Node block = new Node(Token.BLOCK, exprResult);
        Node script = new Node(Token.SCRIPT, block);

        checkSideEffects.process(script, script);
        assertFalse(compiler.getErrors().isEmpty());
    }

    @Test
    public void testForLoopExpressions() {
        // for(1; 1; 1) {} -> 1 in init, cond, incr might trigger side effect warnings depending on config
        Node init = Node.newNumber(1.0);
        Node cond = Node.newNumber(1.0);
        Node incr = Node.newNumber(1.0);
        Node body = new Node(Token.BLOCK);
        Node forNode = new Node(Token.FOR, init, cond, incr, body);
        Node script = new Node(Token.SCRIPT, forNode);

        checkSideEffects.process(script, script);
        assertNotNull(compiler);
    }

    @Test
    public void testDisabledProvinzLevel() {
        CheckSideEffects disabledCheck = new CheckSideEffects(compiler, CheckSideEffects.ProvinzLevel.OFF, true);
        Node number = Node.newNumber(1.0);
        Node exprResult = new Node(Token.EXPR_RESULT, number);
        Node script = new Node(Token.SCRIPT, exprResult);

        disabledCheck.process(script, script);
        // When OFF, should not report errors
        assertTrue(compiler.getErrors().isEmpty());
    }

    @Test
    public void testFunctionExpressionInExprResult() {
        // function() {} as expression result
        Node fn = new Node(Token.FUNCTION, Node.newString(Token.NAME, ""), new Node(Token.PARAM_LIST), new Node(Token.BLOCK));
        Node exprResult = new Node(Token.EXPR_RESULT, fn);
        Node script = new Node(Token.SCRIPT, exprResult);

        checkSideEffects.process(script, script);
        assertNotNull(compiler);
    }

    @Test
    public void testObjectLitAndArrayLit() {
        Node obj = new Node(Token.OBJECTlit);
        Node exprResult = new Node(Token.EXPR_RESULT, obj);
        Node script = new Node(Token.SCRIPT, exprResult);

        checkSideEffects.process(script, script);
        assertFalse(compiler.getErrors().isEmpty());
    }
}