package com.google.javascript.jscomp.parsing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.javascript.jscomp.parsing.JsDocInfoParser;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.JSTypeExpression;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for JsDocInfoParser, targeting maximum coverage and fault detection.
 * Includes edge cases for nullable/optional type expressions (Closure bug 133).
 */
public class JsDocInfoParserTest {

  private JsDocInfoParser parser;

  @Before
  public void setUp() {
    // Initialize parser with a dummy source name and line number
    parser = new JsDocInfoParser(
        new JsDocTokenStream("", 0, 0),
        "test.js",
        0,
        null,
        null);
  }

  // Helper to parse a JSDoc comment string and return JSDocInfo
  private JSDocInfo parseComment(String comment) {
    parser = new JsDocInfoParser(
        new JsDocTokenStream(comment, 0, 0),
        "test.js",
        0,
        null,
        null);
    return parser.parse();
  }

  // ==================== Basic Parsing ====================

  @Test
  public void testEmptyComment() {
    JSDocInfo info = parseComment("/** */");
    assertNotNull(info);
    assertTrue(info.getParameterCount() == 0);
  }

  @Test
  public void testSimpleParam() {
    JSDocInfo info = parseComment("/** @param {number} x */");
    assertNotNull(info);
    assertTrue(info.hasParameter("x"));
    JSTypeExpression type = info.getParameterType("x");
    assertNotNull(type);
  }

  @Test
  public void testMultipleParams() {
    JSDocInfo info = parseComment("/** @param {string} a @param {boolean} b */");
    assertNotNull(info);
    assertTrue(info.hasParameter("a"));
    assertTrue(info.hasParameter("b"));
  }

  @Test
  public void testReturnType() {
    JSDocInfo info = parseComment("/** @return {Array.<string>} */");
    assertNotNull(info);
    assertTrue(info.hasReturnType());
  }

  @Test
  public void testTypeAnnotation() {
    JSDocInfo info = parseComment("/** @type {number} */");
    assertNotNull(info);
    assertTrue(info.hasType());
  }

  // ==================== Edge Cases ====================

  @Test
  public void testParamWithoutType() {
    JSDocInfo info = parseComment("/** @param x */");
    assertNotNull(info);
    assertTrue(info.hasParameter("x"));
    assertNull(info.getParameterType("x"));
  }

  @Test
  public void testParamWithOptionalType() {
    // Optional parameter with '=' marker
    JSDocInfo info = parseComment("/** @param {number=} x */");
    assertNotNull(info);
    assertTrue(info.hasParameter("x"));
    JSTypeExpression type = info.getParameterType("x");
    assertNotNull(type);
    // The type should be nullable/optional; we just check it parses
  }

  @Test
  public void testParamWithNullableType() {
    // Nullable type with '?' marker
    JSDocInfo info = parseComment("/** @param {?number} x */");
    assertNotNull(info);
    assertTrue(info.hasParameter("x"));
    JSTypeExpression type = info.getParameterType("x");
    assertNotNull(type);
  }

  @Test
  public void testParamWithNullableOptionalType() {
    // Combined nullable and optional (bug 133 scenario)
    JSDocInfo info = parseComment("/** @param {?number=} x */");
    assertNotNull(info);
    assertTrue(info.hasParameter("x"));
    JSTypeExpression type = info.getParameterType("x");
    assertNotNull(type);
  }

  @Test
  public void testReturnTypeNullable() {
    JSDocInfo info = parseComment("/** @return {?string} */");
    assertNotNull(info);
    assertTrue(info.hasReturnType());
  }

  @Test
  public void testReturnTypeOptional() {
    JSDocInfo info = parseComment("/** @return {string=} */");
    assertNotNull(info);
    assertTrue(info.hasReturnType());
  }

  @Test
  public void testTypeAnnotationNullable() {
    JSDocInfo info = parseComment("/** @type {?boolean} */");
    assertNotNull(info);
    assertTrue(info.hasType());
  }

  @Test
  public void testTypeAnnotationOptional() {
    JSDocInfo info = parseComment("/** @type {number=} */");
    assertNotNull(info);
    assertTrue(info.hasType());
  }

  // ==================== Complex Types ====================

  @Test
  public void testRecordType() {
    JSDocInfo info = parseComment("/** @param {{x: number, y: string}} obj */");
    assertNotNull(info);
    assertTrue(info.hasParameter("obj"));
  }

  @Test
  public void testUnionType() {
    JSDocInfo info = parseComment("/** @param {(number|string)} val */");
    assertNotNull(info);
    assertTrue(info.hasParameter("val"));
  }

  @Test
  public void testArrayType() {
    JSDocInfo info = parseComment("/** @param {Array.<number>} arr */");
    assertNotNull(info);
    assertTrue(info.hasParameter("arr"));
  }

  @Test
  public void testFunctionType() {
    JSDocInfo info = parseComment("/** @param {function(string): number} fn */");
    assertNotNull(info);
    assertTrue(info.hasParameter("fn"));
  }

  // ==================== Tags Without Types ====================

  @Test
  public void testDeprecatedTag() {
    JSDocInfo info = parseComment("/** @deprecated */");
    assertNotNull(info);
    assertTrue(info.isDeprecated());
  }

  @Test
  public void testSeeTag() {
    JSDocInfo info = parseComment("/** @see MyClass */");
    assertNotNull(info);
    // No direct assertion, just ensure no exception
  }

  @Test
  public void testThrowsTag() {
    JSDocInfo info = parseComment("/** @throws {Error} if something fails */");
    assertNotNull(info);
    assertTrue(info.getThrownTypes().size() > 0);
  }

  // ==================== Malformed Input ====================

  @Test(expected = Exception.class)
  public void testUnclosedComment() {
    parseComment("/* @param {number} x");
  }

  @Test
  public void testInvalidTypeSyntax() {
    // Should not crash, may return null or partial info
    JSDocInfo info = parseComment("/** @param {invalid} x */");
    assertNotNull(info);
    // Depending on implementation, type may be null
  }

  @Test
  public void testEmptyTypeBraces() {
    JSDocInfo info = parseComment("/** @param {} x */");
    assertNotNull(info);
    assertTrue(info.hasParameter("x"));
    // Type may be null or empty
  }

  // ==================== Multiple Tags ====================

  @Test
  public void testMultipleTags() {
    JSDocInfo info = parseComment("/** @param {number} x @return {string} @type {boolean} */");
    assertNotNull(info);
    assertTrue(info.hasParameter("x"));
    assertTrue(info.hasReturnType());
    assertTrue(info.hasType());
  }

  // ==================== Line Number and Source ====================

  @Test
  public void testLineNumberTracking() {
    // Use multi-line comment
    JSDocInfo info = parseComment("/**\n * @param {number} x\n */");
    assertNotNull(info);
    // Ensure no exception; line numbers are internal
  }

  // ==================== Bug-Specific Tests (Closure 133) ====================

  @Test
  public void testNullableOptionalParamBug() {
    // This test targets the specific bug where nullable and optional markers
    // in type expressions caused incorrect parsing.
    JSDocInfo info = parseComment("/** @param {?number=} x */");
    assertNotNull(info);
    assertTrue(info.hasParameter("x"));
    JSTypeExpression type = info.getParameterType("x");
    assertNotNull("Type expression should not be null for nullable optional param", type);
    // The root node should be a union or nullable type; we just verify it parses
  }

  @Test
  public void testNullableOptionalReturnBug() {
    JSDocInfo info = parseComment("/** @return {?string=} */");
    assertNotNull(info);
    assertTrue(info.hasReturnType());
    JSTypeExpression type = info.getReturnType();
    assertNotNull("Return type should not be null for nullable optional return", type);
  }

  @Test
  public void testNullableOptionalTypeAnnotationBug() {
    JSDocInfo info = parseComment("/** @type {?boolean=} */");
    assertNotNull(info);
    assertTrue(info.hasType());
    JSTypeExpression type = info.getType();
    assertNotNull("Type annotation should not be null for nullable optional type", type);
  }
}