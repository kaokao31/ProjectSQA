package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.jscomp.NodeUtil;

/**
 * Test suite for NodeUtil with focus on isValidDefineValue and other utility methods.
 * Targets the bug where TRUE, FALSE, NULL literals were not recognized as valid define values.
 */
public class NodeUtilTest {

    // Helper to create a simple NAME node
    private Node nameNode(String name) {
        return new Node(Token.NAME, name);
    }

    // Helper to create a STRING node
    private Node stringNode(String value) {
        return new Node(Token.STRING, value);
    }

    // Helper to create a NUMBER node
    private Node numberNode(double value) {
        return new Node(Token.NUMBER, value);
    }

    // Helper to create a call node (e.g., foo())
    private Node callNode(Node target, Node... args) {
        Node call = new Node(Token.CALL, target);
        for (Node arg : args) {
            call.addChildToBack(arg);
        }
        return call;
    }

    // Helper to create a GETPROP node (e.g., a.b)
    private Node getPropNode(Node object, String property) {
        return new Node(Token.GETPROP, object, new Node(Token.STRING, property));
    }

    // ===================== Tests for isValidDefineValue =====================

    @Test
    public void testIsValidDefineValue_name() {
        // Simple name like "x" should be valid
        Node name = nameNode("x");
        assertTrue("NAME node should be valid define value", NodeUtil.isValidDefineValue(name, false));
    }

    @Test
    public void testIsValidDefineValue_string() {
        Node str = stringNode("hello");
        assertTrue("STRING node should be valid define value", NodeUtil.isValidDefineValue(str, false));
    }

    @Test
    public void testIsValidDefineValue_number() {
        Node num = numberNode(42.0);
        assertTrue("NUMBER node should be valid define value", NodeUtil.isValidDefineValue(num, false));
    }

    @Test
    public void testIsValidDefineValue_negativeNumber() {
        Node neg = numberNode(-1.0);
        assertTrue("Negative NUMBER should be valid define value", NodeUtil.isValidDefineValue(neg, false));
    }

    @Test
    public void testIsValidDefineValue_zero() {
        Node zero = numberNode(0.0);
        assertTrue("Zero NUMBER should be valid define value", NodeUtil.isValidDefineValue(zero, false));
    }

    @Test
    public void testIsValidDefineValue_true() {
        Node trueNode = new Node(Token.TRUE);
        assertTrue("TRUE node should be valid define value", NodeUtil.isValidDefineValue(trueNode, false));
    }

    @Test
    public void testIsValidDefineValue_false() {
        Node falseNode = new Node(Token.FALSE);
        assertTrue("FALSE node should be valid define value", NodeUtil.isValidDefineValue(falseNode, false));
    }

    @Test
    public void testIsValidDefineValue_null() {
        Node nullNode = new Node(Token.NULL);
        assertTrue("NULL node should be valid define value", NodeUtil.isValidDefineValue(nullNode, false));
    }

    @Test
    public void testIsValidDefineValue_qualifiedName() {
        // a.b.c is a GETPROP chain, which is valid as a define value if checkRecursive is false
        Node prop = getPropNode(
            getPropNode(nameNode("a"), "b"),
            "c"
        );
        assertTrue("Qualified name GETPROP should be valid define value", NodeUtil.isValidDefineValue(prop, false));
    }

    @Test
    public void testIsValidDefineValue_call() {
        // A function call should not be a valid define value
        Node call = callNode(nameNode("foo"));
        assertFalse("CALL node should not be valid define value", NodeUtil.isValidDefineValue(call, false));
    }

    @Test
    public void testIsValidDefineValue_callWithArguments() {
        Node call = callNode(nameNode("foo"), stringNode("bar"));
        assertFalse("CALL with arguments should not be valid define value", NodeUtil.isValidDefineValue(call, false));
    }

    @Test
    public void testIsValidDefineValue_recursiveCheckTrue() {
        // When checkRecursive is true, a compound expression like a.b should still be valid
        Node prop = getPropNode(nameNode("a"), "b");
        assertTrue("Qualified name with recursive check true should be valid", NodeUtil.isValidDefineValue(prop, true));
    }

    @Test
    public void testIsValidDefineValue_recursiveCheckTrueInvalid() {
        // A call nested inside a GETPROP? Actually CALL is not a valid define value, so nested call should be invalid
        Node call = callNode(nameNode("foo"));
        Node prop = getPropNode(call, "x");
        // This is a property access on a call result, which is not a valid define value
        assertFalse("Property of call should not be valid define value with recursive check", NodeUtil.isValidDefineValue(prop, true));
    }

    @Test
    public void testIsValidDefineValue_emptyString() {
        Node empty = stringNode("");
        assertTrue("Empty STRING should be valid define value", NodeUtil.isValidDefineValue(empty, false));
    }

    @Test
    public void testIsValidDefineValue_specialNumber() {
        Node nan = numberNode(Double.NaN);
        assertTrue("NaN NUMBER should be valid define value (number literals are valid)", NodeUtil.isValidDefineValue(nan, false));
    }

    // ===================== Tests for other NodeUtil methods =====================

    @Test
    public void testIsLiteralValue_number() {
        assertTrue(NodeUtil.isLiteralValue(numberNode(3.14)));
    }

    @Test
    public void testIsLiteralValue_string() {
        assertTrue(NodeUtil.isLiteralValue(stringNode("test")));
    }

    @Test
    public void testIsLiteralValue_boolean() {
        assertTrue(NodeUtil.isLiteralValue(new Node(Token.TRUE)));
        assertTrue(NodeUtil.isLiteralValue(new Node(Token.FALSE)));
    }

    @Test
    public void testIsLiteralValue_null() {
        assertTrue(NodeUtil.isLiteralValue(new Node(Token.NULL)));
    }

    @Test
    public void testIsLiteralValue_name() {
        assertFalse("NAME node is not a literal", NodeUtil.isLiteralValue(nameNode("x")));
    }

    @Test
    public void testIsLiteralValue_getProp() {
        assertFalse("GETPROP node is not a literal", NodeUtil.isLiteralValue(getPropNode(nameNode("a"), "b")));
    }

    @Test
    public void testIsCallTo() {
        Node callFoo = callNode(nameNode("foo"));
        assertTrue(NodeUtil.isCallTo(callFoo, "foo"));
        assertFalse(NodeUtil.isCallTo(callFoo, "bar"));
        // Non-call node should return false
        assertFalse(NodeUtil.isCallTo(nameNode("foo"), "foo"));
    }

    @Test
    public void testIsGet() {
        assertTrue(NodeUtil.isGet(getPropNode(nameNode("a"), "b")));
        assertFalse(NodeUtil.isGet(nameNode("a")));
        assertFalse(NodeUtil.isGet(new Node(Token.STRING, "x")));
    }

    @Test
    public void testIsName() {
        assertTrue(NodeUtil.isName(nameNode("foo")));
        assertFalse(NodeUtil.isName(stringNode("foo")));
        assertFalse(NodeUtil.isName(new Node(Token.TRUE)));
    }

    @Test
    public void testIsSimpleOperator_number() {
        // Number node is not an operator
        assertFalse(NodeUtil.isSimpleOperator(numberNode(1)));
    }

    @Test
    public void testIsSimpleOperator_add() {
        Node add = new Node(Token.ADD, numberNode(1), numberNode(2));
        assertTrue(NodeUtil.isSimpleOperator(add));
    }

    @Test
    public void testIsSimpleOperator_sub() {
        Node sub = new Node(Token.SUB, numberNode(1), numberNode(2));
        assertTrue(NodeUtil.isSimpleOperator(sub));
    }

    @Test
    public void testIsSimpleOperator_not() {
        Node not = new Node(Token.NOT, new Node(Token.TRUE));
        assertTrue(NodeUtil.isSimpleOperator(not));
    }

    @Test
    public void testIsSimpleOperator_call() {
        assertFalse(NodeUtil.isSimpleOperator(callNode(nameNode("foo"))));
    }

    @Test
    public void testMayHaveSideEffects_name() {
        // A simple name reference has no side effects
        Node name = nameNode("x");
        assertFalse(NodeUtil.mayHaveSideEffects(name, null));
    }

    @Test
    public void testMayHaveSideEffects_call() {
        Node call = callNode(nameNode("foo"));
        assertTrue("Function call may have side effects", NodeUtil.mayHaveSideEffects(call, null));
    }

    @Test
    public void testMayHaveSideEffects_getProp() {
        // Property access on a name is generally safe (no side effects)
        Node prop = getPropNode(nameNode("x"), "y");
        assertFalse(NodeUtil.mayHaveSideEffects(prop, null));
    }

    @Test
    public void testMayHaveSideEffects_getPropOnCall() {
        // Property access on a call result may have side effects because the call itself has side effects
        Node call = callNode(nameNode("getObject"));
        Node prop = getPropNode(call, "property");
        assertTrue(NodeUtil.mayHaveSideEffects(prop, null));
    }

    @Test
    public void testContainsCall() {
        Node call = callNode(nameNode("f"));
        assertTrue(NodeUtil.containsCall(call));
        Node name = nameNode("x");
        assertFalse(NodeUtil.containsCall(name));
        // Nested call
        Node outer = new Node(Token.ADD, callNode(nameNode("g")), stringNode("hello"));
        assertTrue(NodeUtil.containsCall(outer));
    }

    @Test
    public void testIsFunctionExpression() {
        Node function = new Node(Token.FUNCTION);
        assertTrue(NodeUtil.isFunctionExpression(function));
        assertFalse(NodeUtil.isFunctionExpression(nameNode("x")));
    }

    @Test
    public void testIsObjectLitKey() {
        // Assume object literal key is of type STRING_KEY
        Node key = new Node(Token.STRING_KEY, "key");
        assertTrue(NodeUtil.isObjectLitKey(key, key.getParent()));
        // Actually STRING_KEY may not be directly under object literal; need proper context.
        // This test may be simplified.
    }

    @Test
    public void testGetPrototypeClassName() {
        // For GETPROP like "a.prototype"
        Node prop = getPropNode(nameNode("Array"), "prototype");
        assertEquals("Array", NodeUtil.getPrototypeClassName(prop));
        Node prop2 = getPropNode(getPropNode(nameNode("a"), "b"), "prototype");
        assertEquals("a.b", NodeUtil.getPrototypeClassName(prop2));
        // Not a prototype reference
        Node plainProp = getPropNode(nameNode("x"), "y");
        assertNull(NodeUtil.getPrototypeClassName(plainProp));
    }
}