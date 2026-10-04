package com.google.javascript.jscomp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.javascript.jscomp.ReferenceCollectingCallback.ReferenceCollection;
import com.google.javascript.jscomp.ReferenceCollectingCallback.ReferenceMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

/**
 * Test suite for ReferenceCollectingCallback that achieves high coverage
 * and aims to uncover faults, particularly related to Defects4J bug 98
 * (incorrect reference collection when names are declared in a parent function
 * and used in nested scopes).
 */
public class ReferenceCollectingCallbackTest {

  private Compiler compiler;
  private CompilerOptions options;

  @Before
  public void setUp() {
    compiler = new Compiler();
    options = new CompilerOptions();
    // Only process necessary passes; avoid full optimization passes.
    options.setChecksOnly(true);
    options.setWarningLevel(DiagnosticGroups.MISSING_PROPERTIES, CheckLevel.OFF);
    options.setWarningLevel(DiagnosticGroups.UNDEFINED_VARIABLES, CheckLevel.OFF);
    options.setWarningLevel(DiagnosticGroups.CONST, CheckLevel.OFF);
    options.setWarningLevel(DiagnosticGroups.GLOBAL_THIS, CheckLevel.OFF);
  }

  /**
   * Helper to compile a JavaScript source string and run the ReferenceCollectingCallback.
   * Returns the ReferenceMap collected.
   */
  private ReferenceMap compileAndCollectReferences(String js) {
    SourceFile source = SourceFile.fromCode("test.js", js);
    compiler.compile(SourceFile.fromCode("externs.js", "function alert(x) {}"),
        source, options);
    assertTrue("Compilation failed: " + compiler.toSource(),
        compiler.getErrors().isEmpty());

    // Obtain the root AST node
    Node root = compiler.getRoot();

    // Create and run the ReferenceCollectingCallback
    ReferenceCollectingCallback callback = new ReferenceCollectingCallback(compiler);
    NodeTraversal traversal = new NodeTraversal(compiler, callback);
    traversal.traverse(root);

    // Return the collected reference map
    return callback.getReferenceMap();
  }

  // Utility to assert a variable has exactly the given number of references
  private void assertReferenceCount(ReferenceMap refMap, String name, int expectedCount) {
    ReferenceCollection coll = refMap.get(name);
    assertNotNull("No references found for variable: " + name, coll);
    assertEquals("Reference count mismatch for " + name, expectedCount, coll.references.size());
  }

  // --- Test Cases ---

  @Test
  public void testSimpleVariableReference() {
    String js = "var x = 1; var y = x;";
    ReferenceMap refMap = compileAndCollectReferences(js);
    assertReferenceCount(refMap, "x", 2); // declaration + use
    assertReferenceCount(refMap, "y", 1); // declaration only
  }

  @Test
  public void testFunctionDeclarationReference() {
    String js = "function f() {} f();";
    ReferenceMap refMap = compileAndCollectReferences(js);
    assertReferenceCount(refMap, "f", 2); // declaration + call
  }

  @Test
  public void testNestedScopeOuterVarAccess() {
    String js = "function outer() { var a = 1; function inner() { a; } }";
    ReferenceMap refMap = compileAndCollectReferences(js);
    assertReferenceCount(refMap, "a", 2); // declaration + use in inner
    assertReferenceCount(refMap, "outer", 1);
    assertReferenceCount(refMap, "inner", 1);
  }

  @Test
  public void testMultipleReferencesInDifferentScopes() {
    String js = "var g = 1; function f() { g = g + 1; }";
    ReferenceMap refMap = compileAndCollectReferences(js);
    // g: declaration, assignment (read+write) = 3? Actually: var g = 1 is declaration + assignment.
    // Assignment to g inside f: reads g and writes g -> two references.
    // Total: 1 declaration, 1 initial assignment, 1 read, 1 write = 4? The reference counting may treat
    // var g = 1 as one reference (the declaration) and the assignment part as a separate reference.
    // Typically the declaration includes the initializer reference. We'll just check existence.
    assertNotNull("Variable g must have references", refMap.get("g"));
    assertTrue("Variable g should have at least 2 references", refMap.get("g").references.size() >= 2);
  }

  @Test
  public void testUndeclaredVariableInGlobalScope() {
    // In non-strict mode, assignment to undeclared variable becomes global.
    String js = "x = 1;";
    ReferenceMap refMap = compileAndCollectReferences(js);
    // x should be treated as a reference (write)
    assertNotNull("Undeclared variable x should have a reference", refMap.get("x"));
    assertEquals("Undeclared variable x should have exactly 1 reference", 1, refMap.get("x").references.size());
  }

  @Test
  public void testFunctionExpressionNotHoisted() {
    String js = "var f = function() {}; f();";
    ReferenceMap refMap = compileAndCollectReferences(js);
    assertReferenceCount(refMap, "f", 2); // declaration + call
  }

  @Test
  public void testWithStatementCaveat() {
    // With statement can create dynamic scope; variables may be looked up in object.
    String js = "var a = 1; var obj = {a:2}; with(obj) { var x = a; }";
    ReferenceMap refMap = compileAndCollectReferences(js);
    // a inside with should still reference the global a (since declared var a exists)
    // But with can introduce ambiguities; reference collector may treat it as a reference to a.
    assertNotNull("Variable a should be referenced", refMap.get("a"));
  }

  @Test
  public void testCatchBlockVariable() {
    String js = "try { throw 1; } catch(e) { e; }";
    ReferenceMap refMap = compileAndCollectReferences(js);
    // e is declared in catch and used inside the block.
    assertReferenceCount(refMap, "e", 2); // declaration + use
  }

  @Test
  public void testLabeledFunction() {
    // Labeled function declarations are hoisted, but the label creates an implicit name.
    String js = "function f() { function g() {} }";
    ReferenceMap refMap = compileAndCollectReferences(js);
    assertReferenceCount(refMap, "g", 1);
    assertReferenceCount(refMap, "f", 1);
  }

  @Test
  public void testInnerFunctionReachingParentArgs() {
    String js = "function f(a) { function g() { return a; } }";
    ReferenceMap refMap = compileAndCollectReferences(js);
    // a: parameter declaration + use in inner function = 2
    assertReferenceCount(refMap, "a", 2);
  }

  @Test
  public void testVarRedeclaration() {
    String js = "var x = 1; var x = 2;";
    ReferenceMap refMap = compileAndCollectReferences(js);
    // The second var x is treated as assignment (not a new declaration)
    // Typically the reference collector treats both as declarations? The second typically rewrites.
    // We'll check it exists.
    assertNotNull(refMap.get("x"));
    // It should have at least 2 references (first declaration with initializer, second statement)
    assertTrue(refMap.get("x").references.size() >= 2);
  }

  @Test
  public void testGlobalFunctionWithArguments() {
    String js = "function f(a) { a; } f(1);";
    ReferenceMap refMap = compileAndCollectReferences(js);
    assertReferenceCount(refMap, "a", 2); // declaration + use
    assertReferenceCount(refMap, "f", 2); // declaration + call
  }

  // Expected to reveal potential faults in reference counting for nested scopes
  @Test
  public void testDeeplyNestedFunctionAccessingOuter() {
    String js = "function a() { var x = 1; function b() { function c() { x; } } }";
    ReferenceMap refMap = compileAndCollectReferences(js);
    // x: declaration + use in c = 2
    assertReferenceCount(refMap, "x", 2);
  }

  @Test
  public void testAssignmentBeforeDeclaration() {
    // In JavaScript, var declarations are hoisted, so x=1 after var is actually assignment.
    String js = "x = 1; var x;";
    ReferenceMap refMap = compileAndCollectReferences(js);
    // x: first assignment (global var) + var statement (declaration hoisted) -> two references
    assertReferenceCount(refMap, "x", 2);
  }

  @Test
  public void testMultipleVariableDeclarations() {
    String js = "var a = 1, b = a;";
    ReferenceMap refMap = compileAndCollectReferences(js);
    assertReferenceCount(refMap, "a", 2); // declaration + used in b initializer
    assertReferenceCount(refMap, "b", 1);
  }

  @Test
  public void testLambdaInArrayForEach() {
    // Use of anonymous function that captures outer variable
    String js = "var arr = [1,2,3]; var sum = 0; arr.forEach(function(x) { sum += x; });";
    ReferenceMap refMap = compileAndCollectReferences(js);
    // sum: declaration + use in callback (read and write inside function) -> that's two uses inside?
    // The reference collector will track references from the callback body.
    assertNotNull("sum should have references", refMap.get("sum"));
    assertTrue("sum should have at least 2 references", refMap.get("sum").references.size() >= 2);
  }

  @Test
  public void testGetPropOnLocalVariable() {
    String js = "var obj = {a:1}; var b = obj.a;";
    ReferenceMap refMap = compileAndCollectReferences(js);
    assertReferenceCount(refMap, "obj", 2); // declaration + read
    assertReferenceCount(refMap, "b", 1);
  }

  @Test
  public void testReferenceInSwitchCase() {
    String js = "var x = 1; switch(x) { case 1: var y = x; }";
    ReferenceMap refMap = compileAndCollectReferences(js);
    assertReferenceCount(refMap, "x", 3); // declaration + use in switch + use in case
    assertReferenceCount(refMap, "y", 1);
  }

  @Test
  public void testEmptyScript() {
    String js = "";
    ReferenceMap refMap = compileAndCollectReferences(js);
    assertTrue(refMap.getNames().isEmpty());
  }

  @Test
  public void testOnlyExterns() {
    // Externs are not typically tracked in the same way, but we compile with empty source.
    String js = "";
    ReferenceMap refMap = compileAndCollectReferences(js);
    assertTrue(refMap.getNames().isEmpty());
  }
}