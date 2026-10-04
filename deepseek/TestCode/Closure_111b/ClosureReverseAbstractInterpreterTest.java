package com.google.javascript.jscomp.type;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.type.ClosureReverseAbstractInterpreter;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeNative;
import com.google.javascript.rhino.jstype.JSTypeRegistry;

/**
 * Test class for ClosureReverseAbstractInterpreter.
 * Designed to cover core functionality and edge cases
 * potentially related to Defects4J bug #111.
 */
public class ClosureReverseAbstractInterpreterTest {

  private Compiler compiler;
  private JSTypeRegistry registry;
  private ClosureReverseAbstractInterpreter interpreter;

  @Before
  public void setUp() {
    compiler = new Compiler();
    registry = compiler.getTypeRegistry();
    interpreter = new ClosureReverseAbstractInterpreter(compiler);
  }

  private Node createTypeofNode() {
    return new Node(Token.TYPEOF, Node.newString("x"));
  }

  private Node createTypeofNodeWithoutChild() {
    return new Node(Token.TYPEOF);
  }

  @Test
  public void testRestrictByTrueTypeOfNonTypeofNode() {
    Node n = Node.newString("not_typeof");
    JSType type = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSType result = interpreter.restrictByTrueTypeOf(n, type);
    assertSame("Non-typeof node should return original type", type, result);
  }

  @Test
  public void testRestrictByFalseTypeOfNonTypeofNode() {
    Node n = Node.newString("not_typeof");
    JSType type = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSType result = interpreter.restrictByFalseTypeOf(n, type);
    assertSame("Non-typeof node should return original type", type, result);
  }

  @Test
  public void testRestrictByTrueTypeOfTypeofNumber() {
    JSType type = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSType result = interpreter.restrictByTrueTypeOf(createTypeofNode(), type);
    assertNotNull("Result should not be null", result);
    assertEquals("Number type should be preserved", type, result);
  }

  @Test
  public void testRestrictByFalseTypeOfTypeofNumber() {
    JSType type = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSType result = interpreter.restrictByFalseTypeOf(createTypeofNode(), type);
    assertNotNull("Result should not be null", result);
    JSType emptyType = registry.getNativeType(JSTypeNative.NO_TYPE);
    assertEquals("False branch of typeof number should be empty", emptyType, result);
  }

  @Test
  public void testRestrictByTrueTypeOfTypeofString() {
    JSType type = registry.getNativeType(JSTypeNative.STRING_TYPE);
    JSType result = interpreter.restrictByTrueTypeOf(createTypeofNode(), type);
    assertNotNull("Result should not be null", result);
    assertEquals("String type should be preserved", type, result);
  }

  @Test
  public void testRestrictByFalseTypeOfTypeofString() {
    JSType type = registry.getNativeType(JSTypeNative.STRING_TYPE);
    JSType result = interpreter.restrictByFalseTypeOf(createTypeofNode(), type);
    assertNotNull("Result should not be null", result);
    JSType emptyType = registry.getNativeType(JSTypeNative.NO_TYPE);
    assertEquals("False branch of typeof string should be empty", emptyType, result);
  }

  @Test
  public void testRestrictByTrueTypeOfTypeofUndefined() {
    JSType type = registry.getNativeType(JSTypeNative.VOID_TYPE);
    JSType result = interpreter.restrictByTrueTypeOf(createTypeofNode(), type);
    assertNotNull("Result should not be null", result);
    assertEquals("Undefined type should be preserved", type, result);
  }

  @Test
  public void testRestrictByFalseTypeOfTypeofUndefined() {
    JSType type = registry.getNativeType(JSTypeNative.VOID_TYPE);
    JSType result = interpreter.restrictByFalseTypeOf(createTypeofNode(), type);
    assertNotNull("Result should not be null", result);
    JSType emptyType = registry.getNativeType(JSTypeNative.NO_TYPE);
    assertEquals("False branch of typeof undefined should be empty", emptyType, result);
  }

  @Test
  public void testRestrictByTrueTypeOfTypeofNull() {
    JSType type = registry.getNativeType(JSTypeNative.NULL_TYPE);
    JSType result = interpreter.restrictByTrueTypeOf(createTypeofNode(), type);
    assertNotNull("Result should not be null", result);
    assertEquals("Null type should be preserved (typeof null is 'object')", type, result);
  }

  @Test
  public void testRestrictByFalseTypeOfTypeofNull() {
    JSType type = registry.getNativeType(JSTypeNative.NULL_TYPE);
    JSType result = interpreter.restrictByFalseTypeOf(createTypeofNode(), type);
    assertNotNull("Result should not be null", result);
    JSType emptyType = registry.getNativeType(JSTypeNative.NO_TYPE);
    assertEquals("False branch of typeof null should be empty", emptyType, result);
  }

  @Test
  public void testRestrictByTrueTypeOfTypeofObject() {
    JSType type = registry.getNativeType(JSTypeNative.OBJECT_TYPE);
    JSType result = interpreter.restrictByTrueTypeOf(createTypeofNode(), type);
    assertNotNull("Result should not be null", result);
    assertEquals("Object type should be preserved", type, result);
  }

  @Test
  public void testRestrictByFalseTypeOfTypeofObject() {
    JSType type = registry.getNativeType(JSTypeNative.OBJECT_TYPE);
    JSType result = interpreter.restrictByFalseTypeOf(createTypeofNode(), type);
    assertNotNull("Result should not be null", result);
    JSType emptyType = registry.getNativeType(JSTypeNative.NO_TYPE);
    assertEquals("False branch of typeof object should be empty", emptyType, result);
  }

  @Test
  public void testRestrictByTrueTypeOfTypeofWithoutChild() {
    Node typeofNode = createTypeofNodeWithoutChild();
    JSType type = registry.getNativeType(JSTypeNative.ALL_TYPE);
    try {
      JSType result = interpreter.restrictByTrueTypeOf(typeofNode, type);
      assertNotNull("Result should not be null", result);
    } catch (Exception e) {
      fail("Unexpected exception: " + e.getMessage());
    }
  }

  @Test
  public void testRestrictByFalseTypeOfTypeofWithoutChild() {
    Node typeofNode = createTypeofNodeWithoutChild();
    JSType type = registry.getNativeType(JSTypeNative.ALL_TYPE);
    try {
      JSType result = interpreter.restrictByFalseTypeOf(typeofNode, type);
      assertNotNull("Result should not be null", result);
    } catch (Exception e) {
      fail("Unexpected exception: " + e.getMessage());
    }
  }
}