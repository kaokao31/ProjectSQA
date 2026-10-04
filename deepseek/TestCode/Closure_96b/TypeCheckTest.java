package com.google.javascript.jscomp;

import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.CheckLevel;
import com.google.javascript.jscomp.Result;
import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.jscomp.SourceMap;
import com.google.javascript.jscomp.TypeCheck;
import com.google.javascript.jscomp.VariableRenamingPolicy;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * JUnit 4 test suite for TypeCheck, targeting maximum coverage and fault detection.
 * Designed for Closure Compiler bug #96 (generic type extends).
 */
public class TypeCheckTest {

  private Compiler compiler;
  private CompilerOptions options;
  private Result result;

  @Before
  public void setUp() {
    compiler = new Compiler();
    options = new CompilerOptions();
    // Enable type checking
    options.setCheckSymbols(true);
    options.setCheckTypes(true);
    options.setIdeMode(false);
    options.setClosurePass(false);
    options.setLanguageIn(CompilerOptions.LanguageMode.ECMASCRIPT5);
    options.setLanguageOut(CompilerOptions.LanguageMode.ECMASCRIPT5);
    options.setVariableRenaming(VariableRenamingPolicy.OFF);
    options.setPropertyRenaming(CompilerOptions.PropertyRenamingPolicy.OFF);
    options.setSourceMapOutputPath(null);
  }

  @After
  public void tearDown() {
    compiler = null;
    options = null;
    result = null;
  }

  private List<JSError> getErrors() {
    return result.errors;
  }

  private List<JSError> getWarnings() {
    return result.warnings;
  }

  private void compile(String code) {
    SourceFile input = SourceFile.fromCode("test.js", code);
    result = compiler.compile(
        new SourceFile[] { SourceFile.fromCode("externs.js", "") },
        new SourceFile[] { input },
        options);
  }

  // ---------- Basic coverage tests ----------

  @Test
  public void testNoCode() {
    compile("");
    assertTrue("No errors expected for empty code", getErrors().isEmpty());
    assertTrue("No warnings expected for empty code", getWarnings().isEmpty());
  }

  @Test
  public void testBasicTypeCheck() {
    compile("var x = 1;");
    assertTrue("No errors expected", getErrors().isEmpty());
    assertTrue("No warnings expected", getWarnings().isEmpty());
  }

  @Test
  public void testFunctionTypeCheck() {
    compile("/** @param {number} n */ function f(n) { return n + 1; }");
    assertTrue("No errors expected for valid function", getErrors().isEmpty());
    assertTrue("No warnings expected for valid function", getWarnings().isEmpty());
  }

  @Test
  public void testTypeMismatch() {
    compile("/** @param {number} n */ function f(n) { return 'hello'; }");
    // Expect a type mismatch warning (return type mismatch)
    assertTrue("Expected warnings for return type mismatch", getWarnings().size() > 0);
  }

  @Test
  public void testNullInput() {
    // TypeCheck should handle null node gracefully in unit tests – this is a boundary case.
    // In actual usage, null is avoided; but we test defensive behavior.
    // We'll compile a minimal code and then manually call TypeCheck with null (if possible).
    // However, TypeCheck is invoked by the compiler pass, so we rely on the compiler chain.
    compile("var a = null;");
    assertTrue("No errors expected for null literals", getErrors().isEmpty());
    // Ensure no crashes from null in type environment
  }

  // ---------- Fault detection: Bug #96 (generic extends) ----------

  @Test
  public void testGenericExtendsConstructor() {
    // This snippet triggered a false positive type mismatch in Closure #96.
    compile(
        "/** @constructor @extends {Array.<number>} */ function MyArray() {}\n" +
        "var a = new MyArray();\n" +
        "a[0] = 42;\n");
    // Should not report any errors or warnings
    assertTrue("No errors expected for generic extends constructor",
               getErrors().isEmpty());
    assertTrue("No warnings expected for generic extends constructor",
               getWarnings().isEmpty());
  }

  @Test
  public void testInheritedGeneric() {
    compile(
        "/** @constructor */ function Base() {}\n" +
        "/** @param {number} n */ Base.prototype.foo = function(n) {};\n" +
        "/** @constructor @extends {Base} */ function Derived() {}\n" +
        "var d = new Derived();\n" +
        "d.foo(10);\n");
    assertTrue("No errors expected for generic inheritance", getErrors().isEmpty());
    assertTrue("No warnings expected for generic inheritance", getWarnings().isEmpty());
  }

  // ---------- Edge cases and boundary conditions ----------

  @Test
  public void testLargeNumberLiteral() {
    compile("var x = 1e308;");
    assertTrue("No errors expected for large number", getErrors().isEmpty());
  }

  @Test
  public void testNestedFunctions() {
    compile(
        "/** @return {function(): number} */ function outer() {\n" +
        "  return function() { return 42; };\n" +
        "}");
    assertTrue("No errors expected for nested functions", getErrors().isEmpty());
  }

  @Test
  public void testUnknownType() {
    compile("/** @param {?} x */ function f(x) {}");
    assertTrue("No errors expected for unknown type", getErrors().isEmpty());
  }

  @Test
  public void testVoidType() {
    compile("/** @return {void} */ function f() { return; }");
    assertTrue("No errors expected for void return", getErrors().isEmpty());
  }

  @Test
  public void testUnionType() {
    compile("/** @param {number|string} x */ function f(x) { return x; }");
    assertTrue("No errors expected for union type", getErrors().isEmpty());
  }

  @Test
  public void testRecordType() {
    compile("/** @param {{x: number}} obj */ function f(obj) { return obj.x; }");
    assertTrue("No errors expected for record type", getErrors().isEmpty());
  }

  @Test
  public void testConstructorWithoutNew() {
    compile(
        "/** @constructor */ function Foo() {}\n" +
        "var f = Foo();");  // Missing 'new' – should warn
    assertTrue("Expected warning for constructor called without 'new'",
               getWarnings().size() > 0);
  }

  @Test
  public void testDuplicateVariable() {
    compile("var x = 1; var x = 2;");
    // Should produce an error (duplicate variable)
    assertTrue("Expected error for duplicate variable declaration",
               getErrors().size() > 0);
  }
}