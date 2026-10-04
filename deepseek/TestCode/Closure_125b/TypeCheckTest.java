package com.google.javascript.jscomp;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

public class TypeCheckTest {

  private List<JSError> typeCheck(String js) {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    options.setWarningLevel(DiagnosticGroups.CHECK_TYPES, CheckLevel.WARNING);

    SourceFile[] externs = new SourceFile[] {
        JSSourceFile.fromCode("externs.js", "")};
    SourceFile[] inputs = new SourceFile[] {
        JSSourceFile.fromCode("input.js", js)};
    compiler.compile(externs, inputs, options);

    List<JSError> diagnostics = new ArrayList<JSError>();
    Collections.addAll(diagnostics, compiler.getErrors());
    Collections.addAll(diagnostics, compiler.getWarnings());
    return diagnostics;
  }

  private void assertNoTypeErrors(String js) {
    List<JSError> diagnostics = typeCheck(js);
    assertTrue("Expected no type errors for: " + js + " but got " + diagnostics,
        diagnostics.isEmpty());
  }

  private void assertHasTypeError(String js) {
    List<JSError> diagnostics = typeCheck(js);
    assertFalse("Expected a type error for: " + js + " but got " + diagnostics,
        diagnostics.isEmpty());
  }

  @Test
  public void testValidSimpleNumber() {
    assertNoTypeErrors("var x = 1;");
  }

  @Test
  public void testValidSimpleString() {
    assertNoTypeErrors("var s = 'hello';");
  }

  @Test
  public void testValidSimpleBoolean() {
    assertNoTypeErrors("var b = true;");
  }

  @Test
  public void testValidArithmetic() {
    assertNoTypeErrors("var x = 1 + 2 * 3;");
  }

  @Test
  public void testValidComparison() {
    assertNoTypeErrors("var a = 1; var b = 2; var c = a < b;");
  }

  @Test
  public void testValidConditional() {
    assertNoTypeErrors("var x = true ? 1 : 2;");
  }

  @Test
  public void testValidVoidOperator() {
    assertNoTypeErrors("var x = void 0;");
  }

  @Test
  public void testValidFunctionDeclaration() {
    assertNoTypeErrors("function f() { return 1; }");
  }

  @Test
  public void testValidFunctionCall() {
    assertNoTypeErrors("function f() {}; f();");
  }

  @Test
  public void testValidFunctionParams() {
    assertNoTypeErrors("function f(/** number */ a, /** string */ b) {}; f(1, 'x');");
  }

  @Test
  public void testValidFunctionExpression() {
    assertNoTypeErrors("var f = function() { return 1; };");
  }

  @Test
  public void testValidNullableType() {
    assertNoTypeErrors("/** @type {?number} */ var x = null;");
  }

  @Test
  public void testValidNullableAssignment() {
    assertNoTypeErrors("/** @type {?string} */ var x = null; x = 'a';");
  }

  @Test
  public void testValidUnionType() {
    assertNoTypeErrors("/** @type {(number|string)} */ var x = 1; x = 'a';");
  }

  @Test
  public void testValidObjectLiteralType() {
    assertNoTypeErrors("/** @type {{x: number, y: string}} */ var o = {x: 1, y: 'a'};");
  }

  @Test
  public void testValidArrayType() {
    assertNoTypeErrors("/** @type {Array.<number>} */ var a = [1, 2];");
  }

  @Test
  public void testValidTypedFunctionExpression() {
    assertNoTypeErrors(
        "/** @type {function(number):number} */ var f = function(x) { return x; }; " +
        "var y = f(1);");
  }

  @Test
  public void testValidConstructor() {
    assertNoTypeErrors("/** @constructor */ function Foo() {}; var f = new Foo();");
  }

  @Test
  public void testValidPropertyAccess() {
    assertNoTypeErrors(
        "/** @constructor */ function Foo() { this.x = 1; }; " +
        "var f = new Foo(); var y = f.x;");
  }

  @Test
  public void testValidEnum() {
    assertNoTypeErrors("/** @enum {number} */ var Color = {RED: 1}; var c = Color.RED;");
  }

  @Test
  public void testValidTypedef() {
    assertNoTypeErrors(
        "/** @typedef {number|string} */ var T; " +
        "/** @type {T} */ var x = 1; x = 'a';");
  }

  @Test
  public void testValidCatchBlock() {
    assertNoTypeErrors("try { var x = 1; } catch (e) { e; }");
  }

  @Test
  public void testMismatchedVariableInitialization() {
    assertHasTypeError("/** @type {number} */ var x = 'a';");
  }

  @Test
  public void testMismatchedAssignment() {
    assertHasTypeError("/** @type {number} */ var x = 1; x = 'a';");
  }

  @Test
  public void testNullToNonNullableType() {
    assertHasTypeError(
        "/** @constructor */ function Foo() {}; " +
        "/** @type {!Foo} */ var x = null;");
  }

  @Test
  public void testUnknownProperty() {
    assertHasTypeError(
        "/** @constructor */ function Foo() {}; " +
        "var f = new Foo(); f.bar;");
  }

  @Test
  public void testPropertyOnPrimitive() {
    assertHasTypeError("var x = 1; x.foo;");
  }

  @Test
  public void testPropertyOnNull() {
    assertHasTypeError("var x = null; x.foo;");
  }

  @Test
  public void testParameterTypeMismatch() {
    assertHasTypeError("/** @param {number} a */ function f(a) {}; f('x');");
  }

  @Test
  public void testReturnTypeMismatch() {
    assertHasTypeError("/** @return {string} */ function f() { return 1; }");
  }

  @Test
  public void testMissingReturnValue() {
    assertHasTypeError("/** @return {number} */ function f() {}");
  }

  @Test
  public void testCallNonFunction() {
    assertHasTypeError("var x = 1; x();");
  }

  @Test
  public void testCallUndefinedValue() {
    assertHasTypeError("var x; x();");
  }

  @Test
  public void testCallNullableFunction() {
    assertHasTypeError("/** @type {?function()} */ var f = null; f();");
  }

  @Test
  public void testInstanceofPrimitiveRhs() {
    assertHasTypeError("var x = 1 instanceof 2;");
  }

  @Test
  public void testArrayElementTypeMismatch() {
    assertHasTypeError("/** @type {Array.<number>} */ var a = [1, 'x'];");
  }

  @Test
  public void testObjectLiteralPropertyTypeMismatch() {
    assertHasTypeError("/** @type {{x: number}} */ var o = {x: 'q'};");
  }

  @Test
  public void testMissingObjectLiteralProperty() {
    assertHasTypeError("/** @type {{x: number, y: number}} */ var o = {x: 1};");
  }

  @Test
  public void testTooManyFunctionArguments() {
    assertHasTypeError("function f(/** number */ a) {}; f(1, 2);");
  }

  @Test
  public void testTooFewFunctionArguments() {
    assertHasTypeError(
        "function f(/** number */ a, /** number */ b) {}; f(1);");
  }

  @Test
  public void testInvalidNewOnPrimitive() {
    assertHasTypeError("var x = 1; new x();");
  }
}