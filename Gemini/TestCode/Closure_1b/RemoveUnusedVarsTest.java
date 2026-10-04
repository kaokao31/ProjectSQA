package com.google.javascript.jscomp;

public class RemoveUnusedVarsTest extends CompilerTestCase {

  private boolean removeGlobal;
  private boolean preserveFunctionExpressionNames;
  private boolean modifyCallSites;

  public RemoveUnusedVarsTest() {
    super();
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    removeGlobal = true;
    preserveFunctionExpressionNames = false;
    modifyCallSites = false;
  }

  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    return new RemoveUnusedVars(compiler, removeGlobal, preserveFunctionExpressionNames, modifyCallSites);
  }

  @Override
  protected int getNumRepetitions() {
    return 1;
  }

  public void testRemoveUnusedSimpleVar() {
    test("var a = 1;", "");
    test("var a;", "");
    test("var a, b = 1, c;", "");
  }

  public void testKeepUsedVar() {
    testSame("var a = 1; alert(a);");
    testSame("var a; a = 1; alert(a);");
  }

  public void testRemoveUnusedVarWithSideEffects() {
    test("var a = foo();", "foo();");
    test("var a = 1, b = foo(), c = 2;", "foo();");
    test("var a = foo(), b = bar();", "foo(); bar();");
  }

  public void testRemoveUnusedFunctionDeclaration() {
    test("function foo() { alert(1); }", "");
    test("function foo() { function bar() { alert(1); } }", "");
    testSame("function foo() { alert(1); } foo();");
  }

  public void testRecursiveFunction() {
    test("function f() { f(); }", "");
    testSame("function f() { f(); } f();");
  }

  public void testMutualRecursion() {
    test("function f() { g(); } function g() { f(); }", "");
    testSame("function f() { g(); } function g() { f(); } f();");
  }

  public void testFunctionExpression() {
    test("var f = function() { alert(1); };", "");
    test("var f = function foo() { alert(1); };", "");
    test("var f = function foo() { foo(); };", "");
  }

  public void testPreserveFunctionExpressionNames() {
    preserveFunctionExpressionNames = true;
    testSame("var f = function foo() { return 1; }; f();");
    test("var f = function foo() { return 1; };", "");
  }

  public void testDoNotRemoveGlobalsWhenDisabled() {
    removeGlobal = false;
    testSame("var a = 1;");
    testSame("function foo() {}");
    test("function foo() { var a = 1; } foo();", "function foo() {} foo();");
  }

  public void testUnusedVarsInForLoops() {
    test("for (var a = 0; a < 10; a++) {}", "for (var a = 0; a < 10; a++) {}");
    test("for (var a in obj) {}", "for (var a in obj) {}");
    test("function f() { for (var a = 0, b = 1; a < 10; a++) {} } f();",
         "function f() { for (var a = 0; a < 10; a++) {} } f();");
  }

  public void testCatchClauseParameter() {
    testSame("try { foo(); } catch (e) { alert(e); }");
    testSame("try { foo(); } catch (e) {}");
  }

  public void testMultipleVarDeclarationsPartialUse() {
    test("var a = 1, b = 2; alert(a);", "var a = 1; alert(a);");
    test("var a = 1, b = 2; alert(b);", "var b = 2; alert(b);");
    test("var a = foo(), b = 2; alert(b);", "foo(); var b = 2; alert(b);");
  }

  public void testUnusedAssignToLocalVar() {
    test("function f() { var a = 1; a = 2; } f();", "function f() {} f();");
    test("function f() { var a = 1; a += 2; } f();", "function f() {} f();");
    test("function f() { var a = 1; a = foo(); } f();", "function f() { foo(); } f();");
  }

  public void testUnusedAssignPropertiesOnUnusedVar() {
    test("var a = {}; a.b = 1;", "");
    test("var a = {}; a.b = foo();", "foo();");
    test("var a = []; a[0] = 1;", "");
  }

  public void testModifyCallSites() {
    modifyCallSites = true;
    test("function f(a, b) { alert(a); } f(1, 2);",
         "function f(a) { alert(a); } f(1);");
    test("function f(a, b) { alert(a); } f(1);",
         "function f(a) { alert(a); } f(1);");
    test("function f(a, b) { alert(b); } f(1, 2);",
         "function f(a, b) { alert(b); } f(0, 2);");
  }

  public void testNestedFunctionsAndScoping() {
    test("function f() { var a = 1; function g() { alert(a); } return g; } f();",
         "function f() { var a = 1; function g() { alert(a); } return g; } f();");

    test("function f() { var a = 1; var b = 2; function g() { alert(a); } return g; } f();",
         "function f() { var a = 1; function g() { alert(a); } return g; } f();");
  }

  public void testShadowedVariables() {
    test("var a = 1; function f() { var a = 2; alert(a); } f(); alert(a);",
         "var a = 1; function f() { var a = 2; alert(a); } f(); alert(a);");
    test("var a = 1; function f() { var a = 2; } f(); alert(a);",
         "var a = 1; function f() {} f(); alert(a);");
  }

  public void testUnusedFunctionArgumentsWithoutCallSiteModification() {
    modifyCallSites = false;
    testSame("function f(a, b) { alert(a); } f(1, 2);");
    testSame("function f(a, b) { alert(b); } f(1, 2);");
  }

  public void testIncDecOnUnusedVar() {
    test("function f() { var x = 0; x++; } f();", "function f() {} f();");
    test("function f() { var x = 0; ++x; } f();", "function f() {} f();");
    test("function f() { var x = 0; x--; } f();", "function f() {} f();");
    test("function f() { var x = 0; --x; } f();", "function f() {} f();");
  }

  public void testComplexChainedAssignments() {
    test("var a, b; a = b = 1; alert(a);", "var a; a = 1; alert(a);");
    test("var a, b; a = b = 1; alert(b);", "var b; b = 1; alert(b);");
    test("var a, b, c; a = b = c = foo();", "foo();");
  }

  public void testExportedVariables() {
    testSame("/** @export */ var a = 1;");
    testSame("/** @export */ function f() {}");
  }

  public void testAnonymousFunctionSelfCalling() {
    test("(function() { var a = 1; })();", "(function() {})();");
    test("(function() { var a = foo(); })();", "(function() { foo(); })();");
  }

  public void testIssueClosureCases() {
    test("var goog = {}; goog.exportSymbol = function(name, val) {};" +
         "function Foo() {} Foo.prototype.bar = function() {};" +
         "goog.exportSymbol('Foo', Foo);",
         "var goog = {}; goog.exportSymbol = function(name, val) {};" +
         "function Foo() {} Foo.prototype.bar = function() {};" +
         "goog.exportSymbol('Foo', Foo);");
  }
}