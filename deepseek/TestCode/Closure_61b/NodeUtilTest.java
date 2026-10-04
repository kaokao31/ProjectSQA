package com.google.javascript.jscomp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class NodeUtilTest {

  private static Node name(String value) {
    Node n = new Node(Token.NAME);
    n.setString(value);
    return n;
  }

  private static Node string(String value) {
    Node n = new Node(Token.STRING);
    n.setString(value);
    return n;
  }

  private static Node number(double value) {
    Node n = new Node(Token.NUMBER);
    n.setDouble(value);
    return n;
  }

  private static Node function(String functionName) {
    Node fn = new Node(Token.FUNCTION);
    fn.addChildToBack(name(functionName));
    fn.addChildToBack(new Node(Token.BLOCK));
    return fn;
  }

  private static Node call(String callee) {
    Node callNode = new Node(Token.CALL);
    callNode.addChildToBack(name(callee));
    return callNode;
  }

  @Test
  public void testIsName() {
    assertTrue(NodeUtil.isName(name("x")));
    assertFalse(NodeUtil.isName(string("x")));
    assertFalse(NodeUtil.isName(number(1)));
    assertFalse(NodeUtil.isName(new Node(Token.THIS)));
  }

  @Test
  public void testIsString() {
    assertTrue(NodeUtil.isString(string("x")));
    assertFalse(NodeUtil.isString(name("x")));
    assertFalse(NodeUtil.isString(number(1)));
  }

  @Test
  public void testIsNumber() {
    assertTrue(NodeUtil.isNumber(number(0)));
    assertTrue(NodeUtil.isNumber(number(Double.NaN)));
    assertFalse(NodeUtil.isNumber(string("0")));
  }

  @Test
  public void testIsBooleanLiteral() {
    assertTrue(NodeUtil.isBoolean(new Node(Token.TRUE)));
    assertTrue(NodeUtil.isBoolean(new Node(Token.FALSE)));
    assertFalse(NodeUtil.isBoolean(number(1)));
    assertFalse(NodeUtil.isBoolean(name("true")));
  }

  @Test
  public void testIsNull() {
    assertTrue(NodeUtil.isNull(new Node(Token.NULL)));
    assertFalse(NodeUtil.isNull(name("null")));
  }

  @Test
  public void testIsThis() {
    assertTrue(NodeUtil.isThis(new Node(Token.THIS)));
    assertFalse(NodeUtil.isThis(name("this")));
  }

  @Test
  public void testIsFunction() {
    assertTrue(NodeUtil.isFunction(function("")));
    assertFalse(NodeUtil.isFunction(name("f")));
  }

  @Test
  public void testIsArrayLiteral() {
    assertTrue(NodeUtil.isArrayLiteral(new Node(Token.ARRAYLIT)));
    assertFalse(NodeUtil.isArrayLiteral(new Node(Token.OBJECTLIT)));
  }

  @Test
  public void testIsObjectLiteral() {
    assertTrue(NodeUtil.isObjectLiteral(new Node(Token.OBJECTLIT)));
    assertFalse(NodeUtil.isObjectLiteral(new Node(Token.ARRAYLIT)));
  }

  @Test
  public void testIsNaN() {
    assertTrue(NodeUtil.isNaN(number(Double.NaN)));
    assertFalse(NodeUtil.isNaN(number(0)));
    assertFalse(NodeUtil.isNaN(name("NaN")));
  }

  @Test
  public void testIsUndefined() {
    assertTrue(NodeUtil.isUndefined(name("undefined")));
    assertFalse(NodeUtil.isUndefined(name("x")));
    assertFalse(NodeUtil.isUndefined(new Node(Token.NULL)));
  }

  @Test
  public void testIsExprCall() {
    Node expr = new Node(Token.EXPR_RESULT);
    expr.addChildToBack(call("foo"));
    assertTrue(NodeUtil.isExprCall(expr));

    Node expr2 = new Node(Token.EXPR_RESULT);
    expr2.addChildToBack(name("foo"));
    assertFalse(NodeUtil.isExprCall(expr2));

    assertFalse(NodeUtil.isExprCall(call("foo")));
  }

  @Test
  public void testIsCallTo() {
    assertTrue(NodeUtil.isCallTo(call("foo"), "foo"));
    assertFalse(NodeUtil.isCallTo(call("foo"), "bar"));
  }

  @Test
  public void testIsVarAndAssign() {
    assertTrue(NodeUtil.isVar(new Node(Token.VAR)));
    assertFalse(NodeUtil.isVar(name("x")));

    assertTrue(NodeUtil.isAssign(new Node(Token.ASSIGN)));
    assertFalse(NodeUtil.isAssign(new Node(Token.VAR)));
  }

  @Test
  public void testGetBooleanValue() {
    assertTrue(NodeUtil.getBooleanValue(new Node(Token.TRUE)));
    assertFalse(NodeUtil.getBooleanValue(new Node(Token.FALSE)));

    assertFalse(NodeUtil.getBooleanValue(number(0)));
    assertTrue(NodeUtil.getBooleanValue(number(1)));
    assertFalse(NodeUtil.getBooleanValue(number(Double.NaN)));

    assertFalse(NodeUtil.getBooleanValue(string("")));
    assertTrue(NodeUtil.getBooleanValue(string("x")));

    assertFalse(NodeUtil.getBooleanValue(new Node(Token.NULL)));
    assertFalse(NodeUtil.getBooleanValue(name("undefined")));
    assertFalse(NodeUtil.getBooleanValue(name("NaN")));
    assertTrue(NodeUtil.getBooleanValue(name("x")));
  }

  @Test
  public void testIsImmutableValue() {
    assertTrue(NodeUtil.isImmutableValue(string("x")));
    assertTrue(NodeUtil.isImmutableValue(number(1)));
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.TRUE)));
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.FALSE)));
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.NULL)));
    assertTrue(NodeUtil.isImmutableValue(name("undefined")));
    assertTrue(NodeUtil.isImmutableValue(name("NaN")));

    assertFalse(NodeUtil.isImmutableValue(name("x")));
    assertFalse(NodeUtil.isImmutableValue(new Node(Token.ARRAYLIT)));
    assertFalse(NodeUtil.isImmutableValue(new Node(Token.OBJECTLIT)));
  }

  @Test
  public void testIsLiteralValue() {
    assertTrue(NodeUtil.isLiteralValue(string("x"), false));
    assertTrue(NodeUtil.isLiteralValue(number(1), false));
    assertTrue(NodeUtil.isLiteralValue(new Node(Token.TRUE), false));
    assertTrue(NodeUtil.isLiteralValue(new Node(Token.NULL), false));
    assertTrue(NodeUtil.isLiteralValue(new Node(Token.ARRAYLIT), false));
    assertTrue(NodeUtil.isLiteralValue(new Node(Token.OBJECTLIT), false));
    assertTrue(NodeUtil.isLiteralValue(new Node(Token.REGEXP), false));
    assertTrue(NodeUtil.isLiteralValue(name("undefined"), false));

    assertFalse(NodeUtil.isLiteralValue(name("x"), false));
    assertFalse(NodeUtil.isLiteralValue(function(""), false));
    assertTrue(NodeUtil.isLiteralValue(function(""), true));
  }

  @Test
  public void testIsValidDefineValue() {
    Set<String> consts = new HashSet<String>();
    consts.add("CONST");

    assertTrue(NodeUtil.isValidDefineValue(string("a"), consts));
    assertTrue(NodeUtil.isValidDefineValue(number(1), consts));
    assertTrue(NodeUtil.isValidDefineValue(number(-1), consts));
    assertTrue(NodeUtil.isValidDefineValue(new Node(Token.TRUE), consts));
    assertTrue(NodeUtil.isValidDefineValue(new Node(Token.FALSE), consts));

    assertTrue(NodeUtil.isValidDefineValue(name("CONST"), consts));
    assertFalse(NodeUtil.isValidDefineValue(name("OTHER"), consts));
    assertFalse(NodeUtil.isValidDefineValue(name("OTHER"), null));

    assertFalse(NodeUtil.isValidDefineValue(new Node(Token.ARRAYLIT), consts));
    assertFalse(NodeUtil.isValidDefineValue(new Node(Token.OBJECTLIT), consts));
    assertFalse(NodeUtil.isValidDefineValue(new Node(Token.REGEXP), consts));
    assertFalse(NodeUtil.isValidDefineValue(function(""), consts));
  }

  @Test
  public void testIsStatement() {
    assertTrue(NodeUtil.isStatement(new Node(Token.BLOCK)));
    assertTrue(NodeUtil.isStatement(new Node(Token.VAR)));
    assertTrue(NodeUtil.isStatement(new Node(Token.IF)));
    assertTrue(NodeUtil.isStatement(new Node(Token.FOR)));
    assertTrue(NodeUtil.isStatement(new Node(Token.WHILE)));
    assertTrue(NodeUtil.isStatement(new Node(Token.DO)));
    assertTrue(NodeUtil.isStatement(new Node(Token.SWITCH)));
    assertTrue(NodeUtil.isStatement(new Node(Token.TRY)));
    assertTrue(NodeUtil.isStatement(new Node(Token.RETURN)));
    assertTrue(NodeUtil.isStatement(new Node(Token.THROW)));
    assertTrue(NodeUtil.isStatement(new Node(Token.EXPR_RESULT)));
    assertTrue(NodeUtil.isStatement(new Node(Token.LABEL)));
    assertTrue(NodeUtil.isStatement(function("")));

    assertFalse(NodeUtil.isStatement(name("x")));
    assertFalse(NodeUtil.isStatement(string("x")));
    assertFalse(NodeUtil.isStatement(number(1)));
    assertFalse(NodeUtil.isStatement(new Node(Token.NULL)));
  }

  @Test
  public void testIsControlStructure() {
    assertTrue(NodeUtil.isControlStructure(new Node(Token.IF)));
    assertTrue(NodeUtil.isControlStructure(new Node(Token.FOR)));
    assertTrue(NodeUtil.isControlStructure(new Node(Token.WHILE)));
    assertTrue(NodeUtil.isControlStructure(new Node(Token.DO)));
    assertTrue(NodeUtil.isControlStructure(new Node(Token.SWITCH)));
    assertTrue(NodeUtil.isControlStructure(new Node(Token.TRY)));
    assertTrue(NodeUtil.isControlStructure(new Node(Token.LABEL)));

    assertFalse(NodeUtil.isControlStructure(new Node(Token.BLOCK)));
    assertFalse(NodeUtil.isControlStructure(new Node(Token.VAR)));
  }

  @Test
  public void testIsLoopStructure() {
    assertTrue(NodeUtil.isLoopStructure(new Node(Token.FOR)));
    assertTrue(NodeUtil.isLoopStructure(new Node(Token.WHILE)));
    assertTrue(NodeUtil.isLoopStructure(new Node(Token.DO)));

    assertFalse(NodeUtil.isLoopStructure(new Node(Token.IF)));
    assertFalse(NodeUtil.isLoopStructure(new Node(Token.SWITCH)));
  }

  @Test
  public void testIsForIn() {
    assertTrue(NodeUtil.isForIn(new Node(Token.FOR_IN)));
    assertFalse(NodeUtil.isForIn(new Node(Token.FOR)));
  }

  @Test
  public void testIsNameDeclaration() {
    assertTrue(NodeUtil.isNameDeclaration(new Node(Token.VAR)));
    assertFalse(NodeUtil.isNameDeclaration(name("x")));
    assertFalse(NodeUtil.isNameDeclaration(new Node(Token.ASSIGN)));
  }

  @Test
  public void testIsFunctionDeclarationAndExpression() {
    Node script = new Node(Token.SCRIPT);
    Node fnDecl = function("f");
    script.addChildToBack(fnDecl);

    assertTrue(NodeUtil.isFunctionDeclaration(fnDecl));
    assertFalse(NodeUtil.isFunctionExpression(fnDecl));

    Node exprResult = new Node(Token.EXPR_RESULT);
    Node fnExpr = function("");
    exprResult.addChildToBack(fnExpr);

    assertFalse(NodeUtil.isFunctionDeclaration(fnExpr));
    assertTrue(NodeUtil.isFunctionExpression(fnExpr));

    Node nameVar = name("x");
    Node fnInVar = function("g");
    nameVar.addChildToBack(fnInVar);

    assertFalse(NodeUtil.isFunctionDeclaration(fnInVar));
    assertTrue(NodeUtil.isFunctionExpression(fnInVar));
  }

  @Test
  public void testGetFunctionName() {
    assertEquals("foo", NodeUtil.getFunctionName(function("foo")));
    assertNull(NodeUtil.getFunctionName(function("")));
    assertNull(NodeUtil.getFunctionName(name("foo")));
  }
}