package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class PeepholeFoldConstantsTest {

    private PeepholeFoldConstants peepholeFoldConstants;
    private AbstractCompiler compiler;

    @Before
    public void setUp() {
        peepholeFoldConstants = new PeepholeFoldConstants();
        compiler = new Compiler();
        peepholeFoldConstants.beginTraversal(compiler);
    }

    @Test
    public void testFoldAddStringAndNumber() {
        // "a" + 1 -> "a1"
        Node left = Node.newString("a");
        Node right = Node.newNumber(1);
        Node add = new Node(Token.ADD, left, right);

        Node result = peepholeFoldConstants.optimizeSubtree(add);
        assertNotNull(result);
        assertEquals(Token.STRING, result.getType());
        assertEquals("a1", result.getString());
    }

    @Test
    public void testFoldAddNumbers() {
        // 1 + 2 -> 3
        Node left = Node.newNumber(1);
        Node right = Node.newNumber(2);
        Node add = new Node(Token.ADD, left, right);

        Node result = peepholeFoldConstants.optimizeSubtree(add);
        assertNotNull(result);
        assertEquals(Token.NUMBER, result.getType());
        assertEquals(3.0, result.getDouble(), 0.001);
    }

    @Test
    public void testFoldBitwiseAnd() {
        // 5 & 3 -> 1
        Node left = Node.newNumber(5);
        Node right = Node.newNumber(3);
        Node bitAnd = new Node(Token.BITAND, left, right);

        Node result = peepholeFoldConstants.optimizeSubtree(bitAnd);
        assertNotNull(result);
        assertEquals(Token.NUMBER, result.getType());
        assertEquals(1.0, result.getDouble(), 0.001);
    }

    @Test
    public void testFoldGetElemArray() {
        // [1, 2, 3][1] -> 2
        Node array = Node.newArray(Node.newNumber(1), Node.newNumber(2), Node.newNumber(3));
        Node index = Node.newNumber(1);
        Node getElem = new Node(Token.GETELEM, array, index);

        Node result = peepholeFoldConstants.optimizeSubtree(getElem);
        assertNotNull(result);
        assertEquals(Token.NUMBER, result.getType());
        assertEquals(2.0, result.getDouble(), 0.001);
    }

    @Test
    public void testFoldGetElemStringLiteral() {
        // "abc"[1] -> "b"
        Node str = Node.newString("abc");
        Node index = Node.newNumber(1);
        Node getElem = new Node(Token.GETELEM, str, index);

        Node result = peepholeFoldConstants.optimizeSubtree(getElem);
        assertNotNull(result);
        assertEquals(Token.STRING, result.getType());
        assertEquals("b", result.getString());
    }

    @Test
    public void testFoldGetElemOutOfBounds() {
        // [1, 2, 3][5] should not crash or fold incorrectly
        Node array = Node.newArray(Node.newNumber(1), Node.newNumber(2), Node.newNumber(3));
        Node index = Node.newNumber(5);
        Node getElem = new Node(Token.GETELEM, array, index);

        Node result = peepholeFoldConstants.optimizeSubtree(getElem);
        // Depending on implementation, might return original or null/optimized, but must not throw.
        assertNotNull(result);
    }

    @Test
    public void testUndefinedComparison() {
        // void 0 === void 1 -> true or folded correctly
        Node void1 = new Node(Token.VOID, Node.newNumber(0));
        Node void2 = new Node(Token.VOID, Node.newNumber(0));
        Node eq = new Node(Token.SHEQ, void1, void2);

        Node result = peepholeFoldConstants.optimizeSubtree(eq);
        assertNotNull(result);
    }

    @Test
    public void testTypeofString() {
        // typeof "abc" -> "string"
        Node str = Node.newString("abc");
        Node typeof = new Node(Token.TYPEOF, str);

        Node result = peepholeFoldConstants.optimizeSubtree(typeof);
        assertNotNull(result);
        assertEquals(Token.STRING, result.getType());
        assertEquals("string", result.getString());
    }

    @Test
    public void testVoidFold() {
        // void 0 -> undefined (or void node handled)
        Node num = Node.newNumber(0);
        Node voidNode = new Node(Token.VOID, num);

        Node result = peepholeFoldConstants.optimizeSubtree(voidNode);
        assertNotNull(result);
    }

    @Test
    public void testNegateNumber() {
        // - 5 -> -5
        Node num = Node.newNumber(5);
        Node neg = new Node(Token.NEG, num);

        Node result = peepholeFoldConstants.optimizeSubtree(neg);
        assertNotNull(result);
        assertEquals(Token.NUMBER, result.getType());
        assertEquals(-5.0, result.getDouble(), 0.001);
    }

    @Test
    public void testNotBoolean() {
        // !true -> false
        Node trueNode = Node.newTrue();
        Node not = new Node(Token.NOT, trueNode);

        Node result = peepholeFoldConstants.optimizeSubtree(not);
        assertNotNull(result);
        assertEquals(Token.FALSE, result.getType());
    }

    @Test
    public void testInfixArithmeticDivision() {
        // 6 / 2 -> 3
        Node left = Node.newNumber(6);
        Node right = Node.newNumber(2);
        Node div = new Node(Token.DIV, left, right);

        Node result = peepholeFoldConstants.optimizeSubtree(div);
        assertNotNull(result);
        assertEquals(Token.NUMBER, result.getType());
        assertEquals(3.0, result.getDouble(), 0.001);
    }

    @Test
    public void testDivisionByZero() {
        // 6 / 0 -> Infinity or handled safely
        Node left = Node.newNumber(6);
        Node right = Node.newNumber(0);
        Node div = new Node(Token.DIV, left, right);

        Node result = peepholeFoldConstants.optimizeSubtree(div);
        assertNotNull(result);
    }

    @Test
    public void testModulus() {
        // 5 % 2 -> 1
        Node left = Node.newNumber(5);
        Node right = Node.newNumber(2);
        Node mod = new Node(Token.MOD, left, right);

        Node result = peepholeFoldConstants.optimizeSubtree(mod);
        assertNotNull(result);
        assertEquals(Token.NUMBER, result.getType());
        assertEquals(1.0, result.getDouble(), 0.001);
    }

    @Test
    public void testLsh() {
        // 1 << 2 -> 4
        Node left = Node.newNumber(1);
        Node right = Node.newNumber(2);
        Node lsh = new Node(Token.LSH, left, right);

        Node result = peepholeFoldConstants.optimizeSubtree(lsh);
        assertNotNull(result);
        assertEquals(Token.NUMBER, result.getType());
        assertEquals(4.0, result.getDouble(), 0.001);
    }

    @Test
    public void testRsh() {
        // 4 >> 1 -> 2
        Node left = Node.newNumber(4);
        Node right = Node.newNumber(1);
        Node rsh = new Node(Token.RSH, left, right);

        Node result = peepholeFoldConstants.optimizeSubtree(rsh);
        assertNotNull(result);
        assertEquals(Token.NUMBER, result.getType());
        assertEquals(2.0, result.getDouble(), 0.001);
    }

    @Test
    public void testRshh() {
        // 4 >>> 1 -> 2
        Node left = Node.newNumber(4);
        Node right = Node.newNumber(1);
        Node rshh = new Node(Token.RSHH, left, right);

        Node result = peepholeFoldConstants.optimizeSubtree(rshh);
        assertNotNull(result);
        assertEquals(Token.NUMBER, result.getType());
        assertEquals(2.0, result.getDouble(), 0.001);
    }

    @Test
    public void testBitXor() {
        // 3 ^ 1 -> 2
        Node left = Node.newNumber(3);
        Node right = Node.newNumber(1);
        Node bitXor = new Node(Token.BITXOR, left, right);

        Node result = peepholeFoldConstants.optimizeSubtree(bitXor);
        assertNotNull(result);
        assertEquals(Token.NUMBER, result.getType());
        assertEquals(2.0, result.getDouble(), 0.001);
    }

    @Test
    public void testBitOr() {
        // 4 | 2 -> 6
        Node left = Node.newNumber(4);
        Node right = Node.newNumber(2);
        Node bitOr = new Node(Token.BITOR, left, right);

        Node result = peepholeFoldConstants.optimizeSubtree(bitOr);
        assertNotNull(result);
        assertEquals(Token.NUMBER, result.getType());
        assertEquals(6.0, result.getDouble(), 0.001);
    }

    @Test
    public void testRelationalOperators() {
        // 3 < 5 -> true
        Node left = Node.newNumber(3);
        Node right = Node.newNumber(5);
        Node lt = new Node(Token.LT, left, right);

        Node result = peepholeFoldConstants.optimizeSubtree(lt);
        assertNotNull(result);
        assertEquals(Token.TRUE, result.getType());
    }

    @Test
    public void testStringLengthOptimization() {
        // "abc".length -> 3 (if applicable)
        Node getProp = new Node(Token.GETPROP, Node.newString("abc"), Node.newString("length"));
        Node result = peepholeFoldConstants.optimizeSubtree(getProp);
        assertNotNull(result);
    }
}