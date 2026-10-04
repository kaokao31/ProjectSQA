package com.google.javascript.jscomp;

import com.google.javascript.jscomp.Node;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.JSType;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test class for TypedScopeCreator, targeting coverage and Defects4J bug #172.
 */
public class TypedScopeCreatorTest {
  private Compiler compiler;
  private TypedScopeCreator scopeCreator;

  @Before
  public void setUp() {
    compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
    scopeCreator = new TypedScopeCreator(compiler);
  }

  @Test
  public void testNullRoot() {
    // Edge case: null node should throw or handle gracefully
    try {
      scopeCreator.createScope(null, null);
      fail("Expected NullPointerException or IllegalArgumentException");
    } catch (NullPointerException | IllegalArgumentException e) {
      // Expected
    }
  }

  @Test
  public void testEmptyScript() {
    String js = "";
    Node root = parse(js);
    Scope scope = scopeCreator.createScope(root, null);
    assertNotNull(scope);
    assertTrue(scope.getChildren().length == 0);
  }

  @Test
  public void testSimpleFunction() {
    String js = "function foo() {}";
    Node root = parse(js);
    Scope scope = scopeCreator.createScope(root, null);
    assertNotNull(scope);
    Var var = scope.getVar("foo");
    assertNotNull(var);
    assertEquals("foo", var.getName());
    assertTrue(var.getType() instanceof FunctionType);
  }

  @Test
  public void testFunctionWithParam() {
    String js = "function bar(x) {}";
    Node root = parse(js);
    Scope scope = scopeCreator.createScope(root, null);
    assertNotNull(scope);
    Var funcVar = scope.getVar("bar");
    assertNotNull(funcVar);
    FunctionType funcType = (FunctionType) funcVar.getType();
    assertNotNull(funcType.getParameters());
    assertEquals(1, funcType.getParameters().length);
    Node paramNode = funcType.getParameters()[0].getNode();
    assertNotNull(paramNode);
  }

  @Test
  public void testForwardReference() {
    // Defects4J bug #172: forward reference to type in function parameter
    String js = "function f(x) { var y = new x(); }";
    Node root = parse(js);
    Scope scope = scopeCreator.createScope(root, null);
    assertNotNull(scope);
    Var fVar = scope.getVar("f");
    assertNotNull(fVar);
  }

  @Test
  public void testConstructorFunction() {
    String js = "/** @constructor */ function Foo() {}";
    Node root = parse(js);
    Scope scope = scopeCreator.createScope(root, null);
    assertNotNull(scope);
    Var fooVar = scope.getVar("Foo");
    assertNotNull(fooVar);
    assertTrue(fooVar.getType() instanceof FunctionType);
    assertNotNull(((FunctionType) fooVar.getType()).getPrototypeProperty());
  }

  @Test
  public void testMultipleScopes() {
    String js = "var a = 1; function outer() { var b = 2; function inner() { var c = 3; } }";
    Node root = parse(js);
    Scope globalScope = scopeCreator.createScope(root, null);
    assertNotNull(globalScope);
    assertTrue(globalScope.getChildren().length > 0);
    Scope outerScope = null;
    for (Scope child : globalScope.getChildren()) {
      if (child.getRootNode().getFirstChild().getString().equals("outer")) {
        outerScope = child;
        break;
      }
    }
    assertNotNull("Outer scope not found", outerScope);
    Var outerVar = outerScope.getVar("outer");
    assertNull(outerVar); // outer is in global scope, not in its own scope
    Var bVar = outerScope.getVar("b");
    assertNotNull("b should be in outer scope", bVar);
  }

  @Test
  public void testNestedFunctionTypeInference() {
    // Test that type inference works with nested functions
    String js = "function makeAdder(x) { return function(y) { return x + y; }; }";
    Node root = parse(js);
    Scope scope = scopeCreator.createScope(root, null);
    assertNotNull(scope);
    Var makeAdder = scope.getVar("makeAdder");
    assertNotNull(makeAdder);
    assertTrue(makeAdder.getType() instanceof FunctionType);
    FunctionType makeAdderType = (FunctionType) makeAdder.getType();
    assertEquals(1, makeAdderType.getParameters().length);
    // The inner function's type should be inferred
    JSType returnType = makeAdderType.getReturnType();
    assertTrue(returnType instanceof FunctionType);
  }

  @Test
  public void testVarDeclarations() {
    String js = "var x = 10; var y = 'hello'; var z = null;";
    Node root = parse(js);
    Scope scope = scopeCreator.createScope(root, null);
    assertNotNull(scope);
    Var xVar = scope.getVar("x");
    assertNotNull(xVar);
    assertNotNull(xVar.getType());
    Var yVar = scope.getVar("y");
    assertNotNull(yVar);
    assertNotNull(yVar.getType());
    Var zVar = scope.getVar("z");
    assertNotNull(zVar);
    assertNotNull(zVar.getType());
  }

  @Test
  public void testFunctionExpression() {
    String js = "var f = function() {};";
    Node root = parse(js);
    Scope scope = scopeCreator.createScope(root, null);
    assertNotNull(scope);
    Var fVar = scope.getVar("f");
    assertNotNull(fVar);
    assertTrue(fVar.getType() instanceof FunctionType);
  }

  @Test
  public void testCatchScope() {
    // Test try-catch creates a scope for the error variable
    String js = "try { var a = 1; } catch(e) { var b = e; }";
    Node root = parse(js);
    Scope scope = scopeCreator.createScope(root, null);
    assertNotNull(scope);
    Var aVar = scope.getVar("a");
    assertNotNull(aVar);
    Var bVar = scope.getVar("b");
    assertNotNull(bVar);
    // The catch scope should contain 'e'
  }

  @Test
  public void testWithStatement() {
    // With statement creates its own scope
    String js = "var obj = {x:1}; with(obj) { var y = x; }";
    Node root = parse(js);
    Scope scope = scopeCreator.createScope(root, null);
    assertNotNull(scope);
    Var objVar = scope.getVar("obj");
    assertNotNull(objVar);
    Var yVar = scope.getVar("y");
    assertNotNull(yVar);
  }

  // Helper method to parse JavaScript source without errors
  private Node parse(String js) {
    Node script = compiler.parse(js);
    assertNotNull("Parsing failed for: " + js, script);
    assertTrue(compiler.getErrors().isEmpty());
    assertEquals(Token.SCRIPT, script.getType());
    return script;
  }
}