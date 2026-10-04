package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.ObjectType;
import com.google.javascript.rhino.jstype.UnknownType;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test suite for FunctionTypeBuilder, targeting maximum coverage and fault detection.
 * Specifically designed to expose the bug in Defects4J Closure bug 41.
 */
public class FunctionTypeBuilderTest {

  private JSTypeRegistry registry;
  private FunctionTypeBuilder builder;

  @Before
  public void setUp() {
    registry = new JSTypeRegistry(null);
    builder = new FunctionTypeBuilder(registry);
  }

  /**
   * Test building a function type with no parameters and a known return type.
   */
  @Test
  public void testBuildNoParameters() {
    Node functionNode = new Node(Node.FUNCTION);
    JSType returnType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    builder.withReturnType(returnType);
    builder.withParameters(functionNode);
    FunctionType result = builder.build();
    assertNotNull("Function type should not be null", result);
    assertEquals("Return type should be number", returnType, result.getReturnType());
    assertEquals("Parameter count should be 0", 0, result.getParameters().size());
  }

  /**
   * Test building a function type with a single parameter of known type.
   */
  @Test
  public void testBuildSingleKnownParameter() {
    Node paramNode = new Node(Node.PARAM_LIST);
    paramNode.addChildToBack(new Node(Node.NAME, "x"));
    JSType paramType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    JSType returnType = registry.getNativeType(JSTypeNative.VOID_TYPE);
    builder.withReturnType(returnType);
    builder.withParameterType(paramType);
    builder.withParameters(paramNode);
    FunctionType result = builder.build();
    assertNotNull("Function type should not be null", result);
    assertEquals("Return type should be void", returnType, result.getReturnType());
    assertEquals("Parameter count should be 1", 1, result.getParameters().size());
    assertEquals("Parameter type should be string", paramType, result.getParameters().get(0).getType());
  }

  /**
   * Test building a function type with a parameter of unknown type (null).
   * This scenario triggers the bug in Defects4J Closure bug 41.
   */
  @Test
  public void testBuildWithUnknownParameterType() {
    Node paramNode = new Node(Node.PARAM_LIST);
    paramNode.addChildToBack(new Node(Node.NAME, "x"));
    // Simulate unknown type by passing null
    JSType unknownType = null;
    JSType returnType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    builder.withReturnType(returnType);
    builder.withParameterType(unknownType);
    builder.withParameters(paramNode);
    // The buggy version may throw NullPointerException or produce incorrect type.
    // The fixed version should handle null gracefully.
    FunctionType result = builder.build();
    assertNotNull("Function type should not be null", result);
    assertEquals("Return type should be number", returnType, result.getReturnType());
    assertEquals("Parameter count should be 1", 1, result.getParameters().size());
    // The parameter type should be the unknown type (or no type)
    JSType actualParamType = result.getParameters().get(0).getType();
    assertTrue("Parameter type should be unknown or null", actualParamType == null || actualParamType.isUnknownType());
  }

  /**
   * Test building a function type with multiple parameters, some known and some unknown.
   */
  @Test
  public void testBuildMixedParameterTypes() {
    Node paramNode = new Node(Node.PARAM_LIST);
    paramNode.addChildToBack(new Node(Node.NAME, "a"));
    paramNode.addChildToBack(new Node(Node.NAME, "b"));
    JSType knownType = registry.getNativeType(JSTypeNative.BOOLEAN_TYPE);
    JSType unknownType = null;
    JSType returnType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    builder.withReturnType(returnType);
    builder.withParameterType(knownType);
    builder.withParameterType(unknownType);
    builder.withParameters(paramNode);
    FunctionType result = builder.build();
    assertNotNull("Function type should not be null", result);
    assertEquals("Return type should be string", returnType, result.getReturnType());
    assertEquals("Parameter count should be 2", 2, result.getParameters().size());
    assertEquals("First parameter type should be boolean", knownType, result.getParameters().get(0).getType());
    JSType secondParamType = result.getParameters().get(1).getType();
    assertTrue("Second parameter type should be unknown or null", secondParamType == null || secondParamType.isUnknownType());
  }

  /**
   * Test building a function type with no return type set (should default to unknown).
   */
  @Test
  public void testBuildNoReturnType() {
    Node paramNode = new Node(Node.PARAM_LIST);
    builder.withParameters(paramNode);
    FunctionType result = builder.build();
    assertNotNull("Function type should not be null", result);
    assertTrue("Return type should be unknown", result.getReturnType().isUnknownType());
    assertEquals("Parameter count should be 0", 0, result.getParameters().size());
  }

  /**
   * Test building a function type with null parameter list (edge case).
   */
  @Test
  public void testBuildNullParameterList() {
    JSType returnType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    builder.withReturnType(returnType);
    builder.withParameters(null);
    FunctionType result = builder.build();
    assertNotNull("Function type should not be null", result);
    assertEquals("Return type should be number", returnType, result.getReturnType());
    assertEquals("Parameter count should be 0", 0, result.getParameters().size());
  }

  /**
   * Test building a function type with a parameter that has a null name node (edge case).
   */
  @Test
  public void testBuildParameterWithNullName() {
    Node paramNode = new Node(Node.PARAM_LIST);
    paramNode.addChildToBack(new Node(Node.NAME, null)); // name is null
    JSType paramType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    JSType returnType = registry.getNativeType(JSTypeNative.VOID_TYPE);
    builder.withReturnType(returnType);
    builder.withParameterType(paramType);
    builder.withParameters(paramNode);
    FunctionType result = builder.build();
    assertNotNull("Function type should not be null", result);
    assertEquals("Parameter count should be 1", 1, result.getParameters().size());
    // The parameter should still have a type even if name is null
    assertEquals("Parameter type should be string", paramType, result.getParameters().get(0).getType());
  }

  /**
   * Test building a function type with a return type that is null (should default to unknown).
   */
  @Test
  public void testBuildNullReturnType() {
    Node paramNode = new Node(Node.PARAM_LIST);
    builder.withReturnType(null);
    builder.withParameters(paramNode);
    FunctionType result = builder.build();
    assertNotNull("Function type should not be null", result);
    assertTrue("Return type should be unknown", result.getReturnType().isUnknownType());
  }

  /**
   * Test building a function type with multiple calls to withParameterType (should accumulate).
   */
  @Test
  public void testBuildMultipleParameterTypes() {
    Node paramNode = new Node(Node.PARAM_LIST);
    paramNode.addChildToBack(new Node(Node.NAME, "x"));
    paramNode.addChildToBack(new Node(Node.NAME, "y"));
    paramNode.addChildToBack(new Node(Node.NAME, "z"));
    JSType type1 = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSType type2 = registry.getNativeType(JSTypeNative.STRING_TYPE);
    JSType type3 = registry.getNativeType(JSTypeNative.BOOLEAN_TYPE);
    JSType returnType = registry.getNativeType(JSTypeNative.VOID_TYPE);
    builder.withReturnType(returnType);
    builder.withParameterType(type1);
    builder.withParameterType(type2);
    builder.withParameterType(type3);
    builder.withParameters(paramNode);
    FunctionType result = builder.build();
    assertNotNull("Function type should not be null", result);
    assertEquals("Parameter count should be 3", 3, result.getParameters().size());
    assertEquals("First parameter type should be number", type1, result.getParameters().get(0).getType());
    assertEquals("Second parameter type should be string", type2, result.getParameters().get(1).getType());
    assertEquals("Third parameter type should be boolean", type3, result.getParameters().get(2).getType());
  }

  /**
   * Test building a function type with a parameter that has a type but no corresponding parameter node.
   * This should be handled gracefully (extra types ignored or cause error?).
   * We assume the builder ignores extra types.
   */
  @Test
  public void testBuildExtraParameterTypes() {
    Node paramNode = new Node(Node.PARAM_LIST);
    paramNode.addChildToBack(new Node(Node.NAME, "x"));
    JSType paramType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSType extraType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    JSType returnType = registry.getNativeType(JSTypeNative.VOID_TYPE);
    builder.withReturnType(returnType);
    builder.withParameterType(paramType);
    builder.withParameterType(extraType); // extra type, only one parameter
    builder.withParameters(paramNode);
    FunctionType result = builder.build();
    assertNotNull("Function type should not be null", result);
    assertEquals("Parameter count should be 1", 1, result.getParameters().size());
    assertEquals("Parameter type should be number", paramType, result.getParameters().get(0).getType());
  }

  /**
   * Test building a function type with a parameter that has no type set (should default to unknown).
   */
  @Test
  public void testBuildParameterWithoutType() {
    Node paramNode = new Node(Node.PARAM_LIST);
    paramNode.addChildToBack(new Node(Node.NAME, "x"));
    JSType returnType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    builder.withReturnType(returnType);
    // No call to withParameterType
    builder.withParameters(paramNode);
    FunctionType result = builder.build();
    assertNotNull("Function type should not be null", result);
    assertEquals("Parameter count should be 1", 1, result.getParameters().size());
    JSType paramType = result.getParameters().get(0).getType();
    assertTrue("Parameter type should be unknown", paramType == null || paramType.isUnknownType());
  }
}