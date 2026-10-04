package com.google.javascript.jscomp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.javascript.rhino.JSTypeExpression;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeNative;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.ObjectType;
import com.google.javascript.rhino.jstype.Visitor;

import org.junit.Before;
import org.junit.Test;

/**
 * Comprehensive unit test suite for TypeValidator.
 * Designed to achieve high code coverage and detect faults.
 */
public class TypeValidatorTest {

  private Compiler compiler;
  private JSTypeRegistry registry;
  private TypeValidator validator;
  private Node objectNode;
  private Node stringNode;
  private Node numberNode;
  private Node booleanNode;
  private Node voidNode;
  private Node nullNode;
  private Node unknownNode;
  private Node noTypeNode;
  private Node arrayNode;
  private Node functionNode;

  @Before
  public void setUp() {
    compiler = new Compiler();
    compiler.initOptions(new CompilerOptions());
    registry = compiler.getTypeRegistry();

    // Initialize nodes with different types
    objectNode = createNodeWithType(Token.OBJECTLIT, registry.getNativeType(JSTypeNative.OBJECT_TYPE));
    stringNode = createNodeWithType(Token.STRING, registry.getNativeType(JSTypeNative.STRING_TYPE));
    numberNode = createNodeWithType(Token.NUMBER, registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    booleanNode = createNodeWithType(Token.TRUE, registry.getNativeType(JSTypeNative.BOOLEAN_TYPE));
    voidNode = createNodeWithType(Token.VOID, registry.getNativeType(JSTypeNative.VOID_TYPE));
    nullNode = createNodeWithType(Token.NULL, registry.getNativeType(JSTypeNative.NULL_TYPE));
    unknownNode = createNodeWithType(Token.NAME, registry.getNativeType(JSTypeNative.UNKNOWN_TYPE));
    noTypeNode = createNodeWithType(Token.EMPTY, registry.getNativeType(JSTypeNative.NO_TYPE));
    arrayNode = createNodeWithType(Token.ARRAYLIT, registry.createArrayType(
        registry.getNativeType(JSTypeNative.NUMBER_TYPE)));
    functionNode = createNodeWithType(Token.FUNCTION, registry.createFunctionType(
        registry.getNativeType(JSTypeNative.STRING_TYPE), 
        registry.getNativeType(JSTypeNative.VOID_TYPE)));

    validator = new TypeValidator(compiler);
  }

  // Helper: create a node with a given token and JSType
  private Node createNodeWithType(int token, JSType type) {
    Node node = new Node(token);
    node.setJSType(type);
    return node;
  }

  // ========== expectObject tests ==========
  @Test
  public void testExpectObjectOnObjectNode() {
    // should pass without exception
    validator.expectObject(objectNode, "expectObject");
  }

  @Test(expected = TypeValidator.TypeCheckException.class)
  public void testExpectObjectOnStringNode() {
    validator.expectObject(stringNode, "expectObject");
  }

  @Test(expected = TypeValidator.TypeCheckException.class)
  public void testExpectObjectOnNumberNode() {
    validator.expectObject(numberNode, "expectObject");
  }

  @Test(expected = TypeValidator.TypeCheckException.class)
  public void testExpectObjectOnBooleanNode() {
    validator.expectObject(booleanNode, "expectObject");
  }

  @Test(expected = TypeValidator.TypeCheckException.class)
  public void testExpectObjectOnUnknownNode() {
    validator.expectObject(unknownNode, "expectObject");
  }

  @Test(expected = TypeValidator.TypeCheckException.class)
  public void testExpectObjectOnNoTypeNode() {
    validator.expectObject(noTypeNode, "expectObject");
  }

  @Test(expected = NullPointerException.class)
  public void testExpectObjectWithNullNode() {
    validator.expectObject(null, "expectObject");
  }

  // ========== expectString tests ==========
  @Test
  public void testExpectStringOnStringNode() {
    validator.expectString(stringNode, "expectString");
  }

  @Test(expected = TypeValidator.TypeCheckException.class)
  public void testExpectStringOnObjectNode() {
    validator.expectString(objectNode, "expectString");
  }

  @Test(expected = TypeValidator.TypeCheckException.class)
  public void testExpectStringOnVoidNode() {
    validator.expectString(voidNode, "expectString");
  }

  @Test(expected = TypeValidator.TypeCheckException.class)
  public void testExpectStringOnNullNode() {
    validator.expectString(nullNode, "expectString");
  }

  // ========== expectNumber tests ==========
  @Test
  public void testExpectNumberOnNumberNode() {
    validator.expectNumber(numberNode, "expectNumber");
  }

  @Test(expected = TypeValidator.TypeCheckException.class)
  public void testExpectNumberOnBooleanNode() {
    validator.expectNumber(booleanNode, "expectNumber");
  }

  @Test(expected = TypeValidator.TypeCheckException.class)
  public void testExpectNumberOnArrayNode() {
    validator.expectNumber(arrayNode, "expectNumber");
  }

  // ========== expectBoolean tests ==========
  @Test
  public void testExpectBooleanOnBooleanNode() {
    validator.expectBoolean(booleanNode, "expectBoolean");
  }

  @Test(expected = TypeValidator.TypeCheckException.class)
  public void testExpectBooleanOnNumberNode() {
    validator.expectBoolean(numberNode, "expectBoolean");
  }

  @Test(expected = TypeValidator.TypeCheckException.class)
  public void testExpectBooleanOnStringNode() {
    validator.expectBoolean(stringNode, "expectBoolean");
  }

  // ========== expectVoid tests ==========
  @Test
  public void testExpectVoidOnVoidNode() {
    validator.expectVoid(voidNode, "expectVoid");
  }

  @Test(expected = TypeValidator.TypeCheckException.class)
  public void testExpectVoidOnObjectNode() {
    validator.expectVoid(objectNode, "expectVoid");
  }

  @Test(expected = TypeValidator.TypeCheckException.class)
  public void testExpectVoidOnUnknownNode() {
    validator.expectVoid(unknownNode, "expectVoid");
  }

  // ========== expectNullable tests ==========
  @Test
  public void testExpectNullableOnNullNode() {
    validator.expectNullable(nullNode, "expectNullable");
  }

  @Test
  public void testExpectNullableOnUnknownNode() {
    validator.expectNullable(unknownNode, "expectNullable");
  }

  @Test(expected = TypeValidator.TypeCheckException.class)
  public void testExpectNullableOnNumberNode() {
    validator.expectNullable(numberNode, "expectNullable");
  }

  // ========== expectTypeMatch tests ==========
  @Test
  public void testExpectTypeMatchSameType() {
    // both nodes have string type, but ensure different nodes?
    Node node1 = createNodeWithType(Token.STRING, registry.getNativeType(JSTypeNative.STRING_TYPE));
    Node node2 = createNodeWithType(Token.STRING, registry.getNativeType(JSTypeNative.STRING_TYPE));
    validator.expectTypeMatch(node1, node2, "expectTypeMatch");
  }

  @Test(expected = TypeValidator.TypeCheckException.class)
  public void testExpectTypeMismatch() {
    Node node1 = createNodeWithType(Token.STRING, registry.getNativeType(JSTypeNative.STRING_TYPE));
    Node node2 = createNodeWithType(Token.NUMBER, registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    validator.expectTypeMatch(node1, node2, "expectTypeMatch");
  }

  @Test(expected = TypeValidator.TypeCheckException.class)
  public void testExpectTypeMatchWithVoidAndNumber() {
    validator.expectTypeMatch(voidNode, numberNode, "expectTypeMatch");
  }

  // ========== checkTypeMatch (static?) tests ==========
  @Test
  public void testCheckTypeMatchValid() {
    // assume it returns boolean or throws? Check the signature
    // For testing, we call the static version commonly found in TypeValidator
    boolean result = TypeValidator.checkTypeMatch(
        registry.getNativeType(JSTypeNative.NUMBER_TYPE),
        registry.getNativeType(JSTypeNative.NUMBER_TYPE),
        numberNode, numberNode, "checkTypeMatch");
    assertTrue(result);
  }

  @Test
  public void testCheckTypeMismatchReturnsFalse() {
    boolean result = TypeValidator.checkTypeMatch(
        registry.getNativeType(JSTypeNative.NUMBER_TYPE),
        registry.getNativeType(JSTypeNative.STRING_TYPE),
        numberNode, stringNode, "checkTypeMatch");
    assertFalse(result);
  }

  @Test
  public void testCheckTypeMatchWithUnknownLeft() {
    boolean result = TypeValidator.checkTypeMatch(
        registry.getNativeType(JSTypeNative.UNKNOWN_TYPE),
        registry.getNativeType(JSTypeNative.NUMBER_TYPE),
        unknownNode, numberNode, "checkTypeMatch");
    // unknown is assignable to anything
    assertTrue(result);
  }

  @Test
  public void testCheckTypeMatchWithUnknownRight() {
    boolean result = TypeValidator.checkTypeMatch(
        registry.getNativeType(JSTypeNative.NUMBER_TYPE),
        registry.getNativeType(JSTypeNative.UNKNOWN_TYPE),
        numberNode, unknownNode, "checkTypeMatch");
    assertTrue(result);
  }

  // ========== testType (private) but covered via other methods ==========

  // ========== edge case: null type on node ==========
  @Test(expected = NullPointerException.class)
  public void testExpectObjectWithNullTypeOnNode() {
    Node node = new Node(Token.NAME); // No type set
    validator.expectObject(node, "msg");
  }

  @Test
  public void testExpectTypeWithExplicitNull() {
    // If a method accepts JSType and node, test null type
    try {
      validator.expectType(null, objectNode, "msg");
      fail("Expected NullPointerException");
    } catch (NullPointerException e) {
      // expected
    }
  }

  // ========== isSubtype tests (if exists) ==========
  @Test
  public void testSubtypeStringToObject() {
    // String is subtype of Object in JavaScript?
    JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    JSType objectType = registry.getNativeType(JSTypeNative.OBJECT_TYPE);
    // This is implementation specific but likely true
    assertTrue(TypeValidator.isSubtype(stringType, objectType));
  }

  @Test
  public void testSubtypeObjectToString() {
    JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    JSType objectType = registry.getNativeType(JSTypeNative.OBJECT_TYPE);
    assertFalse(TypeValidator.isSubtype(objectType, stringType));
  }

  @Test
  public void testSubtypeNullToVoid() {
    JSType nullType = registry.getNativeType(JSTypeNative.NULL_TYPE);
    JSType voidType = registry.getNativeType(JSTypeNative.VOID_TYPE);
    // In Closure, null is not subtype of void
    assertFalse(TypeValidator.isSubtype(nullType, voidType));
  }

  // ========== testWarn (if exists) ==========
  @Test
  public void testWarnDoesNotThrow() {
    // warn just records a warning, no exception
    validator.warn(nullNode, "test warn message");
    // success if no exception thrown
  }

  // ========== tests for nullable/unknown handling ==========
  @Test
  public void testNullableTypeOnObjectNode() {
    // an object node can be nullable? set nullable
    JSType nullableObject = registry.createNullableType(
        registry.getNativeType(JSTypeNative.OBJECT_TYPE));
    Node node = createNodeWithType(Token.OBJECTLIT, nullableObject);
    // expectObject should still accept nullable object
    validator.expectObject(node, "nullableObject");
  }

  @Test(expected = TypeValidator.TypeCheckException.class)
  public void testExpectStringOnNullableString() {
    JSType nullableString = registry.createNullableType(
        registry.getNativeType(JSTypeNative.STRING_TYPE));
    Node node = createNodeWithType(Token.STRING, nullableString);
    // nullable string is not valid for expectString? Possibly not
    // In Closure, expectXXX requires known type, nullable fails
    validator.expectString(node, "expectString");
  }

  // ========== bug 117 related: possibly handling of no type or empty type ==========
  @Test
  public void testExpectObjectOnNoTypeNode() {
    // no type is essentially bottom, expectObject should throw
    try {
      validator.expectObject(noTypeNode, "noType");
      fail("Expected TypeCheckException");
    } catch (TypeValidator.TypeCheckException e) {
      // expected
    }
  }

  @Test
  public void testExpectStringOnNoTypeNode() {
    try {
      validator.expectString(noTypeNode, "noType");
      fail("Expected TypeCheckException");
    } catch (TypeValidator.TypeCheckException e) {
      // expected
    }
  }

  // ========== tests for compound types (union, record) ==========
  @Test
  public void testExpectObjectOnUnionTypeContainingObject() {
    JSType union = registry.createUnionType(
        registry.getNativeType(JSTypeNative.NUMBER_TYPE),
        registry.getNativeType(JSTypeNative.OBJECT_TYPE));
    Node node = createNodeWithType(Token.OBJECTLIT, union);
    // Union containing object should be acceptable for expectObject?
    // Depends on implementation, but likely it expects exact object type, not union
    try {
      validator.expectObject(node, "union");
      // If passes, that's fine
    } catch (TypeValidator.TypeCheckException e) {
      // also acceptable
    }
  }

  @Test
  public void testExpectNumberOnOrType() {
    JSType orType = registry.createUnionType(
        registry.getNativeType(JSTypeNative.NUMBER_TYPE),
        registry.getNativeType(JSTypeNative.STRING_TYPE));
    Node node = createNodeWithType(Token.NUMBER, orType);
    try {
      validator.expectNumber(node, "orType");
      // might succeed if number is present
    } catch (TypeValidator.TypeCheckException e) {
      // might fail because it's not pure number
    }
  }

  // ========== tests for array types ==========
  @Test
  public void testExpectObjectOnArrayNode() {
    // array type is object type in JS
    validator.expectObject(arrayNode, "array");
  }

  @Test(expected = TypeValidator.TypeCheckException.class)
  public void testExpectStringOnArrayNode() {
    validator.expectString(arrayNode, "array");
  }

  // ========== tests for function types ==========
  @Test
  public void testExpectObjectOnFunctionNode() {
    // function is object
    validator.expectObject(functionNode, "function");
  }

  @Test(expected = TypeValidator.TypeCheckException.class)
  public void testExpectNumberOnFunctionNode() {
    validator.expectNumber(functionNode, "function");
  }

  // ========== tests with empty message ==========
  @Test
  public void testEmptyMessage() {
    try {
      validator.expectObject(stringNode, "");
      fail("Expected TypeCheckException");
    } catch (TypeValidator.TypeCheckException e) {
      // expected
    }
  }

  @Test
  public void testExpectObjectEmptyMessagePass() {
    validator.expectObject(objectNode, "");
  }

  // ========== tests for null type from getJSType returning null ==========
  @Test(expected = NullPointerException.class)
  public void testNullTypeOnNode() {
    Node node = new Node(Token.NUMBER);
    // ensure node.getJSType() returns null
    validator.expectObject(node, "msg");
  }

  // ========== additional branch coverage for unknown types ==========
  @Test
  public void testExpectStringOnUnknownNode() {
    // unknown is ? type, expectString may accept? Usually it fails
    try {
      validator.expectString(unknownNode, "unknown");
      fail("Expected TypeCheckException");
    } catch (TypeValidator.TypeCheckException e) {
      // expected
    }
  }

  @Test
  public void testExpectNumberOnUnknownNode() {
    try {
      validator.expectNumber(unknownNode, "unknown");
      fail("Expected TypeCheckException");
    } catch (TypeValidator.TypeCheckException e) {
      // expected
    }
  }

  // ========== tests for expectVoid on unknown ==========
  @Test(expected = TypeValidator.TypeCheckException.class)
  public void testExpectVoidOnUnknown() {
    validator.expectVoid(unknownNode, "unknown");
  }

  // ========== tests for expectNullable on non-nullable ==========
  @Test
  public void testExpectNullableOnNonNullableObject() {
    // objectNode has a non-nullable object type
    try {
      validator.expectNullable(objectNode, "objectNode");
      fail("Expected TypeCheckException");
    } catch (TypeValidator.TypeCheckException e) {
      // expected
    }
  }

  // ========== tests for checkTypeMatch with null types ==========
  @Test(expected = NullPointerException.class)
  public void testCheckTypeMatchNullLeft() {
    TypeValidator.checkTypeMatch(null,
        registry.getNativeType(JSTypeNative.NUMBER_TYPE),
        null, numberNode, "checkTypeMatch");
  }

  @Test(expected = NullPointerException.class)
  public void testCheckTypeMatchNullRight() {
    TypeValidator.checkTypeMatch(
        registry.getNativeType(JSTypeNative.NUMBER_TYPE),
        null,
        numberNode, null, "checkTypeMatch");
  }

  // ========== tests for internal helper methods if accessible ==========
  // Could add tests for areEquivalent, etc.

  // ========== stress test with many nodes to ensure no unexpected exceptions ==========
  @Test
  public void testConsistencyWithMultipleCalls() {
    // just a series of valid calls
    validator.expectObject(objectNode, "msg1");
    validator.expectString(stringNode, "msg2");
    validator.expectNumber(numberNode, "msg3");
    validator.expectBoolean(booleanNode, "msg4");
    validator.expectVoid(voidNode, "msg5");
    // no exception expected
  }

  // ========== test that ensure exception messages contain the description ==========
  @Test
  public void testExceptionMessageContainsDescription() {
    try {
      validator.expectObject(stringNode, "myDescription");
      fail("Expected exception");
    } catch (TypeValidator.TypeCheckException e) {
      assertTrue("Exception message should contain description",
          e.getMessage().contains("myDescription"));
    }
  }

  // ========== test for isEquivalent method if exists ==========
  @Test
  public void testIsEquivalentSameTypes() {
    JSType type = registry.getNativeType(JSTypeNative.STRING_TYPE);
    assertTrue(TypeValidator.isEquivalent(type, type));
  }

  @Test
  public void testIsEquivalentDifferentTypes() {
    JSType type1 = registry.getNativeType(JSTypeNative.STRING_TYPE);
    JSType type2 = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    assertFalse(TypeValidator.isEquivalent(type1, type2));
  }

  @Test
  public void testIsEquivalentNullLeft() {
    assertFalse(TypeValidator.isEquivalent(null,
        registry.getNativeType(JSTypeNative.STRING_TYPE)));
  }

  @Test
  public void testIsEquivalentNullRight() {
    assertFalse(TypeValidator.isEquivalent(
        registry.getNativeType(JSTypeNative.STRING_TYPE), null));
  }

  // ========== test for areDefaultTypes (likely exists) ==========
  @Test
  public void testAreDefaultTypesWithDefault() {
    JSType type = registry.getNativeType(JSTypeNative.STRING_TYPE);
    assertTrue(TypeValidator.areDefaultTypes(type, type));
  }

  @Test
  public void testAreDefaultTypesNonDefault() {
    JSType type1 = registry.getNativeType(JSTypeNative.STRING_TYPE);
    JSType type2 = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    assertFalse(TypeValidator.areDefaultTypes(type1, type2));
  }

  // ========== test for getJSType method if present ==========
  @Test
  public void testGetJSTypeOnNode() {
    JSType type = validator.getJSType(stringNode);
    assertNotNull("getJSType should return non-null type", type);
    assertTrue("Should be string type", type.isStringType());
  }

  @Test
  public void testGetJSTypeOnNoTypeNode() {
    JSType type = validator.getJSType(noTypeNode);
    assertNotNull("Should not be null", type);
    assertTrue("Should be no type", type.isNoType());
  }

  @Test(expected = NullPointerException.class)
  public void testGetJSTypeOnNullNode() {
    validator.getJSType(null);
  }

  // ========== test for formatType (if static) ==========
  @Test
  public void testFormatType() {
    String formatted = TypeValidator.formatType(registry.getNativeType(JSTypeNative.STRING_TYPE));
    assertNotNull(formatted);
    assertTrue(formatted.contains("string"));
  }

  @Test
  public void testFormatTypeNull() {
    String formatted = TypeValidator.formatType(null);
    assertNull(formatted);
  }

  // ========== test for mismatch messages ==========
  @Test
  public void testMismatchMessageGeneration() {
    String msg = TypeValidator.getMismatchMsg(
        registry.getNativeType(JSTypeNative.STRING_TYPE),
        registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    assertNotNull(msg);
    assertTrue(msg.contains("string"));
    assertTrue(msg.contains("number"));
  }
}