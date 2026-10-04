package com.google.javascript.jscomp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.ObjectType;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class TypedScopeCreatorTest {

  private Compiler compiler;
  private TypedScopeCreator scopeCreator;

  @Before
  public void setUp() {
    compiler = new Compiler();
    compiler.initOptions(new CompilerOptions());
    scopeCreator = new TypedScopeCreator(compiler);
  }

  private Scope createScope(String source) {
    Node root = compiler.parse(SourceFile.fromCode("test.js", source));
    assertNotNull("Compiler failed to parse source", root);
    return scopeCreator.createScope(root, null);
  }

  private FunctionType getFunctionType(Scope scope, String name) {
    JSType type = scope.getVar(name).getType();
    assertNotNull("Type for " + name + " should not be null", type);
    assertTrue("Type for " + name + " should be a function type",
        type.isFunctionType());
    return type.toMaybeFunctionType();
  }

  @Test
  public void testGlobalScopeContainsVarDeclarations() {
    Scope scope = createScope("var a; var b = 1;");
    assertTrue(scope.isGlobal());
    assertNotNull(scope.getVar("a"));
    assertNotNull(scope.getVar("b"));
    assertNull(scope.getVar("c"));
  }

  @Test
  public void testLiteralTypesAreInferred() {
    Scope scope = createScope("var n = 1; var s = 'x'; var b = true;");
    assertTrue(scope.getVar("n").getType().isNumber());
    assertTrue(scope.getVar("s").getType().isString());
    assertTrue(scope.getVar("b").getType().isBoolean());
  }

  @Test
  public void testObjectLiteralTypeIsObject() {
    Scope scope = createScope("var o = {a: 1};");
    assertTrue(scope.getVar("o").getType().isObject());
  }

  @Test
  public void testArrayLiteralTypeIsArray() {
    Scope scope = createScope("var a = [];");
    assertTrue(scope.getVar("a").getType().isArrayType());
  }

  @Test
  public void testFunctionDeclarationCreatesChildScope() {
    Scope global = createScope("function f() { var x; }");
    assertNotNull(global.getVar("f"));
    List<Scope> children = global.getChildren();
    assertEquals(1, children.size());
    Scope fnScope = children.get(0);
    assertNotNull(fnScope.getVar("x"));
    assertNull(fnScope.getVar("y"));
  }

  @Test
  public void testFunctionExpressionNameDoesNotLeakToGlobalScope() {
    Scope global = createScope("var f = function g() { var x; };");
    assertNotNull(global.getVar("f"));
    assertNull("function expression name should be function-local",
        global.getVar("g"));
    List<Scope> children = global.getChildren();
    assertEquals(1, children.size());
    Scope fnScope = children.get(0);
    assertNotNull(fnScope.getVar("g"));
    assertNotNull(fnScope.getVar("x"));
  }

  @Test
  public void testAnonymousFunctionExpression() {
    Scope global = createScope("var f = function() { var x; };");
    List<Scope> children = global.getChildren();
    assertEquals(1, children.size());
    Scope fnScope = children.get(0);
    assertNotNull(fnScope.getVar("x"));
  }

  @Test
  public void testJSDocTypeAnnotation() {
    Scope scope = createScope("/** @type {string} */ var s;");
    assertTrue(scope.getVar("s").getType().isString());
  }

  @Test
  public void testConstructorJSDoc() {
    Scope scope = createScope("/** @constructor */ function Foo() {}");
    FunctionType fnType = getFunctionType(scope, "Foo");
    assertTrue("constructor should be a constructor", fnType.isConstructor());
    assertFalse(fnType.isInterface());
    assertNotNull(fnType.getInstanceType());
  }

  @Test
  public void testInterfaceJSDoc() {
    Scope scope = createScope("/** @interface */ function Foo() {}");
    FunctionType fnType = getFunctionType(scope, "Foo");
    assertTrue("interface should be marked as interface", fnType.isInterface());
  }

  @Test
  public void testPrototypePropertyDeclaredWithJSDoc() {
    String source = ""
        + "/** @constructor */ function Foo() {}\n"
        + "/** @type {number} */ Foo.prototype.bar = 1;\n";
    Scope scope = createScope(source);
    FunctionType ctorType = getFunctionType(scope, "Foo");
    ObjectType proto = ctorType.getPrototype();
    assertNotNull(proto);
    assertTrue("prototype should have property bar", proto.hasProperty("bar"));
    assertTrue("prototype.bar should be a number",
        proto.getPropertyType("bar").isNumber());
  }

  @Test
  public void testOrdinaryFunctionIsNotConstructor() {
    Scope scope = createScope("function Foo() {}\nFoo.prototype.bar = 1;");
    FunctionType fnType = getFunctionType(scope, "Foo");
    assertFalse(fnType.isConstructor());
  }

  @Test
  public void testEnumJSDoc() {
    Scope scope = createScope("/** @enum {string} */ var E = {A: 'a', B: 'b'};");
    JSType type = scope.getVar("E").getType();
    assertNotNull(type);
    assertTrue("enum variable should have enum type", type.isEnumType());
  }

  @Test
  public void testReturnTypeIsSet() {
    Scope scope = createScope("/** @return {number} */ function f() { return 1; }");
    FunctionType fnType = getFunctionType(scope, "f");
    assertTrue(fnType.getReturnType().isNumber());
  }

  @Test
  public void testNestedFunctionScopeContainsParameters() {
    String source = "function outer(a) { var b = function inner(c) { var d; }; }";
    Scope global = createScope(source);
    Scope outer = global.getChildren().get(0);
    assertNotNull(outer.getVar("a"));
    assertNotNull(outer.getVar("b"));
    assertTrue(outer.getVar("b").getType().isFunctionType());
    assertEquals(1, outer.getChildren().size());
    Scope inner = outer.getChildren().get(0);
    assertNotNull(inner.getVar("c"));
    assertNotNull(inner.getVar("d"));
  }

  @Test
  public void testFunctionScopeHasOwnRootNode() {
    Scope global = createScope("function f() { var x; }");
    Scope fn = global.getChildren().get(0);
    Node fnNode = fn.getRootNode();
    assertNotNull(fnNode);
    assertEquals(Token.FUNCTION, fnNode.getType());
    Node name = fnNode.getFirstChild();
    assertNotNull(name);
    assertEquals("f", name.getString());
  }

  @Test
  public void testExtendsCreatesInheritance() {
    String source = ""
        + "/** @constructor */ function A() {}\n"
        + "/** @constructor @extends {A} */ function B() {}\n";
    Scope scope = createScope(source);
    FunctionType aCtor = getFunctionType(scope, "A");
    FunctionType bCtor = getFunctionType(scope, "B");
    ObjectType bProto = bCtor.getPrototype();
    ObjectType aProto = aCtor.getPrototype();
    assertNotNull(bProto);
    assertNotNull(aProto);
    assertSame(aProto, bProto.getImplicitPrototype());
  }

  @Test
  public void testForwardExtendsReference() {
    String source = ""
        + "/** @constructor @extends {A} */ function B() {}\n"
        + "/** @constructor */ function A() {}\n";
    Scope scope = createScope(source);
    FunctionType bCtor = getFunctionType(scope, "B");
    ObjectType bProto = bCtor.getPrototype();
    ObjectType aProto = getFunctionType(scope, "A").getPrototype();
    assertNotNull(bProto);
    assertNotNull(aProto);
    assertSame(aProto, bProto.getImplicitPrototype());
  }

  @Test
  public void testForwardTypeReference() {
    String source = "/** @type {Foo} */ var x;\n"
        + "/** @constructor */ function Foo() {}";
    Scope scope = createScope(source);
    JSType type = scope.getVar("x").getType();
    assertNotNull(type);
    assertTrue("forward-referenced declared type should be an object",
        type.isObject());
  }
}