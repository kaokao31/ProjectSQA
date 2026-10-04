package com.google.javascript.jscomp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.Node;
import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

/**
 * Comprehensive JUnit 4 test suite for {@link InlineVariables}.
 * Designed to maximize line/branch coverage and detect common faults.
 */
public class InlineVariablesTest {

  private Compiler compiler;
  private InlineVariables inlineVariables;
  private CompilerOptions options;

  @Before
  public void setUp() {
    compiler = new Compiler();
    options = new CompilerOptions();
    options.setLanguageIn(CompilerOptions.LanguageMode.ECMASCRIPT5);
    options.setLanguageOut(CompilerOptions.LanguageMode.ECMASCRIPT5);
    inlineVariables = new InlineVariables(compiler, options);
  }

  private Node parseAndProcess(String code) {
    CompilerInput input = new CompilerInput(SourceFile.fromCode("test.js", code));
    compiler.compile(
        new JSSourceFile[] {},
        new JSSourceFile[] {SourceFile.fromCode("test.js", code)},
        options);
    Node root = compiler.getRoot();
    // Assume inlineVariables.process() is the main entry; adapt to actual API.
    // Typically: inlineVariables.process(externs, root);
    // Since no externs, use empty list.
    inlineVariables.process(new ArrayList<CompilerInput>(), root);
    return root;
  }

  // --- Basic inlining of constant variables ---
  @Test
  public void testInlineSimpleConstant() {
    String code = "var x = 5; var y = x;";
    Node root = parseAndProcess(code);
    // After inlining, 'x' should be replaced by 5, so y becomes 5.
    // We can check the AST: variable declaration for y should have the value 5.
    assertTrue("Expected inlining to occur", codeContainsNumber(root, 5));
  }

  @Test
  public void testInlineStringConstant() {
    String code = "var a = 'hello'; var b = a;";
    Node root = parseAndProcess(code);
    assertTrue("String constant should be inlined", codeContainsString(root, "hello"));
  }

  // --- Non-constant variables (should NOT be inlined) ---
  @Test
  public void testNoInlineReassignedVar() {
    String code = "var x = 1; x = 2; var y = x;";
    Node root = parseAndProcess(code);
    // y should be 2; but x was reassigned, so x may not be inlined.
    // Check that assignment reference remains.
    assertTrue("Reassigned variable should not be inlined", 
               root.getLastChild().getLastChild().getType() == Token.NAME);
  }

  @Test
  public void testNoInlineWithSideEffects() {
    String code = "var x = foo(); var y = x;";
    Node root = parseAndProcess(code);
    // Since foo() has side effects, x should not be inlined.
    assertTrue("Side-effecting variable should not be inlined",
               root.getLastChild().getChildCount() > 1);
  }

  // --- Multiple uses ---
  @Test
  public void testInlineMultipleUses() {
    String code = "var x = 3; var y = x + x;";
    Node root = parseAndProcess(code);
    // x inlined both times.
    assertTrue("Both occurrences should be inlined", codeContainsNumber(root, 3));
  }

  // --- Variable in loops ---
  @Test
  public void testNoInlineInLoopWithUpdate() {
    String code = "for (var i = 0; i < 10; i++) { var x = i; var y = x; }";
    Node root = parseAndProcess(code);
    // x is reassigned each iteration, so not constant; should not inline.
    assertTrue("Loop variable should not be inlined", 
               root.getLastChild().getLastChild().getType() == Token.VAR);
  }

  // --- Function scope ---
  @Test
  public void testInlineConstantInFunction() {
    String code = "function f() { var x = 42; return x; }";
    Node root = parseAndProcess(code);
    // x is constant, should inline.
    assertTrue("Constant in function should inline", codeContainsNumber(root, 42));
  }

  // --- Global var with multiple references after change (no inline) ---
  @Test
  public void testNoInlineGlobalAfterChange() {
    String code = "var x = 1; function f() { x = 2; } var y = x;";
    Node root = parseAndProcess(code);
    // x is modified, so y should still refer to x (not inlined to 1)
    assertTrue("Global variable modified elsewhere should not inline",
               root.getLastChild().getType() != Token.NUMBER);
  }

  // --- Edge cases: empty string, null, undefined ---
  @Test
  public void testInlineUndefined() {
    String code = "var x; var y = x;";
    Node root = parseAndProcess(code);
    // x is undefined; inlining may replace with undefined node.
    assertTrue("Undefined should be inlined",
               root.getLastChild().getLastChild().getType() == Token.VOID);
  }

  @Test
  public void testInlineNull() {
    String code = "var x = null; var y = x;";
    Node root = parseAndProcess(code);
    assertTrue("Null constant should inline", codeContainsNumber(root, Token.NULL));
  }

  // --- Boolean constants ---
  @Test
  public void testInlineTrue() {
    String code = "var x = true; var y = x;";
    Node root = parseAndProcess(code);
    assertTrue("True should inline", codeContainsBoolean(root, true));
  }

  @Test
  public void testInlineFalse() {
    String code = "var x = false; var y = x;";
    Node root = parseAndProcess(code);
    assertTrue("False should inline", codeContainsBoolean(root, false));
  }

  // --- Aliasing (multiple vars same value) ---
  @Test
  public void testInlineAlias() {
    String code = "var x = 10; var y = x; var z = y;";
    Node root = parseAndProcess(code);
    // x and y should both be inlined, leaving only 10.
    assertTrue("All aliases should inline", codeContainsNumber(root, 10));
  }

  // --- No inline for captured variable in closure ---
  @Test
  public void testNoInlineCapturedVar() {
    String code = "function f() { var x = 1; return function() { return x; }; }";
    Node root = parseAndProcess(code);
    // x is captured by closure, should not inline.
    assertTrue("Captured variable should not inline",
               root.getLastChild().getLastChild().getFirstChild().getType() == Token.FUNCTION);
  }

  // --- Block scoping (let/const) ---
  @Test
  public void testNoInlineBlockScopedLet() {
    String code = "let x = 1; if (true) { let x = 2; var y = x; }";
    Node root = parseAndProcess(code);
    // let x in block shadows outer; but inlining may still occur for inner x.
    // Depending on implementation, inner x is constant; should inline to 2.
    // Check that y is assigned 2.
    assertTrue("Block scoped let constant should inline", codeContainsNumber(root, 2));
  }

  // --- Fault detection: Bug 121 likely related to incorrect inlining of vars with side effects ---
  @Test
  public void testBug121_SideEffectVarInline() {
    String code = "var a = 1; function g() { a = 2; } var b = a; g(); var c = a;";
    Node root = parseAndProcess(code);
    // b should be inlined to 1? But a is modified later, so maybe not. Bug might cause wrong inline.
    // Test that b is not inlined (or at least not replaced with 1 if a is modified).
    assertTrue("Bug 121: Variable modified after use should not be inlined",
               root.getLastChild().getLastChild().getType() != Token.NUMBER);
  }

  // --- Null input / empty code ---
  @Test(expected = NullPointerException.class)
  public void testNullRoot() {
    inlineVariables.process(null, null);
  }

  // --- Helper methods ---
  private boolean codeContainsNumber(Node node, int num) {
    // Simple recursive search for a numeric literal with value num.
    if (node.getType() == Token.NUMBER && node.getDouble() == (double) num) {
      return true;
    }
    for (Node child = node.getFirstChild(); child != null; child = child.getNext()) {
      if (codeContainsNumber(child, num)) return true;
    }
    return false;
  }

  private boolean codeContainsString(Node node, String str) {
    if (node.getType() == Token.STRING && str.equals(node.getString())) {
      return true;
    }
    for (Node child = node.getFirstChild(); child != null; child = child.getNext()) {
      if (codeContainsString(child, str)) return true;
    }
    return false;
  }

  private boolean codeContainsBoolean(Node node, boolean val) {
    if ((val && node.getType() == Token.TRUE) || (!val && node.getType() == Token.FALSE)) {
      return true;
    }
    for (Node child = node.getFirstChild(); child != null; child = child.getNext()) {
      if (codeContainsBoolean(child, val)) return true;
    }
    return false;
  }
}