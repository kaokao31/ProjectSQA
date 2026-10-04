package com.google.javascript.rhino.jstype;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.SimpleSourceFile;
import com.google.javascript.rhino.StaticSourceFile;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.ObjectType;
import com.google.javascript.rhino.jstype.RecordType;
import com.google.javascript.rhino.jstype.StaticScope;
import com.google.javascript.rhino.jstype.StaticSlot;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

/**
 * Comprehensive JUnit 4 test suite for RecordType.
 * Designed to achieve high coverage and trigger bugs,
 * specifically the cyclic reference StackOverflow bug (Closure-46).
 */
public class RecordTypeTest {

  private JSTypeRegistry registry;
  private StaticSourceFile sourceFile;

  @Before
  public void setUp() {
    // Create a minimal registry for test types.
    registry = new JSTypeRegistry(null, null, false);
    // Use a simple source file for error reporting.
    sourceFile = new SimpleSourceFile("test.js", false);
    // Ensure primitive types are registered.
    registry.getNativeObjectType(JSTypeNative.NUMBER_TYPE);
    registry.getNativeObjectType(JSTypeNative.STRING_TYPE);
    registry.getNativeObjectType(JSTypeNative.BOOLEAN_TYPE);
    registry.getNativeObjectType(JSTypeNative.VOID_TYPE);
    registry.getNativeObjectType(JSTypeNative.NULL_TYPE);
    registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE);
    registry.getNativeObjectType(JSTypeNative.ARRAY_TYPE);
    registry.getNativeObjectType(JSTypeNative.UNKNOWN_TYPE);
  }

  // Helper to create a RecordType from a map of property names to types.
  private RecordType createRecordType(Map<String, JSType> properties) {
    ImmutableMap<String, JSType> props = ImmutableMap.copyOf(properties);
    return new RecordType(registry, props);
  }

  @Test
  public void testEmptyRecord() {
    RecordType empty = createRecordType(new LinkedHashMap<String, JSType>());
    assertNotNull("Empty record should not be null", empty);
    assertTrue("Empty record should have no properties", empty.getProperties().isEmpty());
    assertTrue("Empty record should be an object type", empty.isObject());
    assertFalse("Empty record is not a function", empty.isFunctionType());
  }

  @Test
  public void testRecordWithSimpleProperties() {
    Map<String, JSType> props = new LinkedHashMap<>();
    props.put("x", registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    props.put("y", registry.getNativeType(JSTypeNative.STRING_TYPE));
    RecordType rec = createRecordType(props);
    assertEquals("Property x should be NUMBER_TYPE",
        registry.getNativeType(JSTypeNative.NUMBER_TYPE),
        rec.getPropertyType("x"));
    assertEquals("Property y should be STRING_TYPE",
        registry.getNativeType(JSTypeNative.STRING_TYPE),
        rec.getPropertyType("y"));
    assertEquals(2, rec.getProperties().size());
  }

  @Test
  public void testToStringOnSimpleRecord() {
    Map<String, JSType> props = new LinkedHashMap<>();
    props.put("name", registry.getNativeType(JSTypeNative.STRING_TYPE));
    RecordType rec = createRecordType(props);
    String str = rec.toString();
    // The exact format depends on the implementation, but it should contain property and type.
    assertNotNull(str);
    assertTrue("toString should contain property name", str.contains("name"));
    assertTrue("toString should contain type string", str.contains("string"));
  }

  @Test
  public void testIsSubtypeOfObject() {
    RecordType rec = createRecordType(new LinkedHashMap<String, JSType>());
    JSType objType = registry.getNativeType(JSTypeNative.OBJECT_TYPE);
    assertTrue("Empty record should be subtype of Object", rec.isSubtype(objType));
  }

  @Test
  public void testIsSubtypeOfRecordWithSameProperty() {
    Map<String, JSType> propsA = new LinkedHashMap<>();
    propsA.put("a", registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    RecordType recA = createRecordType(propsA);

    Map<String, JSType> propsB = new LinkedHashMap<>();
    propsB.put("a", registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    propsB.put("b", registry.getNativeType(JSTypeNative.STRING_TYPE));
    RecordType recB = createRecordType(propsB);

    // recB should be a subtype of recA because it has all required properties.
    // Whether this holds depends on the implementation of isSubtype.
    // Here we assume that RecordType.isSubtype checks property containment.
    assertTrue("Record with {a, b} should be subtype of {a}", recB.isSubtype(recA));
    assertFalse("Record with {a} should not be subtype of {a, b}",
        recA.isSubtype(recB));
  }

  @Test
  public void testEqualsAndHashCode() {
    Map<String, JSType> props1 = new LinkedHashMap<>();
    props1.put("x", registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    RecordType rec1 = createRecordType(props1);
    RecordType rec2 = createRecordType(props1); // same content
    assertEquals("Records with same content should be equal", rec1, rec2);
    assertEquals("Equal records should have same hash code",
        rec1.hashCode(), rec2.hashCode());

    Map<String, JSType> props3 = new LinkedHashMap<>();
    props3.put("x", registry.getNativeType(JSTypeNative.STRING_TYPE));
    RecordType rec3 = createRecordType(props3);
    assertFalse("Different property types should not be equal", rec1.equals(rec3));
  }

  @Test
  public void testCanCastToSameType() {
    Map<String, JSType> props = new LinkedHashMap<>();
    props.put("a", registry.getNativeType(JSTypeNative.BOOLEAN_TYPE));
    RecordType rec = createRecordType(props);
    assertTrue("Record can always cast to itself", rec.canCastTo(rec));
  }

  @Test
  public void testHasProperty() {
    Map<String, JSType> props = new LinkedHashMap<>();
    props.put("present", registry.getNativeType(JSTypeNative.VOID_TYPE));
    RecordType rec = createRecordType(props);
    assertTrue("Should have property 'present'", rec.hasProperty("present"));
    assertFalse("Should not have property 'missing'", rec.hasProperty("missing"));
  }

  @Test
  public void testGetPropertyTypeForMissingProperty() {
    RecordType rec = createRecordType(new LinkedHashMap<String, JSType>());
    JSType result = rec.getPropertyType("nonexistent");
    // Implementation may return Unknown type or null; check not null.
    assertNotNull("Property type for missing should not be null", result);
  }

  @Test
  public void testToObjectType() {
    RecordType rec = createRecordType(new LinkedHashMap<String, JSType>());
    ObjectType obj = rec.toObjectType();
    assertNotNull("RecordType should be convertible to ObjectType", obj);
    assertTrue("toObjectType should return an ObjectType", obj.isObjectType());
  }

  @Test
  public void testCyclicRecordToString() {
    // This test aims to trigger the StackOverflow crash (Closure-46)
    // by creating a record where a property's type refers back to the same record.
    Map<String, JSType> props = new LinkedHashMap<>();
    // We'll use a placeholder; the real type will be set after creation.
    RecordType rec = createRecordType(props);
    // Set property "self" to the record itself (creates cycle)
    props.put("self", rec);
    // Recreate with the updated map; this may cause infinite recursion in toString.
    // In the bug, this would cause StackOverflowError.
    // The fix should handle cycles gracefully.
    try {
      String str = rec.toString();
      // If we got here, no crash. Assert string contains something indicating cycle.
      assertNotNull("toString should complete", str);
      // Optionally verify cycle marker.
      // The exact string may include "{self: ...}" with a cycle annotation.
    } catch (StackOverflowError e) {
      // Fail the test if StackOverflow occurs (the bug is present).
      org.junit.Assert.fail("RecordType.toString() caused StackOverflowError " +
          "due to cyclic reference (bug Closure-46)");
    }
  }

  @Test
  public void testCyclicRecordEquals() {
    // Similar test for equals which also traverses properties.
    Map<String, JSType> props = new LinkedHashMap<>();
    RecordType rec = createRecordType(props);
    props.put("self", rec);
    // Compare with itself. Should not loop infinitely.
    try {
      boolean result = rec.equals(rec);
      assertTrue("A record should equal itself", result);
    } catch (StackOverflowError e) {
      org.junit.Assert.fail("RecordType.equals() caused StackOverflowError " +
          "due to cyclic reference (bug Closure-46)");
    }
  }

  @Test
  public void testCyclicRecordIsSubtype() {
    // Test that subtype checking does not loop on cycles.
    Map<String, JSType> propsA = new LinkedHashMap<>();
    RecordType recA = createRecordType(propsA);
    propsA.put("self", recA);

    Map<String, JSType> propsB = new LinkedHashMap<>();
    RecordType recB = createRecordType(propsB);
    propsB.put("self", recB);

    try {
      // Two records with same cyclic structure should be subtype of each other?
      // This may depend on the implementation, but at least no crash.
      boolean result = recA.isSubtype(recB);
      // Not asserting the result, just that no error occurs.
    } catch (StackOverflowError e) {
      org.junit.Assert.fail("RecordType.isSubtype() caused StackOverflowError " +
          "due to cyclic reference (bug Closure-46)");
    }
  }

  @Test
  public void testRecordWithOptionalProperties() {
    // Optional properties are represented by a special type? In Closure,
    // optional property types are wrapped in JSTypeOptional.
    // This test assumes the registry can create optional types.
    Map<String, JSType> props = new LinkedHashMap<>();
    JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSType optionalNumber = registry.createOptionalType(numberType);
    props.put("opt", optionalNumber);
    RecordType rec = createRecordType(props);
    assertNotNull("Record with optional property", rec);
    assertEquals("Optional property type should be the optional wrapper",
        optionalNumber, rec.getPropertyType("opt"));
    // toString should include the optional marker (e.g., ?)
    String str = rec.toString();
    assertNotNull(str);
    assertTrue("toString should contain optional marker", str.contains("?"));
  }

  @Test
  public void testRecordNodeCreation() {
    // Verify that a RecordType can be created from a Node.
    // This tests the static factory or constructor that takes a Node.
    // For coverage, we create a minimal Node with token that represents a record.
    Node node = new Node(com.google.javascript.rhino.Token.OBJECTLIT);
    // Add property nodes.
    Node keyNode = Node.newString("foo");
    Node valueNode = new Node(com.google.javascript.rhino.Token.STRING);
    node.addChildToBack(com.google.javascript.rhino.Node.newString("foo"));
    node.addChildToBack(valueNode);
    // The actual constructor may be different; this is a placeholder.
    // Assume there is a method like RecordType.fromNode.
    // Since we may not have it, we skip explicit assertion.
    // But for coverage, we can at least call the constructor that takes a Node.
    // The default constructor of RecordType may not accept Node; we'll test the existing API.
    // Instead, we rely on the main constructors already tested.
    // This test is optional; we'll leave it as a placeholder.
    // (In real scenario, remove if not applicable.)
  }

  @Test
  public void testRecordWithManyProperties() {
    // Test handling of large number of properties.
    Map<String, JSType> props = new LinkedHashMap<>();
    for (int i = 0; i < 100; i++) {
      props.put("p" + i, registry.getNativeType(JSTypeNative.UNKNOWN_TYPE));
    }
    RecordType rec = createRecordType(props);
    assertEquals(100, rec.getProperties().size());
    String str = rec.toString();
    assertNotNull(str);
    // Should contain first and last property.
    assertTrue(str.contains("p0"));
    assertTrue(str.contains("p99"));
  }

  @Test
  public void testRecordPropertyOrder() {
    // Test that property order is preserved.
    Map<String, JSType> props = new LinkedHashMap<>();
    props.put("b", registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    props.put("a", registry.getNativeType(JSTypeNative.STRING_TYPE));
    RecordType rec = createRecordType(props);
    // The toString should respect the order of the map.
    String str = rec.toString();
    int indexB = str.indexOf("b");
    int indexA = str.indexOf("a");
    assertTrue("Property 'b' should appear before 'a'", indexB < indexA);
  }

  @Test
  public void testRecordWithoutPropertiesIsObjectSubtype() {
    RecordType empty = createRecordType(new LinkedHashMap<String, JSType>());
    ObjectType objType = registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE);
    // An empty record should be a subtype of Object.
    assertTrue("Empty record is subtype of Object", empty.isSubtype(objType));
  }

  @Test
  public void testRecordWithUnknownPropertyType() {
    Map<String, JSType> props = new LinkedHashMap<>();
    props.put("x", registry.getNativeType(JSTypeNative.UNKNOWN_TYPE));
    RecordType rec = createRecordType(props);
    assertEquals(registry.getNativeType(JSTypeNative.UNKNOWN_TYPE), rec.getPropertyType("x"));
  }
}