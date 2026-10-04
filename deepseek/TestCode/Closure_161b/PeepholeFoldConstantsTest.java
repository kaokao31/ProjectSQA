package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test suite for PeepholeFoldConstants.
 * Designed to achieve high code coverage and detect potential faults.
 */
public class PeepholeFoldConstantsTest {

    private Compiler compiler;
    private PeepholeFoldConstants foldConstants;

    @Before
    public void setUp() {
        compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        compiler.initOptions(options);
        foldConstants = new PeepholeFoldConstants();
    }

    // Helper to create a simple AST with a single expression
    private Node createAst(Node expr) {
        Node script = new Node(Token.SCRIPT);
        Node exprResult = new Node(Token.EXPR_RESULT, expr);
        script.addChildToBack(exprResult);
        return script;
    }

    // Helper to fold and return the result node
    private Node foldExpression(Node expr) {
        Node ast = createAst(expr);
        foldConstants.process(compiler, ast);
        // The folded expression is the first child of the EXPR_RESULT
        return ast.getFirstChild().getFirstChild();
    }

    // --- Arithmetic folding tests ---

    @Test
    public void testAddNumbers() {
        Node expr = new Node(Token.ADD, Node.newNumber(2), Node.newNumber(3));
        Node result = foldExpression(expr);
        assertEquals(Token.NUMBER, result.getToken());
        assertEquals(5.0, result.getDouble(), 0.0);
    }

    @Test
    public void testSubtractNumbers() {
        Node expr = new Node(Token.SUB, Node.newNumber(10), Node.newNumber(4));
        Node result = foldExpression(expr);
        assertEquals(Token.NUMBER, result.getToken());
        assertEquals(6.0, result.getDouble(), 0.0);
    }

    @Test
    public void testMultiplyNumbers() {
        Node expr = new Node(Token.MUL, Node.newNumber(7), Node.newNumber(8));
        Node result = foldExpression(expr);
        assertEquals(Token.NUMBER, result.getToken());
        assertEquals(56.0, result.getDouble(), 0.0);
    }

    @Test
    public void testDivideNumbers() {
        Node expr = new Node(Token.DIV, Node.newNumber(15), Node.newNumber(4));
        Node result = foldExpression(expr);
        assertEquals(Token.NUMBER, result.getToken());
        assertEquals(3.75, result.getDouble(), 0.0);
    }

    @Test
    public void testModuloNumbers() {
        Node expr = new Node(Token.MOD, Node.newNumber(17), Node.newNumber(5));
        Node result = foldExpression(expr);
        assertEquals(Token.NUMBER, result.getToken());
        assertEquals(2.0, result.getDouble(), 0.0);
    }

    @Test
    public void testDivisionByZero() {
        Node expr = new Node(Token.DIV, Node.newNumber(1), Node.newNumber(0));
        Node result = foldExpression(expr);
        // Should fold to Infinity or NaN depending on implementation
        assertTrue(result.getToken() == Token.NUMBER);
        assertTrue(Double.isInfinite(result.getDouble()) || Double.isNaN(result.getDouble()));
    }

    @Test
    public void testOverflow() {
        Node expr = new Node(Token.ADD, Node.newNumber(Double.MAX_VALUE), Node.newNumber(Double.MAX_VALUE));
        Node result = foldExpression(expr);
        assertEquals(Token.NUMBER, result.getToken());
        assertTrue(Double.isInfinite(result.getDouble()));
    }

    // --- String concatenation folding ---

    @Test
    public void testStringConcat() {
        Node expr = new Node(Token.ADD, Node.newString("Hello, "), Node.newString("World!"));
        Node result = foldExpression(expr);
        assertEquals(Token.STRING, result.getToken());
        assertEquals("Hello, World!", result.getString());
    }

    @Test
    public void testStringAndNumberConcat() {
        Node expr = new Node(Token.ADD, Node.newString("Value: "), Node.newNumber(42));
        Node result = foldExpression(expr);
        assertEquals(Token.STRING, result.getToken());
        assertEquals("Value: 42", result.getString());
    }

    @Test
    public void testEmptyStringConcat() {
        Node expr = new Node(Token.ADD, Node.newString(""), Node.newString("test"));
        Node result = foldExpression(expr);
        assertEquals(Token.STRING, result.getToken());
        assertEquals("test", result.getString());
    }

    // --- Comparison folding ---

    @Test
    public void testEqTrueTrue() {
        Node expr = new Node(Token.EQ, Node.newString("true"), Node.newString("true"));
        Node result = foldExpression(expr);
        assertEquals(Token.TRUE, result.getToken());
    }

    @Test
    public void testEqFalseTrue() {
        Node expr = new Node(Token.EQ, Node.newString("false"), Node.newString("true"));
        Node result = foldExpression(expr);
        assertEquals(Token.FALSE, result.getToken());
    }

    @Test
    public void testLtNumbers() {
        Node expr = new Node(Token.LT, Node.newNumber(3), Node.newNumber(5));
        Node result = foldExpression(expr);
        assertEquals(Token.TRUE, result.getToken());
    }

    @Test
    public void testGtNumbers() {
        Node expr = new Node(Token.GT, Node.newNumber(10), Node.newNumber(2));
        Node result = foldExpression(expr);
        assertEquals(Token.TRUE, result.getToken());
    }

    // --- Unary operations ---

    @Test
    public void testUnaryNegation() {
        Node expr = new Node(Token.NEG, Node.newNumber(7));
        Node result = foldExpression(expr);
        assertEquals(Token.NUMBER, result.getToken());
        assertEquals(-7.0, result.getDouble(), 0.0);
    }

    @Test
    public void testUnaryBitwiseNot() {
        Node expr = new Node(Token.BITNOT, Node.newNumber(5));
        Node result = foldExpression(expr);
        assertEquals(Token.NUMBER, result.getToken());
        assertEquals(-6.0, result.getDouble(), 0.0);
    }

    @Test
    public void testUnaryNotTrue() {
        Node expr = new Node(Token.NOT, new Node(Token.TRUE));
        Node result = foldExpression(expr);
        assertEquals(Token.FALSE, result.getToken());
    }

    // --- Logical operators ---

    @Test
    public void testAndTrueTrue() {
        Node expr = new Node(Token.AND, new Node(Token.TRUE), new Node(Token.TRUE));
        Node result = foldExpression(expr);
        assertEquals(Token.TRUE, result.getToken());
    }

    @Test
    public void testAndTrueFalse() {
        Node expr = new Node(Token.AND, new Node(Token.TRUE), new Node(Token.FALSE));
        Node result = foldExpression(expr);
        assertEquals(Token.FALSE, result.getToken());
    }

    @Test
    public void testOrFalseFalse() {
        Node expr = new Node(Token.OR, new Node(Token.FALSE), new Node(Token.FALSE));
        Node result = foldExpression(expr);
        assertEquals(Token.FALSE, result.getToken());
    }

    @Test
    public void testOrTrueFalse() {
        Node expr = new Node(Token.OR, new Node(Token.TRUE), new Node(Token.FALSE));
        Node result = foldExpression(expr);
        assertEquals(Token.TRUE, result.getToken());
    }

    // --- Edge cases with null/undefined ---

    @Test
    public void testAddNullToNumber() {
        Node expr = new Node(Token.ADD, Node.newNull(), Node.newNumber(5));
        Node result = foldExpression(expr);
        // Depending on implementation, may fold to number or string
        assertNotNull(result);
    }

    @Test
    public void testAddUndefinedToNumber() {
        Node expr = new Node(Token.ADD, new Node(Token.VOID, Node.newNumber(0)), Node.newNumber(5));
        Node result = foldExpression(expr);
        assertNotNull(result);
    }

    // --- Bitwise operations ---

    @Test
    public void testBitAnd() {
        Node expr = new Node(Token.BITAND, Node.newNumber(6), Node.newNumber(3));
        Node result = foldExpression(expr);
        assertEquals(Token.NUMBER, result.getToken());
        assertEquals(2.0, result.getDouble(), 0.0);
    }

    @Test
    public void testBitOr() {
        Node expr = new Node(Token.BITOR, Node.newNumber(4), Node.newNumber(8));
        Node result = foldExpression(expr);
        assertEquals(Token.NUMBER, result.getToken());
        assertEquals(12.0, result.getDouble(), 0.0);
    }

    @Test
    public void testBitXor() {
        Node expr = new Node(Token.BITXOR, Node.newNumber(5), Node.newNumber(3));
        Node result = foldExpression(expr);
        assertEquals(Token.NUMBER, result.getToken());
        assertEquals(6.0, result.getDouble(), 0.0);
    }

    @Test
    public void testShiftLeft() {
        Node expr = new Node(Token.LSH, Node.newNumber(1), Node.newNumber(4));
        Node result = foldExpression(expr);
        assertEquals(Token.NUMBER, result.getToken());
        assertEquals(16.0, result.getDouble(), 0.0);
    }

    @Test
    public void testShiftRight() {
        Node expr = new Node(Token.RSH, Node.newNumber(16), Node.newNumber(2));
        Node result = foldExpression(expr);
        assertEquals(Token.NUMBER, result.getToken());
        assertEquals(4.0, result.getDouble(), 0.0);
    }

    @Test
    public void testUnsignedShiftRight() {
        Node expr = new Node(Token.URSH, Node.newNumber(-1), Node.newNumber(1));
        Node result = foldExpression(expr);
        assertEquals(Token.NUMBER, result.getToken());
        assertEquals(2147483647.0, result.getDouble(), 0.0);
    }

    // --- Type coercion edge cases ---

    @Test
    public void testAddStringAndBoolean() {
        Node expr = new Node(Token.ADD, Node.newString("test"), new Node(Token.TRUE));
        Node result = foldExpression(expr);
        assertEquals(Token.STRING, result.getToken());
        assertEquals("testtrue", result.getString());
    }

    @Test
    public void testSubtractStringFromNumber() {
        Node expr = new Node(Token.SUB, Node.newNumber(10), Node.newString("3"));
        Node result = foldExpression(expr);
        // Should fold to number 7 after coercion
        assertEquals(Token.NUMBER, result.getToken());
        assertEquals(7.0, result.getDouble(), 0.0);
    }

    // --- Non-foldable cases (should remain unchanged) ---

    @Test
    public void testNonConstantLeft() {
        Node name = Node.newString(Token.NAME, "x");
        Node expr = new Node(Token.ADD, name, Node.newNumber(5));
        Node result = foldExpression(expr);
        // Should not fold because left is not constant
        assertEquals(Token.ADD, result.getToken());
    }

    @Test
    public void testNonConstantRight() {
        Node name = Node.newString(Token.NAME, "y");
        Node expr = new Node(Token.ADD, Node.newNumber(3), name);
        Node result = foldExpression(expr);
        assertEquals(Token.ADD, result.getToken());
    }

    // --- Complex nested folding ---

    @Test
    public void testNestedArithmetic() {
        // (2 + 3) * 4 = 20
        Node inner = new Node(Token.ADD, Node.newNumber(2), Node.newNumber(3));
        Node expr = new Node(Token.MUL, inner, Node.newNumber(4));
        Node result = foldExpression(expr);
        assertEquals(Token.NUMBER, result.getToken());
        assertEquals(20.0, result.getDouble(), 0.0);
    }

    @Test
    public void testNestedStringConcat() {
        // "a" + ("b" + "c") = "abc"
        Node inner = new Node(Token.ADD, Node.newString("b"), Node.newString("c"));
        Node expr = new Node(Token.ADD, Node.newString("a"), inner);
        Node result = foldExpression(expr);
        assertEquals(Token.STRING, result.getToken());
        assertEquals("abc", result.getString());
    }

    // --- Potential bug triggers (Defects4J style) ---

    @Test
    public void testFoldingWithNegativeZero() {
        Node expr = new Node(Token.ADD, Node.newNumber(0), Node.newNumber(-0.0));
        Node result = foldExpression(expr);
        assertEquals(Token.NUMBER, result.getToken());
        // Should preserve negative zero? Typically 0 + (-0) = 0
        assertEquals(0.0, result.getDouble(), 0.0);
    }

    @Test
    public void testFoldingNaN() {
        Node expr = new Node(Token.ADD, Node.newNumber(Double.NaN), Node.newNumber(5));
        Node result = foldExpression(expr);
        assertEquals(Token.NUMBER, result.getToken());
        assertTrue(Double.isNaN(result.getDouble()));
    }

    @Test
    public void testFoldingInfinity() {
        Node expr = new Node(Token.SUB, Node.newNumber(Double.POSITIVE_INFINITY), Node.newNumber(Double.POSITIVE_INFINITY));
        Node result = foldExpression(expr);
        assertEquals(Token.NUMBER, result.getToken());
        assertTrue(Double.isNaN(result.getDouble()));
    }

    @Test
    public void testStringConcatWithNull() {
        Node expr = new Node(Token.ADD, Node.newString("a"), Node.newNull());
        Node result = foldExpression(expr);
        assertEquals(Token.STRING, result.getToken());
        assertEquals("anull", result.getString());
    }

    @Test
    public void testStringConcatWithUndefined() {
        Node expr = new Node(Token.ADD, Node.newString("a"), new Node(Token.VOID, Node.newNumber(0)));
        Node result = foldExpression(expr);
        assertEquals(Token.STRING, result.getToken());
        assertEquals("aundefined", result.getString());
    }

    // --- Additional edge cases for branch coverage ---

    @Test
    public void testEqSameTypeDifferentValue() {
        Node expr = new Node(Token.EQ, Node.newNumber(1), Node.newNumber(2));
        Node result = foldExpression(expr);
        assertEquals(Token.FALSE, result.getToken());
    }

    @Test
    public void testNeDifferentValues() {
        Node expr = new Node(Token.NE, Node.newNumber(1), Node.newNumber(2));
        Node result = foldExpression(expr);
        assertEquals(Token.TRUE, result.getToken());
    }

    @Test
    public void testSheqSameTypeSameValue() {
        Node expr = new Node(Token.SHEQ, Node.newNumber(5), Node.newNumber(5));
        Node result = foldExpression(expr);
        assertEquals(Token.TRUE, result.getToken());
    }

    @Test
    public void testShneSameTypeDifferentValue() {
        Node expr = new Node(Token.SHNE, Node.newNumber(5), Node.newNumber(6));
        Node result = foldExpression(expr);
        assertEquals(Token.TRUE, result.getToken());
    }

    @Test
    public void testLtEqual() {
        Node expr = new Node(Token.LE, Node.newNumber(3), Node.newNumber(3));
        Node result = foldExpression(expr);
        assertEquals(Token.TRUE, result.getToken());
    }

    @Test
    public void testGtEqual() {
        Node expr = new Node(Token.GE, Node.newNumber(5), Node.newNumber(3));
        Node result = foldExpression(expr);
        assertEquals(Token.TRUE, result.getToken());
    }

    @Test
    public void testUnaryPlus() {
        Node expr = new Node(Token.POS, Node.newNumber(-3));
        Node result = foldExpression(expr);
        assertEquals(Token.NUMBER, result.getToken());
        assertEquals(-3.0, result.getDouble(), 0.0);
    }

    @Test
    public void testUnaryTypeOf() {
        Node expr = new Node(Token.TYPEOF, Node.newNumber(42));
        Node result = foldExpression(expr);
        assertEquals(Token.STRING, result.getToken());
        assertEquals("number", result.getString());
    }

    @Test
    public void testUnaryNotNumber() {
        Node expr = new Node(Token.NOT, Node.newNumber(0));
        Node result = foldExpression(expr);
        assertEquals(Token.TRUE, result.getToken());
    }

    @Test
    public void testUnaryNotNonZero() {
        Node expr = new Node(Token.NOT, Node.newNumber(1));
        Node result = foldExpression(expr);
        assertEquals(Token.FALSE, result.getToken());
    }

    // --- Test that folding does not occur for non-constant children ---

    @Test
    public void testNoFoldWithFunctionCall() {
        Node call = new Node(Token.CALL, Node.newString(Token.NAME, "foo"));
        Node expr = new Node(Token.ADD, call, Node.newNumber(1));
        Node result = foldExpression(expr);
        assertEquals(Token.ADD, result.getToken());
    }

    @Test
    public void testNoFoldWithArrayLiteral() {
        Node array = new Node(Token.ARRAYLIT);
        Node expr = new Node(Token.ADD, array, Node.newNumber(1));
        Node result = foldExpression(expr);
        assertEquals(Token.ADD, result.getToken());
    }
}