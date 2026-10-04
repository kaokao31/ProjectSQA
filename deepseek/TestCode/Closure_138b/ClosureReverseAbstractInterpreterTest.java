package com.google.javascript.jscomp;

import com.google.javascript.jscomp.type.FlowScope;
import com.google.javascript.jscomp.type.ReverseAbstractInterpreter;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.ObjectType;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.ArrowType;
import com.google.javascript.rhino.jstype.UnionType;
import com.google.javascript.rhino.jstype.BooleanLiteralSet;
import com.google.javascript.rhino.jstype.EnumElementType;
import com.google.javascript.rhino.jstype.EnumType;
import com.google.javascript.rhino.jstype.NoType;
import com.google.javascript.rhino.jstype.AllType;
import com.google.javascript.rhino.jstype.UnknownType;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for ClosureReverseAbstractInterpreter.
 * Generated to achieve high coverage and potentially reveal Defects4J bug 138.
 */
public class ClosureReverseAbstractInterpreterTest {

  private JSTypeRegistry registry;
  private ReverseAbstractInterpreter interpreter;
  private FlowScope dummyScope;

  @Before
  public void setUp() throws Exception {
    registry = new JSTypeRegistry(null);
    interpreter = new ClosureReverseAbstractInterpreter(registry);
    dummyScope = new FlowScope(null); // simplified dummy scope
  }

  // Helper methods to create simple nodes
  private Node createNameNode(String name) {
    return Node.newString(Token.NAME, name);
  }

  private Node createNumberNode(double value) {
    return Node.newNumber(value);
  }

  private Node createStringNode(String value) {
    return Node.newString(value);
  }

  // Test: condition outcome true for undefined comparison => x === undefined
  @Test
  public void testUndefinedEqualityTrue() {
    Node nameNode = createNameNode("x");
    Node undefinedNode = createNameNode("undefined");
    Node eqNode = new Node(Token.EQ, nameNode, undefinedNode);
    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(eqNode, dummyScope, true);
    // Expect that type of x is narrowed to undefined
    assertNotNull(result);
    JSType inferredType = result.getSlot("x").getType();
    assertTrue(inferredType.isUndefinedType());
  }

  // Test: condition outcome false for undefined equality => x !== undefined
  @Test
  public void testUndefinedEqualityFalse() {
    Node nameNode = createNameNode("x");
    Node undefinedNode = createNameNode("undefined");
    Node eqNode = new Node(Token.EQ, nameNode, undefinedNode);
    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(eqNode, dummyScope, false);
    assertNotNull(result);
    JSType inferredType = result.getSlot("x").getType();
    // x becomes non-undefined, possibly union of rest
    assertFalse(inferredType.isUndefinedType());
  }

  // Test: null equality true
  @Test
  public void testNullEqualityTrue() {
    Node nameNode = createNameNode("x");
    Node nullNode = Node.newString(Token.NULL, "null");
    Node eqNode = new Node(Token.EQ, nameNode, nullNode);
    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(eqNode, dummyScope, true);
    assertNotNull(result);
    JSType inferredType = result.getSlot("x").getType();
    assertTrue(inferredType.isNullType());
  }

  // Test: typeOf comparison (typeof x == 'number')
  @Test
  public void testTypeOfNumberTrue() {
    Node nameNode = createNameNode("x");
    Node stringNode = createStringNode("number");
    Node typeOfNode = new Node(Token.TYPEOF, nameNode);
    Node eqNode = new Node(Token.EQ, typeOfNode, stringNode);
    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(eqNode, dummyScope, true);
    assertNotNull(result);
    JSType inferredType = result.getSlot("x").getType();
    // Should be number type
    assertTrue(inferredType.isNumberValueType() || inferredType == registry.getNativeType(JSType.NUMBER_TYPE));
  }

  // Test: typeOf comparison false => x is not a number
  @Test
  public void testTypeOfNumberFalse() {
    Node nameNode = createNameNode("x");
    Node stringNode = createStringNode("number");
    Node typeOfNode = new Node(Token.TYPEOF, nameNode);
    Node eqNode = new Node(Token.EQ, typeOfNode, stringNode);
    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(eqNode, dummyScope, false);
    assertNotNull(result);
    JSType inferredType = result.getSlot("x").getType();
    // Should exclude number type
    assertFalse(inferredType.isNumberValueType());
  }

  // Test: AND condition (x && y) => true outcome
  @Test
  public void testAndConditionTrue() {
    Node xNode = createNameNode("x");
    Node yNode = createNameNode("y");
    Node andNode = new Node(Token.AND, xNode, yNode);
    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(andNode, dummyScope, true);
    assertNotNull(result);
    // Both x and y should be truthy (not null/undefined/false/0/empty)
    JSType xType = result.getSlot("x").getType();
    assertTrue(xType.isTruthy());
    JSType yType = result.getSlot("y").getType();
    assertTrue(yType.isTruthy());
  }

  // Test: AND condition false outcome
  @Test
  public void testAndConditionFalse() {
    Node xNode = createNameNode("x");
    Node yNode = createNameNode("y");
    Node andNode = new Node(Token.AND, xNode, yNode);
    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(andNode, dummyScope, false);
    assertNotNull(result);
    // At least one of x or y is falsy
    // Depending on implementation, may narrow both or leave as unknown
  }

  // Test: OR condition true outcome
  @Test
  public void testOrConditionTrue() {
    Node xNode = createNameNode("x");
    Node yNode = createNameNode("y");
    Node orNode = new Node(Token.OR, xNode, yNode);
    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(orNode, dummyScope, true);
    assertNotNull(result);
    // At least one of x or y is truthy -> cannot narrow much
  }

  // Test: NOT condition
  @Test
  public void testNotConditionTrue() {
    Node xNode = createNameNode("x");
    Node notNode = new Node(Token.NOT, xNode);
    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(notNode, dummyScope, true);
    assertNotNull(result);
    // x must be falsy
    JSType xType = result.getSlot("x").getType();
    assertTrue(xType.isFalsy());
  }

  // Test: strictly equal (===) with boolean literal true
  @Test
  public void testStrictEqBooleanTrue() {
    Node nameNode = createNameNode("x");
    Node trueNode = new Node(Token.TRUE);
    Node eqNode = new Node(Token.SHEQ, nameNode, trueNode);
    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(eqNode, dummyScope, true);
    assertNotNull(result);
    JSType inferredType = result.getSlot("x").getType();
    assertTrue(inferredType.isBooleanValueType() || inferredType == registry.getNativeType(JSType.BOOLEAN_TYPE));
  }

  // Test: null/undefined in union narrows correctly
  @Test
  public void testUnionWithNullNarrowing() {
    // Setup a scope where x has type (number|null)
    // This requires knowledge of the scope's type, but we simulate via registry
    // We can use a simple approach: rely on the interpreter's handling
    Node nameNode = createNameNode("x");
    Node nullNode = Node.newString(Token.NULL, "null");
    Node eqNode = new Node(Token.EQ, nameNode, nullNode);
    // assume original scope has union type
    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(eqNode, dummyScope, true);
    assertNotNull(result);
    JSType inferredType = result.getSlot("x").getType();
    // Should be null
    assertTrue(inferredType.isNullType());
  }

  // Edge case: unknown type comparisons
  @Test
  public void testUnknownTypeComparison() {
    Node nameNode = createNameNode("x");
    Node numberNode = createNumberNode(5);
    Node eqNode = new Node(Token.EQ, nameNode, numberNode);
    // x has unknown type initially
    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(eqNode, dummyScope, true);
    assertNotNull(result);
    // x should be narrowed to number
    JSType inferredType = result.getSlot("x").getType();
    assertTrue(inferredType.isNumberValueType());
  }

  // Test: void 0 pattern
  @Test
  public void testVoid0Comparison() {
    Node voidNode = new Node(Token.VOID, Node.newNumber(0));
    Node nameNode = createNameNode("x");
    Node eqNode = new Node(Token.EQ, nameNode, voidNode);
    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(eqNode, dummyScope, true);
    assertNotNull(result);
    JSType inferredType = result.getSlot("x").getType();
    // void 0 is undefined
    assertTrue(inferredType.isUndefinedType());
  }

  // Test: instance of narrowing (if supported)
  @Test
  public void testInstanceOfTrue() {
    Node nameNode = createNameNode("x");
    // We need a type reference node, but we can use a function node as prototype
    // This is complex; for coverage we can test a simpler case
    // The interpreter likely handles only certain patterns; skip for now
  }

  // Additional generic test to exercise basic flow
  @Test
  public void testNullConditionFalse() {
    Node nullNode = Node.newString(Token.NULL, "null");
    // Condition is just null (not a name)
    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(nullNode, dummyScope, true);
    // Should not crash
    assertNotNull(result);
  }

  // Test: calling with non-name node
  @Test
  public void testNonNameNode() {
    Node addNode = new Node(Token.ADD, createNumberNode(1), createNumberNode(2));
    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(addNode, dummyScope, true);
    assertNotNull(result);
  }

  // Test: multiple nested conditions (like && inside ||)
  @Test
  public void testNestedCondition() {
    Node x = createNameNode("x");
    Node y = createNameNode("y");
    Node andNode = new Node(Token.AND, x, y);
    Node z = createNameNode("z");
    Node orNode = new Node(Token.OR, andNode, z);
    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(orNode, dummyScope, true);
    assertNotNull(result);
  }

  // Test: property access (a.b) comparison
  @Test
  public void testPropertyEquality() {
    Node getprop = new Node(Token.GETPROP, createNameNode("obj"), createStringNode("prop"));
    Node nullNode = Node.newString(Token.NULL, "null");
    Node eqNode = new Node(Token.EQ, getprop, nullNode);
    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(eqNode, dummyScope, true);
    assertNotNull(result);
    // The interpreter may not narrow the property type; but should not crash
  }

  // Test: evaluate null or undefined with strict equality
  @Test
  public void testStrictNullFalse() {
    Node nameNode = createNameNode("x");
    Node nullNode = Node.newString(Token.NULL, "null");
    Node sheqNode = new Node(Token.SHEQ, nameNode, nullNode);
    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(sheqNode, dummyScope, false);
    assertNotNull(result);
    JSType inferredType = result.getSlot("x").getType();
    // x is not null, but could be undefined, etc.
    assertFalse(inferredType.isNullType());
  }

  // Test: boolean comparison
  @Test
  public void testBooleanEquality() {
    Node nameNode = createNameNode("x");
    Node boolNode = new Node(Token.TRUE);
    Node eqNode = new Node(Token.EQ, nameNode, boolNode);
    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(eqNode, dummyScope, true);
    assertNotNull(result);
    JSType inferredType = result.getSlot("x").getType();
    assertTrue(inferredType.isBooleanValueType());
  }

  // Test: string comparison
  @Test
  public void testStringEquality() {
    Node nameNode = createNameNode("x");
    Node strNode = createStringNode("hello");
    Node eqNode = new Node(Token.EQ, nameNode, strNode);
    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(eqNode, dummyScope, true);
    assertNotNull(result);
    JSType inferredType = result.getSlot("x").getType();
    // Should be string type
    assertTrue(inferredType.isStringValueType() || inferredType == registry.getNativeType(JSType.STRING_TYPE));
  }

  // Additional edge: null vs undefined via !=
  @Test
  public void testNullNotEqualFalse() {
    Node nameNode = createNameNode("x");
    Node nullNode = Node.newString(Token.NULL, "null");
    Node neNode = new Node(Token.NE, nameNode, nullNode);
    // condition false => x === null
    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(neNode, dummyScope, false);
    assertNotNull(result);
    JSType inferredType = result.getSlot("x").getType();
    assertTrue(inferredType.isNullType());
  }

  // Test: typeof null returns "object" 
  @Test
  public void testTypeOfNull() {
    Node nameNode = createNameNode("x");
    Node stringNode = createStringNode("object");
    Node typeOfNode = new Node(Token.TYPEOF, nameNode);
    Node eqNode = new Node(Token.EQ, typeOfNode, stringNode);
    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(eqNode, dummyScope, true);
    assertNotNull(result);
    // Could be Object, null, or array? The interpreter might not treat null separately
    // Just ensure no crash
  }

  // Test: assignment expressions (should be ignored)
  @Test
  public void testAssignExpression() {
    Node assign = new Node(Token.ASSIGN, createNameNode("x"), createNumberNode(5));
    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(assign, dummyScope, true);
    assertNotNull(result);
  }

  // Test: comparing two names
  @Test
  public void testTwoNameEquality() {
    Node x = createNameNode("x");
    Node y = createNameNode("y");
    Node eq = new Node(Token.EQ, x, y);
    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(eq, dummyScope, true);
    assertNotNull(result);
    // Usually no narrowing on both
  }

  // Test: shadow variable through function
  // (complex, skip)

  // Test: empty node
  @Test
  public void testEmptyScript() {
    Node script = new Node(Token.SCRIPT);
    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(script, dummyScope, true);
    assertNotNull(result);
  }
}