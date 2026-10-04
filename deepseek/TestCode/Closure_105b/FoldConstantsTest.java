package com.google.javascript.jscomp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.google.javascript.rhino.Node;
import java.util.Arrays;
import org.junit.Test;

public class FoldConstantsTest {

  private void testFold(String js, String expected) {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.init(
        Arrays.<SourceFile>asList(SourceFile.fromCode("externs", "")),
        Arrays.<SourceFile>asList(SourceFile.fromCode("test", js)),
        options);
    Node root = compiler.parseInputs();
    assertNotNull("JavaScript parse error for input: " + js, root);
    new FoldConstants(compiler).process(compiler.getExternsRoot(), root);

    Compiler expectedCompiler = new Compiler();
    expectedCompiler.init(
        Arrays.<SourceFile>asList(SourceFile.fromCode("externs", "")),
        Arrays.<SourceFile>asList(SourceFile.fromCode("expected", expected)),
        options);
    Node expectedRoot = expectedCompiler.parseInputs();
    assertNotNull("JavaScript parse error for expected: " + expected, expectedRoot);

    String actualSource = compiler.toSource();
    String expectedSource = expectedCompiler.toSource();
    assertEquals("Folding mismatch for JS: " + js, expectedSource, actualSource);
  }

  private void testSame(String js) {
    testFold(js, js);
  }

  @Test
  public void testFoldArithmetic() {
    testFold("var x = 1 + 2;", "var x = 3;");
    testFold("var x = 1 - 2;", "var x = -1;");
    testFold("var x = 3 * 4;", "var x = 12;");
    testFold("var x = 7 / 2;", "var x = 3.5;");
    testFold("var x = 10 % 4;", "var x = 2;");
  }

  @Test
  public void testFoldStringConcat() {
    testFold("var x = 'a' + 'b';", "var x = 'ab';");
    testFold("var x = 'a' + 1;", "var x = 'a1';");
    testFold("var x = 1 + 'a';", "var x = '1a';");
  }

  @Test
  public void testFoldBitwise() {
    testFold("var x = 5 & 3;", "var x = 1;");
    testFold("var x = 5 | 2;", "var x = 7;");
    testFold("var x = 5 ^ 2;", "var x = 7;");
    testFold("var x = 5 << 2;", "var x = 20;");
    testFold("var x = -5 >> 1;", "var x = -3;");
    testFold("var x = 5 >>> 1;", "var x = 2;");
  }

  @Test
  public void testFoldLogical() {
    testFold("var x = true && false;", "var x = false;");
    testFold("var x = true || false;", "var x = true;");
    testFold("var x = 0 || 1;", "var x = 1;");
    testFold("var x = 1 && 2;", "var x = 2;");
  }

  @Test
  public void testFoldUnary() {
    testFold("var x = !true;", "var x = false;");
    testFold("var x = !0;", "var x = true;");
    testFold("var x = !!1;", "var x = true;");
    testFold("var x = ~5;", "var x = -6;");
    testFold("var x = - -5;", "var x = 5;");
  }

  @Test
  public void testFoldComparison() {
    testFold("var x = 1 < 2;", "var x = true;");
    testFold("var x = 1 <= 1;", "var x = true;");
    testFold("var x = 1 > 2;", "var x = false;");
    testFold("var x = 1 == '1';", "var x = true;");
    testFold("var x = 1 === '1';", "var x = false;");
  }

  @Test
  public void testFoldTypeof() {
    testFold("var x = typeof 1;", "var x = 'number';");
    testFold("var x = typeof 'a';", "var x = 'string';");
    testFold("var x = typeof true;", "var x = 'boolean';");
    testFold("var x = typeof null;", "var x = 'object';");
    testFold("var x = typeof undeclaredVar;", "var x = 'undefined';");
    testFold("var x; var y = typeof x;", "var x; var y = 'undefined';");
    testFold(
        "function f() {} var x = typeof f;",
        "function f() {} var x = 'function';");
  }

  @Test
  public void testTypeofDoesNotFoldFunctionParameter() {
    testSame("function f(x) { return typeof x; }");
  }

  @Test
  public void testFoldHook() {
    testFold("var x = true ? 1 : 2;", "var x = 1;");
    testFold("var x = false ? 1 : 2;", "var x = 2;");
    testFold("var x = 1 ? 'a' : 'b';", "var x = 'a';");
  }

  @Test
  public void testFoldArrayObjectLiterals() {
    testFold("var x = [1 + 2];", "var x = [3];");
    testFold("var x = {a: 1 + 2};", "var x = {a: 3};");
    testFold("var x = {a: 1 + 2, b: 3};", "var x = {a: 3, b: 3};");
  }

  @Test
  public void testFoldEdgeCases() {
    testFold("var x = 1 / 0;", "var x = Infinity;");
    testFold("var x = 0 / 0;", "var x = NaN;");
    testSame("var x = -0;");
    testFold("var x = 0 * -0;", "var x = -0;");
  }

  @Test
  public void testDoesNotFoldNonConstants() {
    testSame("var x = 1 + y;");
    testSame("var x = y + 1;");
    testSame("var x = true ? foo() : 1;");
  }
}