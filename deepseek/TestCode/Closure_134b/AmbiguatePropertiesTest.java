package com.google.javascript.jscomp;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit 4 test suite for AmbiguateProperties.
 * Designed to achieve high line and branch coverage and detect potential faults.
 */
public class AmbiguatePropertiesTest {

  private Compiler compiler;
  private AmbiguateProperties pass;
  private CompilerOptions options;

  @Before
  public void setUp() {
    compiler = new Compiler();
    options = new CompilerOptions();
    // Enable property ambiguity
    options.setAmbiguateProperties(true);
    // Use a default coding convention (typically GoogleCodingConvention)
    options.setCodingConvention(new GoogleCodingConvention());
    pass = new AmbiguateProperties(compiler, options);
  }

  /**
   * Test on an empty source: no properties to rename.
   */
  @Test
  public void testEmptySource() {
    String source = "";
    Node ast = parseAndRun(source);
    assertNotNull("AST should not be null", ast);
    // No properties, so pass should not modify anything
    assertEquals("Empty script should have 0 children", 0, ast.getChildCount());
  }

  /**
   * Test renaming of a simple property access.
   */
  @Test
  public void testSimplePropertyRename() {
    String source = "var obj = {a: 1}; obj.a;";
    Node ast = parseAndRun(source);
    // After renaming, 'a' should be renamed to something else (e.g., 'a' -> 'b' or use mapping)
    // We cannot know the exact new name, but we can assert that the property name has changed
    Node propNode = findPropertyAccess(ast, "a");
    assertNull("Property 'a' should have been renamed", propNode);
    // Alternatively, check that it's replaced by a new STRING node with different value
    assertTrue("Property should have been renamed to a shorter name",
               isPropertyRenamed(ast, "a"));
  }

  /**
   * Test that extern properties are not renamed.
   */
  @Test
  public void testExternPropertiesNotRenamed() {
    // Use an extern like "window" that is not renamed
    String externs = "var window;";
    String source = "window.document;";
    Node ast = parseWithExterns(externs, source);
    // Property 'document' on extern 'window' should keep its name
    Node propNode = findPropertyAccess(ast, "document");
    assertNotNull("Extern property 'document' should not be renamed", propNode);
    assertEquals("document", propNode.getString());
  }

  /**
   * Test that reserved property names (like 'length', 'prototype', etc.) are not renamed.
   */
  @Test
  public void testReservedPropertiesNotRenamed() {
    String source = "var arr = [1,2]; arr.length;";
    Node ast = parseAndRun(source);
    Node propNode = findPropertyAccess(ast, "length");
    assertNotNull("Reserved property 'length' should not be renamed", propNode);
    assertEquals("length", propNode.getString());
  }

  /**
   * Test properties with numeric names (e.g., obj[0]).
   */
  @Test
  public void testNumericProperties() {
    String source = "var obj = {}; obj[0] = 1;";
    Node ast = parseAndRun(source);
    // Numeric properties are not renamed (they are not string property names)
    // Assert that the property access remains a NUMBER node
    Node propNode = findPropertyAccess(ast, "0");
    // Since it's a number, it won't be in a GETPROP node but in GETELEM
    Node elemNode = findElementAccess(ast, 0);
    assertNotNull("Numeric property access should remain", elemNode);
    assertTrue(elemNode.getFirstChild().isNumber());
  }

  /**
   * Test quoted properties (e.g., obj['a']).
   */
  @Test
  public void testQuotedPropertiesAreRenamed() {
    String source = "var obj = {}; obj['a'] = 1;";
    Node ast = parseAndRun(source);
    // Quoted string property 'a' should be renamed
    Node propNode = findElementAccess(ast, "'a'");
    // After renaming, the string should have changed
    assertNull("Quoted property 'a' should have been renamed", propNode);
  }

  /**
   * Test property renaming on multiple objects.
   */
  @Test
  public void testMultipleObjectsSameProperty() {
    String source = "var o1 = {x: 1}; var o2 = {x: 2}; o1.x + o2.x;";
    Node ast = parseAndRun(source);
    // Both 'x' properties should be renamed consistently
    Node prop1 = findPropertyAccess(ast, "x");
    assertNull("Property 'x' should have been renamed in o1", prop1);
  }

  /**
   * Test null or undefined inputs to the pass methods.
   */
  @Test(expected = NullPointerException.class)
  public void testNullRootNode() {
    pass.process(null, null);
  }

  /**
   * Test the pass with a source containing prototypal inheritance.
   */
  @Test
  public void testPrototypeProperties() {
    String source = "function Foo() {}; Foo.prototype.bar = 1;";
    Node ast = parseAndRun(source);
    // Properties on prototype should be renamed (unless reserved)
    Node propNode = findPropertyAccess(ast, "bar");
    assertNull("Prototype property 'bar' should have been renamed", propNode);
  }

  /**
   * Test that 'goog' property (a known reserved prefix) is not renamed.
   * This is a common pattern in Closure and may relate to bug 134.
   */
  @Test
  public void testGoogPropertiesNotRenamed() {
    String source = "goog.require('foo'); goog.provide('bar');";
    Node ast = parseAndRun(source);
    // The property 'require' and 'provide' on the 'goog' object should remain
    Node requireNode = findPropertyAccess(ast, "require");
    assertNotNull("Property 'require' on 'goog' should not be renamed", requireNode);
    assertEquals("require", requireNode.getString());
  }

  // ---------- Utility methods ----------

  private Node parseAndRun(String source) {
    Node root = compiler.parseSyntheticCode(source);
    pass.process(compiler.getExternsRoot(), root);
    return root;
  }

  private Node parseWithExterns(String externs, String source) {
    Node externRoot = compiler.parseSyntheticCode(externs);
    Node sourceRoot = compiler.parseSyntheticCode(source);
    pass.process(externRoot, sourceRoot);
    return sourceRoot;
  }

  /**
   * Helper to find a GETPROP node with the given string literal.
   * Returns the first such node found in a depth-first traversal.
   */
  private Node findPropertyAccess(Node node, String propName) {
    if (node == null) return null;
    if (node.isGetProp() && node.getLastChild().isString()
        && node.getLastChild().getString().equals(propName)) {
      return node;
    }
    for (Node child = node.getFirstChild(); child != null; child = child.getNext()) {
      Node result = findPropertyAccess(child, propName);
      if (result != null) return result;
    }
    return null;
  }

  /**
   * Helper to find a GETELEM node where the index is a string matching the given string.
   */
  private Node findElementAccess(Node node, String str) {
    if (node == null) return null;
    if (node.isGetElem()) {
      Node index = node.getLastChild();
      if (index.isString() && index.getString().equals(str)) {
        return node;
      }
    }
    for (Node child = node.getFirstChild(); child != null; child = child.getNext()) {
      Node result = findElementAccess(child, str);
      if (result != null) return result;
    }
    return null;
  }

  /**
   * Helper to find a GETELEM node where the index is a number matching the given value.
   */
  private Node findElementAccess(Node node, int num) {
    if (node == null) return null;
    if (node.isGetElem()) {
      Node index = node.getLastChild();
      if (index.isNumber() && index.getDouble() == num) {
        return node;
      }
    }
    for (Node child = node.getFirstChild(); child != null; child = child.getNext()) {
      Node result = findElementAccess(child, num);
      if (result != null) return result;
    }
    return null;
  }

  /**
   * Helper to check whether a property named 'propName' has been renamed.
   * Returns true if no GETPROP with that original name exists.
   */
  private boolean isPropertyRenamed(Node node, String propName) {
    return findPropertyAccess(node, propName) == null;
  }
}