package com.google.javascript.jscomp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.TernaryValue;
import org.junit.Test;

public class NodeUtilTest {

  private static Node newFunction(String name) {
    Node fn = new Node(Token.FUNCTION);
    Node fnName = new Node(Token.NAME, name);
    Node params = new Node(Token.PARAM_LIST);
    Node body = new Node(Token.BLOCK);
    fn.addChildToBack(fnName);
    fn.addChildToBack(params);
    fn.addChildToBack(body);
    return fn;
  }

  private static Node getprop(String object, String property) {
    Node getprop = new Node(Token.GETPROP);
    getprop.addChildToBack(new Node(Token.NAME, object));
    getprop.addChildToBack(new Node(Token.STRING, property));
    return getprop;
  }

  @Test
  public void testIsFunctionDeclarationAndExpression() {
    Node fnDecl = newFunction("foo");
    Node script = new Node(Token.SCRIPT);
    script.addChildToBack(fnDecl);
    assertTrue(NodeUtil.isFunctionDeclaration(fnDecl));
    assertFalse(NodeUtil.isFunctionExpression(fnDecl));

    Node fnExpr = newFunction("bar");
    Node assign = new Node(Token.ASSIGN);
    assign.addChildToBack(new Node(Token.NAME, "x"));
    assign.addChildToBack(fnExpr);
    assertFalse(NodeUtil.isFunctionDeclaration(fnExpr));
    assertTrue(NodeUtil.isFunctionExpression(fnExpr));
  }

  @Test
  public void testGetFunctionName() {
    Node named = newFunction("foo");
    assertEquals("foo", NodeUtil.getFunctionName(named));
  }

  @Test
  public void testGetFunctionBody() {
    Node fn = newFunction("foo");
    Node body = fn.getChildAtIndex(2);
    assertSame(body, NodeUtil.getFunctionBody(fn));
  }

  @Test
  public void testIsNameDeclaration() {
    assertTrue(NodeUtil.isNameDeclaration(new Node(Token.VAR)));
    assertFalse(NodeUtil.isNameDeclaration(new Node(Token.NAME)));
  }

  @Test
  public void testIsVarDeclaration() {
    assertTrue(NodeUtil.isVarDeclaration(new Node(Token.VAR)));
    assertFalse(NodeUtil.isVarDeclaration(new Node(Token.NAME)));
  }

  @Test
  public void testIsReferenceName() {
    assertTrue(NodeUtil.isReferenceName(new Node(Token.NAME, "a")));
    assertTrue(NodeUtil.isReferenceName(getprop("a", "b")));
    assertFalse(NodeUtil.isReferenceName(new Node(Token.NUMBER, 1.0)));
  }

  @Test
  public void testIsAssignmentOp() {
    assertTrue(NodeUtil.isAssignmentOp(new Node(Token.ASSIGN)));
    assertFalse(NodeUtil.isAssignmentOp(new Node(Token.ADD)));
  }

  @Test
  public void testIsComparisonOp() {
    assertTrue(NodeUtil.isComparisonOp(new Node(Token.EQ)));
    assertFalse(NodeUtil.isComparisonOp(new Node(Token.ADD)));
  }

  @Test
  public void testIsUnaryOp() {
    assertTrue(NodeUtil.isUnaryOp(new Node(Token.NOT)));
    assertFalse(NodeUtil.isUnaryOp(new Node(Token.ADD)));
  }

  @Test
  public void testIsBinaryOp() {
    assertTrue(NodeUtil.isBinaryOp(new Node(Token.ADD)));
    assertFalse(NodeUtil.isBinaryOp(new Node(Token.NOT)));
  }

  @Test
  public void testIsLiteral() {
    assertTrue(NodeUtil.isLiteral(new Node(Token.NUMBER, 1.0)));
    assertTrue(NodeUtil.isLiteral(new Node(Token.STRING, "a")));
    assertTrue(NodeUtil.isLiteral(new Node(Token.TRUE)));
    assertTrue(NodeUtil.isLiteral(new Node(Token.NULL)));
    assertFalse(NodeUtil.isLiteral(new Node(Token.NAME)));
  }

  @Test
  public void testIsImmutableValue() {
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.NUMBER, 1.0)));
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.STRING, "a")));
    assertFalse(NodeUtil.isImmutableValue(new Node(Token.NAME)));
  }

  @Test
  public void testGetBooleanValueNumber() {
    assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(new Node(Token.NUMBER, 0.0)));
    assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(new Node(Token.NUMBER, -0.0)));
    assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(new Node(Token.NUMBER, Double.NaN)));
    assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(new Node(Token.NUMBER, 1.0)));
    assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(new Node(Token.NUMBER, -1.0)));
  }

  @Test
  public void testGetBooleanValueOther() {
    assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(new Node(Token.STRING, "")));
    assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(new Node(Token.STRING, "x")));
    assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(new Node(Token.FALSE)));
    assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(new Node(Token.TRUE)));
    assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(new Node(Token.NULL)));
    assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(new Node(Token.VOID)));
    assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(new Node(Token.ARRAYLIT)));
    assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(new Node(Token.OBJECTLIT)));
    assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(new Node(Token.REGEXP)));
    assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(newFunction("f")));
  }

  @Test
  public void testGetStringValue() {
    assertEquals("a", NodeUtil.getStringValue(new Node(Token.STRING, "a")));
    assertEquals("1", NodeUtil.getStringValue(new Node(Token.NUMBER, 1.0)));
    assertEquals("true", NodeUtil.getStringValue(new Node(Token.TRUE)));
    assertEquals("null", NodeUtil.getStringValue(new Node(Token.NULL)));
  }

  @Test
  public void testIsExpressionResultUsed() {
    Node exprResult = new Node(Token.EXPR_RESULT);
    Node name1 = new Node(Token.NAME, "a");
    exprResult.addChildToBack(name1);
    assertFalse(NodeUtil.isExpressionResultUsed(name1));

    Node returnNode = new Node(Token.RETURN);
    Node name2 = new Node(Token.NAME, "b");
    returnNode.addChildToBack(name2);
    assertTrue(NodeUtil.isExpressionResultUsed(name2));
  }

  @Test
  public void testIsStatement() {
    Node block = new Node(Token.BLOCK);
    Node exprResult = new Node(Token.EXPR_RESULT);
    block.addChildToBack(exprResult);
    assertTrue(NodeUtil.isStatement(exprResult));

    Node assign = new Node(Token.ASSIGN);
    Node lhs = new Node(Token.NAME, "a");
    Node rhs = new Node(Token.NUMBER, 1.0);
    assign.addChildToBack(lhs);
    assign.addChildToBack(rhs);
    assertFalse(NodeUtil.isStatement(assign));
  }

  @Test
  public void testIsPropertyAccess() {
    assertTrue(NodeUtil.isPropertyAccess(getprop("a", "b")));
    assertFalse(NodeUtil.isPropertyAccess(new Node(Token.NAME, "a")));
  }

  @Test
  public void testIsControlStructure() {
    assertTrue(NodeUtil.isControlStructure(new Node(Token.IF)));
    assertTrue(NodeUtil.isControlStructure(new Node(Token.FOR)));
    assertFalse(NodeUtil.isControlStructure(new Node(Token.NAME)));
  }

  @Test
  public void testIsLoopStructure() {
    assertTrue(NodeUtil.isLoopStructure(new Node(Token.FOR)));
    assertTrue(NodeUtil.isLoopStructure(new Node(Token.WHILE)));
    assertFalse(NodeUtil.isLoopStructure(new Node(Token.IF)));
  }
}