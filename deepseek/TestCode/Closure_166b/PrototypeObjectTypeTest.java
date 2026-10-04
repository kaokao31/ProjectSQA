package com.google.javascript.rhino.jstype;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.javascript.rhino.ErrorReporter;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.ObjectType;
import com.google.javascript.rhino.jstype.PrototypeObjectType;
import com.google.javascript.rhino.testing.DefaultErrorReporter;
import org.junit.Before;
import org.junit.Test;

public class PrototypeObjectTypeTest {

  private ErrorReporter errorReporter;
  private JSTypeRegistry registry;
  private PrototypeObjectType prototypeObjectType;

  @Before
  public void setUp() {
    errorReporter = new DefaultErrorReporter(false);
    registry = new JSTypeRegistry(errorReporter);
    prototypeObjectType = new PrototypeObjectType(registry, "TestClass", null);
  }

  @Test
  public void testHasOwnPropertyWithNonExistingProperty() {
    assertFalse(prototypeObjectType.hasOwnProperty("nonExistent"));
  }

  @Test
  public void testHasOwnPropertyWithNullPropertyName() {
    assertFalse(prototypeObjectType.hasOwnProperty(null));
  }

  @Test
  public void testHasOwnPropertyWithPropertyOfObjectType() {
    // Create a simple object type and set it as property
    ObjectType objectType = registry.createObjectType("Inner", null, null);
    prototypeObjectType.defineDeclaredProperty("prop", objectType, null);
    assertTrue(prototypeObjectType.hasOwnProperty("prop"));
  }

  @Test(expected = ClassCastException.class)
  public void testHasOwnPropertyWithNonObjectProperty() {
    // Set a property with a non-ObjectType (e.g., NumberType)
    JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    prototypeObjectType.defineDeclaredProperty("numberProp", numberType, null);
    // In the buggy version, this will throw ClassCastException
    prototypeObjectType.hasOwnProperty("numberProp");
  }

  @Test
  public void testGetPropertyTypeReturnsNullForNonExistent() {
    assertNull(prototypeObjectType.getPropertyType("nonExistent"));
  }

  @Test
  public void testGetPropertyTypeReturnsCorrectType() {
    JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    prototypeObjectType.defineDeclaredProperty("str", stringType, null);
    assertEquals(stringType, prototypeObjectType.getPropertyType("str"));
  }

  @Test
  public void testSetAndGetPropertyType() {
    JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    prototypeObjectType.setPropertyType("num", numberType);
    assertEquals(numberType, prototypeObjectType.getPropertyType("num"));
  }

  @Test
  public void testIsPropertyTypeDeclared() {
    JSType booleanType = registry.getNativeType(JSTypeNative.BOOLEAN_TYPE);
    prototypeObjectType.defineDeclaredProperty("boolProp", booleanType, null);
    assertTrue(prototypeObjectType.isPropertyTypeDeclared("boolProp"));
  }

  @Test
  public void testIsPropertyTypeDeclaredForNonExistent() {
    assertFalse(prototypeObjectType.isPropertyTypeDeclared("nonexistent"));
  }

  @Test
  public void testIsPropertyTypeInferred() {
    JSType undefinedType = registry.getNativeType(JSTypeNative.UNDEFINED_TYPE);
    prototypeObjectType.defineInferredProperty("inferredProp", undefinedType, null);
    assertTrue(prototypeObjectType.isPropertyTypeInferred("inferredProp"));
  }

  @Test
  public void testIsPropertyTypeInferredForDeclared() {
    JSType voidType = registry.getNativeType(JSTypeNative.VOID_TYPE);
    prototypeObjectType.defineDeclaredProperty("declaredProp", voidType, null);
    assertFalse(prototypeObjectType.isPropertyTypeInferred("declaredProp"));
  }

  @Test
  public void testGetOwnPropertyNames() {
    assertNotNull(prototypeObjectType.getOwnPropertyNames());
    assertEquals(0, prototypeObjectType.getOwnPropertyNames().size());
    JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    prototypeObjectType.defineDeclaredProperty("a", stringType, null);
    prototypeObjectType.defineDeclaredProperty("b", stringType, null);
    assertEquals(2, prototypeObjectType.getOwnPropertyNames().size());
    assertTrue(prototypeObjectType.getOwnPropertyNames().contains("a"));
    assertTrue(prototypeObjectType.getOwnPropertyNames().contains("b"));
  }

  @Test
  public void testGetPropertiesCount() {
    assertEquals(0, prototypeObjectType.getPropertiesCount());
    JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    prototypeObjectType.defineDeclaredProperty("x", numberType, null);
    assertEquals(1, prototypeObjectType.getPropertiesCount());
  }

  @Test
  public void testDefinePropertyTwice() {
    JSType firstType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    prototypeObjectType.defineDeclaredProperty("dup", firstType, null);
    // Redefine with a different type
    JSType secondType = registry.getNativeType(JSTypeNative.BOOLEAN_TYPE);
    prototypeObjectType.defineDeclaredProperty("dup", secondType, null);
    // The last define should win
    assertEquals(secondType, prototypeObjectType.getPropertyType("dup"));
  }

  @Test
  public void testHasOwnPropertyAfterRemovingProperty() {
    JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    prototypeObjectType.defineDeclaredProperty("removeMe", stringType, null);
    assertTrue(prototypeObjectType.hasOwnProperty("removeMe"));
    // There is no remove method directly, but setting property to null may have effect?
    // We'll test that a later define with null type doesn't break it
    prototypeObjectType.defineDeclaredProperty("removeMe", null, null);
    // In many implementations, this might remove the property or cause issues
    // The test should not throw exceptions
    assertFalse(prototypeObjectType.hasOwnProperty("removeMe"));
  }

  @Test
  public void testIsSubtypeOfItself() {
    assertTrue(prototypeObjectType.isSubtype(prototypeObjectType));
  }

  @Test
  public void testIsSubtypeOfObjectType() {
    ObjectType objectType = registry.createObjectType("Object", null, null);
    assertTrue(prototypeObjectType.isSubtype(objectType));
  }

  @Test
  public void testIsSubtypeOfNull() {
    assertFalse(prototypeObjectType.isSubtype(null));
  }
}