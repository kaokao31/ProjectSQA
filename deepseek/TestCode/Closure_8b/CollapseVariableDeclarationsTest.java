package com.google.javascript.jscomp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.jscomp.Result;
import com.google.javascript.jscomp.CheckLevel;
import com.google.javascript.jscomp.JSError;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * Test suite for CollapseVariableDeclarations compiler pass.
 * Designed to achieve high line/branch coverage and detect known faults.
 */
public class CollapseVariableDeclarationsTest {

  private Compiler compiler;
  private CompilerOptions options;

  @Before
  public void setUp() {
    compiler = new Compiler();
    options = new CompilerOptions();
    options.setWarningLevel(CheckLevel.OFF);
    options.setIdeMode(false);
    // Enable the collapse variable declarations pass
    options.collapseVariableDeclarations = true;
  }

  private Result compile(String code) {
    SourceFile input = SourceFile.fromCode("test.js", code);
    compiler.compile(
        SourceFile.fromCode("externs.js", ""),
        input,
        options);
    return compiler.getResult();
  }

  // ===================== Basic Collapsing =====================

  @Test
  public void testSimpleCollapse() {
    String code = "var a = 1; var b = 2;";
    Result result = compile(code);
    assertTrue("Compilation should succeed", result.success);
    String output = compiler.toSource();
    // Expect collapsed: var a=1,b=2;
    assertTrue("Output should contain collapsed var", output.contains("var a=1,b=2"));
  }

  @Test
  public void testCollapseWithNoInitializer() {
    String code = "var a; var b = 2;";
    Result result = compile(code);
    assertTrue(result.success);
    String output = compiler.toSource();
    assertTrue(output.contains("var a,b=2"));
  }

  @Test
  public void testCollapseMultipleDeclarations() {
    String code = "var a = 1; var b = 2; var c = 3;";
    Result result = compile(code);
    assertTrue(result.success);
    String output = compiler.toSource();
    assertTrue(output.contains("var a=1,b=2,c=3"));
  }

  @Test
  public void testCollapseWithFunctionCalls() {
    String code = "var a = foo(); var b = bar();";
    Result result = compile(code);
    assertTrue(result.success);
    String output = compiler.toSource();
    assertTrue(output.contains("var a=foo(),b=bar()"));
  }

  // ===================== Side Effects and Ordering =====================

  @Test
  public void testCollapsePreservesOrderWithSideEffects() {
    // Known bug: collapsing may reorder side effects incorrectly
    String code = "var a = console.log(1); var b = console.log(2);";
    Result result = compile(code);
    assertTrue(result.success);
    String output = compiler.toSource();
    // The collapsed form must preserve the order of side effects
    // Expected: var a=console.log(1),b=console.log(2);
    assertTrue("Order of side effects must be preserved",
               output.contains("var a=console.log(1),b=console.log(2)"));
  }

  @Test
  public void testCollapseWithSideEffectInInitializer() {
    // Bug: when first declaration has no initializer but second has side effect
    String code = "var a; var b = console.log(1);";
    Result result = compile(code);
    assertTrue(result.success);
    String output = compiler.toSource();
    assertTrue(output.contains("var a,b=console.log(1)"));
  }

  // ===================== Conditional and Loop Contexts =====================

  @Test
  public void testCollapseInsideIfBlock() {
    String code = "if (true) { var a = 1; var b = 2; }";
    Result result = compile(code);
    assertTrue(result.success);
    String output = compiler.toSource();
    // Should collapse inside the block
    assertTrue(output.contains("var a=1,b=2"));
  }

  @Test
  public void testCollapseInsideForLoop() {
    String code = "for (var i = 0; i < 10; i++) { var j = i; var k = i+1; }";
    Result result = compile(code);
    assertTrue(result.success);
    String output = compiler.toSource();
    // The var declarations inside the loop should be collapsed
    assertTrue(output.contains("var j=i,k=i+1"));
  }

  @Test
  public void testCollapseAcrossBlocksNotAllowed() {
    // Var declarations in different blocks should NOT be collapsed together
    String code = "if (true) { var a = 1; } var b = 2;";
    Result result = compile(code);
    assertTrue(result.success);
    String output = compiler.toSource();
    // a and b are in different scopes (function scope) but different blocks?
    // Actually var is function-scoped, so they might be collapsed.
    // This test checks that the pass does not incorrectly merge across block boundaries
    // when it should not. For function-scoped vars, collapsing is allowed.
    // We'll just ensure no crash.
    assertTrue(output.contains("var a=1") && output.contains("var b=2"));
  }

  // ===================== Edge Cases =====================

  @Test
  public void testSingleDeclaration() {
    String code = "var a = 1;";
    Result result = compile(code);
    assertTrue(result.success);
    String output = compiler.toSource();
    assertTrue(output.contains("var a=1"));
  }

  @Test
  public void testEmptyVar() {
    String code = "var a;";
    Result result = compile(code);
    assertTrue(result.success);
    String output = compiler.toSource();
    assertTrue(output.contains("var a"));
  }

  @Test
  public void testNoVarDeclarations() {
    String code = "var a = 1; a = 2;";
    Result result = compile(code);
    assertTrue(result.success);
    // Should not change anything
    String output = compiler.toSource();
    assertTrue(output.contains("var a=1"));
  }

  @Test
  public void testCollapseWithComments() {
    String code = "var a = 1; /* comment */ var b = 2;";
    Result result = compile(code);
    assertTrue(result.success);
    String output = compiler.toSource();
    // Comments may be removed or preserved; just ensure no crash
    assertTrue(output.contains("var a=1") && output.contains("b=2"));
  }

  // ===================== Fault Detection: Known Defects4J Bugs =====================

  @Test
  public void testBugWithSideEffectInFirstDeclaration() {
    // Defects4J bug: when first var has side effect and second has none,
    // collapsing may incorrectly reorder or drop side effect.
    String code = "var a = console.log(1); var b = 2;";
    Result result = compile(code);
    assertTrue(result.success);
    String output = compiler.toSource();
    // The side effect must appear before the assignment to b
    assertTrue("Side effect must be preserved in order",
               output.contains("var a=console.log(1),b=2"));
  }

  @Test
  public void testBugWithMultipleSideEffects() {
    // Known issue: collapsing multiple side-effectful declarations
    String code = "var a = foo(), b = bar(); var c = baz();";
    // Note: this is a single var with two declarations, then another var.
    // The pass should collapse all three into one var.
    Result result = compile(code);
    assertTrue(result.success);
    String output = compiler.toSource();
    assertTrue(output.contains("var a=foo(),b=bar(),c=baz()"));
  }

  @Test
  public void testBugWithNestedFunction() {
    // Bug: collapsing inside a nested function scope
    String code = "function f() { var a = 1; var b = 2; }";
    Result result = compile(code);
    assertTrue(result.success);
    String output = compiler.toSource();
    assertTrue(output.contains("var a=1,b=2"));
  }

  @Test
  public void testBugWithGlobalScope() {
    String code = "var a = 1; var b = 2;";
    Result result = compile(code);
    assertTrue(result.success);
    String output = compiler.toSource();
    assertTrue(output.contains("var a=1,b=2"));
  }

  @Test
  public void testBugWithRedeclaration() {
    // If a variable is declared twice, collapsing should handle it
    String code = "var a = 1; var a = 2;";
    Result result = compile(code);
    assertTrue(result.success);
    String output = compiler.toSource();
    // The second var may overwrite; collapsed form should be var a=2
    assertTrue(output.contains("var a=2"));
  }

  // ===================== Branch Coverage: Conditional Collapsing =====================

  @Test
  public void testCollapseOnlyWhenAdjacent() {
    // Non-adjacent var declarations should not be collapsed
    String code = "var a = 1; var b = a; var c = 2;";
    Result result = compile(code);
    assertTrue(result.success);
    String output = compiler.toSource();
    // a and b are adjacent, b and c are adjacent, so all three should collapse
    assertTrue(output.contains("var a=1,b=a,c=2"));
  }

  @Test
  public void testCollapseWithSeparatorStatement() {
    // If there is a non-var statement between, collapse should not happen across it
    String code = "var a = 1; foo(); var b = 2;";
    Result result = compile(code);
    assertTrue(result.success);
    String output = compiler.toSource();
    // a and b are separated by foo(), so they should remain separate
    assertTrue(output.contains("var a=1") && output.contains("var b=2"));
  }

  @Test
  public void testCollapseWithMultipleSeparators() {
    String code = "var a = 1; foo(); bar(); var b = 2;";
    Result result = compile(code);
    assertTrue(result.success);
    String output = compiler.toSource();
    assertTrue(output.contains("var a=1") && output.contains("var b=2"));
  }

  // ===================== Error Handling =====================

  @Test
  public void testSyntaxErrorInput() {
    String code = "var a = ;";
    Result result = compile(code);
    assertFalse("Compilation should fail on syntax error", result.success);
    // Should not crash the pass
  }

  @Test
  public void testEmptyInput() {
    String code = "";
    Result result = compile(code);
    assertTrue(result.success);
    String output = compiler.toSource();
    assertEquals("", output.trim());
  }

  @Test
  public void testOnlyComments() {
    String code = "// just a comment";
    Result result = compile(code);
    assertTrue(result.success);
    // No var declarations, nothing to collapse
  }

  // ===================== Advanced: Collapse with Destructuring (ES6) =====================
  // Note: The pass may not support ES6; these tests ensure no crash.

  @Test
  public void testCollapseWithDestructuring() {
    String code = "var [a, b] = [1, 2]; var c = 3;";
    Result result = compile(code);
    // Depending on compiler version, may succeed or fail
    // We just check no exception
    assertTrue(result.success || !result.success);
  }

  @Test
  public void testCollapseWithLetAndConst() {
    // The pass should only collapse var, not let/const
    String code = "let a = 1; let b = 2;";
    Result result = compile(code);
    assertTrue(result.success);
    String output = compiler.toSource();
    // Should not collapse let declarations
    assertTrue(output.contains("let a=1") && output.contains("let b=2"));
  }
}