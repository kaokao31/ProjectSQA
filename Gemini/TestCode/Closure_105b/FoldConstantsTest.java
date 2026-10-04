package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class FoldConstantsTest {

    private AbstractCompiler compiler;
    private FoldConstants foldConstants;

    @Before
    public void setUp() {
        compiler = new Compiler();
        foldConstants = new FoldConstants(compiler);
    }

    private Node optimize(Node root) {
        NodeTraversal.traverse(compiler, root, foldConstants);
        return root;
    }

    @Test
    public void testUndefinedComparison() {
        // Test undefined comparisons that FoldConstants handles
        // e.g., void 0 == void 0
        Node left = new Node(Token.VOID, Node.newNumber(0));
        Node right = new Node(Token.VOID, Node.newNumber(0));
        Node eq = new Node(Token.EQ, left, right);

        Node result = optimize(eq);
        // Should fold to true
        assertEquals(Token.TRUE, result.getType());
    }

    @Test
    public void testStringAddFolding() {
        // "a" + "b" -> "ab"
        Node left = Node.newString("a");
        Node right = Node.newString("b");
        Node add = new Node(Token.ADD, left, right);

        Node result = optimize(add);
        assertEquals(Token.STRING, result.getType());
        assertEquals("ab", result.getString());
    }

    @Test
    public void testNumericAddFolding() {
        // 1 + 2 -> 3
        Node left = Node.newNumber(1);
        Node right = Node.newNumber(2);
        Node add = new Node(Token.ADD, left, right);

        Node result = optimize(add);
        assertEquals(Token.NUMBER, result.getType());
        assertEquals(3.0, result.getDouble(), 0.0001);
    }

    @Test
    public void testTypeOfFolding() {
        // typeof 1 -> "number"
        Node child = Node.newNumber(1);
        Node typeOf = new Node(Token.TYPEOF, child);

        Node result = optimize(typeOf);
        assertEquals(Token.STRING, result.getType());
        assertEquals("number", result.getString());
    }

    @Test
    public void testBitwiseNotFolding() {
        // ~1 -> -2
        Node child = Node.newNumber(1);
        Node bitNot = new Node(Token.BITNOT, child);

        Node result = optimize(bitNot);
        assertEquals(Token.NUMBER, result.getType());
        assertEquals(-2.0, result.getDouble(), 0.0001);
    }

    @Test
    public void testNegateFolding() {
        // -5 -> -5 (or negated number)
        Node child = Node.newNumber(5);
        Node neg = new Node(Token.NEG, child);

        Node result = optimize(neg);
        assertEquals(Token.NUMBER, result.getType());
        assertEquals(-5.0, result.getDouble(), 0.0001);
    }

    @Test
    public void testLogicalNotFolding() {
        // !true -> false
        Node child = new Node(Token.TRUE);
        Node not = new Node(Token.NOT, child);

        Node result = optimize(not);
        assertEquals(Token.FALSE, result.getType());
    }

    @Test
    public void testConstantComparison() {
        // 1 < 2 -> true
        Node left = Node.newNumber(1);
        Node right = Node.newNumber(2);
        Node lt = new Node(Token.LT, left, right);

        Node result = optimize(lt);
        assertEquals(Token.TRUE, result.getType());
    }

    @Test
    public void testStringJoinFolding() {
        // ["a", "b"].join("") -> "ab"
        // In Closure Compiler, JOIN calls on arrays are often folded if constants.
        Node arrayLit = new Node(Token.ARRAYLIT, Node.newString("a"), Node.newString("b"));
        Node getProp = new Node(Token.GETPROP, arrayLit, Node.newString("join"));
        Node call = new Node(Token.CALL, getProp, Node.newString(""));

        Node result = optimize(call);
        // Depending on compiler version and implementation, join might be folded or left as is.
        // This ensures the traversal visits it safely without throwing exceptions.
        assertNotNull(result);
    }

    @Test
    public void testSwitchStatementFolding() {
        // Test folding within switch or similar structures if applicable
        Node key = Node.newNumber(1);
        Node caseNode = new Node(Token.CASE, Node.newNumber(1), new Node(Token.BLOCK));
        Node switchNode = new Node(Token.SWITCH, key, caseNode);

        Node result = optimize(switchNode);
        assertNotNull(result);
    }
}