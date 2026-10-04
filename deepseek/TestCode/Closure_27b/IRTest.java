package com.google.javascript.rhino;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class IRTest {

  @Test
  public void testEmpty() {
    Node node = IR.empty();
    assertNotNull(node);
    assertEquals(Token.EMPTY, node.getType());
    assertEquals(0, node.getChildCount());
  }

  @Test
  public void testFunction() {
    Node name = IR.name("f");
    Node params = IR.paramList(IR.name("a"), IR.name("b"));
    Node body = IR.block(IR.returnNode(IR.name("a")));
    Node fn = IR.function(name, params, body);

    assertNotNull(fn);
    assertEquals(Token.FUNCTION, fn.getType());
    assertEquals(3, fn.getChildCount());
    assertSame(name, fn.getFirstChild());
    assertSame(params, fn.getFirstChild().getNext());
    assertSame(body, fn.getLastChild());
  }

  @Test
  public void testFunctionWithEmptyParamList() {
    Node name = IR.name("g");
    Node params = IR.paramList();
    Node body = IR.block();
    Node fn = IR.function(name, params, body);

    assertEquals(Token.FUNCTION, fn.getType());
    assertEquals(3, fn.getChildCount());
    assertEquals(0, params.getChildCount());
  }

  @Test
  public void testParamList() {
    Node a = IR.name("a");
    Node b = IR.name("b");
    Node params = IR.paramList(a, b);

    assertNotNull(params);
    assertEquals(Token.PARAM_LIST, params.getType());
    assertEquals(2, params.getChildCount());
    assertSame(a, params.getFirstChild());
    assertSame(b, params.getLastChild());
  }

  @Test
  public void testEmptyParamList() {
    Node params = IR.paramList();
    assertEquals(Token.PARAM_LIST, params.getType());
    assertEquals(0, params.getChildCount());
  }

  @Test
  public void testName() {
    Node name = IR.name("x");
    assertNotNull(name);
    assertEquals(Token.NAME, name.getType());
    assertEquals("x", name.getString());
  }

  @Test
  public void testString() {
    Node string = IR.string("hello");
    assertNotNull(string);
    assertEquals(Token.STRING, string.getType());
    assertEquals("hello", string.getString());
  }

  @Test
  public void testNumber() {
    Node number = IR.number(42.5);
    assertNotNull(number);
    assertEquals(Token.NUMBER, number.getType());
    assertEquals(42.5, number.getDouble(), 0.0001);
  }

  @Test
  public void testNumberEdgeCases() {
    Node nan = IR.number(Double.NaN);
    assertEquals(Token.NUMBER, nan.getType());
    assertTrue(Double.isNaN(nan.getDouble()));

    Node inf = IR.number(Double.POSITIVE_INFINITY);
    assertEquals(Token.NUMBER, inf.getType());
    assertEquals(Double.POSITIVE_INFINITY, inf.getDouble(), 0.0);

    Node negInf = IR.number(Double.NEGATIVE_INFINITY);
    assertEquals(Token.NUMBER, negInf.getType());
    assertEquals(Double.NEGATIVE_INFINITY, negInf.getDouble(), 0.0);
  }

  @Test
  public void testBooleanNullThis() {
    Node trueNode = IR.trueNode();
    Node falseNode = IR.falseNode();
    Node nullNode = IR.nullNode();
    Node thisNode = IR.thisNode();

    assertNotNull(trueNode);
    assertNotNull(falseNode);
    assertNotNull(nullNode);
    assertNotNull(thisNode);

    assertEquals(Token.TRUE, trueNode.getType());
    assertEquals(Token.FALSE, falseNode.getType());
    assertEquals(Token.NULL, nullNode.getType());
    assertEquals(Token.THIS, thisNode.getType());
  }

  @Test
  public void testArrayLit() {
    Node empty = IR.arraylit();
    assertNotNull(empty);
    assertEquals(Token.ARRAYLIT, empty.getType());
    assertEquals(0, empty.getChildCount());

    Node a = IR.name("a");
    Node b = IR.name("b");
    Node array = IR.arraylit(a, b);
    assertEquals(Token.ARRAYLIT, array.getType());
    assertEquals(2, array.getChildCount());
    assertSame(a, array.getFirstChild());
    assertSame(b, array.getLastChild());
  }

  @Test
  public void testObjectLit() {
    Node empty = IR.objectlit();
    assertNotNull(empty);
    assertEquals(Token.OBJECTLIT, empty.getType());
    assertEquals(0, empty.getChildCount());

    Node key = IR.string("key");
    Node value = IR.number(1);
    Node obj = IR.objectlit(key, value);
    assertEquals(Token.OBJECTLIT, obj.getType());
    assertEquals(2, obj.getChildCount());
    assertSame(key, obj.getFirstChild());
    assertSame(value, obj.getLastChild());
  }

  @Test
  public void testPropDef() {
    Node key = IR.string("key");
    Node value = IR.number(1);
    Node propDef = IR.propdef(key, value);

    assertNotNull(propDef);
    assertEquals(2, propDef.getChildCount());
    assertSame(key, propDef.getFirstChild());
    assertSame(value, propDef.getLastChild());
  }

  @Test
  public void testBlock() {
    Node empty = IR.block();
    assertNotNull(empty);
    assertEquals(Token.BLOCK, empty.getType());
    assertEquals(0, empty.getChildCount());

    Node stmt1 = IR.exprResult(IR.name("a"));
    Node stmt2 = IR.exprResult(IR.name("b"));
    Node block = IR.block(stmt1, stmt2);
    assertEquals(Token.BLOCK, block.getType());
    assertEquals(2, block.getChildCount());
    assertSame(stmt1, block.getFirstChild());
    assertSame(stmt2, block.getLastChild());
  }

  @Test
  public void testScript() {
    Node empty = IR.script();
    assertNotNull(empty);
    assertEquals(Token.SCRIPT, empty.getType());
    assertEquals(0, empty.getChildCount());

    Node stmt = IR.exprResult(IR.number(1));
    Node script = IR.script(stmt);
    assertEquals(Token.SCRIPT, script.getType());
    assertEquals(1, script.getChildCount());
    assertSame(stmt, script.getFirstChild());
  }

  @Test
  public void testVar() {
    Node target = IR.name("x");
    Node value = IR.number(1);
    Node var = IR.var(target, value);

    assertNotNull(var);
    assertEquals(Token.VAR, var.getType());
    assertEquals(2, var.getChildCount());
    assertSame(target, var.getFirstChild());
    assertSame(value, var.getLastChild());
  }

  @Test
  public void testVarWithoutValue() {
    Node target = IR.name("x");
    Node var = IR.var(target, null);

    assertNotNull(var);
    assertEquals(Token.VAR, var.getType());
    assertEquals(1, var.getChildCount());
    assertSame(target, var.getFirstChild());
  }

  @Test
  public void testReturn() {
    Node emptyReturn = IR.returnNode();
    assertNotNull(emptyReturn);
    assertEquals(Token.RETURN, emptyReturn.getType());
    assertEquals(0, emptyReturn.getChildCount());

    Node expr = IR.name("x");
    Node returnNode = IR.returnNode(expr);
    assertEquals(Token.RETURN, returnNode.getType());
    assertEquals(1, returnNode.getChildCount());
    assertSame(expr, returnNode.getFirstChild());
  }

  @Test
  public void testThrow() {
    Node expr = IR.name("error");
    Node throwNode = IR.throwNode(expr);

    assertNotNull(throwNode);
    assertEquals(Token.THROW, throwNode.getType());
    assertEquals(1, throwNode.getChildCount());
    assertSame(expr, throwNode.getFirstChild());
  }

  @Test
  public void testIfWithElse() {
    Node cond = IR.name("c");
    Node thenBlock = IR.block();
    Node elseBlock = IR.block();
    Node ifNode = IR.ifNode(cond, thenBlock, elseBlock);

    assertNotNull(ifNode);
    assertEquals(Token.IF, ifNode.getType());
    assertEquals(3, ifNode.getChildCount());
    assertSame(cond, ifNode.getFirstChild());
    assertSame(thenBlock, ifNode.getFirstChild().getNext());
    assertSame(elseBlock, ifNode.getLastChild());
  }

  @Test
  public void testIfWithoutElse() {
    Node cond = IR.name("c");
    Node thenBlock = IR.block();
    Node ifNode = IR.ifNode(cond, thenBlock);

    assertNotNull(ifNode);
    assertEquals(Token.IF, ifNode.getType());
    assertEquals(2, ifNode.getChildCount());
    assertSame(cond, ifNode.getFirstChild());
    assertSame(thenBlock, ifNode.getLastChild());
  }

  @Test
  public void testWhile() {
    Node cond = IR.name("c");
    Node body = IR.block();
    Node whileNode = IR.whileNode(cond, body);

    assertNotNull(whileNode);
    assertEquals(Token.WHILE, whileNode.getType());
    assertEquals(2, whileNode.getChildCount());
    assertSame(cond, whileNode.getFirstChild());
    assertSame(body, whileNode.getLastChild());
  }

  @Test
  public void testDo() {
    Node body = IR.block();
    Node cond = IR.name("c");
    Node doNode = IR.doNode(body, cond);

    assertNotNull(doNode);
    assertEquals(Token.DO, doNode.getType());
    assertEquals(2, doNode.getChildCount());
    assertSame(body, doNode.getFirstChild());
    assertSame(cond, doNode.getLastChild());
  }

  @Test
  public void testFor() {
    Node init = IR.var(IR.name("i"), IR.number(0));
    Node cond = IR.name("i");
    Node incr = IR.name("i");
    Node body = IR.block();
    Node forNode = IR.forNode(init, cond, incr, body);

    assertNotNull(forNode);
    assertEquals(Token.FOR, forNode.getType());
    assertEquals(4, forNode.getChildCount());
    assertSame(init, forNode.getFirstChild());
    assertSame(cond, forNode.getFirstChild().getNext());
    assertSame(incr, forNode.getFirstChild().getNext().getNext());
    assertSame(body, forNode.getLastChild());
  }

  @Test
  public void testAssign() {
    Node target = IR.name("a");
    Node value = IR.name("b");
    Node assign = IR.assign(target, value);

    assertNotNull(assign);
    assertEquals(Token.ASSIGN, assign.getType());
    assertEquals(2, assign.getChildCount());
    assertSame(target, assign.getFirstChild());
    assertSame(value, assign.getLastChild());
  }

  @Test
  public void testExprResult() {
    Node expr = IR.call(IR.name("f"));
    Node exprResult = IR.exprResult(expr);

    assertNotNull(exprResult);
    assertEquals(Token.EXPR_RESULT, exprResult.getType());
    assertEquals(1, exprResult.getChildCount());
    assertSame(expr, exprResult.getFirstChild());
  }

  @Test
  public void testCallNoArgs() {
    Node target = IR.name("f");
    Node call = IR.call(target);

    assertNotNull(call);
    assertEquals(Token.CALL, call.getType());
    assertEquals(1, call.getChildCount());
    assertSame(target, call.getFirstChild());
  }

  @Test
  public void testCallWithArgs() {
    Node target = IR.name("f");
    Node arg1 = IR.name("a");
    Node arg2 = IR.name("b");
    Node call = IR.call(target, arg1, arg2);

    assertNotNull(call);
    assertEquals(Token.CALL, call.getType());
    assertEquals(3, call.getChildCount());
    assertSame(target, call.getFirstChild());
    assertSame(arg1, call.getFirstChild().getNext());
    assertSame(arg2, call.getLastChild());
  }

  @Test
  public void testNewNoArgs() {
    Node target = IR.name("F");
    Node newNode = IR.newNode(target);

    assertNotNull(newNode);
    assertEquals(Token.NEW, newNode.getType());
    assertEquals(1, newNode.getChildCount());
    assertSame(target, newNode.getFirstChild());
  }

  @Test
  public void testNewWithArgs() {
    Node target = IR.name("F");
    Node arg1 = IR.name("a");
    Node arg2 = IR.name("b");
    Node newNode = IR.newNode(target, arg1, arg2);

    assertNotNull(newNode);
    assertEquals(Token.NEW, newNode.getType());
    assertEquals(3, newNode.getChildCount());
    assertSame(target, newNode.getFirstChild());
    assertSame(arg1, newNode.getFirstChild().getNext());
    assertSame(arg2, newNode.getLastChild());
  }

  @Test
  public void testGetProp() {
    Node target = IR.name("obj");
    Node prop = IR.string("x");
    Node getProp = IR.getprop(target, prop);

    assertNotNull(getProp);
    assertEquals(Token.GETPROP, getProp.getType());
    assertEquals(2, getProp.getChildCount());
    assertSame(target, getProp.getFirstChild());
    assertSame(prop, getProp.getLastChild());
  }

  @Test
  public void testGetElem() {
    Node target = IR.name("arr");
    Node elem = IR.name("i");
    Node getElem = IR.getelem(target, elem);

    assertNotNull(getElem);
    assertEquals(Token.GETELEM, getElem.getType());
    assertEquals(2, getElem.getChildCount());
    assertSame(target, getElem.getFirstChild());
    assertSame(elem, getElem.getLastChild());
  }

  @Test
  public void testComma() {
    Node first = IR.name("a");
    Node second = IR.name("b");
    Node comma = IR.comma(first, second);

    assertNotNull(comma);
    assertEquals(Token.COMMA, comma.getType());
    assertEquals(2, comma.getChildCount());
    assertSame(first, comma.getFirstChild());
    assertSame(second, comma.getLastChild());
  }
}