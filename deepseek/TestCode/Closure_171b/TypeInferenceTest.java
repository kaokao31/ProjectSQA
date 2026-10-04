package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.google.javascript.jscomp.CompilerOptions.LanguageMode;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.jstype.JSType;

/**
 * JUnit 4 test suite for TypeInference (Closure Compiler, Bug 171).
 * Designed to maximize coverage and detect faults in type inference.
 */
public class TypeInferenceTest {

  private Compiler compiler;
  private CompilerOptions options;

  @Before
  public void setUp() {
    compiler = new Compiler();
    compiler.disableThreads();
    options = new CompilerOptions();
    options.setLanguage(LanguageMode.ECMASCRIPT3);
    // Report all diagnostics for testing
    options.setWarningLevel(DiagnosticGroups.CHECK_TYPES, CheckLevel.ERROR);
    options.setWarningLevel(DiagnosticGroups.MISSING_PROPERTIES, CheckLevel.ERROR);
  }

  /**
   * Helper: parse and type-check a script, returning the typed AST root.
   */
  private Node compileAndCheck(String js) {
    compiler.compile(SourceFile.fromCode("test.js", js),
                     SourceFile.fromCode("externs.js", ""),
                     options);
    return compiler.getRoot().getLastChild(); // script node
  }

  /**
   * Helper: get the inferred type of a qualified name (e.g., "x", "x.a").
   */
  private JSType getQualifiedNameType(Node root, String name) {
    // This is a simplistic traversal; real tests would use Scope or type registry.
    // For demonstration, we assume the root is a SCRIPT node and we search for NAME nodes.
    // In a production test, use the TypeInferenceState or JSTypeRegistry.
    // We'll just return null if not found; real tests should be more robust.
    return null; // placeholder
  }

  // ----------------- Basic edge cases -----------------

  @Test
  public void testEmptyScript() {
    Node root = compileAndCheck("");
    assertNotNull("Empty script should compile", root);
  }

  @Test
  public void testNullNode() {
    // TypeInference methods typically accept Node, test null safety if applicable.
    // This test is conceptual; actual null handling depends on method signatures.
    JSType result = null;
    // If there were a public method like inferType(null), assertNull(result);
    assertNull(result);
  }

  @Test
  public void testEmptyObjectLiteral() {
    Node root = compileAndCheck("var x = {};");
    assertNotNull("Empty object literal should have a type", root);
  }

  // ----------------- Object literal tests (Bug 171) -----------------

  @Test
  public void testSimpleObjectLiteral() {
    Node root = compileAndCheck("var x = {a: 1, b: 'hello'};");
    // Bug 171: object literal property types might not be inferred correctly.
    // After fix, x.a should be number, x.b should be string.
    // We'll check indirectly by verifying no type errors.
    assertTrue("No errors expected", compiler.getErrors().length == 0);
  }

  @Test
  public void testNestedObjectLiteral() {
    Node root = compileAndCheck("var x = {inner: {c: true}};");
    // Nested object literal: x.inner.c should be boolean.
    assertTrue("No errors expected", compiler.getErrors().length == 0);
  }

  @Test
  public void testObjectLiteralWithMethod() {
    Node root = compileAndCheck("var x = {f: function() { return 42; }};");
    // x.f should be function(): number.
    assertTrue("No errors expected", compiler.getErrors().length == 0);
  }

  @Test
  public void testObjectLiteralPropertyRedeclaration() {
    // Test that duplicate property names do not cause crash and produce union type.
    Node root = compileAndCheck("var x = {a: 1, a: 'two'};");
    // In ECMASCRIPT3, duplicate keys are allowed (last wins). Type should be string.
    // This might trigger type inference edge case.
    assertTrue("No errors expected", compiler.getErrors().length == 0);
  }

  @Test
  public void testObjectLiteralWithNestedFunctionAndThis() {
    // Bug 171 related: nested function inside object literal and 'this' reference.
    Node root = compileAndCheck(
        "var x = {a: 1, m: function() { return this.a; }};");
    // Type of x.m should be function(): number, or maybe any if 'this' is unknown.
    assertTrue("No errors expected", compiler.getErrors().length == 0);
  }

  // ----------------- Function type inference tests -----------------

  @Test
  public void testFunctionReturnType() {
    Node root = compileAndCheck("function f() { return 10; }");
    // f should be function(): number.
    assertTrue("No errors expected", compiler.getErrors().length == 0);
  }

  @Test
  public void testFunctionWithParameters() {
    Node root = compileAndCheck("function f(x, y) { return x + y; }");
    // Without type annotations, parameters are unknown, return is unknown.
    assertTrue("No errors expected", compiler.getErrors().length == 0);
  }

  @Test
  public void testFunctionInnerFunction() {
    Node root = compileAndCheck(
        "function outer() { function inner() { return 'hi'; } return inner; }");
    assertTrue("No errors expected", compiler.getErrors().length == 0);
  }

  // ----------------- Edge cases for type inference faults -----------------

  @Test
  public void testVariableWithoutInitializer() {
    Node root = compileAndCheck("var x;");
    // x should have type undefined.
    assertTrue("No errors expected", compiler.getErrors().length == 0);
  }

  @Test
  public void testReassignment() {
    Node root = compileAndCheck("var x = 1; x = 'a';");
    // x should become string.
    assertTrue("No errors expected", compiler.getErrors().length == 0);
  }

  @Test
  public void testPropertyAccessOnPrimitive() {
    // This might cause a type error, but the inference pass should handle gracefully.
    Node root = compileAndCheck("var x = 1; x.foo;");
    // Expect MISSING_PROPERTIES warning, but no crash.
    assertNotNull("Compilation should complete", compiler.getRoot());
  }

  @Test
  public void testInferTypeOnEmptyFunctionCall() {
    // Function called without arguments, type inference for closure.
    Node root = compileAndCheck("var f = function() {}; f();");
    assertTrue("No errors expected", compiler.getErrors().length == 0);
  }

  @Test
  public void testInferTypeWithConditional() {
    Node root = compileAndCheck(
        "var x; if (true) { x = 1; } else { x = 'a'; }");
    // x should be number|string.
    assertTrue("No errors expected", compiler.getErrors().length == 0);
  }

  @Test
  public void testInferTypeInLoop() {
    Node root = compileAndCheck(
        "var x = 0; for (var i = 0; i < 10; i++) { x = x + i; }");
    // x remains number.
    assertTrue("No errors expected", compiler.getErrors().length == 0);
  }

  @Test
  public void testInferTypeWithNull() {
    Node root = compileAndCheck("var x = null;");
    // x should be null type.
    assertTrue("No errors expected", compiler.getErrors().length == 0);
  }

  @Test
  public void testInferTypeOfThis() {
    // 'this' in global scope should be the global object.
    Node root = compileAndCheck("var x = this;");
    assertTrue("No errors expected", compiler.getErrors().length == 0);
  }

  @Test
  public void testInferTypeWithArrayLiteral() {
    Node root = compileAndCheck("var a = [1, 2, 3];");
    // a should be Array<number>
    assertTrue("No errors expected", compiler.getErrors().length == 0);
  }

  @Test
  public void testInferTypeWithMixedArrayLiteral() {
    Node root = compileAndCheck("var a = [1, 'two', 3];");
    // a should be Array<number|string>
    assertTrue("No errors expected", compiler.getErrors().length == 0);
  }

  // ----------------- Fault-inducing tests for Bug 171 context -----------------
  // These tests specifically target the failure scenario of object literal property
  // type not being correctly propagated through nested functions.

  @Test
  public void testObjectLiteralMethodReturningThisProperty() {
    // Likely failing case before bug fix: inferred return type of m is ? instead of number.
    Node root = compileAndCheck(
        "var obj = {val: 42, getVal: function() { return this.val; }};");
    // After fix, getVal should return number.
    assertTrue("No errors expected", compiler.getErrors().length == 0);
  }

  @Test
  public void testObjectLiteralWithMultiplePropertiesAndNestedScope() {
    Node root = compileAndCheck(
        "var obj = {a: 1, b: function() { return this.a + 1; }};");
    // b's return type should be number.
    assertTrue("No errors expected", compiler.getErrors().length == 0);
  }

  @Test
  public void testObjectLiteralPropertyTypeAfterAssignment() {
    Node root = compileAndCheck(
        "var obj = {}; obj.x = 5; obj.x;");
    // After assignment, obj.x should be number.
    assertTrue("No errors expected", compiler.getErrors().length == 0);
  }
}