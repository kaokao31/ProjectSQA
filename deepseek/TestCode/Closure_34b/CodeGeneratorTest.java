package com.google.javascript.jscomp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

/**
 * Test suite for CodeGenerator, targeting maximum coverage and fault detection
 * (including Defects4J Closure bug 34).
 */
public class CodeGeneratorTest {

  private CodeGenerator codeGenerator;
  private Compiler compiler;

  @Before
  public void setUp() {
    compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
    codeGenerator = new CodeGenerator(compiler);
  }

  // ========== Basic code generation tests ==========

  @Test
  public void testGenerateEmptyScript() {
    Node script = new Node(Token.SCRIPT);
    String result = codeGenerator.generate(script, false, false);
    assertEquals("", result);
  }

  @Test
  public void testGenerateNullNode() {
    try {
      codeGenerator.generate(null, false, false);
      fail("Expected NullPointerException for null node");
    } catch (NullPointerException e) {
      // expected
    }
  }

  @Test
  public void testGenerateNumberLiteral() {
    Node number = Node.newNumber(42.0);
    String result = codeGenerator.generate(number, false, false);
    assertEquals("42", result);
  }

  @Test
  public void testGenerateStringLiteral() {
    Node string = Node.newString("hello");
    String result = codeGenerator.generate(string, false, false);
    assertEquals("\"hello\"", result);
  }

  @Test
  public void testGenerateStringLiteralWithSpecialChars() {
    Node string = Node.newString("line1\nline2");
    String result = codeGenerator.generate(string, false, false);
    assertEquals("\"line1\\nline2\"", result);
  }

  @Test
  public void testGenerateBooleanTrue() {
    Node bool = new Node(Token.TRUE);
    String result = codeGenerator.generate(bool, false, false);
    assertEquals("true", result);
  }

  @Test
  public void testGenerateBooleanFalse() {
    Node bool = new Node(Token.FALSE);
    String result = codeGenerator.generate(bool, false, false);
    assertEquals("false", result);
  }

  @Test
  public void testGenerateNullLiteral() {
    Node nullNode = new Node(Token.NULL);
    String result = codeGenerator.generate(nullNode, false, false);
    assertEquals("null", result);
  }

  // ========== Expression tests ==========

  @Test
  public void testGenerateAddExpression() {
    Node add = new Node(Token.ADD, Node.newString("a"), Node.newString("b"));
    String result = codeGenerator.generate(add, false, false);
    assertEquals("a + b", result);
  }

  @Test
  public void testGenerateAddWithNullOperand() {
    // Bug 34: string concatenation with null may produce incorrect output
    Node add = new Node(Token.ADD, Node.newString("x"), new Node(Token.NULL));
    String result = codeGenerator.generate(add, false, false);
    // Expected: "x + null" but bug might produce "x + "null"" or similar
    assertEquals("x + null", result);
  }

  @Test
  public void testGenerateSubtractExpression() {
    Node sub = new Node(Token.SUB, Node.newNumber(10), Node.newNumber(3));
    String result = codeGenerator.generate(sub, false, false);
    assertEquals("10 - 3", result);
  }

  @Test
  public void testGenerateMultiplyExpression() {
    Node mul = new Node(Token.MUL, Node.newNumber(2), Node.newNumber(3));
    String result = codeGenerator.generate(mul, false, false);
    assertEquals("2 * 3", result);
  }

  @Test
  public void testGenerateDivideExpression() {
    Node div = new Node(Token.DIV, Node.newNumber(10), Node.newNumber(2));
    String result = codeGenerator.generate(div, false, false);
    assertEquals("10 / 2", result);
  }

  // ========== Unary operator tests ==========

  @Test
  public void testGenerateUnaryNegation() {
    Node neg = new Node(Token.NEG, Node.newNumber(5));
    String result = codeGenerator.generate(neg, false, false);
    assertEquals("-5", result);
  }

  @Test
  public void testGenerateUnaryNot() {
    Node not = new Node(Token.NOT, new Node(Token.TRUE));
    String result = codeGenerator.generate(not, false, false);
    assertEquals("!true", result);
  }

  // ========== Assignment tests ==========

  @Test
  public void testGenerateSimpleAssignment() {
    Node assign = new Node(Token.ASSIGN, Node.newString("x"), Node.newNumber(1));
    String result = codeGenerator.generate(assign, false, false);
    assertEquals("x = 1", result);
  }

  @Test
  public void testGenerateAddAssignment() {
    Node assignAdd = new Node(Token.ASSIGN_ADD, Node.newString("x"), Node.newNumber(1));
    String result = codeGenerator.generate(assignAdd, false, false);
    assertEquals("x += 1", result);
  }

  // ========== Control flow tests ==========

  @Test
  public void testGenerateIfStatement() {
    Node ifNode = new Node(Token.IF,
        new Node(Token.TRUE),
        new Node(Token.BLOCK, Node.newString("x")));
    String result = codeGenerator.generate(ifNode, false, false);
    assertEquals("if (true) {\n  x\n}", result);
  }

  @Test
  public void testGenerateIfElseStatement() {
    Node ifElse = new Node(Token.IF,
        new Node(Token.FALSE),
        new Node(Token.BLOCK, Node.newString("a")),
        new Node(Token.BLOCK, Node.newString("b")));
    String result = codeGenerator.generate(ifElse, false, false);
    assertEquals("if (false) {\n  a\n} else {\n  b\n}", result);
  }

  @Test
  public void testGenerateWhileLoop() {
    Node whileNode = new Node(Token.WHILE,
        new Node(Token.TRUE),
        new Node(Token.BLOCK, Node.newString("x")));
    String result = codeGenerator.generate(whileNode, false, false);
    assertEquals("while (true) {\n  x\n}", result);
  }

  @Test
  public void testGenerateForLoop() {
    Node forNode = new Node(Token.FOR,
        new Node(Token.EMPTY),  // init
        new Node(Token.TRUE),   // condition
        new Node(Token.EMPTY),  // increment
        new Node(Token.BLOCK, Node.newString("x")));
    String result = codeGenerator.generate(forNode, false, false);
    assertEquals("for (; true; ) {\n  x\n}", result);
  }

  // ========== Function tests ==========

  @Test
  public void testGenerateFunctionDeclaration() {
    Node function = new Node(Token.FUNCTION,
        Node.newString("f"),
        new Node(Token.PARAM_LIST),
        new Node(Token.BLOCK, Node.newString("return 1")));
    String result = codeGenerator.generate(function, false, false);
    assertEquals("function f() {\n  return 1\n}", result);
  }

  @Test
  public void testGenerateFunctionCall() {
    Node call = new Node(Token.CALL,
        Node.newString("alert"),
        Node.newString("hello"));
    String result = codeGenerator.generate(call, false, false);
    assertEquals("alert(\"hello\")", result);
  }

  // ========== Array and object literal tests ==========

  @Test
  public void testGenerateArrayLiteral() {
    Node array = new Node(Token.ARRAYLIT,
        Node.newNumber(1),
        Node.newNumber(2));
    String result = codeGenerator.generate(array, false, false);
    assertEquals("[1, 2]", result);
  }

  @Test
  public void testGenerateEmptyArrayLiteral() {
    Node array = new Node(Token.ARRAYLIT);
    String result = codeGenerator.generate(array, false, false);
    assertEquals("[]", result);
  }

  @Test
  public void testGenerateObjectLiteral() {
    Node obj = new Node(Token.OBJECTLIT,
        new Node(Token.STRING_KEY, Node.newString("key")),
        Node.newNumber(1));
    String result = codeGenerator.generate(obj, false, false);
    assertEquals("{key: 1}", result);
  }

  // ========== Edge cases and bug-specific tests ==========

  @Test
  public void testGenerateEmptyBlock() {
    Node block = new Node(Token.BLOCK);
    String result = codeGenerator.generate(block, false, false);
    assertEquals("", result);
  }

  @Test
  public void testGenerateNestedBlocks() {
    Node outer = new Node(Token.BLOCK,
        new Node(Token.BLOCK, Node.newString("x")));
    String result = codeGenerator.generate(outer, false, false);
    assertEquals("{\n  x\n}", result);
  }

  @Test
  public void testGenerateWithLinePreservation() {
    Node script = new Node(Token.SCRIPT);
    script.addChildToBack(Node.newString("a"));
    script.addChildToBack(Node.newString("b"));
    String result = codeGenerator.generate(script, true, false);
    // With line preservation, there should be a newline between statements
    assertTrue(result.contains("\n"));
  }

  @Test
  public void testGenerateWithFunctionDeclarationPreservation() {
    Node func = new Node(Token.FUNCTION,
        Node.newString("f"),
        new Node(Token.PARAM_LIST),
        new Node(Token.BLOCK));
    String result = codeGenerator.generate(func, false, true);
    assertEquals("function f() {\n}", result);
  }

  @Test
  public void testGenerateLargeNumber() {
    Node number = Node.newNumber(Double.MAX_VALUE);
    String result = codeGenerator.generate(number, false, false);
    assertNotNull(result);
    assertTrue(result.length() > 0);
  }

  @Test
  public void testGenerateNegativeNumber() {
    Node number = Node.newNumber(-3.14);
    String result = codeGenerator.generate(number, false, false);
    assertEquals("-3.14", result);
  }

  @Test
  public void testGenerateStringWithQuotes() {
    Node string = Node.newString("he\"llo");
    String result = codeGenerator.generate(string, false, false);
    assertEquals("\"he\\\"llo\"", result);
  }

  @Test
  public void testGenerateStringWithBackslash() {
    Node string = Node.newString("a\\b");
    String result = codeGenerator.generate(string, false, false);
    assertEquals("\"a\\\\b\"", result);
  }

  // ========== Additional coverage for add() method ==========

  @Test
  public void testAddNullNode() {
    try {
      codeGenerator.add(null);
      fail("Expected NullPointerException");
    } catch (NullPointerException e) {
      // expected
    }
  }

  @Test
  public void testAddEmptyScript() {
    Node script = new Node(Token.SCRIPT);
    codeGenerator.add(script);
    String result = codeGenerator.getCode();
    assertEquals("", result);
  }

  @Test
  public void testAddMultipleStatements() {
    Node script = new Node(Token.SCRIPT);
    script.addChildToBack(Node.newString("a"));
    script.addChildToBack(Node.newString("b"));
    codeGenerator.add(script);
    String result = codeGenerator.getCode();
    assertEquals("a\nb\n", result);
  }

  // ========== Regression test for Defects4J Closure bug 34 ==========
  // Bug 34: CodeGenerator incorrectly handles string concatenation with null
  // This test verifies that the generated code is syntactically correct.
  @Test
  public void testBug34StringConcatWithNull() {
    Node add = new Node(Token.ADD,
        Node.newString("prefix"),
        new Node(Token.NULL));
    String result = codeGenerator.generate(add, false, false);
    // The bug may produce "prefixnull" instead of "prefix + null"
    // We expect the correct output with the operator.
    assertEquals("prefix + null", result);
  }

  @Test
  public void testBug34NestedConcatWithNull() {
    Node innerAdd = new Node(Token.ADD,
        Node.newString("a"),
        new Node(Token.NULL));
    Node outerAdd = new Node(Token.ADD,
        innerAdd,
        Node.newString("b"));
    String result = codeGenerator.generate(outerAdd, false, false);
    assertEquals("a + null + b", result);
  }
}