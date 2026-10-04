package com.google.javascript.jscomp.parsing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.javascript.jscomp.ErrorReporter;
import com.google.javascript.jscomp.parsing.ParserRunner;
import com.google.javascript.rhino.JSDocInfo;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class JsDocInfoParserTest {

  private static final ParserRunner.Config CONFIG =
      new ParserRunner.Config(
          ParserRunner.Config.LanguageMode.ECMASCRIPT3,
          true,
          true);

  @Test
  public void testParseSimpleType() {
    JSDocInfo info = parse("@type {string}");
    assertTrue(info.hasType());
    assertTypeContains(info, "string");
  }

  @Test
  public void testParseQualifiedTypeName() {
    JSDocInfo info = parse("@type {goog.ui.Control}");
    assertTrue(info.hasType());
    assertTypeContains(info, "goog.ui.Control");
  }

  @Test
  public void testParseNonNullableType() {
    JSDocInfo info = parse("@type {!Object}");
    assertTrue(info.hasType());
    assertTypeContains(info, "Object");
  }

  @Test
  public void testParseNullableType() {
    JSDocInfo info = parse("@type {?Object}");
    assertTrue(info.hasType());
    assertTypeContains(info, "Object");
  }

  @Test
  public void testParseUnionType() {
    JSDocInfo info = parse("@type {(string|number)}");
    assertTrue(info.hasType());
    assertTypeContains(info, "string");
    assertTypeContains(info, "number");
  }

  @Test
  public void testParseGenericType() {
    JSDocInfo info = parse("@type {Array.<string>}");
    assertTrue(info.hasType());
    assertTypeContains(info, "Array");
    assertTypeContains(info, "string");
  }

  @Test
  public void testParseNestedGenericType() {
    JSDocInfo info = parse("@type {Array.<Array.<number>>}");
    assertTrue(info.hasType());
    assertTypeContains(info, "Array");
    assertTypeContains(info, "number");
  }

  @Test
  public void testParseRecordType() {
    JSDocInfo info = parse("@type {{foo: string, bar: number}}");
    assertTrue(info.hasType());
    assertTypeContains(info, "foo");
    assertTypeContains(info, "bar");
  }

  @Test
  public void testParseFunctionType() {
    JSDocInfo info = parse("@type {function(string): number}");
    assertTrue(info.hasType());
    assertTypeContains(info, "function");
    assertTypeContains(info, "string");
    assertTypeContains(info, "number");
  }

  @Test
  public void testParseFunctionTypeWithNew() {
    JSDocInfo info = parse("@type {function(new:Array)}");
    assertTrue(info.hasType());
    assertTypeContains(info, "Array");
  }

  @Test
  public void testParseFunctionTypeWithThis() {
    JSDocInfo info = parse("@type {function(this:Window)}");
    assertTrue(info.hasType());
    assertTypeContains(info, "Window");
  }

  @Test
  public void testParseFunctionTypeWithOptionalParameter() {
    JSDocInfo info = parse("@type {function(?string=)}");
    assertTrue(info.hasType());
    assertTypeContains(info, "string");
  }

  @Test
  public void testParseConstructorTag() {
    JSDocInfo info = parse("@constructor");
    assertTrue(info.isConstructor());
  }

  @Test
  public void testParseExtendsTag() {
    JSDocInfo info = parse("@extends {Base}");
    assertTrue(info.hasBaseType());
    assertTypeContains(info, "Base");
  }

  @Test
  public void testParseEnumTag() {
    JSDocInfo info = parse("@enum {string}");
    assertTrue(info.hasEnumParameterType());
    assertTypeContains(info, "string");
  }

  @Test
  public void testParseSuppressTag() {
    JSDocInfo info = parse("@suppress {undefinedNames}");
    assertTrue(info.getSuppressions().contains("undefinedNames"));
  }

  @Test
  public void testParseParamTag() {
    JSDocInfo info = parse("@param {string} name");
    assertTrue(info.hasParameterType("name"));
    assertTypeContains(info, "string");
  }

  @Test
  public void testParseReturnTag() {
    JSDocInfo info = parse("@return {number}");
    assertTrue(info.hasReturnType());
    assertTypeContains(info, "number");
  }

  @Test
  public void testParseComplexFunctionType() {
    JSDocInfo info = parse("@type {function(new:goog.ui.Control, Element): void}");
    assertTrue(info.hasType());
    assertTypeContains(info, "goog.ui.Control");
    assertTypeContains(info, "Element");
  }

  private JSDocInfo parse(String comment) {
    TestErrorReporter reporter = new TestErrorReporter();
    JsDocInfoParser parser = new JsDocInfoParser(
        new JsDocTokenStream(comment),
        comment,
        0,
        null,
        CONFIG,
        reporter);
    JSDocInfo info = parser.parse();
    assertNotNull(info);
    assertEquals("Parser errors: " + reporter.errors, 0, reporter.errors.size());
    return info;
  }

  private void assertTypeContains(JSDocInfo info, String expected) {
    String typeTree = info.getType().getRoot().toStringTree();
    assertTrue(
        "Type expression did not contain '" + expected + "': " + typeTree,
        typeTree.contains(expected));
  }

  private static class TestErrorReporter implements ErrorReporter {
    final List<String> errors = new ArrayList<String>();
    final List<String> warnings = new ArrayList<String>();

    @Override
    public void error(String message, String sourceName, int line, int lineOffset) {
      errors.add(message);
    }

    @Override
    public void warning(String message, String sourceName, int line, int lineOffset) {
      warnings.add(message);
    }
  }
}