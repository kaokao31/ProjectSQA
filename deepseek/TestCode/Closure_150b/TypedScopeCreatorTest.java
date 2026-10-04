package com.google.javascript.jscomp;

import com.google.javascript.jscomp.Scope.Var;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test suite for TypedScopeCreator targeting maximum coverage and fault detection.
 * Designed for Defects4J Closure bug 150.
 */
public class TypedScopeCreatorTest {

  private Compiler compiler;
  private TypedScopeCreator creator;
  private ErrorReporter errorReporter;

  @Before
  public void setUp() {
    compiler = new Compiler();
    compiler.initOptions(new CompilerOptions());
    errorReporter = new ErrorReporter();
    creator = new TypedScopeCreator(compiler);
  }

  // Test basic scope creation from a simple program
  @Test
  public void testCreateScopeSimple() {
    String source = "var a = 1;";
    Node root = parse(source);
    Scope scope = creator.createScope(root, null);
    assertNotNull("Scope should not be null", scope);
    assertTrue("Scope should have a variable 'a'", scope.isDeclared("a", true));
    Var varA = scope.getVar("a");
    assertNotNull("Variable 'a' should exist", varA);
    assertEquals("Variable name should be 'a'", "a", varA.getName());
  }

  // Test scope creation with function declarations
  @Test
  public void testCreateScopeWithFunction() {
    String source = "function foo(x) { return x + 1; }";
    Node root = parse(source);
    Scope globalScope = creator.createScope(root, null);
    assertNotNull("Global scope should not be null", globalScope);
    assertTrue("Global scope should have function 'foo'", globalScope.isDeclared("foo", true));

    // Get the function node and create its scope
    Node fnNode = findFunction(root, "foo");
    assertNotNull("Function node should exist", fnNode);
    Scope fnScope = creator.createScope(fnNode, globalScope);
    assertNotNull("Function scope should not be null", fnScope);
    assertTrue("Function scope should have parameter 'x'", fnScope.isDeclared("x", true));
  }

  // Test scope creation with goog.provide and goog.require
  @Test
  public void testCreateScopeWithGoogProvide() {
    String source = "goog.provide('foo.bar'); var baz = 1;";
    Node root = parse(source);
    Scope scope = creator.createScope(root, null);
    assertNotNull("Scope should not be null", scope);
    // goog.provide creates a qualified name, but scope may not have it directly
    // This tests for potential NPE or incorrect handling (Closure bug 150)
    assertTrue("Scope should have variable 'baz'", scope.isDeclared("baz", true));
  }

  // Test scope creation with type annotations (JSDoc)
  @Test
  public void testCreateScopeWithTypeAnnotation() {
    String source = "/** @type {number} */ var x;";
    Node root = parse(source);
    Scope scope = creator.createScope(root, null);
    assertNotNull("Scope should not be null", scope);
    assertTrue("Scope should have variable 'x'", scope.isDeclared("x", true));
  }

  // Test scope creation with object literal
  @Test
  public void testCreateScopeWithObjectLiteral() {
    String source = "var obj = {a: 1, b: function() { return 2; }};";
    Node root = parse(source);
    Scope scope = creator.createScope(root, null);
    assertNotNull("Scope should not be null", scope);
    assertTrue("Scope should have variable 'obj'", scope.isDeclared("obj", true));
  }

  // Test scope creation with nested functions and closures
  @Test
  public void testCreateScopeWithNestedFunctions() {
    String source = "function outer() { var x = 1; function inner() { return x; } return inner; }";
    Node root = parse(source);
    Scope globalScope = creator.createScope(root, null);
    assertNotNull("Global scope should not be null", globalScope);

    Node outerFn = findFunction(root, "outer");
    assertNotNull("Outer function node should exist", outerFn);
    Scope outerScope = creator.createScope(outerFn, globalScope);
    assertNotNull("Outer function scope should not be null", outerScope);
    assertTrue("Outer scope should have variable 'x'", outerScope.isDeclared("x", true));
    assertTrue("Outer scope should have function 'inner'", outerScope.isDeclared("inner", true));
  }

  // Test scope creation with try-catch block (catch introduces scope)
  @Test
  public void testCreateScopeWithTryCatch() {
    String source = "try { var a = 1; } catch(e) { var b = 2; }";
    Node root = parse(source);
    Scope scope = creator.createScope(root, null);
    assertNotNull("Scope should not be null", scope);
    // Variables inside try/catch are hoisted to function/global scope
    assertTrue("Scope should have variable 'a'", scope.isDeclared("a", true));
    assertTrue("Scope should have variable 'b'", scope.isDeclared("b", true));
  }

  // Test scope creation with an empty program
  @Test
  public void testCreateScopeEmpty() {
    String source = "";
    Node root = parse(source);
    Scope scope = creator.createScope(root, null);
    assertNotNull("Scope should not be null", scope);
    assertTrue("Scope should have no variables", scope.getAllSymbols().isEmpty());
  }

  // Test scope creation with multiple scripts (simulated by wrapping in SCRIPT node)
  @Test
  public void testCreateScopeMultipleScripts() {
    String source = "var a = 1; var b = 2;";
    Node root = parse(source);
    Scope scope = creator.createScope(root, null);
    assertNotNull("Scope should not be null", scope);
    assertTrue("Scope should have variable 'a'", scope.isDeclared("a", true));
    assertTrue("Scope should have variable 'b'", scope.isDeclared("b", true));
  }

  // Test that createScope does not throw on null parent (root scope)
  @Test(expected = NullPointerException.class)
  public void testCreateScopeNullParent() {
    creator.createScope(null, null);
  }

  // Test that createScope throws on invalid root node type
  @Test(expected = IllegalArgumentException.class)
  public void testCreateScopeInvalidRoot() {
    Node invalidRoot = new Node(Token.NAME);
    creator.createScope(invalidRoot, null);
  }

  // Helper methods

  private Node parse(String source) {
    CompilerInput input = new CompilerInput(
        SourceFile.fromCode("test.js", source));
    compiler.compile(
        Collections.singletonList(SourceFile.fromCode("externs.js", "")),
        Collections.singletonList(input));
    return compiler.getRoot().getLastChild();
  }

  private Node findFunction(Node root, String name) {
    if (root == null) return null;
    if (root.isFunction()) {
      Node nameNode = root.getFirstChild();
      if (nameNode != null && nameNode.getString().equals(name)) {
        return root;
      }
    }
    for (Node child = root.getFirstChild(); child != null; child = child.getNext()) {
      Node result = findFunction(child, name);
      if (result != null) return result;
    }
    return null;
  }

  // Inner error reporter for capturing warnings/errors
  private static class ErrorReporter implements com.google.javascript.jscomp.ErrorReporter {
    private final List<JSError> errors = new ArrayList<>();

    @Override
    public void reportError(JSError error) {
      errors.add(error);
    }

    @Override
    public void reportWarning(JSError warning) {
      // ignore warnings for test simplicity
    }

    public List<JSError> getErrors() {
      return errors;
    }
  }
}