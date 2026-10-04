package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import com.google.javascript.jscomp.CompilerOptions.LanguageMode;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class CheckGlobalThisTest {

  private Compiler compiler;
  private CheckGlobalThis checkGlobalThis;

  @Before
  public void setUp() {
    compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setLanguageMode(LanguageMode.ECMASCRIPT5);
    checkGlobalThis = new CheckGlobalThis();
  }

  private Node parseAndGetRoot(String code) {
    Node root = compiler.parseSyntheticCode(code);
    assertNotNull("Parsing failed", root);
    return root;
  }

  private void runCheck(Node root) {
    checkGlobalThis.process(compiler, root);
  }

  private void assertNoWarnings() {
    assertTrue("Expected no warnings, but got: " + compiler.getWarnings(), 
               compiler.getWarnings().isEmpty());
    assertTrue("Expected no errors, but got: " + compiler.getErrors(), 
               compiler.getErrors().isEmpty());
  }

  private void assertWarningType(JSError.Type expectedType) {
    assertFalse("Expected warnings, but none found", compiler.getWarnings().isEmpty());
    for (JSError warning : compiler.getWarnings()) {
      assertEquals("Warning type mismatch", expectedType, warning.getType());
    }
  }

  // Test basic function declaration - no THIS reference
  @Test
  public void testFunctionWithoutThis() {
    String code = "function foo() { var x = 1; }";
    Node root = parseAndGetRoot(code);
    runCheck(root);
    assertNoWarnings();
  }

  // Test global THIS in function (should warn)
  @Test
  public void testGlobalThisInFunction() {
    String code = "function foo() { this.bar = 1; }";
    Node root = parseAndGetRoot(code);
    runCheck(root);
    assertWarningType(CheckGlobalThis.GLOBAL_THIS);
  }

  // Test nested function with THIS (should warn)
  @Test
  public void testNestedFunctionGlobalThis() {
    String code = "function outer() { function inner() { this.baz = 2; } }";
    Node root = parseAndGetRoot(code);
    runCheck(root);
    assertWarningType(CheckGlobalThis.GLOBAL_THIS);
  }

  // Test object method - should NOT warn
  @Test
  public void testObjectMethodThis() {
    String code = "var obj = { method: function() { this.x = 1; } };";
    Node root = parseAndGetRoot(code);
    runCheck(root);
    assertNoWarnings();
  }

  // Test constructor function - should NOT warn
  @Test
  public void testConstructorThis() {
    String code = "function MyClass() { this.x = 1; }";
    Node root = parseAndGetRoot(code);
    runCheck(root);
    assertNoWarnings();
  }

  // Test prototype method - should NOT warn
  @Test
  public void testPrototypeMethodThis() {
    String code = "MyClass.prototype.method = function() { this.x = 1; };";
    Node root = parseAndGetRoot(code);
    runCheck(root);
    assertNoWarnings();
  }

  // Test goog.bind with this reference (should be treated as safe)
  @Test
  public void testGoogBindThis() {
    String code = "var fn = goog.bind(function() { this.x = 1; }, obj);";
    Node root = parseAndGetRoot(code);
    runCheck(root);
    assertNoWarnings();
  }

  // Test .call() with this reference (should be treated as safe)
  @Test
  public void testCallWithThis() {
    String code = "function foo() { this.x = 1; } foo.call(obj);";
    Node root = parseAndGetRoot(code);
    runCheck(root);
    assertNoWarnings();
  }

  // Test .apply() with this reference (should be treated as safe)
  @Test
  public void testApplyWithThis() {
    String code = "function foo() { this.x = 1; } foo.apply(obj);";
    Node root = parseAndGetRoot(code);
    runCheck(root);
    assertNoWarnings();
  }

  // Test eval with this reference (should warn - bug detection)
  @Test
  public void testEvalWithThis() {
    String code = "function foo() { eval('this.x = 1'); }";
    Node root = parseAndGetRoot(code);
    runCheck(root);
    assertWarningType(CheckGlobalThis.GLOBAL_THIS);
  }

  // Test arrow function (ES6 - should not warn because arrow functions inherit this)
  @Test
  public void testArrowFunctionThis() {
    String code = "var obj = { method: function() { var arrow = () => { this.x = 1; }; } };";
    Node root = parseAndGetRoot(code);
    runCheck(root);
    assertNoWarnings();
  }

  // Test getter with this
  @Test
  public void testGetterThis() {
    String code = "var obj = { get prop() { return this.x; } };";
    Node root = parseAndGetRoot(code);
    runCheck(root);
    assertNoWarnings();
  }

  // Test setter with this
  @Test
  public void testSetterThis() {
    String code = "var obj = { set prop(v) { this.x = v; } };";
    Node root = parseAndGetRoot(code);
    runCheck(root);
    assertNoWarnings();
  }

  // Test global THIS at top level (no function wrapper)
  @Test
  public void testGlobalThisTopLevel() {
    String code = "this.foo = 1;";
    Node root = parseAndGetRoot(code);
    runCheck(root);
    assertNoWarnings(); // Top-level this is allowed
  }

  // Test nested objects with method (should not warn)
  @Test
  public void testNestedObjectMethod() {
    String code = "var outer = { inner: { method: function() { this.x = 1; } } };";
    Node root = parseAndGetRoot(code);
    runCheck(root);
    assertNoWarnings();
  }

  // Test IIFE with this reference (should warn)
  @Test
  public void testIIFEWithGlobalThis() {
    String code = "(function() { this.x = 1; })();";
    Node root = parseAndGetRoot(code);
    runCheck(root);
    assertWarningType(CheckGlobalThis.GLOBAL_THIS);
  }

  // Test function expression assigned to variable - not a constructor
  @Test
  public void testFunctionExpressionGlobalThis() {
    String code = "var foo = function() { this.x = 1; };";
    Node root = parseAndGetRoot(code);
    runCheck(root);
    assertWarningType(CheckGlobalThis.GLOBAL_THIS);
  }

  // Test multiple this references in same function (should produce one warning)
  @Test
  public void testMultipleThisReferences() {
    String code = "function foo() { this.x = 1; this.y = 2; this.z = 3; }";
    Node root = parseAndGetRoot(code);
    runCheck(root);
    assertWarningType(CheckGlobalThis.GLOBAL_THIS);
    assertEquals("Should have exactly one warning for multiple this references", 
                 1, compiler.getWarnings().size());
  }

  // Test conditional this reference
  @Test
  public void testConditionalThisReference() {
    String code = "function foo(cond) { if (cond) { this.x = 1; } }";
    Node root = parseAndGetRoot(code);
    runCheck(root);
    assertWarningType(CheckGlobalThis.GLOBAL_THIS);
  }

  // Test this in for loop
  @Test
  public void testThisInForLoop() {
    String code = "function foo() { for (var i = 0; i < 10; i++) { this.x = i; } }";
    Node root = parseAndGetRoot(code);
    runCheck(root);
    assertWarningType(CheckGlobalThis.GLOBAL_THIS);
  }

  // Test this in a method that is added later (prototype assignment)
  @Test
  public void testPrototypeAssignmentMethod() {
    String code = "var obj = {}; obj.prototype.method = function() { this.x = 1; };";
    Node root = parseAndGetRoot(code);
    runCheck(root);
    assertNoWarnings();
  }

  // Test empty code - edge case
  @Test
  public void testEmptyCode() {
    String code = "";
    Node root = parseAndGetRoot(code);
    runCheck(root);
    assertNoWarnings();
  }

  // Test code with only comments
  @Test
  public void testCodeWithComments() {
    String code = "/* comment */ // line comment";
    Node root = parseAndGetRoot(code);
    runCheck(root);
    assertNoWarnings();
  }

  // Test var declaration with no this reference
  @Test
  public void testVarDeclarationNoThis() {
    String code = "function foo() { var x = 1, y = 2; }";
    Node root = parseAndGetRoot(code);
    runCheck(root);
    assertNoWarnings();
  }

  // Test exception handling with this
  @Test
  public void testTryCatchWithThis() {
    String code = "function foo() { try { this.x = 1; } catch(e) { this.y = 2; } }";
    Node root = parseAndGetRoot(code);
    runCheck(root);
    assertWarningType(CheckGlobalThis.GLOBAL_THIS);
  }

  // Test this in a function passed as callback
  @Test
  public void testCallbackWithThis() {
    String code = "function foo(callback) { callback(this.x); }";
    Node root = parseAndGetRoot(code);
    runCheck(root);
    assertNoWarnings(); // Inside callback not analyzed
  }

  // Test nested function in prototype method (should warn for inner function)
  @Test
  public void testNestedFunctionInsidePrototypeMethod() {
    String code = "MyClass.prototype.method = function() { function inner() { this.x = 1; } };";
    Node root = parseAndGetRoot(code);
    runCheck(root);
    assertWarningType(CheckGlobalThis.GLOBAL_THIS);
  }

  // Test computed property method
  @Test
  public void testComputedPropertyMethod() {
    String code = "var obj = { [prop]: function() { this.x = 1; } };";
    Node root = parseAndGetRoot(code);
    runCheck(root);
    assertNoWarnings();
  }

  // Test function as property of a function (should treat as method)
  @Test
  public void testFunctionAsPropertyOfFunction() {
    String code = "function Foo() {} Foo.someMethod = function() { this.x = 1; };";
    Node root = parseAndGetRoot(code);
    runCheck(root);
    assertNoWarnings();
  }

  // Test class syntax with this (ES6)
  @Test
  public void testClassMethodThis() {
    String code = "class MyClass { constructor() { this.x = 1; } method() { this.y = 2; } }";
    Node root = parseAndGetRoot(code);
    runCheck(root);
    assertNoWarnings();
  }

  // Test this inside a function passed to array.filter
  @Test
  public void testThisInArrayMethodCallback() {
    String code = "var result = [1, 2, 3].filter(function(x) { return this.predicate(x); }, this);";
    Node root = parseAndGetRoot(code);
    runCheck(root);
    assertNoWarnings(); // thisArg provided
  }
}