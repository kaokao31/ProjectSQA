package com.google.javascript.jscomp.parsing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.rhino.Node;
import java.util.Collections;
import org.junit.Test;

public class IRFactoryTest {

  private Node parseAndTransform(String source) {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setLanguage(CompilerOptions.LanguageMode.ECMASCRIPT5);
    compiler.init(
        Collections.<SourceFile>emptyList(),
        Collections.singletonList(SourceFile.fromCode("test.js", source)),
        options);
    Node root = compiler.parseInputs();
    assertFalse("Errors in parsing for source: " + source, compiler.hasErrors());
    assertNotNull("Parsing returned null for source: " + source, root);
    return root;
  }

  @Test
  public void testEmptyScript() {
    Node root = parseAndTransform("");
    assertNotNull(root);
    assertEquals(0, root.getChildCount());
  }

  @Test
  public void testSimpleVar() {
    Node root = parseAndTransform("var x = 1;");
    assertNotNull(root);
    assertEquals(1, root.getChildCount());
  }

  @Test
  public void testFunctionDeclaration() {
    Node root = parseAndTransform("function f() { return 1; }");
    assertNotNull(root);
    assertEquals(1, root.getChildCount());
  }

  @Test
  public void testObjectLiteralGetterStringKey() {
    Node root = parseAndTransform("var o = {get b() { return 1; }};");
    assertNotNull(root);
    assertEquals(1, root.getChildCount());
  }

  @Test
  public void testObjectLiteralSetterStringKey() {
    Node root = parseAndTransform("var o = {set b(v) {}};");
    assertNotNull(root);
    assertEquals(1, root.getChildCount());
  }

  @Test
  public void testObjectLiteralGetterNumericKey() {
    Node root = parseAndTransform("var o = {get 1() { return 1; }};");
    assertNotNull(root);
    assertEquals(1, root.getChildCount());
  }

  @Test
  public void testObjectLiteralGetterSpaceInName() {
    Node root = parseAndTransform("var o = {get 'a b'() { return 1; }};");
    assertNotNull(root);
    assertEquals(1, root.getChildCount());
  }

  @Test
  public void testObjectLiteralGetterKeywordName() {
    Node root = parseAndTransform("var o = {get if() { return 1; }};");
    assertNotNull(root);
    assertEquals(1, root.getChildCount());
  }

  @Test
  public void testObjectLiteralGetterEscapedQuote() {
    Node root = parseAndTransform("var o = {get 'a\\'b'() { return 1; }};");
    assertNotNull(root);
    assertEquals(1, root.getChildCount());
  }

  @Test
  public void testObjectLiteralSetterNumericKey() {
    Node root = parseAndTransform("var o = {set 1(v) {}};");
    assertNotNull(root);
    assertEquals(1, root.getChildCount());
  }

  @Test
  public void testObjectLiteralMixedProperties() {
    Node root = parseAndTransform("var o = {a: 1, get b() { return 2; }, set c(v) {}};");
    assertNotNull(root);
    assertEquals(1, root.getChildCount());
  }

  @Test
  public void testObjectLiteralWithComputedProperty() {
    Node root = parseAndTransform("var o = {['a' + 'b']: 1};");
    assertNotNull(root);
    assertEquals(1, root.getChildCount());
  }

  @Test
  public void testClassDeclaration() {
    Node root = parseAndTransform("class A { get x() { return 1; } }");
    assertNotNull(root);
    assertEquals(1, root.getChildCount());
  }

  @Test
  public void testArrowFunction() {
    Node root = parseAndTransform("var f = () => {};");
    assertNotNull(root);
    assertEquals(1, root.getChildCount());
  }

  @Test
  public void testTryCatch() {
    Node root = parseAndTransform("try { x(); } catch (e) { y(); } finally { z(); }");
    assertNotNull(root);
    assertEquals(1, root.getChildCount());
  }

  @Test
  public void testSwitch() {
    Node root = parseAndTransform("switch(x) { case 1: break; default: break; }");
    assertNotNull(root);
    assertEquals(1, root.getChildCount());
  }

  @Test
  public void testForLoop() {
    Node root = parseAndTransform("for(var i=0;i<10;i++) { x(); }");
    assertNotNull(root);
    assertEquals(1, root.getChildCount());
  }

  @Test
  public void testWhileLoop() {
    Node root = parseAndTransform("while(true) { break; }");
    assertNotNull(root);
    assertEquals(1, root.getChildCount());
  }

  @Test
  public void testDoWhile() {
    Node root = parseAndTransform("do { x(); } while (false);");
    assertNotNull(root);
    assertEquals(1, root.getChildCount());
  }

  @Test
  public void testRegex() {
    Node root = parseAndTransform("var r = /ab+c/gi;");
    assertNotNull(root);
    assertEquals(1, root.getChildCount());
  }

  @Test
  public void testStringEscape() {
    Node root = parseAndTransform("var s = 'a\\'b';");
    assertNotNull(root);
    assertEquals(1, root.getChildCount());
  }

  @Test
  public void testNullLiteral() {
    Node root = parseAndTransform("var n = null;");
    assertNotNull(root);
    assertEquals(1, root.getChildCount());
  }

  @Test
  public void testUndefined() {
    Node root = parseAndTransform("var u;");
    assertNotNull(root);
    assertEquals(1, root.getChildCount());
  }

  @Test
  public void testBooleanLiteral() {
    Node root = parseAndTransform("var b = true;");
    assertNotNull(root);
    assertEquals(1, root.getChildCount());
  }

  @Test
  public void testNumberNegative() {
    Node root = parseAndTransform("var n = -5;");
    assertNotNull(root);
    assertEquals(1, root.getChildCount());
  }

  @Test
  public void testArrayLiteral() {
    Node root = parseAndTransform("var a = [1, 2, 3];");
    assertNotNull(root);
    assertEquals(1, root.getChildCount());
  }

  @Test
  public void testObjectLiteralEmpty() {
    Node root = parseAndTransform("var o = {};");
    assertNotNull(root);
    assertEquals(1, root.getChildCount());
  }

  @Test
  public void testObjectLiteralWithNestedObject() {
    Node root = parseAndTransform("var o = {a: {b: 2}};");
    assertNotNull(root);
    assertEquals(1, root.getChildCount());
  }

  @Test
  public void testFunctionWithRestParam() {
    Node root = parseAndTransform("function f(...args) {}");
    assertNotNull(root);
    assertEquals(1, root.getChildCount());
  }

  @Test
  public void testFunctionWithDefaultParam() {
    Node root = parseAndTransform("function f(a = 1) {}");
    assertNotNull(root);
    assertEquals(1, root.getChildCount());
  }

  @Test
  public void testTemplateLiteral() {
    Node root = parseAndTransform("var s = `hello ${name}`;");
    assertNotNull(root);
    assertEquals(1, root.getChildCount());
  }

  @Test
  public void testSpreadOperator() {
    Node root = parseAndTransform("var a = [...b];");
    assertNotNull(root);
    assertEquals(1, root.getChildCount());
  }

  @Test
  public void testImportDeclaration() {
    Node root = parseAndTransform("import x from 'y';");
    assertNotNull(root);
    assertEquals(1, root.getChildCount());
  }

  @Test
  public void testExportDeclaration() {
    Node root = parseAndTransform("export var x = 1;");
    assertNotNull(root);
    assertEquals(1, root.getChildCount());
  }

  @Test
  public void testParseError() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setLanguage(CompilerOptions.LanguageMode.ECMASCRIPT5);
    compiler.init(
        Collections.<SourceFile>emptyList(),
        Collections.singletonList(SourceFile.fromCode("test.js", "var x = ;")),
        options);
    Node root = compiler.parseInputs();
    assertTrue(compiler.hasErrors());
  }
}