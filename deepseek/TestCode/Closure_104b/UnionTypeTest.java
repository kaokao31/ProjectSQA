package com.google.javascript.rhino.jstype;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.google.javascript.rhino.ErrorReporter;
import java.util.HashSet;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;

public class UnionTypeTest {

  private JSTypeRegistry registry;
  private JSType number;
  private JSType string;
  private JSType bool;
  private JSType object;
  private JSType array;
  private JSType nullType;
  private JSType unknown;
  private JSType noType;

  @Before
  public void setUp() {
    registry = new JSTypeRegistry(new TestErrorReporter());
    number = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    string = registry.getNativeType(JSTypeNative.STRING_TYPE);
    bool = registry.getNativeType(JSTypeNative.BOOLEAN_TYPE);
    object = registry.getNativeType(JSTypeNative.OBJECT_TYPE);
    array = registry.getNativeType(JSTypeNative.ARRAY_TYPE);
    nullType = registry.getNativeType(JSTypeNative.NULL_TYPE);
    unknown = registry.getNativeType(JSTypeNative.UNKNOWN_TYPE);
    noType = registry.getNativeType(JSTypeNative.NO_TYPE);
  }

  private JSType union(JSType... types) {
    return registry.createUnionType(types);
  }

  @Test
  public void testIsUnionType() {
    assertTrue(union(number, string).isUnionType());
    assertFalse(number.isUnionType());
  }

  @Test
  public void testGetAlternates() {
    UnionType type = (UnionType) union(number, string);
    Set<JSType> alternates = new HashSet<JSType>();
    for (JSType alternate : type.getAlternates()) {
      alternates.add(alternate);
    }
    assertEquals(2, alternates.size());
    assertTrue(alternates.contains(number));
    assertTrue(alternates.contains(string));
  }

  @Test
  public void testEqualsAndHashCode() {
    JSType first = union(number, string);
    JSType second = union(string, number);
    assertTrue(first.equals(second));
    assertEquals(first.hashCode(), second.hashCode());
    assertFalse(first.equals(number));
  }

  @Test
  public void testIsSubtype() {
    JSType numOrString = union(number, string);
    assertTrue(numOrString.isSubtype(numOrString));
    assertTrue(numOrString.isSubtype(union(number, string, bool)));
    assertTrue(numOrString.isSubtype(unknown));
    assertFalse(numOrString.isSubtype(number));
    assertFalse(numOrString.isSubtype(union(number, bool)));
  }

  @Test
  public void testLeastSupertypeWithAlternate() {
    JSType numOrString = union(number, string);
    JSType result = numOrString.getLeastSupertype(number);
    assertTrue("Expected union, got " + result, result.isUnionType());
    assertEquals(numOrString, result);
  }

  @Test
  public void testLeastSupertypeWithAnotherAlternate() {
    JSType numOrString = union(number, string);
    JSType result = numOrString.getLeastSupertype(string);
    assertTrue("Expected union, got " + result, result.isUnionType());
    assertEquals(numOrString, result);
  }

  @Test
  public void testLeastSupertypeWithUnrelatedType() {
    JSType result = union(number, string).getLeastSupertype(bool);
    assertTrue(result.isUnionType());
    assertEquals(union(number, string, bool), result);
  }

  @Test
  public void testLeastSupertypeWithSupertype() {
    assertSame(object, union(number, string).getLeastSupertype(object));
  }

  @Test
  public void testLeastSupertypeWithUnion() {
    JSType result = union(number, string).getLeastSupertype(union(number, bool));
    assertTrue(result.isUnionType());
    assertEquals(union(number, string, bool), result);
  }

  @Test
  public void testGreatestSubtypeWithAlternate() {
    assertSame(number, union(number, string).getGreatestSubtype(number));
  }

  @Test
  public void testGreatestSubtypeWithUnion() {
    assertSame(number, union(number, string).getGreatestSubtype(union(number, bool)));
  }

  @Test
  public void testGreatestSubtypeWithDisjointType() {
    assertSame(noType, union(number, string).getGreatestSubtype(bool));
  }

  @Test
  public void testMeetWithAlternate() {
    assertSame(number, union(number, string).meet(number));
  }

  @Test
  public void testMeetWithUnion() {
    assertSame(number, union(number, string).meet(union(number, bool)));
  }

  @Test
  public void testMeetWithDisjointType() {
    assertSame(noType, union(number, string).meet(bool));
  }

  @Test
  public void testIsNullable() {
    assertTrue(union(number, nullType).isNullable());
    assertFalse(union(number, string).isNullable());
  }

  @Test
  public void testIsObject() {
    assertTrue(union(object, array).isObject());
    assertFalse(union(number, string).isObject());
  }

  @Test
  public void testRestrictByNotNull() {
    JSType result = union(number, nullType).restrictByNotNull();
    assertEquals(number, result);
  }

  @Test
  public void testRestrictByNotNullPreservesOtherAlternates() {
    JSType result = union(number, string, nullType).restrictByNotNull();
    assertEquals(union(number, string), result);
  }

  private static class TestErrorReporter implements ErrorReporter {
    @Override
    public void error(String message, String sourceName, int line, int lineOffset) {
      // No-op test reporter.
    }

    @Override
    public void warning(String message, String sourceName, int line, int lineOffset) {
      // No-op test reporter.
    }
  }
}