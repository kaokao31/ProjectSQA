package com.google.javascript.jscomp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.jscomp.NodeUtil;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * Test suite for {@link DevirtualizePrototypeMethods}.
 * Designed to achieve maximum code coverage and reveal potential bugs.
 */
public class DevirtualizePrototypeMethodsTest {

  private Compiler compiler;
  private CompilerOptions options;
  private DevirtualizePrototypeMethods pass;

  @Before
  public void setUp() {
    compiler = new Compiler();
    options = new CompilerOptions();
    // Enable devirtualization
    options.setDevirtualizePrototypeMethods(true);
    pass = new DevirtualizePrototypeMethods();
  }

  /**
   * Helper to compile JavaScript source and return the compiled AST.
   */
  private Node compile(String js) {
    SourceFile input = SourceFile.fromCode("test.js", js);
    compiler.compile(
        SourceFile.fromCode("externs.js", "function Function() {}"), input, options);
    return compiler.getRoot().getLastChild();
  }

  /**
   * Helper to run DevirtualizePrototypeMethods on a given source and return the modified AST.
   */
  private Node applyPass(String js) {
    Node root = compile(js);
    pass.process(compiler, root);
    return root;
  }

  @Test
  public void testSimplePrototypeMethodCall() {
    String js = "/** @constructor */ function Foo() {}\n"
        + "Foo.prototype.bar = function() { return 1; };\n"
        + "var x = new Foo();\n"
        + "x.bar();";
    Node root = applyPass(js);
    // Expect that x.bar() is replaced with Foo.prototype.bar.call(x)
    // Check for a CALL node with GETPROP target
    String result = compiler.toSource();
    assertTrue("Devirtualized call should appear: Foo.prototype.bar.call(x)",
        result.contains("Foo.prototype.bar.call(x)"));
  }

  @Test
  public void testNotDevirtualizedOnWrongReceiver() {
    // Call on a different object type that doesn't have the method defined
    String js = "/** @constructor */ function Foo() {}\n"
        + "Foo.prototype.bar = function() { return 1; };\n"
        + "/** @constructor */ function Baz() {}\n"
        + "Baz.prototype.bar = function() { return 2; };\n"
        + "var x = new Foo();\n"
        + "var y = new Baz();\n"
        + "x.bar();\n"
        + "y.bar();";
    Node root = applyPass(js);
    String result = compiler.toSource();
    // Only call on Foo should be devirtualized (or both if both are valid?)
    // Actually both have prototype methods, but devirtualization only applies when
    // the receiver type is known. Here types are known, so both should be devirtualized.
    // To test non-devirtualization, we need a case where receiver is not a known constructor.
    // Let's test a global object call.
  }

  @Test
  public void testNotDevirtualizedOnUnknownReceiver() {
    String js = "var x = {};\n"
        + "x.bar = function() { return 1; };\n"
        + "x.bar();";
    Node root = applyPass(js);
    String result = compiler.toSource();
    // Should not devirtualize because x is not a constructor with a prototype.
    assertTrue("Should keep original call", result.contains("x.bar()"));
  }

  @Test
  public void testMethodWithThisUsage() {
    String js = "/** @constructor */ function Foo(val) { this.val = val; }\n"
        + "Foo.prototype.getVal = function() { return this.val; };\n"
        + "var x = new Foo(42);\n"
        + "x.getVal();";
    Node root = applyPass(js);
    String result = compiler.toSource();
    // Should be devirtualized: Foo.prototype.getVal.call(x)
    assertTrue("Devirtualized call should appear",
        result.contains("Foo.prototype.getVal.call(x)"));
  }

  @Test
  public void testMethodNotOnPrototype() {
    String js = "/** @constructor */ function Foo() {}\n"
        + "Foo.prototype.bar = function() { return 1; };\n"
        + "var x = new Foo();\n"
        + "x.baz();"; // baz not defined
    Node root = applyPass(js);
    String result = compiler.toSource();
    // Should not devirtualize (method not found in prototype chain)
    assertTrue("Should keep original call", result.contains("x.baz()"));
  }

  @Test
  public void testCallOnSuperType() {
    // Testing that devirtualization handles prototype chain correctly
    String js = "/** @constructor */ function Super() {}\n"
        + "Super.prototype.method = function() { return 1; };\n"
        + "/** @constructor @extends {Super} */ function Sub() {}\n"
        + "goog.inherits(Sub, Super);\n"
        + "var x = new Sub();\n"
        + "x.method();";
    Node root = applyPass(js);
    String result = compiler.toSource();
    // Should devirtualize to Super.prototype.method.call(x)
    assertTrue("Should devirtualize to Super prototype",
        result.contains("Super.prototype.method.call(x)"));
  }

  @Test
  public void testMultipleCallsSameMethod() {
    String js = "/** @constructor */ function Foo() {}\n"
        + "Foo.prototype.bar = function() { return 1; };\n"
        + "var x = new Foo();\n"
        + "x.bar();\n"
        + "x.bar();";
    Node root = applyPass(js);
    String result = compiler.toSource();
    // Both calls should be devirtualized
    int count = countOccurrences(result, "Foo.prototype.bar.call(x)");
    assertEquals("Both calls should be devirtualized", 2, count);
  }

  @Test
  public void testMethodCallWithArguments() {
    String js = "/** @constructor */ function Foo() {}\n"
        + "Foo.prototype.add = function(a, b) { return a + b; };\n"
        + "var x = new Foo();\n"
        + "x.add(3, 4);";
    Node root = applyPass(js);
    String result = compiler.toSource();
    // Should be: Foo.prototype.add.call(x, 3, 4)
    assertTrue("Arguments should be passed", result.contains("Foo.prototype.add.call(x, 3, 4)"));
  }

  @Test
  public void testNoDevirtualizationOnGlobalFunction() {
    String js = "function globalFunc() { return 1; }\n"
        + "globalFunc();";
    Node root = applyPass(js);
    String result = compiler.toSource();
    // Should not be affected
    assertTrue("Global function call should remain", result.contains("globalFunc()"));
  }

  @Test(expected = Exception.class)
  public void testNullInput() {
    pass.process(compiler, null);
  }

  @Test
  public void testEmptyProgram() {
    String js = "";
    Node root = applyPass(js);
    // Should not throw and produce empty output
    String result = compiler.toSource();
    assertTrue("Empty source should produce empty output", result.isEmpty());
  }

  @Test
  public void testNoMatchingCalls() {
    String js = "/** @constructor */ function Foo() {}\n"
        + "Foo.prototype.bar = function() { return 1; };\n"
        + "var z = Foo.prototype.bar();"; // static call to prototype method
    Node root = applyPass(js);
    String result = compiler.toSource();
    // This is a direct call to the prototype method, not an instance call.
    // Should not devirtualize (already static).
    assertTrue("Static call to prototype method should remain unchanged",
        result.contains("Foo.prototype.bar()"));
  }

  @Test
  public void testDeoptimizedDueToSideEffects() {
    // If the method has side effects that depend on 'this', devirtualization might be skipped.
    // We need to check that the pass correctly identifies when 'this' is used.
    // Already covered in testMethodWithThisUsage.
  }

  @Test
  public void testCovariantReceiver() {
    // Where receiver type is known but method defined in supertype
    String js = "/** @constructor */ function A() {}\n"
        + "A.prototype.foo = function() { return 'A'; };\n"
        + "/** @constructor @extends {A} */ function B() {}\n"
        + "goog.inherits(B, A);\n"
        + "var b = new B();\n"
        + "b.foo();";
    Node root = applyPass(js);
    String result = compiler.toSource();
    assertTrue("Should devirtualize to A.prototype.foo.call(b)",
        result.contains("A.prototype.foo.call(b)"));
  }

  // Helper to count occurrences
  private int countOccurrences(String str, String sub) {
    int count = 0;
    int idx = 0;
    while ((idx = str.indexOf(sub, idx)) != -1) {
      count++;
      idx += sub.length();
    }
    return count;
  }
}