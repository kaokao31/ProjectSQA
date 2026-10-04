package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Comprehensive JUnit 4 test suite for NodeUtil.
 * Targets maximum code coverage and fault detection, including
 * the Defects4J Closure-10 bug scenario where mayEffectMutableState
 * incorrectly handles goog.bind calls with side-effectful arguments.
 */
public class NodeUtilTest {

    private Node scriptRoot;
    private Node emptyBlock;

    @Before
    public void setUp() {
        scriptRoot = new Node(Token.SCRIPT);
        emptyBlock = new Node(Token.BLOCK);
    }

    // ========== Helper methods ==========

    private Node createName(String name) {
        return Node.newString(Token.NAME, name);
    }

    private Node createString(String str) {
        return Node.newString(str);
    }

    private Node createNumber(double num) {
        return Node.newNumber(num);
    }

    private Node createCall(Node target, Node... args) {
        Node call = new Node(Token.CALL, target);
        for (Node arg : args) {
            call.addChildToBack(arg);
        }
        return call;
    }

    private Node createGetProp(Node obj, String prop) {
        return new Node(Token.GETPROP, obj, createString(prop));
    }

    private Node createGetElem(Node obj, Node index) {
        return new Node(Token.GETELEM, obj, index);
    }

    private Node createFunction(String name, Node body, Node... params) {
        Node fn = new Node(Token.FUNCTION);
        Node nameNode = (name == null) ? new Node(Token.NAME, "") : createName(name);
        fn.addChildToBack(nameNode);
        Node lp = new Node(Token.PARAM_LIST);
        for (Node p : params) {
            lp.addChildToBack(p);
        }
        fn.addChildToBack(lp);
        the.addChildToBack(body);
        return fn;
    }

    // ========== Tests for mayEffectMutableState ==========

    @Test
    public void testMayEffectMutableState_literal() {
        assertFalse(NodeUtil.mayEffectMutableState(createNumber(1.0), null));
        assertFalse(NodeUtil.mayEffectMutableState(createString("hello"), null));
        assertFalse(NodeUtil.mayEffectMutableState(createName("undefined"), null));
    }

    @Test
    public void testMayEffectMutableState_name() {
        assertFalse(NodeUtil.mayEffectMutableState(createName("x"), null));
    }

    @Test
    public void testMayEffectMutableState_fnCall_noSideEffects() {
        Node target = createGetProp(createName("Math"), "sin");
        Node call = createCall(target, createNumber(0.0));
        assertFalse(NodeUtil.mayEffectMutableState(call, null));
    }

    @Test
    public void testMayEffectMutableState_fnCall_withSideEffects() {
        // call to alert has side effects
        Node target = createName("alert");
        Node call = createCall(target, createString("test"));
        assertTrue(NodeUtil.mayEffectMutableState(call, null));
    }

    @Test
    public void testMayEffectMutableState_googBind_withSideEffectfulArg() {
        // Simulate goog.bind(fn, obj) where obj.getProperty has side effects
        Node fn = createFunction("f", emptyBlock);
        Node obj = createGetProp(createName("someObj"), "getter"); // getter might have side effects
        Node target = createGetProp(createName("goog"), "bind");
        Node call = createCall(target, fn, obj);
        // In Closure-10, this incorrectly returned false if the target was goog.bind
        assertTrue("goog.bind with side-effectful argument should return true",
                NodeUtil.mayEffectMutableState(call, null));
    }

    @Test
    public void testMayEffectMutableState_googPartial_withSideEffectfulArg() {
        Node fn = createFunction("f", emptyBlock);
        Node argWithSideEffect = createGetProp(createName("x"), "prop");
        Node target = createGetProp(createName("goog"), "partial");
        Node call = createCall(target, fn, argWithSideEffect);
        assertTrue("goog.partial with side-effectful argument should return true",
                NodeUtil.mayEffectMutableState(call, null));
    }

    @Test
    public void testMayEffectMutableState_newCall() {
        Node target = createName("Object");
        Node call = new Node(Token.NEW, target);
        assertTrue(NodeUtil.mayEffectMutableState(call, null));
    }

    @Test
    public void testMayEffectMutableState_delete() {
        Node delete = new Node(Token.DELPROP, createGetProp(createName("obj"), "prop"));
        assertTrue(NodeUtil.mayEffectMutableState(delete, null));
    }

    @Test
    public void testMayEffectMutableState_assign() {
        Node assign = new Node(Token.ASSIGN, createName("x"), createNumber(1));
        assertTrue(NodeUtil.mayEffectMutableState(assign, null));
    }

    @Test
    public void testMayEffectMutableState_incDec() {
        Node inc = new Node(Token.INC, createName("x"));
        assertTrue(NodeUtil.mayEffectMutableState(inc, null));
    }

    // ========== Tests for mayHaveSideEffects ==========

    @Test
    public void testMayHaveSideEffects_literal() {
        assertFalse(NodeUtil.mayHaveSideEffects(createNumber(1.0), null));
    }

    @Test
    public void testMayHaveSideEffects_name() {
        assertFalse(NodeUtil.mayHaveSideEffects(createName("x"), null));
    }

    @Test
    public void testMayHaveSideEffects_call() {
        Node call = createCall(createName("alert"), createString("msg"));
        assertTrue(NodeUtil.mayHaveSideEffects(call, null));
    }

    // ========== Tests for containsCall ==========

    @Test
    public void testContainsCall_noCall() {
        Node block = new Node(Token.BLOCK, createNumber(1), createName("x"));
        assertFalse(NodeUtil.containsCall(block));
    }

    @Test
    public void testContainsCall_directCall() {
        Node call = createCall(createName("f"));
        assertTrue(NodeUtil.containsCall(call));
    }

    @Test
    public void testContainsCall_nestedInExpr() {
        Node expr = new Node(Token.ADD, createCall(createName("f")), createNumber(2));
        assertTrue(NodeUtil.containsCall(expr));
    }

    // ========== Tests for isUnmodifiable ==========

    @Test
    public void testIsUnmodifiable_null() {
        assertFalse(NodeUtil.isUnmodifiable(null));
    }

    @Test
    public void testIsUnmodifiable_notUnmodifiable() {
        Node node = createName("x");
        assertFalse(NodeUtil.isUnmodifiable(node));
    }

    @Test
    public void testIsUnmodifiable_literal() {
        Node num = createNumber(3.14);
        assertTrue(NodeUtil.isUnmodifiable(num));
        Node str = createString("const");
        assertTrue(NodeUtil.isUnmodifiable(str));
        Node bool = new Node(Token.TRUE);
        assertTrue(NodeUtil.isUnmodifiable(bool));
        Node nil = new Node(Token.NULL);
        assertTrue(NodeUtil.isUnmodifiable(nil));
    }

    // ========== Tests for isFunction ==========

    @Test
    public void testIsFunction_function() {
        Node fn = createFunction("f", emptyBlock);
        assertTrue(NodeUtil.isFunction(fn));
    }

    @Test
    public void testIsFunction_nonFunction() {
        assertFalse(NodeUtil.isFunction(createName("x")));
    }

    // ========== Tests for isStatement ==========

    @Test
    public void testIsStatement_block() {
        assertTrue(NodeUtil.isStatement(emptyBlock));
    }

    @Test
    public void testIsStatement_exprResult() {
        Node expr = new Node(Token.EXPR_RESULT, createNumber(1));
        assertTrue(NodeUtil.isStatement(expr));
    }

    @Test
    public void testIsStatement_expression() {
        assertFalse(NodeUtil.isStatement(createNumber(1)));
    }

    // ========== Tests for isExpressionNode ==========

    @Test
    public void testIsExpressionNode_true() {
        Node expr = new Node(Token.EXPR_RESULT, createNumber(1));
        assertTrue(NodeUtil.isExpressionNode(expr));
    }

    @Test
    public void testIsExpressionNode_false() {
        assertFalse(NodeUtil.isExpressionNode(emptyBlock));
    }

    // ========== Tests for isReferenceTo ==========

    @Test
    public void testIsReferenceTo_nameMatch() {
        Node name = createName("foo");
        assertTrue(NodeUtil.isReferenceTo(name, "foo"));
    }

    @Test
    public void testIsReferenceTo_nameNoMatch() {
        Node name = createName("bar");
        assertFalse(NodeUtil.isReferenceTo(name, "foo"));
    }

    @Test
    public void testIsReferenceTo_qualifiedName() {
        Node getprop = createGetProp(createName("a"), "b");
        assertTrue(NodeUtil.isReferenceTo(getprop, "a.b"));
    }

    // ========== Tests for isLiteralValue ==========

    @Test
    public void testIsLiteralValue_number() {
        assertTrue(NodeUtil.isLiteralValue(createNumber(1.0), false));
    }

    @Test
    public void testIsLiteralValue_string() {
        assertTrue(NodeUtil.isLiteralValue(createString("hi"), false));
    }

    @Test
    public void testIsLiteralValue_array() {
        Node array = new Node(Token.ARRAYLIT);
        assertTrue(NodeUtil.isLiteralValue(array, false));
    }

    @Test
    public void testIsLiteralValue_object() {
        Node obj = new Node(Token.OBJECTLIT);
        assertTrue(NodeUtil.isLiteralValue(obj, false));
    }

    // ========== Tests for isImmutableValue ==========

    @Test
    public void testIsImmutableValue_number() {
        assertTrue(NodeUtil.isImmutableValue(createNumber(1.0)));
    }

    @Test
    public void testIsImmutableValue_array() {
        assertFalse(NodeUtil.isImmutableValue(new Node(Token.ARRAYLIT)));
    }

    // ========== Tests for effectivelyConstant ==========

    @Test
    public void testEffectivelyConstant_literal() {
        assertTrue(NodeUtil.effectivelyConstant(createNumber(1.0)));
    }

    @Test
    public void testEffectivelyConstant_function() {
        Node fn = createFunction("f", emptyBlock);
        assertTrue(NodeUtil.effectivelyConstant(fn));
    }

    // ========== Edge cases ==========

    @Test(expected = NullPointerException.class)
    public void testMayEffectMutableState_nullNode() {
        NodeUtil.mayEffectMutableState(null, null);
    }

    @Test(expected = NullPointerException.class)
    public void testMayHaveSideEffects_nullNode() {
        NodeUtil.mayHaveSideEffects(null, null);
    }

    @Test
    public void testIsNormalBlock_null() {
        assertFalse(NodeUtil.isNormalBlock(null));
    }

    @Test
    public void testIsNormalBlock_block() {
        assertTrue(NodeUtil.isNormalBlock(emptyBlock));
    }

    @Test
    public void testIsNormalBlock_script() {
        assertFalse(NodeUtil.isNormalBlock(scriptRoot));
    }

    @Test
    public void testIsNotNormalBlock() {
        Node notBlock = new Node(Token.IF);
        assertFalse(NodeUtil.isNormalBlock(notBlock));
    }
}