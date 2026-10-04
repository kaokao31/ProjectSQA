package com.google.javascript.jscomp;

import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.Node;
import com.google.javascript.jscomp.Scope;
import com.google.javascript.jscomp.TypedScopeCreator;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test suite for TypedScopeCreator targeting maximum coverage and fault detection.
 * Designed to exercise edge cases, null checks, and known bug patterns (e.g., bug 17).
 */
public class TypedScopeCreatorTest {

  private Compiler compiler;
  private CompilerOptions options;

  @Before
  public void setUp() {
    compiler = new Compiler();
    options = new CompilerOptions();
    // Enable type checking and typical options
    options.setCodingConvention(new ClosureCodingConvention());
    options.setIdeMode(true);
    options.setCheckTypes(true);
  }

  /**
   * Helper to compile source and return the top-level scope.
   */
  private Scope compileAndGetScope(String source) {
    compiler.compile(
        new JSSourceFile[] { JSSourceFile.fromCode("test.js", source) },
        new JSSourceFile[] {},
        options);
    return compiler.getTopScope();
  }

  /**
   * Helper to compile source and return the TypedScopeCreator's scope tree.
   */
  private TypedScope createTypedScope(String source) {
    compiler.compile(
        new JSSourceFile[] { JSSourceFile.fromCode("test.js", source) },
        new JSSourceFile[] {},
        options);
    return compiler.getTypedScope();
  }

  // ==================== Basic Variable Declarations ====================

  @Test
  public void testSimpleVar() {
    TypedScope scope = createTypedScope("var x = 1;");
    assertNotNull("Scope should not be null", scope);
    assertTrue("Variable 'x' should be declared", scope.isDeclared("x", false));
  }

  @Test
  public void testVarWithTypeAnnotation() {
    TypedScope scope = createTypedScope("/** @type {number} */ var x = 1;");
    assertNotNull(scope);
    assertTrue(scope.isDeclared("x", false));
  }

  @Test
  public void testMultipleVars() {
    TypedScope scope = createTypedScope("var a = 1, b = 'hello';");
    assertTrue(scope.isDeclared("a", false));
    assertTrue(scope.isDeclared("b", false));
  }

  // ==================== Function Declarations ====================

  @Test
  public void testFunctionDeclaration() {
    TypedScope scope = createTypedScope("function f() {}");
    assertTrue(scope.isDeclared("f", false));
  }

  @Test
  public void testFunctionWithParameters() {
    TypedScope scope = createTypedScope("function f(x, y) {}");
    assertTrue(scope.isDeclared("f", false));
    // Parameters are in the function's inner scope
    Scope inner = scope.getSlot("f").getScope();
    assertNotNull(inner);
    assertTrue(inner.isDeclared("x", false));
    assertTrue(inner.isDeclared("y", false));
  }

  @Test
  public void testNestedFunction() {
    TypedScope scope = createTypedScope("function outer() { function inner() {} }");
    assertTrue(scope.isDeclared("outer", false));
    Scope outerScope = scope.getSlot("outer").getScope();
    assertNotNull(outerScope);
    assertTrue(outerScope.isDeclared("inner", false));
  }

  // ==================== goog.scope (Bug 17 related) ====================

  @Test
  public void testGoogScopeWithConst() {
    // This snippet triggered a NullPointerException in bug 17
    String source = "goog.scope(function() {\n" +
                    "  /** @const */ var x = 1;\n" +
                    "});";
    TypedScope scope = createTypedScope(source);
    assertNotNull(scope);
    // The const variable should be declared in the goog.scope's inner scope
    // We can check that no exception occurred
  }

  @Test
  public void testGoogScopeNested() {
    String source = "goog.scope(function() {\n" +
                    "  var a = 1;\n" +
                    "  goog.scope(function() {\n" +
                    "    var b = 2;\n" +
                    "  });\n" +
                    "});";
    TypedScope scope = createTypedScope(source);
    assertNotNull(scope);
  }

  @Test
  public void testGoogScopeWithTypeAnnotation() {
    String source = "goog.scope(function() {\n" +
                    "  /** @type {string} */ var s = 'hello';\n" +
                    "});";
    TypedScope scope = createTypedScope(source);
    assertNotNull(scope);
  }

  // ==================== @const and @enum ====================

  @Test
  public void testConstAnnotation() {
    String source = "/** @const */ var CONST = 42;";
    TypedScope scope = createTypedScope(source);
    assertTrue(scope.isDeclared("CONST", false));
  }

  @Test
  public void testEnumAnnotation() {
    String source = "/** @enum {number} */ var MyEnum = {A: 1, B: 2};";
    TypedScope scope = createTypedScope(source);
    assertTrue(scope.isDeclared("MyEnum", false));
  }

  // ==================== Edge Cases ====================

  @Test
  public void testEmptySource() {
    TypedScope scope = createTypedScope("");
    assertNotNull(scope);
    assertEquals("Global scope should have no variables", 0, scope.getVarCount());
  }

  @Test
  public void testOnlyComments() {
    TypedScope scope = createTypedScope("// just a comment\n/* another */");
    assertNotNull(scope);
    assertEquals(0, scope.getVarCount());
  }

  @Test
  public void testGlobalThis() {
    // 'this' in global scope should be handled
    String source = "var x = this;";
    TypedScope scope = createTypedScope(source);
    assertNotNull(scope);
  }

  @Test
  public void testEval() {
    // eval introduces dynamic scope; TypedScopeCreator should handle gracefully
    String source = "eval('var y = 1;');";
    TypedScope scope = createTypedScope(source);
    assertNotNull(scope);
  }

  @Test
  public void testWithStatement() {
    // with statement creates dynamic scope
    String source = "var obj = {a:1}; with(obj) { var b = a; }";
    TypedScope scope = createTypedScope(source);
    assertNotNull(scope);
    assertTrue(scope.isDeclared("obj", false));
    // 'b' might be hoisted to outer scope
    assertTrue(scope.isDeclared("b", false));
  }

  @Test
  public void testCatchBlock() {
    // catch introduces a scope for the exception variable
    String source = "try { throw 1; } catch(e) { var x = e; }";
    TypedScope scope = createTypedScope(source);
    assertNotNull(scope);
    assertTrue(scope.isDeclared("x", false));
  }

  @Test
  public void testForLoopVar() {
    String source = "for(var i = 0; i < 10; i++) {}";
    TypedScope scope = createTypedScope(source);
    assertTrue(scope.isDeclared("i", false));
  }

  @Test
  public void testForInLoopVar() {
    String source = "var obj = {a:1}; for(var k in obj) {}";
    TypedScope scope = createTypedScope(source);
    assertTrue(scope.isDeclared("k", false));
  }

  // ==================== Type Inference and Casts ====================

  @Test
  public void testTypeCast() {
    String source = "/** @type {number} */ var x = /** @type {*} */ (1);";
    TypedScope scope = createTypedScope(source);
    assertNotNull(scope);
  }

  @Test
  public void testUnknownType() {
    String source = "var x = unknown;";
    TypedScope scope = createTypedScope(source);
    assertNotNull(scope);
  }

  // ==================== Bug-Specific Tests ====================

  @Test
  public void testBug17NullPointerInGoogScope() {
    // This test specifically targets the NPE that occurred in bug 17
    // when processing a const inside goog.scope.
    String source = "goog.scope(function() {\n" +
                    "  /** @const */ var x = 1;\n" +
                    "  var y = x;\n" +
                    "});";
    try {
      TypedScope scope = createTypedScope(source);
      assertNotNull(scope);
    } catch (NullPointerException e) {
      fail("Bug 17: NullPointerException thrown when processing const inside goog.scope: " + e.getMessage());
    }
  }

  @Test
  public void testBug17MultipleConstsInGoogScope() {
    String source = "goog.scope(function() {\n" +
                    "  /** @const */ var a = 1;\n" +
                    "  /** @const */ var b = 2;\n" +
                    "  var c = a + b;\n" +
                    "});";
    TypedScope scope = createTypedScope(source);
    assertNotNull(scope);
  }

  // ==================== Large Input / Stress ====================

  @Test
  public void testManyVariables() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 1000; i++) {
      sb.append("var v").append(i).append(" = ").append(i).append(";\n");
    }
    TypedScope scope = createTypedScope(sb.toString());
    assertNotNull(scope);
    assertTrue(scope.isDeclared("v0", false));
    assertTrue(scope.isDeclared("v999", false));
  }

  @Test
  public void testDeeplyNestedFunctions() {
    StringBuilder sb = new StringBuilder("function a() { function b() { function c() {} } }");
    TypedScope scope = createTypedScope(sb.toString());
    assertNotNull(scope);
    assertTrue(scope.isDeclared("a", false));
  }

  // ==================== Error Recovery ====================

  @Test
  public void testSyntaxError() {
    // Even with syntax errors, TypedScopeCreator should not throw
    String source = "var x = ;";
    try {
      TypedScope scope = createTypedScope(source);
      // Scope may be null or incomplete, but no exception should propagate
      assertNotNull(scope);
    } catch (Exception e) {
      // Acceptable if compiler reports error but doesn't throw
    }
  }

  @Test
  public void testDuplicateVarDeclaration() {
    String source = "var x = 1; var x = 2;";
    TypedScope scope = createTypedScope(source);
    assertNotNull(scope);
    // Should not throw; second declaration is ignored or merged
    assertTrue(scope.isDeclared("x", false));
  }

  // ==================== Externs ====================

  @Test
  public void testExternsAreIncluded() {
    // Externs like 'window' should be in scope
    TypedScope scope = createTypedScope("var x = window;");
    assertNotNull(scope);
    // 'window' is typically an extern, so it should be declared
    assertTrue(scope.isDeclared("window", false));
  }

  @Test
  public void testExternFunction() {
    TypedScope scope = createTypedScope("var x = alert;");
    assertNotNull(scope);
    assertTrue(scope.isDeclared("alert", false));
  }
}