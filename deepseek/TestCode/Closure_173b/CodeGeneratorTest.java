package com.google.javascript.jscomp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link CodeGenerator} covering diverse expression and statement
 * generation scenarios, including edge cases and potential bug triggers.
 */
public class CodeGeneratorTest {

  private CodeGenerator codeGenerator;

  @Before
  public void setUp() {
    // Using a real Compiler instance to avoid potential NPEs if the generator
    // relies on compiler context.
    codeGenerator = new CodeGenerator(new Compiler());
  }

  /** Helper to generate code from a node. */
  private String generate(Node node) {
    return codeGenerator.generate(node);
  }

  // ----- Number literals -----

  @Test
  public void testSimpleNumber() {
    assertEquals("1", generate(Node.newNumber(1)));
  }

  @Test
  public void testNegativeNumber() {
    assertEquals("-1.5", generate(Node.newNumber(-1.5)));
  }

  @Test
  public void testZeroNumber() {
    assertEquals("0", generate(Node.newNumber(0)));
  }

  @Test
  public void testNegativeZeroNumber() {
    // -0 should be represented as "-0" to preserve semantics.
    assertEquals("-0", generate(Node.newNumber(-0.0)));
  }

  @Test
  public void testLargeNumber() {
    assertEquals("1e21", generate(Node.newNumber(1e21)));
  }

  @Test
  public void testFractionNumber() {
    assertEquals("0.1", generate(Node.newNumber(0.1)));
  }

  // ----- String literals -----

  @Test
  public void testSimpleString() {
    assertEquals("\"hello\"", generate(Node.newString("hello")));
  }

  @Test
  public void testEmptyString() {
    assertEquals("\"\"", generate(Node.newString("")));
  }

  @Test
  public void testStringWithDoubleQuote() {
    assertEquals("\"a\\\"b\"", generate(Node.newString("a\"b")));
  }

  @Test
  public void testStringWithBackslash() {
    assertEquals("\"a\\\\b\"", generate(Node.newString("a\\b")));
  }

  @Test
  public void testStringWithNewline() {
    assertEquals("\"a\\nb\"", generate(Node.newString("a\nb")));
  }

  @Test
  public void testStringWithTab() {
    assertEquals("\"a\\tb\"", generate(Node.newString("a\tb")));
  }

  @Test
  public void testStringWithUnicode() {
    assertEquals("\"\\u00e9\"", generate(Node.newString("\u00e9")));
  }

  // ----- Boolean and null -----

  @Test
  public void testTrue() {
    assertEquals("true", generate(Node.newTrueLiteral()));
  }

  @Test
  public void testFalse() {
    assertEquals("false", generate(Node.newFalseLiteral()));
  }

  @Test
  public void testNull() {
    assertEquals("null", generate(Node.newNull()));
  }

  // ----- Name references -----

  @Test
  public void testName() {
    assertEquals("foo", generate(Node.newName("foo")));
  }

  @Test
  public void testNameWithDollar() {
    assertEquals("$", generate(Node.newName("$")));
  }

  // ----- Binary operations -----

  @Test
  public void testSimpleAddition() {
    Node left = Node.newNumber(1);
    Node right = Node.newNumber(2);
    assertEquals("1 + 2", generate(Node.newBinOp(Token.ADD, left, right)));
  }

  @Test
  public void testAdditionWithParentheses() {
    // (1 + 2) * 3
    Node addNode = Node.newBinOp(Token.ADD, Node.newNumber(1), Node.newNumber(2));
    Node mulNode = Node.newBinOp(Token.MUL, addNode, Node.newNumber(3));
    assertEquals("(1 + 2) * 3", generate(mulNode));
  }

  @Test
  public void testOperatorPrecedence() {
    // 1 + 2 * 3 should not have parentheses for multiplication.
    Node mul = Node.newBinOp(Token.MUL, Node.newNumber(2), Node.newNumber(3));
    Node add = Node.newBinOp(Token.ADD, Node.newNumber(1), mul);
    assertEquals("1 + 2 * 3", generate(add));
  }

  @Test
  public void testDivision() {
    Node left = Node.newNumber(1);
    Node right = Node.newNumber(2);
    assertEquals("1 / 2", generate(Node.newBinOp(Token.DIV, left, right)));
  }

  @Test
  public void testModulo() {
    Node left = Node.newNumber(5);
    Node right = Node.newNumber(2);
    assertEquals("5 % 2", generate(Node.newBinOp(Token.MOD, left, right)));
  }

  @Test
  public void testLeftShift() {
    Node left = Node.newNumber(1);
    Node right = Node.newNumber(2);
    assertEquals("1 << 2", generate(Node.newBinOp(Token.SHL, left, right)));
  }

  @Test
  public void testSignedRightShift() {
    Node left = Node.newNumber(8);
    Node right = Node.newNumber(1);
    assertEquals("8 >> 1", generate(Node.newBinOp(Token.SHR, left, right)));
  }

  @Test
  public void testUnsignedRightShift() {
    Node left = Node.newNumber(8);
    Node right = Node.newNumber(1);
    assertEquals("8 >>> 1", generate(Node.newBinOp(Token.USHR, left, right)));
  }

  @Test
  public void testLessThan() {
    Node left = Node.newNumber(1);
    Node right = Node.newNumber(2);
    assertEquals("1 < 2", generate(Node.newBinOp(Token.LT, left, right)));
  }

  @Test
  public void testGreaterThan() {
    Node left = Node.newNumber(2);
    Node right = Node.newNumber(1);
    assertEquals("2 > 1", generate(Node.newBinOp(Token.GT, left, right)));
  }

  @Test
  public void testEquality() {
    Node left = Node.newNumber(1);
    Node right = Node.newNumber(1);
    assertEquals("1 == 1", generate(Node.newBinOp(Token.EQ, left, right)));
  }

  @Test
  public void testStrictEqualityNoParentheses() {
    Node left = Node.newNumber(1);
    Node right = Node.newNumber(1);
    assertEquals("1 === 1", generate(Node.newBinOp(Token.SHEQ, left, right)));
  }

  @Test
  public void testLogicalAnd() {
    Node left = Node.newName("a");
    Node right = Node.newName("b");
    assertEquals("a && b", generate(Node.newBinOp(Token.AND, left, right)));
  }

  @Test
  public void testLogicalOr() {
    Node left = Node.newName("a");
    Node right = Node.newName("b");
    assertEquals("a || b", generate(Node.newBinOp(Token.OR, left, right)));
  }

  // ----- Unary operations -----

  @Test
  public void testLogicalNot() {
    Node operand = Node.newName("a");
    assertEquals("!a", generate(Node.newUnaryOp(Token.NOT, operand)));
  }

  @Test
  public void testBitwiseNot() {
    Node operand = Node.newNumber(5);
    assertEquals("~5", generate(Node.newUnaryOp(Token.BITNOT, operand)));
  }

  @Test
  public void testUnaryMinus() {
    Node operand = Node.newName("a");
    assertEquals("-a", generate(Node.newUnaryOp(Token.NEG, operand)));
  }

  @Test
  public void testUnaryPlus() {
    Node operand = Node.newName("a");
    assertEquals("+a", generate(Node.newUnaryOp(Token.POS, operand)));
  }

  @Test
  public void testTypeof() {
    Node operand = Node.newName("a");
    assertEquals("typeof a", generate(Node.newUnaryOp(Token.TYPEOF, operand)));
  }

  @Test
  public void testVoid() {
    Node operand = Node.newNumber(0);
    assertEquals("void 0", generate(Node.newUnaryOp(Token.VOID, operand)));
  }

  @Test
  public void testDelete() {
    Node operand = Node.newName("a");
    assertEquals("delete a", generate(Node.newUnaryOp(Token.DELPROP, operand)));
  }

  // ----- Conditional expression -----

  @Test
  public void testConditionalExpression() {
    Node cond = Node.newName("a");
    Node trueExpr = Node.newNumber(1);
    Node falseExpr = Node.newNumber(2);
    Node conditional = new Node(Token.HOOK, cond, trueExpr, falseExpr);
    assertEquals("a ? 1 : 2", generate(conditional));
  }

  @Test
  public void testConditionalExpressionWithNested() {
    // (a ? 1 : 2) + 3 should preserve parentheses.
    Node cond = Node.newName("a");
    Node trueExpr = Node.newNumber(1);
    Node falseExpr = Node.newNumber(2);
    Node conditional = new Node(Token.HOOK, cond, trueExpr, falseExpr);
    Node add = Node.newBinOp(Token.ADD, conditional, Node.newNumber(3));
    assertEquals("(a ? 1 : 2) + 3", generate(add));
  }

  // ----- Call expressions -----

  @Test
  public void testCallNoArgs() {
    Node target = Node.newName("foo");
    assertEquals("foo()", generate(Node.newCall(target)));
  }

  @Test
  public void testCallWithArgs() {
    Node target = Node.newName("foo");
    Node arg1 = Node.newNumber(1);
    Node arg2 = Node.newString("bar");
    assertEquals("foo(1, \"bar\")", generate(Node.newCall(target, arg1, arg2)));
  }

  // ----- New expressions -----

  @Test
  public void testNewNoArgs() {
    Node target = Node.newName("Foo");
    // This is a likely bug source: new Foo() should be generated, not new Foo.
    assertEquals("new Foo()", generate(Node.newNew(target)));
  }

  @Test
  public void testNewWithArgs() {
    Node target = Node.newName("Foo");
    Node arg = Node.newNumber(1);
    assertEquals("new Foo(1)", generate(Node.newNew(target, arg)));
  }

  @Test
  public void testNewWithParenthesesOnTarget() {
    // new (foo)() should be handled correctly.
    Node target = Node.newName("foo");
    Node newExpr = Node.newNew(target);
    assertEquals("new foo()", generate(newExpr));
  }

  // ----- Array literals -----

  @Test
  public void testEmptyArray() {
    assertEquals("[]", generate(Node.newArrayLiteral()));
  }

  @Test
  public void testArrayWithElements() {
    Node element1 = Node.newNumber(1);
    Node element2 = Node.newString("a");
    assertEquals("[1, \"a\"]", generate(Node.newArrayLiteral(element1, element2)));
  }

  @Test
  public void testNestedArray() {
    Node inner = Node.newArrayLiteral(Node.newNumber(1));
    Node outer = Node.newArrayLiteral(inner, Node.newNumber(2));
    assertEquals("[[1], 2]", generate(outer));
  }

  // ----- Object literals -----

  @Test
  public void testEmptyObjectLiteral() {
    assertEquals("{}", generate(Node.newObjectLit()));
  }

  @Test
  public void testObjectLiteralWithProperties() {
    Node key1 = Node.newString(Token.STRING_KEY, "a");
    Node value1 = Node.newNumber(1);
    Node key2 = Node.newString(Token.STRING_KEY, "b");
    Node value2 = Node.newName("foo");
    Node obj = Node.newObjectLit(key1, value1, key2, value2);
    assertEquals("{a: 1, b: foo}", generate(obj));
  }

  @Test
  public void testObjectLiteralWithNumericKey() {
    Node key = Node.newString(Token.NUMBER_KEY, "1");
    Node value = Node.newNumber(10);
    Node obj = Node.newObjectLit(key, value);
    // Keys that are numbers might need quoting depending on output.
    assertEquals("{1: 10}", generate(obj));
  }

  // ----- Function expressions -----

  @Test
  public void testAnonymousFunctionExpression() {
    Node body = Node.newBlock(new Node(Token.RETURN, Node.newNumber(1)));
    Node params = Node.newParamList(); // no params
    Node func = Node.newFunction("", params, body);
    assertEquals("function() { return 1; }", generate(func));
  }

  @Test
  public void testNamedFunctionExpression() {
    Node body = Node.newBlock(new Node(Token.RETURN, Node.newName("x")));
    Node params = Node.newParamList(Node.newName("x"));
    Node func = Node.newFunction("f", params, body);
    assertEquals("function f(x) { return x; }", generate(func));
  }

  // ----- Potential bug triggers -----

  @Test
  public void testNewWithComputedPropertyName() {
    // Might be relevant to a bug.
    Node target = Node.newName("C");
    Node arg = Node.newName("a");
    Node newExpr = Node.newNew(target, arg);
    assertEquals("new C(a)", generate(newExpr));
  }

  @Test
  public void testStringWithEscapedBackslashAndQuote() {
    // Regression test for escaping correctness.
    String input = "\\\"";
    assertEquals("\"\\\\\\\"\"", generate(Node.newString(input)));
  }

  @Test
  public void testUnaryOperatorOnParenthesizedExpression() {
    // !(a + b) should be generated as !(a + b).
    Node add = Node.newBinOp(Token.ADD, Node.newName("a"), Node.newName("b"));
    Node not = Node.newUnaryOp(Token.NOT, add);
    assertEquals("!(a + b)", generate(not));
  }

  @Test
  public void testNestedCalls() {
    Node inner = Node.newCall(Node.newName("f"), Node.newNumber(1));
    Node outer = Node.newCall(Node.newName("g"), inner);
    assertEquals("g(f(1))", generate(outer));
  }

  // ----- Null input handling -----

  @Test(expected = NullPointerException.class)
  public void testNullNodeThrows() {
    generate(null);
  }
}