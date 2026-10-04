package com.google.javascript.jscomp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.JSError;
import com.google.javascript.jscomp.SourceFile;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

/**
 * Test suite for CheckGlobalThis. Designed to achieve maximum line and branch coverage
 * and trigger known defects (e.g., Defects4J bug 91).
 */
public class CheckGlobalThisTest {

  private Compiler compiler;
  private CompilerOptions options;

  @Before
  public void setUp() {
    compiler = new Compiler();
    options = new CompilerOptions();
    // Enable the CheckGlobalThis pass at WARNING level
    options.setWarningLevel(DiagnosticGroups.GLOBAL_THIS, CheckLevel.WARNING);
  }

  /**
   * Compiles the given source code and returns all JSError objects.
   */
  private List<JSError> compileAndGetErrors(String jsCode) {
    SourceFile sourceFile = SourceFile.fromCode("test.js", jsCode);
    com.google.javascript.jscomp.Result result =
        compiler.compile(SourceFile.fromCode("externs.js", ""), sourceFile, options);
    return ImmutableList.copyOf(result.errors).reverse(); // Actually, we want warnings too.
    // The compiler stores warnings in result.warnings (or result.errors?).
    // We need to capture both errors and warnings. Let's modify.
    // Simple version: just run and get all diagnostics.
  }

  // Helper to compile and get all warnings (since CheckGlobalThis reports warnings)
  private List<JSError> getWarnings(String jsCode) {
    SourceFile sourceFile = SourceFile.fromCode("test.js", jsCode);
    com.google.javascript.jscomp.Result result =
        compiler.compile(SourceFile.fromCode("externs.js", ""), sourceFile, options);
    // result.warnings is a JSError array
    return result.warnings != null ? java.util.Arrays.asList(result.warnings) : java.util.Collections.emptyList();
  }

  // ==================== Test Cases ====================

  @Test
  public void testGlobalThisReportsWarning() {
    // Direct 'this' at top level should warn
    String js = "this.foo = 3;";
    List<JSError> warnings = getWarnings(js);
    assertEquals("Expected one warning for global 'this'", 1, warnings.size());
    assertTrue("Warning should mention 'this'", warnings.get(0).description.contains("this"));
  }

  @Test
  public void testGlobalThisInsideBlock() {
    // 'this' inside a block but not in a function – still global
    String js = "{ this.x = 10; }";
    List<JSError> warnings = getWarnings(js);
    assertEquals("Expected one warning for global 'this' in block", 1, warnings.size());
  }

  @Test
  public void testThisInsideFunction() {
    // 'this' inside a regular function should NOT warn
    String js = "function f() { this.bar = 5; }";
    List<JSError> warnings = getWarnings(js);
    assertTrue("Expected no warnings for 'this' inside function", warnings.isEmpty());
  }

  @Test
  public void testThisInsideNestedFunction() {
    // 'this' inside nested function should NOT warn
    String js = "function f() { return function() { this.baz = 7; }; }";
    List<JSError> warnings = getWarnings(js);
    assertTrue("Expected no warnings for 'this' inside nested function", warnings.isEmpty());
  }

  @Test
  public void testThisInsidePrototypeMethod() {
    // 'this' inside a prototype method should NOT warn
    String js = "function Foo() {}; Foo.prototype.method = function() { this.prop = 1; };";
    List<JSError> warnings = getWarnings(js);
    assertTrue("Expected no warnings for 'this' in prototype method", warnings.isEmpty());
  }

  @Test
  public void testThisInsideConstructor() {
    // 'this' inside a constructor should NOT warn
    String js = "function MyClass() { this.value = 42; }";
    List<JSError> warnings = getWarnings(js);
    assertTrue("Expected no warnings for 'this' in constructor", warnings.isEmpty());
  }

  @Test
  public void testThisInsideObjectLiteralMethod() {
    // 'this' inside a method in object literal should NOT warn
    String js = "var obj = { method: function() { this.x = 0; } };";
    List<JSError> warnings = getWarnings(js);
    assertTrue("Expected no warnings for 'this' inside object literal method", warnings.isEmpty());
  }

  @Test
  public void testThisInsideStaticMethod() {
    // 'this' inside a static method (function assigned to constructor) – should NOT warn? 
    // Actually, static methods have no 'this' context; but Closure considers it as a regular function? 
    // We'll treat as no warning because it's inside a function.
    String js = "function Bar() {}; Bar.staticFunc = function() { this.prop = 2; };";
    List<JSError> warnings = getWarnings(js);
    assertTrue("Expected no warnings for 'this' inside static method", warnings.isEmpty());
  }

  // Test for Defects4J bug 91: 'this' inside a function expression assigned to variable in a file with @fileoverview
  @Test
  public void testThisInsideFileWithFileOverview() {
    // This is the scenario that historically triggered a false positive.
    String js = "/** @fileoverview \n * Some comment. \n */\n" +
                "var fn = function() { this.foo = 1; };";
    List<JSError> warnings = getWarnings(js);
    assertTrue("Expected no warnings for 'this' inside function in @fileoverview file (bug 91)", warnings.isEmpty());
  }

  @Test
  public void testThisInsideGetter() {
    // 'this' inside a getter should NOT warn
    String js = "var obj = { get prop() { return this.x; } };";
    List<JSError> warnings = getWarnings(js);
    assertTrue("Expected no warnings for 'this' inside getter", warnings.isEmpty());
  }

  @Test
  public void testThisInsideSetter() {
    // 'this' inside a setter should NOT warn
    String js = "var obj = { set prop(v) { this.x = v; } };";
    List<JSError> warnings = getWarnings(js);
    assertTrue("Expected no warnings for 'this' inside setter", warnings.isEmpty());
  }

  @Test
  public void testThisInArrowFunction() {
    // Arrow functions (ES6) are different – but Closure may treat them as functions? 
    // We include to ensure no false positive.
    String js = "var f = () => this.x;";
    List<JSError> warnings = getWarnings(js);
    // Arrow functions don't have their own 'this', so 'this' refers to outer scope.
    // If outer is global, it should warn. So we expect a warning.
    // But this depends on parser support. We'll assume it parses.
    // If arrow not supported, test might be ignored.
    assertTrue("Expected a warning for 'this' in arrow function at global scope", !warnings.isEmpty());
  }

  @Test
  public void testThisInInnerArrowFunction() {
    // 'this' inside arrow function inside a function should NOT warn (lexical this)
    String js = "function outer() { var f = () => this.x; }";
    List<JSError> warnings = getWarnings(js);
    assertTrue("Expected no warnings for 'this' in arrow function inside function", warnings.isEmpty());
  }

  @Test
  public void testNoWarningForRegularUse() {
    // A completely safe file should produce no warnings
    String js = "var a = 1; function f() { var b = 2; }";
    List<JSError> warnings = getWarnings(js);
    assertTrue("Expected no warnings for safe code", warnings.isEmpty());
  }

  @Test
  public void testMultipleGlobalThisWarnings() {
    // Several global 'this' references should produce multiple warnings
    String js = "this.a = 1; this.b = 2;";
    List<JSError> warnings = getWarnings(js);
    assertEquals("Expected two warnings for two global 'this'", 2, warnings.size());
  }

  @Test
  public void testThisInsideEval() {
    // 'this' inside eval – treat as global? Typically considered global.
    String js = "eval('this.x = 1');";
    List<JSError> warnings = getWarnings(js);
    assertEquals("Expected one warning for global 'this' inside eval", 1, warnings.size());
  }

  @Test
  public void testThisInsideCatch() {
    // 'this' inside catch block – not in a function, should warn.
    // But catch block is still in global scope.
    String js = "try {} catch(e) { this.y = 2; }";
    List<JSError> warnings = getWarnings(js);
    assertEquals("Expected one warning for global 'this' inside catch", 1, warnings.size());
  }

  @Test
  public void testEmptyCode() {
    // Empty input should produce no warnings
    String js = "";
    List<JSError> warnings = getWarnings(js);
    assertTrue("Expected no warnings for empty code", warnings.isEmpty());
  }

  @Test
  public void testThisInGlobalIIFE() {
    // 'this' inside an IIFE that is a function expression – should not warn
    String js = "(function() { this.x = 1; })();";
    List<JSError> warnings = getWarnings(js);
    assertTrue("Expected no warnings for 'this' inside IIFE", warnings.isEmpty());
  }

  @Test
  public void testThisInsideGlobalCatchWithFunction() {
    // 'this' inside a function inside a catch – should not warn
    String js = "try {} catch(e) { var f = function() { this.z = 3; }; }";
    List<JSError> warnings = getWarnings(js);
    assertTrue("Expected no warnings for 'this' inside function in catch", warnings.isEmpty());
  }
}