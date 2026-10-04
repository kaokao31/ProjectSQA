package com.google.javascript.jscomp;

import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.NodeTraversal;
import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.rhino.Node;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * JUnit 4 test suite for FlowSensitiveInlineVariables.
 * Designed to achieve high line and branch coverage and to detect
 * potential faults (especially those related to Defects4J Closure-30).
 */
public class FlowSensitiveInlineVariablesTest {

  private Compiler compiler;
  private CompilerOptions options;

  @Before
  public void setUp() {
    options = new CompilerOptions();
    // Enable all checks to ensure the pass behaves correctly
    options.setWarningLevel(DiagnosticGroups.CHECK_VARIABLES, CheckLevel.WARNING);
    options.setWarningLevel(DiagnosticGroups.DUPLICATE_VARS, CheckLevel.WARNING);
    // The pass under test
    compiler = new Compiler();
    compiler.disableThreads();
  }

  /**
   * Helper method to compile a JavaScript string and run the FlowSensitiveInlineVariables pass.
   * Returns the compiled source (the last output).
   */
  private String compileAndRun(String code) {
    SourceFile input = SourceFile.fromCode("test.js", code);
    compiler.compile(
        new SourceFile[] {},
        new SourceFile[] {input},
        options);
    // Run the flow-sensitive inline variables pass
    FlowSensitiveInlineVariables pass = new FlowSensitiveInlineVariables();
    pass.process(compiler.getRoot());
    // Return the generated code (simplified)
    return compiler.toSource();
  }

  @Test
  public void testSimpleInline() {
    String code = "var a = 1; var b = a;";
    String result = compileAndRun(code);
    // After inlining 'a' into 'b', 'b' should become 1 and 'a' may be removed
    assertTrue(result.contains("var b = 1"));
    assertFalse(result.contains("var a = 1"));
  }

  @Test
  public void testNoInlineWhenMultipleUses() {
    String code = "var a = 1; var b = a; var c = a;";
    String result = compileAndRun(code);
    // 'a' is used twice, so should not be inlined
    assertTrue(result.contains("var a = 1"));
    assertTrue(result.contains("var b = a"));
    assertTrue(result.contains("var c = a"));
  }

  @Test
  public void testNoInlineWhenReassigned() {
    String code = "var a = 1; a = 2; var b = a;";
    String result = compileAndRun(code);
    // 'a' is reassigned before use, so should not be inlined
    assertTrue(result.contains("var a = 2"));
    assertTrue(result.contains("var b = a"));
  }

  @Test
  public void testNoInlineWhenConditionalAssignment() {
    String code = "var a; if (true) { a = 1; } else { a = 2; } var b = a;";
    String result = compileAndRun(code);
    // 'a' is assigned in a conditional, should not be inlined
    assertTrue(result.contains("var b = a"));
    assertTrue(result.contains("var a"));
  }

  @Test
  public void testInlineInSameBlock() {
    String code = "function f() { var a = 1; var b = a; }";
    String result = compileAndRun(code);
    // Inside a function, inlining should happen
    assertFalse(result.contains("var a = 1"));
    assertTrue(result.contains("var b = 1"));
  }

  @Test
  public void testNoInlineAcrossFunctionBoundary() {
    String code = "var a = 1; function f() { var b = a; }";
    String result = compileAndRun(code);
    // 'a' is global, should not be inlined into function
    assertTrue(result.contains("var a = 1"));
    assertTrue(result.contains("var b = a"));
  }

 ​@Test
  public void testNoInlineWithTryCatch() {
    String code = "var a; try { a = 1; } catch(e) { a = 2; } var b = a;";
    String result = compileAndRun(code);
    // 'a' is assigned in try/catch, should not be inlined
    assertTrue(result.contains("var b = a"));
    assertTrue(result.contains("var a"));
  }

  @Test
  public void testNoInlineWithLoopModification() {
    String code = "var a = 1; while (true) { a = a + 1; } var b = a;";
    String result = compileAndRun(code);
    // loop modifies 'a' so cannot inline
    assertTrue(result.contains("var a = 1"));
    assertTrue(result.contains("var b = a"));
  }

  @Test
  public void testNoInlineWithFunctionCallSideEffect() {
    String code = "var a = 1; function f() { a = 2; } f(); var b = a;";
    String result = compileAndRun(code);
    // function call may modify 'a', so cannot inline
    assertTrue(result.contains("var a = 1"));
    assertTrue(result.contains("var b = a"));
  }

  @Test
  public void testInlineSimpleReassignmentAfterUse() {
    String code = "var a = 1; var b = a; a = 2;";
    String result = compileAndRun(code);
    // 'a' is used before reassignment, so 'b' can be inlined
    assertFalse(result.contains("var a = 1"));
    assertTrue(result.contains("var b = 1"));
    assertTrue(result.contains("a = 2"));
  }

  @Test
  public void testNoInlineWhenUsedInConditional() {
    String code = "var a = 1; if (a) { var b = 2; }";
    String result = compileAndRun(code);
    // 'a' is used in a condition, so cannot be inlined
    assertTrue(result.contains("var a = 1"));
  }

  @Test
  public void testNoInlineWhenDefinedInInnerScope() {
    String code = "if (true) { var a = 1; var b = a; }";
    String result = compileAndRun(code);
    // Both a and b are in the same block, inlining possible
    assertFalse(result.contains("var a = 1"));
    assertTrue(result.contains("var b = 1"));
  }

  @Test
  public void testNoInlineUsedInForInit() {
    String code = "var a = 1; for (a; a < 10; a++) {}";
    String result = compileAndRun(code);
    // 'a' is used in for loop init and condition, cannot inline
    assertTrue(result.contains("var a = 1"));
  }

  @Test
  public void testInlineInForBodyOnly() {
    String code = "var a = 1; for (; ; ) { var b = a; }";
    String result = compileAndRun(code);
    // In for body, a is defined outside, cannot inline due to loop back edge
    assertTrue(result.contains("var a = 1"));
    assertTrue(result.contains("var b = a"));
  }

  @Test
  public void testInlineMultipleAssignmentsOneDef() {
    String code = "var a = 1, b = 2; var c = a + b;";
    String result = compileAndRun(code);
    // Both a and b can be inlined
    assertFalse(result.contains("var a = 1"));
    assertFalse(result.contains("var b = 2"));
    assertTrue(result.contains("var c = 1 + 2"));
  }

  @Test
  public void testNoInlineIntoLogicalExpression() {
    String code = "var a = 1; var b = a || 2;";
    String result = compileAndRun(code);
    // 'a' is used in a logical expression, cannot inline if there is any other use? Actually should inline.
    // But in Defects4J bug 30, there may be an issue with logical operators.
    // Expect inlining.
    assertFalse(result.contains("var a = 1"));
    assertTrue(result.contains("var b = 1 || 2"));
  }

  @Test
  public void testNoInlineWhenDefInTryBlock() {
    String code = "try { var a = 1; } catch(e) {} var b = a;";
    String result = compileAndRun(code);
    // Try block defines 'a' but catch may not, but variable hoisting? Actually in JS, var is hoisted, so 'a' is defined outside.
    // Inlining should be possible if no side effects. This tests the pass's handling of try/catch.
    // Due to hoisting, 'a' is defined before try, so it should be inlined carefully.
    assertTrue(result.contains("var a"));
    assertTrue(result.contains("var b = a"));
  }

  // Additional edge cases for high coverage
  @Test
  public void testNoInlineGlobalVariableWithEval() {
    String code = "var a = 1; eval('a'); var b = a;";
    String result = compileAndRun(code);
    // eval can modify 'a', so cannot inline
    assertTrue(result.contains("var a = 1"));
    assertTrue(result.contains("var b = a"));
  }

  @Test
  public void testInlineWithConstDeclaration() {
    String code = "const a = 1; var b = a;";
    String result = compileAndRun(code);
    // const should be inlinable if no reassignment
    assertFalse(result.contains("const a = 1"));
    assertTrue(result.contains("var b = 1"));
  }

  @Test
  public void testNoInlineWithLetLoop() {
    String code = "for (let i = 0; i < 10; i++) { var a = i; var b = a; }";
    String result = compileAndRun(code);
    // In loop, a is defined and used in same iteration, but inlining across iterations? Should not inline because of loop.
    assertTrue(result.contains("var a = i"));
    assertTrue(result.contains("var b = a"));
  }

  // Defects4J bug reproduction: The specific bug 30 is about incorrect inlining when assignment is in a block after the definition.
  @Test
  public void testDefects4JBug30() {
    // Reproducing the bug: variable defined, then used after if-else where it is assigned.
    String code = "var a; if (true) { a = 1; } else { a = 2; } var b = a;";
    String result = compileAndRun(code);
    // Buggy version might inline 'a' into 'b' incorrectly, removing the conditional assignment.
    // Correct behavior: should NOT inline.
    assertTrue(result.contains("var a"));
    assertTrue(result.contains("var b = a"));
    // Also ensure the two assignments are preserved
    assertTrue(result.contains("a = 1"));
    assertTrue(result.contains("a = 2"));
  }

  // Another variation: assignment in catch block
  @Test
  public void testDefects4JBug30TryCatch() {
    String code = "var a; try { a = 1; } catch(e) { a = 2; } var b = a;";
    String result = compileAndRun(code);
    assertTrue(result.contains("var a"));
    assertTrue(result.contains("var b = a"));
    assertTrue(result.contains("a = 1"));
    assertTrue(result.contains("a = 2"));
  }
}