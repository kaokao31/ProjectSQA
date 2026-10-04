package com.google.javascript.jscomp;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class NameAnalyzerTest extends CompilerTestCase {

  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    return new NameAnalyzer(compiler, true);
  }

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
  }

  @Override
  @After
  public void tearDown() throws Exception {
    super.tearDown();
  }

  @Test
  public void testNoCode() {
    testSame("");
  }

  @Test
  public void testPreservedFunctionIsKept() {
    testSame("/** @preserve */ function f() {}");
  }

  @Test
  public void testUnreferencedFunctionRemoved() {
    test("function f() {}", "");
  }

  @Test
  public void testReferencedFunctionKept() {
    testSame("function f() {} f();");
  }

  @Test
  public void testUnreferencedVarRemoved() {
    test("var x = 1;", "");
  }

  @Test
  public void testReferencedVarKept() {
    testSame("var x = 1; alert(x);");
  }

  @Test
  public void testUnreferencedFunctionExpressionRemoved() {
    test("var f = function() {};", "");
  }

  @Test
  public void testReferencedFunctionExpressionKept() {
    testSame("var f = function() {}; f();");
  }

  @Test
  public void testFunctionReferenceFromAnotherFunctionKeepsCallGraph() {
    testSame("function a() {} function b() { a(); } b();");
  }

  @Test
  public void testVarUsedInsideFunctionKeepsVariable() {
    testSame("var x = 1; function f() { alert(x); } f();");
  }

  @Test
  public void testPropertyReferenceKeepsObject() {
    testSame("var a = {}; a.b = 1; alert(a.b);");
  }

  @Test
  public void testObjectLiteralPropertyReferenceKeepsObject() {
    testSame("var a = {b: 1}; alert(a.b);");
  }

  @Test
  public void testExportedFunctionKept() {
    testSame("function f() {} window[\"f\"] = f;");
  }

  @Test
  public void testMultipleDeclarationsWithUse() {
    testSame("var x = 1, y = x; alert(y);");
  }
}