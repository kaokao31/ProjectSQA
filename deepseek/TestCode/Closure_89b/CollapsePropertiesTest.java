package com.google.javascript.jscomp;

import org.junit.Before;
import org.junit.Test;

public class CollapsePropertiesTest extends CompilerTestCase {

  private boolean collapsePropertiesOnExternTypes;

  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    return new CollapseProperties(compiler, collapsePropertiesOnExternTypes);
  }

  @Before
  public void setUp() throws Exception {
    super.setUp();
    collapsePropertiesOnExternTypes = true;
  }

  @Test
  public void testCollapseSimpleProperty() {
    test("var a = {}; a.b = 1;", "var a$b = 1;");
  }

  @Test
  public void testCollapseObjectLiteral() {
    test("var a = {b: 1};", "var a$b = 1;");
  }

  @Test
  public void testCollapseMultipleProperties() {
    test("var a = {}; a.b = 1; a.c = 2;",
         "var a$b = 1; var a$c = 2;");
  }

  @Test
  public void testCollapseNestedProperties() {
    test("var a = {}; a.b = {}; a.b.c = 1;",
         "var a$b$c = 1;");
  }

  @Test
  public void testCollapsePropertyRead() {
    test("var a = {}; a.b = 1; var c = a.b;",
         "var a$b = 1; var c = a$b;");
  }

  @Test
  public void testCollapseFunctionProperty() {
    test("var a = {}; a.b = function() {}; var c = a.b;",
         "var a$b = function() {}; var c = a$b;");
  }

  @Test
  public void testNoCollapseWhenRootIsAliased() {
    testSame("var a = {}; a.b = 1; var c = a; var d = c.b;");
  }

  @Test
  public void testNoCollapseWhenRootEscapes() {
    testSame("var a = {b: 1}; var c = a; var d = c.b;");
  }

  @Test
  public void testNoCollapseWithComputedProperty() {
    testSame("var a = {}; a.b = 1; var c = 'b'; var d = a[c];");
  }

  @Test
  public void testNoCollapseWithPrototypeProperty() {
    testSame("function Foo() {} Foo.prototype.bar = 1; var x = Foo.prototype.bar;");
  }

  @Test
  public void testNoCollapseWithDelete() {
    testSame("var a = {}; a.b = 1; delete a.b;");
  }

  @Test
  public void testNoCollapseWithNonIdentifierPropertyName() {
    testSame("var a = {}; a['foo-bar'] = 1; var c = a['foo-bar'];");
  }

  @Test
  public void testNoCollapseWhenGeneratedNameConflictsWithVariable() {
    testSame("var a = {}; a.b = 1; var a$b = 2; var c = a.b;");
  }

  @Test
  public void testNoCollapseWhenGeneratedNameConflictsWithFunction() {
    testSame("var a = {}; a.b = 1; function a$b() {} var c = a.b;");
  }

  @Test
  public void testIssue89() {
    testSame("var a = {}; a.b = 1; function f() { var a$b = 2; return a.b; }");
  }

  @Test
  public void testNoCollapseWhenGeneratedNameConflictsWithParameter() {
    testSame("var a = {}; a.b = 1; function f(a$b) { return a.b; }");
  }

  @Test
  public void testNoCollapseWhenGeneratedNameConflictsWithCatchParam() {
    testSame("var a = {}; a.b = 1; try { throw 0; } catch (a$b) { var c = a.b; }");
  }
}