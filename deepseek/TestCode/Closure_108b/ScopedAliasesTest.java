package com.google.javascript.jscomp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ScopedAliasesTest {

  private static final String EXTERNS =
      "var goog = {}; "
          + "goog.scope = function(fn) {}; "
          + "goog.inherits = function(child, parent) {}; "
          + "goog.exportSymbol = function(name, obj) {};"
          + "var bar = {}; var baz = {}; var ns = {}; var x = {};";

  private String compileSuccess(String source) {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setClosurePass(true);
    options.setProcessClosurePrimitives(true);
    Result result = compiler.compile(
        SourceFile.fromCode("externs", EXTERNS),
        SourceFile.fromCode("test", source),
        options);
    assertTrue("Compilation failed: " + formatErrors(result), result.success);
    return compiler.toSource();
  }

  private Result compileResult(String source) {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setClosurePass(true);
    options.setProcessClosurePrimitives(true);
    return compiler.compile(
        SourceFile.fromCode("externs", EXTERNS),
        SourceFile.fromCode("test", source),
        options);
  }

  private String formatErrors(Result result) {
    StringBuilder sb = new StringBuilder();
    for (JSError error : result.errors) {
      sb.append(error.toString()).append("; ");
    }
    return sb.toString();
  }

  private static String compact(String js) {
    return js.replaceAll("\\s+", "");
  }

  private void assertCompilesTo(String source, String expected) {
    assertEquals(compact(expected), compact(compileSuccess(source)));
  }

  @Test
  public void testBasicAliasReplacement() {
    assertCompilesTo(
        "goog.scope(function() { var foo = bar.baz; foo(); });",
        "bar.baz();");
  }

  @Test
  public void testAliasInPropertyAccess() {
    assertCompilesTo(
        "goog.scope(function() { var foo = bar.baz; foo.qux(); });",
        "bar.baz.qux();");
  }

  @Test
  public void testAliasInNewExpression() {
    assertCompilesTo(
        "goog.scope(function() { var Foo = bar.Baz; new Foo(); });",
        "new bar.Baz();");
  }

  @Test
  public void testMultipleAliases() {
    assertCompilesTo(
        "goog.scope(function() { var a = foo.bar; var b = baz.qux; a(); b(); });",
        "foo.bar(); baz.qux();");
  }

  @Test
  public void testAliasUsedInObjectLiteral() {
    assertCompilesTo(
        "goog.scope(function() { var foo = bar.baz; var x = {foo: foo}; });",
        "var x = {foo: bar.baz};");
  }

  @Test
  public void testNonAliasVarPreserved() {
    assertCompilesTo(
        "goog.scope(function() { var Foo = function() {}; Foo.prototype.bar = function() {}; });",
        "var Foo = function() {}; Foo.prototype.bar = function() {};");
  }

  @Test
  public void testUnusedAliasRemoved() {
    String result = compileSuccess("goog.scope(function() { var foo = bar.baz; });");
    assertFalse(result.contains("foo"));
    assertFalse(result.contains("goog.scope"));
  }

  @Test
  public void testTypeAnnotation() {
    assertCompilesTo(
        "goog.scope(function() { var Foo = ns.Foo; /** @type {Foo} */ var x; });",
        "/** @type {ns.Foo} */ var x;");
  }

  @Test
  public void testTypeAnnotationInFunction() {
    assertCompilesTo(
        "goog.scope(function() { var Foo = ns.Foo; /** @param {Foo} x */ function f(x) {} });",
        "/** @param {ns.Foo} x */ function f(x) {}");
  }

  @Test
  public void testGoogInherits() {
    assertCompilesTo(
        "goog.scope(function() { var Child = ns.Child; var Parent = ns.Parent; "
            + "goog.inherits(Child, Parent); });",
        "goog.inherits(ns.Child, ns.Parent);");
  }

  @Test
  public void testAliasShadowingByInnerFunction() {
    assertCompilesTo(
        "goog.scope(function() { var foo = ns.foo; function f(foo) { return foo; } foo(); });",
        "function f(foo) { return foo; } ns.foo();");
  }

  @Test
  public void testAliasAssignedToProperty() {
    assertCompilesTo(
        "goog.scope(function() { var foo = ns.foo; foo.bar = 1; });",
        "ns.foo.bar = 1;");
  }

  @Test
  public void testAliasReassignmentFails() {
    Result result = compileResult(
        "goog.scope(function() { var foo = ns.foo; foo = 3; });");
    assertFalse("Expected error for alias reassignment", result.success);
  }

  @Test
  public void testGoogScopeNonFunctionFails() {
    Result result = compileResult("goog.scope(3);");
    assertFalse("Expected error for non-function argument", result.success);
  }

  @Test
  public void testGoogScopeFunctionWithParamFails() {
    Result result = compileResult("goog.scope(function(x) {});");
    assertFalse("Expected error for scope function with parameter", result.success);
  }

  @Test
  public void testNonAliasFunctionCallVarPreserved() {
    assertCompilesTo(
        "goog.scope(function() { var foo = bar(); foo(); });",
        "var foo = bar(); foo();");
  }

  @Test
  public void testAliasUsedInInstanceof() {
    assertCompilesTo(
        "goog.scope(function() { var Foo = bar.Foo; var b = x instanceof Foo; });",
        "var b = x instanceof bar.Foo;");
  }

  @Test
  public void testAliasUsedInTypeof() {
    assertCompilesTo(
        "goog.scope(function() { var foo = bar.baz; var t = typeof foo; });",
        "var t = typeof bar.baz;");
  }

  @Test
  public void testAliasUsedInExportSymbol() {
    assertCompilesTo(
        "goog.scope(function() { var Foo = bar.Foo; goog.exportSymbol('Foo', Foo); });",
        "goog.exportSymbol('Foo', bar.Foo);");
  }

  @Test
  public void testNoGoogScopeUnchanged() {
    assertCompilesTo("var x = 1;", "var x = 1;");
  }

  @Test
  public void testScopeInsideFunction() {
    assertCompilesTo(
        "function outer() { goog.scope(function() { var foo = bar.baz; foo(); }); }",
        "function outer() { bar.baz(); }");
  }

  @Test
  public void testTypeAnnotationInExtends() {
    assertCompilesTo(
        "goog.scope(function() { var Foo = ns.Foo; "
            + "/** @constructor @extends {Foo} */ var Bar = function() {}; });",
        "/** @constructor @extends {ns.Foo} */ var Bar = function() {};");
  }

  @Test
  public void testTypeAnnotationInArrayType() {
    assertCompilesTo(
        "goog.scope(function() { var Foo = ns.Foo; /** @type {Array.<Foo>} */ var x; });",
        "/** @type {Array.<ns.Foo>} */ var x;");
  }
}