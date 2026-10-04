package com.google.javascript.jscomp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;

public class CodeGeneratorTest {

  private String generate(Node node) {
    return CodeGenerator.generateExpression(node);
  }

  private Node name(String name) {
    return Node.newString(Token.NAME, name);
  }

  private Node number(double value) {
    return Node.newNumber(value);
  }

  private Node binary(int type, Node left, Node right) {
    return new Node(type, left, right);
  }

  @Test
  public void testName() {
    assertEquals("a", generate(name("a")));
  }

  @Test
  public void testNumber() {
    assertEquals("0", generate(number(0)));
    assertEquals("1", generate(number(1)));
    assertEquals("1.5", generate(number(1.5)));
  }

  @Test
  public void testStringLiteral() {
    assertEquals("\"\"", generate(Node.newString("")));
    assertEquals("\"foo\"", generate(Node.newString("foo")));
    assertTrue(generate(Node.newString("a\nb")).contains("\\" + "n"));
  }

  @Test
  public void testStringEscapesUnicodeLineSeparators() {
    String output = generate(Node.newString("before\u2028after\u2029end"));

    assertTrue("U+2028 must be escaped as \\u2028",
        output.contains("\\" + "u2028"));
    assertTrue("U+2029 must be escaped as \\u2029",
        output.contains("\\" + "u2029"));
    assertFalse("Raw U+2028 must not be emitted",
        output.contains("\u2028"));
    assertFalse("Raw U+2029 must not be emitted",
        output.contains("\u2029"));
  }

  @Test
  public void testLiteralKeywords() {
    assertEquals("true", generate(new Node(Token.TRUE)));
    assertEquals("false", generate(new Node(Token.FALSE)));
    assertEquals("null", generate(new Node(Token.NULL)));
    assertEquals("this", generate(new Node(Token.THIS)));
  }

  @Test
  public void testUnaryOperators() {
    assertEquals("!a", generate(new Node(Token.NOT, name("a"))));
    assertEquals("-a", generate(new Node(Token.NEG, name("a"))));
    assertEquals("+a", generate(new Node(Token.POS, name("a"))));
    assertEquals("~a", generate(new Node(Token.BITNOT, name("a"))));
    assertEquals("typeof a", generate(new Node(Token.TYPEOF, name("a"))));
    assertEquals("void 0", generate(new Node(Token.VOID, number(0))));
  }

  @Test
  public void testBinaryOperators() {
    assertEquals("a+b", generate(binary(Token.ADD, name("a"), name("b"))));
    assertEquals("a-b", generate(binary(Token.SUB, name("a"), name("b"))));
    assertEquals("a*b", generate(binary(Token.MUL, name("a"), name("b"))));
    assertEquals("a/b", generate(binary(Token.DIV, name("a"), name("b"))));
    assertEquals("a%b", generate(binary(Token.MOD, name("a"), name("b"))));
    assertEquals("a<b", generate(binary(Token.LT, name("a"), name("b"))));
    assertEquals("a>b", generate(binary(Token.GT, name("a"), name("b"))));
    assertEquals("a<=b", generate(binary(Token.LE, name("a"), name("b"))));
    assertEquals("a>=b", generate(binary(Token.GE, name("a"), name("b"))));
    assertEquals("a==b", generate(binary(Token.EQ, name("a"), name("b"))));
    assertEquals("a!=b", generate(binary(Token.NE, name("a"), name("b"))));
    assertEquals("a===b", generate(binary(Token.SHEQ, name("a"), name("b"))));
    assertEquals("a!==b", generate(binary(Token.SHNE, name("a"), name("b"))));
    assertEquals("a&&b", generate(binary(Token.AND, name("a"), name("b"))));
    assertEquals("a||b", generate(binary(Token.OR, name("a"), name("b"))));
    assertEquals("a&b", generate(binary(Token.BITAND, name("a"), name("b"))));
    assertEquals("a|b", generate(binary(Token.BITOR, name("a"), name("b"))));
    assertEquals("a^b", generate(binary(Token.BITXOR, name("a"), name("b"))));
    assertEquals("a<<b", generate(binary(Token.LSH, name("a"), name("b"))));
    assertEquals("a>>b", generate(binary(Token.RSH, name("a"), name("b"))));
    assertEquals("a>>>b", generate(binary(Token.URSH, name("a"), name("b"))));
    assertEquals("a in b", generate(binary(Token.IN, name("a"), name("b"))));
    assertEquals("a instanceof b",
        generate(binary(Token.INSTANCEOF, name("a"), name("b"))));
  }

  @Test
  public void testAssignmentOperators() {
    assertEquals("a=b", generate(binary(Token.ASSIGN, name("a"), name("b"))));
    assertEquals("a+=b", generate(binary(Token.ASSIGN_ADD, name("a"), name("b"))));
    assertEquals("a-=b", generate(binary(Token.ASSIGN_SUB, name("a"), name("b"))));
    assertEquals("a*=b", generate(binary(Token.ASSIGN_MUL, name("a"), name("b"))));
    assertEquals("a/=b", generate(binary(Token.ASSIGN_DIV, name("a"), name("b"))));
    assertEquals("a%=b", generate(binary(Token.ASSIGN_MOD, name("a"), name("b"))));
  }

  @Test
  public void testGetProp() {
    Node prop = new Node(Token.GETPROP, name("a"), name("b"));
    assertEquals("a.b", generate(prop));
  }

  @Test
  public void testGetElem() {
    Node elem = new Node(Token.GETELEM, name("a"), name("b"));
    assertEquals("a[b]", generate(elem));
  }

  @Test
  public void testCall() {
    Node noArgs = new Node(Token.CALL, name("a"));
    assertEquals("a()", generate(noArgs));

    Node withArgs = new Node(Token.CALL, name("a"), name("b"), name("c"));
    assertEquals("a(b,c)", generate(withArgs));
  }

  @Test
  public void testArrayLiteral() {
    Node empty = new Node(Token.ARRAYLIT);
    assertEquals("[]", generate(empty));

    Node twoElems = new Node(Token.ARRAYLIT, number(1), number(2));
    assertEquals("[1,2]", generate(twoElems));
  }

  @Test
  public void testConditional() {
    Node hook = new Node(Token.HOOK, name("a"), name("b"), name("c"));
    assertEquals("a?b:c", generate(hook));
  }

  @Test
  public void testCommaExpression() {
    Node comma = new Node(Token.COMMA, name("a"), number(1));
    assertEquals("a,1", generate(comma));
  }

  @Test
  public void testExpressionStatement() {
    Node call = new Node(Token.CALL, name("foo"));
    Node exprStatement = new Node(Token.EXPR_RESULT, call);
    Node script = new Node(Token.SCRIPT, exprStatement);

    assertEquals("foo();", CodeGenerator.generateScript(script));
  }

  @Test
  public void testVarStatement() {
    Node x = name("x");
    x.addChildToBack(number(1));
    Node varStatement = new Node(Token.VAR, x);
    Node script = new Node(Token.SCRIPT, varStatement);

    assertEquals("var x=1;", CodeGenerator.generateScript(script));
  }

  @Test
  public void testConstStatement() {
    Node x = name("x");
    x.addChildToBack(number(1));
    Node constStatement = new Node(Token.CONST, x);
    Node script = new Node(Token.SCRIPT, constStatement);

    assertEquals("const x=1;", CodeGenerator.generateScript(script));
  }
}