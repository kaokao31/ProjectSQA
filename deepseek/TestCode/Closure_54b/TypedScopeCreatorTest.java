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

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class TypedScopeCreatorTest {

  private Compiler compiler;
  private TypedScopeCreator creator;

  @Before
  public void setUp() {
    compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
    creator = new TypedScopeCreator(compiler);
  }

  private Node parse(String js) {
    return compiler.parseTestCode(js);
  }

  private Scope createGlobalScope(String js) {
    return creator.createScope(parse(js), null);
  }

  private List<Node> findFunctions(Node n) {
    List<Node> functions = new ArrayList<Node>();
    if (n.getType() == Token.FUNCTION) {
      functions.add(n);
    }
    for (Node child = n.getFirstChild(); child != null; child = child.getNext()) {
      functions.addAll(findFunctions(child));
    }
    return functions;
  }

  private ObjectType getTypeOfThis(Node functionNode) {
    JSType type = functionNode.getJSType();
    assertNotNull("Function node has no JSType", type);
    assertTrue("Function node has non-function type: " + type,
        type.isFunctionType());
    FunctionType fnType = (FunctionType) type;
    ObjectType thisType = fnType.getTypeOfThis();
    assertNotNull("Function type has no this type", thisType);
    return thisType;
  }

  @Test
  public void testGlobalScopeContainsGlobalDeclarations() {
    Scope global = createGlobalScope("var x = 1; function f() {}");
    assertNotNull(global.getVar("x"));
    assertNotNull(global.getVar("f"));
    assertEquals("x", global.getVar("x").getNameNode().getString());
  }

  @Test
  public void testFunctionScopeContainsParametersAndVars() {
    Node root = parse("function f(a, b) { var c; }");
    Scope global = creator.createScope(root, null);
    Scope.Var fVar = global.getVar("f");
    assertNotNull(fVar);

    Node fnNode = fVar.getNameNode().getParent();
    Scope fnScope = creator.createScope(fnNode, global);

    assertNotNull(fnScope.getVar("a"));
    assertNotNull(fnScope.getVar("b"));
    assertNotNull(fnScope.getVar("c"));
    assertSame(global, fnScope.getParent());
  }

  @Test
  public void testFunctionExpressionNameIsScopedToFunction() {
    Node root = parse("var f = function g() { return g; };");
    Scope global = creator.createScope(root, null);

    assertNull(global.getVar("g"));

    Scope.Var fVar = global.getVar("f");
    assertNotNull(fVar);

    Node nameNode = fVar.getNameNode();
    Node init = nameNode.getFirstChild();
    assertNotNull(init);
    assertEquals(Token.FUNCTION, init.getType());

    Scope fnScope = creator.createScope(init, global);
    Scope.Var gVar = fnScope.getVar("g");
    assertNotNull(gVar);
    assertTrue(gVar.getType().isFunctionType());
  }

  @Test
  public void testConstructorFunctionScopeThisType() {
    Node root = parse("/** @constructor */ function Foo() {}");
    Scope global = creator.createScope(root, null);

    Scope.Var fooVar = global.getVar("Foo");
    assertNotNull(fooVar);

    Node fnNode = fooVar.getNameNode().getParent();
    Scope fnScope = creator.createScope(fnNode, global);

    ObjectType thisType = fnScope.getTypeOfThis();
    assertNotNull(thisType);
    assertEquals("Foo", thisType.toString());
  }

  @Test
  public void testPrototypeMethodThisType() {
    Node root = parse(
        "/** @constructor */ function Foo() {}\n" +
        "Foo.prototype.bar = function() { return this; };");
    creator.createScope(root, null);

    List<Node> functions = findFunctions(root);
    assertTrue(functions.size() >= 2);

    ObjectType thisType = getTypeOfThis(functions.get(1));
    assertEquals("Foo", thisType.toString());
  }

  @Test
  public void testInheritedPrototypeMethodThisType() {
    Node root = parse(
        "/** @constructor */ function Parent() {}\n" +
        "/** @constructor @extends {Parent} */ function Child() {}\n" +
        "Child.prototype.method = function() { return this; };");
    creator.createScope(root, null);

    List<Node> functions = findFunctions(root);
    assertTrue(functions.size() >= 3);

    ObjectType thisType = getTypeOfThis(functions.get(2));
    assertEquals("Child", thisType.toString());
  }

  @Test
  public void testThisAnnotationIsHonored() {
    Node root = parse(
        "/** @constructor */ function Foo() {}\n" +
        "/** @this {Foo} */ function bar() { return this; };");
    creator.createScope(root, null);

    List<Node> functions = findFunctions(root);
    assertTrue(functions.size() >= 2);

    ObjectType thisType = getTypeOfThis(functions.get(1));
    assertEquals("Foo", thisType.toString());
  }

  @Test
  public void testObjectLiteralOnPrototypeThisType() {
    Node root = parse(
        "/** @constructor */ function Foo() {}\n" +
        "Foo.prototype = { bar: function() { return this; } };");
    creator.createScope(root, null);

    List<Node> functions = findFunctions(root);
    assertTrue(functions.size() >= 2);

    ObjectType thisType = getTypeOfThis(functions.get(1));
    assertNotNull(thisType);
    assertEquals("Foo", thisType.toString());
  }

  @Test
  public void testObjectLiteralMethodThisTypeIsNotUnknown() {
    Node root = parse("var obj = { method: function() { return this; } };");
    creator.createScope(root, null);

    List<Node> functions = findFunctions(root);
    assertEquals(1, functions.size());

    ObjectType thisType = getTypeOfThis(functions.get(0));
    assertFalse("this type should not be unknown: " + thisType,
        thisType.isUnknownType());
  }

  @Test
  public void testQualifiedFunctionAssignmentDoesNotCreateQualifiedGlobalVar() {
    Node root = parse("var ns = {};\nns.foo = function() { return this; };");
    Scope global = creator.createScope(root, null);

    assertNotNull(global.getVar("ns"));
    assertNull(global.getVar("ns.foo"));

    List<Node> functions = findFunctions(root);
    assertEquals(1, functions.size());

    ObjectType thisType = getTypeOfThis(functions.get(0));
    assertNotNull(thisType);
    assertFalse("this type should not be unknown: " + thisType,
        thisType.isUnknownType());
  }

  @Test
  public void testJSDocDeclaredFunctionType() {
    Node root = parse(
        "/** @param {number} x\n" +
        "  * @return {string} */\n" +
        "function f(x) { return String(x); }");
    Scope global = creator.createScope(root, null);

    Scope.Var fVar = global.getVar("f");
    assertNotNull(fVar);

    JSType type = fVar.getType();
    assertTrue(type.isFunctionType());

    FunctionType fnType = (FunctionType) type;
    assertEquals("string", fnType.getReturnType().toString());
  }

  @Test
  public void testInferredFunctionTypeFromFunctionExpression() {
    Scope global = createGlobalScope("var f = function() {};");

    Scope.Var fVar = global.getVar("f");
    assertNotNull(fVar);

    JSType type = fVar.getType();
    assertTrue(type.isFunctionType());

    FunctionType fnType = (FunctionType) type;
    assertNotNull(fnType.getReturnType());
  }
}