package com.google.javascript.jscomp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.ObjectType;
import org.junit.Before;
import org.junit.Test;

/**
 * Test suite for FunctionTypeBuilder.
 * Targets maximum coverage and fault detection (Defects4J Closure bug 144).
 */
public class FunctionTypeBuilderTest {

  private JSTypeRegistry registry;
  private FunctionTypeBuilder builder;
  private Node functionNode;

  @Before
  public void setUp() {
    // Use default coding convention and registry
    CodingConvention convention = new DefaultCodingConvention();
    registry = new JSTypeRegistry(convention);
    // Create a minimal function node (e.g., FUNCTION node with name and body)
    functionNode = new Node(Token.FUNCTION);
    functionNode.addChildToFront(Node.newString(Token.NAME, "testFunc"));
    functionNode.addChildToBack(new Node(Token.PARAM_LIST));
    functionNode.addChildToBack(new Node(Token.BLOCK));
    builder = new FunctionTypeBuilder(registry, functionNode);
  }

  // Helper to create a simple parameter node
  private Node createParamNode(String name) {
    return Node.newString(Token.NAME, name);
  }

  // Helper to create a parameter list with given names
  private Node createParamList(String... names) {
    Node paramList = new Node(Token.PARAM_LIST);
    for (String name : names) {
      paramList.addChildToBack(createParamNode(name));
    }
    return paramList;
  }

  // ===================== Basic Build Tests =====================

  @Test
  public void testBuildBasicFunction() {
    FunctionType type = builder.build();
    assertNotNull("Built function type should not be null", type);
    assertTrue("Should be a function type", type.isFunctionType());
    assertEquals("Return type should be unknown", registry.getNativeType(JSTypeNative.UNKNOWN_TYPE), type.getReturnType());
    assertEquals("Parameter count should be 0", 0, type.getParameters().size());
  }

  @Test
  public void testBuildWithReturnType() {
    builder.setReturnType(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    FunctionType type = builder.build();
    assertEquals("Return type should be number", registry.getNativeType(JSTypeNative.NUMBER_TYPE), type.getReturnType());
  }

  @Test
  public void testBuildWithParameters() {
    Node paramList = createParamList("a", "b");
    builder.setParameterTypes(paramList);
    FunctionType type = builder.build();
    assertEquals("Parameter count should be 2", 2, type.getParameters().size());
    assertEquals("First param type should be unknown", registry.getNativeType(JSTypeNative.UNKNOWN_TYPE), type.getParameters().get(0).getType());
  }

  @Test
  public void testBuildWithTypedParameters() {
    Node paramList = createParamList("x", "y");
    // Set types for parameters (simulate type annotations)
    builder.setParameterTypes(paramList);
    // Manually assign types to parameter nodes (as the builder would read from AST)
    paramList.getFirstChild().setJSType(registry.getNativeType(JSTypeNative.STRING_TYPE));
    paramList.getLastChild().setJSType(registry.getNativeType(JSTypeNative.BOOLEAN_TYPE));
    FunctionType type = builder.build();
    assertEquals("First param type should be string", registry.getNativeType(JSTypeNative.STRING_TYPE), type.getParameters().get(0).getType());
    assertEquals("Second param type should be boolean", registry.getNativeType(JSTypeNative.BOOLEAN_TYPE), type.getParameters().get(1).getType());
  }

  // ===================== Edge Cases =====================

  @Test
  public void testBuildNullReturnType() {
    builder.setReturnType(null);
    FunctionType type = builder.build();
    assertNull("Return type should be null when set to null", type.getReturnType());
  }

  @Test
  public void testBuildEmptyParamList() {
    Node emptyParamList = new Node(Token.PARAM_LIST);
    builder.setParameterTypes(emptyParamList);
    FunctionType type = builder.build();
    assertEquals("Parameter count should be 0", 0, type.getParameters().size());
  }

  @Test
  public void testBuildWithOptionalParameters() {
    // Simulate optional parameter (e.g., param with DEFAULT value)
    Node paramList = createParamList("opt");
    Node optParam = paramList.getFirstChild();
    optParam.setOptional(true); // Mark as optional
    builder.setParameterTypes(paramList);
    FunctionType type = builder.build();
    assertTrue("Parameter should be optional", type.getParameters().get(0).isOptional());
  }

  @Test
  public void testBuildWithVarArgs() {
    Node paramList = createParamList("rest");
    Node restParam = paramList.getFirstChild();
    restParam.setVarArgs(true);
    builder.setParameterTypes(paramList);
    FunctionType type = builder.build();
    assertTrue("Parameter should be var_args", type.getParameters().get(0).isVarArgs());
  }

  @Test
  public void testBuildWithThisType() {
    ObjectType thisType = registry.getNativeObjectType(JSTypeNative.ARRAY_TYPE);
    builder.setThisType(thisType);
    FunctionType type = builder.build();
    assertEquals("This type should be array", thisType, type.getThisType());
  }

  @Test
  public void testBuildWithTemplateType() {
    builder.setTemplateTypeName("T");
    FunctionType type = builder.build();
    assertEquals("Template type name should be T", "T", type.getTemplateTypeName());
  }

  // ===================== Fault Detection (Bug 144) =====================
  // Bug 144: FunctionTypeBuilder incorrectly handles default parameters
  // (optional parameters that have a default value should be treated as optional)
  @Test
  public void testBuildWithDefaultParameter() {
    // Create a parameter with a default value (e.g., param = 5)
    Node paramList = createParamList("def");
    Node defParam = paramList.getFirstChild();
    // Simulate a default value node (e.g., a NUMBER node)
    Node defaultValue = Node.newNumber(5);
    defParam.addChildToBack(defaultValue);
    // The builder should mark this parameter as optional
    builder.setParameterTypes(paramList);
    FunctionType type = builder.build();
    // Bug: In some versions, default parameters were not marked optional
    assertTrue("Parameter with default value should be optional", type.getParameters().get(0).isOptional());
  }

  @Test
  public void testBuildWithMixedOptionalAndRequired() {
    Node paramList = createParamList("req", "opt", "rest");
    Node optParam = paramList.getChildren().get(1);
    optParam.setOptional(true);
    Node restParam = paramList.getChildren().get(2);
    restParam.setVarArgs(true);
    builder.setParameterTypes(paramList);
    FunctionType type = builder.build();
    assertEquals("Parameter count should be 3", 3, type.getParameters().size());
    assertTrue("Second param should be optional", type.getParameters().get(1).isOptional());
    assertTrue("Third param should be var_args", type.getParameters().get(2).isVarArgs());
    assertTrue("First param should be required", !type.getParameters().get(0).isOptional());
  }

  // ===================== Null/Invalid Input Tests =====================

  @Test(expected = NullPointerException.class)
  public void testBuildWithNullRegistry() {
    new FunctionTypeBuilder(null, functionNode);
  }

  @Test(expected = NullPointerException.class)
  public void testBuildWithNullNode() {
    new FunctionTypeBuilder(registry, null);
  }

  @Test
  public void testBuildWithNullParamList() {
    builder.setParameterTypes(null);
    FunctionType type = builder.build();
    // Should not throw; parameter list remains empty
    assertEquals("Parameter count should be 0", 0, type.getParameters().size());
  }

  @Test
  public void testBuildWithNullThisType() {
    builder.setThisType(null);
    FunctionType type = builder.build();
    assertNull("This type should be null", type.getThisType());
  }

  // ===================== Branch Coverage: Multiple Calls =====================

  @Test
  public void testBuildMultipleTimes() {
    builder.setReturnType(registry.getNativeType(JSTypeNative.STRING_TYPE));
    FunctionType first = builder.build();
    // Modify and build again
    builder.setReturnType(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    FunctionType second = builder.build();
    assertEquals("First return type should be string", registry.getNativeType(JSTypeNative.STRING_TYPE), first.getReturnType());
    assertEquals("Second return type should be number", registry.getNativeType(JSTypeNative.NUMBER_TYPE), second.getReturnType());
  }

  @Test
  public void testBuildWithInferredReturnType() {
    // If no return type set, builder may infer from body (simulate)
    // For coverage, just ensure no exception
    FunctionType type = builder.build();
    assertNotNull(type);
  }

  // ===================== Additional Edge Cases =====================

  @Test
  public void testBuildWithOnlyThisType() {
    ObjectType thisType = registry.getNativeObjectType(JSTypeNative.WINDOW_TYPE);
    builder.setThisType(thisType);
    FunctionType type = builder.build();
    assertEquals("This type should be window", thisType, type.getThisType());
    assertEquals("Return type should be unknown", registry.getNativeType(JSTypeNative.UNKNOWN_TYPE), type.getReturnType());
  }

  @Test
  public void testBuildWithOnlyTemplateType() {
    builder.setTemplateTypeName("E");
    FunctionType type = builder.build();
    assertEquals("Template type name should be E", "E", type.getTemplateTypeName());
  }

  @Test
  public void testBuildWithAllOptions() {
    ObjectType thisType = registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE);
    builder.setReturnType(registry.getNativeType(JSTypeNative.VOID_TYPE));
    builder.setThisType(thisType);
    builder.setTemplateTypeName("T");
    Node paramList = createParamList("a", "b");
    paramList.getFirstChild().setOptional(true);
    builder.setParameterTypes(paramList);
    FunctionType type = builder.build();
    assertEquals("Return type should be void", registry.getNativeType(JSTypeNative.VOID_TYPE), type.getReturnType());
    assertEquals("This type should be object", thisType, type.getThisType());
    assertEquals("Template type should be T", "T", type.getTemplateTypeName());
    assertEquals("Parameter count should be 2", 2, type.getParameters().size());
    assertTrue("First param should be optional", type.getParameters().get(0).isOptional());
  }
}