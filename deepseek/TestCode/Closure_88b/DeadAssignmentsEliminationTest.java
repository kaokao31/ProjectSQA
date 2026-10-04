package com.google.javascript.jscomp;

import org.junit.Before;
import org.junit.Test;

public class DeadAssignmentsEliminationTest extends CompilerTestCase {

  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    return new DeadAssignmentsElimination(compiler);
  }

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
  }

  @Test
  public void testSimpleDeadAssignment() {
    test("function f() { var x; x = 1; x = 2; return x; }",
         "function f() { var x; x = 2; return x; }");
  }

  @Test
  public void testLiveAssignmentKept() {
    testSame("function f() { var x; x = 1; return x; }");
  }

  @Test
  public void testDeadAssignmentAtEnd() {
    test("function f() { var x; x = 1; }",
         "function f() { var x; }");
  }

  @Test
  public void testSideEffectPreserved() {
    test("function f() { var x; x = foo(); }",
         "function f() { var x; foo(); }");
  }

  @Test
  public void testConditionalAssignmentLive() {
    testSame("function f(c) { var x; if (c) { x = 1; } return x; }");
  }

  @Test
  public void testAssignmentBeforeConditionalLive() {
    testSame("function f(c) { var x; x = 1; if (c) { x = 2; } return x; }");
  }

  @Test
  public void testLoopAssignmentLive() {
    testSame("function f(n) { var x; for (var i = 0; i < n; i++) { x = i; } return x; }");
  }

  @Test
  public void testDeadAssignmentBeforeLoop() {
    test("function f(n) { var x; x = 1; for (var i = 0; i < n; i++) { x = i; alert(x); } }",
         "function f(n) { var x; for (var i = 0; i < n; i++) { x = i; alert(x); } }");
  }

  @Test
  public void testWhileLoopMayNotExecute() {
    testSame("function f(c) { var x; x = 1; while (c) { x = 2; c = false; } return x; }");
  }

  @Test
  public void testDoLoopAlwaysExecutes() {
    testSame("function f() { var x; do { x = 1; } while (false); return x; }");
  }

  @Test
  public void testForInLoopAssignmentLive() {
    testSame("function f(obj) { var x; x = 1; for (x in obj) {} return x; }");
  }

  @Test
  public void testCapturedVariableAssignmentLive() {
    testSame("function f() { var x; x = 1; return function() { return x; }; }");
  }

  @Test
  public void testCapturedVariableAssignmentLiveAfterClosure() {
    testSame("function f() { var x; var g = function() { return x; }; x = 1; return g(); }");
  }

  @Test
  public void testAssignmentUsedInRhsLive() {
    testSame("function f() { var x; x = 1; x = x + 1; return x; }");
  }

  @Test
  public void testMultipleDeadAssignments() {
    test("function f() { var x, y; x = 1; y = 2; x = 3; return x; }",
         "function f() { var x, y; x = 3; return x; }");
  }

  @Test
  public void testAssignmentInTernary() {
    test("function f(c) { var x; x = 1; x = c ? 2 : 3; return x; }",
         "function f(c) { var x; x = c ? 2 : 3; return x; }");
  }

  @Test
  public void testGlobalNotRemoved() {
    testSame("var x = 1; x = 2;");
  }

  @Test
  public void testPropertyNotRemoved() {
    testSame("function f(a) { a.x = 1; a.x = 2; }");
  }

  @Test
  public void testTryFinallyAssignmentLive() {
    testSame("function f() { var x; try { x = 1; } finally { alert(x); } }");
  }

  @Test
  public void testDeadAssignmentInTryCatch() {
    test("function f() { var x; try { x = 1; x = 2; } catch (e) {} return x; }",
         "function f() { var x; try { x = 2; } catch (e) {} return x; }");
  }
}