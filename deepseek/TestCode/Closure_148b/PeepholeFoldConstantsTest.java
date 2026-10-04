package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test suite for PeepholeFoldConstants.
 * Targets high code coverage and fault detection for Closure Bug #148.
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

  // Helper to create a simple number node
  private Node numberNode(double value) {
    return Node.newNumber(value);
  }

  // Helper to create a string node
  private Node stringNode(String value) {
    return Node.newString(value);
  }

  // Helper to create an ADD node with two children
  private Node addNode(Node left, Node right) {
    return new Node(Token.ADD, left, right);
  }

  // Helper to create a SUB node
  private Node subNode(Node left, Node right) {
    return new Node(Token.SUB, left, right);
  }

  // Helper to create a MUL node
  private Node mulNode(Node left, Node right) {
    return new Node(Token.MUL, left, right);
  }

  // Helper to create a DIV node
  private Node divNode(Node left, Node right) {
    return new Node(Token.DIV, left, right);
  }

  // Helper to create a MOD node
  private Node modNode(Node left, Node right) {
    return new Node(Token.MOD, left, right);
  }

  // Helper to create a NEG node
  private Node negNode(Node child) {
    return new Node(Token.NEG, child);
  }

  // Helper to create a BITNOT node
  private Node bitNotNode(Node child) {
    return new Node(Token.BITNOT, child);
  }

  // Helper to create a POS node
  private Node posNode(Node child) {
    return new Node(Token.POS, child);
  }

  // Helper to create a NOT node
  private Node notNode(Node child) {
    return new Node(Token.NOT, child);
  }

  // Helper to create an AND node
  private Node andNode(Node left, Node right) {
    return new Node(Token.AND, left, right);
  }

  // Helper to create an OR node
  private Node orNode(Node left, Node right) {
    return new Node(Token.OR, left, right);
  }

  // Helper to create a HOOK (ternary) node
  private Node hookNode(Node cond, Node trueExpr, Node falseExpr) {
    return new Node(Token.HOOK, cond, trueExpr, falseExpr);
  }

  // Helper to create a COMMA node
  private Node commaNode(Node left, Node right) {
    return new Node(Token.COMMA, left, right);
  }

  // Helper to create a TYPEOF node
  private Node typeofNode(Node child) {
    return new Node(Token.TYPEOF, child);
  }

  // Helper to create an EQ node
  private Node eqNode(Node left, Node right) {
    return new Node(Token.EQ, left, right);
  }

  // Helper to create a NE node
  private Node neNode(Node left, Node right) {
    return new Node(Token.NE, left, right);
  }

  // Helper to create a SHEQ node
  private Node sheqNode(Node left, Node right) {
    return new Node(Token.SHEQ, left, right);
  }

  // Helper to create a SHNE node
  private Node shneNode(Node left, Node right) {
    return new Node(Token.SHNE, left, right);
  }

  // Helper to create a LT node
  private Node ltNode(Node left, Node right) {
    return new Node(Token.LT, left, right);
  }

  // Helper to create a LE node
  private Node leNode(Node left, Node right) {
    return new Node(Token.LE, left, right);
  }

  // Helper to create a GT node
  private Node gtNode(Node left, Node right) {
    return new Node(Token.GT, left, right);
  }

  // Helper to create a GE node
  private Node geNode(Node left, Node right) {
    return new Node(Token.GE, left, right);
  }

  // Helper to create an ARRAYLIT node
  private Node arrayLitNode(Node... elements) {
    Node array = new Node(Token.ARRAYLIT);
    for (Node element : elements) {
      array.addChildToBack(element);
    }
    return array;
  }

  // Helper to create an OBJECTLIT node
  private Node objectLitNode(Node... props) {
    Node obj = new Node(Token.OBJECTLIT);
    for (Node prop : props) {
      obj.addChildToBack(prop);
    }
    return obj;
  }

  // Helper to create a STRING_KEY node
  private Node stringKeyNode(String name, Node value) {
    Node key = Node.newString(Token.STRING_KEY, name);
    key.addChildToBack(value);
    return key;
  }

  // Helper to call optimizeSubtree
  private Node optimize(Node n) {
    return foldConstants.optimizeSubtree(n);
  }

  // ==============================
  // Tests for Arithmetic Folding
  // ==============================

  @Test
  public void testAddNumbers() {
    Node add = addNode(numberNode(2), numberNode(3));
    Node result = optimize(add);
    assertTrue(result.isNumber());
    assertEquals(5.0, result.getDouble(), 0.0);
  }

  @Test
  public void testAddStringNumber() {
    Node add = addNode(stringNode("a"), numberNode(2));
    Node result = optimize(add);
    assertTrue(result.isString());
    assertEquals("a2", result.getString());
  }

  @Test
  public void testAddNumberString() {
    Node add = addNode(numberNode(2), stringNode("a"));
    Node result = optimize(add);
    assertTrue(result.isString());
    assertEquals("2a", result.getString());
  }

  @Test
  public void testAddStringString() {
    Node add = addNode(stringNode("hello"), stringNode(" world"));
    Node result = optimize(add);
    assertTrue(result.isString());
    assertEquals("hello world", result.getString());
  }

  @Test
  public void testAddBooleanNumber() {
    Node add = addNode(new Node(Token.TRUE), numberNode(1));
    Node result = optimize(add);
    assertTrue(result.isNumber());
    assertEquals(2.0, result.getDouble(), 0.0);
  }

  @Test
  public void testAddNullNumber() {
    Node add = addNode(new Node(Token.NULL), numberNode(5));
    Node result = optimize(add);
    assertTrue(result.isNumber());
    assertEquals(5.0, result.getDouble(), 0.0);
  }

  @Test
  public void testAddUndefinedNumber() {
    Node add = addNode(new Node(Token.VOID, numberNode(0)), numberNode(5));
    Node result = optimize(add);
    assertTrue(Double.isNaN(result.getDouble()));
  }

  @Test
  public void testSubtractNumbers() {
    Node sub = subNode(numberNode(10), numberNode(3));
    Node result = optimize(sub);
    assertTrue(result.isNumber());
    assertEquals(7.0, result.getDouble(), 0.0);
  }

  @Test
  public void testMultiplyNumbers() {
    Node mul = mulNode(numberNode(4), numberNode(5));
    Node result = optimize(mul);
    assertTrue(result.isNumber());
    assertEquals(20.0, result.getDouble(), 0.0);
  }

  @Test
  public void testDivideNumbers() {
    Node div = divNode(numberNode(10), numberNode(4));
    Node result = optimize(div);
    assertTrue(result.isNumber());
    assertEquals(2.5, result.getDouble(), 0.0);
  }

  @Test
  public void testDivideByZero() {
    Node div = divNode(numberNode(1), numberNode(0));
    Node result = optimize(div);
    assertTrue(result.isNumber());
    assertTrue(Double.isInfinite(result.getDouble()));
  }

  @Test
  public void testModNumbers() {
    Node mod = modNode(numberNode(10), numberNode(3));
    Node result = optimize(mod);
    assertTrue(result.isNumber());
    assertEquals(1.0, result.getDouble(), 0.0);
  }

  @Test
  public void testNegNumber() {
    Node neg = negNode(numberNode(5));
    Node result = optimize(neg);
    assertTrue(result.isNumber());
    assertEquals(-5.0, result.getDouble(), 0.0);
  }

  @Test
  public void testPosNumber() {
    Node pos = posNode(numberNode(-3));
    Node result = optimize(pos);
    assertTrue(result.isNumber());
    assertEquals(-3.0, result.getDouble(), 0.0);
  }

  @Test
  public void testBitNotNumber() {
    Node bitNot = bitNotNode(numberNode(5));
    Node result = optimize(bitNot);
    assertTrue(result.isNumber());
    assertEquals(-6.0, result.getDouble(), 0.0);
  }

  @Test
  public void testNotBoolean() {
    Node not = notNode(new Node(Token.TRUE));
    Node result = optimize(not);
    assertTrue(result.isBoolean());
    assertFalse(result.getBoolean());
  }

  @Test
  public void testNotNumber() {
    Node not = notNode(numberNode(0));
    Node result = optimize(not);
    assertTrue(result.isBoolean());
    assertTrue(result.getBoolean());
  }

  @Test
  public void testNotString() {
    Node not = notNode(stringNode(""));
    Node result = optimize(not);
    assertTrue(result.isBoolean());
    assertTrue(result.getBoolean());
  }

  // ==============================
  // Tests for Comparison Folding
  // ==============================

  @Test
  public void testEqNumbers() {
    Node eq = eqNode(numberNode(2), numberNode(2));
    Node result = optimize(eq);
    assertTrue(result.isBoolean());
    assertTrue(result.getBoolean());
  }

  @Test
  public void testEqDifferentNumbers() {
    Node eq = eqNode(numberNode(2), numberNode(3));
    Node result = optimize(eq);
    assertTrue(result.isBoolean());
    assertFalse(result.getBoolean());
  }

  @Test
  public void testNeNumbers() {
    Node ne = neNode(numberNode(2), numberNode(3));
    Node result = optimize(ne);
    assertTrue(result.isBoolean());
    assertTrue(result.getBoolean());
  }

  @Test
  public void testSheqNumbers() {
    Node sheq = sheqNode(numberNode(2), numberNode(2));
    Node result = optimize(sheq);
    assertTrue(result.isBoolean());
    assertTrue(result.getBoolean());
  }

  @Test
  public void testSheqDifferentTypes() {
    Node sheq = sheqNode(numberNode(2), stringNode("2"));
    Node result = optimize(sheq);
    assertTrue(result.isBoolean());
    assertFalse(result.getBoolean());
  }

  @Test
  public void testShneNumbers() {
    Node shne = shneNode(numberNode(2), numberNode(3));
    Node result = optimize(shne);
    assertTrue(result.isBoolean());
    assertTrue(result.getBoolean());
  }

  @Test
  public void testLtNumbers() {
    Node lt = ltNode(numberNode(2), numberNode(3));
    Node result = optimize(lt);
    assertTrue(result.isBoolean());
    assertTrue(result.getBoolean());
  }

  @Test
  public void testLeNumbers() {
    Node le = leNode(numberNode(3), numberNode(3));
    Node result = optimize(le);
    assertTrue(result.isBoolean());
    assertTrue(result.getBoolean());
  }

  @Test
  public void testGtNumbers() {
    Node gt = gtNode(numberNode(4), numberNode(3));
    Node result = optimize(gt);
    assertTrue(result.isBoolean());
    assertTrue(result.getBoolean());
  }

  @Test
  public void testGeNumbers() {
    Node ge = geNode(numberNode(3), numberNode(3));
    Node result = optimize(ge);
    assertTrue(result.isBoolean());
    assertTrue(result.getBoolean());
  }

  @Test
  public void testLtStringCompare() {
    Node lt = ltNode(stringNode("apple"), stringNode("banana"));
    Node result = optimize(lt);
    assertTrue(result.isBoolean());
    assertTrue(result.getBoolean());
  }

  @Test
  public void testLtNullUndefined() {
    Node lt = ltNode(new Node(Token.NULL), new Node(Token.VOID, numberNode(0)));
    Node result = optimize(lt);
    assertTrue(result.isBoolean());
    assertFalse(result.getBoolean()); // null < undefined -> false
  }

  // ==============================
  // Tests for Boolean Logic Folding
  // ==============================

  @Test
  public void testAndTrueTrue() {
    Node and = andNode(new Node(Token.TRUE), new Node(Token.TRUE));
    Node result = optimize(and);
    assertTrue(result.isBoolean());
    assertTrue(result.getBoolean());
  }

  @Test
  public void testAndFalseAnything() {
    Node and = andNode(new Node(Token.FALSE), new Node(Token.TRUE));
    Node result = optimize(and);
    assertTrue(result.isBoolean());
    assertFalse(result.getBoolean());
  }

  @Test
  public void testAndNumberNonZero() {
    Node and = andNode(numberNode(1), numberNode(2));
    // Should evaluate to second operand (2)
    Node result = optimize(and);
    assertTrue(result.isNumber());
    assertEquals(2.0, result.getDouble(), 0.0);
  }

  @Test
  public void testOrTrueAnything() {
    Node or = orNode(new Node(Token.TRUE), new Node(Token.FALSE));
    Node result = optimize(or);
    assertTrue(result.isBoolean());
    assertTrue(result.getBoolean());
  }

  @Test
  public void testOrFalseFalse() {
    Node or = orNode(new Node(Token.FALSE), new Node(Token.FALSE));
    Node result = optimize(or);
    assertTrue(result.isBoolean());
    assertFalse(result.getBoolean());
  }

  @Test
  public void testOrStringEmpty() {
    Node or = orNode(stringNode(""), stringNode("nonempty"));
    Node result = optimize(or);
    assertTrue(result.isString());
    assertEquals("nonempty", result.getString());
  }

  // ==============================
  // Tests for Ternary Operator Folding
  // ==============================

  @Test
  public void testHookTrueBranch() {
    Node hook = hookNode(new Node(Token.TRUE), numberNode(1), numberNode(2));
    Node result = optimize(hook);
    assertTrue(result.isNumber());
    assertEquals(1.0, result.getDouble(), 0.0);
  }

  @Test
  public void testHookFalseBranch() {
    Node hook = hookNode(new Node(Token.FALSE), numberNode(1), numberNode(2));
    Node result = optimize(hook);
    assertTrue(result.isNumber());
    assertEquals(2.0, result.getDouble(), 0.0);
  }

  // ==============================
  // Tests for Comma Operator Folding
  // ==============================

  @Test
  public void testCommaFolding() {
    Node comma = commaNode(numberNode(1), numberNode(2));
    Node result = optimize(comma);
    assertTrue(result.isNumber());
    assertEquals(2.0, result.getDouble(), 0.0);
  }

  // ==============================
  // Tests for Typeof Folding
  // ==============================

  @Test
  public void testTypeofNumber() {
    Node typeof = typeofNode(numberNode(5));
    Node result = optimize(typeof);
    assertTrue(result.isString());
    assertEquals("number", result.getString());
  }

  @Test
  public void testTypeofString() {
    Node typeof = typeofNode(stringNode("hello"));
    Node result = optimize(typeof);
    assertTrue(result.isString());
    assertEquals("string", result.getString());
  }

  @Test
  public void testTypeofUndefined() {
    Node typeof = typeofNode(new Node(Token.VOID, numberNode(0)));
    Node result = optimize(typeof);
    assertTrue(result.isString());
    assertEquals("undefined", result.getString());
  }

  @Test
  public void testTypeofNull() {
    Node typeof = typeofNode(new Node(Token.NULL));
    Node result = optimize(typeof);
    assertTrue(result.isString());
    assertEquals("object", result.getString());
  }

  // ==============================
  // Tests for Bitwise Operators
  // ==============================

  @Test
  public void testBitAnd() {
    Node bitAnd = new Node(Token.BITAND, numberNode(5), numberNode(3));
    Node result = optimize(bitAnd);
    assertTrue(result.isNumber());
    assertEquals(1.0, result.getDouble(), 0.0);
  }

  @Test
  public void testBitOr() {
    Node bitOr = new Node(Token.BITOR, numberNode(5), numberNode(3));
    Node result = optimize(bitOr);
    assertTrue(result.isNumber());
    assertEquals(7.0, result.getDouble(), 0.0);
  }

  @Test
  public void testBitXor() {
    Node bitXor = new Node(Token.BITXOR, numberNode(5), numberNode(3));
    Node result = optimize(bitXor);
    assertTrue(result.isNumber());
    assertEquals(6.0, result.getDouble(), 0.0);
  }

  @Test
  public void testShiftLeft() {
    Node shift = new Node(Token.LSH, numberNode(5), numberNode(2));
    Node result = optimize(shift);
    assertTrue(result.isNumber());
    assertEquals(20.0, result.getDouble(), 0.0);
  }

  @Test
  public void testShiftRightSigned() {
    Node shift = new Node(Token.RSH, numberNode(-20), numberNode(2));
    Node result = optimize(shift);
    assertTrue(result.isNumber());
    assertEquals(-5.0, result.getDouble(), 0.0);
  }

  @Test
  public void testShiftRightUnsigned() {
    Node shift = new Node(Token.URSH, numberNode(-20), numberNode(2));
    Node result = optimize(shift);
    assertTrue(result.isNumber());
    assertEquals(1073741819.0, result.getDouble(), 0.0);
  }

  // ==============================
  // Tests for Array Literal Folding
  // ==============================

  @Test
  public void testArrayLiteral() {
    Node arr = arrayLitNode(numberNode(1), stringNode("a"), new Node(Token.TRUE));
    Node result = optimize(arr);
    // Array literals are not typically folded completely, but may be optimized
    // For now, ensure no crash and structure remains
    assertNotNull(result);
    assertEquals(Token.ARRAYLIT, result.getToken());
  }

  // ==============================
  // Tests for Object Literal Folding
  // ==============================

  @Test
  public void testObjectLiteral() {
    Node obj = objectLitNode(stringKeyNode("key", numberNode(42)));
    Node result = optimize(obj);
    assertNotNull(result);
    assertEquals(Token.OBJECTLIT, result.getToken());
  }

  // ==============================
  // Edge Cases and Bug Triggers
  // ==============================

  @Test
  public void testFoldingNaN() {
    Node add = addNode(numberNode(Double.NaN), numberNode(5));
    Node result = optimize(add);
    assertTrue(result.isNumber());
    assertTrue(Double.isNaN(result.getDouble()));
  }

  @Test
  public void testFoldingInfinity() {
    Node add = addNode(numberNode(Double.POSITIVE_INFINITY), numberNode(1));
    Node result = optimize(add);
    assertTrue(result.isNumber());
    assertTrue(Double.isInfinite(result.getDouble()));
  }

  @Test
  public void testFoldingNegativeZero() {
    Node add = addNode(numberNode(-0.0), numberNode(0.0));
    Node result = optimize(add);
    assertTrue(result.isNumber());
    assertEquals(0.0, result.getDouble(), 0.0);
  }

  @Test
  public void testFoldingLargeNumber() {
    Node add = addNode(numberNode(Double.MAX_VALUE), numberNode(Double.MAX_VALUE));
    Node result = optimize(add);
    assertTrue(result.isNumber());
    assertTrue(Double.isInfinite(result.getDouble()));
  }

  @Test
  public void testStringConcatWithEmpty() {
    Node add = addNode(stringNode(""), stringNode("test"));
    Node result = optimize(add);
    assertTrue(result.isString());
    assertEquals("test", result.getString());
  }

  @Test
  public void testSubtractStringToNumber() {
    Node sub = subNode(stringNode("10"), stringNode("5"));
    Node result = optimize(sub);
    assertTrue(result.isNumber());
    assertEquals(5.0, result.getDouble(), 0.0);
  }

  @Test
  public void testMultiplyStringNumber() {
    Node mul = mulNode(stringNode("6"), numberNode(7));
    Node result = optimize(mul);
    assertTrue(result.isNumber());
    assertEquals(42.0, result.getDouble(), 0.0);
  }

  @Test
  public void testDivideNonNumericString() {
    Node div = divNode(stringNode("abc"), numberNode(2));
    Node result = optimize(div);
    assertTrue(Double.isNaN(result.getDouble()));
  }

  @Test
  public void testModZero() {
    Node mod = modNode(numberNode(5), numberNode(0));
    Node result = optimize(mod);
    assertTrue(Double.isNaN(result.getDouble()));
  }

  @Test
  public void testNegNaN() {
    Node neg = negNode(numberNode(Double.NaN));
    Node result = optimize(neg);
    assertTrue(Double.isNaN(result.getDouble()));
  }

  @Test
  public void testBitwiseOperandsCoercion() {
    Node bitAnd = new Node(Token.BITAND, stringNode("10"), stringNode("6"));
    Node result = optimize(bitAnd);
    assertTrue(result.isNumber());
    assertEquals(2.0, result.getDouble(), 0.0);
  }

  @Test
  public void testLogicalAndWithString() {
    Node and = andNode(stringNode(""), numberNode(0));
    Node result = optimize(and);
    assertTrue(result.isString());
    assertEquals("", result.getString());
  }

  @Test
  public void testLogicalOrWithString() {
    Node or = orNode(stringNode(""), numberNode(0));
    Node result = optimize(or);
    assertTrue(result.isNumber());
    assertEquals(0.0, result.getDouble(), 0.0);
  }

  @Test
  public void testTernaryWithBooleanCondition() {
    Node hook = hookNode(new Node(Token.FALSE), new Node(Token.TRUE), new Node(Token.FALSE));
    Node result = optimize(hook);
    assertTrue(result.isBoolean());
    assertFalse(result.getBoolean());
  }

  @Test
  public void testCommaWithSideEffects() {
    Node comma = commaNode(new Node(Token.INC, numberNode(0)), numberNode(5));
    // Increment has side effects, so folding should not happen
    Node result = optimize(comma);
    assertEquals(Token.COMMA, result.getToken());
  }

  @Test
  public void testTypeofWithUndefinedExpression() {
    Node typeof = typeofNode(new Node(Token.NAME, "undefinedVar"));
    // Should remain typeof expression
    Node result = optimize(typeof);
    assertEquals(Token.TYPEOF, result.getToken());
  }

  @Test
  public void testComparisonWithNaN() {
    Node lt = ltNode(numberNode(Double.NaN), numberNode(5));
    Node result = optimize(lt);
    assertTrue(result.isBoolean());
    assertFalse(result.getBoolean());
  }

  @Test
  public void testSheqWithNullUndefined() {
    Node sheq = sheqNode(new Node(Token.NULL), new Node(Token.VOID, numberNode(0)));
    Node result = optimize(sheq);
    assertTrue(result.isBoolean());
    assertFalse(result.getBoolean());
  }

  // Additional edge cases for code coverage
  @Test
  public void testBitwiseOrWithNegative() {
    Node bitOr = new Node(Token.BITOR, numberNode(-1), numberNode(0));
    Node result = optimize(bitOr);
    assertTrue(result.isNumber());
    assertEquals(-1.0, result.getDouble(), 0.0);
  }

  @Test
  public void testBitwiseXorWithSame() {
    Node bitXor = new Node(Token.BITXOR, numberNode(7), numberNode(7));
    Node result = optimize(bitXor);
    assertTrue(result.isNumber());
    assertEquals(0.0, result.getDouble(), 0.0);
  }

  @Test
  public void testShiftLeftByZero() {
    Node shift = new Node(Token.LSH, numberNode(5), numberNode(0));
    Node result = optimize(shift);
    assertTrue(result.isNumber());
    assertEquals(5.0, result.getDouble(), 0.0);
  }

  @Test
  public void testShiftRightByLarge() {
    Node shift = new Node(Token.RSH, numberNode(1), numberNode(32));
    Node result = optimize(shift);
    assertTrue(result.isNumber());
    assertEquals(1.0, result.getDouble(), 0.0); // shift by 32 is effectively 0
  }

  @Test
  public void testUnaryPlusString() {
    Node pos = posNode(stringNode("123"));
    Node result = optimize(pos);
    assertTrue(result.isNumber());
    assertEquals(123.0, result.getDouble(), 0.0);
  }

  @Test
  public void testUnaryMinusString() {
    Node neg = negNode(stringNode("456"));
    Node result = optimize(neg);
    assertTrue(result.isNumber());
    assertEquals(-456.0, result.getDouble(), 0.0);
  }

  // Test that optimization does not crash on null child
  @Test(expected = NullPointerException.class)
  public void testNullChild() {
    Node node = new Node(Token.ADD, null, numberNode(1));
    optimize(node);
  }

  // Test that optimization does not crash on missing child
  @Test(expected = NullPointerException.class)
  public void testMissingChild() {
    Node node = new Node(Token.SUB, numberNode(1));
    optimize(node);
  }
}