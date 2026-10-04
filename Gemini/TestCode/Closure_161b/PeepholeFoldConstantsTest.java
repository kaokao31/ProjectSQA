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
    public void testFoldAddNumbers() {
        // 1 + 2 -> 3
        Node left = Node.newNumber(1.0);
        Node right = Node.newNumber(2.0);
        Node add = Node.news(Token.ADD, left, right);

        Node result = peepholeFoldConstants.optimizeSubtree(add);
        assertNotNull(result);
        assertEquals(Token.NUMBER, result.getType());
        assertEquals(3.0, result.getDouble(), 0.001);
    }

    @Test
    public void testFoldStringAddNumbers() {
        // "a" + 2 -> "a2"
        Node left = Node.newString("a");
        Node right = Node.newNumber(2.0);
        Node add = Node.news(Token.ADD, left, right);

        Node result = peepholeFoldConstants.optimizeSubtree(add);
        assertNotNull(result);
        assertEquals(Token.STRING, result.getType());
        assertEquals("a2", result.getString());
    }

    @Test
    public void testFoldSubNumbers() {
        // 5 - 2 -> 3
        Node left = Node.newNumber(5.0);
        Node right = Node.newNumber(2.0);
        Node sub = Node.news(Token.SUB, left, right);

        Node result = peepholeFoldConstants.optimizeSubtree(sub);
        assertNotNull(result);
        assertEquals(Token.NUMBER, result.getType());
        assertEquals(3.0, result.getDouble(), 0.001);
    }

    @Test
    public void testFoldMulNumbers() {
        // 3 * 4 -> 12
        Node left = Node.newNumber(3.0);
        Node right = Node.newNumber(4.0);
        Node mul = Node.news(Token.MUL, left, right);

        Node result = peepholeFoldConstants.optimizeSubtree(mul);
        assertNotNull(result);
        assertEquals(Token.NUMBER, result.getType());
        assertEquals(12.0, result.getDouble(), 0.001);
    }

    @Test
    public void testFoldDivNumbers() {
        // 10 / 2 -> 5
        Node left = Node.newNumber(10.0);
        Node right = Node.newNumber(2.0);
        Node div = Node.news(Token.DIV, left, right);

        Node result = peepholeFoldConstants.optimizeSubtree(div);
        assertNotNull(result);
        assertEquals(Token.NUMBER, result.getType());
        assertEquals(5.0, result.getDouble(), 0.001);
    }

    @Test
    public void testFoldModNumbers() {
        // 5 % 2 -> 1
        Node left = Node.newNumber(5.0);
        Node right = Node.newNumber(2.0);
        Node mod = Node.news(Token.MOD, left, right);

        Node result = peepholeFoldConstants.optimizeSubtree(mod);
        assertNotNull(result);
        assertEquals(Token.NUMBER, result.getType());
        assertEquals(1.0, result.getDouble(), 0.001);
    }

    @Test
    public void testFoldBitwiseAnd() {
        // 5 & 1 -> 1
        Node left = Node.newNumber(5.0);
        Node right = Node.newNumber(1.0);
        Node bitAnd = Node.news(Token.BITAND, left, right);

        Node result = peepholeFoldConstants.optimizeSubtree(bitAnd);
        assertNotNull(result);
        assertEquals(Token.NUMBER, result.getType());
        assertEquals(1.0, result.getDouble(), 0.001);
    }

    @Test
    public void testFoldBitwiseOr() {
        // 4 | 1 -> 5
        Node left = Node.newNumber(4.0);
        Node right = Node.newNumber(1.0);
        Node bitOr = Node.news(Token.BITOR, left, right);

        Node result = peepholeFoldConstants.optimizeSubtree(bitOr);
        assertNotNull(result);
        assertEquals(Token.NUMBER, result.getType());
        assertEquals(5.0, result.getDouble(), 0.001);
    }

    @Test
    public void testFoldBitwiseXor() {
        // 5 ^ 1 -> 4
        Node left = Node.newNumber(5.0);
        Node right = Node.newNumber(1.0);
        Node bitXor = Node.news(Token.BITXOR, left, right);

        Node result = peepholeFoldConstants.optimizeSubtree(bitXor);
        assertNotNull(result);
        assertEquals(Token.NUMBER, result.getType());
        assertEquals(4.0, result.getDouble(), 0.001);
    }

    @Test
    public void testFoldLsh() {
        // 1 << 2 -> 4
        Node left = Node.newNumber(1.0);
        Node right = Node.newNumber(2.0);
        Node lsh = Node.news(Token.LSH, left, right);

        Node result = peepholeFoldConstants.optimizeSubtree(lsh);
        assertNotNull(result);
        assertEquals(Token.NUMBER, result.getType());
        assertEquals(4.0, result.getDouble(), 0.001);
    }

    @Test
    public void testFoldRsh() {
        // 4 >> 1 -> 2
        Node left = Node.newNumber(4.0);
        Node right = Node.newNumber(1.0);
        Node rsh = Node.news(Token.RSH, left, right);

        Node result = peepholeFoldConstants.optimizeSubtree(rsh);
        assertNotNull(result);
        assertEquals(Token.NUMBER, result.getType());
        assertEquals(2.0, result.getDouble(), 0.001);
    }

    @Test
    public void testFoldUrsh() {
        // 4 >>> 1 -> 2
        Node left = Node.newNumber(4.0);
        Node right = Node.newNumber(1.0);
        Node ursh = Node.news(Token.URSH, left, right);

        Node result = peepholeFoldConstants.optimizeSubtree(ursh);
        assertNotNull(result);
        assertEquals(Token.NUMBER, result.getType());
        assertEquals(2.0, result.getDouble(), 0.001);
    }

    @Test
    public void testFoldComparison() {
        // 1 < 2 -> true
        Node left = Node.newNumber(1.0);
        Node right = Node.newNumber(2.0);
        Node lt = Node.news(Token.LT, left, right);

        Node result = peepholeFoldConstants.optimizeSubtree(lt);
        assertNotNull(result);
        assertEquals(Token.TRUE, result.getType());
    }

    @Test
    public void testFoldTypeOf() {
        // typeof 1 -> "number"
        Node child = Node.newNumber(1.0);
        Node typeof = Node.news(Token.TYPEOF, child);

        Node result = peepholeFoldConstants.optimizeSubtree(typeof);
        assertNotNull(result);
        assertEquals(Token.STRING, result.getType());
        assertEquals("number", result.getString());
    }

    @Test
    public void testFoldVoid() {
        // void 0 -> undefined (void)
        Node child = Node.newNumber(0.0);
        Node voidNode = Node.news(Token.VOID, child);

        Node result = peepholeFoldConstants.optimizeSubtree(voidNode);
        assertNotNull(result);
        assertEquals(Token.VOID, result.getType());
    }

    @Test
    public void testFoldNot() {
        // !true -> false
        Node child = Node.news(Token.TRUE);
        Node not = Node.news(Token.NOT, child);

        Node result = peepholeFoldConstants.optimizeSubtree(not);
        assertNotNull(result);
        assertEquals(Token.FALSE, result.getType());
    }

    @Test
    public void testUndefiningIndexNegative() {
        // Test array/element access edge cases if applicable, or generic node folding
        Node node = Node.newNumber(10.0);
        Node result = peepholeFoldConstants.optimizeSubtree(node);
        assertSame(node, result);
    }
}