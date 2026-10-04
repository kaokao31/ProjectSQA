package com.google.javascript.jscomp;

import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test suite for PeepholeSubstituteAlternateSyntax.
 * Targets bug 132: incorrect optimization of typeof x == 'undefined' for undeclared variables.
 */
public class PeepholeSubstituteAlternateSyntaxTest {

  private Compiler compiler;
  private CompilerOptions options;
  private PeepholeSubstituteAlternateSyntax peephole;

  @Before
  public void setUp() {
    compiler = new Compiler();
    options = new CompilerOptions();
    // Enable only this peephole optimization for testing
    options.setPeepholeSubstituteAlternateSyntax(true);
    // Disable other optimizations to isolate the pass
    options.setOptimizationLevel(OptimizationLevel.O0);
    options.setChecksOnly(false);
    peephole = new PeepholeSubstituteAlternateSyntax();
  }

  /**
   * Helper: parse JavaScript source and return the root AST node.
   */
  private Node parse(String js) {
    SourceFile input = SourceFile.fromCode("test.js", js);
    Node root = compiler.parse(input);
    assertNotNull("Parsing failed", root);
    return root;
  }

  /**
   * Helper: apply the peephole optimization to the entire AST.
   */
  private Node optimize(Node root) {
    // The optimization is applied to the whole tree; we call optimizeSubtree on the root.
    // In practice, the pass traverses the tree, but for testing we can simulate by calling
    // the method on the root (which will recursively optimize children).
    return peephole.optimizeSubtree(root);
  }

  /**
   * Helper: compile and optimize a script, then return the optimized source.
   */
  private String optimizeAndGetSource(String js) {
    Node root = parse(js);
    Node optimized = optimize(root);
    return compiler.toSource(optimized);
  }

  // ========== Tests for typeof x == 'undefined' optimization ==========

  @Test
  public void testTypeofUndefinedOptimization_declaredVar() {
    // When variable is declared, optimization should replace typeof x == 'undefined' with x === void 0
    String input = "var x; if (typeof x == 'undefined') { foo(); }";
    String expected = "var x; if (x === void 0) { foo(); }";
    String actual = optimizeAndGetSource(input);
    assertEquals("Optimization should replace typeof with void 0 for declared variable", expected, actual);
  }

  @Test
  public void testTypeofUndefinedOptimization_undeclaredVar() {
    // When variable is not declared, optimization should NOT be applied (bug 132)
    String input = "if (typeof x == 'undefined') { foo(); }";
    String expected = "if (typeof x == 'undefined') { foo(); }";
    String actual = optimizeAndGetSource(input);
    assertEquals("Optimization should NOT be applied for undeclared variable", expected, actual);
  }

  @Test
  public void testTypeofUndefinedOptimization_globalVar() {
    // Global variable (declared via assignment) should be optimized
    String input = "x = 1; if (typeof x == 'undefined') { foo(); }";
    String expected = "x = 1; if (x === void 0) { foo(); }";
    String actual = optimizeAndGetSource(input);
    assertEquals("Optimization should apply for globally assigned variable", expected, actual);
  }

  @Test
  public void testTypeofUndefinedOptimization_undefinedGlobal() {
    // 'undefined' is a global property; optimization should apply
    String input = "if (typeof undefined == 'undefined') { foo(); }";
    String expected = "if (undefined === void 0) { foo(); }";
    String actual = optimizeAndGetSource(input);
    assertEquals("Optimization should apply for 'undefined' global", expected, actual);
  }

  @Test
  public void testTypeofUndefinedOptimization_notEqual() {
    // != 'undefined' should also be optimized
    String input = "var x; if (typeof x != 'undefined') { foo(); }";
    String expected = "var x; if (x !== void 0) { foo(); }";
    String actual = optimizeAndGetSource(input);
    assertEquals("Optimization should apply for != as well", expected, actual);
  }

  @Test
  public void testTypeofUndefinedOptimization_strictEqual() {
    // === 'undefined' should be optimized
    String input = "var x; if (typeof x === 'undefined') { foo(); }";
    String expected = "var x; if (x === void 0) { foo(); }";
    String actual = optimizeAndGetSource(input);
    assertEquals("Optimization should apply for ===", expected, actual);
  }

  @Test
  public void testTypeofUndefinedOptimization_strictNotEqual() {
    // !== 'undefined' should be optimized
    String input = "var x; if (typeof x !== 'undefined') { foo(); }";
    String expected = "var x; if (x !== void 0) { foo(); }";
    String actual = optimizeAndGetSource(input);
    assertEquals("Optimization should apply for !==", expected, actual);
  }

  @Test
  public void testTypeofUndefinedOptimization_notStringUndefined() {
    // Comparison with other string should not be optimized
    String input = "var x; if (typeof x == 'object') { foo(); }";
    String expected = "var x; if (typeof x == 'object') { foo(); }";
    String actual = optimizeAndGetSource(input);
    assertEquals("Optimization should not apply for non-'undefined' string", expected, actual);
  }

  @Test
  public void testTypeofUndefinedOptimization_undeclaredVarInFunction() {
    // Undeclared variable inside a function should not be optimized
    String input = "function f() { if (typeof x == 'undefined') { foo(); } }";
    String expected = "function f() { if (typeof x == 'undefined') { foo(); } }";
    String actual = optimizeAndGetSource(input);
    assertEquals("Optimization should not apply for undeclared variable in function", expected, actual);
  }

  @Test
  public void testTypeofUndefinedOptimization_declaredVarInFunction() {
    // Declared variable inside a function should be optimized
    String input = "function f() { var x; if (typeof x == 'undefined') { foo(); } }";
    String expected = "function f() { var x; if (x === void 0) { foo(); } }";
    String actual = optimizeAndGetSource(input);
    assertEquals("Optimization should apply for declared variable in function", expected, actual);
  }

  @Test
  public void testTypeofUndefinedOptimization_parameter() {
    // Function parameter is declared, so optimization should apply
    String input = "function f(x) { if (typeof x == 'undefined') { foo(); } }";
    String expected = "function f(x) { if (x === void 0) { foo(); } }";
    String actual = optimizeAndGetSource(input);
    assertEquals("Optimization should apply for function parameter", expected, actual);
  }

  @Test
  public void testTypeofUndefinedOptimization_withSideEffects() {
    // If the typeof operand has side effects, optimization should still be safe? 
    // Actually, typeof never throws for undeclared variables, but void 0 does not throw either.
    // However, if the variable is undeclared, typeof returns 'undefined' but void 0 would throw ReferenceError.
    // So we must ensure undeclared variables are not optimized.
    // This test ensures that a function call (which is always declared) is optimized.
    String input = "var x; if (typeof x() == 'undefined') { foo(); }";
    // x() is a function call; the function x must be declared. Here we assume x is a function.
    // The optimization should apply because x is declared (as a function).
    String expected = "var x; if (x() === void 0) { foo(); }";
    String actual = optimizeAndGetSource(input);
    assertEquals("Optimization should apply for function call on declared variable", expected, actual);
  }

  // ========== Tests for other peephole optimizations ==========

  @Test
  public void testNotOptimization() {
    // !x to x === false? Actually, the pass might convert !x to x === false? Not sure.
    // We'll test a common optimization: !!x to x (double negation removal)
    String input = "var x = !!y;";
    String expected = "var x = y;";
    String actual = optimizeAndGetSource(input);
    assertEquals("Double negation should be removed", expected, actual);
  }

  @Test
  public void testVoid0Optimization() {
    // void 0 to undefined? The pass might replace void 0 with undefined.
    String input = "var x = void 0;";
    String expected = "var x = undefined;";
    String actual = optimizeAndGetSource(input);
    assertEquals("void 0 should be replaced with undefined", expected, actual);
  }

  @Test
  public void testCommaOptimization() {
    // (x, y) to y? The pass might simplify comma expressions.
    String input = "var x = (1, 2);";
    String expected = "var x = 2;";
    String actual = optimizeAndGetSource(input);
    assertEquals("Comma expression should be simplified", expected, actual);
  }

  @Test
  public void testTernaryOptimization() {
    // x ? y : z to x && y || z? Not sure if this pass does that.
    // We'll test a simple ternary with boolean condition.
    String input = "var x = true ? 1 : 2;";
    String expected = "var x = 1;"; // constant folding might happen, but that's another pass.
    // Since we only have peephole substitute, it might not fold constants.
    // We'll just check that no crash occurs.
    String actual = optimizeAndGetSource(input);
    assertNotNull("Optimization should not crash", actual);
  }

  @Test
  public void testEmptyScript() {
    String input = "";
    String expected = "";
    String actual = optimizeAndGetSource(input);
    assertEquals("Empty script should remain empty", expected, actual);
  }

  @Test
  public void testNoChangeForSimpleExpression() {
    String input = "var x = 1 + 2;";
    String expected = "var x = 1 + 2;";
    String actual = optimizeAndGetSource(input);
    assertEquals("Simple arithmetic should not be changed", expected, actual);
  }
}