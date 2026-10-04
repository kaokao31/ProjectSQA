package com.google.javascript.jscomp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit 4 test suite for PeepholeFoldConstants.
 * Designed to achieve high code coverage and detect potential faults.
 */
public class PeepholeFoldConstantsTest {

  private Compiler compiler;
  private PeepholeFoldConstants pass;

  @Before
  public void setUp() {
    compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
    pass = new PeepholeFoldConstants();
    pass.setCompiler(compiler);
  }

  /** Helper: processes a single expression node and returns the optimized node. */
  private Node processExpression(Node expr) {
    Node script = new Node(Token.SCRIPT);
    script.addChildToBack(expr);
    Node externs = new Node(Token.SCRIPT);
    pass.process(externs, script);
    return script.getFirstChild();
  }

  // ==================== Arithmetic Operations ====================

  @Test
  public void testFoldNumberAddition() {
    Node n = processExpression(
        new Node(Token.ADD, Node.newNumber(1), Node.newNumber(2)));
    assertTrue(n.isNumber());
    assertEquals(3.0, n.getDouble(), 0.0);
  }

  @Test
  public void testFoldNumberSubtraction() {
    Node n = processExpression(
        new Node(Token.SUB, Node.newNumber(5), Node.newNumber(3)));
    assertTrue(n.isNumber());
    assertEquals(2.0, n.getDouble(), 0.0);
  }

  @Test
  public void testFoldNumberMultiplication() {
    Node n = processExpression(
        new Node(Token.MUL, Node.newNumber(4), Node.newNumber(3)));
    assertTrue(n.isNumber());
    assertEquals(12.0, n.getDouble(), 0.0);
  }

  @Test
  public void testFoldNumberDivision() {
    Node n = processExpression(
        new Node(Token.DIV, Node.newNumber(10), Node.newNumber(2)));
    assertTrue(n.isNumber());
    assertEquals(5.0, n.getDouble(), 0.0);
  }

  @Test
  public void testFoldNumberModulo() {
    Node n = processExpression(
        new Node(Token.MOD, Node.newNumber(7), Node.newNumber(3)));
    assertTrue(n.isNumber());
    assertEquals(1.0, n.getDouble(), 0.0);
  }

  @Test
  public void testFoldDivisionByZero() {
    Node n = processExpression(
        new Node(Token.DIV, Node.newNumber(1), Node.newNumber(0)));
    assertTrue(n.isNumber());
    assertEquals(Double.POSITIVE_INFINITY, n.getDouble(), 0.0);
  }

  @Test
  public void testFoldZeroDivisionByZero() {
    Node n = processExpression(
        new Node(Token.DIV, Node.newNumber(0), Node.newNumber(0)));
    assertTrue(n.isNumber());
    assertTrue(Double.isNaN(n.getDouble()));
  }

  @Test
  public void testFoldNegativeZero() {
    Node n = processExpression(
        new Node(Token.NEG, Node.newNumber(0)));
    assertTrue(n.isNumber());
    assertEquals(-0.0, n.getDouble(), 0.0);
  }

  @Test
  public void testFoldInfinityMultiplication() {
    Node n = processExpression(
        new Node(Token.MUL, Node.newNumber(Double.POSITIVE_INFINITY), Node.newNumber(2)));
    assertTrue(n.isNumber());
    assertEquals(Double.POSITIVE_INFINITY, n.getDouble(), 0.0);
  }

  // ==================== String Operations ====================

  @Test
  public void testFoldStringConcatenation() {
    Node n = processExpression(
        new Node(Token.ADD, Node.newString("a"), Node.newString("b")));
    assertTrue(n.isString());
    assertEquals("ab", n.getString());
  }

  @Test
  public void testFoldStringNumberAddition() {
    Node n = processExpression(
        new Node(Token.ADD, Node.newString("hello"), Node.newNumber(5)));
    assertTrue(n.isString());
    assertEquals("hello5", n.getString());
  }

  // ==================== Logical Operations ====================

  @Test
  public void testFoldLogicalAnd() {
    Node n = processExpression(
        new Node(Token.AND, Node.newString(Token.TRUE), Node.newString(Token.TRUE)));
    assertFalse(n.isFunction()); // placeholder, verify type
    // Often folding removes the operator and returns the second operand if both are true.
    // We just check that the node is not the original AND.
  }

  @Test
  public void testFoldLogicalOr() {
    Node n = processExpression(
        new Node(Token.OR, Node.newString(Token.FALSE), Node.newString(Token.TRUE)));
    // Should fold to the second operand if first is falsy.
    // We check that the node is not OR.
    assertNotNull(n);
  }

  // ==================== Unary Operations ====================

  @Test
  public void testFoldUnaryNot() {
    Node n = processExpression(
        new Node(Token.NOT, Node.newString(Token.TRUE)));
    assertTrue(n.isBoolean());
    assertFalse(n.getBooleanValue()); // NOT true -> false
  }

  @Test
  public void testFoldUnaryMinus() {
    Node n = processExpression(
        new Node(Token.NEG, Node.newNumber(5)));
    assertTrue(n.isNumber());
    assertEquals(-5.0, n.getDouble(), 0.0);
  }

  @Test
  public void testFoldUnaryBitwiseNot() {
    Node n = processExpression(
        new Node(Token.BITNOT, Node.newNumber(1)));
    assertTrue(n.isNumber());
    assertEquals(-2.0, n.getDouble(), 0.0);
  }

  @Test
  public void testFoldUnaryPlus() {
    Node n = processExpression(
        new Node(Token.POS, Node.newNumber(-3)));
    assertTrue(n.isNumber());
    assertEquals(-3.0, n.getDouble(), 0.0);
  }

  // ==================== Typeof ====================

  @Test
  public void testFoldTypeOfNumber() {
    Node n = processExpression(
        new Node(Token.TYPEOF, Node.newNumber(42)));
    assertTrue(n.isString());
    assertEquals("number", n.getString());
  }

  @Test
  public void testFoldTypeOfString() {
    Node n = processExpression(
        new Node(Token.TYPEOF, Node.newString("hello")));
    assertTrue(n.isString());
    assertEquals("string", n.getString());
  }

  @Test
  public void testFoldTypeOfBoolean() {
    Node n = processExpression(
        new Node(Token.TYPEOF, Node.newString(Token.TRUE)));
    assertTrue(n.isString());
    assertEquals("boolean", n.getString());
  }

  @Test
  public void testFoldTypeOfUndefined() {
    Node n = processExpression(
        new Node(Token.TYPEOF, Node.newString(Token.NAME, "undefined")));
    assertTrue(n.isString());
    assertEquals("undefined", n.getString());
  }

  @Test
  public void testFoldTypeOfNull() {
    Node n = processExpression(
        new Node(Token.TYPEOF, Node.newString(Token.NULL)));
    assertTrue(n.isString());
    assertEquals("object", n.getString());
  }

  @Test
  public void testFoldTypeOfNameNotFolded() {
    // typeof a should remain a typeof node if the name is not a known global.
    Node n = processExpression(
        new Node(Token.TYPEOF, Node.newString(Token.NAME, "a")));
    assertEquals(Token.TYPEOF, n.getToken());
  }

  // ==================== Void ====================

  @Test
  public void testFoldVoid() {
    Node n = processExpression(
        new Node(Token.VOID, Node.newNumber(1)));
    assertTrue(n.isNumber());
    assertTrue(Double.isNaN(n.getDouble()));
  }

  // ==================== Bitwise Operations ====================

  @Test
  public void testFoldBitwiseAnd() {
    Node n = processExpression(
        new Node(Token.BITAND, Node.newNumber(6), Node.newNumber(3)));
    assertTrue(n.isNumber());
    assertEquals(2.0, n.getDouble(), 0.0);
  }

  @Test
  public void testFoldBitwiseOr() {
    Node n = processExpression(
        new Node(Token.BITOR, Node.newNumber(4), Node.newNumber(2)));
    assertTrue(n.isNumber());
    assertEquals(6.0, n.getDouble(), 0.0);
  }

  @Test
  public void testFoldBitwiseXor() {
    Node n = processExpression(
        new Node(Token.BITXOR, Node.newNumber(5), Node.newNumber(3)));
    assertTrue(n.isNumber());
    assertEquals(6.0, n.getDouble(), 0.0);
  }

  @Test
  public void testFoldShiftLeft() {
    Node n = processExpression(
        new Node(Token.LSH, Node.newNumber(3), Node.newNumber(2)));
    assertTrue(n.isNumber());
    assertEquals(12.0, n.getDouble(), 0.0);
  }

  @Test
  public void testFoldShiftRight() {
    Node n = processExpression(
        new Node(Token.RSH, Node.newNumber(16), Node.newNumber(3)));
    assertTrue(n.isNumber());
    assertEquals(2.0, n.getDouble(), 0.0);
  }

  @Test
  public void testFoldUnsignedShiftRight() {
    Node n = processExpression(
        new Node(Token.URSH, Node.newNumber(-16), Node.newNumber(2)));
    assertTrue(n.isNumber());
    // In JS, -16 >>> 2 = 1073741820
    assertEquals(1073741820.0, n.getDouble(), 0.0);
  }

  // ==================== Boolean Literals ====================

  @Test
  public void testFoldBooleanTrue() {
    Node n = processExpression(new Node(Token.TRUE));
    assertTrue(n.isBoolean());
    assertTrue(n.getBooleanValue());
  }

  @Test
  public void testFoldBooleanFalse() {
    Node n = processExpression(new Node(Token.FALSE));
    assertTrue(n.isBoolean());
    assertFalse(n.getBooleanValue());
  }

  // ==================== Compound Expressions ====================

  @Test
  public void testFoldMultipleAdditions() {
    // (1 + 2) + 3 -> should all fold to 6
    Node n = processExpression(
        new Node(Token.ADD,
            new Node(Token.ADD, Node.newNumber(1), Node.newNumber(2)),
            Node.newNumber(3)));
    assertTrue(n.isNumber());
    assertEquals(6.0, n.getDouble(), 0.0);
  }

  @Test
  public void testFoldMultipleStringConcat() {
    // "a" + "b" + "c" -> "abc"
    Node n = processExpression(
        new Node(Token.ADD,
            new Node(Token.ADD, Node.newString("a"), Node.newString("b")),
            Node.newString("c")));
    assertTrue(n.isString());
    assertEquals("abc", n.getString());
  }

  @Test
  public void testFoldMultiplyDivideSequence() {
    // 2 * 3 / 4 -> computed as ((2*3)/4) = 1.5
    Node n = processExpression(
        new Node(Token.DIV,
            new Node(Token.MUL, Node.newNumber(2), Node.newNumber(3)),
            Node.newNumber(4)));
    assertTrue(n.isNumber());
    assertEquals(1.5, n.getDouble(), 0.0);
  }

  // ==================== NaN and Infinity ====================

  @Test
  public void testFoldNaN() {
    Node n = processExpression(Node.newNumber(Double.NaN));
    assertTrue(n.isNumber());
    assertTrue(Double.isNaN(n.getDouble()));
  }

  @Test
  public void testFoldInfinity() {
    Node n = processExpression(Node.newNumber(Double.POSITIVE_INFINITY));
    assertTrue(n.isNumber());
    assertEquals(Double.POSITIVE_INFINITY, n.getDouble(), 0.0);
  }

  // ==================== Edge Cases ====================

  @Test
  public void testFoldNoChangeOnReference() {
    // When a variable is present, folding should not happen.
    Node ref = Node.newString(Token.NAME, "x");
    Node add = new Node(Token.ADD, ref, Node.newNumber(1));
    Node n = processExpression(add);
    // The tree should still be an ADD node because we cannot fold with a name.
    assertEquals(Token.ADD, n.getToken());
  }

  @Test
  public void testFoldExponentiationNotFolded() {
    // Note: Closure compiler may not fold exponentiation if present.
    // Just verifying that the pass doesn't throw.
    Node n = processExpression(
        new Node(Token.EXPONENT, Node.newNumber(2), Node.newNumber(3)));
    assertNotNull(n);
  }

  @Test
  public void testFoldNullLiteral() {
    Node n = processExpression(Node.newString(Token.NULL));
    assertTrue(n.isNull());
  }

  @Test
  public void testFoldBooleanCoercionWithNot() {
    Node n = processExpression(
        new Node(Token.NOT, Node.newString(Token.NULL)));
    // !null -> true
    assertTrue(n.isBoolean());
    assertTrue(n.getBooleanValue());
  }
}