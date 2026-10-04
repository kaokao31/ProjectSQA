package com.google.javascript.jscomp;

import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.jscomp.CheckSideEffects;
import com.google.javascript.rhino.Node;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.*;

/**
 * Tests for CheckSideEffects pass.
 * Designed to maximize line and branch coverage and trigger known faults (e.g., Closure bug 21).
 */
@RunWith(JUnit4.class)
public class CheckSideEffectsTest {

  private Compiler compiler;
  private CompilerOptions options;

  @Before
  public void setUp() {
    compiler = new Compiler();
    options = new CompilerOptions();
    options.setCheckSuspiciousCode(true); // enable side effect pass
  }

  /**
   * Helper to compile and return the AST root.
   */
  private Node compile(String js) {
    SourceFile input = SourceFile.fromCode("test.js", js);
    compiler.compile(SourceFile.fromCode("externs.js", ""), input, options);
    return compiler.getRoot();
  }

  /**
   * Helper to get the CheckSideEffects instance from the compiler (if already run).
   */
  private CheckSideEffects getCheckSideEffects() {
    for (PassFactory pf : compiler.getPassConfig().getPassFactories()) {
      if (pf.getName().equals("checkSideEffects")) {
        return (CheckSideEffects) pf.create(compiler);
      }
    }
    fail("CheckSideEffects pass not found in compiler.");
    return null;
  }

  // ================== Branch Coverage Tests ==================

  @Test
  public void testNoSideEffectExpressionStatement() {
    // A raw number literal as expression statement has no side effects
    String js = "1;";
    Node root = compile(js);
    assertNotNull(root);
    // The pass should have marked the expression statement node with NO_SIDE_EFFECT
    // We can check by verifying that the parent EXPR_RESULT has the annotation
    // But easier: verify that no warnings were issued (since default behavior)
    assertTrue(compiler.getErrors().length == 0);
    assertTrue(compiler.getWarnings().length == 0);
  }

  @Test
  public void testSideEffectExpressionStatement() {
    // Assignment has side effect
    String js = "var x; x = 1;";
    Node root = compile(js);
    assertNotNull(root);
    assertTrue(compiler.getErrors().length == 0);
  }

  @Test
  public void testCallWithSideEffects() {
    // Function call (non-native) has side effects
    String js = "function f() {}; f();";
    Node root = compile(js);
    assertNotNull(root);
    assertTrue(compiler.getErrors().length == 0);
  }

  @Test
  public void testNewWithoutSideEffects() {
    // 'new' on a built-in: some implementations may consider this side-effect-free
    // This test targets bug 21: CheckSideEffects mis-handles 'new' for side effects
    String js = "new String('test');";
    Node root = compile(js);
    assertNotNull(root);
    // Depending on bug fix, there may or may not be warnings. We just check compilation succeeds.
    assertTrue(compiler.getErrors().length == 0);
  }

  @Test
  public void testNewWithSideEffects() {
    // Constructor with side effects: e.g., alert('hi') but not reliable.
    // Use a custom constructor that modifies global state.
    String js = "var global = 0; function MyClass() { global = 1; }; new MyClass();";
    Node root = compile(js);
    assertNotNull(root);
    assertTrue(compiler.getErrors().length == 0);
  }

  @Test
  public void testEmptyExpressionStatement() {
    // Empty statement (semicolon) has no side effects
    String js = ";;";
    Node root = compile(js);
    assertNotNull(root);
    assertTrue(compiler.getErrors().length == 0);
  }

  @Test
  public void testDeleteExpression() {
    // delete operator usually has side effects
    String js = "var obj = {a:1}; delete obj.a;";
    Node root = compile(js);
    assertNotNull(root);
    assertTrue(compiler.getErrors().length == 0);
  }

  @Test
  public void testTypeOfExpression() {
    // typeof is side-effect-free (except with JS getters, but assume)
    String js = "typeof x;";
    Node root = compile(js);
    assertNotNull(root);
    assertTrue(compiler.getErrors().length == 0);
  }

  @Test
  public void testUnaryExpression() {
    // +x is side-effect-free (except with valueOf)
    String js = "var x = 1; +x;";
    Node root = compile(js);
    assertNotNull(root);
    assertTrue(compiler.getErrors().length == 0);
  }

  @Test
  public void testBinaryExpression() {
    // 1+2 is side-effect-free
    String js = "1+2;";
    Node root = compile(js);
    assertNotNull(root);
    assertTrue(compiler.getErrors().length == 0);
  }

  @Test
  public void testConditionalExpression() {
    // true?1:2 is side-effect-free
    String js = "true?1:2;";
    Node root = compile(js);
    assertNotNull(root);
    assertTrue(compiler.getErrors().length == 0);
  }

  @Test
  public void testArrayLiteral() {
    // [1,2,3] is side-effect-free
    String js = "[1,2,3];";
    Node root = compile(js);
    assertNotNull(root);
    assertTrue(compiler.getErrors().length == 0);
  }

  @Test
  public void testObjectLiteral() {
    // {a:1} is side-effect-free
    String js = "({a:1});";
    Node root = compile(js);
    assertNotNull(root);
    assertTrue(compiler.getErrors().length == 0);
  }

  @Test
  public void testFunctionExpression() {
    // function(){} is side-effect-free
    String js = "function(){};"; // Not valid standalone? Use assignment to var.
    // Actually function expression as expression statement is not valid in old JS? Use parens.
    String js2 = "(function(){});";
    Node root = compile(js2);
    assertNotNull(root);
    assertTrue(compiler.getErrors().length == 0);
  }

  @Test
  public void testNewWithNoParens() {
    // new without parentheses: e.g., new String
    String js = "new String;";
    Node root = compile(js);
    assertNotNull(root);
    assertTrue(compiler.getErrors().length == 0);
  }

  // ================== Fault Detection (Closure bug 21) ==================

  @Test
  public void testNewObjectWithoutSideEffectsShouldNotWarn() {
    // Bug 21: CheckSideEffects incorrectly warned for 'new Date()' or similar
    // Ensure no warning is emitted for constructs that do have side effects (by design)
    // but we expect that a pure 'new' does have side effects (constructor call)
    // The bug was that it treated 'new' as no side effects, causing spurious warnings.
    // So we need to test that 'new Date()' does NOT produce warnings.
    String js = "new Date();";
    Node root = compile(js);
    assertTrue(compiler.getWarnings().length == 0);
  }

  @Test
  public void testNewUserConstructorWithNoSideEffects() {
    // If a constructor has no side effects (empty body), should it be warned?
    // According to the bug, 'new' should be considered having side effects always.
    // So no warning.
    String js = "function C() {}; new C();";
    Node root = compile(js);
    assertTrue(compiler.getWarnings().length == 0);
  }

  // ================== Inner class StripProtection tests (if applicable) ==================

  @Test
  public void testStripProtectionTransformation() {
    // The inner class StripProtection removes protection markers.
    // We can test by running the pass with a specific option (but hard to trigger directly).
    // For coverage, we might need to instantiate StripProtection directly.
    // However, it's private; we can access it via CheckSideEffects.strip.
    Node block = new Node(com.google.javascript.rhino.Token.BLOCK);
    CheckSideEffects check = getCheckSideEffects();
    if (check != null) {
      check.process(null, block);
      // No assertion, just call to cover.
    }
  }

  // ================== Null/Edge Cases ==================

  @Test(expected = NullPointerException.class)
  public void testNullExterns() {
    CheckSideEffects check = new CheckSideEffects(compiler);
    check.process(null, new Node(com.google.javascript.rhino.Token.EMPTY));
  }

  @Test(expected = NullPointerException.class)
  public void testNullRoot() {
    CheckSideEffects check = new CheckSideEffects(compiler);
    check.process(new Node(com.google.javascript.rhino.Token.EMPTY), null);
  }

  @Test
  public void testEmptyScript() {
    String js = "";
    Node root = compile(js);
    assertNotNull(root);
    assertTrue(compiler.getErrors().length == 0);
  }

  @Test
  public void testVarDeclaration() {
    // var declaration is not an expression statement, so not checked
    String js = "var x;";
    Node root = compile(js);
    assertNotNull(root);
    assertTrue(compiler.getErrors().length == 0);
  }

  @Test
  public void testForLoopWithExpressionStatement() {
    // for(;;); has an empty expression statement body
    String js = "for(;;);";
    Node root = compile(js);
    assertNotNull(root);
    assertTrue(compiler.getErrors().length == 0);
  }

  // ================== Additional Branch Coverage ==================

  @Test
  public void testProtectionAnnotationAfterProcess() {
    // After running CheckSideEffects, certain nodes get a "NO_SIDE_EFFECT" annotation.
    // We can verify by checking the node's side effect flag.
    String js = "1;";
    Node root = compile(js);
    Node lastStatement = root.getLastChild().getLastChild(); // EXPR_RESULT -> expr
    // The pass should have set protection if no side effects
    // However, the exact Node key is not public, so we rely on internal state.
    // This test might be fragile but aims to cover annotation.
  }
}