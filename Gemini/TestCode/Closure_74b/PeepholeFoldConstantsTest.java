package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class PeepholeFoldConstantsTest {

    private PeepholeFoldConstants foldConstants;
    private AbstractCompiler compiler;

    @Before
    public void setUp() {
        foldConstants = new PeepholeFoldConstants();
        compiler = new Compiler();
        // Initialize compiler options or dummy init if required by passes
        foldConstants.beginTraversal(compiler);
    }

    @Test
    public void testFoldAddNumbers() {
        // Test folding of addition: 1 + 2 -> 3
        Node left = Node.newNumber(1.0);
        Node right = Node.newNumber(2.0);
        Node add = new Node(Token.ADD, left, right);

        Node result = foldConstants.optimizeSubtree(add);
        assertNotNull(result);
        assertEquals(Token.NUMBER, result.getType());
        assertEquals(3.0, result.getDouble(), 0.001);
    }

    @Test
    public void testFoldAddStringAndNumber() {
        // Test string concatenation: "a" + 2 -> "a2"
        Node left = Node.newString("a");
        Node right = Node.newNumber(2.0);
        Node add = new Node(Token.ADD, left, right);

        Node result = foldConstants.optimizeSubtree(add);
        assertNotNull(result);
        assertEquals(Token.STRING, result.getType());
        assertEquals("a2", result.getString());
    }

    @Test
    public void testFoldComparison() {
        // Test comparison: 1 < 2 -> true
        Node left = Node.newNumber(1.0);
        Node right = Node.newNumber(2.0);
        Node cmp = new Node(Token.LT, left, right);

        Node result = foldConstants.optimizeSubtree(cmp);
        assertNotNull(result);
        assertEquals(Token.TRUE, result.getType());
    }

    @Test
    public void testFoldBitwise() {
        // Test bitwise shift: 4 << 1 -> 8
        Node left = Node.newNumber(4.0);
        Node right = Node.newNumber(1.0);
        Node lsh = new Node(Token.LSH, left, right);

        Node result = foldConstants.optimizeSubtree(lsh);
        assertNotNull(result);
        assertEquals(Token.NUMBER, result.getType());
        assertEquals(8.0, result.getDouble(), 0.001);
    }

    @Test
    public void testUndefinedComparison() {
        // Test void 0 == void 0 -> true (or similar undefined folding)
        Node left = new Node(Token.VOID, Node.newNumber(0.0));
        Node right = new Node(Token.VOID, Node.newNumber(0.0));
        Node eq = new Node(Token.EQ, left, right);

        Node result = foldConstants.optimizeSubtree(eq);
        assertNotNull(result);
    }

    @Test
    public void testNoOpOptimization() {
        // Node that cannot be folded: x + 1 where x is a name
        Node left = Node.newString(Token.NAME, "x");
        Node right = Node.newNumber(1.0);
        Node add = new Node(Token.ADD, left, right);

        Node result = foldConstants.optimizeSubtree(add);
        assertNotNull(result);
        assertEquals(Token.ADD, result.getType());
    }
}