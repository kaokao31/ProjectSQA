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
        foldConstants.initialize(compiler);
    }

    @Test
    public void testFoldArithmeticAddition() {
        // Test folding of additions: 1 + 2 -> 3
        Node left = Node.newNumber(1.0);
        Node right = Node.newNumber(2.0);
        Node add = new Node(Token.ADD, left, right);

        Node result = foldConstants.optimizeSubtree(add);
        assertNotNull(result);
        assertEquals(Token.NUMBER, result.getType());
        assertEquals(3.0, result.getDouble(), 0.0001);
    }

    @Test
    public void testFoldArithmeticAdditionStringConcat() {
        // String concatenation should not be folded numerically: "a" + 2 -> "a2" (depending on implementation, or left alone)
        Node left = Node.newString("a");
        Node right = Node.newNumber(2.0);
        Node add = new Node(Token.ADD, left, right);

        Node result = foldConstants.optimizeSubtree(add);
        assertNotNull(result);
    }

    @Test
    public void testFoldArithmeticSubtraction() {
        Node left = Node.newNumber(5.0);
        Node right = Node.newNumber(2.0);
        Node sub = new Node(Token.SUB, left, right);

        Node result = foldConstants.optimizeSubtree(sub);
        assertNotNull(result);
        assertEquals(Token.NUMBER, result.getType());
        assertEquals(3.0, result.getDouble(), 0.0001);
    }

    @Test
    public void testFoldArithmeticMulDiv() {
        // Multiplication: 3 * 4 -> 12
        Node mul = new Node(Token.MUL, Node.newNumber(3.0), Node.newNumber(4.0));
        Node resMul = foldConstants.optimizeSubtree(mul);
        assertEquals(Token.NUMBER, resMul.getType());
        assertEquals(12.0, resMul.getDouble(), 0.0001);

        // Division: 10 / 2 -> 5
        Node div = new Node(Token.DIV, Node.newNumber(10.0), Node.newNumber(2.0));
        Node resDiv = foldConstants.optimizeSubtree(div);
        assertEquals(Token.NUMBER, resDiv.getType());
        assertEquals(5.0, resDiv.getDouble(), 0.0001);
    }

    @Test
    public void testFoldShiftOperations() {
        // Left shift: 4 << 1 -> 8
        Node lsh = new Node(Token.LSH, Node.newNumber(4.0), Node.newNumber(1.0));
        Node resLsh = foldConstants.optimizeSubtree(lsh);
        assertEquals(Token.NUMBER, resLsh.getType());
        assertEquals(8.0, resLsh.getDouble(), 0.0001);

        // Right shift: 4 >> 1 -> 2
        Node rsh = new Node(Token.RSH, Node.newNumber(4.0), Node.newNumber(1.0));
        Node resRsh = foldConstants.optimizeSubtree(rsh);
        assertEquals(Token.NUMBER, resRsh.getType());
        assertEquals(2.0, resRsh.getDouble(), 0.0001);

        // Unsigned right shift: 4 >>> 1 -> 2
        Node ursh = new Node(Token.URSH, Node.newNumber(4.0), Node.newNumber(1.0));
        Node resUrsh = foldConstants.optimizeSubtree(ursh);
        assertEquals(Token.NUMBER, resUrsh.getType());
        assertEquals(2.0, resUrsh.getDouble(), 0.0001);
    }

    @Test
    public void testBitwiseOperations() {
        // Bitwise AND: 5 & 1 -> 1
        Node bAnd = new Node(Token.BITAND, Node.newNumber(5.0), Node.newNumber(1.0));
        Node resAnd = foldConstants.optimizeSubtree(bAnd);
        assertEquals(Token.NUMBER, resAnd.getType());
        assertEquals(1.0, resAnd.getDouble(), 0.0001);

        // Bitwise OR: 4 | 1 -> 5
        Node bOr = new Node(Token.BITOR, Node.newNumber(4.0), Node.newNumber(1.0));
        Node resOr = foldConstants.optimizeSubtree(bOr);
        assertEquals(Token.NUMBER, resOr.getType());
        assertEquals(5.0, resOr.getDouble(), 0.0001);

        // Bitwise XOR: 5 ^ 1 -> 4
        Node bXor = new Node(Token.BITXOR, Node.newNumber(5.0), Node.newNumber(1.0));
        Node resXor = foldConstants.optimizeSubtree(bXor);
        assertEquals(Token.NUMBER, resXor.getType());
        assertEquals(4.0, resXor.getDouble(), 0.0001);
    }

    @Test
    public void testUnaryOperations() {
        // Negation: -5
        Node neg = new Node(Token.NEG, Node.newNumber(5.0));
        Node resNeg = foldConstants.optimizeSubtree(neg);
        assertEquals(Token.NUMBER, resNeg.getType());
        assertEquals(-5.0, resNeg.getDouble(), 0.0001);

        // Bitwise NOT: ~0 -> -1
        Node bitNot = new Node(Token.BITNOT, Node.newNumber(0.0));
        Node resBitNot = foldConstants.optimizeSubtree(bitNot);
        assertEquals(Token.NUMBER, resBitNot.getType());
        assertEquals(-1.0, resBitNot.getDouble(), 0.0001);

        // Pos: +5 -> 5
        Node pos = new Node(Token.POS, Node.newNumber(5.0));
        Node resPos = foldConstants.optimizeSubtree(pos);
        assertEquals(Token.NUMBER, resPos.getType());
        assertEquals(5.0, resPos.getDouble(), 0.0001);
    }

    @Test
    public void testFoldComparisons() {
        // LT: 1 < 2 -> true
        Node lt = new Node(Token.LT, Node.newNumber(1.0), Node.newNumber(2.0));
        Node resLt = foldConstants.optimizeSubtree(lt);
        assertEquals(Token.TRUE, resLt.getType());

        // GT: 2 > 1 -> true
        Node gt = new Node(Token.GT, Node.newNumber(2.0), Node.newNumber(1.0));
        Node resGt = foldConstants.optimizeSubtree(gt);
        assertEquals(Token.TRUE, resGt.getType());

        // LE: 2 <= 2 -> true
        Node le = new Node(Token.LE, Node.newNumber(2.0), Node.newNumber(2.0));
        Node resLe = foldConstants.optimizeSubtree(le);
        assertEquals(Token.TRUE, resLe.getType());

        // GE: 2 >= 2 -> true
        Node ge = new Node(Token.GE, Node.newNumber(2.0), Node.newNumber(2.0));
        Node resGe = foldConstants.optimizeSubtree(ge);
        assertEquals(Token.TRUE, resGe.getType());

        // EQ: 2 == 2 -> true
        Node eq = new Node(Token.SHEQ, Node.newNumber(2.0), Node.newNumber(2.0));
        Node resEq = foldConstants.optimizeSubtree(eq);
        assertEquals(Token.TRUE, resEq.getType());

        // NE: 2 != 1 -> true
        Node ne = new Node(Token.SHNE, Node.newNumber(2.0), Node.newNumber(1.0));
        Node resNe = foldConstants.optimizeSubtree(ne);
        assertEquals(Token.TRUE, resNe.getType());
    }

    @Test
    public void testFoldTypeof() {
        // typeof 1 -> "number"
        Node typeofNode = new Node(Token.TYPEOF, Node.newNumber(1.0));
        Node res = foldConstants.optimizeSubtree(typeofNode);
        assertNotNull(res);
        if (res.getType() == Token.STRING) {
            assertEquals("number", res.getString());
        }
    }

    @Test
    public void testFoldVoid() {
        // void 0
        Node voidNode = new Node(Token.VOID, Node.newNumber(0.0));
        Node res = foldConstants.optimizeSubtree(voidNode);
        assertNotNull(res);
    }

    @Test
    public void testFoldInstanceOf() {
        // Non-folding or specific instance of checks
        Node instanceofNode = new Node(Token.INSTANCEOF, Node.newNumber(1.0), Node.newString("Object"));
        Node res = foldConstants.optimizeSubtree(instanceofNode);
        assertNotNull(res);
    }

    @Test
    public void testDivisionByZero() {
        // Division by zero: 5 / 0 -> Infinity or NaN depending on JS semantics handled by compiler
        Node div = new Node(Token.DIV, Node.newNumber(5.0), Node.newNumber(0.0));
        Node res = foldConstants.optimizeSubtree(div);
        assertNotNull(res);
    }

    @Test
    public void testModulus() {
        // Modulus: 5 % 2 -> 1
        Node mod = new Node(Token.MOD, Node.newNumber(5.0), Node.newNumber(2.0));
        Node res = foldConstants.optimizeSubtree(mod);
        assertEquals(Token.NUMBER, res.getType());
        assertEquals(1.0, res.getDouble(), 0.0001);
    }

    @Test
    public void testUndefinedAndBooleanFolding() {
        // true && false -> false
        Node andNode = new Node(Token.AND, Node.newTrue(), Node.newFalse());
        Node resAnd = foldConstants.optimizeSubtree(andNode);
        assertEquals(Token.FALSE, resAnd.getType());

        // true || false -> true
        Node orNode = new Node(Token.OR, Node.newTrue(), Node.newFalse());
        Node resOr = foldConstants.optimizeSubtree(orNode);
        assertEquals(Token.TRUE, resOr.getType());
    }

    @Test
    public void testSwitchAndIfFoldingSafety() {
        // If condition true: if (true) { x(); } else { y(); }
        Node cond = Node.newTrue();
        Node thenBlock = new Node(Token.BLOCK, Node.newNumber(1.0));
        Node elseBlock = new Node(Token.BLOCK, Node.newNumber(2.0));
        Node ifNode = new Node(Token.IF, cond, thenBlock, elseBlock);

        Node res = foldConstants.optimizeSubtree(ifNode);
        assertNotNull(res);
    }
}