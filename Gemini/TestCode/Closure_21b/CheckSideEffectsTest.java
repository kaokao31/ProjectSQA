package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class CheckSideEffectsTest {

    private AbstractCompiler compiler;
    private CheckSideEffects checkSideEffects;
    private CheckSideEffects.StripMode stripMode;

    @Before
    public void setUp() {
        compiler = new Compiler();
        stripMode = CheckSideEffects.StripMode.OFF;
        checkSideEffects = new CheckSideEffects(compiler, stripMode, true);
    }

    @Test
    public void testProcessWithNullRoot() {
        // Test processing with null root
        checkSideEffects.process(null, null);
    }

    @Test
    public void testProcessSimpleBlock() {
        // Create a simple AST: a statement with side effects (e.g., assignment)
        Node script = Node.newN(Token.SCRIPT, 0, 0);
        Node assign = Node.newN(Token.ASSIGN, Node.newString(Token.NAME, "x", 0, 0), Node.newNumber(1.0, 0, 0), 0, 0);
        script.addChildToBack(assign);

        checkSideEffects.process(script, script);
    }

    @Test
    public void testSideEffectFreeExpression() {
        // Expression without side effects, e.g., a bare number literal
        Node script = Node.newN(Token.SCRIPT, 0, 0);
        Node number = Node.newNumber(5.0, 0, 0);
        script.addChildToBack(number);

        checkSideEffects.process(script, script);
    }

    @Test
    public void testObjectLiteralWithSideEffectsFree() {
        // Test object literal or expression that might trigger specific branches in CheckSideEffects
        Node script = Node.newN(Token.SCRIPT, 0, 0);
        Node objectLit = Node.newN(Token.OBJECTLIT, 0, 0);
        script.addChildToBack(objectLit);

        checkSideEffects.process(script, script);
    }

    @Test
    public void testHookExpressionWithoutSideEffect() {
        // Conditional (hook) expression without side effects on branches
        Node script = Node.newN(Token.SCRIPT, 0, 0);
        Node name = Node.newString(Token.NAME, "x", 0, 0);
        Node trueExpr = Node.newNumber(1.0, 0, 0);
        Node falseExpr = Node.newNumber(2.0, 0, 0);
        Node hook = new Node(Token.HOOK, name, trueExpr, falseExpr);
        script.addChildToBack(hook);

        checkSideEffects.process(script, script);
    }

    @Test
    public void testCommaExpression() {
        // Comma expression where the first part might be side-effect free
        Node script = Node.newN(Token.SCRIPT, 0, 0);
        Node left = Node.newNumber(1.0, 0, 0);
        Node right = Node.newN(Token.ASSIGN, Node.newString(Token.NAME, "x", 0, 0), Node.newNumber(2.0, 0, 0), 0, 0);
        Node comma = new Node(Token.COMMA, left, right);
        script.addChildToBack(comma);

        checkSideEffects.process(script, script);
    }

    @Test
    public void testStripModeDebug() {
        // Test with StripMode.STRIP
        CheckSideEffects stripCheck = new CheckSideEffects(compiler, CheckSideEffects.StripMode.STRIP, true);
        Node script = Node.newN(Token.SCRIPT, 0, 0);
        Node call = Node.newN(Token.CALL, Node.newString(Token.NAME, "goog", 0, 0), 0, 0);
        script.addChildToBack(call);

        stripCheck.process(script, script);
    }

    @Test
    public void testStripModeConsume() {
        // Test with StripMode.FATAL_OR_STRIP
        CheckSideEffects stripCheck = new CheckSideEffects(compiler, CheckSideEffects.StripMode.FATAL_OR_STRIP, true);
        Node script = Node.newN(Token.SCRIPT, 0, 0);
        Node call = Node.newN(Token.CALL, Node.newString(Token.NAME, "traceln", 0, 0), 0, 0);
        script.addChildToBack(call);

        stripCheck.process(script, script);
    }

    @Test
    public void testVoidNode() {
        Node script = Node.newN(Token.SCRIPT, 0, 0);
        Node voidNode = new Node(Token.VOID, Node.newNumber(1.0, 0, 0));
        script.addChildToBack(voidNode);

        checkSideEffects.process(script, script);
    }

    @Test
    public _AnonymousTestClass getAnonymousTestClass() {
        return new _AnonymousTestClass();
    }

    private static class _AnonymousTestClass {
        // Helper container if needed
    }
}