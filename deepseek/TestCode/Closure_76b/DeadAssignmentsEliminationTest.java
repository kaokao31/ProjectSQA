package com.google.javascript.jscomp;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.google.javascript.rhino.Node;

public class DeadAssignmentsEliminationTest {

  private void assertResult(String input, String expected) throws Exception {
    Compiler compiler = new Compiler();
    compiler.initOptions(new CompilerOptions());
    Node externs = compiler.parseTestCode("");
    Node root = compiler.parseTestCode(input);
    new DeadAssignmentsElimination(compiler).process(externs, root);
    String actual = compiler.toSource().trim();

    Compiler expectedCompiler = new Compiler();
    expectedCompiler.initOptions(new CompilerOptions());
    expectedCompiler.parseTestCode(expected);
    String expectedSource = expectedCompiler.toSource().trim();
    assertEquals(expectedSource, actual);
  }

  @Test
  public void testLiveAssignmentUsedInReturn() throws Exception {
    assertResult(
        "function f() { var x = 1; return x; }",
        "function f() { var x = 1; return x; }");
  }

  @Test
  public void testRemoveOverwrittenAssignment() throws Exception {
    assertResult(
        "function f() { var x; x = 1; x = 2; return x; }",
        "function f() { var x; x = 2; return x; }");
  }

  @Test
  public void testRemoveDeadInitializer() throws Exception {
    assertResult(
        "function f() { var x = 1; x = 2; return x; }",
        "function f() { var x; x = 2; return x; }");
  }

  @Test
  public void testRemoveAssignmentNeverRead() throws Exception {
    assertResult(
        "function f() { var x; x = 1; }",
        "function f() { var x; }");
  }

  @Test
  public void testRemoveAssignmentBeforeReturnOfConstant() throws Exception {
    assertResult(
        "function f() { var x; x = 1; return 0; }",
        "function f() { var x; return 0; }");
  }

  @Test
  public void testLiveAssignmentInConditionalExpression() throws Exception {
    assertResult(
        "function f(a) { var x; x = a ? 1 : 2; return x; }",
        "function f(a) { var x; x = a ? 1 : 2; return x; }");
  }

  @Test
  public void testLiveAssignmentInIfCondition() throws Exception {
    assertResult(
        "function f(a) { var x; if (x = a) { return x; } return 0; }",
        "function f(a) { var x; if (x = a) { return x; } return 0; }");
  }

  @Test
  public void testLiveAssignmentAsArgument() throws Exception {
    assertResult(
        "function f(a) { var x; g(x = a); }",
        "function f(a) { var x; g(x = a); }");
  }

  @Test
  public void testLiveAssignmentInFinally() throws Exception {
    assertResult(
        "function f() { var x; try { x = 1; } finally { g(x); } }",
        "function f() { var x; try { x = 1; } finally { g(x); } }");
  }

  @Test
  public void testLiveAssignmentInCatch() throws Exception {
    assertResult(
        "function f() { var x; try { x = 1; } catch (e) { g(x); } }",
        "function f() { var x; try { x = 1; } catch (e) { g(x); } }");
  }

  @Test
  public void testLiveAssignmentCapturedByInnerFunction() throws Exception {
    assertResult(
        "function f() { var x; function g() { return x; } x = 1; return g; }",
        "function f() { var x; function g() { return x; } x = 1; return g; }");
  }

  @Test
  public void testLiveAssignmentInLoopHeader() throws Exception {
    assertResult(
        "function f() { var x; for (x = 0; x < 10; x++) {} return x; }",
        "function f() { var x; for (x = 0; x < 10; x++) {} return x; }");
  }

  @Test
  public void testLiveAssignmentInForIn() throws Exception {
    assertResult(
        "function f(obj) { var x; for (x in obj) {} return x; }",
        "function f(obj) { var x; for (x in obj) {} return x; }");
  }

  @Test
  public void testDeadAssignmentBeforeLoop() throws Exception {
    assertResult(
        "function f() { var x; x = 0; for (x = 0; x < 10; x++) {} return x; }",
        "function f() { var x; for (x = 0; x < 10; x++) {} return x; }");
  }

  @Test
  public void testRemoveDeadAssignmentInIfBranch() throws Exception {
    assertResult(
        "function f(a) { var x; if (a) { x = 1; } x = 2; return x; }",
        "function f(a) { var x; if (a) {} x = 2; return x; }");
  }

  @Test
  public void testRemoveDeadAssignmentInSwitchCase() throws Exception {
    assertResult(
        "function f(a) { var x; switch (a) { case 1: x = 1; break; } x = 2; return x; }",
        "function f(a) { var x; switch (a) { case 1: break; } x = 2; return x; }");
  }

  @Test
  public void testGlobalAssignmentNotRemoved() throws Exception {
    assertResult("x = 1; x = 2;", "x = 1; x = 2;");
  }

  @Test
  public void testAssignmentDeterminedByEvalNotRemoved() throws Exception {
    assertResult(
        "function f() { var x; x = 1; eval('g(x)'); }",
        "function f() { var x; x = 1; eval('g(x)'); }");
  }

  @Test
  public void testPropertyAssignmentNotTouched() throws Exception {
    assertResult(
        "function f() { var o = {}; o.x = 1; o.x = 2; return o.x; }",
        "function f() { var o = {}; o.x = 1; o.x = 2; return o.x; }");
  }
}