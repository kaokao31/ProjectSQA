package com.google.javascript.rhino.jstype;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import org.junit.Before;
import org.junit.Test;

public class PrototypeObjectTypeTest {

  private JSTypeRegistry registry;
  private PrototypeObjectType prototypeObjectType;
  private PrototypeObjectType childPrototype;

  @Before
  public void setUp() {
    registry = new JSTypeRegistry();
    // Create an implicit prototype (Object type)
    ObjectType implicitProto =
        registry.createAnonymousObjectType(null);
    prototypeObjectType = new PrototypeObjectType(registry, "TestType", implicitProto);
    childPrototype = new PrototypeObjectType(registry, "ChildType", prototypeObjectType);
  }

  @Test
  public void testConstructor_DoesNotThrowException() {
    assertNotNull(prototypeObjectType);
    assertNotNull(childPrototype);
  }

  @Test
  public void testHasProperty_Exist() {
    String propertyName = "prop1";
    prototypeObjectType.defineDeclaredProperty(propertyName, registry.getNativeType(JSTypeNative.NUMBER_TYPE), null);
    assertTrue(prototypeObjectType.hasProperty(propertyName));
  }

  @Test
  public void testHasProperty_NotExist() {
    assertFalse(prototypeObjectType.hasProperty("nonexistent"));
  }

  @Test
  public void testHasProperty_Null() {
    try {
      prototypeObjectType.hasProperty(null);
      fail("Expected NullPointerException");
    } catch (NullPointerException e) {
      // Expected
    }
  }

  @Test
  public void testGetPropertyType_DefinedProperty() {
    String propertyName = "prop2";
    JSType numType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    prototypeObjectType.defineDeclaredProperty(propertyName, numType, null);
    assertEquals(numType, prototypeObjectType.getPropertyType(propertyName));
  }

  @Test
  public void testGetPropertyType_UndefinedProperty_ReturnsNull() {
    // Might cause NullPointerException in buggy code (Bug #33)
    JSType type = prototypeObjectType.getPropertyType("undefinedProp");
    assertNull("Should return null for undefined property without throwing", type);
  }

  @Test
  public void testGetPropertyType_InheritedProperty() {
    String propertyName = "inheritedProp";
    JSType numType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    prototypeObjectType.defineDeclaredProperty(propertyName, numType, null);
    // Child should inherit the property
    assertEquals(numType, childPrototype.getPropertyType(propertyName));
  }

  @Test
  public void testGetPropertyType_Null() {
    try {
      prototypeObjectType.getPropertyType(null);
      fail("Expected NullPointerException");
    } catch (NullPointerException e) {
      // Expected
    }
  }

  @Test
  public void testSetPropertyType_Overrides() {
    String propertyName = "overridden";
    JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    prototypeObjectType.defineDeclaredProperty(propertyName, stringType, null);
    // Now override with number
    JSType numType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    childPrototype.defineDeclaredProperty(propertyName, numType, null);
    assertEquals(numType, childPrototype.getPropertyType(propertyName));
  }

  @Test
  public void testGetOwnPropertyNames() {
    prototypeObjectType.defineDeclaredProperty("myProp", registry.getNativeType(JSTypeNative.STRING_TYPE), null);
    prototypeObjectType.defineDeclaredProperty("other", registry.getNativeType(JSTypeNative.BOOLEAN_TYPE), null);
    // Ensure own properties are listed (inherited ones not included)
    Iterable<String> names = prototypeObjectType.getOwnPropertyNames();
    boolean myPropFound = false;
    boolean otherFound = false;
    for (String name : names) {
      if ("myProp".equals(name)) {
        myPropFound = true;
      }
      if ("other".equals(name)) {
        otherFound = true;
      }
    }
    assertTrue(myPropFound);
    assertTrue(otherFound);
  }

  @Test
  public void testGetOwnPropertyNames_NoOwnProperties() {
    Iterable<String> names = prototypeObjectType.getOwnPropertyNames();
    assertFalse(names.iterator().hasNext());
  }

  @Test
  public void testToString() {
    String result = prototypeObjectType.toString();
    assertNotNull(result);
    assertTrue(result.contains("TestType"));
  }

  @Test
  public void testIsObjectType() {
    assertTrue(prototypeObjectType.isObjectType());
  }

  @Test
  public void testGetImplicitPrototype() {
    assertNotNull(prototypeObjectType.getImplicitPrototype());
  }

  @Test
  public void testGetImplicitPrototype_ChildLinksToParent() {
    assertEquals(prototypeObjectType, childPrototype.getImplicitPrototype());
  }

  @Test
  public void testRegisterProperty_DoesNotThrow() {
    prototypeObjectType.defineDeclaredProperty("prop", registry.getNativeType(JSTypeNative.VOID_TYPE), null);
    // This should work without exception
  }

  @Test
  public void testHasOwnProperty_True() {
    prototypeObjectType.defineDeclaredProperty("own", registry.getNativeType(JSTypeNative.NUMBER_TYPE), null);
    assertTrue(prototypeObjectType.hasOwnProperty("own"));
  }

  @Test
  public void testHasOwnProperty_FalseForInherited() {
    prototypeObjectType.defineDeclaredProperty("inherited", registry.getNativeType(JSTypeNative.STRING_TYPE), null);
    assertFalse(childPrototype.hasOwnProperty("inherited"));
  }

  @Test
  public void testHasOwnProperty_Null() {
    try {
      prototypeObjectType.hasOwnProperty(null);
      fail("Expected NullPointerException");
    } catch (NullPointerException e) {
      // Expected
    }
  }

  @Test
  public void testGetPropertyValueInfos_WhenNoProperties() {
    assertNotNull(prototypeObjectType.getPropertyValueInfos());
    assertFalse(prototypeObjectType.getPropertyValueInfos().hasNext());
  }

  @Test
  public void testDefineProperty_OverridesExisting() {
    JSType origType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    prototypeObjectType.defineDeclaredProperty("dup", origType, null);
    JSType newType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    prototypeObjectType.defineDeclaredProperty("dup", newType, null);
    assertEquals(newType, prototypeObjectType.getPropertyType("dup"));
  }

  @Test
  public void testSetInheritedPropertyType_ShouldNotModifyParent() {
    String propertyName = "parentProp";
    JSType parentType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    prototypeObjectType.defineDeclaredProperty(propertyName, parentType, null);
    JSType childType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    // Set property on child – should create own property
    childPrototype.defineDeclaredProperty(propertyName, childType, null);
    // Parent property should remain unchanged
    assertEquals(parentType, prototypeObjectType.getPropertyType(propertyName));
  }

  @Test
  public void testTryToFoldToObjectType_WhenNoProperties() {
    // The method tryToFoldToObjectType is internal; check it doesn't crash
    // We call it via a public method that uses it, e.g., getPropertyType on invalid property
    // Covered by earlier test: getPropertyType("nonexistent") should return null without crash
    // Address bug #33: previously threw NullPointerException
  }

  @Test
  public void testClone() {
    PrototypeObjectType cloned = prototypeObjectType.clone();
    assertNotNull(cloned);
    assertEquals(prototypeObjectType.toString(), cloned.toString());
  }

  @Test
  public void testEquals_SameObject() {
    assertTrue(prototypeObjectType.equals(prototypeObjectType));
  }

  @Test
  public void testEquals_Null() {
    assertFalse(prototypeObjectType.equals(null));
  }

  @Test
  public void testHashCode() {
    int code1 = prototypeObjectType.hashCode();
    int code2 = prototypeObjectType.hashCode();
    assertEquals(code1, code2);
  }

  // Additional tests to cover edge cases and branches

  @Test
  public void testDefineDeclaredProperty_NullType() {
    try {
      prototypeObjectType.defineDeclaredProperty("nullType", null, null);
      fail("Expected NullPointerException or IllegalArgumentException");
    } catch (Exception e) {
      // OK - defensive check
    }
  }

  @Test
  public void testDefineDeclaredProperty_EmptyName() {
    JSType numType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    prototypeObjectType.defineDeclaredProperty("", numType, null);
    assertTrue(prototypeObjectType.hasProperty(""));
  }
}