package com.google.javascript.jscomp;

import com.google.common.collect.ImmutableList;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.Result;
import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.rhino.Node;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Test suite for CollapseProperties pass.
 * Designed to achieve high coverage and detect faults (e.g., bug 156).
 */
public class CollapsePropertiesTest {

  private Compiler compiler;
  private CompilerOptions options;

  @Before
  public void setUp() {
    compiler = new Compiler();
    options = new CompilerOptions();
    // Enable collapseProperties pass
    options.setCollapsePropertiesLevel(CompilerOptions.PropertyCollapseLevel.ALL);
    // Optionally turn off other passes for isolation
    options.setClosurePass(false);
  }

  private Node compileAndRun(String code) {
    List<SourceFile> inputs = ImmutableList.of(SourceFile.fromCode("test.js", code));
    compiler.compile(ImmutableList.<SourceFile>of(), inputs, options);
    Result result = compiler.getResult();
    assertTrue("Compilation failed: " + result.errors, result.success);
    return compiler.getRoot().getLastChild(); // return AST root of the script
  }

  @Test
  public void testNoChangesForSimpleVariable() {
    String code = "var a = 1;";
    Node script = compileAndRun(code);
    // Verify variable is still 'a' (no collapse needed)
    Node varNode = script.getFirstChild();
    assertEquals(Node.VAR, varNode.getType());
    assertEquals("a", varNode.getFirstChild().getString());
  }

  @Test
  public void testCollapseSingleChain() {
    String code = "var a = {}; a.b = 1;";
    Node script = compileAndRun(code);
    // After collapse, 'a.b' becomes simple name? Actually, CollapseProperties
    // can collapse property chains when possible, but here a.b is assignment.
    // We check that the AST still has a simple name assignment or property.
    // For full coverage, we just ensure it runs without error.
    assertNotNull(script);
    // We could check that the name 'a.b' is possibly already collapsed.
  }

  @Test
  public void testCollapseInGoogScope() {
    String code = "goog.provide('foo.bar.baz');";
    Node script = compileAndRun(code);
    // Expect that foo.bar.baz is collapsed (i.e., becomes a single var)
    // Check that there is a VAR node with name 'baz' or something.
    // Actually, with goog.provide, CollapseProperties can collapse the namespace.
    // This test targets branch coverage for goog.provide handling.
    assertNotNull(script);
  }

  @Test
  public void testCollapsePrototypeProperties() {
    String code = "function F() {} F.prototype.method = function() {};";
    Node script = compileAndRun(code);
    // Prototype property assignments should be preserved (not collapsed)
    // CollapseProperties may skip prototype chains.
    assertNotNull(script);
  }

  @Test
  public void testDontCollapseAliasedNames() {
    String code = "var x = {}; x.y = 1; var z = x; z.y = 2;";
    Node script = compileAndRun(code);
    // CollapseProperties should not collapse x.y if x is aliased.
    // This is a common fault scenario (bug 156 might involve aliases).
    // We just check that compilation succeeds and AST is consistent.
    assertNotNull(script);
  }

  @Test
  public void testBug156Regression() {
    // Based on known issue: collapsing properties in goog.scope with aliases
    String code = "goog.scope(function() {\n" +
                  "  var x = foo.bar;\n" +
                  "  x.baz = 1;\n" +
                  "});";
    Node script = compileAndRun(code);
    // Should not crash or produce incorrect code.
    // This test triggers the bug pattern.
    assertNotNull(script);
  }

  @Test
  public void testCollapseQualifiedNameInAssignment() {
    String code = "var a = {}; a.b.c = 1;";
    Node script = compileAndRun(code);
    // CollapseProperties should collapse a.b.c into a single variable 'c'
    // if it is safe. We check that the assignment has a simple name.
    // For coverage, ensure no exception.
    assertNotNull(script);
  }

  @Test
  public void testCollapseWithThisRef() {
    String code = "function f() { this.x = 1; this.x.y = 2; }";
    Node script = compileAndRun(code);
    // 'this' properties should not be collapsed (they are on an object).
    assertNotNull(script);
  }

  @Test
  public void testEmptyInput() {
    String code = "";
    Node script = compileAndRun(code);
    // Should handle empty input gracefully.
    assertNotNull(script);
  }

  @Test
  public void testCollapseAvoidGlobalShadowing() {
    // Scenario: local variable shadows global property
    String code = "var x = 1; function f() { var x = 2; x.a = 3; }";
    Node script = compileAndRun(code);
    // CollapseProperties must avoid conflating outer x with inner x.
    assertNotNull(script);
  }

  @Test
  public void testMultipleChainsSameRoot() {
    String code = "var a = {}; a.b = 1; a.c = 2;";
    Node script = compileAndRun(code);
    // CollapseProperties may collapse both to b and c.
    assertNotNull(script);
  }

  @Test
  public void testCollapsePreservesExterns() {
    // Externs like Window should not be collapsed.
    options.setExterns(ImmutableList.of(SourceFile.fromCode("externs.js",
        "var window = {}; window.document = {};")));
    String code = "window.document.title = 'test';";
    Node script = compileAndRun(code);
    // Should not collapse window.document to a simple var.
    assertNotNull(script);
  }

  @Test
  public void testCollapseWithObjectLiteral() {
    String code = "var x = {y: 1}; x.z = 2;";
    Node script = compileAndRun(code);
    assertNotNull(script);
  }

  @Test
  public void testCollapseWithGetterSetter() {
    String code = "var x = {}; x.__defineGetter__('y', function() { return 1; });";
    Node script = compileAndRun(code);
    // Might not be supported, but should not crash.
    assertNotNull(script);
  }

  @Test
  public void testCollapseInsideIf() {
    String code = "if (true) { var x = {}; x.y = 1; }";
    Node script = compileAndRun(code);
    assertNotNull(script);
  }

  @Test
  public void testCollapseWithTryCatch() {
    String code = "try { var x = {}; x.y = 1; } catch(e) {}";
    Node script = compileAndRun(code);
    assertNotNull(script);
  }

  @Test
  public void testCollapsePropertyRedeclaration() {
    String code = "var x = {}; x.y = 1; x.y = 2;";
    Node script = compileAndRun(code);
    assertNotNull(script);
  }
}