package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * JUnit 4 test suite for PeepholeFoldConstants.
 * Designed to achieve high branch and line coverage for Closure Bug 78.
 */
public class PeepholeFoldConstantsTest {

    private PeepholeFoldConstants foldConstants;
    private AbstractCompiler compiler;

    @Before
    public void setUp() {
        foldConstants = new PeepholeFoldConstants();
        compiler = new Compiler();
        foldConstants.beginTraversal(null);
    }

    @Test
    public void testUndefinedComparison() {
        // Test comparisons with undefined / void 0
        // e.g., x == undefined
        Node left = Node.newString(Token.NAME, "x");
        Node right = new Node(Token.VOID, Node.newNumber(0));
        Node parent = new Node(Token.EQ, left, right);

        Node result = foldConstants.optimizeSubtree(parent);
        assertNotNull(result);
    }

    @Test
    public void testFoldAdd() {
        // 1 + 2 => 3
        Node left = Node.newNumber(1);
        Node right = Node.newNumber(2);
        Node parent = new Node(Token.ADD, left, right);

        Node result = foldConstants.optimizeSubtree(parent);
        assertEquals(Token.NUMBER, result.getType());
        assertEquals(3.0, result.getDouble(), 0.001);
    }

    @Test
    public void testFoldStringAdd() {
        // "a" + "b" => "ab"
        Node left = Node.newString("a");
        Node right = Node.newString("b");
        Node parent = new Node(Token.ADD, left, right);

        Node result = foldConstants.optimizeSubtree(parent);
        assertEquals(Token.STRING, result.getType());
        assertEquals("ab", result.getString());
    }

    @Test
    public void testFoldBitwiseOr() {
        // 1 | 2 => 3
        Node left = Node.newNumber(1);
        Node right = Node.newNumber(2);
        Node parent = new Node(Token.BITOR, left, right);

        Node result = foldConstants.optimizeSubtree(parent);
        assertEquals(Token.NUMBER, result.getType());
        assertEquals(3.0, result.getDouble(), 0.001);
    }

    @Test
    public void testFoldBitwiseAnd() {
        // 3 & 1 => 1
        Node left = Node.newNumber(3);
        Node right = Node.newNumber(1);
        Node parent = new Node(Token.BITAND, left, right);

        Node result = foldConstants.optimizeSubtree(parent);
        assertEquals(Token.NUMBER, result.getType());
        assertEquals(1.0, result.getDouble(), 0.001);
    }

    @Test
    public void testFoldBitwiseXor() {
        // 3 ^ 1 => 2
        Node left = Node.newNumber(3);
        Node right = Node.newNumber(1);
        Node parent = new Node(Token.BITXOR, left, right);

        Node result = foldConstants.optimizeSubtree(parent);
        assertEquals(Token.NUMBER, result.getType());
        assertEquals(2.0, result.getDouble(), 0.001);
    }

    @Test
    public void testFoldShift() {
        // 1 << 2 => 4
        Node left = Node.newNumber(1);
        Node right = Node.newNumber(2);
        Node parent = new Node(Token.LSH, left, right);

        Node result = foldConstants.optimizeSubtree(parent);
        assertEquals(Token.NUMBER, result.getType());
        assertEquals(4.0, result.getDouble(), 0.001);
    }

    @Test
    public void testFoldArithmetic() {
        // 5 - 2 => 3
        Node left = Node.newNumber(5);
        Node right = Node.newNumber(2);
        Node parent = new Node(Token.SUB, left, right);

        Node result = foldConstants.optimizeSubtree(parent);
        assertEquals(Token.NUMBER, result.getType());
        assertEquals(3.0, result.getDouble(), 0.001);
    }

    @Test
    public void testFoldMulDiv() {
        // 6 / 2 => 3
        Node left = Node.newNumber(6);
        Node right = Node.newNumber(2);
        Node parent = new Node(Token.DIV, left, right);

        Node result = foldConstants.optimizeSubtree(parent);
        assertEquals(Token.NUMBER, result.getType());
        assertEquals(3.0, result.getDouble(), 0.001);
    }

    @Test
    public void testDivisionByZero() {
        // 5 / 0 -> should handle gracefully (NaN or Infinity or un-foldable depending on implementation)
        Node left = Node.newNumber(5);
        Node right = Node.newNumber(0);
        Node parent = new Node(Token.DIV, left, right);

        Node result = foldConstants.optimizeSubtree(parent);
        assertNotNull(result);
    }

    @Test
    public void testMod() {
        // 5 % 2 => 1
        Node left = Node.newNumber(5);
        Node right = Node.newNumber(2);
        Node parent = new Node(Token.MOD, left, right);

        Node result = foldConstants.optimizeSubtree(parent);
        assertEquals(Token.NUMBER, result.getType());
        assertEquals(1.0, result.getDouble(), 0.001);
    }

    @Test
    public void testUnaryOperations() {
        // -5
        Node child = Node.newNumber(5);
        Node parent = new Node(Token.NEG, child);

        Node result = foldConstants.optimizeSubtree(parent);
        assertEquals(Token.NUMBER, result.getType());
        assertEquals(-5.0, result.getDouble(), 0.001);
    }

    @Test
    public void testNotOperations() {
        // !true => false
        Node child = Node.newTrue();
        Node parent = new Node(Token.NOT, child);

        Node result = foldConstants.optimizeSubtree(parent);
        assertEquals(Token.FALSE, result.getType());
    }

    @Test
    public void testTypeOf() {
        // typeof 1 => "number"
        Node child = Node.newNumber(1);
        Node parent = new Node(Token.TYPEOF, child);

        Node result = foldConstants.optimizeSubtree(parent);
        assertEquals(Token.STRING, result.getType());
        assertEquals("number", result.getString());
    }

    @Test
    public void testVoidNode() {
        // void 0
        Node child = Node.newNumber(0);
        Node parent = new Node(Token.VOID, child);

        Node result = foldConstants.optimizeSubtree(parent);
        assertNotNull(result);
    }

    @Test
    public void testGetPropStringLength() {
        // "abc".length => 3
        Node stringNode = Node.newString("abc");
        Node propNode = Node.newString("length");
        Node parent = new Node(Token.GETPROP, stringNode, propNode);

        Node result = foldConstants.optimizeSubtree(parent);
        assertNotNull(result);
    }

    @Test
    public void testGetElemStringLength() {
        // "abc"[0] => "a"
        Node stringNode = Node.newString("abc");
        Node indexNode = Node.newNumber(0);
        Node parent = new Node(Token.GETELEM, stringNode, indexNode);

        Node result = foldConstants.optimizeSubtree(parent);
        assertNotNull(result);
    }

    @Test
    public void testInstanceOf() {
        // x instanceof Object
        Node left = Node.newString(Token.NAME, "x");
        Node right = Node.newString(Token.NAME, "Object");
        Node parent = new Node(Token.INSTANCEOF, left, right);

        Node result = foldConstants.optimizeSubtree(parent);
        assertNotNull(result);
    }

    @Test
    public void testInOperator() {
        // "a" in obj
        Node left = Node.newString("a");
        Node right = Node.newString(Token.NAME, "obj");
        Node parent = new Node(Token.IN, left, right);

        Node result = foldConstants.optimizeSubtree(parent);
        assertNotNull(result);
    }

    @Test
    public void testSwitchConstants() {
        // Switch case folding tests if applicable
        Node switchNode = new Node(Token.SWITCH, Node.newNumber(1));
        Node result = foldConstants.optimizeSubtree(switchNode);
        assertNotNull(result);
    }

    @Test
    public void testAndOrFolding() {
        // true && false => false
        Node left = Node.newTrue();
        Node right = Node.newFalse();
        Node parent = new Node(Token.AND, left, right);

        Node result = foldConstants.optimizeSubtree(parent);
        assertNotNull(result);

        // true || false => true
        Node parentOr = new Node(Token.OR, Node.newTrue(), Node.newFalse());
        Node resultOr = foldConstants.optimizeSubtree(parentOr);
        assertNotNull(resultOr);
    }
}