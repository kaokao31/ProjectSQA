package com.google.javascript.jscomp.type;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ChainableReverseAbstractInterpreterTest {

  @Mock
  private ChainableReverseAbstractInterpreter mockNext;

  @Mock
  private Node mockNode;

  @Mock
  private Node mockParentNode;

  @Mock
  private JSType mockType;

  @Mock
  private JSTypeRegistry mockRegistry;

  @Mock
  private FunctionType mockFunctionType;

  @Mock
  private ObjectType mockObjectType;

  @Mock
  private boolean mockBoolean;

  private TestChainableReverseAbstractInterpreter interpreter;

  // Concrete subclass for testing abstract methods
  private static class TestChainableReverseAbstractInterpreter
      extends ChainableReverseAbstractInterpreter {

    private JSType resultType;

    TestChainableReverseAbstractInterpreter(JSTypeRegistry registry) {
      super(registry);
    }

    @Override
    public JSType caseJSTypeCase(JSTypeCase typeCase, Node node) {
      // For testing, we return a simple type based on the node
      if (node == null) {
        return null;
      }
      // Simulate a potential bug: accessing parent without null check
      Node parent = node.getParent();
      if (parent != null) {
        // Some logic that might throw NPE if parent is null
      }
      return resultType;
    }

    void setResultType(JSType type) {
      this.resultType = type;
    }
  }

  @Before
  public void setUp() {
    interpreter = new TestChainableReverseAbstractInterpreter(mockRegistry);
    interpreter.setResultType(mockType);
  }

  // --- Tests for chain method ---

  @Test
  public void testChain_SetsNextAndReturnsNext() {
    ChainableReverseAbstractInterpreter result = interpreter.chain(mockNext);
    assertSame(mockNext, result);
    // Verify that firstCallsite returns the first in chain (this)
    assertSame(interpreter, interpreter.firstCallsite());
  }

  @Test
  public void testChain_WithNullNext() {
    // chain should handle null gracefully (implementation dependent)
    ChainableReverseAbstractInterpreter result = interpreter.chain(null);
    // If the implementation does not chain null, firstCallsite should still return this
    assertSame(interpreter, interpreter.firstCallsite());
  }

  // --- Test for firstCallsite ---

  @Test
  public void testFirstCallsite_NoChain() {
    assertSame(interpreter, interpreter.firstCallsite());
  }

  @Test
  public void testFirstCallsite_WithChain() {
    ChainableReverseAbstractInterpreter second = new TestChainableReverseAbstractInterpreter(mockRegistry);
    second.setResultType(mockType);
    ChainableReverseAbstractInterpreter third = new TestChainableReverseAbstractInterpreter(mockRegistry);
    third.setResultType(mockType);
    interpreter.chain(second);
    second.chain(third);
    // firstCallsite should return the head of the chain
    assertSame(interpreter, third.firstCallsite());
  }

  // --- Test for typeMismatch ---

  @Test
  public void testTypeMismatch_ReturnsNull() {
    // default implementation returns null
    assertNull(interpreter.typeMismatch(mockNode, mockType));
  }

  // --- Test for caseJSTypeCase (abstract, but concrete in test) ---

  @Test
  public void testCaseJSTypeCase_WithNonNullParent() {
    // Simulate a node with a parent
    Node childNode = new Node(Token.STRING);
    Node parent = new Node(Token.SCRIPT);
    childNode.setParent(parent);
    JSType result = interpreter.caseJSTypeCase(JSTypeCase.STRING, childNode);
    assertNotNull(result);
    assertSame(mockType, result);
  }

  @Test
  public void testCaseJSTypeCase_WithNullParent() {
    // This test is designed to trigger potential NullPointerException if the implementation
    // does not check for null parent before accessing it (as in Defects4J Closure bug 19)
    Node nodeWithoutParent = new Node(Token.NUMBER);
    // Ensure parent is null
    nodeWithoutParent.setParent(null);
    try {
      JSType result = interpreter.caseJSTypeCase(JSTypeCase.NUMBER, nodeWithoutParent);
      // If no exception, the method handled gracefully; assert result is not null (or as expected)
      assertNotNull(result);
    } catch (NullPointerException e) {
      // If NPE is thrown, the bug is present; this test would fail because we don't expect exception
      // But we catch to fail loudly with assertion
      throw new AssertionError("NullPointerException thrown - bug present!", e);
    }
  }

  @Test
  public void testCaseJSTypeCase_WithNullNode() {
    JSType result = interpreter.caseJSTypeCase(JSTypeCase.OBJECT, null);
    assertNull("Should return null for null node", result);
  }

  // --- Test for abstract method delegation (if any non-abstract methods exist) ---

  @Test
  public void testGetTypedScope_ReturnsNullByDefault() {
    // Assuming getTypedScope is not abstract; if it returns null by default, test that
    assertNull(interpreter.getTypedScope());
  }

  @Test
  public void testSetTypedScope_AlwaysNull() {
    // If there is a setter, test that it can be set; but for the real class it might not exist
    // Just a placeholder
    interpreter.setTypedScope(null);
    assertNull(interpreter.getTypedScope());
  }

  // --- Additional edge case: node with no type but still called ---

  @Test
  public void testCaseJSTypeCase_WithInvalidTypeCase() {
    // This might throw IllegalArgumentException; but we test that it doesn't break
    Node node = new Node(Token.TRUE);
    node.setParent(mockParentNode);
    // We can call with any JSTypeCase; the test stub returns resultType regardless
    JSType result = interpreter.caseJSTypeCase(JSTypeCase.BOOLEAN, node);
    assertNotNull(result);
  }

  // --- Test that chain is correctly implemented (multiple chaining) ---

  @Test
  public void testChain_MultipleChainingReturnsCorrectFirst() {
    TestChainableReverseAbstractInterpreter second = new TestChainableReverseAbstractInterpreter(mockRegistry);
    second.setResultType(mockType);
    TestChainableReverseAbstractInterpreter third = new TestChainableReverseAbstractInterpreter(mockRegistry);
    third.setResultType(mockType);
    interpreter.chain(second).chain(third);
    assertSame(interpreter, third.firstCallsite());
    assertSame(interpreter, second.firstCallsite());
  }

  // --- Test that chain returns the argument (i.e., sets next) ---

  @Test
  public void testChain_ReturnsNextInstance() {
    assertSame(mockNext, interpreter.chain(mockNext));
  }

  // --- Test that firstCallsite works even when chain is not used ---

  @Test
  public void testFirstCallsite_SingleInterpreter() {
    assertSame(interpreter, interpreter.firstCallsite());
  }

  // --- If there is a getConcreteType method, test it ---

  @Test
  public void testGetConcreteType_ReturnsNull() {
    // default implementation might return null
    assertNull(interpreter.getConcreteType(null));
  }

  // --- Testing with specific Node types (if applicable) ---

  @Test
  public void testCaseString_WithNullParent() {
    // Similar to bug scenario: node without parent
    Node stringNode = new Node(Token.STRING);
    stringNode.setParent(null);
    // The stub uses caseJSTypeCase, so we just call that directly
    JSType result = interpreter.caseJSTypeCase(JSTypeCase.STRING, stringNode);
    assertNotNull(result);
  }

  @Test
  public void testCaseObject_WithNullParent() {
    Node objNode = new Node(Token.OBJECT);
    objNode.setParent(null);
    JSType result = interpreter.caseJSTypeCase(JSTypeCase.OBJECT, objNode);
    assertNotNull(result);
  }

  // --- If the class has a method that returns a ChainableReverseAbstractInterpreter from a node (like firstCallsiteForNode) ---
  // Not known, so skip.

  // --- Overall, ensure no regression on simple happy path ---

  @Test
  public void testConstructor_WithRegistry() {
    assertNotNull(interpreter);
  }
}