package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * JUnit 4 test class for PeepholeFoldConstants.
 * Designed to achieve high branch and line coverage for Closure Bug 148.
 */
public class PeepholeFoldConstantsTest {

    private PeepholeFoldConstants foldConstants;
    private AbstractCompiler compiler;

    @Before
    public void setUp() {
        foldConstants = new PeepholeFoldConstants();
        compiler = new Compiler();
        foldConstants.initialize(compiler);
    }

    @Test
    public void testTryFoldAdd() {
        // Test folding of addition with constants: 1 + 2 -> 3
        Node left = Node.newNumber(1.0);
        Node right = Node.newNumber(2.0);
        Node addNode = new Node(Token.ADD, left, right);

        Node result = foldConstants.optimizeSubtree(addNode);
        assertNotNull(result);
        assertEquals(Token.NUMBER, result.getType());
        assertEquals(3.0, result.getDouble(), 0.001);
    }

    @Test
    public void testTryFoldAddString() {
        // Test string concatenation: "a" + "b" -> "ab"
        Node left = Node.newString("a");
        Node right = Node.newString("b");
        Node addNode = new Node(Token.ADD, left, right);

        Node result = foldConstants.optimizeSubtree(addNode);
        assertNotNull(result);
        assertEquals(Token.STRING, result.getType());
        assertEquals("ab", result.getString());
    }

    @Test
    public void testTryFoldArithmetic() {
        // Test multiplication: 3 * 4 -> 12
        Node left = Node.newNumber(3.0);
        Node right = Node.newNumber(4.0);
        Node mulNode = new Node(Token.MUL, left, right);

        Node result = foldConstants.optimizeSubtree(mulNode);
        assertNotNull(result);
        assertEquals(Token.NUMBER, result.getType());
        assertEquals(12.0, result.getDouble(), 0.001);
    }

    @Test
    public void testTryFoldBitwise() {
        // Test bitwise AND: 5 & 3 -> 1
        Node left = Node.newNumber(5.0);
        Node right = Node.newNumber(3.0);
        Node bitAnd = new Node(Token.BITAND, left, right);

        Node result = foldConstants.optimizeSubtree(bitAnd);
        assertNotNull(result);
        assertEquals(Token.NUMBER, result.getType());
        assertEquals(1.0, result.getDouble(), 0.001);
    }

    @Test
    public void testTryFoldUnaryOperations() {
        // Test unary minus: -(-5) -> 5
        Node innerNum = Node.newNumber(5.0);
        Node neg1 = new Node(Token.NEG, innerNum);
        Node neg2 = new Node(Token.NEG, neg1);

        Node result = foldConstants.optimizeSubtree(neg2);
        assertNotNull(result);
        assertEquals(Token.NUMBER, result.getType());
        assertEquals(5.0, result.getDouble(), 0.001);
    }

    @Test
    public void testTryFoldComparisons() {
        // Test comparison: 1 < 2 -> true
        Node left = Node.newNumber(1.0);
        Node right = Node.newNumber(2.0);
        Node ltNode = new Node(Token.LT, left, right);

        Node result = foldConstants.optimizeSubtree(ltNode);
        assertNotNull(result);
        assertEquals(Token.TRUE, result.getType());
    }

    @Test
    public void testUndefinedComparison() {
        // Test comparison with undefined or void 0
        Node left = new Node(Token.VOID, Node.newNumber(0.0));
        Node right = Node.newNumber(1.0);
        Node eqNode = new Node(Token.EQ, left, right);

        Node result = foldConstants.optimizeSubtree(eqNode);
        assertNotNull(result);
    }

    @Test
    public void testInstanceOfFolding() {
        // Test instanceof folding where possible
        Node left = Node.newNumber(1.0);
        Node right = Node.newString("Object");
        Node instanceOfNode = new Node(Token.INSTANCEOF, left, right);

        Node result = foldConstants.optimizeSubtree(instanceOfNode);
        assertNotNull(result);
    }

    @Test
    public void testDivisionByZero() {
        // Test division by zero: 1 / 0
        Node left = Node.newNumber(1.0);
        Node right = Node.newNumber(0.0);
        Node divNode = new Node(Token.DIV, left, right);

        Node result = foldConstants.optimizeSubtree(divNode);
        assertNotNull(result);
    }

    @Test
    public void testNoFoldNeeded() {
        // Test a node that cannot be folded: x + 1 where x is a name
        Node left = Node.newName("x");
        Node right = Node.newNumber(1.0);
        Node addNode = new Node(Token.ADD, left, right);

        Node result = foldConstants.optimizeSubtree(addNode);
        assertSame(addNode, result);
    }
}