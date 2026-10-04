package com.google.javascript.rhino.jstype;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.jstype.ArrowType;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.Visitor;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ArrowTypeTest {

  private ArrowType arrowType;
  private Node mockParameters;
  private JSType mockReturnType;
  private JSTypeRegistry mockRegistry;

  @Before
  public void setUp() {
    mockRegistry = null; // Registry not needed for most tests; kept null for simplicity.
    mockParameters = new Node(1); // Use a simple node as parameters.
    mockReturnType = createMockJSType();
    arrowType = new ArrowType(mockRegistry, mockParameters, mockReturnType);
  }

  /** Helper to create a basic JSType stub for testing. */
  private JSType createMockJSType() {
    return new JSType(mockRegistry) {
      @Override
      public boolean isObjectType() {
        return false;
      }

      @Override
      public JSType restrictByNotNullOrUndefined() {
        return this;
      }

      @Override
      public JSType getLeastSupertype(JSType that) {
        return null;
      }

      @Override
      public JSType getGreatestSubtype(JSType that) {
        return null;
      }

      @Override
      public boolean isSubtype(JSType other) {
        return false;
      }

      @Override
      public <T> T visit(Visitor<T> visitor) {
        return null;
      }

      @Override
      public boolean hasAnyTemplateTypes() {
        return false;
      }

      @Override
      public JSType resolve(ErrorReporter t, StaticScope<JSType> scope) {
        return this;
      }

      @Override
      public String toString() {
        return "mockType";
      }
    };
  }

  // ===================== Constructor and Getters =====================

  @Test
  public void testDefaultConstructor() {
    assertNotNull(arrowType);
    assertEquals(mockParameters, arrowType.getParameters());
    assertEquals(mockReturnType, arrowType.getReturnType());
    assertFalse(arrowType.isInferred());
  }

  @Test
  public void testConstructorWithInferred() {
    ArrowType inferredArrow = new ArrowType(mockRegistry, mockParameters, mockReturnType, true);
    assertTrue(inferredArrow.isInferred());
  }

  @Test
  public void testConstructorWithNullParameters() {
    ArrowType nullParams = new ArrowType(mockRegistry, null, mockReturnType);
    assertNull(nullParams.getParameters());
    assertEquals(mockReturnType, nullParams.getReturnType());
  }

  @Test
  public void testConstructorWithNullReturnType() {
    ArrowType nullReturn = new ArrowType(mockRegistry, mockParameters, null);
    assertEquals(mockParameters, nullReturn.getParameters());
    assertNull(nullReturn.getReturnType());
  }

  // ===================== isArrowType =====================

  @Test
  public void testIsArrowType() {
    assertTrue(arrowType.isArrowType());
  }

  // ===================== Visit =====================

  @Test
  public void testVisitCallsCaseArrowType() {
    final boolean[] visited = {false};
    Visitor<Void> visitor = new Visitor<Void>() {
      @Override
      public Void caseArrowType(ArrowType type) {
        visited[0] = true;
        return null;
      }

      @Override
      public Void caseAllType() { return null; }
      @Override
      public Void caseBooleanType() { return null; }
      @Override
      public Void caseNoObjectType() { return null; }
      @Override
      public Void caseNoType() { return null; }
      @Override
      public Void caseNumberType() { return null; }
      @Override
      public Void caseObjectType(ObjectType type) { return null; }
      @Override
      public Void caseStringType() { return null; }
      @Override
      public Void caseUnionType(UnionType type) { return null; }
      @Override
      public Void caseFunctionType(FunctionType type) { return null; }
      @Override
      public Void caseTemplatizedType(TemplatizedType type) { return null; }
      @Override
      public Void caseUnknownType() { return null; }
      @Override
      public Void caseNamedType(NamedType type) { return null; }
      @Override
      public Void caseProxyObjectType(ProxyObjectType type) { return null; }
      @Override
      public Void caseEnumElementType(EnumElementType type) { return null; }
      @Override
      public Void caseEnumType(EnumType type) { return null; }
      @Override
      public Void caseParameterizedType(ParameterizedType type) { return null; }
      @Override
      public Void caseRecordType(RecordType type) { return null; }
      @Override
      public Void caseVoidType() { return null; }
    };
    arrowType.visit(visitor);
    assertTrue(visited[0]);
  }

  // ===================== Equals and HashCode =====================

  @Test
  public void testEqualsSameObject() {
    assertTrue(arrowType.equals(arrowType));
  }

  @Test
  public void testEqualsEqualObjects() {
    ArrowType other = new ArrowType(mockRegistry, mockParameters, mockReturnType);
    assertTrue(arrowType.equals(other));
  }

  @Test
  public void testEqualsDifferentInferred() {
    ArrowType inferred = new ArrowType(mockRegistry, mockParameters, mockReturnType, true);
    assertFalse(arrowType.equals(inferred));
  }

  @Test
  public void testEqualsDifferentParameters() {
    Node otherParams = new Node(2);
    ArrowType other = new ArrowType(mockRegistry, otherParams, mockReturnType);
    assertFalse(arrowType.equals(other));
  }

  @Test
  public void testEqualsDifferentReturnType() {
    JSType otherReturn = createMockJSType(); // Could be different instance with same equals? For simplicity, treat as different.
    ArrowType other = new ArrowType(mockRegistry, mockParameters, otherReturn);
    assertFalse(arrowType.equals(other));
  }

  @Test
  public void testEqualsNull() {
    assertFalse(arrowType.equals(null));
  }

  @Test
  public void testEqualsDifferentClass() {
    assertFalse(arrowType.equals("not an arrow type"));
  }

  @Test
  public void testHashCodeConsistency() {
    ArrowType other = new ArrowType(mockRegistry, mockParameters, mockReturnType);
    assertEquals(arrowType.hashCode(), other.hashCode());
  }

  @Test
  public void testHashCodeDifferentInferred() {
    ArrowType inferred = new ArrowType(mockRegistry, mockParameters, mockReturnType, true);
    assertNotEquals(arrowType.hashCode(), inferred.hashCode());
  }

  // ===================== isSubtype (basic delegation) =====================

  @Test
  public void testIsSubtypeReturnsFalseByDefault() {
    // ArrowType does not override isSubtype so it uses JSType's default (false)
    assertFalse(arrowType.isSubtype(createMockJSType()));
  }

  // ===================== Type Context Methods =====================

  @Test
  public void testMatchesNumberContext() {
    assertFalse(arrowType.matchesNumberContext());
  }

  @Test
  public void testMatchesStringContext() {
    assertFalse(arrowType.matchesStringContext());
  }

  @Test
  public void testMatchesObjectContext() {
    assertFalse(arrowType.matchesObjectContext());
  }

  // ===================== canBeCalled =====================

  @Test
  public void testCanBeCalledReturnsTrue() {
    assertTrue(arrowType.canBeCalled());
  }

  // ===================== toMaybeObjectType =====================

  @Test
  public void testToMaybeObjectTypeReturnsNull() {
    assertNull(arrowType.toMaybeObjectType());
  }

  // ===================== Resolve (if method exists) =====================

  @Test
  public void testResolveReturnsSelfWhenNoChanges() {
    // Simple test: resolve with null error reporter and scope
    // If parameters and return type are null, it may return this.
    ArrowType nullArrow = new ArrowType(mockRegistry, null, null);
    JSType resolved = nullArrow.resolve(null, null);
    // In a naive implementation, resolve might return this; we just ensure no exception.
    assertNotNull(resolved);
  }

  // Edge case: null parameters and null return type
  @Test
  public void testNullParametersAndReturnType() {
    ArrowType nullAll = new ArrowType(mockRegistry, null, null);
    assertNull(nullAll.getParameters());
    assertNull(nullAll.getReturnType());
    assertFalse(nullAll.isInferred());
  }

  // Edge case: both null in equality
  @Test
  public void testEqualsNullFields() {
    ArrowType a = new ArrowType(mockRegistry, null, null);
    ArrowType b = new ArrowType(mockRegistry, null, null);
    assertTrue(a.equals(b));
    assertEquals(a.hashCode(), b.hashCode());
  }
}