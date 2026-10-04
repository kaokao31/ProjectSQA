package com.google.javascript.jscomp;

import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.SourceFile;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test suite for RemoveUnusedVars compiler pass.
 * Targets line/branch coverage and specifically the bug from Closure Bug #45,
 * where a variable used solely in a property assignment (e.g., a.b = 5) was
 * incorrectly removed.
 */
public class RemoveUnusedVarsTest {

  private Compiler compiler;
  private CompilerOptions options;

  @Before
  public void setUp() {
    compiler = new Compiler();
    options = new CompilerOptions();
    // Enable the remove unused variables pass.
    options.setRemoveUnusedVariables(CompilerOptions.Reach.ALL);
    // Use safe code generation to avoid dropping other constructs.
    options.setAssumeClosuresOnlyOnReferences(true);
    options.setWarningLevel(com.google.javascript.jscomp.CheckLevel.OFF);
  }

  private String compileAndGetOutput(String jsCode) {
    SourceFile externs = SourceFile.fromCode("externs", "");
    SourceFile input = SourceFile.fromCode("test.js", jsCode);
    compiler.compile(externs, input, options);
    return compiler.toSource();
  }

  // ---- Basic unused removal tests ----

  @Test
  public void testUnusedVariableRemoved() {
    String js = "var x = 5; var y = 10;";
    String result = compileAndGetOutput(js);
    // Both x and y are unused; expect their declarations to be removed.
    assertFalse("x should be removed", result.contains("x"));
    assertFalse("y should be removed", result.contains("y"));
  }

  @Test
  public void testUsedVariableKept() {
    String js = "var x = 5; alert(x);";
    String result = compileAndGetOutput(js);
    assertTrue("used variable x should be kept", result.contains("x"));
  }

  // ---- Bug #45 scenario: variable used only in property assignment ----

  @Test
  public void testVariableUsedInPropertyAssignment() {
    // Variable a is used only as target of property assignment a.b = 5.
    // This is a valid usage and should NOT be removed.
    String js = "var a; a.b = 5;";
    String result = compileAndGetOutput(js);
    assertTrue("variable 'a' used in property assignment should be kept", result.contains("var a"));
  }

  @Test
  public void testVariableUsedInNestedPropertyAssignment() {
    // Variable obj is used as part of a deeper property chain.
    String js = "var obj; obj.x.y = 1;";
    String result = compileAndGetOutput(js);
    assertTrue("variable 'obj' used in nested property assignment should be kept", result.contains("var obj"));
  }

  @Test
  public void testVariableUsedInPropertyAccessNotRemoved() {
    // Variable used in property get (not set) should also be kept.
    String js = "var a; var b = a.c;";
    String result = compileAndGetOutput(js);
    assertTrue("variable 'a' used in property get should be kept", result.contains("var a"));
  }

  // ---- Edge cases: variables used in expressions ----

  @Test
  public void testVariableUsedInIncrement() {
    String js = "var x = 0; x++;";
    String result = compileAndGetOutput(js);
    assertTrue("x used in increment should be kept", result.contains("var x"));
  }

  @Test
  public void testVariableUsedInAssignmentToSelf() {
    String js = "var x = 5; x = x + 1;";
    String result = compileAndGetOutput(js);
    assertTrue("x used in self-assignment should be kept", result.contains("var x"));
  }

  @Test
  public void testVariableUsedInLogicalExpression() {
    String js = "var a = true; if (a && false) {}";
    String result = compileAndGetOutput(js);
    assertTrue("a used in logical expression should be kept", result.contains("var a"));
  }

  @Test
  public void testVariableUsedInTernary() {
    String js = "var x = 1; var y = x ? 2 : 3;";
    String result = compileAndGetOutput(js);
    assertTrue("x used in ternary condition should be kept", result.contains("var x"));
  }

  // ---- Scope and block tests ----

  @Test
  public void testVariableUsedInInnerBlock() {
    String js = "var x = 0; if (true) { x = 1; }";
    String result = compileAndGetOutput(js);
    assertTrue("x used inside a block should be kept", result.contains("var x"));
  }

  @Test
  public void testVariableDeclaredInBlockUsedOutside() {
    // The pass should keep variables that are used in outer scope.
    String js = "function f() { var v = 5; return v; }";
    String result = compileAndGetOutput(js);
    assertTrue("v used inside function should be kept", result.contains("var v") || result.contains("v = 5"));
  }

  @Test
  public void testGlobalVariableExportedNotRemoved() {
    // Variables mentioned in @export annotation may be preserved.
    // We simulate by having a global variable used only by an export comment.
    String js = "/** @export */ var exportedVar = 42;";
    String result = compileAndGetOutput(js);
    assertTrue("exported variable should be kept", result.contains("exportedVar"));
  }

  // ---- Aggressive mode tests ----

  @Test
  public void testAggressiveModeRemovesUnusedFunctionExpression() {
    options.setRemoveUnusedVariables(CompilerOptions.Reach.ALL);
    // In aggressive mode, even function expressions might be removed if unused.
    // But we focus on variables.
    String js = "var unusedFunc = function(){};";
    String result = compileAndGetOutput(js);
    assertFalse("unused function expression should be removed", result.contains("unusedFunc"));
  }

  // ---- Cascade assignment test ----

  @Test
  public void testCascadeAssignmentPreservesUsedVariables() {
    String js = "var a, b; a = b = 5;";
    // Both a and b are used – a assigned, b assigned and value used.
    String result = compileAndGetOutput(js);
    assertTrue("a should be kept", result.contains("var a"));
    assertTrue("b should be kept", result.contains("var b"));
  }

  // ---- Test that the pass does not introduce errors for valid code ----

  @Test
  public void testNoErrorsOnValidCode() {
    String js = "function test() { var x = 10; test2(); } function test2() { var y = 20; }";
    compiler.compile(SourceFile.fromCode("externs", ""),
                     SourceFile.fromCode("test.js", js), options);
    assertTrue("No errors should be reported", compiler.getErrors().length == 0);
  }

  // ---- Regression: variable used in array literal ----

  @Test
  public void testVariableUsedInArrayLiteral() {
    String js = "var x = [1, 2]; var y = x[0];";
    String result = compileAndGetOutput(js);
    assertTrue("x used in array element should be kept", result.contains("var x"));
  }

  // ---- Regression: variable used in object literal ----

  @Test
  public void testVariableUsedInObjectLiteralProperty() {
    String js = "var key = 'a'; var obj = {}; obj[key] = 1;";
    String result = compileAndGetOutput(js);
    assertTrue("key used in computed property should be kept", result.contains("var key"));
  }
}