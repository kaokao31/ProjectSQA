package com.google.javascript.jscomp;

import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.Result;
import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.jscomp.TypedScope;
import com.google.javascript.jscomp.TypedVar;
import com.google.javascript.rhino.jstype.JSType;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for TypeInference pass in the Closure Compiler.
 * These tests exercise basic type inference and target edge cases
 * that may reveal regressions in the inference logic.
 */
public class TypeInferenceTest {

  private Compiler compiler;
  private CompilerOptions options;

  @Before
  public void setUp() {
    compiler = new Compiler();
    options = new CompilerOptions();
    // Enable type inference and type checking
    options.setInferTypes(true);
    options.setCheckTypes(true);
  }

  /** Compiles the given JavaScript and returns the inferred type of a variable. */
  private JSType getVariableType(String js, String varName) {
    Result result = compiler.compile(
        SourceFile.fromCode("externs.js", ""),
        SourceFile.fromCode("input.js", js),
        options);
    assertTrue("Compilation failed: " + result.errors, result.success);

    TypedScope topScope = compiler.getTopScope();
    TypedVar var = topScope.getVar(varName);
    if (var == null) {
      return null;
    }
    return var.getType();
  }

  @Test
  public void testNumberInference() {
    JSType type = getVariableType("var x = 1;", "x");
    assertNotNull("Variable type should not be null", type);
    assertTrue("Expected number type", type.isNumber());
  }

  @Test
  public void testStringInference() {
    JSType type = getVariableType("var s = 'hello';", "s");
    assertNotNull(type);
    assertTrue("Expected string type", type.isString());
  }

  @Test
  public void testBooleanInference() {
    JSType type = getVariableType("var b = true;", "b");
    assertNotNull(type);
    assertTrue("Expected boolean type", type.isBoolean());
  }

  @Test
  public void testFunctionInference() {
    JSType type = getVariableType("function f() {}", "f");
    assertNotNull(type);
    assertTrue("Expected function type", type.isFunctionType());
  }

  @Test
  public void testNullInference() {
    JSType type = getVariableType("var n = null;", "n");
    assertNotNull(type);
    assertTrue("Expected null type", type.isNullType());
  }

  @Test
  public void testUndefinedInference() {
    JSType type = getVariableType("var u;", "u");
    assertNotNull(type);
    assertTrue("Expected void (undefined) type", type.isVoidType());
  }

  @Test
  public void testObjectInference() {
    JSType type = getVariableType("var o = {};", "o");
    assertNotNull(type);
    assertFalse("Object type should not be unknown", type.isUnknownType());
    assertTrue("Expected object type", type.isObject());
  }

  @Test
  public void testArrayInference() {
    JSType type = getVariableType("var a = [1, 2];", "a");
    assertNotNull(type);
    assertTrue("Expected array type", type.isArrayType());
  }

  @Test
  public void testUnionTypeInference() {
    JSType type = getVariableType("var u = Math.random() > 0.5 ? 1 : 'x';", "u");
    assertNotNull(type);
    assertTrue("Expected union type", type.isUnionType());
  }

  /**
   * Edge case: for-in loop iteration variable should be inferred as string.
   * This test targets potential regressions in type inference for for-in loops.
   */
  @Test
  public void testForInVariableInference() {
    JSType type = getVariableType(
        "var obj = {a: 1, b: 2}; for (var k in obj) {}", "k");
    assertNotNull(type);
    assertTrue("For-in variable should be a string", type.isString());
  }

  /**
   * Edge case: typeof operator on a variable can refine its type.
   * This test checks that typeof narrowing works as expected.
   */
  @Test
  public void testTypeofNarrowing() {
    JSType type = getVariableType(
        "var x = Math.random() > 0.5 ? 1 : null; if (typeof x === 'number') { var y = x; }", "y");
    assertNotNull(type);
    assertTrue("Variable y should be number after typeof guard", type.isNumber());
  }

  /**
   * Edge case: type inference should handle missing property access gracefully.
   * The variable 'p' should have an unknown or error type, not a crash.
   */
  @Test
  public void testMissingPropertyAccess() {
    JSType type = getVariableType("var q = {}; var p = q.missing;", "p");
    assertNotNull(type);
    // We don't check the exact type, just that inference does not fail.
    assertFalse(type.isFunctionType());
  }
}