package com.google.gson.internal;

import org.junit.Test;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.*;

public class $Gson$TypesTest {

  // Dummy interface and classes for testing type resolution
  private interface A<T> {}
  private interface B<T> extends A<T> {}
  private static class C implements B<String> {}

  @Test
  public void testNewParameterizedTypeWithOwner() {
    Parameterized Type type = $Gson$Types.newParameterizedTypeWithOwner(null, Map.class, String.class, String.class);
    assertNotNull(type);
    assertEquals(Map.class, type.getRawType());
    assertNull(type.getOwnerType());
    assertEquals(2, type.getActualTypeArguments().length);
    assertEquals(String.class, type.getActualTypeArguments()[0]);
    assertEquals(String.class, type.getActualTypeArguments()[1]);

    // Test with owner type
    ParameterizedType typeWithOwner = $Gson$Types.newParameterizedTypeWithOwner(Map.class, Map.Entry.class, String.class, String.class);
    assertNotNull(typeWithOwner);
    assertEquals(Map.Entry.class, typeWithOwner.getRawType());
    assertEquals(Map.class, typeWithOwner.getOwnerType());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNewParameterizedTypeWithOwnerNullRawType() {
    $Gson$Types.newParameterizedTypeWithOwner(null, null, String.class);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNewParameterizedTypeWithOwnerInvalidOwner() {
    // Owner type mismatch or invalid context could throw IAE or similar
    $Gson$Types.newParameterizedTypeWithOwner(String.class, Map.Entry.class, String.class, String.class);
  }

  @Test
  public void testNewArrayType() {
    GenericArrayType arrayType = $Gson$Types.newArrayType(String.class);
    assertNotNull(arrayType);
    assertEquals(String.class, arrayType.getGenericComponentType());
  }

  @Test(expected = NullPointerException.class)
  public void testNewArrayTypeNull() {
    $Gson$Types.newArrayType(null);
  }

  @Test
  public void testWildcardTypeWithUpperBound() {
    WildcardType wildcard = $Gson$Types.subtypeOf(String.class);
    assertNotNull(wildcard);
    assertEquals(1, wildcard.getUpperBounds().length);
    assertEquals(String.class, wildcard.getUpperBounds()[0]);
    assertEquals(0, wildcard.getLowerBounds().length);
  }

  @Test
  public void testWildcardTypeWithLowerBound() {
    WildcardType wildcard = $Gson$Types.supertypeOf(String.class);
    assertNotNull(wildcard);
    assertEquals(1, wildcard.getLowerBounds().length);
    assertEquals(String.class, wildcard.getLowerBounds()[0]);
    assertEquals(1, wildcard.getUpperBounds().length);
    assertEquals(Object.class, wildcard.getUpperBounds()[0]);
  }

  @Test
  public void testCanonicalize() {
    // Class
    Type canonicalClass = $Gson$Types.canonicalize(String.class);
    assertEquals(String.class, canonicalClass);

    // ParameterizedType
    ParameterizedType pt = $Gson$Types.newParameterizedTypeWithOwner(null, Map.class, String.class, String.class);
    Type canonicalPt = $Gson$Types.canonicalize(pt);
    assertEquals(pt, canonicalPt);

    // GenericArrayType
    GenericArrayType gat = $Gson$Types.newArrayType(String.class);
    Type canonicalGat = $Gson$Types.canonicalize(gat);
    assertEquals(gat, canonicalGat);

    // WildcardType (subtype)
    WildcardType wtSub = $Gson$Types.subtypeOf(String.class);
    Type canonicalWtSub = $Gson$Types.canonicalize(wtSub);
    assertEquals(wtSub, canonicalWtSub);

    // WildcardType (super)
    WildcardType wtSuper = $Gson$Types.supertypeOf(String.class);
    Type canonicalWtSuper = $Gson$Types.canonicalize(wtSuper);
    assertEquals(wtSuper, canonicalWtSuper);

    // Null
    assertNull($Gson$Types.canonicalize(null));
  }

  @Test
  public void testGetRawType() {
    assertEquals(String.class, $Gson$Types.getRawType(String.class));
    
    ParameterizedType pt = $Gson$Types.newParameterizedTypeWithOwner(null, Map.class, String.class, String.class);
    assertEquals(Map.class, $Gson$Types.getRawType(pt));

    GenericArrayType gat = $Gson$Types.newArrayType(String.class);
    assertEquals(String[].class, $Gson$Types.getRawType(gat));

    WildcardType wt = $Gson$Types.subtypeOf(String.class);
    assertEquals(String.class, $Gson$Types.getRawType(wt));

    // Object.class for null or unresolvable
    assertEquals(Object.class, $Gson$Types.getRawType(null));
  }

  @Test
  public void testEqualsAndHashCode() {
    Type t1 = String.class;
    Type t2 = String.class;
    assertTrue($Gson$Types.equals(t1, t2));

    assertFalse($Gson$Types.equals(t1, null));
    assertFalse($Gson$Types.equals(null, t1));
    assertTrue($Gson$Types.equals(null, null));

    ParameterizedType pt1 = $Gson$Types.newParameterizedTypeWithOwner(null, Map.class, String.class, String.class);
    ParameterizedType pt2 = $Gson$Types.newParameterizedTypeWithOwner(null, Map.class, String.class, String.class);
    assertTrue($Gson$Types.equals(pt1, pt2));

    GenericArrayType gat1 = $Gson$Types.newArrayType(String.class);
    GenericArrayType gat2 = $Gson$Types.newArrayType(String.class);
    assertTrue($Gson$Types.equals(gat1, gat2));

    WildcardType wt1 = $Gson$Types.subtypeOf(String.class);
    WildcardType wt2 = $Gson$Types.subtypeOf(String.class);
    assertTrue($Gson$Types.equals(wt1, wt2));

    // Test hashcode helper
    assertNotEquals(0, $Gson$Types.hashCodeOrZero(t1));
    assertEquals(0, $Gson$Types.hashCodeOrZero(null));
  }

  @Test
  public void testTypeToString() {
    assertNotNull($Gson$Types.typeToString(String.class));
    assertNotNull($Gson$Types.typeToString($Gson$Types.newArrayType(String.class)));
    assertNotNull($Gson$Types.typeToString($Gson$Types.newParameterizedTypeWithOwner(null, Map.class, String.class, String.class)));
    assertNotNull($Gson$Types.typeToString($Gson$Types.subtypeOf(String.class)));
    assertNotNull($Gson$Types.typeToString($Gson$Types.supertypeOf(String.class)));
  }

  @Test
  public void testGetGenericSupertype() {
    Type supertype = $Gson$Types.getGenericSupertype(C.class, B.class, A.class);
    assertNotNull(supertype);
  }

  @Test
  public void testGetArrayComponentType() {
    assertEquals(String.class, $Gson$Types.getArrayComponentType(String[].class));
    assertEquals(String.class, $Gson$Types.getArrayComponentType($Gson$Types.newArrayType(String.class)));
  }

  @Test
  public void testGetCollectionElementType() {
    Type elementType = $Gson$Types.getCollectionElementType(Properties.class, Map.class);
    assertNotNull(elementType);
  }

  @Test
  public void testResolve() {
    Type resolved = $Gson$Types.resolve(String.class, String.class, String.class);
    assertEquals(String.class, resolved);
  }
}