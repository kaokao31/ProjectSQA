package com.google.javascript.jscomp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.javascript.jscomp.NodeUtil;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

/**
 * Comprehensive unit tests for NodeUtil covering defect detection and high code coverage.
 * Designed specifically for bug #60 in the Closure Compiler Defects4J dataset.
 */
public class NodeUtilTest {

  private Node nameNode;
  private Node numberNode;
  private Node stringNode;
  private Node functionNode;
  private Node callNode;
  private Node getpropNode;
  private Node getelemNode;

  @Before
  public void setUp() {
    nameNode = new Node(Token.NAME, "x");
    numberNode = Node.newNumber(42);
    stringNode = Node.newString("hello");
    functionNode = new Node(Token.FUNCTION, new Node(Token.PARAM_LIST), new Node(Token.BLOCK));
    callNode = new Node(Token.CALL, nameNode);
    getpropNode = new Node(Token.GETPROP, nameNode, Node.newString("prop"));
    getelemNode = new Node(Token.GETELEM, nameNode, numberNode);
  }

  @Test
  public void testIsPropertyAssignment() {
    // Test for GETPROP (the bug fix for issue 60)
    Node assignGetprop = new Node(Token.ASSIGN, getpropNode, numberNode);
    assertTrue("GETPROP assignment should be recognized", NodeUtil.isPropertyAssignment(assignGetprop));

    // Test for GETELEM
    Node assignGetelem = new Node(Token.ASSIGN, getelemNode, stringNode);
    assertTrue("GETELEM assignment should be recognized", NodeUtil.isPropertyAssignment(assignGetelem));

    // Test for non-property assignments
    Node assignName = new Node(Token.ASSIGN, nameNode, stringNode);
    assertFalse("Simple name assignment is not a property assignment", NodeUtil.isPropertyAssignment(assignName));

    // Test for non-assignment nodes
    assertFalse("CALL is not a property assignment", NodeUtil.isPropertyAssignment(callNode));
    assertFalse("FUNCTION is not a property assignment", NodeUtil.isPropertyAssignment(functionNode));
  }

  @Test
  public void testIsSimpleOperator() {
    assertTrue(NodeUtil.isSimpleOperator(new Node(Token.ADD)));
    assertTrue(NodeUtil.isSimpleOperator(new Node(Token.SUB)));
    assertTrue(NodeUtil.isSimpleOperator(new Node(Token.MUL)));
    assertFalse(NodeUtil.isSimpleOperator(new Node(Token.ASSIGN)));
    assertFalse(NodeUtil.isSimpleOperator(new Node(Token.CALL)));
  }

  @Test
  public void testIsFunction() {
    assertTrue(NodeUtil.isFunction(functionNode));
    assertFalse(NodeUtil.isFunction(nameNode));
    assertFalse(NodeUtil.isFunction(numberNode));
  }

  @Test
  public void testIsName() {
    assertTrue(NodeUtil.isName(nameNode));
    assertFalse(NodeUtil.isName(numberNode));
    assertFalse(NodeUtil.isName(getpropNode));
  }

  @Test
  public void testIsLiteral() {
    assertTrue(NodeUtil.isLiteral(numberNode));
    assertTrue(NodeUtil.isLiteral(stringNode));
    assertTrue(NodeUtil.isLiteral(new Node(Token.TRUE)));
    assertTrue(NodeUtil.isLiteral(new Node(Token.FALSE)));
    assertTrue(NodeUtil.isLiteral(new Node(Token.NULL)));
    assertFalse(NodeUtil.isLiteral(nameNode));
    assertFalse(NodeUtil.isLiteral(functionNode));
  }

  @Test
  public void testIsImmutableValue() {
    assertTrue(NodeUtil.isImmutableValue(numberNode));
    assertTrue(NodeUtil.isImmutableValue(stringNode));
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.TRUE)));
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.FALSE)));
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.NULL)));
    assertFalse(NodeUtil.isImmutableValue(nameNode));
    assertFalse(NodeUtil.isImmutableValue(functionNode));
  }

  @Test
  public void testMayHaveSideEffects() {
    // Simple name has no side effects
    assertFalse(NodeUtil.mayHaveSideEffects(nameNode));

    // Simple number has no side effects
    assertFalse(NodeUtil.mayHaveSideEffects(numberNode));

    // A call may have side effects
    assertTrue(NodeUtil.mayHaveSideEffects(callNode));

    // Assignment may have side effects
    Node assign = new Node(Token.ASSIGN, nameNode, numberNode);
    assertTrue(NodeUtil.mayHaveSideEffects(assign));

    // GETPROP alone has no side effects
    assertFalse(NodeUtil.mayHaveSideEffects(getpropNode));
  }

  @Test
  public void testGetBooleanValue() {
    assertEquals(Boolean.TRUE, NodeUtil.getBooleanValue(new Node(Token.TRUE)));
    assertEquals(Boolean.FALSE, NodeUtil.getBooleanValue(new Node(Token.FALSE)));
    assertEquals(Boolean.TRUE, NodeUtil.getBooleanValue(stringNode));
    assertEquals(Boolean.FALSE, NodeUtil.getBooleanValue(Node.newNumber(0)));
    assertEquals(Boolean.TRUE, NodeUtil.getBooleanValue(Node.newNumber(1)));
    assertEquals(Boolean.FALSE, NodeUtil.getBooleanValue(new Node(Token.NULL)));
    // For non-literal nodes, should return null
    assertEquals(null, NodeUtil.getBooleanValue(nameNode));
  }

  @Test
  public void testIsAssignmentOp() {
    assertTrue(NodeUtil.isAssignmentOp(new Node(Token.ASSIGN)));
    assertTrue(NodeUtil.isAssignmentOp(new Node(Token.ASSIGN_BITOR)));
    assertTrue(NodeUtil.isAssignmentOp(new Node(Token.ASSIGN_BITXOR)));
    assertTrue(NodeUtil.isAssignmentOp(new Node(Token.ASSIGN_BITAND)));
    assertTrue(NodeUtil.isAssignmentOp(new Node(Token.ASSIGN_LSH)));
    assertTrue(NodeUtil.isAssignmentOp(new Node(Token.ASSIGN_RSH)));
    assertTrue(NodeUtil.isAssignmentOp(new Node(Token.ASSIGN_URSH)));
    assertTrue(NodeUtil.isAssignmentOp(new Node(Token.ASSIGN_ADD)));
    assertTrue(NodeUtil.isAssignmentOp(new Node(Token.ASSIGN_SUB)));
    assertTrue(NodeUtil.isAssignmentOp(new Node(Token.ASSIGN_MUL)));
    assertTrue(NodeUtil.isAssignmentOp(new Node(Token.ASSIGN_DIV)));
    assertTrue(NodeUtil.isAssignmentOp(new Node(Token.ASSIGN_MOD)));
    assertFalse(NodeUtil.isAssignmentOp(new Node(Token.CALL)));
    assertFalse(NodeUtil.isAssignmentOp(new Node(Token.ADD)));
  }

  @Test
  public void testIsExprCall() {
    Node exprResult = new Node(Token.EXPR_RESULT, callNode);
    assertTrue(NodeUtil.isExprCall(exprResult));
    Node nonExpr = new Node(Token.BLOCK);
    assertFalse(NodeUtil.isExprCall(nonExpr));
  }

  @Test
  public void testIsControlStructure() {
    assertTrue(NodeUtil.isControlStructure(new Node(Token.IF)));
    assertTrue(NodeUtil.isControlStructure(new Node(Token.WHILE)));
    assertTrue(NodeUtil.isControlStructure(new Node(Token.FOR)));
    assertTrue(NodeUtil.isControlStructure(new Node(Token.SWITCH)));
    assertTrue(NodeUtil.isControlStructure(new Node(Token.TRY)));
    assertFalse(NodeUtil.isControlStructure(new Node(Token.BLOCK)));
    assertFalse(NodeUtil.isControlStructure(new Node(Token.CALL)));
  }

  @Test
  public void testIsStatementParent() {
    assertTrue(NodeUtil.isStatementParent(new Node(Token.SCRIPT)));
    assertTrue(NodeUtil.isStatementParent(new Node(Token.ROOT)));
    assertTrue(NodeUtil.isStatementParent(new Node(Token.BLOCK)));
    assertFalse(NodeUtil.isStatementParent(new Node(Token.IF)));
    assertFalse(NodeUtil.isStatementParent(new Node(Token.FUNCTION)));
  }

  @Test
  public void testIsReference() {
    // NAME and GETPROP and GETELEM are potential references
    assertTrue(NodeUtil.isReference(nameNode));
    assertTrue(NodeUtil.isReference(getpropNode));
    assertTrue(NodeUtil.isReference(getelemNode));
    assertFalse(NodeUtil.isReference(numberNode));
    assertFalse(NodeUtil.isReference(stringNode));
    assertFalse(NodeUtil.isReference(new Node(Token.THIS)));
  }

  @Test
  public void testContainsCall() {
    Node blockWithCall = new Node(Token.BLOCK, callNode);
    assertTrue(NodeUtil.containsCall(blockWithCall));
    Node blockWithoutCall = new Node(Token.BLOCK, nameNode);
    assertFalse(NodeUtil.containsCall(blockWithoutCall));
    // Test nested
    Node nested = new Node(Token.BLOCK, new Node(Token.IF, nameNode, blockWithCall));
    assertTrue(NodeUtil.containsCall(nested));
  }

  @Test
  public void testAny() {
    // The bug in issue 60 might also affect NodeUtil.any method
    // Test that any() correctly iterates over children
    Node parent = new Node(Token.BLOCK, nameNode, numberNode);
    boolean foundNumber = NodeUtil.any(parent, new NodeUtil.Predicate() {
      @Override
      public boolean apply(Node n) {
        return n.getType() == Token.NUMBER;
      }
    });
    assertTrue(foundNumber);

    boolean foundString = NodeUtil.any(parent, new NodeUtil.Predicate() {
      @Override
      public boolean apply(Node n) {
        return n.getType() == Token.STRING;
      }
    });
    assertFalse(foundString);

    // Test with null predicate: should return false, no exception
    assertFalse(NodeUtil.any(parent, null));

    // Test with empty parent
    Node empty = new Node(Token.BLOCK);
    assertFalse(NodeUtil.any(empty, new NodeUtil.Predicate() {
      @Override
      public boolean apply(Node n) {
        return true;
      }
    }));
  }

  @Test
  public void testAll() {
    Node parent = new Node(Token.BLOCK, nameNode, nameNode);
    boolean allNames = NodeUtil.all(parent, new NodeUtil.Predicate() {
      @Override
      public boolean apply(Node n) {
        return n.getType() == Token.NAME;
      }
    });
    assertTrue(allNames);

    parent.addChildToBack(numberNode);
    allNames = NodeUtil.all(parent, new NodeUtil.Predicate() {
      @Override
      public boolean apply(Node n) {
        return n.getType() == Token.NAME;
      }
    });
    assertFalse(allNames);
  }

  @Test
  public void testBestVarForUndefNode() {
    // Edge case: null node should return null
    Node result = NodeUtil.bestVarForUndefNode(null);
    assertEquals(null, result);
  }
}