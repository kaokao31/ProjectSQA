package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.*;

/**
 * Comprehensive test suite for NodeUtil.
 * Designed to cover high code coverage and detect faults, including Defects4J Closure-80.
 */
@RunWith(JUnit4.class)
public class NodeUtilTest {

  // Helper methods to create simple AST nodes
  private Node createNumberNode(double value) {
    return Node.newNumber(value);
  }

  private Node createStringNode(String value) {
    return Node.newString(value);
  }

  private Node createBooleanNode(boolean value) {
    return new Node(value ? Token.TRUE : Token.FALSE);
  }

  private Node createNameNode(String name) {
    return Node.newString(Token.NAME, name);
  }

  private Node createEmptyNode() {
    return new Node(Token.EMPTY);
  }

  // ========== isBooleanResult tests ==========

  @Test
  public void testIsBooleanResult_TrueLiteral() {
    Node node = createBooleanNode(true);
    assertTrue("True literal should be boolean result", NodeUtil.isBooleanResult(node));
  }

  @Test
  public void testIsBooleanResult_FalseLiteral() {
    Node node = createBooleanNode(false);
    assertTrue("False literal should be boolean result", NodeUtil.isBooleanResult(node));
  }

  @Test
  public void testIsBooleanResult_NumberLiteral() {
    Node node = createNumberNode(1.0);
    assertFalse("Number literal should not be boolean result", NodeUtil.isBooleanResult(node));
  }

  @Test
  public void testIsBooleanResult_StringLiteral() {
    Node node = createStringNode("test");
    assertFalse("String literal should not be boolean result", NodeUtil.isBooleanResult(node));
  }

  @Test
  public void testIsBooleanResult_NotOperator() {
    Node node = new Node(Token.NOT);
    node.addChildToBack(createBooleanNode(true));
    assertTrue("NOT of boolean should be boolean result", NodeUtil.isBooleanResult(node));
  }

  @Test
  public void testIsBooleanResult_TyOfOperator() {
    Node node = new Node(Token.TYPEOF);
    node.addChildToBack(createNumberNode(1));
    assertTrue("TYPEOF should be boolean result (string actually, but isBooleanResult checks type)");
    // verify actual: typeof returns string, but NodeUtil.isBooleanResult might not handle correctly.
    assertFalse("TYPEOF returns string, not boolean", NodeUtil.isBooleanResult(node));
  }

  @Test
  public void testIsBooleanResult_EqualOperator() {
    Node node = new Node(Token.EQ);
    node.addChildToBack(createNumberNode(1));
    node.addChildToBack(createNumberNode(2));
    assertTrue("EQ comparison should be boolean result", NodeUtil.isBooleanResult(node));
  }

  @Test
  public void testIsBooleanResult_OrOperator() {
    Node node = new Node(Token.OR);
    node.addChildToBack(createBooleanNode(true));
    node.addChildToBack(createBooleanNode(false));
    assertTrue("OR of booleans should be boolean result (but it may return value, still boolean?)", NodeUtil.isBooleanResult(node));
  }

  // Additional edge: AND, BIT_OR (possible bug in Closure-80)
  @Test
  public void testIsBooleanResult_BitOrOperator() {
    Node node = new Node(Token.BIT_OR);
    node.addChildToBack(createNumberNode(1));
    node.addChildToBack(createNumberNode(2));
    // BIT_OR returns integer, not boolean. Bug in Closure-80 existed where it returned true.
    assertFalse("BIT_OR returns number, not boolean", NodeUtil.isBooleanResult(node));
  }

  @Test
  public void testIsBooleanResult_BitAndOperator() {
    Node node = new Node(Token.BIT_AND);
    node.addChildToBack(createNumberNode(1));
    node.addChildToBack(createNumberNode(2));
    assertFalse("BIT_AND returns number, not boolean", NodeUtil.isBooleanResult(node));
  }

  @Test
  public void testIsBooleanResult_NullNode() {
    // NodeUtil should handle null gracefully (throw exception or return false based on implementation)
    // Here we assume it will throw NullPointerException, but we can catch.
    try {
      NodeUtil.isBooleanResult(null);
      fail("Expected NullPointerException for null node");
    } catch (NullPointerException e) {
      // Expected
    }
  }

  // ========== isNumberResult tests ==========

  @Test
  public void testIsNumberResult_NumberLiteral() {
    Node node = createNumberNode(5.5);
    assertTrue("Number literal should be number result", NodeUtil.isNumberResult(node));
  }

  @Test
  public void testIsNumberResult_StringLiteral() {
    Node node = createStringNode("hello");
    assertFalse("String literal should not be number result", NodeUtil.isNumberResult(node));
  }

  @Test
  public void testIsNumberResult_UnaryMinus() {
    Node node = new Node(Token.NEG);
    node.addChildToBack(createNumberNode(3));
    assertTrue("Unary minus of number should be number", NodeUtil.isNumberResult(node));
  }

  @Test
  public void testIsNumberResult_AddOperator() {
    Node node = new Node(Token.ADD);
    node.addChildToBack(createNumberNode(1));
    node.addChildToBack(createNumberNode(2));
    assertTrue("ADD of numbers should be number", NodeUtil.isNumberResult(node));
  }

  @Test
  public void testIsNumberResult_StringConcat() {
    Node node = new Node(Token.ADD);
    node.addChildToBack(createStringNode("a"));
    node.addChildToBack(createNumberNode(1));
    // ADD with string results in string -> not number
    assertFalse("String concatenation should not be number result", NodeUtil.isNumberResult(node));
  }

  @Test
  public void testIsNumberResult_BitOr() {
    Node node = new Node(Token.BIT_OR);
    node.addChildToBack(createNumberNode(1));
    node.addChildToBack(createNumberNode(2));
    assertTrue("BIT_OR yields number", NodeUtil.isNumberResult(node));
  }

  // ========== isStringResult tests ==========

  @Test
  public void testIsStringResult_StringLiteral() {
    Node node = createStringNode("text");
    assertTrue("String literal should be string result", NodeUtil.isStringResult(node));
  }

  @Test
  public void testIsStringResult_TyOfOperator() {
    Node node = new Node(Token.TYPEOF);
    node.addChildToBack(createNumberNode(1));
    assertTrue("TYPEOF returns string", NodeUtil.isStringResult(node));
  }

  @Test
  public void testIsStringResult_NumberLiteral() {
    Node node = createNumberNode(42);
    assertFalse("Number literal should not be string result", NodeUtil.isStringResult(node));
  }

  @Test
  public void testIsStringResult_StringConcat() {
    Node node = new Node(Token.ADD);
    node.addChildToBack(createStringNode("a"));
    node.addChildToBack(createNumberNode(1));
    assertTrue("String concatenation is string result", NodeUtil.isStringResult(node));
  }

  // ========== mayEffectMutableState tests ==========

  @Test
  public void testMayEffectMutableState_NumberLiteral() {
    Node node = createNumberNode(1);
    assertFalse("Number literal does not affect mutable state", NodeUtil.mayEffectMutableState(node, null));
  }

  @Test
  public void testMayEffectMutableState_StringLiteral() {
    Node node = createStringNode("static");
    assertFalse("String literal does not affect mutable state", NodeUtil.mayEffectMutableState(node, null));
  }

  @Test
  public void testMayEffectMutableState_NameNode() {
    Node node = createNameNode("x");
    // Name may have side effects if x is a getter or volatile? Usually false.
    assertFalse("Simple name reference does not affect mutable state", NodeUtil.mayEffectMutableState(node, null));
  }

  @Test
  public void testMayEffectMutableState_Assign() {
    Node node = new Node(Token.ASSIGN);
    node.addChildToBack(createNameNode("a"));
    node.addChildToBack(createNumberNode(5));
    assertTrue("Assignment affects mutable state", NodeUtil.mayEffectMutableState(node, null));
  }

  @Test
  public void testMayEffectMutableState_Call() {
    Node node = new Node(Token.CALL);
    node.addChildToBack(createNameNode("foo"));
    assertTrue("Function call may affect mutable state", NodeUtil.mayEffectMutableState(node, null));
  }

  @Test
  public void testMayEffectMutableState_New() {
    Node node = new Node(Token.NEW);
    node.addChildToBack(createNameNode("Array"));
    assertTrue("Constructor call may affect mutable state", NodeUtil.mayEffectMutableState(node, null));
  }

  // ========== getBooleanValue tests ==========

  @Test
  public void testGetBooleanValue_True() {
    Node node = createBooleanNode(true);
    assertTrue("True node should have boolean value true", NodeUtil.getBooleanValue(node));
  }

  @Test
  public void testGetBooleanValue_False() {
    Node node = createBooleanNode(false);
    assertFalse("False node should have boolean value false", NodeUtil.getBooleanValue(node));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetBooleanValue_InvalidNode() {
    Node node = createNumberNode(0.0);
    NodeUtil.getBooleanValue(node); // Should throw per typical implementation
  }

  // ========== isLiteralOrConstValue tests ==========

  @Test
  public void testIsLiteralOrConstValue_Number() {
    Node node = createNumberNode(10);
    assertTrue("Number is literal", NodeUtil.isLiteralOrConstValue(node, null));
  }

  @Test
  public void testIsLiteralOrConstValue_String() {
    Node node = createStringNode("const");
    assertTrue("String is literal", NodeUtil.isLiteralOrConstValue(node, null));
  }

  @Test
  public void testIsLiteralOrConstValue_Empty() {
    Node node = createEmptyNode();
    assertFalse("Empty node is not literal", NodeUtil.isLiteralOrConstValue(node, null));
  }

  // ========== maybeBoolean tests ==========

  @Test
  public void testMaybeBoolean_True() {
    Node node = createBooleanNode(true);
    assertTrue("True node may be boolean", NodeUtil.maybeBoolean(node));
  }

  @Test
  public void testMaybeBoolean_Number() {
    Node node = createNumberNode(1);
    assertTrue("Number may be boolean (truthy/falsy)", NodeUtil.maybeBoolean(node));
  }

  @Test
  public void testMaybeBoolean_Null() {
    Node node = new Node(Token.NULL);
    assertTrue("Null may be boolean (falsy)", NodeUtil.maybeBoolean(node));
  }

  // Additional edge: VOID
  @Test
  public void testMaybeBoolean_Void() {
    Node node = new Node(Token.VOID);
    node.addChildToBack(createNumberNode(0));
    assertTrue("Void expression may be boolean (undefined -> false)", NodeUtil.maybeBoolean(node));
  }

  // ========== maybeNumber tests ==========

  @Test
  public void testMaybeNumber_Number() {
    Node node = createNumberNode(3.14);
    assertTrue("Number may be number", NodeUtil.maybeNumber(node));
  }

  @Test
  public void testMaybeNumber_StringNumber() {
    Node node = createStringNode("123");
    assertTrue("String containing number may be number", NodeUtil.maybeNumber(node));
  }

  @Test
  public void testMaybeNumber_True() {
    Node node = createBooleanNode(true);
    assertTrue("Boolean may be number (coercion)", NodeUtil.maybeNumber(node));
  }

  // ========== maybeString tests ==========

  @Test
  public void testMaybeString_String() {
    Node node = createStringNode("hello");
    assertTrue("String may be string", NodeUtil.maybeString(node));
  }

  @Test
  public void testMaybeString_Number() {
    Node node = createNumberNode(42);
    assertTrue("Number may be string (toString)", NodeUtil.maybeString(node));
  }

  @Test
  public void testMaybeString_Null() {
    Node node = new Node(Token.NULL);
    assertTrue("Null may be string ('null')", NodeUtil.maybeString(node));
  }

  // Additional tests for completeness
  @Test
  public void testIsExprAssign_SimpleName() {
    Node node = createNameNode("a");
    assertTrue("Simple name is expression assign? Might depend on context, assume false");
    assertFalse(NodeUtil.isExprAssign(node));
  }

  @Test
  public void testIsExprAssign_AssignNode() {
    Node node = new Node(Token.ASSIGN);
    node.addChildToBack(createNameNode("a"));
    node.addChildToBack(createNumberNode(1));
    assertTrue("Assignment should be expression assign", NodeUtil.isExprAssign(node));
  }

  @Test
  public void testGetFunctionBody_FunctionNode() {
    Node function = new Node(Token.FUNCTION);
    Node body = new Node(Token.BLOCK);
    function.addChildToBack(createNameNode("f")); // name
    function.addChildToBack(body); // body
    assertSame("getFunctionBody should return the BLOCK child", body, NodeUtil.getFunctionBody(function));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetFunctionBody_NonFunction() {
    Node node = createNumberNode(1);
    NodeUtil.getFunctionBody(node);
  }

  @Test
  public void testIsFunctionDeclaration_Valid() {
    Node function = new Node(Token.FUNCTION);
    function.addChildToBack(createNameNode("f"));
    function.addChildToBack(new Node(Token.BLOCK));
    // By default, a FUNCTION node is considered a function declaration if it's a statement.
    // But IsFunctionDeclaration checks context like parent.
    // Dummy test to cover method.
    // We'll create a parent EXPR_RESULT to make it an expression.
    Node parent = new Node(Token.EXPR_RESULT);
    parent.addChildToBack(function);
    assertFalse("Function as statement inside EXPR_RESULT is not declaration", NodeUtil.isFunctionDeclaration(function));
  }

  // Edge: null inputs for static methods
  @Test(expected = NullPointerException.class)
  public void testIsBooleanResult_Null_Throws() {
    NodeUtil.isBooleanResult(null);
  }

  @Test(expected = NullPointerException.class)
  public void testIsNumberResult_Null_Throws() {
    NodeUtil.isNumberResult(null);
  }
}