package com.google.javascript.jscomp;

import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.JSSourceFile;
import com.google.javascript.jscomp.Result;
import com.google.javascript.rhino.Node;
import org.junit.Before;
import org.junit.Test;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Test suite for CollapseProperties compiler pass (Closure bug 130 context).
 * Designed to achieve high coverage and reveal potential aliasing-related faults.
 */
public class CollapsePropertiesTest {

  private Compiler compiler;
  private CompilerOptions options;

  @Before
  public void setUp() {
    compiler = new Compiler();
    options = new CompilerOptions();
    // Enable collapse properties
    options.setCollapseProperties(true);
    // Additional flags to mimic production settings
    options.setClosurePass(true);
    options.setCheckSymbols(true);
    // Include externs (simplified)
    Compiler.setLoggingLevel(java.util.logging.Level.SEVERE);
  }

  // Helper to compile source and run CollapseProperties pass
  private Result compile(String source) {
    JSSourceFile[] inputs = new JSSourceFile[] {
        JSSourceFile.fromCode("test.js", source)
    };
    JSSourceFile[] externs = new JSSourceFile[] {
        JSSourceFile.fromCode("externs.js", "")
    };
    return compiler.compile(externs, inputs, options);
  }

  // Helper to get flattened source
  private String getResultSource() {
    return compiler.toSource();
  }

  // --- Basic Property Collapse ---
  @Test
  public void testSimpleCollapse() {
    String source = "var a = {};\n" +
                    "a.b = 1;\n" +
                    "var x = a.b;";
    Result result = compile(source);
    assertTrue("Compilation should succeed", result.success);
    // After collapse, property access a.b should become a$b
    String out = getResultSource();
    assertNotNull("Output should not be null", out);
    // Basic check: the property chain should be replaced
    assertFalse("a.b should be collapsed", out.contains("a.b ="));
    assertTrue("a$b variable should exist", out.contains("a$b"));
  }

  // --- Collapse with alias ---
  // Known pattern for Defects4J bug 130: alias of a property subobject
  @Test
  public void testAliasCollapse() {
    String source = "var goog = {};\n" +
                    "goog.ui = {};\n" +
                    "var ui = goog.ui;\n" +
                    "ui.Component = function(){};\n" +
                    "var c = new goog.ui.Component();";
    Result result = compile(source);
    assertTrue("Compilation should succeed", result.success);
    String out = getResultSource();
    // The alias 'ui' should not break the collapse of goog.ui.Component
    // Bug scenario: if the pass renames goog.ui to something different,
    // the alias ui will still refer to the original object but the property
    // accesses via goog.ui.Component may be renamed incorrectly.
    // We verify that the constructor call is correctly renamed.
    assertTrue("goog.ui.Component should be collapsed to goog$ui$Component",
               out.contains("goog$ui$Component"));
    // Additionally, the alias 'ui' should still be usable (may still reference goog$ui)
    assertTrue("ui variable should exist", out.contains("var ui"));
  }

  // --- Prototype property collapse ---
  @Test
  public void testPrototypeCollapse() {
    String source = "var A = function() {};\n" +
                    "A.prototype.b = 1;\n" +
                    "var x = A.prototype.b;";
    Result result = compile(source);
    assertTrue("Compilation should succeed", result.success);
    String out = getResultSource();
    // Prototype accesses may be left uncollapsed in some cases; but if collapsed,
    // they should not cause errors.
    // For coverage, at least we ensure no failure.
  }

  // --- Conditional assignment (if block) ---
  @Test
  public void testConditionalAssignment() {
    String source = "var obj = {};\n" +
                    "if (true) {\n" +
                    "  obj.x = 1;\n" +
                    "}\n" +
                    "var y = obj.x;";
    Result result = compile(source);
    assertTrue("Compilation should succeed", result.success);
  }

  // --- Assignment of property to itself ---
  @Test
  public void testSelfAssignment() {
    String source = "var obj = {};\n" +
                    "obj.x = obj.x || 2;\n" +
                    "var z = obj.x;";
    Result result = compile(source);
    assertTrue("Compilation should succeed", result.success);
  }

  // --- Property access on this ---
  @Test
  public void testThisPropertyAccess() {
    String source = "function F() { this.x = 1; }\n" +
                    "F.prototype.m = function() { return this.x; };";
    Result result = compile(source);
    assertTrue("Compilation should succeed", result.success);
  }

  // --- Null / empty input ---
  @Test
  public void testEmptyProgram() {
    String source = "";
    Result result = compile(source);
    assertTrue("Empty program should compile", result.success);
    assertEquals("Empty program output should be empty or just whitespace",
                 "", getResultSource().trim());
  }

  // --- Large chain with multiple levels ---
  @Test
  public void testDeepCollapse() {
    String source = "var a = {};\n" +
                    "a.b = {};\n" +
                    "a.b.c = {};\n" +
                    "a.b.c.d = 1;\n" +
                    "var v = a.b.c.d;";
    Result result = compile(source);
    assertTrue("Compilation should succeed", result.success);
    String out = getResultSource();
    assertTrue("Deep chain should be collapsed to a$b$c$d",
               out.contains("a$b$c$d"));
  }

  // --- Function call result property ---
  @Test
  public void testFunctionCallProperty() {
    String source = "function f() { return {}; }\n" +
                    "var o = f();\n" +
                    "o.x = 1;\n" +
                    "var y = o.x;";
    Result result = compile(source);
    assertTrue("Compilation should succeed", result.success);
    // o is not a simple name; CollapseProperties may not collapse it
    // but should not crash.
  }

  // --- Multiple variables with similar prefixes ---
  @Test
  public void testConflictingPrefixes() {
    String source = "var a = {};\n" +
                    "var ab = {};\n" +
                    "a.b = 1;\n" +
                    "ab.c = 2;";
    Result result = compile(source);
    assertTrue("Compilation should succeed", result.success);
    // Ensure a.b becomes a$b, but ab remains unchanged
    String out = getResultSource();
    assertTrue("a.b should become a$b", out.contains("a$b"));
    assertFalse("ab should not be modified", out.contains("ab$c"));
  }

  // --- Shadow variable in inner scope ---
  @Test
  public void testShadowVariable() {
    String source = "var x = {};\n" +
                    "x.y = 1;\n" +
                    "(function() {\n" +
                    "  var x = {};\n" +
                    "  x.y = 2;\n" +
                    "})();\n" +
                    "var z = x.y;";
    Result result = compile(source);
    assertTrue("Compilation should succeed", result.success);
  }

  // --- getter/setter (ES5) (if supported by compiler) ---
  @Test
  public void testGetterSetter() {
    String source = "var obj = {};\n" +
                    "obj.__defineGetter__('p', function() { return 1; });\n" +
                    "var a = obj.p;";
    Result result = compile(source);
    // This may or may not be handled; we expect no crash.
    assertTrue("Compilation should succeed", result.success);
  }

  // --- Multiple assignments to same property ---
  @Test
  public void testMultiplePropertyAssignments() {
    String source = "var obj = {};\n" +
                    "obj.x = 1;\n" +
                    "obj.x = 2;\n" +
                    "var y = obj.x;";
    Result result = compile(source);
    assertTrue("Compilation should succeed", result.success);
    String out = getResultSource();
    // Both assignments should be collapsed
    assertFalse("obj.x assignment should be collapsed", out.contains("obj.x ="));
  }

  // --- Property access before assignment ---
  @Test
  public void testReadBeforeAssign() {
    String source = "var obj = {};\n" +
                    "var x = obj.y;\n" +
                    "obj.y = 1;";
    Result result = compile(source);
    assertTrue("Compilation should succeed", result.success);
  }
}