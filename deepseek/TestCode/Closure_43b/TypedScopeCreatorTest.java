package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;

public class TypedScopeCreatorTest {

  private static Scope globalScope(String code) {
    Compiler compiler = new Compiler();
    compiler.initOptions(new CompilerOptions());
    Node root = compiler.parseTestCode(code);
    assertNotNull("Failed to parse: " + code, root);
    return new TypedScopeCreator(compiler).createScope(root, null);
  }

  private static Scope functionScope(String code) {
    Compiler compiler = new Compiler();
    compiler.initOptions(new CompilerOptions());
    Node root = compiler.parseTestCode(code);
    assertNotNull("Failed to parse: " + code, root);
    Node function = findFirstFunction(root);
    assertNotNull("No function found in: " + code, function);
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope global = creator.createScope(root, null);
    return creator.createScope(function, global);
  }

  private static Scope catchScope(String code) {
    Compiler compiler = new Compiler();
    compiler.initOptions(new CompilerOptions());
    Node root = compiler.parseTestCode(code);
    assertNotNull("Failed to parse: " + code, root);
    Node catchNode = findFirstToken(root, Token.CATCH);
    assertNotNull("No catch found in: " + code, catchNode);
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope global = creator.createScope(root, null);
    return creator.createScope(catchNode, global);
  }

  private static Node findFirstFunction(Node node) {
    if (node.getType() == Token.FUNCTION) {
      return node;
    }
    for (Node child = node.getFirstChild(); child != null; child = child.getNext()) {
      Node result = findFirstFunction(child);
      if (result != null) {
        return result;
      }
    }
    return null;
  }

  private static Node findFirstToken(Node node, int tokenType) {
    if (node.getType() == tokenType) {
      return node;
    }
    for (Node child = node.getFirstChild(); child != null; child = child.getNext()) {
      Node result = findFirstToken(child, tokenType);
      if (result != null) {
        return result;
      }
    }
    return null;
  }

  @Test
  public void testEmptyGlobalScope() {
    Scope scope = globalScope("");
    assertNotNull(scope);
    assertTrue(scope.isGlobal());
    assertNull(scope.getParent());
  }

  @Test
  public void testGlobalVarDeclaration() {
    Scope scope = globalScope("var x;");
    Var x = scope.getVar("x");
    assertNotNull(x);
    assertEquals("x", x.getName());
    assertSame(scope, x.getScope());
  }

  @Test
  public void testFunctionDeclarationIsVarInGlobalScope() {
    Scope scope = globalScope("function f() {}");
    Var f = scope.getVar("f");
    assertNotNull(f);
    assertNotNull(f.getType());
  }

  @Test
  public void testGlobalVarWithInitializerGetsInferredType() {
    Scope scope = globalScope("var x = 1;");
    Var x = scope.getVar("x");
    assertNotNull(x);
    assertNotNull(x.getType());
  }

  @Test
  public void testJsDocTypeAnnotation() {
    Scope scope = globalScope("/** @type {string} */ var s;");
    Var s = scope.getVar("s");
    assertNotNull(s);
    assertNotNull(s.getType());
    assertEquals("string", s.getType().toString());
  }

  @Test
  public void testFunctionParametersAndLocals() {
    Scope scope = functionScope("function f(a, b) { var c = 1; }");
    assertNotNull(scope.getVar("a"));
    assertNotNull(scope.getVar("b"));
    assertNotNull(scope.getVar("c"));
    assertSame(scope, scope.getVar("a").getScope());
  }

  @Test
  public void testFunctionScopeParentIsGlobal() {
    Scope scope = functionScope("function f() {}");
    assertNotNull(scope.getParent());
    assertTrue(scope.getParent().isGlobal());
  }

  @Test
  public void testNestedFunctionDeclarationInLocalScope() {
    Scope scope = functionScope("function f() { function g() {} }");
    assertNotNull(scope.getVar("g"));
  }

  @Test
  public void testArgumentsIsDeclaredInFunctionScope() {
    Scope scope = functionScope("function f() { return arguments; }");
    assertNotNull(scope.getVar("arguments"));
  }

  @Test
  public void testNamedFunctionExpressionNameIsNotInOuterScope() {
    Scope scope = globalScope("var f = function g() {};");
    assertNotNull(scope.getVar("f"));
    assertNull(scope.getVar("g"));
  }

  @Test
  public void testNamedFunctionExpressionNameIsInFunctionScope() {
    Scope scope = functionScope("var f = function g() {};");
    Var g = scope.getVar("g");
    assertNotNull(g);
    assertSame(scope, g.getScope());
  }

  @Test
  public void testAnonymousFunctionExpressionHasNoEmptyNameVar() {
    Scope scope = globalScope("var f = function() {};");
    assertNotNull(scope.getVar("f"));
    assertNull(scope.getVar(""));
  }

  @Test
  public void testQualifiedFunctionExpressionNameIsLocalOnly() {
    Scope scope = globalScope("var ns = {}; ns.foo = function bar() {};");
    assertNotNull(scope.getVar("ns"));
    assertNull(scope.getVar("bar"));
  }

  @Test
  public void testCatchParameterIsInCatchScope() {
    Scope scope = catchScope("try { throw 1; } catch (e) { var x = 1; }");
    assertNotNull(scope.getVar("e"));
  }

  @Test
  public void testForLoopVarIsInGlobalScope() {
    Scope scope = globalScope("for (var i = 0; i < 10; i++) {}");
    assertNotNull(scope.getVar("i"));
  }

  @Test
  public void testConstructorFunctionIsTyped() {
    Scope scope = globalScope("/** @constructor */ function Foo() {}");
    Var foo = scope.getVar("Foo");
    assertNotNull(foo);
    assertNotNull(foo.getType());
    assertTrue(foo.getType().isConstructor());
  }

  @Test
  public void testInterfaceFunctionIsTyped() {
    Scope scope = globalScope("/** @interface */ function Foo() {}");
    Var foo = scope.getVar("Foo");
    assertNotNull(foo);
    assertNotNull(foo.getType());
    assertTrue(foo.getType().isInterface());
  }

  @Test
  public void testEnumType() {
    Scope scope = globalScope("/** @enum {string} */ var E = {A: 'a'};");
    Var e = scope.getVar("E");
    assertNotNull(e);
    assertNotNull(e.getType());
    assertTrue(e.getType().isEnumType());
  }

  @Test
  public void testRecordTypeAnnotation() {
    Scope scope = globalScope("/** @type {{a: number}} */ var o;");
    Var o = scope.getVar("o");
    assertNotNull(o);
    assertNotNull(o.getType());
  }

  @Test
  public void testTypeOfThisInConstructor() {
    Scope scope = functionScope("/** @constructor */ function Foo() { this.x = 1; }");
    assertNotNull(scope.getTypeOfThis());
  }
}