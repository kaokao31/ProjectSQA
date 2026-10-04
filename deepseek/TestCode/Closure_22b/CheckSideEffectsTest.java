package com.google.javascript.jscomp;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

/**
 * Test suite for CheckSideEffects.
 * Exercises various JavaScript constructs to ensure side-effect detection works.
 */
public class CheckSideEffectsTest extends CompilerTestCase {

  private boolean protectSideEffects = false;

  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    return new CheckSideEffects(compiler, CheckLevel.WARNING, protectSideEffects);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    protectSideEffects = false;
  }

  // ==================== No side-effect constructs ====================

  @Test
  public void testLiteralNoSideEffectWarns() {
    testWarning("1;", "1;");
  }

  @Test
  public void testStringLiteralNoSideEffectWarns() {
    testWarning("'a';", "'a';");
  }

  @Test
  public void testNameNoSideEffectWarns() {
    testWarning("x;", "x;");
  }

  @Test
  public void testPropertyAccessNoSideEffectWarns() {
    testWarning("x.y;", "x.y;");
  }

  @Test
  public void testArrayLiteralNoSideEffectWarns() {
    testWarning("[1,2];", "[1,2];");
  }

  @Test
  public void testObjectLiteralNoSideEffectWarns() {
    testWarning("({a:1});", "({a:1});");
  }

  @Test
  public void testBinaryOpNoSideEffectWarns() {
    testWarning("a + b;", "a + b;");
  }

  @Test
  public void testLogicalOpNoSideEffectWarns() {
    testWarning("a && b;", "a && b;");
  }

  @Test
  public void testConditionalNoSideEffectWarns() {
    testWarning("a ? b : c;", "a ? b : c;");
  }

  @Test
  public void testTypeOfNoSideEffectWarns() {
    testWarning("typeof a;", "typeof a;");
  }

  @Test
  public void testVoidNoSideEffectWarns() {
    testWarning("void 0;", "void 0;");
  }

  // ==================== Side-effect constructs (should NOT warn) ====================

  @Test
  public void testCallNoSideEffectWarning() {
    testSame("foo();");
  }

  @Test
  public void testNewNoSideEffectWarning() {
    testSame("new Foo();");
  }

  @Test
  public void testAssignNoSideEffectWarning() {
    testSame("x = 1;");
  }

  @Test
  public void testCompoundAssignNoSideEffectWarning() {
    testSame("x += 1;");
  }

  @Test
  public void testIncNoSideEffectWarning() {
    testSame("x++;");
  }

  @Test
  public void testDecNoSideEffectWarning() {
    testSame("x--;");
  }

  @Test
  public void testDeleteNoSideEffectWarning() {
    testSame("delete x.y;");
  }

  @Test
  public void testCallWithSideEffectsInArgsNoWarning() {
    testSame("foo(bar());");
  }

  @Test
  public void testNestedCallNoWarning() {
    testSame("foo()();");
  }

  // ==================== Control flow ====================

  @Test
  public void testIfWithSideEffectBodyNoWarning() {
    testSame("if (x) foo();");
  }

  @Test
  public void testIfWithNoSideEffectBodyWarns() {
    testWarning("if (x) 1;", "if (x) 1;");
  }

  @Test
  public void testIfEmptyBodyWarns() {
    testWarning("if (x) ;", "if (x) ;");
  }

  @Test
  public void testWhileWithSideEffectBodyNoWarning() {
    testSame("while (x) foo();");
  }

  @Test
  public void testWhileWithNoSideEffectBodyWarns() {
    testWarning("while (x) 1;", "while (x) 1;");
  }

  @Test
  public void testForWithNoSideEffectBodyWarns() {
    testWarning("for (;;) 1;", "for (;;) 1;");
  }

  @Test
  public void testDoWhileWithNoSideEffectBodyWarns() {
    testWarning("do { 1; } while (x);", "do { 1; } while (x);");
  }

  @Test
  public void testReturnWithNoSideEffectExpressionWarns() {
    testWarning("function f() { return 1; }", "function f() { return 1; }");
  }

  @Test
  public void testReturnWithSideEffectExpressionNoWarning() {
    testSame("function f() { return foo(); }");
  }

  // ==================== protectSideEffects ====================

  @Test
  public void testProtectSideEffectsTrue() {
    protectSideEffects = true;
    testSame("1;");
  }

  @Test
  public void testProtectSideEffectsTrueWithCall() {
    protectSideEffects = true;
    testSame("foo();");
  }

  @Test
  public void testProtectSideEffectsTrueWithProperty() {
    protectSideEffects = true;
    testSame("x.y;");
  }

  // ==================== Edge cases ====================

  @Test
  public void testEmptyInput() {
    testSame("");
  }

  @Test
  public void testMultipleStatements() {
    testWarning("1; 2; 3;", "1; 2; 3;");
  }

  @Test
  public void testTryCatchWithSideEffectNoWarning() {
    testSame("try { foo(); } catch (e) { bar(); }");
  }

  @Test
  public void testTryCatchWithoutSideEffectWarns() {
    testWarning("try { 1; } catch (e) { 2; }", "try { 1; } catch (e) { 2; }");
  }

  @Test
  public void testThrowNoWarning() {
    testSame("throw new Error();");
  }

  @Test
  public void testThisNoSideEffectWarns() {
    testWarning("this;", "this;");
  }

  @Test
  public void testTemplateLiteralNoSideEffectWarns() {
    testWarning("`hello`;", "`hello`;");
  }

  // ==================== Bug-specific regression ====================

  @Test
  public void testChainedPropertyNoSideEffectWarns() {
    testWarning("a.b.c;", "a.b.c;");
  }

  @Test
  public void testChainedCallNoWarning() {
    testSame("a.b().c();");
  }

  @Test
  public void testCallOnPropertyNoWarning() {
    testSame("a.b();");
  }

  @Test
  public void testProtectSideEffectsNested() {
    protectSideEffects = true;
    testSame("function f() { return 1; }");
  }

  @Test
  public void testSuspiciousIfStatement() {
    // Regression for issue where `if (x);` could cause a warning
    testWarning("if (x);", "if (x);");
  }

  @Test
  public void testEmptyStatementNoWarning() {
    testSame(";");
  }
}