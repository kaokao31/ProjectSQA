package com.google.javascript.jscomp;

import org.junit.Test;

public class UnreachableCodeEliminationTest extends CompilerTestCase {

  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    return new UnreachableCodeElimination(compiler);
  }

  @Test
  public void testNoUnreachableCode() throws Exception {
    testSame("function f() { g(); }");
  }

  @Test
  public void testRemoveStatementAfterReturn() throws Exception {
    test("function f() { return 1; g(); }", "function f() { return 1; }");
  }

  @Test
  public void testRemoveStatementAfterThrow() throws Exception {
    test("function f() { throw 1; g(); }", "function f() { throw 1; }");
  }

  @Test
  public void testRemoveStatementAfterBreak() throws Exception {
    test("function f(x) { while (x) { break; g(); } }",
         "function f(x) { while (x) { break; } }");
  }

  @Test
  public void testRemoveStatementAfterContinue() throws Exception {
    test("function f(x) { while (x) { continue; g(); } }",
         "function f(x) { while (x) { continue; } }");
  }

  @Test
  public void testRemoveUnreachableReturnInsideIf() throws Exception {
    test("function f(x) { if (x) { return 1; return 2; } }",
         "function f(x) { if (x) { return 1; } }");
  }

  @Test
  public void testRemoveUnreachableCodeInsideTry() throws Exception {
    test("function f() { try { return 1; return 2; } finally { } }",
         "function f() { try { return 1; } finally { } }");
  }

  @Test
  public void testPreserveVarDeclarationAfterReturn() throws Exception {
    testSame("function f() { return; var x = 1; }");
  }

  @Test
  public void testPreserveFunctionDeclarationAfterReturn() throws Exception {
    testSame("function f() { return; function g() {} }");
  }

  @Test
  public void testPreserveHoistedFunctionUsedBeforeDeclaration() throws Exception {
    testSame("function f() { g(); return; function g() {} }");
  }

  @Test
  public void testRemoveCodeAfterPreservedFunctionDeclaration() throws Exception {
    test("function f() { return; function g() {} h(); }",
         "function f() { return; function g() {} }");
  }

  @Test
  public void testFinallyBlockRemainsReachable() throws Exception {
    testSame("function f() { try { return; } finally { g(); } }");
  }

  @Test
  public void testCatchBlockRemainsReachable() throws Exception {
    testSame("function f() { try { throw 1; } catch (e) { g(); } }");
  }
}