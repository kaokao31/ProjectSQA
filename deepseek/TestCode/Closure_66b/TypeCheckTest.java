package com.google.javascript.jscomp;

import static org.junit.Assert.fail;

import com.google.javascript.jscomp.CheckLevel;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.DiagnosticGroups;
import com.google.javascript.jscomp.JSError;
import com.google.javascript.jscomp.SourceFile;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

public class TypeCheckTest {

  private Compiler compiler;
  private CompilerOptions options;

  @Before
  public void setUp() {
    compiler = new Compiler();
    options = new CompilerOptions();
    options.setCheckTypes(true);
    options.setWarningLevel(DiagnosticGroups.CHECK_TYPES, CheckLevel.ERROR);
  }

  private JSError[] check(String code) {
    return check("", code);
  }

  private JSError[] check(String externs, String code) {
    compiler.compile(
        SourceFile.fromCode("externs", externs),
        SourceFile.fromCode("input", code),
        options);

    JSError[] errors = compiler.getErrors();
    JSError[] warnings = compiler.getWarnings();
    JSError[] all = new JSError[errors.length + warnings.length];
    System.arraycopy(errors, 0, all, 0, errors.length);
    System.arraycopy(warnings, 0, all, errors.length, warnings.length);
    return all;
  }

  private void assertNoErrors(String code) {
    JSError[] errors = check(code);
    if (errors.length > 0) {
      fail("Expected no errors, got: " + Arrays.toString(errors));
    }
  }

  private void assertNoErrors(String externs, String code) {
    JSError[] errors = check(externs, code);
    if (errors.length > 0) {
      fail("Expected no errors, got: " + Arrays.toString(errors));
    }
  }

  private void assertErrors(String code) {
    JSError[] errors = check(code);
    if (errors.length == 0) {
      fail("Expected errors but none were reported.");
    }
  }

  private void assertErrors(String externs, String code) {
    JSError[] errors = check(externs, code);
    if (errors.length == 0) {
      fail("Expected errors but none were reported.");
    }
  }

  @Test
  public void testValidNumberDeclaration() {
    assertNoErrors("/** @type {number} */ var x = 1;");
  }

  @Test
  public void testTypeMismatchInVariableDeclaration() {
    assertErrors("/** @type {number} */ var x = 'a';");
  }

  @Test
  public void testTypeMismatchAssignment() {
    assertErrors("/** @type {number} */ var x = 1; x = 'a';");
  }

  @Test
  public void testKnownPropertyOnConstructorInstances() {
    assertNoErrors(
        "/** @constructor */ function Foo() { this.bar = 1; }" +
        "(new Foo()).bar;");
  }

  @Test
  public void testMissingPropertyOnConstructorInstances() {
    assertErrors(
        "/** @constructor */ function Foo() {}" +
        "(new Foo()).bar;");
  }

  @Test
  public void testCorrectParameterType() {
    assertNoErrors(
        "/** @param {number} x */ function f(x) {}" +
        "f(1);");
  }

  @Test
  public void testWrongParameterType() {
    assertErrors(
        "/** @param {number} x */ function f(x) {}" +
        "f('a');");
  }

  @Test
  public void testCorrectReturnType() {
    assertNoErrors(
        "/** @return {number} */ function f() { return 1; }");
  }

  @Test
  public void testWrongReturnType() {
    assertErrors(
        "/** @return {number} */ function f() { return 'a'; }");
  }

  @Test
  public void testConstructorCall() {
    assertNoErrors(
        "/** @constructor */ function Foo() {}" +
        "new Foo();");
  }

  @Test
  public void testInterfaceCannotBeInstantiated() {
    assertErrors(
        "/** @interface */ function Foo() {}" +
        "new Foo();");
  }

  @Test
  public void testObjectLiteralMethod() {
    assertNoErrors("var obj = { method: function() { return 1; } };");
  }

  @Test
  public void testObjectLiteralMethodWithThis() {
    assertNoErrors("var obj = { method: function() { return this; } };");
  }

  @Test
  public void testNestedFunctionWithThis() {
    assertNoErrors("function f() { return function() { return this; }; }");
  }

  @Test
  public void testCallOnNonFunction() {
    assertErrors("var f = 1; f();");
  }

  @Test
  public void testNewOnNonConstructor() {
    assertErrors("var f = 1; new f();");
  }

  @Test
  public void testArrayTypeMatch() {
    assertNoErrors("/** @type {Array.<number>} */ var x = [1, 2];");
  }

  @Test
  public void testArrayTypeMismatch() {
    assertErrors("/** @type {Array.<number>} */ var x = ['a'];");
  }

  @Test
  public void testFunctionTypeMatch() {
    assertNoErrors(
        "/** @type {function(number): string} */ var f = function(x) { return 'a'; };");
  }

  @Test
  public void testFunctionTypeMismatch() {
    assertErrors(
        "/** @type {function(number): string} */ var f = function(x) { return 1; };");
  }

  @Test
  public void testExternConstructorProperty() {
    assertNoErrors(
        "/** @constructor */ function Foo() { this.bar = 1; }",
        "var f = new Foo(); f.bar;");
  }

  @Test
  public void testExternMissingProperty() {
    assertErrors(
        "/** @constructor */ function Foo() {}",
        "var f = new Foo(); f.bar;");
  }

  @Test
  public void testObjectLiteralKnownProperty() {
    assertNoErrors("var x = {a: 1}; x.a;");
  }

  @Test
  public void testObjectLiteralUnknownProperty() {
    assertErrors("var x = {a: 1}; x.b;");
  }

  @Test
  public void testTernaryTypeMismatch() {
    assertErrors("/** @type {number} */ var x = true ? 1 : 'a';");
  }

  @Test
  public void testLogicalExpressionType() {
    assertNoErrors("/** @type {number} */ var x = 0 || 1;");
  }

  @Test
  public void testEnumType() {
    assertNoErrors(
        "/** @enum {number} */ var E = {A: 1, B: 2};" +
        "var x = E.A;");
  }

  @Test
  public void testTypeCast() {
    assertNoErrors("var x = /** @type {number} */ (1);");
  }

  @Test
  public void testNullPropertyAccess() {
    assertErrors("var x = null; x.foo;");
  }

  @Test
  public void testStringPropertyAccess() {
    assertNoErrors("var x = 'foo'; x.length;");
  }

  @Test
  public void testThisPropertyAccess() {
    assertErrors(
        "/** @constructor */ function Foo() {}" +
        "/** @this {Foo} */ function f() { return this.x; }");
  }

  @Test
  public void testThisPropertyAccessWithDeclaration() {
    assertNoErrors(
        "/** @constructor */ function Foo() { this.x = 1; }" +
        "/** @this {Foo} */ function f() { return this.x; }");
  }

  @Test
  public void testLendsAnnotation() {
    assertNoErrors(
        "/** @constructor */ function Foo() {}" +
        "var obj = /** @lends {Foo.prototype} */ { method: function() {} };");
  }

  @Test
  public void testWrongArgumentCount() {
    assertErrors(
        "/** @param {number} x */ function f(x) {}" +
        "f(1, 2);");
  }

  @Test
  public void testOptionalParameter() {
    assertNoErrors(
        "/** @param {number=} x */ function f(x) {}" +
        "f();");
  }
}