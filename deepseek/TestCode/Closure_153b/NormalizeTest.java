package com.google.javascript.jscomp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;

public class NormalizeTest {

  private Compiler compiler;
  private Node externs;

  @Before
  public void setUp() {
    compiler = new Compiler();
    compiler.init(
        new ArrayList<SourceFile>(),
        new ArrayList<SourceFile>(),
        new CompilerOptions());
    externs = compiler.getExternsRoot();
  }

  private Node parse(String code) {
    Node node = compiler.parseSyntheticCode(code);
    assertNotNull("Parsing returned null for: " + code, node);
    assertNoErrors();
    return node;
  }

  private void assertNoErrors() {
    JSError[] errors = compiler.getErrors();
    assertTrue(
        "Unexpected compiler errors: " + Arrays.toString(errors),
        errors.length == 0);
  }

  private Node normalize(String code) {
    Node root = parse(code);
    new Normalize(compiler, false).process(externs, root);
    assertNoErrors();
    return root;
  }

  private Node normalizeWithAssert(String code) {
    Node root = parse(code);
    new Normalize(compiler, true).process(externs, root);
    assertNoErrors();
    return root;
  }

  private void assertSingleDeclVars(Node node) {
    if (node.getType() == Token.VAR) {
      assertEquals(
          "Var statement has multiple declarations",
          1,
          node.getChildCount());
    }
    for (Node child = node.getFirstChild();
        child != null;
        child = child.getNext()) {
      assertSingleDeclVars(child);
    }
  }

  @Test
  public void testEmptyScript() {
    Node root = normalize(";");
    assertNotNull(root);
  }

  @Test
  public void testSimpleVar() {
    Node root = normalize("var a = 1;");
    assertNotNull(root);
    assertSingleDeclVars(root);
  }

  @Test
  public void testMultipleVarDeclarations() {
    Node root = normalize("var a = 1, b = 2;");
    assertSingleDeclVars(root);
  }

  @Test
  public void testMultipleVarDeclarationsNoInit() {
    Node root = normalize("var a, b;");
    assertSingleDeclVars(root);
  }

  @Test
  public void testVarInForLoop() {
    Node root = normalize(
        "var a = 0; for (var i = 0; i < 10; i++) { a += i; }");
    assertSingleDeclVars(root);
  }

  @Test
  public void testVarInForIn() {
    Node root = normalize(
        "var obj = {}; for (var k in obj) { obj[k] = k; }");
    assertSingleDeclVars(root);
  }

  @Test
  public void testForInEmptyBody() {
    Node root = normalize("var obj = {}; for (var k in obj) ;");
    assertNotNull(root);
    assertSingleDeclVars(root);
  }

  @Test
  public void testForInEmptyBlock() {
    Node root = normalize("var obj = {}; for (var k in obj) {}");
    assertNotNull(root);
    assertSingleDeclVars(root);
  }

  @Test
  public void testFunctionDeclarations() {
    Node root = normalize(
        "function f() { var x = 1; function g() { var y = 2; } return x; }");
    assertSingleDeclVars(root);
  }

  @Test
  public void testFunctionExpressions() {
    Node root = normalize(
        "var f = function() { var x = 1; return x; };");
    assertSingleDeclVars(root);
  }

  @Test
  public void testNamedFunctionExpression() {
    Node root = normalize(
        "var f = function g() { var x = g(); return x; };");
    assertSingleDeclVars(root);
  }

  @Test
  public void testNestedFunctions() {
    Node root = normalize(
        "function f() { function g() { return function() { var x = 1; }; } }");
    assertSingleDeclVars(root);
  }

  @Test
  public void testTryCatchFinally() {
    Node root = normalize(
        "try { var a = 1; } catch (e) { var b = e; } finally { var c = 2; }");
    assertSingleDeclVars(root);
  }

  @Test
  public void testSwitchStatement() {
    Node root = normalize(
        "switch (a) { case 1: var b = 2; break; default: var c = 3; }");
    assertSingleDeclVars(root);
  }

  @Test
  public void testDoWhileLoop() {
    Node root = normalize(
        "var i = 0; do { var j = i; i++; } while (i < 10);");
    assertSingleDeclVars(root);
  }

  @Test
  public void testLabeledLoops() {
    Node root = normalize(
        "var x = 0; outer: for (var i = 0; i < 10; i++) {"
        + " for (var j = 0; j < 10; j++) {"
        + " if (j > 5) break outer; x++; } }");
    assertSingleDeclVars(root);
  }

  @Test
  public void testObjectAndArrayLiterals() {
    Node root = normalize(
        "var obj = {a: 1, b: function() { var c = 2; return c; }};"
        + " var arr = [function() { var d = 3; return d; }, 4];");
    assertSingleDeclVars(root);
  }

  @Test
  public void testExpressions() {
    Node root = normalize(
        "var a = 1;"
        + " var b = a + 2;"
        + " var c = typeof a;"
        + " var d = a instanceof Object;"
        + " var e = new Object();");
    assertSingleDeclVars(root);
  }

  @Test
  public void testReservedNames() {
    Node root = normalize(
        "var undefined = 1; var NaN = 2; var Infinity = 3;");
    assertSingleDeclVars(root);
  }

  @Test
  public void testNormalizeCodeStatic() {
    Node root = parse("var a = 1, b = 2;");
    Normalize.normalizeCode(compiler, externs, root);
    assertNoErrors();
    assertSingleDeclVars(root);
  }

  @Test
  public void testNormalizeWithAssertOnChange() {
    Node root = normalizeWithAssert("var a = 1;");
    assertNotNull(root);
    assertSingleDeclVars(root);
  }

  @Test
  public void testIssue153() {
    Node root = normalize("for (var k in {a: 1}) ;");
    assertNotNull(root);
  }

  @Test
  public void testIssue153NonEmptyBody() {
    Node root = normalize("for (var k in {a: 1}) { k = k; }");
    assertNotNull(root);
    assertSingleDeclVars(root);
  }
}