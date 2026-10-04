package com.google.javascript.jscomp;

import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.jscomp.Result;
import com.google.javascript.rhino.Node;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test suite for MakeDeclaredNamesUnique compiler pass.
 * Designed to achieve high coverage and detect faults (e.g., bug 49).
 */
public class MakeDeclaredNamesUniqueTest {

  private Compiler compiler;
  private CompilerOptions options;

  @Before
  public void setUp() {
    compiler = new Compiler();
    options = new CompilerOptions();
    // Enable the pass under test
    options.setMakeDeclaredNamesUnique(true);
  }

  /**
   * Helper to compile source and return the root node after all passes.
   */
  private Node compile(String js) {
    SourceFile input = SourceFile.fromCode("test.js", js);
    compiler.compile(
        SourceFile.fromCode("externs.js", ""),
        input,
        options);
    return compiler.getRoot();
  }

  /**
   * Helper to get the pretty-printed output of the compiled code.
   */
  private String compileAndPrint(String js) {
    SourceFile input = SourceFile.fromCode("test.js", js);
    Result result = compiler.compile(
        SourceFile.fromCode("externs.js", ""),
        input,
        options);
    if (result.success) {
      return compiler.toSource();
    } else {
      return null;
    }
  }

  // ==================== Basic Renaming Tests ====================

  @Test
  public void testSimpleRename() {
    String js = "function f() { var x = 1; var x = 2; }";
    String output = compileAndPrint(js);
    assertNotNull("Compilation should succeed", output);
    // The second 'x' should be renamed to something like 'x$0'
    assertTrue("Second variable should be renamed", output.contains("x$0"));
  }

  @Test
  public void testNoRenameNeeded() {
    String js = "function f() { var a = 1; var b = 2; }";
    String output = compileAndPrint(js);
    assertNotNull(output);
    assertFalse("No renaming should occur", output.contains("$"));
  }

  @Test
  public void testRenameInNestedFunction() {
    String js = "function f() { var x = 1; function g() { var x = 2; } }";
    String output = compileAndPrint(js);
    assertNotNull(output);
    // Inner 'x' should be renamed to avoid conflict with outer 'x'
    assertTrue("Inner variable should be renamed", output.contains("x$0"));
  }

  // ==================== Edge Cases ====================

  @Test
  public void testEmptyFunction() {
    String js = "function f() {}";
    String output = compileAndPrint(js);
    assertNotNull(output);
    assertEquals("function f(){}", output.replaceAll("\\s+", ""));
  }

  @Test
  public void testGlobalScopeRename() {
    String js = "var x = 1; var x = 2;";
    String output = compileAndPrint(js);
    assertNotNull(output);
    assertTrue("Second global var should be renamed", output.contains("x$0"));
  }

  @Test
  public void testMultipleRenames() {
    String js = "function f() { var a = 1; var a = 2; var a = 3; }";
    String output = compileAndPrint(js);
    assertNotNull(output);
    assertTrue("Third variable should be renamed to a$1", output.contains("a$1"));
  }

  // ==================== Bug 49 Related: 'arguments' Handling ====================

  @Test
  public void testArgumentsNotRenamed() {
    String js = "function f() { var arguments = 1; }";
    String output = compileAndPrint(js);
    assertNotNull(output);
    // The 'arguments' variable should NOT be renamed (it's a reserved name)
    assertFalse("arguments should not be renamed", output.contains("arguments$"));
  }

  @Test
  public void testArgumentsInNestedFunction() {
    String js = "function f() { var arguments = 1; function g() { var arguments = 2; } }";
    String output = compileAndPrint(js);
    assertNotNull(output);
    // Inner 'arguments' should be renamed to avoid conflict with outer 'arguments'
    // But note: 'arguments' is special; the pass might treat it differently.
    // This test checks that renaming still occurs for inner 'arguments'.
    assertTrue("Inner arguments should be renamed", output.contains("arguments$0"));
  }

  // ==================== Loop and Block Scope ====================

  @Test
  public void testRenameInLoop() {
    String js = "function f() { for (var i = 0; i < 10; i++) { var i = 5; } }";
    String output = compileAndPrint(js);
    assertNotNull(output);
    assertTrue("Inner i should be renamed", output.contains("i$0"));
  }

  @Test
  public void testRenameInTryCatch() {
    String js = "function f() { try { var e = 1; } catch (e) { var e = 2; } }";
    String output = compileAndPrint(js);
    assertNotNull(output);
    // The catch variable 'e' and the outer 'e' should be disambiguated
    assertTrue("Catch variable should be renamed", output.contains("e$0"));
  }

  // ==================== Parameter Renaming ====================

  @Test
  public void testParameterRename() {
    String js = "function f(x) { var x = 1; }";
    String output = compileAndPrint(js);
    assertNotNull(output);
    // The local 'x' should be renamed to avoid conflict with parameter 'x'
    assertTrue("Local variable should be renamed", output.contains("x$0"));
  }

  @Test
  public void testMultipleParameters() {
    String js = "function f(a, b) { var a = 1; var b = 2; }";
    String output = compileAndPrint(js);
    assertNotNull(output);
    assertTrue("First parameter local should be renamed", output.contains("a$0"));
    assertTrue("Second parameter local should be renamed", output.contains("b$0"));
  }

  // ==================== Complex Scenarios ====================

  @Test
  public void testDeepNesting() {
    String js = "function f() { var x = 1; function g() { var x = 2; function h() { var x = 3; } } }";
    String output = compileAndPrint(js);
    assertNotNull(output);
    // Expect x$0 for g's x, x$1 for h's x
    assertTrue("g's x should be x$0", output.contains("x$0"));
    assertTrue("h's x should be x$1", output.contains("x$1"));
  }

  @Test
  public void testRenameWithSameNameInDifferentFunctions() {
    String js = "function f() { var x = 1; } function g() { var x = 2; }";
    String output = compileAndPrint(js);
    assertNotNull(output);
    // No renaming needed because they are in different scopes
    assertFalse("No renaming should occur", output.contains("$"));
  }

  // ==================== Null/Empty Input ====================

  @Test
  public void testEmptyScript() {
    String js = "";
    String output = compileAndPrint(js);
    assertNotNull(output);
    assertEquals("", output.trim());
  }

  @Test
  public void testOnlyComments() {
    String js = "// just a comment";
    String output = compileAndPrint(js);
    assertNotNull(output);
    // Should produce empty output or just the comment? Depends on compiler settings.
    // At least no crash.
    assertTrue("Output should be empty or contain comment", output.isEmpty() || output.contains("comment"));
  }

  // ==================== Fault Detection: Bug 49 Specific ====================

  @Test
  public void testBug49Regression() {
    // Bug 49: MakeDeclaredNamesUnique incorrectly renames 'arguments' in some cases.
    // This test ensures that 'arguments' is not renamed when it is the only declaration.
    String js = "function f() { var arguments; }";
    String output = compileAndPrint(js);
    assertNotNull(output);
    // The variable 'arguments' should remain as is.
    assertFalse("arguments should not be renamed", output.contains("arguments$"));
  }

  @Test
  public void testBug49WithNestedFunction() {
    // Another variant: inner function with 'arguments' parameter.
    String js = "function f() { function g(arguments) { var arguments = 1; } }";
    String output = compileAndPrint(js);
    assertNotNull(output);
    // The local 'arguments' should be renamed to avoid conflict with parameter.
    assertTrue("Local arguments should be renamed", output.contains("arguments$0"));
  }
}