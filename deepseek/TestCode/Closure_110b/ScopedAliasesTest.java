package com.google.javascript.jscomp;

import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.CompilerPass;
import com.google.javascript.jscomp.CompilerTestCase;
import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.rhino.Node;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test suite for ScopedAliases.
 * Designed to achieve maximum coverage and detect faults
 * typical of Defects4J bug 110.
 */
public class ScopedAliasesTest extends CompilerTestCase {

  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    return new ScopedAliases(compiler, new CompilerOptions());
  }

  // ===================== Basic Aliasing =====================

  @Test
  public void testSimpleAlias() {
    test("goog.scope(function() { var foo = bar; var baz = foo; });",
         "goog.scope(function() { var foo = bar; var baz = bar; });");
  }

  @Test
  public void testAliasToGlobal() {
    test("var x = 1; goog.scope(function() { var y = x; });",
         "var x = 1; goog.scope(function() { var y = x; });");
  }

  @Test
  public void testAliasToAlias() {
    test("goog.scope(function() { var a = b; var c = a; var d = c; });",
         "goog.scope(function() { var a = b; var c = b; var d = b; });");
  }

  @Test
  public void testMultipleAliases() {
    test("goog.scope(function() { var x = a; var y = b; var z = x + y; });",
         "goog.scope(function() { var x = a; var y = b; var z = a + b; });");
  }

  // ===================== Edge Cases =====================

  @Test
  public void testNoAliases() {
    testSame("goog.scope(function() { var x = 1; });");
  }

  @Test
  public void testEmptyScope() {
    testSame("goog.scope(function() {});");
  }

  @Test
  public void testNoScopeUsage() {
    testSame("var x = 1;");
  }

  @Test
  public void testAliasUnused() {
    test("goog.scope(function() { var foo = bar; });",
         "goog.scope(function() { var foo = bar; });");
  }

  @Test
  public void testAliasReassigned() {
    testSame("goog.scope(function() { var foo = bar; foo = baz; });");
    // Reassignment should break the alias
  }

  @Test
  public void testNestedScopes() {
    test("goog.scope(function() { var a = x; goog.scope(function() { var b = a; }); });",
         "goog.scope(function() { var a = x; goog.scope(function() { var b = x; }); });");
  }

  @Test
  public void testDeepNestedScopes() {
    test("goog.scope(function() { var a = x; " +
         "goog.scope(function() { var b = a; " +
         "goog.scope(function() { var c = b; }); }); });",
         "goog.scope(function() { var a = x; " +
         "goog.scope(function() { var b = x; " +
         "goog.scope(function() { var c = x; }); }); });");
  }

  // ===================== Error and Warning Conditions =====================

  @Test
  public void testDuplicateAliasWarning() {
    // Same alias name twice should produce a warning
    testSame("goog.scope(function() { var foo = bar; var foo = baz; });");
    assertTrue("Expected duplicate alias warning",
               getLastCompiler().getWarnings().length > 0);
  }

  @Test
  public void testAliasWithSameNameAsOriginal() {
    testSame("goog.scope(function() { var bar = bar; });");
    // Should not cause infinite loop or error
  }

  @Test
  public void testAliasToUndefined() {
    testSame("goog.scope(function() { var x = y; });");
    // y is undefined, but ScopedAliases may not report error
  }

  @Test
  public void testAliasWithSideEffects() {
    // Alias initializer with side effects should not be removed
    testSame("goog.scope(function() { var x = console.log('side'); });");
  }

  @Test
  public void testAliasInFunctionInsideScope() {
    test("goog.scope(function() { var x = a; function f() { return x; } });",
         "goog.scope(function() { var x = a; function f() { return a; } });");
  }

  @Test
  public void testAliasInInnerFunction() {
    test("goog.scope(function() { var x = a; var obj = {method: function() { return x; }}; });",
         "goog.scope(function() { var x = a; var obj = {method: function() { return a; }}; });");
  }

  // ===================== Complex Scenarios =====================

  @Test
  public void testAliasThroughMultipleScopes() {
    test("goog.scope(function() { var a = x; " +
         "goog.scope(function() { var b = a; }); " +
         "goog.scope(function() { var c = a; }); });",
         "goog.scope(function() { var a = x; " +
         "goog.scope(function() { var b = x; }); " +
         "goog.scope(function() { var c = x; }); });");
  }

  @Test
  public void testAliasShadowedByLocalVar() {
    test("goog.scope(function() { var x = a; var y = x; var x = b; var z = x; });",
         "goog.scope(function() { var x = a; var y = a; var x = b; var z = b; });");
  }

  @Test
  public void testGlobalAliasInScope() {
    // Global alias definitions are not allowed inside goog.scope
    // This should not be transformed
    testSame("goog.scope(function() { var x = y; }); var z = x;");
  }

  @Test
  public void testAliasInConditional() {
    test("goog.scope(function() { var x = a; if (true) { var y = x; } });",
         "goog.scope(function() { var x = a; if (true) { var y = a; } });");
  }

  @Test
  public void testAliasInLoop() {
    test("goog.scope(function() { var x = a; for (var i = 0; i < 10; i++) { var y = x; } });",
         "goog.scope(function() { var x = a; for (var i = 0; i < 10; i++) { var y = a; } });");
  }

  @Test
  public void testAliasInSwitchCase() {
    test("goog.scope(function() { var x = a; switch(x) { case 1: var y = x; break; } });",
         "goog.scope(function() { var x = a; switch(a) { case 1: var y = a; break; } });");
  }

  @Test
  public void testAliasWithIncrement() {
    // Aliasing with ++ operator: alias should not be replaced if the original is modified
    testSame("goog.scope(function() { var x = a; x++; var y = x; });");
  }

  @Test
  public void testAliasWithAssignment() {
    testSame("goog.scope(function() { var x = a; x = b; var y = x; });");
  }

  @Test
  public void testAliasInEval() {
    // Aliases in eval strings are not resolved
    testSame("goog.scope(function() { var x = a; eval('var y = x;'); });");
  }

  @Test
  public void testAliasWithThis() {
    // 'this' is not a valid alias target
    testSame("goog.scope(function() { var x = this; });");
  }

  @Test
  public void testAliasWithNew() {
    testSame("goog.scope(function() { var x = new Foo(); });");
  }

  // ===================== Null/Edge Inputs =====================

  @Test
  public void testNullRoot() {
    // Should handle gracefully without NPE
    Compiler compiler = new Compiler();
    ScopedAliases pass = new ScopedAliases(compiler, new CompilerOptions());
    try {
      pass.process(compiler, null);
      fail("Expected NullPointerException or RuntimeException");
    } catch (Exception e) {
      // Expected
    }
  }

  @Test
  public void testEmptySourceFile() {
    testSame("");
  }

  @Test
  public void testOnlyGoogScope() {
    testSame("goog.scope(function() { });");
  }

  // ===================== Fault Detection (Defects4J bug 110 specific) =====================

  @Test
  public void testAliasToExtern() {
    // Aliases to externs should be resolved
    test("goog.scope(function() { var alert = window.alert; alert('hi'); });",
         "goog.scope(function() { var alert = window.alert; window.alert('hi'); });");
  }

  @Test
  public void testAliasToExternProperty() {
    test("goog.scope(function() { var doc = document; var body = doc.body; });",
         "goog.scope(function() { var doc = document; var body = document.body; });");
  }

  @Test
  public void testAliasInMultipleBranches() {
    test("goog.scope(function() { var x = a; if (true) { var y = x; } else { var z = x; } });",
         "goog.scope(function() { var x = a; if (true) { var y = a; } else { var z = a; } });");
  }

  @Test
  public void testAliasUsedInObjectLiteral() {
    test("goog.scope(function() { var x = a; var obj = {key: x}; });",
         "goog.scope(function() { var x = a; var obj = {key: a}; });");
  }

  @Test
  public void testAliasUsedInArrayLiteral() {
    test("goog.scope(function() { var x = a; var arr = [x]; });",
         "goog.scope(function() { var x = a; var arr = [a]; });");
  }

  @Test
  public void testAliasUsedInPropertyAccess() {
    test("goog.scope(function() { var x = a; var y = x.prop; });",
         "goog.scope(function() { var x = a; var y = a.prop; });");
  }

  @Test
  public void testAliasUsedInCall() {
    test("goog.scope(function() { var x = a; x(); });",
         "goog.scope(function() { var x = a; a(); });");
  }

  @Test
  public void testAliasUsedInNew() {
    test("goog.scope(function() { var x = a; new x(); });",
         "goog.scope(function() { var x = a; new a(); });");
  }

  @Test
  public void testAliasUsedInTypeof() {
    test("goog.scope(function() { var x = a; typeof x; });",
         "goog.scope(function() { var x = a; typeof a; });");
  }

  @Test
  public void testAliasUsedInDelete() {
    test("goog.scope(function() { var x = a; delete x; });",
         "goog.scope(function() { var x = a; delete a; });");
  }

  // ===================== Helper Methods (if needed) =====================

  // Not required as CompilerTestCase provides test/testSame.
}